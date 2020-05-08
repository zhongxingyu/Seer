 package org.jdryad.common;
 
 /**
  * A simple class that takes care of running an <code>Application</code>
  *
  * @author Balraja Subbiah
  * @version $Id:$
  */
 public class ApplicationExecutor
 {
     private final Application myApplication;
 
     private static class TerminatingThreadGroup extends ThreadGroup
     {
         /**
          * CTOR
          */
         public TerminatingThreadGroup(String name)
         {
             super(name);
         }
 
         /**
          * {@inheritDoc}
          */
         @Override
         public void uncaughtException(Thread t, Throwable e)
         {
             System.exit(0);
         }
     }
 
     /**
      * CTOR
      */
     public ApplicationExecutor(Application application)
     {
         myApplication = application;
     }
 
     /**
      * Starts the application.
      */
     public void run()
     {
         TerminatingThreadGroup tgp =
             new TerminatingThreadGroup(
                 myApplication.getClass().getSimpleName() + " App");
         Thread t = new Thread(tgp, new Runnable() {
             /**
              * {@inheritDoc}
              */
             @Override
             public void run()
             {
                 myApplication.start();
             }
         });
         Runtime.getRuntime().addShutdownHook(new Thread() {
             @Override
             public void run() { myApplication.stop(); }
         });
     }
 
 }
