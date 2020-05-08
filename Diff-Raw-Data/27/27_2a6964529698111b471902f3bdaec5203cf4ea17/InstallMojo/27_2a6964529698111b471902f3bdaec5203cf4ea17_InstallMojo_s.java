 package com.atlassian.maven.plugins.refapp.pdk;
 
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.PluginManager;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.artifact.Artifact;
import com.atlassian.maven.plugins.refapp.pdk.AbstractPdkMojo;
 import com.atlassian.maven.plugins.refapp.MavenGoals;
 
 /**
 * @goal uninstall
  */
 public class InstallMojo extends AbstractPdkMojo {
 
     public void execute() throws MojoExecutionException, MojoFailureException {
        MavenGoals goals = new MavenGoals(project, session, pluginManager, getLog());
 
         ensurePluginKeyExists();
         goals.installPlugin(pluginKey, httpPort);
     }
 }
