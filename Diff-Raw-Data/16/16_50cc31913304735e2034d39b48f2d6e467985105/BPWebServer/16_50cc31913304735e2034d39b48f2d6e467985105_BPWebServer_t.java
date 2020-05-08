 /*
  * @author     ucchy
  * @license    LGPLv3
  * @copyright  Copyright ucchy 2014
  */
 package com.github.ucchyocean.bp.webstat;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.SocketException;
 import java.util.logging.Logger;
 
 import org.bukkit.scheduler.BukkitRunnable;
 
 import com.github.ucchyocean.bp.BattlePoints;
 
 /**
  * Webstatサーバー
  * @author ucchy
  */
 public class BPWebServer extends BukkitRunnable {
 
     private ServerSocket server;
     private boolean isRunning;
     
     /**
      * Webサーバーの開始処理
      * @see java.lang.Runnable#run()
      */
     @Override
     public void run() {
         
         int port = BattlePoints.getInstance().getBPConfig().getWebstatPortNumber();
         Logger logger = BattlePoints.getInstance().getLogger();
         FileCache cache = new FileCache();
         BPDataSorter sorter = new BPDataSorter();
         isRunning = true;
 
         try {
             // サーバサイドのSocketインスタンスを生成
             server = new ServerSocket(port);
            logger.info("Webstats server starts to listen on port "
                     + server.getLocalPort() + ".");
 
             while (true) {
                 // 接続待ち
                 Socket client = server.accept();
                 
                 // 接続処理スレッド
                 ConnectionThread ct = new ConnectionThread(client, cache, sorter);
                 ct.start();
             }
 
         } catch (SocketException e) {
             if ( !isRunning ) {
                logger.info("Webstats server was closed.");
             } else {
                 e.printStackTrace();
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
     
     /**
      * Webサーバーの停止処理
      */
     public void stop() {
         isRunning = false;
         if ( server == null ) {
             return;
         }
         try {
             server.close();
         } catch (IOException e) {
             // do nothing.
         }
     }
 }
