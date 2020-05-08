 /*
  * @author     ucchy
  * @license    LGPLv3
  * @copyright  Copyright ucchy 2014
  */
 package com.github.ucchyocean.bp.webstat;
 
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 
 import javax.imageio.ImageIO;
 
 import com.github.ucchyocean.bp.BattlePoints;
 import com.github.ucchyocean.bp.Utility;
 
 /**
  * プレイヤーのスキンをダウンロードし、顔の部分を切り抜いて保存するユーティリティクラス
  * @author ucchy
  */
 public class PlayerFaceDownloader {
 
     private static final String URL_SKIN = "http://s3.amazonaws.com/MinecraftSkins/%s.png";
     
    
     /**
      * 指定したプレイヤー名のスキンをダウンロードし、顔の部分を切り抜いて保存する。
      * @param playerName プレイヤー名
      * @param file 保存先
      * @param useSteve ダウンロードに失敗した場合、代わりにスティーブを保存するか
      * @return ダウンロードに成功したかどうか
      */
     public static boolean downloadSkin(String playerName, File file, boolean useSteve) {
         
         // 親フォルダが無いなら、フォルダを作成する
         File folder = file.getParentFile();
         if ( !folder.exists() || !folder.isDirectory() ) {
             folder.mkdirs();
         }
         
         try {
             
             // スキンをダウンロードする。
             URL url = new URL(String.format(URL_SKIN, playerName));
             BufferedImage img = ImageIO.read(url);
             
             // 顔の部分を切り抜く
             int[] face = img.getRGB(8, 8, 8, 8, null, 0, 8);
             // 頭の周囲の透過部分を切り抜く
             int[] trans = img.getRGB(40, 8, 8, 8, null, 0, 8);
             
             // 重ねあわせ処理を行う
             boolean transp = false;
             int v = trans[0];
             for ( int i=0; i<64; i++ ) {
                if ( (trans[i] & 0xFF000000) == 0) {
                    transp = true;
                    break;
                }
                else if (trans[i] != v) {
                     transp = true;
                     break;
                 }
             }
             if (transp) {
                 for ( int i=0; i<64; i++ ) {
                    if ( (trans[i] & 0xFF000000) != 0) {
                         face[i] = trans[i];
                     }
                 }
             }
             
             // 2倍化
             int[] doubleFace = new int[8*8*4];
             for ( int y=0; y<8*2; y++ ) {
                 for ( int x=0; x<8*2; x++ ) {
                     doubleFace[y*8*2+x] = face[(y/2)*8+(x/2)];
                 }
             }
             
             // 書き出し
             BufferedImage output = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
             output.setRGB(0, 0, 16, 16, doubleFace, 0, 16);
             ImageIO.write(output, "png", file);
             
             return true;
 
         } catch (IOException e) {
             
             if ( useSteve ) {
                 // ダウンロードに失敗した場合は、代わりにスティーブの顔を保存する。
                 Utility.copyFileFromJar(BattlePoints.getPluginJarFile(), file, "steve.png", true);
             }
             return false;
         }
     }
 }
