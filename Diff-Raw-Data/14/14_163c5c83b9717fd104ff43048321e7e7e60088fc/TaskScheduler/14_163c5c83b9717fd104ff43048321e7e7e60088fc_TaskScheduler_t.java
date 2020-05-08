 /*
  * Copyright © 2012 jbundle.org. All rights reserved.
  */
 package org.jbundle.thin.base.thread;
 
 /**
  * @(#)TaskScheduler.java 1.16 95/12/14 Don Corley
  *
  * Copyright © 2012 tourapp.com. All Rights Reserved.
  *      don@tourgeek.com
  *
  */
 import java.applet.Applet;
 import java.util.Hashtable;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Vector;
 
 import org.jbundle.model.App;
 import org.jbundle.model.Freeable;
 import org.jbundle.model.Task;
 import org.jbundle.model.util.Param;
 import org.jbundle.model.util.Util;
 import org.jbundle.util.osgi.finder.ClassServiceUtility;
 
 /**
  * Schedules jobs to run in the background (under different tasks).
  */
 public class TaskScheduler extends Object
     implements Runnable, Freeable
 {
     /**
      * The task's Parent application.
      */
     protected App m_application = null;
     /**
      * FIFO list of jobs to run.
      */
     static private Vector<Object> gvJobs = new Vector<Object>();
     /**
      * Maximum threads.
      */
     static private int giMaxThreads = 10;
     /**
      * Currently running threads.
      */
     static private int giCurrentThreads = 0;
 
     /**
      * Constructor.
      */
     public TaskScheduler()
     {
         super();
     }
     /**
      * Constructor.
      * @param application The parent application.
      * @param iMaxThreads The maximum number of threads to run (-1 = default).
      */
     public TaskScheduler(App application, int iMaxThreads)
     {
         this();
         this.init(application, iMaxThreads);
     }
     /**
      * Constructor.
      * @param application The parent application.
      * @param iMaxThreads The maximum number of threads to run (-1 = default).
      */
     public void init(App application, int iMaxThreads)
     {
         m_application = application;
         if (iMaxThreads > 0)
             giMaxThreads = iMaxThreads;     // No need to sync, because you only call this with a non zero value once
     }
     /**
      * Stop using this TaskScheduler.
      */
     public void free()
     {
         // todo(don) Stop further job adds, wait for all to completer, remove myself from App.
     }
     /**
      * Set the max threads that can run at a time (usually only set once, or take default).
      * @param iMaxThreads The maximum number of threads to run (-1 = default).
      */
     public synchronized void setMaxThreads(int iMaxThreads)
     {
         giMaxThreads = iMaxThreads;
     }
     /**
      * Add a job to the queue.
      * @param objJobDef Can be a AutoTask object or a string describing the job to run (in the overriding class).
      */
     public void addTask(Object objJobDef)
     {
         synchronized (this)
         {
             gvJobs.addElement(objJobDef); // Vector is thread safe.
             if (giCurrentThreads >= giMaxThreads)
                 return;             // The max threads are running... let the scheduler process them in order
             giCurrentThreads++;     // I'm going to start another thread
         }
         TaskScheduler js = this.makeNewTaskScheduler(0);
         // This should be done more efficiently... having the threads waiting in a pool to be awakened when needed.
         Thread thread = new Thread(js, "TaskSchedulerThread");
         thread.start();
     }
     /**
      * Run this thread (start another task scheduler thread).
      */
     public void run()
     {
         Object objJobDef = null;
         while ((objJobDef = this.getNextJob()) != null)     // Get and remove next job
         { // Until end of jobs
             this.runThread(objJobDef);
         }
         synchronized (this)
         {
             giCurrentThreads--;     // This thread is going to die, decrement the thread count.
         }
     }
     /**
      * Start this task running in this thread.
      * @param objJobDef The job to run in this thread.
      */
    @SuppressWarnings({ "rawtypes", "unchecked" })
     public void runThread(Object objJobDef)
     {
         if (objJobDef instanceof Task)
         {
             Task task = (Task)objJobDef;
             if (task.getApplication() == null)  // init()ed yet?
                 task.initTask(this.getApplication(), null);     // No, Initialize it
             task.run();
         }
         else if ((objJobDef instanceof String) || (objJobDef instanceof Properties))
         {
             String strJobDef = null;
             Map<String,Object> properties = null;
             if (objJobDef instanceof String)
             {
                 strJobDef = (String)objJobDef;
                 properties = new Hashtable<String,Object>();
                 Util.parseArgs(properties, strJobDef);
             }
             if (objJobDef instanceof Properties)
                 properties = (Map)objJobDef;
             String strClass = (String)properties.get(Param.TASK);
             if (strClass == null)
                 strClass = (String)properties.get(Param.APPLET); // Applets are also run as tasks
             Object job = ClassServiceUtility.getClassService().makeObjectFromClassName(strClass);
             if (job instanceof Task)
             {
                 ((Task)job).initTask(this.getApplication(), properties);
                 ((Task)job).run();
             }
             else if (job instanceof Applet)
             { // An applet
                 //new JBaseFrame("Applet", (Applet)job);
             	System.out.println("********** Applet task type needs to be fixed *********");
             }
             else if (job instanceof Runnable)
             { // A thread (Is this okay?? I already have my own thread)
 //x                         new Thread((Runnable)job).start();  // No, don't start another thread
                 ((Runnable)job).run();  // This should be okay, as I already have my own thread
             }
             else
                Util.getLogger().warning("Illegal job type passed to TaskScheduler: " + job);
         }
         else if (objJobDef instanceof Runnable)
             ((Runnable)objJobDef).run();
         else
             Util.getLogger().warning("Error: Illegal job type");
     }
     /**
      * Pull the next job off the queue and remove it.
      * @return The next job.
      */
     public synchronized Object getNextJob()
     {
         Object strJobDef = null;
         synchronized (this)
         {
             if (gvJobs.size() > 0)
                 strJobDef = gvJobs.remove(0);
         }
         return strJobDef;
     }
     /**
      * Make a new job scheduler.
      * NOTE: Override this to make a new instanceof the same class!!!
      * @param iMaxTasks The maximum tasks to run at once.
      */
     public TaskScheduler makeNewTaskScheduler(int iMaxTasks)
     {
         return new TaskScheduler(m_application, iMaxTasks);   // Start using default number of threads
     }
     /**
      * Return the number of running threads.
      * @return The thread count.
      */
     public int getThreadCount()
     {
         return giCurrentThreads;
     }
     /**
      * Get the application object.
      * @return The application object.
      */
     public App getApplication()
     {
         return m_application;
     }
     /**
      * Start a task that calls the syncNotify 'done' method when the screen is done displaying.
      * This class is a platform-neutral implementation of SwinSyncPageWorker that
      * guarantees a page has displayed before doing a compute-intensive task.
      */
     public static SyncWorker startPageWorker(SyncPage syncPage, SyncNotify syncNotify, Runnable swingPageLoader, Map<String,Object> map, boolean bManageCursor)
     {
         SyncWorker syncWorker = (SyncWorker)ClassServiceUtility.getClassService().makeObjectFromClassName(JAVA_WORKER);
         if (syncWorker == null)
             syncWorker = (SyncWorker)ClassServiceUtility.getClassService().makeObjectFromClassName(ANDROID_WORKER);
         if (syncWorker != null)
         {
             syncWorker.init(syncPage, syncNotify, swingPageLoader, map, bManageCursor);
             syncWorker.start();
         }
         else
             Util.getLogger().severe("SyncWorker does not exist!");
         return syncWorker;
     }
     public static final String JAVA_WORKER = "org.jbundle.thin.base.screen.print.thread.SwingSyncPageWorker";
     public static final String ANDROID_WORKER = "org.jbundle.thin.base.screen.print.thread.SwingSyncPageWorker";
 }
