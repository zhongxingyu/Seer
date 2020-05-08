 package fi.helsinki.cs.tmc.maven;
 
 import fi.helsinki.cs.tmc.testscanner.TestMethod;
 import fi.helsinki.cs.tmc.testscanner.TestScanner;
 import java.io.IOException;
 import org.apache.maven.artifact.DependencyResolutionRequiredException;
 import org.apache.maven.plugin.MojoExecutionException;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.ResourceBundle;
 import org.apache.commons.lang.StringUtils;
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
 import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
 import org.apache.maven.execution.MavenSession;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.project.MavenProject;
 import org.apache.maven.repository.RepositorySystem;
import org.codehaus.plexus.util.FileUtils;
 import org.codehaus.plexus.util.cli.CommandLineException;
 import org.codehaus.plexus.util.cli.CommandLineUtils;
 import org.codehaus.plexus.util.cli.Commandline;
 import org.codehaus.plexus.util.cli.StreamConsumer;
 
 /**
  * Runs tests using the TMC test runner.
  *
  * @goal test
  * @execute phase=test-compile
  * @requiresDirectInvocation
  * @requiresDependencyResolution test
  */
 public class TmcTestMojo extends AbstractMojo {
 
     /**
      * The maven project.
      *
      * @parameter expression="${project}"
      * @required
      */
     private MavenProject project;
     
     /** 
      * The Maven Session Object 
      * 
      * @parameter expression="${session}" 
      * @required 
      * @readonly 
      */
     private MavenSession session;
     
     /**
      * @parameter expression="${tmcjunitrunner.version}"
      */
     private String tmcJunitRunnerVersion;
     
     /**
      * Output JSON file.
      * @parameter expression="${tmc.test.test_output_file}" default-value="${project.build.directory}/test_output.txt"
      * @required
      */
     private File outputFile;
     
     /**
      * Standard output log file.
      * @parameter expression="${tmc.test.stdout_file}" default-value="${project.build.directory}/stdout.txt"
      * @required
      */
     private File stdoutFile;
     
     /**
      * Error output log file.
      * @parameter expression="${tmc.test.stderr_file}" default-value="${project.build.directory}/stderr.txt"
      * @required
      */
     private File stderrFile;
     
     /**
      * JVM options. Defaults to reading from $MAVEN_OPTS, if set.
      * @parameter expression="${tmc.test.jvm_opts}" default-value="USE_MAVEN_OPTS"
      * @required
      */
     private String jvmOpts;
     
     /**
      * @component
      */
     private RepositorySystem repoSystem;
 
     private ResourceBundle bundle;
 
     public TmcTestMojo() {
         bundle = ResourceBundle.getBundle(this.getClass().getCanonicalName());
     }
     
     private void setParamsFromSession(ArtifactResolutionRequest request)
     {
         request.setLocalRepository(session.getLocalRepository());
         request.setOffline(session.isOffline());
         request.setForceUpdate(session.getRequest().isUpdateSnapshots());
         request.setServers(session.getRequest().getServers());
         request.setMirrors(session.getRequest().getMirrors());
         request.setProxies(session.getRequest().getProxies());
     }
     
     public void execute() throws MojoExecutionException {
         List<String> classPathParts;
         try {
             classPathParts = project.getTestClasspathElements();
         } catch (DependencyResolutionRequiredException ex) {
             throw new MojoExecutionException("Failed to get project classpath", ex);
         }
         String classPath = StringUtils.join(classPathParts, File.pathSeparatorChar);
         
         TestScanner scanner = new TestScanner();
         for (String testSourceDir : project.getTestCompileSourceRoots()) {
             scanner.addSource(new File(testSourceDir));
         }
         scanner.setClassPath(classPath);
         List<TestMethod> cases = scanner.findTests();
         
         classPath = getTestRunnerClassPath() + File.pathSeparatorChar + classPath;
         
         List<String> jvmArgs = new ArrayList<String>();
         List<String> args = new ArrayList<String>();
         
         if (project.getTestCompileSourceRoots().size() != 1) {
             throw new MojoExecutionException("TMC test runner supports exactly one test directory. There are " + project.getTestCompileSourceRoots().size());
         }
         jvmArgs.add("-ea");
         jvmArgs.addAll(getUserJvmOpts());
         jvmArgs.add("-Dtmc.test_class_dir=" + project.getTestCompileSourceRoots().get(0));
         jvmArgs.add("-Dtmc.results_file=" + outputFile.toString());
         
         for (TestMethod tc : cases) {
             args.add(tc.toString());
         }
         
         int exitCode = runInForkedVM(classPath, jvmArgs, "fi.helsinki.cs.tmc.testrunner.Main", args);
         if (exitCode != 0) {
            StringBuilder msg = new StringBuilder();
            msg.append("Failed to run tests. Exit code: ").append(exitCode);
            msg.append("\n");
            try {
                String stdout = FileUtils.fileRead(stdoutFile);
                msg.append("Stdout:\n").append(stdout);
            } catch (IOException e) {
                //Ignore
            }
            try {
                String stderr = FileUtils.fileRead(stderrFile);
                msg.append("Stdout:\n").append(stderr);
            } catch (IOException e) {
                //Ignore
            }
            throw new MojoExecutionException(msg.toString());
         }
     }
     
     private List<String> getUserJvmOpts() {
         ArrayList<String> result = new ArrayList<String>();
         
         String opts = this.jvmOpts;
         if (opts.equals("USE_MAVEN_OPTS")) {
             opts = System.getenv("MAVEN_OPTS");
             if (opts == null) {
                 opts = "";
             }
         }
         
         result.addAll(Arrays.asList(StringUtils.split(opts)));
         return result;
     }
     
     private String getTestRunnerClassPath() throws MojoExecutionException {
         Artifact runnerArt = repoSystem.createArtifact(
                 "fi.helsinki.cs.tmc",
                 "tmc-junit-runner",
                 getTestRunnerVersion(),
                 "jar"
                 );
 
         ArtifactResolutionRequest req = new ArtifactResolutionRequest();
         setParamsFromSession(req);
         req.setArtifact(runnerArt);
         req.setResolveTransitively(true);
 
         ArtifactResolutionResult result = repoSystem.resolve(req);
         if (!result.isSuccess()) {
             String msg = "Failed to resolve " + runnerArt.toString();
             Exception cause = null;
             if (!result.getExceptions().isEmpty()) {
                 cause = result.getExceptions().get(0);
                 msg += ": " + cause.getMessage();
             }
             throw new MojoExecutionException(msg, cause);
         }
         
         List<String> parts = new ArrayList<String>(result.getArtifacts().size());
         for (Artifact art : result.getArtifacts()) {
             parts.add(art.getFile().getPath());
         }
         return StringUtils.join(parts, File.pathSeparatorChar);
     }
     
     private String getTestRunnerVersion() {
         if (tmcJunitRunnerVersion != null) {
             return tmcJunitRunnerVersion;
         } else {
             return bundle.getString("tmcjunitrunner.version");
         }
     }
     
     private int runInForkedVM(String classPath, List<String> jvmArgs, String target, List<String> args) {
         Commandline cli = new Commandline();
         
         cli.setExecutable(getJavaCommand());
         cli.setWorkingDirectory(new File(".").getAbsolutePath());
         try {
             cli.addSystemEnvironment();
         } catch (Exception ex) {
             this.getLog().warn("Failed to use system envvars");
         }
         cli.addEnvironment("CLASSPATH", classPath);
         
         String[] argArray = new String[jvmArgs.size() + 1 + args.size()];
         int i = 0;
         for (String arg : jvmArgs) {
             argArray[i++] = arg;
         }
         argArray[i++] = target;
         for (String arg : args) {
             argArray[i++] = arg;
         }
         cli.addArguments(argArray);
         getLog().debug("TmcTestMojo executing " + cli.toString());
        getLog().debug("Classpath: " + classPath);
         
         OutputWriter stdout = new OutputWriter(stdoutFile);
         OutputWriter stderr = new OutputWriter(stderrFile);
         try {
             return CommandLineUtils.executeCommandLine(cli, stdout, stderr);
         } catch (CommandLineException ex) {
             getLog().error("Failed to run tests: " + ex.getMessage());
             return 127;
         } finally {
             stdout.close();
             stderr.close();
         }
     }
     
     private String getJavaCommand() {
         return System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
     }
     
     private class OutputWriter implements StreamConsumer {
         private Writer writer;
         private boolean errorLogged = false;
         
         public OutputWriter(Writer writer) {
             this.writer = writer;
         }
 
         public OutputWriter(File file) {
             try {
                 this.writer = new FileWriter(file);
             } catch (IOException e) {
                 logError(e);
                 this.writer = new SinkWriter();
             }
         }
         
         public synchronized void consumeLine(String string) {
             try {
                 writer.append(string).append('\n');
                 writer.flush();
             } catch (IOException e) {
                 logError(e);
             }
         }
         
         public void close() {
             try {
                 writer.close();
                 writer = null;
             } catch (IOException e) {
                 logError(e);
             }
         }
         
         private void logError(IOException e) {
             if (!errorLogged) {
                 getLog().error("Failed to write output: " + e.getMessage());
                 errorLogged = true;
             }
         }
     }
     
     
     private static class SinkWriter extends Writer {
         @Override
         public void write(char[] cbuf, int off, int len) throws IOException {
         }
         @Override
         public void flush() throws IOException {
         }
         @Override
         public void close() throws IOException {
         }
     }
 }
