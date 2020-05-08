 package org.burgers.elasticsearch.plugin;
 
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 
 /**
  * Starts Elasticsearch.
  * @goal run
  * @requiresProject false
  */
 public class ElasticSearchMojo extends AbstractMojo {
     @Override
     public void execute() throws MojoExecutionException, MojoFailureException {
        System.out.println("I'm running!!!");
     }
 }
