 /*
  * Created on Nov 12, 2004
  *
  */
 
 import java.util.Vector;
 
 /**
  * @author burke
  *  
  */
 public class NotifyHaveChunkMethodHandler implements MethodHandler
 {
   final static String methodName = "NotifyHaveChunk";
 
   public String getMethodName()
   {
     return methodName;
   }
 
   // TODO - test
   public void HandleMethod( PeerConnection receiver, final Vector parameters ) throws Exception
   {
     if ( receiver.flood == null )
     {
       throw new Exception( "flood specific method from an unregistered peer!" );
     }
 
     final String targetFilename = (String) parameters.elementAt( 0 );
     final Integer chunkIndex = (Integer) parameters.elementAt( 1 );
 
     if ( targetFilename == null )
     {
      throw new Exception( "No target file specified in NotifyHaveChunk" );
     }
     if ( chunkIndex == null )
     {
       throw new Exception( "No chunk index specified in NotifyHaveChunk");
     }
 
    char[] peerChunkMap = (char[]) receiver.chunkMaps.get(targetFilename);
    if( peerChunkMap == null )
     {
      throw new Exception( "Unknown target file specified in NotifyHaveChunk");
     }

    if(chunkIndex.intValue() > peerChunkMap.length)
     {
      throw new Exception( "Chunk index out of bounds in NotifyHaveChunk");
     }
    
    peerChunkMap[chunkIndex.intValue()] = '1';
   }
 }
