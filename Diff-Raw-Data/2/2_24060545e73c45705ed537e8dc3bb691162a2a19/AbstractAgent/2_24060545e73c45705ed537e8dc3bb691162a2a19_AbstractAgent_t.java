 package agent;
 
 import environment.AttemptedAction;
 import environment.Action;
 import environment.Environment;
 
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 
 import static environment.Action.Type;
 
 public abstract class AbstractAgent {
 
     protected int tankIndex;
 
     protected AbstractAgent(int tankIndex) {
         this.tankIndex = tankIndex;
     }
 
     public abstract List<Action> getActions(Environment environment);
 
     public abstract void processAttemptedActions( List<AttemptedAction> attemptedActions );
 
 	/**
 	 * @return a map from desired component to the parameters to pass to the server. If a desired component does not
	 * require any parameters, then any value (including null) is okay for the Collection<String>
 	 */
 	public abstract Map<Environment.Component, Collection<String>> desiredEnvironment();
 
     protected Action createAction(Type type, String value) {
         return new Action(this, type, value);
     }
 
     public int getTankNumber() {
         return tankIndex;
     }
 }
