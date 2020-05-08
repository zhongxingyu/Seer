 package edu.berkeley.gamesman.util;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import edu.berkeley.gamesman.core.Game;
 import edu.berkeley.gamesman.core.Hasher;
 
 /**
  * The DependencyResolver is used to automatically choose hashers and databases for selected Games
  * @author Steven Schlansker
  *
  */
 public final class DependencyResolver {
 
 	private DependencyResolver(){}
 	
 	static HashMap<Class<? extends Game<?,?>>,ArrayList<Class <? extends Hasher>>> allowable = new HashMap<Class<? extends Game<?,?>>,ArrayList<Class <? extends Hasher>>>();
 	
	/**
	 * Declare that a game allows the use of a certain hasher
	 * @param game The game
	 * @param hasher The hasher
	 */
 	public static void allowHasher(Class<? extends Game<?,?>> game, Class<? extends Hasher> hasher){
 		if(allowable.containsKey(game))
 			allowable.get(game).add(hasher);
 		else{
 			ArrayList<Class<? extends Hasher>> a = new ArrayList<Class<? extends Hasher>>();
 			a.add(hasher);
 			allowable.put(game, a);
 		}
 	}
 	
	/**
	 * Queries whether a hasher is allowable for a given game
	 * @param game The game that this query is in reference to
	 * @param hasher The hasher we're interested in
	 * @return True is the hasher is allowable
	 */
	public static boolean isHasherAllowed(Class<?> game, Class<?> hasher){
 		return allowable.containsKey(game) && allowable.get(game).contains(hasher);
 	}
 	
 }
