 package com.cs3240.parsergenerator;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Scanner;
 
 import com.cs3240.parsergenerator.Domain.Grammar;
 import com.cs3240.parsergenerator.Domain.NonterminalSymbol;
 import com.cs3240.parsergenerator.Domain.Symbol;
 import com.cs3240.parsergenerator.Domain.TerminalSymbol;
 
 public final class GrammarFileParser {
 	public static Grammar parse(final String fileName) throws FileNotFoundException {
 		Scanner grammarScanner = new Scanner(new File(fileName));
     	grammarScanner.useDelimiter("\n");
     	Grammar myGrammar = new Grammar();
     	boolean isDefiningRules = false;
     	while (grammarScanner.hasNext()) {
     		String[] lineSplit = grammarScanner.next().split(" ");
     		//Switching between Tokens, Non-terminals, Start, and Rules
     		if (lineSplit[0].trim().equals("%Tokens")) {
     			for (int i = 1; i < lineSplit.length; i++) {
     				TerminalSymbol symbol = new TerminalSymbol(lineSplit[i].trim());
     				myGrammar.addSymbol(symbol);
     			}
     		} else if (lineSplit[0].trim().equals("%Non-terminals")) {
     			for (int i = 1; i < lineSplit.length; i++) {
     				NonterminalSymbol symbol = new NonterminalSymbol(lineSplit[i].trim());
     				myGrammar.addSymbol(symbol);
     			}
     		} else if (lineSplit[0].trim().equals("%Start")) {
    			myGrammar.setStartRule((NonterminalSymbol) myGrammar.getSymbol(lineSplit[1].trim()));
     		} else if (lineSplit[0].trim().equals("%Rules")) {
     			isDefiningRules = true;
     		} else {
     			if (isDefiningRules) {
     				NonterminalSymbol symbol =
     					(NonterminalSymbol) myGrammar.getSymbol(lineSplit[0].trim());
     				List<Symbol> rule = new ArrayList<Symbol>();
     				for (int i = 2; i < lineSplit.length; i++) {
     					if (lineSplit[i].trim().equals("|")) {
     						myGrammar.addRule(symbol, rule);
     						rule = new ArrayList<Symbol>();
     						continue;
     					}
     					rule.add(myGrammar.getSymbol(lineSplit[i].trim()));
     				}
     				myGrammar.addRule(symbol, rule);
     			} else {
     				System.err.println("DID NOT SET RULES");
     			}
     		}
     		
     	}
     	return myGrammar;
 	}
 	
 
 }
