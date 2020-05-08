 package net.praqma.hudson.scm;
 
 import hudson.model.Build;
 import hudson.model.AbstractProject;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 
 import net.praqma.clearcase.ucm.entities.Baseline;
 import net.praqma.clearcase.ucm.entities.Project;
 import java.util.ConcurrentModificationException;
 
 import net.praqma.clearcase.ucm.entities.Component;
 import net.praqma.clearcase.ucm.entities.Stream;
 import net.praqma.clearcase.ucm.view.SnapshotView;
 import net.praqma.util.debug.PraqmaLogger;
 import net.praqma.util.debug.PraqmaLogger.Logger;
 
 /**
  * This is the state object for the Pucm jobs
  * 
  * @author wolfgang
  * 
  */
 public class PucmState {
 
     private List<State> states = Collections.synchronizedList(new ArrayList<State>());
     private static final String linesep = System.getProperty("line.separator");
     private Logger logger = PraqmaLogger.getLogger();
 
     /**
      * Get a state given job name and job number
      * 
      * @param jobName
      *            the hudson job name
      * @param jobNumber
      *            the hudson job number
      * @return
      */
     public State getState(String jobName, Integer jobNumber) {
         for (State s : states) {
             if (s.getJobName().equals(jobName)
                     && s.getJobNumber().equals(jobNumber)) {
                 return s;
             }
         }
 
         State s = new State(jobName, jobNumber);
         states.add(s);
         return s;
     }
 
     public boolean removeState(String jobName, Integer jobNumber) {
         for (State s : states) {
             if (s.getJobName().equals(jobName) && s.getJobNumber() == jobNumber) {
                 states.remove(s);
                 return true;
             }
         }
 
         return false;
     }
 
     public State getStateByBaseline(String jobName, String baseline) {
         for (State s : states) {
             if (s.getJobName().equals(jobName) && s.getBaseline() != null && s.getBaseline().getFullyQualifiedName().equals(baseline)) {
                 return s;
             }
         }
 
         return null;
     }
 
     public void addState(State state) {
         this.states.add(state);
     }
 
     public boolean stateExists(State state) {
         return stateExists(state.jobName, state.jobNumber);
     }
 
     public boolean stateExists(String jobName, Integer jobNumber) {
         for (State s : states) {
             if (s.getJobName().equals(jobName) && s.getJobNumber() == jobNumber) {
                 return true;
             }
         }
 
         return false;
     }
 
     public boolean removeState(State state) {
         return states.remove(state);
     }
 
     public int recalculate(AbstractProject<?, ?> project) {
         int count = 0;
 
         try {
             State s = null;
             Iterator<State> it = states.iterator();
 
             while (it.hasNext()) {
                 s = it.next();
                 Integer bnum = s.getJobNumber();
                 Object o = project.getBuildByNumber(bnum);
                 Build bld = (Build) o;
 
                 /* The job is not running */
                 if (!bld.isLogUpdated()) {
                     it.remove();
                     count++;
                 }
             }
         } catch (ConcurrentModificationException e) {
             logger.warning("Concurrency warning in PucmState");
         } catch (NullPointerException e) {
             logger.warning("This should not happen");
         }
 
         return count;
     }
 
     public int size() {
         return states.size();
     }
 
     public String stringify() {
         return net.praqma.util.structure.Printer.listPrinterToString(states);
     }
 
     public class State {
 
         private Baseline baseline;
         private Stream stream;
         private Component component;
         private boolean doPostBuild = true;
         private Project.Plevel plevel;
         private String loadModule;
         private String jobName;
         private Integer jobNumber;
         private boolean addedByPoller = false;
         private long multiSiteFrequency = 0;
         private List<Baseline> baselines = null;
         private Polling polling;
         private boolean needsToBeCompleted = true;
         private Logger logger;
         private BaselineInformation bi;
         private SnapshotView snapView;
         private SnapshotView deliverView;
 
         public State() {
         }
 
         public State(String jobName, Integer jobNumber) {
             this.jobName = jobName;
             this.jobNumber = jobNumber;
         }
 
         public State(String jobName, Integer jobNumber, Baseline baseline,
                 Stream stream, Component component, boolean doPostBuild) {
             this.jobName = jobName;
             this.jobNumber = jobNumber;
             this.baseline = baseline;
             this.stream = stream;
             this.component = component;
             this.doPostBuild = doPostBuild;
         }
 
         @Deprecated
         public void save() {
             PucmState.this.addState(this);
         }
 
         public boolean remove() {
             return PucmState.this.removeState(this);
         }
 
         public Baseline getBaseline() {
             return baseline;
         }
 
         public void setBaseline(Baseline baseline) {
             this.baseline = baseline;
         }
 
         public Stream getStream() {
             return stream;
         }
 
         public Polling getPolling() {
             return polling;
         }
 
         public void setPolling(Polling polling) {
             this.polling = polling;
         }
 
         public void setStream(Stream stream) {
             this.stream = stream;
         }
 
         public Component getComponent() {
             return component;
         }
 
         public void setComponent(Component component) {
             this.component = component;
         }
 
         public boolean doPostBuild() {
             return doPostBuild;
         }
 
         public void setPostBuild(boolean doPostBuild) {
             this.doPostBuild = doPostBuild;
         }
 
         public String getJobName() {
             return jobName;
         }
 
         public void setJobName(String jobName) {
             this.jobName = jobName;
         }
 
         public Integer getJobNumber() {
             return jobNumber;
         }
 
         public void setJobNumber(Integer jobNumber) {
             this.jobNumber = jobNumber;
         }
 
         public void setPlevel(Project.Plevel plevel) {
             this.plevel = plevel;
         }
 
         public Project.Plevel getPlevel() {
             return plevel;
         }
 
         public void setLogger(Logger logger) {
             this.logger = logger;
         }
 
         public Logger getLogger() {
             return logger;
         }
 
         public String stringify() {
             StringBuffer sb = new StringBuffer();
 
             sb.append("Job name      : " + this.jobName + linesep);
             sb.append("Job number    : " + this.jobNumber + linesep);
             sb.append("Component     : " + this.component + linesep);
             sb.append("Stream        : " + this.stream + linesep);
             sb.append("Baseline      : " + this.baseline + linesep);
             sb.append("Poll level    : " + (this.plevel != null ? this.plevel.toString() : "Missing") + linesep);
             sb.append("Load Module   : " + this.loadModule + linesep);
            sb.append("Baseline list : " + (this.baseline != null ? this.baselines.size() : "0") + linesep);
             sb.append("Added by poll : " + (this.addedByPoller ? "Yes" : "No") + linesep);
             sb.append("Multi site    : " + (this.multiSiteFrequency > 0 ? StoredBaselines.milliToMinute(this.multiSiteFrequency) : "N/A") + linesep);
             sb.append("postBuild     : " + this.doPostBuild + linesep);
 
             return sb.toString();
         }
 
         public String toString() {
             return "(" + jobName + ", " + jobNumber + ")";
         }
 
         public boolean equals(Object other) {
             if (other instanceof State) {
                 if (this.getJobName().equals(((State) other).getJobName())
                         && this.getJobNumber().equals(
                         ((State) other).getJobNumber())) {
                     return true;
                 }
 
             }
 
             return false;
         }
 
         public void setLoadModule(String loadModule) {
             this.loadModule = loadModule;
         }
 
         public String getLoadModule() {
             return loadModule;
         }
 
         public void setAddedByPoller(boolean addedByPoller) {
             this.addedByPoller = addedByPoller;
         }
 
         public boolean isAddedByPoller() {
             return addedByPoller;
         }
 
         public void setMultiSiteFrequency(long multiSiteFrquency) {
             this.multiSiteFrequency = multiSiteFrquency;
         }
 
         public long getMultiSiteFrquency() {
             return multiSiteFrequency;
         }
 
         public boolean isMultiSite() {
             return this.multiSiteFrequency > 0;
         }
 
         public void setBaselines(List<Baseline> baselines) {
             this.baselines = baselines;
         }
 
         public List<Baseline> getBaselines() {
             return baselines;
         }
 
         public SnapshotView getSnapView() {
             return this.snapView;
         }
         
         public void setSnapView(SnapshotView snapView) {
             this.snapView = snapView;
         }
         
         public SnapshotView getDeliverView() {
             return this.deliverView;
         }
         
         public void setDeliverView(SnapshotView deliverView) {
             this.deliverView = deliverView;
         }
         
         public void setNeedsToBeCompleted( boolean s ) {
             this.needsToBeCompleted = s;
         }
         
         public boolean needsToBeCompleted() {
             return this.needsToBeCompleted;
         }
         
         public void setBaselineInformation( BaselineInformation bi ) {
         	this.bi = bi;
         }
         
         public BaselineInformation getBaselineInformation() {
         	return this.bi;
         }
     }
 }
