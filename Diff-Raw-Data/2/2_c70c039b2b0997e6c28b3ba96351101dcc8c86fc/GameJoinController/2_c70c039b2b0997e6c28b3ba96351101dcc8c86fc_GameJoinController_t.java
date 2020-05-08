 package com.jaredpearson.puzzlestrike.web;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import com.jaredpearson.puzzlestrike.Game;
 import com.jaredpearson.puzzlestrike.GameManager;
 import com.jaredpearson.puzzlestrike.PlayerManager;
 import com.jaredpearson.puzzlestrike.actions.JoinGameAction;
 
 public class GameJoinController extends AbstractGameController implements Controller {
 	
 	public GameJoinController(GameManager gameManager, PlayerManager playerManager) {
 		super(gameManager, playerManager);
 	}
 	
 	public View handle(HttpServletRequest request, HttpServletResponse response, String actionPath)
 			throws Exception {
 		Game game = getGame();
 		if(!game.isOpen()) {
 			throw new IllegalStateException("Game is currently not open.");
 		}
 		
 		game.applyAction(getCurrentPlayer(), new JoinGameAction());
 		
 		return new JspView("redirect:/Game/" + getGame().getId());
 	}
 
 }
