 /*
  * Created on Nov 12, 2004
  *
  */
 
 import java.util.*;
 
 /**
  * @author burke
  *  
  */
 public class SendChunkMapsMethodHandler implements MethodHandler
 {
   final static String methodName = "SendChunkMaps";
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
     
     Iterator paramIter = parameters.iterator();
     while(paramIter.hasNext())
     {
       String targetFilename = (String) paramIter.next();
       String chunkMapstring = (String) paramIter.next();
       
       Flood.RuntimeTargetFile runtimeTargetFile = (Flood.RuntimeTargetFile) receiver.flood.runtimeTargetFiles.get(targetFilename); 
       if( runtimeTargetFile == null )
       {
         throw new Exception( "Unknown target filename specified in " + methodName);
       }
            
      char[] peerChunkMap = new char[runtimeTargetFile.chunkMap.length];
      if( chunkMapstring.length() > peerChunkMap.length )
       {
          throw new Exception( "ChunkMap out of bounds in " + methodName);
       }
       
       for( int chunkMapIndex = 0; chunkMapIndex < runtimeTargetFile.chunkMap.length; chunkMapIndex++ )
       {
        peerChunkMap[chunkMapIndex] = chunkMapstring.charAt(chunkMapIndex);
       }
      
      receiver.chunkMaps.put(targetFilename, peerChunkMap);
     }
   }
 }
