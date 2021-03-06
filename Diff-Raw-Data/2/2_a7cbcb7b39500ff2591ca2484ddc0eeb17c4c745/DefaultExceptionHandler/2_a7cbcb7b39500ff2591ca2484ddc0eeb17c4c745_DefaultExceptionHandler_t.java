 package org.apache.maven.exception;
 
 import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
 import org.apache.maven.artifact.resolver.ArtifactResolutionException;
 import org.apache.maven.lifecycle.NoPluginFoundForPrefixException;
 import org.apache.maven.plugin.CycleDetectedInPluginGraphException;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.plugin.MojoNotFoundException;
 import org.apache.maven.plugin.PluginDescriptorParsingException;
 import org.apache.maven.plugin.PluginNotFoundException;
 import org.apache.maven.plugin.PluginResolutionException;
 import org.codehaus.plexus.component.annotations.Component;
 
 /*
 
 - test projects for each of these
 - how to categorize the problems so that the id of the problem can be match to a page with descriptive help and the test project
 - nice little sample projects that could be run in the core as well as integration tests
 
 All Possible Errors
 - invalid lifecycle phase (maybe same as bad CLI param, though you were talking about embedder too)
 - <module> specified is not found
 - malformed settings
 - malformed POM
 - local repository not writable
 - remote repositories not available
 - artifact metadata missing
 - extension metadata missing
 - extension artifact missing
 - artifact metadata retrieval problem
 - version range violation 
 - circular dependency
 - artifact missing
 - artifact retrieval exception
 - md5 checksum doesn't match for local artifact, need to redownload this
 - POM doesn't exist for a goal that requires one
 - parent POM missing (in both the repository + relative path)
 - component not found
 
 Plugins:
 - plugin metadata missing
 - plugin metadata retrieval problem
 - plugin artifact missing
 - plugin artifact retrieval problem
 - plugin dependency metadata missing
 - plugin dependency metadata retrieval problem
 - plugin configuration problem
 - plugin execution failure due to something that is know to possibly go wrong (like compilation failure)
 - plugin execution error due to something that is not expected to go wrong (the compiler executable missing)
 - asking to use a plugin for which you do not have a version defined - tools to easily select versions
 - goal not found in a plugin (probably could list the ones that are)
 
  */
 
 //PluginNotFoundException, PluginResolutionException, PluginDescriptorParsingException, CycleDetectedInPluginGraphException;
 
 @Component(role=ExceptionHandler.class)
 public class DefaultExceptionHandler
     implements ExceptionHandler
 {
     public ExceptionSummary handleException( Exception exception )
     {
         String message;
         
         String reference = "http://";
         
         // Plugin problems
         if ( exception instanceof PluginNotFoundException )
         {
             message = exception.getMessage();
         }
         else if ( exception instanceof PluginResolutionException )
         {
             message = exception.getMessage();           
         }
         else if ( exception instanceof PluginDescriptorParsingException )
         {
             message = exception.getMessage();           
         }
         else if ( exception instanceof CycleDetectedInPluginGraphException )
         {
             message = exception.getMessage();           
         }        
         else if ( exception instanceof NoPluginFoundForPrefixException )
         {
             message = exception.getMessage();                       
         }
         
         // Project dependency downloading problems.
         else if ( exception instanceof ArtifactNotFoundException )
         {
             message = exception.getMessage();
         }
         else if ( exception instanceof ArtifactResolutionException )
         {
            message = exception.getMessage();
         }        
         
         // Mojo problems
         else if ( exception instanceof MojoNotFoundException )
         {
             message = exception.getMessage();            
         }        
         else if ( exception instanceof MojoFailureException )
         {
             message = ((MojoFailureException)exception).getLongMessage();
         }
         else if ( exception instanceof MojoExecutionException )
         {
             message = ((MojoExecutionException)exception).getLongMessage();
         }
         
         else
         {
             message = exception.getMessage();
         }        
         
         return new ExceptionSummary( exception, message, reference );
     }
 }
