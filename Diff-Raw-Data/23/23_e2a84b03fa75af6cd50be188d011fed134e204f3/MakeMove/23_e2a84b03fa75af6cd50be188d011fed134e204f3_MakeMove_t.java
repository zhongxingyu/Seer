 package com.philihp.weblabora.action;
 
 import java.util.Arrays;
 import java.util.List;
 
 import javax.persistence.EntityManager;
 import javax.persistence.TypedQuery;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.swing.ActionMap;
 
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 
 import com.philihp.weblabora.form.MoveForm;
 import com.philihp.weblabora.jpa.Game;
 import com.philihp.weblabora.jpa.State;
 import com.philihp.weblabora.jpa.User;
 import com.philihp.weblabora.model.Board;
 import com.philihp.weblabora.model.GameCountry;
 import com.philihp.weblabora.model.GameLength;
 import com.philihp.weblabora.model.GamePlayers;
 import com.philihp.weblabora.model.MoveProcessor;
 import com.philihp.weblabora.model.WeblaboraException;
 
 public class MakeMove extends BaseAction {
 
 	@Override
 	public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request,
 			HttpServletResponse response, User user) throws Exception {
 		MoveForm form = (MoveForm) actionForm;
 		EntityManager em = (EntityManager)request.getAttribute("em");
 
 		String submit = request.getParameter("submit");
 		if(submit != null && submit.equalsIgnoreCase("Explore") == false) return mapping.findForward("save");
 
 		TypedQuery<Game> query = em.createQuery("SELECT g FROM Game g WHERE g.gameId = :gameId", Game.class);
 		query.setParameter("gameId", form.getGameId());
 		Game game = query.getSingleResult();
 		
 		if(game.getState().getStateId() != form.getStateId()) {
 			return calculateGameForward(mapping, game);
 		}
 		
 		
 		State state = searchForExploredState(game, form.getToken());
 		if (state != null) {
 			// all of this has happened before. all of this will happen again.
 			game.setState(state);
 		} else {
 			Board board = new Board(
 					GamePlayers.valueOf(game.getPlayers()),
 					GameLength.valueOf(game.getLength()),
 					GameCountry.valueOf(game.getCountry())
 					);
 			MoveProcessor.processMoves(board, game.getStates(),null);
 			try {
 				if(Arrays.asList(game.getAllUsers()).contains(user) == false)
 					throw new WeblaboraException("User "+user+" is not one of the players in game "+game.getGameId()); 
 				
 				if(board.isGameOver())
 					throw new WeblaboraException("Game has already ended.");
 				
 				state = new State();
 				state.setToken(form.getToken());
 				state.setSrcState(game.getState());
 				state.setExplorer(user);
 				state.setGame(game);
 				board.preMove(state);
 				MoveProcessor.processActions(board, form.getToken());
 				board.testValidity();
 			}
 			catch(WeblaboraException e) {
 				request.setAttribute("error", e);
 				request.setAttribute("game", game);
 				request.setAttribute("token", form.getToken());
 				return mapping.findForward("badMove");
 			}
 			
 			em.persist(state);
			game.getState().getDstStates().add(state);
 			game.setState(state);
 
 			Game.Player player = ShowGame.findPlayerInGame(game, user);
 			if(player != null) {
 				player.setMove("");
 			}
 			
 		}
 		
 		String to = null;
 		for(User gameUser : game.getAllUsers()) {
 			if(gameUser != null) {
 				if(to == null) 
 					to = gameUser.getFacebookId();
 				else
 					to += ","+gameUser.getFacebookId();
 			}
 		}
 		request.setAttribute("to",to);
 		request.setAttribute("move",form.getToken());
 
 		return calculateGameForward(mapping, game);
 	}
 	
 	private ActionForward calculateGameForward(ActionMapping mapping, Game game) {
 		ActionForward forward = mapping.findForward("show-game");
 		String path = forward.getPath()+"?gameId="+game.getGameId();
 		return new ActionForward(forward.getName(), path, forward.getRedirect(), forward.getModule());
 	}
 
 	protected State searchForExploredState(Game game, String token) throws WeblaboraException {
 		List<State> possibleStates = game.getState().getDstStates();
 		for (State state : possibleStates) {
 			if (token.equals(state.getToken()))
 				return state;
 		}
 		return null;
 
 	}
 
 }
