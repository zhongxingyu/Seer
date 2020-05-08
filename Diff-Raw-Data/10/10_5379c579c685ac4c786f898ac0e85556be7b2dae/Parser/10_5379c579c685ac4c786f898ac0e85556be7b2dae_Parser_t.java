 /**
  * Lisp Subinterpreter, an interpreter for a sublanguage of Lisp
  * Copyright (C) 2011  Meisam Fathi Salmi <fathi@cse.ohio-state.edu>
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package edu.osu.cse.meisam.interpreter;
 
 import edu.osu.cse.meisam.interpreter.tokens.Atom;
 import edu.osu.cse.meisam.interpreter.tokens.CloseParentheses;
 import edu.osu.cse.meisam.interpreter.tokens.Dot;
 import edu.osu.cse.meisam.interpreter.tokens.OpenParentheses;
 import edu.osu.cse.meisam.interpreter.tokens.Token;
 
 // <S> ::= <E>
 // <E> ::= atom
 // <E> ::= ( <X>
 // <X> ::= <E> <Y>
 // <X> ::= ) 
 // <Y> ::= . <E> )
 // <Y> ::= <R> )
 // <R> ::= NIL
 // <R> ::= <E> <R>
 
 /**
  * @author Meisam Fathi Salmi <fathi@cse.ohio-state.edu>
  * 
  */
 public class Parser {
 
     private static final String DEFAULT_ERROR_MESSAGE = "Input is not a valid lisp program";
 
     /**
      * 
      */
     private final Lexer lexer;
 
     /**
      * Current token
      */
     private Token token;
 
     private ParseTree parseTree;
 
     /**
      * @param lexer
      */
     public Parser(final Lexer lexer) {
         this.lexer = lexer;
     }
 
     /**
      * @return the parseTree
      */
     public ParseTree getParseTree() {
         return this.parseTree;
     }
 
    private void move() {
        this.token = this.lexer.nextToken();
    }

     public void parse() {
         this.parseTree = null;
         while (this.lexer.hasMoreTokens()) {
             this.token = this.lexer.nextToken();
             final ParseTree newParseTree = parseE();
             if (this.parseTree == null) {
                 this.parseTree = newParseTree;
             } else {
                 this.parseTree = new ParseTree(null, getParseTree(),
                         newParseTree);
             }
         }
     }
 
     private ParseTree parseE() {
         if (this.token instanceof Atom) {
             return parseAtom();
         } else if (this.token instanceof OpenParentheses) {
             parseOpenParentheses();
             return parseX();
         } else { // error
             return raiseParserError(Parser.DEFAULT_ERROR_MESSAGE);
         }
     }
 
     private ParseTree parseX() {
         if (this.token instanceof CloseParentheses) {
             parseCloseParentheses();
             return null;
         } else if ((this.token instanceof OpenParentheses)
                 || (this.token instanceof Atom)) { // head of E
             final ParseTree leftParseTree = parseE();
             final ParseTree rightParseTree = parseY();
             return new ParseTree(this.token, leftParseTree, rightParseTree);
         } else { // error
             return raiseParserError(Parser.DEFAULT_ERROR_MESSAGE);
         }
     }
 
     private ParseTree parseY() {
         if (this.token instanceof Dot) {
             parseDot();
             final ParseTree parseTree = parseE();
             parseCloseParentheses();
             return parseTree;
         } else {
             final ParseTree parseTree = parseR();
             parseCloseParentheses();
             return parseTree;
         }
     }
 
     private ParseTree parseR() {
         if ((this.token instanceof Atom)
                 || (this.token instanceof OpenParentheses)) {
             final ParseTree leftParseTree = parseE();
             final ParseTree rightParseTree = parseR();
             return new ParseTree(this.token, leftParseTree, rightParseTree);
         } else {
             return null;
         }
     }
 
     private ParseTree parseAtom() {
         if (this.token instanceof Atom) {
             final Token currentToken = this.token;
             move();
             return new ParseTree(currentToken, null, null);
         } else {
             return raiseParserError("Looking for a lisp atom but fouund "
                     + this.token.getClass().getSimpleName());
         }
     }
 
     private void parseOpenParentheses() {
         if (this.token instanceof OpenParentheses) {
             move();
         } else {
             raiseParserError("Looking for a ( but fouund "
                     + this.token.getClass().getSimpleName());
         }
     }
 
     private void parseCloseParentheses() {
         if (this.token instanceof CloseParentheses) {
             move();
         } else {
             raiseParserError("Looking for a ) but fouund "
                     + this.token.getClass().getSimpleName());
         }
     }
 
     private void parseDot() {
         if (this.token instanceof Dot) {
             move();
         } else {
             raiseParserError("Looking for a . but fouund "
                     + this.token.getClass().getSimpleName());
         }
     }
 
     private ParseTree raiseParserError(final String msg) {
         throw new ParserException(msg);
     }
 
 }
