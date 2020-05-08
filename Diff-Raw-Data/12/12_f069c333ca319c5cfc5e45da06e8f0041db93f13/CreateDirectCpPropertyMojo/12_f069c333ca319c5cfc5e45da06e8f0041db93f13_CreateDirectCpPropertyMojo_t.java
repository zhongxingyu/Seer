 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.cids.mavenplugin;
 
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.project.MavenProject;
 
 import java.io.File;
 
 import java.util.Set;
 
 /**
  * Goal which stores the classpath of the directly declared dependencies into a property.
  *
  * @version                       1.0
  * @goal                          create-direct-cp-property
  * @phase                         initialize
  * @requiresDependencyResolution  compile
  */
 public final class CreateDirectCpPropertyMojo extends AbstractMojo {
 
     //~ Static fields/initializers ---------------------------------------------
 
     /**
      * The property ' <code>classpath.directDependencies</code>' whose value is created by this mojo. After a successful
      * execution it will contain a classpath consisting of artifacts that are explicitely declared in this project's
      * pom.
      */
     public static final String PROP_DIRECT_COMPILE_CLASSPATH = "classpath.directDependencies";                      // NOI18N
 
     //~ Instance fields --------------------------------------------------------
 
     /**
      * The enclosing maven project.
      *
      * @parameter  expression="${project}"
      * @required   true
      * @readonly   true
      */
     protected transient MavenProject project;
     /**
      * Whether to skip the execution of this mojo.
      *
      * @parameter  expression="${de.cismet.cids.create-direct-cp-property.skip}" default-value="false"
      * @required
      */
     private transient Boolean skip;
     /**
      * Whether to include system scope dependencies.
      *
      * @parameter  expression="${de.cismet.cids.create-direct-cp-property.includeSystemScope}" default-value="false"
      * @required
      */
     private transient Boolean includeSystemScope;
    /**
     * Whether to include provided scope dependencies.
     *
     * @parameter  expression="${de.cismet.cids.create-direct-cp-property.includeProvidedScope}" default-value="true"
     * @required
     */
    private transient Boolean includeProvidedScope;
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public void execute() throws MojoExecutionException, MojoFailureException {
         if (skip) {
             if (getLog().isInfoEnabled()) {
                 getLog().info(
                     "Skipping direct compile classpath property generation due to configuration, skip = " // NOI18N
                             + skip);
             }
 
             return;
         }
         // api is 1.4 style, no way to get rid of this warning some other way except using instanceof + cast
         @SuppressWarnings("unchecked")
         final Set<Artifact> directDeps = project.getDependencyArtifacts();
 
         final StringBuilder directCpProp = new StringBuilder();
         for (final Artifact a : directDeps) {
            if ("compile".equals(a.getScope())                                          // NOI18N
                        || (includeSystemScope && "system".equals(a.getScope()))        // NOI18N
                        || (includeProvidedScope && "provided".equals(a.getScope()))) { // NOI18N
                 directCpProp.append(a.getFile().getAbsolutePath()).append(File.pathSeparatorChar);
             }
         }
 
         if (directCpProp.length() > 0) {
             directCpProp.deleteCharAt(directCpProp.length() - 1);
         }
 
         project.getProperties().put(PROP_DIRECT_COMPILE_CLASSPATH, directCpProp.toString());
 
         if (getLog().isInfoEnabled()) {
             getLog().info("Created direct compile classpath property: ${" + PROP_DIRECT_COMPILE_CLASSPATH // NOI18N
                         + "} = "                        // NOI18N
                         + directCpProp.toString());
         }
     }
 }
