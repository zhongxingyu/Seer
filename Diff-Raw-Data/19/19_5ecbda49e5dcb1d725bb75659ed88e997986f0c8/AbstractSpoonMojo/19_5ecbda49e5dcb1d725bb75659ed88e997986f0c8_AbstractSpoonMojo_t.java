 package net.sf.alchim.spoon.contrib.maven;
 
 
 
 import java.io.File;
 import java.util.List;
 
 import net.sf.alchim.spoon.contrib.launcher.Launcher;
 
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.project.MavenProject;
 
 import spoon.processing.FileGenerator;
 import spoon.support.ByteCodeOutputProcessor;
 import spoon.support.JavaOutputProcessor;
 
 /**
  * Apply a set of spoonlet and Spoon's processor.
  */
 public abstract class AbstractSpoonMojo extends AbstractMojo {
     protected boolean compileCode = false;
 
     /**
      * Set the jdk compliance level.
      *
      * @parameter expression="${maven.spoon.compliance}" default-value="5"
      */
     protected int compliance = 5;
 
     /**
      * Set to true to include debugging information in the compiled class files.
      *
      * @parameter expression="${maven.spoon.debug}" default-value="false"
      */
     protected boolean debug;
 
     /**
      * Set to true to show messages about what the compiler is doing.
      *
      * @parameter expression="${maven.spoon.verbose}" default-value="true"
      */
     protected boolean verbose;
 
     /**
      * Set to true to show messages about what the compiler is doing.
      *
      * @parameter expression="${maven.spoon.cfg}" default-value="${basedir}/spoon.cfg.xml"
      */
     protected File cfg;
 
     /**
      * Set to true to stop the build if spoon generated warnings
      *
      * @parameter expression="${maven.spoon.failOnWarning}" default-value="false"
      */
     protected boolean failOnWarning;
 
     /**
      * Set to true to stop the build if spoon generated warnings
      *
      * @parameter expression="${maven.spoon.failOnError}" default-value="true"
      */
     protected boolean failOnError;
 
     /**
      * @parameter expression="${project}"
      * @required
      * @readonly
      */
     protected MavenProject project;
 
     /**
      * The path of the result/log of spoon compiler/analyzer.
      *
      * @parameter expression="${project.build.directory}/spoon-report.csv"
      * @required
      */
     private File reportDataFile;
     
     /**
      * A list of system properties to be passed. 
      *
      * @parameter
      */
     private Property[] systemProperties;
     
     abstract protected List<String> getSourceRoots() throws Exception;
     abstract protected File getSrcOutputDir() throws Exception;
     abstract protected File getClassesOutputDir() throws Exception;
 
     @SuppressWarnings("unchecked")
     protected List<String> getCompileDependencies() throws Exception {
         return project.getCompileClasspathElements();
     }
 
     public void execute() throws MojoExecutionException {
         try {
             executeBasic();
             updateSourceRoots();
         } catch (MojoExecutionException exc) {
             throw exc;
         } catch (Throwable exc) {
             throw new MojoExecutionException("fail to execute", exc);
         }
     }
 
     protected MavenEnvironment newEnvironment() throws Exception {
        MavenEnvironment environment = new MavenEnvironment(project.getBasedir(), getLog(), reportDataFile);
         environment.setComplianceLevel(compliance);
         environment.setVerbose(verbose || debug);
         environment.setDebug(debug);
         File outputdir = getSrcOutputDir();
         if (outputdir != null) {
             // env_.setXmlRootFolder(getArguments().getFile("properties"));
             environment.setDefaultFileGenerator(new JavaOutputProcessor(outputdir));
             if (getClassesOutputDir() != null) {
                 FileGenerator<?> printer = environment.getDefaultFileGenerator();
                 environment.setDefaultFileGenerator(new ByteCodeOutputProcessor((JavaOutputProcessor) printer, getClassesOutputDir()));
             }
         }
         return environment;
     }
 
     protected void updateSourceRoots() throws Exception {
         List<String> l = getSourceRoots();
         if ((getSrcOutputDir() != null) && (l != null)) {
             l.clear();
             l.add(getSrcOutputDir().getAbsolutePath());
         }
     }
 
     private void executeBasic() throws Throwable {
         Launcher launcher = new Launcher();
         MavenEnvironment env = newEnvironment();
         for(Property p:systemProperties)
             System.setProperty(p.getKey(), (p.getValue()!=null)?p.getValue():"");
         launcher.run(cfg, getSourceRoots(), getCompileDependencies(), env);
         if (failOnError && env.hasError()) {
             throw new MojoExecutionException("spoon generate some errors");
         }
         if (failOnWarning && env.hasWarning()) {
             throw new MojoExecutionException("spoon generate some warnings");
         }
     }
 
 }
