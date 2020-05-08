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
 package ru.histone.acceptance;
 
 import com.fasterxml.jackson.core.JsonToken;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.node.ArrayNode;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 
 import org.junit.ComparisonFailure;
 import org.junit.runner.Description;
 import org.junit.runner.RunWith;
 import org.junit.runner.Runner;
 import org.junit.runner.notification.Failure;
 import org.junit.runner.notification.RunNotifier;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.slf4j.MDC;
 import ru.histone.GlobalProperty;
 import ru.histone.Histone;
 import ru.histone.HistoneBuilder;
 import ru.histone.HistoneException;
 import ru.histone.HistoneTokensHolder;
 import ru.histone.acceptance.helpers.*;
 import ru.histone.evaluator.EvaluatorException;
 import ru.histone.evaluator.functions.global.GlobalFunction;
 import ru.histone.evaluator.functions.node.NodeFunction;
 import ru.histone.evaluator.nodes.*;
 import ru.histone.parser.Parser;
 import ru.histone.parser.ParserException;
 import ru.histone.tokenizer.TokenizerFactory;
 import ru.histone.utils.CollectionUtils;
 
 import javax.xml.stream.XMLInputFactory;
 import javax.xml.stream.XMLStreamConstants;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 import javax.xml.stream.util.StreamReaderDelegate;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.StringReader;
 import java.net.URI;
 import java.net.URL;
 import java.util.*;
 import java.util.Map.Entry;
 
 @RunWith(HistoneAcceptanceTest.class)
 public class HistoneAcceptanceTest extends Runner {
     private static final Logger log = LoggerFactory.getLogger(HistoneAcceptanceTest.class);
     protected static final String MDC_TEST_NAME = "testCaseName";
 
     private Description testSuiteDescription;
     private static final XMLInputFactory inputFactory = XMLInputFactory.newInstance();
 
     private ObjectMapper jackson;
     private Parser parser;
     private NodeFactory nodeFactory;
 
     public HistoneAcceptanceTest(Class<?> testClass) {
         testSuiteDescription = Description.createSuiteDescription("Histone Acceptance Test Cases");
         jackson = new ObjectMapper();
         nodeFactory = new NodeFactory(null);
     }
 
     @Override
     public Description getDescription() {
         return testSuiteDescription;
     }
 
     @Override
     public void run(RunNotifier notifier) {
         MDC.put(MDC_TEST_NAME, "before");
         Reader reader = new InputStreamReader(getClass().getResourceAsStream("/acceptance-test-cases.json"));
         try {
             JsonNode testCasesList = jackson.readTree(reader);
             Iterator<JsonNode> iter = testCasesList.iterator();
             while (iter.hasNext()) {
                 JsonNode element = iter.next();
                 runTestCasesFromJsonFile(notifier, element.asText());
             }
         } catch (IOException e) {
             throw new RuntimeException("Error reading json", e);
         }
     }
 
     private void runTestCasesFromJsonFile(RunNotifier notifier, String fileName) {
         //TODO change all this stuff to using ObjectMapper to read json files
     	
         try {
            Reader reader = new InputStreamReader(getClass().getResourceAsStream("/"+fileName));
             try {
             	//get constructions from filename
             	final String constructions = fileName.substring(fileName.lastIndexOf("/") + 1,fileName.lastIndexOf(".json"));
                 JsonNode list = jackson.readTree(reader);
                 final JsonNode mainElement = list.get(0);
                 final String name = mainElement.get("name").asText();
                 final ArrayNode cases = (ArrayNode) mainElement.get("cases");
 				final TestSuiteHolder suite = new TestSuiteHolder(name, constructions);
 				Iterator<JsonNode> iter = cases.iterator();
 				while (iter.hasNext()) {
                 	JsonNode element = iter.next();
                     readCase(notifier, element, suite);
                 }
             } catch (IOException e) {
                 throw new RuntimeException("Error reading json", e);
             }
 //            XMLStreamReader xmlStreamReader = new StreamReaderDelegate(inputFactory.createXMLStreamReader(getClass().getResourceAsStream("/evaluator/" + fileName))) {
 //                public int next() throws XMLStreamException {
 //                    while (true) {
 //                        int event = super.next();
 //                        switch (event) {
 //                            case XMLStreamConstants.COMMENT:
 //                            case XMLStreamConstants.PROCESSING_INSTRUCTION:
 //                                continue;
 //                            default:
 //                                return event;
 //                        }
 //                    }
 //                }
 //            };
 //
 //            TestSuiteHolder suite = null;
 //            while (xmlStreamReader.hasNext()) {
 //                int event = xmlStreamReader.next();
 //                log.debug("run(): event={}", new Object[]{event});
 //
 //                if (event == XMLStreamConstants.START_ELEMENT) {
 //                    if ("suite".equals(xmlStreamReader.getLocalName())) {
 //                        suite = new TestSuiteHolder(fileName, xmlStreamReader.getAttributeValue(null, "name"));
 //                    } else if ("case".equals(xmlStreamReader.getLocalName())) {
 //                        readCase(notifier, xmlStreamReader, suite);
 //                    }
 //                } else if (event == XMLStreamConstants.END_ELEMENT) {
 //                    if ("suite".equals(xmlStreamReader.getLocalName())) {
 //                        suite = null;
 //                    }
 //                }
 //            }
         } catch (Exception e) {
             notifier.fireTestFailure(new Failure(Description.createTestDescription(this.getClass(), fileName), e));
         }
     }
 
 	private void readCase(RunNotifier notifier, JsonNode caseNode, TestSuiteHolder suite) {
 		log.debug("readCase(): {}", new Object[] { caseNode });
 		TestCaseHolder testCase = new TestCaseHolder(suite);
 		if (caseNode.get("input") != null) {
 			testCase.setInput(caseNode.get("input").asText());
 		}
 		if (caseNode.get("context") != null) {
             testCase.setContext(caseNode.get("context").asText());
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
 		if (caseNode.get("function") != null) { 
 			final JsonNode node = caseNode.get("function"); 			
             final String name = node.get("name").asText();
             final String result = node.get("result").asText();
             final String nodeType = node.get("node") == null ? null:  node.get("node").asText();
 //            String data = null);
 //            if (nodeType == null) {
 //                testCase.addMockGlobalFunction(new MockGlobalFunctionHolder(name, returnType, data));
 //            } else {
 //                testCase.addMockNodeFunction(new MockNodeFunctionHolder(name, nodeType, returnType, data));
 //            }
 			throw new RuntimeException("Not supported tagName: function");
 		}
 		if (caseNode.get("global") != null) {
 			throw new RuntimeException("Not supported tagName: global");
 		}
 		if (caseNode.get("expectedException") != null) {
 			 testCase.setException(readException(caseNode.get("expectedException")));
 		}
 		runTestCase(notifier, testCase);
 		log.debug("readCase(): >>>", new Object[] {});
 	}
 
     private EvaluatorException readException(JsonNode node) {
         final int line = node.get("line").asInt();
         final String expected = node.get("expected").asText();
         final String found = node.get("found").asText();
         return new EvaluatorException(line, expected, found);
     }
 
     private void readCase(RunNotifier notifier, XMLStreamReader xmlStreamReader, TestSuiteHolder suite) throws XMLStreamException {
         log.debug("readCase(): >>>", new Object[]{});
         TestCaseHolder testCase = new TestCaseHolder(suite);
 
         while (xmlStreamReader.hasNext()) {
             int event = xmlStreamReader.next();
             log.debug("readCase(): event={}", new Object[]{event});
 
             if (event == XMLStreamConstants.START_ELEMENT) {
                 String tagName = xmlStreamReader.getLocalName();
                 if ("input".equals(tagName)) {
                     testCase.setInput(readTagValue(xmlStreamReader));
                 }
                 if ("context".equals(tagName)) {
                     testCase.setContext(readTagValue(xmlStreamReader));
                 }
                 if ("expected".equals(tagName)) {
                     testCase.setExpected(readTagValue(xmlStreamReader));
                 }
                 if ("data".equals(tagName)) {
                     String location = xmlStreamReader.getAttributeValue(null, "url");
                     String data = readTagValue(xmlStreamReader);
                     testCase.addMockFile(new MockFileDataHolder(location, data));
                 }
                 if ("function".equals(tagName)) {
                     String name = xmlStreamReader.getAttributeValue(null, "name");
                     String returnType = xmlStreamReader.getAttributeValue(null, "return");
                     String nodeType = xmlStreamReader.getAttributeValue(null, "node");
                     String data = readTagValue(xmlStreamReader);
                     if (nodeType == null) {
                         testCase.addMockGlobalFunction(new MockGlobalFunctionHolder(name, returnType, data));
                     } else {
                         testCase.addMockNodeFunction(new MockNodeFunctionHolder(name, nodeType, returnType, data));
                     }
                 }
                 if ("global".equals(tagName)) {
                     String name = xmlStreamReader.getAttributeValue(null, "name");
                     String value = xmlStreamReader.getAttributeValue(null, "value");
                     testCase.addGlobalProp(name, value);
                 }
                 if ("exception".equals(tagName)) {
                     testCase.setException(readException(xmlStreamReader));
                 }
 
             } else if (event == XMLStreamConstants.END_ELEMENT) {
                 String tagName = xmlStreamReader.getLocalName();
                 if ("case".equals(tagName)) {
                     break;
                 }
             }
 
         }
         runTestCase(notifier, testCase);
         log.debug("readCase(): >>>", new Object[]{});
     }
 
     private EvaluatorException readException(XMLStreamReader xmlStreamReader) throws XMLStreamException {
         String expected = null, found = null;
         int line = 0;
 
         while (xmlStreamReader.hasNext()) {
             int event = xmlStreamReader.next();
 
             if (event == XMLStreamConstants.START_ELEMENT) {
                 if ("line".equals(xmlStreamReader.getLocalName())) {
                     String lineStrVal = readTagValue(xmlStreamReader);
                     try {
                         line = Integer.parseInt(lineStrVal);
                     } catch (Exception e) {
                         log.warn("{} is illegal value of \"line\" tag", lineStrVal);
                     }
                 }
                 if ("expected".equals(xmlStreamReader.getLocalName())) {
                     expected = readTagValue(xmlStreamReader);
                 }
                 if ("found".equals(xmlStreamReader.getLocalName())) {
                     found = readTagValue(xmlStreamReader);
                 }
 
             } else if (event == XMLStreamConstants.END_ELEMENT) {
                 break;
 
             }
 
         }
         return new EvaluatorException(line, expected, found);
     }
 
     private String readTagValue(XMLStreamReader xmlStreamReader) throws XMLStreamException {
         StringBuilder sb = new StringBuilder();
 
         while (xmlStreamReader.hasNext()) {
             int event = xmlStreamReader.next();
 
             if (event == XMLStreamConstants.END_ELEMENT) {
                 break;
             } else if (event == XMLStreamConstants.CHARACTERS) {
                 sb.append(xmlStreamReader.getText());
             }
         }
 
         return sb.toString();
     }
 
     long testIdx = 0;
 
     private void runTestCase(RunNotifier notifier, TestCaseHolder testCase) {
         testIdx++;
         log.debug("case({}): input={}, context={}, expected={}, exception={}", new Object[]{testIdx, testCase.getInput(), testCase.getContext(), testCase.getExpected(), testCase.getException()});
 
         Description description = Description.createTestDescription(this.getClass(), testIdx + "_" + testCase.toString());
         testSuiteDescription.addChild(description);
         notifier.fireTestStarted(description);
 
         Histone histone;
         try {
 
             final String baseURI = findBaseURI();
             testCase.setInput(testCase.getInput() == null ? null: testCase.getInput().replaceAll("\\:baseURI\\:", baseURI));
             testCase.setExpected(testCase.getExpected() == null ? null: testCase.getExpected().replaceAll("\\:baseURI\\:", baseURI));
             if (testCase.getGlobalProperties().containsKey(GlobalProperty.BASE_URI)) {
                 String oldBaseURI = testCase.getGlobalProperties().get(GlobalProperty.BASE_URI);
                 testCase.getGlobalProperties().put(GlobalProperty.BASE_URI, oldBaseURI.replaceAll("\\:baseURI\\:", baseURI));
             } else {
                 testCase.getGlobalProperties().put(GlobalProperty.BASE_URI, baseURI);
             }
 
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
 
             histone = histoneBuilder.build();
         } catch (HistoneException e) {
             notifier.fireTestFailure(new Failure(description, e));
             return;
         }
         //***** RUN TESTS *****
 		final ArrayNode expectedAST = testCase.getExpectedAST();
 		final EvaluatorException expectedException = testCase.getException();
 		try {
 			if (expectedAST != null) {
 				// TEST FOR EXPECTEDAST
 				Reader input = new StringReader(testCase.getInput());
 				ArrayNode ast = histone.parseTemplateToAST(input);
 				final String expectedASTString = ast.toString();
 				final String aSTString = ast.toString();
 				log.debug("case({}): tree.json={}", new Object[] { testIdx, aSTString });
 				boolean result = expectedASTString.equals(aSTString);
 				log.debug("case({}): result={}", new Object[] { testIdx, result });
 				if (result) {
 					notifier.fireTestFinished(description);
 				} else {
 					String msgF = "For input='" + testCase.getInput() + "'";
 					notifier.fireTestFailure(new Failure(description,
 							new ComparisonFailure(msgF, expectedASTString, aSTString)));
 				}
 			} else if (expectedException != null) {
 				// TEST FOR EXPECTEDEXCEPTION
 				// we need to check for ParserException and EvaluatorException
 				// separately
 				final String msgF = "For input='" + testCase.getInput() + "'";
 				ArrayNode ast;
 				try {
 					Reader input = new StringReader(testCase.getInput());
 					ast = histone.parseTemplateToAST(input);
 					log.debug("case({}): tree.json={}", new Object[] { testIdx, ast.toString() });
 				} catch (ParserException e) {
 					log.debug("case({}): e.message={}", new Object[] { testIdx, e.getMessage() });
 					checkExpectedExceptionAndFireTest(notifier, description, msgF, expectedException, e);
 					return;
 				}
 
 				try {
 					JsonNode context = testCase.getContext() == null ? null : jackson.readTree(testCase.getContext());
 					String output = histone.evaluateAST(ast, context);
 					log.debug("case({}): output={}", new Object[] { testIdx, output });
 				} catch (EvaluatorException e) {
 					log.debug("case({}): e.message={}", new Object[] { testIdx, e.getMessage() });
 					checkExpectedExceptionAndFireTest(notifier, description, msgF, expectedException, e);
 					return;
 				}
 
 				String expectedF = "expected=" + expectedException.getExpected() + ", found=" + expectedException.getFound();
 				notifier.fireTestFailure(new Failure(description, new ComparisonFailure(msgF, expectedF, "")));
 			} else {
 				// TEST FOR OUTPUT
 				Reader input = new StringReader(testCase.getInput());
 				JsonNode context = (testCase.getContext() == null) ? jackson.getNodeFactory().nullNode() : jackson.readTree(testCase
 						.getContext());
 				String output = histone.evaluate(input, context);
 				log.debug("case({}): output={}", new Object[] { testIdx, output });
 
 				boolean result = output.equals(testCase.getExpected());
 				log.debug("case({}): result={}", new Object[] { testIdx, result });
 				if (result) {
 					notifier.fireTestFinished(description);
 				} else {
 					String msgF = "For input='" + testCase.getInput() + "'";
 					notifier.fireTestFailure(new Failure(description, new ComparisonFailure(msgF, testCase.getExpected(), output)));
 				}
 			}
 		} catch (Exception e) {
 			String msg = "For input='" + testCase.getInput() + "', expected=" + testCase.getExpected() + ", but exception occured="
 					+ e.getMessage();
 			log.debug("Error msg={}", msg, e);
 			notifier.fireTestFailure(new Failure(description, e));
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
 			notifier.fireTestFailure(new Failure(description, new ComparisonFailure(msgF, expectedF, actualF)));
 			return;
 		}
 	}
 
     private String findBaseURI() throws HistoneException {
         URL baseURI = this.getClass().getClassLoader().getResource("acceptance-test-cases.json");
 
         if (baseURI == null) {
            throw new HistoneException("Error searching for \"acceptance-test-cases.json\" in classpath. Can't determine baseURI value.");
         }
 
         String result = baseURI.toString().substring(0,baseURI.toString().indexOf("acceptance-test-cases.json"));
 
         return result;
     }
 
     private Set<GlobalFunction> toGlobalFunctions(Set<MockGlobalFunctionHolder> mockFunctions) {
         Set<GlobalFunction> globalFunctions = new HashSet<GlobalFunction>();
         for (final MockGlobalFunctionHolder function : mockFunctions) {
             GlobalFunction globalFunction = new MockGlobalFunction(nodeFactory,function.getName(), function.getReturnType(), function.getData());
             globalFunctions.add(globalFunction);
         }
         return globalFunctions;
     }
 
     private Map<Class<? extends Node>, Set<NodeFunction<? extends Node>>> toNodeFunctions(Set<MockNodeFunctionHolder> mockFunctions) {
         Map<Class<? extends Node>, Set<NodeFunction<? extends Node>>> nodeFunctions = new HashMap<Class<? extends Node>, Set<NodeFunction<? extends Node>>>();
 
         for (final MockNodeFunctionHolder function : mockFunctions) {
             NodeFunction nodeFunction = new MockNodeFunction(nodeFactory, function.getName(), function.getReturnType(), function.getData());
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
 
 }
