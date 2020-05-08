 package controllers;
 
 import java.util.Date;
 import java.util.List;
 
 import models.Game;
 import models.User;
 import play.data.validation.Required;
 import play.i18n.Messages;
 import play.mvc.Controller;
 import play.mvc.With;
 
 /**
  * Games controller.
  * 
  * @author GuoLin
  *
  */
 @With(Secure.class)
 public class Games extends Controller {
 
 	public static void list() {
 		List<Game> games = Game.all().fetch();
 		render(games);
 	}
 	
 	public static void show(@Required Long gameId) {
 		Game game = Game.findById(gameId);
 		notFoundIfNull(game);
 		
 		User connectedUser = Security.connectedUser();
         if (game.type == Game.Type.MEETING && !game.hasMember(connectedUser)) {
 		    forbidden(Messages.get("game.message.forbidden", game.name));
 		}
 		
 		render(game);
 	}
 	
 	public static void ajaxCreate(@Required Game game) {
 		User user = Security.connectedUser();
		game.type = Game.Type.TALKSHOW;  // TODO This default type is temp
 		game.startTime = new Date();
 		game.status = Game.Status.OPEN;
 		game.master = user;
 		validation.valid(game);
 		if (validation.hasErrors()) {
 			error(400, Messages.get("game.message.save-failure", game.name));
 		}
 		game.save();
 		
 		request.format = "json";
 		render(game);
 	}
 
     public static void ajaxUpdate(@Required Long gameId, @Required Game game) {
         if (game.id == null || game.id != gameId) {
             badRequest();
         }
         if (!game.isPersistent()) {
             notFound(Messages.get("game.message.not-exists", gameId));
         }
         User user = Security.connectedUser();
         if (!user.equals(game.master)) {
             forbidden(Messages.get("game.message.forbidden", game.name));
         }
         validation.valid(game);
         if (validation.hasErrors()) {
             error(400, Messages.get("game.message.save-failure", game.name));
         }
         game.save();
         
         request.format = "json";
         render(game); 
 	}
     
     public static void ajaxDelete(@Required Long gameId) {
         Game game = Game.findById(gameId);
         if (game == null) {
             notFound(Messages.get("game.message.not-exists", gameId));
         }
         User user = Security.connectedUser();
         if (!user.equals(game.master)) {
             forbidden(Messages.get("game.message.forbidden", game.name));
         }
         game.delete();
         ok();
     }
 
 }
