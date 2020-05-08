/*
  * Copyright ucchy 2013
  */
 package com.github.ucchyocean.cmt.command;
 
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.Vector;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 import com.github.ucchyocean.cmt.ColorMeTeaming;
 
 /**
  * @author ucchy
  * colorclass(cclass)コマンドの実行クラス
  */
 public class CClassCommand implements CommandExecutor {
 
     private static final String PREERR = ChatColor.RED.toString();
     private static final String PREINFO = ChatColor.GRAY.toString();
 
     private static final String REGEX_ITEM_PATTERN = "([0-9]+)(@[0-9]+)?(:[0-9]+)?";
     private static Pattern pattern;
 
     /**
      * @see org.bukkit.plugin.java.JavaPlugin#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
      */
     public boolean onCommand(
             CommandSender sender, Command command, String label, String[] args) {
 
         if ( args.length < 2 ) {
             return false;
         }
 
         String group = args[0];
         String clas = args[1];
 
         Hashtable<String, Vector<Player>> members = ColorMeTeaming.getAllColorMembers();
 
         // 有効なグループ名かユーザー名が指定されたか確認する
         boolean isGroup = false;
         if ( members.containsKey(group)  ) {
             // グループ指定
             isGroup = true;
         } else if ( ColorMeTeaming.getAllPlayers().contains(ColorMeTeaming.getPlayerExact(group)) ) {
             // ユーザー指定
         } else {
             sender.sendMessage(PREERR + "グループまたはプレイヤー " + group + " が存在しません。");
             return true;
         }
 
         // 有効なクラス名が指定されたか確認する
         if ( !ColorMeTeaming.classItems.containsKey(clas) ) {
             sender.sendMessage(PREERR + "クラス " + clas + " が存在しません。");
             return true;
         }
 
         // クラス設定を実行する
         String items = ColorMeTeaming.classItems.get(clas);
         String armor = ColorMeTeaming.classArmors.get(clas);
         int[][] itemData = parseClassItemData(items);
 
         Vector<Player> playersToSet;
         if ( isGroup ) {
             playersToSet = members.get(group);
         } else {
             playersToSet = new Vector<Player>();
             playersToSet.add(ColorMeTeaming.getPlayerExact(group));
         }
 
         for ( Player p : playersToSet ) {
 
             // インベントリの消去
             p.getInventory().clear();
             p.getInventory().setHelmet(null);
             p.getInventory().setChestplate(null);
             p.getInventory().setLeggings(null);
             p.getInventory().setBoots(null);
 
             // アイテムの配布
             for ( int[] data : itemData ) {
                give(p, data[0], data[1], data[2]);
             }
 
             // 防具の配布
             if ( armor != null ) {
                 int[][] armorData = parseClassItemData(armor);
                 if (armorData[0][0] != 0) {
                     p.getInventory().setHelmet(getItemStack(armorData[0][0], 1, 0));
                 }
                 if (armorData[1][0] != 0) {
                     p.getInventory().setChestplate(getItemStack(armorData[1][0], 1, 0));
                 }
                 if (armorData[2][0] != 0) {
                     p.getInventory().setLeggings(getItemStack(armorData[2][0], 1, 0));
                 }
                 if (armorData[3][0] != 0) {
                     p.getInventory().setBoots(getItemStack(armorData[3][0], 1, 0));
                 }
             }
         }
 
         String type = "グループ";
         if ( !isGroup ) {
             type = "ユーザー";
         }
 
         sender.sendMessage(PREINFO +
                 String.format("%s %s に、%s クラスの装備とアイテムを配布しました。", type, group, clas));
 
         return true;
     }
 
 
     /**
      * Classのアイテムデータ文字列を解析し、int配列に変換する。
      * @param data 解析元の文字列　例）"44:64,44@2:64,281:10"
      * @return 解析結果　例）{{44,64,0},{44,64,2},{281,10,0}}
      */
     private int[][] parseClassItemData(String data) {
 
         if ( pattern == null ) {
             pattern = Pattern.compile(REGEX_ITEM_PATTERN);
         }
 
         ArrayList<int[]> buffer = new ArrayList<int[]>();
         String[] array = data.split("[,]");
         for (int i = 0; i < array.length; i++) {
 
            int item = 0, damage = 0, amount = 0;
             Matcher matcher = pattern.matcher(array[i]);
 
             if ( matcher.matches() ) {
                 item = Integer.parseInt(matcher.group(1));
                 if ( matcher.group(2) != null ) {
                     damage = Integer.parseInt(matcher.group(2).substring(1));
                 }
                 if ( matcher.group(3) != null ) {
                     amount = Integer.parseInt(matcher.group(3).substring(1));
                 }
             }
 
            if ( amount > 0 || item > 0 ) {
                buffer.add(new int[]{item, amount, damage});
            }
         }
 
         int[][] result = new int[buffer.size()][];
         buffer.toArray(result);
 
         return result;
     }
 
     /**
      * アイテムを配布する
      * @param player 配布先プレイヤー
      * @param item 配布するアイテムのID
      * @param amount 配布するアイテムの数量
      * @param damage 配布するアイテムのダメージ値（指定しない場合は0にする）
      * @return 配布したかどうか。
      */
     private boolean give(Player player, int item, int amount, int damage) {
 
         // Materialの取得
         Material m = Material.getMaterial(item);
         if (m == null) {
             player.sendMessage(PREERR + "指定されたItemID " + item + " が見つかりません。");
             return false;
         }
 
         // 65個を超える配布量の場合は、64個ごとに配布する。
         while (amount > 64) {
             player.getInventory().addItem(getItemStack(item, 64, damage));
             amount -= 64;
         }
 
         // アイテムの配布
         player.getInventory().addItem(getItemStack(item, amount, damage));
 
         return true;
     }
 
     /**
      * ItemStackインスタンスを返す
      * @param item 配布するアイテムのID
      * @param amount 配布するアイテムの数量
      * @param damage 配布するアイテムのダメージ値（指定しない場合は0にする）
      * @return ItemStackインスタンス
      */
     private ItemStack getItemStack(int item, int amount, int damage) {
         if ( damage > 0 )
             return new ItemStack(item, amount, (byte)damage);
         else
             return new ItemStack(item, amount);
     }
 }
