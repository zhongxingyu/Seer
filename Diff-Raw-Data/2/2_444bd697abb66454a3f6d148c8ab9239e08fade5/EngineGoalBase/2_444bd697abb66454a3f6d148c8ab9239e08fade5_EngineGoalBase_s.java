 /* Copyright 2009 Kindleit.net Software Development
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package net.kindleit.gae;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Properties;
 
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.project.MavenProject;
 import org.apache.maven.settings.Settings;
 
 import com.google.appengine.tools.KickStart;
 import com.google.appengine.tools.admin.AppCfg;
 
 /** Base MOJO class for working with the Google App Engine SDK.
  *
  * @author rhansen@kindleit.net
  */
 public abstract class EngineGoalBase extends AbstractMojo {
 
   private static final String GAE_PROPS = "gae.properties";
 
   protected static final String[] ARG_TYPE = new String[0];
 
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
   * @parameter expression="${gae.home}" default-value="${settings.localRepository}/com/google/appengine/appengine-java-sdk/${gae.version}/appengine-java-sdk-${gae.version}"
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
 
   protected Properties gaeProperties;
 
   public EngineGoalBase() {
     gaeProperties = new Properties();
     try {
       gaeProperties.load(EngineGoalBase.class.getResourceAsStream(GAE_PROPS));
     } catch (final IOException e) {
       throw new RuntimeException("Unable to load version", e);
     }
   }
 
 
   /** Passes command to the Google App Engine AppCfg runner.
   *
   * @param command command to run through AppCfg
   * @param commandArguments arguments to the AppCfg command.
    * @throws MojoExecutionException If {@link #assureSystemProperties()} fails
   */
   protected final void runAppCfg(final String command,
       final String ... commandArguments) throws MojoExecutionException {
 
     final List<String> args = new ArrayList<String>();
     args.addAll(getAppCfgArgs());
     args.add(command);
     args.addAll(Arrays.asList(commandArguments));
     assureSystemProperties();
     AppCfg.main(args.toArray(ARG_TYPE));
   }
 
   /** Passes command to the Google App Engine KickStart runner.
   *
   * @param startClass command to run through KickStart
   * @param commandArguments arguments to the KickStart command.
   * @throws MojoExecutionException If {@link #assureSystemProperties()} fails
   */
   protected final void runKickStart(final String startClass,
     final String ... commandArguments) throws MojoExecutionException {
 
     final List<String> args = new ArrayList<String>();
     args.add(startClass);
     args.addAll(getCommonArgs());
     args.addAll(Arrays.asList(commandArguments));
 
     assureSystemProperties();
     KickStart.main(args.toArray(ARG_TYPE));
   }
 
 
 
 
 
   /** Groups alterations to System properties for the proper execution
    * of the actual GAE code.
    * @throws MojoExecutionException When the gae.home variable cannot be set. */
   protected void assureSystemProperties() throws MojoExecutionException {
     // explicitly specify SDK root, as auto-discovery fails when
     // appengine-tools-api.jar is loaded from Maven repo, not SDK
     String sdk = System.getProperty("appengine.sdk.root");
     if (sdk == null) {
       if (sdkDir == null) {
         throw new MojoExecutionException(this, "${gae.home} property not set",
            gaeProperties.getProperty("hone_undefined"));
       }
       System.setProperty("appengine.sdk.root", sdk = sdkDir);
     }
 
     if (!new File(sdk).isDirectory()) {
       throw new MojoExecutionException(this, "${gae.home} is not a directory",
           gaeProperties.getProperty("home_invalid"));
     }
 
 
     // hack for getting appengine-tools-api.jar on a runtime classpath
     // (KickStart checks java.class.path system property for classpath entries)
     final String classpath = System.getProperty("java.class.path");
     final String toolsJar = sdkDir + "/lib/appengine-tools-api.jar";
     if (!classpath.contains(toolsJar)) {
       System.setProperty("java.class.path",
           classpath + File.pathSeparator + toolsJar);
     }
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
 
 }
