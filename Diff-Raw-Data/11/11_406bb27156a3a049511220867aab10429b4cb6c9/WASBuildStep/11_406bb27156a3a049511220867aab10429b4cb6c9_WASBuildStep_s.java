 /*
  * The MIT License
  *
  * Copyright (c) 2009, Manufacture Française des Pneumatiques Michelin, Romain Seguy
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  * THE SOFTWARE.
  */
 
 package com.michelin.cio.hudson.plugins.wasbuilder;
 
 import hudson.EnvVars;
 import hudson.Extension;
 import hudson.FilePath;
 import hudson.Launcher;
 import hudson.Util;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.model.BuildListener;
 import hudson.model.Descriptor.FormException;
 import hudson.model.Hudson;
 import hudson.tasks.BuildStepDescriptor;
 import hudson.tasks.Builder;
 import hudson.util.ArgumentListBuilder;
 import hudson.util.FormValidation;
 import hudson.util.VariableResolver;
 import java.io.IOException;
 import net.sf.json.JSONObject;
 import org.jvnet.localizer.ResourceBundleHolder;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.QueryParameter;
 import org.kohsuke.stapler.StaplerRequest;
 
 /**
  * This builder uses wsadmin to run commands (or scripts) on WAS.
  *
  * <p>Please refer to {@link http://publib.boulder.ibm.com/infocenter/wasinfo/v7r0/index.jsp?topic=/com.ibm.websphere.nd.doc/info/ae/ae/rxml_commandline.html}
  * to get a full description of wsadmin options.</p>
  *
  * @author Romain Seguy
 * @version 1.0
  */
 public class WASBuildStep extends Builder {
 
     public final static String LANG_JACL = "Jacl";
     public final static String LANG_JYTHON = "Jython";
     public final static String[] LANG = { LANG_JYTHON, LANG_JACL };
 
     /** Corresponds to the -wsadmin_classpath option of wsadmin. */
     private final String additionalClasspath;
     /** Corresponds to the -appendtrace option of wsadmin. */
     private final boolean appendTrace;
     /** Corresponds to the -c option of wsadmin. */
     private final String commands;
     /** Corresponds to the -javaoption option of wsadmin. */
     private final String javaOptions;
     /** Corresponds to the -jobid option of wsadmin (not available for WAS 6.0). */
     private final String jobId;
     /** Corresponds to the -lang option of wsadmin */
     private final String language;
     /** Corresponds to the -profile option of wsadmin. */
     private final String profileScriptFiles;
     /** Corresponds to the -p option of wsadmin. */
     private final String propertiesFiles;
     /**
      * If {@code runIf} contains a variable name and if this variable is set,
      * then the build step is run. If the variable is not set, or if this
      * attribute is {@code null}, or if it is an empty string, then the build
      * step is not run.
      */
     private final String runIf;
     /** Corresponds to the -f option of wsadmin. */
     private final String scriptFile;
     /**
      * Corresponds to the script paramaters that are specified at the end of the
      * wsadmin command.
      */
     private final String scriptParameters;
     /** Corresponds to the -tracefile option of wsadmin (not available for WAS 6.0). */
     private final String traceFile;
     /* Identitifies the {@link WASServer} to be used. */
     private final String wasServerName;
 
     @DataBoundConstructor
     public WASBuildStep(String additionalClasspath, boolean appendTrace, String commands, String javaOptions, String jobId, String language, String profileScriptFiles, String propertiesFiles, String runIf, String scriptFile, String scriptParameters, String traceFile, String wasServerName) {
         this.additionalClasspath = additionalClasspath.trim();
         this.appendTrace = appendTrace;
         this.commands = commands.trim();
         this.javaOptions = javaOptions.trim();
         this.jobId = jobId.trim();
         if(!language.equals(LANG_JACL) && !language.equals(LANG_JYTHON)) {
             // we may get here if the user has manually modified the config file
             // default assumed language: jython (it has my preference!)
             this.language = LANG_JYTHON;
         }
         else {
             this.language = language;
         }
         this.profileScriptFiles = profileScriptFiles.trim();
         this.propertiesFiles = propertiesFiles.trim();
         this.runIf = runIf.trim();
         this.scriptFile = scriptFile.trim();
         this.scriptParameters = scriptParameters.trim();
         this.traceFile = traceFile.trim();
         this.wasServerName = wasServerName;
     }
 
     public String getAdditionalClasspath() {
         return additionalClasspath;
     }
 
     public boolean isAppendTrace() {
         return appendTrace;
     }
 
     public String getCommands() {
         return commands;
     }
 
     @Override
     public DescriptorImpl getDescriptor() {
         return (DescriptorImpl) super.getDescriptor();
     }
 
     public String getJavaOptions() {
         return javaOptions;
     }
 
     public String getJobId() {
         return jobId;
     }
 
     public String getLanguage() {
         return language;
     }
 
     public String getProfileScriptFiles() {
         return profileScriptFiles;
     }
 
     public String getPropertiesFiles() {
         return propertiesFiles;
     }
 
     public String getRunIf() {
         return runIf;
     }
 
     public String getScriptFile() {
         return scriptFile;
     }
 
     public String getScriptParameters() {
         return scriptParameters;
     }
 
     public String getTraceFile() {
         return traceFile;
     }
 
     /**
      * Returns the {@link WASServer} to use when the build takes place ({@code
      * null} if none has been set).
      */
     public WASServer getWasServer() {
         for(WASServer server: getDescriptor().getWasServers()) {
             if(getWasServerName() != null && server.getName().equals(getWasServerName())) {
                 return server;
             }
         }
         
         return null;
     }
 
     public String getWasServerName() {
         return wasServerName;
     }
 
     @Override
     public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
         AbstractProject project = build.getProject();
         ArgumentListBuilder args = new ArgumentListBuilder();
         EnvVars env = build.getEnvironment(listener);
         VariableResolver<String> varResolver = build.getBuildVariableResolver();
 
         // --- runIf ---
 
         if(getRunIf() != null && getRunIf().length() > 0) {
             listener.getLogger().println(ResourceBundleHolder.get(WASBuildStep.class).format("SearchingForBuildOrEnvVar", getRunIf()));
             
             // 1st check: is the var defined at the build level?
             String runIfVar = varResolver.resolve(getRunIf());
             if(runIfVar != null) {
                 // does the build var has a value?
                 if(runIfVar.trim().length() > 0) {
                     listener.getLogger().println(ResourceBundleHolder.get(WASBuildStep.class).format("BuildStepRunBecauseOfBuildVar", getRunIf()));
                 }
                 // the build var exists and has no value
                 else {
                     listener.getLogger().println(ResourceBundleHolder.get(WASBuildStep.class).format("BuildStepNotRunBecauseOfBuildVar", getRunIf()));
                     return true;
                 }
             }
             // 3rd check: is the var defined at the environment level?
             else {
                 listener.getLogger().println(ResourceBundleHolder.get(WASBuildStep.class).format("BuildVarNotFound", getRunIf()));
 
                 if(env.containsKey(getRunIf())) {
                     listener.getLogger().println(ResourceBundleHolder.get(WASBuildStep.class).format("BuildStepRunBecauseOfEnvVar", getRunIf()));
                 }
                 else {
                     listener.getLogger().println(ResourceBundleHolder.get(WASBuildStep.class).format("BuildStepNotRunBecauseOfEnvVar", getRunIf()));
                     return true;
                 }
             }
         }
 
         // --- wsadmin.bat/wsadmin.sh ---
 
         WASServer wasServer = getWasServer();
         if(wasServer != null) {
             WASInstallation wasInstallation = wasServer.getWasInstallation();
             if(wasInstallation != null) {
                 String wsAdminExecutable = wasInstallation.getWsadminExecutable(launcher);
                 if(wsAdminExecutable != null) {
                     args.add(wsAdminExecutable);
                 }
                 else {
                     listener.fatalError(ResourceBundleHolder.get(WASBuildStep.class).format("NoWsAdminExecutable", wasInstallation.getName(), wasServer.getName()));
                     return false;
                 }
             }
             else {
                 listener.fatalError(ResourceBundleHolder.get(WASBuildStep.class).format("NoInstallationSet", wasServer.getName()));
                 return false;
             }
         }
         else {
             listener.fatalError(ResourceBundleHolder.get(WASBuildStep.class).format("NoServerSet"));
             return false;
         }
 
        // -- server parameters (defined in the corresponding WASServer) --
 
         args.add("-conntype", wasServer.getConntype());
         args.add("-host", wasServer.getHost());
         args.add("-port", Integer.toString(wasServer.getPort()));
         if(wasServer.getUser() != null && wasServer.getUser().length() > 0) {
             args.add("-user", wasServer.getUser());
         }
         if(wasServer.getPassword() != null && wasServer.getPassword().length() > 0) {
             args.add("-password");
             args.addMasked(wasServer.getPassword());
         }
 
         // --- lang ---
 
         args.add("-lang", getLanguage().toLowerCase());
 
         // --- commands or script file ---
 
         if(getCommands() != null && getCommands().length() > 0) {
             for(String command: Util.tokenize(Util.replaceMacro(env.expand(getCommands()), varResolver), "\n\r\f")) {
                 args.add("-c", command);
             }
         }
         else if(getScriptFile() != null && getScriptFile().length() > 0) {
             FilePath scriptFilePath = project.getWorkspace().child(Util.replaceMacro(env.expand(getScriptFile()), varResolver));
             if(scriptFilePath.exists()) {
                 args.add("-f");
                 args.add(scriptFilePath);
             }
             else {
                 listener.fatalError(ResourceBundleHolder.get(WASBuildStep.class).format("ScriptFileNotFound", scriptFilePath.toURI()));
                 return false;
             }
         }
         else {
             listener.fatalError(ResourceBundleHolder.get(WASBuildStep.class).format("NoCommandNorScriptFileSet"));
             return false;
         }
 
         // --- properties files ---
 
         if(getPropertiesFiles() != null && getPropertiesFiles().length() > 0) {
             for(String propertiesFile: Util.tokenize(Util.replaceMacro(env.expand(getPropertiesFiles()), varResolver))) {
                 FilePath propertiesFilePath = project.getWorkspace().child(propertiesFile);
                 if(propertiesFilePath.exists()) {
                     args.add("-p");
                     args.add(propertiesFilePath);
                 }
                 else {
                     listener.error(ResourceBundleHolder.get(WASBuildStep.class).format("PropertiesFileNotFound"), propertiesFilePath.toURI());
                 }
             }
         }
 
         // --- profile script files ---
 
         if(getProfileScriptFiles() != null && getProfileScriptFiles().length() > 0) {
             for(String profileScriptFile: Util.tokenize(Util.replaceMacro(env.expand(getProfileScriptFiles()), varResolver))) {
                 FilePath profileScriptFilePath = project.getWorkspace().child(profileScriptFile);
                 if(profileScriptFilePath.exists()) {
                     args.add("-profile");
                     args.add(profileScriptFilePath);
                 }
                 else {
                     listener.error(ResourceBundleHolder.get(WASBuildStep.class).format("ProfileScriptFileNotFound", profileScriptFilePath.toURI()));
                 }
             }
         }
 
         // --- Java options ---
 
         if(getJavaOptions() != null && getJavaOptions().length() > 0) {
             for(String javaOption: Util.tokenize(Util.replaceMacro(env.expand(getJavaOptions()), varResolver))) {
                 args.add("-javaoption", javaOption);
             }
         }
 
         // --- additional classpath ---
 
         if(getAdditionalClasspath() != null && getAdditionalClasspath().length() > 0) {
             args.add("-wsadmin_classpath", getAdditionalClasspath());
         }
 
         // --- job ID ---
 
         if(getJobId() != null && getJobId().length() > 0) {
             args.add("-jobid", Util.replaceMacro(env.expand(getJobId()), varResolver));
         }
 
         // --- trace file ---
 
         if(getTraceFile() != null && getTraceFile().length() > 0) {
             FilePath traceFilePath = project.getWorkspace().child(Util.replaceMacro(env.expand(getTraceFile()), varResolver));
             args.add("-tracefile");
             args.add(traceFilePath);
         }
 
         // --- append trace ---
         
         if(isAppendTrace()) {
             args.add("-appendtrace", Boolean.toString(isAppendTrace()));
         }
         
         // --- parameters ---
 
         if(getScriptParameters() != null && getScriptFile().length() > 0) {
             args.addTokenized(Util.replaceMacro(env.expand(getScriptParameters()), varResolver).replaceAll("[\t\r\n]+"," "));
         }
 
         try {
             return launcher.launch().cmds(args).envs(env).stdout(listener).join() == 0;
         }
         catch(IOException ioe) {
             Util.displayIOException(ioe, listener);
             listener.fatalError(ResourceBundleHolder.get(WASBuildStep.class).format("ExecutionFailed"));
             return false;
         }
     }
 
     @Extension
     public static class DescriptorImpl extends BuildStepDescriptor<Builder> {
 
         public DescriptorImpl() {
             load();
         }
 
         protected DescriptorImpl(Class<? extends WASBuildStep> clazz) {
             super(clazz);
         }
 
         @Override
         public String getDisplayName() {
             return ResourceBundleHolder.get(WASBuildStep.class).format("DisplayName");
         }
 
         /**
          * Returns the possible languages supported by wsadmin.
          *
          * <p>This method needs to be placed here so that the list can be
          * accessible from WASBuildStep's config.jelly file: config.jelly
          * is not able to access such a method if it is placed, even statically,
          * into WASBuildStep.</p>
          */
         public String[] getLanguages() {
             return WASBuildStep.LANG;
         }
 
         public WASServer[] getWasServers() {
             return Hudson.getInstance().getDescriptorByType(WASInstallation.DescriptorImpl.class).getServers();
         }
 
         @Override
         public boolean isApplicable(Class<? extends AbstractProject> jobType) {
             if(getWasServers() != null && getWasServers().length > 0) {
                 return true;
             }
             return false;
         }
 
         @Override
         public Builder newInstance(StaplerRequest req, JSONObject formData) throws FormException {
             // The code below is commented until issue HUDSON-5028 (which is
             // about the use of radioBlocks in a BuildStep) is resolved.
             //
             // we need to do a little JSON processing to handle radioBlocks
             // which generates (for example) the following JSON data:
             // "commandsOrScript => {"commands":"aaa","value":"commands"}"
             //try {
             //    String commands = ((JSONObject) formData.getJSONObject("commands")).getString("commands");
             //    formData.put("commands", commands);
             //}
             //catch(JSONException je) {
             //    String scriptFile = ((JSONObject) formData.getJSONObject("scriptFile")).getString("scriptFile");
             //    formData.put("scriptFile", scriptFile);
             ////}
             ////formData.remove("commandsOrScript");
 
             // now we can safely use bindJSON()
             return req.bindJSON(WASBuildStep.class, formData);
         }
 
         public FormValidation doCheckCommands(@QueryParameter String value) {
             if(value == null || value.length() == 0) {
                 return FormValidation.warning(ResourceBundleHolder.get(WASBuildStep.class).format("CommandsOrScriptFileMustBeSet"));
             }
 
             return FormValidation.ok();
         }
 
         public FormValidation doCheckScriptFile(@QueryParameter String value) {
             if(value == null || value.length() == 0) {
                 return FormValidation.warning(ResourceBundleHolder.get(WASBuildStep.class).format("ScriptFileOrCommandsMustBeSet"));
             }
 
             return FormValidation.ok();
         }
 
     }
 
 }
