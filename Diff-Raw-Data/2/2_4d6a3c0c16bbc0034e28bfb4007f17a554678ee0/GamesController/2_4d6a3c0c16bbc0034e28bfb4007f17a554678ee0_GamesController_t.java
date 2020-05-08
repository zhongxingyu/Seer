 package edu.gmu.swe681.checkers.controller;
 
 
 import java.io.IOException;
 import java.security.Principal;
 import java.util.HashSet;
 import java.util.List;
 
 import javax.servlet.http.HttpServletResponse;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.view.RedirectView;
 
 import edu.gmu.swe681.checkers.model.Board;
 import edu.gmu.swe681.checkers.model.Game;
 import edu.gmu.swe681.checkers.model.Piece;
 import edu.gmu.swe681.checkers.model.User;
 import edu.gmu.swe681.checkers.service.GameService;
 import edu.gmu.swe681.checkers.service.UserService;
 
 @Controller
 @RequestMapping("/games")
 public class GamesController {
 	
 	private static final transient Logger LOG = LoggerFactory.getLogger(GamesController.class);
 	
 	@Autowired
 	private UserService userService;
 	
 	@Autowired
 	private GameService gameService;
 
 	@RequestMapping(method = RequestMethod.GET)
 	public ModelAndView getCurrentUserProfile(Principal user) {
 		ModelAndView mav = new ModelAndView("games");
 		mav.addObject("availableGames", gameService.getAvailableGames());
 		mav.addObject("myActiveGames", gameService.getMyActiveGames(user.getName()));
 		
 		return mav;
 	}
 
 	@RequestMapping(method = RequestMethod.POST)
 	public RedirectView buildNewGame(Principal user) {
 		LOG.info(user.getName() + " is creating new game...");
 		
 		Game newGame = gameService.save(new Game(userService.retrieve(user.getName())));
 		
 		LOG.info(user.getName() + " created new game of id [" + newGame.getId() + "]");
 		
 		return new RedirectView("/games/" + newGame.getId(), true);
 	}
 	
 	@RequestMapping(value = "/{gameId}/join", method = RequestMethod.POST)
 	public RedirectView joinGame(@PathVariable("gameId") Long gameId, Principal userPrincipal, HttpServletResponse response) throws IOException {
 		User user = userService.retrieve(userPrincipal.getName());
 		
 		Game game = gameService.retrieve(gameId);
 		if(game.getSecondPlayer() != null){
 			//Stops processing at this point (throws exception)
 			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Game has already been joined by a second player.");
 		} else if (game.hasPlayer(user)) {
 			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "You have already joined this game.");
 		}
 		
 		game.setSecondPlayer(user);
 		gameService.save(game);
 		
 		return new RedirectView("/games/" + gameId, true);
 	}
 	
 	//TODO: ROUGH GUESTIMATE -- DIDN'T TEST THIS PART YET
 	@RequestMapping(value = "/{gameId}/move", method = RequestMethod.POST)
 	public RedirectView joinGame(@PathVariable("gameId") Long gameId, @RequestParam("board") List<Piece> pieces, Principal userPrincipal, HttpServletResponse response) throws IOException {
 		User user = userService.retrieve(userPrincipal.getName());
 		
 		Game game = gameService.retrieve(gameId);
 		if(!game.hasPlayer(user)) {
 			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "You are not apart of this game.");
 		}
 		
 		Board nextBoard = game.getBoard().buildNextBoard(new HashSet<Piece>(pieces));
 		game.setBoard(nextBoard);
 		
 		gameService.save(game);
 		
 		return new RedirectView("/games/" + gameId, true);
 	}
 	
 	@RequestMapping(value = "/{gameId}", method = RequestMethod.GET)
 	public ModelAndView getGame(@PathVariable("gameId") Long gameId, Principal userPrincipal, HttpServletResponse response) throws IOException {
 		User user = userService.retrieve(userPrincipal.getName());
 		
 		Game game = gameService.retrieve(gameId);
 		if(!game.hasPlayer(user)) {
			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "You are not a part of this game.");
 		}
 		
 		return new ModelAndView("game", "game", game);
 	}
 	
 	@RequestMapping(value = "/{gameId}/history", method = RequestMethod.GET)
 	public ModelAndView getGame(@PathVariable("gameId") Long gameId, HttpServletResponse response) throws IOException {
 		
 		Game game = gameService.retrieve(gameId);
 		if(game.getWinner() == null) {
 			//TODO: UNCOMMENT WHEN DONE TESTING
 			//response.sendError(HttpServletResponse.SC_NOT_FOUND, "Game has not ended, not history available.");
 		}
 		
 		return new ModelAndView("history", "game", game);
 	}
 	
 	
 }
