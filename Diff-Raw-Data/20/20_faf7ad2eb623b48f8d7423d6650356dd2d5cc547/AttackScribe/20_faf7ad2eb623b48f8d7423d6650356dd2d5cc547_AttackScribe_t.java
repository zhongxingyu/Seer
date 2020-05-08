 package game;
 
 import com.jme3.math.Quaternion;
 import com.jme3.math.Vector3f;
 import java.io.IOException;
 import rice.p2p.commonapi.Endpoint;
 import rice.p2p.commonapi.Id;
 import rice.p2p.commonapi.NodeHandle;
 import rice.p2p.scribe.ScribeContent;
 import rice.p2p.scribe.rawserialization.RawScribeContent;
 import rice.p2p.commonapi.rawserialization.*;
 
 
 public class AttackScribe implements RawScribeContent
 {
   protected static final short TYPE = 987;
   
   public Id from;
   public PlayerData player;
   
   
   public AttackScribe(Id from, PlayerData pd)
   {
     this.from = from;
     this.player = pd;
   }
 
   
   
   public AttackScribe(InputBuffer buf, Endpoint endpoint) throws IOException
   {
     from = endpoint.readId(buf, endpoint.getId().getType());
     float x = buf.readFloat();
     float y = buf.readFloat();
     float z = buf.readFloat();
     float rw = buf.readFloat();
     float rx = buf.readFloat();
     float ry = buf.readFloat();
     float rz = buf.readFloat();
    String name = buf.readUTF();    
     player = new PlayerData(from, new Vector3f(x,y,z), new Quaternion(rx,ry,rz,rw),name);
    player.health=buf.readInt();
    player.lastDamageTaken=buf.readInt();    
   }
   
   public String toString()
   {
     return "Attack Scribe!!!";
   }
 
   public short getType()
   {
     return TYPE;
   }
 
   public void serialize(OutputBuffer buf) throws IOException
   {
     from.serialize(buf);
     buf.writeFloat(player.position.x);
     buf.writeFloat(player.position.y);
     buf.writeFloat(player.position.z);
     buf.writeFloat(player.rotation.getW());
     buf.writeFloat(player.rotation.getX());
     buf.writeFloat(player.rotation.getY());
     buf.writeFloat(player.rotation.getZ());
     buf.writeUTF(player.name);
    buf.writeInt(player.health);
    buf.writeInt(player.lastDamageTaken);    
   }
 }
 
