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
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Properties;
 import java.util.regex.Pattern;
 
 import javax.mail.Address;
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
 
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.plugins.annotations.Mojo;
 import org.apache.maven.plugins.annotations.Parameter;
 import org.codehaus.plexus.util.FileUtils;
 import org.codehaus.plexus.util.IOUtil;
 import org.joda.time.DateTime;
 
 /**
  * Mojo to detect changes between the current and the previous run of this mojo.
  */
 @Mojo( name = "detect-changes", requiresProject = false )
 public class DetectChangesMojo extends BaseMojo
 {
 
     @Parameter( required = true, property = "yal10n.status", defaultValue = "target/yal10n-status.json" )
     private String yal10nStatus;
     
     /**
      * Instantiates a new detect changes mojo.
      */
     public DetectChangesMojo()
     {
         super();
     }
 
     /**
      * Instantiates a new detect changes mojo.
      *
      * @param svn the svn
      * @param analyzer the analyzer
      */
     DetectChangesMojo( SVNUtil svn, ResourceAnalyzer analyzer )
     {
         this.svn = svn;
         this.analyzer = analyzer;
     }
 
     /**
      * {@inheritDoc}
      */
     public final void execute() throws MojoExecutionException, MojoFailureException
     {
         
         if ( offline )
         {
             throw new MojoFailureException( "Can't work in offline mode." );
         }
 
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
 
         checkout( config, newStatus );
         newStatus.writeToFile( yal10nStatus );
 
         if ( !firstRun )
         {
             List<ResourceBundle> bundles = analyzer.getBundles();
             getLog().info( "Found " + bundles.size() + " bundles:" );
             for ( ResourceBundle bundle : bundles )
             {
                 getLog().info( "  " + bundle.getLocaleBasePath() );
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
                     
                     String oldRevision = oldRepoStatus.getRevision();
                     String newRevision = newRepoStatus.getRevision();
                     
                     getLog().debug( "Analyzing changes for" );
                     getLog().debug( "  " + fullLocalPath );
                     getLog().debug( "  old revision: " + oldRevision + " new revision: " + newRevision );
                     getLog().debug( "" );
                     
                     SVNLogChange changesFound = svn.log( getLog(), repo.getType(), svnUrl, dstPath,
                            defaultFile.getRelativeFilePath(), oldRevision, newRevision );
 
                     getLog().debug( "    Changes found: " + changesFound );
                     if ( changesFound == SVNLogChange.MODIFICATION )
                     {
                         String diff = svn.diff( getLog(), repo.getType(), svnUrl, dstPath,
                                 defaultFile.getRelativeFilePath(), oldRevision, newRevision );
                         UnifiedDiff unifiedDiff = new UnifiedDiff( diff );
                         if ( unifiedDiff.getHunks().isEmpty() )
                         {
                             getLog().info( "    There were no changes to the file content of " + fullSvnPath );
                         }
                         else
                         {
                             String viewvcDiff = buildViewvcUrl( fullSvnPath, svnUrl, viewvcUrl );
                             viewvcDiff += "?r1=" + oldRevision + "&r2=" + newRevision;
                             getLog().info( "    Change found: ViewVC url: " + viewvcDiff );
                             sendEmail( config, repo, bundle.getProjectName(), viewvcDiff, unifiedDiff );
                         }
                     }
                     else if ( changesFound == SVNLogChange.ADD )
                     {
                         String viewvcDiff = buildViewvcUrl( fullSvnPath, svnUrl, viewvcUrl );
                         viewvcDiff += "?view=markup";
                         getLog().info( "    Change found: ViewVC url: " + viewvcDiff );
 
                         String filename = fullLocalPath.substring( dstPath.length() );
                         String fileContent = readFile( fullLocalPath );
                         UnifiedDiff unifiedDiff = new UnifiedDiff( fileContent, true, filename );
                         sendEmail( config, repo, bundle.getProjectName(), viewvcDiff, unifiedDiff );
                     }
                 }
                 else
                 {
                     getLog().warn( "    No default messages file for bundle " + bundle.getId() );
                 }
             }
         }
     }
 
     private String readFile( String fullLocalPath )
     {
         Reader r = null;
         String result = null;
         try
         {
             File f = new File( fullLocalPath );
             r = new InputStreamReader( new FileInputStream( f ), "UTF-8" );
             result = IOUtil.toString( r );
         }
         catch ( IOException e )
         {
             throw new RuntimeException( e );
         }
         finally
         {
             IOUtil.close( r );
         }
         return result;
     }
 
     private void sendEmail( DashboardConfiguration config, Repository repo, String projectName, String viewvcDiff,
             UnifiedDiff unifiedDiff )
     {
         Properties props = new Properties();
         props.put( "mail.smtp.host", config.getNotification().getSmtpServer() );
         props.put( "mail.smtp.port", config.getNotification().getSmtpPort() );
         Session session = Session.getInstance( props );
 
         try
         {
             String from = config.getNotification().getMailFrom();
             List<Address> recipients = config.getNotification().getRecipientsAddresses();
             recipients.addAll( repo.getNotification().getRecipientsAddresses() );
             String subject = config.getNotification().getSubject()
                     .replaceAll( Pattern.quote( "{{projectName}}" ), projectName );
 
             MimeMessage msg = new MimeMessage( session );
             msg.setFrom( new InternetAddress( from ) );
             msg.setRecipients( Message.RecipientType.TO, recipients.toArray( new Address[recipients.size()] ) );
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
 
     /**
      * Sets the yal10n status.
      *
      * @param yal10nStatus the new yal10n status
      */
     public void setYal10nStatus( String yal10nStatus )
     {
         this.yal10nStatus = yal10nStatus;
     }
 }
