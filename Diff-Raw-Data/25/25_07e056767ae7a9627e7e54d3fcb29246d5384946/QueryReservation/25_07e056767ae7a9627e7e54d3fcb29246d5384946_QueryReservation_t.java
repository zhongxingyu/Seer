 package net.es.oscars.wbui.servlets;
 
 
 import java.io.*;
 import java.util.*;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 import net.es.oscars.common.soap.gen.*;
 import org.apache.log4j.Logger;
 import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneHopContent;
 import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneLinkContent;
 import org.ogf.schema.network.topology.ctrlplane.CtrlPlanePathContent;
 import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneSwcapContent;
 import org.ogf.schema.network.topology.ctrlplane.CtrlPlaneSwitchingCapabilitySpecificInfo;
 
 import net.sf.json.*;
 
 import oasis.names.tc.saml._2_0.assertion.AttributeType;
 import net.es.oscars.logging.ErrSev;
 import net.es.oscars.logging.OSCARSNetLogger;
 import net.es.oscars.utils.clients.AuthNPolicyClient;
 import net.es.oscars.utils.clients.AuthZClient;
 import net.es.oscars.utils.clients.CoordClient;
 import net.es.oscars.utils.sharedConstants.AuthZConstants;
 import net.es.oscars.utils.soap.OSCARSServiceException;
 import net.es.oscars.utils.svc.ServiceNames;
 import net.es.oscars.utils.topology.NMWGParserUtil;
 import net.es.oscars.utils.topology.PathTools;
 import net.es.oscars.api.soap.gen.v06.Layer2Info;
 import net.es.oscars.api.soap.gen.v06.Layer3Info;
 import net.es.oscars.api.soap.gen.v06.MplsInfo;
 import net.es.oscars.api.soap.gen.v06.PathInfo;
 import net.es.oscars.api.soap.gen.v06.QueryResContent;
 import net.es.oscars.api.soap.gen.v06.QueryResReply;
 import net.es.oscars.api.soap.gen.v06.ReservedConstraintType;
 import net.es.oscars.api.soap.gen.v06.UserRequestConstraintType;
 import net.es.oscars.api.soap.gen.v06.VlanTag;
 import net.es.oscars.api.soap.gen.v06.ResDetails;
 import net.es.oscars.authZ.soap.gen.CheckAccessReply;
 
 /**
  * Query reservation servlet
  *
  * @author David Robertson, Mary Thompson
  *
  */
 public class QueryReservation extends HttpServlet {
     private static Logger log = Logger.getLogger(QueryReservation.class);
    protected OSCARSNetLogger netLogger = null;
 
     /**
      * Handles QueryReservation servlet request.
      *
      * @param servletRequest HttpServletRequest contains the gri of the reservation
      * @param response HttpServletResponse contains: gri, status, user,
      *        description start, end and create times, bandwidth, vlan tag,
      *        and path information.
      */
     public void
         doGet(HttpServletRequest servletRequest, HttpServletResponse response)
             throws IOException, ServletException {
 
         String methodName = "QueryReservation";
         String transId  = PathTools.getLocalDomainId() + "-WBUI-" + UUID.randomUUID().toString();
         this.netLogger = new OSCARSNetLogger();
         netLogger.init(ServiceNames.SVC_WBUI,transId);
         OSCARSNetLogger.setTlogger(netLogger);
         netLogger.setGRI(servletRequest.getParameter("gri"));
         log.info(netLogger.start(methodName));
 
         PrintWriter out = response.getWriter();
         response.setContentType("application/json");
         ServletCore core = ServletCore.getInstance();
         getServletContext().getAttribute(ServletCore.CORE);
         if (core == null) {
             ServletUtils.fatalError(out, methodName);
         }
         CoordClient coordClient = core.getCoordClient();
         AuthNPolicyClient authNPolicyClient = core.getAuthNPolicyClient();
         AuthZClient authZClient = core.getAuthZClient();
         UserSession userSession = new UserSession(core);
         CheckSessionReply sessionReply =
             userSession.checkSession(out, authNPolicyClient, servletRequest,
                     methodName);
         if (sessionReply == null) {
             log.warn(netLogger.error(methodName,ErrSev.MINOR,"No user session: cookies invalid"));
             return;
         }
         String userName = sessionReply.getUserName();
         if (userName == null) {
             log.warn(netLogger.error(methodName,ErrSev.MINOR,"No user session: cookies invalid"));
             return;
         }
 
         List<AttributeType> userAttributes = sessionReply.getAttributes(); 
         SubjectAttributes subjectAttrs = new SubjectAttributes();
         List<AttributeType> reqAttrs = subjectAttrs.getSubjectAttribute();
         for (AttributeType attr: userAttributes) {
             reqAttrs.add(attr);
         }
         QueryResContent queryReq = new QueryResContent();
         MessagePropertiesType msgProps = new MessagePropertiesType();
         msgProps.setOriginator(subjectAttrs);
         msgProps.setGlobalTransactionId(transId);
         queryReq.setMessageProperties(msgProps);
         String gri = servletRequest.getParameter("gri");
         queryReq.setGlobalReservationId(gri);
         ResDetails resDetails= null;
 
         //QueryResReply queryRe
         String authVal = null;
         Map<String, Object> outputMap = new HashMap<String, Object>();
         try {
             // Send a queryReservation request
             // Coordinator enforces which hops the user is allowed to see
             Object[] req = new Object[]{subjectAttrs,queryReq};
             Object[] res = coordClient.invoke("queryReservation",req);
             resDetails = ((QueryResReply) res[0]).getReservationDetails();
             List <OSCARSFaultReport> faultReports = ((QueryResReply) res[0]).getErrorReport();
 
             // check to see if user is allowed to see the buttons allowing
             // reservation modification
             CheckAccessReply accessReply = ServletUtils.checkAccess(authZClient,
                                                    userAttributes, 
                                                    AuthZConstants.RESERVATIONS, 
                                                    AuthZConstants.MODIFY);
             authVal = accessReply.getPermission();
             if (!authVal.equals(AuthZConstants.ACCESS_DENIED)) {
                 outputMap.put("resvModifyDisplay", Boolean.TRUE);
                 Boolean allowed = false;
                 AuthConditions authConditions = accessReply.getConditions();
                 for (AuthConditionType authCond: authConditions.getAuthCondition()){
                     if (authCond.getName().equals(AuthZConstants.UNSAFE_ALLOWED)) {
                         if ( authCond.getConditionValue().get(0).equals("true") )
                             allowed = true;
                         break;
                     }
                 }
                 outputMap.put("resvCautionDisplay", allowed);
             } else {
                 outputMap.put("resvModifyDisplay", Boolean.FALSE);
                 outputMap.put("resvCautionDisplay", Boolean.FALSE);
             }
             // check to see if user is allowed to see the clone button, which
             // requires generic reservation create authorization
             CheckAccessReply checkAccessReply  =  ServletUtils.checkAccess(authZClient, 
                                                     userAttributes, 
                                                     AuthZConstants.RESERVATIONS, 
                                                     AuthZConstants.CREATE);
             authVal = checkAccessReply.getPermission();
             if (!authVal.equals(AuthZConstants.ACCESS_DENIED)) {
                 outputMap.put("resvCloneDisplay", Boolean.TRUE);
             } else {
                 outputMap.put("resvCloneDisplay", Boolean.FALSE);
             }
 
             this.contentSection(resDetails, faultReports, outputMap);
         } catch (Exception ex) {
             log.error("caught exception " + ex.getMessage());
             // any error will show up as an exception
             ServletUtils.handleFailure(out, log, ex, methodName);
             return;
         }
         if (resDetails.getStatus() == null) {
             outputMap.put("status", "Reservation details for " + gri);
         } else {
             outputMap.put("status", resDetails.getStatus());
         }
         outputMap.put("method", methodName);
         outputMap.put("success", Boolean.TRUE);
         JSONObject jsonObject = JSONObject.fromObject(outputMap);
         out.println("{}&&" + jsonObject);
         log.info(netLogger.end(methodName));
         return;
     }
 
     public void
         doPost(HttpServletRequest request, HttpServletResponse response)
             throws IOException, ServletException {
 
         this.doGet(request, response);
     }
 
     public void
         contentSection(ResDetails resv, List <OSCARSFaultReport> faultReports, Map<String,Object> outputMap)
             throws OSCARSServiceException {
         try {
         InetAddress inetAddress = null;
         String hostName = null;
         Long longParam = null;
         Integer intParam = null;
         String strParam = null;
 
         String gri = resv.getGlobalReservationId();
         UserRequestConstraintType uConstraint = resv.getUserRequestConstraint();
         PathInfo pathInfo = null;
         String pathType = null;
         ReservedConstraintType rConstraint = resv.getReservedConstraint();
         if (rConstraint !=  null) {
             pathInfo=rConstraint.getPathInfo();
             pathType = "reserved";
         } else {
             uConstraint = resv.getUserRequestConstraint();
             if (uConstraint == null) {
                 throw new OSCARSServiceException("invalid reservation, no reserved or requested path");
             }
             pathInfo=uConstraint.getPathInfo();
             pathType="requested";
             log.debug(netLogger.getMsg("QueryReservationSatus","no path reserved, using requested path "));
         }
         CtrlPlanePathContent path = null;
         Layer2Info layer2Info = null;
         Layer3Info layer3Info = null;
         MplsInfo mplsInfo = null;
         if (pathInfo != null) {
             layer2Info = pathInfo.getLayer2Info();
             layer3Info = pathInfo.getLayer3Info();
             mplsInfo = pathInfo.getMplsInfo();
             path = pathInfo.getPath();
         }
         // if not layer2Info create one from path -- hack to make rest of code work
         if (layer2Info == null && layer3Info ==  null && path != null) {
             layer2Info = createLayer2(path);
         }
         String status = resv.getStatus();
         // always blank NEW GRI field, current GRI is in griReplace's
         // innerHTML
         outputMap.put("newGri", "");
         outputMap.put("griReplace", gri);
         outputMap.put("statusReplace", status);
         outputMap.put("userReplace", resv.getLogin());
         String sanitized = resv.getDescription().replace("<", "");
         String sanitized2 = sanitized.replace(">", "");
         outputMap.put("descriptionReplace", sanitized2);
         outputMap.put("modifyStartSeconds", uConstraint.getStartTime());
         outputMap.put("modifyEndSeconds", uConstraint.getEndTime());
         outputMap.put("createdTimeConvert", resv.getCreateTime());
         // now stored in Mbps, commas added by Dojo
         outputMap.put("bandwidthReplace", Integer.toString(uConstraint.getBandwidth()));
         // Do error report now, since failed reservations may cause exceptions to be thrown
         if (!faultReports.isEmpty()) {
             handleErrorReports(faultReports, outputMap);
         } else {
             outputMap.put("errorReportReplace"," " );
         }
         if (layer2Info != null) {
             outputMap.put("sourceReplace", layer2Info.getSrcEndpoint());
             outputMap.put("destinationReplace", layer2Info.getDestEndpoint());
             QueryReservation.handleVlans(path, status, layer2Info, outputMap);
         } else if (layer3Info != null) {
             if(path == null || path.getHop() == null || path.getHop().isEmpty()){
                 outputMap.put("sourceReplace", "none");
                 outputMap.put("destinationReplace", "none");
             }else{
                 outputMap.put("sourceReplace", NMWGParserUtil.getURN(path.getHop().get(0)));
                 outputMap.put("destinationReplace", NMWGParserUtil.getURN(path.getHop().get(path.getHop().size() - 1)));
             }
             strParam = layer3Info.getSrcHost();
             try {
                 inetAddress = InetAddress.getByName(strParam);
                 hostName = inetAddress.getHostName();
             } catch (UnknownHostException e) {
                 hostName = strParam;
             }
             outputMap.put("sourceIPReplace", hostName);
             strParam = layer3Info.getDestHost();
             try {
                 inetAddress = InetAddress.getByName(strParam);
                 hostName = inetAddress.getHostName();
             } catch (UnknownHostException e) {
                 hostName = strParam;
             }
             outputMap.put("destinationIPReplace", hostName);
             intParam = layer3Info.getSrcIpPort();
             if ((intParam != null) && (intParam != 0)) {
                 outputMap.put("sourcePortReplace", intParam);
             }
             intParam = layer3Info.getDestIpPort();
             if ((intParam != null) && (intParam != 0)) {
                 outputMap.put("destinationPortReplace", intParam);
             }
             strParam = layer3Info.getProtocol();
             if (strParam != null) {
                 outputMap.put("protocolReplace", strParam);
             }
             strParam =  layer3Info.getDscp();
             if (strParam !=  null) {
                 outputMap.put("dscpReplace", strParam);
             }
         }
         if (mplsInfo != null) {
             intParam = mplsInfo.getBurstLimit();
             if (intParam != null) {
                 outputMap.put("burstLimitReplace", intParam);
             }
             if (mplsInfo.getLspClass() != null) {
                 outputMap.put("lspClassReplace", mplsInfo.getLspClass());
             }
         }
 
         QueryReservation.outputPaths(path, outputMap);
 
 
         } catch (Exception e){
             System.out.println("caught exception in ContentSection");
             e.printStackTrace();
         }
     }
 
     public static void handleVlans(CtrlPlanePathContent path, String status, Layer2Info layer2Info,
                                    Map<String,Object> outputMap) 
             throws OSCARSServiceException {
 
         log.debug("in handleVlans");
         //set defaults
         List<String> vlanTags = new ArrayList<String>();
         String srcVlanTag = "";
         boolean srcTagged = true;
         String destVlanTag = "";
         boolean destTagged = true;
         
         ArrayList<CtrlPlaneHopContent> hops = (ArrayList<CtrlPlaneHopContent>) path.getHop();
         if (hops.size() > 0) {
         	int hopCount = 0;
             for (CtrlPlaneHopContent hop: hops){
                 CtrlPlaneLinkContent link = hop.getLink();
                 if(link != null){
                     String vlanRangeAvail = "0";
                     CtrlPlaneSwcapContent swcap= link.getSwitchingCapabilityDescriptors();
                     if (swcap != null) {
                         CtrlPlaneSwitchingCapabilitySpecificInfo specInfo = swcap.getSwitchingCapabilitySpecificInfo();
                         if (specInfo != null) {
                             if (specInfo.getVlanRangeAvailability() == null) {
                                 vlanTags.add(vlanRangeAvail);
                             } else {
                                 vlanTags.add(specInfo.getVlanRangeAvailability());
                                 if(hopCount == 0){
                                 	srcVlanTag = specInfo.getVlanRangeAvailability();
                                 }else if(hopCount == (hops.size() - 1)){
                                 	destVlanTag = specInfo.getVlanRangeAvailability();
                                 }
                             }
                         }
                     }
                 }
                 hopCount++;
             }
         }
         
         if (!vlanTags.isEmpty()) {
             QueryReservation.outputVlanTable(vlanTags, "vlanInterPathReplace",
                                              outputMap);
         }
         
         //parse layer 2 info
         if (layer2Info != null ) {
             if (layer2Info.getSrcVtag() != null) {
                 srcVlanTag = layer2Info.getSrcVtag().getValue();
                 srcTagged = layer2Info.getSrcVtag().isTagged();
             }
             if (layer2Info.getDestVtag() != null ){
                 destVlanTag = layer2Info.getDestVtag().getValue();
                 destTagged = layer2Info.getDestVtag().isTagged();
             }
             log.debug("srcVlanTag:" + srcVlanTag + " destVlanTag:" + destVlanTag);
         }
         
         //finally outpur vlans
         if (status.equals("ACCEPTED") || status.equals("INPATHCALCULATION")) {
             outputMap.put("srcVlanReplace", "VLAN setup in progress");
             outputMap.put("destVlanReplace", "VLAN setup in progress");
             outputMap.put("srcTaggedReplace", "");
             outputMap.put("destTaggedReplace", "");
         } else {
         	if (!"".equals(srcVlanTag)) {
                 QueryReservation.outputVlan(srcVlanTag, srcTagged, outputMap, "src");
             }else{
             	outputMap.put("srcVlanReplace", "No VLAN tag was ever set up");
             	outputMap.put("srcTaggedReplace", "");
             }
         	
         	if (!"".equals(destVlanTag)) {
                 QueryReservation.outputVlan(destVlanTag, destTagged, outputMap, "dest");
             }else{
             	outputMap.put("destVlanReplace", "No VLAN tag was ever set up");
             	outputMap.put("destTaggedReplace", "");
             }
         }
     }
 
     
     public static void outputVlan(String vlanTag, boolean tagged, Map<String, Object> outputMap,
                                   String prefix) {
 
         if (vlanTag == null) {
             return;
         }
         
         //If its a negative number try converting it
         //Prior to reservation completing may be a range or "any"
         try {
             vlanTag = Math.abs(Integer.parseInt(vlanTag)) + "";
         } catch (Exception e) {}
         
         outputMap.put(prefix + "VlanReplace", vlanTag);
         outputMap.put(prefix + "TaggedReplace", (tagged ? "true" : "false"));
     }
 
     public static void
     outputPaths(CtrlPlanePathContent path, Map<String,Object> outputMap) throws OSCARSServiceException {
 
         String pathStr = new String();
         if (path != null  ){
             //log.debug("path not null");
             ArrayList<CtrlPlaneHopContent> hops = (ArrayList<CtrlPlaneHopContent>) path.getHop();
             if (hops.size() > 0) {
                 StringBuilder sb = new StringBuilder();
                 sb.append("<tbody>");
                 for ( CtrlPlaneHopContent ctrlHop : hops ) {
                     CtrlPlaneLinkContent link = ctrlHop.getLink();
                     if (link != null ) {
                         sb.append("<tr><td class='innerHops'>" + link.getId()+ "</td></tr>");
                     } else {
                         String id = ctrlHop.getLinkIdRef();
                         sb.append("<tr><td class='innerHops'>" + id+ "</td></tr>");
                     }
                 }
                 sb.append("</tbody>");
                 //if (Layer2) {
                 if (true) {
                     //log.debug("hop: " + sb.toString());
                     outputMap.put("interPathReplace", sb.toString());
                 } else {
                     outputMap.put("interPath3Replace", sb.toString());
                 }
             }
         }
     }
 
     public static void outputVlanTable(List<String> vlanTags, String nodeId,
                                        Map<String,Object> outputMap) {
         StringBuilder sb = new StringBuilder();
         sb.append("<tbody>");
         for (String vlanTag: vlanTags) {
             if("0".equals(vlanTag)){
                 vlanTag = "n/a*";
             }else if(vlanTag.startsWith("-")){
                 vlanTag = vlanTag.replaceFirst("-", "");
                 vlanTag += "*";
             }
             sb.append("<tr><td class='innerHops'>" + vlanTag + "</td></tr>");
         }
         sb.append("</tbody>");
         outputMap.put(nodeId, sb.toString());
     }
 
    public static void handleErrorReports(List<OSCARSFaultReport> faultReports, Map<String,Object> outputMap)  {
         StringBuilder sb = new StringBuilder();
         sb.append("<tbody>");
         for (OSCARSFaultReport faultReport: faultReports) {
             sb.append("<tr><td> Error Code </td><td>" + faultReport.getErrorCode() + "</td></tr>");
             sb.append("<tr><td> Error Message </td><td>" + faultReport.getErrorMsg() + "</td></tr>");
             sb.append("<tr><td> Error Type </td><td>" + faultReport.getErrorType() + "</td></tr>");
         }
         sb.append("</tbody>");
        outputMap.put("errorReportReplace", sb.toString());
     }
     /**
      * hack to create a layer2Info from a CtrlPlanPathContent 
      * @param path
      * @return
      */
     public static Layer2Info createLayer2(CtrlPlanePathContent path){
         try {
         log.debug("createLayer:start");
         Layer2Info layer2Info = new Layer2Info();
         ArrayList<CtrlPlaneHopContent> hops = (ArrayList<CtrlPlaneHopContent>) path.getHop();
         CtrlPlaneLinkContent link = hops.get(0).getLink();
         layer2Info.setSrcEndpoint(link.getId());
         String vlanRangeAvail = "any";
         CtrlPlaneSwcapContent swcap= link.getSwitchingCapabilityDescriptors();
         if (swcap != null) {
             CtrlPlaneSwitchingCapabilitySpecificInfo specInfo = swcap.getSwitchingCapabilitySpecificInfo();
             if (specInfo != null) {
                 vlanRangeAvail = specInfo.getVlanRangeAvailability(); 
             }
         }
         VlanTag srcVtag = new VlanTag();
         srcVtag.setValue(vlanRangeAvail);
         srcVtag.setTagged(true);
         try{
             if(Integer.parseInt(vlanRangeAvail) <= 0){
                 srcVtag.setTagged(false);
             }
         }catch(Exception e){}
         layer2Info.setSrcVtag(srcVtag);
         
         VlanTag destVtag = new VlanTag();
         link = hops.get(hops.size()-1).getLink();
         layer2Info.setDestEndpoint(link.getId());
         vlanRangeAvail = "any";
         swcap= link.getSwitchingCapabilityDescriptors();
         if (swcap != null) {
             CtrlPlaneSwitchingCapabilitySpecificInfo specInfo = swcap.getSwitchingCapabilitySpecificInfo();
             if (specInfo != null) {
                 vlanRangeAvail = specInfo.getVlanRangeAvailability(); 
             }
         }
         destVtag.setValue(vlanRangeAvail);
         destVtag.setTagged(true);
         try{
             if(Integer.parseInt(vlanRangeAvail) <= 0){
                 destVtag.setTagged(false);
             }
         }catch(Exception e){}
         layer2Info.setDestVtag(destVtag);
         log.debug("createLayer:end");
         return layer2Info;
 
         } catch (Exception e) {
             e.printStackTrace();
         }
         return null;
     }
 
 }
