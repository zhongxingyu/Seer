 package org.apache.maven.archiva.meeper;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.commons.mail.EmailException;
 import org.apache.commons.mail.SimpleEmail;
 import org.codehaus.plexus.util.IOUtil;
 import org.codehaus.plexus.util.cli.CommandLineException;
 import org.codehaus.plexus.util.cli.CommandLineUtils;
 import org.codehaus.plexus.util.cli.Commandline;
 
 /**
  * exclusions=$HOME/bin/synchronize/syncopate/exclusions.txt BASEDIR=$HOME/repository-staging/to-ibiblio/maven2
  * CHANGED_LOG=/tmp/sync-changed.log
  */
 public class Synchronizer
 {
 
     private static final String RSYNC = "rsync";
 
     private static final String SVN = "svn";
 
     private static final String DRY_RUN = "-n";
 
     private SynchronizerOptions options;
 
     private List failedRepositories = new ArrayList();
 
     public Synchronizer( SynchronizerOptions options )
     {
         this.options = options;
     }
 
     public void sync( List repositories )
     {
         int i = 1;
         Iterator it = repositories.iterator();
         while ( it.hasNext() )
         {
             SyncedRepository repo = (SyncedRepository) it.next();
             try
             {
                 System.out.println( "[" + ( i++ ) + " of " + repositories.size() + "] Synchronizing " +
                     repo.getGroupId() + " " + repo.getLocation() );
                 sync( repo );
             }
             catch ( RuntimeException e )
             {
                 System.out.println( "Error synchronizing repository " + repo.getGroupId() + ". " + e.getMessage() );
                 failedRepositories.add( repo );
                 Throwable cause = e;
                 while ( cause != null )
                 {
                     repo.getErr().append( cause.getMessage() );
                     cause = cause.getCause();
                 }
             }
         }
     }
 
     public void sync( SyncedRepository repo )
     {
         /* update from svn if necessary */
         if ( SyncedRepository.PROTOCOL_SVN.equals( repo.getProtocol() ) )
         {
             Commandline cl = new Commandline();
             cl.setExecutable( SVN );
             cl.createArg().setValue( "update" );
             cl.createArg().setValue( appendGroupFolder( repo, repo.getLocation() ) );
             int exitCode = executeCommandLine( cl, repo );
             if ( exitCode != 0 )
             {
                 throw new RuntimeException( "Error updating from SVN. Exit code: " + exitCode );
             }
         }
 
         int exitCode = syncMetadata( repo );
         if ( exitCode != 0 )
         {
             throw new RuntimeException( "Error synchronizing metadata. Exit code: " + exitCode );
         }
         exitCode = syncArtifacts( repo );
         if ( exitCode != 0 )
         {
             throw new RuntimeException( "Error synchronizing artifacts. Exit code: " + exitCode );
         }
     }
 
     private int syncMetadata( SyncedRepository repo )
     {
         Commandline cl = new Commandline();
         cl.setExecutable( RSYNC );
 
         cl.createArg().setValue( "--include=*/" );
         cl.createArg().setValue( "--include=**/maven-metadata.xml*" );
         cl.createArg().setValue( "--exclude=*" );
         cl.createArg().setValue( "--exclude-from=" + options.getExclusionsFile() );
         addCommonArguments( cl, repo );
 
         return executeCommandLine( cl, repo );
     }
 
     private int syncArtifacts( SyncedRepository repo )
     {
         Commandline cl = new Commandline();
         cl.setExecutable( RSYNC );
 
         cl.createArg().setValue( "--exclude-from=" + options.getExclusionsFile() );
         cl.createArg().setValue( "--ignore-existing" );
         addCommonArguments( cl, repo );
 
         return executeCommandLine( cl, repo );
     }
 
     private void addCommonArguments( Commandline cl, SyncedRepository repo )
     {
         if ( options.isDryRun() )
         {
             cl.createArg().setValue( DRY_RUN );
         }
         // cl.createArg().setValue("$RSYNC_OPTS");
         cl.createArg().setValue( "-Lrtivz" );
         if ( SyncedRepository.PROTOCOL_SSH.equals( repo.getProtocol() ) )
         {
             String s = repo.getSshOptions() == null ? "" : repo.getSshOptions();
             cl.createArg().setValue( "--rsh=ssh " + s );
         }
 
         cl.createArg().setValue( appendGroupFolder( repo, repo.getLocation() ) );
 
         String destinationFolder = appendGroupFolder( repo, options.getBasedir() );
         ( new File( destinationFolder ) ).mkdirs();
         cl.createArg().setValue( destinationFolder );
     }
 
     private String appendGroupFolder( SyncedRepository repo, String location )
     {
         String groupDir = "";
         if ( repo.getGroupId() != null )
         {
             groupDir = repo.getGroupId().replaceAll( "\\.", "\\/" ) + "/";
         }
         return location + "/" + groupDir;
     }
 
     private int executeCommandLine( Commandline cl, SyncedRepository repo )
     {
         CommandLineUtils.StringStreamConsumer out = new CommandLineUtils.StringStreamConsumer();
         CommandLineUtils.StringStreamConsumer err = new CommandLineUtils.StringStreamConsumer();
 
         // System.out.println( "About to execute " + cl );
 
         repo.setCommandline(cl);
 
         int exitCode;
         try
         {
             exitCode = CommandLineUtils.executeCommandLine( cl, out, err, options.getTimeout() * 60 );
         }
         catch ( CommandLineException e )
         {
             throw new RuntimeException( e );
         }
 
         repo.getOut().append( out.getOutput() );
 
         String serr = err.getOutput();
         if ( ( serr != null ) && serr.length() > 0 )
         {
             repo.getErr().append( serr );
         }
 
         return exitCode;
     }
 
 
     public static void main( String[] args )
     {
         if ( ( args.length != 2 ) && ( args.length != 3 ) )
         {
             System.out.println( "Arguments required: CONFIG_PROPERTIES_FILE REPOSITORIES_FILE [go]" );
             return;
         }
 
         int i = 0;
         SynchronizerOptions options = SynchronizerOptions.parse( new File( args[i++] ) );
         Synchronizer synchronizer = new Synchronizer( options );
 
         FileInputStream is = null;
         try
         {
             is = new FileInputStream( new File( args[i++] ) );
         }
         catch ( FileNotFoundException e )
         {
             System.err.println( "Repositories file " + args[i - 1] + " is not present" );
         }
 
         List repositories;
         try
         {
             repositories = new CsvReader().parse( is );
         }
         catch ( IOException e )
         {
            synchronizer.sendEmail( synchronizer.failedRepositories, "ERROR", e.getMessage() );
             throw new RuntimeException( e );
         }
         finally
         {
             IOUtil.close( is );
         }
 
         if ( args.length == 3 )
         {
             String go = args[i++];
             if ( ( go != null ) && ( "go".equals( go ) ) )
             {
                 options.setDryRun( false );
             }
         }
 
         synchronizer.sync( repositories );
 
         if ( synchronizer.failedRepositories.isEmpty() )
         {
             synchronizer.sendEmail( Collections.EMPTY_LIST, "SUCCESS",
                                     "--- All repositories synchronized successfully ---" );
         }
         else
         {
             StringBuffer sb = new StringBuffer();
             sb.append( "--- Some repositories were not synchronized ---" );
             sb.append( "\n" );
 
             Iterator it = synchronizer.failedRepositories.iterator();
             while ( it.hasNext() )
             {
                 SyncedRepository repo = (SyncedRepository) it.next();
                 sb.append( "groupId: " );
                 sb.append( repo.getGroupId() );
                 sb.append( "\nError:\n" );
                 sb.append( repo.getErr() );
                 sb.append( "\n" );
                 sb.append( "Command line executed: " );
                 sb.append( repo.getCommandline() );
                 sb.append( "\n" );
                 sb.append( "\n" );
             }
             synchronizer.sendEmail( synchronizer.failedRepositories, "FAILURE", sb.toString() );
         }
 
     }
 
     /**
      * send email out
      */
     private void sendEmail( List failedRepos, String subject, String text )
     {
         SimpleEmail email = new SimpleEmail();
         email.setHostName( options.getMailHostname() );
         try
         {
             email.addTo( options.getMailTo() );
             Iterator it = failedRepos.iterator();
             while ( it.hasNext() )
             {
                 SyncedRepository repo = (SyncedRepository) it.next();
                 if ( repo.getContactMail() != null )
                 {
                     email.addTo( repo.getContactMail(), repo.getContactName() );
                 }
             }
             email.setFrom( options.getMailFrom() );
             email.setSubject( options.getMailSubject() + " " + subject );
             email.setMsg( text + options.getMailFooter() );
             email.send();
         }
         catch ( EmailException e )
         {
             throw new RuntimeException( e );
         }
     }
 }
