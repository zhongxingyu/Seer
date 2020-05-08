 /*
  Kick Assembler plugin - An Eclipse plugin for convenient Kick Assembling
  Copyright (c) 2012 - P-a Backstrom <pa.backstrom@gmail.com>
  
  Based on ASMPlugin - http://sourceforge.net/projects/asmplugin/
  Copyright (c) 2006 - Andy Reek, D. Mitte
  
  This program is free software; you can redistribute it and/or
  modify it under the terms of the GNU General Public License
  as published by the Free Software Foundation; either version 2
  of the License, or (at your option) any later version.
 
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */ 
 package org.lyllo.kickassplugin.editor;
 
 import org.eclipse.jface.text.rules.ICharacterScanner;
 import org.eclipse.jface.text.rules.IToken;
 import org.eclipse.jface.text.rules.IWordDetector;
 import org.eclipse.jface.text.rules.Token;
 import org.eclipse.jface.text.rules.WordRule;
 
 /**
  * A special WordRule, which ingnores the case of a given word.
  * 
  * @author Andy Reek
  * @since 25.11.2005
  */
 public class WordRuleCaseInsensitive extends WordRule {
 
   /**
    * Buffer used for pattern detection
    */
   private StringBuffer fBuffer = new StringBuffer();
   private boolean caseSensitive = false;
 private IRuleListener listener;
 
   /**
    * The constructor.
    */
   public WordRuleCaseInsensitive(IRuleListener listener) {
     this(Token.UNDEFINED, listener);
   }
 
   /**
    * Creates a rule which. If no token has been associated, the specified
    * default token will be returned.
    * 
    * @param  the default token to be returned on success if nothing
    *          else is specified, may not be <code>null</code>
    * 
    * @see #addWord(String, IToken)
    */
   public WordRuleCaseInsensitive(IToken defaultToken, IRuleListener listener) {
     super(new IWordDetector() { // A dummy. WordDetector will be
             // replaced a
             // few rows below.
             public boolean isWordPart(char c) {
               return false;
             }
 
             public boolean isWordStart(char c) {
               return false;
             }
           } );
 
     fDetector = new MyWordDetector();
     this.listener = listener;
   }
 
   /**
    * {@inheritDoc}
    */
   public IToken evaluate(ICharacterScanner scanner) {
     int c = 0;
 
     if (scanner.getColumn() > 0) {
       scanner.unread();
       c = scanner.read();
       if (!Character.isWhitespace((char) c)) {
         return fDefaultToken;
       }
     }
 
     c = scanner.read();
     if (!caseSensitive)
     	c = Character.toLowerCase((char) c);
     if (fDetector.isWordStart((char) c)) {
       if (fColumn == UNDEFINED || (fColumn == scanner.getColumn() - 1)) {
 
         fBuffer.setLength(0);
         do {
           fBuffer.append((char) c);
           c = scanner.read();
           if (!caseSensitive)
         	  c = Character.toLowerCase((char) c);
         } while (c != ICharacterScanner.EOF && c!= '(' && fDetector.isWordPart((char) c));
         scanner.unread();
 
         IToken token = (IToken) fWords.get(fBuffer.toString());
         if (token != null) {
         	this.listener.tokenFound(new ITokenMatch(fBuffer.toString(), scanner.getColumn(), token));
           return token;
         }
 
         if (fDefaultToken.isUndefined()) {
           unreadBuffer(scanner);
         }
 
         return fDefaultToken;
       }
     }
 
     scanner.unread();
     return Token.UNDEFINED;
   }
 
   /**
    * {@inheritDoc}
    */
   public void addWord(String word, IToken token) {
    super.addWord(caseSensitive ? word : word.toLowerCase(), token);
   }
 
   /**
    * Returns the characters in the buffer to the scanner.
    * 
    * @param scanner the scanner to be used
    */
   protected void unreadBuffer(ICharacterScanner scanner) {
     for (int i = fBuffer.length() - 1; i > -1; i--) {
       scanner.unread();
     }
   }
 
   public boolean isCaseSensitive() {
 	return caseSensitive;
 }
 
 public void setCaseSensitive(boolean caseSensitive) {
 	this.caseSensitive = caseSensitive;
 }
 
 /**
    * A WordDetector, which recognizes all typable characters.
    * 
    * @author andre
    * 
    */
   private class MyWordDetector implements IWordDetector {
     /**
      * {@inheritDoc}
      */
     public boolean isWordStart(char c) {
       return ((c > ' ') && (c <= '~'));
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean isWordPart(char c) {
       return ((c > ' ') && (c <= '~'));
     }
   }
 }
