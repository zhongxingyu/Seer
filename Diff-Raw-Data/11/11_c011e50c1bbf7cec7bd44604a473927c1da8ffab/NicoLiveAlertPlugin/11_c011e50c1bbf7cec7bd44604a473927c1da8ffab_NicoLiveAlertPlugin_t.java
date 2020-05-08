 /*
  * @author     ucchy
  * @license    LGPLv3
  * @copyright  Copyright ucchy 2012
  */
 package com.github.ucchyocean.nicolivealert;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 
 import org.bukkit.configuration.MemorySection;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.scheduler.BukkitTask;
 
 /**
  * @author ucchy
  * ニコ生アラートプラグイン
  */
 public class NicoLiveAlertPlugin extends JavaPlugin implements Listener {
 
     private static final String URL_TEMPLATE = "http://live.nicovideo.jp/watch/lv%s";
 
     private static final String KEYWORD_COMMUNITY = "$com";
     private static final String KEYWORD_USER = "$user";
     private static final String KEYWORD_TITLE = "$title";
     private static final String KEYWORD_URL = "$url";
 
     protected Logger logger;
     private String messageTemplate;
     private String messageTemplate2;
     private String messageTemplate3;
     private String messageTemplate4;
     private String messageTemplate5;
     private String messageTemplateURL;
     protected List<String> community;
     protected List<String> user;
     protected MemorySection communityNicknames;
     protected MemorySection userNicknames;
     private BukkitTask task;
     private NicoLiveConnector connector;
     protected List<String> titleKeywords;
 
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
 
         // 監視イベントを登録
         getServer().getPluginManager().registerEvents(this, this);
 
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
         if ( task == null ) {
             connector = new NicoLiveConnector(this);
             task = getServer().getScheduler().runTaskAsynchronously(this, connector);
             return true;
         }
         return false;
     }
 
     /**
      * アラートサーバーとの接続を切断する
      */
     protected boolean disconnect() {
         if ( task != null ) {
             connector.cancel();
             getServer().getScheduler().cancelTask(task.getTaskId());
             task = null;
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
         if ( community == null ) {
             community = new ArrayList<String>();
         }
         user = config.getStringList("user");
         if ( user == null ) {
             user = new ArrayList<String>();
         }
 
         Object communityNicknames_temp = config.get("communityNicknames");
         if ( communityNicknames_temp != null ) {
             communityNicknames = (MemorySection)communityNicknames_temp;
         }
         Object userNicknames_temp = config.get("userNicknames");
         if ( userNicknames_temp != null ) {
             userNicknames = (MemorySection)userNicknames_temp;
         }
 
         messageTemplate = Utility.replaceColorCode(
                config.getString("messageTemplate", "&cニコ生が開始しました！") );
         messageTemplate2 = Utility.replaceColorCode(
                config.getString("messageTemplate2", "&bコミュニティ：$com") );
         messageTemplate3 = Utility.replaceColorCode(
                config.getString("messageTemplate3", "&b放送者：$user") );
         messageTemplate4 = Utility.replaceColorCode(
                config.getString("messageTemplate4", "&b$title") );
         messageTemplate5 = Utility.replaceColorCode(
                 config.getString("messageTemplate5", "") );
 
         messageTemplateURL = config.getString("messageTemplateURL", 
                 "{\"text\":\"＞放送ページはこちら！＜\","
                 + "\"color\":\"red\",\"underlined\":\"true\",\"clickEvent\":{"
                 + "\"action\":\"open_url\",\"value\":\"$url\"}}");
 
         titleKeywords = config.getStringList("titleKeywords");
         if ( titleKeywords == null ) {
             titleKeywords = new ArrayList<String>();
         }
     }
 
     /**
      * 監視対象の放送が見つかったときに呼び出されるメソッド
      * @param event イベント。見つかった放送の詳細が格納される。
      */
     @EventHandler
     public void onAlertFound(NicoLiveAlertFoundEvent event) {
 
         // タイトルキーワードが設定されており、キーワードが見つからない場合は、
         // 通知せずに終了する。一応、ログは出力しておく。
         if ( titleKeywords.size() > 0 ) {
             boolean keywordFound = false;
             for ( String keyword : titleKeywords ) {
                 if ( event.getTitle().contains(keyword) ) {
                     keywordFound = true;
                     break;
                 }
             }
             if ( !keywordFound ) {
                 logger.info("Alert was found. But title didn't contain the keywords. " + event.getId());
                 return;
             }
         }
 
         // 各通知行をキーワードで置き換えして、ブロードキャストに流す。
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
 
         String urlMessage = replaceKeywords(messageTemplateURL, event);
         if ( urlMessage.contains(KEYWORD_URL) ) {
             String url = String.format(URL_TEMPLATE, event.getId());
             urlMessage = urlMessage.replace(KEYWORD_URL, url);
         }
         
         JsonChatBroadcasterV17R1.broadcastJson(urlMessage);
     }
 
     /**
      * 文字列の中のキーワードを置き換えするメソッド
      * @param source 置き換え元の文字列
      * @param event 置き換えに使用するイベント
      * @return 置き換え後の文字列
      */
     private String replaceKeywords(String source, NicoLiveAlertFoundEvent event) {
 
         String result = source;
         if ( result.contains(KEYWORD_COMMUNITY) ) {
             result = result.replace(KEYWORD_COMMUNITY, event.getCommunityNickname());
         }
         if ( result.contains(KEYWORD_USER) ) {
             result = result.replace(KEYWORD_USER, event.getUserNickname());
         }
         if ( result.contains(KEYWORD_TITLE) ) {
             result = result.replace(KEYWORD_TITLE, event.getTitle());
         }
         return result;
     }
 }
