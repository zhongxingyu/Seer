 package net.floodlightcontroller.interdomainforwarding;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.openflow.protocol.OFFlowMod;
 import org.openflow.protocol.OFMatch;
 import org.openflow.protocol.OFPacketIn;
 import org.openflow.protocol.OFPacketOut;
 import org.openflow.protocol.OFType;
 import org.openflow.protocol.action.OFAction;
 import org.openflow.protocol.action.OFActionDataLayerDestination;
 import org.openflow.protocol.action.OFActionOutput;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import net.floodlightcontroller.bgproute.IBgpRouteService;
 import net.floodlightcontroller.bgproute.Rib;
 import net.floodlightcontroller.core.FloodlightContext;
 import net.floodlightcontroller.core.IFloodlightProviderService;
 import net.floodlightcontroller.core.IOFSwitch;
 import net.floodlightcontroller.core.module.FloodlightModuleContext;
 import net.floodlightcontroller.core.module.FloodlightModuleException;
 import net.floodlightcontroller.core.module.IFloodlightModule;
 import net.floodlightcontroller.devicemanager.IDevice;
 import net.floodlightcontroller.devicemanager.IDeviceService;
 import net.floodlightcontroller.forwarding.Forwarding;
 import net.floodlightcontroller.packet.ARP;
 import net.floodlightcontroller.packet.Ethernet;
 import net.floodlightcontroller.packet.IPacket;
 import net.floodlightcontroller.packet.IPv4;
 import net.floodlightcontroller.routing.IRoutingDecision;
 import net.floodlightcontroller.routing.Route;
 import net.floodlightcontroller.topology.NodePortTuple;
 import net.floodlightcontroller.util.MACAddress;
 
 public class InterDomainForwarding extends Forwarding implements
         IFloodlightModule {
     protected static Logger log = LoggerFactory
             .getLogger(InterDomainForwarding.class);
 
     protected static IBgpRouteService bgpRoute;
     // proxyArp MAC address - can replace with other values in future
     public final String GW_PROXY_ARP_MACADDRESS = "12:34:56:78:90:12";
     protected MACAddress proxyArpAddress = MACAddress.valueOf(GW_PROXY_ARP_MACADDRESS);
 
 
     protected static ArrayList<Integer> proxyGwIp = new ArrayList<Integer>();
     protected static ArrayList<Integer> bgpIncomingGwIp = new ArrayList<Integer>();
     protected static ArrayList<Integer> localSubnet = new ArrayList<Integer>();
     protected static ArrayList<Integer> localSubnetMaskBits = new ArrayList<Integer>();
 
     protected Map<Integer, byte[]> gwIPtoMac;
 
     @Override
     public void init(FloodlightModuleContext context)
             throws FloodlightModuleException {
 
         bgpRoute = context.getServiceImpl(IBgpRouteService.class);
         gwIPtoMac = new HashMap<Integer, byte[]>();
 
         // read our config options
         Map<String, String> configOptions = context.getConfigParams(this);
 
         String proxyGwIpString = configOptions.get("proxyGateway");
         if (proxyGwIpString != null) {
             String[] proxyGwIpRead = proxyGwIpString.split("[/, ]+");
             for (int i = 0; i < proxyGwIpRead.length; i++) {
                 log.debug("add proxy gateway {}", proxyGwIpRead[i]);
                 proxyGwIp.add(IPv4.toIPv4Address(proxyGwIpRead[i]));
             }
         }
 
         String bgpIncomingGwIpString = configOptions.get("bgpIncomingGateway");
         if (bgpIncomingGwIpString != null) {
             String[] bgpIncomingGwIpRead = bgpIncomingGwIpString.split("[/, ]+");
             for (int i = 0; i < bgpIncomingGwIpRead.length; i++) {
                 log.debug("add bgpIncoming gateway {}", bgpIncomingGwIpRead[i]);
                 bgpIncomingGwIp.add(IPv4.toIPv4Address(bgpIncomingGwIpRead[i]));
             }
         }
 
         String subnet = configOptions.get("localSubnet");
         if (subnet != null) {
             String[] fields = subnet.split("[/, ]+");
             int addresses = fields.length / 2;
             for (int i = 0; i < addresses; i++) {
                 localSubnet.add(IPv4.toIPv4Address(fields[2 * i]));
                 localSubnetMaskBits.add(Integer.parseInt(fields[2 * i + 1]));
                 log.debug("add local subnet {}/{}",
                         IPv4.fromIPv4Address(localSubnet.get(i)),
                         localSubnetMaskBits.get(i));
             }
         }
 
         super.init(context);
     }
 
     @Override
     public Command processPacketInMessage(IOFSwitch sw, OFPacketIn pi,
             IRoutingDecision decision, FloodlightContext cntx) {
         Ethernet eth = IFloodlightProviderService.bcStore.get(cntx,
                 IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
 
         if (eth.isBroadcast() || eth.isMulticast()) {
 
             // InterDomainForwarding - check ARP target gateway
             if (eth.getEtherType() == Ethernet.TYPE_ARP) {
                 // retrieve arp to determine target IP address
                 ARP arpRequest = (ARP) eth.getPayload();
 
                 // If arping for proxy gateway configured in
                 // interdomain.properties
                 // and run with -cf interdomain.properties
                 // ==> isExternal==true
                 // ==> respond with proxy arp; otherwise, flood for now
 
                 // TODO: add restAPI for user to configure:
                 // 1) proxyGwIp
                 // TODO: remove localSubnet from properties
 
                 int targetProtocolAddress = IPv4.toIPv4Address(arpRequest
                         .getTargetProtocolAddress());
                 int senderProtocolAddress = IPv4.toIPv4Address(arpRequest
                         .getSenderProtocolAddress());
 
                 boolean isOutgoing = false;
 
                 if (proxyGwIp.contains(targetProtocolAddress) && 
                         !bgpIncomingGwIp.contains(senderProtocolAddress)) {
                         isOutgoing = true;
                         log.debug("ARP target address {} is a Gw",
                                 IPv4.fromIPv4Address(targetProtocolAddress));
                 }
                 
                 if (isOutgoing) {
                     doProxyArp(sw, pi, cntx);
 
                     // arp pushed already, complete forwarding
                     return Command.CONTINUE;
                 }
             }
 
             // For now we treat multicast as broadcast
             doFlood(sw, pi, cntx);
         } else {
             MACAddress dstMac = MACAddress.valueOf(eth
                     .getDestinationMACAddress());
 
             log.debug("dst is " + dstMac + " gw is " + proxyArpAddress);
 
            if (proxyArpAddress.toLong() == dstMac.toLong()) {
                 cntx = prepInterDomainForwarding(cntx);
             }
 
             doForwardFlow(sw, pi, cntx, false);
         }
 
         return Command.CONTINUE;
     }
 
     // doProxyArp called when destination is external to SDN network
     protected void doProxyArp(IOFSwitch sw, OFPacketIn pi,
             FloodlightContext cntx) {
         log.debug("InterDomainForwarding: doProxyArp");
 
         Ethernet eth = IFloodlightProviderService.bcStore.get(cntx,
                 IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
 
         // retrieve original arp to determine host configured gw IP address
         ARP arpRequest = (ARP) eth.getPayload();
 
         // generate proxy ARP reply
         byte[] proxyArpReply = MACAddress.valueOf(GW_PROXY_ARP_MACADDRESS)
                 .toBytes();
 
         IPacket arpReply = new Ethernet()
                 .setSourceMACAddress(proxyArpReply)
                 .setDestinationMACAddress(eth.getSourceMACAddress())
                 .setEtherType(Ethernet.TYPE_ARP)
                 .setVlanID(eth.getVlanID())
                 .setPriorityCode(eth.getPriorityCode())
                 .setPayload(
                         new ARP()
                                 .setHardwareType(ARP.HW_TYPE_ETHERNET)
                                 .setProtocolType(ARP.PROTO_TYPE_IP)
                                 .setHardwareAddressLength((byte) 6)
                                 .setProtocolAddressLength((byte) 4)
                                 .setOpCode(ARP.OP_REPLY)
                                 .setSenderHardwareAddress(proxyArpReply)
                                 .setSenderProtocolAddress(
                                         arpRequest.getTargetProtocolAddress())
                                 .setTargetHardwareAddress(
                                         eth.getSourceMACAddress())
                                 .setTargetProtocolAddress(
                                         arpRequest.getSenderProtocolAddress()));
 
         // TODO: generate empty flowmod to drop switch buffered arp request (see
         // VirtualNetworkingFilter example
 
         // push ARP out
         pushPacket(arpReply, sw, OFPacketOut.BUFFER_ID_NONE, (short) 4,
                 pi.getInPort(), cntx, true);
         log.debug("proxy ARP reply (unicast) pushed");
 
         return;
     }
 
     protected FloodlightContext prepInterDomainForwarding(FloodlightContext cntx) {
 
         log.debug("InterDomainForwarding applied - gateway-bound traffic");
 
         Ethernet eth = IFloodlightProviderService.bcStore.get(cntx,
                 IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
 
         IPacket pkt = eth.getPayload();
 
         if (pkt instanceof IPv4) {
             IPv4 ipPkt = (IPv4) pkt;
             // Here query BgpRoute for the right gateway IP
             log.debug("prep destination {}",
                     IPv4.fromIPv4Address(ipPkt.getDestinationAddress()));
 
             byte[] targetIPAddressByte = null;
             if (bgpRoute != null)
                 targetIPAddressByte = bgpRoute.lookupRib(
                         IPv4.toIPv4AddressBytes(ipPkt.getDestinationAddress()))
                         .getNextHop().getAddress();
             else
                 log.debug("prep destination bgpRoute null");
 
             log.debug("prep nexthop {}", IPv4.fromIPv4Address(IPv4.toIPv4Address(targetIPAddressByte)));
 
              if (targetIPAddressByte == null) return cntx; // no next hop info - give up
 
             Integer targetIPAddress = IPv4.toIPv4Address(targetIPAddressByte);
 
             // Below searches for gateway device using IDeviceService
 
             // retrieve all known devices
             Collection<? extends IDevice> allDevices = deviceManager
                     .getAllDevices();
 
             // look for device with chosen gateway's IP address
             IDevice targetDevice = null;
 
             for (IDevice d : allDevices) {
                 for (int i = 0; i < d.getIPv4Addresses().length; i++) {
                     if (targetIPAddress.equals(d.getIPv4Addresses()[i])) {
                         targetDevice = d;
                         break;
                     }
                 }
             }
 
             // gw device found
             if (targetDevice != null) {
                 // overwrite dst device info in cntx
                 IDeviceService.fcStore.put(cntx,
                         IDeviceService.CONTEXT_DST_DEVICE, targetDevice);
                 log.debug("Interdomain forwarding: assigned gw {} found",
                         IPv4.fromIPv4Address(targetIPAddress));
             } else {
                 // if no known devices match the BgpRoute suggested gateway
                 // IP, this is an error in BgpRoute to be handled
                 log.debug(
                         "Interdomain forwarding: assigned gw {} not known (error condition)",
                         IPv4.fromIPv4Address(targetIPAddress));
             }
         } else {
             // non-IP packets get here - not supported
             log.debug("non-IP packet in prepInterDomainForwarding");
         }
 
         return cntx;
     }
 
     /**
      * Push routes for interdomain forwarding
      * 
      * @param route
      *            Route to push
      * @param match
      *            OpenFlow fields to match on
      * @param wildcard_hints
      *            wildcard hints
      * @param bufferId
      *            BufferId of the original PacketIn
      * @param packetIn
      *            original PacketIn
      * @param pinSwitch
      *            switch that produced PacketIn
      * @param cookie
      *            The cookie to set in each flow_mod
      * @param cntx
      *            The floodlight context
      * @param reqeustFlowRemovedNotifn
      *            if set to true then the switch would send a flow mod removal
      *            notification when the flow mod expires
      * @param doFlush
      *            if set to true then the flow mod would be immediately written
      *            to the switch
      * @param flowModCommand
      *            flow mod. command to use, e.g. OFFlowMod.OFPFC_ADD,
      *            OFFlowMod.OFPFC_MODIFY etc.
      */
 
     @Override
     public boolean pushRoute(Route route, OFMatch match,
             Integer wildcard_hints, OFPacketIn pi, long pinSwitch, long cookie,
             FloodlightContext cntx, boolean reqeustFlowRemovedNotifn,
             boolean doFlush, short flowModCommand) {
 
         log.debug("interdomain push route");
 
         boolean srcSwitchIncluded = false;
 
         Ethernet eth = IFloodlightProviderService.bcStore.get(cntx,
                 IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
 
         // get packet's destination IP address and find out matching subnet
         IPacket pkt = eth.getPayload();
 
         byte[] pktDstMac = eth.getDestinationMACAddress();
         int pktDstIp = 0;
         
         boolean matchIP = false;
         boolean matchEthertype = false;
         boolean rewriteNeeded = false;
         boolean subnetWildcard = false;
 
         if (pkt instanceof IPv4) {
             IPv4 ip_pkt = (IPv4) eth.getPayload();
             pktDstIp = ip_pkt.getDestinationAddress();
             matchIP = true;
             
             log.debug("before check subnetWildcard dst mac {} proxyArp {}", MACAddress.valueOf(pktDstMac), proxyArpAddress);
             
             if (pktDstMac.equals(proxyArpAddress.toBytes())) {
                 log.debug("rewrite and subnetWildcard needed");
                 rewriteNeeded = true;
                 subnetWildcard = true;
             }
 
             updateGwBinding();
 
             if (!rewriteNeeded) {
                 for (int i = 0; i < bgpIncomingGwIp.size(); i++) {
                     byte[] bgpIncomingGwMac = gwIPtoMac
                             .get(bgpIncomingGwIp.get(i));
                     log.info("ip {} has mac {}", IPv4.fromIPv4Address(bgpIncomingGwIp.get(i)),
                             bgpIncomingGwMac);
 
                     // Going to bgp incoming gw
                     if (bgpIncomingGwMac != null
                            && MACAddress.valueOf(pktDstMac).toLong() == MACAddress.valueOf(bgpIncomingGwMac).toLong()) {
                         log.info("PAYLOAD ip {} has mac {}", pktDstIp, pktDstMac);
                         
                         // but the real destination is not gw
                         if (pktDstIp != bgpIncomingGwIp.get(i)) {
                             rewriteNeeded = true;
 
                             
                          // look for device with dst host IP address
                             IDevice targetDevice = null;
                             
                          // retrieve all known devices
                             Collection<? extends IDevice> allDevices = deviceManager
                                     .getAllDevices();
                             
                             for (IDevice d : allDevices) {
                                 for (int j = 0; j < d.getIPv4Addresses().length; j++) {
                                     if (pktDstIp == d.getIPv4Addresses()[i]) {
                                         targetDevice = d;
                                         break;
                                     }
                                 }
                             }
 
                             // device found
                             if (targetDevice != null) {
                                 // overwrite dst device info in cntx
                                 IDeviceService.fcStore.put(cntx,
                                         IDeviceService.CONTEXT_DST_DEVICE, targetDevice);
                                 log.debug("Interdomain forwarding: assigned gw {} found",
                                         IPv4.fromIPv4Address(pktDstIp));
                             } else {
                                 // if no known devices match the BgpRoute suggested gateway
                                 // IP, this is an error in BgpRoute to be handled
                                 log.debug(
                                         "Interdomain forwarding: dest host {} not in device list (error condition)",
                                         IPv4.fromIPv4Address(pktDstIp));
                             }
                         }
                         break;
                     }
                 }
             }
 
             
         } else if (pkt instanceof ARP) {
             ARP arp_pkt = (ARP) eth.getPayload();
             pktDstIp = IPv4.toIPv4Address(arp_pkt.getTargetProtocolAddress());
             
             log.info("sender {} target {}", IPv4.fromIPv4Address(IPv4
                     .toIPv4Address(arp_pkt.getSenderProtocolAddress())), IPv4
                     .fromIPv4Address(IPv4.toIPv4Address(arp_pkt
                             .getTargetProtocolAddress())));
             matchEthertype = true;
         }
 
         OFFlowMod fm = (OFFlowMod) floodlightProvider.getOFMessageFactory()
                 .getMessage(OFType.FLOW_MOD);
 
         // add initialized OUTPUT action
         OFActionOutput action = new OFActionOutput();
         action.setMaxLength((short) 0xffff);
         List<OFAction> actions = new ArrayList<OFAction>();
         actions.add(action);
 
         fm.setIdleTimeout((short) 5)
                 .setBufferId(OFPacketOut.BUFFER_ID_NONE)
                 .setCookie(cookie)
                 .setCommand(flowModCommand)
                 .setMatch(match)
                 .setActions(actions)
                 .setLengthU(
                         OFFlowMod.MINIMUM_LENGTH
                                 + OFActionOutput.MINIMUM_LENGTH);
 
        if (rewriteNeeded) {
            IDevice srcDevice = IDeviceService.fcStore.get(cntx,
                    IDeviceService.CONTEXT_SRC_DEVICE);
            IDevice targetDevice = IDeviceService.fcStore.get(cntx,
                    IDeviceService.CONTEXT_DST_DEVICE);
            route = routingEngine.getRoute(srcDevice.getAttachmentPoints()[0].getSwitchDPID(), targetDevice.getAttachmentPoints()[0].getSwitchDPID());
        }
        
         List<NodePortTuple> switchPortList = route.getPath();
 
         for (int indx = switchPortList.size() - 1; indx > 0; indx -= 2) {
             // indx and indx-1 will always have the same switch DPID.
             long switchDPID = switchPortList.get(indx).getNodeId();
             IOFSwitch sw = floodlightProvider.getSwitches().get(switchDPID);
             if (sw == null) {
                 if (log.isWarnEnabled()) {
                     log.warn("Unable to push route, switch at DPID {} "
                             + "not available", switchDPID);
                 }
                 return srcSwitchIncluded;
             }
 
             // set the default match.
             fm.setMatch(wildcard(match, sw, wildcard_hints));
 
             short outPort = switchPortList.get(indx).getPortId();
             short inPort = switchPortList.get(indx - 1).getPortId();
             
             // set input and output ports on the switch
             fm.getMatch().setInputPort(inPort);
             ((OFActionOutput) fm.getActions().get(0)).setPort(outPort);
             
 
 
             // InterDomainForwarding specific handling starts here
             // retrieve cntx to set rewrite+forward action for 1st switch and
             // forward action for all other switches
             IDevice dstDevice = IDeviceService.fcStore.get(cntx,
                     IDeviceService.CONTEXT_DST_DEVICE);
 
             if (indx == 1) {
 
                 if ((reqeustFlowRemovedNotifn)
                         && (match.getDataLayerType() != Ethernet.TYPE_ARP)) {
                     fm.setFlags(OFFlowMod.OFPFF_SEND_FLOW_REM);
 //                    match.setWildcards(fm.getMatch().getWildcards());
                 }
 
                 List<OFAction> newActions = fm.getActions();
 
                 if (matchEthertype) {
                     log.info("match Ethertype");
 
                     // if ARP reply, match dl-type==ARP
                     fm.getMatch().setDataLayerType(Ethernet.TYPE_ARP);
 
                     fm.getMatch().setWildcards(
                             fm.getMatch().getWildcards() & ~OFMatch.OFPFW_DL_TYPE 
                             | OFMatch.OFPFW_NW_SRC_ALL
                             | OFMatch.OFPFW_NW_DST_ALL
                             | OFMatch.OFPFW_NW_PROTO);
                 } else if (matchIP) {
                     // add rewrite action to current output action
                     if (rewriteNeeded) {
                         // create rewrite action with chosen gw MAC address
                         OFAction rewriteAction = new OFActionDataLayerDestination(
                                 MACAddress.valueOf(dstDevice.getMACAddress())
                                 .toBytes());
 
                         newActions.add(0, rewriteAction);
                         fm.setActions(newActions);
                         fm.setLengthU(fm.getLengthU()
                                     + rewriteAction.getLengthU());
                     }
                         
                     int wildcard_bits = 0;
                     int matched_ip = 0;
 
                     if (subnetWildcard) {
                         Rib foundRib = bgpRoute.lookupRib(IPv4
                                 .toIPv4AddressBytes(pktDstIp));
                         wildcard_bits = 32 - foundRib.getMasklen();
                     }
                     
                     matched_ip = (pktDstIp >> wildcard_bits) << wildcard_bits;
 
                     if (matched_ip == 0)
                         log.debug("no matching local subnet found - cannot set correct ip_prefix wildcard");
                     else {
                         // set flow mod dst IP address and wildcard
                         fm.getMatch().setDataLayerType(Ethernet.TYPE_IPv4);
                         fm.getMatch().setNetworkDestination(matched_ip);
 
                         fm.getMatch().setWildcards(
                                 (fm.getMatch().getWildcards()
                                         & ~OFMatch.OFPFW_NW_DST_ALL & ~OFMatch.OFPFW_DL_TYPE)
                                         | (wildcard_bits << OFMatch.OFPFW_NW_DST_SHIFT)
                                         | OFMatch.OFPFW_NW_SRC_ALL
                                         | OFMatch.OFPFW_NW_PROTO);
                     }
                 } 
             } else {
                 log.debug("matching neighther ARP nor IP");
                 // update match for output action
                 fm.getMatch().setDataLayerDestination(
                         MACAddress.valueOf(dstDevice.getMACAddress())
                         .toBytes());
             }
  
             // InterDomainForwarding specific handling concludes here
             try {
                 counterStore.updatePktOutFMCounterStore(sw, fm);
                 if (log.isTraceEnabled()) {
                     log.trace("Pushing Route flowmod routeIndx={} "
                             + "sw={} inPort={} outPort={}", new Object[] {
                             indx, sw, fm.getMatch().getInputPort(), outPort });
                 }
                 sw.write(fm, cntx);
                 if (doFlush) {
                     sw.flush();
                 }
 
                 // Push the packet out the source switch
                 if (sw.getId() == pinSwitch) {
                     // TODO: Instead of doing a packetOut here we could also
                     // send a flowMod with bufferId set....
                     pushPacket(sw, match, pi, outPort, cntx);
                     srcSwitchIncluded = true;
                 }
             } catch (IOException e) {
                 log.error("Failure writing flow mod", e);
             }
 
             try {
                 fm = fm.clone();
             } catch (CloneNotSupportedException e) {
                 log.error("Failure cloning flow mod", e);
             }
         }
 
         return srcSwitchIncluded;
     }
 
     private void updateGwBinding() {
         if (gwIPtoMac.size() == bgpIncomingGwIp.size())
             return;
 
         // retrieve all known devices
         Collection<? extends IDevice> allDevices = deviceManager
                 .getAllDevices();
 
         for (int i = 0; i < bgpIncomingGwIp.size(); i++) {
             if (gwIPtoMac.containsKey(bgpIncomingGwIp.get(i)))
                 continue;
 
             for (IDevice d : allDevices) {
                 for (int j = 0; j < d.getIPv4Addresses().length; j++) {
                     if (bgpIncomingGwIp.get(i).equals(d.getIPv4Addresses()[j])) {
                         gwIPtoMac.put(bgpIncomingGwIp.get(i),
                                 MACAddress.valueOf(d.getMACAddress())
                                 .toBytes());
                         break;
                     }
                 }
                 if (gwIPtoMac.containsKey(bgpIncomingGwIp.get(i)))
                     break;
             }
         }
 
     }
 
 }
