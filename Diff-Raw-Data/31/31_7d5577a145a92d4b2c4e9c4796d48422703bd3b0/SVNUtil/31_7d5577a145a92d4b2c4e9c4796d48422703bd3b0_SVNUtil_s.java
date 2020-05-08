 package net.sf.yal10n.svn;
 
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
 import java.security.MessageDigest;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import net.sf.yal10n.settings.ScmType;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.maven.plugin.logging.Log;
 import org.apache.maven.scm.ChangeFile;
 import org.apache.maven.scm.ChangeSet;
 import org.apache.maven.scm.CommandParameters;
 import org.apache.maven.scm.ScmException;
 import org.apache.maven.scm.ScmFileSet;
 import org.apache.maven.scm.ScmFileStatus;
 import org.apache.maven.scm.ScmResult;
 import org.apache.maven.scm.ScmRevision;
 import org.apache.maven.scm.command.changelog.ChangeLogScmRequest;
 import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
 import org.apache.maven.scm.command.checkout.CheckOutScmResult;
 import org.apache.maven.scm.command.diff.DiffScmResult;
 import org.apache.maven.scm.command.info.InfoItem;
 import org.apache.maven.scm.command.info.InfoScmResult;
 import org.apache.maven.scm.manager.BasicScmManager;
 import org.apache.maven.scm.manager.ScmManager;
 import org.apache.maven.scm.provider.ScmProvider;
 import org.apache.maven.scm.provider.ScmProviderRepository;
 import org.apache.maven.scm.provider.git.gitexe.GitExeScmProvider;
 import org.apache.maven.scm.provider.svn.svnexe.SvnExeScmProvider;
 import org.apache.maven.scm.repository.ScmRepository;
 import org.codehaus.plexus.component.annotations.Component;
 
 /**
  * Simple SVN utility for checking out files from subversion.
  */
 @Component( role = SVNUtil.class, hint = "SVNUtil" )
 public class SVNUtil
 {
     private static final int BYTE_MASK = 0xff;
 
     private ScmManager scmManager;
 
     /**
      * Instantiates a new SVN util.
      */
     public SVNUtil()
     {
         scmManager = new BasicScmManager();
         scmManager.setScmProvider( "svn", new SvnExeScmProvider() );
         scmManager.setScmProvider( "git", new GitExeScmProvider() );
     }
 
     /**
      * Determines the correct url. The url can start with a dot or two dots,
      * which will be interpreted as a relative file url.
      * @param svnUrl the url
      * @return the SVN url
      */
     private String getUrl( String svnUrl )
     {
         if ( svnUrl.startsWith( ".." ) || svnUrl.startsWith( "." ) )
         {
             String completePath = new File( ".", svnUrl ).getAbsolutePath();
             return "file://" + completePath;
         }
         return svnUrl;
     }
 
     private String createScmSvnUrl( ScmType type, String svnUrl )
     {
         String result = getUrl( svnUrl );
         if ( !result.startsWith( "scm:" ) )
         {
             switch ( type )
             {
             case SVN:
                 result = "scm:svn:" + result;
                 break;
             case GIT:
                 result = "scm:git:" + result;
                 break;
             default:
                 throw new RuntimeException( "Scm type " + type + " is not supported." );
             }
         }
         return result;
     }
 
     private void checkResult( ScmResult result )
     {
         if ( !result.isSuccess() )
         {
             System.err.println( "Provider message:" );
 
             System.err.println( result.getProviderMessage() == null ? "" : result.getProviderMessage() );
 
             System.err.println( "Command output:" );
 
             System.err.println( result.getCommandOutput() == null ? "" : result.getCommandOutput() );
 
             throw new RuntimeException( "Command failed." + StringUtils.defaultString( result.getProviderMessage() ) );
         }
     }
 
     /**
      * Checkout from the given svn url to the destination directory.
      *
      * @param log the log
      * @param type the scm type
      * @param svnUrl the svn url
      * @param destination the destination
      * @return the current checked out version
      */
     public String checkout( Log log, ScmType type, String svnUrl, String destination )
     {
         try
         {
             log.info( "Updating " + svnUrl );
 
             String scmUrl = createScmSvnUrl( type, svnUrl );
             log.debug( "Converted Url: " + scmUrl );
 
             ScmProvider scm = scmManager.getProviderByUrl( scmUrl );
             ScmRepository repository = scmManager.makeScmRepository( scmUrl );
             ScmProviderRepository providerRepository = repository.getProviderRepository();
 
             File dstPath = new File( destination );
             if ( dstPath.exists() && !dstPath.isDirectory() )
             {
                 throw new RuntimeException( "Path is not a directory: " + dstPath );
             }
             else if ( !dstPath.exists() && !dstPath.mkdirs() )
             {
                 throw new RuntimeException( "Couldn't create directory " + dstPath );
             }
             CheckOutScmResult checkOutResult = scm.checkOut( repository, new ScmFileSet( dstPath ) );
             checkResult( checkOutResult );
             String revision = checkOutResult.getRevision();
             if ( revision == null )
             {
                 InfoScmResult info = scm.info( providerRepository, new ScmFileSet( dstPath ), null );
                 checkResult( info );
                 revision = info.getInfoItems().get( 0 ).getRevision();
             }
             log.info( "At revision " + revision );
             return revision;
         }
         catch ( ScmException e )
         {
             throw new RuntimeException( e );
         }
     }
 
     /**
      * Gets the information about a file in a local working directory.
      *
      * @param log the log
      * @param type the scm type
      * @param svnUrl the checked out repository url
      * @param baseDir the directory where the repository has been checked out
      * @param relativeFilePath the file to check relative to baseDir
      * @return the svn information like revision.
      */
     public SVNInfo checkFile( Log log, ScmType type, String svnUrl, String baseDir, String relativeFilePath )
     {
         try
         {
             String scmUrl = createScmSvnUrl( type, svnUrl );
             ScmProvider scm = scmManager.getProviderByUrl( scmUrl );
             ScmRepository repository = scmManager.makeScmRepository( scmUrl );
             ScmProviderRepository providerScmRepository = repository.getProviderRepository();
             ScmFileSet fileSet = new ScmFileSet( new File( baseDir ), new File( relativeFilePath ) );
             InfoScmResult info = scm.info( providerScmRepository, fileSet, (CommandParameters) null );
             checkResult( info );
             InfoItem item = info.getInfoItems().get( 0 );
             return new SVNInfo( item.getLastChangedRevision(), item.getLastChangedDate() );
         }
         catch ( ScmException e )
         {
             throw new RuntimeException( e );
         }
     }
 
     /**
      * Determines whether a given file has been modified between two revisions.
      *
      * @param log the log
      * @param type the scm type
      * @param svnUrl the repository url
      * @param checkoutDir the checkout directory
      * @param relativeFilePath the file to check, relative to the checkout directory
      * @param baseRevision the old revision (exclusive)
      * @param newRevision the new revision (inclusive)
      * @return the change type, e.g. ADD, MODIFICATION or NONE
      */
     public SVNLogChange log( Log log, ScmType type, String svnUrl, String checkoutDir, String relativeFilePath,
             String baseRevision, String newRevision )
     {
         try
         {
             String scmUrl = createScmSvnUrl( type, svnUrl );
             ScmProvider scm = scmManager.getProviderByUrl( scmUrl );
             ScmRepository repository = scmManager.makeScmRepository( scmUrl );
             ChangeLogScmRequest scmRequest = new ChangeLogScmRequest( repository,
                     new ScmFileSet( new File( checkoutDir ), new File ( relativeFilePath ) ) );
             scmRequest.setStartRevision( new ScmRevision( baseRevision ) );
             scmRequest.setEndRevision( new ScmRevision( newRevision ) );
             ChangeLogScmResult changeLog = scm.changeLog( scmRequest );
             checkResult( changeLog );
 
             Set<String> changeTypes = new HashSet<String>();
             List<ChangeSet> changeSets = changeLog.getChangeLog().getChangeSets();
             for ( ChangeSet cs : changeSets )
             {
                 for ( ChangeFile f : cs.getFiles() )
                 {
                     if ( f.getName().endsWith( relativeFilePath ) )
                     {
                         changeTypes.add( String.valueOf( f.getAction() ) );
                     }
                 }
             }
             SVNLogChange result;
             if ( changeTypes.isEmpty() )
             {
                 result = SVNLogChange.NONE;
             }
             else if ( changeTypes.contains( ScmFileStatus.ADDED.toString() ) )
             {
                 result = SVNLogChange.ADD;
             }
             else
             {
                 result = SVNLogChange.MODIFICATION;
             }
             return result;
         }
         catch ( ScmException e )
         {
             throw new RuntimeException( e );
         }
     }
 
     /**
      * Retrieves a unified diff for a given file and revision.
      *
      * @param log the log
      * @param type the scm type
      * @param svnUrl the repository url
      * @param checkoutDir the checkout directory
      * @param relativeFilePath the file to diff, relative to the checkout directory
      * @param baseRevision the old revision
      * @param newRevision the new revision
      * @return the diff as a string
      */
     public String diff( Log log, ScmType type, String svnUrl, String checkoutDir, String relativeFilePath,
             String baseRevision, String newRevision )
     {
         try
         {
             String scmUrl = createScmSvnUrl( type, svnUrl );
             ScmRepository repository = scmManager.makeScmRepository( scmUrl );
 
             ScmFileSet scmFileSet = new ScmFileSet( new File( checkoutDir ), new File( relativeFilePath ) );
             DiffScmResult diffResult = scmManager.diff( repository, scmFileSet,
                     new ScmRevision( baseRevision ), new ScmRevision( newRevision ) );
             checkResult( diffResult );
             return diffResult.getPatch();
         }
         catch ( ScmException e )
         {
             throw new RuntimeException( e );
         }
     }
 
     /**
      * Utility method to combine a prefix and url to get the complete repository URL.
      * Duplicated slashed will be removed.
      * @param repoPrefix the prefix
      * @param repoUrl the url
      * @return the complete repo url
      */
     public static String toCompleteUrl( String repoPrefix, String repoUrl )
     {
         String prefix = StringUtils.trimToEmpty( repoPrefix );
         String suffix = StringUtils.trimToEmpty( repoUrl );
         if ( !prefix.isEmpty() && !prefix.endsWith( "/" ) )
         {
             prefix = prefix + "/";
         }
         if ( suffix.startsWith( "/" ) )
         {
             suffix = suffix.substring( 1 );
         }
         if ( suffix.endsWith( "/" ) )
         {
             suffix = suffix.substring( 0, suffix.lastIndexOf( "/" ) );
         }
         return prefix + suffix;
     }
 
     /**
      * Calculates a unique id for the given repo url.
      * @param repoPrefix the prefix
      * @param repoUrl the url
      * @return the id
      */
     public static String toRepoId( String repoPrefix, String repoUrl )
     {
         String completeUrl = toCompleteUrl( repoPrefix, repoUrl );
         try
         {
             MessageDigest digest = MessageDigest.getInstance( "MD5" );
             byte[] md5 = digest.digest( completeUrl.getBytes( "UTF-8" ) );
             StringBuilder sb = new StringBuilder( 32 );
             for ( int i = 0; i < md5.length; i++ )
             {
                 int part = md5[i] & BYTE_MASK;
                 String h = Integer.toHexString( part );
                 if ( part < 0x10 )
                 {
                     sb.append( "0" );
                 }
                 sb.append( h );
             }
             return sb.toString();
         }
         catch ( Exception e )
         {
             throw new RuntimeException( e );
         }
     }
 }
