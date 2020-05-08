 package soapproxy.application.mapping;
 
 import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlContext;
 import com.eviware.soapui.impl.wsdl.support.wsdl.WsdlUtils;
 import org.apache.xmlbeans.SchemaAnnotation;
 import org.apache.xmlbeans.SchemaGlobalElement;
 import org.apache.xmlbeans.SchemaLocalElement;
 import org.apache.xmlbeans.SchemaParticle;
 import org.apache.xmlbeans.SchemaType;
 import org.dom4j.dom.DOMElement;
 import org.mortbay.jetty.Request;
 import org.w3c.dom.Element;
 
 import javax.servlet.Servlet;
 import javax.servlet.ServletRequest;
 import javax.servlet.http.HttpServletRequest;
 import javax.wsdl.BindingOperation;
 import javax.wsdl.Definition;
 import javax.wsdl.Part;
 import javax.xml.namespace.QName;
 
 public class DefaultMappingGenerator extends AbstractMappingGenerator {
 
   private WsdlContext wsdlContext;
   private String wsdlUri;
   private String operationName;
   public static final QName MODEL_REFERENCE_ATTRIBUTE = new QName("http://www.w3.org/ns/sawsdl", "modelReference");
  private ServletRequest httpServletRequest;
 
   public DefaultMappingGenerator(String wsdlUri, String operationName, HttpServletRequest httpServletRequest) {
     super(wsdlUri, operationName);
     this.httpServletRequest = httpServletRequest;
     this.setWsdlUri(wsdlUri);
     this.setOperationName(operationName);
     setWsdlContext(new WsdlContext(wsdlUri));
   }
 
   public String getMapping() throws Exception {
     return generateMapping();
   }
 
   public String generateMapping() throws Exception {
     BindingOperation bindingOperation = getBindingOperation();
     return "<?xml version=\"1.0\" ?><frames>"
            + getOutputFrameAsXml(bindingOperation)
            + getInputFrameAsXml(bindingOperation)
            + "</frames>";
   }
 
   public String getOutputFrameAsXml(BindingOperation bindingOperation) throws Exception {
     Element outputFrame = getOutputFrame(bindingOperation);
     String outputFrameAsXml = ((DOMElement)outputFrame).asXML();
     return outputFrameAsXml;
   }
 
   public Element getOutputFrame(BindingOperation bindingOperation) throws Exception {
     Part[] outputParts = WsdlUtils.getOutputParts(bindingOperation);
     Element outputFrame = generateOutputFrame(outputParts);
     return outputFrame;
   }
 
   public String getInputFrameAsXml(BindingOperation bindingOperation) throws Exception {
     Element inputFrame = getInputFrame(bindingOperation);
     String inputFrameAsXml = ((DOMElement)inputFrame).asXML();
     return inputFrameAsXml;
   }
 
   public BindingOperation getBindingOperation() throws Exception {
     Definition definition = getWsdlContext().getDefinition();
     BindingOperation bindingOperation = WsdlUtils.findBindingOperation(definition, getOperationName());
     return bindingOperation;
   }
 
   private Element generateOutputFrame(Part[] outputParts) throws Exception {
     return generateFrame(outputParts, true, getOutputTopic(), null);
   }
 
   public Element getInputFrame(BindingOperation bindingOperation) throws Exception {
     Part[] inputParts = WsdlUtils.getInputParts(bindingOperation);
     return generateFrame(inputParts, false, getInputTopic(), getInputSchemaLocation());
   }
 
   public String getInputSchemaLocation() {
    return ((Request)httpServletRequest).getRootURL() + "/json-schema?wsdl=" + getWsdlUri() + "&operation=" + getOperationName() + "&message=input";
 //    return "http://localhost/SoapServiceWidget/widgets/schemas/personName.js";
   }
 
   public String getOutputTopic() {
     return "ee.stacc.soapwidgetgenerator." + getCommonTopic() + ".output";
   }
 
   public String getInputTopic() {
     return "ee.stacc.soapwidgetgenerator." + getCommonTopic() + ".input";
   }
 
   public String getCommonTopic() {
     String clearedWsdl = getWsdlUri().replaceAll("\\W", "-");
     return clearedWsdl + "." + getOperationName();
   }
 
   private Element generateFrame(Part[] parts, boolean outgoingOnly, String topic, String schemaLocation) throws Exception {
     Element frame = new DOMElement("frame");
     addTopic(frame, outgoingOnly, topic);
     // TODO format might not only be json, but also simple string
     addFormat(frame);
     addSchema(frame, schemaLocation);
     Element mappings = new DOMElement("mappings");
     frame.appendChild(mappings);
 
     for(Part part : parts) {
       addPartMapping(mappings, part);
     }
 
     return frame;
   }
 
   protected void addSchema(Element frame, String schemaLocation) {
     if (schemaLocation != null) {
       Element schema = new DOMElement("schema");
       schema.setNodeValue(schemaLocation);
       frame.appendChild(schema);
     }
   }
 
   private void addPartMapping(Element mappings, Part part) throws Exception {
     // get part type
     QName elementName = part.getElementName();
     if (elementName != null) {
       addMappingFromPartElement(mappings, part);
     } else {
       addMappingFromPartType(mappings, part);
     }
   }
 
   private void addMappingFromPartType(Element mappings, Part part) throws Exception {
     SchemaType schemaTypeForPart = WsdlUtils.getSchemaTypeForPart(getWsdlContext(), part);
     if (schemaTypeForPart.isPrimitiveType()) {
       addMappingFromPrimitivePartType(mappings, part);
     }
   }
 
   private void addMappingFromPartElement(Element mappings, Part part) throws Exception {
     SchemaGlobalElement element = getWsdlContext().getSchemaTypeLoader().findElement(part.getElementName());
     String name = element.getName().getLocalPart();
     SchemaType type = element.getType();
     // TODO: process element attributes. Ignore min/maxOccurs, because those attributes cannot be used when element's parent is schema
     addMappingFromType(mappings, type, "/" + name);
   }
 
   private void addMappingFromType(Element mappings, SchemaType type, String path) {
     if (type.isSimpleType() || type.isURType()) {
       addMappingFromSimpleType(mappings, type, path);
       return;
     }
     if (SchemaType.ELEMENT_CONTENT == type.getContentType() && type.getContentModel() != null) {
       addMappingFromParticle(mappings, path, type.getContentModel());
     }
     
   }
 
   private void addMappingFromParticle(Element mappings, String path, SchemaParticle schemaParticle) {
 
     // determine element count
     int minOccurs = schemaParticle.getIntMinOccurs();
     int maxOccurs = schemaParticle.getIntMaxOccurs();
 
     if (minOccurs == 0) {
       //TODO: can be null or empty array?
     }
 
     if (maxOccurs > 1) {
       if (SchemaParticle.ELEMENT == schemaParticle.getParticleType() && !schemaParticle.getType().isPrimitiveType()) {
         path += "/" + schemaParticle.getName().getLocalPart();
       }
       // repeating element group
       mappings = addRepeatingElementGroup(mappings, path);
     }
 
     switch (schemaParticle.getParticleType()) {
       case SchemaParticle.ELEMENT:
         processElementParticle(mappings, path, schemaParticle);
         break;
       case SchemaParticle.SEQUENCE:
         processSequenceParticle(mappings, path, schemaParticle);
         break;
     }
 
     
   }
 
   private void processSequenceParticle(Element mappings, String path, SchemaParticle schemaParticle) {
     SchemaParticle[] spArray = schemaParticle.getParticleChildren();
     for (SchemaParticle sp : spArray) {
       addMappingFromParticle(mappings, path, sp);
     }
   }
 
   private void processElementParticle(Element mappings, String path, SchemaParticle schemaParticle) {
     SchemaLocalElement element = (SchemaLocalElement) schemaParticle;
     String elementPath = path + "/" + element.getName().getLocalPart();
 
     if (element.getType().isPrimitiveType()) {
       addMapping(mappings, elementPath, getGlobalReference(element));
     } else {
       addMappingFromType(mappings, element.getType(), path);
     }
   }
 
   private String getGlobalReference(SchemaLocalElement element) {
     String result = null;
 
     if (element.getAnnotation() != null) {
       for (SchemaAnnotation.Attribute attribute : element.getAnnotation().getAttributes()) {
         if (attribute.getName().equals(MODEL_REFERENCE_ATTRIBUTE)) {
           result = attribute.getValue();
           break;
         }
       }
     }
     return result;
   }
 
   protected Element addRepeatingElementGroup(Element mappings, String path) {
     Element repeatingElement = new DOMElement("repeating_element_group");
     repeatingElement.setAttribute("path", path);
     mappings.appendChild(repeatingElement);
     return repeatingElement;
   }
 
   private void addMappingFromSimpleType(Element mappings, SchemaType type, String path) {
     //To change body of created methods use File | Settings | File Templates.
   }
 
 
   private void addMappingFromPrimitivePartType(Element mappings, Part part) throws Exception {
     String path = "/" + part.getName();
     QName globalReference = (QName)part.getExtensionAttribute(MODEL_REFERENCE_ATTRIBUTE);
 
     addMapping(mappings, path, globalReference.getLocalPart());
   }
 
   protected void addMapping(Element mappings, String path, String globalReference) {
 
     if (path == null && globalReference == null){
       return;
     }
     
     Element mapping = new DOMElement("mapping");
     mappings.appendChild(mapping);
 
     if (globalReference != null) {
       Element globalRefElement = new DOMElement("global_ref");
       globalRefElement.setNodeValue(globalReference);
       mapping.appendChild(globalRefElement);
     }
 
     if (path != null) {
       Element pathElement = new DOMElement("path");
       pathElement.setNodeValue(path);
       mapping.appendChild(pathElement);
     }
   }
 
   protected void addMappingWithAttributes(Element mappings, String path, String globalReference) {
     Element mapping = new DOMElement("mapping");
     mappings.appendChild(mapping);
 
     if (globalReference != null) {
       mapping.setAttribute("global_ref", globalReference);
     }
     mapping.setAttribute("path", path);
   }
 
   protected void addFormat(Element frame) {
     Element format = new DOMElement("format");
     format.setNodeValue("json");
     frame.appendChild(format);
   }
 
   protected void addTopic(Element frame, boolean outgoingOnly, String topicName) {
     Element topic = new DOMElement("topic");
     if (outgoingOnly){
       topic.setAttribute("outgoing_only", "true");
     }
     topic.setNodeValue(topicName);
     frame.appendChild(topic);
   }
 
   public WsdlContext getWsdlContext() {
     return wsdlContext;
   }
 
   public void setWsdlContext(WsdlContext wsdlContext) {
     this.wsdlContext = wsdlContext;
   }
 
   public String getWsdlUri() {
     return wsdlUri;
   }
 
   public void setWsdlUri(String wsdlUri) {
     this.wsdlUri = wsdlUri;
   }
 
   public String getOperationName() {
     return operationName;
   }
 
   public void setOperationName(String operationName) {
     this.operationName = operationName;
   }
 
 
   public ServletRequest getHttpServletRequest() {
     return httpServletRequest;
   }
 
  public void setHttpServletRequest(ServletRequest httpServletRequest) {
     this.httpServletRequest = httpServletRequest;
   }
 }
