 /*
  * @author     ucchy
  * @license    LGPLv3
  * @copyright  Copyright ucchy 2013
  */
 package com.github.ucchyocean.ct.listener;
 
 import org.bukkit.GameMode;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 
 import com.github.ucchyocean.ct.ColorTeaming;
 import com.github.ucchyocean.ct.KanaConverter;
 import com.github.ucchyocean.ct.config.ColorTeamingConfig;
 
 /**
  * チャットが発生したときに、チームチャットへ転送するためのリスナークラス
  * @author ucchy
  */
 public class PlayerChatListener implements Listener {
 
     private static final String GLOBAL_CHAT_MARKER = "#GLOBAL#";
 
     private ColorTeaming plugin;
 
     public PlayerChatListener(ColorTeaming plugin) {
         this.plugin = plugin;
     }
 
     /**
      * Playerがチャットを送信したときに発生するイベント
      * @param event
      */
     @EventHandler
     public void onPlayerChat(AsyncPlayerChatEvent event) {
 
         // GLOBALマーカーが付いていたら、/g コマンドを経由してきたので、
         // GLOBALマーカーを取り除いてから抜ける。
         ColorTeamingConfig config = plugin.getCTConfig();
         if ( event.getMessage().startsWith(GLOBAL_CHAT_MARKER) ) {
             String newMessage = event.getMessage().substring(GLOBAL_CHAT_MARKER.length());
             if ( config.isShowJapanizeGlobalChat() ) {
                 newMessage = addJapanize(newMessage); // Japanize化
             }
             event.setMessage(newMessage);
             return;
         }
 
         // チームチャット無効なら、何もせずに抜ける
         if ( !config.isTeamChatMode() ) {
             if ( config.isShowJapanizeGlobalChat() ) {
                 event.setMessage( addJapanize(event.getMessage()) ); // Japanize化
             }
             return;
         }
 
         Player player = event.getPlayer();
 
         // プレイヤーのゲームモードがクリエイティブなら、何もせずに抜ける
         if ( player.getGameMode() == GameMode.CREATIVE ) {
             if ( config.isShowJapanizeGlobalChat() ) {
                 event.setMessage( addJapanize(event.getMessage()) ); // Japanize化
             }
             return;
         }
 
         // チームに所属していなければ、何もせずに抜ける
         if ( plugin.getAPI().getPlayerTeamName(player) == null ) {
             return;
         }
 
         // チームメンバに送信する
         plugin.getAPI().sendTeamChat(player, event.getMessage());
 
         // 元のイベントをキャンセル
         event.setCancelled(true);
     }
 
     /**
      * ローマ字をかな変換して、うしろにくっつける
      * @param message 変換元
      * @return 変換後
      */
     private String addJapanize(String message) {
 
         // 2byteコードを含む場合や、半角カタカナしか含まない場合は、
         // 処理しないようにする。
         if ( message.getBytes().length == message.length() &&
                 !message.matches("[ \\uFF61-\\uFF9F]+") ) {
             String kana = KanaConverter.conv(message);
             message = message + "(" + kana + ")";
         }
         return message;
     }
 }
