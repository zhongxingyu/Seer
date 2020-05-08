 package water.jenkins.plugins.dashboard.tests;
 
 import hudson.model.Job;
 import hudson.model.Run;
 import hudson.model.Run.Artifact;
 import hudson.tasks.junit.PackageResult;
 import hudson.tasks.junit.CaseResult;
 import hudson.tasks.test.TestResult;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 
 public class TestRunResults {
   
   public static final TestRunResults EMPTY_RESULT = new TestRunResults(null, null);
   
   private ArrayList<PackageResult> packageResults = new ArrayList<PackageResult>();
   private HashMap<PackageResult, ArrayList<CaseResult>> caseResults = new HashMap<PackageResult, ArrayList<CaseResult>>(); 
   
   final private Job job;
   final private Run run;
   final private String testResultAction;
 
   private int failCount;
   private int passCount;
   
   public TestRunResults(final Run run, final String testResultAction) {
     this.run = run;
     this.job = (run != null) ? run.getParent() : null;
     this.testResultAction = testResultAction;
     this.passCount = 0;
     this.failCount = 0;
   }
 
   public List<PackageResult> getPackages() {
     List<PackageResult> result = packageResults;    
     return result;
   }
   
   public void addTestPackage(final PackageResult pack) {
     // This is a hack to skip wrong package name
     if (skipPackage(pack)) return;
     if (!caseResults.containsKey(pack)) {
       packageResults.add(pack);
       caseResults.put(pack, new ArrayList<CaseResult>());      
     }
   }  
   
   public void addCaseResult(final PackageResult pack, final CaseResult cr) {
     // This is a hack to skip wrong package name
     if (skipPackage(pack)) return;    
     if (!caseResults.containsKey(pack)) {
       addTestPackage(pack);      
     } 
     caseResults.get(pack).add(cr);
     if (cr.isPassed()) 
       passCount++;
     else
       failCount++;
     
   }
   
   private boolean skipPackage(final PackageResult pack) {
     return pack.getName().startsWith("<nose");
   }
   
   public List<CaseResult> getCaseResults(final PackageResult pack) {
     List<CaseResult> result = caseResults.get(pack);
     Collections.sort(caseResults.get(pack));
     
     return result;
   }
   
   public String getURL(final TestResult tr) {
     StringBuffer sb = new StringBuffer();
     sb.append(tr.getOwner().getUrl()).append(testResultAction).append(tr.getUrl());
     return sb.toString();
   }   
   
   public Run getRun() {
     return this.run;
   }
   
   public Job getJob() {
     return this.job;
   }
   
   public int getFailCount() {
     return this.failCount;    
   }
   
   public int getPassCount() {
     return this.passCount;
   }
   
   public int getMaxNumBuilds(int max) {
    return run != null ? Math.min(run.getNumber(), max) : max;
   }
   
   public String getRunArtifactURL(final Run run) {
     List<Artifact> ars = run.getArtifactsUpTo(1);
     
     return ars.size() == 1 ? ars.get(0).getHref() : "";
   }
 }
