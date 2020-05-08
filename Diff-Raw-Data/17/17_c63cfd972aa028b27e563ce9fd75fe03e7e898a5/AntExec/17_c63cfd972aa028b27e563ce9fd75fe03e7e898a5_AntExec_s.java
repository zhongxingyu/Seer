 /*
  * The MIT License
  *
  * Copyright (c) 2011, Milos Svasek
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
 package hudson.plugins.antexec;
 
 import hudson.*;
 import hudson.model.*;
 //TODO: Change this:  dependency on hudson.tasks._ant.AntConsoleAnnotator
 import hudson.tasks._ant.AntConsoleAnnotator;
 import hudson.util.ArgumentListBuilder;
 import hudson.util.FormValidation;
 import hudson.tasks.Builder;
 import hudson.tasks.BuildStepDescriptor;
 import hudson.util.VariableResolver;
 import net.sf.json.JSONObject;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.QueryParameter;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 /**
  * Invokes the Apache Ant script entered on the hudson build configuration.
  * <p/>
  *
  * @author Milos Svasek
  */
 public class AntExec extends Builder {
     private static final String myName = "antexec";
     private String scriptSource;
     private String properties;
     private String antHome;
     private String antOpts;
 
     // Fields in config.jelly must match the parameter names in the "DataBoundConstructor"
     @DataBoundConstructor
     public AntExec(String scriptSource, String properties, String antHome, String antOpts) {
         this.scriptSource = scriptSource;
         this.properties = properties;
         this.antHome = antHome;
         this.antOpts = antOpts;
     }
 
     public String getScriptSource() {
         return scriptSource;
     }
 
     public String getProperties() {
         return properties;
     }
 
     public String getAntHome() {
         return antHome;
     }
 
     public String getAntOpts() {
         return antOpts;
     }
 
     @Override
     public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
         ArgumentListBuilder args = new ArgumentListBuilder();
         EnvVars env = build.getEnvironment(listener);
 
         String antExe = launcher.isUnix() ? "bin/ant" : "bin/ant.bat";
         File antHomeUse = null;
 
         if ((!env.get("ANT_HOME").isEmpty()) && (new File(env.get("ANT_HOME"), antExe).exists()) && (new File(env.get("ANT_HOME"), "lib/ant.jar").exists()) && (new File(env.get("ANT_HOME"), antExe).canExecute())) {
             antHomeUse = new File(env.get("ANT_HOME"));
             listener.getLogger().println(Messages.AntExec_AntHomeEnvVarFound(antHomeUse));
         } else {
             listener.getLogger().println(Messages.AntExec_AntHomeEnvVarNotFound());
         }
 
         if ((!antHome.isEmpty()) && (new File(antHome, antExe).exists()) && (new File(antHome, "lib/ant.jar").exists()) && new File(antHome, antExe).canExecute()) {
             antHomeUse = new File(antHome);
             listener.getLogger().println(Messages._AntExec_AntHomeReplacing(antHomeUse));
             env.put("ANT_HOME", antHomeUse.getAbsolutePath());
            listener.getLogger().println(Messages.AntExec_EnvironmentChanged("ANT_HOME", antHomeUse));
         }
 
 
         File antExeFile = new File(antHomeUse, antExe);
         FilePath buildFile = makeBuildFile(scriptSource, new FilePath(build.getWorkspace(), "antexec_build.xml"));
         args.add(antExeFile);
 
         args.add("-file", buildFile.getName());
         VariableResolver<String> vr = build.getBuildVariableResolver();
         Set<String> sensitiveVars = build.getSensitiveBuildVariables();
         args.addKeyValuePairs("-D", build.getBuildVariables(), sensitiveVars);
         args.addKeyValuePairsFromPropertyString("-D", properties, vr, sensitiveVars);
 
         if (antOpts != null) {
             env.put("ANT_OPTS", env.expand(antOpts));
            listener.getLogger().println(Messages.AntExec_EnvironmentChanged("ANT_OPTS", antOpts));
         }
 
         // Get and prepare ant-contrib.jar
         if (getDescriptor().useAntContrib()) {
             listener.getLogger().println(Messages.AntExec_UseAntContribTasks());
             URL urlAntContrib = new URL(env.get("HUDSON_URL") + "plugin/" + myName + "/lib/ant-contrib.jar");
             FilePath antLibDir = new FilePath(build.getWorkspace(), "antlib");
             FilePath antContribJar = new FilePath(antLibDir, "ant-contrib.jar");
 
             antContribJar.copyFrom(urlAntContrib);
             args.add("-lib", antLibDir.getName());
         } else {
             listener.getLogger().println(Messages.AntExec_UseAntCoreTasksOnly());
         }
 
         if (!launcher.isUnix()) {
             args = args.toWindowsCommand();
             // For some reason, ant on windows rejects empty parameters but unix does not.
             // Add quotes for any empty parameter values:
             List<String> newArgs = new ArrayList<String>(args.toList());
             newArgs.set(newArgs.size() - 1, newArgs.get(newArgs.size() - 1).replaceAll("(?<= )(-D[^\" ]+)= ", "$1=\"\" "));
             args = new ArgumentListBuilder(newArgs.toArray(new String[newArgs.size()]));
         }
 
         // Debug //
         listener.getLogger().println();
        listener.getLogger().println("Content of scriptSource text area --- begin ---");
         listener.getLogger().println(scriptSource);
        listener.getLogger().println("Content of scriptSource text area ---- end ----");
         listener.getLogger().println();
        listener.getLogger().println("Content of properties text area --- begin ---");
         listener.getLogger().println(properties);
        listener.getLogger().println("Content of properties text area ---- end ----");
         listener.getLogger().println();
 
         try {
             AntConsoleAnnotator aca = new AntConsoleAnnotator(listener.getLogger(), build.getCharset());
             int r;
             try {
                 r = launcher.launch().cmds(args).envs(env).stdout(aca).pwd(buildFile.getParent()).join();
             } finally {
                 aca.forceEol();
             }
             return r == 0;
         } catch (IOException e) {
             Util.displayIOException(e, listener);
             String errorMessage = Messages.AntExec_ExecFailed() + Messages.AntExec_ProjectConfigNeeded();
             e.printStackTrace(listener.fatalError(errorMessage));
             return false;
         }
     }
 
     @Override
     public DescriptorImpl getDescriptor() {
         return (DescriptorImpl) super.getDescriptor();
     }
 
     @Extension
     public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
         private boolean useAntContrib;
 
         public DescriptorImpl() {
             super(AntExec.class);
             load();
         }
 
         public FormValidation doCheckAntHome(@QueryParameter File value) {
             // this can be used to check the existence of a file on the server, so needs to be protected
             if (!Hudson.getInstance().hasPermission(Hudson.ADMINISTER))
                 return FormValidation.ok();
 
             if (value.length() == 0)
                 return FormValidation.error(Messages.AntExec_AntHomeValidation());
 
             if (value.getPath().equals(""))
                 return FormValidation.ok();
 
             if (!value.isDirectory())
                 return FormValidation.error(Messages.AntExec_NotADirectory(value));
 
             File antJar = new File(value, "lib/ant.jar");
             if (!antJar.exists())
                 return FormValidation.error(Messages.AntExec_NotAntDirectory(value));
 
             return FormValidation.ok();
         }
 
         public boolean isApplicable(Class<? extends AbstractProject> aClass) {
             // indicates that this builder can be used with all kinds of project types
             return true;
         }
 
         public String getDisplayName() {
             return hudson.plugins.antexec.Messages.AntExec_DisplayName();
         }
 
         @Override
         public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
             useAntContrib = formData.getBoolean("useAntContrib");
             save();
             return super.configure(req, formData);
         }
 
         public boolean useAntContrib() {
             return useAntContrib;
         }
     }
 
     private static FilePath makeBuildFile(String targetSource, FilePath buildFile) throws IOException, InterruptedException {
         StringBuilder sb = new StringBuilder();
         sb.append("<project default=\"AntExec_Build_Step\" xmlns:antcontrib=\"antlib:net.sf.antcontrib\" basedir=\".\">\n");
         sb.append("<target name=\"AntExec_Build_Step\">\n\n");
         sb.append(targetSource);
         sb.append("\n</target>\n");
         sb.append("</project>\n");
 
         buildFile.write(sb.toString(), null);
         return buildFile;
     }
 }
 
