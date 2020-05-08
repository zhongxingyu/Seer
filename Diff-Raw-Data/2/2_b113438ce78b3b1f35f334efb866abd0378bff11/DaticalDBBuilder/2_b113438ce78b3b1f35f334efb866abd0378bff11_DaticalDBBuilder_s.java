 package com.datical.integration.jenkins;
 import hudson.EnvVars;
 import hudson.Launcher;
 import hudson.Extension;
 import hudson.Messages;
 import hudson.Util;
 import hudson.util.ArgumentListBuilder;
 import hudson.util.FormValidation;
 import hudson.model.AbstractBuild;
 import hudson.model.BuildListener;
 import hudson.model.AbstractProject;
 import hudson.tasks.Builder;
 import hudson.tasks.BuildStepDescriptor;
 import net.sf.json.JSONObject;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.QueryParameter;
 
 import javax.servlet.ServletException;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Sample {@link Builder}.
  *
  * <p>
  * When the user configures the project and enables this builder,
  * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked
  * and a new {@link DaticalDBBuilder} is created. The created
  * instance is persisted to the project configuration XML by using
  * XStream, so this allows you to use instance fields (like {@link #name})
  * to remember the configuration.
  *
  * <p>
  * When a build is performed, the {@link #perform(AbstractBuild, Launcher, BuildListener)}
  * method will be invoked. 
  *
  * @author <a href="mailto:info@datical.com">Robert Reeves</a>
  */
 public class DaticalDBBuilder extends Builder {
 	
 	
 	private static final Pattern WIN_ENV_VAR_REGEX = Pattern.compile("%([a-zA-Z0-9_]+)%");
 	private static final Pattern UNIX_ENV_VAR_REGEX = Pattern.compile("\\$([a-zA-Z0-9_]+)");
 
     private final String name;
     
     
     private final String daticalProjectDir;
     private final String daticalDBServer;
     private final String daticalDBAction; // Forecast, Snapshot, Deploy, Rollback
 
     // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
     @DataBoundConstructor
     public DaticalDBBuilder(String name, String daticalProjectDir, String daticalDBServer, String daticalDBAction) {
         this.name = name;
         
         this.daticalProjectDir = daticalProjectDir;
         this.daticalDBServer = daticalDBServer;
         this.daticalDBAction = daticalDBAction;
     }
 
     /**
      * We'll use this from the <tt>config.jelly</tt>.
      */
     public String getName() {
         return name;
     }
 
     public String getDaticalProjectDir() {
 		return daticalProjectDir;
 	}
 
 	public String getDaticalDBServer() {
 		return daticalDBServer;
 	}
 
 	public String getDaticalDBAction() {
 		return daticalDBAction;
 	}
 
 	@Override
     public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) {
 		
 		
 //		C:\DaticalDB-1.1.0.201305141509\repl>C:\DaticalDB-1.1.0.201305141509\repl\hammer
 //		.bat --drivers="C:\DaticalDB-1.1.0.201305141509\drivers" --project="C:\Users\r2\
 //		datical\MyDeploymentProject" checkdrivers
 
 		String UNIX_SEP = "/";
 		String WINDOWS_SEP = "\\";
 		
         // This is where you 'build' the project.
         // Since this is a dummy, we just say 'hello world' and call that a build.
 
         // This also shows how you can consult the global configuration of the builder
         if (getDescriptor().getUseFrench())
             listener.getLogger().println("Bonjour, "+name+"!");
         else
             listener.getLogger().println("Hello, "+name+"!");
         
         listener.getLogger().println("Global Config:");
         
         listener.getLogger().println("daticalDBInstallDir = " + getDescriptor().getDaticalDBInstallDir());
         listener.getLogger().println("daticalDBDriversDir = " + getDescriptor().getDaticalDBDriversDir());        
         
         listener.getLogger().println("Project Specific Config:");
         
         listener.getLogger().println("daticalProjectDir = " + daticalProjectDir);
         listener.getLogger().println("daticalDBServer = " + daticalDBServer);
         listener.getLogger().println("daticalDBAction = " + daticalDBAction);
         
         listener.getLogger().println("build.getWorkspace().toString() = " + build.getWorkspace());
         
         
 
 		String daticalCmd = getDescriptor().getDaticalDBInstallDir() + "\\repl\\hammer.bat";
 		String daticalDriversArg = "--drivers=" + getDescriptor().getDaticalDBDriversDir();
 		String daticalProjectArg = "--project=" + daticalProjectDir;
 		
 		String commandLine = daticalCmd + " " + "\"" + daticalDriversArg + "\"" + " " + "\"" + daticalProjectArg + "\"" + " " + getDaticalDBActionForCmd(daticalDBAction, daticalDBServer);
 		
 		
         
         
 
         String cmdLine = convertSeparator(commandLine, (launcher.isUnix() ? UNIX_SEP : WINDOWS_SEP));
         listener.getLogger().println("File separators sanitized: " + cmdLine);
           
         if (launcher.isUnix()) {
           cmdLine = convertEnvVarsToUnix(cmdLine);
         } else {
           cmdLine = convertEnvVarsToWindows(cmdLine);
         }
         listener.getLogger().println("Environment variables sanitized: " + cmdLine);
 
         ArgumentListBuilder args = new ArgumentListBuilder();
         if (cmdLine != null) {
           //args.addTokenized((launcher.isUnix() && executeFromWorkingDir) ? "./" + cmdLine : cmdLine);
         	args.addTokenized((launcher.isUnix()) ? "./" + cmdLine : cmdLine);
           listener.getLogger().println("Execute from working directory: " + args.toStringWithQuote());
         }
 
         if (!launcher.isUnix()) {
           args = args.toWindowsCommand();
           listener.getLogger().println("Windows command: " + args.toStringWithQuote());
         }
 
         EnvVars env = null;
 		try {
 			env = build.getEnvironment(listener);
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (InterruptedException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
         env.putAll(build.getBuildVariables());
 
        listener.getLogger().println("Environment variables: " + env.entrySet().toString());
         listener.getLogger().println("Command line: " + args.toStringWithQuote());
         listener.getLogger().println("Working directory: " + build.getWorkspace());
 
         try {
           final int result = launcher.decorateFor(build.getBuiltOn()).launch().cmds(args).envs(env).stdout(listener).pwd(build.getWorkspace()).join();
           return result == 0;
         } catch (final IOException e) {
           Util.displayIOException(e, listener);
           final String errorMessage = "Command execution failed";
           e.printStackTrace(listener.fatalError(errorMessage));
           return false;
         } catch (final InterruptedException e) {
             final String errorMessage = "Command execution failed";
             e.printStackTrace(listener.fatalError(errorMessage));
             return false;
 		}
 
         
         
         //return true;
     }
 
 	private String getDaticalDBActionForCmd(String daticalDBAction, String daticalDBServer) {
 
 		// See config.jelly for all options used.
 		String daticalDBActionForCmd = null;
 		
 		if (daticalDBAction.equals("forecast")) {
 			
 			daticalDBActionForCmd = daticalDBAction + " " + "\"" + daticalDBServer + "\"" ;
 			
 		} else if (daticalDBAction.equals("snapshot")) {
 			
 			daticalDBActionForCmd = daticalDBAction + " " + "\"" + daticalDBServer + "\"" ;
 			
 		} else if (daticalDBAction.equals("deploy")) {
 			
 			daticalDBActionForCmd = daticalDBAction + " " + "\"" + daticalDBServer + "\"" ;
 			
 		} else if (daticalDBAction.equals("status")) {
 			
 			daticalDBActionForCmd = daticalDBAction + " " + "\"" + daticalDBServer + "\"" ;
 			
 		} else if (daticalDBAction.equals("checkdrivers")) {
 			
 			daticalDBActionForCmd = daticalDBAction;
 			
 		}
 		
 		return daticalDBActionForCmd;
 
 	}
 
 	// Overridden for better type safety.
     // If your plugin doesn't really define any property on Descriptor,
     // you don't have to do this.
     @Override
     public DescriptorImpl getDescriptor() {
         return (DescriptorImpl)super.getDescriptor();
     }
 
     /**
      * Descriptor for {@link DaticalDBBuilder}. Used as a singleton.
      * The class is marked as public so that it can be accessed from views.
      *
      * <p>
      * See <tt>src/main/resources/hudson/plugins/hello_world/DaticalDBBuilder/*.jelly</tt>
      * for the actual HTML fragment for the configuration screen.
      */
     @Extension // This indicates to Jenkins that this is an implementation of an extension point.
     public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
         /**
          * To persist global configuration information,
          * simply store it in a field and call save().
          *
          * <p>
          * If you don't want fields to be persisted, use <tt>transient</tt>.
          */
         private boolean useFrench;
         private String daticalDBInstallDir;
         private String daticalDBDriversDir;
 
         /**
          * Performs on-the-fly validation of the form field 'name'.
          *
          * @param value
          *      This parameter receives the value that the user has typed.
          * @return
          *      Indicates the outcome of the validation. This is sent to the browser.
          */
         public FormValidation doCheckName(@QueryParameter String value)
                 throws IOException, ServletException {
             if (value.length() == 0)
                 return FormValidation.error("Please set a name");
             if (value.length() < 4)
                 return FormValidation.warning("Isn't the name too short?");
             return FormValidation.ok();
         }
 
         public boolean isApplicable(Class<? extends AbstractProject> aClass) {
             // Indicates that this builder can be used with all kinds of project types 
             return true;
         }
 
         /**
          * This human readable name is used in the configuration screen.
          */
         public String getDisplayName() {
             return "Datical DB";
         }
 
         @Override
         public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
             // To persist global configuration information,
             // set that to properties and call save().
             useFrench = formData.getBoolean("useFrench");
             daticalDBInstallDir = formData.getString("daticalDBInstallDir");
             daticalDBDriversDir = formData.getString("daticalDBDriversDir");
             
             // ^Can also use req.bindJSON(this, formData);
             //  (easier when there are many fields; need set* methods for this, like setUseFrench)
             save();
             return super.configure(req,formData);
         }
 
         /**
          * This method returns true if the global configuration says we should speak French.
          *
          * The method name is bit awkward because global.jelly calls this method to determine
          * the initial state of the checkbox by the naming convention.
          */
         public boolean getUseFrench() {
             return useFrench;
         }
 
 		public String getDaticalDBInstallDir() {
 			return daticalDBInstallDir;
 		}
 
 		public String getDaticalDBDriversDir() {
 			return daticalDBDriversDir;
 		}
         
         
     }
     
     public static String convertSeparator(String cmdLine, String newSeparator) {
         String match = "[/" + Pattern.quote("\\") + "]";
         String replacement = Matcher.quoteReplacement(newSeparator);
 
         Pattern words = Pattern.compile("\\S+");
         Pattern urls = Pattern.compile("(https*|ftp|git):");
         StringBuffer sb = new StringBuffer();
         Matcher m = words.matcher(cmdLine);
         while (m.find()) {
           String item = m.group();
           if (!urls.matcher(item).find()) {
             // Not sure if File.separator is right if executing on slave with OS different from master's one
             //String cmdLine = commandLine.replaceAll("[/\\\\]", File.separator);
             m.appendReplacement(sb, Matcher.quoteReplacement(item.replaceAll(match, replacement)));
           }
         }
         m.appendTail(sb);
 
         return sb.toString();
       }
     
     /**
      * Convert Windows-style environment variables to UNIX-style.
      * E.g. "script --opt=%OPT%" to "script --opt=$OPT"
      *
      * @param cmdLine The command line with Windows-style env vars to convert.
      * @return The command line with UNIX-style env vars.
      */
     public static String convertEnvVarsToUnix(String cmdLine) {
       if (cmdLine == null) {
         return null;
       }
 
       StringBuffer sb = new StringBuffer();
 
       Matcher m = WIN_ENV_VAR_REGEX.matcher(cmdLine);
       while (m.find()) {
         m.appendReplacement(sb, "\\$$1");
       }
       m.appendTail(sb);
 
       return sb.toString();
     }
 
     /**
      * Convert UNIX-style environment variables to Windows-style.
      * E.g. "script --opt=$OPT" to "script --opt=%OPT%"
      *
      * @param cmdLine The command line with Windows-style env vars to convert.
      * @return The command line with UNIX-style env vars.
      */
     public static String convertEnvVarsToWindows(String cmdLine) {
       if (cmdLine == null) {
         return null;
       }
 
       StringBuffer sb = new StringBuffer();
 
       Matcher m = UNIX_ENV_VAR_REGEX.matcher(cmdLine);
       while (m.find()) {
         m.appendReplacement(sb, "%$1%");
       }
       m.appendTail(sb);
 
       return sb.toString();
     }
 }
 
 
