 package com.thalesgroup.jenkins.plugins.variablesextractor.extractors;
 
 import hudson.EnvVars;
 import hudson.Extension;
 import hudson.FilePath;
 import hudson.FilePath.FileCallable;
 import hudson.model.TaskListener;
 import hudson.model.AbstractBuild;
 import hudson.remoting.VirtualChannel;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
import java.util.ArrayList;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import org.kohsuke.stapler.DataBoundConstructor;
 
 import com.thalesgroup.jenkins.plugins.variablesextractor.Logger;
 import com.thalesgroup.jenkins.plugins.variablesextractor.util.ExtractionException;
 
 public class PropertiesFileExtractor extends Extractor {
 
     private static final long serialVersionUID = 426727170587542760L;
 
     /**********
      * FIELDS *
      **********/
     @Extension
     public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();
 
     private final String propertiesFile;
     private final String restrictedNames;
 
     /***************
      * CONSTRUCTOR *
      ***************/
 
     @DataBoundConstructor
     public PropertiesFileExtractor(String propertiesFile, String restrictedNames) {
         super();
         this.propertiesFile = propertiesFile;
         this.restrictedNames = restrictedNames;
     }
 
     /************
      * OVERRIDE *
      ************/
 
     @SuppressWarnings("serial")
     @Override
     public Map<String, String> extractVariables(AbstractBuild<?, ?> build, TaskListener listener)
             throws ExtractionException
     {
         FilePath workspace = build.getWorkspace();
         EnvVars environment;
         try {
             environment = build.getEnvironment(listener);
         } catch (Exception e1) {
             throw new ExtractionException(e1);
         }
        
        List<String> names = new ArrayList<String>();
        for (String name : restrictedNames.split("\\s*,\\s*")) {
            if (name != null && !"".equals(name.trim())) {
                names.add(name);
            }
        }
         String resolvedPropertiesFile = environment.expand(this.propertiesFile);
 
         FilePath filePath;
         if (new File(resolvedPropertiesFile).isAbsolute()) {
             filePath = new FilePath(workspace.getChannel(), resolvedPropertiesFile);
         } else {
             filePath = workspace.child(resolvedPropertiesFile);
         }
 
         try {
             Logger logger = new Logger(listener.getLogger());
             logger.log("Extracting variables from properties file: " + resolvedPropertiesFile);
             
            Properties properties = filePath.act(new FileCallable<Properties>() {
 
                public Properties invoke(File f, VirtualChannel channel) throws IOException,
                         InterruptedException
                 {
                     FileInputStream is = new FileInputStream(f);
                    Properties props = new Properties();
                    props.load(is);
                     is.close();
                    return props;
                 }
             });
 
             Map<String, String> vars = new LinkedHashMap<String, String>();
 
             for (String propName : properties.stringPropertyNames()) {
                 if (names != null && names.size() != 0) {
                     if (names.contains(propName)) {
                         vars.put(propName, properties.getProperty(propName));
                     }
                 } else {
                     vars.put(propName, properties.getProperty(propName));
                 }
             }
 
             return vars;
 
         } catch (IOException e) {
             throw new ExtractionException("Error reading file: " + resolvedPropertiesFile, e);
         } catch (InterruptedException e) {
             throw new ExtractionException("Error reading file: " + resolvedPropertiesFile, e);
         }
     }
 
     public hudson.model.Descriptor<Extractor> getDescriptor() {
         return DESCRIPTOR;
     }
 
     /***********
      * GETTERS *
      ***********/
 
     public String getPropertiesFile() {
         return propertiesFile;
     }
 
     public String getRestrictedNames() {
         return restrictedNames;
     }
 
     /**************
      * DESCRIPTOR *
      **************/
 
     public static class DescriptorImpl extends Extractor.Descriptor {
 
         private static final long serialVersionUID = -2514421151652686635L;
 
         @Override
         public String getDisplayName() {
             return "Properties File Extractor";
         }
 
     }
 }
