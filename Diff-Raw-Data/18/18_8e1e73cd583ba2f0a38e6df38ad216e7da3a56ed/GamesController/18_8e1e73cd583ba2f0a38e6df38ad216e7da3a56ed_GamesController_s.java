 package edu.gmu.swe681.checkers.controller;
 
 
 import java.io.IOException;
 import java.security.Principal;
 import java.util.HashSet;
 
 import javax.servlet.http.HttpServletResponse;
 import javax.validation.Valid;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.validation.BindException;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.MethodArgumentNotValidException;
 import org.springframework.web.bind.annotation.ExceptionHandler;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.view.RedirectView;
 
 import edu.gmu.swe681.checkers.dto.GameMove;
 import edu.gmu.swe681.checkers.model.*;
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
 			return null;
 		} else if (game.hasPlayer(user)) {
 			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "You have already joined this game.");
 			return null;
 		}
 		
 		game.setSecondPlayer(user);
 		gameService.save(game);
 		
 		return new RedirectView("/games/" + gameId, true);
 	}
 	
 	@RequestMapping(value = "/{gameId}/move", method = RequestMethod.POST)
 	public RedirectView performGameMove(@PathVariable("gameId") Long gameId, @Valid @RequestBody GameMove gameMove, Principal userPrincipal, HttpServletResponse response, BindingResult result) throws IOException {
 		if(result.hasErrors()) {
 			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid board state sent.");
 			return null;
 		}
 		
 		User user = userService.retrieve(userPrincipal.getName());
 		Game game = gameService.retrieve(gameId);
 		if(!game.hasPlayer(user)) {
 			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "You are not apart of this game.");
 			return null;
 		} else if(game.getWinner() != null) {
 			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "The game is over, " + game.getWinner().getUsername() + " won.");
 			return null;
 		} else if(!game.isUserTurn(user)) {
 			LOG.debug("it's not " +user.getUsername() + "'s turn");
 			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "It's not your turn.");
 			return null;
 		}
 		
 		Board nextBoard = game.getBoard().buildNextBoard(new HashSet<Piece>(gameMove.getBoard()));
 		game.updateBoard(nextBoard);
 		Player winner = nextBoard.getWinner();
 		if(winner != null) {
 			game.setWinner(game.getUser(winner));
 		}
 		
 		gameService.save(game);
 		
 		return new RedirectView("/games/" + gameId, true);
 	}
 	
 	@RequestMapping(value = "/{gameId}", method = RequestMethod.GET)
 	public ModelAndView getGame(@PathVariable("gameId") Long gameId, Principal userPrincipal, HttpServletResponse response) throws IOException {
 		User user = userService.retrieve(userPrincipal.getName());
 		
 		Game game = gameService.retrieve(gameId);
		if(game.getWinner() != null) {
 			return new ModelAndView(new RedirectView("/games/" + gameId + "/history", true));
 		} else if(!game.hasPlayer(user)) {
 			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "You are not a part of this game.");
 			return null;
 		}
 		
 		return new ModelAndView("game", "game", game).addObject("myTurn", game.isUserTurn(user));
 	}
 	
 	@ResponseBody
 	@RequestMapping(value = "/{gameId}/myturn", method = RequestMethod.GET)
 	public Object myTurn(@PathVariable("gameId") Long gameId, Principal userPrincipal, HttpServletResponse response) throws IOException {
 		User user = userService.retrieve(userPrincipal.getName());
 		
 		Game game = gameService.retrieve(gameId);
		if(!game.hasPlayer(user)) {
 			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "You are not a part of this game.");
 			return false;
 		}
 		
 		if(game.getWinner() != null) {
 			return "Game Over";
 		} else {
 			return game.isUserTurn(user);
 		}
 		
 	}
 	
 	@ResponseBody
 	@RequestMapping(value = "/{gameId}/mycolor", method = RequestMethod.GET)
 	public String myPlayerColor(@PathVariable("gameId") Long gameId, Principal userPrincipal, HttpServletResponse response) throws IOException {
 		User user = userService.retrieve(userPrincipal.getName());
 		
 		Game game = gameService.retrieve(gameId);
 		if(!game.hasPlayer(user)) {
 			response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "You are not a part of this game.");
 			return "";
 		}
 		
 		return game.getBlackPlayer().equals(user) ? Player.BLACK.toString() : Player.RED.toString();
 	}
 	
 	
 	
 	@RequestMapping(value = "/{gameId}/history", method = RequestMethod.GET)
 	public ModelAndView getGame(@PathVariable("gameId") Long gameId, HttpServletResponse response) throws IOException {
 		
 		Game game = gameService.retrieve(gameId);
 		if(game.getWinner() == null) {
 			//TODO: UNCOMMENT WHEN DONE TESTING
 			//response.sendError(HttpServletResponse.SC_NOT_FOUND, "Game has not ended, not history available.");
 			//return null;
 		}
 		
 		return new ModelAndView("history", "game", game);
 	}
 
 	@ExceptionHandler
 	public void handleException(HttpServletResponse response, Exception ex) throws IOException {
 		response.sendError(HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
 	}
 	
 	@ExceptionHandler(value = {BindException.class, MethodArgumentNotValidException.class})
 	public void handleBindException(HttpServletResponse response, Exception ex) throws IOException {
 		response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid input recieved.");
 	}
 }
