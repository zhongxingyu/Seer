 package org.apache.maven.plugin.deploy;
 
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.artifact.factory.ArtifactFactory;
 import org.apache.maven.artifact.resolver.filter.ScopeArtifactFilter;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.project.MavenProject;
 import org.apache.maven.project.MavenProjectBuilder;
 
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * Find an artifact in a repo and deploys it
  *
  * @author <a href="mailto:dieter.houthooft@gmail.com">Dieter Houthooft</a>
  * @goal find-and-deploy
  * @threadSafe
  */
 public class FindAndDeployMojo extends DeployMojo {
 
     /**
      * @parameter expression="${groupId}"
      * @required
      */
     private String groupId;
 
     /**
      * @parameter expression="${artifactId}"
      * @required
      */
     private String artifactId;
 
     /**
      * @parameter expression="${version}"
      * @required
      */
     private String version;
 
     /**
     * @parameter expression="${type}"
     * @required
     */
    private String type;

    /**
      * Used to look up Artifacts in the remote repository.
      *
      * @component
      */
     protected ArtifactFactory factory;
 
     public void execute() throws MojoExecutionException, MojoFailureException {
         try {
             Artifact target = artifactFactory
                     .createArtifact(groupId, artifactId, version, "",
                            type);
 
             artifactResolver.resolve(target, this.remoteRepos, this.local);
 
             MavenProject project = mavenProjectBuilder.buildFromRepository(target, this.remoteRepos, this.local);
 
             Set<Artifact> artifactsTBR = project.createArtifacts(this.factory,Artifact.SCOPE_TEST, new ScopeArtifactFilter(Artifact.SCOPE_TEST));
 
             for (Artifact art : artifactsTBR) {
                 artifactResolver.resolve(art, this.remoteRepos, this.local);
             }
 
             artifactsTBR.add(target);
 
             executeWithArtifacts(artifactsTBR);
         } catch (Exception e) {
             throw new MojoExecutionException(e.getMessage(), e);
         }
     }
 }
