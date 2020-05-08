 package hudson.plugins.sctmexecutor.publisher;
 
 import hudson.Util;
 import hudson.model.AbstractBuild;
 import hudson.tasks.test.TabulatedResult;
 import hudson.tasks.test.AbstractTestResultAction;
 import hudson.tasks.test.TestObject;
 import hudson.tasks.test.TestResult;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.StaplerResponse;
 
 public final class SCTMTestSuiteResult extends TabulatedResult implements Comparable<SCTMTestSuiteResult> {
   private static final long serialVersionUID = -5725371200267573341L;
 
   private TestObject parent;
   private final String name;
   private final Collection<TestResult> childResults;
   private final Map<String, SCTMTestResult> configurationResults;
 
   private transient AbstractTestResultAction<SCTMResultAction> parentAction;
 
   public SCTMTestSuiteResult(String name) {
     this.name = name;
     this.childResults = new ArrayList<TestResult>();
     this.configurationResults = new HashMap<String, SCTMTestResult>();
   }
 
   private int getXCount(SCTMTestResult.TestState state) {
     int count = 0;
     for (SCTMTestResult result : this.getConfigurationResult().values()) {
       switch (state) {
       case PASSED:
         count += result.getPassCount();
         break;
       case SKIPPED:
         count += result.getSkipCount();
         break;
       case FAILED:
         count += result.getFailCount();
         break;
       }
     }
     return count;
   }
 
   synchronized void calculateConfigurationResults() {
     for (TestResult result : this.childResults) {
       Map<String, SCTMTestResult> configurationResult = null;
       if (result instanceof SCTMTestCaseResult) {
         configurationResult = ((SCTMTestCaseResult) result).getConfigurationResult();
       } else if (result instanceof SCTMTestSuiteResult) {
         ((SCTMTestSuiteResult) result).calculateConfigurationResults();
         configurationResult = ((SCTMTestSuiteResult) result).getConfigurationResult();
       }
       if (configurationResult != null) {
         for (Entry<String, SCTMTestResult> childResult : configurationResult.entrySet()) {
           SCTMTestResult testResult = configurationResults.get(childResult.getKey());
           if (testResult == null)
             configurationResults.put(childResult.getKey(), new SCTMTestResult(childResult.getValue()));
           else {
             SCTMTestResult childTestResult = childResult.getValue();
             testResult.addSubTestCounts(childTestResult.getPassCount(), childTestResult.getSkipCount(),
                 childTestResult.getFailCount());
             testResult.addSubDuration(childTestResult.getDuration());
           }
         }
       }
     }
   }
 
   synchronized Map<String, SCTMTestResult> getConfigurationResult() {
     return this.configurationResults;
   }
 
   @Override
   public int getPassCount() {
     return getXCount(SCTMTestResult.TestState.PASSED);
   }
 
   @Override
   public int getSkipCount() {
     return getXCount(SCTMTestResult.TestState.SKIPPED);
   }
 
   @Override
   public int getFailCount() {
     return getXCount(SCTMTestResult.TestState.FAILED);
   }
 
   @Override
   public float getDuration() {
     float duration = 0;
     for (SCTMTestResult conf : this.getConfigurationResult().values())
       duration += conf.getDuration();
     return duration;
   }
 
   @Override
   public String getDurationString() {
     return Util.getTimeSpanString((long) getDuration());
   }
 
   @Override
   public Collection<? extends TestResult> getChildren() {
     return Collections.unmodifiableCollection(this.childResults);
   }
 
   @Override
   public boolean hasChildren() {
     return this.childResults != null && !this.childResults.isEmpty();
   }
 
   @Override
   public AbstractBuild<?, ?> getOwner() {
     return this.parentAction == null ? null : this.parentAction.owner;
   }
 
   @Override
   public TestObject getParent() {
     return parent;
   }
 
   @Override
   public void setParent(TestObject parent) {
     this.parent = parent;
   }
 
   @Override
   public String getDisplayName() {
     return name;
   }
 
   @Override
   public String getName() {
    return name.replaceAll("/|\\|:|\\x2A|\\x3F|<|>|\\x7c", "_");
   }
 
   @Override
   public TestResult findCorrespondingResult(String id) {
     String myID = safe(getName());
     if (myID.equals(id))
       return this;
     else {
       TestResult testResult = null;
       for (TestResult result : this.childResults) {
         testResult = result.findCorrespondingResult(id);
         if (testResult != null)
           return testResult;
       }
     }
     return null;
   }
 
   @Override
   public int compareTo(SCTMTestSuiteResult o) {
     if (this.name.equals(o.getName())) {
       if (parent != null)
         return ((SCTMTestSuiteResult) parent).compareTo((SCTMTestSuiteResult) o.getParent());
     }
     return -1;
   }
 
   @Override
   public Object getDynamic(String token, StaplerRequest req, StaplerResponse rsp) {
     for (TestResult testResult : this.childResults) {
       if (testResult.getName().equals(token))
         return testResult;
       else
         testResult.getDynamic(token, req, rsp);
     }
     return super.getDynamic(token, req, rsp);
   }
 
   @Override
   public String toString() {
     return String.format("TestSuite [%s]", this.name);
   }
 
   @SuppressWarnings({ "rawtypes", "unchecked" })
   @Override
   public void setParentAction(AbstractTestResultAction action) {
     this.parentAction = action;
     for (TestResult result : this.childResults) {
       result.setParentAction(action);
     }
   }
 
   @Override
   public TestResult getPreviousResult() {
     AbstractBuild<?, ?> build = getOwner();
     if (build == null)
       return null;
 
     do {
       build = build.getPreviousBuild();
       if (build != null) {
         SCTMResultAction action = build.getAction(SCTMResultAction.class);
         if (action != null) {
           TestResult result = action.findCorrespondingResult(this.getId());
           if (result != null)
             return result;
         }
       } else
         return null; // no more builds in the past
     } while (true);
   }
 
   public synchronized void addChild(TestResult test) {
     childResults.add(test);
   }
 
   public synchronized SCTMTestSuiteResult getChildSuiteByName(String name) {
     for (TestResult child : this.childResults) {
       if ((child instanceof SCTMTestSuiteResult) && name.equals(child.getDisplayName()))
         return (SCTMTestSuiteResult) child;
     }
     return null;
   }
 
   public synchronized SCTMTestCaseResult getChildTestByName(String name) {
     for (TestResult child : this.childResults) {
       if ((child instanceof SCTMTestCaseResult) && name.equals(child.getDisplayName()))
         return (SCTMTestCaseResult) child;
     }
     return null;
   }
 
   public SCTMTestResult getTestResultForConfiguration(String configuration) {
     return this.getConfigurationResult().get(configuration);
   }
 
   public Collection<String> getConfigurations() {
     List<String> list = new ArrayList<String>(getConfigurationResult().keySet());
     Collections.sort(list);
     return list;
   }
 }
