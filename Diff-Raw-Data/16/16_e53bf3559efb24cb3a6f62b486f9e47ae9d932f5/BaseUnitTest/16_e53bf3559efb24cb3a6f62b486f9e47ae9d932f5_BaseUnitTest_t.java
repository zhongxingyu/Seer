 package org.jboss.pressgang.ccms.contentspec.client;
 
 import java.io.ByteArrayInputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 import java.io.PrintStream;
 import java.io.UnsupportedEncodingException;
 
 import net.sf.ipsedixit.integration.junit.JUnit4IpsedixitTestRunner;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.runner.RunWith;
 import org.mockito.MockitoAnnotations;
 
 /**
  * @author kamiller@redhat.com (Katie Miller)
  */
 @RunWith(JUnit4IpsedixitTestRunner.class)
 @Ignore // We don't expect any tests on this class
 public class BaseUnitTest {
     protected static final String SYSTEM_EXIT_ERROR = "Program did not call System.exit()";
     private final ByteArrayOutputStream stdOutCapture = new ByteArrayOutputStream();
     private final ByteArrayOutputStream stdErrCapture = new ByteArrayOutputStream();
    private PrintStream stdOut;
    private PrintStream stdErr;
    private InputStream stdIn;
 
     @Before
     public void initMocks() {
         MockitoAnnotations.initMocks(this);
        stdOut = System.out;
        stdErr = System.err;
        stdIn = System.in;
     }
 
     @After
     public void resetStdStreams() {
         System.setIn(stdIn);
         System.setOut(stdOut);
         System.setErr(stdErr);
     }
 
     protected void bindStdOutAndErr() {
         bindStdErr();
         bindStdOut();
     }
 
     protected void bindStdOut() {
         try {
             System.setOut(new PrintStream(stdOutCapture, false, "UTF-8"));
         } catch (UnsupportedEncodingException e) {
             throw new RuntimeException(e);
         }
     }
 
     protected void bindStdErr() {
         try {
             System.setErr(new PrintStream(stdErrCapture, false, "UTF-8"));
         } catch (UnsupportedEncodingException e) {
             throw new RuntimeException(e);
         }
     }
 
     protected void setStdInput(String input) {
         try {
             System.setIn(new ByteArrayInputStream(input.getBytes("UTF-8")));
         } catch (UnsupportedEncodingException e) {
             throw new RuntimeException(e);
         }
     }
 
     protected String getStdOutLogs() {
         try {
             return stdOutCapture.toString("UTF-8");
         } catch (UnsupportedEncodingException e) {
             throw new RuntimeException(e);
         }
     }
 
     protected String getStdErrLogs() {
         try {
             return stdErrCapture.toString("UTF-8");
         } catch (UnsupportedEncodingException e) {
             throw new RuntimeException(e);
         }
     }
 }
