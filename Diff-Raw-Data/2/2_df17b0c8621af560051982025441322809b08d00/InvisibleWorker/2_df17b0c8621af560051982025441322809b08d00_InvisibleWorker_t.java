 /**
  * SakuraCmd - Package: net.syamn.sakuracmd.worker
  * Created: 2013/01/04 12:31:55
  */
 package net.syamn.sakuracmd.worker;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 
 import net.syamn.sakuracmd.permission.Perms;
 import net.syamn.sakuracmd.player.PlayerManager;
 import net.syamn.sakuracmd.player.Power;
 import net.syamn.sakuracmd.utils.plugin.DynmapHandler;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 
 /**
  * InvisibleWorker (InvisibleWorker.java)
  * @author syam(syamn)
  */
 public class InvisibleWorker {
     private static InvisibleWorker instance = null;
     //private final ConcurrentHashMap<Player, Object> invisiblePlayers = new ConcurrentHashMap<Player, Object>();
     private final Set<Player> invisiblePlayers = Collections.newSetFromMap(new ConcurrentHashMap<Player, Boolean>());
     
     public static InvisibleWorker getInstance(){
         return instance;
     }
     public static void dispose(){
         instance = null;
     }
     public static void createInstance(){
         instance = new InvisibleWorker();
     }
     
     public Collection<Player> getAllInvisiblePlayers(){
         return Collections.unmodifiableCollection(invisiblePlayers);
     }
     
     public void onPlayerQuit(final Player player){
         invisiblePlayers.remove(player);
     }
     
     public void vanish(final Player player, final boolean onJoin){
         if (invisiblePlayers.contains(player)){
             return;
         }
         
         PlayerManager.getPlayer(player).addPower(Power.INVISIBLE);
         invisiblePlayers.add(player);
         DynmapHandler.getInstance().setPlayerVisiblity(player, false);
         
         for (final Player p : Bukkit.getOnlinePlayers()){
             invisible(player, p);
         }
         
         if (!onJoin){
             //TODO send join message if in fake quit mode
         }
     }
     public void sendInvisibleOnJoin(final Player joined){
         for (final Player inv : invisiblePlayers){
             invisible(inv, joined);
         }
     }
     public void reappear(final Player player){
         PlayerManager.getPlayer(player).removePower(Power.INVISIBLE);
         invisiblePlayers.remove(player);
         DynmapHandler.getInstance().setPlayerVisiblity(player, true);
         
         for (final Player p : Bukkit.getOnlinePlayers()){
             uninvisible(player, p);
         }
         
         //TODO send join message if in fake quit mode
     }
     
     private void invisible(final Player player, final Player from){
         if (Perms.INV_CANSEE.has(from)){
             return;
         }
         from.hidePlayer(player);
     }
     private void uninvisible(final Player player, final Player from){
        if (Perms.INV_CANSEE.has(from)){
             return;
         }
         from.showPlayer(player);
     }
     
     public boolean isInvisible(final Player player){
         if (player == null) return false;
         return invisiblePlayers.contains(player);
     }
 }
