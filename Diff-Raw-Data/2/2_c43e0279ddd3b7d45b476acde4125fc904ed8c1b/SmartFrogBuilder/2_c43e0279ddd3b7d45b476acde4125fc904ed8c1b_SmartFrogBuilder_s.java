 /*
  * JBoss, Home of Professional Open Source.
  * Copyright 2012, Red Hat, Inc., and individual contributors
  * as indicated by the @author tags. See the copyright.txt file in the
  * distribution for a full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package builder.smartfrog;
 
 import hudson.Extension;
 import hudson.FilePath;
 import hudson.Launcher;
 import hudson.matrix.Combination;
 import hudson.matrix.MatrixConfiguration;
 import hudson.model.BuildListener;
 import hudson.model.Result;
 import hudson.model.AbstractBuild;
 import hudson.model.Descriptor;
 import hudson.tasks.Builder;
 import hudson.util.ListBoxModel;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import net.sf.json.JSONObject;
 
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.StaplerRequest;
 
 import builder.smartfrog.SmartFrogAction.State;
 import builder.smartfrog.util.ConsoleLogger;
 import builder.smartfrog.util.Functions;
 
 /**
  * SmartFrog Hudson/Jenkins plugin main.
  * 
  * @author <a href="mailto:rhusar@redhat.com">Radoslav Husar</a>
  * @author vjuranek
  * 
  */
 public class SmartFrogBuilder extends Builder implements SmartFrogActionListener {
 
     public static final String ENV_SF_HOME = "SFHOME";
     public static final String ENV_SF_USER_HOME = "SFUSERHOME";
     public static final long HEARTBEAT_PERIOD = 10000;
 
     private String smartFrogName;
     private String deployHost;
     private String hosts;
     // SF is able to accept only 4 additional variables (class paths)
     private String sfUserHome;
     private String sfUserHome2;
     private String sfUserHome3;
     private String sfUserHome4;
     private String sfOpts;
     private String builderId;
     private String sfIni;
     private boolean useAltIni;
     private ScriptSource sfScriptSource;
 
     // private transient BuildListener listener;
     private transient SmartFrogInstance sfInstance;
     private transient ConsoleLogger console;
     private transient String exportMatrixAxes;
     private transient boolean componentTerminated;
     private transient boolean terminatedNormally;
 
     //backward compatibility variables
     @Deprecated
     private transient String scriptName;
     @Deprecated
     private transient String scriptPath;
     @Deprecated
     private transient String scriptSource;
     @Deprecated
     private transient String scriptContent;
 
     
     @DataBoundConstructor
     public SmartFrogBuilder(String smartFrogName, String deployHost, String hosts, String sfUserHome,
             String sfUserHome2, String sfUserHome3, String sfUserHome4, String sfOpts, String builderId, boolean useAltIni, String sfIni,
             ScriptSource sfScriptSource) {
         this.smartFrogName = smartFrogName;
         this.deployHost = deployHost;
         this.hosts = hosts;
         this.sfUserHome = sfUserHome;
         this.sfUserHome2 = sfUserHome2;
         this.sfUserHome3 = sfUserHome3;
         this.sfUserHome4 = sfUserHome4;
         this.sfOpts = sfOpts;
         this.builderId = builderId;
         this.useAltIni = useAltIni;
         this.sfIni = sfIni;
         this.sfScriptSource = sfScriptSource;
         this.sfInstance = getDescriptor().getSFInstanceByName(smartFrogName);
     }
 
     protected Object readResolve(){
         sfInstance = getDescriptor().getSFInstanceByName(smartFrogName);
         if(sfInstance == null)
             LOGGER.info("Smart Frog instance namned " + smartFrogName + " doesn't exists, job needs to be reconfigured!");
         //backward compatibility
         if(scriptSource != null){
             if(scriptSource.equals("path"))
                 sfScriptSource = new FileScriptSource(scriptName,scriptPath);
             if(scriptSource.equals("content"))
                 sfScriptSource = new StringScriptSource(scriptName,scriptContent);
         }
         return this;
     }
     
     public String getSmartFrogName() {
         return smartFrogName;
     }
 
     public String getDeployHost() {
         return deployHost;
     }
 
     public String getHosts() {
         return hosts;
     }
 
     public String getSfUserHome() {
         return sfUserHome;
     }
 
     // Additional SFUSERHOMEs, since 1 is used for Support Libs (terminate hooks).
     public String getSfUserHome2() {
         return sfUserHome2;
     }
 
     public String getSfUserHome3() {
         return sfUserHome3;
     }
 
     public String getSfUserHome4() {
         return sfUserHome4;
     }
 
     public String getSfOpts() {
         return sfOpts;
     }
 
     public String getSfIni() {
         return sfIni;
     }
 
     public String getBuilderId() {
         return builderId;
     }
     
     public boolean isUseAltIni() {
         return useAltIni;
     }
 
     public ScriptSource getSfScriptSource() {
         return sfScriptSource;
     }
 
     public SmartFrogInstance getSfInstance() {
         return sfInstance;
     }
     
     @Override
     public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
         throws IOException, InterruptedException  {
         // transient fields need to be initialize
         exportMatrixAxes = "";
         componentTerminated = false;
         console = new ConsoleLogger(listener);
         //reload SF Instance in case global config has changed
         sfInstance = getDescriptor().getSFInstanceByName(smartFrogName);
         // check if SF script exists or create new one
         if (!prepareScript(build))
             return false;
         
         // Export Matrix parameters if matrix project block
         if (build.getProject() instanceof MatrixConfiguration)
             exportMatrixAxes = exportMatrixAxes(build);
 
         // create daemons and run them
         SmartFrogAction[] sfActions = createDaemons(build, launcher);
         // wait until all daemons are ready
         if(!daemonsReady(sfActions)){
             failBuild(build, sfActions);
             return false;
         }
         // deploy terminate hook
         if(!deployTerminateHook(build, launcher)){
             failBuild(build, sfActions);
             return false;
         }
         // deploy script
         if(!deployScript(build, launcher)){
             failBuild(build, sfActions);
             return false;
         }
         // wait for component termination
         if(!waitForCompletion()){
             build.setResult(Result.ABORTED);
             killAllDaemons(sfActions);
             return false;
         }
         // terminate daemons
         killAllDaemons(sfActions);
         
         build.setResult(terminatedNormally ? Result.SUCCESS : Result.FAILURE); //TODO really setup result??
         return true;
     }
 
     private boolean prepareScript(AbstractBuild<?, ?> build) throws InterruptedException {
         
         // create temporary file with SF script 
         try {
             sfScriptSource.createScriptFile(build);
         } catch (IOException ioe) {
             log("[SmartFrog] ERROR: Could not get canonical path to workspace:" + ioe);
             ioe.printStackTrace();
             build.setResult(Result.FAILURE);
             return false;
         }
         
         // verify that file really exists in workspace
         FilePath fp = new FilePath(build.getWorkspace(), sfScriptSource.getDefaultScriptPath());
         try {
             if ( !fp.exists()) {
                 log("[SmartFrog] ERROR: Script file " + fp.getName() + " doesn't exists on channel" + fp.getChannel() + "!");
                 build.setResult(Result.FAILURE);
                 return false;
             }
         } catch (IOException e) {
             log("[SmartFrog] ERROR: failed to verify that " + fp.getName() + " exists on channel" + fp.getChannel() + "! IOException cought, check Jenkins log for more details");
             LOGGER.log(Level.INFO, "SmartFrog error: failed to verify that " + fp.getName() + " exists on channel" + fp.getChannel() + "!", e);
             build.setResult(Result.FAILURE);
             return false;
         }
         
         return true;
     }
 
     private String exportMatrixAxes(AbstractBuild<?, ?> build) {
         //TODO String builder
         String exportedMatrixAxes = " ";
         MatrixConfiguration matrix = (MatrixConfiguration) build.getProject();
         Combination combinations = matrix.getCombination();
         // Add only "SF_" prefixed variables.
         for (Map.Entry<String, String> entry : combinations.entrySet()) {
             if (entry.getKey().startsWith("SF_")) {
                 exportedMatrixAxes = exportedMatrixAxes + "export " + entry.getKey() + "=" + entry.getValue() + "; ";
             }
         }
         return exportedMatrixAxes;
     }
 
     /**
      * Create daemons and run them 
      */
     private SmartFrogAction[] createDaemons(AbstractBuild<?, ?> build, Launcher launcher) throws IOException, InterruptedException {
         String[] hostList = hosts.split("[ \t]+");
         SmartFrogAction[] sfActions = new SmartFrogAction[hostList.length];
         // start daemons
         for (int k = 0; k < hostList.length; k++) {
             String host = hostList[k];
             SmartFrogAction a = new SmartFrogAction(this, host, builderId);
             build.addAction(a);
             a.addStateListener(this);
             sfActions[k] = a;
             //TODO improve logging
             a.perform(build, launcher, console);
         }
         return sfActions;
     }
 
     /**
      * Waits for all daemons to be ready, if one of them fails, fail whole build
      */
     private synchronized boolean daemonsReady(SmartFrogAction[] sfActions) {
         boolean allStarted = false;
         do {
             allStarted = true;
             for (SmartFrogAction a : sfActions) {
                 if (a.getState() == SmartFrogAction.State.FAILED) {
                     log("[SmartFrog] ERROR: SmartFrog deamon on host " + a.getHost() + " failed.");
                     return false;
                 }
                 if (a.getState() == SmartFrogAction.State.STARTING) {
                     allStarted = false;
                     break; 
                 }
             }
             
             if (allStarted) 
                 break;
             
             try {
                 wait();
             } catch (InterruptedException ioe) {
                 log("[SmartFrog] ERROR: Interrupted.");
                 return false;
             }
         } while (allStarted == false);
         log("[SmartFrog] INFO: All Smart Frog daemons are running ...");
         return true;
     }
     
     private boolean deployTerminateHook(AbstractBuild<?, ?> build, Launcher launcher) {
         String[] deploySLCl = buildDeployCommandLine(deployHost, sfInstance.getSupportScriptPath(), "terminate-hook", Functions.convertWsToCanonicalPath(build.getWorkspace()));
         try {
             int status = launcher.launch().cmds(deploySLCl).envs(build.getEnvironment(console.getListener())).stdout(console.getListener()).pwd(build.getWorkspace()).join();
             if (status != 0) {
                 log("[SmartFrog] ERROR: Deployment of support component failed.");
                 return false;
             }
         } catch (IOException ioe) {
             return false;
         } catch (InterruptedException e){
             return false;
         }
         log("[SmartFrog] INFO: Support component deployed ...");
         return true;
     }
 
     private boolean deployScript(AbstractBuild<?, ?> build, Launcher launcher) {
         String defaultScriptPath = sfScriptSource != null ? sfScriptSource.getDefaultScriptPath() : "";
         String[] deployCl = buildDeployCommandLine(deployHost, defaultScriptPath, sfScriptSource.getScriptName(),
                 Functions.convertWsToCanonicalPath(build.getWorkspace()));
         try {
             int status = launcher.launch().cmds(deployCl).envs(build.getEnvironment(console.getListener())).stdout(console.getListener()).pwd(build.getWorkspace()).join();
             if (status != 0) {
                 log("[SmartFrog] ERROR: Deployment failed.");
                 return false;
             }
         } catch (IOException ioe) {
             return false;
         } catch (InterruptedException e){
             return false;
         }
         log("[SmartFrog] INFO: SF script deployed ...");
         return true;
     }
     
     private synchronized boolean waitForCompletion() {
         while (!componentTerminated) {
             try {
                 wait();
             } catch (InterruptedException ioe) {
                 return false;
             }
         }
         log("[SmartFrog] INFO: Component terminated");
         return true;
     }
     
     private void killAllDaemons(SmartFrogAction[] sfActions) {
         for (SmartFrogAction a : sfActions) {
             //if(a.getState() == SmartFrogAction.State.FAILED || a.getState() == SmartFrogAction.State.FAILED)
             a.interrupt();
         }
     }
     
     private void failBuild(AbstractBuild<?, ?> build, SmartFrogAction[] sfActions) {
         build.setResult(Result.FAILURE);
         killAllDaemons(sfActions);
     }
     
     protected String[] buildDaemonCommandLine(String host, String workspace) {
         String iniPath = useAltIni ? sfIni : sfInstance.getPath() + "/bin/default.ini";
         return new String[] { "bash", "-xe", sfInstance.getSupport() + "/runSF.sh", host, sfInstance.getPath(),
                 sfUserHome, sfInstance.getSupport(), sfUserHome2, sfUserHome3, sfUserHome4, workspace, getSfOpts(),
                 iniPath, exportMatrixAxes };
     }
 
     protected String[] buildStopDaemonCommandLine(String host) {
         return new String[] { "bash", "-xe", sfInstance.getSupport() + "/stopSF.sh", host, sfInstance.getPath(),
                 sfUserHome };
     }
 
     protected String[] buildKilleThemAllCommandLine(String host) {
         return new String[] { "bash", "-xe", sfInstance.getSupport() + "/killThemAll.sh", host};
     }
     
     protected String[] buildDeployCommandLine(String host, String scriptPath, String componentName, String workspace) {
         return new String[] { "bash", "-xe", sfInstance.getSupport() + "/deploySF.sh", host, sfInstance.getPath(),
                 sfUserHome, sfInstance.getSupport(), sfUserHome2, sfUserHome3, sfUserHome4, scriptPath, //sfInstance.getSupportScriptPath(),
                 componentName, workspace, exportMatrixAxes };
     }
 
     public synchronized void stateChanged() {
         notifyAll();
     }
     
     public synchronized void stateChanged(SmartFrogAction action, State newState) {
         notifyAll();
     }
 
     public synchronized void componentTerminated(boolean normal) {
         componentTerminated = true;
         terminatedNormally = normal;
         notifyAll();
     }
 
     private void log(String message){
         console.logAnnot(message);
         //console.log(message);
     }
     
     @Override
     public DescriptorImpl getDescriptor() {
         return (DescriptorImpl) super.getDescriptor();
     }
 
     @Extension
     public static final class DescriptorImpl extends Descriptor<Builder> {
 
         private List<SmartFrogInstance> smartfrogInstances = new ArrayList<SmartFrogInstance>();
 
         public DescriptorImpl() {
             super(SmartFrogBuilder.class);
             load();
         }
 
         public List<SmartFrogInstance> getSmartfrogInstances() {
             return smartfrogInstances;
         }
 
         public SmartFrogInstance getSFInstanceByName(String name) {
             for (SmartFrogInstance sf : smartfrogInstances) {
                 if (sf.getName().equals(name))
                     return sf;
             }
             return null;
         }
 
         public String getDisplayName() {
             return "Deploy SmartFrog component";
         }
 
         @Override
         public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
             smartfrogInstances.clear();
             smartfrogInstances.addAll(req.bindJSONToList(SmartFrogInstance.class, json.get("smartfrogInstances")));
             save();
             return super.configure(req, json);
         }
 
         public ListBoxModel doFillSmartFrogNameItems() {
             ListBoxModel lb = new ListBoxModel();
             for (SmartFrogInstance sf : smartfrogInstances) {
                 lb.add(sf.getName(), sf.getName());
             }
             return lb;
         }
 
     }
 
     private static final Logger LOGGER = Logger.getLogger(SmartFrogBuilder.class.getName());
 }
