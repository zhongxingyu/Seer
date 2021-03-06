 /*
  * Copyright (c) 2002-2009 Gargoyle Software Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.gargoylesoftware.htmlunit.javascript.background;
 
 import static java.lang.Thread.currentThread;
 import static java.util.concurrent.TimeUnit.MILLISECONDS;
 
 import java.lang.ref.WeakReference;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.concurrent.Future;
 import java.util.concurrent.ScheduledThreadPoolExecutor;
 import java.util.concurrent.ThreadFactory;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.mozilla.javascript.Function;
 
 import com.gargoylesoftware.htmlunit.Page;
 import com.gargoylesoftware.htmlunit.WebWindow;
 import com.gargoylesoftware.htmlunit.javascript.host.Window;
 
 /**
  * <p><span style="color:red">INTERNAL API - SUBJECT TO CHANGE AT ANY TIME - USE AT YOUR OWN RISK.</span></p>
  *
  * <p>Default implementation of {@link JavaScriptJobManager}.</p>
  *
  * <p>This job manager class is guaranteed not to keep old windows in memory (no window memory leaks).</p>
  *
  * @version $Revision$
  * @author Daniel Gredler
  * @see MemoryLeakTest
  */
 public class JavaScriptJobManagerImpl implements JavaScriptJobManager {
 
     /** The window to which this job manager belongs (weakly referenced, so as not to leak memory). */
     private final WeakReference<WebWindow> window_;
 
     /** Single-threaded scheduled executor which executes the {@link JavaScriptJob}s behind the scenes. */
     private final ScheduledThreadPoolExecutor executor_ = new ScheduledThreadPoolExecutor(1);
 
     /** The job IDs and their corresponding {@link Future}s, which can be used to cancel the associated jobs. */
     private final Map<Integer, Future> futures_ = new TreeMap<Integer, Future>();
 
     /** A counter used to generate the IDs assigned to {@link JavaScriptJob}s. */
     private static final AtomicInteger NEXT_JOB_ID = new AtomicInteger(1);
 
     /** A counter used to generate the IDs assigned to threads. */
     private static final AtomicInteger NEXT_THREAD_ID = new AtomicInteger(1);
 
     /** Priority to use for background threads (bigger than the current thread's so that JS jobs execute ASAP). */
     private static final int PRIORITY = Math.min(Thread.MAX_PRIORITY, currentThread().getPriority() + 1);
 
     /** Logging support. */
     private static final Log LOG = LogFactory.getLog(JavaScriptJobManagerImpl.class);
 
     /**
      * Creates a new instance.
      * @param window the window associated with the new job manager
      */
     public JavaScriptJobManagerImpl(final WebWindow window) {
         window_ = new WeakReference<WebWindow>(window);
         executor_.setThreadFactory(new ThreadFactory() {
             public Thread newThread(final Runnable r) {
                 // Make sure the thread is a daemon thread so that it doesn't keep the JVM
                 // running unnecessarily; we also bump up the thread's priority so that
                 // JavaScript jobs execute ASAP.
                 final String name = "JavaScript Job Thread " + NEXT_THREAD_ID.getAndIncrement();
                 final Thread t = new Thread(r, name);
                 t.setDaemon(true);
                 t.setPriority(PRIORITY);
                 return t;
             }
         });
     }
 
     /** {@inheritDoc} */
     public int getJobCount() {
         // This method reads the job count a couple of times, to make sure that the count returned
         // is stable; the underlying ThreadPoolExecutor API only guarantees approximate results, so
         // we need to do a little bit of extra work to ensure that we return results that are as
         // reliable as possible.
         int count1 = Integer.MIN_VALUE;
         int count2 = Integer.MAX_VALUE;
         while (count1 != count2) {
             count1 = getJobCountInner();
             sleep(50);
             count2 = getJobCountInner();
         }
         return count1;
     }
 
     /** {@inheritDoc} */
     public int addJob(final String code, final int delay, final String description, final Page page) {
         final JavaScriptExecutionJob job = new JavaScriptExecutionJob(description, getWindow(), code);
         return addJob(job, delay, page);
     }
 
     /** {@inheritDoc} */
     public int addJob(final Function code, final int delay, final String description, final Page page) {
         final JavaScriptExecutionJob job = new JavaScriptExecutionJob(description, getWindow(), code);
         return addJob(job, delay, page);
     }
 
     /** {@inheritDoc} */
     public synchronized int addJob(final JavaScriptJob job, final int delay, final Page page) {
         final Window w = getWindow();
         if (w == null) {
             // The window to which this job manager belongs has been garbage collected.
             // Don't spawn any more jobs for it.
             return 0;
         }
         if (w.getWebWindow().getEnclosedPage() != page) {
             // The page requesting the addition of the job is no longer contained by our owner window.
             // Don't let it spawn any more jobs.
             return 0;
         }
 
         final int id = NEXT_JOB_ID.getAndIncrement();
         job.setId(id);
 
         final Future future = executor_.schedule(job, delay, MILLISECONDS);
         futures_.put(id, future);
         LOG.debug("Added job: " + job);
         return id;
     }
 
     /** {@inheritDoc} */
     public int addRecurringJob(final String code, final int period, final String description, final Page page) {
         final JavaScriptExecutionJob job = new JavaScriptExecutionJob(description, getWindow(), code);
         return addRecurringJob(job, period, page);
     }
 
     /** {@inheritDoc} */
     public int addRecurringJob(final Function code, final int period, final String description, final Page page) {
         final JavaScriptExecutionJob job = new JavaScriptExecutionJob(description, getWindow(), code);
         return addRecurringJob(job, period, page);
     }
 
     /** {@inheritDoc} */
     public synchronized int addRecurringJob(final JavaScriptJob job, final int period, final Page page) {
         final Window w = getWindow();
         if (w == null) {
             // The window to which this job manager belongs has been garbage collected.
             // Don't spawn any more jobs for it.
             return 0;
         }
         if (w.getWebWindow().getEnclosedPage() != page) {
             // The page requesting the addition of the job is no longer contained by our owner window.
             // Don't let it spawn any more jobs.
             return 0;
         }
 
         final int id = NEXT_JOB_ID.getAndIncrement();
         job.setId(id);
 
        final Future future = executor_.scheduleAtFixedRate(job, period, period, MILLISECONDS);
         futures_.put(id, future);
         LOG.debug("Added recurring job: " + job);
         return id;
     }
 
     /** {@inheritDoc} */
     public synchronized void stopJobAsap(final int id) {
         final Future future = futures_.remove(id);
         if (future != null) {
             future.cancel(false);
             LOG.debug("Stopped job (ASAP): " + id);
         }
     }
 
     /** {@inheritDoc} */
     public synchronized void stopJobNow(final int id) {
         final Future future = futures_.remove(id);
         if (future != null) {
             future.cancel(true);
             LOG.debug("Stopped job (now): " + id);
         }
     }
 
     /** {@inheritDoc} */
     public synchronized void stopAllJobsAsap() {
         for (final Future future : futures_.values()) {
             future.cancel(false);
         }
         futures_.clear();
         LOG.debug("Stopped all jobs.");
     }
 
     /** {@inheritDoc} */
     public boolean waitForAllJobsToFinish(final long maxWaitMillis) {
         LOG.debug("Waiting for all jobs to finish (will wait max " + maxWaitMillis + " millis).");
         final long start = System.currentTimeMillis();
         final long interval = Math.min(maxWaitMillis, 100);
         while (getJobCount() > 0 && System.currentTimeMillis() - start < maxWaitMillis) {
             sleep(interval);
         }
         final int jobs = getJobCount();
         LOG.debug("Finished waiting for all jobs to finish (final job count is " + jobs + ").");
         return jobs == 0;
     }
 
     /**
      * Returns the window to which this job manager belongs, or <tt>null</tt> if it has been garbage collected.
      * @return the window to which this job manager belongs, or <tt>null</tt> if it has been garbage collected
      */
     private Window getWindow() {
         final WebWindow ww = window_.get();
         if (ww == null) {
             // The window has been garbage collected!
             return null;
         }
         final Window w = (Window) ww.getScriptObject();
         return w;
     }
 
     /**
      * Returns the approximate number of jobs currently executing and waiting to be executed. This
      * method can only guarantee approximate results, because these are the only guarantees provided
      * by the {@link ScheduledThreadPoolExecutor} API which this method is built on.
      * @return the approximate number of jobs currently executing and waiting to be executed
      */
     private synchronized int getJobCountInner() {
         executor_.purge();
         return (int) (executor_.getTaskCount() - executor_.getCompletedTaskCount());
     }
 
     /**
      * Does a best effort attempt at sleeping the specified number of milliseconds. This method may
      * return early if the current thread is interrupted while it is sleeping.
      * @param millis the number of milliseconds to try to sleep
      */
     private void sleep(final long millis) {
         try {
             Thread.sleep(millis);
         }
         catch (final InterruptedException e) {
             // Ignore; we did our best.
         }
     }
 
 }
