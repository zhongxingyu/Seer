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
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Set;
 
 import net.kindleit.gae.runner.KickStartRunner;
 
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.artifact.repository.ArtifactRepository;
 import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
 import org.apache.maven.artifact.resolver.ArtifactResolutionException;
 import org.apache.maven.artifact.resolver.ArtifactResolver;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 
 /**
  * Runs the WAR project locally on the Google App Engine development server,
  * without executing the package phase first.
  *
  * This is intended to be included in your project's POM and runs in the
  * pre-integration-test phase.
  *
  * @author tmoore@incrementalism.net
  * @goal start
  * @requiresDependencyResolution runtime
  * @phase pre-integration-test
  * @since 0.5.8
  */
 public class StartGoal extends EngineGoalBase {
 
   /**
    * Used to look up Artifacts in the remote repository.
    *
    * @component
    */
   private ArtifactResolver resolver;
 
   /** This plugins dependent artifacts.
    *
    * @parameter expression="${project.pluginArtifacts}"
    * @required
    * @readonly
    */
   private Set<Artifact> plugins;
 
   /**
    * Location of the local repository.
    *
    * @parameter expression="${localRepository}"
    * @readonly
    * @required
    */
   private ArtifactRepository localRepo;
 
   /**
    * List of Remote Repositories used by the resolver
    *
    * @parameter expression="${project.remoteArtifactRepositories}"
    * @readonly
    * @required
    */
   private List<ArtifactRepository> remoteRepos;
 
   /** Port to run in.
    *
    * @parameter expression="${gae.port}" default-value="8080"
    */
   protected int port;
 
   /** Address to bind to.
    *
    * @parameter expression="${gae.address}" default-value="0.0.0.0"
    */
   protected String address;
 
   /** Do not check for new SDK versions.
    *
    * @parameter expression="${gae.disableUpdateCheck}" default-value="false"
    */
   protected boolean disableUpdateCheck;
 
   /** Arbitrary list of JVM Flags to send to the KickStart task.
    *
    * @parameter
    */
   protected List<String> jvmFlags;
 
   /** Optional javaAgent parameter prepended to the jvmFalgs.
    *
    * @parameter expression="${gae.javaAgent}"
    */
   protected String javaAgent;
 
  /** 
   * Optional javaAgentSkipNoVerify parameter to not add noverify flag.  Only
   * honored if javaAgent is set.
   *
   * @parameter expression="${gae.javaAgentSkipNoVerify}" default-value=false
   */
  protected boolean javaAgentSkipNoVerify;

   @Override
   public void execute() throws MojoExecutionException, MojoFailureException {
     final List<String> arguments = new ArrayList<String>();
 
     arguments.add("--address=" + address);
     arguments.add("--port=" + port);
     if (disableUpdateCheck) {
       arguments.add("--disable_update_check");
     }
     if (goalArguments != null) {
       arguments.addAll(goalArguments);
     }
     if(javaAgent != null) {
		if (!javaAgentSkipNoVerify) {
      	  arguments.add("--jvm_flag=-noverify");
	    }
       arguments.add("--jvm_flag=-javaagent:" + javaAgent);
     }
     if (jvmFlags != null) {
       for (final String jvmFlag : jvmFlags) {
         arguments.add("--jvm_flag=" + jvmFlag);
       }
     }
     arguments.add(appDir);
 
     runKickStart("com.google.appengine.tools.development.DevAppServerMain",
         arguments.toArray(new String[]{}));
   }
 
   /** Passes command to the Google App Engine KickStart runner.
    *
    * @param startClass command to run through KickStart
    * @param commandArguments arguments to the KickStart command.
    * @throws MojoExecutionException If {@link #ensureSystemProperties()} fails
    */
   protected final void runKickStart(final String startClass,
       final String ... commandArguments) throws MojoExecutionException {
 
     final List<String> args = new ArrayList<String>();
     args.add(startClass);
     args.addAll(getCommonArgs());
     args.addAll(Arrays.asList(commandArguments));
 
     ensureSystemProperties();
 
     try {
       resolveArtifacts(plugins);
       KickStartRunner.createRunner(wait, plugins, gaeProperties, getLog())
       .start(monitorPort, monitorKey, args);
     } catch (final Exception e) {
       throw new MojoExecutionException(e.getMessage(), e);
     }
   }
 
   protected final void resolveArtifacts(final Set<Artifact> artifacts)
       throws MojoExecutionException {
     for (final Artifact pluginA : artifacts) {
       try {
         resolver.resolve(pluginA, remoteRepos, localRepo);
       } catch (final ArtifactResolutionException e) {
         throw new MojoExecutionException("Failure Resolving Artifacts", e);
       } catch (final ArtifactNotFoundException e) {
         throw new MojoExecutionException("Failure Resolving Artifacts", e);
       }
     }
   }
 
 }
