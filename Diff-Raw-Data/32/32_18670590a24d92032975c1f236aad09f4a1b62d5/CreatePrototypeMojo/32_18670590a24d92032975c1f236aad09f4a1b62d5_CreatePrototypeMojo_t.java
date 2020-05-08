 /*
  * Copyright 2007 The Kuali Foundation
  * 
  * Licensed under the Educational Community License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * http://www.opensource.org/licenses/ecl2.php
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.kualigan.maven.plugins.kfs;
 
 import org.kualigan.maven.plugins.api.PrototypeHelper;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.OptionBuilder;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.PosixParser;
 
 import org.apache.maven.archetype.Archetype;
 import org.apache.maven.artifact.repository.ArtifactRepository;
 import org.apache.maven.artifact.repository.ArtifactRepositoryFactory;
 import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
 import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
 
 import org.apache.maven.shared.invoker.DefaultInvocationRequest;
 import org.apache.maven.shared.invoker.DefaultInvoker;
 import org.apache.maven.shared.invoker.InvocationOutputHandler;
 import org.apache.maven.shared.invoker.InvocationRequest;
 import org.apache.maven.shared.invoker.InvocationResult;
 import org.apache.maven.shared.invoker.Invoker;
 import org.apache.maven.shared.invoker.InvokerLogger;
 import org.apache.maven.shared.invoker.MavenInvocationException;
 
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugins.annotations.Component;
 import org.apache.maven.plugins.annotations.Mojo;
 import org.apache.maven.plugins.annotations.Parameter;
 import org.apache.maven.project.MavenProject;
 
 import org.codehaus.plexus.archiver.Archiver;
 import org.codehaus.plexus.archiver.ArchiverException;
 import org.codehaus.plexus.archiver.UnArchiver;
 import org.codehaus.plexus.archiver.manager.ArchiverManager;
 import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;
 import org.codehaus.plexus.archiver.util.DefaultFileSet;
 import org.codehaus.plexus.components.io.fileselectors.IncludeExcludeFileSelector;
 
 import org.codehaus.plexus.components.interactivity.Prompter;
 import org.codehaus.plexus.util.FileUtils;
 import org.codehaus.plexus.util.IOUtil;
 import org.codehaus.plexus.util.StringUtils;
 import org.codehaus.plexus.util.cli.CommandLineUtils;
 
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.StringTokenizer;
 
 /**
  * Creates a prototype from the given KFS project resource. A KFS project resource can be either
  * of the following:
  * <ul>
  *   <li>KFS war file</li>
  *   <li>KFS project directory with source</li>
  *   <li>KFS svn repo</li>
  * </ul>
  * 
  */
  @Mojo(
      name="create-prototype",
      requiresProject = false
      )
 public class CreatePrototypeMojo extends AbstractMojo {
     @Component
     private PrototypeHelper helper;
 
     /**
      */
     @Parameter(property="localRepository")
     protected ArtifactRepository localRepository;
     
     /**
      * Path for where the KFS instance is we want to migrate
      * 
      */
     @Parameter(property="kfs.local.path")
     protected String kfsPath;
 
     /**
      */
     @Parameter(property="packageName",defaultValue="org.kuali.kfs")
     protected String packageName;
 
     /**
      */
     @Parameter(property="groupId",defaultValue="org.kuali.kfs")
     protected String groupId;
 
     /**
      */
     @Parameter(property="artifactId",defaultValue="kfs")
     protected String artifactId;
 
     /**
      */
     @Parameter(property="version",defaultValue="5.0")
     protected String version;
 
     /**
      * WAR file to create a prototype from. Only used when creating a prototype from a war.
      */
     @Parameter(property="file")
     protected File file;
 
     /**
      * Assembled sources file.
      */
     @Parameter(property="sources")
     protected File sources;
 
     /**
      */
     @Parameter(property="project")
     protected MavenProject project;
     
     /**
      */
     @Parameter(property="repositoryId")
     protected String repositoryId;
 
     /**
      * The {@code M2_HOME} parameter to use for forked Maven invocations.
      *
      */
     @Parameter(defaultValue = "${maven.home}")
     protected File mavenHome;
     
     /**
      * <p>Create a prototype</p>
      * 
      * <p>The following are the steps for creating a prototype from a KFS instance</p>
      * <p>
      * When using a war file:
      * <ol>
      *   <li>Basically, use the install-file mojo and generate a POM from the archetype</li>
      * </ol>
      * </p>
      * <p>When using an svn repo:
      * <ol>
      *   <li>Checkout the source.</li>
      *   <li>Run migrate on it.</li>
      * </ol>
      * </p>
      * <p>When using a local path:
      * <ol>
      *   <li>Delegate to migrate</li>
      * </ol>
      * </p>
      * 
      * The basic way to understand how this works is the kfs-archetype is used to create kfs
      * maven projects, but it is dynamically generated. Then, source files are copied to it.
      */ 
     public void execute() throws MojoExecutionException {
         final String basedir = System.getProperty("user.dir");
         helper.setCaller(this);

        final Map<String, String> map = new HashMap<String, String>();
        map.put("basedir", basedir);
        map.put("package", packageName);
        map.put("packageName", packageName);
        map.put("groupId", groupId);
        map.put("artifactId", artifactId);
        map.put("version", version);

         try {

            final List archetypeRemoteRepositories = new ArrayList();
            
             /* TODO: Allow remote repositories later 
 
             if (remoteRepositories != null) {
                 getLog().info("We are using command line specified remote repositories: " + remoteRepositories);
 
                 archetypeRemoteRepositories = new ArrayList();
 
                 String[] s = StringUtils.split(remoteRepositories, ",");
 
                 for (int i = 0; i < s.length; i++) {
                     archetypeRemoteRepositories.add(createRepository(s[i], "id" + i));
                 }
             }*/
             
             final File prototypeJar = helper.repack(file, artifactId);
             helper.extractTempPom();
             helper.installArtifact(file, null, 
                                    getMavenHome(), 
                                    groupId, 
                                    artifactId, 
                                    version, 
                                    repositoryId);
             helper.installArtifact(prototypeJar, 
                                    sources, 
                                    getMavenHome(), 
                                    groupId, 
                                    artifactId, 
                                    version, 
                                    repositoryId);
 
        } 
        catch (Exception e) {
             throw new MojoExecutionException("Failed to create a new KFS Prototype",e);
         }
     }
     
     
     public void setMavenHome(final File mavenHome) {
         this.mavenHome = mavenHome;
     }
     
     public File getMavenHome() {
         return this.mavenHome;
     }
         
     public void setRepositoryId(final String repositoryId) {
         this.repositoryId = repositoryId;
     }
     
     public String getRepositoryId() {
         return this.repositoryId;
     }
 }
