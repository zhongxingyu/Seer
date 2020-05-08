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
 import org.openflow.protocol.OFPort;
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
     boolean rewriteNeeded = false;
     boolean subnetWildcard = false;
 
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
         IPacket pkt = eth.getPayload();
 
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
             // init rewriteNeeded flag
             rewriteNeeded = false;
             subnetWildcard = false;
             
             // check outgoing case
             byte[] dstMacBytes = eth.getDestinationMACAddress();
             byte[] proxyArpAddressBytes = proxyArpAddress.toBytes();            
             
             log.debug("dst is " + MACAddress.valueOf(dstMacBytes) + "("+ MACAddress.valueOf(dstMacBytes).toLong() + ")"
                     + " gw is " + MACAddress.valueOf(proxyArpAddressBytes) + "(" + MACAddress.valueOf(proxyArpAddressBytes).toLong() +")");
 
             if ( proxyArpAddressBytes[0] == dstMacBytes[0] && 
                  proxyArpAddressBytes[1] == dstMacBytes[1] && 
                  proxyArpAddressBytes[2] == dstMacBytes[2] && 
                  proxyArpAddressBytes[3] == dstMacBytes[3] && 
                  proxyArpAddressBytes[4] == dstMacBytes[4] && 
                  proxyArpAddressBytes[5] == dstMacBytes[5] ) {
                 log.debug("rewriteNeeded, subnetWildcard");
                 
                 // get destination gateway via bgpRoute
                 cntx = prepInterDomainForwarding(cntx);
                 
                 // new dest (gw) decided, will rewrite dest mac and do subnet based match
                 rewriteNeeded = true;
                 subnetWildcard = true;
 
             } else { 
                 if (pkt instanceof IPv4) {            
                     IPv4 ip_pkt = (IPv4) pkt;
                     int pktDstIp = ip_pkt.getDestinationAddress();
                     
                     updateGwBinding();
                     
                     // check returning to BGP case
                     for (int i = 0; i < bgpIncomingGwIp.size(); i++) {
                         byte[] bgpIncomingGwMac = gwIPtoMac
                                 .get(bgpIncomingGwIp.get(i));
                         log.info("ip {} has mac {}", IPv4.fromIPv4Address(bgpIncomingGwIp.get(i)),
                                 bgpIncomingGwMac);
        
                         
                         // Going to bgp incoming gw
                         if (bgpIncomingGwMac != null
                                 && dstMacBytes[0] == bgpIncomingGwMac[0]
                                 && dstMacBytes[1] == bgpIncomingGwMac[1]
                                 && dstMacBytes[2] == bgpIncomingGwMac[2]
                                 && dstMacBytes[3] == bgpIncomingGwMac[3]
                                 && dstMacBytes[4] == bgpIncomingGwMac[4]
                                 && dstMacBytes[5] == bgpIncomingGwMac[5]) {
                             
                             log.info("Traffic going to BgpIncoming interface {}", IPv4.fromIPv4Address(bgpIncomingGwIp.get(i)));
                             
                             // but the real destination is not gw
                             if (pktDstIp != bgpIncomingGwIp.get(i)) {
                             
                                 // look for device with dst host IP address
                                 IDevice targetDevice = null;
                                 
                                 // retrieve all known devices
                                 Collection<? extends IDevice> allDevices = deviceManager
                                         .getAllDevices();
                                 
                                 for (IDevice d : allDevices) {
                                     for (int j = 0; j < d.getIPv4Addresses().length; j++) {
                                         if (pktDstIp == d.getIPv4Addresses()[j]) {
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
                                     log.debug("Interdomain forwarding: real dest {} found put into cntx",
                                             IPv4.fromIPv4Address(pktDstIp));
                                     
                                     // correct dest found, will rewrite dest mac
                                     rewriteNeeded = true;
                                     
                                 } else {
                                     // if no known devices match the right dest 
                                     // this is an error in BgpRoute to be handled
                                     log.debug(
                                             "Interdomain forwarding: dest host {} not in device list (error condition)",
                                             IPv4.fromIPv4Address(pktDstIp));
                                 }
                             }
                             break;
                         }
                     }
                 }
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
 
             byte[] targetIPAddressByte = null;
             Integer targetIPAddress;
             
            if (bgpRoute != null && bgpRoute.lookupRib(IPv4.toIPv4AddressBytes(ipPkt.getDestinationAddress())) != null && bgpRoute.lookupRib(IPv4.toIPv4AddressBytes(ipPkt.getDestinationAddress()))
                    .getNextHop() != null)
                 // Here query BgpRoute for the right gateway IP
                 targetIPAddressByte = 
                     bgpRoute.lookupRib(IPv4.toIPv4AddressBytes(ipPkt.getDestinationAddress()))
                     .getNextHop().getAddress();
             else
                 log.debug("prep destination bgpRoute null");
 
            if (targetIPAddressByte != null)
                log.debug("prep nexthop {}", IPv4.fromIPv4Address(IPv4.toIPv4Address(targetIPAddressByte)));
            else
                return cntx; // no next hop info - give up
 
             targetIPAddress = IPv4.toIPv4Address(targetIPAddressByte);
 
             // Below searches for target device using IDeviceService
 
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
 
             // target device found
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
         
         boolean matchIP = false;
         boolean matchEthertype = false;
 
         int pktDstIp = 0;
         if (pkt instanceof IPv4) {
             IPv4 ip_pkt = (IPv4) pkt;
             pktDstIp = ip_pkt.getDestinationAddress();
             matchIP = true;
         } else if (pkt instanceof ARP) {
             ARP arp_pkt = (ARP) eth.getPayload();
             pktDstIp = IPv4.toIPv4Address(arp_pkt.getTargetProtocolAddress());
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
 
         IDevice dstDevice = IDeviceService.fcStore.get(cntx,
                 IDeviceService.CONTEXT_DST_DEVICE);
                 
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
 
             if (indx == 1) {
                 log.debug("1st hop prep flowmod");
                 // done in forwarding - not necessarily relevant here
                 if ((reqeustFlowRemovedNotifn)
                         && (match.getDataLayerType() != Ethernet.TYPE_ARP)) {
                     fm.setFlags(OFFlowMod.OFPFF_SEND_FLOW_REM);
                     match.setWildcards(fm.getMatch().getWildcards());
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
                     log.debug("match IP");
                     // add rewrite action to current output action
                     if (rewriteNeeded) {
                         log.debug("rewrite action created");
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
                         log.debug("subnetWildcard true - look up Rib");
                         Rib foundRib = bgpRoute.lookupRib(IPv4
                                 .toIPv4AddressBytes(pktDstIp));
                         wildcard_bits = 32 - foundRib.getMasklen();
                     }
                     
                     matched_ip = (pktDstIp >> wildcard_bits) << wildcard_bits;
 
                     if (matched_ip == 0)
                         log.debug("no matching local subnet found - cannot set correct ip_prefix wildcard");
                     else {
                         log.debug("writing IP dest flowmod");
                         // set flow mod with dst IP address and wildcard
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
                 log.debug("{} hop prep flowmod", indx);
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
                     pushPacket(sw, fm.getMatch(), pi, OFPort.OFPP_TABLE.getValue(), cntx);
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
         if (gwIPtoMac.size() == bgpIncomingGwIp.size()) {
             log.debug("all {} gw's MAC known already", gwIPtoMac.size());
             return;
         }
         
         log.debug("update gwIPtoMac (current size: {})", gwIPtoMac.size());
 
         // retrieve all known devices
         Collection<? extends IDevice> allDevices = deviceManager
                 .getAllDevices();
 
         for (int i = 0; i < bgpIncomingGwIp.size(); i++) {
             if (gwIPtoMac.containsKey(bgpIncomingGwIp.get(i))) {
                 log.debug("known gwIP {} mac {}", IPv4.fromIPv4Address(bgpIncomingGwIp.get(i)), 
                         MACAddress.valueOf(gwIPtoMac.get(bgpIncomingGwIp.get(i))));
                 continue;
             }
 
             for (IDevice d : allDevices) {
                 boolean foundOne = false;
                 for (int j = 0; j < d.getIPv4Addresses().length; j++) {
                     log.debug("check match gw {} device ip {}", IPv4.fromIPv4Address(bgpIncomingGwIp.get(i)), IPv4.fromIPv4Address(d.getIPv4Addresses()[j]));
                     if (bgpIncomingGwIp.get(i).equals(d.getIPv4Addresses()[j])) {
                         gwIPtoMac.put(bgpIncomingGwIp.get(i),
                                 MACAddress.valueOf(d.getMACAddress())
                                 .toBytes());
                         log.debug("found gw {} mac {}", IPv4.fromIPv4Address(bgpIncomingGwIp.get(i)), MACAddress.valueOf(d.getMACAddress()));
                         foundOne = true;
                         break;
                     }
                 }
                 if (foundOne)
                     break;
             }
         }
 
     }
 
 }
