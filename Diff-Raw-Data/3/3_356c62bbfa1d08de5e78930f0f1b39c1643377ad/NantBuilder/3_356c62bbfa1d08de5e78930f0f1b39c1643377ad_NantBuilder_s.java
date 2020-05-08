 package hudson.plugins.nant;
 
 import hudson.CopyOnWrite;
 import hudson.Launcher;
 import hudson.Launcher.LocalLauncher;
 import hudson.Util;
 import hudson.model.AbstractBuild;
 import hudson.model.AbstractProject;
 import hudson.model.Build;
 import hudson.model.BuildListener;
 import hudson.model.Descriptor;
 import hudson.model.TaskListener;
 import hudson.remoting.Callable;
 import hudson.tasks.Builder;
 import hudson.util.ArgumentListBuilder;
 import hudson.util.FormFieldValidator;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.StaplerRequest;
 import org.kohsuke.stapler.StaplerResponse;
 
 import javax.servlet.ServletException;
 import java.io.File;
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.Map;
 
 import net.sf.json.JSONObject;
 
 /**
  * Sample {@link Builder}.
  *
  * <p>
  * When the user configures the project and enables this builder,
  * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked
  * and a new {@link NantBuilder} is created. The created
  * instance is persisted to the project configuration XML by using
  * XStream, so this allows you to use instance fields (like {@link #nantName})
  * to remember the configuration.
  *
  * <p>
  * When a build is performed, the {@link #perform(Build, Launcher, BuildListener)} method
  * will be invoked. 
  * 
  * @author kyle.sweeney@valtech.com
  *
  */
 public class NantBuilder extends Builder {
 
 	/**
 	 * A whitespace separated list of nant targets to be run
 	 */
     private final String targets;
     
     /**
      * The location of the nant build file to run
      */
 	private final String nantBuildFile;
 	
     /**
      * Identifies {@link NantInstallation} to be used.
      */
     private final String nantName;
     
 	/**
 	 * When this builder is created in the project configuration step,
 	 * the builder object will be created from the strings below.
 	 * @param nantBuildFile	The name/location of the nant build fild
 	 * @param targets Whitespace separated list of nant targets to run
 	 */
     @DataBoundConstructor
     public NantBuilder(String nantBuildFile,String nantName, String targets) {
     	super();
     	if(nantBuildFile==null || nantBuildFile.trim().length()==0)
     		this.nantBuildFile = "";
     	else
     		this.nantBuildFile = nantBuildFile;
     	
     	this.nantName = nantName;
     	
     	if(targets == null || targets.trim().length()==0)
     		this.targets = "";
     	else
     		this.targets = targets;	
     }
     
     /**
      * Gets the NAnt to invoke,
      * or null to invoke the default one.
      */
     public NantInstallation getNant() {
         for( NantInstallation i : DESCRIPTOR.getInstallations() ) {
             if(nantName!=null && i.getName().equals(nantName))
                 return i;
         }
         return null;
     }
 
     /**
      * We'll use these from the <tt>config.jelly</tt>.
      */
     public String getTargets() {
         return targets;
     }
     public String getNantBuildFile(){
     	return nantBuildFile;
     }
     public String getNantName(){
     	return nantName;
     }
 
     public boolean perform(AbstractBuild<?,?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
         AbstractProject proj = build.getProject();
         ArgumentListBuilder args = new ArgumentListBuilder();
         
         String execName;
         if(launcher.isUnix())
             execName = "NAnt";
         else
             execName = "NAnt.exe";
 
         //Get the path to the nant installation
         NantInstallation ni = getNant();
         if(ni==null) {
             args.add(execName);
         } else {
             args.add(ni.getExecutable(launcher));
         }
         
         //If a nant build file is specified, then add it as an argument, otherwise
         //nant will search for any file that ends in .build
         if(nantBuildFile != null && nantBuildFile.trim().length() > 0){
         	args.add("-buildfile:"+nantBuildFile);
         }
         
         //Remove all tabs, carriage returns, and newlines and replace them with
         //whitespaces, so that we can add them as parameters to the executable
         String normalizedTarget = targets.replaceAll("[\t\r\n]+"," ");
         if(normalizedTarget.trim().length()>0)
         	args.addTokenized(normalizedTarget);
         
         //According to the Ant builder source code, in order to launch a program 
         //from the command line in windows, we must wrap it into cmd.exe.  This 
         //way the return code can be used to determine whether or not the build failed.
         if(!launcher.isUnix()) {
             args.prepend("cmd.exe","/C");
             args.add("&&","exit","%%ERRORLEVEL%%");
         }
 
         //Try to execute the command
     	listener.getLogger().println("Executing command: "+args.toString());
     	Map<String,String> env = build.getEnvVars();
         try {
             int r = launcher.launch(args.toCommandArray(),env,listener.getLogger(),proj.getModuleRoot()).join();
             return r==0;
         } catch (IOException e) {
             Util.displayIOException(e,listener);
             e.printStackTrace( listener.fatalError("command execution failed") );
             return false;
         }
     }
 
     public Descriptor<Builder> getDescriptor() {
         // see Descriptor javadoc for more about what a descriptor is.
         return DESCRIPTOR;
     }
 
     /**
      * Descriptor should be singleton.
      */
     public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();
 
     /**
      * Descriptor for {@link NantBuilder}. Used as a singleton.
      * The class is marked as public so that it can be accessed from views.
      */
     public static final class DescriptorImpl extends Descriptor<Builder> {
         /**
          * To persist global configuration information,
          * simply store it in a field and call save().
          *
          * <p>
          * If you don't want fields to be persisted, use <tt>transient</tt>.
          */
     	public static String PARAMETERNAME_PATH_TO_NANT = "pathToNant";
     	
     	@CopyOnWrite
         private volatile NantInstallation[] installations = new NantInstallation[0];
         
         
 
     	private DescriptorImpl() {
             super(NantBuilder.class);
             load();
         }
     	
     	@Override
         protected void convert(Map<String,Object> oldPropertyBag) {
             if(oldPropertyBag.containsKey("installations"))
                 installations = (NantInstallation[]) oldPropertyBag.get("installations");
         }
 
         /**
          * This human readable name is used in the configuration screen.
          */
         public String getDisplayName() {
             return Messages.NantBuilder_DisplayName();
         }
         
         public NantInstallation[] getInstallations() {
             return installations;
         }
         
         @Override
         public boolean configure(StaplerRequest req) throws FormException{
         	// to persist global configuration information,
             // set that to properties and call save().
             
             int i;
             String[] names = req.getParameterValues("nant_name");
             String[] homes = req.getParameterValues("nant_home");
             int len;
             if(names!=null && homes!=null)
                 len = Math.min(names.length,homes.length);
             else
                 len = 0;
             NantInstallation[] insts = new NantInstallation[len];
 
             for( i=0; i<len; i++ ) {
                 if(names[i].length()==0 || homes[i].length()==0)    continue;
                 insts[i] = new NantInstallation(names[i],homes[i]);
             }
 
             this.installations = insts;
             
             save();
             return true;
         }
 
        public Builder newInstance(StaplerRequest req, JSONObject o) {
            return req.bindParameters(NantBuilder.class,"nantBuilder.");
        }
         //
         // web methods
         //
         
         /**
          * Checks if the NANT_HOME is valid.
          */
         public void doCheckNantHome( StaplerRequest req, StaplerResponse rsp ) throws IOException, ServletException {
             // this can be used to check the existence of a file on the server, so needs to be protected
             new FormFieldValidator(req,rsp,true) {
                 public void check() throws IOException, ServletException {
                     File f = getFileParameter("value");
                     if(!f.isDirectory()) {
                         error(f+" is not a directory");
                         return;
                     }
 
                     File nantExe = new File(f,"bin/NAnt.exe");
                     if(!nantExe.exists()) {
                         error(f+" is not a NAnt installation directory.");
                         return;
                     }
 
                     ok();
                 }
             }.process();
         }
     }
     
     public static final class NantInstallation implements Serializable {
         private final String name;
         private final String nantHome;
 
         public NantInstallation(String name, String nantHome) {
             this.name = name;
             this.nantHome = nantHome;
         }
 
         /**
          * install directory.
          */
         public String getNantHome() {
             return nantHome;
         }
 
         /**
          * Human readable display name.
          */
         public String getName() {
             return name;
         }
 
         public String getExecutable(Launcher launcher) throws IOException, InterruptedException {
             return launcher.getChannel().call(new Callable<String,IOException>() {
                 public String call() throws IOException {
                     File exe = getExeFile();
                     if(exe.exists())
                         return exe.getPath();
                     throw new IOException(exe.getPath()+" doesn't exist");
                 }
             });
         }
 
         private File getExeFile() {
             String execName;
             if(File.separatorChar=='\\')
                 execName = "NAnt.exe";
             else
                 execName = "NAnt";
 
             return new File(getNantHome(),"bin/"+execName);
         }
 
         /**
          * Returns true if the executable exists.
          */
         public boolean getExists() throws IOException, InterruptedException {
             LocalLauncher launcher = new LocalLauncher(TaskListener.NULL);
             return launcher.getChannel().call(new Callable<Boolean,IOException>() {
                 public Boolean call() throws IOException {
                     return getExeFile().exists();
                 }
             });
         }
 
         private static final long serialVersionUID = 1L;
     }
 }
