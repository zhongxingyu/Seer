 package net.surguy.maven.licensing;
 
 import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactCollector;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.project.MavenProject;
 import org.apache.maven.project.artifact.MavenMetadataSource;
 
 /**
  * Maven plugin to display the licenses of all dependencies, if they are specified in the POM.
  * <p/>
  * Invoke from the command-line with "net.surguy:maven-displaylicense-plugin:displaylicense".
  * <p/>
  * The majority of the code for this plugin is in Scala - this is only a Java wrapper so Maven's
  * Javadoc-based dependency injection can be used.
  *
  * @goal displaylicense
  * @phase process-sources
  */
 @SuppressWarnings({"UnusedDeclaration"})
 public class LicenseCheckerMojo extends AbstractMojo {
 
     /**
      * The maven project.
      *
      * @parameter expression="${project}"
      * @readonly
      */
     @SuppressWarnings({"UnusedDeclaration"})
     private MavenProject project;
 
     /**
      * @parameter expression="${localRepository}" default="${maven.repo.local}"
      */
     @SuppressWarnings({"UnusedDeclaration"})
     private ArtifactRepository localRepository;
 
     /**
      * Artifact resolver, needed to download source jars for inclusion in classpath.
      *
      * @component role="org.apache.maven.artifact.metadata.ArtifactMetadataSource"
      * @required
      * @readonly
      */
     @SuppressWarnings({"UnusedDeclaration"})
     private MavenMetadataSource metadataSource;
 
    /**
     * The Artifact Collector.
     *
     * @component
     * @required
     * @readonly
     */
    @SuppressWarnings({"UnusedDeclaration"})
    private ArtifactCollector collector;

     public void execute() throws MojoExecutionException {
         assert project != null : "Project is null - dependency injection is not working";
        LicenseChecker licenseChecker = new LicenseChecker(project, localRepository, metadataSource, collector, getLog());
         licenseChecker.execute();
     }
 
 }
