 /**
  * SakuraCmd - Package: net.syamn.sakuracmd.player
  * Created: 2013/01/01 22:27:55
  */
 package net.syamn.sakuracmd.player;
 
 import static net.syamn.sakuracmd.storage.I18n._;
 import net.syamn.sakuracmd.SCHelper;
 import net.syamn.sakuracmd.permission.PermissionManager;
 import net.syamn.sakuracmd.permission.Perms;
 import net.syamn.sakuracmd.storage.ConfigurationManager;
 import net.syamn.sakuracmd.utils.plugin.SakuraCmdUtil;
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
     
     public String getName(boolean hideStatus){
         if (player == null){
             throw new IllegalStateException("Null Player!");
         }
         
         if (config.getUseNamePrefix()){
             final String prefix = getPrefix(hideStatus);
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
     public String getName(){
         return getName(false);
     }
     
     public String getPrefix(boolean hideStatus){
         String prefix = PermissionManager.getPrefix(player);
         if (prefix == null) prefix = "";
         if (hideStatus){
            return prefix;
         }
         
         String status = "";
         
         if (InvisibleWorker.getInstance().isInvisible(player)){
             status += _("invisiblePrefix");
         }
         if (AFKWorker.getInstance().isAfk(player)){
             status += _("afkPrefix");
         }
         
         return Util.coloring(status + prefix);
     }
     public String getPrefix(){
         return getPrefix(false);
     }
     
     public PlayerData getData(){
         return data;
     }
     
     public void initStatus(){
         //this.isAfk = false;
     }
     
     public void restorePowers(){
         removePowerNotPerms(Power.INVISIBLE, Perms.INVISIBLE);
         removePowerNotPerms(Power.FLY, Perms.FLY);
         removePowerNotPerms(Power.GODMODE, Perms.GOD);
         
         // Invisible power
         if (hasPower(Power.INVISIBLE)){
             InvisibleWorker.getInstance().vanish(player, true);
         }
         // Fly power
         if (hasPower(Power.FLY)){
             SakuraCmdUtil.changeFlyMode(player, true);
         }
     }
     
     private void removePowerNotPerms(final Power power, final Perms perms){
         if (getPlayer() != null && hasPower(power) && !perms.has(getPlayer())){
             removePower(power);
         }
     }
     
     /* *** Status getter/setter */
     public boolean isAfk(){
         return AFKWorker.getInstance().isAfk(this.player);
     }
     public boolean isInvisible(){
         return InvisibleWorker.getInstance().isInvisible(this.player);
     }
     
     // infos:
     public void updateLastLocation(){
         if (this.player != null){
             this.data.setLastLocation(this.player.getLocation());
         }
     }
     
     // Powers:
     public boolean hasPower(final Power power){
         return this.data.hasPower(power);
     }
     public void addPower(final Power power){
         this.data.addPower(power);
     }
     public void removePower(final Power power){
         this.data.removePower(power);
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
