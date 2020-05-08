 package org.dita.dost;
 
 import static org.custommonkey.xmlunit.XMLAssert.assertXMLEqual;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.IOException;
import java.util.Arrays;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Vector;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import nu.validator.htmlparser.dom.HtmlDocumentBuilder;
 
 import org.apache.tools.ant.BuildEvent;
 import org.apache.tools.ant.BuildListener;
 import org.apache.tools.ant.MagicNames;
 import org.apache.tools.ant.Project;
 import org.apache.tools.ant.ProjectHelper;
 import org.custommonkey.xmlunit.XMLUnit;
 import org.dita.dost.util.FileUtils;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 import org.junit.runners.Parameterized.Parameters;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 @RunWith(Parameterized.class)
 public final class IntegrationTest {
     
     private static final Collection<String> canCompare = Arrays.asList(new String[] { "xhtml", "preprocess" });
     
     private static final File resourceDir = new File("testcase");
     private static final File resultDir = new File("testresult");
     private static DocumentBuilder db;
     private static HtmlDocumentBuilder htmlb;
 
     private final File testDir;
     
     /**
      * Get test cases
      * 
      * @return test cases which have comparable expected results
      */
     @Parameters
     public static Collection<Object[]> getFiles() {
         final File[] cases = resourceDir.listFiles(new FileFilter() {
             @Override
             public boolean accept(File f) {
                 if (!f.isDirectory() || !new File(f, "build.xml").exists()) {
                     return false;
                 }
if (!f.getName().startsWith("Metadata")) return false;
                 final File exp = new File(f, "exp");
                 if (exp.exists()) {
                     for (final String t: exp.list()) {
                         if (canCompare.contains(t)) {
                             return true;
                         }
                     }
                 }
                 return false;
             }}); 
         final Collection<Object[]> params = new ArrayList<Object[]>(cases.length);
         for (final File f : cases) {
                 final Object[] arr = new Object[] { f };
                 params.add(arr);
         }
         return params;
     }
     
     public IntegrationTest(final File testDir) {
         this.testDir = testDir;
     }
     
     @BeforeClass
     public static void setUpBeforeClass() throws Exception {
         db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
         htmlb = new HtmlDocumentBuilder();
     }
     
     @Test
     public void test() throws Exception {
         final File expDir = new File(testDir, "exp");
         
         System.out.println(testDir.getName());
         try {
             run(testDir, expDir.list());
             compare(expDir, new File(resultDir, testDir.getName()));
         } catch (final Exception e) {
             throw new Exception("Case " + testDir.getName() + " failed: " + e.getMessage(), e);
         }
         
     }
     
     /**
      * Run test conversion
      * 
      * @param d test source directory
      * @param transtypes list of transtypes to test
      * @throws Exception if conversion failed
      */
     private void run(final File d, final String[] transtypes) throws Exception {
         if (transtypes.length == 0) {
             return;
         }
         final File buildFile = new File(d, "build.xml");
         final Project project = new Project();
         project.addBuildListener(new TestListener());
         project.fireBuildStarted();
         project.init();
         for (final String transtype: transtypes) {
             if (canCompare.contains(transtype)) {
                 project.setUserProperty("run." + transtype, "true");
             }
         }
         project.setUserProperty("preprocess.copy-generated-files.skip", "true");
        project.setUserProperty("ant.file", buildFile.getAbsolutePath());
        project.setUserProperty("ant.file.type", "file");
         project.setKeepGoingMode(false);
         ProjectHelper.configureProject(project, buildFile);
         final Vector<String> targets = new Vector<String>();
         targets.addElement(project.getDefaultTarget());
         project.executeTargets(targets);
     }
 
     private void compare(final File exp, final File act) throws Exception {
         for (final File e: exp.listFiles()) {
             final File a = new File(act, e.getName());
             if (a.exists()) {
                 if (e.isDirectory()) {
                     compare(e, a);
                 } else {
                     final String name = e.getName();
                     if (name.endsWith(".html") || name.endsWith(".htm") || name.endsWith(".xhtml")) {
                         TestUtils.resetXMLUnit();
                         XMLUnit.setNormalizeWhitespace(true);
                         XMLUnit.setIgnoreWhitespace(true);
                         XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true);
                         assertXMLEqual(parseHtml(e), parseHtml(a));
                     } else if (FileUtils.isDITAFile(name)) {
                         TestUtils.resetXMLUnit();
                         XMLUnit.setNormalizeWhitespace(true);
                         XMLUnit.setIgnoreWhitespace(true);
                         XMLUnit.setIgnoreDiffBetweenTextAndCDATA(true);
                         assertXMLEqual(parseXml(e), parseXml(a));
                     }
                 }
             }
         }
     }
     
     private Document parseHtml(final File f) throws SAXException, IOException {
         final Document d = htmlb.parse(f);
         return d;
     }
     
     private Document parseXml(final File f) throws SAXException, IOException {
         final Document d = db.parse(f);
         final NodeList elems = d.getElementsByTagName("*");
         for (int i = 0; i < elems.getLength(); i++) {
             final Element e = (Element) elems.item(i);
             for (final String a: new String[] {"xtrf", "xtrc"}) {
                 e.removeAttribute(a);
             }
         }
         return d;
     }
     
     static class TestListener implements BuildListener {
 
         @Override
         public void buildStarted(BuildEvent event) {
             //System.out.println("build started: " + event.getMessage());
         }
 
         @Override
         public void buildFinished(BuildEvent event) {
             //System.out.println("build finished: " + event.getMessage());
         }
 
         @Override
         public void targetStarted(BuildEvent event) {
             //System.out.println(event.getTarget().getName() + ":");
         }
 
         @Override
         public void targetFinished(BuildEvent event) {
             //System.out.println("target finished: " + event.getTarget().getName());
         }
 
         @Override
         public void taskStarted(BuildEvent event) {
             //System.out.println("task started: " + event.getTask().getTaskName());
         }
 
         @Override
         public void taskFinished(BuildEvent event) {
             //System.out.println("task finished: " + event.getTask().getTaskName());
         }
 
         @Override
         public void messageLogged(BuildEvent event) {
             switch (event.getPriority()) {
             case Project.MSG_DEBUG:
             case Project.MSG_VERBOSE:
                 break;
             case Project.MSG_INFO:
                 //System.out.println(event.getMessage());
                 break;
             default:
                 System.err.println(event.getMessage());
             }
         }
     }
     
 }
