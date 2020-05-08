 package net.kindleit.gae;
 
 import java.io.File;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.project.MavenProject;
 import org.apache.maven.settings.Settings;
 
 import sun.misc.JarFilter;
 
import com.google.appengine.tools.KickStart;
import com.google.appengine.tools.admin.AppCfg;

 /** Base MOJO class for working with the Google App Engine SDK.
  *
  * @author rhansen@kindleit.net
  */
 public abstract class EngineGoalBase extends AbstractMojo {
   public static final String PLUGIN_VERSION="0.9";
 
   protected static final String[] ARG_TYPE = new String[0];
 
   private static final String APPCFG_CLASS =
     "com.google.appengine.tools.admin.AppCfg";
   private static final String KICKSTART_CLASS =
     "com.google.appengine.tools.admin.KickStart";
 
   private static final String SDK_LIBS_NOTFOUND =
     "AppEngine tools SDK could not be found";
   private static final String SDK_APPCFG_NOTFOUND =
     "AppCfg Class not found at: " + APPCFG_CLASS;
   private static final String SDK_KICKSTART_NOTFOUND =
     "KickStart Class not found at: " + KICKSTART_CLASS;
 
   private static URLClassLoader classLoader;
 
 
   /** The Maven project reference.
    *
    * @parameter expression="${project}"
    * @required
    * @readonly
    */
   protected MavenProject project;
 
   /** The Maven settings reference.
   *
   * @parameter expression="${settings}"
   * @required
   * @readonly
   */
  protected Settings settings;
 
   /** Overrides where the Project War Directory is located.
    *
    * @parameter expression="${project.build.directory}/${project.build.finalName}"
    * @required
    */
   protected String appDir;
 
   /** Specifies where the Google App Engine SDK is located.
   *
   * @parameter expression="${appengine.sdk.root}" default-value="${project.build.directory}/appEngine"
   * @required
   */
  protected String sdkDir;
 
   /** Split large jar files (> 10M) into smaller fragments.
    *
    * @parameter expression="${gae.deps.split}" default-value="false"
    */
   protected boolean splitJars;
 
   /** The username to use. Will prompt if omitted.
    *
    * @parameter expression="${gae.email}"
    */
   protected String emailAccount;
 
   /** The server to connect to.
    *
    * @parameter expression="${gae.server}"
    */
   protected String uploadServer;
 
   /** Overrides the Host header sent with all RPCs.
    *
    * @parameter expression="${gae.host}"
    */
   protected String hostString;
 
   /** Do not delete temporary directory used in uploading.
    *
    * @parameter expression="${gae.keepTemps}" default-value="false"
    */
   protected boolean keepTempUploadDir;
 
   /** Always read the login password from stdin.
    *
    * @parameter expression="${gae.passin}" default-value="false"
    */
   protected boolean passIn;
 
 
   /** Passes command to the Google App Engine AppCfg runner.
   *
   * @param command command to run through AppCfg
   * @param commandArguments arguments to the AppCfg command.
   */
   protected final void runAppCfg(final String command,
       final String ... commandArguments) {
 
     final List<String> args = new ArrayList<String>();
     args.addAll(getAppCfgArgs());
     args.add(command);
     args.addAll(Arrays.asList(commandArguments));
 
     AppCfg.main(args.toArray(ARG_TYPE));
   }
 
   /** Passes command to the Google App Engine KickStart runner.
   *
   * @param startClass command to run through KickStart
   * @param commandArguments arguments to the KickStart command.
   */
   protected final void runKickStart(final String startClass,
     final String ... commandArguments) {
 
     final List<String> args = new ArrayList<String>();
     args.add(startClass);
     args.addAll(getCommonArgs());
     args.addAll(Arrays.asList(commandArguments));
 
     KickStart.main(args.toArray(ARG_TYPE));
   }
 
   /** Generate all common Google AppEngine Task Parameters for use in all the
    * goals.
    *
    * @return List of arguments to add.
    */
   protected final List<String> getAppCfgArgs () {
     final List<String> args = getCommonArgs();
 
 
     addBooleanOption(args, "--disable_prompt", !settings.getInteractiveMode());
 
     addStringOption(args, "--email=", emailAccount);
     addStringOption(args, "--host=", hostString);
     addBooleanOption(args, "--passin", passIn);
     addBooleanOption(args, "--enable_jar_splitting", splitJars);
     addBooleanOption(args, "--retain_upload_dir", keepTempUploadDir);
 
     return args;
   }
 
   protected final List<String> getCommonArgs() {
     final List<String> args = new ArrayList<String>(8);
 
     args.add("--sdk_root=" + sdkDir);
     addStringOption(args, "--server=", uploadServer);
 
     return args;
   }
 
   private final void addBooleanOption(final List<String> args, final String key,
       final boolean var) {
     if (var) {
       args.add(key);
     }
   }
 
   private final void addStringOption(final List<String> args, final String key,
       final String var) {
     if (var != null && var.length() > 0) {
       args.add(key + var);
     }
   }
 
   protected Class<?> getEngineClass(final String clsName,
       final String clsErrorMsg) throws MojoExecutionException {
     try {
       return getClassLoader().loadClass(clsName);
     } catch (final ClassNotFoundException e) {
       throw new MojoExecutionException(clsErrorMsg, e);
     }
   }
 
   private ClassLoader getClassLoader() throws MojoExecutionException {
     synchronized (EngineGoalBase.class) {
       if (classLoader != null) {
         return classLoader;
       }
     }
     synchronized (EngineGoalBase.class) {
       try {
         final List<URL> urls = new ArrayList<URL>();
 
         for(final File jar : getSdkLibDir().listFiles(new JarFilter())) {
           urls.add(jar.toURI().toURL());
         }
 
         classLoader = new URLClassLoader(urls.toArray(new URL[0]));
       } catch (final MalformedURLException e) {
         throw new MojoExecutionException(SDK_LIBS_NOTFOUND, e);
       }
       return classLoader;
     }
   }
 
   private File getSdkLibDir() {
     return null;
   }
 
 }
