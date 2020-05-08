 package com.tombrus.hudson.dostrigger;
 
 import antlr.ANTLRException;
 import hudson.EnvVars;
 import hudson.Extension;
 import hudson.FilePath;
 import hudson.Launcher;
 import hudson.Util;
 import hudson.model.Cause;
 import hudson.model.Hudson;
 import hudson.model.ParameterDefinition;
 import hudson.model.ParametersDefinitionProperty;
 import hudson.model.PasswordParameterValue;
 import hudson.model.Project;
 import hudson.model.StringParameterValue;
 import hudson.model.Item;
 import hudson.model.TaskListener;
 import hudson.model.TopLevelItem;
 import hudson.tasks.Messages;
 import hudson.triggers.Trigger;
 import hudson.triggers.TriggerDescriptor;
 import hudson.util.LogTaskListener;
 import org.kohsuke.stapler.DataBoundConstructor;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class DosTrigger extends Trigger<Project> {
     private static final Logger LOGGER    = Logger.getLogger(DosTrigger.class.getName());
 
     private        final String script;
     private static final String MARKER    = "#:#:#";
     private static final String CAUSE_VAR = "CAUSE";
     private static final String CRLF      = "\r\n";
 
     private int nextBuildNum;
 
     @DataBoundConstructor
     public DosTrigger(String schedule, String script) throws ANTLRException {
         super(schedule);
         this.script = script;
     }
 
     @SuppressWarnings({"UnusedDeclaration"})
     public String getScript() {
         return script;
     }
 
     @SuppressWarnings({"UnusedDeclaration"})
     public String getSchedule() {
         return spec;
     }
 
 	/**
      * 
      */
     private void triggerScript() {
         try {
             String output = runScript();
             String cause = output == null ? "" : getVar(CAUSE_VAR, output);
             if (cause.length()>0) {
                 job.scheduleBuild(0, new MyCause(cause));
             }
         } catch (Exception e) {
             LOGGER.log(Level.SEVERE, "Problem while executing DosTrigger.run()", e);
         }
     }
     
 
     /**
      * Plugin entry point is here.
      */
     public void run() {
     	//added to check if project is disabled/buildable or not.
         if (!Hudson.getInstance().isQuietingDown() && this.job.isBuildable()) {
         	this.triggerScript();
         }
     }
 
     private String getVar(final String var, final String output) {
         Matcher matcher     = Pattern.compile("(?s).*"+MARKER + var +MARKER + "([^\n\r]*)"+MARKER+".*").matcher(output);
         String  description = null;
         if (matcher.matches()) {
             description = matcher.group(1).trim();
         }
         if (description == null || description.length() == 0) {
             description = "";
         }
         return description;
     }
 
     @Extension
     public static final class DescriptorImpl extends TriggerDescriptor {
         public String getDisplayName() {
             return "Poll with a Windows Batch Command";
         }
 
         public boolean isApplicable(Item item) {
             return item instanceof TopLevelItem;
         }
     }
     
     /**
      * Builds up the environment variable map of build settings
      * for the job which the trigger is examining.
      */
     public final EnvVars initCharacteristicEnvVars(EnvVars env) {
 //        env.put("JENKINS_SERVER_COOKIE",Util.getDigestOf("ServerID:"+Hudson.getInstance().getSecretKey()));
 //        env.put("HUDSON_SERVER_COOKIE",Util.getDigestOf("ServerID:"+Hudson.getInstance().getSecretKey())); // Legacy compatibility
     	
         this.nextBuildNum = this.job.getNextBuildNumber();
     	//check if build is in progress... if so, increase next build number.
     	if(this.job.isBuilding()) {
     		this.nextBuildNum++;
     	}
         env.put("BUILD_NUMBER",String.valueOf(this.nextBuildNum));
 //        env.put("BUILD_ID",getId());
 //        env.put("BUILD_TAG","hudson-"+getParent().getName()+"-"+number);
 //        env.put("JOB_NAME",getParent().getFullName());
         return env;
     }
     
     
     /**
      * Builds the environment variables for the parameters used in building the project
      * @param listener
      * @return
      * @throws IOException
      * @throws InterruptedException
      */
     private EnvVars buildEnvironmentForScriptToRun(TaskListener listener) throws IOException, InterruptedException {
     	EnvVars envVars = new EnvVars();
 
     	ParametersDefinitionProperty p = (ParametersDefinitionProperty) this.job.getProperty(ParametersDefinitionProperty.class);
    	List <ParameterDefinition> paramList = p.getParameterDefinitions();
    	for (ParameterDefinition parameter : paramList ) {
    		Object obj = parameter.getDefaultParameterValue();
    		if (obj instanceof PasswordParameterValue) {
    			PasswordParameterValue password = (PasswordParameterValue)obj;
    			password.buildEnvVars(null, envVars);
    		}else {
    			StringParameterValue stringParam = (StringParameterValue) obj;
    			stringParam.buildEnvVars(null, envVars);
    		}
        		
        	this.initCharacteristicEnvVars(envVars); 
        }
         
     	return envVars;
     }
 
     private String runScript() throws InterruptedException {
         final TaskListener listener = new LogTaskListener(LOGGER, Level.INFO);
         try {
             final FilePath  ws        = Hudson.getInstance().getWorkspaceFor((TopLevelItem) job);
             final FilePath  batchFile = ws.createTextTempFile("hudson", ".bat", makeScript(), false);
             final FilePath  logFile   = ws.child("dos-trigger.log");
             final LogStream logStream = new LogStream(logFile);
             try {
                 final Launcher launcher = Hudson.getInstance().createLauncher(listener);
                 final String[] cmd      = new String[]{"cmd", "/c", "call", batchFile.getRemote()};
 
                 final EnvVars envVars = this.buildEnvironmentForScriptToRun(listener);
             	if (envVars.size()>0) {
                     launcher.launch().cmds(cmd).envs(envVars).stdout(logStream).pwd(ws).join();
             		
             	}else {
             		LOGGER.log(Level.WARNING, "EnvVars returned with nothing in it..");
 					assert(envVars.size()>0);
             	}
                 return logStream.toString();
             } catch (IOException e) {
                 Util.displayIOException(e, listener);
                 e.printStackTrace(listener.fatalError(Messages.CommandInterpreter_CommandFailed()));
                 return null;
             } finally {
                 try {
                     batchFile.delete();
                 } catch (IOException e) {
                     Util.displayIOException(e, listener);
                     e.printStackTrace(listener.fatalError(Messages.CommandInterpreter_UnableToDelete(batchFile)));
                 }
                 logStream.close();
             }
         } catch (IOException e) {
             Util.displayIOException(e, listener);
             e.printStackTrace(listener.fatalError(Messages.CommandInterpreter_UnableToProduceScript()));
             return null;
         }
     }
 
     private String makeScript() {
         return ""
                 + "@set "+ CAUSE_VAR +"=" +CRLF
                 + "@echo off" + CRLF
                 + "call :TheActualScript" + CRLF
                 + "@echo off" + CRLF
                 + "echo " + MARKER + CAUSE_VAR + MARKER + "%" + CAUSE_VAR + "%" + MARKER + CRLF
                 + "goto :EOF" + CRLF
                 + ":TheActualScript" + CRLF
                 + script + CRLF;
     }
 
     private static class MyCause extends Cause {
         private final String description;
 
         public MyCause(String description) {
             this.description = description;
         }
 
         public String getShortDescription() {
             return description;
         }
     }
 
     private static class LogStream extends OutputStream {
         private final StringBuilder log       = new StringBuilder();
         private final OutputStream  logStream;
 
         public LogStream(FilePath logFile) throws IOException, InterruptedException {
             logStream = logFile.write();
         }
 
         public void write(int b) throws IOException {
             log.append((char) b);
             logStream.write(b);
         }
 
         public void close() throws IOException {
             super.close();
             logStream.close();
         }
 
         public String toString() {
             return log.toString();
         }
     }
 }
 
