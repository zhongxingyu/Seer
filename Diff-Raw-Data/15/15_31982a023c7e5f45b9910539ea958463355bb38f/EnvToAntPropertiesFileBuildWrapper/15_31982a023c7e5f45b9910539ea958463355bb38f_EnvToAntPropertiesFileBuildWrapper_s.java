 package jenkins.plugins.env_to_ant_properties_file;
 
 import hudson.Extension;
 import hudson.Launcher;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.model.BuildListener;
 import hudson.tasks.BuildWrapper;
 import hudson.tasks.BuildWrapperDescriptor;
 
 import org.apache.commons.io.FileUtils;
 import org.kohsuke.stapler.DataBoundConstructor;
 
 import java.io.*;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.net.InetAddress;
 
 public class EnvToAntPropertiesFileBuildWrapper extends BuildWrapper {
 
     private File propertiesFile;
 
     @DataBoundConstructor
     public EnvToAntPropertiesFileBuildWrapper(File propertiesFile) {
         this.propertiesFile = propertiesFile;
     }
 
     @Extension
     public static class DescriptorImpl extends BuildWrapperDescriptor {
 
         @Override
         public String getDisplayName() {
             return "Environment to Ant Properties File";
         }
 
         @Override
         public boolean isApplicable(AbstractProject<?, ?> arg0) {
             return true;
         }
     }
 
     @SuppressWarnings({"rawtypes"})
     public Environment setUp(final AbstractBuild build, final Launcher launcher, final BuildListener listener)
             throws IOException, InterruptedException {
         //getBuildVariables leaves out system variables, including the workspace
         Map envVars = build.getBuildVariables();
         try {
             envVars.put("JOB_NAME", build.getEnvironment().get("JOB_NAME"));
             envVars.put("P4_CHANGELIST", build.getEnvironment().get("P4_CHANGELIST"));
         } catch (Exception e) {
             //Variable did not exist
         }
 
         // Get hostname
        InetAddress addr = InetAddress.getLocalHost();
        String nodeName = addr.getHostName();
 
         //convert C:/ to C$/, replace and variables, and prepend the build node (use getEnvVars to ensure WORKSPACE is included)
         String nodePath = "//" + nodeName + "/" + replaceEnvVars(build.getEnvVars(), propertiesFile.getPath()).replaceFirst(":", "\\$");
 
         listener.getLogger().print("Writing environment variables to properties file.  ");
 
         //Replace any environment variables in the properties file path
         File expandedPropertiesFile = new File(nodePath);
 
         //Print out the path to the file
         listener.getLogger().println("\"file:" + nodePath + "\"");
 
         //Create the file and write the current variables to it
         PrintWriter writer = new PrintWriter(new FileWriter(expandedPropertiesFile));
         Iterator it = envVars.entrySet().iterator();
         while (it.hasNext()) {
             Map.Entry entry = (Map.Entry) it.next();
             writer.println(entry.getKey() + "=" + entry.getValue());
         }
         // Close the file
         writer.close();
 
         return new BuildWrapper.Environment() {
         };
     }
 
     private String replaceEnvVars(Map map, String str) {
         //search for variables of the from ${VARNAME}
         Pattern pattern = Pattern.compile("\\$\\{.+?\\}");
         Matcher m = pattern.matcher(str);
         while (m.find()) {
             String s = m.group();
             //strip the variable indicator
             String varName = s.replaceAll("[${}]", "");
             //find the value of the variable
             String varVal = (String) map.get(varName);
             //if variable still has the form ${}, dereference it again (so long as it doesn't reference itself)
             if (s != varVal && varVal.matches("\\$\\{.+?\\}")) {
                 varVal = replaceEnvVars(map, varVal);
             }
 
             //replace${var} with the expanded environment variable
             if (varVal != null) {
                 str = str.replace(s, varVal);
             }
         }
         return str;
     }
 
     public File getPropertiesFile() {
         return propertiesFile;
     }
 
     public void setPropertiesFile(File propertiesFile) {
         this.propertiesFile = propertiesFile;
     }
 }
