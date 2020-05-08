 package de.hszg.atocc.language.java.internal;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 import javax.xml.xpath.XPathExpressionException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 import de.hszg.atocc.core.util.AbstractXmlTransformationService;
 import de.hszg.atocc.core.util.CollectionHelper;
 import de.hszg.atocc.core.util.GrammarService;
 import de.hszg.atocc.core.util.SerializationException;
 import de.hszg.atocc.core.util.WebServiceResultStatus;
 import de.hszg.atocc.core.util.WebUtilService;
 import de.hszg.atocc.core.util.XmlTransformationException;
 import de.hszg.atocc.core.util.XmlUtilService;
 import de.hszg.atocc.core.util.XmlValidationException;
 import de.hszg.atocc.core.util.grammar.Grammar;
 
 public class GenerateLLParser extends AbstractXmlTransformationService {
 
     private static final String TEMPLATE_BASE = "de/hszg/atocc/vcc/templates";
 
     private static final String GENERATE_SCANNER_URL = "vcc/java/generate/scanner";
     private static final String MAIN_CLASS = "Compiler.java.ll(1).template";
     private static final String PARSER = "LL1Parser.java.template";
 
     private XmlUtilService xmlUtils;
     private WebUtilService webUtils;
     private GrammarService grammarService;
 
     private String scannerClass;
     private String parserClass;
     private String mainClass;
 
     private List<String> tokenClasses;
 
     private Grammar grammar;
 
     @Override
     protected void transform() throws XmlTransformationException {
         tryToGetRequiredServices();
 
         try {
             final Document resultDocument = webUtils.post(getServiceUrl("vcc/export/grammar"),
                     getInput());
             final Document grammarDocument = xmlUtils.getContent(resultDocument);
 
             checkLLConditions(grammarDocument);
 
             grammar = grammarService.grammarFrom(grammarDocument);
 
             findTokenClasses();
             generateScanner();
             generateParserClass();
             generateMainClass();
 
             generateCompileTask();
         } catch (IOException e) {
             throw new XmlTransformationException("Java|IO_ERROR", e);
         } catch (XmlValidationException e) {
             throw new XmlTransformationException("Java|TASK_VALIDATION_ERROR", e);
         } catch (RuntimeException | XPathExpressionException e) {
             throw new XmlTransformationException("Java|SERVICE_ERROR", e);
         } catch (SerializationException e) {
             throw new XmlTransformationException("Java|COULD_NOT_IMPORT_GRAMMAR", e);
         }
     }
 
     private void checkLLConditions(final Document grammarDocument) {
         Document resultDocument = webUtils.post(getServiceUrl("kfgedit/checkllcondition/1"),
                 grammarDocument);
 
         if (xmlUtils.getResultStatus(resultDocument) != WebServiceResultStatus.SUCCESS) {
             throw new RuntimeException("COULD_NOT_CHECK_LL_CONDITION_1");
         }
 
         if (!Boolean.parseBoolean(xmlUtils.getContent(resultDocument).getDocumentElement()
                 .getAttribute("conditionFulfilled"))) {
             throw new RuntimeException("LL(1)_CONDITION_1_NOT_FULFILLED");
         }
 
         resultDocument = webUtils
                 .post(getServiceUrl("kfgedit/checkllcondition/2"), grammarDocument);
 
         if (xmlUtils.getResultStatus(resultDocument) != WebServiceResultStatus.SUCCESS) {
             throw new RuntimeException("COULD_NOT_CHECK_LL_CONDITION_2");
         }
 
         if (!Boolean.parseBoolean(xmlUtils.getContent(resultDocument).getDocumentElement()
                 .getAttribute("conditionFulfilled"))) {
             throw new RuntimeException("LL(1)_CONDITION_2_NOT_FULFILLED");
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
 
     private void generateParserClass() throws IOException, XPathExpressionException {
         parserClass = getTemplate(PARSER);
 
         final Set<String> terminals = grammar.findTerminals();
 
         final StringBuilder rules = new StringBuilder();
         final StringBuilder actions = new StringBuilder();
 
         final NodeList ruleNodes = xmlUtils.extractNodes(getInput(), "//RULES/RULE");
 
         // start rule
         final Element startRuleElement = (Element) ruleNodes.item(0);
         parserClass = parserClass.replace("{{START_RULE}}",
                 String.format("Rule_%s", startRuleElement.getAttribute("name")));
 
         int currentAction = 0;
         int currentLhs = 0;
 
         for (String lhs : grammar.getLeftHandSides()) {
             rules.append(String.format("\tpublic String Rule_%s() {\n", lhs));
 
             final Element ruleElement = (Element) ruleNodes.item(currentLhs++);
             final NodeList rightSideElements = ruleElement.getElementsByTagName("RIGHTSIDE");
 
             final Collection<String> rightHandSides = grammar.getRightHandSidesFor(lhs);
 
             int currentRhs = 0;
             for (String rhs : rightHandSides) {
                 ++currentAction;
                 final Element codeElement = (Element) rightSideElements.item(currentRhs);
 
                 final String[] parts = rhs.split(" ");
                 final String[] params = new String[parts.length];
                 final String[] paramsWithType = new String[parts.length];
 
                 final String first = parts[0];
 
                 final Set<String> firstSet = new HashSet<>();
 
                 if (terminals.contains(first)) {
                     firstSet.add(first);
                 } else {
                     firstSet.addAll(grammarService.calculateFirstSetFor(first, grammar));
                 }
 
                 for (String firstSetElement : firstSet) {
                    rules.append(String.format("\t\tif(token.equals(\"%s\") {\n", firstSetElement));
 
                     for (int i = 0; i < parts.length; ++i) {
                         String ruleOrToken = "";
                         params[i] = "$" + i;
                         paramsWithType[i] = "String " + params[i];
 
                         if (terminals.contains(parts[i])) {
                             ruleOrToken = String.format("readToken(\"%s\")", parts[i]);
                         } else {
                             ruleOrToken = String.format("Rule_%s()", parts[i]);
                         }
 
                         rules.append(String.format("\t\t\tString $%s = %s;\n", i, ruleOrToken));
                     }
 
                     // TODO: create and call actions
                     rules.append(String.format("\t\t\treturn action%s(%s);\n", currentAction,
                             CollectionHelper.makeString(params, ", ")));
                     rules.append("\t\t}\n");
 
                     String code = codeElement.getTextContent().trim();
 
                     if (code.isEmpty()) {
                         code = String.format("\t\tString $$ = %s;\n\t\treturn $$;", CollectionHelper.makeString(params, " + "));
                     }
 
                     actions.append(String.format("\tpublic String action%s(%s) {\n", currentAction,
                             CollectionHelper.makeString(paramsWithType, ", ")));
                     actions.append(String.format("%s\n\n", code));
                     actions.append("\t\t return $$;\n");
                     actions.append("\t}\n");
                 }
 
             }
 
             rules.append("\t\tsyntaxError = true;\n");
             rules.append("\t\tSystem.out.println(\"Syntax Error\");\n");
             rules.append("\t\tSystem.exit(1);\n");
             rules.append("\t\treturn \"\";\n");
             rules.append("\t}\n\n");
         }
 
         parserClass = parserClass.replace("{{ACTIONS}}", actions.toString());
         parserClass = parserClass.replace("{{RULES}}", rules.toString());
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
         grammarService = getService(GrammarService.class);
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
