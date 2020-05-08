 /*
  * Copyright (c) 2007-2013 Sonatype, Inc. All rights reserved.
  *
  * This program is licensed to you under the Apache License Version 2.0,
  * and you may not use this file except in compliance with the Apache License Version 2.0.
  * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the Apache License Version 2.0 is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
  */
 
 package org.sonatype.nexus.pluginbundle.maven;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.sonatype.aether.RepositorySystem;
 import org.sonatype.aether.RepositorySystemSession;
 import org.sonatype.aether.collection.CollectRequest;
 import org.sonatype.aether.collection.CollectResult;
 import org.sonatype.aether.collection.DependencyCollectionException;
 import org.sonatype.aether.graph.Dependency;
 import org.sonatype.aether.graph.DependencyNode;
 import org.sonatype.aether.graph.DependencyVisitor;
 import org.sonatype.aether.util.artifact.DefaultArtifact;
 import org.sonatype.nexus.pluginbundle.maven.scm.GitRevParseCommand;
 import org.sonatype.nexus.pluginbundle.maven.scm.GitRevParseScmResult;
 import org.sonatype.nexus.pluginbundle.maven.scm.HgDebugIdCommand;
 import org.sonatype.nexus.pluginbundle.maven.scm.HgDebugIdScmResult;
 import org.sonatype.plexus.build.incremental.BuildContext;
 
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.model.License;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.plugins.annotations.Component;
 import org.apache.maven.plugins.annotations.Mojo;
 import org.apache.maven.plugins.annotations.Parameter;
 import org.apache.maven.scm.ScmException;
 import org.apache.maven.scm.ScmFileSet;
 import org.apache.maven.scm.manager.ScmManager;
 import org.apache.maven.scm.provider.ScmProviderRepository;
 import org.apache.maven.scm.provider.git.AbstractGitScmProvider;
 import org.apache.maven.scm.provider.hg.HgScmProvider;
 import org.apache.maven.scm.provider.svn.AbstractSvnScmProvider;
 import org.apache.maven.scm.provider.svn.command.info.SvnInfoItem;
 import org.apache.maven.scm.provider.svn.command.info.SvnInfoScmResult;
 import org.apache.maven.scm.repository.ScmRepository;
 import org.codehaus.plexus.util.StringUtils;
 
 import static org.apache.maven.artifact.Artifact.SCOPE_COMPILE;
 import static org.apache.maven.artifact.Artifact.SCOPE_PROVIDED;
 import static org.apache.maven.artifact.Artifact.SCOPE_RUNTIME;
 import static org.apache.maven.artifact.Artifact.SCOPE_TEST;
 import static org.apache.maven.plugins.annotations.LifecyclePhase.PROCESS_CLASSES;
 import static org.apache.maven.plugins.annotations.ResolutionScope.TEST;
 
 /**
  * Generates a plugins {@code plugin.xml} descriptor file based on the project's POM and class annotations.
  *
  * @since 1.0
  */
 @Mojo(name = "generate-metadata", defaultPhase = PROCESS_CLASSES, requiresDependencyResolution = TEST)
 public class GenerateMetadataMojo
     extends MojoSupport
 {
   @Component
   private ScmManager scmManager;
 
   /**
    * The username that is used when connecting to the SCM system.
    */
   @Parameter(property = "username")
   private String username;
 
   /**
    * The password that is used when connecting to the SCM system.
    */
   @Parameter(property = "password")
   private String password;
 
   /**
    * The list of classpath dependencies to be excluded from bundling for some reason
    * (for example because you are shading it into plugin artifact).
    */
   @Parameter
   private List<String> classpathDependencyExcludes;
 
   /**
    * A list of groupId:artifactId references to non-plugin dependencies that should be shared
    * along with main plugin JAR to dependants of this plugin.
    */
   @Parameter
   private List<String> sharedDependencies;
 
   /**
    * Configures the plugin name.
    */
   @Parameter(property = "pluginName", defaultValue = "${project.name}")
   private String pluginName;
 
   /**
    * Configures the plugin description.
    */
   @Parameter(property = "pluginDescription", defaultValue = "${project.description}")
   private String pluginDescription;
 
   /**
    * Configures the plugin site URL.
    */
   @Parameter(property = "pluginSiteUrl", defaultValue = "${project.url}")
   private String pluginSiteUrl;
 
   /**
    * m2e workspace integration hook
    */
   @Component
   private BuildContext buildContext;
 
   @Component
   private RepositorySystem repositorySession;
 
   @Parameter(property = "repositorySystemSession")
   private RepositorySystemSession repositorySystemSession;
 
   /**
    * Artifact ID of nexus-plugin-api which is used to calculate banned dependencies for auto-exclusion.
    */
   @Parameter
   private String bannedRootArtifactId;
 
  /**
   * Allows the dependency banning logic to be disabled.
   */
  @Parameter
  private boolean dependencyBanningEnabled = true;

  /**
   * List of banned groupId:artifactId dependencies.
   */
   private List<String> bannedIds;
 
   public void execute() throws MojoExecutionException, MojoFailureException {
     // skip if wrong packaging
     if (!isNexusPluginPacakging()) {
       return;
     }
 
     PluginDescriptorGenerationRequest request = new PluginDescriptorGenerationRequest();
     request.setGroupId(project.getGroupId());
     request.setArtifactId(project.getArtifactId());
     request.setVersion(project.getVersion());
 
     request.setName(pluginName);
     request.setDescription(pluginDescription);
     request.setPluginSiteURL(pluginSiteUrl);
 
     // licenses
     if (project.getLicenses() != null) {
       for (License mavenLicenseModel : project.getLicenses()) {
         request.addLicense(mavenLicenseModel.getName(), mavenLicenseModel.getUrl());
       }
     }
 
     // dependencies
     if (bannedRootArtifactId != null) {
      if (!dependencyBanningEnabled) {
        getLog().debug("Dependency banning is disabled");
       }
      else {
        try {
          bannedIds = collectBannedDependencies();
        }
        catch (DependencyCollectionException e) {
          throw new MojoFailureException(e.getMessage(), e);
        }
       }
     }
     Set<Artifact> classpathArtifacts = fillInDependencies(request);
 
     // scm information
     fillScmInfo(request);
 
     File outputDir = new File(project.getBuild().getOutputDirectory());
     File file = new File(outputDir, "META-INF/nexus/plugin.xml");
     request.setOutputFile(file);
 
     getLog().info("Generating metadata descriptor: " + file.getAbsolutePath());
     try {
       new PluginDescriptorGenerator(buildContext).generate(request);
     }
     catch (Exception e) {
       throw new MojoFailureException("Failed to generate plugin metadata file: " + e, e);
     }
 
     try {
       ClasspathUtils.write(buildContext, classpathArtifacts, project);
     }
     catch (Exception e) {
       throw new MojoFailureException("Failed to generate plugin classpath file: " + e, e);
     }
   }
 
   private Set<Artifact> fillInDependencies(final PluginDescriptorGenerationRequest request)
       throws MojoFailureException
   {
     List<Artifact> artifacts = project.getTestArtifacts();
     Set<Artifact> classpathArtifacts = new HashSet<Artifact>();
     if (artifacts != null) {
 
       Set<String> excludedArtifactIds = new HashSet<String>();
       Set<String> userExcludesIds = new HashSet<String>();
       Set<String> autoBannedIds = new HashSet<String>();
       Set<String> pluginIds = new HashSet<String>();
 
       // FIXME: Drop need for label, the following is already complex and hard to comprehend
       artifactLoop:
       for (Artifact artifact : artifacts) {
         final String artifactKey = ClasspathUtils.formatArtifactKey(artifact);
         boolean excluded = isExcluded(artifactKey);
         boolean banned = isBanned(artifactKey);
 
         if (artifact.getType().equals(NEXUS_PLUGIN)) {
           if (!SCOPE_PROVIDED.equals(artifact.getScope())) {
             throw new MojoFailureException(
                 "Nexus plugin dependency must use 'provided' scope: " + artifact.getDependencyConflictId());
           }
 
           // plugin inter-dependencies will use baseVersion, and let PluginManager resolve them runtime
           request.addPluginDependency(new GAVCoordinate(
               artifact.getGroupId(),
               artifact.getArtifactId(),
               artifact.getBaseVersion(),
               artifact.getClassifier(),
               artifact.getType(),
               false
           ));
 
           excludedArtifactIds.add(artifactKey);
           pluginIds.add(artifactKey);
         }
         else if (SCOPE_PROVIDED.equals(artifact.getScope()) || SCOPE_TEST.equals(artifact.getScope())) {
           excludedArtifactIds.add(artifact.getId());
         }
         else if (SCOPE_COMPILE.equals(artifact.getScope()) || SCOPE_RUNTIME.equals(artifact.getScope())) {
           if (artifact.getDependencyTrail() != null) {
             for (String trailId : artifact.getDependencyTrail()) {
               if (excludedArtifactIds.contains(trailId)) {
                 getLog().debug(String.format(
                     "Dependency artifact: %s is part of the transitive dependency set for a dependency with 'provided' or 'test' scope: %s\n" +
                         "This artifact will be excluded from the plugin classpath", artifact.getId(), trailId));
                 continue artifactLoop;
               }
             }
           }
 
           if (banned) {
             autoBannedIds.add(artifactKey);
           }
           else if (excluded) {
             userExcludesIds.add(artifactKey);
           }
           else {
             boolean isShared = sharedDependencies != null &&
                 sharedDependencies.contains(artifact.getGroupId() + ":" + artifact.getArtifactId());
 
             // classpath dependencies uses baseVersion, and let PluginManager resolve them runtime
             // this enables easy development turnaround, by not having recompiling the plugin to drop-in newer snapshot
             request.addClasspathDependency(new GAVCoordinate(
                 artifact.getGroupId(),
                 artifact.getArtifactId(),
                 artifact.getBaseVersion(),
                 artifact.getClassifier(),
                 artifact.getType(),
                 isShared
             ));
             classpathArtifacts.add(artifact);
           }
         }
       }
 
       if (!pluginIds.isEmpty()) {
         List<String> ids = new ArrayList<String>(pluginIds);
         Collections.sort(ids);
         getLog().info("Including " + pluginIds.size() + " plugin dependencies:");
         for (String id : ids) {
           getLog().info(" + " + id);
         }
       }
 
       if (!autoBannedIds.isEmpty()) {
         List<String> ids = new ArrayList<String>(autoBannedIds);
         Collections.sort(ids);
         getLog().info("Banned " + autoBannedIds.size() + " dependencies:");
         for (String id : ids) {
           getLog().info(" - " + id);
         }
       }
 
       if (!userExcludesIds.isEmpty()) {
         List<String> ids = new ArrayList<String>(userExcludesIds);
         Collections.sort(ids);
         getLog().info("User excluded " + userExcludesIds.size() + " dependencies:");
         for (String id : ids) {
           getLog().info(" - " + id);
         }
       }
 
       if (getLog().isDebugEnabled()) {
         if (!excludedArtifactIds.isEmpty()) {
           List<String> ids = new ArrayList<String>(excludedArtifactIds);
           Collections.sort(ids);
           getLog().debug("All excluded " + userExcludesIds.size() + " dependencies:");
           for (String id : ids) {
             getLog().debug(" - " + id);
           }
         }
       }
     }
     return classpathArtifacts;
   }
 
   protected boolean isBanned(final String key) {
     if (bannedIds != null) {
       for (String exclude : bannedIds) {
        // check if artifact key is banned, append ":" to ensure we only get full groupId:artifactId matches
        if (key.startsWith(exclude + ":")) {
           return true;
         }
       }
     }
     return false;
   }
 
   protected boolean isExcluded(final String key) {
     if (classpathDependencyExcludes != null) {
       for (String exclude : classpathDependencyExcludes) {
         if (key.startsWith(exclude)) {
           return true;
         }
       }
     }
     return false;
   }
 
   private List<String> collectBannedDependencies() throws DependencyCollectionException {
     getLog().debug("Resolving plugin api dependencies: " + bannedRootArtifactId);
 
     final List<String> banned = new ArrayList<String>();
     final org.sonatype.aether.artifact.Artifact root = new DefaultArtifact(bannedRootArtifactId);
 
     CollectRequest request = new CollectRequest();
     request.setRepositories(project.getRemoteProjectRepositories());
     request.setRoot(new Dependency(root, "runtime"));
     CollectResult result = repositorySession.collectDependencies(repositorySystemSession, request);
     result.getRoot().accept(new DependencyVisitor()
     {
       public boolean visitEnter(final DependencyNode node) {
 
         org.sonatype.aether.artifact.Artifact artifact = node.getDependency().getArtifact();
         if (!root.equals(artifact)) {
           String id = String.format("%s:%s", artifact.getGroupId(), artifact.getArtifactId());
           getLog().debug("banned: " + id);
           banned.add(id);
         }
         return true;
       }
 
       public boolean visitLeave(final DependencyNode node) {
         return true;
       }
     });
 
     return banned;
   }
 
   // SCM
 
   protected void fillScmInfo(final PluginDescriptorGenerationRequest request) {
     if (project.getScm() == null) {
       getLog().warn("No SCM information defined in POM; unable to determine SCM details");
       return;
     }
 
     // FIXME: The SCM URL here (for git at least) in a multi-module build isn't correct
     // FIXME: ... returning things like: scm:git:git@github.com:sonatype/oss-parents.git/nexus-plugin-parent/nexus-plugins/nexus-plugin-console-plugin
     // FIXME: ... for sub-modules: https://github.com/sonatype/nexus/tree/master/plugins/nexus-plugin-console-plugin
 
     String url = project.getScm().getDeveloperConnection();
     if (StringUtils.isEmpty(url)) {
       getLog().warn("No SCM developerConnection information defined in POM; unable to determine SCM details");
       return;
     }
 
     getLog().debug("SCM developerConnection URL: " + url);
 
     try {
       final ScmRepository repository = getScmRepository(url);
 
       getLog().info("Fetching SCM details");
 
       final String provider = repository.getProvider();
 
       if ("svn".equals(provider)) {
         fillSvnScmInfo(request, repository);
       }
       else if ("git".equals(provider)) {
         fillGitScmInfo(request, repository);
       }
       else if ("hg".equals(provider)) {
         fillHgScmInfo(request, repository);
       }
 
       request.setScmUrl(url);
     }
     catch (ScmException e) {
       getLog().warn("Failed to get SCM information: " + e.getMessage());
       getLog().debug(e);
     }
   }
 
   protected ScmRepository getScmRepository(final String url) throws ScmException {
     ScmRepository repository = scmManager.makeScmRepository(url);
 
     ScmProviderRepository scmRepo = repository.getProviderRepository();
 
     if (!StringUtils.isEmpty(username)) {
       scmRepo.setUser(username);
     }
 
     if (!StringUtils.isEmpty(password)) {
       scmRepo.setPassword(password);
     }
 
     return repository;
   }
 
   protected void fillSvnScmInfo(final PluginDescriptorGenerationRequest request, final ScmRepository repository)
       throws ScmException
   {
     AbstractSvnScmProvider provider = (AbstractSvnScmProvider) scmManager.getProviderByType("svn");
 
     SvnInfoScmResult result = provider.info(
         repository.getProviderRepository(), new ScmFileSet(project.getBasedir()), null);
 
     if (!result.isSuccess()) {
       throw new ScmException(result.getCommandOutput());
     }
 
     SvnInfoItem info = (SvnInfoItem) result.getInfoItems().get(0);
 
     request.setScmVersion(info.getLastChangedRevision());
     request.setScmTimestamp(info.getLastChangedDate());
   }
 
   protected void fillGitScmInfo(final PluginDescriptorGenerationRequest request, final ScmRepository repository)
       throws ScmException
   {
     AbstractGitScmProvider provider = (AbstractGitScmProvider) scmManager.getProviderByType("git");
 
     GitRevParseCommand cmd = new GitRevParseCommand();
 
     cmd.setLogger(provider.getLogger());
 
     GitRevParseScmResult result = (GitRevParseScmResult)
         cmd.execute(repository.getProviderRepository(), new ScmFileSet(project.getBasedir()), null);
 
     if (!result.isSuccess()) {
       throw new ScmException(result.getCommandOutput());
     }
 
     request.setScmVersion(result.getChangeSetHash());
     request.setScmTimestamp(result.getChangeSetDate());
   }
 
   protected void fillHgScmInfo(final PluginDescriptorGenerationRequest request, final ScmRepository repository)
       throws ScmException
   {
     HgScmProvider provider = (HgScmProvider) scmManager.getProviderByType("hg");
 
     HgDebugIdCommand cmd = new HgDebugIdCommand();
 
     cmd.setLogger(provider.getLogger());
 
     HgDebugIdScmResult result = (HgDebugIdScmResult)
         cmd.execute(repository.getProviderRepository(), new ScmFileSet(project.getBasedir()), null);
 
     if (!result.isSuccess()) {
       throw new ScmException(result.getCommandOutput());
     }
 
     request.setScmVersion(result.getChangeSetHash());
     request.setScmTimestamp(result.getChangeSetDate());
   }
 }
