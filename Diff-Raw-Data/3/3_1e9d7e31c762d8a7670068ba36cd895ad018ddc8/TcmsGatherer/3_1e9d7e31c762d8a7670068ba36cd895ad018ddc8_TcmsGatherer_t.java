 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package NitrateIntegration;
 
 import NitrateIntegration.CommandWrapper;
 import com.redhat.engineering.jenkins.testparser.Parser;
 import com.redhat.engineering.jenkins.testparser.results.MethodResult;
 import com.redhat.engineering.jenkins.testparser.results.TestResults;
 import com.redhat.nitrate.*;
 import hudson.FilePath;
 import hudson.matrix.Combination;
 import hudson.matrix.MatrixRun;
 import hudson.model.AbstractBuild;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.*;
 import redstone.xmlrpc.XmlRpcStruct;
 
 /**
  *
  * @author asaleh
  */
 public class TcmsGatherer implements Iterable<CommandWrapper> {
 
     PrintStream logger;
     private int run_id;
     private int build_id;
     private TcmsProperties properties;
     CommandWrapper build_s;
     
     LinkedList<CommandWrapper> list = new LinkedList<CommandWrapper>();
     HashMap<TcmsCommand,CommandWrapper> commands = new  HashMap<TcmsCommand,CommandWrapper>();
     
     HashMap<Class<TcmsCommand>,LinkedList<CommandWrapper>> commands_sorted = new HashMap<Class<TcmsCommand>,LinkedList<CommandWrapper>>();
     
     
     public TcmsGatherer(PrintStream logger, TcmsProperties properties) {
         this.logger = logger;
         this.properties = properties;
         this.build_s=null;
     }
 
     private TestCase.create tcmsCreateCase(MethodResult result) {
         TestCase.create create = new TestCase.create();
         create.product = this.properties.getProductID();
         create.category = this.properties.getCategoryID();
         create.priority = this.properties.getPriorityID();
         create.summary = result.getName();
         return create;
     }
 
     private Build.create tcmsCreateBuild(AbstractBuild build) {
         Build.create create = new Build.create();
         create.product = this.properties.getProductID();
         create.name = build.getId();
         create.description = build.getDescription();
         return create;
     }
 
     private TestRun.create tcmsCreateRun(AbstractBuild run) {
         TestRun.create create = new TestRun.create();
         create.product = this.properties.getProductID();
         create.product_version = this.properties.getProduct_vID();
         create.plan = this.properties.getPlanID();
         create.build = -1;
         create.manager = this.properties.getManagerId();
         create.summary = run.getDisplayName();
         if(run instanceof MatrixRun){
             MatrixRun mrun = (MatrixRun) run;
             Combination c= mrun.getProject().getCombination();
             create.summary += c.toString(); 
         }
         return create;
     }
     
     private TestCaseRun.create tcmsCreateCaseRun(int status) {
         TestCaseRun.create c = new TestCaseRun.create();
         c.run = -1;
         c.caseVar = -1;
         c.build = -1;
         c.case_run_status = status;
         return c;
     }
     
     
     private void CreateTestCaseRun(MethodResult result, int status, CommandWrapper run, CommandWrapper build) {
         
         CommandWrapper dependency = null;
         TestCaseRun.create c_case_run = tcmsCreateCaseRun(status);
          
         TestCase.create c_case = tcmsCreateCase(result);
         if(commands.containsKey(c_case)){
             dependency =commands.get(c_case);
         }else{
             dependency = add(c_case,TestCase.class);
         }
         CommandWrapper case_run = add(c_case_run,TestCaseRun.class);
         case_run.addDependecy(dependency);
         case_run.addDependecy(run);
         case_run.addDependecy(build);
     }
 
     private void gatherTestInfo(TestResults results, CommandWrapper run, CommandWrapper build) {
 
         for (MethodResult result : results.getFailedTests()) {
             CreateTestCaseRun(result, TestCaseRun.FAILED, run,build);
         }
         for (MethodResult result : results.getPassedTests()) {
             CreateTestCaseRun(result, TestCaseRun.PASSED, run,build);
         }
         for (MethodResult result : results.getSkippedTests()) {
             CreateTestCaseRun(result, TestCaseRun.WAIVED, run,build);
         }
 
     }
 
     public void gather(FilePath[] paths, AbstractBuild build, AbstractBuild run) throws IOException, InterruptedException {
         
         Parser testParser = new Parser(logger);
 
         TestResults results = testParser.parse(paths, false);
 
         if (results == null) {
             return;
         }
         
         if(build_s==null) build_s = add(tcmsCreateBuild(build),Build.class);
         CommandWrapper run_s =  add(tcmsCreateRun(run),TestRun.class);
         run_s.addDependecy(build_s);
         gatherTestInfo(results, run_s,build_s);
 
     }
 
     public void clear() {
         list.clear();
         commands.clear();
         commands_sorted.clear();
     }
 
     private CommandWrapper add(TcmsCommand current,Class result_class) {
         CommandWrapper script = CommandWrapper.wrap(current,result_class);
         list.add(script);
         commands.put(current,script);
         
         if(commands_sorted.containsKey(current.getClass())==false){
            Class c = current.getClass();
            commands_sorted.put(c, new LinkedList<CommandWrapper>());
         }
         commands_sorted.get(current.getClass()).add(script);
         
         return script;
     }
 
     public Iterator<CommandWrapper> iterator() {
         return list.listIterator();
     }
 
     LinkedList<CommandWrapper> getCommandList(Class<TcmsCommand> c){
         return commands_sorted.get(c);
     }
     Set<Class<TcmsCommand>> getComandClasses(){
         return commands_sorted.keySet();
     }
 
 }
