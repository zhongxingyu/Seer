 /*
  * Copyright (C) 2011 Bosch Software Innovations GmbH. All rights reserved.
  */
 
 package org.sourcepit.maven.bootstrap.core;
 
 import java.io.File;
 import java.lang.annotation.Annotation;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
 
 import org.apache.maven.MavenExecutionException;
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.artifact.ArtifactUtils;
 import org.apache.maven.artifact.repository.ArtifactRepository;
 import org.apache.maven.artifact.resolver.ArtifactResolutionException;
 import org.apache.maven.artifact.resolver.ArtifactResolutionRequest;
 import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
 import org.apache.maven.artifact.resolver.ResolutionErrorHandler;
 import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
 import org.apache.maven.classrealm.ClassRealmManager;
 import org.apache.maven.execution.DefaultMavenExecutionRequest;
 import org.apache.maven.execution.DefaultMavenExecutionResult;
 import org.apache.maven.execution.MavenExecutionRequest;
 import org.apache.maven.execution.MavenExecutionResult;
 import org.apache.maven.execution.MavenSession;
 import org.apache.maven.lifecycle.LifecycleExecutionException;
 import org.apache.maven.lifecycle.internal.DependencyContext;
 import org.apache.maven.lifecycle.internal.MojoExecutor;
 import org.apache.maven.model.Dependency;
 import org.apache.maven.plugin.LegacySupport;
 import org.apache.maven.plugin.MojoExecution;
 import org.apache.maven.plugin.descriptor.MojoDescriptor;
 import org.apache.maven.project.DefaultProjectBuildingRequest;
 import org.apache.maven.project.DuplicateProjectException;
 import org.apache.maven.project.MavenProject;
 import org.apache.maven.project.ProjectBuilder;
 import org.apache.maven.project.ProjectBuildingException;
 import org.apache.maven.project.ProjectBuildingRequest;
 import org.apache.maven.project.ProjectBuildingResult;
 import org.apache.maven.project.ProjectSorter;
 import org.apache.maven.repository.RepositorySystem;
 import org.codehaus.plexus.PlexusContainer;
 import org.codehaus.plexus.classworlds.ClassWorld;
 import org.codehaus.plexus.classworlds.ClassWorldListener;
 import org.codehaus.plexus.classworlds.realm.ClassRealm;
 import org.codehaus.plexus.classworlds.realm.DuplicateRealmException;
 import org.codehaus.plexus.component.annotations.Requirement;
 import org.codehaus.plexus.configuration.PlexusConfigurationException;
 import org.codehaus.plexus.logging.Logger;
 import org.codehaus.plexus.util.dag.CycleDetectedException;
 import org.sonatype.aether.util.DefaultRepositorySystemSession;
 import org.sonatype.aether.util.repository.ChainedWorkspaceReader;
 import org.sonatype.guice.bean.locators.BeanLocator;
 import org.sonatype.inject.BeanEntry;
 import org.sourcepit.guplex.Guplex;
 import org.sourcepit.guplex.InjectorRequest;
 import org.sourcepit.maven.bootstrap.internal.core.ExtensionDescriptor;
 import org.sourcepit.maven.bootstrap.internal.core.ExtensionDescriptorReader;
 import org.sourcepit.maven.bootstrap.internal.core.PluginConfigurationReader;
 import org.sourcepit.maven.bootstrap.internal.core.ReactorReader;
 import org.sourcepit.maven.bootstrap.participation.BootstrapParticipant;
 import org.sourcepit.maven.exec.intercept.MavenExecutionParticipant;
 
 import com.google.inject.Injector;
 import com.google.inject.Key;
 
 public abstract class AbstractBootstrapper implements MavenExecutionParticipant
 {
    @Requirement
    private Logger logger;
 
    @Requirement
    private PlexusContainer plexusContainer;
 
    @Requirement
    private Guplex guplex;
 
    @Requirement
    private LegacySupport legacySupport;
 
    @Requirement
    private ProjectBuilder projectBuilder;
 
    @Requirement
    private RepositorySystem repositorySystem;
 
    @Requirement
    private ResolutionErrorHandler resolutionErrorHandler;
 
    @Requirement
    private ClassRealmManager classRealmManager;
 
    @Requirement
    private MojoExecutor mojoExecutor;
 
    private final ImportEnforcer importEnforcer;
 
    private final Set<String> extensionRealmPrefixes = new HashSet<String>();
 
    private final Map<MavenSession, MavenSession> actualToBootSession = new HashMap<MavenSession, MavenSession>();
    private final Map<MavenSession, MavenSession> bootToActualSession = new HashMap<MavenSession, MavenSession>();
 
    private final String extensionKey;
 
    public AbstractBootstrapper(String groupId, String artifactId)
    {
       this.extensionKey = groupId + ":" + artifactId;
 
       final List<String> imports = new ArrayList<String>();
       imports.add("javax.inject.*");
       imports.add("com.google.inject.*");
       imports.add("com.google.inject.name.*");
       imports.add("org.sonatype.inject.*");
       imports.add("org.slf4j.*");
       imports.add("org.slf4j.impl.*");
 
       imports.add(ImportEnforcer.toImportPattern(BootstrapParticipant.class));
 
       extensionRealmPrefixes.add("extension>" + extensionKey);
 
       importEnforcer = new ImportEnforcer(getClass().getClassLoader(), extensionRealmPrefixes, imports);
    }
 
    public void executionStarted(MavenSession actualSession, MavenExecutionRequest executionRequest)
       throws MavenExecutionException
    {
       final MavenSession bootSession = createBootSession(actualSession);
 
       final List<File> descriptors = getProjectDescriptors(bootSession);
       if (descriptors.isEmpty())
       {
          logger.info("Skipping bootstrapper " + extensionKey + ". No projects found.");
          return;
       }
 
 
       mapSessions(actualSession, bootSession);
       
       logger.info("Executing bootstrapper " + extensionKey + "...");
       
       plexusContainer.getContainerRealm().getWorld().addListener(importEnforcer);
 
       final MavenSession oldSession = legacySupport.getSession();
       try
       {
          legacySupport.setSession(bootSession);
          setupBootSession(bootSession, descriptors);
 
          final List<MavenProject> projects = bootSession.getProjects();
          if (projects.size() > 1)
          {
             logger.info("");
             logger.info("------------------------------------------------------------------------");
             logger.info("Bootstrapper Build Order:");
             logger.info("");
             for (MavenProject project : projects)
             {
                logger.info(project.getName());
             }
          }
 
          performBootSession(bootSession);
          adjustActualSession(bootSession, actualSession);
          
          logger.info("");
          logger.info("------------------------------------------------------------------------");
          logger.info("Finished bootstrapper " + extensionKey);
       }
       finally
       {
          legacySupport.setSession(oldSession);
       }
    }
 
    private MavenSession createBootSession(MavenSession actualSession)
    {
       final DefaultRepositorySystemSession repositorySession = new DefaultRepositorySystemSession(
          actualSession.getRepositorySession());
       final MavenExecutionRequest executionRequest = DefaultMavenExecutionRequest.copy(actualSession.getRequest());
      // fix: copy ignors start time...
      executionRequest.setStartTime(actualSession.getRequest().getStartTime());
       final DefaultMavenExecutionResult executionResult = new DefaultMavenExecutionResult();
       return new MavenSession(plexusContainer, repositorySession, executionRequest, executionResult);
    }
 
    private void mapSessions(MavenSession actualSession, final MavenSession bootSession)
    {
       actualToBootSession.put(actualSession, bootSession);
       bootToActualSession.put(bootSession, actualSession);
    }
 
    private void setupBootSession(MavenSession bootSession, Collection<File> descriptors)
    {
       bootSession.setProjects(buildBootstrapProjects(bootSession, descriptors));
 
       try
       {
          Map<String, MavenProject> projectMap = getProjectMap(bootSession.getProjects());
          DefaultRepositorySystemSession repoSession = (DefaultRepositorySystemSession) bootSession
             .getRepositorySession();
          repoSession.setWorkspaceReader(ChainedWorkspaceReader.newInstance(new ReactorReader(projectMap),
             repoSession.getWorkspaceReader()));
       }
       catch (org.apache.maven.DuplicateProjectException e)
       {
          throw new IllegalStateException(e);
       }
    }
 
    private void performBootSession(final MavenSession bootSession)
    {
       for (MavenProject bootProject : bootSession.getProjects())
       {
          bootSession.setCurrentProject(bootProject);
 
          final List<ClassRealm> bootExtensionClassRealms = discoverBootExtensionClassRealms(bootProject);
          for (ClassRealm bootExtensionClassRealm : bootExtensionClassRealms)
          {
             performBootProject(bootSession, bootProject, bootExtensionClassRealm);
          }
       }
    }
 
    private void performBootProject(MavenSession bootSession, MavenProject bootProject,
       ClassRealm bootExtensionClassRealm)
    {
       ensureDependenciesAreResolved(bootSession);
 
       final ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
       Thread.currentThread().setContextClassLoader(bootExtensionClassRealm);
       try
       {
          final List<BootstrapParticipant> bootParticipants = discoverBootstrapParticipants(bootSession, bootProject,
             bootExtensionClassRealm);
          for (BootstrapParticipant bootParticipant : bootParticipants)
          {
             bootParticipant.beforeBuild(bootSession, bootProject, bootToActualSession.get(bootSession));
          }
       }
       finally
       {
          Thread.currentThread().setContextClassLoader(originalClassLoader);
       }
    }
 
    private void ensureDependenciesAreResolved(MavenSession session)
    {
       final MojoDescriptor mojoDescriptor = new MojoDescriptor();
       mojoDescriptor.setAggregator(false);
       // mojoDescriptor.setDependencyCollectionRequired(getDependencyResolutionRequired());
       mojoDescriptor.setDependencyResolutionRequired(getDependencyResolutionRequired());
 
       final MojoExecution mojoExecution = new MojoExecution(mojoDescriptor);
 
       final DependencyContext dependencyContext = mojoExecutor.newDependencyContext(session,
          Collections.singletonList(mojoExecution));
 
       try
       {
          mojoExecutor.ensureDependenciesAreResolved(mojoDescriptor, session, dependencyContext);
       }
       catch (LifecycleExecutionException e)
       {
          throw new IllegalStateException(e);
       }
    }
 
    private Map<String, MavenProject> getProjectMap(List<MavenProject> projects)
       throws org.apache.maven.DuplicateProjectException
    {
       Map<String, MavenProject> index = new LinkedHashMap<String, MavenProject>();
       Map<String, List<File>> collisions = new LinkedHashMap<String, List<File>>();
 
       for (MavenProject project : projects)
       {
          String projectId = ArtifactUtils.key(project.getGroupId(), project.getArtifactId(), project.getVersion());
 
          MavenProject collision = index.get(projectId);
 
          if (collision == null)
          {
             index.put(projectId, project);
          }
          else
          {
             List<File> pomFiles = collisions.get(projectId);
 
             if (pomFiles == null)
             {
                pomFiles = new ArrayList<File>(Arrays.asList(collision.getFile(), project.getFile()));
                collisions.put(projectId, pomFiles);
             }
             else
             {
                pomFiles.add(project.getFile());
             }
          }
       }
 
       if (!collisions.isEmpty())
       {
          throw new org.apache.maven.DuplicateProjectException("Two or more projects in the reactor"
             + " have the same identifier, please make sure that <groupId>:<artifactId>:<version>"
             + " is unique for each project: " + collisions, collisions);
       }
 
       return index;
    }
 
    protected abstract String getDependencyResolutionRequired();
 
    protected abstract void adjustActualSession(MavenSession bootSession, MavenSession actualSession);
 
    public void executionEnded(MavenSession actualSession, MavenExecutionResult executionResult)
    {
       final MavenSession bootSession = actualToBootSession.remove(actualSession);
       if (bootSession == null)
       {
          return;
       }
 
       final MavenSession oldSession = legacySupport.getSession();
       try
       {
          bootToActualSession.remove(bootSession);
 
          legacySupport.setSession(bootSession);
          shutdownBootSession(bootSession);
       }
       finally
       {
          legacySupport.setSession(oldSession);
       }
 
       plexusContainer.getContainerRealm().getWorld().removeListener(importEnforcer);
    }
 
    private void shutdownBootSession(MavenSession bootSession)
    {
       for (MavenProject bootProject : bootSession.getProjects())
       {
          bootSession.setCurrentProject(bootProject);
 
          final List<ClassRealm> bootExtensionClassRealms = discoverBootExtensionClassRealms(bootProject);
          for (ClassRealm bootExtensionClassRealm : bootExtensionClassRealms)
          {
             shutdownBootProject(bootSession, bootProject, bootExtensionClassRealm);
          }
       }
    }
 
    private void shutdownBootProject(MavenSession bootSession, MavenProject bootProject,
       ClassRealm bootExtensionClassRealm)
    {
       final ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
       Thread.currentThread().setContextClassLoader(bootExtensionClassRealm);
       try
       {
          final List<BootstrapParticipant> bootParticipants = discoverBootstrapParticipants(bootSession, bootProject,
             bootExtensionClassRealm);
          for (BootstrapParticipant bootParticipant : bootParticipants)
          {
             bootParticipant.afterBuild(bootSession, bootProject, bootToActualSession.get(bootSession));
          }
       }
       finally
       {
          Thread.currentThread().setContextClassLoader(originalClassLoader);
       }
    }
 
    private List<BootstrapParticipant> discoverBootstrapParticipants(MavenSession bootSession, MavenProject bootProject,
       ClassRealm bootExtensionClassRealm)
    {
       final String key = BootstrapParticipant.class.getName() + "@" + bootExtensionClassRealm.getId();
 
       @SuppressWarnings("unchecked")
       List<BootstrapParticipant> bootstrapParticipants = (List<BootstrapParticipant>) bootProject.getContextValue(key);
       if (bootstrapParticipants == null)
       {
          bootstrapParticipants = lookupBootstrapParticipants(bootSession, bootProject, bootExtensionClassRealm);
          bootProject.setContextValue(key, bootstrapParticipants);
       }
       return bootstrapParticipants;
    }
 
    private List<BootstrapParticipant> lookupBootstrapParticipants(MavenSession bootSession, MavenProject bootProject,
       ClassRealm bootExtensionClassRealm)
    {
       final ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
       Thread.currentThread().setContextClassLoader(bootExtensionClassRealm);
       try
       {
          InjectorRequest request = new InjectorRequest();
          request.setUseIndex(true);
          request.getClassLoaders().add(bootExtensionClassRealm);
 
          addCustomClassLoaders(bootSession, bootProject, bootExtensionClassRealm, request.getClassLoaders());
 
          final Injector injector = guplex.createInjector(request);
 
          final BeanLocator locator = injector.getInstance(BeanLocator.class);
          final Key<BootstrapParticipant> key = Key.get(BootstrapParticipant.class);
          final List<BootstrapParticipant> result = new ArrayList<BootstrapParticipant>();
          for (BeanEntry<Annotation, BootstrapParticipant> beanEntry : locator.locate(key))
          {
             result.add(beanEntry.getValue());
          }
          return result;
       }
       finally
       {
          Thread.currentThread().setContextClassLoader(originalClassLoader);
       }
    }
 
    protected void addCustomClassLoaders(MavenSession bootSession, MavenProject bootProject, ClassRealm extensionRealm,
       Set<ClassLoader> classLoaders)
    {
       addExtensionExtensionsClassLoaders(bootSession, bootProject, extensionRealm, classLoaders);
    }
 
    protected final void addExtensionExtensionsClassLoaders(MavenSession bootSession, MavenProject bootProject,
       ClassRealm extensionRealm, Set<ClassLoader> classLoaders)
    {
       if (isAllowExtensionExtensions(bootSession, bootProject))
       {
          final List<Dependency> extensions = getExtensionExtensions(bootSession, bootProject);
          if (extensions != null)
          {
             for (Dependency extension : extensions)
             {
                final ClassRealm newRealm = createExtensionExtensionRealm(bootSession, bootProject, extensionRealm,
                   extension);
                try
                {
                   plexusContainer.discoverComponents(newRealm);
                }
                catch (PlexusConfigurationException e)
                {
                   throw new IllegalStateException(e);
                }
                classLoaders.add(newRealm);
             }
          }
       }
    }
 
    protected abstract boolean isAllowExtensionExtensions(MavenSession bootSession, MavenProject bootProject);
 
    private ClassRealm createExtensionExtensionRealm(MavenSession bootSession, MavenProject bootProject,
       ClassRealm extensionRealm, Dependency extension)
    {
       final URL[] urls = resolveURLs(bootSession, bootProject, extension);
 
       final String realmId = extensionRealm.getId() + "@" + extension.toString();
       final ClassRealm newRealm = newRealm(extensionRealm.getWorld(), realmId);
       for (int j = 0; j < urls.length; j++)
       {
          newRealm.addURL(urls[j]);
       }
 
       newRealm.importFrom(classRealmManager.getCoreRealm(), "org.sonatype.plexus.components");
 
       importEnforcer.addBootstrapImports(newRealm);
 
       if (extensionRealm.getURLs().length > 0)
       {
          final ExtensionDescriptor extensionDescriptor = ExtensionDescriptorReader.read(extensionRealm.getURLs()[0]);
          if (extensionDescriptor == null)
          {
             newRealm.importFrom(extensionRealm, "");
          }
          else
          {
             for (String exportedPackage : extensionDescriptor.getExportedPackages())
             {
                newRealm.importFrom(extensionRealm, exportedPackage);
             }
             newRealm.importFrom(classRealmManager.getMavenApiRealm(), "");
          }
       }
 
       return newRealm;
    }
 
    private URL[] resolveURLs(MavenSession bootSession, MavenProject bootProject, Dependency extension)
    {
       final ArtifactResolutionResult result = resolve(bootSession, bootProject, extension);
 
       final Set<Artifact> artifacts = result.getArtifacts();
 
       final URL[] urls = new URL[artifacts.size()];
       int i = 0;
       for (Iterator<org.apache.maven.artifact.Artifact> it = artifacts.iterator(); it.hasNext(); i++)
       {
          final org.apache.maven.artifact.Artifact artifact = it.next();
          try
          {
             urls[i] = artifact.getFile().toURI().toURL();
          }
          catch (MalformedURLException e)
          {
             throw new IllegalStateException(e);
          }
       }
       return urls;
    }
 
    private ClassRealm newRealm(ClassWorld world, String id)
    {
       synchronized (world)
       {
          String realmId = id;
 
          Random random = new Random();
 
          while (true)
          {
             try
             {
                ClassRealm classRealm = world.newRealm(realmId, null);
 
                if (logger.isDebugEnabled())
                {
                   logger.debug("Created new class realm " + realmId);
                }
 
                return classRealm;
             }
             catch (DuplicateRealmException e)
             {
                realmId = id + '-' + random.nextInt();
             }
          }
       }
    }
 
    protected List<Dependency> getExtensionExtensions(MavenSession bootSession, MavenProject bootProject)
    {
       return PluginConfigurationReader.readExtensions(bootProject, extensionKey);
    }
 
    private ArtifactResolutionResult resolve(MavenSession session, MavenProject project, Dependency dependency)
    {
       final ArtifactResolutionRequest request = new ArtifactResolutionRequest();
       request.setResolveRoot(true);
       request.setResolveTransitively(true);
       request
          .setResolutionFilter(new ScopeArtifactFilter(org.apache.maven.artifact.Artifact.SCOPE_RUNTIME_PLUS_SYSTEM));
       request
          .setCollectionFilter(new ScopeArtifactFilter(org.apache.maven.artifact.Artifact.SCOPE_RUNTIME_PLUS_SYSTEM));
 
       final MavenExecutionRequest executionRequest = session.getRequest();
       request.setForceUpdate(executionRequest.isUpdateSnapshots());
       request.setServers(executionRequest.getServers());
       request.setMirrors(executionRequest.getMirrors());
       request.setProxies(executionRequest.getProxies());
 
       request.setOffline(session.isOffline());
       request.setLocalRepository(session.getLocalRepository());
 
       // project specific
       request.setRemoteRepositories(project.getRemoteArtifactRepositories());
       request.setManagedVersionMap(project.getManagedVersionMap());
 
       request.setArtifact(repositorySystem.createDependencyArtifact(dependency));
 
       final ArtifactResolutionResult result = repositorySystem.resolve(request);
       try
       {
          resolutionErrorHandler.throwErrors(request, result);
       }
       catch (ArtifactResolutionException e)
       {
          throw new IllegalStateException(e);
       }
       return result;
    }
 
    private List<ClassRealm> discoverBootExtensionClassRealms(MavenProject bootProject)
    {
       final List<ClassRealm> bootExtensionRelams = new ArrayList<ClassRealm>();
 
       final ClassRealm projectRealm = bootProject.getClassRealm();
       if (projectRealm != null)
       {
          @SuppressWarnings("unchecked")
          final Collection<ClassRealm> importRealms = projectRealm.getImportRealms();
          for (ClassRealm classRealm : importRealms)
          {
             if (isBootExtensionClassRealm(extensionRealmPrefixes, classRealm))
             {
                bootExtensionRelams.add(classRealm);
             }
          }
       }
 
       return bootExtensionRelams;
    }
 
    private static boolean isBootExtensionClassRealm(Collection<String> extensionRealmPrefixes, ClassRealm realm)
    {
       for (String realmPrefix : extensionRealmPrefixes)
       {
          if (realm.getId().startsWith(realmPrefix))
          {
             return true;
          }
       }
       return false;
    }
 
    private List<MavenProject> buildBootstrapProjects(MavenSession session, Collection<File> descriptors)
    {
       final ProjectBuildingRequest request = new DefaultProjectBuildingRequest(session.getProjectBuildingRequest());
       request.setRemoteRepositories(filterArtifactRepositories(request.getRemoteRepositories()));
 
       final List<ProjectBuildingResult> results;
       try
       {
          results = projectBuilder.build(new ArrayList<File>(descriptors), false, request);
       }
       catch (ProjectBuildingException e)
       {
          throw new IllegalStateException("Cannot build bootstrapper project for " + e.getPomFile(), e);
       }
 
       final List<MavenProject> projects = new ArrayList<MavenProject>(results.size());
       for (ProjectBuildingResult result : results)
       {
          final MavenProject project = result.getProject();
          project.setRemoteArtifactRepositories(filterArtifactRepositories(project.getRemoteArtifactRepositories()));
          project.setPluginArtifactRepositories(filterArtifactRepositories(project.getPluginArtifactRepositories()));
          projects.add(project);
       }
 
       final ProjectSorter projectSorter;
       try
       {
          projectSorter = new ProjectSorter(projects);
       }
       catch (CycleDetectedException e)
       {
          throw new IllegalStateException(e);
       }
       catch (DuplicateProjectException e)
       {
          throw new IllegalStateException(e);
       }
 
       return projectSorter.getSortedProjects();
    }
 
    protected abstract List<ArtifactRepository> filterArtifactRepositories(List<ArtifactRepository> remoteRepositories);
 
    private List<File> getProjectDescriptors(final MavenSession bootSession)
    {
       final Collection<File> descriptors = new LinkedHashSet<File>();
       final Collection<File> skippedDescriptors = new HashSet<File>();
       discoverProjectDescriptors(bootSession, descriptors, skippedDescriptors);
 
       final List<File> pomFiles = new ArrayList<File>();
       for (File descriptor : descriptors)
       {
          if (skippedDescriptors.contains(descriptor))
          {
             logger.info("Skipping module descriptor " + descriptor.getPath());
             continue;
          }
          pomFiles.add(descriptor);
       }
       return pomFiles;
    }
 
    protected abstract void discoverProjectDescriptors(MavenSession session, Collection<File> descriptors,
       Collection<File> skippedDescriptors);
 
    private final static class ImportEnforcer implements ClassWorldListener
    {
       private final ClassLoader classLoader;
 
       private final Set<String> extensionRealmPrefixes;
 
       private final List<String> imports = new ArrayList<String>();
 
       public ImportEnforcer(ClassLoader classLoader, Set<String> extensionRealmPrefixes, List<String> imports)
       {
          this.classLoader = classLoader;
          this.extensionRealmPrefixes = extensionRealmPrefixes;
          this.imports.addAll(imports);
       }
 
       private static String toImportPattern(Class<?> clazz)
       {
          final String name = clazz.getName();
          final StringBuilder sb = new StringBuilder();
          sb.append(name.substring(0, name.lastIndexOf('.')));
          sb.append(".*");
          return sb.toString();
       }
 
       public void realmCreated(ClassRealm realm)
       {
          if (isBootExtensionClassRealm(extensionRealmPrefixes, realm))
          {
             addBootstrapImports(realm);
          }
       }
 
       private void addBootstrapImports(ClassRealm realm)
       {
          for (String packageImport : imports)
          {
             realm.importFrom(classLoader, packageImport);
          }
       }
 
       public void realmDisposed(ClassRealm realm)
       {
       }
    }
 }
