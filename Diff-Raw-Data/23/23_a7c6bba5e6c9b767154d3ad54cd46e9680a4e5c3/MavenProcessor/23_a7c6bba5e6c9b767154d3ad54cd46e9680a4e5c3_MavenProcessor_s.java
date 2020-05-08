 package org.jenkinsci.plugins.jobprofiles;
 
 
 import hudson.maven.MavenEmbedder;
 import hudson.maven.MavenEmbedderException;
 import hudson.maven.MavenUtil;
 import hudson.model.AbstractBuild;
 import hudson.model.TaskListener;
 import hudson.tasks.Maven;
 import net.oneandone.sushi.fs.World;
 import net.oneandone.sushi.fs.file.FileNode;
 import org.apache.maven.project.MavenProject;
 import org.apache.maven.project.ProjectBuildingException;
 
 import java.io.File;
 import java.io.IOException;
 
 public class MavenProcessor {
 
     public static MavenProject mavenProcessor(String pom, World world, TaskListener listener, AbstractBuild build) {
         MavenEmbedder embedder = null;
         MavenProject mavenProject = null;
         FileNode tmpPom = null;
         File mavenHome = null;
 
        mavenHome = Maven.all().get(Maven.DescriptorImpl.class).getInstallations()[0].getHomeDir();
 
         try {
             tmpPom = (FileNode) world.getTemp().createTempFile().writeStrings(pom);
         } catch (IOException e) {
             listener.getLogger().println("Failed to create temp directory " + e.getMessage());
         }
         //embedder = new MavenEmbedder(new File(tmpPom.toString()), null);
         //mavenProject = embedder.readProject(new File(tmpPom.getPath()));
         try {
             assert tmpPom != null;
             embedder = MavenUtil.createEmbedder(listener, mavenHome, null);
             mavenProject = embedder.readProject(new File(tmpPom.toString()));
         } catch (MavenEmbedderException e) {
             listener.getLogger().println("Got error " + e.getMessage());
         } catch (IOException e) {
             listener.getLogger().println("Got error " + e.getMessage());
         } catch (ProjectBuildingException e) {
             listener.getLogger().println("Cannot create Maven Project " + e.getMessage());
         }
         assert mavenProject != null;
         return mavenProject;
 
     }
 }
