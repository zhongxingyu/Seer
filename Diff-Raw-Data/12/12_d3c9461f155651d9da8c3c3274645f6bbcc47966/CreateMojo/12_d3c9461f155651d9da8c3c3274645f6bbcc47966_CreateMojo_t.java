 package com.atlassian.maven.plugins.amps;
 
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.jfrog.maven.annomojo.annotations.MojoGoal;
 import org.jfrog.maven.annomojo.annotations.MojoRequiresProject;
import org.jfrog.maven.annomojo.annotations.MojoRequiresDirectInvocation;
 
 /**
  * Creates a new plugin
  */
 @MojoGoal("create")
@MojoRequiresProject(false)
 public class CreateMojo extends AbstractProductAwareMojo
 {
     public void execute() throws MojoExecutionException, MojoFailureException
     {
         getMavenGoals().createPlugin(getProductId());
     }
 }
