 /**
  * Copyright 1&1 Internet AG, https://github.com/1and1/
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package net.oneandone.maven.plugins.prerelease.util;
 
 import net.oneandone.maven.plugins.prerelease.core.Prerelease;
 import net.oneandone.sushi.fs.World;
 import net.oneandone.sushi.fs.file.FileNode;
 import net.oneandone.sushi.util.Separator;
 import org.apache.maven.artifact.repository.ArtifactRepository;
 import org.apache.maven.execution.DefaultMavenExecutionRequest;
 import org.apache.maven.execution.ExecutionListener;
 import org.apache.maven.execution.MavenExecutionRequest;
 import org.apache.maven.execution.MavenExecutionResult;
 import org.apache.maven.execution.MavenSession;
 import org.apache.maven.model.building.ModelBuildingRequest;
 import org.apache.maven.plugin.logging.Log;
 import org.apache.maven.project.DefaultProjectBuildingRequest;
 import org.apache.maven.project.MavenProject;
 import org.apache.maven.project.MavenProjectHelper;
 import org.apache.maven.project.ProjectBuilder;
 import org.apache.maven.project.ProjectBuildingException;
 import org.apache.maven.project.ProjectBuildingRequest;
 import org.apache.maven.project.ProjectBuildingResult;
 import org.apache.maven.settings.Mirror;
 import org.apache.maven.settings.Proxy;
 import org.apache.maven.settings.Server;
 import org.codehaus.plexus.DefaultPlexusContainer;
 import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
 import org.codehaus.plexus.logging.Logger;
 import org.sonatype.aether.RepositorySystemSession;
 import org.sonatype.aether.artifact.Artifact;
 
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 public class Maven {
     private final World world;
     private final MavenSession parentSession;
     private final ExecutionListener executionListener;
     private final MavenProjectHelper projectHelper;
     private final RepositorySystemSession repositorySession;
     private final List<ArtifactRepository> remoteLegacy; // needed to load poms :(
 
     // TODO: use a project builder that works without legacy classes, esp. without ArtifactRepository ...
     // As far as I know, there's no such project builder in mvn 3.0.2.
     private final ProjectBuilder builder;
 
     public Maven(World world, MavenSession parentSession, ExecutionListener executionListener, MavenProjectHelper projectHelper,
                  RepositorySystemSession repositorySession, ProjectBuilder builder, List<ArtifactRepository> remoteLegacy) {
         this.world = world;
         this.parentSession = parentSession;
         this.executionListener = executionListener;
         this.projectHelper = projectHelper;
         this.repositorySession = repositorySession;
         this.builder = builder;
         this.remoteLegacy = remoteLegacy;
     }
 
     public FileNode getLocalRepositoryDir() {
         return world.file(repositorySession.getLocalRepository().getBasedir());
     }
 
     public FileNode getLocalRepositoryFile(Artifact artifact) {
         return getLocalRepositoryDir().join(repositorySession.getLocalRepositoryManager().getPathForLocalArtifact(artifact));
     }
 
     public ExecutionListener getExecutionListener() {
         return executionListener;
     }
 
     public void build(FileNode basedir, String ... goals) throws Exception {
         build(basedir, new HashMap<String, String>(), executionListener, false, goals);
     }
 
     public void build(FileNode basedir, Map<String, String> userProperties, String ... goals) throws Exception {
         build(basedir, userProperties, executionListener, false, goals);
     }
 
     /**
      * Creates an DefaultMaven instance, initializes it form parentRequest (in Maven, this is done by MavenCli - also by
      * loading settings).
      */
     public void build(FileNode basedir, Map<String, String> userProperties, ExecutionListener theExecutionListener, boolean filter, String ... goals) throws BuildException {
         MavenExecutionRequest parentRequest;
         org.apache.maven.Maven maven;
         MavenExecutionRequest request;
         MavenExecutionResult result;
         BuildException exception;
         PatchedBuilderCommon bc;
 
         parentRequest = parentSession.getRequest();
         try {
             maven = parentSession.getContainer().lookup(org.apache.maven.Maven.class);
         } catch (ComponentLookupException e) {
             throw new IllegalStateException(e);
         }
         request = new DefaultMavenExecutionRequest();
         request.setLoggingLevel(parentRequest.getLoggingLevel());
         request.setUserSettingsFile(parentRequest.getUserSettingsFile());
         request.setGlobalSettingsFile(parentRequest.getGlobalSettingsFile());
         request.setUserToolchainsFile(parentRequest.getUserToolchainsFile());
         request.setShowErrors(parentRequest.isShowErrors());
 
         request.setOffline(parentRequest.isOffline());
         request.setInteractiveMode(parentRequest.isInteractiveMode());
         request.setPluginGroups(parentRequest.getPluginGroups());
         request.setLocalRepository(parentRequest.getLocalRepository());
 
         for (Server server : parentRequest.getServers()) {
             server = server.clone();
             request.addServer( server );
         }
         for (Proxy proxy : parentRequest.getProxies()) {
             if (proxy.isActive()) {
                 request.addProxy(proxy.clone());
             }
         }
 
         for (Mirror mirror : parentRequest.getMirrors()) {
             request.addMirror(mirror.clone());
         }
 
         request.setActiveProfiles(parentRequest.getActiveProfiles());
         for ( org.apache.maven.model.Profile profile : parentRequest.getProfiles() ) {
             request.addProfile(profile.clone());
         }
 
         request.setPom(basedir.join("pom.xml").toPath().toFile());
         request.setGoals(Arrays.asList(goals));
         request.setBaseDirectory(basedir.toPath().toFile());
         request.setSystemProperties(parentRequest.getSystemProperties());
         request.setUserProperties(merged(parentRequest.getUserProperties(), userProperties));
         request.setExecutionListener(theExecutionListener);
         request.setUpdateSnapshots(parentRequest.isUpdateSnapshots());
         request.setTransferListener(parentRequest.getTransferListener());
 
         bc = PatchedBuilderCommon.install(parentSession.getContainer(), filter);
         Logger logger = getLogger((DefaultPlexusContainer) parentSession.getContainer());
         setOutput(logger, indentPrintStream("  "));
         logger.info("[" + basedir + "] mvn " + props(request.getUserProperties()) + Separator.SPACE.join(goals));
         try {
             result = maven.execute(request);
         } finally {
             bc.uninstall();
             setOutput(logger, System.out);
         }
         exception = null;
         for (Throwable e : result.getExceptions()) {
             if (exception == null) {
                 exception = new BuildException("build failed: " + e, e);
             } else {
                 exception.addSuppressed(e);
             }
         }
         if (exception != null) {
             throw exception;
         }
     }
 
     /** @return with tailing space */
     private static String props(Properties props) {
         StringBuilder builder;
 
         builder = new StringBuilder();
         for (Map.Entry<Object, Object> entry : props.entrySet()) {
            builder.append("-D").append(entry.getKey()).append('=').append(entry.getValue());
             builder.append(' ');
         }
         return builder.toString();
     }
 
     //--
 
     private static PrintStream indentPrintStream(final String prefix) {
         return new PrintStream(System.out) {
             private boolean start = true;
             public void print(String str) {
                 if (start) {
                     super.print(prefix);
                     start = false;
                 }
                 super.print(str);
             }
 
             public void println(String str) {
                 print(str);
                 super.println();
                 start = true;
             }
         };
     }
 
     private static Logger getLogger(DefaultPlexusContainer container) {
         return container.getLoggerManager().getLoggerForComponent("notused");
     }
 
     private static void setOutput(Logger logger, PrintStream dest) {
         try {
             logger.getClass().getDeclaredMethod("setStream", PrintStream.class).invoke(logger, dest);
         } catch (Exception e) {
             throw new IllegalStateException(e);
         }
     }
 
     //--
 
     private static Properties merged(Properties left, Map<String, String> right) {
         Properties result;
 
         result = new Properties(left);
         result.putAll(right);
         return result;
     }
 
     public void deployOnly(Log log, Prerelease prerelease) throws Exception {
         PromoteExecutionListener listener;
 
         listener = new PromoteExecutionListener(prerelease, projectHelper, executionListener);
         try {
            build(prerelease.checkout, new HashMap<String, String>(), listener, true, "deploy");
         } catch (BuildException e) {
             if (listener.isFirstSuccess()) {
                 log.warn("Promote succeeded: your artifacts have been deployed, and your svn tag was created. ");
                 log.warn("However, some optional deploy goals failed with this exception:");
                 log.warn(e);
                 log.warn("Thus, you can use your release, but someone should have a look at this exception.");
             }
         }
     }
 
     public List<FileNode> files(List<Artifact> artifacts) {
         List<FileNode> result;
 
         result = new ArrayList<>();
         for (Artifact a : artifacts) {
             result.add(file(a));
         }
         return result;
     }
 
     public FileNode file(Artifact artifact) {
         return world.file(artifact.getFile());
     }
 
     //-- load poms -- for testing only
 
     public MavenProject loadPom(FileNode file) throws ProjectBuildingException {
         ProjectBuildingRequest request;
         ProjectBuildingResult result;
 
         request = new DefaultProjectBuildingRequest();
         request.setRepositorySession(repositorySession);
         request.setRemoteRepositories(remoteLegacy);
         request.setProcessPlugins(false);
         request.setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL);
         request.setSystemProperties(System.getProperties());
         //If you don't turn this into RepositoryMerging.REQUEST_DOMINANT the dependencies will be resolved against Maven Central
         //and not against the configured repositories. The default of the DefaultProjectBuildingRequest is
         // RepositoryMerging.POM_DOMINANT.
         request.setRepositoryMerging(ProjectBuildingRequest.RepositoryMerging.REQUEST_DOMINANT);
         request.setResolveDependencies(false);
         result = builder.build(file.toPath().toFile(), request);
         return result.getProject();
     }
 }
