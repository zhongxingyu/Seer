 package de.zib.gndms.kit.network;
 
 /*
  * Copyright 2008-2011 Zuse Institute Berlin (ZIB)
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 
 
 import de.zib.gndms.model.gorfx.FTPTransferState;
 import org.apache.log4j.Logger;
 import org.globus.ftp.ByteRangeList;
 import org.globus.ftp.FileInfo;
 import org.globus.ftp.GridFTPClient;
 import org.globus.ftp.GridFTPRestartMarker;
 import org.globus.ftp.GridFTPSession;
 import org.globus.ftp.MarkerListener;
 import org.globus.ftp.Session;
 import org.globus.ftp.exception.ClientException;
 import org.globus.ftp.exception.ServerException;
 import org.jetbrains.annotations.NotNull;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.Vector;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 
 /**
  * @author  try ma ik jo rr a zib
  * @version $Id$
  * <p/>
  * User: mjorra, Date: 30.09.2008, Time: 13:02:37
  */
 public class GNDMSFileTransfer {
 
     protected final Logger logger = Logger.getLogger( this.getClass() );
 
     public static final String ELLIPSE = Pattern.quote( "..." );
     protected final Pattern ellipse = Pattern.compile( "(.*)"+ ELLIPSE + "$" );
 
     private GridFTPClient sourceClient;
     private GridFTPClient destinationClient;
     private TreeMap<String,String> files;
 
     private String sourcePath;
     private String destinationPath;
 
 
     public Integer getBufferSize() {
         return bufferSize;
     }
 
 
     public void setBufferSize( Integer bufferSize ) {
         this.bufferSize = bufferSize;
     }
 
 
     private Integer bufferSize;
 
 
     /**
      * Prepares the transfer of a list of files.
      *
      * @param fm
      * The given map consists of pairs of source and target file names.
      * The target file name may be null, if it should be identical to the source files name.
      *
      * This resets any previously prepared download stats.
      */
     public void setFiles( TreeMap<String,String> fm ) {
         files = fm;
     }
 
 
     /**
      * This method is provided for convenience and behaves like the above method.
      */
     public void setFiles( String sfn, String tfn ) {
         files = new TreeMap<String,String>( );
         files.put( sfn, tfn );
     }
 
 
     public TreeMap<String, String> getFiles() {
         return files;
     }
 
 
     /**
      * This method can be called before the actual transfer is performed.
      *
      * It ensures that the file set isn't empty. If it is empty the file listing
      * will be requested from the source client.
      *
      * This is useful if one would like to see the file listing befor the transfer starts.
      */
     public void prepareTransfer( ) throws ServerException, IOException, ClientException {
 
         if( sourceClient == null )
             throw new IllegalStateException( enrichExceptionMsg( "no source client provided" ) );
 
         try {
             if( sourcePath != null )
                 sourceClient.changeDir( sourcePath );
 
             if( files == null || files.size( ) == 0 )
                 files = fetchFileListing( null );
             else if( hasEllipse( files ) ) {
                 logger.debug( "Ellipse detected, using awesome new feature." );
                 files = fetchFileListing( makeFileFilter() );
             }
         } catch ( ServerException ex ) {
             ex.setCustomMessage( enrichExceptionMsg( ex.getMessage() ) );
             throw ex;
         } catch ( ClientException ex ) {
             ex.setCustomMessage( enrichExceptionMsg( ex.getMessage() ) );
             throw ex;
         } catch ( IOException ex ) {
             IOException ioe = new IOException( enrichExceptionMsg( ex.getMessage() ) );
             ioe.setStackTrace( ex.getStackTrace() );
             throw ioe;
         }
     }
 
 
     public Pattern makeFileFilter() {
 
         StringBuilder sb = new StringBuilder( "^(" );
         String last = null;
 
         for ( String f : files.keySet() ) {
 
             if( last != null ) {
                 sb.append( "|" );
             }
 
             Matcher matcher = ellipse.matcher( f );
             if ( matcher.matches() ) {
                 sb.append( matcher.group( 1 ) );
                 sb.append( ".*" );
             } else
                 sb.append( f );
 
             last = f;
         }
 
        sb.append( ")" );
 
         return Pattern.compile( sb.toString() );
     }
 
 
     private boolean hasEllipse( TreeMap<String, String> files ) {
 
         for( String f: files.keySet() )
             if( ellipse.matcher( f ).matches() )
                 return true;
 
         return false;
     }
 
 
     /**
      * Estimates the size of a prepared download or transfer.
      * @return The size in byte.
      */
     public long estimateTransferSize( ) throws IOException, ServerException, ClientException {
 
         String lastFileName = null; // required for nice exceptions names
         try {
             prepareTransfer( );
 
             sourceClient.setType( Session.TYPE_ASCII );
 
             Set<String> src = files.keySet();
             long size = 0;
 
             for ( String aSrc : src ) {
                 // todo evaluate usage of msld command
                 lastFileName = aSrc;
                 size += sourceClient.getSize( lastFileName );
             }
 
             return size;
         } catch ( ServerException ex ) {
             ex.setCustomMessage( enrichExceptionMsg( ex.getMessage(), lastFileName ) );
             throw ex;
         } catch ( ClientException ex ) {
             ex.setCustomMessage( enrichExceptionMsg( ex.getMessage(), lastFileName ) );
             throw ex;
         } catch ( IOException ex ) {
             IOException ioe = new IOException( enrichExceptionMsg( ex.getMessage(), lastFileName ) );
             ioe.setStackTrace( ex.getStackTrace() );
             throw ioe;
         }
     }
 
 
     /**
      * Performs the prepared transfer.
      *
      * NOTE:  not implemented yet.
      */
     public void performTransfer( MarkerListener list ) {
 
     }
 
     /**
      * Performs the prepared transfer using a persistent marker listener.
      *
      * If the listener has a state, i.e. a currentFile and a range, then the transfer will uses this state to continue
      * the transfer.
      */
     public void performPersistentTransfer( @NotNull PersistentMarkerListener plist ) throws ServerException, IOException, ClientException {
 
         String currentFile = null;
         try {
             prepareTransfer( );
 
             if( destinationClient == null || files == null )
                 throw new IllegalStateException( );
 
             setupClient ( sourceClient );
 
             setupClient ( destinationClient );
             destinationClient.changeDir( destinationPath );
 
             sourceClient.setActive( destinationClient.setPassive() );
 
             // todo beautify the code below
             boolean resume = plist.hasCurrentFile();
             String  resumeFile = plist.getCurrentFile();
 
             for( String fn : files.keySet() ) {
                 currentFile = fn;
 
                 // if transfer is resumed skip files til last transferred file is found.
                 if( resume && currentFile.equals( resumeFile ) ) {
                     resume = false;
                     resumeSource( plist.getTransferState() );
                 }
 
                 if( !resume ) {
                     plist.setCurrentFile( currentFile );
                     String destinationFile = files.get( currentFile );
                     sourceClient.extendedTransfer( currentFile, destinationClient,
                         ( destinationFile == null ? currentFile : destinationFile ), plist );
                 }
             }
         } catch ( ServerException ex ) {
             ex.setCustomMessage( enrichExceptionMsg( ex.getMessage(), currentFile ) );
             throw ex;
         } catch ( ClientException ex ) {
             ex.setCustomMessage( enrichExceptionMsg( ex.getMessage(), currentFile ) );
             throw ex;
         } catch ( IOException ex ) {
             IOException ioe = new IOException( enrichExceptionMsg( ex.getMessage(), currentFile ) );
             ioe.setStackTrace( ex.getStackTrace() );
             throw ioe;
         }
     }
 
 
     protected void setupClient( GridFTPClient cnt ) throws ServerException, IOException {
         cnt.setType( GridFTPSession.TYPE_IMAGE );
         cnt.setMode( GridFTPSession.MODE_EBLOCK );
 
         if (bufferSize!=null) {
             cnt.setTCPBufferSize( bufferSize );
         }
     }
 
 
     /**
      * Sets the source and target clients for a third party transfer.
      *
      * The clients will be configured according to their desired roles, before
      * any ftp action takes place. So don't change them after calling this method.
      *
      * @param sclnt the source client.
      * @param tclnt the target client.
      */
     public void setClients(  GridFTPClient sclnt, GridFTPClient tclnt ) {
 
         setSourceClient( sclnt );
         setDestinationClient( tclnt );
     }
 
 
     public GridFTPClient getSourceClient() {
         return sourceClient;
     }
 
 
     public void setSourceClient( GridFTPClient sourceClient ) {
         this.sourceClient = sourceClient;
     }
 
 
     public GridFTPClient getTargetClient() {
         return destinationClient;
     }
 
 
     public void setDestinationClient( GridFTPClient destinationClient ) {
         this.destinationClient = destinationClient;
     }
 
 
     public String getSourcePath() {
         return sourcePath;
     }
 
 
     public void setSourcePath( String sourcePath ) {
         this.sourcePath = sourcePath;
     }
 
 
     public String getDestinationPath() {
         return destinationPath;
     }
 
 
     public void setDestinationPath( String destinationPath ) {
         this.destinationPath = destinationPath;
     }
 
 
     /**
      * This loads the ftp byte range args from a FTPTransferState object into a GridFTPClient.
      *
      */
     private void resumeSource( FTPTransferState stat ) throws ServerException, IOException {
 
         ByteRangeList brl = new ByteRangeList();
         GridFTPRestartMarker rm = new GridFTPRestartMarker( stat.getFtpArgsString( ) );
         brl.merge( rm.toVector() );
         sourceClient.setRestartMarker( brl );
     }
 
     
     private TreeMap<String,String> fetchFileListing( Pattern pattern ) throws ClientException, ServerException, IOException {
 
         if( pattern != null )
             logger.debug( "with pattern: " + pattern.pattern() );
 
         TreeMap<String,String> listing = new TreeMap<String,String>( );
         Vector<FileInfo> inf = sourceClient.list( );
         for( FileInfo fi: inf ) {
             if( fi.isFile() ) {
 
                 if( pattern != null ) {
                     if(! pattern.matcher( fi.getName() ).matches() )
                         continue;
                 }
 
                 logger.debug( "adding file: " + fi.getName() );
                 listing.put( fi.getName(), null );
             }
         }
 
         return listing;
     }
 
 
     private String enrichExceptionMsg( String msg ) {
         return enrichExceptionMsg( msg, null );
     }
 
 
     private String enrichExceptionMsg( String msg, String fn ) {
 
         StringWriter nmsg = new StringWriter(  );
         nmsg.write( "Transfer" );
         if( fn != null ) {
             nmsg.write( " with file " +fn );
         }
 
         nmsg.write( " from " +
             printWithNull( sourceClient )
             +  printWithNull( sourcePath ) );
 
         nmsg.write( " to " +
             printWithNull( destinationClient )
                  + printWithNull( destinationPath ) );
 
         nmsg.write( "\nan Exception occurred: " + msg );
 
         return nmsg.toString( );
     }
 
     static private String printWithNull( String o ) {
         return o == null ? "<null>" : o.toString() ;
     }
 
     static private String printWithNull( GridFTPClient o ) {
         if( o == null )
             return "<null>";
 
         return o.getHost() + ":" +  o.getPort();
     }
 }
