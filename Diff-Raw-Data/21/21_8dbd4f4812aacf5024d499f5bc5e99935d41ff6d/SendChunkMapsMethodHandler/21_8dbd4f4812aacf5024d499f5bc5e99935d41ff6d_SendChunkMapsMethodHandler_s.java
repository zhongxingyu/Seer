 /*
  * Created on Nov 12, 2004
  *
  */
 
 import java.util.Vector;
 
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
   
   public void HandleMethod( PeerConnection receiver, final Vector parameters ) throws Exception
   {
   }
 }
