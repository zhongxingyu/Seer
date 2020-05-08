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
 
 import java.io.PrintStream;
 
 import edu.osu.cse.meisam.interpreter.tokens.Atom;
 import edu.osu.cse.meisam.interpreter.tokens.LiteralAtom;
 import edu.osu.cse.meisam.interpreter.tokens.NumericAtom;
 import edu.osu.cse.meisam.interpreter.tokens.Token;
 
 /**
  * @author Meisam Fathi Salmi <fathi@cse.ohio-state.edu>
  * 
  */
 public class Interpreter {
 
     private final InputProvider in;
     private final Lexer lexer;
     private final Parser parser;
     private final PrintStream out;
 
     public Interpreter(final InputProvider in, final PrintStream out) {
         this.out = out;
         this.in = in;
         this.lexer = new Lexer(this.in);
         this.parser = new Parser(this.lexer);
     }
 
     /**
      * @param args
      */
     public static void main(final String[] args) {
         try {
             final Interpreter interpreter = new Interpreter(
                     new InputStreamProvider(System.in), System.out);
             interpreter.interpret();
         } catch (final Exception ex) {
             System.out.println("Error: " + ex.getMessage());
         }
 
     }
 
     /**
      * 
      */
     public void interpret() {
         try {
             do {
                 final ParseTree parseTree = this.parser.parseNextSExpresion();
                 if (parseTree == null) {
                     break;
                 }
                 evaluate(parseTree);
             } while (true);
         } catch (final Exception e) {
             this.out.append("ERROR: " + e.getMessage());
         }
     }
 
     /**
      * @param parseTree
      */
     private void evaluate(final ParseTree parseTree) {
         if (parseTree instanceof LeafNode) {
             evaluateNode((LeafNode) parseTree);
         } else if (parseTree instanceof InternalNode) {
             final InternalNode internalNode = (InternalNode) parseTree;
             final ParseTree leftTree = internalNode.getLeftTree();
             final ParseTree rightTree = internalNode.getRightTree();
             if (leftTree != null) {
                 if (leftTree instanceof LeafNode) {
                     apply((LeafNode) leftTree, rightTree);
                 } else {
                     raiseInterpreterException("Expecting to see an atom");
                 }
 
             } else if ((leftTree == null) && (rightTree == null)) {
                 evaluateNil();
             } else {
 
             }
         } else {
             raiseInterpreterException("Invalid S-Expression");
         }
     }
 
     private void evaluateNil() {
         System.out.println("evaluatingNill");
     }
 
     /**
      * @param leafNode
      * @param rightTree
      */
     private void apply(final LeafNode leafNode, final ParseTree rightTree) {
         AssertTrue("leafnode is null", leafNode != null);
         final Token token = leafNode.getToken();
         AssertTrue("token in the leaf node is null", token != null);
        AssertTrue(token.getLexval() + " is not a LiteralAtom",
                token instanceof LiteralAtom);
         final Atom atom = (LiteralAtom) token;
         final String lexval = atom.getLexval();
         if (lexval.equals("T")) {
             applyT(rightTree);
         } else if (lexval.equals("NIL")) {
             applyNil(rightTree);
         } else if (lexval.equals("CAR")) {
             applyCar(rightTree);
         } else if (lexval.equals("CDR")) {
             applyCdr(rightTree);
         } else if (lexval.equals("CONS")) {
             applyCons(rightTree);
         } else if (lexval.equals("ATOM")) {
             applyAtom(rightTree);
         } else if (lexval.equals("EQ")) {
             applyEq(rightTree);
         } else if (lexval.equals("NULL")) {
             applyNull(rightTree);
         } else if (lexval.equals("INT")) {
             applyInt(rightTree);
         } else if (lexval.equals("PLUS")) {
             applyPlus(rightTree);
         } else if (lexval.equals("MINUS")) {
             applyMinus(rightTree);
         } else if (lexval.equals("TIMES")) {
             applyTimes(rightTree);
         } else if (lexval.equals("QUOTIENT")) {
             applyQoutient(rightTree);
         } else if (lexval.equals("REMAINDER")) {
             applyRemainder(rightTree);
         } else if (lexval.equals("LESS")) {
             applyLess(rightTree);
         } else if (lexval.equals("GREATER")) {
             applyGreater(rightTree);
         } else if (lexval.equals("COND")) {
             applyCond(rightTree);
         } else if (lexval.equals("QUOTE")) {
             applyQuote(rightTree);
         } else if (lexval.equals("DEFUN")) {
             applyDefun(rightTree);
         }
         System.out.println("applying " + leafNode + " on " + rightTree);
     }
 
     private void applyT(final ParseTree rightTree) {
         System.out.println("Interpreter.enclosing_method()");
         evaluate(rightTree);
     }
 
     private void applyNil(final ParseTree rightTree) {
         System.out.println("Interpreter.applyNil()");
         evaluate(rightTree);
     }
 
     private void applyCar(final ParseTree rightTree) {
         System.out.println("Interpreter.applyCar()");
         evaluate(rightTree);
     }
 
     private void applyCdr(final ParseTree rightTree) {
         System.out.println("Interpreter.applyCdr()");
         evaluate(rightTree);
     }
 
     private void applyCons(final ParseTree rightTree) {
         System.out.println("Interpreter.applyCons()");
         evaluate(rightTree);
     }
 
     private void applyAtom(final ParseTree rightTree) {
         System.out.println("Interpreter.applyAtom()");
         evaluate(rightTree);
     }
 
     private void applyEq(final ParseTree rightTree) {
         System.out.println("Interpreter.applyEq()");
         evaluate(rightTree);
     }
 
     private void applyNull(final ParseTree rightTree) {
         System.out.println("Interpreter.applyNull()");
         evaluate(rightTree);
     }
 
     private void applyInt(final ParseTree rightTree) {
         System.out.println("Interpreter.applyInt()");
         evaluate(rightTree);
     }
 
     private void applyPlus(final ParseTree rightTree) {
         System.out.println("Interpreter.applyPlus()");
         evaluate(rightTree);
     }
 
     private void applyMinus(final ParseTree rightTree) {
         System.out.println("Interpreter.applyMinus()");
         evaluate(rightTree);
     }
 
     private void applyTimes(final ParseTree rightTree) {
         System.out.println("Interpreter.applyTimes()");
         evaluate(rightTree);
     }
 
     private void applyQoutient(final ParseTree rightTree) {
         System.out.println("Interpreter.applyQoutient()");
         evaluate(rightTree);
     }
 
     private void applyRemainder(final ParseTree rightTree) {
         System.out.println("Interpreter.applyRemainder()");
         evaluate(rightTree);
     }
 
     private void applyLess(final ParseTree rightTree) {
         System.out.println("Interpreter.applyLess()");
         evaluate(rightTree);
     }
 
     private void applyGreater(final ParseTree rightTree) {
         System.out.println("Interpreter.applyGreater()");
         evaluate(rightTree);
     }
 
     private void applyCond(final ParseTree rightTree) {
         System.out.println("Interpreter.applyCond()");
         evaluate(rightTree);
     }
 
     private void applyQuote(final ParseTree rightTree) {
         System.out.println("Interpreter.applyQuote()");
         evaluate(rightTree);
     }
 
     private void applyDefun(final ParseTree rightTree) {
         System.out.println("Interpreter.applyDefun()");
         evaluate(rightTree);
     }
 
     /**
      * @param leafNode
      */
     private void evaluateNode(final LeafNode leafNode) {
         final Token token = leafNode.getToken();
         if (token instanceof NumericAtom) {
             System.out.println(token.getLexval());
         } else {
             System.out.println("Evaluating some leafnode");
         }
 
     }
 
     /**
      * @param msg
      * @param b
      */
     private void AssertTrue(final String msg, final boolean b) {
         if (!b) {
             raiseInterpreterException(msg);
         }
     }
 
     /**
      * @param msg
      */
     private void raiseInterpreterException(final String msg) {
         throw new InterPreterException(msg);
     }
 
 }
