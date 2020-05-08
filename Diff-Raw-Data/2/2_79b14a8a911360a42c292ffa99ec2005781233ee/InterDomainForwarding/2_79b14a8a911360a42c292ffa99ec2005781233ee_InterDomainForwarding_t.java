 package net.floodlightcontroller.interdomainforwarding;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
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
 
     protected static String[] proxyGwIp;
     protected static Integer[] localSubnet;
     protected static Integer[] localSubnetMaskBits;
 
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
 
     protected FloodlightContext prepInterDomainForwarding(FloodlightContext cntx){
 
         log.debug("InterDomainForwarding applied - gateway-bound traffic");
 
         Ethernet eth = IFloodlightProviderService.bcStore.get(cntx,
                 IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
 
         IPacket pkt = eth.getPayload();
 
         if (pkt instanceof IPv4) {
             IPv4 ipPkt = (IPv4) pkt;
             // Here query BgpRoute for the right gateway IP
             log.debug("prep destination {}",
                     IPv4.fromIPv4Address(ipPkt.getDestinationAddress()));
                                 
             if (bgpRoute != null) {
                 
                 byte[] gwIPAddressByte = bgpRoute.lookupRib(IPv4.toIPv4AddressBytes(ipPkt.getDestinationAddress())).getNextHop().getAddress();
                                       
                 log.debug("prep nexthop {}", gwIPAddressByte);
 
                 if (gwIPAddressByte == null) return cntx; // no next hop info - give up
                 
                 Integer gwIPAddress = IPv4.toIPv4Address(gwIPAddressByte);
 
                 // Below searches for gateway device handler using
                 // IDeviceService
 
                 // retrieve all known devices
                 Collection<? extends IDevice> allDevices = deviceManager
                         .getAllDevices();
 
                 // look for device with chosen gateway's IP address
                 IDevice gwDevice = null;
 
                 for (IDevice d : allDevices) {
                     for (int i = 0; i < d.getIPv4Addresses().length; i++) {
                         if (gwIPAddress.equals(d.getIPv4Addresses()[i])) {
                             gwDevice = d;
                             break;
                         }
                     }
                 }
 
                 // gw device found
                 if (gwDevice != null) {
                     // overwrite dst device info in cntx
                     IDeviceService.fcStore.put(cntx,
                             IDeviceService.CONTEXT_DST_DEVICE, gwDevice);
                     log.debug("Interdomain forwarding: assigned gw {} found",
                             IPv4.fromIPv4Address(gwIPAddress));
                 } else {
                     // if no known devices match the BgpRoute suggested gateway
                     // IP, this is an error in BgpRoute to be handled
                     log.debug(
                             "Interdomain forwarding: assigned gw {} not known (error condition)",
                             IPv4.fromIPv4Address(gwIPAddress));
                 }
             } else {
                 // non-IP packets get here - not supported
                 log.debug("non-IP packet in prepInterDomainForwarding");
             }
         } else {
             log.debug("prep destination bgpRoute null");
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
             Integer wildcard_hints, OFPacketIn pi,
             long pinSwitch, long cookie, FloodlightContext cntx,
             boolean reqeustFlowRemovedNotifn, boolean doFlush,
             short flowModCommand) {
 
         log.debug("KC pushing route");
 
         boolean srcSwitchIncluded = false;
         OFFlowMod fm = (OFFlowMod) floodlightProvider.getOFMessageFactory()
                 .getMessage(OFType.FLOW_MOD);
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
 
             // set the match.
             fm.setMatch(wildcard(match, sw, wildcard_hints));
 
             // set buffer id if it is the source switch
             if (1 == indx) {
                 // Set the flag to request flow-mod removal notifications only
                 // for the
                 // source switch. The removal message is used to maintain the
                 // flow
                 // cache. Don't set the flag for ARP messages - TODO generalize
                 // check
                 if ((reqeustFlowRemovedNotifn)
                         && (match.getDataLayerType() != Ethernet.TYPE_ARP)) {
                     fm.setFlags(OFFlowMod.OFPFF_SEND_FLOW_REM);
                     match.setWildcards(fm.getMatch().getWildcards());
                 }
             }
 
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
             List<OFAction> newActions = fm.getActions();
 
             if (indx == 1) {
                 if (MACAddress.valueOf(fm.getMatch().getDataLayerDestination())
                         .equals(MACAddress.valueOf(GW_PROXY_ARP_MACADDRESS))) {
                     // create rewrite action with chosen gw MAC address
                     OFAction rewriteAction = new OFActionDataLayerDestination(
                             MACAddress.valueOf(dstDevice.getMACAddress())
                                     .toBytes());
 
                     // add action to current output action
                     newActions.add(0, rewriteAction);
                     fm.setActions(newActions);
                     fm.setLengthU(fm.getLengthU() + rewriteAction.getLengthU());
                     
                     Ethernet eth = IFloodlightProviderService.bcStore.get(cntx,
                             IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
                     
                     // get packet's destination IP address and find out matching subnet
                     IPv4 ip_pkt = (IPv4) eth.getPayload();
                     int ip_pkt_dstIpAddress = ip_pkt.getDestinationAddress();
                     int wildcard_bits = 0;
                     int matched_ip = 0;
                     
                     Rib foundRib = bgpRoute.lookupRib(IPv4.toIPv4AddressBytes(ip_pkt_dstIpAddress));
                     
                     wildcard_bits = 32-foundRib.getMasklen();
                    matched_ip = (ip_pkt_dstIpAddress >> wildcard_bits) << wildcard_bits;
                     
                     if (matched_ip==0)
                         log.debug("no matching local subnet found - cannot set correct ip_prefix wildcard");
                     else {
                         // set flow mod dst IP address and wildcard 
                         fm.getMatch().setDataLayerType(Ethernet.TYPE_IPv4);
                         fm.getMatch().setNetworkDestination(matched_ip);
                         int current_wildcard = fm.getMatch().getWildcards();
                         fm.getMatch().setWildcards((current_wildcard & ~OFMatch.OFPFW_NW_DST_ALL & ~OFMatch.OFPFW_DL_TYPE) 
                                 | (wildcard_bits << OFMatch.OFPFW_NW_DST_SHIFT) 
                                 | OFMatch.OFPFW_NW_SRC_ALL 
                                 | OFMatch.OFPFW_NW_PROTO);                    
                     }
                 }
             } else {
                 // update match for output action
                 fm.getMatch()
                         .setDataLayerDestination(
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
 
                 byte[] targetProtocolAddress = arpRequest
                         .getTargetProtocolAddress();
 
 
                 boolean isExternal = false;
                 for (int i=0; i<proxyGwIp.length; i++) {
                     if (IPv4.toIPv4Address(targetProtocolAddress) == IPv4.toIPv4Address(proxyGwIp[i])) {
                         isExternal = true;
                         log.debug("isEqual address {} : {}",
                                 IPv4.toIPv4Address(targetProtocolAddress),
                                 IPv4.toIPv4Address(proxyGwIp[i]));
                         break;
                     }
                 }
                 if (isExternal) {
                     doProxyArp(sw, pi, cntx);
 
                     // arp pushed already, complete forwarding
                     return Command.CONTINUE;
                 }
             }
 
             // For now we treat multicast as broadcast
             doFlood(sw, pi, cntx);
         } else {
             MACAddress proxyArpAddress = MACAddress
                     .valueOf(GW_PROXY_ARP_MACADDRESS);
             MACAddress dstMac = MACAddress.valueOf(eth
                     .getDestinationMACAddress());
 
             log.debug("dst is " + dstMac + " gw is " + proxyArpAddress);
 
             if (proxyArpAddress.equals(dstMac)) {
                 cntx = prepInterDomainForwarding(cntx);
             }
 
             doForwardFlow(sw, pi, cntx, false);
         }
 
         return Command.CONTINUE;
     }
 
     @Override
     public void init(FloodlightModuleContext context)
             throws FloodlightModuleException {
 
         bgpRoute = context.getServiceImpl(IBgpRouteService.class);
 
         // read our config options
         Map<String, String> configOptions = context.getConfigParams(this);
 
         String proxyGwIpString = configOptions.get("proxyGateway");
         if (proxyGwIpString != null) {
             proxyGwIp = proxyGwIpString.split("[/, ]+");
             for (int i=0; i < proxyGwIp.length; i++) {
                 log.debug("add proxy gateway {}",
                         proxyGwIp[i]);
             }
         }
 
         String subnet = configOptions.get("localSubnet");
         if (subnet != null) {
             String[] fields = subnet.split("[/, ]+");
             int addresses = fields.length/2;
             localSubnet = new Integer[addresses];
             localSubnetMaskBits = new Integer[addresses];
             for (int i=0; i<addresses; i++) {
                 localSubnet[i] = IPv4.toIPv4Address(fields[2*i]);
                 localSubnetMaskBits[i] = Integer.parseInt(fields[2*i+1]);
                 log.debug("add local subnet {}/{}",
                         IPv4.fromIPv4Address(localSubnet[i]), localSubnetMaskBits[i]);
             }
         }
 
         super.init(context);
     }
 
 }
