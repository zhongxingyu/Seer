 package controllers;
 
 import game.SpitzerGameState;
 
 import java.util.List;
 
 import com.google.common.collect.Lists;
 
 import models.Game;
 import models.Game.GameMode;
 import models.User;
 
 import play.libs.Json;
 import play.mvc.*;
 
 import views.html.*;
 
 import static play.data.Form.form;
 
 public class GameController extends Controller {
 	
     public static Result getAll()
     {
     	return ok();
     }
     
     @BodyParser.Of(BodyParser.Json.class)
     public static Result state(Integer gameId)
     {
     	Game game = getGameById(gameId);
 
 	if(game == null)
 		return redirect(controllers.routes.HomeController.index());
 
 	User user = UserController.getCurrentUser();
     	return ok(game.toJson(user));
     }
     
     public static Result join(Integer gameId)
     {
     	Game game = getGameById(gameId);
     	User user = UserController.getCurrentUser();
     	
     	// Just redirect to view, if the player is already in the game
     	if(game.containsPlayer(user))
     		redirect(controllers.routes.GameController.view(gameId));
     	
     	if(user == null || game == null || !game.onPlayerJoin(user))
    		redirect(controllers.routes.GameController.view(gameId));
     	
     	// Add player to game state
     	game.getGameState().addPlayer(user);
     	game.update();
     	
     	return redirect(controllers.routes.GameController.view(gameId));
     }
     
     public static Result view(Integer gameId)
     {
     	Game game = getGameById(gameId);
     	
     	return ok(lobby.render(game));
     }
     
     public static Result host()
     {
     	User user = UserController.getCurrentUser();
     	
     	if(user == null)
     		return redirect(controllers.routes.HomeController.index());
 
     	Game newGame = new Game();
     	newGame.hostUserId = user.id;
     	newGame.state = GameMode.OPEN.getId();    	
     	newGame.onPlayerJoin(user);
 		newGame.name = form().bindFromRequest().get("name"); 
     	SpitzerGameState gameState = new SpitzerGameState();
     	gameState.startGame(newGame);
     	newGame.setGameState(gameState);
     	newGame.save();
     	
     	return redirect(controllers.routes.GameController.view(newGame.id));
     }
 
 	public static Result hostPage()
 	{
 		return ok(host.render());
 	}
     
     public static Game getGameById(Integer id)
     {
     	if(id == null)
     		return null;
     	
     	return Game.find.byId(id);
     }
     
     public static List<Game> getActiveGames(User user)
     {
     	List<Game> allGames = Game.find.all();
     	
     	List<Game> activeGames = Lists.newArrayList();
     	
     	// Find all games that the player is in
     	for(Game game : allGames)
     	{
     		if(game.containsPlayer(user) && !game.getState().equals(GameMode.COMPLETE))
     			activeGames.add(game);
     	}
     	
     	return activeGames;
     }
     
     public static List<Game> getOpenGames(User user)
     {
     	// Get all open games
 		List<Game> allGames = Game.find.where().eq("state", GameMode.OPEN.getId()).findList();
 		List<Game> openGames = Lists.newArrayList();
 
 		for(Game game : allGames)
 		{
 			if(!game.containsPlayer(user))
 				openGames.add(game);
 		}
 		return openGames;
     }
 }
