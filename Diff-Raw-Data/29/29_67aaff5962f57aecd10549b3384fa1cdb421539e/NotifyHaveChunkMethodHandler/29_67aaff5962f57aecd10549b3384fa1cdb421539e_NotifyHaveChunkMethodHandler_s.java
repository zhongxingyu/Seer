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
      throw new Exception( "Unknown target file specified in NotifyHaveChunk" );
     }
    
     if ( chunkIndex == null )
     {
       throw new Exception( "No chunk index specified in NotifyHaveChunk");
     }
 
    Flood.RuntimeTargetFile runtimeTargetFile = (Flood.RuntimeTargetFile) receiver.flood.runtimeTargetFiles.get( targetFilename );
    if ( runtimeTargetFile != null )
     {
      if ( chunkIndex.intValue() > 0 && chunkIndex.intValue() < runtimeTargetFile.chunkMap.length )
      {
        runtimeTargetFile.chunkMap[chunkIndex.intValue()] = '1';
      }
      else
      {
        throw new Exception( "Chunk index out of bounds in NotifyHaveChunk for " );
      }
     }
    else
     {
      // FIXME throw exception? we don't know about this targetFile
     }
   }
 }
