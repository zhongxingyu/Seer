 /*!
  * Java Shunting-yard Implementierung
  * Copyright 2012 - droptable <murdoc@raidrush.org>
  *
  * Referenz: <http://en.wikipedia.org/wiki/Shunting-yard_algorithm>
  *
  * ---------------------------------------------------------------- 
  *
  * Permission is hereby granted, free of charge, to any person obtaining a 
  * copy of this software and associated documentation files (the "Software"), 
  * to deal in the Software without restriction, including without 
  * limitation the rights to use, copy, modify, merge, publish, distribute, 
  * sublicense, and/or sell copies of the Software, and to permit persons to 
  * whom the Software is furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included 
  * in all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
  * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
  * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS 
  * BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, 
  * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR 
  * OTHER DEALINGS IN THE SOFTWARE.
  *
  * <http://opensource.org/licenses/mit-license.php>
  */
 
 package com.rr.term_parser;
 
 import com.rr.term_parser.Stack;
 import com.rr.term_parser.Token;
 import com.rr.term_parser.Ident;
 import com.rr.term_parser.Number;
 import com.rr.term_parser.Scanner;
 import com.rr.term_parser.Context;
 import com.rr.term_parser.Operator;
 import com.rr.term_parser.ParseError;
 import com.rr.term_parser.SyntaxError;
 import com.rr.term_parser.RuntimeError;
 
 public class Parser 
 {
 	public static final short ST_1 = 1,
 					          ST_2 = 2;
 	
 	protected Scanner scanner;
 	protected short state;
 	protected Stack queue, stack;		
 	
 	public Parser(Scanner s) throws ParseError
 	{
 		Token t;
 		
 		this.scanner = s;
 		
 		// alloc
 		this.queue = new Stack();
 		this.stack = new Stack();
 		
 		// queue erzeugen
 		while ((t = this.scanner.next()) != null)
 			this.handle(t);
 		
 		// When there are no more tokens to read:
	    // While there are still operator tokens in the stack:
 		while ((t = this.stack.pop()) != null) {
 			if (t.type == Token.T_POPEN || t.type == Token.T_PCLOSE)
 				throw new ParseError("fehlerhafte verschachtelung von `(` und `)`");
 			
 			this.queue.push(t);
 		}
 	}
 	
 	public double reduce(Context ctx) throws RuntimeError
 	{
 		this.stack = new Stack();
 		int len = 0;
 		
 		Token t;
 		
 		// While there are input tokens left
 		// Read the next token from input.
 		while ((t = this.queue.shift()) != null) {
 			switch (t.type) {
 				case Token.T_NUMBER:
 				case Token.T_IDENT:
 					if (t.type == Token.T_IDENT)
 						t = new Number(ctx.cs(((Ident) t).value), Token.T_NUMBER);
 					
 					// If the token is a value or identifier
 					// Push it onto the stack.
 					this.stack.push(t);
 					++len;
 					break;
 					
 				case Token.T_PLUS:
 				case Token.T_MINUS:
 				case Token.T_UNARY_PLUS:
 				case Token.T_UNARY_MINUS:
 				case Token.T_TIMES:
 				case Token.T_DIV:
 				case Token.T_MOD:
 				case Token.T_POW:
 				case Token.T_NOT: {
 					// It is known a priori that the operator takes n arguments.
 					int na = this.argc(t);
 					
 					// If there are fewer than n values on the stack
 					if (len < na)
 						throw new RuntimeError("zu wenig token fr operator `" + ((Operator) t).value + "`");
 					
 					Token rhs = this.stack.pop();
 					Token lhs = null;
			          
 					if (na > 1) lhs = this.stack.pop();
			           
 					len -= (na - 1);
 					
			        // Push the returned results, if any, back onto the stack.
			        this.stack.push(new Number(this.op(t.type, lhs, rhs), Token.T_NUMBER));
			        break;
 				}
 				
 				case Token.T_FUNCTION: {
 					int      argc = ((Ident) t).argc;
 					double[] argv = new double[argc];
 					
 					len -= (argc - 1);
 					
 					for (; argc > 0; --argc)
 						argv[argc - 1] = ((Number) this.stack.pop()).value;
 					
 					// Push the returned results, if any, back onto the stack.
 					this.stack.push(new Number(ctx.fn(((Ident) t).value, argv), Token.T_NUMBER));
 					break;
 				}
 				
 				default:
 					throw new RuntimeError("unerwarteter token #" + t.type);
 			}
 		}
 		
 		// If there is only one value in the stack
 		// That value is the result of the calculation.
 		if (this.stack.size() == 1)
 			return ((Number) this.stack.pop()).value;
 		
 		throw new RuntimeError("zu viele werte auf dem stack");
 	}
 	
 	protected double op(short type, Token lhs, Token rhs) throws RuntimeError
 	{
 		if (lhs != null) {
 			double v1 = ((Number) lhs).value;
 			double v2 = ((Number) rhs).value;
 
 			switch (type) {
 				case Token.T_PLUS:
 					return v1 + v2;
 
 				case Token.T_MINUS:
 					return v1 - v2;
 
 				case Token.T_TIMES:
 					return v1 * v2;
 
 				case Token.T_DIV:
 					if (v2 == 0.)
 						throw new RuntimeError("teilung durch 0");
 					
 					return v1 / v2;
 
 				case Token.T_MOD:
 					if (v2 == 0.)
 						throw new RuntimeError("rest-teilung durch 0");
 					
 					return (double) (v1 % v2);
 
 				case Token.T_POW:
 					return Math.pow(v1, v2);
 			}
 
 			// throw?
 			return 0.;
 		}
 
 		switch (type) {
 			case Token.T_NOT:
 				return ((Number) rhs).value > 0. ? 0. : 1.;
 
 			case Token.T_UNARY_MINUS:
 				return -((Number) rhs).value;
 
 			case Token.T_UNARY_PLUS:
 				return +((Number) rhs).value;
 		}
 		
 		// throw?
 		return 0.;
 	}
 	
 	protected int argc(Token t)
 	{
 		switch (t.type) {
 			case Token.T_PLUS:
 			case Token.T_MINUS:
 			case Token.T_TIMES:
 			case Token.T_DIV:
 			case Token.T_MOD:
 			case Token.T_POW:
 				return 2;
 		}
 	    
 		return 1;
 	}
 	
 	protected void fargs(Token fn) throws ParseError
 	{
 		this.handle(this.scanner.next()); // '('
 
 		int argc = 0;
 		Token next = this.scanner.peek();
 
 		if (next != null && next.type != Token.T_PCLOSE) {
 			argc = 1;
 
 			while ((next = this.scanner.next()) != null) {
 				this.handle(next);
 		
 				if (next.type == Token.T_PCLOSE)
 					break;
 		
 				if (next.type == Token.T_COMMA)
 					++argc;
 			}
 		}
 	
 		((Ident) fn).argc = argc;
 	}
 	
 	protected void handle(Token t) throws ParseError
 	{
 		switch (t.type) {
 			case Token.T_NUMBER:
 			case Token.T_IDENT:
 				// If the token is a number (identifier), then add it to the output queue.        
 				this.queue.push(t);
 				this.state = Parser.ST_2;
 				break;
 
 			case Token.T_FUNCTION:
 				// If the token is a function token, then push it onto the stack.
 				this.stack.push(t);
 				this.fargs(t);
 				break;
 
 
 			case Token.T_COMMA: {
 				// If the token is a function argument separator (e.g., a comma):
 
 				boolean pe = false;
 				
 				while ((t = this.stack.last()) != null) {
 					if (t.type == Token.T_POPEN) {
 						pe = true;
 						break;
 					}
 
 					// Until the token at the top of the stack is a left parenthesis,
 					// pop operators off the stack onto the output queue.
 					this.queue.push(this.stack.pop());
 				}
 
 				// If no left parentheses are encountered, either the separator was misplaced
 				// or parentheses were mismatched.
 				if (pe != true)
 					throw new ParseError("vermisster token `(` oder fehlplazierter token `,`");
 
 				break;
 			}
 
 			// If the token is an operator, op1, then:
 			case Token.T_PLUS:
 			case Token.T_MINUS:
 				if (this.state == Parser.ST_1)
 					t.type = t.type == Token.T_PLUS ? Token.T_UNARY_PLUS : Token.T_UNARY_MINUS;
 
 				// kein break
 
 				// design-bedingt wechseln wir anschlieend wieder in ST_1
 				// es sind also mehrere vorzeichen erlaubt: -+1 = okay
 
 			case Token.T_TIMES:
 			case Token.T_DIV:
 			case Token.T_MOD:
 			case Token.T_POW:
 			case Token.T_NOT: {
 				parent_while: while (this.stack.size() > 0) {
 					Token s = this.stack.last();
 
 					// While there is an operator token, o2, at the top of the stack
 					// op1 is left-associative and its precedence is less than or equal to that of op2,
 					// or op1 has precedence less than that of op2,
 					// Let + and ^ be right associative.
 					// Correct transformation from 1^2+3 is 12^3+
 					// The differing operator priority decides pop / push
 					// If 2 operators have equal priority then associativity decides.
 					switch (s.type) {
 						default: break parent_while;
 
 						case Token.T_PLUS:
 						case Token.T_MINUS:
 						case Token.T_UNARY_PLUS:
 						case Token.T_UNARY_MINUS:
 						case Token.T_TIMES:
 						case Token.T_DIV:
 						case Token.T_MOD:
 						case Token.T_POW:
 						case Token.T_NOT: {
 							int p1 = this.preced(t);
 							int p2 = this.preced(s);
 
 							if (!((this.assoc(t) == 1 && (p1 <= p2)) || (p1 < p2)))
 								break parent_while;
 
 							// Pop o2 off the stack, onto the output queue;
 							this.queue.push(this.stack.pop());
 						}
 					}
 				}
 
 				// push op1 onto the stack.
 				this.stack.push(t);
 				this.state = Parser.ST_1;
 				break;
 			}
 		
 			case Token.T_POPEN:
 				// If the token is a left parenthesis, then push it onto the stack.
 				this.stack.push(t);
 				this.state = Parser.ST_1;
 				break;
 
 			// If the token is a right parenthesis:  
 			case Token.T_PCLOSE: {
 				boolean pe = false;
 
 				// Until the token at the top of the stack is a left parenthesis,
 				// pop operators off the stack onto the output queue
 				while ((t = this.stack.pop()) != null) {
 					if (t.type == Token.T_POPEN) {
 						// Pop the left parenthesis from the stack, but not onto the output queue.
 						pe = true;
 						break;
 					}
 
 					this.queue.push(t);
 				}
 
 				// If the stack runs out without finding a left parenthesis, then there are mismatched parentheses.
 				if (pe != true)
 					throw new ParseError("unerwarteter token `)`");
 
 				// If the token at the top of the stack is a function token, pop it onto the output queue.
 				if ((t = this.stack.last()) != null && t.type == Token.T_FUNCTION)
 					this.queue.push(this.stack.pop());
 
 				this.state = Parser.ST_2;  
 				break;
 			}
 			
 			default:
 				throw new ParseError("unbekannter token #" + t.type);          
 		}
 	}
 	
 	protected int assoc(Token t)
 	{
 		switch (t.type) {
 			case Token.T_TIMES:
 			case Token.T_DIV:
 			case Token.T_MOD:
 
 			case Token.T_PLUS:
 			case Token.T_MINUS:
 				return 1; // ltr
 
 			case Token.T_NOT:  
 			case Token.T_UNARY_PLUS:
 			case Token.T_UNARY_MINUS:
 
 			case Token.T_POW:  
 				return 2; // rtl
 		}
 
 		return 0; // nassoc
 	}
 	
 	protected int preced(Token t)
 	{
 		switch (t.type) {
 			case Token.T_NOT:
 			case Token.T_UNARY_PLUS:
 			case Token.T_UNARY_MINUS:
 				return 4;
 		
 			case Token.T_POW:
 				return 3;
 		
 			case Token.T_TIMES:
 			case Token.T_DIV:
 			case Token.T_MOD:
 				return 2;
 		
 			case Token.T_PLUS:
 			case Token.T_MINUS:
 				return 1;
 		}
 	
 		return 0;
 	}
 	
 	public static double parse(String term) throws SyntaxError, ParseError, RuntimeError
 	{
 		return Parser.parse(term, new Context());
 	}
 	
 	public static double parse(String term, Context ctx) throws SyntaxError, ParseError, RuntimeError
 	{
 		return new Parser(new Scanner(term)).reduce(ctx);
 	}
 }
