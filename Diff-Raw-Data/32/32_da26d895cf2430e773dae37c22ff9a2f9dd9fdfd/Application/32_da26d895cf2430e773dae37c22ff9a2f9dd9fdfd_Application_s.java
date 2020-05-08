 package controllers;
 
 import models.Game;
 import views.html.game;
 import views.html.index;
 
 import play.data.Form;
 import play.mvc.Controller;
 import play.mvc.Result;
 
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
        return ok(game.render(Game.find.byId(id)));
     }
 
 }
