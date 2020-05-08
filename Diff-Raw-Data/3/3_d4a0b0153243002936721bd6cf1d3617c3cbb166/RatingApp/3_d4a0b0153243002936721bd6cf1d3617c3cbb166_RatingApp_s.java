 package controllers;
 
 import java.util.Date;
 import java.util.List;
 
 import models.Game;
 import models.Player;
 import models.Post;
 import play.Play;
 import play.cache.Cache;
 import play.data.validation.Required;
 import play.mvc.Before;
 import play.mvc.Controller;
 
 public class RatingApp extends Controller {
 	public static void index() {
 		List<Game> games = Game.find("order by playedAt desc").from(0)
 				.fetch(10);
 		int numberOfGames = (int) Game.count();
 		render(games, numberOfGames);
 	}
 	
 	public static void create(
 			@Required(message = "A game should at least have players") String playerOneName, 
 			@Required(message = "A game should at least have players") String playerTwoName, 
 			Date playedAt, 
 			String winnerName) {
 		
 		Game g = new Game(new Date());
 		Player playerOne = Player.find("byName", playerOneName).first();
 		Player playerTwo = Player.find("byName", playerTwoName).first();
 		
 		if (playerOne == null) {
 			playerOne = new Player(playerOneName);
 			playerOne.validateAndCreate();
 		}
 		g.playerOne = playerOne;
 
 		if (playerTwo == null) {
 			playerTwo = new Player(playerTwoName);
 			playerTwo.validateAndCreate();
 		}
 		g.playerTwo = playerTwo;
 		
 		if (validation.hasErrors()) {
 			render("RatingApp/form.html", g);
 		}
 		
 		if (winnerName != null) {
 			if (winnerName.equalsIgnoreCase(playerOneName)) {
 				g.winGame(playerOne);
 			}
 			if (winnerName.equalsIgnoreCase(playerTwoName)) {
 				g.winGame(playerTwo);
 			}
 		}
 		flash.success("Created a game between %s and %s", playerOne, playerTwo);
 		g.validateAndCreate();
 		show(g.id);
 	}
 	
 	public static void form() {
 		List<Player> players = Player.findAll(); 
 		render(players);
 	}
 	
 	public static void playerForm() {
 		render();
 	}
 	
 	public static void createPlayer(
 			@Required(message = "A player should at least have a name") String playerName) {
 		
 		Player player = new Player(playerName);
 		player.validateAndCreate();
 
 		if (validation.hasErrors()) {
 			render("RatingApp/playerForm.html", player);
 		}
 		
 		flash.success("Created a player called %s", playerName);
 
 		playerList();
 	}
 	
 	public static void winform(Long id) {
 		Game game = Game.findById(id);
 		render(game);
 	}
 	
 	public static void show(Long id) {
 		Game game = Game.findById(id);
 		render(game);
 	}
 
 	public static void playerList()	{
 		renderArgs.put("menuItem", Play.configuration.getProperty("menu.rank"));
 
 		int playerCount = (int) Player.count();
 		List<Player> players = Player.find("order by eloRating desc").from(0)
 				.fetch(10);
 		render(players, playerCount);
 	}
 	
 	public static void postComment(Long id,
 			@Required(message = "Author is required") String author,
 			@Required(message = "A message is required") String content,
 			@Required(message = "Please type the code") String code,
 			String randomID) {
 		Game game = Game.findById(id);
 		//TODO: re-enable captcha
 //		if (code != null && !code.equalsIgnoreCase("")) {
 //			validation.equals(code, Cache.get(randomID)).message(
 //					"Invalid code. Please type it again");
 //		}
 
 		if (validation.hasErrors()) {
 			render("RatingApp/show.html", game, randomID);
 		}
 		game.addComment(author, content);
 		flash.success("Thanks for posting your message, %s", author);
 		Cache.delete(randomID);
 		show(id);
 	}
 
 	public static void winGame(Long id, String winnerName) {
 		Game game = Game.findById(id);
 		Player winner = Player.find("byName", winnerName).first();
 		game.winGame(winner);
 		game.validateAndSave();
 		show(game.id);
 	}
 	
 	@Before
 	static void addDefaults() {
 		renderArgs.put("tvkTitle", Play.configuration.getProperty("tvk.title"));
 		renderArgs.put("tvkBaseline",
 				Play.configuration.getProperty("tvk.baseline"));
 		renderArgs.put("menuItem", Play.configuration.getProperty("menu.game"));
 	}
 }
