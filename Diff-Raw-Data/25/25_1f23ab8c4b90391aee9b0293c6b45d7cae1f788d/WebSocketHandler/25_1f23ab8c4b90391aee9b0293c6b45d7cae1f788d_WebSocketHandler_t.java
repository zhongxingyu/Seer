 package com.me.Battleships;
 
 import java.io.IOException;
 import java.net.URI;
 import java.util.ArrayList;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import Core.NativeFunctions;
 import Utilities.Turn;
 import android.util.Log;
 
 import com.badlogic.gdx.math.Vector2;
 import com.codebutler.android_websockets.SocketIOClient;
 
 public class WebSocketHandler implements NativeFunctions {
 	private static SocketIOClient client;
 	private final WebSocketInputHandler socketHandler;
 	
 	public WebSocketHandler(WebSocketInputHandler handler) {
 		this.socketHandler = handler;
 	}
 
 	@Override
 	public void setLogicHandler(GameLogic.GameLogicHandler logicHandler) {
 		socketHandler.setGameLogicHandler(logicHandler);		
 	}
 	
 	@Override
 	public void connect() {
 		client = new SocketIOClient(URI.create("http://198.211.119.249"), socketHandler);
 
 		client.connect();
 	}
 	
 	@Override
 	public void disconnect() {
 		try {
 			client.disconnect();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void sendReady(Turn t) {
 		Log.d("battleships", "emitting ready");
 		try {
 			client.emit("ready", null);
 		} catch (JSONException e1) {
 			e1.printStackTrace();
 		}
 	}
 
 	@Override
 	public void sendShoot(Turn t) {
 		Log.d("battleships", "emitting turn");
 
 		JSONArray jsonArray = new JSONArray();
 		JSONObject json = new JSONObject();
 		
 		try {
 			json.put("x", t.x);
 			json.put("y", t.y);
 			json.put("weapon", t.weapon);
 			
 			jsonArray.put(json);
 			client.emit("shoot", jsonArray);
 			
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void sendResult(Turn t) {
 		ArrayList<Vector2> hits = t.hits;
 		try {
 			JSONArray array = new JSONArray();
 
 			for(Vector2	hit : hits) {
 				JSONArray subArray = new JSONArray();
 
 				subArray.put(hit.x);
 				subArray.put(hit.y);
 				array.put(subArray);
 			}
			client.emit("result", new JSONArray().put(array));
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 	}
 
 }
