 package org.dbmigaret4j.migration;
 
 import org.apache.velocity.Template;
 import org.apache.velocity.VelocityContext;
 import org.apache.velocity.app.VelocityEngine;
 import org.apache.velocity.exception.ResourceNotFoundException;
 import org.apache.velocity.runtime.RuntimeConstants;
 import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
 import org.jailsframework.util.FileUtil;
 import org.jailsframework.util.StringUtil;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * @author <a href="mailto:sanjusoftware@gmail.com">Sanjeev Mishra</a>
  * @version $Revision: 0.1
  *          Date: Apr 4, 2010
  *          Time: 12:21:35 AM
  */
 public class MigrationGenerator {
 
     private String migrationPath;
     private String migrationPackage;
 
     public MigrationGenerator(String migrationPath, String migrationPackage) {
         this.migrationPath = migrationPath;
         this.migrationPackage = migrationPackage;
     }
 
     public Long generate(String componentFileName) {
         try {
             Long version = getMigrationVersion();
             String migrationFileNameWithTimeStamp = getMigrationFileNameWithTimeStamp(version, new StringUtil(componentFileName).camelize());
             File migrationFile = new File(migrationPath, migrationFileNameWithTimeStamp.concat(".java"));
             if (!FileUtil.createFile(migrationFile)) {
                throw new RuntimeException("Could not create migration file");
             }
             writeContent(migrationFile, getSubstitutions(version, migrationFileNameWithTimeStamp));
             return version;
         } catch (Exception e) {
             throw new RuntimeException("Could not generate migration : " + e);
         }
     }
 
     private Map<String, String> getSubstitutions(long version, String migrationFileNameWithTimeStamp) {
         Map<String, String> substitutions = new HashMap<String, String>();
         substitutions.put("migrationFileNameWithTimeStamp", migrationFileNameWithTimeStamp);
         if(migrationPackage != null) {
             substitutions.put("package", migrationPackage);
         }
         substitutions.put("version", version + "L");
         return substitutions;
     }
 
     private String getMigrationFileNameWithTimeStamp(long version, String migrationFileName) {
         return "Migration_" + version + "_".concat(migrationFileName);
     }
 
     private long getMigrationVersion() {
         return new Date().getTime();
     }
 
     private void writeContent(File file, Map<String, String> substitutions) throws Exception {
         VelocityEngine velocityEngine = new VelocityEngine();
        velocityEngine.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, "dbmigrate4j/src");
         velocityEngine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
 
         velocityEngine.init();
         Template template;
         try {
             template = velocityEngine.getTemplate("migration.vm");
         } catch (ResourceNotFoundException e) {
             throw new RuntimeException("Please make sure that your template file exists in the classpath : " + e);
         }
         FileWriter fileWriter = new FileWriter(file);
         template.merge(new VelocityContext(substitutions), fileWriter);
         fileWriter.flush();
         fileWriter.close();
     }
 }
