 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Scanner;
 
 /**
  * LL1 Grammar made up with a list of Terminals, list of Variables, list of Rules, and a start Variable.
  */
 public class LL1Grammar {
 	private static Scanner grammarScanner;
 	private ArrayList<Terminal> terminals;
 	private ArrayList<Variable> variables;
 	private Variable startVariable;
 	private List<Rule> rules;
 	
 	/**
 	 * Construct a new LL1 Grammar by parsing the description of the grammar.
 	 * @param grammarDescription File which contains the description of the grammar.
 	 */
 	public LL1Grammar(File grammarDescription) throws LL1GrammarException {
 		try {
 			grammarScanner = new Scanner(grammarDescription);
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 		
 		int lineCount = 1;
 		
 		// Parse through each line of the grammar description file
 		while (grammarScanner.hasNext()) {
 			String[] line = grammarScanner.nextLine().split(" ");
 			
 			if (line[0].equalsIgnoreCase("%Tokens")) {
 				// If we encounter the %Tokens line, generate the list of Tokens
 				terminals = new ArrayList<Terminal>();
 				
 				for (int i = 1; i < line.length; i++) {
 					terminals.add(new Terminal(line[i]));
 				}
 				terminals.add(new Terminal("$"));
 			} else if (line[0].equalsIgnoreCase("%Non-terminals")) {
 				// If we encounter the %Non-Terminals line, generate the list of Variables
 				variables = new ArrayList<Variable>();
 				
 				for (int i = 1; i < line.length; i++) {
 					variables.add(new Variable(line[i]));
 				}
 			} else if (line[0].equalsIgnoreCase("%Start")) {
 				// If we encounter the %Start line, set the start Variable
 				startVariable = variableForIdentifier(line[1]);
 				
 				if (startVariable == null) {
					throw new LL1GrammarException("Start variable was not declared");
 				}
 			} else if (line[0].equalsIgnoreCase("%Rules")) {
 				// Once we encounter %Rules, start creating the rules
 				rules = new ArrayList<Rule>();
 				
 				while (grammarScanner.hasNext()) {
 					lineCount++;
 					String[] ruleLine = grammarScanner.nextLine().split(" ");
 					
 					if (ruleLine[0].charAt(0) != '<') {
 						throw new LL1GrammarException("Unexpected " + ruleLine[0] + " on line " + lineCount);
 					}
 					
 					Variable leftSide = variableForIdentifier(ruleLine[0]);
					
					if (leftSide == null) {
						throw new LL1GrammarException("Variable " + ruleLine[0] + " referenced on line " + lineCount + " was not declared");
					}
					
 					ArrayList<RuleElement> rightSide = new ArrayList<RuleElement>();
 					// Start at two two skip colon
 					for (int i = 2; i < ruleLine.length; i++) {
 						if (ruleLine[i].equals("|")) {
 							// New rule because of OR
 							rules.add(new Rule(leftSide, rightSide));
 							rightSide = new ArrayList<RuleElement>();
 						} else {
 							rightSide.add(ruleElementForIdentifier(ruleLine[i]));
 						}
 					}
 					
 					rules.add(new Rule(leftSide, rightSide));
 				}
 			} else {
 				throw new LL1GrammarException("Unexpected " + line[0] + " on line " + lineCount);
 			}
 			
 			lineCount++;
 		}
 		
 		// Remove left recursion and left factoring
 		removeLeftRecursion();
 		removeLeftFactoring();
 	}
 	
 	/**
 	 * Remove left recursion from the grammar.
 	 */
 	public void removeLeftRecursion() {
 		ArrayList<Rule> newRules = new ArrayList<Rule>();
 		
 		for (Rule rule : rules) {
 			if (rule.hasLeftRecursion()) {		
 				// Only applies to rules that have left recursion
 				
 				// Create a new rule with a similar left side as the old rule
 				String tailVariableIdentifier = rule.getLeftSide().getIdentifier().replace(">", "_tail>");
 				Variable tail = new Variable(tailVariableIdentifier);
 				variables.add(tail);
 
 				// Add RuleElements from the old rule's right side that are the non-recursive part (i.e. start from the 2nd rule)
 				List<RuleElement> tailRuleRightSide = new ArrayList<RuleElement>();
 				for (int i = 1; i < rule.getRightSide().size(); i++) {
 					tailRuleRightSide.add(rule.getRightSide().get(i));
 				}
 				
 				tailRuleRightSide.add(tail);
 				
 				Rule tailRule = new Rule(tail, tailRuleRightSide);
 				Rule tailEpsilonRule = new Rule(tail, new EmptyString());
 				
 				newRules.add(tailRule);
 				newRules.add(tailEpsilonRule);
 				
 				List<Rule> siblingRules = rulesWithLeftSide(rule.getLeftSide());
 				
 				for (Rule sibling : siblingRules) {
 					sibling.addToRightSide(tail);
 				}
 			} else {
 				// If the rule does not have left recursion, keep it around
 				newRules.add(rule);
 			}
 		}
 		
 		rules = newRules;
 	}
 	
 	/**
 	 * Remove left factoring from the grammar.
 	 */
 	public void removeLeftFactoring() {
 		ArrayList<Rule> newRules = new ArrayList<Rule>();
 		ArrayList<Rule> processedRules = new ArrayList<Rule>();
 		
 		for (Rule rule : rules) {
 			if (processedRules.contains(rule)) {
 				continue;
 			}
 			
 			// Find all rules where the left side is the same
 			ArrayList<Rule> similarRules = new ArrayList<Rule>();
 			for (Rule aRule : rules) {
 				if (aRule.getLeftSide().equals(rule.getLeftSide())) {
 					similarRules.add(aRule);
 				}
 			}
 			
 			// Find all rules from the left side set that have the same first right side variable / terminal
 			ArrayList<Rule> commonRules = new ArrayList<Rule>();
 			for (Rule aRule : similarRules) {
 				for (Rule bRule : similarRules) {
 					if (!bRule.equals(aRule)) {
 						if (aRule.getRightSide().get(0).equals(bRule.getRightSide().get(0))) {
 							if (!commonRules.contains(aRule)) {
 								commonRules.add(aRule);
 							}
 							
 							commonRules.add(bRule);
 						}
 					}
 				}
 				
 				if (commonRules.size() > 0) {
 					break;
 				}
 			}
 			
 			if (commonRules.size() > 0) {				
 				Variable leftSideVariable = commonRules.get(0).getLeftSide();
 				
 				// Create a new master rule that contains the common right side value plus the newVariable
 				Variable newVariable = new Variable(leftSideVariable.toString() + "'");
 				variables.add(newVariable);
 				
 				ArrayList<RuleElement> rightSideList = new ArrayList<RuleElement>();
 				rightSideList.add(commonRules.get(0).getRightSide().get(0));
 				rightSideList.add(newVariable);
 				
 				Rule newMasterRule = new Rule(leftSideVariable, rightSideList);
 				newRules.add(newMasterRule);
 												
 				// Create the newVariable rule(s) for all the commonRules (minus the common right side value)
 				for (Rule aRule : commonRules) {
 					rightSideList = new ArrayList<RuleElement>();
 
 					List<RuleElement> aRuleRightSide = aRule.getRightSide();
 					if (aRuleRightSide.size() == 1) {
 						rightSideList.add(new EmptyString());
 					} else {
 						for (int i = 1; i < aRuleRightSide.size(); i++) {
 							rightSideList.add(aRuleRightSide.get(i));
 						}
 					}
 					
 					Rule newVariableRule = new Rule(newVariable, rightSideList);
 					newRules.add(newVariableRule);
 				}
 								
 				processedRules.addAll(commonRules);
 			} else {
 				newRules.add(rule);
 			}
 		}
 		
 		rules = newRules;
 	}
 	
 	/**
 	 * Determine Rules with the same left side Variable.
 	 * @param v Variable to test
 	 * @return Rules that have the same left side as the Variable given
 	 */
 	public List<Rule> rulesWithLeftSide(Variable v) {
 		ArrayList<Rule> rulesWithLeftSide = new ArrayList<Rule>();
 		
 		for (Rule rule : rules) {
 			if (rule.getLeftSide() == v) {
 				rulesWithLeftSide.add(rule);
 			}
 		}
 		
 		return rulesWithLeftSide;
 	}
 	
 	/**
 	 * Determine the RuleElement for a String identifier.
 	 * @param identifier String identifier
 	 * @return RuleElement that matches the given identifier
 	 */
 	public RuleElement ruleElementForIdentifier(String identifier) {
 		RuleElement re = variableForIdentifier(identifier);
 		if (re == null) {
 			re = terminalForIdentifier(identifier);
 		}
 		
 		return re;
 	}
 	
 	/**
 	 * Return the Variable that matches the given String identifier.
 	 * @param identifier String identifier
 	 * @return Variable that has the same identifier as the one given
 	 */
 	public Variable variableForIdentifier(String identifier) {
 		for (Variable v : variables) {
 			if (v.getIdentifier().equalsIgnoreCase(identifier)) {
 				return v;
 			}
 		}
 		
 		return null;
 	}
 	
 	/**
 	 * Return the Terminal that matches the given String identifier.
 	 * @param identifier String identifier
 	 * @return Terminal that has the same identifier as the one given
 	 */
 	public Terminal terminalForIdentifier(String identifier) {
 		for (Terminal t : terminals) {
 			if (t.getIdentifier().equalsIgnoreCase(identifier)) {
 				return t;
 			}
 		}
 		
 		return null;
 	}
 
 	/**
 	 * Return the start Variable.
 	 * @return Start Variable
 	 */
 	public Variable getStartVariable() {
 		return startVariable;
 	}
 
 	/**
 	 * Return the list of Variables.
 	 * @return List of Variables
 	 */
 	public List<Variable> getVariables() {
 		return variables;
 	}
 
 	/**
 	 * Return the list of Rules.
 	 * @return List of Rules
 	 */
 	public List<Rule> getRules() {
 		return rules;
 	}
 
 	/**
 	 * Return the list of Terminals.
 	 * @return List of Terminals
 	 */
 	public List<Terminal> getTerminals() {
 		return terminals;
 	}
 	
 	@Override
 	public String toString() {
 		StringBuilder sb = new StringBuilder();
 		
 		sb.append("Terminals: ");
 		for (Terminal t : terminals) {
 			sb.append(t + " ");
 		}
 		sb.append("\n");
 		
 		sb.append("Variables: ");
 		for (Variable v : variables) {
 			sb.append(v + " ");
 		}
 		sb.append("\n");
 
 		sb.append("Start Variable: ");
 		sb.append(startVariable);
 		sb.append("\n");
 		
 		sb.append("Rules:\n");
 		for (Rule r : rules) {
 			sb.append(r);
 			sb.append("\n");
 		}
 		
 		return sb.toString();
 	}
 
 	public Terminal getEndTerminal() {
 		for (Terminal t : terminals) {
 			if (t.getIdentifier().equals("$")) {
 				return t;
 			}
 		}
 		
 		return null;
 	}
 }
