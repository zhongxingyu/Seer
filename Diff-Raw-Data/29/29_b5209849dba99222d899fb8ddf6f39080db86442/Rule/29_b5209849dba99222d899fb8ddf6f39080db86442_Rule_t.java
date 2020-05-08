 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 
 public class Rule {
 	// Parent grammar
 	private Grammar grammar;
 	// Variable this rule substitutes
 	private Variable variable;
 	// Ordered list of the token classes/variables
 	// Can't be an ordered set because a Rule Item may appear multiple times
 	private List<RuleItem> items;
 	
 	// First set
 	private Set<Terminal> first = new HashSet<Terminal>();
 	// Follow set
 	private Set<Terminal> follow = new HashSet<Terminal>();
 	
 	// Constructors
 	public Rule(Grammar grammar, Variable variable) {
 		super();
 		this.grammar = grammar;
 		this.variable = variable;
 		this.items = new LinkedList<RuleItem>();
 	}
 	
 	// Getters/setters
 	public Grammar getGrammar() {
 		return grammar;
 	}
 	public void setGrammar(Grammar grammar) {
 		this.grammar = grammar;
 	}
 	public Variable getVariable() {
 		return variable;
 	}
 	public void setVariable(Variable variable) {
 		this.variable = variable;
 	}
 	public List<RuleItem> getItems() {
 		return items;
 	}
 	
 	// First/follow sets
 	public boolean addToFirst(Terminal klass) {
		boolean changed = first.add(klass);
		return variable.addToFirst(klass) || changed;
 	}
 	
 	public boolean addAllToFirst(Set<Terminal> klasses) {
		boolean changed = first.addAll(klasses);
		return variable.addAllToFirst(klasses) || changed;
 	}
 	
 	public boolean addToFollow(Terminal klass) {
		boolean changed = follow.add(klass);
		return variable.addToFollow(klass) || changed;
 	}
 	
 	public boolean addAllToFollow(Set<Terminal> klasses) {
		boolean changed = follow.addAll(klasses);
		return variable.addAllToFollow(klasses) || changed;
 	}
 	
 	// Manipulation
 	public void addItem(RuleItem item) {
 		// Add item to end
 		items.add(item);
 	}
 	
 	// Utility
 	public String toString() {
 		// Print out pretty rule here
 		return variable.toString()+" => "+toVarlessString();
 	}
 	
 	public String toVarlessString() {
 		// Print out pretty rule here
 		return items.toString();
 	}
 }
