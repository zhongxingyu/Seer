 /*
  * Copyright ucchy 2013
  */
 package com.github.ucchyocean.cmt;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.github.ucchyocean.cmt.command.CChatCommand;
 import com.github.ucchyocean.cmt.command.CChatGlobalCommand;
 import com.github.ucchyocean.cmt.command.CClassCommand;
 import com.github.ucchyocean.cmt.command.CCountCommand;
 import com.github.ucchyocean.cmt.command.CExplodeCommand;
 import com.github.ucchyocean.cmt.command.CFriendlyFireCommand;
 import com.github.ucchyocean.cmt.command.CKillCommand;
 import com.github.ucchyocean.cmt.command.CLeaderCommand;
 import com.github.ucchyocean.cmt.command.CRandomCommand;
 import com.github.ucchyocean.cmt.command.CRemoveCommand;
 import com.github.ucchyocean.cmt.command.CSpawnCommand;
 import com.github.ucchyocean.cmt.command.CTPCommand;
 import com.github.ucchyocean.cmt.command.CTeamingCommand;
 import com.github.ucchyocean.cmt.listener.EntityDamageListener;
 import com.github.ucchyocean.cmt.listener.PlayerChatListener;
 import com.github.ucchyocean.cmt.listener.PlayerDeathListener;
 import com.github.ucchyocean.cmt.listener.PlayerQuitListener;
 import com.github.ucchyocean.cmt.listener.PlayerRespawnListener;
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 
 import de.dustplanet.colorme.Actions;
 import de.dustplanet.colorme.ColorMe;
 
 /**
  * @author ucchy
  * ColorMe を使用した、簡易PVPチーミングプラグイン
  */
 public class ColorMeTeaming extends JavaPlugin {
 
     private static final String TEAM_CHAT_FORMAT = "&a[%s&a]<%s&r&a> %s";
     private static final String TEAM_INFORMATION_FORMAT = "&a[%s&a] %s";
 
     protected static ColorMeTeaming instance;
     private static ColorMe colorme;
     public static WorldGuardHandler wghandler;
 
     public static Logger logger;
     public static RespawnConfiguration respawnConfig;
     public static TPPointConfiguration tppointConfig;
 
     public static Hashtable<String, ArrayList<String>> leaders;
     public static Hashtable<String, int[]> killDeathCounts;
     public static Hashtable<String, int[]> killDeathUserCounts;
 
     /**
      * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
      */
     @Override
     public void onEnable() {
 
         instance = this;
         logger = getLogger();
 
         // 設定の読み込み処理
         ColorMeTeamingConfig.reloadConfig();
 
         // 前提プラグイン ColorMe の取得
         Plugin temp = getServer().getPluginManager().getPlugin("ColorMe");
         if ( temp != null && temp instanceof ColorMe ) {
             colorme = (ColorMe)temp;
         } else {
             logger.severe("ColorMe がロードされていません。");
             getServer().getPluginManager().disablePlugin(this);
             return;
         }
 
         // コマンドをサーバーに登録
         CCountCommand ccCommand = new CCountCommand();
         getCommand("colorcount").setExecutor(ccCommand);
         getCommand("colorcountsay").setExecutor(ccCommand);
 
         getCommand("colorfriendlyfire").setExecutor(new CFriendlyFireCommand());
 
         getCommand("colorchat").setExecutor(new CChatCommand());
 
         getCommand("colorglobal").setExecutor(new CChatGlobalCommand());
 
         getCommand("colorleader").setExecutor(new CLeaderCommand());
 
         getCommand("colortp").setExecutor(new CTPCommand());
 
         getCommand("colorclass").setExecutor(new CClassCommand());
 
         getCommand("colorkill").setExecutor(new CKillCommand());
 
         getCommand("colorspawn").setExecutor(new CSpawnCommand());
 
         getCommand("colorrandom").setExecutor(new CRandomCommand());
 
         getCommand("colorremove").setExecutor(new CRemoveCommand());
 
         getCommand("colorexplode").setExecutor(new CExplodeCommand());
 
         getCommand("colorteaming").setExecutor(new CTeamingCommand());
 
         // イベント購読をサーバーに登録
         getServer().getPluginManager().registerEvents(new EntityDamageListener(), this);
 
         getServer().getPluginManager().registerEvents(new PlayerChatListener(), this);
 
         getServer().getPluginManager().registerEvents(new PlayerDeathListener(), this);
 
         getServer().getPluginManager().registerEvents(new PlayerQuitListener(), this);
 
         getServer().getPluginManager().registerEvents(new PlayerRespawnListener(), this);
 
         // 変数の初期化
         killDeathCounts = new Hashtable<String, int[]>();
         killDeathUserCounts = new Hashtable<String, int[]>();
         leaders = new Hashtable<String, ArrayList<String>>();
         respawnConfig = new RespawnConfiguration();
         tppointConfig = new TPPointConfiguration();
     }
 
     /**
      * Player に設定されている、ColorMe の色設定を取得する。
      * @param player プレイヤー
      * @return ColorMeの色
      */
     public static String getPlayerColor(Player player) {
 
         Actions actions = new Actions(colorme);
         return actions.get(player.getName(), "default", "colors");
     }
 
     /**
      * Player に、ColorMe の色を設定する。
      * @param player プレイヤー
      * @param color ColorMeの色
      */
     public static void setPlayerColor(Player player, String color) {
 
         Actions actions = new Actions(colorme);
         actions.set(player.getName(), color, "default", "colors");
         actions.checkNames(player.getName(), "default");
     }
 
     /**
      * Player に設定されている、ColorMe の色設定を削除する。
      * @param player プレイヤー
      */
     public static void removePlayerColor(Player player) {
 
         Actions actions = new Actions(colorme);
         actions.remove(player.getName(), "default", "colors");
         actions.checkNames(player.getName(), "default");
     }
 
     /**
      * ColorMeに設定されている色情報で、ユーザーをグループごとのメンバーに整理して返すメソッド<br>
      * ignoreGroupに設定されている色グループに所属しているプレーヤーは、除外される。
      * @return 色をKey メンバーをValueとした Hashtable
      */
     public static Hashtable<String, ArrayList<Player>> getAllColorMembers() {
 
         Hashtable<String, ArrayList<Player>> result = new Hashtable<String, ArrayList<Player>>();
         Player[] players = Bukkit.getOnlinePlayers();
 
         for ( Player p : players ) {
 
             String color = getPlayerColor(p);
 
             if ( ColorMeTeamingConfig.ignoreGroups.contains(color) ) {
                 continue;
             }
 
             if ( result.containsKey(color) ) {
                 result.get(color).add(p);
             } else {
                 ArrayList<Player> data = new ArrayList<Player>();
                 data.add(p);
                 result.put(color, data);
             }
         }
 
         return result;
     }
 
     /**
      * 全てのプレイヤーを取得する
      * @return 全てのプレイヤー
      */
     public static ArrayList<Player> getAllPlayers() {
         Player[] temp = instance.getServer().getOnlinePlayers();
         ArrayList<Player> result = new ArrayList<Player>();
         for ( Player p : temp ) {
             result.add(p);
         }
         return result;
     }
 
     /**
     * 指定したワールドにいる全てのプレイヤーを取得する。
     * ただし、指定したワールドが存在しない場合は、空のリストが返される。
     * @param worldName ワールド名
     * @return 全てのプレイヤー
     */
    public static ArrayList<Player> getAllPlayersOnWorld(String worldName) {

        Player[] temp = instance.getServer().getOnlinePlayers();
        ArrayList<Player> result = new ArrayList<Player>();
        World world = getWorld(worldName);
        if ( world == null ) {
            return result;
        }
        for ( Player p : temp ) {
            if ( p.getWorld().equals(world) ) {
                result.add(p);
            }
        }
        return result;
    }

    /**
      * メッセージをブロードキャストに送信する。
      * @param message 送信するメッセージ
      */
     public static void sendBroadcast(String message) {
         instance.getServer().broadcastMessage(message);
     }
 
     /**
      * メッセージをチームチャットに送信する。
      * @param player 送信元プレイヤー
      * @param message 送信するメッセージ
      */
     public static void sendTeamChat(Player player, String message) {
 
         String color = getPlayerColor(player);
 
         // メッセージを生成
         String partyMessage = String.format(
                 Utility.replaceColorCode(TEAM_CHAT_FORMAT),
                 Utility.replaceColors(color) + color,
                 player.getDisplayName(),
                 message
                 );
 
         // チームメンバに送信する
         ArrayList<Player> playersToSend = getAllColorMembers().get(color);
         if ( ColorMeTeamingConfig.isOPDisplayMode ) {
             Player[] players = instance.getServer().getOnlinePlayers();
             for ( Player p : players ) {
                 if ( p.isOp() && !playersToSend.contains(p) ) {
                     playersToSend.add(p);
                 }
             }
         }
         for ( Player p : playersToSend ) {
             p.sendMessage(partyMessage);
         }
     }
 
     /**
      * 情報をチームチャットに送信する。
      * @param color 送信先のチーム
      * @param message 送信するメッセージ
      */
     public static void sendTeamChat(String color, String message) {
 
         // メッセージを生成
         String partyMessage = String.format(
                 Utility.replaceColorCode(TEAM_INFORMATION_FORMAT),
                 Utility.replaceColors(color) + color,
                 message
                 );
 
         // チームメンバに送信する
         ArrayList<Player> playersToSend = getAllColorMembers().get(color);
         if ( playersToSend != null ) {
             for ( Player p : playersToSend ) {
                 p.sendMessage(partyMessage);
             }
         }
     }
 
     /**
      * プレイヤー名からPlayerインスタンスを返す。
      * @param name プレイヤー名
      * @return
      */
     public static Player getPlayerExact(String name) {
         return instance.getServer().getPlayerExact(name);
     }
 
     /**
      * ワールド名からWorldインスタンスを返す。
      * @param name ワールド名
      * @return
      */
     public static World getWorld(String name) {
         return instance.getServer().getWorld(name);
     }
 
     /**
      * このプラグインのJarファイル自身を示すFileクラスを返す。
      * @return
      */
     protected static File getPluginJarFile() {
         return instance.getFile();
     }
 
     /**
      * WorldGuardプラグインをロードする
      */
     protected void loadWorldGuard() {
         Plugin temp = getServer().getPluginManager().getPlugin("WorldGuard");
         if ( temp != null && temp instanceof WorldGuardPlugin ) {
             wghandler = new WorldGuardHandler((WorldGuardPlugin)temp);
         } else {
             logger.warning("WorldGuard がロードされていません。");
             logger.warning("protectRespawnPointWithWorldGuard の設定を false に変更します。");
             ColorMeTeamingConfig.setConfigValue(
                     "protectRespawnPointWithWorldGuard", false);
             ColorMeTeamingConfig.protectRespawnPointWithWorldGuard = false;
         }
     }
 }
