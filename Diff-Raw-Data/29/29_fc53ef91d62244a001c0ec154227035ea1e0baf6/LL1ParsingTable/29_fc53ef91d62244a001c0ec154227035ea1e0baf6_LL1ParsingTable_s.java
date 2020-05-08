 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 
 
 public class LL1ParsingTable {
 	private Map<Variable, List<Terminal>> first;
 	private Map<Variable, List<Terminal>> follow;
 	private Map<RuleElement, Map<TokenType, List<RuleElement>>> table;
 
 	public LL1ParsingTable(LL1Grammar grammar) {
 		first = first(grammar.getRules());
 		System.out.println("First:");
 		for (Variable v : grammar.getVariables()) {
 			System.out.println(v + " " + first.get(v));
 		}
 		System.out.println();
 		
 		System.out.println("Follow:");
 		follow = follow(grammar.getRules(), first, grammar.getStartVariable());
 		for (Variable v : grammar.getVariables()) {
 			System.out.println(v + " " + follow.get(v));
 		}
 		System.out.println();
 	}
 
 	private static Map<Variable, List<Terminal>> first(List<Rule> rules) {
 		HashMap<Variable, List<Terminal>> firstSet = new HashMap<Variable, List<Terminal>>();
 		
 		int i = -1;
 		boolean changed;
 		do {
 			i++;
 			changed = false;
 			
 			for (Rule r : rules) {
 				Variable v = r.getLeftSide();
 				
 				List<Terminal> firstTerminalList;
 				if (firstSet.containsKey(v)) {
 					firstTerminalList = firstSet.get(v);
 				} else {
 					firstTerminalList = new ArrayList<Terminal>();
 				}
 				
 				for (RuleElement re : r.getRightSide()) {
 					if (re instanceof Terminal) {
 						if (!firstTerminalList.contains(re)) {
 							firstTerminalList.add((Terminal)re);
 							changed = true;
 						}
 						break;
 					} else if (i > 0 && re instanceof Variable) {
 						List<Terminal> variableFirstList = firstSet.get(re); 
 						
 						if (variableFirstList != null && variableFirstList.size() > 0) {
 							for (Terminal t : variableFirstList) {
 								if (!firstTerminalList.contains(t)) {
 									firstTerminalList.add(t);
 									changed = true;
 								}
 							}
 							break;
 						}
 					} else {
 						break;
 					}
 				}
 				
 				if (i > 0 && firstTerminalList.size() == 0) {
 					firstTerminalList.add(new EmptyString());
 					changed = true;
 				}
 				
 				if (firstSet.containsKey(v)) {
 					firstSet.remove(v);
 					firstSet.put(v, firstTerminalList);
 				} else {
 					firstSet.put(v, firstTerminalList);
 				}
 			}
 		} while (changed || i < 3);
 		
 		return firstSet;
 	}
 
 	private static Map<Variable, List<Terminal>> follow(List<Rule> rules, Map<Variable, List<Terminal>> first, Variable startVariable) {
 		HashMap<Variable, List<Terminal>> followSet = new HashMap<Variable, List<Terminal>>();
 		
 		List<Terminal> startVariableTerminalList = new ArrayList<Terminal>();
 		startVariableTerminalList.add(new Terminal("$"));
 		followSet.put(startVariable, startVariableTerminalList);
 		
 		boolean changed;
 		do {
 			changed = false;
 			
 			for (Rule r : rules) {				
 				for (int j = 0; j < r.getRightSide().size(); j++) {
 					RuleElement re = r.getRightSide().get(j);
 					if (re instanceof Variable) {
 						Variable v = (Variable)re;
 						
 						List<Terminal> followTerminalList;
 						if (followSet.containsKey(v)) {
 							followTerminalList = followSet.get(v);
 						} else {
 							followTerminalList = new ArrayList<Terminal>();
 						}
 						
 						int k = j + 1;
						while (k < r.getRightSide().size()) {
 							if (r.getRightSide().get(k) instanceof Variable) {
 								RuleElement nextVariable = r.getRightSide().get(k);
 								List<Terminal> firstOfNextVariable = first.get(nextVariable);
 								
 								for (Terminal t : firstOfNextVariable) {
 									if (!(t instanceof EmptyString) && !(followTerminalList.contains(t))) {
 										followTerminalList.add(t);
 										changed = true;
									}
 								}
 							} else if (r.getRightSide().get(k) instanceof Terminal) {
 								Terminal t = (Terminal)r.getRightSide().get(k);
 								if (!followTerminalList.contains(t)) {
 									followTerminalList.add(t);
 									changed = true;
 								}
 							}
							break;
 						}
 						
 						if (followSet.containsKey(v)) {
 							followSet.remove(v);
 							followSet.put(v, followTerminalList);
 						} else {
 							followSet.put(v, followTerminalList);
 						}	
 					}
 				}
 			}
 		} while (changed);
 		
 		return followSet;
 	}
 
 	public List<RuleElement> getRuleElements(RuleElement n, TokenType t) {
 		return null;
 	}
 	
 }
