 package game;
 
 import com.jme3.math.Quaternion;
 import com.jme3.math.Vector3f;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.util.LinkedList;
 import java.util.List;
 import rice.environment.Environment;
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
                 view.updatePlayer(playerUpdates.get(0));
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
 
     // =========================================================================
     // For modifying game state
     // =========================================================================
     
     List<ChestObject> objectUpdates = new LinkedList<ChestObject>();
     
     public void updateObject(ChestObject obj)
     {
         objectUpdates.add(obj);
     }
     
     // =========================================================================
     // For modifying players
     // =========================================================================
     
     List<PlayerData> playerUpdates = new LinkedList<PlayerData>();
     List<NodeHandle> playerRemovals = new LinkedList<NodeHandle>();
     
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
     
     public void removePlayer(NodeHandle id)
     { 
         playerRemovals.add(id);
     }
     
     public void updatePlayer(NodeHandle id, PlayerData data)
     {
         data.id = id;
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
     
     public void receiveChatMessage(String message)
     {
        if(view != null)
        {
            view.receiveChatMessage(message);
        }
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
