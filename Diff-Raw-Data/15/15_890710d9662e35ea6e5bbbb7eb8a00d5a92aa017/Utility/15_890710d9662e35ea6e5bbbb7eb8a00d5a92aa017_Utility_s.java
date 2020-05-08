 /*
  * @author     ucchy
  * @license    LGPLv3
  * @copyright  Copyright ucchy 2013
  */
 package com.github.ucchyocean.ct;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.jar.JarFile;
 import java.util.zip.ZipEntry;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.potion.PotionEffect;
 
 /**
  * @author ucchy
  * ユーティリティクラス
  */
 public class Utility {
 
     /**
      * jarファイルの中に格納されているテキストファイルを、jarファイルの外にコピーするメソッド<br/>
      * WindowsだとS-JISで、MacintoshやLinuxだとUTF-8で保存されます。
      * @param jarFile jarファイル
      * @param targetFile コピー先
      * @param sourceFilePath コピー元
      */
     public static void copyFileFromJar(
             File jarFile, File targetFile, String sourceFilePath) {
 
         InputStream is = null;
         FileOutputStream fos = null;
         BufferedReader reader = null;
         BufferedWriter writer = null;
 
         File parent = targetFile.getParentFile();
         if ( !parent.exists() ) {
             parent.mkdirs();
         }
 
         try {
             JarFile jar = new JarFile(jarFile);
             ZipEntry zipEntry = jar.getEntry(sourceFilePath);
             is = jar.getInputStream(zipEntry);
 
             fos = new FileOutputStream(targetFile);
 
             reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
             writer = new BufferedWriter(new OutputStreamWriter(fos));
 
             String line;
             while ((line = reader.readLine()) != null) {
                 writer.write(line);
                 writer.newLine();
             }
 
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         } finally {
             if ( writer != null ) {
                 try {
                     writer.flush();
                     writer.close();
                 } catch (IOException e) {
                     // do nothing.
                 }
             }
             if ( reader != null ) {
                 try {
                     reader.close();
                 } catch (IOException e) {
                     // do nothing.
                 }
             }
             if ( fos != null ) {
                 try {
                     fos.flush();
                     fos.close();
                 } catch (IOException e) {
                     // do nothing.
                 }
             }
             if ( is != null ) {
                 try {
                     is.close();
                 } catch (IOException e) {
                     // do nothing.
                 }
             }
         }
     }
 
     /**
      * 文字列内のカラーコードを置き換えする
      * @param source 置き換え元の文字列
      * @return 置き換え後の文字列
      */
     public static String replaceColorCode(String source) {
 
         //return source.replaceAll("&([0-9a-fk-or])", "\u00A7$1");
         return ChatColor.translateAlternateColorCodes('&', source);
     }
 
     /**
      * 文字列が整数値に変換可能かどうかを判定する
      * @param source 変換対象の文字列
      * @return 整数に変換可能かどうか
      */
     public static boolean tryIntParse(String source) {
 
         return source.matches("^-?[0-9]+$");
     }
 
     /**
      * カラーコードをChatColorに変換する
      * @param colorCode カラーコード
      * @return ChatColorオブジェクト
      */
     public static ChatColor getChatColorFromColorCode(String colorCode) {
         
         if ( !colorCode.matches("&[0-9a-fk-or]") ) {
             return ChatColor.WHITE;
         }
         
         char code = colorCode.charAt(1);
         return ChatColor.getByChar(code);
     }
 
     /**
      * Jarファイル内から指定したファイルを直接読み込み、内容を返すメソッド
      * @return ファイルの内容
      */
     public static ArrayList<String> getContentsFromJar(String name) {
 
         ArrayList<String> contents = new ArrayList<String>();
         JarFile jarFile = null;
         InputStream inputStream = null;
         try {
             jarFile = new JarFile(ColorTeaming.instance.getPluginJarFile());
             ZipEntry zipEntry = jarFile.getEntry(name);
             inputStream = jarFile.getInputStream(zipEntry);
             BufferedReader reader =
                     new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
             String line;
             while ( (line = reader.readLine()) != null ) {
                 contents.add(line);
             }
         } catch (IOException e) {
             e.printStackTrace();
         } finally {
             if ( jarFile != null ) {
                 try {
                     jarFile.close();
                 } catch (IOException e) {
                     // do nothing.
                 }
             }
             if ( inputStream != null ) {
                 try {
                     inputStream.close();
                 } catch (IOException e) {
                     // do nothing.
                 }
             }
         }
 
         return contents;
     }
     
     /**
      * プレイヤーの全回復、および、全エフェクトの除去を行う
      * @param player 対象プレイヤー
      */
    public static void heal(Player player) {
         
         player.setHealth(player.getMaxHealth());
         player.setFireTicks(0);
         player.setFallDistance(0);
         player.setFoodLevel(20);
         player.setRemainingAir(player.getMaximumAir());
         Collection<PotionEffect> effects = player.getActivePotionEffects();
         for ( PotionEffect e : effects ) {
             player.removePotionEffect(e.getType());
         }
     }
     
     /**
      * 経験値表示を更新する
      * @param player 更新対象のプレイヤー
      */
     public static void updateExp(Player player) {
 
         int total = player.getTotalExperience();
         player.setLevel(0);
         player.setExp(0);
         while ( total > player.getExpToLevel() ) {
             total -= player.getExpToLevel();
             player.setLevel(player.getLevel()+1);
         }
         float xp = (float)total / (float)player.getExpToLevel();
         player.setExp(xp);
     }
 }
