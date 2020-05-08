 package net.es.oscars.pss.eompls.verify;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 
 import net.es.oscars.utils.soap.OSCARSServiceException;
 import net.es.oscars.utils.topology.PathTools;
 import org.apache.log4j.Logger;
 import org.jaxen.JaxenException;
 import org.jaxen.SimpleNamespaceContext;
 import org.jaxen.XPath;
 import org.jaxen.jdom.JDOMXPath;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.JDOMException;
 import org.jdom.input.SAXBuilder;
 import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
 import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneLinkContent;
 
 import net.es.oscars.api.soap.gen.v06.PathInfo;
 import net.es.oscars.api.soap.gen.v06.ResDetails;
 import net.es.oscars.api.soap.gen.v06.ReservedConstraintType;
 import net.es.oscars.pss.api.Verifier;
 import net.es.oscars.pss.beans.PSSAction;
 import net.es.oscars.pss.beans.PSSException;
 import net.es.oscars.pss.beans.config.GenericConfig;
 import net.es.oscars.pss.enums.ActionStatus;
 import net.es.oscars.pss.enums.ActionType;
 import net.es.oscars.pss.eompls.api.EoMPLSDeviceAddressResolver;
 import net.es.oscars.pss.eompls.service.EoMPLSService;
 import net.es.oscars.pss.eompls.util.EoMPLSClassFactory;
 import net.es.oscars.pss.eompls.util.EoMPLSUtils;
 import net.es.oscars.pss.soap.gen.TeardownReqContent;
 import net.es.oscars.pss.util.ConnectorUtils;
 import net.es.oscars.pss.util.URNParser;
 import net.es.oscars.pss.util.URNParserResult;
 
 public class EoMPLSVerifier implements Verifier {
     private GenericConfig config;
     private Logger log = Logger.getLogger(EoMPLSVerifier.class);
     private boolean performVerify;
     private boolean cleanupOnFail;
     private int verifyTries;
     private int delaySec;
     private int tryIntervalSec;
     
     public PSSAction verify(PSSAction action, String deviceId) throws PSSException {
         if (performVerify == false) {
             // automatic immediate success
             log.debug("not performing verify because of config, returning success");
             action.setStatus(ActionStatus.SUCCESS);
             return action;
         }
         // no need to verify a status
         if (action.getActionType().equals(ActionType.STATUS)) {
             log.debug("not performing verify because action is status check, returning success");
             action.setStatus(ActionStatus.SUCCESS);
             return action;
         } else if (action.getActionType().equals(ActionType.MODIFY)) {
             // TODO: modify always fails (for now)
             log.debug("not performing verify because action is modify, returning failure");
             action.setStatus(ActionStatus.FAIL);
             return action;
         }
             
         // first sleep 
         try {
             Thread.sleep(delaySec * 1000);
         } catch (InterruptedException e) {
             throw new PSSException(e);
         }
         
         // then send a status check
         boolean decided = false;
         boolean success = false;
         
         // keep trying the status check until 
         // either success OR verifyTries is reached
         int tries = 1;
         while (!decided) {
             while (tries <= verifyTries) {
                 log.debug("verify try");
                 tries++;
                 log.debug("verify try: "+tries+" of "+verifyTries+" starting");
                 success = this.checkStatus(deviceId, action);
                 if (success) {
                     log.debug("verify try: "+tries+" succeeded");
                     decided = true;
                 } else {
                     log.debug("verify try: "+tries+" failed, will try again in "+delaySec+" seconds");
                     try {
                         Thread.sleep(delaySec * 1000);
                     } catch (InterruptedException e) {
                         throw new PSSException(e);
                     }
                 }
             }
             decided = true;
         }
         
         // by now it's either successful so return
         if (success) {
             action.setStatus(ActionStatus.SUCCESS);
             log.debug("verify success, returning");
             return action;
             
         // or failed & we need to clean up 
         } else if (cleanupOnFail) {
             log.debug("verify failed all attempts, cleaning up");
             if (action.getActionType().equals(ActionType.SETUP)) {
                 log.error("cleaning up setup: tearing down");
                 PSSAction cleanupAction = new PSSAction();
                 cleanupAction.setActionType(ActionType.TEARDOWN);
                 cleanupAction.setRequest(action.getRequest());
                 cleanupAction.setStatus(ActionStatus.OUTSTANDING);
                 
                 TeardownReqContent td = new TeardownReqContent();
                 td.setReservation(action.getRequest().getSetupReq().getReservation());
                 td.setCallbackEndpoint(action.getRequest().getSetupReq().getCallbackEndpoint());
                 td.setTransactionId(action.getRequest().getSetupReq().getTransactionId());
                 
                 cleanupAction.getRequest().setTeardownReq(td);
                 
                 this.cleanup(cleanupAction, deviceId);
             } else if (action.getActionType().equals(ActionType.TEARDOWN)) {
                 log.error("cleaning up teardown: don't know how");
                 // can't cleanup a failed teardown
                 // TODO: notify IDC admins
             }
             action.setStatus(ActionStatus.FAIL);
             return action;
         // or we don't need to clean up
         } else {
             action.setStatus(ActionStatus.FAIL);
             return action;
         }
     }
     
     @SuppressWarnings("rawtypes")
     private boolean checkStatus(String deviceId, PSSAction prevAction) throws PSSException {
         boolean success = false;
         
         ActionType prevActionType = prevAction.getActionType();
         ResDetails res;
         if (prevActionType.equals(ActionType.SETUP)) {
             res = prevAction.getRequest().getSetupReq().getReservation();
         } else if (prevActionType.equals(ActionType.TEARDOWN)) {
             res = prevAction.getRequest().getTeardownReq().getReservation();
         } else {
             throw new PSSException("cannot check status unless previous action was setup or teardown");
         }
         String gri = res.getGlobalReservationId();
         log.debug("checking status for "+gri+" action: "+prevActionType);
         
         PSSAction statusAction = new PSSAction();
         statusAction.setActionType(ActionType.STATUS);
         statusAction.setRequest(prevAction.getRequest());
         statusAction.setStatus(ActionStatus.OUTSTANDING);
         String resultString = ConnectorUtils.sendAction(statusAction, deviceId, EoMPLSService.SVC_ID);
         log.debug("status result string:\n"+resultString);
         
         
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
         String srcDeviceId = EoMPLSUtils.getDeviceId(res, false);
 
         String ifceName, ifceVlan, lspTargetDeviceId;
         if (srcDeviceId.equals(deviceId)) {
             // forward direction
             log.debug("forward");
             ifceName = srcRes.getPortId();
            ifceVlan = ingressLink.getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().getVlanRangeAvailability();
             lspTargetDeviceId = dstRes.getNodeId();
         } else {
             // reverse direction
             log.debug("reverse");
             ifceName = dstRes.getPortId();
            ifceVlan = egressLink.getSwitchingCapabilityDescriptors().getSwitchingCapabilitySpecificInfo().getVlanRangeAvailability();
             lspTargetDeviceId = srcRes.getNodeId();
         }
         String ifceFullName = ifceName+"."+ifceVlan;
         EoMPLSClassFactory ecf = EoMPLSClassFactory.getInstance();
         EoMPLSDeviceAddressResolver dar = ecf.getEomplsDeviceAddressResolver();
 
         String l2circuitEgress         = dar.getDeviceAddress(lspTargetDeviceId);
 
         
         // XML parsing bit
         // NOTE WELL: if response format changes, this won't work
         SAXBuilder sb = new SAXBuilder();
         Document responseDoc = null;
         try {
             responseDoc = sb.build(new StringReader(resultString));
         } catch (JDOMException e) {
             throw new PSSException(e);
         } catch (IOException e) {
             throw new PSSException(e);
         }
         Element root = responseDoc.getRootElement();
         // this is element "rpc-reply"
         Element rpcReply = (Element) root.getChildren().get(0);
         // firstChild will be "l2circuit-connection-information"
         // we should get the namespace from that element because it changes
         // with each JunOS release.. 
         Element firstChild = (Element) rpcReply.getChildren().get(0);
         String uri = firstChild.getNamespaceURI();
         
         HashMap<String, String> nsmap = new HashMap<String, String>();
         nsmap.put( "ns", uri);
 
         String xpathExpr = "//ns:l2circuit-neighbor[ns:neighbor-address='"+l2circuitEgress+"']/ns:connection[ns:local-interface/ns:interface-name='"+ifceFullName+"']";
         
         log.debug("xpath is: "+xpathExpr);
         
         XPath xpath;
         Element conn = null;
         try {
             xpath = new JDOMXPath(xpathExpr);
             xpath.setNamespaceContext(new SimpleNamespaceContext(nsmap));
             conn = (Element) xpath.selectSingleNode(responseDoc);
         } catch (JaxenException e1) {
             // TODO Auto-generated catch block
             e1.printStackTrace();
         }
         String connectionStatus = "";
         String ifceStatus = "";
         boolean isVCup = false;
         boolean isVCConfigured = false;
 
         if (conn == null) {
             log.info("could not locate connection XML node, will retry");
         } else {
             isVCConfigured = true;
 
             List connectionChildren = conn.getChildren();
             for (Iterator j = connectionChildren.iterator(); j.hasNext();) {
                 Element e = (Element) j.next();
     
                 if (e.getName().equals("connection-status")) {
                     connectionStatus = e.getText();
                     log.debug("conn status : "+connectionStatus);
                 } else if (e.getName().equals("local-interface")) {
                     List localInterfaces = e.getChildren();
                     for (Iterator k = localInterfaces.iterator(); k.hasNext();) {
                         Element ifceElem = (Element) k.next();
                         if (ifceElem.getName().equals("interface-status")) {
                             ifceStatus = ifceElem.getText();
                             log.debug("ifce status : "+ifceStatus);
                         } else if (ifceElem.getName().equals("interface-description")) {
                             String ifceDescription = ifceElem.getText();
                             log.debug("ifce description: "+ifceDescription);
                         }
                     }
                 }
             }
             if (connectionStatus != null && connectionStatus.toLowerCase().trim().equals("up")) {
                 isVCup = true;
             }
         } 
         
         if (isVCup) {
             log.debug(gri+": VC is up"); 
         } else {
             log.debug(gri+": VC is down"); 
         }
             
         if (isVCConfigured) {
             log.debug(gri+": VC is configured"); 
         } else {
             log.debug(gri+": VC is not configured"); 
             
         }
         
         
         if (prevActionType.equals(ActionType.SETUP)) {
             if (isVCConfigured && isVCup) {
                 success = true;
             }
         } else if (prevActionType.equals(ActionType.TEARDOWN)) {
             if (!isVCConfigured && !isVCup) {
                 success = true;
             }
         }
         return success;
     }
     
     private String cleanup(PSSAction action, String deviceId) throws PSSException {
         return ConnectorUtils.sendAction(action, deviceId, EoMPLSService.SVC_ID);
     }
     
 
     public void setConfig(GenericConfig configToSet) throws PSSException {
         this.config = configToSet;
         if (config == null) { 
             throw new PSSException("no verifier configuration");
         } else if (config.getParams() == null) {
             throw new PSSException("no verifier parameters stanza");
         }
         try {
             performVerify   = (Boolean) config.getParams().get("performVerify");
             cleanupOnFail   = (Boolean) config.getParams().get("cleanupOnFail");
             verifyTries     = (Integer) config.getParams().get("verifyTries");
             delaySec        = (Integer) config.getParams().get("delaySec");
             tryIntervalSec  = (Integer) config.getParams().get("tryIntervalSec");
             if (verifyTries <= 0 || verifyTries > 10) {
                 throw new PSSException("verifyTries must be in the range 1..10");
             }
             if (delaySec < 0 || delaySec > 600) {
                 throw new PSSException("delaySec must be in the range 0..600");
             }
             if (tryIntervalSec <= 0 || tryIntervalSec > 600) {
                 throw new PSSException("tryIntervalSec must be in the range 0..600");
             }
             
         } catch (Exception e) {
             throw new PSSException(e);
         }
         
     }
 
 }
