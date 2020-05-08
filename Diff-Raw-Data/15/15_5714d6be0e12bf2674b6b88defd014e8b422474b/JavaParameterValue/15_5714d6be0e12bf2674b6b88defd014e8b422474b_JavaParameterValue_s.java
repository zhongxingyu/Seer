 package com.datalex.jdkparameter;
 
 import hudson.EnvVars;
 import hudson.model.AbstractBuild;
 import hudson.model.ParameterValue;
 import hudson.tasks.BuildWrapper;
 import org.kohsuke.stapler.DataBoundConstructor;
 
 import java.util.List;
 import java.util.logging.Logger;
 
 /**
  * Created with IntelliJ IDEA.
  * User: barisbatiege
  * Date: 6/14/13
  * Time: 11:28 AM
  * To change this template use File | Settings | File Templates.
  */
 public class JavaParameterValue extends ParameterValue {
 
     private static final Logger LOGGER = Logger.getLogger(JavaParameterValue.class.getName());
 
     private String selectedJDK;
 
     @DataBoundConstructor
     public JavaParameterValue(String name, String description, String selectedJDK){
         super(name, description);
         this.selectedJDK = selectedJDK;
     }
 
     public String getSelectedJDK() {
         return selectedJDK;
     }
 
     public void setSelectedJDK(String selectedJDK) {
         this.selectedJDK = selectedJDK;
     }
 
     @Override
     public void buildEnvVars(AbstractBuild<?, ?> build, EnvVars env){
         String JAVA_HOME="xxxx";
 
     }
 
     @Override
     public BuildWrapper createBuildWrapper(AbstractBuild<?,?> build) {
         //this is where all the actions should be performed
         return null;
     }
 
 }
 
