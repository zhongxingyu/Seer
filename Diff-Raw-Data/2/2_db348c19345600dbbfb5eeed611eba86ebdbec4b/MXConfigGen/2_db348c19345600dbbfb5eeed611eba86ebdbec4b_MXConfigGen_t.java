 package net.es.oscars.pss.eompls.junos;
 
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
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
 import net.es.oscars.pss.eompls.api.EoMPLSDeviceAddressResolver;
 import net.es.oscars.pss.eompls.api.EoMPLSIfceAddressResolver;
 import net.es.oscars.pss.eompls.beans.LSP;
 import net.es.oscars.pss.eompls.util.EoMPLSClassFactory;
 import net.es.oscars.pss.eompls.util.EoMPLSUtils;
 import net.es.oscars.pss.util.URNParser;
 import net.es.oscars.pss.util.URNParserResult;
 import net.es.oscars.utils.soap.OSCARSServiceException;
 import net.es.oscars.utils.topology.PathTools;
 
 public class MXConfigGen implements DeviceConfigGenerator {
     private Logger log = Logger.getLogger(MXConfigGen.class);
    
 
     
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
         ResDetails res = action.getRequest().getSetupReq().getReservation();
         
         String srcDeviceId = EoMPLSUtils.getDeviceId(res, false);
         String dstDeviceId = EoMPLSUtils.getDeviceId(res, true);
         boolean sameDevice = srcDeviceId.equals(dstDeviceId);
         if (sameDevice) {
             return "";
         } else {
             return this.getLSPStatus(action, deviceId);
         }
         
     }
     private String getSetup(PSSAction action, String deviceId) throws PSSException {
         log.debug("getSetup start");
         
         ResDetails res = action.getRequest().getSetupReq().getReservation();
         
         String srcDeviceId = EoMPLSUtils.getDeviceId(res, false);
         String dstDeviceId = EoMPLSUtils.getDeviceId(res, true);
         boolean sameDevice = srcDeviceId.equals(dstDeviceId);
         
         if (sameDevice) {
             return this.getSwitchingSetup(res, deviceId);
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
             return this.getSwitchingTeardown(res, deviceId);
         } else {
             return this.getLSPTeardown(res, deviceId);
         }
     }
     
     @SuppressWarnings("rawtypes")
     private String getLSPStatus(PSSAction action, String deviceId)  throws PSSException {
         String templateFile = "junos-mx-lsp-status.txt";
         Map root = new HashMap();
         String config       = EoMPLSUtils.generateConfig(root, templateFile);
         return config;
     }
 
     @SuppressWarnings({ "rawtypes", "unchecked" })
     private String getLSPSetup(ResDetails res, String deviceId) throws PSSException  {
         log.debug("getLSPSetup start");
 
         String templateFile = "junos-mx-lsp-setup.txt";
 
         String srcDeviceId = EoMPLSUtils.getDeviceId(res, false);
 
         String ifceName;
         String srcIfceName;
         String ifceDescription;
         String ifceVlan;
         String srcIfceVlan;
         String egressVlan;
         
         String policyName;
         String policyTerm;
         String communityName;
         String communityMembers;
         Long lspBandwidth;
         String pathName;
         String lspName;
         
         String l2circuitVCID;
         String l2circuitEgress;
         String l2circuitDescription;
         String policerName;
         Long policerBurstSizeLimit;
         Long policerBandwidthLimit;
         String statsFilterName;
         String statsFilterTerm;
         String statsFilterCount;
         String policingFilterName;
         String policingFilterTerm;
         String policingFilterCount;
 
         
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
         
         SDNNameGenerator ng = SDNNameGenerator.getInstance();
         String gri = res.getGlobalReservationId();
         
 
         
         // bandwidth in Mbps 
         lspBandwidth = 1000000L*bw;
         
         policerBandwidthLimit = lspBandwidth;
         policerBurstSizeLimit = lspBandwidth / 10;
 
         String lspTargetDeviceId;
         boolean reverse = false;
         log.debug("source edge device id is: "+srcDeviceId+", config to generate is for "+deviceId);
         if (srcDeviceId.equals(deviceId)) {
             // forward direction
             log.debug("forward");
             ifceName = srcRes.getPortId();
             srcIfceName = ifceName;
             ifceVlan = ingressLink.getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().getSuggestedVLANRange();
             srcIfceVlan = ifceVlan;
             egressVlan = egressLink.getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().getSuggestedVLANRange();
             lspTargetDeviceId = dstRes.getNodeId();
         } else {
             // reverse direction
             log.debug("reverse");
             ifceName = dstRes.getPortId();
             srcIfceName = srcRes.getPortId();
             ifceVlan = egressLink.getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().getSuggestedVLANRange();
             srcIfceVlan = ingressLink.getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().getSuggestedVLANRange();
            egressVlan = ingressLink.getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().getSuggestedVLANRange();
             lspTargetDeviceId = srcRes.getNodeId();
             reverse = true;
         }
         LSP lspBean = new LSP(deviceId, pi, dar, iar, reverse);
 
     
         ifceDescription = ng.getInterfaceDescription(gri, lspBandwidth);
 
         policingFilterName      = ng.getFilterName(gri, "policing");
         policingFilterTerm      = policingFilterName;
         policingFilterCount     = policingFilterName;
         statsFilterName         = ng.getFilterName(gri, "stats");
         statsFilterTerm         = statsFilterName;
         statsFilterCount        = statsFilterName;
         communityName           = ng.getCommunityName(gri);
         policyName              = ng.getPolicyName(gri);
         policyTerm              = policyName;
         policerName             = ng.getPolicerName(gri);
         pathName                = ng.getPathName(gri);
         lspName                 = ng.getLSPName(gri);
         l2circuitDescription    = ng.getL2CircuitDescription(gri);
         l2circuitEgress         = dar.getDeviceAddress(lspTargetDeviceId);
         l2circuitVCID           = EoMPLSUtils.genJunosVCId(srcIfceName, srcIfceVlan);
 
         // community is 30000 - 65500
         String oscarsCommunity;
         Random rand = new Random();
         Integer randInt = 30000 + rand.nextInt(35500);
         if (ng.getOscarsCommunity(gri) > 65535) {
             oscarsCommunity  = ng.getOscarsCommunity(gri)+"L";
         } else {
             oscarsCommunity  = ng.getOscarsCommunity(gri).toString();
         }
         
         communityMembers    = "65000:"+oscarsCommunity+":"+randInt;
         
         
         
         // create and populate the model
         // this needs to match with the template
         Map root = new HashMap();
         Map lsp = new HashMap();
         Map path = new HashMap();
         Map ifce = new HashMap();
         Map filters = new HashMap();
         Map stats = new HashMap();
         Map policing = new HashMap();
         Map community = new HashMap();
         Map policy = new HashMap();
         Map l2circuit = new HashMap();
         Map policer = new HashMap();
 
         root.put("lsp", lsp);
         root.put("path", path);
         root.put("ifce", ifce);
         root.put("filters", filters);
         root.put("policy", policy);
         root.put("policer", policer);
         root.put("l2circuit", l2circuit);
         root.put("community", community);
         root.put("egressvlan", egressVlan);
 
         filters.put("stats", stats);
         filters.put("policing", policing);
 
         stats.put("name", statsFilterName);
         stats.put("term", statsFilterTerm);
         stats.put("count", statsFilterCount);
         policing.put("name", policingFilterName);
         policing.put("term", policingFilterTerm);
         policing.put("count", policingFilterCount);
 
 
         ifce.put("name", ifceName);
         ifce.put("vlan", ifceVlan);
         ifce.put("description", ifceDescription);
 
         lsp.put("name", lspName);
         lsp.put("from", lspBean.getFrom());
         lsp.put("to", lspBean.getTo());
         lsp.put("bandwidth", lspBandwidth);
         
         path.put("hops", lspBean.getPathAddresses());
         path.put("name", pathName);
 
         l2circuit.put("egress", l2circuitEgress);
         l2circuit.put("vcid", l2circuitVCID);
         l2circuit.put("description", l2circuitDescription);
 
         policer.put("name", policerName);
         policer.put("burst_size_limit", policerBurstSizeLimit);
         policer.put("bandwidth_limit", policerBandwidthLimit);
 
 
         community.put("name", communityName);
         community.put("members", communityMembers);
 
         policy.put("name", policyName);
         policy.put("term", policyTerm);
 
         log.debug("getLSPSetup ready");
         String config       = EoMPLSUtils.generateConfig(root, templateFile);
 
         log.debug("getLSPSetup done");
         return config;
     }
     
     
     
     @SuppressWarnings({ "rawtypes", "unchecked" })
     private String getLSPTeardown(ResDetails res, String deviceId) throws PSSException {
 
         String templateFile = "junos-mx-lsp-teardown.txt";
 
         String srcDeviceId = EoMPLSUtils.getDeviceId(res, false);
 
         String ifceName;
         String ifceVlan;
         
         String policyName;
         String communityName;
         String pathName;
         String lspName;
         
         String l2circuitEgress;
         String policerName;
         String statsFilterName;
         String policingFilterName;
 
         
         EoMPLSClassFactory ecf = EoMPLSClassFactory.getInstance();
         
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
         
         
         EoMPLSDeviceAddressResolver dar = ecf.getEomplsDeviceAddressResolver();
         
         SDNNameGenerator ng = SDNNameGenerator.getInstance();
         String gri = res.getGlobalReservationId();
         /* *********************** */
         /* BEGIN POPULATING VALUES */
         /* *********************** */
 
 
         String lspTargetDeviceId;
         log.debug("source edge device id is: "+srcDeviceId+", config to generate is for "+deviceId);
         if (srcDeviceId.equals(deviceId)) {
             // forward direction
             log.debug("forward");
             ifceName = srcRes.getPortId();
             ifceVlan = ingressLink.getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().getSuggestedVLANRange();
             lspTargetDeviceId = dstRes.getNodeId();
         } else {
             // reverse direction
             log.debug("reverse");
             ifceName = dstRes.getPortId();
             ifceVlan = egressLink.getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().getSuggestedVLANRange();
             lspTargetDeviceId = srcRes.getNodeId();
         }
         policingFilterName      = ng.getFilterName(gri, "policing");
         statsFilterName         = ng.getFilterName(gri, "stats");
         communityName           = ng.getCommunityName(gri);
         policyName              = ng.getPolicyName(gri);
         policerName             = ng.getPolicerName(gri);
         pathName                = ng.getPathName(gri);
         lspName                 = ng.getLSPName(gri);
         l2circuitEgress         = dar.getDeviceAddress(lspTargetDeviceId);
 
 
 
         // create and populate the model
         // this needs to match with the template
         Map root = new HashMap();
         Map lsp = new HashMap();
         Map path = new HashMap();
         Map ifce = new HashMap();
         Map filters = new HashMap();
         Map stats = new HashMap();
         Map policing = new HashMap();
         Map community = new HashMap();
         Map policy = new HashMap();
         Map l2circuit = new HashMap();
         Map policer = new HashMap();
 
         root.put("lsp", lsp);
         root.put("path", path);
         root.put("ifce", ifce);
         root.put("filters", filters);
         root.put("policy", policy);
         root.put("policer", policer);
         root.put("l2circuit", l2circuit);
         root.put("community", community);
 
         filters.put("stats", stats);
         filters.put("policing", policing);
 
         ifce.put("name", ifceName);
         ifce.put("vlan", ifceVlan);
         
         stats.put("name", statsFilterName);
         policing.put("name", policingFilterName);
 
         lsp.put("name", lspName);
         
         path.put("name", pathName);
 
         l2circuit.put("egress", l2circuitEgress);
 
         policer.put("name", policerName);
 
         community.put("name", communityName);
 
         policy.put("name", policyName);
         
         
         log.debug("getLSPTeardown ready");
         String config       = EoMPLSUtils.generateConfig(root, templateFile);
 
         log.debug("getLSPTeardown done");
         return config;
     }
 
     @SuppressWarnings({ "unchecked", "rawtypes" })
     private String getSwitchingSetup(ResDetails res, String deviceId) throws PSSException {
         String templateFile = "junos-mx-sw-setup.txt";
         
         
         String ifceAName, ifceZName;
         String ifceAVlan, ifceZVlan;
         String ifceADesc, ifceZDesc;
         String filterName, filterTerm, filterCount;
         String iswitchName;
         String policerName;
         Long policerBurstSizeLimit;
         Long policerBandwidthLimit;
 
         /* *********************** */
         /* BEGIN POPULATING VALUES */
         /* *********************** */
         
         ReservedConstraintType rc = res.getReservedConstraint();
         Integer bw = rc.getBandwidth();
         PathInfo pi = rc.getPathInfo();
         CtrlPlaneLinkContent ingressLink = pi.getPath().getHop().get(0).getLink();
         CtrlPlaneLinkContent egressLink = pi.getPath().getHop().get(pi.getPath().getHop().size()-1).getLink();
         
         String srcLinkId = ingressLink.getId();
         URNParserResult srcRes = URNParser.parseTopoIdent(srcLinkId);
         String dstLinkId = egressLink.getId();
         URNParserResult dstRes = URNParser.parseTopoIdent(dstLinkId);
         
         
         
         SDNNameGenerator ng = SDNNameGenerator.getInstance();
         String gri = res.getGlobalReservationId();
         
 
         
         
         Long bandwidth = 1000000L*bw;
         
         policerBandwidthLimit = bandwidth;
         policerBurstSizeLimit = bandwidth / 10;
         
         ifceAName       = srcRes.getPortId();
         ifceZName       = dstRes.getPortId();
         ifceAVlan       = ingressLink.getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().getSuggestedVLANRange();
         ifceZVlan       = egressLink.getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().getSuggestedVLANRange();
         
         policerName     = ng.getPolicerName(gri);
         ifceADesc       = ng.getInterfaceDescription(gri, bandwidth);
         ifceZDesc       = ng.getInterfaceDescription(gri, bandwidth);
         iswitchName     = ng.getIswitchTerm(gri);
         
         filterName      = ng.getFilterName(gri, "policing");
         filterTerm      = filterName;
         filterCount     = filterName;
 
         // create and populate the model
         // this needs to match with the template
         Map root = new HashMap();
         Map ifce_a = new HashMap();
         Map ifce_z = new HashMap();
         Map filter = new HashMap();
         Map iswitch = new HashMap();
         Map policer = new HashMap();
 
         root.put("ifce_a", ifce_a);
         root.put("ifce_z", ifce_z);
         root.put("filter", filter);
         root.put("iswitch", iswitch);
         root.put("policer", policer);
 
 
         ifce_a.put("name", ifceAName);
         ifce_a.put("vlan", ifceAVlan);
         ifce_a.put("description", ifceADesc);
 
         ifce_z.put("name", ifceZName);
         ifce_z.put("vlan", ifceZVlan);
         ifce_z.put("description", ifceZDesc);
 
         filter.put("name", filterName);
         filter.put("term", filterTerm);
         filter.put("count", filterCount);
 
         iswitch.put("name", iswitchName);
 
         policer.put("name", policerName);
         policer.put("burst_size_limit", policerBurstSizeLimit);
         policer.put("bandwidth_limit", policerBandwidthLimit);
 
 
         log.debug("getSwitchingSetup ready");
         String config       = EoMPLSUtils.generateConfig(root, templateFile);
 
         log.debug("getSwitchingSetup done");
         return config;
     }
     
     @SuppressWarnings({ "rawtypes", "unchecked" })
     private String getSwitchingTeardown(ResDetails res, String deviceId) throws PSSException {
         String templateFile = "junos-mx-sw-teardown.txt";
         String ifceAName, ifceZName;
         String ifceAVlan, ifceZVlan;
         String filterName;
         String iswitchName;
         String policerName;
 
         /* *********************** */
         /* BEGIN POPULATING VALUES */
         /* *********************** */
         
         ReservedConstraintType rc = res.getReservedConstraint();
         PathInfo pi = rc.getPathInfo();
         
         CtrlPlaneLinkContent ingressLink = pi.getPath().getHop().get(0).getLink();
         CtrlPlaneLinkContent egressLink = pi.getPath().getHop().get(pi.getPath().getHop().size()-1).getLink();
         
         String srcLinkId = ingressLink.getId();
         URNParserResult srcRes = URNParser.parseTopoIdent(srcLinkId);
         String dstLinkId = egressLink.getId();
         URNParserResult dstRes = URNParser.parseTopoIdent(dstLinkId);
         
         SDNNameGenerator ng = SDNNameGenerator.getInstance();
         String gri = res.getGlobalReservationId();
         
         ifceAName       = srcRes.getPortId();
         ifceZName       = dstRes.getPortId();
         ifceAVlan       = ingressLink.getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().getSuggestedVLANRange();
         ifceZVlan       = egressLink.getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().getSuggestedVLANRange();
         policerName     = ng.getPolicerName(gri);
         iswitchName     = ng.getIswitchTerm(gri);
         
         filterName      = ng.getFilterName(gri, "policing");
 
 
         // create and populate the model
         // this needs to match with the template
         Map root = new HashMap();
         Map ifce_a = new HashMap();
         Map ifce_z = new HashMap();
         Map filter = new HashMap();
         Map iswitch = new HashMap();
         Map policer = new HashMap();
 
         root.put("ifce_a", ifce_a);
         root.put("ifce_z", ifce_z);
         root.put("filter", filter);
         root.put("iswitch", iswitch);
         root.put("policer", policer);
 
 
         ifce_a.put("name", ifceAName);
         ifce_a.put("vlan", ifceAVlan);
 
         ifce_z.put("name", ifceZName);
         ifce_z.put("vlan", ifceZVlan);
 
         filter.put("name", filterName);
 
         iswitch.put("name", iswitchName);
 
         policer.put("name", policerName);
 
         
         log.debug("getSwitchingTeardown ready");
         String config       = EoMPLSUtils.generateConfig(root, templateFile);
 
         log.debug("getSwitchingTeardown done");
         return config;
     }
     
     
     public void setConfig(GenericConfig config) throws PSSException {
         // TODO Auto-generated method stub
     }
 
 
 }
