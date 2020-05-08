 package com.cahoots.eclipse;
 
 import org.eclipse.jetty.websocket.WebSocketClientFactory;
 
 import com.cahoots.chat.Chat;
 import com.cahoots.connection.ConnectionDetails;
 import com.cahoots.connection.http.CahootsHttpClient;
 import com.cahoots.connection.websocket.CahootsRealtimeClient;
import com.cahoots.connection.websocket.CahootsWebSocket;
 import com.cahoots.eclipse.collab.share.ShareDocumentManager;
 import com.cahoots.eclipse.collab.share.ShareDocumentSessionManager;
 import com.cahoots.eclipse.optransformation.OpSessionRegister;
 import com.google.inject.Binder;
 import com.google.inject.Module;
 import com.google.inject.Singleton;
 
 public class MainModule implements Module {
 
 	@Override
 	public void configure(final Binder binder) {
 		binder.bind(CahootsRealtimeClient.class).in(Singleton.class);
		
 		binder.bind(Chat.class).in(Singleton.class);
 		binder.bind(ConnectionDetails.class).in(Singleton.class);
 		binder.bind(ShareDocumentManager.class).in(Singleton.class);
 		binder.bind(OpSessionRegister.class).in(Singleton.class);
 		binder.bind(ShareDocumentSessionManager.class).in(Singleton.class);
 		binder.bind(WebSocketClientFactory.class);
 		binder.bind(CahootsHttpClient.class);
 	}
 
 }
