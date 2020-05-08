 package org.rubypeople.rdt.internal.ui.text.ruby;
 
 import org.eclipse.jface.text.Assert;
 import org.eclipse.jface.text.rules.ICharacterScanner;
 import org.eclipse.jface.text.rules.IRule;
 import org.eclipse.jface.text.rules.IToken;
 import org.eclipse.jface.text.rules.Token;
 
 /**
  * This is a base class for recognizing tokens where a single character prefix
  * can determine if the word is of a particular token type. The token ends at
  * EOF, EOL or whitespace.
  * 
  * @author cawilliams
  * 
  */
 public class SingleCharacterPrefixRule implements IRule {
 
     protected char fPrefix;
     protected IToken fToken;
     protected int fMinLength;
     protected int fMaxLength;
 
     public SingleCharacterPrefixRule(char prefix, IToken token, int minLength, int maxLength) {
         fPrefix = prefix;
         Assert.isNotNull(token);
         fToken = token;
         Assert.isLegal(minLength >= 1);
         fMinLength = minLength;
         Assert.isLegal(maxLength >= 1);
         fMaxLength = maxLength;
     }
 
     public IToken evaluate(ICharacterScanner scanner) {
         int c = scanner.read();
         int length = 1;
         if (((char) c) == fPrefix) {
             // Now read until we hit EOF, EOL or whitespace
             while (true) {
                 c = scanner.read();
                if (!isValidCharacter(c, length)) {
                     scanner.unread();
                     if (!lengthInRange(length)) return Token.UNDEFINED;
                     return fToken;
                 }
                 length++;
             }
         }
 
         scanner.unread();
         return Token.UNDEFINED;
     }
 
     /**
     * Determine if the current character is valid for the rule. Return false if
     * the character is not a part of the token. This is not applied to the
     * single character prefix.
     * 
     * @param c
     * @param index 
     * @return
     */
    protected boolean isValidCharacter(int c, int index) {
        return c != ICharacterScanner.EOF && !Character.isWhitespace((char) c);
    }

    /**
      * Determine if the length of the token is valid.
      * 
      * @param length
      * @return
      */
     protected boolean lengthInRange(int length) {
         return (length >= fMinLength) && (length <= fMaxLength);
     }
 
 }
