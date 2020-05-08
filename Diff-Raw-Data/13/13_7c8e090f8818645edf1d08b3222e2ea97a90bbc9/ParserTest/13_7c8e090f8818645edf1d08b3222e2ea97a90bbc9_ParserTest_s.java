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
 
 import junit.framework.TestCase;
 import edu.osu.cse.meisam.interpreter.tokens.Token;
 
 /**
  * @author Meisam Fathi Salmi <fathi@cse.ohio-state.edu>
  * 
  */
 public class ParserTest extends TestCase {
 
     public void testSmokeParser() {
         final String input = "(PluS 5 4)";
         final InputProvider inputProvider = new StringInputProvider(input);
         final Lexer lexer = new Lexer(inputProvider);
         final Parser parser = new Parser(lexer);
 
         parser.parse();
 
         final ParseTree parseTree = parser.getParseTree();
         traverseParseTree(parseTree);
     }
 
     private void traverseParseTree(final ParseTree parseTree) {
         if (parseTree == null) {
             return;
         }
         traverseParseTree(parseTree.getLeftTree());
         traverseNode(parseTree.getToken());
         traverseParseTree(parseTree.getRightTree());
 
     }
 
     private void traverseNode(final Token token) {
         if (token != null) {
            System.out.println(token.getClass().getSimpleName()
                     + token.getLexval());
         } else {
            System.out.println("Null token");
         }
 
     }
 }
