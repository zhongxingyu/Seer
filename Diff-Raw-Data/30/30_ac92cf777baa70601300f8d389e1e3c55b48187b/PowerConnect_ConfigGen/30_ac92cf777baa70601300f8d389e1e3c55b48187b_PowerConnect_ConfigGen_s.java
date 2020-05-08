 package net.es.oscars.pss.eompls.dell;
 
 
 import net.es.oscars.api.soap.gen.v06.PathInfo;
 import net.es.oscars.api.soap.gen.v06.ResDetails;
 import net.es.oscars.api.soap.gen.v06.ReservedConstraintType;
 import net.es.oscars.pss.api.DeviceConfigGenerator;
 import net.es.oscars.pss.beans.PSSAction;
 import net.es.oscars.pss.beans.PSSException;
 import net.es.oscars.pss.beans.config.GenericConfig;
 import net.es.oscars.pss.enums.ActionStatus;
 import net.es.oscars.pss.enums.ActionType;
 import net.es.oscars.pss.eompls.alu.ALUNameGenerator;
 import net.es.oscars.pss.eompls.alu.SRIfceInfo;
 import net.es.oscars.pss.eompls.alu.SR_VPLS_DeviceIdentifiers;
 import net.es.oscars.pss.eompls.alu.SR_VPLS_TemplateParams;
 import net.es.oscars.pss.eompls.api.EoMPLSDeviceAddressResolver;
 import net.es.oscars.pss.eompls.api.EoMPLSIfceAddressResolver;
 import net.es.oscars.pss.eompls.beans.LSP;
 import net.es.oscars.pss.eompls.dao.GCUtils;
 import net.es.oscars.pss.eompls.util.EoMPLSClassFactory;
 import net.es.oscars.pss.eompls.util.EoMPLSUtils;
 import net.es.oscars.pss.eompls.util.VPLS_DomainIdentifiers;
 import net.es.oscars.pss.util.URNParser;
 import net.es.oscars.pss.util.URNParserResult;
 import net.es.oscars.pss.util.VlanGroupConfig;
 import net.es.oscars.utils.config.ConfigException;
 import net.es.oscars.utils.soap.OSCARSServiceException;
 import net.es.oscars.utils.topology.PathTools;
 import org.apache.log4j.Logger;
 import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
 import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneLinkContent;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class PowerConnect_ConfigGen implements DeviceConfigGenerator {
     private Logger log = Logger.getLogger(PowerConnect_ConfigGen.class);
     public PowerConnect_ConfigGen() throws ConfigException, PSSException {
 
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
     public String getSetup(PSSAction action, String deviceId) throws PSSException {
         log.debug("getSetup start");
         return this.onSetup(action, deviceId);
     }
 
     public String getTeardown(PSSAction action, String deviceId) throws PSSException {
         log.debug("getTeardown start");
         return this.onTeardown(action, deviceId);
 
     }
 
     private String onSetup(PSSAction action, String deviceId) throws PSSException {
 
         ResDetails res = action.getRequest().getSetupReq().getReservation();
         String gri = res.getGlobalReservationId();
 
 
         String devSetupConfig = GCUtils.retrieveDeviceConfig(gri, deviceId, ActionType.SETUP);
         if (devSetupConfig == null) {
             log.info("no saved setup config found for gri: "+gri+" device: "+deviceId+", generating now");
             devSetupConfig= this.generateConfig(res, deviceId, ActionType.SETUP);
             GCUtils.storeDeviceConfig(gri, deviceId, ActionType.SETUP, devSetupConfig);
         }
 
         String devTeardownConfig = GCUtils.retrieveDeviceConfig(gri, deviceId, ActionType.TEARDOWN);
         if (devTeardownConfig == null) {
             log.info("no saved teardown config found for gri: "+gri+" device: "+deviceId+", generating now");
             devTeardownConfig = this.generateConfig(res, deviceId, ActionType.TEARDOWN);
             GCUtils.storeDeviceConfig(gri, deviceId, ActionType.TEARDOWN, devTeardownConfig);
         }
 
         String setupConfig = GCUtils.retrieveDeviceConfig(gri, deviceId, ActionType.SETUP);
         if (setupConfig == null) {
             this.onError("could not retrieve setup device config for gri: "+gri+" device: "+deviceId);
         }
 
         return setupConfig;
     }
 
     private String onTeardown(PSSAction action, String deviceId) throws PSSException {
 
 
         ResDetails res = action.getRequest().getTeardownReq().getReservation();
         String gri = res.getGlobalReservationId();
 
         String teardownConfig = GCUtils.retrieveDeviceConfig(gri, deviceId, ActionType.TEARDOWN);
         if (teardownConfig == null) {
             log.info("no saved teardown config found for gri: "+gri+" device: "+deviceId+", generating now");
             teardownConfig = this.generateConfig(res, deviceId, ActionType.TEARDOWN);
             GCUtils.storeDeviceConfig(gri, deviceId, ActionType.TEARDOWN, teardownConfig);
         }
 
         SR_VPLS_DeviceIdentifiers.release(gri, deviceId);
         return teardownConfig;
 
     }
 
     private void onError(String errStr) throws PSSException {
         log.error(errStr);
         throw new PSSException(errStr);
     }
 
     public String generateConfig(ResDetails res, String deviceId, ActionType phase)  throws PSSException {
         String templateFile = null;
 
         if (phase.equals(ActionType.SETUP)) {
             templateFile = "dell-setup.txt";
         } else if (phase.equals(ActionType.TEARDOWN)) {
             templateFile = "dell-teardown.txt";
         } else {
             this.onError("invalid phase");
         }
         Map root = new HashMap();
         HashMap<String, ArrayList<SRIfceInfo>> devInfo = this.getDeviceIfceInfo(res);
         ArrayList<SRIfceInfo> ifceInfos = devInfo.get(deviceId);
         List<String> ifces = new ArrayList<String>();
         String vlan = "";
         for (SRIfceInfo ifceInfo : ifceInfos) {
             ifces.add(ifceInfo.getName());
             vlan = ifceInfo.getVlan();
         }
 
 
         root.put("ifces", ifces);
         root.put("vlan", vlan);
 
         String config = EoMPLSUtils.generateConfig(root, templateFile);
         return config;
     }
 
     public void setConfig(GenericConfig config) throws PSSException {
         // TODO Auto-generated method stub
     }
     private HashMap<String, ArrayList<SRIfceInfo>> getDeviceIfceInfo(ResDetails res) throws PSSException {
         String gri = res.getGlobalReservationId();
 
         String srcDeviceId = EoMPLSUtils.getDeviceId(res, false);
         String dstDeviceId = EoMPLSUtils.getDeviceId(res, true);
 
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
 
        String srcVlan = ingressLink.getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().getSuggestedVLANRange();
        String dstVlan = egressLink.getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().getSuggestedVLANRange();
 
         HashMap<String, ArrayList<SRIfceInfo>> deviceIfceInfo = new HashMap<String, ArrayList<SRIfceInfo>>();
         deviceIfceInfo.put(srcDeviceId, new ArrayList<SRIfceInfo>());
         deviceIfceInfo.put(dstDeviceId, new ArrayList<SRIfceInfo>());
         SRIfceInfo aIfceInfo = new SRIfceInfo();
         aIfceInfo.setDescription(gri);
         aIfceInfo.setName(srcRes.getPortId());
         aIfceInfo.setVlan(srcVlan);
         log.debug("setting "+srcDeviceId+" : "+aIfceInfo.getName());
         deviceIfceInfo.get(srcDeviceId).add(aIfceInfo);
 
         SRIfceInfo zIfceInfo = new SRIfceInfo();
         zIfceInfo.setDescription(gri);
         zIfceInfo.setName(dstRes.getPortId());
         zIfceInfo.setVlan(dstVlan);
         log.debug("setting "+dstDeviceId+" : "+zIfceInfo.getName());
         deviceIfceInfo.get(dstDeviceId).add(zIfceInfo);
 
         return deviceIfceInfo;
     }
 
 
 }
