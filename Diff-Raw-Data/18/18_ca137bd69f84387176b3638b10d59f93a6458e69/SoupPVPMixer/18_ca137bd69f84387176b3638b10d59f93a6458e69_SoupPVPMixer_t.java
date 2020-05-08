 /*
  * @author     ucchy
  * @license    GPLv3
  * @copyright  Copyright ucchy 2013
  */
 package com.github.ucchyocean.spm;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.Random;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.GameMode;
 import org.bukkit.Location;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.github.ucchyocean.bp.BPData;
 import com.github.ucchyocean.bp.BPUserData;
 import com.github.ucchyocean.bp.BattlePoints;
 
 /**
  * Soup PVP Mixer
  * @author ucchy
  */
 public class SoupPVPMixer extends JavaPlugin {
 
     private static String prefix;
 
     private static final String[] COMMANDS = {
         "kit", "clear", "teleport", "return", "match",
     };
 
     protected static SoupPVPMixer instance;
     protected static SoupPVPMixerConfig config;
     private BattlePoints bp;
 
     private Player spectator;
     private ArrayList<MatchingData> matching;
 
     /**
      * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
      */
     @Override
     public void onEnable() {
 
         instance = this;
 
         // コンフィグをロードする
         config = new SoupPVPMixerConfig();
 
         // メッセージの初期化
         Messages.initialize();
         prefix = Messages.get("prefix");
 
         // リスナーを設定する
         getServer().getPluginManager().registerEvents(new PlayerListener(), this);
 
         // BattlePoints を取得する
         bp = (BattlePoints)getServer().getPluginManager().getPlugin("BattlePoints");
     }
 
     /**
      * プラグインのデータフォルダを返す
      * @return プラグインのデータフォルダ
      */
     protected static File getConfigFolder() {
         return instance.getDataFolder();
     }
 
     /**
      * プラグインのJarファイル自体を示すFileオブジェクトを返す
      * @return プラグインのJarファイル
      */
     protected static File getPluginJarFile() {
         return instance.getFile();
     }
 
     /**
      * @see org.bukkit.plugin.java.JavaPlugin#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
      */
     @Override
     public boolean onCommand(
             CommandSender sender, Command command, String label, String[] args) {
 
 
         if ( args.length <= 0 ) {
             return false;
         }
 
         if ( !isValidCommand(args[0]) ) {
             return false;
         }
 
         if ( !sender.hasPermission("souppvpmixer." + args[0]) ) {
             sender.sendMessage(ChatColor.RED + "You don't have permission \"souppvpmixer." + args[0] + "\".");
             return true;
         }
 
         if ( args[0].equalsIgnoreCase("kit") ) {
             return doKit(sender, command, label, args);
         } else if ( args[0].equalsIgnoreCase("clear") ) {
             return doClear(sender, command, label, args);
         } else if ( args[0].equalsIgnoreCase("teleport") ) {
             return doTeleport(sender, command, label, args);
         } else if ( args[0].equalsIgnoreCase("return") ) {
             return doReturn(sender, command, label, args);
         } else if ( args[0].equalsIgnoreCase("match") ) {
             return doMatch(sender, command, label, args);
         }
 
         return false;
     }
 
     /**
      * キットコマンドの実行
      * @param sender
      * @param command
      * @param label
      * @param args
      * @return
      */
     private boolean doKit(CommandSender sender, Command command, String label, String[] args) {
 
         // プレイヤーを取得する
         ArrayList<Player> players = getPlayersWithoutCreative();
 
         if ( players.size() <= 0 ) {
             sender.sendMessage("対象のプレイヤーが誰も居ません。");
             return true;
         }
 
         for ( Player p : players ) {
 
             if ( p.equals(spectator) ) {
                 continue;
             }
 
             // 全回復、全アイテムクリア
             clearInvAndHeal(p);
 
             // アイテム配布
             for ( ItemStack i : config.kitItems ) {
                 p.getInventory().addItem(i);
             }
 
             // 防具配布
             if ( config.kitArmor != null ) {
 
                 if (config.kitArmor.size() >= 1 && config.kitArmor.get(0) != null ) {
                     p.getInventory().setHelmet(config.kitArmor.get(0));
                 }
                 if (config.kitArmor.size() >= 2 && config.kitArmor.get(1) != null ) {
                     p.getInventory().setChestplate(config.kitArmor.get(1));
                 }
                 if (config.kitArmor.size() >= 3 && config.kitArmor.get(2) != null ) {
                     p.getInventory().setLeggings(config.kitArmor.get(2));
                 }
                 if (config.kitArmor.size() >= 4 && config.kitArmor.get(3) != null ) {
                     p.getInventory().setBoots(config.kitArmor.get(3));
                 }
             }
 
         }
 
         return true;
     }
 
     /**
      * クリアコマンドの実行
      * @param sender
      * @param command
      * @param label
      * @param args
      * @return
      */
     private boolean doClear(CommandSender sender, Command command, String label, String[] args) {
 
         // プレイヤーを取得する
         ArrayList<Player> players = getPlayersWithoutCreative();
 
         if ( players.size() <= 0 ) {
             sender.sendMessage("対象のプレイヤーが誰も居ません。");
             return true;
         }
 
         for ( Player p : players ) {
             // 全回復、全アイテムクリア
             clearInvAndHeal(p);
         }
 
         return true;
     }
 
     /**
      * テレポートコマンドの実行
      * @param sender
      * @param command
      * @param label
      * @param args
      * @return
      */
     private boolean doTeleport(CommandSender sender, Command command, String label, String[] args) {
 
         if ( matching == null ) {
             sender.sendMessage("マッチングデータがまだありません。");
             return true;
         }
 
         for ( int i=0; i<matching.size(); i++ ) {
 
             MatchingData data = matching.get(i);
             String key = String.format("player%d-%d", (i+1), 1);
             Player player1 = Bukkit.getPlayerExact(data.getPlayer1().name);
             Location location1 = config.teleport.get(key);
 
             key = String.format("player%d-%d", (i+1), 2);
             Player player2 = Bukkit.getPlayerExact(data.getPlayer2().name);
             Location location2 = config.teleport.get(key);
 
            if ( location1 != null && location2 != null ) {
                location1 = setDirection(location1, location2);
                location2 = setDirection(location2, location1);
            }

             if ( player1 != null && location1 != null ) {
                 player1.teleport(location1.add(0.5, 0, 0.5), TeleportCause.PLUGIN);
             }
             if ( player2 != null && location2 != null ) {
                 player2.teleport(location2.add(0.5, 0, 0.5), TeleportCause.PLUGIN);
             }
         }
 
         Location spectatorLocation = config.teleport.get("spectator");
         if ( spectator != null && spectatorLocation != null ) {
             spectator.teleport(spectatorLocation.add(0.5, 0, 0.5), TeleportCause.PLUGIN);
         }
 
         return true;
     }
 
     /**
      * リターンコマンドの実行
      * @param sender
      * @param command
      * @param label
      * @param args
      * @return
      */
     private boolean doReturn(CommandSender sender, Command command, String label, String[] args) {
 
         ArrayList<Player> players = getPlayersWithoutCreative();
 
         if ( players.size() <= 0 ) {
             sender.sendMessage("対象のプレイヤーが誰も居ません。");
             return true;
         }
 
         Location spectatorLocation = config.teleport.get("spectator");
         if ( spectatorLocation != null ) {
             for ( Player p : players ) {
                 p.teleport(spectatorLocation, TeleportCause.PLUGIN);
             }
         }
         return true;
     }
 
     /**
      * マッチコマンドの実行
      * @param sender
      * @param command
      * @param label
      * @param args
      * @return
      */
     private boolean doMatch(CommandSender sender, Command command, String label, String[] args) {
 
         // プレイヤーを取得する
         ArrayList<Player> players = getPlayersWithoutCreative();
         spectator = null;
 
         if ( players.size() <= 0 ) {
             sender.sendMessage("対象のプレイヤーが誰も居ません。");
             return true;
         }
 
         Random random = new Random(System.currentTimeMillis());
 
         // 人数が奇数なら、1人ランダムで休ませる
         if ( players.size() % 2 == 1 ) {
             int index = random.nextInt(players.size());
             spectator = players.get(index);
             players.remove(index);
         }
 
         BPData data = bp.getBPData();
         HashMap<String, BPUserData> userdata = new HashMap<String, BPUserData>();
         ArrayList<BPUserData> userRandomData = new ArrayList<BPUserData>();
 
         for ( Player p : players ) {
             int point = data.getPoint(p.getName());
             int randomPoint = point + random.nextInt(config.matchingRandomRange);
             userdata.put(p.getName(), new BPUserData(p.getName(), point));
             userRandomData.add(new BPUserData(p.getName(), randomPoint));
         }
 
         // ソート
         Collections.sort(userRandomData, new Comparator<BPUserData>(){
             public int compare(BPUserData ent1, BPUserData ent2){
                 return ent2.point - ent1.point;
             }
         });
 
         // 2人ずつペアにする
         matching = new ArrayList<MatchingData>();
         for ( int i=0; i<userRandomData.size()/2; i++ ) {
             String player1 = userRandomData.get(i*2).name;
             String player2 = userRandomData.get(i*2+1).name;
             MatchingData d = new MatchingData(
                     userdata.get(player1), userdata.get(player2));
             matching.add(d);
         }
 
         // マッチングを表示する
         Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + "======== Matching ========");
         for ( int i=0; i<matching.size(); i++ ) {
             MatchingData d = matching.get(i);
             Bukkit.broadcastMessage(String.format(ChatColor.RED + "%d. %s(%dP) - %s(%dP)",
                     (i+1), d.getPlayer1().name, d.getPlayer1().point,
                     d.getPlayer2().name, d.getPlayer2().point));
         }
         Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + "==========================");
 
         // 各プレイヤーにマッチング相手を表示する
         for ( MatchingData d : matching ) {
             Player p1 = Bukkit.getPlayerExact(d.getPlayer1().name);
             Player p2 = Bukkit.getPlayerExact(d.getPlayer2().name);
             p1.sendMessage(getMessage("matchingResult", d.getPlayer2().name, d.getPlayer2().point));
             p2.sendMessage(getMessage("matchingResult", d.getPlayer1().name, d.getPlayer1().point));
         }
         if ( spectator != null ) {
             spectator.sendMessage(getMessage("matchingResultSpectator"));
         }
 
         return true;
     }
 
     /**
      * クリエイティブ以外のプレイヤーを取得する
      * @return クリエイティブ以外のプレイヤー
      */
     private ArrayList<Player> getPlayersWithoutCreative() {
 
         ArrayList<Player> players = new ArrayList<Player>();
         Player[] all = Bukkit.getOnlinePlayers();
         for ( Player p : all ) {
             if ( p.getGameMode() != GameMode.CREATIVE ) {
                 players.add(p);
             }
         }
         return players;
     }
 
     /**
      * 指定されたコマンドが有効かどうかを返す
      * @param command コマンド
      * @return 有効かどうか
      */
     private boolean isValidCommand(String command) {
 
         for ( String c : COMMANDS ) {
             if ( c.equalsIgnoreCase(command) ) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * メッセージリソースを取得し、Stringを返す
      * @param key メッセージキー
      * @param args メッセージの引数
      * @return メッセージ
      */
     private String getMessage(String key, Object... args) {
         String msg = Messages.get(key, args);
         if ( msg.equals("") ) {
             return "";
         }
         return Utility.replaceColorCode(prefix + msg);
     }
 
     /**
      * originがtargetの方を向くように、pitchとyawを設定したLocationを返す
      * @param origin
      * @param target
      * @return
      */
     private Location setDirection(Location origin, Location target) {
 
         Location result = origin.clone();
         double deltaX = target.getX() - origin.getX();
         double deltaY = target.getY() - origin.getY();
         double deltaZ = target.getZ() - origin.getZ();
         double distance = Math.sqrt(deltaZ * deltaZ + deltaX * deltaX);
        double pitch = -Math.asin(deltaY/distance) * 180 / Math.PI;
        double yaw = -Math.atan2(deltaX, deltaZ) * 180 / Math.PI;
         result.setPitch((float)pitch);
         result.setYaw((float)yaw);
         return result;
     }
 
     /**
      * 指定したプレイヤーのインベントリをクリアし、体力スタミナを回復する
      * @param player
      */
     public static void clearInvAndHeal(Player player) {
 
         player.setHealth(20);
         player.setFoodLevel(20);
         player.getInventory().clear();
         player.getInventory().setHelmet(null);
         player.getInventory().setChestplate(null);
         player.getInventory().setLeggings(null);
         player.getInventory().setBoots(null);
     }
 
     /**
      * プレイヤーからマッチングデータを取得する
      * @param player プレイヤー
      * @return マッチングデータ
      */
     protected MatchingData getMatchingDataFromPlayer(Player player) {
 
         if ( matching == null ) {
             return null;
         }
 
         String name = player.getName();
 
         for ( MatchingData data : matching ) {
 
             if ( data.getPlayer1().name.equals(name) ||
                     data.getPlayer2().name.equals(name) ) {
                 return data;
             }
         }
 
         return null;
     }
 
     /**
      * マッチングデータを削除する
      * @param data 削除するマッチングデータ
      */
     protected void removeMatching(MatchingData data) {
         if ( matching != null ) {
             matching.remove(data);
         }
     }
 }
