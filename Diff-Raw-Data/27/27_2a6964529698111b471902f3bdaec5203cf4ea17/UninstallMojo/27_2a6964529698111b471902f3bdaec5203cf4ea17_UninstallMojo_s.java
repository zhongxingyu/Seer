 package com.atlassian.maven.plugins.refapp.pdk;
 
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
import com.atlassian.maven.plugins.refapp.pdk.AbstractPdkMojo;
 import com.atlassian.maven.plugins.refapp.MavenGoals;
 
 /**
 * @goal install
  */
 public class UninstallMojo extends AbstractPdkMojo {
 
     public void execute() throws MojoExecutionException, MojoFailureException {
        MavenGoals goals = new MavenGoals(project, session, pluginManager, getLog());
 
         ensurePluginKeyExists();
         goals.uninstallPlugin(pluginKey, httpPort);
     }
 }
