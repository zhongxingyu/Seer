 package org.rubypeople.rdt.internal.ui.text.ruby;
 
 import org.eclipse.jface.text.rules.IToken;
 
 /**
  * A rule which will return the given Token for Ruby symbols.
  * 
  * @author cawilliams
  * 
  */
 public class SymbolRule extends SingleCharacterPrefixRule {
 
     private static final char PREFIX = ':';
 
     public SymbolRule(IToken token) {
         super(PREFIX, token, 2, Integer.MAX_VALUE);
     }
 
 }
