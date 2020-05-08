 package grammar;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class Grammar {
 
 	public static final char EPSILON = 'E';
 
 	List<String> nonterminals = new ArrayList<String>();
 	List<String> alphabet = new ArrayList<String>();
 	Map<String, List<String>> productions = new HashMap<String, List<String>>();
 	String startingSymbol;
 
 	public Grammar() {
 
 	}
 
 	/**
	 * Get the set of non terminal symbols
 	 * 
 	 * @return - the set of non terminal symbols
 	 */
 	public List<String> getNonterminals() {
 		return nonterminals;
 	}
 
 	/**
 	 * Get the alphabet
 	 * 
 	 * @return - the set of terminal symbols
 	 */
 	public List<String> getAlphabet() {
 		return alphabet;
 	}
 
 	/**
 	 * Get the set of productions
 	 * 
 	 * @return
 	 */
 	public Map<String, List<String>> getProductions() {
 		return productions;
 	}
 
 	/**
 	 * Return the list of production for a non-terminal symbol
 	 * 
 	 * @param nonTerminal
 	 *            - the non-terminal symbol
 	 * @return - it's list of production
 	 */
 	public List<String> getProduction(String nonTerminal) {
 		return productions.get(nonTerminal);
 	}
 
 	/**
 	 * Get the starting symbol
 	 * 
 	 * @return - the starting symbol
 	 */
 	public String getStartingSymbol() {
 		return startingSymbol;
 	}
 
 	/**
 	 * Add the set of non terminal symbols
 	 * 
 	 * @param non
 	 *            - the set of non terminal symbols
 	 */
 	public void addNonterminals(String[] non) {
 		nonterminals.clear();
 		nonterminals.addAll(Arrays.asList(non));
 	}
 
 	/**
 	 * Add the alphabet
 	 * 
 	 * @param alp
 	 *            - the set of terminal symbols
 	 */
 	public void addAlphabet(String[] alp) {
 		alphabet.clear();
 		alphabet.addAll(Arrays.asList(alp));
 	}
 
 	/**
 	 * Set the starting symbol
 	 * 
 	 * @param startingSymbol
 	 */
 	public void setStartingSymbol(String startingSymbol) {
 		this.startingSymbol = startingSymbol;
 	}
 
 	/**
 	 * Add a single production of form nonterm->prod
 	 * 
 	 * @param nonterm
 	 *            - the nonterminal symbol
 	 * @param prod
 	 *            - the production
 	 */
 	public void addProduction(String nonterm, String prod) {
 		if (productions.containsKey(nonterm)) {
 			productions.get(nonterm).add(prod);
 		} else {
 			productions.put(nonterm, new ArrayList<String>());
 			productions.get(nonterm).add(prod);
 		}
 	}
 
 	/**
 	 * Add productions of form nonterm->prod|prod
 	 * 
 	 * @param prods
 	 *            - array of productions
 	 */
 	public void addProductions(String[] prods) {
 		productions.clear();
 		for (String prod : prods) {
 			String lhs = prod.split("->")[0];
 			String rhs = prod.split("->")[1];
 
 			for (String pr : rhs.split("\\|")) {
 				addProduction(lhs, pr);
 			}
 		}
 	}
 
 	/**
 	 * Read from file of format first line: non terminal symbols, space
 	 * separated second line: alphabet, space separated third line: production
 	 * rules separated by spaces fourth line: the starting symbol
 	 * 
 	 * @param fileName
 	 *            - the file to read from
 	 */
 	public void loadFromFile(String fileName) throws Exception {
 
 		BufferedReader reader = new BufferedReader(new FileReader(fileName));
 		String[] line = reader.readLine().split(" ");
 		addNonterminals(line);
 
 		line = reader.readLine().split(" ");
 		addAlphabet(line);
 
 		line = reader.readLine().split(" ");
 		addProductions(line);
 
 		startingSymbol = reader.readLine();
 
 		reader.close();
 
 	}
 
 	/*
 	 * Verify if a grammar is right linear A grammar is said to be right linear
 	 * if every productions has the form A->a or A->aB , where A,B are
 	 * nonterminals and a is a symbol from alphabet
 	 */
 	public boolean isRightLinear() {
 		for (String leftHandSide : productions.keySet()) {
 
 			for (String production : productions.get(leftHandSide)) {
 				// when: A->a
 				// ignore E
 				if (production.length() == 1) {
 					if (!alphabet.contains(String.valueOf(production.charAt(0))) && production.charAt(0) != EPSILON) {
 						return false;
 					}
 					// when: A->aB
 				} else if (production.length() == 2) {
 					if (!alphabet.contains(String.valueOf(production.charAt(0)))
 							|| !nonterminals.contains(production.substring(1, 2))) {
 						return false;
 					}
 				} else {
 					return false;
 				}
 			}
 
 		}
 		return true;
 	}
 
 	/**
 	 * Verifies if the given nonterminal is present in the right hand side of
 	 * any production
 	 * 
 	 * @return - true if a non terminal symbol is in the right hand side of any
 	 *         production in P.
 	 */
 	private boolean isInRHSofAnyProduction(String nonTerminal) {
 		for (String lhp : productions.keySet()) {
 
 			for (String prod : productions.get(lhp)) {
 				if (prod.contains(nonTerminal)) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * 
 	 * Verify if a grammar is regular
 	 * 
 	 * A grammar is said to be regular if 1. It's right-linear 2. For any
 	 * production S->E S is not present in the RHS of any production
 	 * 
 	 * @return -boolean if the grammar is regular
 	 */
 	public boolean isRegular() {
 		for (String lhp : productions.keySet()) {
 			for (String prod : productions.get(lhp)) {
 				if (prod.charAt(0) == EPSILON) {
 					if (lhp.equals(startingSymbol) && isInRHSofAnyProduction(startingSymbol)) {
 						return false;
 					}
 					if (!lhp.equals(startingSymbol)) {
 						return false;
 					}
 				}
 			}
 		}
 		return isRightLinear();
 	}
 
 	/*
 	 * Convert grammar to the corresponding finite automata
 	 */
 	public FiniteAutomata toFiniteAutomata() {
 
 		// Has to be regular, to be able to convert it
 		if (!isRegular()) {
 			return null;
 		}
 
 		FiniteAutomata finiteAutomata = new FiniteAutomata();
 
 		// Final state that will be added as a complementary state
 		String finalState = "K";
 
 		// Add states
 		finiteAutomata.addStates(Arrays.asList("K").toArray(new String[0]));
 		finiteAutomata.addStates(nonterminals.toArray(new String[0]));
 		// nonterminals.remove(finalState);
 
 		// Add alphabet
 		finiteAutomata.addAlphabet(alphabet.toArray(new String[0]));
 
 		// Add final states
 		List<String> finals = new ArrayList<String>();
 
 		// Final states are composed of finalState + S if we have
 		// S->E
 		finals.add(finalState);
 
 		for (String prod : productions.get(startingSymbol)) {
 			if (prod.equals(String.valueOf(EPSILON))) {
 				finals.add(startingSymbol);
 			}
 		}
 
 		finiteAutomata.addFinalStates(finals.toArray(new String[0]));
 
 		// convert productions after the following rules:
 		// A->a becomes (A,a)=finalState
 		// A->aB becomes (A,a)=B
 		for (String nonterminal : productions.keySet()) {
 			for (String production : productions.get(nonterminal)) {
 
 				// case 1: A->a , ignore E(epsilon)
 				if (production.length() == 1 && production.charAt(0) != EPSILON) {
 					finiteAutomata.addTransition(new Pair(nonterminal, production), finalState);
 					// case 2: A->aB
 				} else if (production.length() == 2) {
 					finiteAutomata.addTransition(new Pair(nonterminal, String.valueOf(production.charAt(0))),
 							String.valueOf(production.charAt(1)));
 				}
 			}
 		}
 
 		finiteAutomata.setInitialState(startingSymbol);
 
 		return finiteAutomata;
 	}
 
 	/**
 	 * Verify if context free A context-free grammar is a grammar in which every
 	 * production rule is of the form V -> w, where V is a single nonterminal
 	 * symbol, and w is a string of terminals and/or nonterminals (w can be
 	 * empty).
 	 * 
 	 * @return - true if the grammar is context free
 	 */
 
 	public boolean isContextFree() {
 
 		// We verify that the left side of each production is a single
 		// nonterminal symbol
 		for (String nonterminal : productions.keySet()) {
 			for (String production : productions.get(nonterminal)) {
 
 				System.out.println("Verifying:" + nonterminal + "->" + production);
 
 				if (nonterminal.length() != 1) {
 					System.out.println("Failed verification, not context free!");
 					return false;
 				}
 			}
 		}
 
 		return true;
 	}
 
 	// 1 - eliminate unproductive symbols
 	/**
 	 * Non-terminal is non-productive if it cannot generate any string of
 	 * terminals if X::=w and w has only terminals, then X is productive if X:=p
 	 * and p has only productive symbols, then X is productive
 	 */
 	public void eliminateUnproductiveSymbols() {
 		System.out.println("Eliminating unproductive symbols");
 
 		List<String> productiveSymbols = new ArrayList<String>();
 		boolean prodSymbolSetChanged = true;
 
 		// We need to stop when the set of productive symbols doesn't
 		while (prodSymbolSetChanged) {
 
 			prodSymbolSetChanged = false;
 
 			for (String nonterminal : productions.keySet()) {
 
 				System.out.println("Checking if " + nonterminal + " is productive");
 				// change in an iteration
 				boolean onlyTerminals = true;
 				boolean onlyProdSymbols = true;
 
 				// For each production of a terminal we check that all the
 				// symbols
 				// in the production are either terminal or productive
 				onlyTerminals = true;
 				onlyProdSymbols = true;
 
 				// Verify each production
 				for (String production : productions.get(nonterminal)) {
 					System.out.println("Production:" + production);
 					for (char sym : production.toCharArray()) {
 						if (!alphabet.contains(String.valueOf(sym))) {
 							onlyTerminals = false;
 							if ((!productiveSymbols.contains(String.valueOf(sym))) && (sym != EPSILON)) {
 								onlyProdSymbols = false;
 							}
 						}
 					}
 				}
 
 				if (onlyTerminals || onlyProdSymbols) {
 					System.out.println("Adding " + nonterminal + " as productive.");
 					if (!productiveSymbols.contains(nonterminal)) {
 						productiveSymbols.add(nonterminal);
 						prodSymbolSetChanged = true;
 					}
 				}
 			}
 
 		}
 
 		for (String nonTerminal : nonterminals) {
 			if (!productiveSymbols.contains(nonTerminal)) {
 				eliminateSymbol(nonTerminal);
 			}
 		}
 	}
 
 	private void eliminateSymbol(String symbol) {
 		nonterminals.remove(symbol);
 		productions.remove(symbol);
 
 		Map<String, List<String>> toEliminate = new HashMap<String, List<String>>();
 
 		for (String nonterminal : productions.keySet()) {
 			for (String production : productions.get(nonterminal)) {
 				if (production.contains(symbol)) {
 
 					if (!toEliminate.containsKey(nonterminal)) {
 						toEliminate.put(nonterminal, new ArrayList<String>());
 					}
 					toEliminate.get(nonterminal).add(production);
 				}
 			}
 		}
 
 		for (String nonterminal : toEliminate.keySet()) {
 			for (String production : toEliminate.get(nonterminal)) {
 				productions.get(nonterminal).remove(production);
 				if (productions.get(nonterminal).isEmpty()) {
 					productions.remove(nonterminal);
 				}
 			}
 		}
 
 	}
 
 	/*
 	 * Remove all unit rules of the form A -> B . Whenever a rule B->u
 	 * appears, add the rule A->u.  u may be a string of variables and
 	 * terminals  Repeat until all unit rules are eliminated
 	 */
 	public void eliminateSingleProductions() {
 		System.out.println("Eliminating single productions:");
 
 		Map<String, List<String>> newProductions = new HashMap<String, List<String>>();
 		boolean containsSingleProduction = true;
 
 		while (containsSingleProduction) {
 			containsSingleProduction = false;
 
 			for (String nonterminal : productions.keySet()) {
 				
 				//Construct the list of all reachable symbols
 				List<String> values = new ArrayList<String>();
 				//Copy the initial list of symbols
 				for (String prod : productions.get(nonterminal)) {
 					values.add(prod);
 				}
 
 				//We iterate over ALLL the productions
 				for (String production : productions.get(nonterminal)) {
 					//Check if has form A->B
 					if ((production.length() == 1) && (!alphabet.contains(production)) && (!production.equals(String.valueOf(EPSILON)))) {
 						if (nonterminal.length() == 1) {
 
 							// We have a production of form A->B
 							System.out.println("Found:" + nonterminal + "->" + production);
 							containsSingleProduction = true;
 
 							// Now traverse B's productions and add the new ones
 							// to A, expanding the production
 							if (productions.get(production) != null) {
 								for (String prod : productions.get(production)) {
 									//if not contained and not ourself
 									if (!values.contains(prod)) {
 										values.add(prod);
 									}
 								}
 							}
 
 							// Eliminate B from A-s productions							
 							values.remove(production);							
 
 						}
 					}
 				}
 
 				if (!newProductions.containsKey(nonterminal)) {
 						newProductions.put(nonterminal, new ArrayList<String>());
 					}
 					newProductions.put(nonterminal, values);
 												
 			}
 			
 			productions = newProductions;
 		}
 		
 		//Check for empty symbols
 		List<String> removeKeys = new ArrayList<String>();
 		
 		for(String nonterm : productions.keySet()) {
 			 if(productions.get(nonterm).isEmpty()) {
 				 removeKeys.add(nonterm);
 			 }
 		}
 		
 		for(String key:removeKeys) {
 			eliminateSymbol(key);
 		}
 	}
 }
