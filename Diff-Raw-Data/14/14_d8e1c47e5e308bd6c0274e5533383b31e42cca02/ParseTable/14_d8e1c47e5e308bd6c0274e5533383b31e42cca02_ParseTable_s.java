 import java.io.ByteArrayOutputStream;
 import java.io.PrintStream;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Scanner;
 
 
 public class ParseTable {
 	private Grammar grammar;
 	private Rule[][] table;
 	private List<Variable> varList;
 	private List<Terminal> termList;
 	
 	// Constructors
 	public ParseTable(Grammar grammar) {
 		this.grammar = grammar;
 		constructTable();
 	}
 
 	// Getters/setters
 	public Grammar getGrammar() {
 		return grammar;
 	}
 	
 	public Rule[][] getTable() {
 		return table;
 	}
 	
 	public void constructTable() {
 		varList = grammar.getOrderedVars();
 		termList = new ArrayList<Terminal>(grammar.getParser().getTokenClasses());
 		termList.add(new DollarTerminal(grammar));
 		
 		table = new Rule[varList.size()][termList.size()];
 		
 		// For each rule
 		for (Rule rule : grammar.getRules()) {
 			int varI = varList.indexOf(rule.getVariable());
 			
 			// For each token in First(rule)
 			for (Terminal term : rule.getFirst()) {
 				int termI = termList.indexOf(term);
 				
 				if (table[varI][termI] == null) {
 					table[varI][termI] = rule;
 				} else {
 					invalid(varI, termI, rule);
 				}
 			}
 			
 			// If epsilon in First(rule)
			if (rule.hasEpsilonInFirst()) {
 				// For each token in Follow(rule)
 				for (Terminal term : rule.getFollow()) {
 					int termI = termList.indexOf(term);
 					
 					if (table[varI][termI] == null) {
 						table[varI][termI] = rule;
 					} else {
 						invalid(varI, termI, rule);
 					}
 				}
 			}
 		}
 	}
 	
 	public void invalid(int var, int term, Rule rule) {
 		System.out.println("The grammar is not LL(1)! Entries clashed.");
 		System.out.println("Cell ["+var+","+term+"] currently has "+table[var][term]+" but tried to assign "+rule);
 		System.exit(0);
 	}
 	
 	public void walk(Scanner input) {
 		List<Token> tokens = grammar.getParser().scan(input);
 	}
 	
 	public String toString() {
 		MultiColumnPrinter tp = new MultiColumnPrinter(table[0].length+1, 2, "-", MultiColumnPrinter.CENTER, false);
 		ByteArrayOutputStream baos = new ByteArrayOutputStream();
 		tp.stream = new PrintStream(baos);
 
 		String[] headers = new String[termList.size()+1];
 		headers[0] = "Variable";
 		for (int i = 1; i < headers.length; i++) {
 			headers[i] = termList.get(i-1).toString();
 		}
 
 		tp.addTitle(headers);
 
 		for (int i = 0; i < table.length; i++) {
 			String[] row = new String[table[i].length+1];
 			row[0] = varList.get(i).toString();
 			for (int j = 0; j < row.length-1; j++) {
 				if (table[i][j] == null) {
 					row[j+1] = "~~";
 					continue;
 				}
 				row[j+1] = table[i][j].toString();
 			}
 			tp.add(row);
 		}
 
 		tp.print();
 		try {
 			return baos.toString("UTF8");
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
 
 		return null;
 	}
 }
