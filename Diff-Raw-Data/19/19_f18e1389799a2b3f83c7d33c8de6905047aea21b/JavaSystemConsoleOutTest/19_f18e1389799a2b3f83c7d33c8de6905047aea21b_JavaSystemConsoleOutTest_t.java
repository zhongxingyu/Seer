 package com.danielwellman.jschallenge.java1.integration;
 
 import com.danielwellman.jschallenge.java1.javaio.JavaSystemConsoleOut;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.io.ByteArrayOutputStream;
 import java.io.PrintStream;
 
 import static org.hamcrest.Matchers.containsString;
 import static org.hamcrest.Matchers.endsWith;
 import static org.junit.Assert.assertThat;
 
 public class JavaSystemConsoleOutTest {
    private static final String NEWLINE = "\n";
     private final ByteArrayOutputStream capturingOut = new ByteArrayOutputStream();
 
     private PrintStream originalSystemOut;
     private final JavaSystemConsoleOut out = new JavaSystemConsoleOut();
 
     @Before
     public void captureSystemOut() {
         originalSystemOut = System.out;
         System.setOut(new PrintStream(capturingOut));
     }
 
     @After
     public void restoreSystemOut() {
         System.setOut(originalSystemOut);
     }
 
     @Test
     public void printsToSystemOut() {
         final String message = "Hello!";
         out.println(message);
         assertThat(capturingOut.toString(), containsString(message));
     }
 
     @Test
     public void appendsANewline() {
         out.println("irrelevant");
 
         assertThat(capturingOut.toString(), endsWith(NEWLINE));
     }
 }
