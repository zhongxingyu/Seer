 package vitro.controller;
 
 import vitro.util.*;
 import vitro.model.*;
 import java.util.*;
 
 public abstract class Controller {
 	
 	public final Model model;
 
 	protected final Map<Class, Agent> classAgents = new HashMap<Class, Agent>();
 	protected final Map<Actor, Agent> actorAgents = new HashMap<Actor, Agent>();
 
 	private List<List<Action>> history = new ArrayList<List<Action>>();
 	private int cursor = 0;
 
 	public Controller(Model model) {
 		this.model = model;
 	}
 
 	public void bind(Class c, Agent agent) {
 		classAgents.put(c, agent);
 	}
 
 	public void bind(Actor actor, Agent agent) {
 		actorAgents.put(actor, agent);
 	}
 
 	// As far as I can tell, there is no type-safe way to extract Agents
 	// from these maps. Even if I made the map signature like:
 	//   Map<Actor, Agent<A extends Actor>>
 	// there wouldn't be any way to ensure that the Agent generic type
 	// matches the Actor generic type. Having generic agents is
 	// desirable because it saves a cast in every implementation.
 	// If there's a better way to do this, fix it.
 	@SuppressWarnings("unchecked")
 	protected <A extends Actor> Agent<A> getAgent(A a) {
 		if (actorAgents.containsKey(a))            { return (Agent<A>)actorAgents.get(a); }
 		if (classAgents.containsKey(a.getClass())) { return (Agent<A>)classAgents.get(a.getClass()); }
 		return null;
 	}
 
 	protected <A extends Actor> Action getAction(A a) {
 		Set<Action> actions = a.actions();
 		Agent<A> agent = getAgent(a);
 		if (actions.size() >= 1 && agent != null) {
 			Action choice = agent.choose(a, Collections.unmodifiableSet(actions));
 			// If the agent returns a malicious Action, using
 			// equals() or hashCode() to confirm it was one of the
 			// available choices may allow it to slip through
 			// the cracks. Thus, we perform reference comparisons:
 			if (choice == null) { return null; }
 			for(Action action : actions) {
 				if (action == choice) { return action; }
 			}
 			throw new Error("Agent selected an invalid choice.");
 		}
 		return Groups.first(actions);
 	}
 
 	public void flush() {
 		while(history.size() > cursor) {
 			history.remove(history.size()-1);
 		}
 	}
 
 	public boolean hasNext() {
		return !model.done();
 	}
 
 	public boolean hasPrev() {
 		return cursor > 0;
 	}
 
 	public void next() {
 		if (!hasNext()) { return; }
		
		synchronized(model) {
 			// generate a new round:
 			if (cursor == history.size()) {
 				history.add(nextRound());
 				cursor =  history.size();
 				return;
 			}
 
 			// replay a historical round:
 			List<Action> actions = history.get(cursor);
 			for(int x = 0; x < actions.size(); x++) {
 				actions.get(x).apply();
 			}
 			cursor++;
 		}
 	}
 
 	public void prev() {
 		if (!hasPrev()) { return; }
 
 		synchronized(model) {
 			cursor--;
 			List<Action> actions = history.get(cursor);
 			for(int x = actions.size() - 1; x >= 0; x--) {
 				actions.get(x).undo();
 			}
 		}
 	}
 
 	protected abstract List<Action> nextRound();
}
