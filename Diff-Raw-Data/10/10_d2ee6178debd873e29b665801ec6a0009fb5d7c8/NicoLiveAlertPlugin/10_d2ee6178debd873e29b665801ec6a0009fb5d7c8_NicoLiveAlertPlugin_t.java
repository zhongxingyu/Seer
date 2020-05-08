 /*
  * @author     ucchy
  * @license    GPLv3
  * @copyright  Copyright ucchy 2012-2013
  */
 package com.github.ucchyocean.nicolivealert;
 
 import java.io.File;
 import java.util.List;
 import java.util.logging.Logger;
 
 import org.bukkit.configuration.MemorySection;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * @author ucchy
  * ニコ生アラートプラグイン
  */
 public class NicoLiveAlertPlugin extends JavaPlugin {
 
     private static final String URL_TEMPLATE = "http://live.nicovideo.jp/watch/lv%s";
 
     private static final String KEYWORD_COMMUNITY = "$com";
     private static final String KEYWORD_USER = "$user";
     private static final String KEYWORD_TITLE = "$title";
 
     protected Logger logger;
     private String urlColor;
     private String urlTemplate;
     private String messageTemplate;
     private String messageTemplate2;
     private String messageTemplate3;
     private String messageTemplate4;
     private String messageTemplate5;
     protected List<String> community;
     protected List<String> user;
     protected MemorySection communityNicknames;
     protected MemorySection userNicknames;
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
             return;
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
         if ( connector == null ||
                 ( connector != null && connector.isCanceled ) ) {
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
//            try {
//                connectorThread.join();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
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
             Utility.copyFileFromJar(getFile(), configFile, "config_ja.yml", false);
         }
 
         reloadConfig();
         FileConfiguration config = getConfig();
 
         community = config.getStringList("community");
         user = config.getStringList("user");
 
         Object communityNicknames_temp = config.get("communityNicknames");
         if ( communityNicknames_temp != null ) {
             communityNicknames = (MemorySection)communityNicknames_temp;
         }
         Object userNicknames_temp = config.get("userNicknames");
         if ( userNicknames_temp != null ) {
             userNicknames = (MemorySection)userNicknames_temp;
         }
 
         String message_temp = config.getString("messageTemplate", "&cニコ生 [$com]で[$title]が開始しました！&r");
         String message2_temp = config.getString("messageTemplate2", "");
         String message3_temp = config.getString("messageTemplate3", "");
         String message4_temp = config.getString("messageTemplate4", "");
         String message5_temp = config.getString("messageTemplate5", "");
         urlColor = config.getString("urlColor", "&c");
 
         messageTemplate = Utility.replaceColorCode(message_temp);
         messageTemplate2 = Utility.replaceColorCode(message2_temp);
         messageTemplate3 = Utility.replaceColorCode(message3_temp);
         messageTemplate4 = Utility.replaceColorCode(message4_temp);
         messageTemplate5 = Utility.replaceColorCode(message5_temp);
         urlTemplate = Utility.replaceColorCode(urlColor) + URL_TEMPLATE;
     }
 
     /**
      * 監視対象の放送が見つかったときに呼び出されるメソッド
      * @param event イベント。見つかった放送の詳細が格納される。
      */
     protected void onAlertFound(AlertFoundEvent event) {
 
         String startMessage = replaceKeywords(messageTemplate, event);
         getServer().broadcastMessage(startMessage);
 
         if ( !messageTemplate2.equals("") ) {
             String startMessage2 = replaceKeywords(messageTemplate2, event);
             getServer().broadcastMessage(startMessage2);
         }
         if ( !messageTemplate3.equals("") ) {
             String startMessage3 = replaceKeywords(messageTemplate3, event);
             getServer().broadcastMessage(startMessage3);
         }
         if ( !messageTemplate4.equals("") ) {
             String startMessage4 = replaceKeywords(messageTemplate4, event);
             getServer().broadcastMessage(startMessage4);
         }
         if ( !messageTemplate5.equals("") ) {
             String startMessage5 = replaceKeywords(messageTemplate5, event);
             getServer().broadcastMessage(startMessage5);
         }
 
         String urlMessage = String.format(urlTemplate, event.id);
         getServer().broadcastMessage(urlMessage);
     }
 
     /**
      * 文字列の中のキーワードを置き換えするメソッド
      * @param source 置き換え元の文字列
      * @param event 置き換えに使用するイベント
      * @return 置き換え後の文字列
      */
     private String replaceKeywords(String source, AlertFoundEvent event) {
 
         String result = source;
         if ( result.contains(KEYWORD_COMMUNITY) ) {
             result = result.replace(KEYWORD_COMMUNITY, event.communityNickname);
         }
         if ( result.contains(KEYWORD_USER) ) {
             result = result.replace(KEYWORD_USER, event.userNickname);
         }
         if ( result.contains(KEYWORD_TITLE) ) {
             result = result.replace(KEYWORD_TITLE, event.title);
         }
         return result;
     }
 }
