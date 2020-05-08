 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.mavenplugin.distgen;
 
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.project.MavenProject;
 import org.apache.maven.project.ProjectBuildingException;
 
 import org.codehaus.plexus.util.FileUtils;
 
 import java.io.File;
 import java.io.IOException;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 
 import de.cismet.cids.jnlp.Homepage;
 import de.cismet.cids.jnlp.Information;
 import de.cismet.cids.jnlp.Jar;
 import de.cismet.cids.jnlp.Jnlp;
 import de.cismet.cids.jnlp.ObjectFactory;
 import de.cismet.cids.jnlp.Resources;
 
 import de.cismet.cids.mavenplugin.AbstractCidsMojo;
 
 /**
  * Goal which generates a the lib folder of a cids distribution.
  *
  * @version                       $Revision$, $Date$
  * @goal                          generate-lib
  * @phase                         package
  * @requiresDependencyResolution  runtime
  */
 public class GenerateLibMojo extends AbstractCidsMojo {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static final String INT_GID_PREFIX = "de.cismet"; // NOI18N
 
     //~ Instance fields --------------------------------------------------------
 
     /**
      * Whether to skip the execution of this mojo.
      *
      * @parameter  expression="${cids.generate-lib.skip}" default-value="false"
      * @required   false
      */
     private transient Boolean skip;
 
     /**
      * The directory where the lib directory shall be created in.
      *
      * @parameter  expression="${cids.generate-lib.outputDirectory}
      * @required   true
      */
     private transient File outputDirectory;
 
     /**
      * The vendor generating the lib structure.
      *
      * @parameter  expression="${cids.generate-lib.vendor}
      */
     private transient String vendor;
 
     /**
      * The homepage of the vendor generating the lib structure.
      *
      * @parameter  expression="${cids.generate-lib.homepage}
      */
     private transient String homepage;
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * Generates a lib directory from the projects dependencies.
      *
      * @throws  MojoExecutionException  if any error occurs during execution
      */
     @Override
     public void execute() throws MojoExecutionException {
         if (skip) {
             if (getLog().isInfoEnabled()) {
                getLog().info("generate lib skipped"); // NOI18N
             }
            
             return;
         }

         try {
             generateStructure();
         } catch (final Exception e) {
             final String message = "cannot generate structure";  // NOI18N
             if (getLog().isErrorEnabled()) {
                 getLog().error(message);
             }
             throw new MojoExecutionException(message, e);
         }
 
         // get all direct artifacts of the project and populate the lib folder
         final Set<Artifact> dependencies = project.getDependencyArtifacts();
         for (final Artifact artifact : dependencies) {
             // only accept artifacts neccessary for runtime
             if (Artifact.SCOPE_COMPILE.equals(artifact.getScope())
                         || Artifact.SCOPE_RUNTIME.equals(artifact.getScope())) {
                 populateLibDir(artifact);
             }
         }
     }
 
     /**
      * Generates the basic lib structure consiting of a lib folder, an int folder and an ext folder. The lib folder is
      * created within the outputDirectory.
      *
      * @return  the generated lib folder <code>File</code>
      *
      * @throws  IOException  if any of the folders cannot be created
      */
     private File generateStructure() throws IOException {
         final File libDir = new File(outputDirectory, LIB_DIR);
         if (!libDir.exists() && !libDir.isDirectory() && !libDir.mkdirs()) {
             throw new IOException("could not create lib folder"); // NOI18N
         }
 
         final File extDir = new File(libDir, LIB_EXT_DIR);
         if (!extDir.exists() && !extDir.isDirectory() && !extDir.mkdir()) {
             throw new IOException("could not create ext folder"); // NOI18N
         }
 
         final File intDir = new File(libDir, LIB_INT_DIR);
         if (!intDir.exists() && !intDir.isDirectory() && !intDir.mkdir()) {
             throw new IOException("could not create int folder"); // NOI18N
         }
 
         return libDir;
     }
 
     /**
      * Populates the lib dir with the given artifact's runtime dependencies. The dependecies are split into "ext" and
      * "int" dependencies using the <code>INT_GID_PREFIX</code>.
      *
      * @param   artifact  the artifact whose dependencies are being copied
      *
      * @throws  MojoExecutionException  if any error occurs during population
      */
     private void populateLibDir(final Artifact artifact) throws MojoExecutionException {
         if (getLog().isDebugEnabled()) {
             getLog().debug("populate libdir for artifact: " + artifact); // NOI18N
         }
 
         try {
             final Set<Artifact> resolved = resolveArtifacts(artifact, Artifact.SCOPE_RUNTIME);
 
             // split the artifacts in ext and int artifacts
             final Set<Artifact> extArtifacts = new HashSet<Artifact>();
             final Set<Artifact> intArtifacts = new HashSet<Artifact>();
             for (final Artifact dep : resolved) {
                 if (dep.getGroupId().startsWith(INT_GID_PREFIX)) {
                     intArtifacts.add(dep);
                 } else {
                     extArtifacts.add(dep);
                 }
             }
 
             final File extJnlp = populateDir(
                     artifact,
                     extArtifacts,
                     new File(outputDirectory, LIB_DIR + File.separator + LIB_EXT_DIR),
                     LIB_EXT_DIR,
                     false);
             final File intJnlp = populateDir(
                     artifact,
                     intArtifacts,
                     new File(outputDirectory, LIB_DIR + File.separator + LIB_INT_DIR),
                     LIB_INT_DIR,
                     true);
         } catch (final Exception ex) {
             final String message = "could not resolve dependencies for artifact: " + artifact; // NOI18N
             if (getLog().isErrorEnabled()) {
                 getLog().error(message, ex);
             }
             throw new MojoExecutionException(message, ex);
         }
     }
 
     /**
      * Populates the given dir with the given dependencies. Additionally a jnlp file will be generated containing
      * jar-entries for any dependency in the dependency set. The jnlp file will be named like that: name of the given
      * artifact's project + "_" + the given name suffix + ".jnlp".<br>
      * If <code>copyBaseArtifact</code> is true the given artifact will be copied and added to the jnlp, too.
      *
      * @param   artifact             the artifact which is the basis for this population
      * @param   dependencies         the resolved dependencies of the artifact
      * @param   dir                  the directory where all the files shall be placed
      * @param   nameSuffix           the suffix to be appended artifact's project name
      * @param   inlucedBaseArtifact  whether the given artifact shall be included or not
      *
      * @return  a generated jnlp file located in the given dir, never null
      *
      * @throws  IOException               if the dependencies cannot be copied to the given dir
      * @throws  JAXBException             if the jnlp file cannot be created
      * @throws  ProjectBuildingException  if the maven project cannot be resolved for the given artifact
      * @throws  IllegalArgumentException  if the given dir is null or not a directory or the nameSuffix is null
      */
     private File populateDir(
             final Artifact artifact,
             final Set<Artifact> dependencies,
             final File dir,
             final String nameSuffix,
             final boolean inlucedBaseArtifact) throws IOException, JAXBException, ProjectBuildingException {
         if ((artifact == null) || (dependencies == null)) {
             return null;
         }
         if ((dir == null) || !dir.isDirectory()) {
             throw new IllegalArgumentException("dir must not be null or no directory: " + dir); // NOI18N
         }
         if (nameSuffix == null) {
             throw new IllegalArgumentException("suffix must not be null");                      // NOI18N
         }
 
         final ObjectFactory objectFactory = new ObjectFactory();
         final Jnlp jnlp = objectFactory.createJnlp();
         jnlp.setSpec("1.0+"); // NOI18N
         final Information info = objectFactory.createInformation();
 
         // set jnlp info title
         final MavenProject artifactProject = resolveProject(artifact);
         if (nameSuffix != null) {
             info.setTitle(artifactProject.getName() + " " + nameSuffix);
         }
         if (vendor != null) {
             info.setVendor(vendor);
         }
         if (homepage != null) {
             final Homepage hp = objectFactory.createHomepage();
             hp.setHref(homepage);
             info.setHomepage(hp);
         }
 
         jnlp.getInformation().add(info);
 
         final Resources resources = objectFactory.createResources();
         final List jars = resources.getJavaOrJ2SeOrJarOrNativelibOrExtensionOrPropertyOrPackage();
         for (final Artifact dep : dependencies) {
             final Jar jar = objectFactory.createJar();
             jar.setHref(dep.getFile().getName());
             jars.add(jar);
             FileUtils.copyFileToDirectory(dep.getFile(), dir);
         }
 
         if (inlucedBaseArtifact) {
             final Jar jar = objectFactory.createJar();
             jar.setHref(artifact.getFile().getName());
             jars.add(jar);
             FileUtils.copyFileToDirectory(artifact.getFile(), dir);
         }
 
         jnlp.getResources().add(resources);
 
         // write the jnlp
         final File outFile = new File(dir, artifactProject.getName().replace(" ", "_") + "_" + nameSuffix + ".jnlp"); // NOI18N
         final JAXBContext jaxbContext = JAXBContext.newInstance("de.cismet.cids.jnlp");                               // NOI18N
         final Marshaller marshaller = jaxbContext.createMarshaller();
         marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
         marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");                                                    // NOI18N
         marshaller.marshal(jnlp, outFile);
 
         if (getLog().isInfoEnabled()) {
             getLog().info("created jnlp: " + outFile); // NOI18N
         }
 
         return outFile;
     }
 }
