 /* Copyright 2010 Kindleit.net Software Development
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
  *
  * Includes contributions adapted from the Jetty Maven Plugin
  * Copyright 2000-2004 Mort Bay Consulting Pty. Ltd.
  */
 package net.kindleit.gae;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.PipedInputStream;
 import java.io.PipedOutputStream;
 import java.io.PrintStream;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Properties;
 
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.settings.Proxy;
 import org.apache.maven.settings.Server;
 import org.apache.maven.settings.Settings;
 import org.codehaus.plexus.PlexusConstants;
 import org.codehaus.plexus.PlexusContainer;
 import org.codehaus.plexus.context.Context;
 import org.codehaus.plexus.context.ContextException;
 import org.codehaus.plexus.personality.plexus.lifecycle.phase.Contextualizable;
 
 import com.google.appengine.tools.admin.AppCfg;
 
 /** Base MOJO class for working with the Google App Engine SDK.
  *
  * @author rhansen@kindleit.net
  */
 public abstract class EngineGoalBase extends AbstractMojo implements Contextualizable {
 
   private static final String SECURITY_DISPATCHER_CLASS_NAME = "org.sonatype.plexus.components.sec.dispatcher.SecDispatcher";
 
   private static final String GAE_PROPS = "gae.properties";
 
   private static final String INTERRUPTED_EXCEPTION =
     "Interrupted waiting for process supervisor thread to finish";
 
   protected static final String[] ARG_TYPE = new String[0];
 
   /**
    * Plexus container, needed to manually lookup components.
    *
    * To be able to use Password Encryption
    * http://maven.apache.org/guides/mini/guide-encryption.html
    */
   protected PlexusContainer container;
 
   /** The Maven settings reference.
    *
    * @parameter expression="${settings}"
    * @required
    * @readonly
    */
   protected Settings settings;
 
   /** The character encoding scheme to be applied interacting with the SDK.
    * Sent as the --compile_encoding flag.
    *
    * @parameter expression="${encoding}" default-value="${project.build.sourceEncoding}"
    * @since 0.8.3
    */
   protected String encoding;
 
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
    * @deprecated use maven settings.xml/server/username and "serverId" parameter
    */
   @Deprecated
   protected String emailAccount;
 
   /** The server id in maven settings.xml to use for emailAccount(username)
    * and password when connecting to GAE.
    *
    * If password present in settings "--passin" is set automatically.
    *
    * @parameter expression="${gae.serverId}"
    */
   protected String serverId;
 
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
 
   /** Tell AppCfg to use a proxy.
    *
    * By default will use first active proxy in maven settings.xml
    *
    * @parameter expression="${gae.proxy}"
    */
   protected String proxy;
 
   /** Decides whether to wait after the server is started or to return the
    * execution flow to the user.
    *
    * @parameter expression="${gae.wait}" default-value="false"
    */
   protected boolean wait;
 
   /** Port to listen for stop requests on.
    *
    * @parameter expression="${gae.monitor.port}" default-value="8081"
    */
   protected int monitorPort;
 
   /** Key to provide when making stop requests.
    *
    * @parameter expression="${gae.monitor.key}" default-value="monitor.${project.artifactId}"
    */
   protected String monitorKey;
 
   protected Properties gaeProperties;
 
   public EngineGoalBase() {
     gaeProperties = new Properties();
     try {
       gaeProperties.load(EngineGoalBase.class.getResourceAsStream(GAE_PROPS));
     } catch (final IOException e) {
       throw new RuntimeException("Unable to load version", e);
     }
   }
 
 
   public void contextualize(final Context context) throws ContextException {
       this.container = (PlexusContainer) context.get(PlexusConstants.PLEXUS_KEY);
   }
 
   protected boolean hasServerSettings() {
       if (serverId == null) {
           return false;
       } else {
           final Server srv = settings.getServer(serverId);
           return srv != null;
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
 
     getLog().debug("execute AppCfg " + args.toString());
 
     if (hasServerSettings()) {
       forkPasswordExpectThread(args.toArray(ARG_TYPE),
           decryptPassword(settings.getServer(serverId).getPassword()));
       return;
     }
 
     AppCfg.main(args.toArray(ARG_TYPE));
 
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
             gaeProperties.getProperty("home_undefined"));
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
 
     addEmailOption(args);
     addStringOption(args, "--host=", hostString);
     addProxyOption(args);
     addBooleanOption(args, "--passin", passIn);
     if (!passIn) {
       addBooleanOption(args, "--disable_prompt", !settings.getInteractiveMode());
     }
     addBooleanOption(args, "--enable_jar_splitting", splitJars);
     addBooleanOption(args, "--retain_upload_dir", keepTempUploadDir);
 
     return args;
   }
 
   protected final List<String> getCommonArgs() {
     final List<String> args = new ArrayList<String>(9);
 
     args.add("--sdk_root=" + sdkDir);
    args.add("--compile_encoding=" + encoding);
     addStringOption(args, "--server=", uploadServer);
 
     return args;
   }
 
   private void forkPasswordExpectThread(final String[] args, final String password) {
     getLog().info("Use Settings configuration from server id {" + serverId + "}");
     // Parent for all threads created by AppCfg
     final ThreadGroup threads = new ThreadGroup("AppCfgThreadGroup");
 
      // Main execution Thread that belong to ThreadGroup threads
     final Thread thread = new Thread(threads, "AppCfgMainThread") {
 
         @Override
         public void run() {
             final PrintStream outOrig = System.out;
             final InputStream inOrig = System.in;
 
             final PipedInputStream inReplace = new PipedInputStream();
             OutputStream stdin;
             try {
               stdin = new PipedOutputStream(inReplace);
             } catch (final IOException e) {
                 getLog().error("Unable to redirect input", e);
                 return;
             }
             System.setIn(inReplace);
 
             final BufferedWriter stdinWriter = new BufferedWriter(new OutputStreamWriter(stdin));
 
             System.setOut(new PrintStream(new PasswordExpectOutputStream(threads, outOrig, new Runnable() {
               public void run() {
                   try {
                       stdinWriter.write(password);
                       stdinWriter.newLine();
                       stdinWriter.flush();
                   } catch (final IOException e) {
                       getLog().error("Unable to enter password", e);
                   }
               }}), true));
 
             try {
                 AppCfg.main(args);
             } catch (final Throwable e) {
                 getLog().error("Unable to execute AppCfg", e);
             } finally {
                 System.setOut(outOrig);
                 System.setIn(inOrig);
             }
         }
     };
     thread.start();
     try {
         thread.join();
     } catch (final InterruptedException e) {
         getLog().error(INTERRUPTED_EXCEPTION, e);
     }
   }
 
   private String decryptPassword(final String password) {
     if (password != null) {
       try {
         final Class<?> securityDispatcherClass = container.getClass()
             .getClassLoader().loadClass(SECURITY_DISPATCHER_CLASS_NAME);
         final Object securityDispatcher = container.lookup(
             SECURITY_DISPATCHER_CLASS_NAME, "maven");
         final Method decrypt = securityDispatcherClass.getMethod("decrypt",
             String.class);
 
         return (String) decrypt.invoke(securityDispatcher, password);
 
       } catch (final Exception e) {
         getLog().warn("security features are disabled. Cannot find plexus security dispatcher", e);
       }
     }
     getLog().debug("password could not be decrypted");
     return password;
   }
 
   private void addEmailOption(final List<String> args) {
     if (hasServerSettings() && emailAccount == null) {
         addStringOption(args, "--email=",
             settings.getServer(serverId).getUsername());
         if (settings.getServer(serverId).getPassword() != null) {
             // Force GAE tools to read from System.in instead of System.console()
             passIn = true;
         }
     } else {
         addStringOption(args, "--email=", emailAccount);
     }
   }
 
   private void addProxyOption(final List<String> args) {
     if (hasServerSettings() && proxy == null) {
         final Proxy activCfgProxy = settings.getActiveProxy();
         if (activCfgProxy != null) {
             addStringOption(args, "--proxy=",
                 activCfgProxy.getHost() + ":" + activCfgProxy.getPort());
         }
     } else {
         addStringOption(args, "--proxy=", proxy);
     }
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
