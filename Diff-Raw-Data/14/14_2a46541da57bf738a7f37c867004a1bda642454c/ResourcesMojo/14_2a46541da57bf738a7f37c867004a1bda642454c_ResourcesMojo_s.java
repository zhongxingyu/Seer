 package edu.northwestern.bioinformatics.bering.maven;
 
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.model.Resource;
 
 /**
  * @author Rhett Sutphin
  *
  * @goal resources
  * @phase generate-resources
  */
 public class ResourcesMojo extends AbstractBeringMojo {
     /**
      * The base path to which the release directories and migration scripts will
      * be copied.
      *
      * @parameter expression="db/migrate"
      */
     private String targetPath;
 
     @Override
     protected void executeInternal() throws MojoExecutionException, MojoFailureException {
         Resource resource = new Resource();
         resource.setTargetPath(getTargetPath());
         resource.setDirectory(getMigrationsDir());
        
         getProject().addResource(resource);
     }
 
     ////// CONFIGURATION
 
     public String getTargetPath() {
         return targetPath;
     }
 
     public void setTargetPath(String targetPath) {
         this.targetPath = targetPath;
     }
 }
