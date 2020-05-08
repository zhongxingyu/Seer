 package hudson.plugins.ruby;
 
 import hudson.FilePath;
 import hudson.model.Descriptor;
 import hudson.tasks.Builder;
 import hudson.tasks.CommandInterpreter;
 import net.sf.json.JSONObject;
 import org.kohsuke.stapler.StaplerRequest;
 
 /**
  * Invokes the ruby interpreter and invokes the Ruby script entered on the hudson build configuration.
  * <p/>
  * It is expected that the ruby interpreter is available on the system PATH.
  *
  * @author Vivek Pandey
  */
 public class Ruby extends CommandInterpreter {
 
     private Ruby(String command) {
         super(command);
     }
 
     protected String[] buildCommandLine(FilePath script) {
         return new String[]{"ruby", "-v", script.getRemote()};
     }
 
     protected String getContents() {
         return command;
     }
 
     protected String getFileExtension() {
         return ".rb";
     }
 
     public Descriptor<Builder> getDescriptor() {
         return DESCRIPTOR;
     }
 
     public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();
 
     public static final class DescriptorImpl extends Descriptor<Builder> {
         private DescriptorImpl() {
             super(Ruby.class);
         }
 
         public Builder newInstance(StaplerRequest req, JSONObject formData) {
             return new Ruby(formData.getString("ruby"));
         }
 
         public String getDisplayName() {
             return "Execute Ruby script";
         }
 
         @Override
         public String getHelpFile() {
            return "/plugin/ruby/help.html";
         }
     }
 }
