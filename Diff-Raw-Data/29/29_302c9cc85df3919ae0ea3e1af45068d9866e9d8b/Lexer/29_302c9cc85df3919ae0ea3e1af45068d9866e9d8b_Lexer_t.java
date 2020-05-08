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
 
 /**
  * @author Meisam Fathi Salmi <fathi@cse.ohio-state.edu>
  * 
  */
 public class Lexer {
 
     private final InputProvider in;
 
     /**
      * 
      */
     public Lexer(final InputProvider inputProvider) {
         this.in = inputProvider;
     }
 
     public String nextToken() {
         try {
             String buffer = "";
 
             removeWhitespace();
 
             if (!in.hasMore()) {
                 return "$";
             }
 
             char lookaheadChar = in.lookaheadChar();
 
             if (isDigit(lookaheadChar)) {
                 buffer = readNumber();
             } else if (isLetter(lookaheadChar)) {
                 buffer = readSymbol();
             } else if (isWhiteSpace(lookaheadChar)) {
                 // buffer = readWhiteSpace()
             } else if (isOpenParentheses(lookaheadChar)) {
                 buffer = readOpenParentheses();
             } else if (isCloseParentheses(lookaheadChar)) {
                 buffer = readCloseParentheses();
            } else{
                throw new LexerExeption("Unknown Symbol in the input:" + lookaheadChar);
             }
             return buffer.toString();
         } catch (LexerExeption ex) {
             throw ex;
         } catch (Exception ex) {
             throw new LexerExeption("Cannot read the input");
         }
     }
 
     private void removeWhitespace() {
         while (in.hasMore() && isWhiteSpace(in.lookaheadChar())) {
             in.nextChar();
         }
     }
 
     private final boolean isDigit(final char ch) {
         if ('0' <= ch && ch <= '9') {
             return true;
         }
         return false;
     }
 
     private final boolean isLowercaseLetter(final char ch) {
         if ('a' <= ch && ch <= 'z') {
             return true;
         }
         return false;
     }
 
     private final boolean isUppercaseLetter(final char ch) {
         if ('A' <= ch && ch <= 'Z') {
             return true;
         }
         return false;
     }
 
     private final boolean isLetter(final char ch) {
         if (isUppercaseLetter(ch) || isLowercaseLetter(ch)) {
             return true;
         }
         return false;
     }
 
     private final boolean isWhiteSpace(char ch) {
         if (ch == ' ' || ch == '\t' || ch == '\r' || ch == '\n') {
             return true;
         }
         return false;
     }
 
     private final boolean isOpenParentheses(char ch) {
         if (ch == '(') {
             return true;
         }
         return false;
     }
 
     private final boolean isCloseParentheses(char ch) {
         if (ch == ')') {
             return true;
         }
         return false;
     }
 
     private final boolean isDelimiter(final char ch) {
         return isOpenParentheses(ch) || isCloseParentheses(ch)
                || isWhiteSpace(ch) ;
     }
 
     private final String readNumber() {
         final StringBuffer buffer = new StringBuffer(100);
         while (in.hasMore()) {
             char nextChar = in.nextChar();
             if (isDigit(nextChar)) {
                 buffer.append(nextChar);
             } else if (isDelimiter(nextChar)) {
                 break;
             } else {
                 throw new LexerExeption("Lexing Error: '" + buffer.toString()
                         + nextChar + "'is not a valid number");
             }
         }
         return buffer.toString();
     }
 
     private final String readSymbol() {
         final StringBuffer buffer = new StringBuffer(100);
         while (in.hasMore()) {
             char nextChar = in.nextChar();
             if (isLetter(nextChar)) {
                 buffer.append(nextChar);
             } else if (isDelimiter(nextChar)) {
                 break;
             } else {
                 throw new LexerExeption("Lexing Error: '" + buffer.toString()
                         + nextChar + "'is not a valid identifier");
             }
         }
         return buffer.toString();
     }
 
     private final String readOpenParentheses() {
         final StringBuffer buffer = new StringBuffer(100);
         char nextChar = in.nextChar();
         if (isOpenParentheses(nextChar)) {
             buffer.append(nextChar);
             return buffer.toString();
         } else {
             throw new LexerExeption("Lexing Error: '" + buffer.toString()
                     + in.lookaheadChar() + "'is not a valid identifier");
         }
     }
 
     private final String readCloseParentheses() {
         final StringBuffer buffer = new StringBuffer(100);
         char nextChar = in.nextChar();
         if (isCloseParentheses(nextChar)) {
             buffer.append(nextChar);
             return buffer.toString();
         } else {
             throw new LexerExeption("Lexing Error: '" + buffer.toString()
                     + in.lookaheadChar() + "'is not a valid identifier");
         }
     }
 
 }
