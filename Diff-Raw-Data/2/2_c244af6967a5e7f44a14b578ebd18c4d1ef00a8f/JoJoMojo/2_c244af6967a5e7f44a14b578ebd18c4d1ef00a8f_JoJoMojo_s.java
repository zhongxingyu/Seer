 /**
  * Copyright (c) 2010-2011 Martin M Reed
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.hardisonbrewing.maven.core;
 
 import java.util.List;
 
 import org.apache.maven.artifact.factory.ArtifactFactory;
 import org.apache.maven.artifact.installer.ArtifactInstaller;
 import org.apache.maven.artifact.repository.ArtifactRepository;
 import org.apache.maven.artifact.resolver.ArtifactResolver;
 import org.apache.maven.execution.MavenSession;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.logging.Log;
 import org.apache.maven.project.MavenProject;
 import org.apache.maven.project.MavenProjectBuilder;
 import org.codehaus.plexus.archiver.manager.ArchiverManager;
 import org.codehaus.plexus.util.cli.CommandLineException;
 import org.codehaus.plexus.util.cli.Commandline;
 import org.codehaus.plexus.util.cli.StreamConsumer;
 import org.hardisonbrewing.maven.core.cli.CommandLineService;
 import org.hardisonbrewing.maven.core.cli.LogStreamConsumer;
 
 public abstract class JoJoMojo extends AbstractMojo {
 
     /**
      * The current {@link JoJoMojo} instance;
      */
     private static JoJoMojo joJoMojo;
 
     protected JoJoMojo() {
 
         joJoMojo = this;
     }
 
     /**
      * Return the current {@link JoJoMojo} instance;
      * @return
      */
     public static final JoJoMojo getMojo() {
 
         return joJoMojo;
     }
 
     protected final int execute( List<String> cmd ) {
 
         return execute( buildCommandline( cmd ) );
     }
 
     protected final int execute( List<String> cmd, StreamConsumer systemOut, StreamConsumer systemErr ) {
 
         return execute( buildCommandline( cmd ), systemOut, systemErr );
     }
 
     protected final int execute( Commandline commandLine ) {
 
         StreamConsumer systemOut = new LogStreamConsumer( LogStreamConsumer.LEVEL_INFO );
         StreamConsumer systemErr = new LogStreamConsumer( LogStreamConsumer.LEVEL_ERROR );
         return execute( commandLine, systemOut, systemErr );
     }
 
     protected final int execute( Commandline commandLine, StreamConsumer systemOut, StreamConsumer systemErr ) {
 
         int exitValue;
 
         try {
             getLog().info( commandLine.toString() );
             exitValue = CommandLineService.execute( commandLine, systemOut, systemErr );
         }
         catch (CommandLineException e) {
            throw new IllegalStateException( e.getCause() );
         }
 
         if ( exitValue != 0 ) {
             throw new IllegalStateException( "Command exited with value[" + exitValue + "]" );
         }
         return exitValue;
     }
 
     protected Commandline buildCommandline( List<String> cmd ) {
 
         Commandline commandLine;
 
         try {
             commandLine = CommandLineService.build( cmd );
         }
         catch (CommandLineException e) {
             throw new IllegalStateException( e.getMessage() );
         }
 
         commandLine.setWorkingDirectory( TargetDirectoryService.getTargetDirectory() );
         return commandLine;
     }
 
     /**
      * Return the current {@link Log} instance.
      */
     @Override
     public final Log getLog() {
 
         return super.getLog();
     }
 
     /**
      * Return the current {@link MavenProject} instance.
      * @return
      */
     public abstract MavenProject getProject();
 
     /**
      * Return the current {@link ArchiverManager} instance.
      * @return
      */
     public abstract ArchiverManager getArchiverManager();
 
     /**
      * 
      * @return
      */
     public abstract ArtifactResolver getArtifactResolver();
 
     /**
      * 
      * @return
      */
     public abstract ArtifactRepository getLocalRepository();
 
     /**
      * 
      * @return
      */
     public abstract ArtifactFactory getArtifactFactory();
 
     /**
      * 
      * @return
      */
     public abstract MavenProjectBuilder getProjectBuilder();
 
     /**
      * 
      * @return
      */
     public abstract List<ArtifactRepository> getRemoteRepositories();
 
     /**
      * 
      * @return
      */
     public abstract ArtifactInstaller getArtifactInstaller();
 
     /**
      * 
      * @return
      */
     public abstract MavenSession getMavenSession();
 }
