 package demos;
 
 import vitro.util.*;
 import vitro.model.*;
 import vitro.model.graph.*;
 import vitro.controller.*;
 import java.util.*;
 import static vitro.util.Groups.*;
 
 public class VacBrain implements Agent<VacWorld.Scrubby> {
 
 	public Action choose(VacWorld.Scrubby actor, Set<Action> options) {

 		// if there's stuff to clean, clean it!
 		DestroyAction clean = actor.destroy(VacWorld.Dirt.class, options);
 		if (clean != null) { return clean; }
 
 		// otherwise, find stuff to clean!
 		Actor dirt = firstOfType(VacWorld.Dirt.class, actor.location().reachableActors());
 		MoveAction move = actor.moveToward(dirt, options);
 		if (move != null) { return move; }
 
 		// life without cleaning is meaningless.
 		return null;
 	}
 }
