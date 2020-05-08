 /*
  * Created on Nov 12, 2004
  *
  */
 
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.*;
 
 /**
  * @author burke
  *  
  */
 public class RequestChunkMethodHandler implements MethodHandler
 {
   final static String methodName = "RequestChunk";
 
   public String getMethodName()
   {
     return methodName;
   }
 
   public void HandleMethod( PeerConnection receiver, final Vector parameters ) throws Exception
   {
     if ( receiver.flood == null )
     {
       throw new Exception( "flood specific method from an unregistered peer!" );
     }
 
     String fileName = (String) parameters.elementAt( 0 );
     Integer chunkIndex = (Integer) parameters.elementAt( 1 );
 
     // read in the appropriate chunk data and send it across the wire
     Vector scParams = new Vector( 3 );
     scParams.add( fileName );
     scParams.add( chunkIndex );
 
     FloodFile.TargetFile targetFile = (FloodFile.TargetFile) receiver.flood.floodFile.targetFiles.get( fileName );
     // 
     if ( targetFile == null )
     {
       throw new Exception( "peer requested invalid data!" );
     }
 
     Flood.RuntimeTargetFile runtimeTargetFile = (Flood.RuntimeTargetFile) receiver.flood.runtimeTargetFiles.get( targetFile.name );
 
     if ( runtimeTargetFile == null )
     {
       throw new Exception( "peer requested an invalid file!" );
     }
 
     if ( runtimeTargetFile.chunkMap[chunkIndex.intValue()] == '0' )
     {
       throw new Exception( "peer requested invalid data!" );
     }
 
     FloodFile.Chunk targetChunk = (FloodFile.Chunk) targetFile.chunks.get( chunkIndex.intValue() );
    long chunkOffset = runtimeTargetFile.chunkOffsets[chunkIndex.intValue()];
 
     InputStream inputFileStream = null;
     try
     {
       inputFileStream = new FileInputStream( fileName );
     }
     catch ( Exception e )
     {
       throw new Exception( "error opening file: " + fileName + " : " + e );
     }
 
     byte[] chunkData = new byte[targetChunk.size];
     int bytesRead = 0;
 
     try
     {
       inputFileStream.skip( chunkOffset );
       bytesRead = inputFileStream.read( chunkData );
     }
     catch ( IOException e )
     {
       throw new Exception( "error reading from file: " + fileName + " : " + e );
     }
 
     if ( bytesRead != targetChunk.size )
     {
       throw new Exception( "error reading from file: " + fileName );
     }
 
     String testHash = Encoder.SHA1Base64Encode( chunkData, targetChunk.size );
     if ( testHash.compareTo( targetChunk.hash ) != 0 )
     {
       throw new Exception( "file data is not correct!" );
     }
 
     scParams.add( chunkData );
     receiver.SendMethod( SendChunkMethodHandler.methodName, scParams );
   }
 }
