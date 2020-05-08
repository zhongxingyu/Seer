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
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Map;
 import java.util.Properties;
 import java.util.regex.Pattern;
 
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 
 import net.sf.yal10n.analyzer.ResourceAnalyzer;
 import net.sf.yal10n.analyzer.ResourceBundle;
 import net.sf.yal10n.analyzer.ResourceFile;
 import net.sf.yal10n.diff.UnifiedDiff;
 import net.sf.yal10n.settings.DashboardConfiguration;
 import net.sf.yal10n.settings.Repository;
 import net.sf.yal10n.status.DetectChangesStatus;
 import net.sf.yal10n.status.RepoStatus;
 import net.sf.yal10n.svn.SVNLogChange;
 import net.sf.yal10n.svn.SVNUtil;
 
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.plugins.annotations.Mojo;
 import org.apache.maven.plugins.annotations.Parameter;
 import org.codehaus.plexus.util.FileUtils;
 import org.joda.time.DateTime;
 
 /**
  * Mojo to detect changes between the current and the previous run of this mojo.
  */
 @Mojo( name = "detect-changes", requiresProject = false )
 public class DetectChangesMojo extends AbstractMojo
 {
 
    @Parameter( required = true, property = "yal10n.settings", defaultValue = "./yal10n-settings.json" )
     private String yal10nSettings;
     
    @Parameter( required = true, property = "yal10n.status",
            defaultValue = "${project.build.directory}/yal10n-status.json" )
     private String yal10nStatus;
     
     @Parameter( required = true, defaultValue = "${settings.offline}" )
     private boolean offline;
 
    @Parameter( required = true, defaultValue = "${project.build.directory}" )
     private String outputDirectory;
 
     /**
      * {@inheritDoc}
      */
     public final void execute() throws MojoExecutionException, MojoFailureException
     {
         
         if ( offline )
         {
             throw new MojoFailureException( "Can't work in offline mode." );
         }
 
         SVNUtil svn = new SVNUtil( getLog() );
         ResourceAnalyzer analyzer = new ResourceAnalyzer( svn, getLog() );
         
         DashboardConfiguration config = DashboardConfiguration.readFromFile( yal10nSettings );
         
         DetectChangesStatus previousStatus = DetectChangesStatus.readFromFile( yal10nStatus );
         
         boolean firstRun = true;
         if ( previousStatus.getLastDetection() != null )
         {
             getLog().info( "Previous run: " + previousStatus.getLastDetection() );
             firstRun = false;
         }
         
         DetectChangesStatus newStatus = new DetectChangesStatus();
         newStatus.setLastDetection( DateTime.now().toString() );
         newStatus.setRepos( new ArrayList<RepoStatus>() );
         
         int repoNumber = 0;
         for ( Repository repo : config.getRepositories() )
         {
             repoNumber++;
             getLog().debug( repoNumber + " url: " + repo.getUrl() );
 
             String svnUrl = SVNUtil.toCompleteUrl( config.getRepoPrefix(), repo.getUrl() );
             String repoId = SVNUtil.toRepoId( config.getRepoPrefix(), repo.getUrl() );
             
             String dstPath = FileUtils.normalize( outputDirectory + "/checkouts/" + repoId + "/" );
             long revision = svn.checkout( svnUrl, dstPath );
             
             RepoStatus status = new RepoStatus();
             status.setId( repoId );
             status.setRevision( revision );
             status.setCompleteRepoUrl( svnUrl );
             newStatus.getRepos().add( status );
             
             analyzer.analyze( svnUrl, dstPath, config, repo, repoId );
         }
         newStatus.writeToFile( yal10nStatus );
         
         if ( !firstRun )
         {
             Map<String, ResourceBundle> bundles = analyzer.getBundles();
             getLog().info( "found " + bundles.size() + " bundles:" );
             for ( Map.Entry<String, ResourceBundle> entry : bundles.entrySet() )
             {
                 ResourceBundle bundle = entry.getValue();
                 getLog().info( "  " + entry.getKey() );
                 String repoId = bundle.getRepoId();
                 ResourceFile defaultFile = bundle.getDefaultFile();
                 if ( defaultFile != null && previousStatus.getRepoStatusById( repoId ) != null )
                 {
                     
                     String fullLocalPath = defaultFile.getFullLocalPath();
                     String fullSvnPath = defaultFile.getSVNPath();
                     Repository repo = config.getRepositoryById( repoId );
                     String svnUrl = SVNUtil.toCompleteUrl( config.getRepoPrefix(), repo.getUrl() );
                     String viewvcUrl = SVNUtil.toCompleteUrl( config.getViewvcPrefix(),
                             repo.getViewvcUrl() == null ? repo.getUrl() : repo.getViewvcUrl() );
                     String dstPath = FileUtils.normalize( outputDirectory + "/checkouts/" + repoId + "/" );
                     
                     RepoStatus oldRepoStatus = previousStatus.getRepoStatusById( repoId );
                     RepoStatus newRepoStatus = newStatus.getRepoStatusById( repoId );
                     
                     long oldRevision = oldRepoStatus.getRevision();
                     long newRevision = newRepoStatus.getRevision();
                     
                     getLog().debug( "Analyzing changes for" );
                     getLog().debug( "  " + fullLocalPath );
                     getLog().debug( "  old revision: " + oldRevision + " new revision: " + newRevision );
                     getLog().debug( "" );
                     
                     if ( oldRevision < newRevision )
                     {
                         SVNLogChange changesFound = svn.log( fullLocalPath, oldRevision + 1, newRevision, 10 );
                         
                         String diff = svn.diff( dstPath, fullLocalPath, oldRevision, newRevision );
                         
                         getLog().debug( "    Changes found: " + changesFound );
                         if ( changesFound == SVNLogChange.MODIFICATION )
                         {
                             String viewvcDiff = buildViewvcUrl( fullSvnPath, svnUrl, viewvcUrl );
                             viewvcDiff += "?r1=" + oldRevision + "&r2=" + newRevision;
                             getLog().info( "    Change found: ViewVC url: " + viewvcDiff );
                             sendEmail( config, bundle.getProjectName(), viewvcDiff, diff );
                         }
                         else if ( changesFound == SVNLogChange.ADD )
                         {
                             String viewvcDiff = buildViewvcUrl( fullSvnPath, svnUrl, viewvcUrl );
                             viewvcDiff += "?view=markup";
                             getLog().info( "    Change found: ViewVC url: " + viewvcDiff );
                             sendEmail( config, bundle.getProjectName(), viewvcDiff, diff );
                         }
                     }
                     else
                     {
                         getLog().debug( "    No changes since last detect-changes run" );
                     }
                 }
                 else
                 {
                     getLog().warn( "    No default messages file for bundle " + bundle.getId() );
                 }
             }
         }
     }
 
     private void sendEmail( DashboardConfiguration config, String projectName, String viewvcDiff, String diff )
     {
         Properties props = new Properties();
         props.put( "mail.smtp.host", config.getNotification().getSmtpServer() );
         props.put( "mail.smtp.port", config.getNotification().getSmtpPort() );
         Session session = Session.getInstance( props );
 
         try
         {
             UnifiedDiff unifiedDiff = new UnifiedDiff( diff );
             String from = config.getNotification().getMailFrom();
             String recipients = config.getNotification().getRecipients();
             String subject = config.getNotification().getSubject()
                     .replaceAll( Pattern.quote( "{{projectName}}" ), projectName );
 
             MimeMessage msg = new MimeMessage( session );
             msg.setFrom( new InternetAddress( from ) );
             msg.setRecipients( Message.RecipientType.TO, recipients );
             msg.setSubject( subject );
             msg.setSentDate( new Date() );
 
             msg.setContent( "<html><body>Changes detected in <strong>" + projectName + "</strong><br>"
                     + "<p>See here: <a href=\"" + viewvcDiff + "\">" + viewvcDiff + "</a></p>"
                     + "<br>"
                     + "<strong>Diff output:</strong><br>"
                     + unifiedDiff.asHtmlSnippet()
                     + "</body></html>", "text/html" );
             
             Transport.send( msg );
 
             getLog().info( "Email sent for project " + projectName + " to " + recipients );
         }
         catch ( MessagingException mex )
         {
             throw new RuntimeException( mex );
         }
     }
 
     /**
      * @param fullSvnPath prefix+url+path  file:///foo/svnrepos/a/trunk/dir/file.x
      * @param svnUrl      prefix+url       file:///foo/svnrepos/a/trunk
      * @param viewvcUrl   prefix+url       http://viewvc/a/trunk
      * @return            prefix+url+path  http://viewvc/a/trunk/dir/file.x
      */
     private String buildViewvcUrl( String fullSvnPath, String svnUrl, String viewvcUrl )
     {
         String svnPath = fullSvnPath.substring( svnUrl.length() );
         return viewvcUrl + svnPath;
     }
 }
