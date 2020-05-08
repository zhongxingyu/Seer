 /*
  * @author     ucchy
  * @license    GPLv3
  * @copyright  Copyright ucchy 2013
  */
 package com.github.ucchyocean.cmt;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 
 /**
  * @author ucchy
  * csaveコマンドやcrestoreコマンドで扱う、チームメンバー保存データのハンドルクラス
  */
 public class TeamMemberSaveDataHandler {
 
     private File saveDir;
 
     /**
      * コンストラクタ
      * @param saveDir セーブデータを保存するフォルダ
      */
     public TeamMemberSaveDataHandler(File dataFolder) {
 
         this.saveDir = new File(dataFolder.getAbsolutePath(), "saves");
 
         if ( !saveDir.exists() ) {
             saveDir.mkdirs();
         }
     }
 
     /**
      * メンバー情報の保存
      * @param name 保存名
      * @return 保存が成功したかどうか
      */
     public boolean save(String name) {
 
         YamlConfiguration config = new YamlConfiguration();
 
         // メンバー情報の保存
         Hashtable<String, ArrayList<Player>> members =
                 ColorMeTeaming.getAllColorMembers();
 
         Enumeration<String> keys = members.keys();
         while ( keys.hasMoreElements() ) {
             String key = keys.nextElement();
 
             // プレイヤーを名前のリストに変換する
             ArrayList<String> names = new ArrayList<String>();
             for ( Player p : members.get(key) ) {
                 names.add(p.getName());
             }
 
             config.set("members." + key, names);
         }
 
         // チームキルデス数の保存
         keys = ColorMeTeaming.killDeathCounts.keys();
         while ( keys.hasMoreElements() ) {
             String key = keys.nextElement();
            config.set("killDeathCounts." + key,
                    ColorMeTeaming.killDeathCounts.get(key));
         }
 
         // ユーザーキルデス数の保存
         keys = ColorMeTeaming.killDeathUserCounts.keys();
         while ( keys.hasMoreElements() ) {
             String key = keys.nextElement();
            config.set("killDeathUserCounts." + key,
                    ColorMeTeaming.killDeathUserCounts.get(key));
         }
 
         // セーブデータをファイルへ保存
         File file = new File(saveDir, name + ".yml");
         try {
             config.save(file);
         } catch (IOException e) {
             e.printStackTrace();
             return false;
         }
 
         return true;
     }
 
     /**
      * メンバー情報の復帰
      * @param name メンバー保存情報の保存名
      * @return 復帰できたかどうか
      */
     public boolean load(String name) {
 
         if ( !isExist(name) ) {
             return false;
         }
 
         // ファイルからYamlConfigurationをロード
         File file = new File(saveDir, name + ".yml");
         YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
 
         // メンバー情報の復帰
         ConfigurationSection msection = config.getConfigurationSection("members");
         if ( msection == null ) {
             return false;
         }
 
         clearAllUsers(); // 一旦、全てのユーザーのグループを解散する
 
         Iterator<String> groups = msection.getValues(false).keySet().iterator();
         while (groups.hasNext()) {
             String group = groups.next();
 
             List<String> groupMembers = msection.getStringList(group);
             for ( String pname : groupMembers ) {
                 Player player = ColorMeTeaming.getPlayerExact(pname);
                 if ( player != null ) {
                     ColorMeTeaming.setPlayerColor(player, group);
                 }
             }
         }
 
         // 保護領域の更新
         if ( ColorMeTeamingConfig.protectRespawnPointWithWorldGuard ) {
             ColorMeTeaming.wghandler.refreshGroupMembers();
         }
 
         // チームキルデス数の復帰
         ConfigurationSection tksection =
                 config.getConfigurationSection("killDeathCounts");
         if ( tksection != null ) {
 
             ColorMeTeaming.killDeathCounts.clear(); // 一旦、全てクリア
 
             groups = tksection.getValues(false).keySet().iterator();
             while (groups.hasNext()) {
                 String group = groups.next();
                 List<Integer> data = tksection.getIntegerList(group);
                 if ( data != null && data.size() >= 3 ) {
                     int[] value = {data.get(0), data.get(1), data.get(2)};
                     ColorMeTeaming.killDeathCounts.put(group, value);
                 }
             }
         }
 
         // プレイヤーキルデス数の復帰
         ConfigurationSection uksection =
                 config.getConfigurationSection("killDeathUserCounts");
         if ( uksection != null ) {
 
             ColorMeTeaming.killDeathUserCounts.clear(); // 一旦、全てクリア
 
             Iterator<String> users = uksection.getValues(false).keySet().iterator();
             while (users.hasNext()) {
                 String user = users.next();
                 List<Integer> data = uksection.getIntegerList(user);
                 if ( data != null && data.size() >= 3 ) {
                     int[] value = {data.get(0), data.get(1), data.get(2)};
                     ColorMeTeaming.killDeathUserCounts.put(user, value);
                 }
             }
         }
 
         return true;
     }
 
     /**
      * 指定した名前のセーブデータが既に存在するかどうかを返すメソッド
      * @param name セーブデータ名
      * @return 存在するかどうか
      */
     public boolean isExist(String name) {
 
         File file = new File(saveDir, name + ".yml");
         return file.exists() && file.isFile();
     }
 
     /**
      * 全てのユーザーの色を解除して、全グループを解散させる
      */
     private void clearAllUsers() {
 
         Hashtable<String, ArrayList<Player>> members = ColorMeTeaming.getAllColorMembers();
         Enumeration<String> keys = members.keys();
         while ( keys.hasMoreElements() ) {
             String group = keys.nextElement();
             for ( Player p : members.get(group) ) {
                 ColorMeTeaming.removePlayerColor(p);
             }
         }
     }
 }
