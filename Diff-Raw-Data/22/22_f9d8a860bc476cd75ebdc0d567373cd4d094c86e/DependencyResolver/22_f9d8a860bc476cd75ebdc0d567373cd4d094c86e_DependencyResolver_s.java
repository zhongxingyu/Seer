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
 	
 	public static void allowHasher(Class<? extends Game<?,?>> game, Class<? extends Hasher> hasher){
 		if(allowable.containsKey(game))
 			allowable.get(game).add(hasher);
 		else{
 			ArrayList<Class<? extends Hasher>> a = new ArrayList<Class<? extends Hasher>>();
 			a.add(hasher);
 			allowable.put(game, a);
 		}
 	}
 	
	public static boolean isHasherAllowed(Class game, Class hasher){
 		return allowable.containsKey(game) && allowable.get(game).contains(hasher);
 	}
 	
 }
