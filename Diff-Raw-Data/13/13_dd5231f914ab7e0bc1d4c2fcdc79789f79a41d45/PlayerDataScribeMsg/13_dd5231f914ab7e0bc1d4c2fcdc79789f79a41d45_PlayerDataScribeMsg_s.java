 package game;
 
 import rice.p2p.commonapi.NodeHandle;
 import rice.p2p.scribe.ScribeContent;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.Vector;
 import java.util.Iterator;
 
 
 public class PlayerDataScribeMsg implements ScribeContent {
   
   //Who sent the message?
   NodeHandle from;
   // Map that holds the player Data Structures
   Map<NodeHandle,PlayerData> nodeToPlayerData;
    
   /**
    * Simple constructor.  Typically, you would also like some
    * interesting payload for your application.
    * 
    * @param from Who sent the message.
    * @param seq the sequence number of this content.
    */
   public PlayerDataScribeMsg(NodeHandle from) {
     nodeToPlayerData = new HashMap();
     this.from = from;    
   }
   
   // This function will handle the input of player data based on our mapping 
   // 1. If not in map, add him, this is the first we know about him
   // 2. otherwise we need to see if this data more recent then what we have
   // We return true if a change occurs, false otherwise
   // This may be used to determine whether we foudn anything new out.
   public boolean updatePlayerData(NodeHandle handle, PlayerData data){    
     if (!nodeToPlayerData.containsKey(handle)){
       nodeToPlayerData.put(handle,data);   
       return true;
     }    
     
     PlayerData ourData = nodeToPlayerData.get(handle);
     if (data.timestamp.after(ourData.timestamp)){
       //The data we receieved is more recent then what we have stored
       nodeToPlayerData.remove(handle);
       nodeToPlayerData.put(handle,data);
       return true;
     }   
     return false;       
   }
   
   public Vector<NodeHandle> dealWithNewPlayerDataSet(PlayerDataScribeMsg set){
     Vector<NodeHandle> retVector = new Vector();
     //Go through each playerData in set
     Iterator it = set.nodeToPlayerData.entrySet().iterator();
     while (it.hasNext()){
       Map.Entry<NodeHandle,PlayerData> pairs = (Map.Entry)it.next();
       NodeHandle nh = pairs.getKey();
       PlayerData data = pairs.getValue();
       //Now we will compare it to our self
       if (this.updatePlayerData(nh, data)){
         //If we are here, that means theres was after ours, so its changed
         retVector.add(nh);
       }
     }    
     return retVector;
   }
   /**
    * Ye ol' toString() 
    */
   public String toString() {
     return "PlayerDataScribe";
   }      
 }
 
