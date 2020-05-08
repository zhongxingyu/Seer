 /**
  *    Copyright 2012 MegaFon
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 package ru.histone.acceptance.support;
 
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.node.ArrayNode;
 import org.eclipse.jetty.server.Server;
 import org.junit.ComparisonFailure;
 import org.junit.runner.Description;
 import org.junit.runner.Runner;
 import org.junit.runner.notification.Failure;
 import org.junit.runner.notification.RunNotifier;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.slf4j.MDC;
 import ru.histone.Histone;
 import ru.histone.HistoneBuilder;
 import ru.histone.HistoneException;
 import ru.histone.acceptance.websever.TestHandler;
 import ru.histone.evaluator.EvaluatorException;
 import ru.histone.evaluator.functions.global.GlobalFunction;
 import ru.histone.evaluator.functions.node.NodeFunction;
 import ru.histone.evaluator.nodes.BooleanHistoneNode;
 import ru.histone.evaluator.nodes.Node;
 import ru.histone.evaluator.nodes.NodeFactory;
 import ru.histone.evaluator.nodes.NumberHistoneNode;
 import ru.histone.evaluator.nodes.StringHistoneNode;
 import ru.histone.parser.ParserException;
 import ru.histone.utils.CollectionUtils;
 
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.StringReader;
 import java.net.URISyntaxException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 public class AcceptanceTestsRunner extends Runner {
     private Logger log;
     private Description description;
     private ObjectMapper jackson;
     private NodeFactory nodeFactory;
     private int testIdx = 0;
     private AcceptanceTest instance;
    private static final String MDC_TEST_NAME = "MDC_TEST_NAME";
 
     public AcceptanceTestsRunner(Class<?> testClass) throws IllegalAccessException, InstantiationException {
         instance = (AcceptanceTest) testClass.newInstance();
         description = instance.getDescription();
         jackson = new ObjectMapper();
         nodeFactory = new NodeFactory(jackson);
         log = LoggerFactory.getLogger(testClass);
     }
 
     @Override
     public Description getDescription() {
         return description;
     }
 
     @Override
     public void run(RunNotifier notifier) {
         Description testCaseDescription = null;
         try {
            Reader reader = new InputStreamReader(getClass().getResourceAsStream(instance.getFileName()));
             JsonNode testSuites = jackson.readTree(reader);
             if (instance.startTestWebServer()) {
                 startTestWebServer();
             }
             for (JsonNode mainElement : testSuites) {
                 final String suiteName = mainElement.get("name").asText();
                 final boolean ignoreSuite = checkIgnoreField(mainElement.path("ignore"));
                 final ArrayNode cases = (ArrayNode) mainElement.path("cases");
                 final TestSuiteHolder suite = new TestSuiteHolder(instance.getFileName(), suiteName);
 
                 for (JsonNode element : cases) {
                     testIdx++;
 
                     MDC.put(MDC_TEST_NAME, testIdx + " " + suiteName);
                     testCaseDescription = Description.createTestDescription(instance.getClass(), testIdx + " " + generateTestCaseName(instance.getFileName(), mainElement, element));
                     description.addChild(testCaseDescription);
                     notifier.fireTestStarted(testCaseDescription);
 
                     try {
                         TestCaseHolder testCase = readTestCase(element, suite);
                         if (ignoreSuite) {
                             testCase.setIgnore(true);
                         }
                         runTestCase(notifier, testCaseDescription, suite, testCase);
                     } catch (Exception e) {
                         log.error("Test failure", e);
                         notifier.fireTestFailure(new Failure(testCaseDescription, e));
                     }
                     MDC.put(MDC_TEST_NAME, "");
                 }
             }
         } catch (Exception e2) {
             log.error(String.format("Error reading/parsing json file '%s'", instance.getFileName()), e2);
             throw new RuntimeException(String.format("Error reading/parsing json file '%s'", instance.getFileName()), e2);
         } finally {
             stopTestWebServer();
         }
     }
 
     private String generateTestCaseName(String fileName, JsonNode suiteJson, JsonNode caseJson) {
         StringBuilder sb = new StringBuilder();
 
         sb.append(testIdx);
         sb.append('{').append(fileName).append(':').append(suiteJson.path("name").asText()).append('}');
         sb.append("input: ").append(caseJson.path("input").asText()).append(';');
         if (caseJson.has("expectedResult")) {
             sb.append(" expected:").append(caseJson.path("expectedResult").asText()).append(';');
         } else if (caseJson.has("expectedException")) {
             sb.append(" expectedException: ");
             sb.append("line=").append(caseJson.path("line").asText());
             sb.append(", expected=").append(caseJson.path("expected").asText());
             sb.append(", found=").append(caseJson.path("found").asText());
             sb.append(";");
         } else {
             sb.append(" expectedAST: ").append(caseJson.path("expectedAST").toString()).append(';');
         }
 
         return sb.toString();
     }
 
     private void runTestCase(RunNotifier notifier, Description testCaseDescription, TestSuiteHolder testSuite, TestCaseHolder testCase) throws HistoneException, URISyntaxException {
         log.trace("runTestCase({}): input={}, context={}, expected={}, exception={}", new Object[]{testIdx, testCase.getInput(), testCase.getContext(), testCase.getExpected(), testCase.getException()});
 
         if (testCase.isIgnore()) {
             log.debug("Test ignored");
             notifier.fireTestIgnored(testCaseDescription);
             return;
         }
 
         Histone histone = createHistoneInstance(testSuite, testCase);
 
         final ArrayNode expectedAST = testCase.getExpectedAST();
         final EvaluatorException expectedException = testCase.getException();
         final String expectedResult = testCase.getExpected();
         String baseURI = this.getClass().getResource(testSuite.getFileName()).toURI().toString();
 
         if (expectedAST != null) {
             try {
                 // TEST FOR EXPECTED-AST
                 Reader input = new StringReader(testCase.getInput());
 
                 // HSTJ-7
                 ArrayNode rootAST = histone.parseTemplateToAST(input);
                 ArrayNode outputAST = (ArrayNode) rootAST.get(1);
 
                 boolean result = expectedAST.toString().equals(nodeFactory.toJsonString(outputAST));
                 if (result) {
                     notifier.fireTestFinished(testCaseDescription);
                 } else {
                     String msgF = "For input='" + testCase.getInput() + "'";
                     notifier.fireTestFailure(new Failure(testCaseDescription, new ComparisonFailure(msgF, expectedAST.toString(), outputAST.toString())));
                 }
             } catch (Exception e) {
                 String msg = "For input='" + testCase.getInput() + "', expectedAST=" + testCase.getExpectedAST() + ", but exception occured=" + e.getMessage();
                 log.debug("Test failure: " + msg, e);
                 notifier.fireTestFailure(new Failure(testCaseDescription, e));
             }
         }
 
         if (expectedException != null) {
             try {
                 // TEST FOR EXPECTEDEXCEPTION
                 // we need to check for ParserException and EvaluatorException
                 // separately
                 final String msgF = "For input='" + testCase.getInput() + "'";
                 ArrayNode ast;
                 try {
                     Reader input = new StringReader(testCase.getInput());
                     ast = histone.parseTemplateToAST(input);
                 } catch (ParserException e) {
                     checkExpectedExceptionAndFireTest(notifier, testCaseDescription, msgF, expectedException, e);
                     return;
                 }
 
                 try {
                     JsonNode context = testCase.getContext() == null ? null : jackson.readTree(testCase.getContext());
                     String output = histone.evaluateAST(baseURI, ast, context);
                 } catch (EvaluatorException e) {
                     checkExpectedExceptionAndFireTest(notifier, testCaseDescription, msgF, expectedException, e);
                     return;
                 }
 
                 String expectedF = "expected=" + expectedException.getExpected() + ", found=" + expectedException.getFound();
                 notifier.fireTestFailure(new Failure(testCaseDescription, new ComparisonFailure(msgF, expectedF, "")));
             } catch (Exception e) {
                 String msg = "For input='" + testCase.getInput() + "', expectedException=" + testCase.getException().toString() + ", but exception occured=" + e.getMessage();
                 log.debug("Test failure: " + msg, e);
                 notifier.fireTestFailure(new Failure(testCaseDescription, e));
             }
         } else if (expectedResult != null) {
             try {
                 // TEST FOR OUTPUT
                 Reader input = new StringReader(testCase.getInput());
                 JsonNode context = (testCase.getContext() == null) ? jackson.getNodeFactory().nullNode() : jackson.readTree(testCase
                         .getContext());
                 String output = histone.evaluate(baseURI, input, context);
 
                 boolean result = output.equals(expectedResult);
                 if (result) {
                     notifier.fireTestFinished(testCaseDescription);
                 } else {
                     String msgF = "For input='" + testCase.getInput() + "'";
                     notifier.fireTestFailure(new Failure(testCaseDescription, new ComparisonFailure(msgF, testCase.getExpected(), output)));
                 }
             } catch (Exception e) {
                 String msg = "For input='" + testCase.getInput() + "', expectedAST=" + testCase.getExpectedAST() + ", but exception occured=" + e.getMessage();
                 log.debug("Test failure: " + msg, e);
                 notifier.fireTestFailure(new Failure(testCaseDescription, e));
             }
         } else {
             log.debug("!!! Wrong test data !!!");
         }
     }
 
     private void checkExpectedExceptionAndFireTest(RunNotifier notifier, Description description, String msgF, EvaluatorException expected,
                                                    HistoneException real) {
         final int errLine = real instanceof ParserException ? ((ParserException) real).getLineNumber() : ((EvaluatorException) real)
                 .getLineNumber();
         final String errExpectedToken = real instanceof ParserException ? ((ParserException) real).getExpected()
                 : ((EvaluatorException) real).getExpected();
         final String errFoundToken = real instanceof ParserException ? ((ParserException) real).getFound() : ((EvaluatorException) real)
                 .getFound();
         boolean matchOk = (expected.getLineNumber() == errLine) && expected.getExpected().equals(errExpectedToken)
                 && expected.getFound().equals(errFoundToken);
 
         if (matchOk) {
             notifier.fireTestFinished(description);
             return;
         } else {
             String expectedF = "line=" + expected.getLineNumber() + ", expected=" + expected.getExpected() + ", found="
                     + expected.getFound();
             String actualF = "line=" + errLine + ", expected=" + errExpectedToken + ", found=" + errFoundToken;
 
             log.debug("Test failure", real);
             notifier.fireTestFailure(new Failure(description, new ComparisonFailure(msgF, expectedF, actualF)));
             return;
         }
     }
 
     private Histone createHistoneInstance(TestSuiteHolder testSuite, TestCaseHolder testCase) throws HistoneException, URISyntaxException {
         HistoneBuilder histoneBuilder = new HistoneBuilder();
 
         histoneBuilder.setJackson(jackson);
 
         if (CollectionUtils.isNotEmpty(testCase.getMockFiles())) {
             //TODO: histoneBuilder.setResourceResolvers(toResourceResolvers(testCase.getMockFiles()));
         }
         if (CollectionUtils.isNotEmpty(testCase.getMockGlobalFunctions())) {
             histoneBuilder.setGlobalFunctions(toGlobalFunctions(testCase.getMockGlobalFunctions()));
         }
         if (CollectionUtils.isNotEmpty(testCase.getMockNodeFunctions())) {
             histoneBuilder.setNodeFunctions(toNodeFunctions(testCase.getMockNodeFunctions()));
         }
         if (CollectionUtils.isNotEmpty(testCase.getGlobalProperties())) {
             histoneBuilder.setGlobalProperties(testCase.getGlobalProperties());
         }
 
         return histoneBuilder.build();
     }
 
     private TestCaseHolder readTestCase(JsonNode caseNode, TestSuiteHolder suite) {
         log.debug("readTestCase(): {}", new Object[]{caseNode});
         TestCaseHolder testCase = new TestCaseHolder(suite);
         testCase.setIgnore(checkIgnoreField(caseNode.path("ignore")));
         if (caseNode.get("input") != null) {
             testCase.setInput(caseNode.get("input").asText());
         }
         if (caseNode.get("context") != null) {
             testCase.setContext(nodeFactory.toJsonString(caseNode.get("context")));
         }
         if (caseNode.get("expectedAST") != null) {
             testCase.setExpectedAST((ArrayNode) caseNode.get("expectedAST"));
         }
         if (caseNode.get("expectedResult") != null) {
             testCase.setExpected(caseNode.get("expectedResult").asText());
         }
         if (caseNode.get("data") != null) {
             throw new RuntimeException("Not supported tagName: data");
         }
         if (caseNode.has("function")) {
             ArrayNode functionsArray = jackson.createArrayNode();
             if (caseNode.path("function").isArray()) {
                 functionsArray.addAll((ArrayNode) caseNode.path("function"));
             } else {
                 functionsArray.add(caseNode.path("function"));
             }
 
             for (JsonNode node : functionsArray) {
                 final JsonNode resultNode = node.path("result");
                 final String name = node.path("name").asText();
                 final boolean exception = node.path("exception").asBoolean(false);
                 final String returnType = node.get("resultType") != null ? node.path("resultType").asText() : getTypeOfNode(resultNode);
                 final String data = resultNode == null ? "string" : resultNode.asText();
                 final String nodeType = node.get("node") == null ? null : node.get("node").asText();
                 if (nodeType == null) {
                     testCase.addMockGlobalFunction(new MockGlobalFunctionHolder(name, returnType, data, exception));
                 } else {
                     testCase.addMockNodeFunction(new MockNodeFunctionHolder(name, nodeType, returnType, data, exception));
                 }
             }
 
         }
         if (caseNode.path("property").isObject()) {
             JsonNode property = caseNode.path("property");
 
             String name = property.path("name").asText();
             String node = property.path("node").asText();
             String result = property.path("result").asText();
 
             if (!"global".equals(node)) {
                 throw new RuntimeException(String.format("Node type '%s' is not supported yet", node));
             }
 
             testCase.addGlobalProp(name, result);
         }
         if (caseNode.get("expectedException") != null) {
             testCase.setException(readException(caseNode.get("expectedException")));
         }
 
         return testCase;
     }
 
     private boolean checkIgnoreField(JsonNode ignore) {
         if (ignore.isArray()) {
             for (JsonNode ignoreImpl : ignore) {
                 if ("java".equalsIgnoreCase(ignoreImpl.asText())) {
                     return true;
                 }
             }
         } else if (ignore.isTextual()) {
             if ("java".equalsIgnoreCase(ignore.asText())) {
                 return true;
             }
         }
         return false;
     }
 
     private EvaluatorException readException(JsonNode node) {
         final int line = node.get("line").asInt();
         final String expected = node.get("expected").asText();
         final String found = node.get("found").asText();
         return new EvaluatorException(line, expected, found);
     }
 
     private String getTypeOfNode(JsonNode node) {
         if (node == null) {
             return "string";
         } else if (node.isMissingNode()) {
             return "string";
         } else if (node.isTextual()) {
             return "string";
         } else if (node.isBigDecimal()) {
             return "number";
         } else if (node.isBigInteger()) {
             return "number";
         } else if (node.isInt()) {
             return "number";
         } else if (node.isDouble()) {
             return "number";
         } else if (node.isBoolean()) {
             return "boolean";
         } else if (node.isObject() || node.isArray()) {
             return "object";
         } else {
             throw new RuntimeException(String.format("Uknown node type: '%s'", node.toString()));
         }
     }
 
     private Set<GlobalFunction> toGlobalFunctions(Set<MockGlobalFunctionHolder> mockFunctions) {
         Set<GlobalFunction> globalFunctions = new HashSet<GlobalFunction>();
         for (final MockGlobalFunctionHolder function : mockFunctions) {
             GlobalFunction globalFunction = new MockGlobalFunction(nodeFactory, function.getName(), function.getReturnType(), function.getData(), function.isException());
             globalFunctions.add(globalFunction);
         }
         return globalFunctions;
     }
 
     private Map<Class<? extends Node>, Set<NodeFunction<? extends Node>>> toNodeFunctions(Set<MockNodeFunctionHolder> mockFunctions) {
         Map<Class<? extends Node>, Set<NodeFunction<? extends Node>>> nodeFunctions = new HashMap<Class<? extends Node>, Set<NodeFunction<? extends Node>>>();
 
         for (final MockNodeFunctionHolder function : mockFunctions) {
             NodeFunction nodeFunction = new MockNodeFunction(nodeFactory, function.getName(), function.getReturnType(), function.getData(), function.isException());
             Class<? extends Node> nodeClass = null;
             if ("string".equalsIgnoreCase(function.getNodeType())) {
                 nodeClass = StringHistoneNode.class;
             } else if ("number".equalsIgnoreCase(function.getNodeType())) {
                 nodeClass = NumberHistoneNode.class;
             } else if ("boolean".equalsIgnoreCase(function.getNodeType())) {
                 nodeClass = BooleanHistoneNode.class;
             } else {
                 throw new RuntimeException(String.format("AstNodeType '%s' not supported by tests", function.getNodeType()));
             }
 
             if (!nodeFunctions.containsKey(nodeClass)) {
                 nodeFunctions.put(nodeClass, new HashSet<NodeFunction<? extends Node>>());
             }
             nodeFunctions.get(nodeClass).add(nodeFunction);
         }
         return nodeFunctions;
     }
 
     private Server jetty;
 
     public void startTestWebServer() {
         try {
             log.debug("Starting jetty web-server");
             jetty = new Server(4442);
             jetty.setHandler(new TestHandler());
 
             jetty.start();
             //server.join();
 
         } catch (Exception e) {
             log.error("Error starting jetty web-server", e);
             throw new RuntimeException(e);
         }
     }
 
     public void stopTestWebServer() {
         if (jetty == null)
             return;
         try {
             log.debug("Stopping jetty web-server");
             jetty.stop();
         } catch (Exception e) {
             log.error("Error stoping jetty web-server", e);
             throw new RuntimeException(e);
         } finally {
             jetty = null;
         }
     }
 
 }
