 package org.sonatype.maven.plugins.tattletale;
 
 import java.io.File;
 import java.util.LinkedHashSet;
 import java.util.Set;
 
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.codehaus.plexus.util.DirectoryScanner;
 import org.jboss.tattletale.Main;
 
 /**
  * Goal that runs tattletale
  * 
  * @extendsPlugin tattletale-maven
  * @extendsGoal report
  * @author Marvin Froeder
  * @goal tattletale
  * @phase verify
  */
 public class TattletaleMojo
     extends AbstractMojo
 {
     /**
      * Blacklisted
      * 
      * @parameter
      */
     private String[] blacklisted;
 
     /**
      * Class loader structure
      * 
      * @parameter
      */
     private String classloaderStructure;
 
     /**
      * Configuration
      * 
      * @parameter
      */
     private File configuration;
 
     /**
      * Destination directory
      * 
      * @parameter
      */
     private File destination;
 
     /**
      * Excludes
      * 
      * @parameter
      */
     private String[] excludes;
 
     /**
      * Fail on error
      * 
      * @parameter
      */
     private boolean failOnError;
 
     /**
      * Fail on info
      * 
      * @parameter
      */
     private boolean failOnInfo;
 
     /**
      * Fail on warning
      * 
      * @parameter
      */
     private boolean failOnWarn;
 
     /**
      * Filter
      * 
      * @parameter
      */
     private File filter;
 
     /**
      * Profiles
      * 
      * @parameter
      */
     private String[] profiles;
 
     /**
      * Reports
      * 
      * @parameter
      */
     private String[] reports;
 
     /**
      * Scan
      * 
      * @parameter
      */
     private String scan;
 
     /**
      * Source directory
      * 
      * @parameter
      */
     private File source;
 
     /**
      * @parameter
      */
     private File sourcesScan;
 
    /**
     * @parameter expression="${tattletale.skip}"
     */
    private boolean skip;

     public String getSources()
     {
         if ( sourcesScan == null )
         {
             return source.getAbsolutePath();
         }
         DirectoryScanner scan = new DirectoryScanner();
         scan.setBasedir( sourcesScan );
         scan.setIncludes( new String[] { "**/*.jar" } );
         scan.addDefaultExcludes();
         scan.scan();
 
         Set<File> directories = new LinkedHashSet<File>();
         String[] files = scan.getIncludedFiles();
         for ( String file : files )
         {
             directories.add( new File( sourcesScan, file ).getAbsoluteFile().getParentFile() );
         }
 
         final StringBuilder source = new StringBuilder();
         for ( File dir : directories )
         {
             if ( source.length() != 0 )
             {
                 source.append( File.pathSeparator );
             }
             source.append( dir.getAbsolutePath() );
         }
 
         return source.toString();
     }
 
     public void execute()
         throws MojoExecutionException, MojoFailureException
     {
        if ( skip )
        {
            getLog().info( "Skipping tattletale" );
            return;
        }
 
         Main main = new Main();
 
         main.setSource( getSources() );
         main.setDestination( destination.getAbsolutePath() );
 
         if ( configuration != null )
         {
             main.setConfiguration( configuration.getAbsolutePath() );
         }
 
         if ( filter != null )
         {
             main.setFilter( filter.getAbsolutePath() );
         }
 
         main.setClassLoaderStructure( classloaderStructure );
 
         if ( reports != null )
         {
             StringBuilder sb = new StringBuilder();
             for ( int i = 0; i < reports.length; i++ )
             {
                 sb = sb.append( reports[i] );
                 if ( i < reports.length - 1 )
                 {
                     sb = sb.append( "," );
                 }
             }
             main.setReports( sb.toString() );
         }
 
         if ( profiles != null )
         {
             StringBuilder sb = new StringBuilder();
             for ( int i = 0; i < profiles.length; i++ )
             {
                 sb = sb.append( profiles[i] );
                 if ( i < profiles.length - 1 )
                 {
                     sb = sb.append( "," );
                 }
             }
             main.setProfiles( sb.toString() );
         }
 
         if ( excludes != null )
         {
             StringBuilder sb = new StringBuilder();
             for ( int i = 0; i < excludes.length; i++ )
             {
                 sb = sb.append( excludes[i] );
                 if ( i < excludes.length - 1 )
                 {
                     sb = sb.append( "," );
                 }
             }
             main.setExcludes( sb.toString() );
         }
 
         if ( blacklisted != null )
         {
             StringBuilder sb = new StringBuilder();
             for ( int i = 0; i < blacklisted.length; i++ )
             {
                 sb = sb.append( blacklisted[i] );
                 if ( i < blacklisted.length - 1 )
                 {
                     sb = sb.append( "," );
                 }
             }
             main.setBlacklisted( sb.toString() );
         }
 
         main.setFailOnInfo( failOnInfo );
         main.setFailOnWarn( failOnWarn );
         main.setFailOnError( failOnError );
 
         main.setScan( scan );
 
         getLog().info( "Scanning: " + getSources() );
 
         try
         {
             main.execute();
         }
         catch ( Exception e )
         {
             throw new MojoExecutionException( e.getMessage(), e );
         }
     }
 }
