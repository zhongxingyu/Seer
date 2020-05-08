 package controllers;
 
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringWriter;
 
import models.Game;
 import models.Client;
 import models.GameWorld;
 import models.MapModel;
 
 import org.apache.commons.io.IOUtils;
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.node.ObjectNode;
 
import play.api.libs.ws.WS;
 import play.libs.Json;
import play.mvc.*;
 import views.html.index;
 
 public class Application extends Controller {
 
 	protected static MapModel map = getMap();
 	protected static Game game = new Game();
 
 	public static Result index() {
 		String player = game.getNextPlayerFor("" + System.currentTimeMillis(),
 				request().remoteAddress());
 		if (player == null) {
			return notFound();
 		}
 		return ok(index.render("Your new application is ready.", player));
 	}
 
 	public static Result map() {
 		ObjectNode result = Json.newObject();
 		result.put("colMap", Json.toJson(map.colMap));
 		result.put("fieldMap", Json.toJson(map.fieldMap));
 		return ok(result);
 	}
 
 	public static Result player() {
 		ObjectNode result = Json.newObject();
 		result.put("player",
 				Json.toJson(game.getPlayer(request().remoteAddress())));
 		return ok(result);
 
 	}
 
 	public static Result players() {
 		ObjectNode result = Json.newObject();
 		result.put("players", Json.toJson(game.getPlayers()));
 		return ok(result);
 	}
 
 	public static WebSocket<JsonNode> join() {
 		return new WebSocket<JsonNode>() {

 			// Called when the Websocket Handshake is done.
 			public void onReady(WebSocket.In<JsonNode> in, WebSocket.Out<JsonNode> out) {
				//Client user = game.getPlayer();
 				// Join the chat room.
 				try {
					GameWorld.join("user.username", in, out);
 				} catch (Exception ex) {
 					ex.printStackTrace();
 				}
 			}
 		};
 	}
 
 	private static MapModel getMap() {
 		BufferedInputStream bis = new BufferedInputStream(
 				Application.class.getResourceAsStream("/public/maps.json"));
 		JsonNode json = Json.parse(getContentOfStream(bis));
 		MapModel mm = Json.fromJson(json, models.MapModel.class);
 		mm.fillUpField();
 		mm.fillUpCols();
 		return mm;
 	}
 
 	private static String getContentOfStream(InputStream is) {
 		String out = "";
 		StringWriter writer = new StringWriter();
 		try {
 			IOUtils.copy(is, writer, "UTF-8");
 			out = writer.toString();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return out;
 	}
 }
