 package controllers;
 
 import interfaces.IObserver;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.UUID;
 
 import model.general.Constances;
 import model.playground.Coordinates;
 import modules.SettingsModule;
 
 import com.fasterxml.jackson.core.JsonProcessingException;
 import com.fasterxml.jackson.databind.JsonNode;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import com.fasterxml.jackson.databind.node.ObjectNode;
 
 import controller.GameController;
 
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 
 import play.libs.F.Callback;
 import play.libs.Json;
 import play.mvc.*;
 
 
 public class Game extends Controller {
 	
 	private static HashMap<String, GameController> controllers = new HashMap<String, GameController>();
 	
 	public static String newGame() {
 		String uuid = null;
 		
 		/* create  a uuid, which not already exist*/
 		while (true) {
 			uuid = UUID.randomUUID().toString();
 			if (!controllers.containsKey(uuid)) {
 				SettingsModule settings = new SettingsModule();
 				settings.setSettings(SettingsModule.Settings.Easy);
 				Injector inject = Guice.createInjector(settings);
 				GameController controller = inject.getInstance(GameController.class);
 				controllers.put(uuid, controller);
 				break;
 			}
 		}
 		return uuid;
 	}
 	
 	public static boolean validController(String uuid) {
 		return controllers.containsKey(uuid) ? true : false;
 	}
 	
 	public static JsonNode MatrixToJson(char[][] matrix) {
 		JsonNode arrNode = null;
 		
 		String json = "[";
 		
 		for (int row = 0; row < matrix.length; row++) {
 			json += "[";
 			for (int column = 0; column < matrix[row].length; column++){
 				json += "{\"x\" : " + row + "," + "\"y\" : " + column + "," + "\"state\" : \""+ matrix[row][column] + "\"}";
 				if (column < matrix[row].length - 1) {
 					json += ",";
 				}
 			}
 			json += "]";
 
 			if (row < matrix.length - 1) {
 				json += ",";
 			}
 		}
 		json += "]";
 		
 		try {
 			arrNode = new ObjectMapper().readTree(json);
 		} catch (JsonProcessingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return arrNode;
 	}
 	
 	
 	public static Result load() {
		System.out.println("Here I am!");
 		JsonNode json = request().body().asJson();
 		ObjectNode result = Json.newObject();
 		if(json == null) {
 			return badRequest("Expecting Json data");
 		} else {
 			String name = json.findPath("name").textValue();
 			
 			if(name == null) {
 				return badRequest("Missing parameter [name]");
 			} else {
				System.out.println("Hallo " + name);
 				result.put("status", "OK");
 				result.put("message", "Hello" + name);
 				return ok(result);
 			}
 		}
 	}  
 	
 
     public static WebSocket<JsonNode> connect() {
         final String uuid = session("uuid");
     	
         return new WebSocket<JsonNode>() {
             
             // Called when the Websocket Handshake is done.
             public void onReady(final WebSocket.In<JsonNode> in, final WebSocket.Out<JsonNode> out){
                             	
             	// Nested class for the Observer of the GameController. Should be a nested class,
             	// because access to the WebSocket.Out variable is required.
             	class Playground implements IObserver {
             		@Override
             		public void update() {
             			System.out.println("---------Update reached--------");
             			ObjectNode status = Json.newObject();                            	
                     	GameController controller = controllers.get(uuid);
                
                 		if (controller.switchedPlayer() && controller.getGameType() == GameController.MULTIPLAYER) {
                 			System.out.println(controller.getActivePlayer()+" please select your target");	
                 		}
                 		model.general.Status controllerStatus = controller.getStatus();
 
                 		if (controllerStatus.errorExist()) {
                 			System.out.println("Error");
                 			status.put("error", controllerStatus.getError());
                   			out.write(status);
                   			controller.getStatus().clear();
                   			return;
                 		}
                 		if (controllerStatus.textExist()) {
                 			System.out.println("Info");
                 			status.put("info", controllerStatus.getText());
                   			out.write(status);
                   			controller.getStatus().clear();
                   			status.removeAll();
                 		}
             			
                 		if (controller.gameFinished()) {
                			return;
                 		}
                 				
                 		if (controller.isAI()) {
                 			while (controller.isAI()) {
                 				if (controller.gameFinished()) {
                 					controllerStatus.getText();
                 					return;
                 				}
                 				int coord = controller.shoot(null);
                 				if (Constances.SHOOT_HIT != coord && Constances.SHOOT_DESTROYED != coord) {
                 					break;
                 				}
                 			}
                 		}
         				/* send an update */
                     	status.put("ownPlayground", MatrixToJson(controller.getOwnPlaygroundAsMatrix()));
                     	status.put("enemyPlayground", MatrixToJson(controller.getEnemyPlaygroundAsMatrix()));
               			out.write(status);
               			return;
             		}
             	}
             	
             	
             	// For each event received on the socket,
                 in.onMessage(new Callback<JsonNode>() {
                     public void invoke(JsonNode event) {
                     	ObjectNode status = Json.newObject();
                     	
                     	GameController controller = controllers.get(uuid);
                     	                    	
                     	if (null != event.findPath("newSinglePlayerGame").textValue()) {
                     		System.out.println("newSinglePlayerGame");
                     		controller.initController(Constances.DEFAULT_ROWS, Constances.DEFAULT_COLUMNS, "Human", GameController.AI_PLAYER_1, GameController.SINGLEPLAYER);
                     		/* send an update */
                         	status.put("ownPlayground", MatrixToJson(controller.getOwnPlaygroundAsMatrix()));
                         	status.put("enemyPlayground", MatrixToJson(controller.getEnemyPlaygroundAsMatrix()));
                   			out.write(status);
                   			return;
                     	}
                     	
                     	if (controller.gameFinished()) {
                 			status.put("info", "Creating a new game is required.");
                 			out.write(status);
                 			return;
                     	}
                     	
                     	if ((event.findPath("shootX").canConvertToInt())
                     	  &&(event.findPath("shootY").canConvertToInt())) {
                     		System.out.println("Shot to target");
                     		int x = event.findPath("shootX").asInt();
                     		int y = event.findPath("shootY").asInt();
                     		Coordinates target = new Coordinates(controller.getRows(), controller.getColumns());
                     		target.setRow(x);
                     		target.setColumn(y);
                     		controller.shoot(target);
                     	}
                     	
                     }
                 });
                 
                GameController controller = controllers.get(uuid);
                
                 // on load of websocket.
                 ObjectNode status = Json.newObject();
             	
             	if (controller.gameFinished()) {
         			status.put("info", "Creating a new game is required.");
         			out.write(status);
         			return;
             	}
             	            	
             	// we have to set the Observers again, because a possible old reference to the
             	// Websocket exist.
     			controller.removeAllObservers();
         		controller.addObserver(new Playground());
             	
         		System.out.println("A game is active");
             	status.put("ownPlayground", MatrixToJson(controller.getOwnPlaygroundAsMatrix()));
             	status.put("enemyPlayground", MatrixToJson(controller.getEnemyPlaygroundAsMatrix()));
       			out.write(status);
             }
         };
     }
 }
