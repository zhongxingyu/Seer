 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.util.Map;
 
 /**
  * User: Ivan
  * Date: 01/02/13
  */
 public class MazewarServerBroadcast extends Thread {
     private static final Logger logger = LoggerFactory.getLogger(MazewarServerBroadcast.class);
 
     @Override
     public void run() {
         try {
             MazewarPacket broadcastPacket;
 
             while (true) {
                 while ((broadcastPacket = MazewarServer.actionQueue.poll()) != null) {
                     for (Map.Entry<String, ObjectOutputStream> entry : MazewarServer.connectedClients.entrySet()) {
                         switch (broadcastPacket.type) {
                             case MazewarPacket.ADD:
                            case MazewarPacket.FIRE:
                             case MazewarPacket.KILLED:
                                 if (broadcastPacket.owner.equals(entry.getKey()))
                                     continue;
                                 break;
                             default:
                                 break;
                         }
                         logger.info("broadcasting to " + entry.getKey() + " " + broadcastPacket.type);
                         entry.getValue().writeObject(broadcastPacket);
                     }
                 }
             }
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 }
