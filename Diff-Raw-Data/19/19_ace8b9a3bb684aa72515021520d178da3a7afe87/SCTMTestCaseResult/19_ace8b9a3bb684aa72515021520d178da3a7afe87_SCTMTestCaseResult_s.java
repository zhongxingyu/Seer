 package hudson.plugins.sctmexecutor.publisher;
 
 import hudson.Util;
 import hudson.model.AbstractBuild;
 import hudson.tasks.test.AbstractTestResultAction;
 import hudson.tasks.test.TestObject;
 import hudson.tasks.test.TestResult;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public final class SCTMTestCaseResult extends TestResult implements Comparable<SCTMTestCaseResult> {
   private static final long serialVersionUID = 120696130376606962L;
 
   private TestObject parent;
   private final String name;
   private final Map<String, SCTMTestResult> configurationResults;
 
   private transient AbstractTestResultAction<SCTMResultAction> parentAction;
 
   public SCTMTestCaseResult(String name) {
     this(name, new HashMap<String, SCTMTestResult>());
   }
 
   SCTMTestCaseResult(String name, Map<String, SCTMTestResult> configurationResults) {
     this.name = name;
     this.configurationResults = configurationResults;
   }
 
   private int getXCount(SCTMTestResult.TestState state) {
     int count = 0;
     for (SCTMTestResult result : this.configurationResults.values()) {
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
 
   Map<String, SCTMTestResult> getConfigurationResult() {
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
   public String getDisplayName() {
     return getName();
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
   public String getName() {
    return name.replaceAll("/|\\|:|\\x2A|\\x3F|<|>|\\x7c", "_");
   }
 
   @Override
   public float getDuration() {
     float duration = 0f;
     for (SCTMTestResult result : this.configurationResults.values()) {
       duration += result.getDuration();
     }
     return duration;
   }
 
   @Override
   public String getDurationString() {
     return Util.getTimeSpanString((long) getDuration());
   }
 
   @Override
   public TestResult findCorrespondingResult(String id) {
     if (id.equals(safe(getName())))
       return this;
     return null;
   }
 
   @Override
   public int compareTo(SCTMTestCaseResult o) {
     if (this.name.equals(o.getName())) {
       if (parent != null)
         return ((SCTMTestSuiteResult) parent).compareTo((SCTMTestSuiteResult) o.getParent());
     }
     return -1;
   }
 
   @Override
   public String toString() {
     return String.format("TestCase [%s]", this.name);
   }
 
   @SuppressWarnings({ "rawtypes", "unchecked" })
   @Override
   public void setParentAction(AbstractTestResultAction action) {
     this.parentAction = action;
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
 
   public void setParent(SCTMTestSuiteResult parent) {
     this.parent = parent;
   }
 
   public void addConfigurationResult(String configuration, SCTMTestResult testResult) {
     this.configurationResults.put(configuration, testResult);
   }
 
   public SCTMTestResult getTestResultForConfiguration(String config) {
     return this.configurationResults.get(config);
   }
 
   public Collection<String> getConfigurations() {
     List<String> list = new ArrayList<String>(this.configurationResults.keySet());
     Collections.sort(list);
     return list;
   }
 }
