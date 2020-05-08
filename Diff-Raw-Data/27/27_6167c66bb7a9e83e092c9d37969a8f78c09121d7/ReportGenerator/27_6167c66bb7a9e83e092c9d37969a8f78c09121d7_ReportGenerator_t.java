 package org.jaccept.testreport;
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.helpers.Loader;
 import org.jaccept.TestEventListener;
 import org.testng.ISuite;
 import org.testng.ITestContext;
 import org.testng.ITestResult;
 import org.testng.xml.XmlSuite;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.*;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.Arrays;
 import java.util.List;
 
 
 public class ReportGenerator implements TestEventListener {
     private static final String STYLE_PATH = "org/jaccept/testreport/testreport.xsl";
     private final Logger log = Logger.getLogger(ReportGenerator.class.getName());
 
     private static InputStream styleSheet = null;
     private static boolean specMode = false;
     private static File testreportFile = new File("target" + File.separator + "" + "jaccept-testreport.html");
     private static File xmlReportFile = new File("target" + File.separator + "" + "jaccept-testreport.xml");
 
     private static final boolean debug = true;
     private static int step = 0;
 
     private Document doc;
     private Element testprojects;
     private Element currentProject;
     private Element currentSuite;
     private Element currentCase;
     private Element currentTest;
     private Element currentStep;
     private Element currentElement;
 
     public ReportGenerator() {
         String specString = System.getProperty("specMode");
         if (specString != null && specString.equals("true")) {
             specMode = true;
         }
         initStylesheet();
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         try {
             DocumentBuilder builder = factory.newDocumentBuilder();
             doc = builder.newDocument();
         } catch (ParserConfigurationException e) {
             throw new RuntimeException(e.getMessage());
         }
         testprojects = doc.createElement("testprojects");
         doc.appendChild(testprojects);
     }
 
     private void initStylesheet() {
         URL jacceptPropertiesUrl = Loader.getResource("testreport.xsl");
         try {
             styleSheet = jacceptPropertiesUrl.openStream();
         } catch (IOException e) {
             throw new RuntimeException("Unable to open stylesheet", e);
         }
 
         if (styleSheet == null) log.fatal("Unable to find style sheet " + STYLE_PATH);
     }
 
     @Override
     public void projectStart(String name) {
         currentProject = simpleAppend(testprojects, "project", name);
         currentElement = currentProject;
         currentSuite = currentCase = currentTest = currentStep = null;
     }
 
     @Override
     public void suiteStart(String name) {
         currentSuite = simpleAppend(currentProject, "suite", name);
         currentElement = currentSuite;
         currentCase = currentTest = currentStep = null;
     }
 
     @Override
     public void classStart(String name) {
         currentCase = simpleAppend(currentSuite, "case", name);
         currentElement = currentCase;
         currentTest = currentStep = null;
     }
 
     @Override
     public void testStart(String name) {
         currentTest = simpleAppend(currentCase, "test", name);
         currentElement = currentTest;
         currentStep = null;
         step = 0;
     }
 
     @Override
     public void testFailure(ITestResult result) {
        if(currentTest == null) {
            log.warn("Failure before test, " +result.getThrowable().getLocalizedMessage());
            return;
        }
         if (currentStep != null) {
             currentStep.setAttribute("outcome", "&divide;");
         }
         currentElement = currentTest;
         simpleAppend(currentElement, "failure", result.getThrowable().getLocalizedMessage());
     }
 
     @Override
     public void testSuccess(ITestResult result) {}
 
     @Override
     public void classFinish() {}
 
     @Override
     public void suiteFinish() {}
 
     @Override
     public void stepStart(String stimuli, String expectedResult) {
         currentStep = simpleAppend(currentTest, "step", "");
         currentStep.setAttribute("outcome", "&#8730;");
         textAppend(currentStep, "number", "" + ++step);
         textAppend(currentStep, "stimuli", stimuli);
         textAppend(currentStep, "expectedResult", expectedResult);
         currentElement = currentStep;
     }
 
     @Override
     public void addFixture(String setupDescription) {
         stepStart(setupDescription, "");
     }
 
     @Override
     public void stepEnd() {}
 
     @Override
     public void addDescription(String description) {
         check("Description");
         simpleAppend(currentElement, "def", description);
     }
 
     @Override
     public void addReference(String reference) {
         check("Reference");
         simpleAppend(currentElement, "ref", reference);
     }
 
     @Override
     public void projectFinish() {
         if (debug) System.out.println("Generating test reports .... ");
         try {
             Transformer transformer = TransformerFactory.newInstance().newTransformer(new StreamSource(styleSheet));
             transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
             transformer.setOutputProperty(OutputKeys.INDENT, "yes");
             transformer.setParameter("specMode", String.valueOf(specMode));
 
             Transformer identityTransformer = TransformerFactory.newInstance().newTransformer();
 
             NodeList projects = doc.getFirstChild().getChildNodes();
             String name;
 
             for (int i = 0; i < projects.getLength(); i++) {
                 name = ((Element) projects.item(i)).getAttribute("name");
                 if (debug) System.out.println("Generating project at : " + testreportFile.getAbsoluteFile());
                 Source source = new DOMSource(projects.item(i));
                 Result result = new StreamResult(testreportFile);
                 transformer.transform(source, result);
                 System.out.println("Generated project: " + name);
 
                 identityTransformer.transform(source, new StreamResult(xmlReportFile));
             }
         } catch (TransformerException e) {
             throw new RuntimeException(e);
         }
     }
 
     private Element simpleAppend(Element parent, String kind, String name) {
         if (parent == null) {
            throw new IllegalStateException("No parent in hierarchy for " + kind);
         }
         Element child = doc.createElement(kind);
         if (name != null && !name.equals("")) child.setAttribute("name", name);
         parent.appendChild(child);
         return child;
     }
 
     private Element textAppend(Element parent, String kind, String text) {
         Element element = simpleAppend(parent, kind, "");
         element.appendChild(doc.createTextNode(text));
         return element;
     }
 
     private void check(String kind) {
         if (currentElement == null || currentElement == currentStep) {
             throw new IllegalStateException
                     (kind + " can only be added to a project, a suite, a case or a test");
         }
     }
 
     public void addStimuli(String stimuli) {
        if (currentStep == null) return;// Test not started yet
         simpleAppend(currentStep, "stimuli", stimuli);
     }
 
     public void addResult(String result) {
        if (currentStep == null) return;// Test not started yet
         simpleAppend(currentStep, "result", result);
     }
 
     public void testSuccess() {
         // TODO Auto-generated method stub
     }
 
     public void onFinish(ISuite arg0) {
         // TODO Auto-generated method stub
     }
 
     public static void setTestreportFile(File testreportFile) {
         ReportGenerator.testreportFile = testreportFile;
     }
 }
