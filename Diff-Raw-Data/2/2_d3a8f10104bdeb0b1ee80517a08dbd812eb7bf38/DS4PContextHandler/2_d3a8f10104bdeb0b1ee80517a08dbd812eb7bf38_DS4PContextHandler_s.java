 /**
  * This software is being provided per FARS 52.227-14 Rights in Data - General.
  * Any redistribution or request for copyright requires written consent by the
  * Department of Veterans Affairs.
  */
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package gov.va.ehtac.ds4p.ws;
 
 import com.jerichosystems.authz.saml2xacml2.simple.SAMLBoundXACMLService;
 import com.jerichosystems.authz.saml2xacml2.simple.SAMLBoundXACMLServicePortType;
 import com.jerichosystems.authz.saml2xacml2.simple.assertion.AssertionType;
 import com.jerichosystems.authz.saml2xacml2.simple.assertion.NameIDType;
 import com.jerichosystems.authz.saml2xacml2.simple.context.ActionType;
 import com.jerichosystems.authz.saml2xacml2.simple.context.AttributeType;
 import com.jerichosystems.authz.saml2xacml2.simple.context.AttributeValueType;
 import com.jerichosystems.authz.saml2xacml2.simple.context.DecisionType;
 import com.jerichosystems.authz.saml2xacml2.simple.context.EnvironmentType;
 import com.jerichosystems.authz.saml2xacml2.simple.context.RequestType;
 import com.jerichosystems.authz.saml2xacml2.simple.context.ResourceType;
 import com.jerichosystems.authz.saml2xacml2.simple.context.ResultType;
 import com.jerichosystems.authz.saml2xacml2.simple.context.StatusType;
 import com.jerichosystems.authz.saml2xacml2.simple.context.SubjectType;
 import com.jerichosystems.authz.saml2xacml2.simple.policy.AttributeAssignmentType;
 import com.jerichosystems.authz.saml2xacml2.simple.policy.ObligationType;
 import com.jerichosystems.authz.saml2xacml2.simple.policy.ObligationsType;
 import com.jerichosystems.authz.saml2xacml2.simple.protocol.XACMLAuthzDecisionQueryType;
 import gov.va.ds4p.cas.constants.DS4PConstants;
 import gov.va.ds4p.cas.providers.OrganizationPolicyProvider;
 import gov.va.ds4p.policy.reference.OrganizationTaggingRules;
 import gov.va.ehtac.ds4p.jpa.AuthLog;
 import gov.va.ehtac.ds4p.jpa.OrganizationalPolicy;
 import gov.va.ehtac.ds4p.kairon.KaironPolicyDecisionClient;
 import gov.va.ehtac.ds4p.kairon.KaironPolicyObject;
 import gov.va.ehtac.ds4p.testobjects.EmergencyTest;
 import gov.va.ehtac.ds4p.testobjects.TreatmentTest;
 import java.io.StringWriter;
 import java.util.*;
 import javax.jws.WebService;
 import javax.jws.WebMethod;
 import javax.jws.WebParam;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBElement;
 import javax.xml.bind.Marshaller;
 import javax.xml.datatype.DatatypeFactory;
 import javax.xml.datatype.XMLGregorianCalendar;
 import javax.xml.namespace.QName;
 import javax.xml.ws.BindingProvider;
 import org.oasis.names.tc.xspa.v2.*;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 /**
  *
  * @author Duane DeCouteau
  */
 @WebService(serviceName = "DS4PContextHandler")
 public class DS4PContextHandler {
     private static final Logger log = LoggerFactory.getLogger(DS4PContextHandler.class);    
     private XspaSubject currSubject;
     private XspaResource currResource;
     private DS4PAudit auditservice = new DS4PAudit();
     private DS4PClinicallyAdaptiveRulesInterface orgData = new DS4PClinicallyAdaptiveRulesInterface();
     
     private XACMLAuthzDecisionQueryType query = new XACMLAuthzDecisionQueryType();
     
     private Date requeststart;
     private Date requestcomplete;
     
     private PolicyEnforcementObject obj;
     private KaironPolicyDecisionClient kClient = new KaironPolicyDecisionClient();  
     
     private OrganizationalPolicy orgPolicyDB;
     private gov.va.ds4p.policy.reference.OrganizationPolicy orgPolicy;
     private OrganizationPolicyProvider policyProvider =  new OrganizationPolicyProvider();
     
     private List<String> orgObligations = new ArrayList();
     private List<String> patientObligations = new ArrayList();
     
     private String kaironauthz = "";
     
     private boolean useJericho = true;
     
     private static DatatypeFactory dateFac;
     static {
         try{
             dateFac = DatatypeFactory.newInstance();
         }catch(Exception e){
             throw new RuntimeException(e);
         }
     }
     
     private String pdpEndpoint = "http://75.145.119.97/SAMLBoundXACMLService/services/SAMLBoundXACMLService";
     //private String pdpEndpoint = "http://vmcentral:11013/SAMLBoundXACMLService/services/SAMLBoundXACMLService";
     
     private ResultType resxacml;
 
     /**
      * Web service operation
      */
     @WebMethod(operationName = "enforcePolicy")
     public PolicyEnforcementObject enforcePolicy(@WebParam(name = "xspasubject") XspaSubject xspasubject, @WebParam(name = "xsparesource") XspaResource xsparesource) {
         //TODO write your implementation code here:
         this.currResource = xsparesource;
         this.currSubject = xspasubject;
         this.requeststart = new Date();
         obj = new PolicyEnforcementObject();
         // just do patient consent eval for now from kairon
         String pou = xspasubject.getSubjectPurposeOfUse();
         obj.setPurposeOfUse(pou);
         obj.setMessageId(currSubject.getMessageId());
         if (pou.equals("TREAT")) pou = "TREAT";
         if (pou.equals("EMERGENCY")) pou = "ETREAT";
         patientObligations = new ArrayList();
         try {
             kClient = new KaironPolicyDecisionClient();
             KaironPolicyObject kObj = kClient.getAuthorizationJerseyClient("1", xsparesource.getResourceId(), pou, xspasubject.getSubjectId());
             System.out.println("KAIRON STATUS RETURNED: "+kObj.getStatus());
             if (!useJericho) {
                 if (kObj.getStatus().equals("PERMIT") || kObj.getStatus().equals("Conditional Allow") || kObj.getStatus().indexOf("Permit") > -1) {
                     kaironauthz = "Permit";
                     obj.setPdpDecision("Permit");
                     obj.setPdpStatus("ok");
                     obj.setRequestTime(getCurrentDateTime());
                     obj.setResourceId(currResource.getResourceId());
                     obj.setResourceName(currResource.getResourceName());
                     obj.setResponseTime(getCurrentDateTime());
                     obj.setHomeCommunityId(currSubject.getSubjectLocality());
                     Iterator iter = kObj.getTopicListRedact().iterator();
                     while (iter.hasNext()) {
                         String kObligation = (String)iter.next();
                         kObligation = DS4PConstants.PATIENT_REDACT_CONSTRUCT+kObligation;
                         obj.getPdpObligation().add(kObligation);
                     }
                 }
                 else if (kObj.getStatus().equals("DENY") || kObj.getStatus().equals("Deny") || kObj.getStatus().indexOf("Deny") > -1) {
                     kaironauthz = "Deny";                
                     obj.setPdpDecision("Deny");
                     obj.setPdpStatus("ok");
                     obj.setRequestTime(getCurrentDateTime());
                     obj.setResponseTime(getCurrentDateTime());
                     obj.setResourceId(currResource.getResourceId());
                     obj.setResourceName(currResource.getResourceName()); 
                     obj.setHomeCommunityId(currSubject.getSubjectLocality());                    
                 }
                 else {
                     kaironauthz = DS4PConstants.INDETERMINATE;                
                     obj.setPdpDecision(DS4PConstants.INDETERMINATE);
                     obj.setPdpStatus("ok");
                     obj.setRequestTime(getCurrentDateTime());
                     obj.setResponseTime(getCurrentDateTime());
                     obj.setResourceId(currResource.getResourceId());
                     obj.setResourceName(currResource.getResourceName()); 
                     obj.setHomeCommunityId(currSubject.getSubjectLocality());                    
                 }
             }
             else {
                 if (kObj.getStatus().equals("PERMIT") || kObj.getStatus().equals("Conditional Allow") || kObj.getStatus().indexOf("Permit") > -1) {
                     kaironauthz = "Permit";
                     Iterator iter = kObj.getTopicListRedact().iterator();
                     while (iter.hasNext()) {
                         String kObligation = (String)iter.next();
                         kObligation = DS4PConstants.PATIENT_REDACT_CONSTRUCT+kObligation;
                         patientObligations.add(kObligation);
                     }
                     iter = kObj.getTopicListMask().iterator();
                     while (iter.hasNext()) {
                         String kObligation = (String)iter.next();
                         kObligation = DS4PConstants.PATIENT_MASK_CONSTRUCT+kObligation;
                         patientObligations.add(kObligation);
                     }
                     
                 }
                 else if (kObj.getStatus().equals("DENY") || kObj.getStatus().equals("Deny") || kObj.getStatus().indexOf("Deny") > -1) {
                     kaironauthz = "Deny";
                     Iterator iter = kObj.getTopicListRedact().iterator();
                     while (iter.hasNext()) {
                         String kObligation = (String)iter.next();
                         kObligation = DS4PConstants.PATIENT_REDACT_CONSTRUCT+kObligation;
                         patientObligations.add(kObligation);
                     }
                     iter = kObj.getTopicListMask().iterator();
                     while (iter.hasNext()) {
                         String kObligation = (String)iter.next();
                         kObligation = DS4PConstants.PATIENT_MASK_CONSTRUCT+kObligation;
                         patientObligations.add(kObligation);
                     }
                 }
                 else {
                     kaironauthz = DS4PConstants.INDETERMINATE;
                     Iterator iter = kObj.getTopicListRedact().iterator();
                     while (iter.hasNext()) {
                         String kObligation = (String)iter.next();
                         kObligation = DS4PConstants.PATIENT_REDACT_CONSTRUCT+kObligation;
                         patientObligations.add(kObligation);
                     }
                     iter = kObj.getTopicListMask().iterator();
                     while (iter.hasNext()) {
                         String kObligation = (String)iter.next();
                         kObligation = DS4PConstants.PATIENT_MASK_CONSTRUCT+kObligation;
                         patientObligations.add(kObligation);
                     }
                 }
                 
                 query = new XACMLAuthzDecisionQueryType();
                 setOrganizationalPolicy();
                 setXACMLRequest();
                 resxacml = getXACMLPDPDecision();
 
                 DecisionType d = resxacml.getDecision();
                 StatusType s = resxacml.getStatus();
                 ObligationsType o = resxacml.getObligations();
 
                 XacmlResultType x = new XacmlResultType();
                 x.setXacmlResultTypeDecision(d.value());
                 x.setXacmlResultTypeResourceId(resxacml.getResourceId());
 
                 XacmlStatusType xStat = new XacmlStatusType();
                 xStat.setXacmlStatusCodeType(s.getStatusCode().getValue());
                 xStat.setXacmlStatusMessage(s.getStatusMessage());
                 XacmlStatusDetailType detail = new XacmlStatusDetailType();
 
                 if (s.getStatusDetail() != null) {
                     List<Object> sObjs = s.getStatusDetail().getAny();
                     Iterator sObjsIter = sObjs.iterator();
                     while (sObjsIter.hasNext()) {
                         String obj = (String) sObjsIter.next();
                         detail.getXacmlStatusDetail().add(obj);
                     }
                 }
 
                 obj.setPdpDecision(d.value());
                 obj.setPdpStatus("ok");
                 //the following is a work around for XACML 2.0
                 obj.getPdpObligation().clear();
                 if (d.value().equals("Permit")) {
                     //add organization obls
                     Iterator iter = orgObligations.iterator();
                     while (iter.hasNext()) {
                         String oO = (String)iter.next();
                         obj.getPdpObligation().add(oO);
                     }
                     //add patient obligations
                     iter = patientObligations.iterator();
                     while (iter.hasNext()) {
                         String pO = (String)iter.next();
                         obj.getPdpObligation().add(pO);
                     }
                 }
                 obj.setPdpRequest(dumpRequestToString());
                 obj.setPdpResponse(dumpResponseToString(resxacml));
                 obj.setRequestTime(convertDateToXMLGregorianCalendar(requeststart));
                 obj.setResponseTime(convertDateToXMLGregorianCalendar(new Date()));
                 obj.setResourceName(currResource.getResourceName());
                 obj.setResourceId(currResource.getResourceId());
                 obj.setHomeCommunityId(currSubject.getSubjectLocality());
                 //obj.setXacmlResultType(x);                
             }
         }
         catch (Exception ex) {
             obj.setPdpDecision(DS4PConstants.INDETERMINATE);
             obj.setPdpStatus("Error Processing Kairon Request: "+ex.getMessage());
             obj.setRequestTime(getCurrentDateTime());
             obj.setResponseTime(getCurrentDateTime());
             obj.setResourceId(currResource.getResourceId());
             obj.setResourceName(currResource.getResourceName());
             obj.setHomeCommunityId(currSubject.getSubjectLocality());
             ex.printStackTrace();
         }
         this.requestcomplete = new Date();
         logEvent();
         return obj;
     }
 
     /**
      * Web service operation
      */
     @WebMethod(operationName = "getAuthorizationObligations")
     public PolicyEnforcementObject getAuthorizationObligations(@WebParam(name = "messageId") String messageId) {
         //TODO write your implementation code here:
         return new PolicyEnforcementObject();
     }
     
    private XMLGregorianCalendar getCurrentDateTime() {
        XMLGregorianCalendar xgc = null;
        try {
         GregorianCalendar gc = new GregorianCalendar();
         DatatypeFactory dtf = DatatypeFactory.newInstance();
         xgc = dtf.newXMLGregorianCalendar(gc);
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
         return xgc;
    }
    
     private String dumpRequestToString() {   
         String res = "";
         JAXBElement<RequestType> element = new JAXBElement<RequestType>(new QName("urn:oasis:names:tc:xacml:2.0:context:schema:os", "Request"), RequestType.class, query.getRequest());
         try {
             JAXBContext context = JAXBContext.newInstance(RequestType.class);
             Marshaller marshaller = context.createMarshaller();
             StringWriter sw = new StringWriter();
             marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
             //marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
             marshaller.marshal(element, sw);
             res = sw.toString();
         }
         catch (Exception ex) {
             log.warn("Unable to Dump Request ToString", ex);
 //            ex.printStackTrace();
         }
         return res;
     }
     
     private String dumpResponseToString(ResultType resp) {
         String res = "";
         JAXBElement<ResultType> element = new JAXBElement<ResultType>(new QName("urn:oasis:names:tc:xacml:2.0:context:schema:os", "Result"), ResultType.class, resp);
         try {
             JAXBContext context = JAXBContext.newInstance(ResultType.class);
             Marshaller marshaller = context.createMarshaller();
             StringWriter sw = new StringWriter();
             marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
             //marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
             marshaller.marshal(element, sw);
             res = sw.toString();
         }
         catch (Exception ex) {
             log.warn("Unable to Dump Response toString",ex);
 //            ex.printStackTrace();
         }  
         return res;
     }
      
    private void logEvent() {
             AuthLog log = new AuthLog();
             if (!useJericho) {
                 //log.setDecision(d.value());
                 log.setHealthcareObject(currResource.getResourceName());
                 log.setMsgDate(new Date());
                 //log.setObligations(decision.getPdpObligation().toString());
                 log.setObligations("");
                 log.setDecision(obj.getPdpDecision());
                 log.setPurposeOfUse(currSubject.getSubjectPurposeOfUse());
                 log.setRequestor(currSubject.getSubjectId());
                 log.setUniqueIdentifier(currResource.getResourceId());
                 //log.setXacmlRequest(dumpRequestToString());
                 log.setXacmlRequest("");
                 //log.setXacmlResponse(dumpResponseToString(res));
                 log.setXacmlResponse("");
                 long startTime = requeststart.getTime();
                 long endTime = requestcomplete.getTime();
                 long respTime = endTime - startTime;
                 log.setHieMsgId(obj.getMessageId());
 
                 log.setServicingOrg(currSubject.getSubjectLocality());
 
                 log.setResponseTime(respTime);
             }
             else {
                 //log.setDecision(d.value());
                 log.setHealthcareObject(currResource.getResourceName());
                 log.setMsgDate(new Date());
                 StringBuffer sb = new StringBuffer();
                 if (!obj.getPdpObligation().isEmpty()) {
                     Iterator iter = obj.getPdpObligation().iterator();
                     while (iter.hasNext()) {
                         String obS = (String)iter.next() + "\n";
                         sb.append(obS);
                     }
                     log.setObligations(sb.toString());
                 }
                 else {
                     log.setObligations("");
                 }
                 log.setDecision(obj.getPdpDecision());
                 log.setPurposeOfUse(currSubject.getSubjectPurposeOfUse());
                 log.setRequestor(currSubject.getSubjectId());
                 log.setUniqueIdentifier(currResource.getResourceId());
                 log.setXacmlRequest(dumpRequestToString());
                 //log.setXacmlRequest("");
                 log.setXacmlResponse(dumpResponseToString(resxacml));
                 //log.setXacmlResponse("");
                 long startTime = requeststart.getTime();
                 long endTime = requestcomplete.getTime();
                 long respTime = endTime - startTime;
                 log.setHieMsgId(obj.getMessageId());
 
                 log.setServicingOrg(currSubject.getSubjectLocality());
 
                 log.setResponseTime(respTime);
                 
             }
             //log.setHieMsgId(currSubject.getMessageId());
             logEvent(log);
        
    }
    
    private void logEvent(AuthLog authlog) {
       try {
           auditservice.persist(authlog);
       }
       catch (Exception ex) {
           ex.printStackTrace();
       }
    }
     
    private void setXACMLRequest() {
         query = new XACMLAuthzDecisionQueryType();
         query.setInputContextOnly(Boolean.TRUE);
         query.setReturnContext(Boolean.TRUE);
         query.setConsent("my consent");
         query.setDestination("my destination");
         query.setID(currSubject.getMessageId());
         query.setIssueInstant(dateFac.newXMLGregorianCalendar());
         query.setVersion("2.0");
         query.setIssuer(new NameIDType());
 
         //Test DoD to VA
         RequestType rt = new RequestType();
         SubjectType st = new SubjectType();
         ActionType act = new ActionType();
         ResourceType resource = new ResourceType();
         EnvironmentType environment = new EnvironmentType();
         //Subject Information Identifier
         AttributeType at = new AttributeType();
         at.setAttributeId(DS4PConstants.SUBJECT_ID_NS);
         at.setDataType(DS4PConstants.STRING);
         AttributeValueType avt = new AttributeValueType();
         avt.getContent().add(currSubject.getSubjectId());
         at.getAttributeValue().add(avt);
         st.getAttribute().add(at);
         //Subject Purpose of Use
         at = new AttributeType();
         at.setAttributeId(DS4PConstants.SUBJECT_PURPOSE_OF_USE_NS);
         at.setDataType(DS4PConstants.STRING);
         avt = new AttributeValueType();
         avt.getContent().add(currSubject.getSubjectPurposeOfUse());
         at.getAttributeValue().add(avt);
         st.getAttribute().add(at);
         //Subject Home Community Identifier
         at = new AttributeType();
         at.setAttributeId(DS4PConstants.SUBJECT_LOCALITY_NS);
         at.setDataType(DS4PConstants.STRING);
         avt = new AttributeValueType();
         avt.getContent().add(currSubject.getSubjectLocality());
         at.getAttributeValue().add(avt);
         st.getAttribute().add(at);
         //Subject ROLE
 //        at = new AttributeType();
 //        at.setAttributeId(DS4PConstants.SUBJECT_STRUCTURED_ROLE_NS);
 //        at.setDataType(DS4PConstants.STRING);
 //        avt = new AttributeValueType();
 //        avt.getContent().add(currSubject.getSubjectStructuredRole().get(0));
 //        at.getAttributeValue().add(avt);
 //        st.getAttribute().add(at);
 
         //the next subject attributes may be informational only as our focus on nwhin is homeCommunity...
         at = new AttributeType();
         at.setAttributeId(DS4PConstants.SUBJECT_ORGANIZATION_NS);
         at.setDataType(DS4PConstants.STRING);
         avt = new AttributeValueType();
         avt.getContent().add(currSubject.getOrganization());
         at.getAttributeValue().add(avt);
         st.getAttribute().add(at);
         //this is location-clinic placeholder
         at = new AttributeType();
         at.setAttributeId(DS4PConstants.SUBJECT_ORGANIZATION_ID_NS);
         at.setDataType(DS4PConstants.STRING);
         avt = new AttributeValueType();
         avt.getContent().add(currSubject.getOrganizationId());
         at.getAttributeValue().add(avt);
         st.getAttribute().add(at);
 
         //Set Resource Attributes - Organization and Region
         at = new AttributeType();
         at.setAttributeId(DS4PConstants.RESOURCE_NWHIN_SERVICE_NS);
         at.setDataType(DS4PConstants.STRING);
         avt = new AttributeValueType();
         avt.getContent().add(currResource.getResourceName());
         at.getAttributeValue().add(avt);
         resource.getAttribute().add(at);
 
         at = new AttributeType();
         at.setAttributeId(DS4PConstants.RESOURCE_LOCALITY_NS);
         at.setDataType(DS4PConstants.STRING);
         avt = new AttributeValueType();
         avt.getContent().add(currSubject.getSubjectLocality());
         at.getAttributeValue().add(avt);
         resource.getAttribute().add(at);
 
         //Kairon Patient Consent
         at = new AttributeType();
        at.setAttributeId(DS4PConstants.MITRE_PATIENT_AUTHORIZATION);
         at.setDataType(DS4PConstants.STRING);
         avt = new AttributeValueType();
         avt.getContent().add(kaironauthz);
         at.getAttributeValue().add(avt);
         resource.getAttribute().add(at);
         
         
         // add organization obligations
         if (!orgObligations.isEmpty()) {
             Iterator iter = orgObligations.iterator();
             at = new AttributeType();
             at.setAttributeId(DS4PConstants.ORG_OBLIGATIONS);
             at.setDataType(DS4PConstants.STRING);
             while (iter.hasNext()) {
                 String s = (String)iter.next();
                 avt = new AttributeValueType();
                 avt.getContent().add(s);
                 at.getAttributeValue().add(avt);
             }
             if (!at.getAttributeValue().isEmpty()) resource.getAttribute().add(at);
         }
 
         // add patient obligations
         if (!patientObligations.isEmpty()) {
             Iterator iter = patientObligations.iterator();
             at = new AttributeType();
             at.setAttributeId(DS4PConstants.PATIENT_OBLIGATIONS);
             at.setDataType(DS4PConstants.STRING);
             while (iter.hasNext()) {
                 String s = (String)iter.next();
                 avt = new AttributeValueType();
                 avt.getContent().add(s);
                 at.getAttributeValue().add(avt);
             }
             if (!at.getAttributeValue().isEmpty()) resource.getAttribute().add(at);
         }
 
         rt.getSubject().add(st);
         rt.getResource().add(resource);
         rt.setAction(act);
         rt.setEnvironment(environment);
         
         query.setRequest(rt);   
    }
  
    private void setOrganizationalPolicy() {
         try {
             orgObligations = new ArrayList();
             orgPolicyDB = orgData.getOrganizationalPolicy(currSubject.getSubjectLocality());
             orgPolicy = policyProvider.createOrganizationPolicyObjectFromXML(orgPolicyDB.getOrganizationalRules());
             //add applicable privacy law
             addToOrganizationObligations(DS4PConstants.ORG_PRIVACY_LAW_CONSTRUCT+orgPolicyDB.getApplicableUsLaw());
             Iterator iter = orgPolicy.getOrganizationTaggingRules().iterator();
             while (iter.hasNext()) {
                 OrganizationTaggingRules r = (OrganizationTaggingRules)iter.next();
                 if (currSubject.getSubjectPurposeOfUse().equals(r.getActReason().getCode())) {
                     String refrain = DS4PConstants.ORG_REFRAIN_POLICY_CONSTRUCT+r.getRefrainPolicy().getCode();
                     addToOrganizationObligations(refrain);
                 }
             }
         }
         catch (Exception ex) {
             ex.printStackTrace();
         }
    }
    
    private void addToOrganizationObligations(String s) {
        if (!orgObligations.contains(s)) {
            orgObligations.add(s);
        }
    }
            
     private ResultType getXACMLPDPDecision() {
         ResultType res = null;
         try {
             SAMLBoundXACMLService svc = new SAMLBoundXACMLService();
             SAMLBoundXACMLServicePortType port = svc.getSAMLBoundXACMLServicePort();
             ((BindingProvider)port).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, pdpEndpoint);
 //            if (securedMode) ((BindingProvider)port).getRequestContext().put(com.sun.xml.ws.developer.JAXWSProperties.SSL_SOCKET_FACTORY, proxy);
             
 
             com.jerichosystems.authz.saml2xacml2.simple.protocol.ResponseType resp = port.xacmlAuthzDecisionQuery(query);
             AssertionType aT = (AssertionType)resp.getAssertionOrEncryptedAssertion().get(0);
             com.jerichosystems.authz.saml2xacml2.simple.assertion.XACMLAuthzDecisionStatementType stmt = (com.jerichosystems.authz.saml2xacml2.simple.assertion.XACMLAuthzDecisionStatementType) aT.getStatementOrAuthnStatementOrAuthzDecisionStatement().get(0);
             com.jerichosystems.authz.saml2xacml2.simple.context.ResponseType resptype = stmt.getResponse();
             res = resptype.getResult().get(0);
         }
         catch (Exception e) {
             log.warn("Authorization Decision Not Available",e);
 //            e.printStackTrace();
         }
         return res;
     }
     private XMLGregorianCalendar convertDateToXMLGregorianCalendar(Date dt) {
         XMLGregorianCalendar xcal = null;
         try {
             GregorianCalendar gc = new GregorianCalendar();
             gc.setTime(dt);
             xcal = DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);
         }
         catch (Exception ex) {
             log.warn("", ex);
         }
         return xcal;
     }
    
 }
