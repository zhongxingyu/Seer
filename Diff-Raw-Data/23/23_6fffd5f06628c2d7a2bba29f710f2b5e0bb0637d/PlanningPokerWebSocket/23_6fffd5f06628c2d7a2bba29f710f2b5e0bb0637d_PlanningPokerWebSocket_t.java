 package com.planningpoker.web.socket;
 
 import static java.lang.System.out;
 
 import java.io.StringReader;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Method;
 import java.util.Map;
 
 import javax.json.Json;
 import javax.json.JsonObject;
 
 import org.eclipse.jetty.websocket.api.Session;
 import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
 import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
 import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
 import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
 import org.eclipse.jetty.websocket.api.annotations.WebSocket;
 
 import com.planningpoker.model.Game;
 import com.planningpoker.model.Play;
 import com.planningpoker.model.Player;
 import com.planningpoker.web.Games;
 
 @WebSocket
 public class PlanningPokerWebSocket {
 
 	@OnWebSocketConnect
 	public void onOpen(final Session session) {
 		final Map<String, String[]> params = session.getUpgradeRequest().getParameterMap();
 		String[] id = params.get("gameId");
 		final Integer gameId = id!=null && !"null".equals(id[0]) ? Integer.valueOf(id[0]) : null;
 		final String playerName = params.get("playerName")[0];
 		
 		out.println("==>> Opening connection " + session.getRemoteAddress() + " - " + gameId + " - " + playerName);
 
 		WebSocketObserver observer = new WebSocketObserver(session);
 		if (gameId == null) {
 			Games.nextGame(playerName, observer);
 		} else {
 			final Game game = Games.getGame(gameId);
 			if(game==null)
 				throw new IllegalArgumentException("Game com id " + gameId + " no existe ou j foi finalizado!");
 			game.addPlayer(playerName, observer);			
 		}		
 	}
 
 	@OnWebSocketMessage
 	public void onMessage(final Session session, final String json) {
 		final Map<String, String[]> params = session.getUpgradeRequest().getParameterMap();
 		String[] id = params.get("gameId");
 		final Integer gameId = id!=null && !"null".equals(id[0]) ? Integer.valueOf(id[0]) : null;
 		final String playerName = params.get("playerName")[0];
 		
 		out.println("==>> Opening connection " + session.getRemoteAddress() + " - " + gameId + " - " + playerName);
 		
 		out.println("==>> On vote " + session.getRemoteAddress() + " - " + json);
 		
 		JsonObject obj = Json.createReader(new StringReader(json)).readObject();
 		String method = obj.getString("action");
 		for(Method m : this.getClass().getMethods()){
 			if(m.getName().equals(method)) {
 				Class<?>[] parameterTypes = m.getParameterTypes();
 				Annotation[][] parameterAnns = m.getParameterAnnotations();
 				Object[] args = new Object[parameterTypes.length];
 				int i = 0;
 				for(Class<?> paramType : parameterTypes) {					
 					String parName = ((ParName)parameterAnns[i][0]).value();
 					if(paramType.isAssignableFrom(Integer.class)) {						
 						args[i++] = obj.getInt(parName);
 					} else {
 						args[i++] = obj.getString(parName);
 					}
 				}
 				try {
 					m.invoke(this, args);
 					return;
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		}	
 	}
 	
 	public void vote(@ParName("gameId") Integer gameId, @ParName("playerName")String playerName, @ParName("points")Integer points){
 		final Game game = Games.getGame(gameId);
 		final Player player = game.getPlayer(playerName);
 		final Play currentPlay = game.getCurrentPlay();
 		currentPlay.vote(player, points);
 	}
 	
 	public void newPlay(@ParName("gameId") Integer gameId, @ParName("playerName")String playerName){
 		final Game game = Games.getGame(gameId);
 		final Player player = game.getPlayer(playerName);
 		if(player.isManager()) {
 			game.newPlay();
 		}
 	}
 	
 	public void showIncompleteResult(@ParName("gameId") Integer gameId, @ParName("playerName")String playerName){
 		final Game game = Games.getGame(gameId);
 		final Player player = game.getPlayer(playerName);
 		if(player.isManager()) {
 			game.getCurrentPlay().showResult();
 		}
 	}
 	
 	@OnWebSocketError
 	public void onErro(final Session session, final Throwable cause) {
 		final Map<String, String[]> params = session.getUpgradeRequest().getParameterMap();
 		String[] id = params.get("gameId");
 		final Integer gameId = id!=null && !"null".equals(id[0]) ? Integer.valueOf(id[0]) : null;
 		final String playerName = params.get("playerName")[0];
 
 		out.println("==>> On Error " + session.getRemoteAddress() + " - " + gameId + " - " + playerName + " - " + cause.getMessage());
		
		new WebSocketObserver(session).onError(cause);
 	}
 
 	@OnWebSocketClose
 	public void onClose(final Session session, int x, String text) {
 		final Map<String, String[]> params = session.getUpgradeRequest().getParameterMap();
 		String[] id = params.get("gameId");
 		final Integer gameId = id!=null && !"null".equals(id[0]) ? Integer.valueOf(id[0]) : null;
 		final String playerName = params.get("playerName")[0];
 
 		out.println("==>> Closing connection " + session.getRemoteAddress() + " - " + gameId + " - " + playerName);
 
 		Game game = Games.getGame(gameId);
 		if(game==null)
 			game = Games.getGameByManagerName(playerName);
 		
 		if(game!=null)
 			game.getPlayer(playerName).offline();
 	}
 }
