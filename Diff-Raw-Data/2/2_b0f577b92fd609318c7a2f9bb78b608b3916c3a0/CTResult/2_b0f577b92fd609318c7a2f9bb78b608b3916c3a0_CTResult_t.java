 
 package se.nctrl.jenkins.plugin;
 
 import hudson.model.AbstractBuild;
 import hudson.model.Result;
 import hudson.tasks.test.TestObject;
 import hudson.tasks.test.TestResult;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  *
  * @author karl
  */
 public class CTResult  extends TestResult{
 
  
    
     private static final long serialVersionUID = 1L;
     
  
     
     
     private AbstractBuild<?, ?> builder;
     private TestObject parent;
     private Collection<CTResult> children;
     
   
     private int cases;
     private String user;
     private String host;
     private String hosts;
     private String lan;
     private String emulator_vsn;
     private String emulator;
     private String otp_release;
     private Date started;
     private String case_name;
     private String log_file;
     private Date ended;
     private int result;
     private String result_msg;
     private float elapsed;
     private String group_time;
     private Date finished;
     private int failed;
     private int successful;
     private int user_skipped;
     private int auto_skipped;
     private String group_props;
     private String node_start;
     private String node_stop;
 
    
     
     
     
     
     public AbstractBuild<?, ?> getBuilder() {
         return builder;
     }
 
     public void setBuilder(AbstractBuild<?, ?> builder) {
         this.builder = builder;
     }
 
     public Collection<CTResult> getChildren() {
         return children;
     }
 
     public void setChildren(Collection<CTResult> children) {
         this.children = children;
     }
 
 
     
     //------------------------------------------------------------------------
     //
     // Fields parsed from log file, getters & setters
     //
     //------------------------------------------------------------------------
     
     
     public int getCases() {
          return cases;
     }
 
     public void setCases(int cases) {
         this.cases = cases;
     }
 
     public String getUser() {
         return user;
     }
 
     public void setUser(String user) {
         this.user = user;
     }
 
     public String getHost() {
         return host;
     }
 
     public void setHost(String host) {
         this.host = host;
     }
 
     public String getHosts() {
         return hosts;
     }
 
     public void setHosts(String hosts) {
         this.hosts = hosts;
     }
 
     public String getLan() {
         return lan;
     }
 
     public void setLan(String lan) {
         this.lan = lan;
     }
 
     public String getEmulator_vsn() {
         return emulator_vsn;
     }
 
     public void setEmulator_vsn(String emulator_vsn) {
         this.emulator_vsn = emulator_vsn;
     }
 
     public String getEmulator() {
         return emulator;
     }
 
     public void setEmulator(String emulator) {
         this.emulator = emulator;
     }
 
     public String getOtp_release() {
         return otp_release;
     }
 
     public void setOtp_release(String otp_release) {
         this.otp_release = otp_release;
     }
 
     public Date getStarted() {
         return started;
     }
 
     public void setStarted(Date started) {
         this.started = started;
     }
 
     public String getCase_name() {
         return case_name;
     }
 
     public void setCase_name(String case_name) {
         this.case_name = case_name;
     }
 
     public String getLog_file() {
         return log_file;
     }
 
     public void setLog_file(String log_file) {
         this.log_file = log_file;
     }
 
     public Date getEnded() {
         return ended;
     }
 
     public void setEnded(Date ended) {
         this.ended = ended;
     }
 
  
     public void setResult(int result) {
         this.result = result;
     }
 
     public String getResult_msg() {
         return result_msg;
     }
 
     public void setResult_msg(String result_msg) {
         this.result_msg = result_msg;
     }
 
     public float getElapsed() {
         return elapsed;
     }
 
     public void setElapsed(float elapsed) {
         this.elapsed = elapsed;
     }
 
     public String getGroup_time() {
         return group_time;
     }
 
     public void setGroup_time(String group_time) {
         this.group_time = group_time;
     }
 
     public Date getFinished() {
         return finished;
     }
 
     public void setFinished(Date finished) {
         this.finished = finished;
     }
 
    public int getFailed() {
       return failed;
     }
 
     public void setFailed(int failed) {
         this.failed = failed;
     }
 
    
     public int getSuccessful() {
         return successful;
 
     }
 
     public void setSuccessful(int successful) {
         this.successful = successful;
     }
 
     public int getUser_skipped() {
         return user_skipped;
 
     }
 
     public void setUser_skipped(int user_skipped) {
         this.user_skipped = user_skipped;
     }
 
     public int getAuto_skipped() {
         return auto_skipped;
 
     }
 
     public void setAuto_skipped(int auto_skipped) {
         this.auto_skipped = auto_skipped;
     }
 
     public String getGroup_props() {
         return group_props;
     }
 
     public void setGroup_props(String group_props) {
         this.group_props = group_props;
     }
 
     public String getNode_start() {
         return node_start;
     }
 
     public void setNode_start(String node_start) {
         this.node_start = node_start;
     }
      
    public String getNode_stop() {
        return node_stop;
     }
 
     public void setNode_stop(String node_stop) {
         this.node_stop = node_stop;
     }  
     //------------------------------------------------------------------------
     
     
     public int getResult() {
         if (hasChildren()) {
             int totalPassed = 0;
             int totalSkipped = 0;
             int totalFailed = 0;
 
             for (CTResult r : this.children) {
                 switch (r.getResult()) {
                     case 0:
                         totalFailed = totalFailed + 1;
                         break;
                     case 1:
                         totalPassed = totalPassed + 1;
                         break;
                     case 2:
                         totalSkipped = totalSkipped + 1;
                         break;
                     default:
                         break;
                 }
 
             }
 
             if (totalFailed > 0) {
                 return 0; // failed
             } else if (totalFailed == 0 && totalSkipped > 0 && totalPassed == 0) {
                 return 2;
             } // skipped
             else if (totalFailed == 0 && totalSkipped == 0 && totalPassed > 0) {
                 return 1;
             } // passed
             else {
                 return -1;
             } // error / unknown
 
         } else {
             return result;
         }
 
 
     }
 
     
     
       /**
      * The total number of test cases including results from children
      * @return the number of test cases
      */
     public int getTotalCases() {
         if (hasChildren()) {
             int totalCases = 0;
             for (CTResult r : this.children)
             {
                 totalCases = totalCases + r.getTotalCases();
             }
             return totalCases;
         } 
         else { return 1; } // no children
     }
 
  
     // recursively count a certain result
     private int countTotalResult(int res) {
         if (hasChildren()) {
             int total = 0;
             for (CTResult r : this.children)
             {
                 total = total + r.countTotalResult(res);
             }
             return total;
         } 
         else { return (result==res ? 1 : 0); } // no children
     
     }
     /**
      * The total number of failed test cases including results from children
      * @return the number of failed test cases
      */
     public int getTotalFailedCases() {
         if (hasChildren()) {
             int totalFailedCases = 0;
             for (CTResult r : this.children)
             {
                 totalFailedCases = totalFailedCases + r.getTotalFailedCases();
             }
             return totalFailedCases;
         } 
         else { return (result==0 ? 1 : 0); } // no children
     }
     
   
      /**
      * The total number of passed test cases including results from children
      * @return the number of passed test cases
      */
     public int getTotalPassedCases() {
         if (hasChildren()) {
             int totalPassedCases = 0;
             for (CTResult r : this.children)
             {
                 totalPassedCases = totalPassedCases + r.getTotalPassedCases();
             }
             return totalPassedCases;
         } 
         else { return (result==1 ? 1 : 0); } // no children
     }
  
         /**
      * The total number of skipped test cases, including results from children
      * @return the number of skipped test cases
      */
     public int getTotalSkippedCases() {
         if (hasChildren()) {
             int totalSkippedCases = 0;
             for (CTResult r : this.children)
             {
                 totalSkippedCases = totalSkippedCases + r.getTotalSkippedCases();
             }
             return totalSkippedCases;
         } 
         else { return (result==2 ? 1 : 0); } // no children
     }
  
     
     
     
     private boolean hasChildren() {
         if (this.children != null) {
             return !this.children.isEmpty();
         } else {
             return false;
         }
     }
   
     public void addChild(CTResult child)
     {
         if (this.children == null) {
             this.children = new ArrayList<CTResult>();
         }
         
         this.children.add(child);
         child.setParent(this);
     }
   
     
     private CTResult() {
         
         this.builder = null; // TODO : set this?
         this.children = null;
         
     }
 
      public CTResult(AbstractBuild<?, ?> builder) {
         
         this.builder = builder;
         this.children = null;
         
     }  
     
     @Override
     public AbstractBuild<?, ?> getOwner() {
         
         return this.builder;
         
     }
 
     @Override
     public TestObject getParent() {
         return this.parent;
     }
 
     @Override
     public  void setParent(TestObject parent) {
         this.parent = parent;
     }
 
     
     @Override
     public TestResult findCorrespondingResult(String string) {
         return null;
     }
 
     public String getDisplayName() {
         if (case_name != null) { return case_name; } else {
             if (this.hasChildren()) {
                 String displayName = "CT Suite";
                 if (this.started != null) {
                     displayName += " @ " + this.started.toString();
                 }
                 return displayName;
             } else {
                 return "Unknown";
             }
         } 
     
     }
 
     private String getSuiteName()
     {
         if (case_name != null) { 
             String suite_name = this.case_name.split(":")[0];
             if (suite_name.length() > 0 ) {
                 return suite_name;
             } else {
                 return "Unknown";
             }
         } else {
             return "Unknown";
         }
     }
     
     
 public Map<String, Collection<CTResult>> getSuites()
 {
     Map<String,Collection<CTResult>> suites = new HashMap<String,Collection<CTResult>>();
     
     if (hasChildren())
     {
         
         for(CTResult r : this.children) {
             
             Map<String,Collection<CTResult>> c_suites = r.getSuites();
             
             for ( Map.Entry<String,Collection<CTResult>> e : r.getSuites().entrySet())
             {
                 Collection<CTResult> s_val = suites.get(e.getKey());
                 if ( s_val != null ) {
                     
                     s_val.addAll(e.getValue());
                     
                 } else {
                 
                     suites.put(e.getKey(), e.getValue());
                 
                 }
             }
             
         }
         
     } else {
        
         ArrayList<CTResult> l = new ArrayList<CTResult>();
         l.add(this);
         suites.put(getSuiteName(), l);
         
     }
     return suites;
 }
     
     
 private Collection<? extends TestResult> filterChildrenByResult( int result)
 {
     ArrayList fa = new ArrayList<CTResult>();
     if (hasChildren())
     {
        
         for(CTResult r : this.children) {
             if (r.getResult() == result) { fa.add(r);}
         }
         
     }
     return fa;
 }
     
 
 //     @Override
 //    public String annotate(String text) {
 //        return "";
 //    }
 
     @Override
     public Result getBuildResult() {
         switch (this.getResult()) {
             case 0:
                 return Result.UNSTABLE;
             case 1:
                 return Result.SUCCESS;
             case 2:
                 return Result.NOT_BUILT;
             default:
                 return Result.FAILURE;
         }
     }
 
     @Override
     public float getDuration() {
         return this.elapsed;
 
     }
 
     @Override
     public String getErrorDetails() {
         if (this.result == 0) {
             return this.result_msg;
         } else {
             return "";
         }
     }
 
     @Override
     public String getErrorStackTrace() {
         return "";
     }
 
     @Override
     public int getFailCount() {
         return this.getTotalFailedCases();
     }
 
     /*
     @Override
     public int getFailedSince() {
         return 0;
     }
 */
     //@Override
     //public  Run<?,?>	getFailedSinceRun()  {return null;}
     
     @Override
     public Collection<? extends TestResult> getFailedTests() {
         return filterChildrenByResult(0);
     }
 
     //@Override
     //public AbstractTestResultAction	getParentAction()  {}
 
     @Override
     public int getPassCount() {
         return this.getTotalPassedCases();
     }
 
     @Override
     public Collection<? extends TestResult> getPassedTests() {
         return filterChildrenByResult(1);
 
     }
 
     @Override
     public TestResult getPreviousResult() {
         return (TestResult)this.builder.getPreviousBuild().getAction(CTResultAction.class).getResult();
     }
 
     @Override
     public int getSkipCount() {
         
         return this.getTotalSkippedCases();
     }
 
     @Override
     public int getTotalCount() {
         
         return this.getTotalCases();
     }
     
     @Override
     public Collection<? extends TestResult> getSkippedTests() {
         return filterChildrenByResult(2);
     }
 
     @Override
     public String getStderr() {
         return "stderr";
     }
 
     @Override
     public String getStdout() {
         return "stdout";
     }
 
     @Override
     public String getTitle() {
         return "title";
     }
 
     @Override
     public boolean isPassed() {
         return this.getResult() == 1;
     }
  
     //@Override
     //public void	setParentAction(AbstractTestResultAction action)  {}
 
     //@Override
     //public void	tally()  {}
     
     
     @Override
     public String getName()
     { return this.case_name;}
     
     
     
 }
