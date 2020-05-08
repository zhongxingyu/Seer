 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Scanner;
 import java.util.Set;
 
 
 public class Grammar {
 	// Map from variable to the set of rules
 	private Map<Variable,Set<Rule>> map = new HashMap<Variable, Set<Rule>>();
 	// Start variable
 	private Variable start;
 	// Parser
 	private Parser parser;
 	
 	// Ordered list of variables for nicer output
 	private List<Variable> orderedVars;
 	
 	// Special characters for parsing
 	private static final char VAR_START = '<';
 	private static final char VAR_END = '>';
 	private static final char NEW_RULE = '|';
 	private static final char ASSIGN_START = ':';
 	private static final String ASSIGN = "::=";
 	
 	// Constructors
 	public Grammar() {
 		super();
 	}
 	
 	// Create grammar from files
 	public Grammar(Scanner grammar, Scanner spec) {
 		super();
 		
 		orderedVars = new ArrayList<Variable>();
 		
 		// Build scanner first
 		parser = new Parser();
 		parser.build(spec);
 
 		Variable currVar = null;
 		Rule currRule = null;
 		
 		int i = 0;
 		while(grammar.hasNextLine()){
             InputStream is = new InputStream(grammar.nextLine());
             
             while(!is.isConsumed()) {
 	            switch(is.peekToken()) {
 	            case VAR_START:
 	            	// It's <variable>; make new var (if it doesn't exist in map.keys())
 	            	// If the label == Variable.EPSILON, make epsilon variable instead.
 	            	// Add to the current rule.
 	            	is.matchToken(VAR_START);
 	            	
 	            	String varName = is.peekTill(VAR_END);
 	            	RuleItem item = new Variable(this, varName);
 	            	
 	            	if (varName.equals(EpsilonTerminal.EPSILON)) {
 	            		if (currRule != null) {
 	            			item = new EpsilonTerminal(this);
 	            		}
 	            	} else {
 		            	// Do variable instantiation stuff here
 		            	if (map.containsKey(item)) {
 		            		// Variable already instantiated; use it
 		            		Variable varMatch = null;
 		            		for (Variable v : map.keySet()) {
 		            			if (v.equals(item)) {
 		            				varMatch = v;
 		            				break;
 		            			}
 		            		}
 		            		
 		            		// Ditch the other one
 		            		item = varMatch;
 		            	} else {
 		            		// Not made; add it to map
 		            		map.put((Variable)item, new HashSet<Rule>());
 		            	}
 	            	}
 	            	
 	            	// See if we are working on rules
 	            	if (currVar == null) {
 	            		// First variable on line
 	            		currVar = (Variable)item;
 	            		
 	            		if (!orderedVars.contains(currVar))
 	            			orderedVars.add(currVar);
 	            		
 	            		if (i == 0) start = currVar;
 	            	} else if (currRule != null) {
 	            		// Working on rule; add
 	            		currRule.addItem(item);
 	            	}
 	            	
 	            	// Consume variable and VAR_END
 	            	is.matchToken(varName);
 	            	is.matchToken(VAR_END);
 	            	
 	            	break;
 	            case NEW_RULE:
 	            	// We found a | so start on the new rule
 	            	// Make new rule, assign to map, and start over
 	            	currRule = new Rule(this, currVar);
 	            	addRule(currRule);
 	            	
 	            	// Consume symbol
 	            	is.matchToken(NEW_RULE);
 	            	
 	            	break;
 	            case ASSIGN_START:
 	            	// Could it be ::=? Try it! Same behavior as NEW_RULE.
 	            	if (is.peekToken(ASSIGN)) {
 	            		currRule = new Rule(this, currVar);
 	            		addRule(currRule);
 	            		
 	            		// Consume string
 	            		is.matchToken(ASSIGN);
 	            	} else {
 	            		invalid(is);
 	            	}
 	            	
 	            	break;
 	            default:
 	            	// Could be a TokenClass like BEGIN, or a matching token like 'begin'
 	            	// Try matching TokenClass first. As a last resort, try scanToken.
 	            	String string = is.peekTillSpace();
 	            	TokenClass klass = parser.findTokenClassIn(string);
 	            	
 	            	if (klass != null) {
 	            		// It's a valid TokenClass, like BEGIN
 	            		// Add to current rule
 	            		currRule.addItem(klass);
 		            	
 	            		// Consume TokenClass
		            	is.matchToken(klass.getName());
 	            	} else {
 	            		// See if any token classes match
 	            		Token token = parser.scanToken(is.peekTillEnd());
 	            		
 	            		if (token != null) {
 	            			// It's a valid string from TokenClass, like begin
 	            			// Add to current rule
 	            			currRule.addItem(token.getKlass());
 	            			
 	            			// Consume the text that matched
 	            			is.matchToken(token.getString());
 	            		} else {
 	            			// That didn't work either, invalid!
 	            			invalid(is);
 	            		}
 	            	}
 	            	
 	            	break;
 	            }
 	            
 	            is.skipWhitespace();
             }
     		
     		// Reset currVariable and currRule.
     		currVar = null;
     		currRule = null;
     		i++;
 		}
 	}
 
 	// Getters/setters
 	public Variable getStart() {
 		return start;
 	}
 
 	public void setStart(Variable start) {
 		this.start = start;
 	}
 	
 	public Set<Variable> getVariables() {
 		return map.keySet();
 	}
 	
 	public List<Variable> getOrderedVars() {
 		return orderedVars;
 	}
 
 	public Parser getParser() {
 		return parser;
 	}
 	
 	// Traversal
 	public Set<Rule> getRulesFor(Variable v) {
 		return map.get(v);
 	}
 	
 	// Get all rules
 	public Set<Rule> getRules() {
 		Set<Rule> rules = new HashSet<Rule>();
 		
 		for (Set<Rule> ruleSet : map.values()) {
 			rules.addAll(ruleSet);
 		}
 		
 		return rules;
 	}
 	
 	// Manipulation
 	public void addRule(Rule r) {
 		r.setGrammar(this);
 		
 		// Does the map already have the variable?
 		if (map.containsKey(r.getVariable())) {
 			map.get(r.getVariable()).add(r);
 		} else {
 			Set<Rule> rules = new HashSet<Rule>();
 			rules.add(r);
 			map.put(r.getVariable(), rules);
 		}
 	}
 	
 	// Utility
 	public String toString() {
 		// Print out pretty grammar here
 		String s = "";
 		for (Entry<Variable, Set<Rule>> entry : map.entrySet()) {
 			s += entry.getKey();
 			int i = 0;
 			for (Rule rule : entry.getValue()) {
 				if (i == 0)
 					s += " ::= ";
 				else
 					s += " | ";
 				
 				s += rule.toVarlessString();
 				i++;
 			}
 			s += "\n";
 		}
 		
 		return s;
 	}
 	
 	public void invalid(InputStream is) {
 		System.out.println("Couldn't parse grammar spec.");
 		System.out.println(is.toString());
 		System.exit(0);
 	}
 	
 	public void calculateFirstSets() {
 		boolean hasChanged = false;
 		do{
 			hasChanged = false;
 			for(Variable variable : map.keySet()){
 				int size = map.get(variable).size();
 				for(Rule rule : map.get(variable)){
 					int k = 0;
 					boolean cont = true;
 					List<RuleItem> items = rule.getItems();
 					size = items.size();
 					while(cont == true && k < size){
 						Set<Terminal> newFirst = items.get(k).getFirst();
 						boolean containsE = false;
 						for(Terminal term : newFirst){
 							if(term.isEpsilon()){
 								containsE = true;
 								newFirst.remove(term);
 							}
 						}
 						if(rule.addAllToFirst(newFirst))
 							hasChanged = true;
 						if(!containsE)
 							cont = false;
 						k++;
 					}
 					if(cont == true){
 						if(rule.addToFirst(new EpsilonTerminal(this)))
 							hasChanged = true;
 					}
 				}
 			}
 		} while(hasChanged);
 	}
 	public void calculateFollowSets() {
 		start.addToFollow(new DollarTerminal(this));	
 		boolean hasChanged = false;
 		do{
 			hasChanged = false;
 			for(Variable variable : map.keySet()){
 				for(Rule rule : map.get(variable)){
 					List<RuleItem> items = rule.getItems();
 					for(int i=0; i<items.size(); i++){
 						RuleItem item = items.get(i);
 						if(item.isVariable()){
 							Set<Terminal> newFirst = new HashSet<Terminal>();
 							for(int j=i+1; j<items.size(); j++){
 								Set<Terminal> currSet = items.get(j).getFirst();
 								newFirst.addAll(currSet);
 								if(!currSet.contains(new EpsilonTerminal(this)))
 									break;
 							}
 							boolean containsE = newFirst.remove(new EpsilonTerminal(this));
 							if(((Variable)item).addAllToFollow(newFirst)){
 								hasChanged = true;
 							}
 							if(containsE || i == (items.size()-1)){
 								Set<Terminal> newFollow = variable.getFollow();
 								if(((Variable)item).addAllToFollow(newFollow))
 									hasChanged = true;
 							}
 						}
 					}
 				}
 			}
 		} while(hasChanged);
 	}
 }
