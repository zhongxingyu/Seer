package edu.rit.se.sse.rapdevx.api
 
 // TODO: add Exceptions for unexpected situations, like the session being not found.
 
 import edu.rit.se.sse.rapdevx.api.dataclasses.Session;
 import edu.rit.se.sse.rapdevx.api.dataclasses.Game;
 
 /**
  * API access to the Session object on the server side
  *
  * Approximates the ActiveRecord pattern
  *
  * @author Ben Nicholas
  */
 public class SessionApi {
 	/**
 	 * Create a new session, optionally specifying a game to join.  If no game 
 	 * is provided, the session will be registered into the matchmaking pool 
 	 * on the server side and be assigned to a game.
 	 *
 	 * @param requestedGame The game that the user would like to join.  If 
 	 * there is no preference, pass in null.
 	 *
 	 * @return The initial session object, potentially populated with game_id.
 	 * If no game_id is provided, periodically check for updates until the 
 	 * session has been given a game.
 	 */
 	public Session createSession(Game requestedGame) {
 		return null;
 	}
 
 	/**
 	 * Check for updates to the given session, primarily used while waiting for 
 	 * a game assignment. 
 	 *
 	 * @param sessionToCheck The session to check for updates(session_id is the 
 	 * only important part)
 	 *
 	 * @return A newly allocated session object, with the server's current view 
 	 * of the provided session.
 	 */
 	public Session updateSession(Session sessionToCheck) {
 		return null;
 	}
 
 	/**
 	 * "Log out" of your current game session.
 	 *
 	 * @param sessionToDestroy The session to be destroyed(session_id is the 
 	 * only important part)
 	 *
 	 * @return A boolean representing whether or not the destroy operation was 
 	 * successful.
 	 */
 	public boolean destroySession(Session sessionToDestroy) {
		return null;
 	}
 }
