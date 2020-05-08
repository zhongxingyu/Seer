 package vitro;
 import java.util.*;
 
 /**
 * An Actor generically represents some object in a model.
 * If the Actor represents something that can take Actions,
 * it should override the actions() method of this class.
 *
 * @author John Earnest
 **/
 public class Actor {
 
 	/**
 	* A collection of Actions that are possible for this Actor.
 	* In a simulation round, each Actor will have the opportunity
 	* to perform a single Action. If this method returns an
 	* empty set (the default), no action will be taken.
 	* If this method returns a single action, it will always
 	* be taken. Otherwise, it is the responsibility of the
 	* Controller to determine which action (if any) is taken.
 	* Controllers can associate Agents with Actors to act as
 	* decision-making 'filters'.
 	*
 	* In general, these Actions should be thought of as a
 	* collection of Actions that are 'physically possible'
 	* given the current state of the Model.
 	*
 	* @return a Set of Actions that this Actor can take.
 	**/
 	public Set<Action> actions() {
 		return new HashSet<Action>();
 	}
}
