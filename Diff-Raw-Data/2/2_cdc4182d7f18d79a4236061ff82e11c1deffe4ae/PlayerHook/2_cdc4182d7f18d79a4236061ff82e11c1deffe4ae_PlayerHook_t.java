 /**
  * This class is used to hook the player's NetworkManager, to be specific, it's used to hook the
  * "inboundQueue" field in there. Why? So we can intercept incoming packet without making use of an
  * external library like protocolLib or BKCommonLib. If you do not understand any of this code
  * then simply go to this youtube video: http://www.youtube.com/watch?v=ktvTqknDobU&list=RD02cmSbXsFE3l8
  * After watching it everything will be clear. *No*
  */
 
 package common.captainbern.npclib.internal;
 
 import common.captainbern.npclib.NPCLib;
 import common.captainbern.npclib.wrapper.WrappedQueue;
 import common.captainbern.reflection.ReflectionUtil;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 import java.lang.reflect.Field;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Queue;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 public class PlayerHook {
 
     private Queue inboundQueue = new ConcurrentLinkedQueue();
     private final Map<Class<?>, Field> fieldMap = new HashMap<Class<?>, Field>();
 
     /**
      * While this code needs a lot of optimization though I got it to work (hooray for me! :D )
      * It basically replaces the inboundQueue field in the networkmanager of {@param player} with my
      * WrapperQueue, this wrapperqueue extends the ConcurrentLinkedQueue and just overrides the "add(E)" method
      * so we can start capturing incoming packets. It's a nifty little system but it would never have been possible
     * without the help of Comphenix aka Kristian aka aadnk, he, without himself realizing it, teached me a lot
      * about packets and java. Thank you for all the help Comphenix!
      */
     public void hookPlayer(Player player, boolean joining){
         Object nm = getNetworkManager(player);
         Class<?> clazz = nm.getClass();
 
         try {
 
             Field f = clazz.getDeclaredField("inboundQueue");
             f.setAccessible(true);
 
             ConcurrentLinkedQueue oldQueue  = (ConcurrentLinkedQueue) f.get(nm);
             WrappedQueue newQueue = null;
 
             if(joining && !(oldQueue instanceof  WrappedQueue)){
                 newQueue = new WrappedQueue(player);
             }
             if(!joining && (oldQueue instanceof  WrappedQueue)){
                 newQueue = (WrappedQueue) oldQueue;
             }
             if(newQueue != null){
                 Field lock = clazz.getDeclaredField("h");
                 lock.setAccessible(true);
                 lock.get(nm);
                 synchronized (lock){
                     newQueue.addAll(oldQueue);
                     oldQueue.clear();
                 }
 
                 f.set(nm, newQueue);
                 NPCLib.instance.log(ChatColor.GREEN + "Successfully hooked {" + player.getName() + "} NetworkManager. Let the magic happen now.");
             }else{
                 NPCLib.instance.log(ChatColor.RED + "Could not hook player {" + player.getName() + "}");
             }
 
         } catch (Exception e) {
             NPCLib.instance.log(ChatColor.RED + "A problem was encountered while trying to hook into player: {" + player.getName() + "}");
             e.printStackTrace();
         }
     }
 
     /**
      * Returns the NetworkManager of a player
      */
     private Object getNetworkManager(Player player){
         try{
             Object playerConnection = getPlayerConnection(player);
             Object networkmanager = playerConnection.getClass().getField("networkManager").get(playerConnection);
             return networkmanager;
         }catch(Exception e){
             NPCLib.instance.log(ChatColor.RED + "Could not retrieve NetworkManager of player => " + player.getName());
             e.printStackTrace();
             return null;
         }
     }
 
     /**
      * Returns the PlayerConnection of a player
      */
     private Object getPlayerConnection(Player player){
         try{
             Object nms = playerToNMS(player);
             Object playerConnection = nms.getClass().getField("playerConnection").get(nms);
             return playerConnection;
         }catch(Exception e){
             NPCLib.instance.log(ChatColor.RED + "Could not retrieve player: " + player.getName() + "'s playerConnection!");
             return null;
         }
     }
 
     /**
      * Used to convert a bukkit player to EntityPlayer
      */
     private Object playerToNMS(Player player){
         Object entityPlayer = null;
         try{
             entityPlayer = ReflectionUtil.getMethod("getHandle", player.getClass(), 0).invoke(player);
         }catch(Exception e){
             NPCLib.instance.log(ChatColor.RED + "Could not convert player: " + player.getName() + " from bukkit to NMS Entity!");
             return null;
         }
         return entityPlayer;
     }
 }
