 /*
  * RHQ Management Platform
  * Copyright 2013, Red Hat Middleware LLC, and individual contributors
  * as indicated by the @author tags. See the copyright.txt file in the
  * distribution for a full listing of individual contributors.
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
  */
 
 package org.rhq.maven.plugins;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.artifact.factory.ArtifactFactory;
 import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
 import org.apache.maven.artifact.repository.ArtifactRepository;
 import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
 import org.apache.maven.artifact.resolver.ArtifactResolutionException;
 import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
 import org.apache.maven.artifact.resolver.ArtifactResolver;
 import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.plugins.annotations.Component;
 import org.apache.maven.plugins.annotations.LifecyclePhase;
 import org.apache.maven.plugins.annotations.Mojo;
 import org.apache.maven.plugins.annotations.Parameter;
 import org.apache.maven.plugins.annotations.ResolutionScope;
 import org.apache.maven.project.MavenProject;
 
 import static org.codehaus.plexus.util.StringUtils.join;
 import static org.rhq.maven.plugins.Utils.findParentPlugins;
 import static org.rhq.maven.plugins.Utils.getAgentPluginArchiveFile;
 import static org.rhq.maven.plugins.Utils.redirectOuput;
 
 /**
  * Validates a freshly built RHQ Agent Plugin.
  *
  * @author Thomas Segismont
  */
 @Mojo(name = "validate", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope
         .COMPILE, threadSafe = true)
 public class ValidateMojo extends AbstractMojo {
 
     private static final String PLUGIN_VALIDATOR_MODULE_GROUP_ID = "org.rhq";
     private static final String PLUGIN_VALIDATOR_MODULE_ARTIFACT_ID = "rhq-core-plugin-container";
     private static final String PLUGIN_VALIDATOR_MAIN_CLASS = "org.rhq.core.pc.plugin.PluginValidator";
     /**
      * The build directory (root of build works)
      */
     @Parameter(defaultValue = "${project.build.directory}", required = true, readonly = true)
     private File buildDirectory;
 
     /**
      * The name of the generated RHQ agent plugin archive
      */
     @Parameter(defaultValue = "${project.build.finalName}", required = true, readonly = true)
     private String finalName;
 
     /**
      * Version of the RHQ Plugin Container API
      */
     @Parameter(required = true)
     private String rhqVersion;
 
     /**
      * Whether to fail the build if an error occurs while validating the plugin.
      */
     @Parameter(defaultValue = "true")
     private boolean failOnError;
 
     /**
      * Whether to skip the execution of this mojo.
      */
     @Parameter(defaultValue = "false")
     private boolean skipValidate;
 
     @Parameter(defaultValue = "${project.remoteArtifactRepositories}", required = true, readonly = true)
     private List remoteRepositories;
 
     @Parameter(defaultValue = "${localRepository}", required = true, readonly = true)
     private ArtifactRepository localRepository;
 
     @Component
     private MavenProject project;
 
     @Component
     private ArtifactFactory artifactFactory;
 
     @Component
     private ArtifactResolver artifactResolver;
 
     @Component
     private ArtifactMetadataSource artifactMetadataSource;
 
     @Override
     public void execute() throws MojoExecutionException, MojoFailureException {
         if (skipValidate) {
             getLog().info("Skipped execution");
         }
         File agentPluginArchive = getAgentPluginArchiveFile(buildDirectory, finalName);
         if (!agentPluginArchive.exists() && agentPluginArchive.isFile()) {
             throw new MojoExecutionException("Agent plugin archive does not exist: " + agentPluginArchive);
         }
         Set<File> parentPlugins;
         try {
             parentPlugins = findParentPlugins(project);
         } catch (IOException e) {
             throw new MojoExecutionException("Error while searching for parent plugins", e);
         }
         validate(agentPluginArchive, parentPlugins);
     }
 
     private void validate(File agentPluginArchive, Set<File> parentPlugins) throws MojoExecutionException {
         // Run the plugin validator as a forked process
         Process process = null;
         try {
             String pluginValidatorClasspath = buildPluginValidatorClasspath(agentPluginArchive, parentPlugins);
             String javaCommand = buildJavaCommand();
             ProcessBuilder processBuilder = buildProcessBuilder(javaCommand, pluginValidatorClasspath);
             process = processBuilder.start();
             redirectOuput(process);
             int exitCode = process.waitFor();
             if (exitCode != 0) {
                 handleFailure("Invalid plugin");
             }
         } catch (Exception e) {
             handleException(e);
         } finally {
             if (process != null) {
                 process.destroy();
             }
         }
     }
 
     private String buildPluginValidatorClasspath(File agentPluginArchive, Set<File> parentPlugins) throws
             ArtifactNotFoundException, ArtifactResolutionException,
             IOException {
         // Resolve dependencies of the plugin validator module
         Artifact dummyOriginatingArtifact =
                 artifactFactory.createBuildArtifact("org.apache.maven.plugins", "maven-downloader-plugin", "1.0",
                         "jar");
         Artifact pluginContainerArtifact = this.artifactFactory.createArtifact(
                 PLUGIN_VALIDATOR_MODULE_GROUP_ID, PLUGIN_VALIDATOR_MODULE_ARTIFACT_ID, rhqVersion,
                 null, "jar");
         ArtifactResolutionResult artifactResolutionResult = artifactResolver.resolveTransitively(Collections
                 .singleton(pluginContainerArtifact), dummyOriginatingArtifact, localRepository,
                 remoteRepositories, artifactMetadataSource, null);
 
         List<String> classpathElements = new LinkedList<String>();
 
         // Add all compile scope dependencies to the classpath
         Iterator iterator = artifactResolutionResult.getArtifacts().iterator();
         ScopeArtifactFilter artifactFilter = new ScopeArtifactFilter(Artifact.SCOPE_COMPILE);
         while (iterator.hasNext()) {
             Artifact artifact = (Artifact) iterator.next();
             if (!artifact.isOptional() && artifact.getType().equals("jar") && artifactFilter.include(artifact)) {
                 classpathElements.add(artifact.getFile().getAbsolutePath());
             }
         }
 
         classpathElements.add(agentPluginArchive.getAbsolutePath());
 
         for (File parentPlugin : parentPlugins) {
             classpathElements.add(parentPlugin.getAbsolutePath());
         }
 
         String pluginValidatorClasspath = join(classpathElements.iterator(), File.pathSeparator);
 
         if (getLog().isDebugEnabled()) {
             getLog().debug("pluginValidatorClasspath = " + pluginValidatorClasspath);
         }
 
         return pluginValidatorClasspath;
     }
 
     private String buildJavaCommand() {
         String javaHome = System.getProperty("java.home");
         String javaCommand = javaHome + File.separator + "bin" + File.separator + "java";
         if (getLog().isDebugEnabled()) {
             getLog().debug("javaCommand = " + javaCommand);
         }
         return javaCommand;
     }
 
     private ProcessBuilder buildProcessBuilder(String javaCommand, String pluginValidatorClasspath) {
         List<String> command = new LinkedList<String>();
         command.add(javaCommand);
         command.add("-classpath");
         command.add(pluginValidatorClasspath);
         command.add("-Dorg.apache.commons.logging.Log=org.apache.commons.logging.impl.SimpleLog");
         command.add(PLUGIN_VALIDATOR_MAIN_CLASS);
         if (getLog().isDebugEnabled()) {
             getLog().debug("Built command to execute = " + join(command.iterator(), " "));
         }
         return new ProcessBuilder().directory(buildDirectory).command(command);
     }
 
     private void handleFailure(String message) throws MojoFailureException {
         if (failOnError) {
             throw new MojoFailureException(message);
         }
         getLog().error(message);
     }
 
     private void handleException(Exception e) throws MojoExecutionException {
         if (failOnError) {
             throw new MojoExecutionException(e.getMessage(), e);
         }
         getLog().error(e.getMessage(), e);
     }
 }
