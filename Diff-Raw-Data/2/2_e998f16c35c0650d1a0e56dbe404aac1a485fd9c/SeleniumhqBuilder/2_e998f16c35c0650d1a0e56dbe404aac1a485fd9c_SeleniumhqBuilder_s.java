 package hudson.plugins.seleniumhq;
 
 import hudson.Extension;
 import hudson.FilePath;
 import hudson.Launcher;
 import hudson.model.AbstractBuild;
 import hudson.model.BuildListener;
 import hudson.model.Descriptor;
 import hudson.model.Result;
 import hudson.tasks.Builder;
 import hudson.util.FormValidation;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import net.sf.json.JSONObject;
 
 import org.apache.commons.io.FileUtils;
 import org.kohsuke.stapler.DataBoundConstructor;
 import org.kohsuke.stapler.QueryParameter;
 import org.kohsuke.stapler.StaplerRequest;
 
 /**
  * Sample {@link Builder}.
  * 
  * <p>
  * When the user configures the project and enables this builder,
  * {@link DescriptorImpl#newInstance(StaplerRequest)} is invoked and a new {@link SeleniumhqBuilder}
  * is created. The created instance is persisted to the project configuration XML by using XStream,
  * so this allows you to use instance fields (like {@link #name}) to remember the configuration.
  * 
  * <p>
  * When a build is performed, the {@link #perform(Build, Launcher, BuildListener)} method will be
  * invoked.
  * 
  * @author Pascal Martin
  */
 public class SeleniumhqBuilder extends Builder {
 
     private final String browser;
     private final String startURL;
     private final String suiteFile;
     private final String resultFile;
     private final String other;
 
     @DataBoundConstructor
     public SeleniumhqBuilder(String browser, String startURL, String suiteFile, String resultFile,
             String other) {
         this.browser = browser;
         this.startURL = startURL;
         this.suiteFile = suiteFile;
         this.resultFile = resultFile;
         this.other = other;
     }
 
     /**
      * We'll use this from the <tt>config.jelly</tt>.
      */
     public String getBrowser() {
         return browser;
     }
 
     /**
      * We'll use this from the <tt>config.jelly</tt>.
      */
     public String getStartURL() {
         return startURL;
     }
 
     /**
      * We'll use this from the <tt>config.jelly</tt>.
      */
     public String getSuiteFile() {
         return suiteFile;
     }
 
     /**
      * We'll use this from the <tt>config.jelly</tt>.
      */
     public String getOther() {
         return other;
     }
 
     /**
      * We'll use this from the <tt>config.jelly</tt>.
      */
     public String getResultFile() {
         return resultFile;
     }
 
     /**
      * Check if the suiteFile is a URL
      * 
      * @return true if the suiteFile is a valid url else return false
      */
     public boolean isURLSuiteFile() {
         try {
             URL url = new URL(this.suiteFile);
             return url != null;
         } catch (Exception e) {
             return false;
         }
     }
 
     /**
      * Check if the suiteFile is a file
      * 
      * @return true if the suiteFile is a filePath else return false
      * @throws InterruptedException 
      * @throws IOException 
      */
     public boolean isFileSuiteFile(AbstractBuild<?,?> build, Launcher launcher) throws IOException, InterruptedException {
         FilePath suiteFilePath = new FilePath(build.getWorkspace(), this.suiteFile);               
         if (suiteFilePath.exists())
         {
         	return suiteFilePath.isDirectory() == false;
         }
         else 
         {
         	suiteFilePath = new FilePath(launcher.getChannel(), this.suiteFile);
         	if (suiteFilePath.exists()) 
             {
             	return suiteFilePath.isDirectory() == false;
             }        	   
         }
         return false;
     }
 
     @Override
     public boolean perform(AbstractBuild<?,?> build, Launcher launcher, BuildListener listener)
             throws IOException, InterruptedException {
     	
         // -------------------------------
         // Check global config
         // -------------------------------
         if (!DESCRIPTOR.isGoodSeleniumRunner()) {
             listener.error("Please configure the Selenium Remote Control htmlSuite Runner in admin of hudson");
             build.setResult(Result.FAILURE);
             return false;
         }
 
         // -------------------------------
         // Check projet config
         // -------------------------------
         if (this.getBrowser() == null || this.getBrowser().length() == 0) {
             listener.error("Build config : browser field is mandatory");
             build.setResult(Result.FAILURE);
             return false;
         }
         if (this.getStartURL() == null || this.getStartURL().length() == 0) {
             listener.error("Build config : startURL field is mandatory");
             build.setResult(Result.FAILURE);
             return false;
         }
         if (this.getSuiteFile() == null || this.getSuiteFile().length() == 0) {
             listener.error("Build config : suiteFile field is mandatory");
             build.setResult(Result.FAILURE);
             return false;
         }
         if (this.getResultFile() == null || this.getResultFile().length() == 0) {
             listener.error("Build config : resultFile field is mandatory");
             build.setResult(Result.FAILURE);
             return false;
         }
 
          // -------------------------------
          // check suiteFile type url or file
          // -------------------------------
          String suiteFile = null;
          FilePath tempSuite = null;
          if (this.isFileSuiteFile(build, launcher)) 
          {        	 
         	 FilePath suiteFilePath = new FilePath(build.getWorkspace(), this.suiteFile);             
         	 if (suiteFilePath.exists() == false) // File exist on remote
              {
         		 suiteFilePath = new FilePath(launcher.getChannel(), this.suiteFile);        		  
         	 }
         	 suiteFile = suiteFilePath.getRemote(); 
          } 
          else if (this.isURLSuiteFile()) 
          {        	         	 
         	 tempSuite = build.getWorkspace().createTempFile("tempHtmlSuite", "html");
         	 suiteFile = tempSuite.getRemote();
         	 try
         	 {
         	     File localWorkspace = new File(build.getRootDir(), "workspace");
         	     File tempSuiteLocal = localWorkspace.createTempFile("tempHtmlSuite", "html");
         	     
         		 listener.getLogger().println("Try downloading suite file on master");
         		 listener.getLogger().println("    from url : " + this.suiteFile );
         		 listener.getLogger().println("    to file  : " + tempSuiteLocal.getPath());
         		         	        		 
         		 FileUtils.copyURLToFile(new URL(this.suiteFile), tempSuiteLocal);
      			 listener.getLogger().println("    ...");
     			 listener.getLogger().println("    Succeed");
         		 
         		 listener.getLogger().println("Try transfer suite file on slave");
         		 listener.getLogger().println("    from file : " + tempSuiteLocal.getPath() );
         		 listener.getLogger().println("    to file   : " + suiteFile);
         		 
         		 FilePath sourceFile = new FilePath(tempSuiteLocal);
         		 sourceFile.copyTo(tempSuite);
      			 listener.getLogger().println("    ...");
     			 listener.getLogger().println("    Succeed");
         		 sourceFile.delete();
         	 }
         	 catch(Exception e)
         	 {
         		 listener.error("Downloading suite file from url failed ! Check your build configuration. ");
                  build.setResult(Result.FAILURE);
                  return false;         	 
         	 }
          }
          else 
          {
 	         // The suiteFile it is a unsuported type
 	         listener.error("The suiteFile is not a file or an url ! Check your build configuration.");
 	         build.setResult(Result.FAILURE);
 	         return false;
          }
 
         // -------------------------------
         // launch : java -jar selenium-server.jar [other] -htmlSuite "{browser}" "{startURL}"
         // "{suiteFile}" "{resultFile}"
         // -------------------------------
         String seleniumRunner = FileUtil.getExecutableAbsolutePath(DESCRIPTOR.getSeleniumRunner());
 
         FilePath resultFilePath = new FilePath(build.getWorkspace(), this.resultFile);
         resultFilePath.getParent().mkdirs();
         String resultFile = resultFilePath.getRemote();
 
         ArrayList cmd = new ArrayList();
         cmd.add("java");
         cmd.add("-jar");
         cmd.add(seleniumRunner);
         cmd.addAll(this.getOthers());
         cmd.add("-htmlSuite");
         cmd.add(this.getBrowser());
         cmd.add(this.getStartURL());
         cmd.add(suiteFile);
         cmd.add(resultFile);
                 
         try 
         {
                 String javaCmdString = "";
                 Iterator<String> itr = cmd.iterator();
                 while(itr.hasNext())
                 {
                     javaCmdString += " " + itr.next();
                 }
 
                 listener.getLogger().println(javaCmdString);
         	launcher.launch().cmds(cmd).envs(build.getEnvironment(listener)).stdout(listener.getLogger()).pwd(build.getWorkspace()).join();
         	return true;
         } 
         catch (IOException e) 
         {
             e.printStackTrace();
             listener.getLogger().println("IOException!");
             return false;
         } 
         catch (InterruptedException e) 
         {
             e.printStackTrace();
             listener.getLogger().println("InterruptedException!");
             return false;
         }
         finally         
         {		
             // -------------------------------
             // Delete the temp suite file
             // -------------------------------
             if (tempSuite != null)
                 tempSuite.delete();
         }        
     }
 
     /**
      * @desc    get the arrayList of optional parameters
      *
      * @return  ArrayList containing parameters
      */
     private final ArrayList getOthers()
     {
         ArrayList cmdParams = new ArrayList();
 
         // fix https://issues.jenkins-ci.org/browse/JENKINS-7246 caused by patch in https://issues.jenkins-ci.org/browse/JENKINS-6996
         // (do not use "this.getOther().isEmpty()", to be compatible with jdk 1.5)
        if (this.getOther() != null && this.getOther().length != 0)
         {
             String otherParams = this.getOther();
             String[] otherParamsArray = otherParams.split(" ");
 
             for (int i = 0; i < otherParamsArray.length; ++i)
             {
                 // Leave spaces between quotes
                 if (otherParamsArray[i].matches("\""))
                 {
                     String paramBuffer = otherParamsArray[i];
                     do
                     {
                         ++i;
                         paramBuffer += otherParamsArray[i];
 
                     } while ((!otherParamsArray[i].matches("\"")) && (i < otherParamsArray.length));
 
                     cmdParams.add(paramBuffer);
                 }
                 else if(otherParamsArray[i].matches("\'"))
                 {
                     String paramBuffer = otherParamsArray[i];
                     do
                     {
                         ++i;
                         paramBuffer += otherParamsArray[i];
 
                     } while ((!otherParamsArray[i].matches("\'")) && (i < otherParamsArray.length));
 
                     cmdParams.add(paramBuffer);
                 }
                 else
                 {
                     cmdParams.add(otherParamsArray[i]);
                 }
             }
         }
 
         return cmdParams;
     }
 
     @Extension
     public static final DescriptorImpl DESCRIPTOR = new DescriptorImpl();
 
     /**
      * Descriptor for {@link SeleniumhqBuilder}. Used as a singleton. The class is marked as public
      * so that it can be accessed from views.
      * 
      * <p>
      * See <tt>views/hudson/plugins/hello_world/HelloWorldBuilder/*.jelly</tt> for the actual HTML
      * fragment for the configuration screen.
      */
     public static final class DescriptorImpl extends Descriptor<Builder> {
         /**
          * To persist global configuration information, simply store it in a field and call save().
          * 
          * <p>
          * If you don't want fields to be persisted, use <tt>transient</tt>.
          */
         private String seleniumRunner;
 
         DescriptorImpl() {
             super(SeleniumhqBuilder.class);
             load();
         }
 
         /**
          * Performs on-the-fly validation of the form field 'name'.
          * 
          * @param value
          *            This receives the current value of the field.
          */
         public FormValidation doCheckSeleniumRunner(@QueryParameter final String value) {
             return FormValidation.validateExecutable(value);
         }
 
         /**
          * This human readable name is used in the configuration screen.
          */
         public String getDisplayName() {
             return "SeleniumHQ htmlSuite Run";
         }
 
         @Override
         public boolean configure(StaplerRequest req, JSONObject o) throws FormException {
             // to persist global configuration information,
             // set that to properties and call save().
             seleniumRunner = o.getString("seleniumRunner");
             save();
             return super.configure(req, o);
         }
 
         public String getSeleniumRunner() {
             return seleniumRunner;
         }
 
         /**
          * For junit test
          * 
          * @param seleniumRunner
          */
         public void setSeleniumRunner(String seleniumRunner) {
             this.seleniumRunner = seleniumRunner;
         }
 
         public boolean isGoodSeleniumRunner() {
             return this.seleniumRunner != null && this.seleniumRunner.length() > 0;
         }
     }
 }
