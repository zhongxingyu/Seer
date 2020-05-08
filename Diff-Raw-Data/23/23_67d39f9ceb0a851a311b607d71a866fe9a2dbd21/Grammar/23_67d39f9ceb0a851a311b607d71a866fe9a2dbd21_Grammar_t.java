 package com.cs3240.parsergenerator.Domain;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * @author Bobby
  * The master class for the Grammar.  This will keep a Mapping of all the Symbols 
  * and their names.  This will also be able to get out all the rules for each 
  * non-terminal by keeping a mapping of nonterminal symbols to rules.
  * 
  * TODO Make it so that a nonterminal can have multiple rules.
  */
 public class Grammar {
 	private List<Symbol> listOfSymbols;
 	private Map<NonterminalSymbol, List<Rule>> rulesMap;
 	private NonterminalSymbol startRule;
 	
 	public Grammar() {
 		this.listOfSymbols = new ArrayList<Symbol>();
 		this.rulesMap = new HashMap<NonterminalSymbol, List<Rule>>();
 	}
 	
 	/**
 	 * Adds a symbol to this grammar.
 	 * @param symbol The symbol to add.
 	 */
 	public void addSymbol(Symbol symbol) {
 		listOfSymbols.add(symbol);
 	}
 	
 	/**
 	 * Adds a rule to the rules map.   
 	 * 
 	 * @param symbol A Nonterminal, since they are only symbols with rules
 	 * @param rule The rule for this nonterminal
 	 */
 	public void addRule(NonterminalSymbol symbol, List<Symbol> rule) {
 		List<Rule> rules = rulesMap.get(symbol);
 		if (rules == null) {
 			rules = new ArrayList<Rule>();
 		}
 		rules.add(new Rule(rule));
 		rulesMap.put(symbol, rules);
 	}
 	
 	/**
 	 * This gets the rule with Symbols in it out of the map. 
 	 * @param symbol The nonterminal that we are getting the rule for.
 	 * @return a List of Symbols representing the rule for this nonterminal.
 	 */
 	public List<Rule> getRules(NonterminalSymbol symbol) {
 		List<Rule> rules = rulesMap.get(symbol);
 		
 		return rules;
 	}
 
 	public void setStartRule(NonterminalSymbol startRule) {
 		this.startRule = startRule;
 	}
 
 	public NonterminalSymbol getStartRule() {
 		return startRule;
 	}
 	
 	/**
 	 * This is for sake of debugging, returns in format
 	 * 
 	 * TERMINALS: ...
 	 * NONTERMINALS: ...
 	 * START: ...
 	 * RULES:
 	 * <>: ...
 	 * <>: ...
 	 * 
 	 */
 	public String toString() {
 		//First, the symbols
 		StringBuilder build = new StringBuilder();
 		build.append("SYMBOLS: ");
 		for (Symbol s : listOfSymbols) {
 			build.append(s.getName() + " ");
 		}
 		build.append("\n");
 		
 		//Now, the Rules
 		Set<NonterminalSymbol> keySetRules = rulesMap.keySet();
 		build.append("RULES: \n");
 		for (NonterminalSymbol sym : keySetRules) {
 			List<Rule> rules = rulesMap.get(sym);
 			for (Rule rule : rules) {
 				build.append(sym.getName() + " : ");
 				for (Symbol s : rule.getRule()) {
 					build.append(s + " ");
 				}
 				build.append("\n");
 			}
 		}
 		return build.toString();
 	}
 	
 	public void removeLeftRecursion() {
 		List<NonterminalSymbol> nonTermList = new ArrayList<NonterminalSymbol>();
 		for (Symbol sym : listOfSymbols) {
 			if (sym instanceof NonterminalSymbol) {
 				nonTermList.add((NonterminalSymbol) sym);
 			}
 		}
 		for (int i = 0; i < nonTermList.size(); i++) {
 			for (int j = 0; j < i - 1; j++) {
 				NonterminalSymbol ai = nonTermList.get(i);
 				if (i == j) {
 					immediateLeftRecursion(ai);
 				}
 				NonterminalSymbol aj = nonTermList.get(j);
 				List<Rule> rulesForAi = rulesMap.get(ai);
 				List<Rule> rulesForAj = rulesMap.get(aj);
 				List<Rule> rulesToRemove = new ArrayList<Rule>();
 				List<Rule> rulesToAdd = new ArrayList<Rule>();
 				for (Rule rule : rulesForAi) {
 					if (rule.get(0).equals(aj.getName())) {
 						rulesToRemove.add(rule);
 						for (Rule ruleJ : rulesForAj) {
 							Rule ruleClone = new Rule(rule.getRule());
 							ruleClone.remove(0);
 							ruleClone.addRuleToFront(ruleJ);
 							rulesToAdd.add(ruleClone);
 						}
 					}
 				}
 				List<Rule> newRulesForAi = new ArrayList<Rule>(rulesForAi);
 				newRulesForAi.removeAll(rulesToRemove);
 				newRulesForAi.addAll(rulesToAdd);
 				rulesMap.put(ai, newRulesForAi);
 				
 			}
 		}
 		
 	}
 
 	private void immediateLeftRecursion(NonterminalSymbol ai) {
 		List<Rule> rulesForAi = rulesMap.get(ai);
 		List<Rule> rulesToAdd = new ArrayList<Rule>();
 		List<Rule> rulesToRemove = new ArrayList<Rule>();
 		NonterminalSymbol newSymbol = new NonterminalSymbol(ai.getName() + "Prime");
 		List<Rule> rulesForNewSymbol = new ArrayList<Rule>();
 		for (Rule rule : rulesForAi){
 			Rule ruleClone = new Rule(rule.getRule());
 			if (ruleClone.get(0).equals(ai.getName())) {
 				rulesToRemove.add(rule);
 				ruleClone.remove(0);
 				ruleClone.add(newSymbol);
 				rulesForNewSymbol.add(ruleClone);
 				continue;
 			}
 			ruleClone.add(newSymbol);
 		}
 		List<Symbol> episilonRule = new ArrayList<Symbol>();
 		episilonRule.add(new TerminalSymbol("."));
 		Rule epsilon = new Rule(episilonRule);
 		rulesForNewSymbol.add(epsilon);
 		List<Rule> newRulesForAi = new ArrayList<Rule>(rulesForAi);
 		newRulesForAi.removeAll(rulesToRemove);
 		newRulesForAi.addAll(rulesToAdd);
 		rulesMap.put(ai, newRulesForAi);
 		listOfSymbols.add(newSymbol);
 		rulesMap.put(newSymbol, rulesForNewSymbol);
 	}
 
 	/**
 	 * @return A list of all nonterminal symbols used in the grammar.
 	 */
 	public List<NonterminalSymbol> getNonTerminals() {
 		// TODO Implement me
 		return null;
 	}
 
	public Symbol getSymbol(String name) {
 		for (Symbol s : listOfSymbols) {
 			if (s.getName().equals(name)) {
				return s;
 			}
 		}
 		return null;
 	}
 	public List<Rule> getRulesForNonterminal(NonterminalSymbol symbol) {
 		// TODO Implement me
 		return null;
 	}
 
 }
