 package brooklyn.util.task;
 
 import static brooklyn.util.JavaGroovyEquivalents.asString;
 import static brooklyn.util.JavaGroovyEquivalents.elvisString;
 import static brooklyn.util.JavaGroovyEquivalents.join;
 import groovy.lang.Closure;
 
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.lang.management.LockInfo;
 import java.lang.management.ManagementFactory;
 import java.lang.management.ThreadInfo;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.LinkedHashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.Callable;
 import java.util.concurrent.CancellationException;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import brooklyn.management.ExecutionManager;
 import brooklyn.management.Task;
 import brooklyn.util.GroovyJavaMethods;
 
 import com.google.common.base.Throwables;
 
 /**
  * The basic concrete implementation of a {@link Task} to be executed.
  *
  * A {@link Task} is a wrapper for an executable unit, such as a {@link Closure} or a {@link Runnable} or
  * {@link Callable} and will run in its own {@link Thread}.
  * <p>
  * The task can be given an optional displayName and description in its constructor (as named
  * arguments in the first {@link Map} parameter). It is guaranteed to have {@link Object#notify()} called
  * once whenever the task starts running and once again when the task is about to complete. Due to
  * the way executors work it is ugly to guarantee notification <em>after</em> completion, so instead we
  * notify just before then expect the user to call {@link #get()} - which will throw errors if the underlying job
  * did so - or {@link #blockUntilEnded()} which will not throw errors.
  *
  * @see BasicTaskStub
  */
 public class BasicTask<T> extends BasicTaskStub implements Task<T> {
     protected static final Logger log = LoggerFactory.getLogger(BasicTask.class);
 
     protected Callable<T> job;
     public final String displayName;
     public final String description;
 
     protected final Set tags = new LinkedHashSet();
 
     protected String blockingDetails = null;
 
     /**
      * Constructor needed to prevent confusion in groovy stubs when looking for default constructor,
      *
      * The generics on {@link Closure} break it if that is first constructor.
      */
     protected BasicTask() { this(Collections.emptyMap()); }
     protected BasicTask(Map flags) { this(flags, (Closure) null); }
 
     public BasicTask(Closure<T> job) { this(Collections.emptyMap(), job); }
     
     public BasicTask(Map flags, Closure<T> job) {
         this.job = job;
 
         if (flags.containsKey("tag")) tags.add(flags.remove("tag"));
         Object ftags = flags.remove("tags");
         if (ftags!=null) {
             if (ftags instanceof Collection) tags.addAll((Collection)ftags);
             else {
                 log.info("discouraged use of non-collection argument for 'tags' ("+ftags+") in "+this, new Throwable("trace of discouraged use of non-colleciton tags argument"));
                 tags.add(ftags);
             }
         }
 
         description = elvisString(flags.remove("description"), "");
         String d = asString(flags.remove("displayName"));
         if (d==null) d = join(tags, "-");
         displayName = d;
     }
 
     public BasicTask(Runnable job) { this(GroovyJavaMethods.closureFromRunnable(job)); }
     public BasicTask(Map flags, Runnable job) { this(flags, GroovyJavaMethods.closureFromRunnable(job)); }
     public BasicTask(Callable<T> job) { this(GroovyJavaMethods.closureFromCallable(job)); }
     public BasicTask(Map flags, Callable<T> job) { this(flags, GroovyJavaMethods.closureFromCallable(job)); }
 
     @Override
     public String toString() { 
         return "Task["+(displayName!=null && displayName.length()>0?displayName+
                 (tags!=null && !tags.isEmpty()?"":";")+" ":"")+
                 (tags!=null && !tags.isEmpty()?tags+"; ":"")+getId()+"]";
     }
 
     // housekeeping --------------------
 
     /*
      * These flags are set by BasicExecutionManager.submit.
      *
      * Order is guaranteed to be as shown below, in order of #. Within each # line it is currently in the order specified by commas but this is not guaranteed.
      * (The spaces between the # section indicate longer delays / logical separation ... it should be clear!)
      *
      * # submitter, submit time set, tags and other submit-time fields set, task tag-linked preprocessors onSubmit invoked
      *
      * # thread set, ThreadLocal getCurrentTask set
      * # start time set, isBegun is true
      * # task tag-linked preprocessors onStart invoked
      * # task end callback run, if supplied
      *
      * # task runs
      *
      * # task end callback run, if supplied
      * # task tag-linked preprocessors onEnd invoked (in reverse order of tags)
      * # end time set
      * # thread cleared, ThreadLocal getCurrentTask set
      * # Task.notifyAll()
      * # Task.get() (result.get()) available, Task.isDone is true
      *
      * Few _consumers_ should care, but internally we rely on this so that, for example, status is displayed correctly.
      * Tests should catch most things, but be careful if you change any of the above semantics.
      */
 
     protected long submitTimeUtc = -1;
     protected long startTimeUtc = -1;
     protected long endTimeUtc = -1;
     protected Task<?> submittedByTask;
 
     protected volatile Thread thread = null;
     private volatile boolean cancelled = false;
     protected Future<T> result = null;
 
     protected ExecutionManager em = null;
 
     void initExecutionManager(ExecutionManager em) {
         this.em = em;
     }
 
     synchronized void initResult(Future result) {
         if (this.result != null) 
             throw new IllegalStateException("task "+this+" is being given a result twice");
         this.result = result;
         notifyAll();
     }
 
     // metadata accessors ------------
 
     public Set<Object> getTags() { return Collections.unmodifiableSet(new LinkedHashSet(tags)); }
     public long getSubmitTimeUtc() { return submitTimeUtc; }
     public long getStartTimeUtc() { return startTimeUtc; }
     public long getEndTimeUtc() { return endTimeUtc; }
 
     public Future<T> getResult() { return result; }
     public Task<?> getSubmittedByTask() { return submittedByTask; }
 
     /** the thread where the task is running, if it is running */
     public Thread getThread() { return thread; }
 
     // basic fields --------------------
 
     public boolean isSubmitted() {
         return submitTimeUtc >= 0;
     }
 
     public boolean isBegun() {
         return startTimeUtc >= 0;
     }
 
     public synchronized boolean cancel() { return cancel(true); }
     public synchronized boolean cancel(boolean mayInterruptIfRunning) {
         if (isDone()) return false;
         boolean cancel = true;
         if (GroovyJavaMethods.truth(result)) { cancel = result.cancel(mayInterruptIfRunning); }
         cancelled = true;
         notifyAll();
         return cancel;
     }
 
     public boolean isCancelled() {
         return cancelled || (result!=null && result.isCancelled());
     }
 
     public boolean isDone() {
         return cancelled || (result!=null && result.isDone());
     }
 
     /**
      * Returns true if the task has had an error.
      *
      * Only true if calling {@link #get()} will throw an exception when it completes (including cancel).
      * Implementations may set this true before completion if they have that insight, or
      * (the default) they may compute it lazily after completion (returning false before completion).
      */
     public boolean isError() {
         if (!isDone()) return false;
         if (isCancelled()) return true;
         try {
             get();
             return false;
         } catch (Throwable t) {
             return true;
         }
     }
 
     public T get() throws InterruptedException, ExecutionException {
         blockUntilStarted();
         return result.get();
     }
 
     // future value --------------------
 
     public synchronized void blockUntilStarted() {
         while (true) {
             if (cancelled) throw new CancellationException();
             if (result==null)
                 try {
                     wait();
                 } catch (InterruptedException e) {
                     Thread.currentThread().interrupt();
                     Throwables.propagate(e);
                 }
             if (result!=null) return;
         }
     }
 
     public void blockUntilEnded() {
         try { blockUntilStarted(); } catch (Throwable t) {
             if (log.isDebugEnabled())
                 log.debug("call from "+Thread.currentThread()+" blocking until "+this+" finishes ended with error: "+t);
             /* contract is just to log errors at debug, otherwise do nothing */
             return; 
         }
         try { result.get(); } catch (Throwable t) {
             if (log.isDebugEnabled())
                 log.debug("call from "+Thread.currentThread()+" blocking until "+this+" finishes ended with error: "+t);
             /* contract is just to log errors at debug, otherwise do nothing */
             return; 
         }
     }
 
     public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
         long start = System.currentTimeMillis();
         long milliseconds = TimeUnit.MILLISECONDS.convert(timeout, unit);
         long end  = start + milliseconds;
         while (end > System.currentTimeMillis()) {
             if (cancelled) throw new CancellationException();
             if (result == null) wait(end - System.currentTimeMillis());
             if (result != null) break;
         }
         long remaining = end -  System.currentTimeMillis();
         if (remaining > 0) {
             return result.get(remaining, TimeUnit.MILLISECONDS);
         } else {
             throw new TimeoutException();
         }
     }
 
     /**
      * Returns a brief status string
      *
      * Plain-text format. Reported status if there is one, otherwise state which will be one of:
      * <ul>
      * <li>Not submitted
      * <li>Submitted for execution
      * <li>Ended by error
      * <li>Ended by cancellation
      * <li>Ended normally
      * <li>Running
      * <li>Waiting
      * </ul>
      */
     public String getStatusSummary() {
         return getStatusString(0);
     }
 
     /**
      * Returns detailed status, suitable for a hover
      *
      * Plain-text format, with new-lines (and sometimes extra info) if multiline enabled.
      */
     public String getStatusDetail(boolean multiline) {
         return getStatusString(multiline?2:1);
     }
 
     /**
      * This method is useful for callers to see the status of a task.
      *
      * Also for developers to see best practices for examining status fields etc
      *
      * @param verbosity 0 = brief, 1 = one-line with some detail, 2 = lots of detail
      */
     protected String getStatusString(int verbosity) {
 //        Thread t = getThread();
         String rv;
         if (submitTimeUtc <= 0) rv = "Not submitted";
         else if (!isCancelled() && startTimeUtc <= 0) {
             rv = "Submitted for execution";
             if (verbosity>0) {
                 long elapsed = System.currentTimeMillis() - submitTimeUtc;
                 rv += " "+elapsed+" ms ago";
             }
         } else if (isDone()) {
             long elapsed = endTimeUtc - submitTimeUtc;
             String duration = ""+elapsed+" ms";
             rv = "Ended ";
             if (isCancelled()) {
                 rv += "by cancellation";
                 if (verbosity >= 1) rv+=" after "+duration;
             } else if (isError()) {
                 rv += "by error";
                 if (verbosity >= 1) {
                     rv += " after "+duration;
                     Object error;
                     try { String rvx = ""+get(); error = "no error, return value "+rvx; /* shouldn't happen */ }
                     catch (Throwable tt) { error = tt; }
 
                     //remove outer ExecException which is reported by the get(), we want the exception the task threw
                     if (error instanceof ExecutionException) error = ((Throwable)error).getCause();
 
                     if (verbosity == 1) rv += " ("+error+")";
                     else {
                         StringWriter sw = new StringWriter();
                        if (error instanceof ExecutionException) 
                            ((Throwable)error).printStackTrace(new PrintWriter(sw));
                         rv += "\n"+sw.getBuffer();
                     }
                 }
             } else {
                 rv += "normally";
                 if (verbosity>=1) {
                     if (verbosity==1) {
                         try {
                             rv += ", result "+get();
                         } catch (Exception e) {
                             rv += ", but error accessing result ["+e+"]"; //shouldn't happen
                         }
                     } else {
                         rv += " after "+duration;
                         try {
                             rv += "\n" + "Result: "+get();
                         } catch (Exception e) {
                             rv += " at first\n" +
                             		"Error accessing result ["+e+"]"; //shouldn't happen
                         }
                     }
                 }
             }
         } else {
 			rv = getActiveTaskStatusString(verbosity);
         }
         return rv;
     }
 
 	protected String getActiveTaskStatusString(int verbosity) {
 		String rv = "";
 		Thread t = getThread();
 	
 		// Normally, it's not possible for thread==null as we were started and not ended
 		
 		// However, there is a race where the task starts sand completes between the calls to getThread()
 		// at the start of the method and this call to getThread(), so both return null even though
 		// the intermediate checks returned started==true isDone()==false.
 		if (t == null) {
 			if (isDone()) {
 				return getStatusString(verbosity);
 			} else {
 			    //should only happen for repeating task which is not active
                 return "Sleeping";
 			}
 		}
 
 		ThreadInfo ti = ManagementFactory.getThreadMXBean().getThreadInfo(t.getId(), (verbosity<=0 ? 0 : verbosity==1 ? 1 : Integer.MAX_VALUE));
 		if (getThread()==null)
 			//thread might have moved on to a new task; if so, recompute (it should now say "done")
 			return getStatusString(verbosity);
 		LockInfo lock = ti.getLockInfo();
 		if (!GroovyJavaMethods.truth(lock) && ti.getThreadState()==Thread.State.RUNNABLE) {
 			//not blocked
 			if (ti.isSuspended()) {
 				// when does this happen?
 				rv = "Waiting";
 				if (verbosity >= 1) rv += ", thread suspended";
 			} else {
 				rv = "Running";
 				if (verbosity >= 1) rv += " ("+ti.getThreadState()+")";
 			}
 		} else {
 			rv = "Waiting";
 			if (verbosity>=1) {
 				if (ti.getThreadState() == Thread.State.BLOCKED) {
 					rv += " (mutex) on "+lookup(lock);
 					//TODO could say who holds it
 				} else if (ti.getThreadState() == Thread.State.WAITING) {
 					rv += " (notify) on "+lookup(lock);
 				} else if (ti.getThreadState() == Thread.State.TIMED_WAITING) {
 					rv += " (timed) on "+lookup(lock);
 				} else {
 					rv = " ("+ti.getThreadState()+") on "+lookup(lock);
 				}
 				if (GroovyJavaMethods.truth(blockingDetails)) rv += " - "+blockingDetails;
 			}
 		}
 		if (verbosity>=2) {
 			StackTraceElement[] st = ti.getStackTrace();
 			st = StackTraceSimplifier.cleanStackTrace(st);
 			if (st!=null && st.length>0)
 				rv += "\n" +"At: "+st[0];
 			for (int ii=1; ii<st.length; ii++) {
 				rv += "\n" +"    "+st[ii];
 			}
 		}
 		return rv;
 	}
 	
     protected String lookup(LockInfo info) {
         return GroovyJavaMethods.truth(info) ? ""+info : "unknown (sleep)";
     }
 
     public String getDisplayName() {
         return displayName;
 
     }
 
     public String getDescription() {
         return description;
     }
     
     /** allows a task user to specify why a task is blocked; for use immediately before a blocking/wait,
      * and typically cleared immediately afterwards; referenced by management api to inspect a task
      * which is blocking
      */
     public void setBlockingDetails(String blockingDetails) {
         this.blockingDetails = blockingDetails;
     }
     
     public String getBlockingDetails() {
         return blockingDetails;
     }
 }
