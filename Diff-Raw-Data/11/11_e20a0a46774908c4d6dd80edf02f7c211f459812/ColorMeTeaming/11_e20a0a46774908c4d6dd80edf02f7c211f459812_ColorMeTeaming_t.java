 /*
  * Copyright ucchy 2013
  */
 package com.github.ucchyocean.cmt;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Vector;
 import java.util.logging.Logger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.World;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.github.ucchyocean.cmt.command.CChatCommand;
 import com.github.ucchyocean.cmt.command.CChatGlobalCommand;
 import com.github.ucchyocean.cmt.command.CClassCommand;
 import com.github.ucchyocean.cmt.command.CCountCommand;
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
 
 import de.dustplanet.colorme.Actions;
 import de.dustplanet.colorme.ColorMe;
 
 /**
  * @author ucchy
  * ColorMe を使用した、簡易PVPチーミングプラグイン
  */
 public class ColorMeTeaming extends JavaPlugin {
 
    private static final String TEAM_CHAT_FORMAT = "&a[%s&a]<%s&r&a> %s";
    private static final String TEAM_INFORMATION_FORMAT = "&a[%s&a] %s";
 
     private static ColorMeTeaming instance;
     private static ColorMe colorme;
 
     private static Logger logger;
 
     public static List<String> ignoreGroups;
     public static boolean isTeamChatMode;
     public static boolean isOPDisplayMode;
     public static boolean isFriendlyFireDisabler;
     public static Map<String, String> classItems;
     public static Map<String, String> classArmors;
     public static boolean autoColorRemove;
 
     public static int killPoint;
     public static int deathPoint;
     public static int tkPoint;
 
     public static Hashtable<String, Vector<Player>> leaders;
     public static Hashtable<String, int[]> killDeathCounts;
 
     /**
      * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
      */
     @Override
     public void onEnable() {
 
         instance = this;
         logger = getLogger();
 
         // 設定の読み込み処理
         try {
             reloadConfigFileInternal();
         } catch (IOException e) {
             e.printStackTrace();
             logger.severe("設定ファイルの読み込みに失敗しました。");
             getServer().getPluginManager().disablePlugin(this);
             return;
         }
 
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
 
         getCommand("colorteaming").setExecutor(new CTeamingCommand());
 
         // イベント購読をサーバーに登録
         getServer().getPluginManager().registerEvents(new EntityDamageListener(), this);
 
         getServer().getPluginManager().registerEvents(new PlayerChatListener(), this);
 
         getServer().getPluginManager().registerEvents(new PlayerDeathListener(), this);
 
         // 変数の初期化
         killDeathCounts = new Hashtable<String, int[]>();
         leaders = new Hashtable<String, Vector<Player>>();
     }
 
     /**
      * config.ymlの読み出し処理。
      * @throws IOException
      */
     private void reloadConfigFileInternal() throws IOException {
 
         File configFile = new File(getDataFolder(), "config.yml");
         if ( !configFile.exists() ) {
             Utility.copyFileFromJar(getFile(), configFile, "config_ja.yml", false);
         }
 
         reloadConfig();
         FileConfiguration config = getConfig();
 
         isTeamChatMode = config.getBoolean("teamChatMode", false);
         isOPDisplayMode = config.getBoolean("opDisplayMode", false);
 
         isFriendlyFireDisabler = config.getBoolean("firelyFireDisabler", true);
 
         ignoreGroups = config.getStringList("ignoreGroups");
         if ( ignoreGroups == null ) {
             ignoreGroups = new ArrayList<String>();
         }
 
         classItems = new HashMap<String, String>();
         classArmors = new HashMap<String, String>();
         ConfigurationSection section = config.getConfigurationSection("classes");
         if ( section != null ) {
             Iterator<String> i = section.getValues(false).keySet().iterator();
             while (i.hasNext()) {
                 String clas = i.next();
                 classItems.put(clas, config.getString("classes." + clas + ".items", "") );
                 if ( config.contains("classes." + clas + ".armor") ) {
                     classArmors.put(clas, config.getString("classes." + clas + ".armor") );
                 }
             }
         }
 
         killPoint = config.getInt("points.killPoint", 1);
         deathPoint = config.getInt("points.deathPoint", -1);
         tkPoint = config.getInt("points.tkPoint", -3);
 
         autoColorRemove = config.getBoolean("autoColorRemove", true);
     }
 
     /**
      * config.yml の再読み込みを行う。
      */
     public static void reloadConfigFile() {
 
         try {
             instance.reloadConfigFileInternal();
         } catch (IOException e) {
             e.printStackTrace();
         }
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
     public static Hashtable<String, Vector<Player>> getAllColorMembers() {
 
         Hashtable<String, Vector<Player>> result = new Hashtable<String, Vector<Player>>();
         Player[] players = Bukkit.getOnlinePlayers();
 
         for ( Player p : players ) {
 
             String color = getPlayerColor(p);
 
             if ( ignoreGroups.contains(color) ) {
                 continue;
             }
 
             if ( result.containsKey(color) ) {
                 result.get(color).add(p);
             } else {
                 Vector<Player> data = new Vector<Player>();
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
     public static Vector<Player> getAllPlayers() {
         Player[] temp = instance.getServer().getOnlinePlayers();
         Vector<Player> result = new Vector<Player>();
         for ( Player p : temp ) {
             result.add(p);
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
                 replaceThings(color) + color,
                 player.getDisplayName(),
                 message
                 );
 
         // チームメンバに送信する
         Vector<Player> playersToSend = getAllColorMembers().get(color);
         if ( isOPDisplayMode ) {
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
                 replaceThings(color) + color,
                 message
                 );
 
         // チームメンバに送信する
         Vector<Player> playersToSend = getAllColorMembers().get(color);
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
      * config.yml に、設定値を保存する
      * @param key 設定値のキー
      * @param value 設定値の値
      */
     public static void setConfigValue(String key, Object value) {
 
         FileConfiguration config = instance.getConfig();
         config.set(key, value);
         instance.saveConfig();
     }
 
     /**
      * ColorMeの色設定を、ChatColorクラスに変換する
      * @param color ColorMeの色設定
      * @return ChatColorクラス
      */
     public static ChatColor replaceThings(String color) {
 
         if (color.equalsIgnoreCase("red")) {
             return ChatColor.RED;
         } else if (color.equalsIgnoreCase("blue")) {
             return ChatColor.BLUE;
         } else if (color.equalsIgnoreCase("yellow")) {
             return ChatColor.YELLOW;
         } else if (color.equalsIgnoreCase("green")) {
             return ChatColor.GREEN;
         } else if (color.equalsIgnoreCase("aqua")) {
             return ChatColor.AQUA;
         } else if (color.equalsIgnoreCase("gray")) {
             return ChatColor.GRAY;
         } else if (color.equalsIgnoreCase("dark_red")) {
             return ChatColor.DARK_RED;
         } else if (color.equalsIgnoreCase("dark_green")) {
             return ChatColor.DARK_GREEN;
         } else if (color.equalsIgnoreCase("dark_aqua")) {
             return ChatColor.DARK_AQUA;
         } else if (color.equalsIgnoreCase("black")) {
             return ChatColor.BLACK;
         } else if (color.equalsIgnoreCase("dark_blue")) {
             return ChatColor.DARK_BLUE;
         } else if (color.equalsIgnoreCase("dark_gray")) {
             return ChatColor.DARK_GRAY;
         } else if (color.equalsIgnoreCase("dark_purple")) {
             return ChatColor.DARK_PURPLE;
         } else if (color.equalsIgnoreCase("gold")) {
             return ChatColor.GOLD;
         } else if (color.equalsIgnoreCase("light_purple")) {
             return ChatColor.LIGHT_PURPLE;
         } else {
             return ChatColor.WHITE;
         }
     }
 }
