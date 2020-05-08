 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.mavenplugin;
 
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.plugin.MojoExecutionException;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.jar.Attributes;
 import java.util.jar.JarOutputStream;
 import java.util.jar.Manifest;
 
 /**
  * Goal which creates properties related to a cids project.
  *
  * @version                       $Revision$, $Date$
  * @goal                          create-properties
  * @phase                         process-classes
  * @requiresDependencyResolution  runtime
  */
 public class CreatePropertiesMojo extends AbstractCidsMojo {
 
     //~ Instance fields --------------------------------------------------------
 
     /**
      * Whether to skip the execution of this mojo.
      *
      * @parameter  expression="${de.cismet.cids.create-properties.skip}" default-value="false"
      * @required   false
      */
     private transient Boolean skip;
 
     /**
      * The <code>de.cismet.cids.lib.local</code> property.
      *
      * @parameter  expression="${de.cismet.cids.lib.local}"
      * @required   false
      * @readonly   true
      */
     private transient File libLocalDir;
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * Generates several cids related properties that can be used by plugins executed in an subsequent phase.
      *
      * @throws  MojoExecutionException  DOCUMENT ME!
      */
     @Override
     public void execute() throws MojoExecutionException {
         if (skip) {
             if (getLog().isInfoEnabled()) {
                 getLog().info("create properties skipped"); // NOI18N
             }
 
             return;
         }
 
         createClasspathProperty();
     }
 
     /**
      * Generates/replaces the <code>de.cismet.cids.classpath</code> property using:<br/>
      * <br/>
      *
      * <ul>
      *   <li>all jars within the folder specified by <code>de.cismet.cids.lib.local</code> property</li>
      *   <li>the project's output directory</li>
      *   <li>the project's runtime artifacts</li>
      *   <li>the project's system artifacts</li>
      * </ul>
      */
     private void createClasspathProperty() {
         final StringBuilder sb = new StringBuilder();
         // to collect all the files for the win long classpath issue [issue:2335]
         final List<File> cpFiles = new ArrayList<File>();
 
         // first collect local jars and append them to the classpath string
         if (libLocalDir.exists()) {
             final File[] jars = libLocalDir.listFiles(
                     new FileFilter() {
 
                         @Override
                         public boolean accept(final File pathname) {
                             return pathname.getName().toLowerCase().endsWith(".jar"); // NOI18N
                         }
                     });
             if (jars == null) {
                 if (getLog().isWarnEnabled()) {
                     getLog().warn("an I/O error occured while fetching jars from lib local folder: " + libLocalDir); // NOI18N
                 }
             } else {
                 for (final File jar : jars) {
                     if (getLog().isDebugEnabled()) {
                         getLog().debug("add jar: " + jar);                            // NOI18N
                     }
                     sb.append(jar.getAbsolutePath()).append(File.pathSeparatorChar);
                     // collect all the files for the win long classpath issue [issue:2335]
                     cpFiles.add(jar);
                 }
             }
         } else {
             if (getLog().isWarnEnabled()) {
                 getLog().warn("lib local dir property does not denote an existing filename: " + libLocalDir); // NOI18N
             }
         }
 
         // then add the project's output directory
         sb.append(project.getBuild().getOutputDirectory()).append(File.pathSeparatorChar);
         // collect the folder for the win long classpath issue
         cpFiles.add(new File(project.getBuild().getOutputDirectory()));
 
         // collect runtime artifacts and append them to the classpath string
         for (final Object o : project.getRuntimeArtifacts()) {
             final Artifact artifact = (Artifact)o;
             sb.append(artifact.getFile().getAbsolutePath()).append(File.pathSeparatorChar);
             // collect all the files for the win long classpath issue [issue:2335]
             cpFiles.add(artifact.getFile());
         }
 
         // also collect system artifacts and append them to the classpath string [issue:1456]
         // we will have to iterate over all dependency artifacts because project.getSystemArtifacts() is a trap...
         boolean first = true;
         for (final Object o : project.getDependencyArtifacts()) {
             final Artifact artifact = (Artifact)o;
             if (Artifact.SCOPE_SYSTEM.equals(artifact.getScope())) {
                 if (first && getLog().isWarnEnabled()) {
                     getLog().warn("adding system dependent libraries to classpath"); // NOI18N
                     first = false;
                 }
                 if (getLog().isDebugEnabled()) {
                     getLog().debug("system-dependent library: " + artifact);         // NOI18N
                 }
                 sb.append(artifact.getFile().getAbsolutePath()).append(File.pathSeparatorChar);
                 // collect all the files for the win long classpath issue [issue:2335]
                 cpFiles.add(artifact.getFile());
             }
         }
 
         // remove the last colon
         sb.deleteCharAt(sb.length() - 1);
 
         // wrap into "" [issue:1457]
         sb.insert(0, "\"").insert(sb.length(), "\""); // NOI18N
 
         // double up all '\' [issue:1455]
         final String classpath = sb.toString().replace("\\", "\\\\"); // NOI18N
 
         if (getLog().isInfoEnabled()) {
             getLog().info("created classpath: " + classpath); // NOI18N
         }
 
         // to fix long classpath issue under win [issue:2335]
         try {
             project.getProperties().put(PROP_CIDS_CLASSPATH, createClassPathJar(cpFiles).getAbsolutePath());
         } catch (final IOException e) {
             if (getLog().isWarnEnabled()) {
                 getLog().warn("cannot create classpath jar, using conventional classpath", e); // NOI18N
             }
             project.getProperties().put(PROP_CIDS_CLASSPATH, classpath);
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param   cpFiles  classpath DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      *
      * @throws  IOException  DOCUMENT ME!
      */
     private File createClassPathJar(final List<File> cpFiles) throws IOException {
         // Generate Manifest and jar File
         final StringBuilder sb = new StringBuilder();
         for (final File file : cpFiles) {
            sb.append(file.toURL().toExternalForm()).append(' ');
         }
 
         final Manifest manifest = new Manifest();
         manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0"); // NOI18N
         manifest.getMainAttributes().put(Attributes.Name.CLASS_PATH, sb.toString());
 
         final String jarname = "gen-classpath.jar"; // NOI18N
 
         // write the jar file
         final File jar = new File(project.getBuild().getDirectory(), jarname);
         final JarOutputStream target = new JarOutputStream(new FileOutputStream(jar), manifest);
 
         // close the stream to finalise file
         target.close();
 
         return jar;
     }
 }
