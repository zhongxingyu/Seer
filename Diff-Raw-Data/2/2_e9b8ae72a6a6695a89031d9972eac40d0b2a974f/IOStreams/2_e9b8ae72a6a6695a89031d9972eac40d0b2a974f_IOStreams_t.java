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
 
 /**
  * Immutable aggregate object which contains STDIN, STDOUT and STDERR streams.
  *
  * It is not good practice to clutter production code with calls to
  * {@link System#out}, {@link System#err}, and {@link System#in}. But on the
  * other hand most applications must do I/O to the user. This aggregate object
  * contains I/O streams to pass around as injected dependency. It is onluy
  * necessary to the systems IO only at the main applications entry point:
  *
  * <code>
  * public void main(final String[] args) {
  *      IOStreams io = new IOStreams(System.in, System.out, System.err);
  *
  *      // Pass around the io object to your client code.
  *
  *      System.exit(0);
  * }
  * </code>
  *
  * In the test code it will be easy to replace the I/O with mocks:
  *
  * <code>
 * &#064;Test public void someTestWithIO() {
  *      IOStreams io = new IOStreams(mock(InputStream.class),
  *                                   mock(PrintStream.class),
  *                                   mock(PrintStream.class));
  *
  *      // Pass around the io object to your client code and assert something.
  * }
  * </code>
  *
  * As a convenience method for creating an I/O streams object with the default I/O streams
  * of {@link System} you can use {@link IOStreams#newDefault()}.
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 public final class IOStreams {
 
     /**
      * Standard input stream.
      */
     private final InputStream stdin;
 
     /**
      * Standard output stream.
      */
     private final PrintStream stdout;
 
     /**
      * Standard error stream.
      */
     private final PrintStream stderr;
 
     /**
      * Initializes the streams.
      *
      * @param stdin Input stream.
      * @param stdout Output stream.
      * @param stderr Error stream.
      */
     public IOStreams(final InputStream stdin, final PrintStream stdout, final PrintStream stderr) {
         this.stdin  = stdin;
         this.stdout = stdout;
         this.stderr = stderr;
     }
 
     /**
      * Get standard error output stream.
      *
      * @return Print stream object.
      */
     public PrintStream getStderr() {
         return stderr;
     }
 
     /**
      * Get standard input stream.
      *
      * @return Input stream object.
      */
     public InputStream getStdin() {
         return stdin;
     }
 
     /**
      * Get standard output stream.
      *
      * @return Print stream object.
      */
     public PrintStream getStdout() {
         return stdout;
     }
 
     /**
      * Creates a new streams object initialized with {@link System#in}, {@link System#out}, and {@link System#err}.
      *
      * @return Return always new instance.
      */
     public static IOStreams newDefault() {
         return new IOStreams(System.in, System.out, System.err);
     }
 
     /**
      * Prints exception stack trace to {@link System#err}.
      *
      * @param ex Exception to print.
      */
     public void printStackTraceToStdErr(Exception ex) {
         ex.printStackTrace(getStderr());
     }
 
     /**
      * Prints line to STDERR.
      *
      * @param str String to print.
      */
     public void printlnErr(final String str) {
         getStderr().println(str);
     }
 
     /**
      * Prints line to STDOUT.
      *
      * @param str String to print.
      */
     public void println(final String str) {
         getStdout().println(str);
     }
 
 }
