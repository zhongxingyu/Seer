 /**
  * SakuraCmd - Package: net.syamn.sakuracmd.player
  * Created: 2013/01/01 22:27:55
  */
 package net.syamn.sakuracmd.player;
 
 import net.syamn.sakuracmd.ConfigurationManager;
 import net.syamn.sakuracmd.SCHelper;
 import net.syamn.sakuracmd.SakuraCmd;
 import net.syamn.sakuracmd.permission.PermissionManager;
 import net.syamn.sakuracmd.worker.AFKWorker;
 import net.syamn.sakuracmd.worker.InvisibleWorker;
 import net.syamn.utils.Util;
 
 import org.bukkit.entity.Player;
 
 /**
  * SakuraPlayer (SakuraPlayer.java)
  * @author syam(syamn)
  */
 public class SakuraPlayer {
     private final ConfigurationManager config;
     
     private Player player;
     private PlayerData data;
     
     /* *** Status ******* */
     public SakuraPlayer(final Player player){
         this.player = player;
         this.data = new PlayerData(player.getName());
         
         this.config = SCHelper.getInstance().getConfig();
     }
     public Player getPlayer(){
         return this.player;
     }
     public SakuraPlayer setPlayer(final Player player){
         initStatus();
         this.player = player;
         // Validate player instance
         if (!player.getName().equalsIgnoreCase(this.data.getPlayerName())){
             throw new IllegalStateException("Wrong player instance! Player: " + player.getName() + " Data: " + this.data.getPlayerName());
         }
         return this;
     }
     
     public String getName(){
         if (player == null){
             throw new IllegalStateException("Null Player!");
         }
         
         if (config.getUseNamePrefix()){
             final String prefix = getPrefix();
             String suffix = PermissionManager.getSuffix(player);
             suffix = (suffix == null) ? "" : Util.coloring(suffix);
             
             if (config.getUseDisplayname()){
                 return prefix + player.getDisplayName() + suffix;
             }else{
                 return prefix + player.getName() + suffix;
             }
         }else{
             return (config.getUseDisplayname()) ? player.getDisplayName() : player.getName();
         }
     }
     
     public String getPrefix(){
         String prefix = PermissionManager.getPrefix(player);
         if (prefix == null) prefix = "";
         
         String status = "";
         
         if (InvisibleWorker.getInstance().isInvisible(player)){
             status += InvisibleWorker.invPrefix;
         }
         if (AFKWorker.getInstance().isAfk(player)){
             status += AFKWorker.afkPrefix;
         }
         
         return Util.coloring(status + prefix);
     }
     
     public PlayerData getData(){
         return data;
     }
     
     public void initStatus(){
         //this.isAfk = false;
     }
     
     /* *** Status getter/setter */
     public boolean isAfk(){
         return AFKWorker.getInstance().isAfk(this.player);
     }
     public boolean isInvisible(){
         return InvisibleWorker.getInstance().isInvisible(this.player);
     }
     
     // Powers
     public boolean hasPower(final Power power){
         return this.data.hasPower(power);
     }
     public void addPower(final Power power){
         this.data.addPower(power);
     }
     public void removePower(final Power power){
        this.data.addPower(power);
     }
     
     @Override
     public boolean equals(final Object obj){
         if (this == obj){
             return true;
         }
         if (obj == null){
             return false;
         }
         if (!(obj instanceof SakuraPlayer)){
             return false;
         }
         final SakuraPlayer sp = (SakuraPlayer) obj;
         
         if (this.player == null){
             if (sp.player != null){
                 return false;
             }
         }else if(sp == null || !this.player.getName().equals(sp.getName())){
             return false;
         }
         
         return true;
     }
 }
