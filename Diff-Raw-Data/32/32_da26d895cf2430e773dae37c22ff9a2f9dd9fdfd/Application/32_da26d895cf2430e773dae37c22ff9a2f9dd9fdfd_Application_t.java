 package controllers;
 
 import models.Game;
 import views.html.game;
 import views.html.index;
 
 import play.data.Form;
import play.libs.Json;
 import play.mvc.Controller;
 import play.mvc.Result;
 
import org.codehaus.jackson.node.ObjectNode;

 public class Application extends Controller {
 
     public static Result index() {
         Form<Game> gameForm = form(Game.class);
         return ok(index.render(gameForm));
     }
 
     public static Result newGame() {
         Form<Game> gameForm = form(Game.class).bindFromRequest();
         if (gameForm.hasErrors()) {
             return badRequest(index.render(gameForm));
         }
         Game newGame = gameForm.get();
         newGame.save();
         return redirect(routes.Application.game(newGame.id));
     }
 
     public static Result game(Long id) {
        Game currentGame = Game.find.byId(id);
        if(currentGame != null) {
            return ok(game.render(currentGame));
        }
        return redirect(routes.Application.index());
    }

    public static Result selectDoor(Long id, int doorNo) {
        Game currentGame = Game.find.byId(id);
        currentGame.selectedDoor = doorNo;
        currentGame.save();
        ObjectNode result = Json.newObject();
        result.put("goatDoor", currentGame.goatDoor());
        return ok(result);
     }
 
 }
