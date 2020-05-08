 package yuuki.action;
 
 import java.util.ArrayDeque;
 import java.util.Deque;
 import java.util.HashMap;
 import java.util.Map;
 
 import yuuki.content.Mergeable;
 import yuuki.util.InvalidIndexException;
 
 /**
  * Generates Action instances.
  */
 public class ActionFactory implements
 Mergeable<Map<Integer, Action.Definition>> {
 	
 	/**
 	 * The bases for creating instances of Action. Used to get an instance
 	 * factory from a String without using reflections.
 	 */
 	private Map<String, Action> bases;
 	
 	/**
 	 * The definitions in this ActionFactory.
 	 */
 	private Map<Integer, Deque<Action.Definition>> definitions;
 	
 	/**
 	 * Creates a new ActionFactory and the associated base Action instances.
 	 */
 	public ActionFactory() {
 		definitions = new HashMap<Integer, Deque<Action.Definition>>();
 		bases = new HashMap<String, Action>();
 		createBaseActions();
 	}
 	
 	/**
 	 * Adds an action definition.
 	 * 
 	 * @param id The ID of the Action definition to create.
 	 * @param name The class name of the base Action.
 	 * @param args The arguments to the createInstance() method of the base
 	 * Action.
 	 */
 	public void addDefinition(int id, String name, String[] args) {
 		Action.Definition def = new Action.Definition();
 		def.name = name;
 		def.args = args;
 		Deque<Action.Definition> d = definitions.get(id);
 		if (d == null) {
 			d = new ArrayDeque<Action.Definition>();
 			definitions.put(id, d);
 		}
 		d.push(def);
 	}
 	
 	@Override
 	public void merge(Map<Integer, Action.Definition> content) {
 		for (int id : content.keySet()) {
 			Action.Definition def = content.get(id);
 			addDefinition(id, def.name, def.args);
 		}
 	}
 	
 	@Override
 	public void subtract(Map<Integer, Action.Definition> content) {
 		for (int id : content.keySet()) {
 			Action.Definition def = content.get(id);
 			Deque<Action.Definition> d = definitions.get(id);
 			if (d != null) {
 				d.remove(def);
 				if (d.isEmpty()) {
					definitions.remove(d);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Creates an instance of an Action from a definition ID.
 	 * 
 	 * @param id The definition ID of the Action to create.
 	 * 
 	 * @return The newly-created Action instance.
 	 * 
 	 * @throws InvalidIndexException If the given index ID does not exist.
 	 */
 	public Action createAction(int id) throws InvalidIndexException {
 		Deque<Action.Definition> adDeque = definitions.get(id);
 		if (adDeque == null) {
 			throw new InvalidIndexException(id);
 		}
 		Action.Definition ad = adDeque.peek();
 		Action base = bases.get(ad.name);
 		Action actualAction = base.createInstance(ad.args);
 		return actualAction;
 	}
 	
 	/**
 	 * Creates one instance of each concrete subclass of Action and stores it
 	 * in the action bases map indexed under the class' name.
 	 */
 	private void createBaseActions() {
 		bases.put("BasicAttack", new BasicAttack(0));
 		bases.put("BasicDefense", new BasicDefense(0));
 		bases.put("Flee", new Flee());
 		bases.put("ItemUse", new ItemUse());
 	}
 	
 }
