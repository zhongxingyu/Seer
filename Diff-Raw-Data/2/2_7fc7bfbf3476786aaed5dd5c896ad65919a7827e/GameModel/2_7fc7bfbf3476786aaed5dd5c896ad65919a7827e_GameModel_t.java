 package game;
 
 import com.jme3.math.Quaternion;
 import com.jme3.math.Vector3f;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import rice.environment.Environment;
 import rice.p2p.commonapi.Id;
 import rice.p2p.commonapi.NodeHandle;
 
 /**
  *
  * @author JP
  */
 public class GameModel
 {
     public Vector3f position;
     public Quaternion rotation;
     public String name;
     
     public boolean positionChanged = false;
     public boolean regionChanged = false;
     Region region;
     
     public boolean clearViewOnRegionChange = true;
     
     boolean connected = false;
     GameNode node;  
     GameClient view;
     
     boolean pokeObject = false;
     
     public GameModel(GameClient view)
     {
         this.view = view;
         region = new Region(-1,-1);
     }
     
     public void connect(String targetIP, int targetPort, int thisPort)
     {
         try
         {
             // Loads pastry configurations
             Environment env = new Environment();
 
             // disable the UPnP setting (in case you are testing this on a NATted LAN)
             env.getParameters().setString("nat_search_policy","never");
             env.getParameters().setString("pastry_socket_writer_max_queue_length","60");
             
             // build the bootaddress from the command line args
             InetAddress bootaddr = InetAddress.getByName(targetIP);
             InetSocketAddress bootaddress = new InetSocketAddress(bootaddr, targetPort);
 
             // launch our node!
             node = new GameNode(thisPort, bootaddress, env, this);
             connected = true;
         }
         catch(Exception e)
         {
            connected = false;
            System.out.println(e.getMessage());
            System.out.println("Shit didn't work, did you pass the right args?");
         }
     }
     
     public boolean isConnected()
     {
         return connected;
     }
     
     public void shutdown()
     {
         // TODO: destroy GameNode here
     }
     
     public void update()       
     {
         // poke the closest object, if there is one
         if(pokeObject)
         {
             System.out.println("POKE A CHEST");
             pokeObject = false;
             for(Iterator it = view.objectsActual.entrySet().iterator(); it.hasNext();)
             {
                 Map.Entry pair = (Map.Entry)it.next();
                 ChestObject item = (ChestObject)pair.getValue();
                 
                ChestObject updated = new ChestObject(item.id, item.x, item.y, item.message);
                 updated.state = (item.state + 1) % 2;
                 node.app.updateChest(updated);
 
                 System.out.println("POKING " + item.id);
             }
         }
         
         // apply object updates
         while(objectUpdates.size() > 0)
         {
             if(view != null)
             {
                 view.updateObject(objectUpdates.get(0));
             }
             objectUpdates.remove(0);
         }
         
         // aply player updates
         while(playerUpdates.size() > 0)
         {
             if(view != null)
             {
                 try
                 {
                     view.updatePlayer(playerUpdates.get(0));
                 }
                 catch(Exception e)
                 {}
             }
             playerUpdates.remove(0);
         }
         
         // remove stale players
         while(playerRemovals.size() > 0)
         {
             if(view != null)
             {
                 view.removePlayer(playerRemovals.get(0));
             }
             playerRemovals.remove(0);
         }
         
         // add new chat messages
         while(newMessages.size() > 0)
         {
             if(view != null)
             {
                 view.receiveChatMessage(newMessages.get(0));
             }
             newMessages.remove(0);
         }
     }
     
     public void checkRegionChange()
     {
         Region newRegion = GameModel.getPlayersRegion(getPlayerPosition());
         if (newRegion.x != region.x || newRegion.y != region.y)
         {          
             regionChanged = true;
             view.clearDirtyRegions();
         }
     }
     
     public int getRegionRange()
     {
         if(node.app instanceof GameAppOneRegion)
             return 0;
         else
             return 1;
     }
 
     // =========================================================================
     // For modifying game state
     // =========================================================================
     
     List<ChestObject> objectUpdates = new LinkedList<ChestObject>();
     
     public void updateObject(ChestObject obj)
     {
         objectUpdates.add(obj);
     }
     
     public void pokeNearestObject()
     {
         pokeObject = true;
     }
     
     // =========================================================================
     // For modifying players
     // =========================================================================
     
     List<PlayerData> playerUpdates = new LinkedList<PlayerData>();
     List<Id> playerRemovals = new LinkedList<Id>();
     
     public Vector3f getPlayerPosition()
     {
         if(view != null)
         {
             position = view.getPlayerPosition();
         }
         return position;
     }
 
     public Quaternion getPlayerRotation()
     {
         if(view != null)
         {
             rotation = view.getPlayerRotation();
         }        
         return rotation;
     }
     
     public void removePlayer(Id id)
     { 
         playerRemovals.add(id);
     }
     
     public void updatePlayer(PlayerData data)
     {
         playerUpdates.add(data);
     }
     
     public String getPlayerName()
     {
         if(view != null)
         {
             name = view.getPlayerName();
         }
         return name;
     }
     
     // =========================================================================
     // For sending and receiving chat messages
     // =========================================================================
     
     List<String> newMessages = new LinkedList<String>();
     
     public void receiveChatMessage(String message)
     {
         newMessages.add(message);
     }
     
     public void sendChatMessage(String message)
     {
         if(node != null)
         {
             node.app.sendChatMessage(message);
         }
     }
     
     // =========================================================================
     // Helper functions
     // =========================================================================
     
     public static Region getPlayersRegion(Vector3f pos)
     {
         int xPos = (int)pos.x + 517;
         int yPos = (int)pos.z + 517;
         return new Region(xPos / 100,yPos / 100);
     }
 }
