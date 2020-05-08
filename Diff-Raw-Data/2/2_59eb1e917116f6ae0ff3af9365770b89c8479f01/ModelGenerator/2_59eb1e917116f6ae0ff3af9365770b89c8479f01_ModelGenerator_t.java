 package org.jailsframework.generators;
 
 import org.apache.velocity.Template;
 import org.apache.velocity.VelocityContext;
 import org.apache.velocity.app.VelocityEngine;
 import org.apache.velocity.exception.ResourceNotFoundException;
 import org.apache.velocity.runtime.RuntimeConstants;
 import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
 import org.jailsframework.exceptions.JailsException;
 import org.jailsframework.util.FileUtil;
 import org.jailsframework.util.StringUtil;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * @author <a href="mailto:sanjusoftware@gmail.com">Sanjeev Mishra</a>
  * @version $Revision: 0.1
  *          Date: May 19, 2010
  *          Time: 1:41:50 AM
  */
 public class ModelGenerator {
 
     private JailsProject project;
 
     public ModelGenerator(JailsProject project) {
         this.project = project;
     }
 
     public boolean generate(String componentFileName) {
         try {
             String modelName = new StringUtil(componentFileName).camelize();
             File modelFile = new File(project.getModelsPath() + "\\" + modelName + ".java");
             if (!FileUtil.createFile(modelFile)) {
                 throw new JailsException("Could not generate migration");
             }
             return writeContent(modelFile, getSubstitutions(modelName));
         } catch (Exception e) {
             e.printStackTrace();
             return false;
         }
     }
 
     private boolean writeContent(File file, Map<String, String> substitutions) throws Exception {
         VelocityEngine velocityEngine = new VelocityEngine();
        velocityEngine.setProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, "src");
         velocityEngine.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
 
         velocityEngine.init();
         Template template;
         try {
             template = velocityEngine.getTemplate("model.vm");
         } catch (ResourceNotFoundException e) {
             throw new RuntimeException("Please make sure that your template file exists in the classpath : " + e);
         }
         FileWriter fileWriter = new FileWriter(file);
         template.merge(new VelocityContext(substitutions), fileWriter);
         fileWriter.flush();
         fileWriter.close();
         return true;
     }
 
     private Map<String, String> getSubstitutions(String modelName) {
         Map<String, String> substitutions = new HashMap<String, String>();
         substitutions.put("modelName", modelName);
         substitutions.put("package", project.getModelPackage());
         return substitutions;
     }
 
 }
