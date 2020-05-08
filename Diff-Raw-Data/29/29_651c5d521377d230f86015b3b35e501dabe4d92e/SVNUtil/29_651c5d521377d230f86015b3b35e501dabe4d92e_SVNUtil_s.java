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
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.security.MessageDigest;
 import java.util.HashSet;
 import java.util.Set;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.maven.plugin.logging.Log;
 import org.codehaus.plexus.component.annotations.Component;
 import org.codehaus.plexus.util.FileUtils;
 import org.tmatesoft.svn.core.ISVNLogEntryHandler;
 import org.tmatesoft.svn.core.SVNDepth;
 import org.tmatesoft.svn.core.SVNException;
 import org.tmatesoft.svn.core.SVNLogEntry;
 import org.tmatesoft.svn.core.SVNLogEntryPath;
 import org.tmatesoft.svn.core.SVNURL;
 import org.tmatesoft.svn.core.wc.SVNClientManager;
 import org.tmatesoft.svn.core.wc.SVNDiffClient;
 import org.tmatesoft.svn.core.wc.SVNInfo;
 import org.tmatesoft.svn.core.wc.SVNLogClient;
 import org.tmatesoft.svn.core.wc.SVNRevision;
 import org.tmatesoft.svn.core.wc.SVNUpdateClient;
 import org.tmatesoft.svn.core.wc.SVNWCClient;
 
 /**
  * Simple SVN utility for checking out files from subversion.
  */
 @Component( role = SVNUtil.class, hint = "SVNUtil" )
 public class SVNUtil
 {
     private static final int BYTE_MASK = 0xff;
 
     private EventHandler eventHandler;
     private SVNClientManager svn;
     private SVNUpdateClient updateClient;
     private SVNWCClient wcClient;
     private SVNLogClient logClient;
     private SVNDiffClient diffClient;
 
     /**
      * Instantiates a new SVN util.
      */
     public SVNUtil()
     {
         eventHandler = new EventHandler( );
         svn = SVNClientManager.newInstance();
         updateClient = svn.getUpdateClient();
         updateClient.setEventHandler( eventHandler );
         wcClient = svn.getWCClient();
         wcClient.setEventHandler( eventHandler );
         logClient = svn.getLogClient();
         diffClient = svn.getDiffClient();
     }
 
     /**
      * Determines the correct url. The url can start with a dot or two dots,
      * which will be interpreted as a relative file url.
      * @param svnUrl the url
      * @return the SVN url
      * @throws SVNException if the URL is invalid
      */
     private SVNURL getUrl( String svnUrl ) throws SVNException
     {
         if ( svnUrl.startsWith( ".." ) || svnUrl.startsWith( "." ) )
         {
             String completePath = new File( ".", svnUrl ).getAbsolutePath();
             return SVNURL.parseURIEncoded( "file://" + FileUtils.normalize( completePath ) );
         }
         return SVNURL.parseURIEncoded( svnUrl );
     }
 
     /**
      * Checkout from the given svn url to the destination directory.
      *
      * @param log the log
      * @param svnUrl the svn url
      * @param destination the destination
      * @return the long
      */
     public long checkout( Log log, String svnUrl, String destination )
     {
         SVNURL url;
         try
         {
             eventHandler.setLog( log );
             url = getUrl( svnUrl );
             File dstPath = new File( destination );
             if ( dstPath.exists() )
             {
                 wcClient.doCleanup( dstPath );
             }
             log.info( "Updating " + svnUrl );
             updateClient.doCheckout( url, dstPath, SVNRevision.HEAD, SVNRevision.HEAD, SVNDepth.UNKNOWN, false );
             return eventHandler.getLastUpdateCompletedRevision();
         }
         catch ( SVNException e )
         {
             throw new RuntimeException( e );
         }
     }
 
     /**
      * Gets the information about a file in a local working directory.
      *
      * @param log the log
      * @param fullFilePath the file
      * @return the svn information like revision.
      */
     public SVNInfo checkFile( Log log, String fullFilePath )
     {
         try
         {
             eventHandler.setLog( log );
             SVNInfo info = wcClient.doInfo( new File( fullFilePath ), SVNRevision.WORKING );
             return info;
         }
         catch ( SVNException e )
         {
             throw new RuntimeException( e );
         }
     }
 
     /**
      * Determines whether a given file has been modified between two revisions.
      *
      * @param log the log
     * @param baseLocalPath the base path where the repository has been checked out
      * @param fullLocalPath the file to check
      * @param baseRevision the old revision
      * @param newRevision the new revision
      * @return the change type, e.g. ADD, MODIFICATION or NONE
      */
    public SVNLogChange log( Log log, String baseLocalPath, String fullLocalPath,
             long baseRevision, long newRevision )
     {
         try
         {
            final String relativePath = fullLocalPath.substring( baseLocalPath.length() );
             final Set<String> changeTypes = new HashSet<String>();
             ISVNLogEntryHandler handler = new ISVNLogEntryHandler()
             {
 
                 @Override
                 public void handleLogEntry( SVNLogEntry logEntry ) throws SVNException
                 {
                     for ( SVNLogEntryPath p : logEntry.getChangedPaths().values() )
                     {
                         if ( relativePath.equals( p.getPath() ) )
                         {
                             changeTypes.add( String.valueOf( p.getType() ) );
                         }
                     }
                 }
             };
             eventHandler.setLog( log );
             logClient.doLog( new File[] { new File( fullLocalPath ) }, SVNRevision.create( baseRevision ),
                     SVNRevision.create( newRevision ), false, true, 0, handler );
 
             SVNLogChange result;
             if ( changeTypes.isEmpty() )
             {
                 result = SVNLogChange.NONE;
             }
             else if ( changeTypes.contains( "A" ) )
             {
                 result = SVNLogChange.ADD;
             }
             else
             {
                 result = SVNLogChange.MODIFICATION;
             }
             return result;
 
         }
         catch ( SVNException e )
         {
             throw new RuntimeException( e );
         }
     }
 
     /**
      * Retrieves a unified diff for a given file and revision.
      *
      * @param log the log
      * @param basePath the base path that should be stripped of from the fullLocalPath in the diff output
      * @param fullLocalPath the file
      * @param baseRevision the old revision
      * @param newRevision the new revision
      * @return the diff as a string
      */
     public String diff( Log log, String basePath, String fullLocalPath, long baseRevision, long newRevision )
     {
         try
         {
             eventHandler.setLog( log );
             ByteArrayOutputStream result = new ByteArrayOutputStream();
             diffClient.getDiffGenerator().setBasePath( new File( basePath ) );
             diffClient.doDiff( new File( fullLocalPath ), SVNRevision.create( newRevision ),
                     SVNRevision.create( baseRevision ), SVNRevision.HEAD, SVNDepth.EMPTY, false, result, null );
             return result.toString( "UTF-8" );
         }
         catch ( Exception e )
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
