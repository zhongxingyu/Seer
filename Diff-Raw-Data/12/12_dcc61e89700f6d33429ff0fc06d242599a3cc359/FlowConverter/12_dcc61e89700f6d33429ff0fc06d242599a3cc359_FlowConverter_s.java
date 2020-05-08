 /*
  @header@
  */
 
 package org.opendaylight.controller.protocol_plugin.packetcable.internal;
 
 import java.math.BigInteger;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.opendaylight.controller.sal.action.Action;
 import org.opendaylight.controller.sal.action.ActionType;
 import org.opendaylight.controller.sal.action.Controller;
 import org.opendaylight.controller.sal.action.Drop;
 import org.opendaylight.controller.sal.action.Flood;
 import org.opendaylight.controller.sal.action.FloodAll;
 import org.opendaylight.controller.sal.action.HwPath;
 import org.opendaylight.controller.sal.action.Loopback;
 import org.opendaylight.controller.sal.action.Output;
 import org.opendaylight.controller.sal.action.PopVlan;
 import org.opendaylight.controller.sal.action.SetDlDst;
 import org.opendaylight.controller.sal.action.SetDlSrc;
 import org.opendaylight.controller.sal.action.SetNwDst;
 import org.opendaylight.controller.sal.action.SetNwSrc;
 import org.opendaylight.controller.sal.action.SetNwTos;
 import org.opendaylight.controller.sal.action.SetTpDst;
 import org.opendaylight.controller.sal.action.SetTpSrc;
 import org.opendaylight.controller.sal.action.SetVlanId;
 import org.opendaylight.controller.sal.action.SetVlanPcp;
 import org.opendaylight.controller.sal.action.SwPath;
 import org.opendaylight.controller.sal.core.Node;
 import org.opendaylight.controller.sal.core.NodeConnector;
 import org.opendaylight.controller.sal.flowprogrammer.Flow;
 import org.opendaylight.controller.sal.match.Match;
 import org.opendaylight.controller.sal.match.MatchField;
 import org.opendaylight.controller.sal.match.MatchType;
 import org.opendaylight.controller.sal.utils.NetUtils;
 import org.opendaylight.controller.sal.utils.NodeConnectorCreator;
 /**
 import org.opendaylight.controller.protocol_plugin.openflow.vendorextension.v6extension.V6FlowMod;
 import org.opendaylight.controller.protocol_plugin.openflow.vendorextension.v6extension.V6Match;
 import org.openflow.protocol.OFFlowMod;
 import org.openflow.protocol.OFMatch;
 import org.openflow.protocol.OFMessage;
 import org.openflow.protocol.OFPacketOut;
 import org.openflow.protocol.OFPort;
 import org.openflow.protocol.OFVendor;
 import org.openflow.protocol.action.OFAction;
 import org.openflow.protocol.action.OFActionDataLayer;
 import org.openflow.protocol.action.OFActionDataLayerDestination;
 import org.openflow.protocol.action.OFActionDataLayerSource;
 import org.openflow.protocol.action.OFActionNetworkLayerAddress;
 import org.openflow.protocol.action.OFActionNetworkLayerDestination;
 import org.openflow.protocol.action.OFActionNetworkLayerSource;
 import org.openflow.protocol.action.OFActionNetworkTypeOfService;
 import org.openflow.protocol.action.OFActionOutput;
 import org.openflow.protocol.action.OFActionStripVirtualLan;
 import org.openflow.protocol.action.OFActionTransportLayer;
 import org.openflow.protocol.action.OFActionTransportLayerDestination;
 import org.openflow.protocol.action.OFActionTransportLayerSource;
 import org.openflow.protocol.action.OFActionVirtualLanIdentifier;
 import org.openflow.protocol.action.OFActionVirtualLanPriorityCodePoint;
 import org.openflow.util.U16;
 import org.openflow.util.U32;
 */
 
 import org.pcmm.PCMMGlobalConfig;
 import org.pcmm.gates.IAMID;
 import org.pcmm.gates.IClassifier;
 import org.pcmm.gates.IExtendedClassifier;
 import org.pcmm.gates.IGateSpec;
 import org.pcmm.gates.IGateSpec.DSCPTOS;
 import org.pcmm.gates.IGateSpec.Direction;
 import org.pcmm.gates.IPCMMGate;
 import org.pcmm.gates.ISubscriberID;
 import org.pcmm.gates.ITrafficProfile;
 import org.pcmm.gates.ITransactionID;
 import org.pcmm.gates.IGateID;
 import org.pcmm.gates.impl.GateID;
 import org.pcmm.gates.impl.AMID;
 import org.pcmm.gates.impl.BestEffortService;
 import org.pcmm.gates.impl.Classifier;
 import org.pcmm.gates.impl.ExtendedClassifier;
 import org.pcmm.gates.impl.DOCSISServiceClassNameTrafficProfile;
 import org.pcmm.gates.impl.GateSpec;
 import org.pcmm.gates.impl.PCMMGateReq;
 import org.pcmm.gates.impl.SubscriberID;
 import org.pcmm.gates.impl.TransactionID;
 
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Utility class for converting a SAL Flow into the PCMM service flow and vice-versa
  */
 public class FlowConverter {
     protected static final Logger logger = LoggerFactory
                                            .getLogger(FlowConverter.class);
 
     /*
      * The value 0xffff (OFP_VLAN_NONE) is used to indicate
      * that no VLAN ID is set for OF Flow.
      */
     private static final short OFP_VLAN_NONE = (short) 0xffff;
 
     private Flow flow; // SAL Flow
 //    private OFMatch ofMatch; // OF 1.0 match or OF 1.0 + IPv6 extension match
 //    private List<OFAction> actionsList; // OF 1.0 actions
     private int actionsLength;
     private boolean isIPv6;
 
     /*
         public FlowConverter(OFMatch ofMatch, List<OFAction> actionsList) {
      //       this.ofMatch = ofMatch;
             this.actionsList = actionsList;
             this.actionsLength = 0;
             this.flow = null;
       //      this.isIPv6 = ofMatch instanceof V6Match;
         }
     */
 
     public FlowConverter(Flow flow) {
 //        this.ofMatch = null;
 //        this.actionsList = null;
         this.actionsLength = 0;
         this.flow = flow;
         this.isIPv6 = flow.isIPv6();
     }
 
 
     // public void dump(Flow flow) {
     public void dump() {
         logger.info("SAL Flow IPv6 : {}", flow.isIPv6());
         logger.info("SAL Flow Actions : {}", flow.getActions());
         logger.info("SAL Flow Priority: {}", flow.getPriority());
         logger.info("SAL Flow Id: {}", flow.getId());
         logger.info("SAL Flow Idle Timeout: {}", flow.getIdleTimeout());
         logger.info("SAL Flow Hard Timeout: {}", flow.getHardTimeout());
     }
 
     public void dumpAction() {
         logger.info("SAL Flow Actions:");
         Iterator<Action> actionIter = flow.getActions().iterator();
         while (actionIter.hasNext()) {
             Action action = actionIter.next();
             switch (action.getType()) {
             case DROP:
                 logger.info("drop");
                 break;
             case LOOPBACK:
                 logger.info("loopback");
                 break;
             case FLOOD:
                 logger.info("flood");
                 break;
             case FLOOD_ALL:
                 logger.info("floodAll");
                 break;
             case CONTROLLER:
                 logger.info("controller");
                 break;
             case INTERFACE:
                 logger.info("interface");
                 break;
             case SW_PATH:
                 logger.info("software path");
                 break;
             case HW_PATH:
                 logger.info("harware path");
                 break;
             case OUTPUT:
                 logger.info("output");
                 break;
             case ENQUEUE:
                 logger.info("enqueue");
                 break;
             case SET_DL_SRC:
                 logger.info("setDlSrc");
                 break;
             case SET_DL_DST:
                 logger.info("setDlDst");
                 break;
             case SET_VLAN_ID:
                 logger.info("setVlan");
                 break;
             case SET_VLAN_PCP:
                 logger.info("setVlanPcp");
                 break;
             case SET_VLAN_CFI:
                 logger.info("setVlanCif");
                 break;
             case POP_VLAN:
                 logger.info("stripVlan");
                 break;
             case PUSH_VLAN:
                 logger.info("pushVlan");
                 break;
             case SET_DL_TYPE:
                 logger.info("setDlType");
                 break;
             case SET_NW_SRC:
                 logger.info("setNwSrc");
                 break;
             case SET_NW_DST:
                 logger.info("setNwDst");
                 break;
             case SET_NW_TOS:
                 logger.info("setNwTos");
                 break;
             case SET_TP_SRC:
                 logger.info("setTpSrc");
                 break;
             case SET_TP_DST:
                 logger.info("setTpDst");
                 break;
             case SET_NEXT_HOP:
                 logger.info("setNextHop");
                 break;
             default:
                 logger.info("Unknown");
                 break;
             }
         }
     }
 
 
     /**
      * Returns the match in Gate Control message
      *
      * @return
      */
     // public OFMatch getOFMatch() {
     public IPCMMGate  getServiceFlow() {
         IPCMMGate gate = new PCMMGateReq();
         // ITransactionID trID = new TransactionID();
 
         IAMID amid = new AMID();
         ISubscriberID subscriberID = new SubscriberID();
         IGateSpec gateSpec = new GateSpec();
         IClassifier classifier = new Classifier();
         IExtendedClassifier eclassifier = new ExtendedClassifier();
         InetAddress defaultmask = null;
 
         /* Constrain priority to  64 to 128 as per spec */
         byte pri = (byte) (flow.getPriority() & 0xFFFF);
         if ((pri < 64) || (pri > 128))
             eclassifier.setPriority((byte) 64);
         else
             eclassifier.setPriority(pri);
 
         int TrafficRate = 0;
         if (pri == 100)
             TrafficRate =   PCMMGlobalConfig.DefaultBestEffortTrafficRate;
         else
             TrafficRate =   PCMMGlobalConfig.DefaultLowBestEffortTrafficRate;
 
         logger.info("FlowConverter Flow Id: {}", flow.getId());
         logger.info("FlowConverter Priority: {}",pri);
         logger.info("FlowConverter Traffic Rate: {}",TrafficRate);
 
         ITrafficProfile trafficProfile = new BestEffortService(
             (byte) 7); //BestEffortService.DEFAULT_ENVELOP);
         ((BestEffortService) trafficProfile).getAuthorizedEnvelop()
         .setTrafficPriority(BestEffortService.DEFAULT_TRAFFIC_PRIORITY);
         ((BestEffortService) trafficProfile).getAuthorizedEnvelop()
         .setMaximumTrafficBurst(
             BestEffortService.DEFAULT_MAX_TRAFFIC_BURST);
         ((BestEffortService) trafficProfile).getAuthorizedEnvelop()
         .setRequestTransmissionPolicy(
             PCMMGlobalConfig.BETransmissionPolicy);
         ((BestEffortService) trafficProfile).getAuthorizedEnvelop()
         .setMaximumSustainedTrafficRate(
             TrafficRate);
 
         ((BestEffortService) trafficProfile).getReservedEnvelop()
         .setTrafficPriority(BestEffortService.DEFAULT_TRAFFIC_PRIORITY);
         ((BestEffortService) trafficProfile).getReservedEnvelop()
         .setMaximumTrafficBurst(
             BestEffortService.DEFAULT_MAX_TRAFFIC_BURST);
         ((BestEffortService) trafficProfile).getReservedEnvelop()
         .setRequestTransmissionPolicy(
             PCMMGlobalConfig.BETransmissionPolicy);
         ((BestEffortService) trafficProfile).getReservedEnvelop()
         .setMaximumSustainedTrafficRate(
             TrafficRate);
 
 
         ((BestEffortService) trafficProfile).getCommittedEnvelop()
         .setTrafficPriority(BestEffortService.DEFAULT_TRAFFIC_PRIORITY);
         ((BestEffortService) trafficProfile).getCommittedEnvelop()
         .setMaximumTrafficBurst(
             BestEffortService.DEFAULT_MAX_TRAFFIC_BURST);
         ((BestEffortService) trafficProfile).getCommittedEnvelop()
         .setRequestTransmissionPolicy(
             PCMMGlobalConfig.BETransmissionPolicy);
         ((BestEffortService) trafficProfile).getCommittedEnvelop()
         .setMaximumSustainedTrafficRate(
             TrafficRate);
 
 
         amid.setApplicationType((short) 1);
         amid.setApplicationMgrTag((short) 1);
         gateSpec.setDirection(Direction.UPSTREAM);
         gateSpec.setDSCP_TOSOverwrite(DSCPTOS.OVERRIDE);
         gateSpec.setTimerT1(PCMMGlobalConfig.GateT1);
         gateSpec.setTimerT2(PCMMGlobalConfig.GateT2);
         gateSpec.setTimerT3(PCMMGlobalConfig.GateT3);
         gateSpec.setTimerT4(PCMMGlobalConfig.GateT4);
 
         try {
             InetAddress subIP = InetAddress
                             .getByName(PCMMGlobalConfig.SubscriberID);
               subscriberID.setSourceIPAddress(subIP);
         } catch (UnknownHostException unae) {
             logger.error("Error getByName" + unae.getMessage());
         }
 
 
 
         Match match = flow.getMatch();
 
 
         if (match.isPresent(MatchType.IN_PORT)) {
             short port = (Short) ((NodeConnector) match.getField(
                                       MatchType.IN_PORT).getValue()).getID();
             if (!isIPv6) {
                 logger.info("Flow : In Port: {}", port);
             } else {
                 logger.info("Flow V6 : In Port: {}", port);
             }
         }
         if (match.isPresent(MatchType.DL_SRC)) {
             byte[] srcMac = (byte[]) match.getField(MatchType.DL_SRC)
                             .getValue();
             if (!isIPv6) {
                 logger.info("Flow : Data Layer Src MAC: {}", srcMac);
             } else {
                 logger.info("Flow V6 : Data Layer Src MAC: {}", srcMac);
             }
         }
         if (match.isPresent(MatchType.DL_DST)) {
             byte[] dstMac = (byte[]) match.getField(MatchType.DL_DST)
                             .getValue();
             if (!isIPv6) {
                 logger.info("Flow : Data Layer Dst MAC: {}", dstMac);
             } else {
                 logger.info("Flow V6 : Data Layer Dst MAC: {}", dstMac);
             }
         }
         if (match.isPresent(MatchType.DL_VLAN)) {
             short vlan = (Short) match.getField(MatchType.DL_VLAN)
                          .getValue();
             if (vlan == MatchType.DL_VLAN_NONE) {
                 vlan = OFP_VLAN_NONE;
             }
             if (!isIPv6) {
                 logger.info("Flow : Data Layer Vlan: {}", vlan);
             } else {
                 logger.info("Flow V6 : Data Layer Vlan: {}", vlan);
             }
         }
         if (match.isPresent(MatchType.DL_VLAN_PR)) {
             byte vlanPr = (Byte) match.getField(MatchType.DL_VLAN_PR)
                           .getValue();
             if (!isIPv6) {
                 logger.info("Flow : Data Layer Vlan Priority: {}", vlanPr);
             } else {
                 logger.info("Flow : Data Layer Vlan Priority: {}", vlanPr);
             }
         }
         if (match.isPresent(MatchType.DL_TYPE)) {
             short ethType = (Short) match.getField(MatchType.DL_TYPE)
                             .getValue();
             if (!isIPv6) {
                 logger.info("Flow : Data Layer Eth Type: {}", ethType);
             } else {
                 logger.info("Flow V6: Data Layer Eth Type: {}", ethType);
             }
         }
         if (match.isPresent(MatchType.NW_TOS)) {
             byte tos = (Byte) match.getField(MatchType.NW_TOS).getValue();
             byte dscp = (byte) (tos << 2);
             if (!isIPv6) {
                 logger.info("Flow : Network TOS : {}", tos);
                 logger.info("Flow : Network DSCP : {}", dscp);
                 // XXX - hook me up
                 gateSpec.setDSCP_TOSOverwrite(DSCPTOS.OVERRIDE);
             } else {
                 logger.info("Flow V6 : Network TOS : {}", tos);
                 logger.info("Flow V6 : Network DSCP : {}", dscp);
             }
         }
         if (match.isPresent(MatchType.NW_PROTO)) {
             byte proto = (Byte) match.getField(MatchType.NW_PROTO)
                          .getValue();
             if (!isIPv6) {
                 logger.info("Flow : Network Protocol : {}", proto);
                 switch (proto) {
                 case 6:
                    classifier.setProtocol(IClassifier.Protocol.TCP);
                     break;
                 case 17:
                    classifier.setProtocol(IClassifier.Protocol.UDP);
                     break;
                 case 0:
                 default:
                    classifier.setProtocol(IClassifier.Protocol.NONE);
                     break;
                 }
             } else {
                 logger.info("Flow V6 : Network Protocol : {}", proto);
             }
         }
         if (match.isPresent(MatchType.NW_SRC)) {
             InetAddress address = (InetAddress) match.getField(MatchType.NW_SRC).getValue();
             InetAddress mask = (InetAddress) match.getField(MatchType.NW_SRC).getMask();
 
             try {
                 defaultmask = InetAddress.getByName("0.0.0.0");
             } catch (UnknownHostException unae) {
                 System.out.println("Error getByName" + unae.getMessage());
             }
 
 
             if (!isIPv6) {
                 int maskLength = (mask == null) ? 32 : NetUtils.getSubnetMaskLength(mask);
                 logger.info("Flow : Network Address Src : {} Mask : {}", address, mask);
                 eclassifier.setSourceIPAddress(address);
                 if (mask == null)
                     eclassifier.setIPSourceMask(defaultmask);
                 else
                     eclassifier.setIPSourceMask(mask);
             } else {
                 logger.info("Flow V6 : Network Address Src : {} Mask : {}", address, mask);
 
             }
         }
         if (match.isPresent(MatchType.NW_DST)) {
             InetAddress address = (InetAddress) match.getField(MatchType.NW_DST).getValue();
             InetAddress mask = (InetAddress) match.getField(MatchType.NW_DST).getMask();
             // InetAddress defaultmask;
             try {
                 defaultmask = InetAddress.getByName("0.0.0.0");
             } catch (UnknownHostException unae) {
                 System.out.println("Error getByName" + unae.getMessage());
             }
 
             if (!isIPv6) {
                 int maskLength = (mask == null) ? 32 : NetUtils.getSubnetMaskLength(mask);
                 logger.info("Flow : Network Address Dst : {} Mask : {}", address, mask);
                 eclassifier.setDestinationIPAddress(address);
                 if (mask == null)
                     eclassifier.setIPDestinationMask(defaultmask);
                 else
                     eclassifier.setIPDestinationMask(mask);
 
             } else {
                 logger.info("Flow V6 : Network Address Dst : {} Mask : {}", address, mask);
             }
         }
         if (match.isPresent(MatchType.TP_SRC)) {
             short port = (Short) match.getField(MatchType.TP_SRC)
                          .getValue();
             if (!isIPv6) {
                 logger.info("Flow : Network Transport Port Src : {} ", port);
                 eclassifier.setSourcePortStart(port);
                 eclassifier.setSourcePortEnd(port);
 
             } else {
                 logger.info("Flow V6 : Network Transport Port Src : {} ", port);
             }
         }
         if (match.isPresent(MatchType.TP_DST)) {
             short port = (Short) match.getField(MatchType.TP_DST)
                          .getValue();
             if (!isIPv6) {
                 logger.info("Flow : Network Transport Port Dst : {} ", port);
                 eclassifier.setDestinationPortStart(port);
                 eclassifier.setDestinationPortEnd(port);
             } else {
                 logger.info("Flow V6: Network Transport Port Dst : {} ", port);
             }
         }
 
         if (!isIPv6) {
         }
         logger.info("SAL Match: {} ", flow.getMatch());
         eclassifier.setClassifierID((short) 0x01);
 /*
         eclassifier.setClassifierID((short) (_classifierID == 0 ? Math
                                              .random() * hashCode() : _classifierID));
 */
         eclassifier.setAction((byte) 0x00);
         eclassifier.setActivationState((byte) 0x01);
         //gate.setTransactionID(trID);
         gate.setAMID(amid);
         gate.setSubscriberID(subscriberID);
         gate.setGateSpec(gateSpec);
         gate.setTrafficProfile(trafficProfile);
         gate.setClassifier(eclassifier);
 
         return gate;
     }
 
     /**
      * Returns the list of actions in OF 1.0 form
      *
      * @return
     public List<OFAction> getOFActions() {
         if (this.actionsList == null) {
             actionsList = new ArrayList<OFAction>();
             for (Action action : flow.getActions()) {
                 if (action.getType() == ActionType.OUTPUT) {
                     Output a = (Output) action;
                     OFActionOutput ofAction = new OFActionOutput();
                     ofAction.setMaxLength((short) 0xffff);
                     ofAction.setPort(PortConverter.toOFPort(a.getPort()));
                     actionsList.add(ofAction);
                     actionsLength += OFActionOutput.MINIMUM_LENGTH;
                     continue;
                 }
                 if (action.getType() == ActionType.DROP) {
                     continue;
                 }
                 if (action.getType() == ActionType.LOOPBACK) {
                     OFActionOutput ofAction = new OFActionOutput();
                     ofAction.setPort(OFPort.OFPP_IN_PORT.getValue());
                     actionsList.add(ofAction);
                     actionsLength += OFActionOutput.MINIMUM_LENGTH;
                     continue;
                 }
                 if (action.getType() == ActionType.FLOOD) {
                     OFActionOutput ofAction = new OFActionOutput();
                     ofAction.setPort(OFPort.OFPP_FLOOD.getValue());
                     actionsList.add(ofAction);
                     actionsLength += OFActionOutput.MINIMUM_LENGTH;
                     continue;
                 }
                 if (action.getType() == ActionType.FLOOD_ALL) {
                     OFActionOutput ofAction = new OFActionOutput();
                     ofAction.setPort(OFPort.OFPP_ALL.getValue());
                     actionsList.add(ofAction);
                     actionsLength += OFActionOutput.MINIMUM_LENGTH;
                     continue;
                 }
                 if (action.getType() == ActionType.CONTROLLER) {
                     OFActionOutput ofAction = new OFActionOutput();
                     ofAction.setPort(OFPort.OFPP_CONTROLLER.getValue());
                     // We want the whole frame hitting the match be sent to the
                     // controller
                     ofAction.setMaxLength((short) 0xffff);
                     actionsList.add(ofAction);
                     actionsLength += OFActionOutput.MINIMUM_LENGTH;
                     continue;
                 }
                 if (action.getType() == ActionType.SW_PATH) {
                     OFActionOutput ofAction = new OFActionOutput();
                     ofAction.setPort(OFPort.OFPP_LOCAL.getValue());
                     actionsList.add(ofAction);
                     actionsLength += OFActionOutput.MINIMUM_LENGTH;
                     continue;
                 }
                 if (action.getType() == ActionType.HW_PATH) {
                     OFActionOutput ofAction = new OFActionOutput();
                     ofAction.setPort(OFPort.OFPP_NORMAL.getValue());
                     actionsList.add(ofAction);
                     actionsLength += OFActionOutput.MINIMUM_LENGTH;
                     continue;
                 }
                 if (action.getType() == ActionType.SET_VLAN_ID) {
                     SetVlanId a = (SetVlanId) action;
                     OFActionVirtualLanIdentifier ofAction = new OFActionVirtualLanIdentifier();
                     ofAction.setVirtualLanIdentifier((short) a.getVlanId());
                     actionsList.add(ofAction);
                     actionsLength += OFActionVirtualLanIdentifier.MINIMUM_LENGTH;
                     continue;
                 }
                 if (action.getType() == ActionType.SET_VLAN_PCP) {
                     SetVlanPcp a = (SetVlanPcp) action;
                     OFActionVirtualLanPriorityCodePoint ofAction = new OFActionVirtualLanPriorityCodePoint();
                     ofAction.setVirtualLanPriorityCodePoint(Integer.valueOf(
                             a.getPcp()).byteValue());
                     actionsList.add(ofAction);
                     actionsLength += OFActionVirtualLanPriorityCodePoint.MINIMUM_LENGTH;
                     continue;
                 }
                 if (action.getType() == ActionType.POP_VLAN) {
                     OFActionStripVirtualLan ofAction = new OFActionStripVirtualLan();
                     actionsList.add(ofAction);
                     actionsLength += OFActionStripVirtualLan.MINIMUM_LENGTH;
                     continue;
                 }
                 if (action.getType() == ActionType.SET_DL_SRC) {
                     SetDlSrc a = (SetDlSrc) action;
                     OFActionDataLayerSource ofAction = new OFActionDataLayerSource();
                     ofAction.setDataLayerAddress(a.getDlAddress());
                     actionsList.add(ofAction);
                     actionsLength += OFActionDataLayer.MINIMUM_LENGTH;
                     continue;
                 }
                 if (action.getType() == ActionType.SET_DL_DST) {
                     SetDlDst a = (SetDlDst) action;
                     OFActionDataLayerDestination ofAction = new OFActionDataLayerDestination();
                     ofAction.setDataLayerAddress(a.getDlAddress());
                     actionsList.add(ofAction);
                     actionsLength += OFActionDataLayer.MINIMUM_LENGTH;
                     continue;
                 }
                 if (action.getType() == ActionType.SET_NW_SRC) {
                     SetNwSrc a = (SetNwSrc) action;
                     OFActionNetworkLayerSource ofAction = new OFActionNetworkLayerSource();
                     ofAction.setNetworkAddress(NetUtils.byteArray4ToInt(a
                             .getAddress().getAddress()));
                     actionsList.add(ofAction);
                     actionsLength += OFActionNetworkLayerAddress.MINIMUM_LENGTH;
                     continue;
                 }
                 if (action.getType() == ActionType.SET_NW_DST) {
                     SetNwDst a = (SetNwDst) action;
                     OFActionNetworkLayerDestination ofAction = new OFActionNetworkLayerDestination();
                     ofAction.setNetworkAddress(NetUtils.byteArray4ToInt(a
                             .getAddress().getAddress()));
                     actionsList.add(ofAction);
                     actionsLength += OFActionNetworkLayerAddress.MINIMUM_LENGTH;
                     continue;
                 }
                 if (action.getType() == ActionType.SET_NW_TOS) {
                     SetNwTos a = (SetNwTos) action;
                     OFActionNetworkTypeOfService ofAction = new OFActionNetworkTypeOfService();
                     ofAction.setNetworkTypeOfService(Integer.valueOf(
                             a.getNwTos()).byteValue());
                     actionsList.add(ofAction);
                     actionsLength += OFActionNetworkTypeOfService.MINIMUM_LENGTH;
                     continue;
                 }
                 if (action.getType() == ActionType.SET_TP_SRC) {
                     SetTpSrc a = (SetTpSrc) action;
                     OFActionTransportLayerSource ofAction = new OFActionTransportLayerSource();
                     ofAction.setTransportPort(Integer.valueOf(a.getPort())
                             .shortValue());
                     actionsList.add(ofAction);
                     actionsLength += OFActionTransportLayer.MINIMUM_LENGTH;
                     continue;
                 }
                 if (action.getType() == ActionType.SET_TP_DST) {
                     SetTpDst a = (SetTpDst) action;
                     OFActionTransportLayerDestination ofAction = new OFActionTransportLayerDestination();
                     ofAction.setTransportPort(Integer.valueOf(a.getPort())
                             .shortValue());
                     actionsList.add(ofAction);
                     actionsLength += OFActionTransportLayer.MINIMUM_LENGTH;
                     continue;
                 }
                 if (action.getType() == ActionType.SET_NEXT_HOP) {
                     logger.info("Unsupported action: {}", action);
                     continue;
                 }
             }
         }
         logger.info("SAL Actions: {} Openflow Actions: {}", flow.getActions(),
                 actionsList);
         return actionsList;
     }
      */
 
 
 
 
     /**
      * Utility to convert a SAL flow to an OF 1.0 (OFFlowMod) or to an OF 1.0 +
      * IPv6 extension (V6FlowMod) Flow modifier Message
      *
      * @param sw
      * @param command
      * @param port
      * @return
     public OFMessage getOFFlowMod(short command, OFPort port) {
         OFMessage fm = (isIPv6) ? new V6FlowMod() : new OFFlowMod();
         if (this.ofMatch == null) {
             getOFMatch();
         }
         if (this.actionsList == null) {
             getOFActions();
         }
         if (!isIPv6) {
             ((OFFlowMod) fm).setMatch(this.ofMatch);
             ((OFFlowMod) fm).setActions(this.actionsList);
             ((OFFlowMod) fm).setPriority(flow.getPriority());
             ((OFFlowMod) fm).setCookie(flow.getId());
             ((OFFlowMod) fm).setBufferId(OFPacketOut.BUFFER_ID_NONE);
             ((OFFlowMod) fm).setLength(U16.t(OFFlowMod.MINIMUM_LENGTH
                     + actionsLength));
             ((OFFlowMod) fm).setIdleTimeout(flow.getIdleTimeout());
             ((OFFlowMod) fm).setHardTimeout(flow.getHardTimeout());
             ((OFFlowMod) fm).setCommand(command);
             if (port != null) {
                 ((OFFlowMod) fm).setOutPort(port);
             }
             if (command == OFFlowMod.OFPFC_ADD
                     || command == OFFlowMod.OFPFC_MODIFY
                     || command == OFFlowMod.OFPFC_MODIFY_STRICT) {
                 if (flow.getIdleTimeout() != 0 || flow.getHardTimeout() != 0) {
                     // Instruct switch to let controller know when flow expires
                     ((OFFlowMod) fm).setFlags((short) 1);
                 }
             }
         } else {
             ((V6FlowMod) fm).setVendor();
             ((V6FlowMod) fm).setMatch((V6Match) ofMatch);
             ((V6FlowMod) fm).setActions(this.actionsList);
             ((V6FlowMod) fm).setPriority(flow.getPriority());
             ((V6FlowMod) fm).setCookie(flow.getId());
             ((V6FlowMod) fm).setLength(U16.t(OFVendor.MINIMUM_LENGTH
                     + ((V6Match) ofMatch).getIPv6ExtMinHdrLen()
                     + ((V6Match) ofMatch).getIPv6MatchLen()
                     + ((V6Match) ofMatch).getPadSize() + actionsLength));
             ((V6FlowMod) fm).setIdleTimeout(flow.getIdleTimeout());
             ((V6FlowMod) fm).setHardTimeout(flow.getHardTimeout());
             ((V6FlowMod) fm).setCommand(command);
             if (port != null) {
                 ((V6FlowMod) fm).setOutPort(port);
             }
             if (command == OFFlowMod.OFPFC_ADD
                     || command == OFFlowMod.OFPFC_MODIFY
                     || command == OFFlowMod.OFPFC_MODIFY_STRICT) {
                 if (flow.getIdleTimeout() != 0 || flow.getHardTimeout() != 0) {
                     // Instruct switch to let controller know when flow expires
                     ((V6FlowMod) fm).setFlags((short) 1);
                 }
             }
         }
         logger.info("Openflow Match: {} Openflow Actions: {}", ofMatch,
                 actionsList);
         logger.info("Openflow Mod Message: {}", fm);
         return fm;
     }
      */
 
     /*
      * Installed flow may not have a Match defined like in case of a
      * drop all flow
       public Flow getFlow(Node node) {
           if (this.flow == null) {
               Match salMatch = new Match();
 
               if (ofMatch != null) {
                   if (!isIPv6) {
                       // Compute OF1.0 Match
                       if (ofMatch.getInputPort() != 0 && ofMatch.getInputPort() != OFPort.OFPP_LOCAL.getValue()) {
                           salMatch.setField(new MatchField(MatchType.IN_PORT,
                                   NodeConnectorCreator.createNodeConnector(
                                           ofMatch.getInputPort(), node)));
                       }
                       if (ofMatch.getDataLayerSource() != null
                               && !NetUtils
                                       .isZeroMAC(ofMatch.getDataLayerSource())) {
                           byte srcMac[] = ofMatch.getDataLayerSource();
                           salMatch.setField(new MatchField(MatchType.DL_SRC,
                                   srcMac.clone()));
                       }
                       if (ofMatch.getDataLayerDestination() != null
                               && !NetUtils.isZeroMAC(ofMatch
                                       .getDataLayerDestination())) {
                           byte dstMac[] = ofMatch.getDataLayerDestination();
                           salMatch.setField(new MatchField(MatchType.DL_DST,
                                   dstMac.clone()));
                       }
                       if (ofMatch.getDataLayerType() != 0) {
                           salMatch.setField(new MatchField(MatchType.DL_TYPE,
                                   ofMatch.getDataLayerType()));
                       }
                       short vlan = ofMatch.getDataLayerVirtualLan();
                       if (vlan != 0) {
                           if (vlan == OFP_VLAN_NONE) {
                               vlan = MatchType.DL_VLAN_NONE;
                           }
                           salMatch.setField(new MatchField(MatchType.DL_VLAN,
                                   vlan));
                       }
                       if (ofMatch.getDataLayerVirtualLanPriorityCodePoint() != 0) {
                           salMatch.setField(MatchType.DL_VLAN_PR, ofMatch
                                   .getDataLayerVirtualLanPriorityCodePoint());
                       }
                       if (ofMatch.getNetworkSource() != 0) {
                           salMatch.setField(MatchType.NW_SRC, NetUtils
                                   .getInetAddress(ofMatch.getNetworkSource()),
                                   NetUtils.getInetNetworkMask(
                                           ofMatch.getNetworkSourceMaskLen(),
                                           false));
                       }
                       if (ofMatch.getNetworkDestination() != 0) {
                           salMatch.setField(MatchType.NW_DST,
                                   NetUtils.getInetAddress(ofMatch
                                           .getNetworkDestination()),
                                   NetUtils.getInetNetworkMask(
                                           ofMatch.getNetworkDestinationMaskLen(),
                                           false));
                       }
                       if (ofMatch.getNetworkTypeOfService() != 0) {
                           int dscp = NetUtils.getUnsignedByte(ofMatch
                                   .getNetworkTypeOfService());
                           byte tos = (byte) (dscp >> 2);
                           salMatch.setField(MatchType.NW_TOS, tos);
                       }
                       if (ofMatch.getNetworkProtocol() != 0) {
                           salMatch.setField(MatchType.NW_PROTO,
                                   ofMatch.getNetworkProtocol());
                       }
                       if (ofMatch.getTransportSource() != 0) {
                           salMatch.setField(MatchType.TP_SRC,
                                   ofMatch.getTransportSource());
                       }
                       if (ofMatch.getTransportDestination() != 0) {
                           salMatch.setField(MatchType.TP_DST,
                                   ofMatch.getTransportDestination());
                       }
                   } else {
                       // Compute OF1.0 + IPv6 extensions Match
                       V6Match v6Match = (V6Match) ofMatch;
                       if (v6Match.getInputPort() != 0 && v6Match.getInputPort() != OFPort.OFPP_LOCAL.getValue()) {
                           // Mask on input port is not defined
                           salMatch.setField(new MatchField(MatchType.IN_PORT,
                                   NodeConnectorCreator.createOFNodeConnector(
                                           v6Match.getInputPort(), node)));
                       }
                       if (v6Match.getDataLayerSource() != null
                               && !NetUtils
                                       .isZeroMAC(ofMatch.getDataLayerSource())) {
                           byte srcMac[] = v6Match.getDataLayerSource();
                           salMatch.setField(new MatchField(MatchType.DL_SRC,
                                   srcMac.clone()));
                       }
                       if (v6Match.getDataLayerDestination() != null
                               && !NetUtils.isZeroMAC(ofMatch
                                       .getDataLayerDestination())) {
                           byte dstMac[] = v6Match.getDataLayerDestination();
                           salMatch.setField(new MatchField(MatchType.DL_DST,
                                   dstMac.clone()));
                       }
                       if (v6Match.getDataLayerType() != 0) {
                           salMatch.setField(new MatchField(MatchType.DL_TYPE,
                                   v6Match.getDataLayerType()));
                       }
                       short vlan = v6Match.getDataLayerVirtualLan();
                       if (vlan != 0) {
                           if (vlan == OFP_VLAN_NONE) {
                               vlan = MatchType.DL_VLAN_NONE;
                           }
                           salMatch.setField(new MatchField(MatchType.DL_VLAN,
                                   vlan));
                       }
                       if (v6Match.getDataLayerVirtualLanPriorityCodePoint() != 0) {
                           salMatch.setField(MatchType.DL_VLAN_PR, v6Match
                                   .getDataLayerVirtualLanPriorityCodePoint());
                       }
                       // V6Match may carry IPv4 address
                       if (v6Match.getNetworkSrc() != null) {
                           salMatch.setField(MatchType.NW_SRC,
                                   v6Match.getNetworkSrc(),
                                   v6Match.getNetworkSourceMask());
                       } else if (v6Match.getNetworkSource() != 0) {
                           salMatch.setField(MatchType.NW_SRC, NetUtils
                                   .getInetAddress(v6Match.getNetworkSource()),
                                   NetUtils.getInetNetworkMask(
                                           v6Match.getNetworkSourceMaskLen(),
                                           false));
                       }
                       // V6Match may carry IPv4 address
                       if (v6Match.getNetworkDest() != null) {
                           salMatch.setField(MatchType.NW_DST,
                                   v6Match.getNetworkDest(),
                                   v6Match.getNetworkDestinationMask());
                       } else if (v6Match.getNetworkDestination() != 0) {
                           salMatch.setField(MatchType.NW_DST,
                                   NetUtils.getInetAddress(v6Match
                                           .getNetworkDestination()),
                                   NetUtils.getInetNetworkMask(
                                           v6Match.getNetworkDestinationMaskLen(),
                                           false));
                       }
                       if (v6Match.getNetworkTypeOfService() != 0) {
                           int dscp = NetUtils.getUnsignedByte(v6Match
                                   .getNetworkTypeOfService());
                           byte tos = (byte) (dscp >> 2);
                           salMatch.setField(MatchType.NW_TOS, tos);
                       }
                       if (v6Match.getNetworkProtocol() != 0) {
                           salMatch.setField(MatchType.NW_PROTO,
                                   v6Match.getNetworkProtocol());
                       }
                       if (v6Match.getTransportSource() != 0) {
                           salMatch.setField(MatchType.TP_SRC,
                                   (v6Match.getTransportSource()));
                       }
                       if (v6Match.getTransportDestination() != 0) {
                           salMatch.setField(MatchType.TP_DST,
                                   (v6Match.getTransportDestination()));
                       }
                   }
               }
 
               // Convert actions
               Action salAction = null;
               List<Action> salActionList = new ArrayList<Action>();
               if (actionsList == null) {
                   salActionList.add(new Drop());
               } else {
                   for (OFAction ofAction : actionsList) {
                       if (ofAction instanceof OFActionOutput) {
                           short ofPort = ((OFActionOutput) ofAction).getPort();
                           if (ofPort == OFPort.OFPP_CONTROLLER.getValue()) {
                               salAction = new Controller();
                           } else if (ofPort == OFPort.OFPP_NONE.getValue()) {
                               salAction = new Drop();
                           } else if (ofPort == OFPort.OFPP_IN_PORT.getValue()) {
                               salAction = new Loopback();
                           } else if (ofPort == OFPort.OFPP_FLOOD.getValue()) {
                               salAction = new Flood();
                           } else if (ofPort == OFPort.OFPP_ALL.getValue()) {
                               salAction = new FloodAll();
                           } else if (ofPort == OFPort.OFPP_LOCAL.getValue()) {
                               salAction = new SwPath();
                           } else if (ofPort == OFPort.OFPP_NORMAL.getValue()) {
                               salAction = new HwPath();
                           } else if (ofPort == OFPort.OFPP_TABLE.getValue()) {
                               salAction = new HwPath(); // TODO: we do not handle
                                                         // table in sal for now
                           } else {
                               salAction = new Output(
                                       NodeConnectorCreator.createOFNodeConnector(
                                               ofPort, node));
                           }
                       } else if (ofAction instanceof OFActionVirtualLanIdentifier) {
                           salAction = new SetVlanId(
                                   ((OFActionVirtualLanIdentifier) ofAction)
                                           .getVirtualLanIdentifier());
                       } else if (ofAction instanceof OFActionStripVirtualLan) {
                           salAction = new PopVlan();
                       } else if (ofAction instanceof OFActionVirtualLanPriorityCodePoint) {
                           salAction = new SetVlanPcp(
                                   ((OFActionVirtualLanPriorityCodePoint) ofAction)
                                           .getVirtualLanPriorityCodePoint());
                       } else if (ofAction instanceof OFActionDataLayerSource) {
                           salAction = new SetDlSrc(
                                   ((OFActionDataLayerSource) ofAction)
                                           .getDataLayerAddress().clone());
                       } else if (ofAction instanceof OFActionDataLayerDestination) {
                           salAction = new SetDlDst(
                                   ((OFActionDataLayerDestination) ofAction)
                                           .getDataLayerAddress().clone());
                       } else if (ofAction instanceof OFActionNetworkLayerSource) {
                           byte addr[] = BigInteger.valueOf(
                                   ((OFActionNetworkLayerSource) ofAction)
                                           .getNetworkAddress()).toByteArray();
                           InetAddress ip = null;
                           try {
                               ip = InetAddress.getByAddress(addr);
                           } catch (UnknownHostException e) {
                               logger.error("", e);
                           }
                           salAction = new SetNwSrc(ip);
                       } else if (ofAction instanceof OFActionNetworkLayerDestination) {
                           byte addr[] = BigInteger.valueOf(
                                   ((OFActionNetworkLayerDestination) ofAction)
                                           .getNetworkAddress()).toByteArray();
                           InetAddress ip = null;
                           try {
                               ip = InetAddress.getByAddress(addr);
                           } catch (UnknownHostException e) {
                               logger.error("", e);
                           }
                           salAction = new SetNwDst(ip);
                       } else if (ofAction instanceof OFActionNetworkTypeOfService) {
                           salAction = new SetNwTos(
                                   ((OFActionNetworkTypeOfService) ofAction)
                                           .getNetworkTypeOfService());
                       } else if (ofAction instanceof OFActionTransportLayerSource) {
                           Short port = ((OFActionTransportLayerSource) ofAction)
                                   .getTransportPort();
                           int intPort = NetUtils.getUnsignedShort(port);
                           salAction = new SetTpSrc(intPort);
                       } else if (ofAction instanceof OFActionTransportLayerDestination) {
                           Short port = ((OFActionTransportLayerDestination) ofAction)
                                   .getTransportPort();
                           int intPort = NetUtils.getUnsignedShort(port);
                           salAction = new SetTpDst(intPort);
                       }
                       salActionList.add(salAction);
                   }
               }
               // Create Flow
               flow = new Flow(salMatch, salActionList);
           }
           logger.info("Openflow Match: {} Openflow Actions: {}", ofMatch,
                   actionsList);
           logger.info("SAL Flow: {}", flow);
           return flow;
       }
 
     */
 }
