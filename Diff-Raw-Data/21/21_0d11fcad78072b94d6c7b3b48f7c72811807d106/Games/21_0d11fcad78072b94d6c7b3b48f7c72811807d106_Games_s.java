 package controllers;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import com.google.gson.reflect.TypeToken;
 
 import controllers.Secure.Security;
 
 import models.Event;
 import models.Game;
 import models.GameMap;
 import models.MapGenerator;
 import models.Player;
 import models.Room;
 import play.libs.F.IndexedEvent;
 import play.mvc.Controller;
 import util.RichUtil;
 import play.mvc.With;
 
 /**
  * Control games.
  * 
  * @author GuoLin
  *
  */
 @With(Secure.class)
 public class Games extends Controller {
 
     public static void game(Integer gameId) {
         Game game = Game.get(gameId);
         notFoundIfNull(game);
        game.enterGame();
         render(game);
     }
 
     public static void waitEvents(Integer gameId, Long lastReceived) {
         Game game = Game.get(gameId);
         notFoundIfNull(game);
         String connected = Security.connected();
         if (!game.validPlayer(connected)) {
             unauthorized();
         }
         List<IndexedEvent<Event>> events = await(game.nextEvents(lastReceived));
         String eventsJson = RichUtil.eventsToJson(events);
         game.refreshLastReceived();
         renderJSON(eventsJson);
     }
 
     public static void action(Integer gameId, String actionName) {
         Game game = Game.get(gameId);
         notFoundIfNull(game);
         String connected = Security.connected();
         if (!game.validPlayer(connected)) {
             unauthorized();
         }
         Game.Action currentAction = Game.Action.valueOf(actionName.toUpperCase());
         notFoundIfNull(currentAction);
         currentAction.doAction(game);
     }
     
     public static void lastReceived(Integer gameId) {
         Game game = Game.get(gameId);
         notFoundIfNull(game);
         String connected = Security.connected();
         if (!game.validPlayer(connected)) {
             unauthorized();
         }
     }
     
 }
