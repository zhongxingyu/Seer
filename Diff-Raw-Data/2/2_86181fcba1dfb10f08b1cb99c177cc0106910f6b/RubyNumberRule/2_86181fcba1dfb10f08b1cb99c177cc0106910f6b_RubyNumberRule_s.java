 package org.rubypeople.rdt.internal.ui.text.ruby;
 
 import org.eclipse.jface.text.rules.ICharacterScanner;
 import org.eclipse.jface.text.rules.IToken;
 import org.eclipse.jface.text.rules.NumberRule;
 import org.eclipse.jface.text.rules.Token;
 
 public class RubyNumberRule extends NumberRule {
 
 	public RubyNumberRule(IToken token){
 		super(token);
 	}
 	
 	/*
 	 * @see IRule#evaluate(ICharacterScanner)
 	 */
 	public IToken evaluate(ICharacterScanner scanner) {
 		int c= scanner.read();
 		if (Character.isDigit((char)c)) {
 			scanner.unread();
 			scanner.unread();
 			c = scanner.read();
			if ( Character.isLetter((char)c)){
 				do {
 					c= scanner.read();
 				} while (Character.isDigit((char) c));
 				scanner.unread();
 				return Token.UNDEFINED;
 			}
 			c = scanner.read();
 			if (fColumn == UNDEFINED || (fColumn == scanner.getColumn() - 1)) {
 				do {
 					c= scanner.read();
 				} while (Character.isDigit((char) c));
 				if ( Character.isLetter((char)c) ){
 					scanner.unread();
 					return Token.UNDEFINED;
 				}
 				scanner.unread();
 				return fToken;
 			}
 		}
 
 		scanner.unread();
 		return Token.UNDEFINED;
 	}
 	
 }
