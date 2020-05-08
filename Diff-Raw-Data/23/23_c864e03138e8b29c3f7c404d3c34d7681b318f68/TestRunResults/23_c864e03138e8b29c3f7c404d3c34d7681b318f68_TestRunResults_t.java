 package water.jenkins.plugins.dashboard.tests;
 
 import hudson.model.Job;
 import hudson.model.Run;
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
   
   private Job job;
   private Run run;
   private String testResultAction;
   
   public TestRunResults(final Run run, final String testResultAction) {
     this.run = run;
     this.job = (run != null) ? run.getParent() : null;
     this.testResultAction = testResultAction;
   }
 
  public List<PackageResult> getPackages() {
    List<PackageResult> result = packageResults;    
    return result;
   }
   
   public void addTestPackage(final PackageResult pack) {
     if (!caseResults.containsKey(pack)) {
      packageResults.add(pack);
       caseResults.put(pack, new ArrayList<CaseResult>());            
     }
   }  
   
   public void addCaseResult(final PackageResult pack, final CaseResult cr) {
     if (!caseResults.containsKey(pack)) {
       addTestPackage(pack);      
     } 
     caseResults.get(pack).add(cr);
   }
   
   public List<CaseResult> getCaseResults(final PackageResult pack) {
    List<CaseResult> result = caseResults.get(pack);
    Collections.sort(caseResults.get(pack));
    
    return result;
   }
   
   public String getURL(final TestResult tr) {
     StringBuffer sb = new StringBuffer();
     sb.append(run.getUrl()).append(testResultAction).append(tr.getUrl());
     
     return sb.toString();
  }    
 }
