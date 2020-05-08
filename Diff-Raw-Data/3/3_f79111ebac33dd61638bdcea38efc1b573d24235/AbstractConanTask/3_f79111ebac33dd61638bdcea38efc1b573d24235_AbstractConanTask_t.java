 package uk.ac.ebi.fgpt.conan.core.task;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import uk.ac.ebi.fgpt.conan.core.process.DefaultProcessRun;
 import uk.ac.ebi.fgpt.conan.model.*;
 import uk.ac.ebi.fgpt.conan.service.exception.ProcessExecutionException;
 import uk.ac.ebi.fgpt.conan.service.exception.TaskExecutionException;
 
 import java.util.*;
 
 /**
  * An abstract implementation of a {@link uk.ac.ebi.fgpt.conan.model.ConanTask} that contains execute() implementations
  * and some useful protected methods for firing status events.  Concrete implementations should define how to construct
  * and manipulate each task.  This task implementation is designed to support the addition of listeners to report on
  * changes in each task.
  *
  * @author Tony Burdett
  * @date 13-Oct-2010
  */
 public abstract class AbstractConanTask<P extends ConanPipeline> implements ConanTask<P> {
     // tracks the current task being executed, always needs to be set
     protected int firstTaskIndex;
     protected int currentExecutionIndex;
     protected String ID;
 
     // dates for tracking start and end of task
     protected Date creationDate;
     protected Date submissionDate;
     protected Date startDate;
     protected Date completionDate;
 
     // tracks the processes that are run
     protected List<ConanProcessRun> processRuns;
 
     // status flags
     protected State currentState;
     protected String statusMessage;
     protected boolean submitted;
     protected boolean paused;
 
     // listeners
     private Set<ConanTaskListener> listeners;
 
     private Logger log = LoggerFactory.getLogger(getClass());
 
     protected AbstractConanTask(int firstTaskIndex) {
         // set the index of the task we will start with
         this.firstTaskIndex = 0;
         this.currentExecutionIndex = firstTaskIndex;
 
         // date of creation
         this.creationDate = new Date();
 
         // set status tracking
         this.processRuns = new ArrayList<ConanProcessRun>();
 
         // add listeners and make sure task isn't paused
         this.listeners = new HashSet<ConanTaskListener>();
         this.paused = false;
 
         // finally set our initial state
         updateCurrentState(State.CREATED);
         updateCurrentStatusMessage("Task created");
     }
 
     protected Logger getLog() {
         return log;
     }
 
     public boolean addConanTaskListener(ConanTaskListener listener) {
         return listeners.add(listener);
     }
 
     public boolean removeConanTaskListener(ConanTaskListener listener) {
         return listeners.remove(listener);
     }
 
     public void setId(String ID) {
         this.ID = ID;
     }
 
     public String getId() {
         return this.ID;
     }
 
     public ConanProcess getFirstProcess() {
         if (firstTaskIndex < getPipeline().getProcesses().size()) {
             return getPipeline().getProcesses().get(firstTaskIndex);
         }
         else {
             return null;
         }
     }
 
     public synchronized ConanProcess getLastProcess() {
         if (currentExecutionIndex > firstTaskIndex && currentExecutionIndex <= getPipeline().getProcesses().size()) {
             return getPipeline().getProcesses().get(currentExecutionIndex - 1);
         }
         else {
             return null;
         }
     }
 
     public synchronized ConanProcess getCurrentProcess() {
         if (getCurrentState() == State.RUNNING && currentExecutionIndex < getPipeline().getProcesses().size()) {
             return getPipeline().getProcesses().get(currentExecutionIndex);
         }
         else {
             return null;
         }
     }
 
     public synchronized ConanProcess getNextProcess() {
         if (getCurrentState() == State.RUNNING) {
             if ((currentExecutionIndex + 1) < getPipeline().getProcesses().size()) {
                 return getPipeline().getProcesses().get(currentExecutionIndex + 1);
             }
             else {
                 return null;
             }
         }
         else {
             if (currentExecutionIndex < getPipeline().getProcesses().size()) {
                 return getPipeline().getProcesses().get(currentExecutionIndex);
             }
             else {
                 return null;
             }
         }
     }
 
     public Date getCreationDate() {
         return creationDate;
     }
 
     public Date getSubmissionDate() {
         return submissionDate;
     }
 
     public Date getStartDate() {
         return startDate;
     }
 
     public Date getCompletionDate() {
         return completionDate;
     }
 
     public List<ConanProcessRun> getConanProcessRuns() {
         return processRuns;
     }
 
     public List<ConanProcessRun> getConanProcessRunsForProcess(ConanProcess process)
             throws IllegalArgumentException {
         if (getPipeline().getProcesses().contains(process)) {
             List<ConanProcessRun> results = new ArrayList<ConanProcessRun>();
             for (ConanProcessRun run : getConanProcessRuns()) {
                 if (run.getProcessName().equals(process.getName())) {
                     results.add(run);
                 }
             }
             return results;
         }
         else {
             throw new IllegalArgumentException("The process '" + process.getName() + "' " +
                                                        "is not part of the pipeline for task '" + getId() + "'");
         }
     }
 
     public State getCurrentState() {
         return currentState;
     }
 
     public String getStatusMessage() {
         return statusMessage;
     }
 
     public boolean execute() throws TaskExecutionException, InterruptedException {
         // check the current state for execution
         checkState();
 
         getLog().debug("Executing task " + getId());
         try {
             // do processes in order
             while (!isPaused() && getCurrentProcess() != null) {
                 if (Thread.interrupted()) {
                     // this thread has been interrupted by a shutdown request, so stop executing
                     throw new InterruptedException();
                 }
 
                 // extract only those parameters we need
                 Map<ConanParameter, String> nextProcessParams = new HashMap<ConanParameter, String>();
                 for (ConanParameter param : getCurrentProcess().getParameters()) {
                     nextProcessParams.put(param, getParameterValues().get(param));
                 }
 
                 // increment the execution index and fire an event as we're about to start
                 getLog().debug("Process being executed for task " + getId() + " is " +
                                        getCurrentProcess().getName() + ", " + "supplying parameters: " +
                                        nextProcessParams);
                 fireProcessStartedEvent();
 
                 // now execute
                 getCurrentProcess().execute(nextProcessParams);
 
                 // once finished, update the end date
                 fireProcessEndedEvent();
             }
 
             // finalise task execution
             return checkExitStatus();
         }
         catch (ProcessExecutionException e) {
             // log this exception
             getLog().error("Process '" + getCurrentProcess().getName() + "' failed to execute, " +
                                    "exit code " + e.getExitValue());
             getLog().debug("Execution exception follows", e);
             if (e.causesAbort()) {
               // critical fail, should cause instant abort
              fireTaskAbortedEvent();
             }
             else {
               fireProcessFailedEvent(e);
             }
             throw new TaskExecutionException(e);
         }
         catch (RuntimeException e) {
             // log this exception
             getLog().error("A runtime exception occurred whilst executing task '" + getId() + "'", e);
             getLog().error("Process '" + getCurrentProcess().getName() + "' failed to execute");
             fireProcessFailedEvent(1);
             throw new TaskExecutionException(e);
         }
         finally {
             // finally, if we have completed or stopped, remove all listeners so this object is dereferenced
             if (getCurrentState() == ConanTask.State.COMPLETED || getCurrentState() == ConanTask.State.ABORTED) {
                 setListeners(Collections.<ConanTaskListener>emptySet());
             }
             getLog().debug("Task '" + getId() + "' execution ended");
         }
     }
 
     public void submit() {
         // resubmitting does nothing
         if (!isSubmitted()) {
             this.submitted = true;
             fireTaskSubmittedEvent();
         }
     }
 
     public boolean isSubmitted() {
         return submitted;
     }
 
     public void pause() {
         // set paused flag
         getLog().debug("Pausing task '" + getName() + "'");
         this.paused = true;
     }
 
     public boolean isPaused() {
         getLog().trace("Checking paused status of task '" + getName() + "', " + (paused ? "paused" : "not paused"));
         return paused;
     }
 
     public void resume() {
         // just reset paused flag
         getLog().debug("Resuming task '" + getName() + "', no longer paused");
         this.paused = false;
     }
 
     public void retryLastProcess() {
         // wind execution index back one
         currentExecutionIndex--;
         // and reset paused flag
         getLog().debug("Retrying task '" + getName() + "', no longer paused");
         this.paused = false;
     }
 
     public void restart() {
         throw new UnsupportedOperationException("Tasks cannot currently be completely restarted");
     }
 
     public void abort() {
         fireTaskAbortedEvent();
     }
 
     protected void checkState() throws TaskExecutionException {
         getLog().debug("Checking current state of task '" + getId() + "': " + getCurrentState());
 
         if (getCurrentState() == ConanTask.State.ABORTED) {
             // stopped tasks must never re-execute
             throw new TaskExecutionException("Task has previously stopped, and cannot be re-executed");
         }
         else if (getCurrentState().compareTo(State.SUBMITTED) < 0) {
             // this is task hasn't ever been submitted
             throw new TaskExecutionException("Task does not appear to have ever been submitted");
         }
         else if (getCurrentState() == State.RUNNING) {
             // this task was recovered after a shutdown, so we can start executing again
             fireTaskRecoveryEvent();
         }
         else {
             // has been submitted but not running, probably paused or failed, so start this task
             fireTaskStartedEvent();
         }
     }
 
     protected boolean checkExitStatus() {
         // was this event paused by an intervention?
         if (isPaused()) {
             // we paused, so we didn't complete everything successfully
             fireTaskPausedEvent();
             return false;
         }
         else {
             // we didn't pause, so we either got to the end or were interrupted
             if (Thread.currentThread().isInterrupted()) {
                 // we were interrupted, so we didn't complete successfully
                 getLog().warn("Task '" + getId() + "' is terminating following an interrupt request " +
                                       "to thread " + Thread.currentThread().getName());
                 return false;
             }
             else {
                 // if we didn't pause this task, we got to the end, so flag it as complete
                 fireTaskCompletedEvent();
                 return true;
             }
         }
     }
 
     protected void updateCurrentState(State currentState) {
         this.currentState = currentState;
     }
 
     protected void updateCurrentStatusMessage(String currentStatusMessage) {
         this.statusMessage = currentStatusMessage;
     }
 
     protected void fireTaskSubmittedEvent() {
         getLog().debug("Task " + getId() + " submitted!");
         updateCurrentState(State.SUBMITTED);
         updateCurrentStatusMessage("Submitted");
 
         this.submissionDate = new Date();
         ConanTaskEvent event = new ConanTaskEvent(this, getCurrentState(), getFirstProcess(), null);
         for (ConanTaskListener listener : getListeners()) {
             listener.stateChanged(event);
         }
 
     }
 
     protected void fireTaskStartedEvent() {
         if (getCurrentState() == State.SUBMITTED) {
             updateCurrentStatusMessage("Started");
             this.startDate = new Date();
         }
         else {
             updateCurrentStatusMessage("Restarted");
         }
 
         getLog().debug("Task " + getId() + " started!");
         updateCurrentState(State.RUNNING);
 
         ConanTaskEvent event = new ConanTaskEvent(this, getCurrentState(), getFirstProcess(), null);
         for (ConanTaskListener listener : getListeners()) {
             listener.stateChanged(event);
         }
     }
 
     protected void fireTaskRecoveryEvent() {
         getLog().debug("Task " + getId() + " was recovered successfully!");
         updateCurrentState(State.RECOVERED);
         updateCurrentStatusMessage("Recovered");
 
         ConanTaskEvent event = new ConanTaskEvent(
                 this, getCurrentState(),
                 getNextProcess(),
                 processRuns.get(processRuns.size() - 1));
         for (ConanTaskListener listener : getListeners()) {
             listener.stateChanged(event);
         }
     }
 
     protected void fireTaskPausedEvent() {
         getLog().debug("Task " + getId() + " paused!");
         updateCurrentState(State.PAUSED);
         if (getNextProcess() == null) {
             updateCurrentStatusMessage("Paused during the last process");
         }
         else {
             updateCurrentStatusMessage("Paused before " + getNextProcess().getName());
         }
 
         ConanTaskEvent event =
                 new ConanTaskEvent(this, getCurrentState(), null, processRuns.get(processRuns.size() - 1));
         for (ConanTaskListener listener : getListeners()) {
             listener.stateChanged(event);
         }
     }
 
     protected void fireTaskCompletedEvent() {
         getLog().debug("Task " + getId() + " completed!");
         updateCurrentState(State.COMPLETED);
         updateCurrentStatusMessage("Complete");
 
         this.completionDate = new Date();
         ConanTaskEvent event = new ConanTaskEvent(this, getCurrentState(), null, null);
         for (ConanTaskListener listener : getListeners()) {
             listener.stateChanged(event);
         }
         getLog().debug("Listeners notified of task completion, so will now be deregistered");
 
         // finally, remove any listeners as this task is complete
         getListeners().clear();
     }
 
     protected void fireTaskAbortedEvent() {
         getLog().debug("Task " + getId() + " was aborted!");
         updateCurrentState(State.ABORTED);
         if (getLastProcess() == null) {
             updateCurrentStatusMessage("Aborted before the first process started");
         }
         else {
             updateCurrentStatusMessage("Aborted after " + getLastProcess().getName());
         }
 
         this.completionDate = new Date();
         ConanTaskEvent event;
         if (processRuns.isEmpty()) {
             event = new ConanTaskEvent(this, getCurrentState(), null, null);
         }
         else {
             event = new ConanTaskEvent(this, getCurrentState(), null, processRuns.get(processRuns.size() - 1));
         }
         for (ConanTaskListener listener : getListeners()) {
             listener.stateChanged(event);
         }
     }
 
     protected void fireProcessStartedEvent() {
         getLog().debug("Task " + getId() + " is commencing next process, " + getCurrentProcess().getName() + " " +
                                "(execution index = " + currentExecutionIndex + ")");
         updateCurrentState(State.RUNNING);
 
         // create our process run object for the process we're going to execute
         DefaultProcessRun pr = new DefaultProcessRun(getCurrentProcess().getName(), getSubmitter());
         processRuns.add(pr);
         pr.setStartDate(new Date());
 
         updateCurrentStatusMessage("Doing " + getCurrentProcess().getName());
         ConanTaskEvent event = new ConanTaskEvent(this, getCurrentState(), getCurrentProcess(), pr);
         for (ConanTaskListener listener : getListeners()) {
             listener.processStarted(event);
         }
     }
 
     protected void fireProcessEndedEvent() {
         getLog().debug("Task " + getId() + " finished its current process");
         updateCurrentStatusMessage("Finished " + getCurrentProcess().getName());
 
         // increment the execution index
         currentExecutionIndex++;
 
         // get the last process run object, and set it's end date
         DefaultProcessRun pr = (DefaultProcessRun) processRuns.get(processRuns.size() - 1);
         pr.setEndDate(new Date());
         pr.setExitValue(0);
 
         ConanTaskEvent event = new ConanTaskEvent(this, getCurrentState(), getLastProcess(), pr);
         for (ConanTaskListener listener : getListeners()) {
             listener.processEnded(event);
         }
     }
 
     protected void fireProcessFailedEvent(ProcessExecutionException pex) {
         getLog().debug("Task " + getId() + " failed its current process, exit code " + pex.getExitValue());
         updateCurrentStatusMessage("Failed at " + getCurrentProcess().getName());
         updateCurrentState(State.FAILED);
 
         // increment the execution index
         currentExecutionIndex++;
 
         // get the last process run object, and set it's end date
         DefaultProcessRun pr = (DefaultProcessRun) processRuns.get(processRuns.size() - 1);
         pr.setEndDate(new Date());
         pr.setExitValue(pex.getExitValue());
 
         ConanTaskEvent event =
                 new ConanTaskEvent(this, getCurrentState(), getLastProcess(), pr, pex);
         for (ConanTaskListener listener : getListeners()) {
             listener.processFailed(event);
         }
     }
 
     protected void fireProcessFailedEvent(int exitValue) {
         getLog().debug("Task " + getId() + " failed its current process, exit code " + exitValue);
         updateCurrentStatusMessage("Failed at " + getCurrentProcess().getName());
         updateCurrentState(State.FAILED);
 
         // increment the execution index
         currentExecutionIndex++;
 
         // get the last process run object, and set it's end date
         DefaultProcessRun pr = (DefaultProcessRun) processRuns.get(processRuns.size() - 1);
         pr.setEndDate(new Date());
         pr.setExitValue(exitValue);
 
         ConanTaskEvent event =
                 new ConanTaskEvent(this, getCurrentState(), getLastProcess(), pr);
         for (ConanTaskListener listener : getListeners()) {
             listener.processFailed(event);
         }
     }
 
     private Set<ConanTaskListener> getListeners() {
         return listeners;
     }
 
     private void setListeners(Set<ConanTaskListener> listeners) {
         this.listeners = listeners;
     }
 }
