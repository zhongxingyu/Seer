 package builder.smartfrog;
 
 import builder.smartfrog.SmartFrogAction.State;
 import hudson.Launcher;
 import hudson.model.Build;
 import hudson.model.BuildListener;
 import hudson.model.Descriptor;
 import hudson.tasks.Builder;
 import org.kohsuke.stapler.StaplerRequest;
 /**
  * Sample {@link Builder}.
  *
  * <p>
  * When the user configures the project and enables this builder,
  * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked
  * and a new {@link HelloWorldBuilder} is created. The created
  * instance is persisted to the project configuration XML by using
  * XStream, so this allows you to use instance fields (like {@link #name})
  * to remember the configuration.
  *
  * <p>
  * When a build is performed, the {@link #perform(Build, Launcher, BuildListener)} method
  * will be invoked.
  *
  * @author Kohsuke Kawaguchi
  */
 import hudson.Launcher;
 import hudson.model.Build;
 import hudson.model.BuildListener;
 import hudson.model.Descriptor;
 import hudson.model.Project;
 import hudson.model.Result;
 import hudson.tasks.Builder;
 import hudson.tasks.Shell;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import org.kohsuke.stapler.StaplerRequest;
 
 /**
  * Sample {@link Builder}.
  *
  * <p>
  * When the user configures the project and enables this builder,
  * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked
  * and a new {@link HelloWorldBuilder} is created. The created
  * instance is persisted to the project configuration XML by using
  * XStream, so this allows you to use instance fields (like {@link #name})
  * to remember the configuration.
  *
  * <p>
  * When a build is performed, the {@link #perform(Build, Launcher, BuildListener)} method
  * will be invoked.
  *
  * @author Kohsuke Kawaguchi
  */
 public class SmartFrogBuilder extends Builder implements SmartFrogActionListener {
 
    public static final String ENV_SF_HOME = "SFHOME";
    public static final String ENV_SF_USER_HOME = "SFUSERHOME";
    public static final long HEARTBEAT_PERIOD = 10000;
 
    private String smartFrogName;
    private String hosts;
    private String deployHost;
    private String sfUserHome;
    private String scriptName;
    private String scriptPath;
    private String scriptSource;
    private String scriptContent;
    private String jvmArgs;
    private boolean useAltIni;
    private String sfIni;
 
    private Project project;  
    private transient BuildListener listener;
    private transient boolean componentTerminated = false;
    private transient boolean terminatedNormally;
    
     private String sfUserHome4;
     private String sfUserHome3;
     private String sfUserHome2;
 
    /**
     * We'll use this from the <tt>config.jelly</tt>.
     */
    //public String getName() {
    //  return name;
    //}
 /*
    public boolean perform(Build build, Launcher launcher, BuildListener listener) {
    // this is where you 'build' the project
    // since this is a dummy, we just say 'hello world' and call that a build
    // this also shows how you can consult the global configuration of the builder
    if(DESCRIPTOR.useFrench())
    listener.getLogger().println("Bonjour, "+name+"!");
    else
    listener.getLogger().println("Hello, "+name+"!");
    return true;
    }
     */
    public Descriptor<Builder> getDescriptor() {
       // see Descriptor javadoc for more about what a descriptor is.
       return DESCRIPTOR;
    }
 
    @Override
    public boolean perform(final Build<?, ?> build, final Launcher launcher, final BuildListener listener) throws InterruptedException {

      componentTerminated = false;

       this.listener = listener;
       String[] hostList = hosts.split("[ \t]+");
 
       this.project = build.getProject();
             
       try {
 
          // write deploy script, if needed
          if (scriptSource.equals("content")) {
             File f = getDefaultScriptFile();
             BufferedWriter w = new BufferedWriter(new FileWriter(f));
             w.write(scriptContent);
             w.close();
          }
 
 
       } catch (IOException ioe) {
          listener.getLogger().println("Could not get cannonical path to workspace.");
          build.setResult(Result.FAILURE);
          return false;
       }
 
       SmartFrogAction[] sfActions = new SmartFrogAction[hostList.length];
 
 
       // start daemons
       for (int k = 0; k < hostList.length; k++) {
          String host = hostList[k];
          SmartFrogAction a = new SmartFrogAction(this, host);
          build.addAction(a);
          a.addStateListener(this);
          sfActions[k] = a;
          a.perform(build, launcher);
       }
 
 
       // wait until all daemons are ready
       synchronized (this) {
          boolean allStarted = false;
          do {
             allStarted = true;
             for (SmartFrogAction a : sfActions) {
                if (a.getState() != SmartFrogAction.State.RUNNING) {
                   if (a.getState() == SmartFrogAction.State.FAILED) {
                      listener.getLogger().println("SmartFrog deamon on host " + a.getHost() + " failed.");
                      build.setResult(Result.FAILURE);
                      for (SmartFrogAction act : sfActions) {
                         act.interrupt();
                      }
                      return false;
                   }
                   allStarted = false;
                   break;
                }
             }
             if (allStarted) {
                break;
             }
             try {
                wait();
             } catch (InterruptedException ioe) {
                for (SmartFrogAction a : sfActions) {
                   a.interrupt();
                }
                build.setResult(Result.FAILURE);
                return false;
             }
          } while (allStarted == false);
       }
 
       // deploy terminate hook
       String[] deploySLCl = buildDeployCommandLine(deployHost, getSupportDescPath(), "terminate-hook");
       try {         
          int status = launcher.launch(deploySLCl, build.getEnvVars(), listener.getLogger(),
             build.getParent().getWorkspace()).join();
          if (status != 0) {
             listener.getLogger().println("Deployment of support component failed.");
             build.setResult(Result.FAILURE);
             for (SmartFrogAction act : sfActions) {
                act.interrupt();
             }
             return false;
          }
       } catch (IOException ioe) {
          build.setResult(Result.FAILURE);
          for (SmartFrogAction act : sfActions) {
             act.interrupt();
          }
          return false;
       }      
       
       // deploy script
       String[] deployCl = buildDeployCommandLine(deployHost);
       try {
          int status = launcher.launch(deployCl, build.getEnvVars(), listener.getLogger(),
             build.getParent().getWorkspace()).join();
          if (status != 0) {
             listener.getLogger().println("Deployment failed.");
             build.setResult(Result.FAILURE);
             for (SmartFrogAction act : sfActions) {
                act.interrupt();
             }
             return false;
          }
       } catch (IOException ioe) {
          build.setResult(Result.FAILURE);
          for (SmartFrogAction act : sfActions) {
             act.interrupt();
          }
          return false;
       }
 
       // wait for component termination
       
       synchronized(this) {
          while (! componentTerminated) {
             try {
                wait();
             } catch (InterruptedException ioe) {
                for (SmartFrogAction a : sfActions) {
                   a.interrupt();
                }
                build.setResult(Result.ABORTED);
                return false;
             }
          }
       }
       
       // periodically check for component status, wait until deployment is finished
       /*
       String[] diagCl = buildDiagCommandLine(deployHost);
       boolean running = true;
       while (running) {
          try {
             int status = launcher.launch(diagCl, build.getEnvVars(), listener.getLogger(), build.getParent().getWorkspace()).join();
             running = status == 0;
             synchronized (this) {
                wait(HEARTBEAT_PERIOD);
             }
          } catch (IOException ex) {
             build.setResult(Result.FAILURE);
             for (SmartFrogAction act : sfActions) {
                act.interrupt();
             }
             return false;
          } catch (InterruptedException ie) {
             for (SmartFrogAction a : sfActions) {
                a.interrupt();
             }
             build.setResult(Result.FAILURE);
             return false;
          }
       }
       */
       
       for (SmartFrogAction act : sfActions) {
          act.interrupt();
       }
       build.setResult(terminatedNormally ? Result.SUCCESS : Result.FAILURE);
 
       return true;
    }
    /**
     * Descriptor should be singleton.
     */
    public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();
 
    public String getSmartFrogName() {
       return smartFrogName;
    }
 
    public void setSmartFrogName(String smartFrogName) {
       this.smartFrogName = smartFrogName;
    }
 
    /**
     * Descriptor for {@link HelloWorldBuilder}. Used as a singleton.
     * The class is marked as public so that it can be accessed from views.
     *
     * <p>
     * See <tt>views/hudson/plugins/hello_world/HelloWorldBuilder/*.jelly</tt>
     * for the actual HTML fragment for the configuration screen.
     */
    public static final class DescriptorImpl extends Descriptor<Builder> {
 
       private SmartFrogInstance[] smartfrogInstances = new SmartFrogInstance[0];
 
       DescriptorImpl() {
          super(SmartFrogBuilder.class);
          load();
       }
 
       /**
        * This human readable name is used in the configuration screen.
        */
       public String getDisplayName() {
          return "Deploy SmartFrog component";
       }
 
       @Override
       public boolean configure(StaplerRequest req) throws FormException {
          // to persist global configuration information,
          // set that to properties and call save().
          String[] names = req.getParameterValues("sfname");
          String[] paths = req.getParameterValues("sfpath");
 
          int count = (names == null) ? 0 : names.length;
 
          smartfrogInstances = new SmartFrogInstance[count];
 
          for (int k = 0; k < count; k++) {
             smartfrogInstances[k] = new SmartFrogInstance(names[k], paths[k]);
          }
 
          save();
          return true;
       }
 
       @Override
       public SmartFrogBuilder newInstance(StaplerRequest req) throws hudson.model.Descriptor.FormException {
 
          SmartFrogBuilder sb = new SmartFrogBuilder();
          req.bindParameters(sb, "sb.");
 
          return sb;
       }
 
       public SmartFrogInstance[] getSmartfrogInstances() {
          return smartfrogInstances;
       }
 
       public void setSmartfrogInstances(SmartFrogInstance[] smartfrogInstances) {
          this.smartfrogInstances = smartfrogInstances;
       }
    }
 
    protected String[] buildDaemonCommandLine(String host) {
       String iniPath = useAltIni ? sfIni : getSfInstance().getPath() + "/bin/default.ini";
       return new String[]{Shell.DESCRIPTOR.getShell(), "-xe", getClass().getResource("runSF.sh").getFile(),
          host, getSfInstance().getPath(), sfUserHome, getSupportLibPath(), sfUserHome2, sfUserHome3,sfUserHome4, getWorkspacePath(), getJvmArgs(), iniPath};
    }
 
    protected String[] buildStopDaemonCommandLine(String host) {
       return new String[]{Shell.DESCRIPTOR.getShell(), "-xe", getClass().getResource("stopSF.sh").getFile(),
          host, getSfInstance().getPath(), sfUserHome};
    }
 
    protected String[] buildDeployCommandLine(String host) {
       String deployPath;
       if ((scriptSource != null) && scriptSource.equals("path")) {
          deployPath = scriptPath;
       } else {
          deployPath = getDefaultScriptFile().getPath();
       }
       return buildDeployCommandLine(host, deployPath);
    }
 
    protected String[] buildDeployCommandLine(String host, String deployPath) {
       return buildDeployCommandLine(host, deployPath, scriptName);
    }
 
    protected String[] buildDeployCommandLine(String host, String deployPath, String componentName) {
       return new String[] {Shell.DESCRIPTOR.getShell(), "-xe", getClass().getResource("deploySF.sh").getFile(),
          host, getSfInstance().getPath(), sfUserHome, getSupportLibPath(), sfUserHome2, sfUserHome3,sfUserHome4,
          deployPath, componentName, getWorkspacePath()};
    }
 
    protected String[] buildDiagCommandLine(String host) {
       return new String[]{Shell.DESCRIPTOR.getShell(), "-xe", getClass().getResource("diagnoseSF.sh").getFile(),
          host, getSfInstance().getPath(), sfUserHome, scriptName};
    }
 
    protected SmartFrogInstance getSfInstance() {
       for (SmartFrogInstance i : DESCRIPTOR.getSmartfrogInstances()) {
          if (i.getName().equals(getSmartFrogName())) {
             return i;
          }
       }
       return null;
    }
 
 
    private File getDefaultScriptFile() {
       return new File(getWorkspacePath(), "deployScript.sf");
    }
 
    public String getHosts() {
       return hosts;
    }
 
    public void setHosts(String hosts) {
       this.hosts = hosts;
    }
 
    public String getDeployHost() {
       return deployHost;
    }
 
    public void setDeployHost(String deployHost) {
       this.deployHost = deployHost;
    }
 
    public String getSfUserHome() {
       return sfUserHome;
    }
 
    public void setSfUserHome(String sfUserHome) {
       this.sfUserHome = sfUserHome;
    }
 
    /**
     * added by rhusar - report problems to rhusar@redhat.com
     * 
     * added SFUSERHOMEs 2,3,4 - #1 is used for Support Libs (terminate hooks)
     */
    public String getSfUserHome2() {
       return sfUserHome2;
    }
 
    public void setSfUserHome2(String sfUserHome2) {
       this.sfUserHome2 = sfUserHome2;
    }
 
       public String getSfUserHome3() {
       return sfUserHome3;
    }
 
    public void setSfUserHome3(String sfUserHome3) {
       this.sfUserHome3 = sfUserHome3;
    }
 
       public String getSfUserHome4() {
       return sfUserHome4;
    }
 
    public void setSfUserHome4(String sfUserHome4) {
       this.sfUserHome4 = sfUserHome4;
    }
 
    public String getScriptName() {
       return scriptName;
    }
 
    public void setScriptName(String scriptName) {
       this.scriptName = scriptName;
    }
 
    public String getScriptSource() {
       return scriptSource;
    }
 
    public void setScriptSource(String scriptSource) {
       this.scriptSource = scriptSource;
    }
 
    public String getScriptPath() {
       return scriptPath;
    }
 
    public void setScriptPath(String scriptPath) {
       this.scriptPath = scriptPath;
    }
 
    public String getScriptContent() {
       return scriptContent;
    /*
    File f = getDefaultScriptFile();
    if (! f.exists()) return null;
    try {
    BufferedReader r = new BufferedReader(new FileReader(f));
    StringWriter w = new StringWriter();
    BufferedWriter bw = new BufferedWriter(w);
    String line = r.readLine();
    while (line != null) {
    bw.write(line);
    bw.newLine();
    line = r.readLine();
    }
    bw.close();
    return w.toString();
    } catch (IOException ioe) {
    return null;
    }
     */
    }
 
    public void setScriptContent(String scriptContent) {
       this.scriptContent = scriptContent;
    /*
    File f = getDefaultScriptFile();
    try {
    BufferedWriter w = new BufferedWriter(new FileWriter(f));
    w.write(scriptContent);
    w.close();
    } catch (Exception e) {           
    }
     */
    }
 
    public synchronized void stateChanged(SmartFrogAction action, State newState) {
       notifyAll();
       if (newState == SmartFrogAction.State.RUNNING) {
          listener.getLogger().println("SmartFrog deamon on host " + action.getHost() + " is running.");
       }
    }
 
   /* private void interrupted() {
    } */
 
    public String getJvmArgs() {
       return jvmArgs;
    }
 
    public void setJvmArgs(String jvmArgs) {
       this.jvmArgs = jvmArgs;
    }
    
    public boolean isUseAltIni() {
       return useAltIni;
    }
    
    public void setUseAltIni(boolean useAltIni) {
       this.useAltIni = useAltIni;
    }
 
    public String getSfIni() {
       return sfIni;
    }
    
    public void setSfIni(String sfIni) {
       this.sfIni = sfIni;
    }
    
    private String getWorkspacePath()  {
       try {
          return new File(project.getWorkspace().toURI()).getCanonicalPath();
       } catch (IOException ioe) {         
       } catch (InterruptedException ie) {         
       }
       return null;
    }
    
    private String getSupportLibPath() {
       return getClass().getResource("sf-lib").getFile();
    }
    
    private String getSupportDescPath() {
       return getClass().getResource("hudson-support.sf").getFile();
    }
    
    public synchronized void componentTerminated(boolean normal) {
       this.componentTerminated = true;
       this.terminatedNormally = normal;
       notifyAll();
    }
 }
