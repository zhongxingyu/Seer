 /*
  * Copyright ucchy 2012
  */
 package com.github.ucchyocean.nicolivealert;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.List;
 import java.util.jar.JarFile;
 import java.util.logging.Logger;
 import java.util.zip.ZipEntry;
 
 import org.bukkit.ChatColor;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * @author ucchy
  * ニコ生アラートプラグイン
  */
 public class NicoLiveAlertPlugin extends JavaPlugin {
 
     private static final String URL_TEMPLATE = ChatColor.RED + "http://live.nicovideo.jp/watch/lv%s";
 
     protected Logger logger;
     private String messageTemplate;
     protected List<String> community;
     protected List<String> user;
     private NicoLiveConnector connector;
     protected Thread connectorThread;
 
     /**
      * プラグインが有効化されたときに呼び出されるメソッド
      * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
      */
     @Override
     public void onEnable() {
 
         logger = getLogger();
 
         try {
             reloadConfigFile();
         } catch (NicoLiveAlertException e) {
             e.printStackTrace();
             getServer().getPluginManager().disablePlugin(this);
         }
 
         // コマンドをサーバーに登録
         getCommand("nicolivealert").setExecutor(new NicoLiveAlertExecutor(this));
 
         // スレッドを起動してアラートサーバーの監視を開始する
         connect();
     }
 
     /**
      * プラグインが無効化されたときに呼び出されるメソッド
      * @see org.bukkit.plugin.java.JavaPlugin#onDisable()
      */
     @Override
     public void onDisable() {
         disconnect();
     }
 
     /**
      * スレッドを起動してアラートサーバーの監視を開始する
      */
     protected boolean connect() {
         if ( connector == null ) {
             connector = new NicoLiveConnector(this);
             connectorThread = new Thread(connector);
             connectorThread.start();
             return true;
         }
         return false;
     }
 
     /**
      * アラートサーバーとの接続を切断する
      */
     protected boolean disconnect() {
         if ( connector != null ) {
             connector.stop();
             try {
                 connectorThread.join();
             } catch (InterruptedException e) {
                 e.printStackTrace();
             }
             connector = null;
             connectorThread = null;
             return true;
         }
         return false;
     }
 
     /**
      * config.ymlの読み出し処理。
      * @throws NicoLiveAlertException
      */
     protected void reloadConfigFile() throws NicoLiveAlertException {
 
         File configFile = new File(getDataFolder(), "config.yml");
         if ( !configFile.exists() ) {
             copyFileFromJar(configFile, "config.yml");
         }
         FileConfiguration config = getConfig();
 
         messageTemplate = config.getString("messageTemplate", "ニコ生 [%s]で[%s]が開始しました！");
         community = config.getStringList("community");
         user = config.getStringList("user");
     }
 
     /**
      * jarファイルの中に格納されているファイルを、jarファイルの外にコピーするメソッド
      * @param outputFile コピー先
      * @param inputFileName コピー元
      */
     private void copyFileFromJar(File outputFile, String inputFileName) {
 
         InputStream is;
         FileOutputStream fos;
         File parent = outputFile.getParentFile();
         if ( !parent.exists() ) {
             parent.mkdirs();
         }
 
         try {
             JarFile jarFile = new JarFile(getFile());
             ZipEntry zipEntry = jarFile.getEntry(inputFileName);
             is = jarFile.getInputStream(zipEntry);
 
             fos = new FileOutputStream(outputFile);
 
             byte[] buf = new byte[8192];
             int len;
             while ( (len = is.read(buf)) != -1 ) {
                 fos.write(buf, 0, len);
             }
             fos.flush();
             fos.close();
             is.close();
 
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 
     /**
      * 監視対象の放送が見つかったときに呼び出されるメソッド
      * @param event イベント。見つかった放送の詳細が格納される。
      */
     protected void onAlertFound(AlertFoundEvent event) {
 
         String startMessage = String.format(messageTemplate, event.communityName, event.title);
         String urlMessage = String.format(URL_TEMPLATE, event.id);
 
         getServer().broadcastMessage(startMessage);
         getServer().broadcastMessage(urlMessage);
     }
 
 }
