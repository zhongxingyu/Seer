 package org.zaproxy.zap.extension.saml;
 
 import org.joda.time.DateTime;
 import org.opensaml.Configuration;
 import org.opensaml.DefaultBootstrap;
 import org.opensaml.common.SAMLVersion;
 import org.opensaml.saml2.core.*;
 import org.opensaml.xml.ConfigurationException;
 import org.opensaml.xml.XMLObject;
 import org.opensaml.xml.io.*;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.xml.sax.SAXException;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.OutputKeys;
 import javax.xml.transform.Source;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 import java.io.*;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Set;
 
 public class SAMLMessage {
     private String originalMessage;
     private Map<String,String> attributeMapping;
     private String samlParameter;
     private XMLObject unmarshalledObject;
 
     public SAMLMessage(String originalMessage, String samlParameter) {
         try {
             DefaultBootstrap.bootstrap();
             this.originalMessage = originalMessage;
             this.samlParameter = samlParameter;
         } catch (ConfigurationException e) {
 
         }
     }
 
     /**
      * Reset the message (which might have changed from original one) back to original message
      */
     public void resetMessage() {
         unmarshalledObject = null;
     }
 
     public String getSamlParameter() {
         return samlParameter;
     }
 
     /**
      * Convert the raw saml xml string to a pretty formatted String
      * @return The Pretty formatted XML String
      * @throws SAMLException If XML parsing failed
      */
     public String getPrettyFormattedMessage() throws SAMLException {
         try {
             Source xmlInput;
             if(unmarshalledObject!=null){
                 MarshallerFactory marshallerFactory = Configuration.getMarshallerFactory();
                 Marshaller marshaller = marshallerFactory.getMarshaller(unmarshalledObject);
                 Element element = marshaller.marshall(unmarshalledObject);
                 xmlInput = new DOMSource(element);
             } else {
                 xmlInput = new StreamSource(new StringReader(originalMessage));
             }
 
             StringWriter stringWriter = new StringWriter();
             StreamResult xmlOutput = new StreamResult(stringWriter);
             TransformerFactory transformerFactory = TransformerFactory.newInstance();
             transformerFactory.setAttribute("indent-number", 4);
             Transformer transformer = transformerFactory.newTransformer();
 
             transformer.setOutputProperty(OutputKeys.INDENT, "yes");
             transformer.transform(xmlInput, xmlOutput);
             return xmlOutput.getWriter().toString();
         } catch (Exception e) {
             throw new SAMLException("Formatting XML failed",e);
         }
     }
 
     public Map<String, String> getAttributeMapping() {
         if(attributeMapping==null||attributeMapping.isEmpty()){
             extractAttributes();
         }
         return attributeMapping;
     }
 
     private void extractAttributes(){
         try {
             DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
             documentBuilderFactory.setNamespaceAware(true);
             DocumentBuilder docBuilder = null;
             docBuilder = documentBuilderFactory.newDocumentBuilder();
             ByteArrayInputStream is = new ByteArrayInputStream(originalMessage.getBytes("UTF-8"));
 
             Document document = docBuilder.parse(is);
             Element element = document.getDocumentElement();
             UnmarshallerFactory unmarshallerFactory = Configuration.getUnmarshallerFactory();
             Unmarshaller unmarshaller = unmarshallerFactory.getUnmarshaller(element);
             unmarshalledObject = unmarshaller.unmarshall(element);
             attributeMapping = new LinkedHashMap<>();
             if("SAMLResponse".equals(samlParameter)){
                 extractResponseAttributes(unmarshalledObject);
             } else if("SAMLRequest".equals(samlParameter)){
                 extractRequestAttributes();
             }
         } catch (ParserConfigurationException e) {
             e.printStackTrace();
         } catch (SAXException e) {
             e.printStackTrace();
         } catch (UnmarshallingException e) {
             e.printStackTrace();
         } catch (UnsupportedEncodingException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     private void extractRequestAttributes(){
         attributeMapping = new LinkedHashMap<>();
         for (String attribute : getSelectedAttributes()) {
             String value = getValueOf(attribute);
             if(value!=null && !"".equals(value)){
                 attributeMapping.put(attribute,value);
             }
         }
 
     }
 
     private void extractResponseAttributes(XMLObject xmlObject){
         Response response = (Response) xmlObject;
         Assertion assertion = response.getAssertions().get(0);
         attributeMapping.put("Assertion ID",assertion.getID());
         attributeMapping.put("version",assertion.getVersion().toString());
         attributeMapping.put("Issuer",assertion.getIssuer().getValue());
         attributeMapping.put("Issuer format",assertion.getIssuer().getFormat());
         attributeMapping.put("Subject : NameID",assertion.getSubject().getNameID().getValue());
         if(assertion.getSubject().getSubjectConfirmations().size()>0){
             SubjectConfirmation subjectConfirmation = assertion.getSubject().getSubjectConfirmations().get(0);
             attributeMapping.put("Subject : Confirmation Method",subjectConfirmation.getMethod());
             attributeMapping.put("Subject : Confirmation Address",
                     subjectConfirmation.getSubjectConfirmationData().getAddress());
             attributeMapping.put("Subject : InResponseTo",subjectConfirmation.getSubjectConfirmationData()
                     .getInResponseTo());
             attributeMapping.put("Subject : Recipient",subjectConfirmation.getSubjectConfirmationData()
                     .getRecipient());
             attributeMapping.put("Subject : NotAfter",subjectConfirmation.getSubjectConfirmationData()
                     .getNotOnOrAfter().toString());
         }
 
         attributeMapping.put("Conditions : NotBefore",assertion.getConditions().getNotBefore().toString());
         attributeMapping.put("Conditions : NotAfter",assertion.getConditions().getNotOnOrAfter().toString());
 
         int restrictionNo = 0;
         for (AudienceRestriction audienceRestriction : assertion.getConditions().getAudienceRestrictions()) {
             attributeMapping.put("Audience "+restrictionNo,audienceRestriction.getAudiences().get(0).getAudienceURI());
         }
 
         for (AuthnStatement authnStatement : assertion.getAuthnStatements()) {
             attributeMapping.put("AuthStatement : Session Index",authnStatement.getSessionIndex());
             attributeMapping.put("AuthStatement : Auth Instant",authnStatement.getAuthnInstant().toString());
 
         }
     }
 
     private Set<String> getSelectedAttributes(){
         Set<String> result = new HashSet<>();
         result.add("AuthnRequest[ID]");
         result.add("AuthnRequest[AssertionConsumerServiceURL]");
         result.add("AuthnRequest[AttributeConsumingServiceIndex]");
         result.add("AuthnRequest[IssueInstant]");
         result.add("AuthnRequest[ProtocolBinding]");
         result.add("AuthnRequest[Version]");
         result.add("AuthnRequest:Issuer");
         result.add("AuthnRequest:NameIDPolicy[Format]");
         result.add("AuthnRequest:NameIDPolicy[SPNameQualifier]");
         result.add("AuthnRequest:NameIDPolicy[AllowCreate]");
         result.add("AuthnRequest:RequestedAuthnContext[Comparison]");
         result.add("AuthnRequest:RequestedAuthnContext:AuthnContextClassRef");
 
         return result;
     }
 
     private String getValueOf(String key){
         if (unmarshalledObject==null){
             return "";
         }
         if(key.startsWith("AuthnRequest")){
             return getAuthnRequestValue(key);
         }
         return "";
     }
 
    public boolean setValueTo(String key, String value) throws SAMLException {
         if (unmarshalledObject==null){
             return false;
         }
         if(key.startsWith("AuthnRequest")){
             return setAuthnRequestValue(key,value);
         }
         return false;
     }
 
     private String getAuthnRequestValue(String key){
         if(!(unmarshalledObject instanceof AuthnRequest)){
             return "";
         }
         AuthnRequest authnRequest = (AuthnRequest) unmarshalledObject;
         switch (key){
             case "AuthnRequest[ID]": return  authnRequest.getID();
             case "AuthnRequest[AssertionConsumerServiceURL]" : return authnRequest.getAssertionConsumerServiceURL();
             case "AuthnRequest[AttributeConsumingServiceIndex]" : return String.valueOf(authnRequest.getAttributeConsumingServiceIndex());
             case "AuthnRequest[IssueInstant]" : return authnRequest.getIssueInstant().toString();
             case "AuthnRequest[ProtocolBinding]" : return authnRequest.getProtocolBinding();
             case "AuthnRequest[Version]" : return authnRequest.getVersion().toString();
             case "AuthnRequest:Issuer" : return authnRequest.getIssuer().getValue();
             case "AuthnRequest:NameIDPolicy[Format]" : return authnRequest.getNameIDPolicy().getFormat();
             case "AuthnRequest:NameIDPolicy[SPNameQualifier]" : return authnRequest.getNameIDPolicy().getSPNameQualifier();
             case "AuthnRequest:NameIDPolicy[AllowCreate]" : return authnRequest.getNameIDPolicy().getAllowCreate().toString();
             case "AuthnRequest:RequestedAuthnContext[Comparison]" : return authnRequest.getRequestedAuthnContext()
                     .getComparison().toString();
             case "AuthnRequest:RequestedAuthnContext:AuthnContextClassRef" : return authnRequest.getRequestedAuthnContext()
                     .getAuthnContextClassRefs().get(0).getAuthnContextClassRef();
         }
         return "";
     }
 
    private boolean setAuthnRequestValue(String key, String value) throws SAMLException {
         if(!(unmarshalledObject instanceof AuthnRequest)){
             return false;
         }
         AuthnRequest authnRequest = (AuthnRequest) unmarshalledObject;
         switch (key){
             case "AuthnRequest[ID]":
                 authnRequest.setID(value);
                 return true;
             case "AuthnRequest[AssertionConsumerServiceURL]" :
                 authnRequest.setAssertionConsumerServiceURL(value);
                 return true;
             case "AuthnRequest[AttributeConsumingServiceIndex]" :
                try{
                    Integer intValue = Integer.parseInt(value);
                    authnRequest.setAttributeConsumingServiceIndex(intValue);
                    return true;
                } catch (NumberFormatException e){
                    throw new SAMLException("Given: "+value+" \nExpected: Integer");
                }
             case "AuthnRequest[IssueInstant]" :
                 authnRequest.setIssueInstant(DateTime.parse(value));
                 return true;
             case "AuthnRequest[ProtocolBinding]" :
                 authnRequest.setProtocolBinding(value);
                 return true;
             case "AuthnRequest[Version]" :
                 authnRequest.setVersion(SAMLVersion.valueOf(value));
                 return true;
             case "AuthnRequest:Issuer" :
                 authnRequest.getIssuer().setValue(value);
                 return true;
             case "AuthnRequest:NameIDPolicy[Format]" :
                 authnRequest.getNameIDPolicy().setFormat(value);
                 return true;
             case "AuthnRequest:NameIDPolicy[SPNameQualifier]" :
                 authnRequest.getNameIDPolicy().setSPNameQualifier(value);
                 return true;
             case "AuthnRequest:NameIDPolicy[AllowCreate]" :
                 authnRequest.getNameIDPolicy().setAllowCreate(Boolean.valueOf(value));
                 return true;
             case "AuthnRequest:RequestedAuthnContext[Comparison]" :
                 AuthnContextComparisonTypeEnumeration type = authnRequest.getRequestedAuthnContext().getComparison();
                 switch (value){
                     case "EXACT": type = AuthnContextComparisonTypeEnumeration.EXACT; break;
                     case "BETTER": type = AuthnContextComparisonTypeEnumeration.BETTER; break;
                     case "MAXIMUM": type = AuthnContextComparisonTypeEnumeration.MAXIMUM; break;
                     case "MINIMUM": type = AuthnContextComparisonTypeEnumeration.MINIMUM; break;
                    default:throw new SAMLException("Incorrect value. \nGiven:"+value+" \nExpected:One of 'EXACT'," +
                            "'BETTER','MAXIMUM','MINIMUM'");
                 }
                 authnRequest.getRequestedAuthnContext().setComparison(type);
                 return true;
             case "AuthnRequest:RequestedAuthnContext:AuthnContextClassRef" :
                 authnRequest.getRequestedAuthnContext().getAuthnContextClassRefs().get(0).setAuthnContextClassRef(value);
                 return true;
         }
         return false;
     }
 
 }
