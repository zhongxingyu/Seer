 package client;
 
 import actors.*;
 //import common.*;
 import timers.*;
 
 import java.net.ConnectException;
 import java.util.Vector;
 
 /**
  * This class handles interactions between Actors 
  * @author dvanhumb
  */
 public class GameEngine
 {
 	/**
 	 * The list of all current Actors
 	 */
 	private Vector<Actor> actorList;
 	/**
 	 * A list of all Animatible objects
 	 */
 	private Vector<Animatible> timerListAnimatibles;
 	/**
 	 * A list of all GameTimer objects
 	 */
 	private Vector<GameTimer> timerListGameTimers;
 	/**
 	 * Whether the game loop should be running
 	 */
 	private boolean running;
 	
 	/**
 	 * Start the game engine
 	 */
 	public GameEngine()
 	{
 		actorList = new Vector<Actor>();
 		timerListAnimatibles = new Vector<Animatible>();
 		timerListGameTimers = new Vector<GameTimer>();
 		
 		running = false;
 	}
 	
 	/**
 	 * Attempts to connect to a server
 	 * @param server  The server name or address to connect to 
 	 * @param username  The player name to use
 	 * @param password  The password to use (may be null if no password given)
 	 * @throws ConnectException  If the request to join the game is not successful, we need to throw some errors
 	 */
 	public void joinGame(String server, String username, String password) throws ConnectException
 	{
 		
 	}
 	
 	/**
 	 * Attempt to start a local game.
 	 * TODO: This will eventually need a long list of parameters:
	 *   + server title
	 *   + server password (optional)
 	 *   + map name to use
 	 *   + any other parameters you want to set
 	 */
 	public void startLocalGame()
 	{
 		// TODO: This has been left blank for the moment, as we won't be supporting this for a while
 	}
 	
 	/**
 	 * Attempt to leave a game
 	 */
 	public void leaveGame()
 	{
 		
 	}
 	
 	/**
 	 * Primarily used by game views to render the currently-visible Actors
 	 * @return  The list of Actors currently alive
 	 */
 	public Vector<Actor> getActorList()
 	{
 		return actorList;
 	}
 	
 	/**
 	 * The main game loop.
 	 * This should be called from a separate thread than the main rendering thread.
 	 */
 	protected void gameLoop()
 	{
 		// Example game loop:
 		while (running)
 		{
 			// Check to see if we have any dead objects that need to be removed
 			for (Actor a : actorList)
 			{
 				if (a.needsRemoving())
 				{
 					a.dispose();
 					actorList.remove(a);
 				}
 			}
 			
 			// Update all timers and time-related objects
 			// Run intelligence on all applicable Actors
 			// Move all applicable Actors
 			// Check all Actors for collisions and react accordingly
 			// Redraw display
 			// repeat
 			
 			// We yield here so we don't hog the entire system
 			Thread.yield();
 		}
 	}
 	
 	/**
 	 * Adds an Actor to the engine
 	 * @param a  The Actor to keep track of
 	 */
 	public void addActor(Actor a)
 	{
 		actorList.add(a);
 	}
 	
 	/**
 	 * Removes an Actor from the engine
 	 * @param a  The Actor to drop
 	 */
 	public void removeActor(Actor a)
 	{
 		a.dispose();
 		actorList.remove(a);
 	}
 	
 	public void addAnimatible(Animatible a)
 	{
 		timerListAnimatibles.add(a);
 	}
 	
 	public void removeAnimatible(Animatible a)
 	{
 		timerListAnimatibles.remove(a);
 	}
 	
 	public void addGameTimer(GameTimer t)
 	{
 		timerListGameTimers.add(t);
 	}
 	
 	public void removeGameTimer(GameTimer t)
 	{
 		timerListGameTimers.remove(t);
 	}
 }
