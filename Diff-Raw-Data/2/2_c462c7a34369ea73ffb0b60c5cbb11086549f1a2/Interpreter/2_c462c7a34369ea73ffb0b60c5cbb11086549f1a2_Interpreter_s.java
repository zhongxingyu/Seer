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
 
 import java.io.InputStream;
 import java.io.PrintStream;
 
 /**
  * @author Meisam Fathi Salmi <fathi@cse.ohio-state.edu>
  * 
  */
 public class Interpreter {
 
     private final InputProvider in;
     private final Lexer lexer;
     private final Parser parser;
 
     public Interpreter(final InputStream in, final PrintStream out) {
         this.in = new InputStreamProvider(in);
         this.lexer = new Lexer(this.in);
         this.parser = new Parser(this.lexer);
     }
 
     /**
      * @param args
      */
     public static void main(final String[] args) {
         try {
             final Interpreter interpreter = new Interpreter(System.in,
                     System.out);
             interpreter.interpret();
         } catch (final Exception ex) {
            System.err.println("Error: " + ex.getMessage());
         }
 
     }
 
     public void interpret() {
         this.parser.parse();
     }
 
 }
