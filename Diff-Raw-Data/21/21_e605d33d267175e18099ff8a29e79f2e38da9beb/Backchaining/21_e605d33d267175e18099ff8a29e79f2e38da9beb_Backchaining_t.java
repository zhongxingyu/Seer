 package main;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Scanner;
 import java.util.Stack;
 
 import utils.Parser;
 
 public class Backchaining {
 	
 	KnowledgeBase knowledgeBase;
 	ArrayList<String> proofRules = new ArrayList<String>();
 	HashSet<String> proofLiterals = new HashSet<String>();
 	
 	HashSet<String> hashStack = new HashSet<String>();
 	Stack<String> stack = new Stack<String>();
 	
 	public Backchaining (KnowledgeBase knowledgeBase) {
 		this.knowledgeBase = knowledgeBase;
 	}
 
 	public boolean query(String literal) {
 		// new query, reset everything
 		proofRules.clear();
 		hashStack.clear();
 		stack.clear();
 		return prove(literal);
 	}
 	
 	private boolean prove(String literal) {
 		push(literal);
 		if (Menu.trace || Main.DEBUG)
 			System.out.println("stack: " + stack);
 
 		pop();
 		
 		// Since we only can have positive symbols on the conclusion, reverse negative
 		if (literal.contains("-")) {
 			if (prove(Parser.negate(literal)))
 				return false;
 		}
 
 		// Try the facts
 		if (knowledgeBase.getFact(literal) != null) {
 			proofLiterals.add(literal);
 			return true;
 		}
 
 		ArrayList<String> rules = knowledgeBase.getRules(literal);
 		// any rules containing the literal?
 		if (rules != null) {
 
 			for (String rule : rules) {
 				
 				Scanner scan = new Scanner(rule);
 				String lit = scan.next();
 				int counter = 0;
 				while (!lit.contains(">")) {
 					if (!lit.contains("^")) {
 						if(push(lit))
 							counter++;
 					}
 					lit = scan.next();
 				}
 				scan.close();
 				boolean ruleProven = true;
 				while (counter != 0 ){
 					// If any term of the implication is false, everything is false
 					if (!prove(pop())) {
 						ruleProven = false;
 						break;
 					}
 					counter--;
 				}
 				// A rule passed completely, the implication is true
 				proofRules.add(rule);
 				if (ruleProven) {
 					proofRules.add("true");
 					return true;
 				}
 				proofRules.add("fail");
 				// else just go to next rule or fail
 			}
 		}
 		// we could not find any rules or facts to prove literal, fail!
 		return false;
 	}
 
 	public boolean push(String literal) {
 		// new literal added
 		if (!hashStack.contains(literal)) {
 			stack.add(literal);
 			hashStack.add(literal);
 			return true;
 		}
 		// no new literal added
 		return false;
 	}
 	
 	public String pop() {
 		String literal = stack.pop();
 		hashStack.remove(literal);
 		return literal;
 	}
 	
 	public void proof() {

 		System.out.println("Literals used:");
 		for(String literal : proofLiterals) {
 			System.out.println(literal);
 		}
 		System.out.println("Rules used:");
 		for(String rule : proofRules) {
 			System.out.println(rule);
 		}
 	}
 }
