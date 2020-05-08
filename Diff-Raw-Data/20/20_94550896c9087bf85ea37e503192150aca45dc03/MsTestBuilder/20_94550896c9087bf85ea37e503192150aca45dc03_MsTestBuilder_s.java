 package org.jenkinsci.plugins;
 
 import hudson.*;
 import hudson.model.AbstractBuild;
 import hudson.model.BuildListener;
 import hudson.model.Computer;
 import hudson.model.Descriptor;
 import hudson.tasks.Builder;
 import hudson.tools.ToolInstallation;
 import hudson.util.ArgumentListBuilder;
 import org.kohsuke.stapler.DataBoundConstructor;
 
 import java.io.IOException;
 import java.util.StringTokenizer;
 
 /**
  * @author Ido Ran
  */
 public class MsTestBuilder extends Builder {
 
     /**
      * GUI fields
      */
     private final String msTestName;
     private final String testFiles;
     private final String categories;
     private final String resultFile;
     private final String cmdLineArgs;
 
     /**
      * When this builder is created in the project configuration step,
      * the builder object will be created from the strings below.
      *
      * @param msTestName The MSTest logical name
      * @param testFiles The path of the test files
      * @param cmdLineArgs Whitespace separated list of command line arguments
      */
     @DataBoundConstructor
     @SuppressWarnings("unused")
     public MsTestBuilder(String msTestName, String testFiles,
             String categories, String resultFile, String cmdLineArgs) {
         this.msTestName = msTestName;
         this.testFiles = testFiles;
         this.categories = categories;
         this.resultFile = resultFile;
         this.cmdLineArgs = cmdLineArgs;
     }
 
     @SuppressWarnings("unused")
     public String getCmdLineArgs() {
         return cmdLineArgs;
     }
 
     @SuppressWarnings("unused")
     public String getTestFiles() {
         return testFiles;
     }
 
     @SuppressWarnings("unused")
     public String getCategories() {
         return categories;
     }
 
     @SuppressWarnings("unused")
     public String getResultFile() {
         return resultFile;
     }
 
     @SuppressWarnings("unused")
     public String getMsTestName() {
         return msTestName;
     }
 
     public MsTestInstallation getMsTest() {
         for (MsTestInstallation i : DESCRIPTOR.getInstallations()) {
             if (msTestName != null && i.getName().equals(msTestName)) {
                 return i;
             }
         }
         return null;
     }
 
     @Override
     public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
         ArgumentListBuilder args = new ArgumentListBuilder();
 
         String execName = "mstest.exe";
         MsTestInstallation installation = getMsTest();
         if (installation == null) {
             listener.getLogger().println("Path To MSTest.exe: " + execName);
             args.add(execName);
         } else {
             EnvVars env = build.getEnvironment(listener);
             installation = installation.forNode(Computer.currentComputer().getNode(), listener);
             installation = installation.forEnvironment(env);
             String pathToMsTest = installation.getHome();
             FilePath exec = new FilePath(launcher.getChannel(), pathToMsTest);
             try {
                 if (!exec.exists()) {
                     listener.fatalError(pathToMsTest + " doesn't exist");
                     return false;
                 }
             } catch (IOException e) {
                 listener.fatalError("Failed checking for existence of " + pathToMsTest);
                 return false;
             }
             listener.getLogger().println("Path To MSTest.exe: " + pathToMsTest);
             args.add(pathToMsTest);
 
             if (installation.getDefaultArgs() != null) {
                 args.addTokenized(installation.getDefaultArgs());
             }
         }
         
         if (resultFile == null || resultFile.trim().length() == 0) {
             listener.fatalError("Result file name was not specified");
             return false;
         }
         
         // Delete old result file
        FilePath resultFilePath = new FilePath(launcher.getChannel(), resultFile);
        if (resultFilePath.exists()) {
             listener.getLogger().println("Delete old result file " + resultFilePath.toURI().toString());
             try {
                 resultFilePath.delete();
             } catch (IOException ex) {
                 ex.printStackTrace(listener.fatalError("Fail to delete old result file"));
                 return false;
             } catch (InterruptedException ex) {
                 ex.printStackTrace(listener.fatalError("Fail to delete old result file"));
                 return false;
             }
         }
         
         args.add("/resultsfile:" + resultFile);
         
         // Always use noisolation flag
         args.add("/noisolation");
 
         EnvVars env = build.getEnvironment(listener);
         String normalizedArgs = cmdLineArgs.replaceAll("[\t\r\n]+", " ");
         normalizedArgs = Util.replaceMacro(normalizedArgs, env);
         normalizedArgs = Util.replaceMacro(normalizedArgs, build.getBuildVariables());
         if (normalizedArgs.trim().length() > 0) {
             args.addTokenized(normalizedArgs);
         }
 
        // TODO: How to add build variables?
         //args.addKeyValuePairs("/P:", build.getBuildVariables());
 
         if (categories != null && categories.trim().length() > 0) {
             args.add("/category:" + categories.trim());
         }
 
         // if no test files are specified fail the build.
         if (testFiles == null || testFiles.trim().length() == 0) {
             listener.fatalError("No test files are specified");
             return false;
         }
 
         StringTokenizer testFilesToknzr = new StringTokenizer(testFiles, " \t\r\n");
         while (testFilesToknzr.hasMoreTokens()) {
             String testFile = testFilesToknzr.nextToken();
             testFile = Util.replaceMacro(testFile, env);
             testFile = Util.replaceMacro(testFile, build.getBuildVariables());
 
             if (testFile.length() > 0) {
                 args.add("/testcontainer:" + testFile);
             }
         }
 
         if (!launcher.isUnix()) {
             args.prepend("cmd.exe", "/C");
             args.add("&&", "exit", "%%ERRORLEVEL%%");
         }
 
        listener.getLogger().println("Executing command: " + args.toStringWithQuote());
         try {
             int r = launcher.launch().cmds(args).envs(env).stdout(listener).pwd(build.getModuleRoot()).join();
             return r == 0;
         } catch (IOException e) {
             Util.displayIOException(e, listener);
             e.printStackTrace(listener.fatalError("MSTest command execution failed"));
             return false;
         }
     }
 
     @Override
     public Descriptor<Builder> getDescriptor() {
         return DESCRIPTOR;
     }
     /**
      * Descriptor should be singleton.
      */
     @Extension
     public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();
 
     public static final class DescriptorImpl extends Descriptor<Builder> {
 
         @CopyOnWrite
         private volatile MsTestInstallation[] installations = new MsTestInstallation[0];
 
         DescriptorImpl() {
             super(MsTestBuilder.class);
             load();
         }
 
         public String getDisplayName() {
             return "Run unit tests with MSTest";
         }
 
         public MsTestInstallation[] getInstallations() {
             return installations;
         }
 
         public void setInstallations(MsTestInstallation... antInstallations) {
             this.installations = antInstallations;
             save();
         }
 
         /**
          * Obtains the {@link MsTestInstallation.DescriptorImpl} instance.
          */
         public MsTestInstallation.DescriptorImpl getToolDescriptor() {
             return ToolInstallation.all().get(MsTestInstallation.DescriptorImpl.class);
         }
     }
 }
