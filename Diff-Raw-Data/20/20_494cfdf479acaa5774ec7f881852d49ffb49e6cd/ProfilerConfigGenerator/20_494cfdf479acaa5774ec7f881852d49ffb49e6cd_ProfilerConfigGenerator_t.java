 package org.sonar.plugins.profiler;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang.StringUtils;
 import org.sonar.api.batch.Phase;
 import org.sonar.api.batch.Sensor;
 import org.sonar.api.batch.SensorContext;
 import org.sonar.api.resources.Project;
 import org.sonar.api.resources.ProjectFileSystem;
 import org.sonar.api.utils.SonarException;
 
 import java.io.File;
 import java.io.IOException;
 
 /**
  * @author Evgeny Mandrikov
  */
 @Phase(name = Phase.Name.PRE)
 public class ProfilerConfigGenerator implements Sensor {
   public void analyse(Project project, SensorContext context) {
     ProjectFileSystem fileSystem = project.getFileSystem();
     File template = new File(fileSystem.getBasedir(), "/profiler-template.xml");
     File config = new File(fileSystem.getBuildDir(), "/profiler/config.xml");
     String license = getLicense(project);
     try {
       generateConfig(template, license, config);
     } catch (Exception e) {
       throw new SonarException(e);
     }
   }
 
   public boolean shouldExecuteOnProject(Project project) {
    // TODO template should exist
     return isLicenseDefined(project);
   }
 
   public void generateConfig(File template, String license, File config) throws IOException {
     FileUtils.writeStringToFile(config, insertLicense(FileUtils.readFileToString(template), license));
   }
 
   protected String insertLicense(String config, String license) {
     return StringUtils.replace(config, "@LICENSE@", license);
   }
 
   @Override
   public String toString() {
     return getClass().getSimpleName();
   }
 
   protected static String getLicense(Project project) {
     return project.getConfiguration().getString(ProfilerPlugin.LICENSE_PROPERTY);
   }
 
   protected static boolean isLicenseDefined(Project project) {
     return !StringUtils.isBlank(getLicense(project));
   }
 }
