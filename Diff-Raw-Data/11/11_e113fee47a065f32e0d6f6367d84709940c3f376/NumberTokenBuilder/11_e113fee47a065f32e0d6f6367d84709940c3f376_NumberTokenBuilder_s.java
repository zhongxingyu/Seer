 package com.wickedspiral.jacss.lexer.builder;
 
 import com.wickedspiral.jacss.lexer.ParserState;
 import com.wickedspiral.jacss.lexer.Token;
 import com.wickedspiral.jacss.lexer.TokenBuilder;
 import com.wickedspiral.jacss.lexer.UnrecognizedCharacterException;
 
 /**
  * @author wasche
  * @since 2011.08.04
  */
 public class NumberTokenBuilder implements TokenBuilder
 {
     public void handle(ParserState state, char c) throws UnrecognizedCharacterException
     {
        boolean nonDigit = ! Character.isDigit(c);
         boolean point = ('.' == state.getLastCharacter());
 
         // .2em or .classname ?
         if (point && nonDigit)
         {
             state.tokenFinished(Token.OP)
                  .unsetTokenBuilder()
                 .tokenize(c);
         }
         // -2px or -moz-something
        else if (nonDigit && '-' == state.getLastCharacter())
         {
             state.setTokenBuilder(Token.IDENTIFIER)
                  .tokenize(c);
         }
        else if (!nonDigit || (!point && ('.' == c ||  '-' == c)))
         {
             state.push(c);
         }
         else if ("#".equals(state.getLastToken()))
         {
             state.setTokenBuilder(Token.IDENTIFIER)
                  .tokenize(c);
         }
         else
         {
             state.tokenFinished(Token.NUMBER)
                  .unsetTokenBuilder()
                  .tokenize(c);
         }
     }
 }
