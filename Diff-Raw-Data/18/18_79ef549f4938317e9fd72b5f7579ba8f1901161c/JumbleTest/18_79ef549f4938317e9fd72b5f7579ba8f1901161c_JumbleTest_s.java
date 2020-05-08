 package jumble;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.StringTokenizer;
 
 import jumble.util.JavaRunner;
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 
 /**
  * Tests the corresonding class.
  * 
  * @author Tin Pavlinic
  * @version $Revision 1.0 $
  */
 public class JumbleTest extends TestCase {
   public void testBroken() throws Exception {
     assertEquals(getExpectedOutput("experiments.Broken"), runCommandLineJumble("experiments.Broken"));
   }
 
   public void testInterface() throws Exception {
     assertEquals(getExpectedOutput("experiments.Interface"), runCommandLineJumble("experiments.Interface"));
   }
 
   public void testJumblerExperiment() throws Exception {
     // Have to allow a some room for the unit test time limit to vary
     String expected = getExpectedOutput("experiments.JumblerExperiment");
     String got = runCommandLineJumble("experiments.JumblerExperiment");
     StringTokenizer tokens1 = new StringTokenizer(expected, "\n");
     StringTokenizer tokens2 = new StringTokenizer(got, "\n");
 
     assertEquals(tokens1.countTokens(), tokens2.countTokens());
 
     assertEquals(tokens1.nextToken(), tokens2.nextToken());
     assertEquals(tokens1.nextToken(), tokens2.nextToken());
     // Skip next line, as it contains timing information
     tokens1.nextToken();
     tokens2.nextToken();
 
     assertEquals(tokens1.nextToken(), tokens2.nextToken());
     assertEquals(tokens1.nextToken(), tokens2.nextToken());
     assertEquals(tokens1.nextToken(), tokens2.nextToken());
   }
 
   public void testNoDebug() throws Exception {
    assertEquals(getExpectedOutput("jumble.NoDebug"), runCommandLineJumble("DebugNone", "experiments.JumblerExperimentTest"));
   }
 
   public void testNoTestClass() throws Exception {
     assertEquals(getExpectedOutput("experiments.NoTestClass"), runCommandLineJumble("experiments.NoTestClass"));
   }
 
   public static Test suite() {
     return new TestSuite(JumbleTest.class);
   }
 
   public static void main(String[] args) {
     junit.textui.TestRunner.run(suite());
   }
 
   private String getExpectedOutput(String className) throws Exception {
     String location = "jumble/" + className.substring(className.lastIndexOf('.') + 1) + ".txt";
     // System.err.println(location);
     return readAll(getClass().getClassLoader().getResourceAsStream(location));
   }
 
   private String runCommandLineJumble(String className) throws Exception {
     JavaRunner runner = new JavaRunner("jumble.Jumble", new String[] {className });
     Process p = runner.start();
 
     return readAll(p.getInputStream());
   }
 
   private String runCommandLineJumble(String className, String testName) throws Exception {
     JavaRunner runner = new JavaRunner("jumble.Jumble", new String[] {className, testName });
     Process p = runner.start();
 
     return readAll(p.getInputStream());
   }
 
   private String readAll(InputStream is) throws Exception {
     StringBuffer buf = new StringBuffer();
     BufferedReader in = new BufferedReader(new InputStreamReader(is));
     String temp;
 
     while ((temp = in.readLine()) != null) {
       buf.append(temp + "\n");
     }
     return buf.toString();
   }
 }
