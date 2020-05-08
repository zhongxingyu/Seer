 package jumble;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.Arrays;
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
     assertEquals(getExpectedOutput("experiments.Broken"), runCommandLineJumble("experiments.Broken", -1));
   }
 
   public void testInterface() throws Exception {
     assertEquals(getExpectedOutput("experiments.Interface"), runCommandLineJumble("experiments.Interface", -1));
   }
 
   public void testJumblerExperiment() throws Exception {
     // Have to allow a some room for the unit test time limit to vary
     String expected = getExpectedOutput("experiments.JumblerExperiment");
     String got = runCommandLineJumble("experiments.JumblerExperiment", -1);
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
     String expected = getExpectedOutput("jumble.NoDebug");
     String got = runCommandLineJumble("DebugNone", "experiments.JumblerExperimentTest");
 
     StringTokenizer tokens1 = new StringTokenizer(expected, "\r\n");
     StringTokenizer tokens2 = new StringTokenizer(got, "\r\n");
 
     assertEquals(tokens1.countTokens(), tokens2.countTokens());
 
     assertEquals(tokens1.nextToken(), tokens2.nextToken());
     assertEquals(tokens1.nextToken(), tokens2.nextToken());
     tokens1.nextToken();
     tokens2.nextToken();
 
     assertEquals(tokens1.nextToken(), tokens2.nextToken());
     assertEquals(tokens1.nextToken(), tokens2.nextToken());
     assertEquals(tokens1.nextToken(), tokens2.nextToken());
     assertEquals(tokens1.nextToken(), tokens2.nextToken());
   }
 
   public void testNoTestClass() throws Exception {
     assertEquals(getExpectedOutput("experiments.NoTestClass"), runCommandLineJumble("experiments.NoTestClass", -1));
   }
 
   public void testLength1() throws Exception {
     // Have to allow a some room for the unit test time limit to vary
     String expected = getExpectedOutput("experiments.JumblerExperiment");
     String got = runCommandLineJumble("experiments.JumblerExperiment", 1);
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
   
   public void testLength2() throws Exception {
     // Have to allow a some room for the unit test time limit to vary
     String expected = getExpectedOutput("experiments.JumblerExperiment");
     String got = runCommandLineJumble("experiments.JumblerExperiment", 2);
    assertEquals(expected, got);
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
 
   private String runCommandLineJumble(String className, int max) throws Exception {
     String[] args = max < 0 ? new String[] {className } : new String[] {className, "-m", "" + max};
 
     JavaRunner runner = new JavaRunner("jumble.Jumble", args);
     Process p = runner.start();
 
     return readAll(p.getInputStream());
   }
 
   private String runCommandLineJumble(String className, String testName) throws Exception {
     JavaRunner runner = new JavaRunner("jumble.Jumble", new String[] {className, testName });
     Process p = runner.start();
 
     return readAll(p.getInputStream());
   }
 
   public static String readAll(InputStream is) throws Exception {
     StringBuffer buf = new StringBuffer();
     BufferedReader in = new BufferedReader(new InputStreamReader(is));
     String temp;
 
     while ((temp = in.readLine()) != null) {
       buf.append(temp + "\n");
     }
     in.close();
     return buf.toString();
   }
 }
