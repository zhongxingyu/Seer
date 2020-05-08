 /**
  * SakuraCmd - Package: net.syamn.sakuracmd
  * Created: 2012/12/28 13:39:58
  */
 package net.syamn.sakuracmd.permission;
 
 import net.syamn.utils.Util;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.bukkit.permissions.Permissible;
 
 /**
  * Perms (Perms.java)
  * @author syam(syamn)
  */
 public enum Perms {
     /* 権限ノード */
     // Item Commands
     REPAIRITEM("item.repairitem"),
     REPAIRALL("item.repairall"),
 
     // Tp Commands
     TP ("tp.tp"),
     TPHERE ("tp.here"),
     TPLOC ("tp.tploc"),
     BACK ("tp.back"),
     RIDE ("tp.ride"),
 
     // Server Commands
     LOCKDOWN ("server.lockdown"),
     LOCKDOWN_BYPASS ("server.lockdown.bypass"),
 
     // Player Commands
     AFK ("player.afk"),
     INVISIBLE ("player.invisible"),
     GOD ("player.god"),
     WHOIS ("player.whois"),
     GAMEMODE ("player.gamemode"),
     GAMEMODE_OTHER ("player.gamemode.other"),
     FLY ("player.fly"),
     FLY_OTHER ("player.fly.other"),
     FLYMODE ("player.flymode"),
     OPENINV ("player.openinv"),
     OPENENDER ("player.openender"),
     SPECCHEST ("player.specchest"),
     SPECCHEST_OTHER ("player.specchest.other"),
     NO_PICKUP ("player.nopickup"),
     NO_PICKUP_OTHER ("player.nopickup.other"),
     HARD_END ("player.hardend"),
     HARD_END_NOSIGN ("player.hardend.nosign"),
     HARD_END_ADMIN ("player.hardend.admin"),
 
     // World Commands
     WEATHER ("world.weather"),
     ENTITY ("world.entity"),
 
     // Database Commands
     REGISTER ("db.register"),
     PASSWORD ("db.password"),
     MAIL ("db.mail"),
 
     // Other / Admin Commands
     RATIO ("admin.ratio"),
     END_RESET ("admin.endreset"),
     ADMIN ("admin.admin"),
     SAKURACMD ("admin.sakuracmd"),
     LIFT_RELOAD ("admin.liftreload"),
 
     // Spec Permissions
     INV_CANSEE ("spec.cansee"),
     HIDE_GEOIP ("spec.hidegeoip"),
     TRUST ("spec.trust"),
     LOG ("spec.log"),
     LOG_HIDE ("spec.log.hide"),
     PLACE_NETHER_TOP ("spec.place.nethertop"),
 
     // Feature
     RIDE_PLAYER ("feature.ride"),
     RIDE_ALLENTITY ("feature.ride.all"),
     ICE_TO_WATER ("feature.icetowater"),
 
     // Bypass permissions
    BYPASS_CREATIVE_ITEM ("bypass.creative.item"),
     BYPASS_CREATIVE_TNT ("bypass.creative.tnt"),
 
     // Sign create, use, break parent perms
     SIGN_CREATE_PARENT ("sign.create"),
     SIGN_USE_PARENT ("sign.use"),
     SIGN_BREAK_PARENT ("sign.break"),
 
     // SpecialItems perimssion
     SPECITEM_USE_PARENT ("specitem.use"),
 
     // Tab color
     TAB_RED ("tab.red"),
     TAB_PURPLE ("tab.purple"),
     TAB_AQUA ("tab.aqua"),
     TAB_NONE ("tab.none"),
     TAB_GRAY ("tab.gray"),
 
     // Rei's Minimap
     REIS_DEFAULT ("reisminimap.default"),
     ;
 
     // ノードヘッダー
     final String HEADER = "sakuracmd.";
     private String node;
 
     /**
      * コンストラクタ
      *
      * @param node
      *            権限ノード
      */
     Perms(final String node) {
         this.node = HEADER + node;
     }
 
     /**
      * 指定したプレイヤーが権限を持っているか
      *
      * @param perm Permissible. Player, CommandSender etc
      * @return boolean
      */
     public boolean has(final Permissible perm) {
         if (perm == null) {
             return false;
         }
         return PermissionManager.hasPerm(perm, this);
     }
 
     /**
      * 指定したプレイヤーが権限を持っているか
      *
      * @param perm Permissible. Player, CommandSender etc
      * @param subPerm subPermission without head period
      * @return boolean
      */
     public boolean has(final Permissible perm, final String subPerm) {
         if (perm == null) {
             return false;
         }
         return PermissionManager.hasPerm(perm, this.getNode().concat("." + subPerm));
     }
 
     /**
      * Send message to players has this permission.
      * @param message send message.
      */
     public void message(final String message){
         for (final Player player : Bukkit.getServer().getOnlinePlayers()){
             if (this.has(player)){
                 Util.message(player, message);
             }
         }
     }
 
     public String getNode(){
         return this.node;
     }
 }
