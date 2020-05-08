 package vitro;
 
 import vitro.util.*;
 import java.util.*;
 
 public abstract class Controller {
 	
 	public final Model model;
 
 	protected final Set<Agent> agents = new HashSet<Agent>();
 	protected final Map<Class, Agent> classAgents = new HashMap<Class, Agent>();
 	protected final Map<Actor, Agent> actorAgents = new HashMap<Actor, Agent>();
 
 	private List<List<Action>>           history   = new ArrayList<List<Action>>();
 	private List<Map<Annotation, Agent>> footnotes = new ArrayList<Map<Annotation, Agent>>();
 	private int cursor = 0;
 
 	public Controller(Model model) {
 		this.model = model;
 	}
 
 	public void bind(Class c, Agent agent) {
 		classAgents.put(c, agent);
 		agents.add(agent);
 	}
 
 	public void bind(Actor actor, Agent agent) {
 		actorAgents.put(actor, agent);
 		agents.add(agent);
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
 
 	public boolean hasNext() {
 		synchronized(model) {
 			return !model.done();
 		}
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
 				Map<Annotation, Agent> annotations = new HashMap<Annotation, Agent>();
 				for(Agent agent : agents) {
 					if (agent instanceof Annotated) {
 						for(Annotation a : ((Annotated)agent).annotations()) {
 							annotations.put(a, agent);
 						}
 					}
 				}
 				footnotes.add(annotations);
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
 
 	public Map<Annotation, Agent> annotations() {
 		synchronized(model) {
 			if (cursor < 1) { return new HashMap<Annotation, Agent>(); }
 			return footnotes.get(cursor-1);
 		}
 	}
 
 	public List<Action> previousActions() {
 		return history.get(cursor - 1);
 	}
 
 	protected List<Actor> actors() {
 		if (!(model instanceof Factional)) {
 			return new ArrayList<Actor>(model.actors);
 		}
 		// If we're interacting with a Factional model,
 		// only neutral actors and actors from the current
 		// team should have a chance to act:
 		List<Actor> ret = new ArrayList<Actor>();
 		int currentTeam = ((Factional)model).team();
 		for(Actor a : model.actors) {
 			if (a instanceof Factional) {
 				if (((Factional)a).team() != currentTeam) { continue; }
 			}
 			ret.add(a);
 		}
 		return ret;
 	}
 
 	protected abstract List<Action> nextRound();
 }
