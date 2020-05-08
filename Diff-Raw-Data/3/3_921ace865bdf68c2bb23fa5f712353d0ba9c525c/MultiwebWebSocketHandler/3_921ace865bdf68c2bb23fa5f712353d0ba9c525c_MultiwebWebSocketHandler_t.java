 package daniel.multiweb;
 
 import daniel.chat.ChatWebSocketHandler;
 import daniel.nagger.NaggerWebSocketHandler;
 import daniel.web.http.server.WebSocketHandler;
 import daniel.web.http.server.util.HostBasedWebSocketHandler;
 
 final class MultiwebWebSocketHandler {
   private MultiwebWebSocketHandler() {}
 
   public static WebSocketHandler getHandler() {
     return new HostBasedWebSocketHandler.Builder()
        .addHandlerForHost(".*nagger\\.lubarov\\.com.*", NaggerWebSocketHandler.singleton)
         .addHandlerForHost(".*jabberings\\.net.*", ChatWebSocketHandler.singleton)
         .build();
   }
 }
