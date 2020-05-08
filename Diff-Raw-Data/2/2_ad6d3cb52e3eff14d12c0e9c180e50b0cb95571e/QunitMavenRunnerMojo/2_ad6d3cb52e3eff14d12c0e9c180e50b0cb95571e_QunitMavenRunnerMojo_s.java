 package com.cj.qunit.mojo;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.maven.plugin.MojoFailureException;
 
 import com.cj.qunit.mojo.QunitMavenRunner.Runner;
 
 /**
  * @phase test
  * @goal test
  */
 public class QunitMavenRunnerMojo extends AbstractQunitMojo {
     
     /**
      * @parameter expression="${qunit.numThreads}"
      */
     public Integer numThreads = 1;
     


     /**
      * @parameter expression="${qunit.runner}" default-value=HTML_UNIT
      */
     public String runner;
     
     public void execute() throws MojoFailureException {
         if(shouldSkipTests()) return;
         
         getLog();
         
         final List<String> filesRun = new ArrayList<String>();
         final QunitMavenRunner.Listener listener = new QunitMavenRunner.Listener() {
             @Override
             public void runningTest(String relativePath) {
                 getLog().info("Running: " + relativePath);
                 filesRun.add(relativePath);
             }
             @Override
             public void debug(String info) {
                 getLog().debug(info);
             }
             @Override
             public void initInfo(String info) {
                 getLog().info(info);
             }
         };
         
         final Runner runner = Runner.valueOf(this.runner.toUpperCase());
         
         final List<String> problems = new QunitMavenRunner(numThreads, runner).run(
                                             webRoot(), 
                                             codePaths(), 
                                             extraPathsToServe(), 
                                             webPathToRequireDotJsConfig(), 
                                             listener, 
                                             returnTimeout());
         
         if(!problems.isEmpty()){
             StringBuffer problemsString = new StringBuffer();
             
             for(String next : problems){
                 problemsString.append(next);
                 problemsString.append('\n');
             }
 
             throw new MojoFailureException(problemsString.toString());
         }else{
             getLog().info("Ran qunit on " + filesRun.size() + " files");
         }
     }
 
     private boolean shouldSkipTests() {
         boolean skipTests = false;
         
         final String[] skipFlags = {"maven.test.skip", "skipTests", "qunit.skip"};
         
         for(String skipFlag : skipFlags){
             String value = System.getProperty(skipFlag);
             if(value!=null && !value.trim().toLowerCase().equals("false")){
                 getLog().warn("###########################################################################");
                 getLog().warn("## Skipping Qunit tests because the \"" + skipFlag + "\" property is set.");
                 getLog().warn("###########################################################################");
                 skipTests = true;
                 break;
             }
         }
         return skipTests;
     }
 }
 
