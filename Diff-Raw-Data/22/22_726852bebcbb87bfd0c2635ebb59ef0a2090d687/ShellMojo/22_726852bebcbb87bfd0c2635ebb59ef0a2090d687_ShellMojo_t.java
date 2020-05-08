 package com.ekaqu.jython;
 
 import com.google.common.base.Joiner;
 import com.google.common.base.Throwables;
 import com.google.common.collect.Lists;
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.artifact.factory.ArtifactFactory;
 import org.apache.maven.artifact.repository.ArtifactRepository;
 import org.apache.maven.artifact.resolver.ArtifactResolver;
 import org.apache.maven.model.Dependency;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.project.MavenProject;
 
 import java.io.File;
 import java.util.List;
 import java.util.Properties;
 
 /**
  * Creates a jython shell
  *
  * @goal shell
  */
 public class ShellMojo extends AbstractMojo {
 
   /**
    * POM
    *
    * @parameter expression="${project}"
    * @readonly
    * @required
    */
   protected MavenProject project;
 
   /**
    * Used to look up Artifacts in the remote repository.
    *
    * @parameter expression=
   * "${component.org.apache.maven.artifact.factory.ArtifactFactory}"
    * @required
    * @readonly
    */
   protected ArtifactFactory factory;
 
   /**
    * Used to look up Artifacts in the remote repository.
    *
    * @parameter expression=
   * "${component.org.apache.maven.artifact.resolver.ArtifactResolver}"
    * @required
    * @readonly
    */
   protected ArtifactResolver artifactResolver;
 
   /**
    * List of Remote Repositories used by the resolver
    *
    * @parameter expression="${project.remoteArtifactRepositories}"
    * @readonly
    * @required
    */
   protected List remoteRepositories;
 
   /**
    * Location of the local repository.
    *
    * @parameter expression="${localRepository}"
    * @readonly
    * @required
    */
   protected ArtifactRepository localRepository;
 
   /**
    * Where jython cache dir lives
    *
    * @parameter expression="${jython.cache.directory}" default-value="${project.build.directory}/jython/cache"
    * @optional
    */
   protected File cacheDirectory;
 
   /**
    * Where the compiled code is
   *
    * @parameter expression="${jython.project.compile.directory}" default-value="${project.build.directory}/classes"
    * @optional
    */
   protected File compiledDirectory;
 
   /**
    * Where the test compiled code is
   *
    * @parameter expression="${jython.project.test.compile.directory}" default-value="${project.build.directory}/test-classes"
    * @optional
    */
   protected File testCompiledDirectory;
 
   public void execute() throws MojoExecutionException, MojoFailureException {
     Properties properties = new Properties();
     properties.putAll(System.getProperties());
     properties.putAll(getPluginContext());
     getLog().debug(properties.toString());
 
     List<Dependency> dependencies = project.getDependencies();
     getLog().debug(dependencies.toString());
     cacheDirectory.mkdirs();
     List<String> jars = Lists.newLinkedList();
     jars.add(compiledDirectory.getAbsolutePath());
     jars.add(testCompiledDirectory.getAbsolutePath());
    for (Dependency dependency : dependencies) {
       Artifact artifact = this.factory.createArtifact(
         dependency.getGroupId(), dependency.getArtifactId(),
         dependency.getVersion(), dependency.getScope(), dependency.getType());
 
       try {
         artifactResolver.resolve(artifact, this.remoteRepositories, this.localRepository);
         getLog().debug("Artifact File : " + artifact.getFile());
         jars.add(artifact.getFile().getAbsolutePath());
       } catch (Throwable e) {
         Throwables.propagate(e);
       }
     }
 
     //TODO should pass the list in rather than do this hack
     properties.setProperty("java.class.path", Joiner.on(":").join(jars));
     getLog().info("CLASSPATH = " + properties.getProperty("java.class.path"));
 
     new JythonShell(properties).run();
   }
 }
