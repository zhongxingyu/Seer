 package net.sf.aislib.tools.entities.plugin.m2;
 
 import java.io.File;
 
 import net.sf.aislib.tools.entities.library.Processor;
 
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 
 import org.apache.maven.project.MavenProject;
 
 /**
  * Generate class file with constants.
  *
  * @goal generate
  * @phase generate-sources
  * @author pikus
  */
 public class EntitiesGeneratingMojo extends AbstractMojo {
 
   /**
    * @parameter expression="${project.build.directory}/generated-sources/entities"
    */
   private File outputDirectory;
 
   /**
   * @parameter name of source file containig entities.
    * @required
    */
   private File sourceFile;
 
   /**
    * @parameter name of result file name (<tt>.java</tt> extension will be added).
    */
   private String resultName = "Entities";
 
   /**
    * @parameter package name.
    * @required
    */
   private String packageName;
 
   /**
    * @parameter expression="${project}"
    *
    * @required
    */
   private MavenProject project;
 
   public void execute() throws MojoExecutionException, MojoFailureException {
     try {
       Processor processor = new Processor();
       processor.setSource(sourceFile);
       processor.setClassName(resultName);
       processor.setPackageName(packageName);
       processor.setDestinationDirectory(outputDirectory);
       processor.process();
 
       project.addCompileSourceRoot(outputDirectory.getAbsolutePath());
     } catch (Exception e) {
       throw new MojoExecutionException(e.getMessage(), e);
     }
   }
 
   public void setOutputDirectory(File outputDirectory) {
     this.outputDirectory = outputDirectory;
   }
 
   public void setSourceFile(File sourceFile) {
     this.sourceFile = sourceFile;
   }
 
   public void setResultName(String resultName) {
     this.resultName = resultName;
   }
 
   public void setPackageName(String packageName) {
     this.packageName = packageName;
   }
 }
