 package controllers;
 
 import java.util.HashMap;
 import java.util.List;
 
 import models.Display;
 import models.DisplayLayout;
 import models.Tile;
 
 import org.codehaus.jackson.JsonNode;
 
 import play.Logger;
 import play.data.Form;
 import play.libs.F.Callback;
 import play.libs.F.Callback0;
 import play.mvc.Controller;
 import play.mvc.Result;
 import play.mvc.WebSocket;
 import views.html.displayManager;
 
 public class DisplayController extends Controller {
 
 	public static HashMap<String, WebSocket.Out<JsonNode>> activeDisplays = new HashMap<String, WebSocket.Out<JsonNode>>();
 	public static HashMap<WebSocket.Out<JsonNode>, String> outToID = new HashMap<WebSocket.Out<JsonNode>, String>();
 
 	/**
 	 * Prepare the display with the tiles selected during
 	 * the layout creation
 	 * @param displayID
 	 * @return
 	 */
 	public static Result setupDisplay(String displayID) {
 		if(!activeDisplays.containsKey(displayID)){
 			Display display = Display.get(new Long(displayID));
 			String name = display.name;
 			Logger.info("DISPLAY CONTROLLER: \n Display " + name + "(" +  displayID + ") ENABLED");
 			List<Tile> tiles = Tile.layoutTiles(display.currentLayoutID);
 			activeDisplays.put(displayID, null);
 			return ok(views.html.display.render(displayID,name,tiles));
 		} else {
 			return ok("DISPLAY " + displayID + " IS ALREADY ACTIVE");
 		}
 	}
 
 
 	static Form<Display> displayRegistrationForm = form(Display.class);
 
 
 	/**
 	 * Receives the input from a form, binds it and enters it
 	 * into a database.
 	 * @return
 	 */
 	public static Result registerDisplay(){
 		Form<Display> filledForm = displayRegistrationForm.bindFromRequest();
 		if(filledForm.hasErrors()) {
 			return badRequest(displayManager.render(Display.all(), filledForm, DisplayLayout.all()));
 		} else {
 			Logger.info("DISPLAY CONTROLLER: \n" +
 					filledForm.get().name 
 					+ " has been added to the database with layout " + filledForm.get().currentLayoutID 
 					);
 			Display.addNew(filledForm.get());
 			return redirect(routes.DisplayController.showAvailableDisplays());  
 		}
 	}
 
 	/**
 	 * Render the default view with all the displays
 	 * @return
 	 */
 	public static Result showAvailableDisplays(){
 		return ok(displayManager.render(Display.all(), displayRegistrationForm, DisplayLayout.all()));
 	}
 
 	/**
 	 * Remove a display from the database
 	 * @param displayID
 	 * @return
 	 */
 	public static Result deleteDisplay(Long displayID){
 		Display.delete(displayID);
 		return redirect(routes.DisplayController.showAvailableDisplays());
 	}
 
 	public static WebSocket<JsonNode> webSocket() {
 		return new WebSocket<JsonNode>() {
 			@Override
 			public void onReady(WebSocket.In<JsonNode> in, final WebSocket.Out<JsonNode> out) {
 				in.onMessage(new Callback<JsonNode>() {
 					public void invoke(JsonNode event) {
 						String displayID = event.get("displayID").asText();
						Logger.info("ADDING " + displayID);
 						activeDisplays.put(displayID, out);
 						outToID.put(out, displayID);
 					}
 				});
 
 				// When the socket is closed.
 				in.onClose(new Callback0() {
 					public void invoke() {
 						String displayID = outToID.get(out);
 						outToID.remove(out);
 						activeDisplays.remove(displayID);
 						Logger.info(
 										"\n ******* MESSAGE RECIEVED *******" +
 										"\n Display " + displayID + "is now disconnected." +
 										"\n*********************************"
 								);
 					}
 
 
 				});
 
 			}
 
 		};
 	}
 }
 
