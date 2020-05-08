 package com.silveregg.wrapper;
 
 /*
  * Copyright (c) 2001 Silver Egg Technology
  *
  * Permission is hereby granted, free of charge, to any person
  * obtaining a copy of this software and associated documentation
  * files (the "Software"), to deal in the Software without 
  * restriction, including without limitation the rights to use, 
  * copy, modify, merge, publish, distribute, sublicense, and/or 
  * sell copies of the Software, and to permit persons to whom the
  * Software is furnished to do so, subject to the following 
  * conditions:
  *
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, 
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES 
  * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND 
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT 
  * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, 
  * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING 
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
  * OTHER DEALINGS IN THE SOFTWARE.
  */
 
 // $Log$
 // Revision 1.3  2001/12/07 06:52:06  mortenson
 // Fix a problem just added with the synchronization of the startup process.
 //
 // Revision 1.2  2001/12/06 10:39:12  mortenson
 // The WrapperSimpleApp method of launchine applications was not
 // working correctly with applications whose main method did not return.
 //
 // Revision 1.1.1.1  2001/11/07 08:54:20  mortenson
 // no message
 //
 
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 
 public class WrapperSimpleApp implements WrapperListener, Runnable {
     /**
      * Application's main method
      */
     private Method _mainMethod;
     
     /**
      * Command line arguments to be passed on to the application
      */
     private String[] _appArgs;
     
     /**
      * Gets set to true when the thread used to launch the application completes.
      */
     private boolean _mainComplete;
     
     /**
      * Exit code to be returned if the application fails to start.
      */
     private Integer _mainExitCode;
     
     /**
      * Flag used to signify that the start method is done waiting for the application to start.
      */
     private boolean _waitTimedOut;
     
     /*---------------------------------------------------------------
      * Constructors
      *-------------------------------------------------------------*/
     private WrapperSimpleApp(Method mainMethod) {
         _mainMethod = mainMethod;
     }
     
     /*---------------------------------------------------------------
      * Runnable Methods
      *-------------------------------------------------------------*/
     /**
      * Used to launch the application in a separate thread.
      */
     public void run() {
         Throwable t = null;
         try {
             if (WrapperManager.isDebugEnabled()) {
                 System.out.println("WrapperSimpleApp: invoking main method");
             }
             _mainMethod.invoke(null, new Object[] {_appArgs});
             if (WrapperManager.isDebugEnabled()) {
                 System.out.println("WrapperSimpleApp: main method completed");
             }
             
             // If we get here, then the application completed normally and we should shut down
             //  if there are no daemon threads running.
             Thread[] threads = new Thread[Thread.activeCount() * 2];
             Thread.enumerate(threads);
             
             // Only shutdown if there are any non daemon threads which are 
             //  still alive other than this thread.
             int liveCount = 0;
             for (int i = 0; i < threads.length; i++) {
                 /*
                 if (threads[i] != null) {
                     System.out.println("Check " + threads[i].getName() + " daemon=" + 
                         threads[i].isDaemon() + " alive=" + threads[i].isAlive());
                 }
                 */
                 if ((threads[i] != null) && (threads[i].isAlive() && (!threads[i].isDaemon()))) {
                     // Do not count this thread or the wrapper connection thread
                     if ((Thread.currentThread() != threads[i]) && 
                         (!WrapperManager.WRAPPER_CONNECTION_THREAD_NAME.equals(threads[i].getName()))) {
                         
                         // Non-Daemon livine thread
                         liveCount++;
                         //System.out.println("  -> Non-Daemon");
                     }
                 }
             }
             
             synchronized(this) {
                 // Let the start() method know that the main method returned, in case it is 
                 //  still waiting.
                 _mainComplete = true;
                 this.notifyAll();
             }
             
             // There will always be one non-daemon thread alive.  This thread is either the main
             //  thread which has not yet completed, or a thread launched by java when the main
             //  thread completes whose job is to wait around for all other non-daemon threads to
             //  complete.  We are overriding that thread here.
             if (liveCount <= 1) {
                 // Exit normally
                 WrapperManager.stop(0);
                 // Will not get here.
             } else {
                 // There are daemons running, let the JVM continue to run.
             }
             return;
         } catch (IllegalAccessException e) {
             t = e;
         } catch (IllegalArgumentException e) {
             t = e;
         } catch (InvocationTargetException e) {
             t = e;
         }
         
        // If we get here, then an error was thrown.  If this happened quickly enough, the
        //  start method should be allowed to shut things down.
         System.out.println("Encountered an error running main: " + t);
         showUsage();
         synchronized(this) {
             if (_waitTimedOut) {
                 // Shut down here.
                 WrapperManager.stop(1);
                 return; // Will not get here.
             } else {
                 // Let start method handle shutdown.
                 _mainComplete = true;
                 _mainExitCode = new Integer(1);
                 this.notifyAll();
                 return;
             }
         }
     }
     
     /*---------------------------------------------------------------
      * WrapperListener Methods
      *-------------------------------------------------------------*/
     /**
      * The start method is called when the WrapperManager is signalled by the 
      *	native wrapper code that it can start its application.  This
      *	method call is expected to return, so a new thread should be launched
      *	if necessary.
      * If there are any problems, then an Integer should be returned, set to
      *	the desired exit code.  If the application should continue,
      *	return null.
      */
     public Integer start(String[] args) {
         if (WrapperManager.isDebugEnabled()) {
             System.out.println("WrapperSimpleApp: start(args)");
         }
         
         Thread mainThread = new Thread(this, "WrapperSimpleAppMain");
         synchronized(this) {
             _appArgs = args;
             mainThread.start();
             // Wait for two seconds to give the application a chance to have failed.
             try {
                 this.wait(2000);
             } catch (InterruptedException e) {
             }
             _waitTimedOut = true;
             
             if (WrapperManager.isDebugEnabled()) {
                 System.out.println("WrapperSimpleApp: start(args) end.  Main Completed=" + 
                     _mainComplete + ", exitCode=" + _mainExitCode);
             }
             return _mainExitCode;
         }
     }
     
     /**
      * Called when the application is shutting down.
      */
     public int stop(int exitCode) {
         if (WrapperManager.isDebugEnabled()) {
             System.out.println("WrapperSimpleApp: stop(" + exitCode + ")");
         }
         
         // Normally an application will be asked to shutdown here.  Standard Java applications do
         //  not have shutdown hooks, so do nothing here.  It will be as if the user hit CTRL-C to
         //  kill the application.
         return exitCode;
     }
     
     /**
      * Called whenever the native wrapper code traps a system control signal
      *  against the Java process.  It is up to the callback to take any actions
      *  necessary.  Possible values are: WrapperManager.WRAPPER_CTRL_C_EVENT, 
      *    WRAPPER_CTRL_CLOSE_EVENT, WRAPPER_CTRL_LOGOFF_EVENT, or 
      *    WRAPPER_CTRL_SHUTDOWN_EVENT
      */
     public void controlEvent(int event) {
         if (WrapperManager.isControlledByNativeWrapper()) {
             if (WrapperManager.isDebugEnabled()) {
                 System.out.println("WrapperSimpleApp: controlEvent(" + event + ") Ignored");
             }
             // Ignore the event as the native wrapper will handle it.
         } else {
             if (WrapperManager.isDebugEnabled()) {
                 System.out.println("WrapperSimpleApp: controlEvent(" + event + ") Stopping");
             }
             
             // Not being run under a wrapper, so this isn't an NT service and should always exit.
             //  Handle the event here.
             WrapperManager.stop(0);
             // Will not get here.
         }
     }
     
     /*---------------------------------------------------------------
      * Methods
      *-------------------------------------------------------------*/
     /**
      * Displays application usage
      */
     private static void showUsage() {
         System.out.println();
         System.out.println("WrapperSimpleApp Usage:");
         System.out.println("  java com.silveregg.wrapper.WrapperSimpleApp {app_class} [app_parameters]");
         System.out.println();
         System.out.println("Where:");
         System.out.println("  app_class:      The fully qualified class name of the application to run.");
         System.out.println("  app_parameters: The parameters that would normally be passed to the");
         System.out.println("                  application.");
     }
     
     /*---------------------------------------------------------------
      * Main Method
      *-------------------------------------------------------------*/
     /**
      * Used to Wrapper enable a standard Java application.  This main
      *  expects the first argument to be the class name of the application
      *  to launch.  All remaining arguments will be wrapped into a new
      *  argument list and passed to the main method of the specified
      *  application.
      */
     public static void main(String args[]) {
         // Get the class name of the application
         if (args.length < 1) {
             showUsage();
             WrapperManager.stop(1);
             return;  // Will not get here
         }
         
         // Look for the specified class by name
         Class mainClass;
         try {
             mainClass = Class.forName(args[0]);
         } catch (ClassNotFoundException e) {
             System.out.println("WrapperSimpleApp: Unable to locate the class " + args[0] + ": " + e);
             showUsage();
             WrapperManager.stop(1);
             return;  // Will not get here
         } catch (LinkageError e) {
             System.out.println("WrapperSimpleApp: Unable to locate the class " + args[0] + ": " + e);
             showUsage();
             WrapperManager.stop(1);
             return;  // Will not get here
         }
         
         // Look for the main method
         Method mainMethod;
         try {
             mainMethod = mainClass.getDeclaredMethod("main", new Class[] {String[].class});
         } catch (NoSuchMethodException e) {
             System.out.println("WrapperSimpleApp: Unable to locate a static main method in class " + args[0] + ": " + e);
             showUsage();
             WrapperManager.stop(1);
             return;  // Will not get here
         } catch (SecurityException e) {
             System.out.println("WrapperSimpleApp: Unable to locate a static main method in class " + args[0] + ": " + e);
             showUsage();
             WrapperManager.stop(1);
             return;  // Will not get here
         }
         
         // Make sure that the method is public and static
         int modifiers = mainMethod.getModifiers();
         if (!(Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers))) {
             System.out.println("WrapperSimpleApp: The main method in class " + args[0] + " must be declared public and static.");
             showUsage();
             WrapperManager.stop(1);
             return;  // Will not get here
         }
         
         // Build the application args array
         String[] appArgs = new String[args.length - 1];
         System.arraycopy(args, 1, appArgs, 0, appArgs.length);
         
         // Create the WrapperSimpleApp
         WrapperSimpleApp app = new WrapperSimpleApp(mainMethod);
         
         // Start the application.  If the JVM was launched from the native
         //  Wrapper then the application will wait for the native Wrapper to
         //  call the application's start method.  Otherwise the start method
         //  will be called immediately.
         WrapperManager.start(app, appArgs);
         
         // This thread ends, the WrapperManager will start the application after the Wrapper has
         //  been propperly initialized by calling the start method above.
     }
 }
 
