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
 import org.openflow.util.Unsigned;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
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
 
     // proxyArp MAC address - can replace with other values in future
     public final String GW_PROXY_ARP_MACADDRESS = "12:34:56:78:90:12";
 
     protected static Integer localSubnet;
     
     protected static Integer localSubnetMaskBits;
     
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
                 pi.getInPort(), cntx);
         log.debug("proxy ARP reply (unicast) pushed");
 
         return;
     }
 
     protected FloodlightContext prepInterDomainForwarding(FloodlightContext cntx) {
 
        log.debug("InterDomainForwarding applied - gateway-bound traffic");
 
         // Here query BgpRoute for the right gateway IP
         // TODO: replace hard coded constant to BgpRoute query call
         String gwIPAddressStr = "192.168.10.1";
         Integer gwIPAddress = IPv4.toIPv4Address(gwIPAddressStr);
 
         // Below searches for gateway device handler using IDeviceService
 
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
             IDeviceService.fcStore.put(cntx, IDeviceService.CONTEXT_DST_DEVICE,
                     gwDevice);
             log.debug("KC L3 forwarding: assigned gw found");
         } else {
             // if no known devices match the BgpRoute suggested gateway
             // IP, this is an error in BgpRoute to be handled
            log.debug("KC L3 forwarding: assigned gw not known (error condition)");
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
             Integer wildcard_hints, int bufferId, OFPacketIn pi,
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
 
                 // If arping for default gateway(s) ==> isExternal==true
                 // respond with proxy arp; otherwise, flood for now
 
                 // check gw IP address, if false, bypass for normal handling
                 // TODO: add restAPI for user to configure available gateways
                // or get from bgpRoute. Former makes better sense, since floodlight
                // should be in charge of SDN subnets.  Currently done with static
                // -cf interdomain.properties at floodlight startup
                 
                 byte[] targetProtocolAddress = arpRequest.getTargetProtocolAddress();
                 byte[] targetSubnetAddress=targetProtocolAddress.clone();
                 
                 if (localSubnetMaskBits >= 24)
                     targetSubnetAddress[3] = (byte) (targetProtocolAddress[3] >> (32-localSubnetMaskBits) << (32-localSubnetMaskBits));
                 else if (localSubnetMaskBits >= 16)
                     targetSubnetAddress[2] = (byte) (targetProtocolAddress[2] >> (24-localSubnetMaskBits) << (24-localSubnetMaskBits));
                 else if (localSubnetMaskBits >= 8)
                     targetSubnetAddress[1] = (byte) (targetProtocolAddress[1] >> (16-localSubnetMaskBits) << (16-localSubnetMaskBits));
                 else if (localSubnetMaskBits >= 0)
                     targetSubnetAddress[0] = (byte) (targetProtocolAddress[0] >> (8-localSubnetMaskBits) << (8-localSubnetMaskBits));
                 
                // TODO: Currently IP hosts would never arp for external address
                //       The following rule is not really meaningful as it should always be true
                //       Instead, isExternal should be a match against a SDN enforced 
                //       default gateway address which must be distributed by DHCP server, or match
                //       against ANY known gateways and send on to prepInterdomainForwarding to
                //       get BGProute's suggested gateway.
                 boolean isExternal = IPv4.toIPv4Address(targetSubnetAddress)!=localSubnet;
 
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
 
             log.debug("dst is "+dstMac+" gw is "+proxyArpAddress);
             
             if (proxyArpAddress.equals(dstMac)) {
                 cntx = prepInterDomainForwarding(cntx);
             }
 
             doForwardFlow(sw, pi, cntx, false);
         }
 
         return Command.CONTINUE;
     }
 
     @Override
     public void init(FloodlightModuleContext context) throws FloodlightModuleException {
         // read our config options
         Map<String, String> configOptions = context.getConfigParams(this);
         
         String subnet = (String) configOptions.get("localSubnet");
         if (subnet != null) {
             String[] fields = subnet.split("[/]+");           
             localSubnet = IPv4.toIPv4Address(fields[0]);
             localSubnetMaskBits = Integer.parseInt(fields[1]);
         }
         log.debug("local subnet set to {}/{}", IPv4.fromIPv4Address(localSubnet), localSubnetMaskBits);
         super.init(context);
     }    
 }
