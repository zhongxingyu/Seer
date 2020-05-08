 package game;
 
 import com.jme3.math.Vector3f;
 import rice.p2p.commonapi.NodeHandle;
 import rice.p2p.scribe.ScribeContent;
 
 /**
  * @author Jeff Hoye
  */
 public class ScribeMoveMsg implements ScribeContent {
   /**
    * The source of this content.
    */
   NodeHandle from;
   
   /**
    * The sequence number of the content.
    */
   float x;
   float y;
   float z;
   float rw;
   float rx;
   float ry;
   float rz;
     
   /**
    * Simple constructor.  Typically, you would also like some
    * interesting payload for your application.
    * 
    * @param from Who sent the message.
    * @param seq the sequence number of this content.
    */
   public ScribeMoveMsg(NodeHandle from, float x, float y, float z, float rw, float rx, float ry, float rz) {
     this.from = from;
     this.x=x;
     this.y=y;
     this.z=z;
     this.rx = rx;
     this.ry = ry;
     this.rz = rz;
   }
 
   /**
    * Ye ol' toString() 
    */
   public String toString() {
    return "Msg From:"+ from +"from" + " w loca :"+x+" "+y+" "+z;
   }  
   
   public NodeHandle getSender(){
 	  return from;	  
   }
   
 }
 
