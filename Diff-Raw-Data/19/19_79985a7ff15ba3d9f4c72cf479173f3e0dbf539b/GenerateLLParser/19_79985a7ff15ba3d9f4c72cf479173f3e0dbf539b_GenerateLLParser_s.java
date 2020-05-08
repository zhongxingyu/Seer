 package de.hszg.atocc.language.java.internal;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import de.hszg.atocc.core.util.AbstractXmlTransformationService;
 import de.hszg.atocc.core.util.WebServiceResultStatus;
 import de.hszg.atocc.core.util.WebUtilService;
 import de.hszg.atocc.core.util.XmlTransformationException;
 import de.hszg.atocc.core.util.XmlUtilService;
 import de.hszg.atocc.core.util.XmlValidationException;
 
 public class GenerateLLParser extends AbstractXmlTransformationService {
 
     private static final String TEMPLATE_BASE = "de/hszg/atocc/vcc/templates";
 
     private static final String GENERATE_SCANNER_URL = "vcc/java/generate/scanner";
     private static final String MAIN_CLASS = "Compiler.java.ll(1).template";
     private static final String PARSER = "LL1Parser.java.template";
 
     private XmlUtilService xmlUtils;
     private WebUtilService webUtils;
 
     private String scannerClass;
     private String parserClass;
     private String mainClass;
 
     private List<String> tokenClasses;
 
     @Override
     protected void transform() throws XmlTransformationException {
         tryToGetRequiredServices();
 
         try {
             findTokenClasses();
             generateScanner();
             generateParserClass();
             generateMainClass();
 
             generateCompileTask();
         } catch (IOException e) {
             throw new XmlTransformationException("Java|IO_ERROR", e);
         } catch (XmlValidationException e) {
             throw new XmlTransformationException("Java|TASK_VALIDATION_ERROR", e);
         }
     }
    
     private void generateMainClass() throws IOException {
         mainClass = getTemplate(MAIN_CLASS);
     }
 
     private void findTokenClasses() {
         tokenClasses = new LinkedList<>();
 
         final Node scannerNode = getInput().getElementsByTagName("SCANNER").item(0);
 
         final NodeList scannerChildNodes = scannerNode.getChildNodes();
 
         for (int i = 0; i < scannerChildNodes.getLength(); ++i) {
             final Node child = scannerChildNodes.item(i);
 
             if (child.getNodeType() != Node.ELEMENT_NODE)
                 continue;
 
             final Element readElement = (Element) child;
             final String token = readElement.getAttribute("token");
 
             if (!"IGNORE".equals(token)) {
                 tokenClasses.add(token);
             }
         }
     }
 
     private void generateScanner() throws XmlTransformationException, IOException {
         final Document resultDocument = webUtils.post(getServiceUrl(GENERATE_SCANNER_URL),
                 getInput());
 
         checkResultDocument(resultDocument);
 
         final Document content = xmlUtils.getContent(resultDocument);
 
         scannerClass = content.getDocumentElement().getTextContent();
     }
 
     private void generateParserClass() throws IOException {
         parserClass = getTemplate(PARSER);
     }
    
     private void generateCompileTask() throws XmlValidationException {
         final Document compileTaskDocument = xmlUtils.createEmptyDocument();
 
         final Element taskElement = compileTaskDocument.createElement("task");
         compileTaskDocument.appendChild(taskElement);
 
         final Element settingsElement = compileTaskDocument.createElement("settings");
         taskElement.appendChild(settingsElement);
 
         final Element optionElement = compileTaskDocument.createElement("option");
         optionElement.setAttribute("name", "language");
         optionElement.setTextContent("Java");
         settingsElement.appendChild(optionElement);
 
         final Element sourcesElement = compileTaskDocument.createElement("sources");
         taskElement.appendChild(sourcesElement);
 
         final Element mainSourceElement = compileTaskDocument.createElement("source");
         mainSourceElement.setAttribute("filename", "Compiler");
         mainSourceElement.appendChild(compileTaskDocument.createCDATASection(mainClass));
         sourcesElement.appendChild(mainSourceElement);
 
         final Element scannerSourceElement = compileTaskDocument.createElement("source");
         scannerSourceElement.setAttribute("filename", "Scanner");
         scannerSourceElement.appendChild(compileTaskDocument.createCDATASection(scannerClass));
         sourcesElement.appendChild(scannerSourceElement);
        
         final Element parserSourceElement = compileTaskDocument.createElement("source");
         parserSourceElement.setAttribute("filename", "LL1Parser");
         parserSourceElement.appendChild(compileTaskDocument.createCDATASection(parserClass));
         sourcesElement.appendChild(parserSourceElement);
        
         setOutput(compileTaskDocument);
         validateOutput("COMPILE_TASK");
     }
 
     private void tryToGetRequiredServices() {
         xmlUtils = getService(XmlUtilService.class);
         webUtils = getService(WebUtilService.class);
     }
 
     private void checkResultDocument(Document resultDocument) {
         if (xmlUtils.getResultStatus(resultDocument) != WebServiceResultStatus.SUCCESS) {
             throw new RuntimeException(xmlUtils.getErrorMessage(resultDocument));
         }
     }
 
     private String getTemplate(String name) throws IOException {
         final String url = String.format("%s/%s", TEMPLATE_BASE, name);
 
         return getResourceAsString(url);
     }
 
     private String getResourceAsString(String resource) throws IOException {
         final ClassLoader classLoader = getClass().getClassLoader();
         final InputStream in = classLoader.getResourceAsStream(resource);
 
         final StringBuilder resourceBuilder = new StringBuilder();
 
         byte[] buffer = null;
 
         for (int available = in.available(); available > 0; available = in.available()) {
             buffer = new byte[available];
             in.read(buffer);
             resourceBuilder.append(new String(buffer) + "\n");
         }
 
         return resourceBuilder.toString();
     }
 
 }
