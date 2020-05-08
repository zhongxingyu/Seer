 package hudson.plugins.sctmexecutor.publisher.handler;
 
 import hudson.plugins.sctmexecutor.publisher.SCTMTestCaseResult;
 import hudson.plugins.sctmexecutor.publisher.SCTMTestResult;
 import hudson.plugins.sctmexecutor.publisher.SCTMTestResult.TestState;
 import hudson.plugins.sctmexecutor.publisher.SCTMTestSuiteResult;
 import hudson.tasks.test.TestResult;
 
 import java.util.Stack;
 import java.util.logging.Logger;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 public class OutputXMLParserHandler extends DefaultHandler {
   private static final Logger LOGGER = Logger.getLogger("hudson.plugins.sctmexecutor");
 
   private final SCTMTestSuiteResult rootSuiteResult;
   private final Stack<TestResult> resultStack;
   private final String configuration;
 
   private boolean nunitSuiteFound;
  private boolean testCase;
   private boolean wasSuccessElement;
   private boolean timerElement;
   private boolean messageElement;
   private boolean infoElement;
 
   private TestState state = TestState.SKIPPED;
   private float duration;
   private StringBuilder errorMessage = new StringBuilder();
 
   public OutputXMLParserHandler(SCTMTestSuiteResult suiteResult, String configuration) {
     this.rootSuiteResult = suiteResult;
     this.configuration = configuration;
     this.resultStack = new Stack<TestResult>();
 
     this.resultStack.push(rootSuiteResult);
   }
 
   @Override
   public void startDocument() throws SAXException {
 
   }
 
   @Override
   public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
     if ("TestSuite".equals(qName) || ("ResultElement".equals(qName))) {
       String name = attributes.getValue("TestItem");
       if (name.contains(".dll")) {
         nunitSuiteFound = true;
         name = name.substring(name.lastIndexOf('\\') + 1, name.length() - 4);
       }
 
       if (!name.equals(configuration)) {
         SCTMTestSuiteResult temp = (SCTMTestSuiteResult) this.resultStack.peek();
         SCTMTestSuiteResult child = temp.getChildSuiteByName(name);
         if (child == null) {
           child = new SCTMTestSuiteResult(name);
           temp.addChild(child);
           child.setParent(temp);
         }
         this.resultStack.push(child);
       }
     } else if ("Test".equals(qName)) {
      this.testCase = true;
       String name = attributes.getValue("TestItem");
       SCTMTestSuiteResult suite = (SCTMTestSuiteResult) this.resultStack.peek();
       SCTMTestCaseResult child = suite.getChildTestByName(name);
       if (child == null) {
         child = new SCTMTestCaseResult(name);
         suite.addChild(child);
         child.setParent(suite);
       }
 
       // reset members for next test
       state = TestState.SKIPPED;
       duration = 0.0f;
       errorMessage = new StringBuilder();
       this.resultStack.push(child);
 
     } else if ("Timer".equals(qName)) {
       timerElement = true;
     } else if ("WasSuccess".equals(qName)) {
       wasSuccessElement = true;
     } else if ("Message".equals(qName)) {
       messageElement = true;
     } else if ("Info".equals(qName)) {
       infoElement = true;
     }
   }
 
   @Override
   public void characters(char[] ch, int start, int length) throws SAXException {
     String item = new String(ch, start, length);
     if (wasSuccessElement)
       state = Boolean.parseBoolean(item) ? TestState.PASSED : TestState.FAILED;
     else if (timerElement) {
       float f = Float.parseFloat(item);
       if (nunitSuiteFound)
         duration = f * 1000; // because the result for nunit is given in s
       else
         duration = f;
     } else if (messageElement) {
       errorMessage.append("<b>Error: </b><br/>");
       errorMessage.append(item);
     } else if (infoElement) {
       errorMessage.append("<b>Stacktrace: </b><br/>");
       errorMessage.append(item);
     }
   }
 
   @Override
   public void endElement(String uri, String localName, String qName) throws SAXException {
     if ("TestSuite".equals(qName) || ("ResultElement".equals(qName))) {
       if (!resultStack.empty()) // in case of the configuration TestSuite the stack is empty
         this.resultStack.pop();
     } else if ("Test".equals(qName)) {
       SCTMTestCaseResult result = (SCTMTestCaseResult) this.resultStack.pop();
       result.addConfigurationResult(this.configuration, new SCTMTestResult(state, duration, errorMessage.toString()));
      this.testCase = false;
     } else if ("Timer".equals(qName)) {
       timerElement = false;
     } else if ("WasSuccess".equals(qName)) {
       wasSuccessElement = false;
     } else if ("Message".equals(qName)) {
       messageElement = false;
     } else if ("Info".equals(qName)) {
       infoElement = false;
     }
   }
 }
