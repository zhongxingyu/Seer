 package com.ning;
 
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.plugin.logging.Log;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Goal which runs lithium php unit tests and fails if they fail.
  *
  * @goal li3-test
  * @phase test
  */
 public class Li3TestMojo extends AbstractMojo
 {
 
     private Log log;
     private Pattern passesPattern;
     private Pattern failsPattern;
     private ProcessBuilderWrapper builderWrapper;
 
      /**
      * Location of the li3 Script.
      *
      * @parameter expression="${li3-test.li3ScriptPath}"
      * @required
      */
     private File li3ScriptPath;
 
     /**
     * Location of the tests to run.
     *
     * @parameter expression="${li3-test.li3TestPath}"
     * @required
     */
    private File li3TestPath;
 
     /**
    * Li3 base directory
    *
    * @parameter expression="${li3-test.li3WorkingDirectory}"
    * @required
    */
   private File li3WorkingDirectory;
 
     public Li3TestMojo()
     {
         this(new ProcessBuilderWrapper());
     }
 
     public Li3TestMojo(ProcessBuilderWrapper builderWrapper)
     {
         log = getLog();
         passesPattern = Pattern.compile("(\\d+) / (\\d+) pass(es)?");
         failsPattern = Pattern.compile("(\\d+) fails? and (\\d+) exceptions?");
         this.builderWrapper = builderWrapper;
     }
 
     public void execute() throws MojoExecutionException, MojoFailureException
     {
         boolean failed = false;
         List<String> command = new ArrayList<String>();
 
         try {
             builderWrapper.runWith(li3ScriptPath.getCanonicalPath(), li3TestPath.getCanonicalPath(), li3WorkingDirectory);
             BufferedReader input = new BufferedReader(new InputStreamReader(builderWrapper.getInputStream()));
             BufferedReader error = new BufferedReader(new InputStreamReader(builderWrapper.getErrorStream()));
             String line;
             while ((line = input.readLine()) != null) {
                 log.info(line);
                 if (isFailure(line)) {
                     failed = true;
                 }
             }
             input.close();
             while ((line = error.readLine()) != null) {
                 log.error(line);
                 //If we see anything in the error log at all, fail.
                 failed = true;
             }
             error.close();
             builderWrapper.waitFor();
         }
         catch (IOException e) {
             throw new MojoExecutionException("Couldn't get li3 test process to start.");
         }
         catch (InterruptedException e) {
             throw new MojoExecutionException("Problem while waiting for li3 test process.");
         }
         if (failed) {
             throw new MojoFailureException("Lithium Tests Failed");
         }
     }
 
     private boolean isFailure(String line)
     {
         List<String> failureStrings = new ArrayList<String>();
         failureStrings.add("Fatal error:");
         for (String failureString : failureStrings) {
             if (line.contains(failureString)) {
                 return true;
             }
         }
         Matcher passesMatcher = passesPattern.matcher(line);
         if (passesMatcher.find()) {
             int first = Integer.parseInt(passesMatcher.group(1));
             int second = Integer.parseInt(passesMatcher.group(2));
             if (first != second) {
                 return true;
             }
         }
         Matcher failsMatcher = failsPattern.matcher(line);
         if (failsMatcher.find()) {
             int fails = Integer.parseInt(failsMatcher.group(1));
             int exceptions = Integer.parseInt(failsMatcher.group(2));
             if (fails != 0 || exceptions != 0) {
                 return true;
             }
         }
         return false;
     }
 
     public void setLi3ScriptPath(File li3ScriptPath){
         this.li3ScriptPath = li3ScriptPath;
     }
 
     public void setLi3TestPath(File li3TestPath){
         this.li3TestPath = li3TestPath;
     }
 
     public void setLi3WorkingDirectory(File li3WorkingDirectory)
     {
         this.li3WorkingDirectory = li3WorkingDirectory;
     }
 }
