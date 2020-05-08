 package net.es.oscars.pss.eompls.alu;
 
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
 import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneLinkContent;
 
 import net.es.oscars.api.soap.gen.v06.PathInfo;
 import net.es.oscars.api.soap.gen.v06.ResDetails;
 import net.es.oscars.api.soap.gen.v06.ReservedConstraintType;
 import net.es.oscars.pss.api.DeviceConfigGenerator;
 import net.es.oscars.pss.beans.PSSAction;
 import net.es.oscars.pss.beans.PSSException;
 import net.es.oscars.pss.beans.config.GenericConfig;
 import net.es.oscars.pss.enums.ActionStatus;
 import net.es.oscars.pss.eompls.api.EoMPLSDeviceAddressResolver;
 import net.es.oscars.pss.eompls.api.EoMPLSIfceAddressResolver;
 import net.es.oscars.pss.eompls.beans.LSP;
 import net.es.oscars.pss.eompls.util.EoMPLSClassFactory;
 import net.es.oscars.pss.eompls.util.EoMPLSUtils;
 import net.es.oscars.pss.util.URNParser;
 import net.es.oscars.pss.util.URNParserResult;
 import net.es.oscars.pss.util.VlanGroupConfig;
 import net.es.oscars.utils.soap.OSCARSServiceException;
 import net.es.oscars.utils.topology.PathTools;
 
 public class SC11_SRConfigGen implements DeviceConfigGenerator {
     private Logger log = Logger.getLogger(SC11_SRConfigGen.class);
     
     private HashMap<String, String[]> multipointHacks = new HashMap<String, String[]>();
     private HashMap<String, String> qosSetupHacks = new HashMap<String, String>();
     private HashMap<String, String> teardownHacks = new HashMap<String, String>();
     private ArrayList<String> setupHacks = new ArrayList<String>();
 
     
     
     public SC11_SRConfigGen() throws PSSException {
 
         VlanGroupConfig.configure();
 
 
         String[] nersc = {"9/1/1", "9/1/2", "9/1/3", "9/1/4", "10/1/3", "10/1/4", "10/1/5", "10/1/6", "10/1/7", "10/1/8", "10/1/9", "10/1/10", "10/1/11"};
         multipointHacks.put("nersc-ani:10ges", nersc);
 
         String[] star = {"10/1/1", "10/1/2",  "10/1/3", "10/1/4", "10/1/5", "10/1/6", "10/1/7", "10/1/8", "10/1/9", "10/1/10", "10/1/11", "10/1/12"};
         multipointHacks.put("star-ani:10ges", star);
 
         qosSetupHacks.put("SC11-SHARED", "3010");
         qosSetupHacks.put("SC11-100G", "3100");
         qosSetupHacks.put("SC11-40G", "3040");
         qosSetupHacks.put("SC11-60G", "3060");
         teardownHacks.put("SC11", "3001");
         setupHacks.add("NOSETUP");
     }
    
 
     
     public String getConfig(PSSAction action, String deviceId) throws PSSException {
         switch (action.getActionType()) {
             case SETUP :
                 return this.getSetup(action, deviceId);
             case TEARDOWN:
                 return this.getTeardown(action, deviceId);
             case STATUS:
                 return this.getStatus(action, deviceId);
             case MODIFY:
                 throw new PSSException("Modify not supported");
         }
         throw new PSSException("Invalid action type");
     }
     
     private String getStatus(PSSAction action, String deviceId) throws PSSException {
         action.setStatus(ActionStatus.SUCCESS);
         return "";
     }
     private String getSetup(PSSAction action, String deviceId) throws PSSException {
         log.debug("getSetup start");
         
         ResDetails res = action.getRequest().getSetupReq().getReservation();
         
         String srcDeviceId = EoMPLSUtils.getDeviceId(res, false);
         String dstDeviceId = EoMPLSUtils.getDeviceId(res, true);
         boolean sameDevice = srcDeviceId.equals(dstDeviceId);
         
         if (sameDevice) {
             throw new PSSException("Same device crossconnects not supported on IOS");
         } else {
             return this.getLSPSetup(res, deviceId);
         }
     }
     
     
     private String getTeardown(PSSAction action, String deviceId) throws PSSException {
         log.debug("getTeardown start");
         
         ResDetails res = action.getRequest().getTeardownReq().getReservation();
         
         String srcDeviceId = EoMPLSUtils.getDeviceId(res, false);
         String dstDeviceId = EoMPLSUtils.getDeviceId(res, true);
         boolean sameDevice = srcDeviceId.equals(dstDeviceId);
         
         if (sameDevice) {
             throw new PSSException("Same device crossconnects not supported on IOS");
         } else {
             return this.getLSPTeardown(res, deviceId);
         }
     }
 
     @SuppressWarnings({ "unchecked", "rawtypes" })
     private String getLSPTeardown(ResDetails res, String deviceId) throws PSSException {
         String templateFile = "alu-vpls-sc11-teardown.txt";
 
         String srcDeviceId = EoMPLSUtils.getDeviceId(res, false);
         String dstDeviceId = EoMPLSUtils.getDeviceId(res, true);
        
         String ingQosId;
         String epipeId;
         String sdpId;
         String pathName;
         String lspName;
         String ifceName;
         String resvVlan;
         
         
         
         ReservedConstraintType rc = res.getReservedConstraint();
         PathInfo pi = rc.getPathInfo();
 
         List<CtrlPlaneHopContent> localHops;
         try {
             localHops = PathTools.getLocalHops(pi.getPath(), PathTools.getLocalDomainId());
         } catch (OSCARSServiceException e) {
             throw new PSSException(e);
         }
         
         CtrlPlaneLinkContent ingressLink = localHops.get(0).getLink();
         CtrlPlaneLinkContent egressLink = localHops.get(localHops.size()-1).getLink();
                 
         String srcLinkId = ingressLink.getId();
         URNParserResult srcRes = URNParser.parseTopoIdent(srcLinkId);
         String dstLinkId = egressLink.getId();
         URNParserResult dstRes = URNParser.parseTopoIdent(dstLinkId);
         
 
         log.debug("source edge device id is: "+srcDeviceId+", config to generate is for "+deviceId);
         if (srcDeviceId.equals(deviceId)) {
             // forward direction
             log.debug("forward");
             ifceName = srcRes.getPortId();
            resvVlan = ingressLink.getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().getSuggestedVLANRange();
         } else {
             // reverse direction
             log.debug("reverse");
             ifceName = dstRes.getPortId();
            resvVlan = egressLink.getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().getSuggestedVLANRange();
         }
         
         ALUNameGenerator ng = ALUNameGenerator.getInstance();
 
 
         pathName                = ng.getPathName(resvVlan);
         lspName                 = ng.getLSPName(resvVlan);
         ingQosId                = ng.getQosId(resvVlan);
         sdpId                   = ng.getSdpId(resvVlan);
         epipeId                 = ng.getEpipeId(resvVlan);
 
 
         Map root = new HashMap();
         Map lsp = new HashMap();
         Map path = new HashMap();
         Map epipe = new HashMap();
         Map sdp = new HashMap();
 
         ArrayList ifces = new ArrayList();
             
         
         boolean teardownqos = true;
 
         String description = res.getDescription();
         for (String hack : teardownHacks.keySet()) {
             if (description.contains(hack)) {
                 teardownqos = false;
                 ingQosId = teardownHacks.get(hack);
             }
         }
         root.put("teardownqos", teardownqos);
 
         ArrayList<String> vlans = VlanGroupConfig.getVlans(deviceId, ifceName, resvVlan);
 
         
         String devIfce = deviceId+":"+ifceName;
         if (multipointHacks.keySet().contains(devIfce)) {
             for (String port : multipointHacks.get(devIfce)) {
                 ifces.add(port);
             }
         } else {
             ifces.add(ifceName);
         }        
         
         // set up data model structure
         root.put("path", path);
         root.put("lsp", lsp);
         root.put("ifces", ifces);
         root.put("vlans", vlans);
         root.put("epipe", epipe);
         root.put("sdp", sdp);
         root.put("ingqosid", ingQosId);
 
         
         path.put("name", pathName);
         lsp.put("name", lspName);
         epipe.put("id", epipeId);
         sdp.put("id", sdpId);
         
         
         
         String config       = EoMPLSUtils.generateConfig(root, templateFile);
         log.debug("getLSPSetup done");
         return config;
 
     }
 
     @SuppressWarnings({ "rawtypes", "unchecked" })
     private String getLSPSetup(ResDetails res, String deviceId) throws PSSException  {
 
         String templateFile = "alu-vpls-sc11-setup.txt";
 
         String srcDeviceId = EoMPLSUtils.getDeviceId(res, false);
         String dstDeviceId = EoMPLSUtils.getDeviceId(res, true);
         
 
         String ifceName;
         
         String pathName;
 
         String lspName;
         String lspFrom;
         String lspTo;
         
         String ingQosId;
         String ingQosDesc;
         Long ingQosBandwidth;
         
         String epipeId;
         String epipeDesc;
 
         String sdpId;
         String sdpDesc;
         
         
         String gri = res.getGlobalReservationId();
         ALUNameGenerator ng = ALUNameGenerator.getInstance();
 
 
         
         EoMPLSClassFactory ecf = EoMPLSClassFactory.getInstance();
         /* *********************** */
         /* BEGIN POPULATING VALUES */
         /* *********************** */
         
 
         ReservedConstraintType rc = res.getReservedConstraint();
         Integer bw = rc.getBandwidth();
         PathInfo pi = rc.getPathInfo();
         
         List<CtrlPlaneHopContent> localHops;
         try {
             localHops = PathTools.getLocalHops(pi.getPath(), PathTools.getLocalDomainId());
         } catch (OSCARSServiceException e) {
             throw new PSSException(e);
         }
         
         CtrlPlaneLinkContent ingressLink = localHops.get(0).getLink();
         CtrlPlaneLinkContent egressLink = localHops.get(localHops.size()-1).getLink();
         
         String srcLinkId = ingressLink.getId();
         URNParserResult srcRes = URNParser.parseTopoIdent(srcLinkId);
         String dstLinkId = egressLink.getId();
         URNParserResult dstRes = URNParser.parseTopoIdent(dstLinkId);
         
         EoMPLSIfceAddressResolver iar = ecf.getEomplsIfceAddressResolver();
         EoMPLSDeviceAddressResolver dar = ecf.getEomplsDeviceAddressResolver();
         
         
         
         
 
         
         // bandwidth in Mbps 
         ingQosBandwidth = 1L*bw;
         String resvVlan;
 
         String lspTargetDeviceId, lspOriginDeviceId;
         boolean reverse = false;
         log.debug("source edge device id is: "+srcDeviceId+", config to generate is for "+deviceId);
         if (srcDeviceId.equals(deviceId)) {
             // forward direction
             log.debug("forward");
             ifceName = srcRes.getPortId();
            resvVlan = ingressLink.getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().getSuggestedVLANRange();
             lspOriginDeviceId = srcRes.getNodeId();
             lspTargetDeviceId = dstRes.getNodeId();
         } else {
             // reverse direction
             log.debug("reverse");
             ifceName = dstRes.getPortId();
            resvVlan = egressLink.getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().getSuggestedVLANRange();
             lspOriginDeviceId = dstRes.getNodeId();
             lspTargetDeviceId = srcRes.getNodeId();
             reverse = true;
         }
         lspFrom         = dar.getDeviceAddress(lspOriginDeviceId);
         lspTo           = dar.getDeviceAddress(lspTargetDeviceId);
 
         LSP lspBean = new LSP(deviceId, pi, dar, iar, reverse);
 
     
         ingQosId                = ng.getQosId(resvVlan);
         sdpId                   = ng.getSdpId(resvVlan);
         epipeId                 = ng.getEpipeId(resvVlan);
         pathName                = ng.getPathName(resvVlan);
         lspName                 = ng.getLSPName(resvVlan);
         
         
         ingQosDesc              = gri;
         sdpDesc                 = gri;
         epipeDesc               = gri;
 
         Map root = new HashMap();
         Map lsp = new HashMap();
         Map path = new HashMap();
         Map epipe = new HashMap();
         Map sdp = new HashMap();
         Map ingqos = new HashMap();
         ArrayList hops = new ArrayList();
         ArrayList ifces = new ArrayList();
             
         
         boolean createqos = true;
 
         String description = res.getDescription();
         for (String hack : qosSetupHacks.keySet()) {
             if (description.contains(hack)) {
                 createqos = false;
                 ingQosId = qosSetupHacks.get(hack);
             }
         }
         root.put("createqos", createqos);
         
         boolean createpath = true;
         for (String hack : setupHacks) {
             if (description.contains(hack)) {
                 createpath = false;
             }
             
         }
         root.put("createpath", createpath);
         ArrayList<String> vlans = VlanGroupConfig.getVlans(deviceId, ifceName, resvVlan);
         
         String devIfce = deviceId+":"+ifceName;
         if (multipointHacks.keySet().contains(devIfce)) {
             for (String port : multipointHacks.get(devIfce)) {
                 ifces.add(port);
             }
         } else {
             ifces.add(ifceName);
         }
 
         // set up data model structure
         root.put("path", path);
         root.put("lsp", lsp);
         root.put("ifces", ifces);
         root.put("epipe", epipe);
         root.put("sdp", sdp);
         root.put("ingqos", ingqos);
         root.put("vlans", vlans);
 
         // fill in scalars
         
         
         path.put("name", pathName);        
         path.put("hops", hops);
         int i = 5;
         for (String ipaddress : lspBean.getPathAddresses()) {
             Map hop = new HashMap();
             hop.put("address", ipaddress);
             hop.put("order", i);
             i += 5;
             hops.add(hop);
         }
 
         lsp.put("name", lspName);
         lsp.put("from", lspFrom);
         lsp.put("to", lspTo);
         
         
         ingqos.put("id", ingQosId);
         ingqos.put("description", ingQosDesc);
         ingqos.put("bandwidth", ingQosBandwidth);
 
         epipe.put("id", epipeId);
         epipe.put("description", epipeDesc);
 
         sdp.put("id", sdpId);
         sdp.put("description", sdpDesc);
 
         String config       = EoMPLSUtils.generateConfig(root, templateFile);
 
         log.debug("getLSPSetup done");
         return config;
     }
     
     
     
     public void setConfig(GenericConfig config) throws PSSException {
         // TODO Auto-generated method stub
     }
 
 
 }
