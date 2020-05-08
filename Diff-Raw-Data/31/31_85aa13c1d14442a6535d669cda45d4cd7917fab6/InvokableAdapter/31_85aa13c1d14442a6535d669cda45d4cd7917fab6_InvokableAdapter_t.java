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
 
 import de.weltraumschaf.commons.system.DefaultExiter;
 import de.weltraumschaf.commons.system.ExitCode;
 import de.weltraumschaf.commons.system.Exitable;
 
 /**
  * Abstract base implementation of an command line invokable.
  *
  * Extend this class in your client code.
  *
  * Example:
  * <code>
  *
  * public class MyApp extends InvokableAdapter {
  *
  *      public static void main(final String[] args) {
  *          InvokableAdapter.main(new App(args));
  *      }
  *
  *      &#064;Override
  *      public void execute() throws Exception {
  *          // Your application code here.
  *      }
  *
  * }
  * </code>
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 public abstract class InvokableAdapter implements Invokable {
 
     /**
      * Copy of the command line arguments.
      */
     private final String[] args;
 
     /**
      * Container for shutdown {@link Runnable callbacks}.
      */
     private final ShutDownHook shutDownHooks = new ShutDownHook();
 
     /**
      * I/O streams.
      */
     private IOStreams ioStreams;
 
     /**
      * Abstraction for {@link System#exit(int)}.
      */
     private Exitable exiter = new DefaultExiter();
 
     /**
      * Copies the command line arguments.
      *
      * @param args Command line arguments.
      */
     public InvokableAdapter(final String[] args) {
         this.args = args.clone();
     }
 
     /**
      * Invokes {@link #main(de.weltraumschaf.commons.Invokable, de.weltraumschaf.commons.IOStreams)
      * invokable} with default I/O.
      *
      * @param invokanle Implementation to invoke.
      */
     public static void main(Invokable invokanle) {
         main(invokanle, IOStreams.newDefault());
     }
 
     /**
      * Inject the I/O streams to the invokable and then calls {@link Invokable#init()} and
      * then {@link Invokable#execute()}.
      *
      * This method handles ell thrown {@link Exception} and calls {@link System#exit(int)}.
      *
      * @param invokanle Implementation to invoke.
      * @param ioStreams I/O streams.
      */
     public static void main(final Invokable invokanle, final IOStreams ioStreams) {
         invokanle.setIoStreams(ioStreams);
 
         try {
             invokanle.init();
             invokanle.execute();
         } catch (Exception ex) {
            ioStreams.error(ex.getMessage());
             invokanle.exit(-1);
         }
 
         invokanle.exit(0);
     }
 
     /**
      * Adds shutdown hook to runtime.
      *
      * @throws Exception Various exceptions from the runtime.
      */
     @Override
     public final void init() throws Exception {
         Runtime.getRuntime().addShutdownHook(shutDownHooks);
     }
 
     /**
      * Get the command line arguments.
      *
      * @return Returns copy of the arguments array.
      */
     public String[] getArgs() {
         return args.clone();
     }
 
     @Override
     public IOStreams getIoStreams() {
         return ioStreams;
     }
 
     @Override
     public void setIoStreams(IOStreams ioStreams) {
         this.ioStreams = ioStreams;
     }
 
     /**
      * Get the shutdown hook object for registering callbacks executed on shutdown.
      *
      * @return Returns the shutdown object.
      */
     protected ShutDownHook getShutDownHooks() {
         return shutDownHooks;
     }
 
     @Override
     public void exit(int status) {
         exiter.exit(status);
     }
 
     @Override
     public void exit(ExitCode status) {
         exiter.exit(status);
     }
 
     @Override
     public void setExiter(final Exitable exiter) {
         this.exiter = exiter;
     }
 
 }
