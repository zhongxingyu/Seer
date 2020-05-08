 package de.hszg.atocc.vcc.jflex.internal;
 
 import de.hszg.atocc.core.util.AbstractXmlTransformationService;
 import de.hszg.atocc.core.util.XmlTransformationException;
 import de.hszg.atocc.core.util.XmlUtilService;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 public class GenerateJFlexInput extends AbstractXmlTransformationService {
 
     private XmlUtilService xmlUtils;
     private StringBuilder jflexInput;
 
     @Override
     protected void transform() throws XmlTransformationException {
         tryToGetRequiredServices();
 
         // TODO validateInput("COMPILER")
         initializeJFlexInput();
         generateJFlexInput();
 
         final Document scannerDocument = xmlUtils.createEmptyDocument();
         final Element scannerElement = scannerDocument.createElement("scanner");
         final Element definitionElement = scannerDocument.createElement("definition");
         final Element inputElement = scannerDocument.createElement("input");
 
         scannerDocument.appendChild(scannerElement);
         scannerElement.appendChild(definitionElement);
         scannerElement.appendChild(inputElement);
 
         definitionElement.setTextContent(String.format("%s", jflexInput.toString()));
 
         setOutput(scannerDocument);
     }
 
     private void initializeJFlexInput() {
         jflexInput = new StringBuilder();
         jflexInput.append("import java.util.*;\n\n");
         jflexInput.append("%%\n\n");
         jflexInput.append("%public\n");
         jflexInput.append("%class Scanner\n");
         jflexInput.append("%standalone\n");
         jflexInput.append("%line\n");
         jflexInput.append("%column\n");
         jflexInput.append("%unicode\n\n");
         jflexInput.append("%{\n");
         jflexInput.append("\tpublic List<String> getTokens() { return tokens; }");
         jflexInput.append("\tprivate List<String> tokens;\n");
         jflexInput.append("%}\n\n");
         
         jflexInput.append("%init{\n");
         jflexInput.append("tokens = new LinkedList<String>();\n");
         jflexInput.append("%init}\n\n");
         
         jflexInput.append("%%\n\n");
     }
 
     private void generateJFlexInput() {
         appendJFlexRules();
         appendDefaultRules();
     }
 
     private void appendJFlexRules() {
         final NodeList readElements = getInput().getElementsByTagName("READ");
 
         for (int i = 0; i < readElements.getLength(); ++i) {
             final Element readElement = (Element) readElements.item(i);
 
             final String value = readElement.getAttribute("value");
             final String token = readElement.getAttribute("token");
 
             appendScannerRule(value, token);
         }
     }
 
     private void appendDefaultRules() {
         jflexInput
                .append(". { throw new Error(\"Illegal character (at \" + (yyline+1) + \":\" + yycolumn + \") \" + yytext()); }");
     }
 
     private void appendScannerRule(final String value, final String token) {
         final boolean isRegex = value.startsWith("[");
 
         if (isRegex) {
             jflexInput.append(value.replace("\\s", " "));
         } else {
             jflexInput.append(String.format("\"%s\"", value));
         }
 
         jflexInput.append(" { ");
 
         if (!"IGNORE".equals(token)) {
             jflexInput.append(String.format("tokens.add(\"%s,\" + yytext()); System.out.println(\"%s,\" + yytext());", token, token));
         }
 
         jflexInput.append(" }\n");
     }
 
     private void tryToGetRequiredServices() {
         xmlUtils = getService(XmlUtilService.class);
     }
 
 }
