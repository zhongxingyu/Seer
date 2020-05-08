 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.mavenplugin;
 
 import org.apache.maven.RepositoryUtils;
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.artifact.handler.manager.ArtifactHandlerManager;
 import org.apache.maven.artifact.repository.ArtifactRepository;
 import org.apache.maven.artifact.resolver.ArtifactResolver;
 import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
 import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
 import org.apache.maven.model.Model;
 import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.project.DefaultProjectBuildingRequest;
 import org.apache.maven.project.MavenProject;
 import org.apache.maven.project.ProjectBuilder;
 import org.apache.maven.project.ProjectBuildingException;
 import org.apache.maven.project.ProjectBuildingRequest;
 import org.apache.maven.project.ProjectBuildingResult;
 
 import org.codehaus.plexus.util.FileUtils;
 
 import org.sonatype.aether.RepositorySystem;
 import org.sonatype.aether.RepositorySystemSession;
 import org.sonatype.aether.collection.CollectRequest;
 import org.sonatype.aether.graph.Dependency;
 import org.sonatype.aether.graph.DependencyFilter;
 import org.sonatype.aether.installation.InstallRequest;
 import org.sonatype.aether.installation.InstallationException;
 import org.sonatype.aether.repository.RemoteRepository;
 import org.sonatype.aether.resolution.ArtifactRequest;
 import org.sonatype.aether.resolution.ArtifactResult;
 import org.sonatype.aether.resolution.DependencyRequest;
 import org.sonatype.aether.resolution.DependencyResult;
 import org.sonatype.aether.util.artifact.DefaultArtifact;
 import org.sonatype.aether.util.artifact.JavaScopes;
 import org.sonatype.aether.util.filter.DependencyFilterUtils;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 
 /**
  * General Mojo for cids related maven plugin stuff.
  *
  * @author   martin.scholl@cismet.de
  * @version  $Revision$, $Date$
  */
 public abstract class AbstractCidsMojo extends AbstractMojo {
 
     //~ Static fields/initializers ---------------------------------------------
 
     public static final String PROP_CIDS_CLASSPATH = "de.cismet.cids.classpath"; // NOI18N
 
     public static final String LIB_DIR = "lib";     // NOI18N
     public static final String LIB_EXT_DIR = "ext"; // NOI18N
     public static final String LIB_INT_DIR = "int"; // NOI18N
 
     //~ Instance fields --------------------------------------------------------
 
     /**
      * Used to look up Artifacts in the remote repository.
      *
      * @component  DOCUMENT ME!
      */
     protected transient ArtifactResolver resolver;
 
     /**
      * DOCUMENT ME!
      *
      * @component  role="org.apache.maven.artifact.handler.manager.ArtifactHandlerManager"
      * @required
      * @readonly
      */
     protected transient ArtifactHandlerManager artifactHandlerManager;
 
     /**
      * Location of the local repository.
      *
      * @parameter  expression="${localRepository}"
      * @required   true
      * @readonly   true
      */
     protected transient ArtifactRepository local;
 
     /**
      * List of Remote Repositories used by the resolver.
      *
      * @parameter  expression="${project.remoteArtifactRepositories}"
      * @required   true
      * @readonly   true
      */
     protected transient List<ArtifactRepository> remoteRepos;
 
     /**
      * The enclosing maven project.
      *
      * @parameter  expression="${project}"
      * @required   true
      * @readonly   true
      */
     protected transient MavenProject project;
 
     /**
      * The entry point to Aether, i.e. the component doing all the work.
      *
      * @component
      */
     protected RepositorySystem repoSystem;
 
     /**
      * The current repository/network configuration of Maven.
      *
      * @parameter  default-value="${repositorySystemSession}"
      * @required
      * @readonly
      */
     protected RepositorySystemSession repoSession;
 
     /**
      * Project builder - builds a model from a pom.xml.
      *
      * @component  role="org.apache.maven.project.ProjectBuilder"
      * @required   true
      * @readonly   true
      */
     protected transient ProjectBuilder projectBuilder;
 
     /**
      * Whether to skip the execution of this mojo.
      *
      * @parameter  expression="${refsystem.reset.skip}" default-value="true"
      * @required   false
      * @readonly   true
      */
     protected transient Boolean failOnError;
 
     /**
      * The project's remote repositories to use for the resolution of project dependencies.
      *
      * @parameter  default-value="${project.remoteProjectRepositories}"
      * @readonly
      */
     protected transient List<RemoteRepository> projectRepos;
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * Resolves the dependencies of the given artifact with the given scope. Uses a {@link ScopeArtifactFilter} with the
      * given scope to resolve the artifacts.
      *
      * @param   artifact  the artifact whose dependencies shall be resolved
      * @param   scope     the scope applied to the resolve process
      *
      * @return  all the dependencies artifacts of the given artifact
      *
      * @throws  org.sonatype.aether.resolution.DependencyResolutionException  InvalidDependencyVersionException if the
      *                                                                        artifacts cannot be created from the
      *                                                                        created maven project
      * @throws  InstallationException                                         if an artifact of the given artifact
      *                                                                        cannot be resolved
      * @throws  IOException                                                   ArtifactNotFoundException if an artifact
      *                                                                        of the given artifact cannot be found
      * @throws  ProjectBuildingException                                      if no maven project can be build from the
      *                                                                        artifact information
      */
     protected Set<Artifact> resolveArtifacts(final Artifact artifact, final String scope)
             throws org.sonatype.aether.resolution.DependencyResolutionException,
                 InstallationException,
                 IOException,
                 ProjectBuildingException {
         return resolveArtifacts(artifact, scope, new ScopeArtifactFilter(scope));
     }
 
     /**
      * Resolves the dependencies of the given artifact with the given scope and the given filter.
      *
      * @param   artifact  the artifact whose dependencies shall be resolved
      * @param   scope     the scope applied to the resolve process
      * @param   filter    the <code>ArtifactFilter</code> to apply
      *
      * @return  all the dependencies artifacts of the given artifact
      *
      * @throws  org.sonatype.aether.resolution.DependencyResolutionException  if the dependencies cannot be resolved for
      *                                                                        the given project for any reason
      * @throws  InstallationException                                         if a temporary artifact cannot be
      *                                                                        installed when dealing with virtual
      *                                                                        artifacts
      * @throws  IOException                                                   if a temporary pom cannot be written when
      *                                                                        dealing with virtual artifacts
      * @throws  ProjectBuildingException                                      if no maven project can be build from the
      *                                                                        artifact information
      *
      * @see     #resolveArtifacts(org.apache.maven.project.MavenProject, java.lang.String,
      *          org.apache.maven.artifact.resolver.filter.ArtifactFilter)
      */
     protected Set<Artifact> resolveArtifacts(final Artifact artifact, final String scope, final ArtifactFilter filter)
             throws org.sonatype.aether.resolution.DependencyResolutionException,
                 InstallationException,
                 IOException,
                 ProjectBuildingException {
         // create a maven project from the pom
         final MavenProject pomProject = resolveProject(artifact);
         if (getLog().isDebugEnabled()) {
             getLog().debug("created mavenproject from pom '" + artifact + "': " + pomProject); // NOI18N
         }
 
         return resolveArtifacts(pomProject, scope, filter);
     }
 
     /**
      * Resolves the dependencies of the given project with the given scope and the given filter.
      *
      * @param   artifactProject  the project who's dependencies shall be resolved
      * @param   scope            the scope applied to the resolve process
      * @param   filter           the <code>ArtifactFilter</code> to apply
      *
      * @return  all the dependencies artifacts of the given project
      *
      * @throws  org.sonatype.aether.resolution.DependencyResolutionException  if the dependencies cannot be resolved for
      *                                                                        the given project for any reason
      * @throws  InstallationException                                         if a temporary artifact cannot be
      *                                                                        installed when dealing with virtual
      *                                                                        artifacts
      * @throws  IOException                                                   if a temporary pom cannot be written when
      *                                                                        dealing with virtual artifacts
      */
     protected Set<Artifact> resolveArtifacts(final MavenProject artifactProject,
             final String scope,
             final ArtifactFilter filter) throws org.sonatype.aether.resolution.DependencyResolutionException,
         InstallationException,
         IOException {
         org.sonatype.aether.artifact.Artifact tmpArtifact = new DefaultArtifact(
                 artifactProject.getGroupId(),
                 artifactProject.getArtifactId(),
                 artifactProject.getPackaging(),
                 artifactProject.getVersion());
 
         if (getLog().isDebugEnabled()) {
             getLog().debug("resolving artifacts for: " + tmpArtifact); // NOI18N
         }
 
         // first we check if the artifact that we handle is actually present
         final ArtifactRequest artifactRequest = new ArtifactRequest();
         artifactRequest.setArtifact(tmpArtifact);
         artifactRequest.setRepositories(projectRepos);
 
         boolean virtual;
         try {
             final ArtifactResult artifactResult = repoSystem.resolveArtifact(repoSession, artifactRequest);
 
             virtual = artifactResult.getArtifact() == null;
         } catch (final org.sonatype.aether.resolution.ArtifactResolutionException ex) {
             if (getLog().isDebugEnabled()) {
                 getLog().debug("artifact not resolved, assuming virtual artifact: " + artifactProject, ex);
             }
 
             virtual = true;
         }
 
         try {
             // so called virtual artifacts have to be deployed temporarily
             if (virtual) {
                 // we deploy a pom artifact for the virtual project
                 tmpArtifact = new DefaultArtifact(
                         tmpArtifact.getGroupId(),
                         tmpArtifact.getArtifactId(),
                         "pom", // NOI18N
                         tmpArtifact.getVersion());
                 final File targetDir = new File(project.getBasedir(), "target"); // NOI18N
                 targetDir.mkdir();
                 final File tmpPom = new File(targetDir, "tmp-pom.xml"); // NOI18N
                 tmpArtifact = tmpArtifact.setFile(tmpPom);
 
                 final MavenXpp3Writer pomWriter = new MavenXpp3Writer();
                 final Model mavenModel = artifactProject.getModel();
                 mavenModel.setModelVersion("4.0.0"); // NOI18N
                 pomWriter.write(new FileWriter(tmpPom), mavenModel);
 
                 final InstallRequest installRequest = new InstallRequest();
                 installRequest.addArtifact(tmpArtifact);
 
                 repoSystem.install(repoSession, installRequest);
             }
 
             // we collect all dependency artifacts and filter them afterwards
             final CollectRequest collectRequest = new CollectRequest();
             collectRequest.setRoot(new Dependency(tmpArtifact, JavaScopes.COMPILE));
             collectRequest.setRepositories(projectRepos);
 
             final DependencyFilter classpathFilter = DependencyFilterUtils.classpathFilter(scope);
             final DependencyRequest dependencyRequest = new DependencyRequest(collectRequest, classpathFilter);
 
             final DependencyResult dependencyResult = repoSystem.resolveDependencies(repoSession, dependencyRequest);
 
             final List<ArtifactResult> artifactResults = dependencyResult.getArtifactResults();
 
             final Set<Artifact> result = new LinkedHashSet<Artifact>();
             final Artifact tmpMvnArtifact = RepositoryUtils.toArtifact(tmpArtifact);
             for (final ArtifactResult ar : artifactResults) {
                 final Artifact resolved = RepositoryUtils.toArtifact(ar.getArtifact());
                 if (!resolved.equals(tmpMvnArtifact) && filter.include(resolved)) {
                     result.add(resolved);
                 }
             }
 
             if (getLog().isDebugEnabled()) {
                 getLog().debug("resolved artifacts: " + result.toString()); // NOI18N
             }
 
             return result;
         } finally {
             if (virtual) {
                 final Artifact virtualArtifact = RepositoryUtils.toArtifact(tmpArtifact);
                 final File artifactFile = new File(local.getBasedir(), local.pathOf(virtualArtifact));
                 final File toDelete = artifactFile.getParentFile().getParentFile();
 
                 try {
                     FileUtils.deleteDirectory(toDelete);
                 } catch (final IOException e) {
                     if (getLog().isWarnEnabled()) {
                         getLog().warn("cannot delete virtual artifact data from local repo: " + virtualArtifact, e); // NOI18N
                     }
                 }
             }
         }
     }
 
     /**
      * Resolves the <code>MavenProject</code> for the given artifact.
      *
      * @param   artifact  the input artifact
      *
      * @return  the <code>MavenProject</code> for the given artifact
      *
      * @throws  ProjectBuildingException  if the project cannot be created
      */
     protected MavenProject resolveProject(final Artifact artifact) throws ProjectBuildingException {
         // create a pom artifact from the given artifact information
         final Artifact pom = new org.apache.maven.artifact.DefaultArtifact(
                 artifact.getGroupId(),
                 artifact.getArtifactId(),
                 artifact.getVersion(),
                 Artifact.SCOPE_RUNTIME,
                 "pom", // NOI18N
                 "", // NOI18N
                 artifactHandlerManager.getArtifactHandler("pom")); // NOI18N
         if (getLog().isDebugEnabled()) {
             getLog().debug("created pom artifact from artifact '" + artifact + "': " + pom); // NOI18N
         }
 
         final ProjectBuildingRequest pbRequest = new DefaultProjectBuildingRequest();
         pbRequest.setRepositorySession(repoSession);
         pbRequest.setRemoteRepositories(remoteRepos);
         pbRequest.setLocalRepository(local);
 
         final ProjectBuildingResult pbResult = projectBuilder.build(pom, pbRequest);
 
         return pbResult.getProject();
     }
 }
