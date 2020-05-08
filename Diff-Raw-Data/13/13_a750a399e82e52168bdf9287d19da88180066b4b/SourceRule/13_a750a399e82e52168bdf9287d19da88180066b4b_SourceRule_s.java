 package com.gamevm.plugin.editors;
 
 import org.eclipse.jface.text.rules.*;
 
 public class SourceRule implements IPredicateRule {
 	
 	private IToken sourceToken;
 	
 	public SourceRule(IToken token) {
 		this.sourceToken = token;
 	}
 	
 	@Override
 	public IToken evaluate(ICharacterScanner scanner) {
		return sourceToken;
 	}
 
 	@Override
 	public IToken getSuccessToken() {
 		return sourceToken;
 	}
 
 	@Override
 	public IToken evaluate(ICharacterScanner scanner, boolean resume) {
		return sourceToken;
 	}
 }
