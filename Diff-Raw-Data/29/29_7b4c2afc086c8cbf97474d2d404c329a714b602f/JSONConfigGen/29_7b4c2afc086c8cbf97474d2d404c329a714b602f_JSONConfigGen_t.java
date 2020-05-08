 package net.es.oscars.pss.openflow.nox;
 
 
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import net.es.oscars.logging.*;
 
 import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneLinkContent;
 import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
 
 import net.es.oscars.api.soap.gen.v06.PathInfo;
 import net.es.oscars.api.soap.gen.v06.ResDetails;
 import net.es.oscars.pss.api.DeviceConfigGenerator;
 import net.es.oscars.pss.beans.PSSAction;
 import net.es.oscars.pss.beans.PSSException;
 import net.es.oscars.pss.beans.config.GenericConfig;
 import net.es.oscars.pss.util.URNParser;
 import net.es.oscars.pss.util.URNParserResult;
 import net.es.oscars.pss.util.ConnectorUtils;
 import net.es.oscars.utils.soap.OSCARSServiceException;
 import net.es.oscars.utils.topology.*;
 
 public class JSONConfigGen implements DeviceConfigGenerator {
     private Logger log = Logger.getLogger(JSONConfigGen.class);
     private OSCARSNetLogger netLogger = OSCARSNetLogger.getTlogger();
    
 
     public String getConfig(PSSAction action, String deviceId) throws PSSException {
         throw new PSSException("should have used getConfig(PSSAction action) instead");
     }
     
     public void setConfig(GenericConfig config) throws PSSException {
         // TODO Auto-generated method stub
     }
 
     public String getConfig(PSSAction action) throws PSSException {
         switch (action.getActionType()) {
             case SETUP :
                 return this.getSetup(action);
             case TEARDOWN:
                 return this.getTeardown(action);
             case STATUS:
                 return this.getVerify(action);
             case MODIFY:
                 throw new PSSException("Modify not supported");
         }
         throw new PSSException("Invalid action type");
     }
     
     private String getSetup(PSSAction action) throws PSSException {
         log.debug("getSetup start");
         ResDetails res = action.getRequest().getSetupReq().getReservation();
         String gri = res.getGlobalReservationId();
         int bw = res.getReservedConstraint().getBandwidth();
         String cmd = "{\"type\":\"oscars-request\", \"action\":\"setup\", \"version\":\"1.0\", \n"
                 + "\"gri\":\"" + gri +"\", \"bandwidth\":\"" + Integer.toString(bw) +"Mbps\", \n"
                 + "\"path\":[\n";
         PathInfo pathInfo = res.getReservedConstraint().getPathInfo();

        List<CtrlPlaneHopContent> hops;
        try {
            hops = PathTools.getLocalHops(pathInfo.getPath(), PathTools.getLocalDomainId());
        } catch (OSCARSServiceException e) {
            throw new PSSException(e);
        }

         for (int i = 0; i < hops.size(); i++) {
             CtrlPlaneHopContent hop1 = hops.get(i);
             try {
                 if (!PathTools.getLocalDomainId().equals(NMWGParserUtil.normalizeURN(NMWGParserUtil.getURN(hop1, NMWGParserUtil.DOMAIN_TYPE)))) {
                     continue;
                 }
             } catch (OSCARSServiceException e) {
                 throw new PSSException("Malformed path hop object: id=" + hop1.getId());
             }
             i++;
             if (i == hops.size())
                 throw new PSSException("Odd number of hops in Path object");
             CtrlPlaneHopContent hop2 = hops.get(i);
             try {
                 if (!PathTools.getLocalDomainId().equals(NMWGParserUtil.normalizeURN(NMWGParserUtil.getURN(hop2, NMWGParserUtil.DOMAIN_TYPE)))) {
                     throw new PSSException("Malformed path object: odd number of local hops");
                 }                
             } catch (OSCARSServiceException e) {
                 throw new PSSException("Malformed path hop object: id=" + hop2.getId());
             }
             CtrlPlaneLinkContent link1 = hop1.getLink();
             CtrlPlaneLinkContent link2 = hop2.getLink();
             if (link1 == null || link2 == null)
                 throw new PSSException("Path Hop is not of Link type");
             URNParserResult urnParser1 = URNParser.parseTopoIdent(link1.getId());
             URNParserResult urnParser2 = URNParser.parseTopoIdent(link2.getId());
             String nodeIp = ConnectorUtils.getDeviceAddress(urnParser1.getNodeId());
             String nodeIp2 = ConnectorUtils.getDeviceAddress(urnParser2.getNodeId());
             String portId1 = urnParser1.getPortId();
             String portId2 = urnParser2.getPortId();
             String vlan1 = link1.getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().getSuggestedVLANRange();
             String vlan2 = link2.getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().getSuggestedVLANRange();
             if (!nodeIp.equals(nodeIp2))
                 throw new PSSException("Malformed path object: hops not paird up on back-to-back order");
             cmd = cmd + "{\"switch\":\"" + nodeIp +"\", \"add-flows\": "
                 + "[{\"port\":\"" + portId1  + "\", \"vlan_range\":\"" + vlan1 + "\"},"
                 + " {\"port\":\"" + portId2 + "\", \"vlan_range\":\"" + vlan2 + "\"}]}";
             if (i == hops.size()-1)
                 cmd += "\n";
             else {
                 CtrlPlaneHopContent hop3 = hops.get(i+1);
                 try {
                     if (!PathTools.getLocalDomainId().equals(NMWGParserUtil.normalizeURN(NMWGParserUtil.getURN(hop3, NMWGParserUtil.DOMAIN_TYPE)))) {
                         cmd += "\n";
                         break;
                     }
                 } catch (OSCARSServiceException e) {
                     throw new PSSException("Malformed path hop object: id=" + hop3.getId());
                 }
                 cmd += ",\n";
             }
         }
         cmd += "]}";
         log.debug("getSetup end");
         return cmd;
     }
     
     
     private String getTeardown(PSSAction action) throws PSSException {
         log.debug("getTeardown start");
         ResDetails res = action.getRequest().getTeardownReq().getReservation();
         String gri = res.getGlobalReservationId();
         int bw = res.getReservedConstraint().getBandwidth();
         String cmd = "{\"type\":\"oscars-request\", \"action\":\"teardown\", \"version\":\"1.0\", \n"
                 + "\"gri\":\"" + gri +"\", \"bandwidth\":\"" + Integer.toString(bw) +"Mbps\", \n"
                 + "\"path\":[\n";
         PathInfo pathInfo = res.getReservedConstraint().getPathInfo();

        List<CtrlPlaneHopContent> hops;
        try {
            hops = PathTools.getLocalHops(pathInfo.getPath(), PathTools.getLocalDomainId());
        } catch (OSCARSServiceException e) {
            throw new PSSException(e);
        }

         for (int i = 0; i < hops.size(); i++) {
             CtrlPlaneHopContent hop1 = hops.get(i);
             try {
                 if (!PathTools.getLocalDomainId().equals(NMWGParserUtil.normalizeURN(NMWGParserUtil.getURN(hop1, NMWGParserUtil.DOMAIN_TYPE)))) {
                     continue;
                 }
             } catch (OSCARSServiceException e) {
                 throw new PSSException("Malformed path hop object: id=" + hop1.getId());
             }
             i++;
             if (i == hops.size())
                 throw new PSSException("Odd number of hops in Path object");
             CtrlPlaneHopContent hop2 = hops.get(i);
             try {
                 if (!PathTools.getLocalDomainId().equals(NMWGParserUtil.normalizeURN(NMWGParserUtil.getURN(hop2, NMWGParserUtil.DOMAIN_TYPE)))) {
                     throw new PSSException("Malformed path object: odd number of local hops");
                 }                
             } catch (OSCARSServiceException e) {
                 throw new PSSException("Malformed path hop object: id=" + hop2.getId());
             }
             CtrlPlaneLinkContent link1 = hop1.getLink();
             CtrlPlaneLinkContent link2 = hop2.getLink();
             if (link1 == null || link2 == null)
                 throw new PSSException("Path Hop is not of Link type");
             URNParserResult urnParser1 = URNParser.parseTopoIdent(link1.getId());
             URNParserResult urnParser2 = URNParser.parseTopoIdent(link2.getId());
             String nodeIp = ConnectorUtils.getDeviceAddress(urnParser1.getNodeId());
             String nodeIp2 = ConnectorUtils.getDeviceAddress(urnParser2.getNodeId());
             String portId1 = urnParser1.getPortId();
             String portId2 = urnParser2.getPortId();
             String vlan1 = link1.getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().getSuggestedVLANRange();
             String vlan2 = link2.getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().getSuggestedVLANRange();
             if (!nodeIp.equals(nodeIp2))
                 throw new PSSException("Malformed path object: hops not paird up");
             cmd = cmd + "{\"switch\":\"" + nodeIp +"\", \"del-flows\": "
                 + "[{\"port\":\"" + portId1  + "\", \"vlan_range\":\"" + vlan1 + "\"},"
                 + " {\"port\":\"" + portId2 + "\", \"vlan_range\":\"" + vlan2 + "\"}]}";
             if (i == hops.size()-1)
                 cmd += "\n";
             else {
                 CtrlPlaneHopContent hop3 = hops.get(i+1);
                 try {
                     if (!PathTools.getLocalDomainId().equals(NMWGParserUtil.normalizeURN(NMWGParserUtil.getURN(hop3, NMWGParserUtil.DOMAIN_TYPE)))) {
                         cmd += "\n";
                         break;
                     }
                 } catch (OSCARSServiceException e) {
                     throw new PSSException("Malformed path hop object: id=" + hop3.getId());
                 }
                 cmd += ",\n";
             }
         }
         cmd += "]}";
         log.debug("getTeardown end");
         return cmd;
     }
     
     private String getVerify(PSSAction action) throws PSSException {
         log.debug("getVerify start");
         ResDetails res = action.getRequest().getStatusReq().getReservation();
         String gri = res.getGlobalReservationId();
         int bw = res.getReservedConstraint().getBandwidth();
         String cmd = "{\"type\":\"oscars-request\", \"action\":\"verify\", \"version\":\"1.0\", \n"
                 + "\"gri\":\"" + gri +"\", \"bandwidth\":\"" + Integer.toString(bw) +"Mbps\", \n"
                 + "\"path\":[\n";
         PathInfo pathInfo = res.getReservedConstraint().getPathInfo();

        List<CtrlPlaneHopContent> hops;
        try {
            hops = PathTools.getLocalHops(pathInfo.getPath(), PathTools.getLocalDomainId());
        } catch (OSCARSServiceException e) {
            throw new PSSException(e);
        }

         for (int i = 0; i < hops.size(); i++) {
             CtrlPlaneHopContent hop1 = hops.get(i);
             try {
                 if (!PathTools.getLocalDomainId().equals(NMWGParserUtil.normalizeURN(NMWGParserUtil.getURN(hop1, NMWGParserUtil.DOMAIN_TYPE)))) {
                     continue;
                 }
             } catch (OSCARSServiceException e) {
                 throw new PSSException("Malformed path hop object: id=" + hop1.getId());
             }
             i++;
             if (i == hops.size())
                 throw new PSSException("Odd number of hops in Path object");
             CtrlPlaneHopContent hop2 = hops.get(i);
             try {
                 if (!PathTools.getLocalDomainId().equals(NMWGParserUtil.normalizeURN(NMWGParserUtil.getURN(hop2, NMWGParserUtil.DOMAIN_TYPE)))) {
                     throw new PSSException("Malformed path object: odd number of local hops");
                 }                
             } catch (OSCARSServiceException e) {
                 throw new PSSException("Malformed path hop object: id=" + hop2.getId());
             }
             CtrlPlaneLinkContent link1 = hop1.getLink();
             CtrlPlaneLinkContent link2 = hop2.getLink();
             if (link1 == null || link2 == null)
                 throw new PSSException("Path Hop is not of Link type");
             URNParserResult urnParser1 = URNParser.parseTopoIdent(link1.getId());
             URNParserResult urnParser2 = URNParser.parseTopoIdent(link2.getId());
             String nodeIp = ConnectorUtils.getDeviceAddress(urnParser1.getNodeId());
             String nodeIp2 = ConnectorUtils.getDeviceAddress(urnParser2.getNodeId());
             String portId1 = urnParser1.getPortId();
             String portId2 = urnParser2.getPortId();
             String vlan1 = link1.getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().getSuggestedVLANRange();
             String vlan2 = link2.getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().getSuggestedVLANRange();
             if (!nodeIp.equals(nodeIp2))
                 throw new PSSException("Malformed path object: hops not paird up");
             cmd = cmd + "{\"switch\":\"" + nodeIp +"\", \"has-flows\": "
                 + "[{\"port\":\"" + portId1  + "\", \"vlan_range\":\"" + vlan1 + "\"},"
                 + " {\"port\":\"" + portId2 + "\", \"vlan_range\":\"" + vlan2 + "\"}]}";
             if (i == hops.size()-1)
                 cmd += "\n";
             else {
                 CtrlPlaneHopContent hop3 = hops.get(i+1);
                 try {
                     if (!PathTools.getLocalDomainId().equals(NMWGParserUtil.normalizeURN(NMWGParserUtil.getURN(hop3, NMWGParserUtil.DOMAIN_TYPE)))) {
                         cmd += "\n";
                         break;
                     }
                 } catch (OSCARSServiceException e) {
                     throw new PSSException("Malformed path hop object: id=" + hop3.getId());
                 }
                 cmd += ",\n";
             }
         }
         cmd += "]}";
         log.debug("getVerify end");
         return cmd;
     }
 }
