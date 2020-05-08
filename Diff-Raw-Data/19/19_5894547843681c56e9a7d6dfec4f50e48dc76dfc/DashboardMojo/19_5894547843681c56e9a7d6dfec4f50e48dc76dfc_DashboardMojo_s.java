 package net.sf.yal10n;
 
 /*
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Map;
 import java.util.Properties;
 
 import net.sf.yal10n.analyzer.ResourceAnalyzer;
 import net.sf.yal10n.analyzer.ResourceBundle;
 import net.sf.yal10n.dashboard.DashboardModel;
 import net.sf.yal10n.dashboard.DashboardRenderer;
 import net.sf.yal10n.report.ReportRenderer;
 import net.sf.yal10n.settings.DashboardConfiguration;
 import net.sf.yal10n.settings.Repository;
 import net.sf.yal10n.svn.SVNUtil;
 import net.sf.yal10n.tmx.TranslationMemoryRenderer;
 
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.plugins.annotations.Mojo;
 import org.apache.maven.plugins.annotations.Parameter;
 import org.codehaus.plexus.util.FileUtils;
 import org.codehaus.plexus.util.IOUtil;
 
 /**
  * This mojo creates a dashboard like overview of all resource bundles.
  */
 @Mojo( name = "dashboard", requiresProject = false )
 public class DashboardMojo extends AbstractMojo
 {
     private static final String GROUP_ID = "net.sf.yal10n";
     private static final String ARTIFACT_ID = "yal10n-maven-plugin";
 
     /** The configuration of the dashboard is read from this file. The file format is json. */
    @Parameter( required = true, property = "yal10n.settings", defaultValue = "./yal10n-settings.json" )
     private String yal10nSettings;
 
     /** Whether to update the local workspace checkouts or not. */
     @Parameter( required = true, defaultValue = "${settings.offline}" )
     private boolean offline;
 
     /** The directory where the dashboard html files should be created. */
    @Parameter( required = true, defaultValue = "${project.build.directory}" )
     private String outputDirectory;
 
     /**
      * {@inheritDoc}
      */
     public final void execute() throws MojoExecutionException,
         MojoFailureException
     {
 
         SVNUtil svn = new SVNUtil( getLog() );
         DashboardConfiguration config = DashboardConfiguration.readFromFile( yal10nSettings );
 
         ResourceAnalyzer analyzer = new ResourceAnalyzer( svn, getLog() );
 
         int repoNumber = 0;
         for ( Repository repo : config.getRepositories() )
         {
             repoNumber++;
             getLog().debug( repoNumber + " url: " + repo.getUrl() );
 
             String svnUrl = SVNUtil.toCompleteUrl( config.getRepoPrefix(), repo.getUrl() );
             String repoId = SVNUtil.toRepoId( config.getRepoPrefix(), repo.getUrl() );
             String dstPath = FileUtils.normalize( outputDirectory + "/checkouts/" + repoId + "/" );
 
             if ( offline )
             {
                 getLog().info( "Offline mode - not updating repo: " + svnUrl );
             }
             else
             {
                 svn.checkout( svnUrl, dstPath );
             }
 
             analyzer.analyze( svnUrl, dstPath, config, repo, repoId );
         }
 
         Map<String, ResourceBundle> bundles = analyzer.getBundles();
         getLog().info( "found " + bundles.size() + " bundles:" );
         for ( String basePath : bundles.keySet() )
         {
             getLog().info( "  " + basePath );
         }
 
         new DashboardRenderer( outputDirectory )
             .render( DashboardModel.create( config.getLanguages(), bundles.values(), config.isCreateTMX() ) );
         ReportRenderer reportRenderer = new ReportRenderer( outputDirectory );
         File reportDirectory = new File( FileUtils.normalize( outputDirectory + "/reports" ) );
         if ( !reportDirectory.exists() && !reportDirectory.mkdirs() )
         {
             throw new MojoExecutionException( "Couldn't create directory: " + reportDirectory );
         }
         for ( ResourceBundle bundle : bundles.values() )
         {
             reportRenderer.render( bundle.getReport() );
         }
         
         if ( config.isCreateTMX() )
         {
             TranslationMemoryRenderer tmxRenderer = new TranslationMemoryRenderer( outputDirectory );
             tmxRenderer.render( bundles.values() );
             for ( ResourceBundle bundle : bundles.values() )
             {
                 tmxRenderer.render( bundle );
             }
         }
         else
         {
             getLog().info( "Skipping TMX creation." );
         }
 
         OutputStream theme = null;
         try
         {
             theme = new FileOutputStream( FileUtils.normalize( outputDirectory + "/default.css" ) );
             IOUtil.copy( DashboardMojo.class.getResourceAsStream( "/themes/default.css" ), theme );
         }
         catch ( IOException e )
         {
             throw new MojoExecutionException( "Couldn't write stylesheet", e );
         }
         finally
         {
             IOUtil.close( theme );
         }
     }
 
     /**
      * Determines the version of this plugin.
      * @return the version
      */
     public static String getVersion()
     {
         String result = "unkown version";
         InputStream properties = DashboardMojo.class.getResourceAsStream( 
                 "/META-INF/maven/" + GROUP_ID + "/" + ARTIFACT_ID + "/pom.properties" );
         if ( properties != null )
         {
             try
             {
                 Properties props = new Properties();
                 props.load( properties );
                 result = props.getProperty( "version" );
             }
             catch ( IOException e )
             {
                 result = "problem determining version";
             }
             finally
             {
                 IOUtil.close( properties );
             }
         }
         return result;
     }
 
 }
