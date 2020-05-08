 package org.dita.dost;
 
 import static org.custommonkey.xmlunit.XMLAssert.*;
 import static org.dita.dost.writer.DitaWriter.*;
 import static org.junit.Assert.*;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Vector;
 import java.util.concurrent.atomic.AtomicInteger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import nu.validator.htmlparser.dom.HtmlDocumentBuilder;
 import org.apache.tools.ant.BuildEvent;
 import org.apache.tools.ant.BuildListener;
 import org.apache.tools.ant.DemuxOutputStream;
 import org.apache.tools.ant.Project;
 import org.apache.tools.ant.ProjectHelper;
 import org.custommonkey.xmlunit.XMLUnit;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 import org.junit.runners.Parameterized.Parameters;
 import org.w3c.dom.Attr;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 import org.dita.dost.util.FileUtils;
 
 @RunWith(Parameterized.class)
 public final class IntegrationTest {
     
     private static final String EXP_DIR = "exp";
     private static final Collection<String> canCompare = Arrays.asList("xhtml", "eclipsehelp", "htmlhelp", "preprocess", "pdf");
     
     private static final File baseDir = new File(System.getProperty("basedir") != null
                                                  ? System.getProperty("basedir")
                                                  : "src" + File.separator + "test" + File.separator + "testsuite");
     private static final File resourceDir = new File(baseDir, "testcase");
     private static final File resultDir = new File(baseDir, "testresult");
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
    	final Set<String> testNames = System.getProperty("only-test") != null && !System.getProperty("only-test").isEmpty()
     							  ? new HashSet<String>(Arrays.asList(System.getProperty("only-test").split("[\\s|,]")))
     							  : null;
         final List<File> cases = Arrays.asList(resourceDir.listFiles(new FileFilter() {
             public boolean accept(File f) {
             	if (testNames != null && !testNames.contains(f.getName())) {
             		return false;
             	}
                 if (!f.isDirectory() || !new File(f, "build.xml").exists()) {
                     return false;
                 }
                 final File exp = new File(f, EXP_DIR);
                 if (exp.exists()) {
                     for (final String t: exp.list()) {
                         if (canCompare.contains(t)) {
                             return true;
                         }
                     }
                 }
                 return false;
             }}));
         Collections.sort(cases, new Comparator<File>() {
                 public int compare(File arg0, File arg1) {
                     return arg0.compareTo(arg1);
                 }
             });
         final List<Object[]> params = new ArrayList<Object[]>(cases.size());
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
     public void test() throws Throwable {
         final File expDir = new File(testDir, EXP_DIR);
         System.err.println("Testcase: " + testDir.getName());
         try {
             run(testDir, expDir.list());
             compare(expDir, new File(resultDir, testDir.getName()));
         } catch (final Throwable e) {
             throw new Throwable("Case " + testDir.getName() + " failed: " + e.getMessage(), e);
         }
         
     }
     
     private int countMessages(final List<TestListener.Message> messages, final int level) {
         int count = 0;
         for (final TestListener.Message m: messages) {
             if (m.level == level) {
                 count++;
             }
         }
         return count;
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
         final TestListener listener = new TestListener(System.out, System.err);
         final PrintStream savedErr = System.err;
         final PrintStream savedOut = System.out;
         try {
             final File buildFile = new File(d, "build.xml");
             final Project project = new Project();
             project.addBuildListener(listener);
             System.setOut(new PrintStream(new DemuxOutputStream(project, false)));
             System.setErr(new PrintStream(new DemuxOutputStream(project, true)));
             project.fireBuildStarted();
             project.init();
             for (final String transtype: transtypes) {
                 if (canCompare.contains(transtype)) {
                     project.setUserProperty("run." + transtype, "true");
                     if (transtype.equals("pdf") || transtype.equals("pdf2")) {
                     	project.setUserProperty("pdf.formatter", "fop");
                     	project.setUserProperty("fop.formatter.output-format", "text/plain");
                     }
                 }
             }
             project.setUserProperty("preprocess.copy-generated-files.skip", "true");
             project.setUserProperty("ant.file", buildFile.getAbsolutePath());
             project.setUserProperty("ant.file.type", "file");
             final String ditaDirProperty = System.getProperty("dita.dir");
             project.setUserProperty("dita.dir", new File(ditaDirProperty != null
                                                          ? ditaDirProperty
                                                          : "src" + File.separator + "main").getAbsolutePath());
             project.setUserProperty("result.dir", new File(resultDir, d.getName()).getAbsolutePath());
             project.setKeepGoingMode(false);
             ProjectHelper.configureProject(project, buildFile);
             final Vector<String> targets = new Vector<String>();
             targets.addElement(project.getDefaultTarget());
             project.executeTargets(targets);
             
             assertEquals("Warn message count does not match expected",
                          project.getProperty("exp.message-count.warn") != null ? Integer.parseInt(project.getProperty("exp.message-count.warn")) : 0,
                          countMessages(listener.messages, Project.MSG_WARN));
             assertEquals("Error message count does not match expected",
                          project.getProperty("exp.message-count.error") != null ? Integer.parseInt(project.getProperty("exp.message-count.error")) : 0,
                          countMessages(listener.messages, Project.MSG_ERR ));
         } finally {
             System.setOut(savedOut);
             System.setErr(savedErr);
         }
     }
     
     private void compare(final File exp, final File act) throws Throwable {
         for (final File e: exp.listFiles()) {
             final File a = new File(act, e.getName());
             if (a.exists()) {
                 if (e.isDirectory()) {
                     compare(e, a);
                 } else {
                     final String name = e.getName();
                     try {
                         if (name.endsWith(".html") || name.endsWith(".htm") || name.endsWith(".xhtml")
                         		|| name.endsWith(".hhk")) {
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
                         } else if (name.endsWith(".txt")) {
                         	//assertEquals(readTextFile(e), readTextFile(a));
                         	assertArrayEquals(readTextFile(e), readTextFile(a));
                         }
                     } catch (final Throwable ex) {
                         throw new Throwable("Failed comparing " + e.getAbsolutePath() + " and " + a.getAbsolutePath() + ": " + ex.getMessage(), ex);
                     }
                 }
             }
         }
     }
     
     /**
      * Read text file into a string.
      * 
      * @param f file to read
      * @return file contents
      * @throws IOException if reading file failed
      */
 //    private String readTextFile(final File f) throws IOException {
 //    	final StringBuilder buf = new StringBuilder();
 //    	BufferedReader r = null;
 //    	try {
 //    		r = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
 //    		String l = null;
 //        	while ((l = r.readLine()) != null) {
 //        		buf.append(l);
 //        	}
 //    	} catch (final IOException e) {
 //    		throw new IOException("Unable to read " + f.getAbsolutePath() + ": " + e.getMessage());
 //    	} finally {
 //    		if (r != null) {
 //    			r.close();
 //    		}
 //    	}
 //    	return buf.toString();
 //    }
     private String[] readTextFile(final File f) throws IOException {
     	final List<String> buf = new ArrayList<String>();
     	BufferedReader r = null;
     	try {
     		r = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"));
     		String l = null;
         	while ((l = r.readLine()) != null) {
         		buf.add(l);
         	}
     	} catch (final IOException e) {
     		throw new IOException("Unable to read " + f.getAbsolutePath() + ": " + e.getMessage());
     	} finally {
     		if (r != null) {
     			r.close();
     		}
     	}
     	return buf.toArray(new String[buf.size()]);
     }
 
 	private static final Map<String, Pattern> htmlIdPattern = new HashMap<String, Pattern>();
     private static final Map<String, Pattern> ditaIdPattern = new HashMap<String, Pattern>();
     static {
         final String SAXON_ID = "d\\d+e\\d+";
         htmlIdPattern.put("id", Pattern.compile("(.*__)" + SAXON_ID + "|" + SAXON_ID + "(.*)"));
         htmlIdPattern.put("href", Pattern.compile("#.+?/" + SAXON_ID + "|#(.+?__)?" + SAXON_ID + "(.*)"));
         htmlIdPattern.put("headers", Pattern.compile(SAXON_ID + "(.*)"));
         
         ditaIdPattern.put("id", htmlIdPattern.get("id"));
         ditaIdPattern.put("href", Pattern.compile("#.+?/" + SAXON_ID + "|#(.+?__)?" + SAXON_ID + "(.*)"));
     }
     
     private Document parseHtml(final File f) throws SAXException, IOException {
         final Document d = htmlb.parse(f);
         return rewriteIds(d, htmlIdPattern);
     }
     
     private Document parseXml(final File f) throws SAXException, IOException {
         final Document d = db.parse(f);
         final NodeList elems = d.getElementsByTagName("*");
         for (int i = 0; i < elems.getLength(); i++) {
             final Element e = (Element) elems.item(i);
             // remove debug attributes
             for (final String a: new String[] {"xtrf", "xtrc"}) {
                 e.removeAttribute(a);
             }
             // remove workdir processing instructions
             removeWorkdirProcessingInstruction(e);
         }
         // rewrite IDs
         return rewriteIds(d, ditaIdPattern);
     }
     
     private void removeWorkdirProcessingInstruction(final Element e) {
         Node n = e.getFirstChild();
         while (n != null) {
             final Node next = n.getNextSibling();
             if (n.getNodeType() == Node.PROCESSING_INSTRUCTION_NODE &&
                     (n.getNodeName().equals(PI_WORKDIR_TARGET) || n.getNodeName().equals(PI_WORKDIR_TARGET_URI))) {
                 e.removeChild(n);
             }
             n = next;
         }
     }
     
     private Document rewriteIds(final Document doc, final Map<String, Pattern> patterns) {
         final Map<String, String> idMap = new HashMap<String, String>();
         AtomicInteger counter = new AtomicInteger();
         final NodeList ns = doc.getElementsByTagName("*");
         for (int i = 0; i < ns.getLength(); i++) {
             final Element e = (Element) ns.item(i);
             for (Map.Entry<String, Pattern> p: patterns.entrySet()) {
                 final Attr id = e.getAttributeNode(p.getKey());
                 if (id != null) {
                     //System.out.println(p.getKey() + ": " + id.getValue());
                     if (p.getKey().equals("headers")) {// split value
                         final List<String> res = new ArrayList<String>();
                         for (final String v: id.getValue().trim().split("\\s+")) {
                             rewriteId(v, idMap, counter, p.getValue());
                             if (idMap.containsKey(v)) {
                                 res.add(idMap.get(v));
                             } else {
                                 res.add(v);
                             }
                         }
                         id.setNodeValue(join(res));
 
                     } else {
                         final String v = id.getValue(); 
                         rewriteId(v, idMap, counter, p.getValue());
                         if (idMap.containsKey(v)) {
                             id.setNodeValue(idMap.get(v));
                         }
                     }
                     //System.out.println("  -> " + id.getValue());
                 }
             }
         }
         return doc;
     }
     
     private String join(final List<String> vals) {
         final StringBuilder buf = new StringBuilder();
         for (final Iterator<String> i = vals.iterator(); i.hasNext();) {
             buf.append(i.next());
             if (i.hasNext()) {
                 buf.append(" ");
             }
         }
         return buf.toString();
     }
     
     /**
      * 
      * @param id old ID value
      * @param idMap ID map
      * @param counter counter
      * @param pattern pattern to test
      */
     private void rewriteId(final String id, final Map<String, String> idMap, final AtomicInteger counter, final Pattern pattern) {
         final Matcher m = pattern.matcher(id);
         if (m.matches()) {
             if (!idMap.containsKey(id)) {
                 final int i = counter.addAndGet(1);
                 final StringBuilder buf = new StringBuilder("gen-id-").append(Integer.toString(i));
 //                if (m.groupCount() > 0) {
 //                    buf.append(m.group(1));
 //                }
                 idMap.put(id, buf.toString());
             }
         }
     }
     
     
     static class TestListener implements BuildListener {
         
         private final Pattern fatalPattern = Pattern.compile("\\[\\w+F\\]\\[FATAL\\]");
         private final Pattern errorPattern = Pattern.compile("\\[\\w+E\\]\\[ERROR\\]");
         private final Pattern warnPattern = Pattern.compile("\\[\\w+W\\]\\[WARN\\]");
         private final Pattern infoPattern = Pattern.compile("\\[\\w+I\\]\\[INFO\\]");
         private final Pattern debugPattern = Pattern.compile("\\[\\w+D\\]\\[DEBUG\\]");
         
         public final List<Message> messages = new ArrayList<Message>();
         final PrintStream out;
         final PrintStream err;
         
         public TestListener(final PrintStream out, final PrintStream err) {
             this.out = out;
             this.err = err;
         }
         
         //@Override
         public void buildStarted(BuildEvent event) {
             //System.out.println("build started: " + event.getMessage());
         }
 
         //@Override
         public void buildFinished(BuildEvent event) {
             //System.out.println("build finished: " + event.getMessage());
         }
 
         //@Override
         public void targetStarted(BuildEvent event) {
             //System.out.println(event.getTarget().getName() + ":");
         }
 
         //@Override
         public void targetFinished(BuildEvent event) {
             //System.out.println("target finished: " + event.getTarget().getName());
         }
 
         //@Override
         public void taskStarted(BuildEvent event) {
             //System.out.println("task started: " + event.getTask().getTaskName());
         }
 
         //@Override
         public void taskFinished(BuildEvent event) {
             //System.out.println("task finished: " + event.getTask().getTaskName());
         }
 
         //@Override
         public void messageLogged(BuildEvent event) {
             final String message = event.getMessage();
             int level;
             if (fatalPattern.matcher(message).find()) {
                 level = Project.MSG_ERR;
             } else if (errorPattern.matcher(message).find()) {
                 level = Project.MSG_ERR;
             } else if (warnPattern.matcher(message).find()) {
                 level = Project.MSG_WARN;
             } else if (infoPattern.matcher(message).find()) {
                 level = Project.MSG_INFO;
             } else if (debugPattern.matcher(message).find()) {
                 level = Project.MSG_DEBUG;
             } else {
                 level = event.getPriority();
             }
 
             switch (level) {
             case Project.MSG_DEBUG:
             case Project.MSG_VERBOSE:
                 break;
             case Project.MSG_INFO:
                 // out.println(event.getMessage());
                 break;
             default:
                 err.println(message);
             }
             
             messages.add(new Message(level, message));
         }
         
         static class Message {
             
             public final int level;
             public final String message;
             
             public Message(final int level, final String message) {
                 this.level = level;
                 this.message = message;
             }
             
         }
         
     }
     
 }
