 /*
  * LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 42):
  * "Sven Strittmatter" <weltraumschaf@googlemail.com> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a beer in return.
  *
  */
 
 package de.weltraumschaf.commons;
 
 import java.io.InputStream;
 import java.io.PrintStream;
 import static org.junit.Assert.*;
 import org.junit.Test;
 import static org.mockito.Mockito.*;
 
 /**
  * Test cases for {@link IOStreams}.
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 public class IOStreamsTest {
 
     private final IOStreams defaultStreams = IOStreams.newDefault();
     private final IOStreams mockedStreams = new IOStreams(
             mock(InputStream.class), mock(PrintStream.class), mock(PrintStream.class));
 
     @Test public void testGetStderrOnDefault() {
         assertSame(System.err, defaultStreams.getStderr());
     }
 
     @Test public void testGetStdinOnDefault() {
         assertSame(System.in, defaultStreams.getStdin());
     }
 
     @Test public void testGetStdoutOnDefault() {
         assertSame(System.out, defaultStreams.getStdout());
     }
 
     @Test public void println() {
         final String msg = "some text";
         mockedStreams.println(msg);
         verify(mockedStreams.getStdout(), times(1)).println(msg);
     }
 
     @Test public void printlnErr() {
         final String msg = "some text";
        mockedStreams.error(msg);
         verify(mockedStreams.getStderr(), times(1)).println(msg);
     }
 
     @Test public void printStackTraceToStdErr() {
         final PrintStream err = mock(PrintStream.class);
         final Exception ex = mock(Exception.class);
         final IOStreams streams = new IOStreams(mock(InputStream.class), mock(PrintStream.class), err);
 
         streams.printStackTraceToStdErr(ex);
         verify(ex, times(1)).printStackTrace(err);
     }
 
 }
