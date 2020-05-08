 package com.adito.maven.plugins.extension;
 
 import com.adito.extension.ExtensionBundle;
 import java.io.File;
 import java.io.IOException;
 import javax.xml.bind.JAXB;
 import org.apache.maven.archiver.MavenArchiveConfiguration;
 import org.apache.maven.archiver.MavenArchiver;
 import org.apache.maven.artifact.DependencyResolutionRequiredException;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.project.MavenProject;
 import org.apache.maven.project.MavenProjectHelper;
 import org.codehaus.plexus.archiver.ArchiverException;
 import org.codehaus.plexus.archiver.jar.JarArchiver;
 import org.codehaus.plexus.archiver.jar.ManifestException;
 import org.codehaus.plexus.archiver.zip.ZipArchiver;
 
 /**
  * @goal adito-extension
  * @phase package
  * @requiresDependencyResolution runtime
  * @threadSafe true
  */
 public class AditoExtensionMojo extends AbstractMojo {
 
     /**
      * The directory for the generated extension.
      *
      * @parameter expression="${project.build.directory}"
      * @required
      */
     protected String outputDirectory;
     /**
      * The directory containing generated classes.
      *
      * @parameter expression="${project.build.outputDirectory}"
      * @required
      */
     private File classesDirectory;
     /**
      * Name of the extention that adito uses for display purpose. It should be
      * one line text.
      *
      * @parameter expression="${project.artifactId}"
      * @required
      * @readonly
      */
     private String extensionName;
     /**
      * Single directory for web files to include in the extention.
      *
      * @parameter expression="${basedir}/src/main/webapp"
      * @required
      */
     private File warSourceDirectory;
     /**
      * Single directory for extra files to include in the extention.
      *
      * @parameter expression="${basedir}/src/main/extension"
      * @required
      */
     private File extensionSourceDirectory;
     /**
      * The maven project.
      *
      * @parameter expression="${project}"
      * @required
      * @readonly
      */
     protected MavenProject project;
     /**
      * The maven archive configuration to use.
      *
      * @parameter
      */
     private final MavenArchiveConfiguration archive;
     /**
      * Used to create .jar archive.
      *
      * @component role="org.codehaus.plexus.archiver.Archiver" role-hint="jar"
      * @required
      */
     private JarArchiver jarArchiver;
     /**
      * Used to create .zip archive.
      *
      * @component role="org.codehaus.plexus.archiver.Archiver" role-hint="zip"
      * @required
      */
     private ZipArchiver extensionArchiver;
     /**
      * Create extension as jar and put as into private.
      *
      * @parameter default-value="true"
      */
     private boolean createJar;
     /**
      * Write/Rewrite Classpath.
      *
      * @parameter default-value="true"
      */
     private boolean writeClasspath;
     /**
      * Finalname.
      *
      * @parameter expression="${project.build.finalName}"
      * @required
      */
     private String finalname;
     /**
      * @component @required
      */
     private MavenProjectHelper projectHelper;
 
     public AditoExtensionMojo() {
         this.archive = new MavenArchiveConfiguration();
     }
 
     public void execute() throws MojoExecutionException, MojoFailureException {
         try {
             performPackaging();
         } catch (ArchiverException ex) {
             throw new MojoExecutionException(String.format("Error assembling extension: %s", ex.getMessage()), ex);
         } catch (ManifestException ex) {
             throw new MojoExecutionException(String.format("Error assembling extension: %s", ex.getMessage()), ex);
         } catch (IOException ex) {
             throw new MojoExecutionException(String.format("Error assembling extension: %s", ex.getMessage()), ex);
         } catch (DependencyResolutionRequiredException ex) {
             throw new MojoExecutionException(String.format("Error assembling extension: %s", ex.getMessage()), ex);
         }
     }
 
     private void performPackaging() throws ArchiverException, ManifestException,
             IOException, DependencyResolutionRequiredException {
         final File extensionZip = new ExtensionArchiverBuilder(extensionName, extensionArchiver, getOutputFile("zip"))
                 .addExtensionDirectory(getExtensionSourceDirectory())
                 .addExtensionClasspathFile(createJar(), "private")
                 .addExtensionClasspathFile(project.getArtifacts(), "private")
                 .addExtensionDirectory(getWebappDirectory(), "webapp")
                 .addExtensionBundle(getExtension())
                 .createArchive(project);
         projectHelper.attachArtifact(project, "zip", extensionZip);
     }
 
     private File createJar() throws ArchiverException, IOException,
             ManifestException, DependencyResolutionRequiredException {
         File jarFile;
         if (createJar) {
             jarFile = createJarFile();
             projectHelper.attachArtifact(project, "jar", jarFile);
         } else {
             jarFile = null;
         }
         return jarFile;
     }
 
     private File createJarFile() throws IOException, ManifestException,
             ArchiverException, DependencyResolutionRequiredException {
         final MavenArchiver archiver = new MavenArchiver();
         archiver.setArchiver(jarArchiver);
         archiver.setOutputFile(getOutputFile("jar"));
         if (getClassesDirectory().exists()) {
             jarArchiver.addDirectory(getClassesDirectory());
         }
        archiver.createArchive(project, archive);
         return archiver.getArchiver().getDestFile();
     }
 
     private ExtensionBundle getExtension() {
         ExtensionBundle extension;
         if (writeClasspath) {
             final File extesionXmlFile = new File(getExtensionSourceDirectory(), "extension.xml");
             extension = JAXB.unmarshal(extesionXmlFile, ExtensionBundle.class);
         } else {
             extension = null;
         }
         return extension;
     }
 
     private File getOutputFile(final String extension) {
         return new File(new File(outputDirectory), String.format("%s.%s", finalname, extension));
     }
 
     private File getWebappDirectory() {
         return warSourceDirectory;
     }
 
     private File getClassesDirectory() {
         return classesDirectory;
     }
 
     private File getExtensionSourceDirectory() {
         return extensionSourceDirectory;
     }
 }
