 /*
  * author: Max DeLiso <maxdeliso@gmail.com>
  * purpose: graphical UDP chat program 
  */
 
 package com.megafrock.teflon;
 
 import java.nio.charset.Charset;
 
 import com.megafrock.teflon.handler.LocalHandler;
 import com.megafrock.teflon.handler.RemoteHandler;
 
 public class Teflon {
    /*
    * These are constants which are global to the entire application.
     */
    public static final int TEFLON_PORT = 1337;
    public static final byte[] TEFLON_RECV_ADDRESS = new byte[] { 0, 0, 0, 0 };
   public static final byte[] TEFLON_SEND_ADDRESS = new byte[] { (byte) 0xff, (byte) 0xff, (byte) 0xff, (byte) 0xff };
    public static final int IO_TIMEOUT_MS = 50;
    public static final int INPUT_BUFFER_LEN = 1024;
    public static final Charset UTF8_CHARSET = Charset.forName("UTF-8");
 
    /*
     * These are are application global variables. A reference to each thread and
     * a boolean maintaining whether or not the application is alive. These
     * references are maintained here so that each handler can easily and
     * elegantly obtain a reference to any other handlers, as well as being able
     * to signal application exit uniformly.
     */
    private RemoteHandler remoteHandler;
    private LocalHandler localHandler;
    private Thread remoteHandlerThread;
    private Thread localHandlerThread;
    private boolean alive;
 
    /*
     * This is an application global helper routine which is used to signal a
     * bug. I pass all unanticipated exceptions here which makes fixing bugs a
     * lot easier.
     */
    public static void reportException(Exception ex) {
       System.err.println("ERROR: " + Thread.currentThread() + " : " + ex);
       ex.printStackTrace();
       System.err.println("there was a fatal error. please report it. aborting.");
       System.exit(1);
    }
 
    /*
     * This is an application global helper routine which is used to report an
     * arbitrary string along with the thread which emanated it. It's very
     * convenient to have all debugging output statements pass through a
     * centralized gate so that they might be easily disabled in a release build.
     */
    public static void debugMessage(String message) {
       System.out.println("DEBUG: " + Thread.currentThread() + " : " + message);
    }
 
    /*
     * This is the application entry point. Note that the application's command
    * line arguments are currently ignored.
     */
    public static void main(String args[]) {
       new Teflon();
    }
 
    /*
     * This is a utility function to convert a string of bytes into a java
     * string. This is necessary because strings need to be converted to and from
     * a byte representation in order to pass over the network. The java virtual
     * machine takes care of abstractions at the transport layer, but you still
     * need to care about the character set.
     */
    public static String decodeUTF8(byte[] bytes) {
       return new String(bytes, UTF8_CHARSET);
    }
 
    /*
     * This function is a simple inverse of decodeUTF8.
     */
    public static byte[] encodeUTF8(String string) {
       return string.getBytes(UTF8_CHARSET);
    }
 
    /*
     * This function returns a reference to the remote handler, which is useful
     * when another child of Teflon wants to be able to communicate with the
     * remote handler, for instance the local handler.
     */
    public RemoteHandler remote() {
       return remoteHandler;
    }
 
    /*
     * This function returns a reference to the local handler, it is useful for
     * the same reasons the remote() function is useful.
     */
    public LocalHandler local() {
       return localHandler;
    }
 
    /*
     * This function returns a boolean which the caller interprets to mean that
     * it should keep executing. Note that this is a synchronized method which
     * means that all invocations of this method on the same instance of this
     * class will block each other. It is used by the handler threads to check
     * that they should continue executing.
     */
    public synchronized boolean alive() {
       return alive;
    }
 
    /*
     * This function sets the alive flag to false, effectively signaling to all
     * the parts of the application that they should initiate an orderly
     * shutdown. It is used by the handlers when they wish to signal termination.
     */
    public synchronized void kill() {
       alive = false;
       remoteHandlerThread.interrupt();
       localHandlerThread.interrupt();
    }
 
    /*
     * This is the constructor of the application class. It creates instances of
     * the local and remote handlers and passes them an instance of itself, so
     * that they may easily acquire references to each other, and any other
     * handlers that may be introduced in the future. Note that the handlers all
     * implement Runnable and are started in their own threads, which means that
    * they execute in parallel.
     */
    public Teflon() {
       alive = true;
       remoteHandler = new RemoteHandler(this);
       localHandler = new LocalHandler(this);
       remoteHandlerThread = new Thread(remoteHandler, "UDP I/O Thread");
       localHandlerThread = new Thread(localHandler, "UI Helper Thread");
 
       remoteHandlerThread.start();
       localHandlerThread.start();
 
       try {
          remoteHandlerThread.join();
          localHandlerThread.join();
       } catch (InterruptedException ie) {
          reportException(ie);
       }
 
       debugMessage("main thread exiting");
    }
 }
