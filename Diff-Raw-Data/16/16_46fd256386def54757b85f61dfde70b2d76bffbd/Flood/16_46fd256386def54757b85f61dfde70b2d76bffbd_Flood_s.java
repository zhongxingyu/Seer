 /*
  * Created on Nov 12, 2004
  *
  */
 
 import java.io.*;
 import java.nio.channels.FileChannel;
 import java.nio.channels.FileLock;
 import java.util.*;
 
 /**
  * @author burke
  *  
  */
 public class Flood
 {
   private static final long DOWNLOADING_CHUNK_TIMEOUT = 600000; // 5 minutes
   private static final long TRACKER_REFRESH_TIMEOUT   =  20000; // 20 seconds
   
   public Peer      localPeer         = null;
   public Vector    peerConnections   = new Vector();
   public FloodFile floodFile         = null;
   private Date     lastTrackerUpdate = null;
 
   // runtime flood data
   public class RuntimeTargetFile
   {
     String               name          = null;
     String               nameOnDisk    = null;
     File                 fileObject    = null;
     RandomAccessFile     fileHandle    = null;
     FileChannel          fileChannel   = null;
     FloodFile.TargetFile targetFile    = null;
     long[]               chunkOffsets  = null;
     char[]               chunkMap      = null;
     Vector               chunks        = null;
   }
   
   public class RuntimeChunk
   {
     int               index                  = -1;
     int               weight                 = 0;
     int               size                   = 0;
     RuntimeTargetFile ownerRuntimeTargetFile = null;
     boolean           downloading            = false;
     Date              downloadStartDate      = null;
     PeerConnection    downloadFrom           = null;
   }
   
   public int                 totalBytes        = 0;
   public int                 bytesAtStartup    = 0;
   public int                 bytesMissing      = 0;
   public int                 bytesDownloading  = 0;
 
   public Hashtable           runtimeTargetFiles = new Hashtable();
   public Vector              chunksToDownload   = new Vector();
   public Vector              chunksDownloading  = new Vector();
 
   public Flood()
   {
   }
 
   public Flood(Peer peer, String floodFilename, ChunkPrioritizer chunkPrioritizer)
   {
     localPeer = peer;
     floodFile = new FloodFile( floodFilename );
     floodFile.Read();
     PrioritizeChunks(chunkPrioritizer);
   }
 
   public Flood(Peer peer, String floodFilename)
   {
     this(peer, floodFilename, new ChunkPrioritizer());
   }
 
   public String Id()
   {
     return floodFile.contentHash;
   }
 
   public void LoopOnce()
   {
     
     Date now = new Date();
     
     // reap timed-out chunks
     Iterator chunksDownloadingIter = chunksDownloading.iterator();
     while(chunksDownloadingIter.hasNext())
     {
       RuntimeChunk chunk = (RuntimeChunk) chunksDownloadingIter.next();
       if(now.getTime() - chunk.downloadStartDate.getTime() >= DOWNLOADING_CHUNK_TIMEOUT)
       {
         chunk.downloading = false;
         chunk.downloadStartDate = null;
         chunksDownloading.remove(chunk);
       }
     }
     
     // get chunks
     GetChunk();
     
     // clone the peers Vector and then process them
     {
       Vector peerConnectionsClone = (Vector) peerConnections.clone();
       Iterator peeriter = peerConnectionsClone.iterator();
       for ( ; peeriter.hasNext(); )
       {
         PeerConnection peer = (PeerConnection) peeriter.next();
         peer.LoopOnce();
       }
     }
 
     // reap disconnected peers
     Iterator peeriter = peerConnections.iterator();
     for ( ; peeriter.hasNext(); )
     {
       PeerConnection peer = (PeerConnection) peeriter.next();
       if ( peer.disconnected )
       {
         System.out.println( "Reaping " + peer.id + ":" + peer.hostname + ":" + peer.port );
         peeriter.remove();
       }
     }
     
     // update trackers
     if ( lastTrackerUpdate == null || ( now.getTime() - lastTrackerUpdate.getTime() >= TRACKER_REFRESH_TIMEOUT ) )
     {
       UpdateTrackers();
       lastTrackerUpdate = new Date();
     }
   }
 
   protected void UpdateTrackers()
   {
     if ( floodFile != null && floodFile.trackers != null )
     {
       Iterator trackeriter = floodFile.trackers.iterator();
       for ( ; trackeriter.hasNext(); )
       {
         FloodFile.TrackerInfo tracker = (FloodFile.TrackerInfo) trackeriter.next();
 
         if ( tracker.id.compareTo( localPeer.id ) != 0 )
         {
           PeerConnection peer = FindPeer( tracker.id );
           if ( peer == null )
           {
             peer = new PeerConnection( this, tracker.host, tracker.port, tracker.id );
             peerConnections.add( peer );
           }
 
           // Request the tracker's peer list
           peer.SendMethod( RequestPeerListMethodHandler.methodName, new Vector() );
         }
       }
     }
   }
 
   public PeerConnection FindPeer( final String peerId )
   {
     PeerConnection retVal = null;
     Iterator peeriter = peerConnections.iterator();
     for ( ; peeriter.hasNext(); )
     {
       PeerConnection peer = (PeerConnection) peeriter.next();
       if ( peer.id.compareTo( peerId ) == 0 )
       {
         retVal = peer;
         break;
       }
     }
     return retVal;
   }
 
   public void SetupFilesAndChunks()
   {
     if ( floodFile.targetFiles.size() > 0 )
     {
       runtimeTargetFiles = new Hashtable( floodFile.targetFiles.size() );
       Iterator targetFileIter = Arrays.asList(floodFile.targetFiles.values().toArray()).iterator();
       for ( int fileIndex = 0; targetFileIter.hasNext(); ++fileIndex )
       {
         FloodFile.TargetFile targetFile = (FloodFile.TargetFile) targetFileIter.next();
 
         RuntimeTargetFile runtimeTargetFile = new RuntimeTargetFile();
         runtimeTargetFile.name       = targetFile.name;
         runtimeTargetFile.nameOnDisk = targetFile.name;  // TODO let people override this somehow 
         runtimeTargetFile.targetFile = targetFile;
         runtimeTargetFile.chunks     = new Vector(targetFile.chunks.size());
         
         try
         {
           // Get a file handle and channel for the file on disk
           runtimeTargetFile.fileObject = new File(runtimeTargetFile.nameOnDisk);
 
           if( !runtimeTargetFile.fileObject.exists() )
           {
             // make any necessary dirs
            String targetFileDirectoryPath = runtimeTargetFile.fileObject.getAbsolutePath();
            File runtimeTargetFileDirectory = new File(targetFileDirectoryPath.substring( 0, targetFileDirectoryPath.lastIndexOf( '/' )));
            runtimeTargetFileDirectory.mkdirs();
           }
           
           runtimeTargetFile.fileHandle = new RandomAccessFile(runtimeTargetFile.fileObject, "rw");
           runtimeTargetFile.fileChannel = runtimeTargetFile.fileHandle.getChannel();
         }
         catch (Exception e)
         {
           e.printStackTrace();
         }
 
         runtimeTargetFiles.put( targetFile.name, runtimeTargetFile );
 
         // track some stats
         totalBytes += targetFile.size;
 
         int numChunks = targetFile.chunks.size();
         runtimeTargetFile.chunkOffsets = new long[numChunks];
         runtimeTargetFile.chunkMap = new char[numChunks];
 
         Iterator chunkiter = targetFile.chunks.iterator();
         for ( long nextOffset = 0; chunkiter.hasNext(); )
         {
           FloodFile.Chunk chunk = (FloodFile.Chunk) chunkiter.next();
 
           RuntimeChunk runtimeChunk           = new RuntimeChunk();
           runtimeChunk.index                  = chunk.index;
           runtimeChunk.weight                 = chunk.weight;
           runtimeChunk.size                   = chunk.size;
           runtimeChunk.ownerRuntimeTargetFile = runtimeTargetFile;
           runtimeTargetFile.chunks.add(runtimeChunk.index, runtimeChunk);
           
           // track the offsets
           runtimeTargetFile.chunkOffsets[chunk.index] = nextOffset;
 
           // mark it as invalid
           runtimeTargetFile.chunkMap[chunk.index] = '0';
 
           // see if the existing chunk is valid
           byte[] chunkData = new byte[chunk.size];
 
           try
           {
             FileLock readLock = null;
             try
             {
               readLock = runtimeTargetFile.fileChannel.tryLock();
             }
             catch( Exception e)
             {
               e.printStackTrace();
             }
             
             runtimeTargetFile.fileHandle.seek( nextOffset );
             if ( runtimeTargetFile.fileHandle.read(chunkData) == chunk.size )
             {
               String existingHash = Encoder.SHA1Base64Encode( chunkData, chunk.size );
               if ( existingHash.compareTo( chunk.hash ) == 0 )
               {
                 runtimeTargetFile.chunkMap[chunk.index] = '1';
 
                 // how many bytes did we start with
                 bytesAtStartup += chunk.size;
               }
             }
             // FIXME throw some error
             else
             {
             }
             readLock.release();
           }
           catch ( IOException e )
           {
             e.printStackTrace();
           }
 
           if ( runtimeTargetFile.chunkMap[chunk.index] == '0' )
           {
             chunksToDownload.add( runtimeChunk );
           }
           
           nextOffset += chunk.size;
         }
       }
     }
 
     // how many bytes are we currently missing
     bytesMissing = totalBytes - bytesAtStartup;
   }
 
  public void PrioritizeChunks( ChunkPrioritizer chunkPrioritizer)
   {
     Collections.sort(chunksToDownload, chunkPrioritizer);
   }
   
   protected void GetChunk(  )
   {
     boolean foundchunk = false;
     PeerConnection peerConnectionToUse = null;
     Flood.RuntimeChunk chunkToDownload = null;
     
     // figure out what chunk to get from which peer and ask for it
     Iterator chunkiter = chunksToDownload.iterator();
     while ( chunkiter.hasNext() && !foundchunk )
     {
       Flood.RuntimeChunk chunk = (Flood.RuntimeChunk) chunkiter.next();
       
       if ( !chunk.downloading )
       {
         RuntimeTargetFile runtimeTargetFile = chunk.ownerRuntimeTargetFile;
         Iterator peeriter = peerConnections.iterator();
         while( peeriter.hasNext() && !foundchunk )
         {
           PeerConnection peerConnection = (PeerConnection) peeriter.next();
           if ( peerConnection.chunksDownloading < 1 )
           {
             char[] peerChunkMap = (char[])peerConnection.chunkMaps.get( runtimeTargetFile.targetFile.name );
             if ( peerChunkMap != null )
             {
               if ( peerChunkMap[ chunk.index ] == '1' )
               {
                 foundchunk           = true;
                 peerConnectionToUse  = peerConnection;
                 chunkToDownload      = chunk;
               }
             }
           }
         }
       }
     }
 
     if ( foundchunk )
     {
       Vector parameters = new Vector( 2 );
       parameters.add( chunkToDownload.ownerRuntimeTargetFile.name );
       parameters.add( new Integer( chunkToDownload.index ) );
       
       peerConnectionToUse.chunksDownloading++;
       peerConnectionToUse.SendMethod( RequestChunkMethodHandler.methodName, parameters );
 
       chunkToDownload.downloading       = true; // FIXME this needs to be checked for timeout in LoopOnce
       chunkToDownload.downloadFrom      = peerConnectionToUse;
       chunkToDownload.downloadStartDate = new Date();
       bytesDownloading += chunkToDownload.size;
     }
   }
 
 
 }
