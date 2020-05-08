package edu.wisc.cs.project.firewall;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.openflow.protocol.OFFlowMod;
 import org.openflow.protocol.OFMatch;
 import org.openflow.protocol.OFMessage;
 import org.openflow.protocol.OFPacketIn;
 import org.openflow.protocol.OFPacketOut;
 import org.openflow.protocol.OFPort;
 import org.openflow.protocol.OFType;
 import org.openflow.protocol.Wildcards;
 import org.openflow.protocol.action.OFAction;
 import org.openflow.protocol.action.OFActionOutput;
 import org.openflow.protocol.action.OFActionTransportLayerDestination;
 import org.openflow.protocol.action.OFActionTransportLayerSource;
 import org.openflow.util.U16;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import net.floodlightcontroller.core.FloodlightContext;
 import net.floodlightcontroller.core.IFloodlightProviderService;
 import net.floodlightcontroller.core.IOFMessageListener;
 import net.floodlightcontroller.core.IOFSwitch;
 import net.floodlightcontroller.core.IOFSwitchListener;
 import net.floodlightcontroller.core.module.FloodlightModuleContext;
 import net.floodlightcontroller.core.module.FloodlightModuleException;
 import net.floodlightcontroller.core.module.IFloodlightModule;
 import net.floodlightcontroller.core.module.IFloodlightService;
 import net.floodlightcontroller.packet.Ethernet;
 import net.floodlightcontroller.packet.IPv4;
 import net.floodlightcontroller.packet.TCP;
 
 public class Firewall implements IOFMessageListener, IFloodlightModule, IOFSwitchListener {
 
   protected IFloodlightProviderService floodlightProvider;
   protected static Logger logger;
   protected HashMap<Long, Short> lookupTable = new HashMap<Long, Short>(); 
   
     protected static short FLOWMOD_DEFAULT_IDLE_TIMEOUT = 60; // in seconds
     protected static short FLOWMOD_DEFAULT_HARD_TIMEOUT = 0; // infinite
 
   @Override
   public String getName() {
     // TODO Auto-generated method stub
     return "Learning Switch";
   }
 
   @Override
   public boolean isCallbackOrderingPrereq(OFType type, String name) {
     // TODO Auto-generated method stub
     return false;
   }
 
   @Override
   public boolean isCallbackOrderingPostreq(OFType type, String name) {
     // TODO Auto-generated method stub
     return false;
   }
 
   @Override
   public Collection<Class<? extends IFloodlightService>> getModuleServices() {
     // TODO Auto-generated method stub
     return null;
   }
 
   @Override
   public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
     // TODO Auto-generated method stub
     return null;
   }
 
   @Override
   public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
     // TODO Auto-generated method stub
     Collection<Class<? extends IFloodlightService>> l = new ArrayList<Class<? extends IFloodlightService>>();
     l.add(IFloodlightProviderService.class);
     return l;
   }
 
   @Override
   public void init(FloodlightModuleContext context)
       throws FloodlightModuleException {
     // TODO Auto-generated method stub
     floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
     logger = LoggerFactory.getLogger(Firewall.class);
   }
 
   @Override
   public void startUp(FloodlightModuleContext context) {
     // TODO Auto-generated method stub
     floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);
     floodlightProvider.addOFSwitchListener(this);
   }
 
   @Override
   public net.floodlightcontroller.core.IListener.Command receive(
       IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
     // TODO Auto-generated method stub
     //logger.info("Receive a packet!");
     
     // look to see if we should drop the packet
         
     Ethernet eth = IFloodlightProviderService.bcStore.get(cntx, IFloodlightProviderService.CONTEXT_PI_PAYLOAD);  
     
     if(eth.getEtherType() != Ethernet.TYPE_IPv4){
       return Command.CONTINUE;
     }
     
     switch(msg.getType()){
       case PACKET_IN:
       
             OFPacketIn pi = (OFPacketIn) msg;
               
             IPv4 ipPacket = (IPv4) eth.getPayload();
                         
             //sendPacket(pi, poAction, cntx, sw);
                   
       default:
         break;
     }
 
         return Command.CONTINUE;
   }
   
   /**
    * Sends a packet out to the switch
    */
   private void pushPacket(IOFSwitch sw, OFPacketIn pi, 
       ArrayList<OFAction> actions, short actionsLength) {
     
     // create an OFPacketOut for the pushed packet
         OFPacketOut po = (OFPacketOut) floodlightProvider.getOFMessageFactory()
                     .getMessage(OFType.PACKET_OUT);        
         
         // Update the inputPort and bufferID
         po.setInPort(pi.getInPort());
         po.setBufferId(pi.getBufferId());
                 
         // Set the actions to apply for this packet    
     po.setActions(actions);
     po.setActionsLength(actionsLength);
           
         // Set data if it is included in the packet in but buffer id is NONE
         if (pi.getBufferId() == OFPacketOut.BUFFER_ID_NONE) {
             byte[] packetData = pi.getPacketData();
             po.setLength(U16.t(OFPacketOut.MINIMUM_LENGTH
                      po.getActionsLength()  packetData.length));
             po.setPacketData(packetData);
         } else {
             po.setLength(U16.t(OFPacketOut.MINIMUM_LENGTH
                      po.getActionsLength()));
         }        
         
         logger.debug("Push packet to switch: "po);
         
         // Push the packet to the switch
         try {
             sw.write(po, null);
         } catch (IOException e) {
             logger.error("failed to write packetOut: ", e);
         }
   }
   
   private void sendPacket(OFPacketIn pi, ArrayList<OFAction> actions, FloodlightContext cntx, IOFSwitch sw){
         OFPacketOut po = (OFPacketOut) floodlightProvider.getOFMessageFactory().getMessage(OFType.PACKET_OUT);
         po.setBufferId(pi.getBufferId());
         po.setInPort(pi.getInPort());
     
         po.setActions(actions);
   
         if(actions.size() == 1){
           po.setActionsLength((short)OFActionOutput.MINIMUM_LENGTH);
         }
         else if(actions.size() == 2){
           po.setActionsLength(((short)(OFActionOutput.MINIMUM_LENGTH  OFActionTransportLayerDestination.MINIMUM_LENGTH)));
           //logger.info("Sending packet from port 80 destined for 443!");
         }
         else if(actions.size() == 3){
           po.setActionsLength(((short)(OFActionOutput.MINIMUM_LENGTH  OFActionTransportLayerDestination.MINIMUM_LENGTH  OFActionTransportLayerSource.MINIMUM_LENGTH)));  
         }
         else{
           logger.info("MORE THAN 2 ACTIONS IN ARRAY....WTF?");
           for(OFAction act : actions){
             act.toString();
           }        
         }
         //po.setActionsLength((short)actionsLength);
         
         if (pi.getBufferId() == 0xffffffff) {
             byte[] packetData = pi.getPacketData();
             po.setLength(U16.t(OFPacketOut.MINIMUM_LENGTH
                      po.getActionsLength()  packetData.length));
             po.setPacketData(packetData);
         } else {
             po.setLength(U16.t(OFPacketOut.MINIMUM_LENGTH
                      po.getActionsLength()));
         }
         
         try {
             sw.write(po, cntx);
             sw.flush();
         } catch (IOException e) {
             logger.error("Failure writing PacketOut", e);
         }
         
         actions.clear();
         //logger.info("DONE WRITING");
   }
   
   @Override
   public void addedSwitch(IOFSwitch sw) {
     // TODO Auto-generated method stub
     
 
   }
 
   @Override
   public void removedSwitch(IOFSwitch sw) {
     // TODO Auto-generated method stub
     
   }
 
   @Override
   public void switchPortChanged(Long switchId) {
     // TODO Auto-generated method stub
     
   }
   /**
    * Block traffic going from H1 to H8 on port 80
    * @param sw
    */
   
   private void dropPacketsH1ToH8(IOFSwitch sw){
     OFFlowMod rule = (OFFlowMod)(OFFlowMod)floodlightProvider.getOFMessageFactory().getMessage(OFType.FLOW_MOD);
     rule.setType(OFType.FLOW_MOD);
       rule.setCommand(OFFlowMod.OFPFC_ADD);
         
       OFMatch match = new OFMatch();
     match.setWildcards(~(OFMatch.OFPFW_DL_TYPE | OFMatch.OFPFW_DL_SRC | OFMatch.OFPFW_DL_DST | OFMatch.OFPFW_NW_PROTO | OFMatch.OFPFW_TP_DST));
     match.setDataLayerType((short)0x0800);
     match.setDataLayerSource("00:00:00:01");
     match.setDataLayerDestination("00:00:00:08");
     match.setNetworkProtocol(IPv4.PROTOCOL_TCP);
     match.setTransportDestination((short)80);
     //match.setNetworkSource(networkSource);
   
   }
 }
