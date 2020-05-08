 /*
  * Copyright 2013 Future Systems
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  * http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.araqne.logdb.query.parser;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Stack;
 
 import org.araqne.logdb.LogQueryParseException;
 import org.araqne.logdb.query.expr.Expression;
 
 public class ExpressionParser {
 
 	public static Expression parse(String s, OpEmitterFactory of, FuncEmitterFactory ff, TermEmitterFactory tf) {
 		if (s == null)
 			throw new IllegalArgumentException("expression string should not be null");
 
 		s = s.replaceAll("\t", "    ");
 		List<Term> terms = tokenize(s);
 		List<Term> output = convertToPostfix(terms);
 		Stack<Expression> exprStack = new Stack<Expression>();
 		for (Term term : output) {
 			if (term instanceof OpTerm) {
 				OpTerm op = (OpTerm) term;
 				of.emit(exprStack, op);
 			} else if (term instanceof TokenTerm) {
 				// parse token expression (variable or numeric constant)
 				TokenTerm t = (TokenTerm) term;
 				tf.emit(exprStack, t);
 			} else {
 				// parse function expression
 				FuncTerm f = (FuncTerm) term;
 				ff.emit(exprStack, f);
 			}
 		}
 
 		return exprStack.pop();
 	}
 
 	private static OpEmitterFactory evalOEF = new EvalOpEmitterFactory();
 	private static FuncEmitterFactory evalFEF = new EvalFuncEmitterFactory();
 	private static TermEmitterFactory evalTEF = new EvalTermEmitterFactory();
 
 	public static Expression parse(String s) {
 		return parse(s, evalOEF, evalFEF, evalTEF);
 	}
 
 	private static List<Term> convertToPostfix(List<Term> tokens) {
 		Stack<Term> opStack = new Stack<Term>();
 		List<Term> output = new ArrayList<Term>();
 
 		int i = 0;
 		int len = tokens.size();
 		while (i < len) {
 			Term token = tokens.get(i);
 
 			if (isDelimiter(token)) {
 				// need to pop operator and write to output?
 				while (needPop(token, opStack, output)) {
 					Term last = opStack.pop();
 					output.add(last);
 				}
 
 				if (token instanceof OpTerm || token instanceof FuncTerm) {
 					opStack.add(token);
 				} else if (((TokenTerm) token).getText().equals("(")) {
 					opStack.add(token);
 				} else if (((TokenTerm) token).getText().equals(")")) {
 					boolean foundMatchParens = false;
 					while (!opStack.isEmpty()) {
 						Term last = opStack.pop();
 						if (last instanceof TokenTerm && ((TokenTerm) last).getText().equals("(")) {
 							foundMatchParens = true;
 							break;
 						} else {
 							output.add(last);
 						}
 					}
 
 					if (!foundMatchParens)
 						throw new LogQueryParseException("parens-mismatch", -1);
					Term last = opStack.pop();
					if (last instanceof FuncTerm) {
						output.add(last);
					} else {
						opStack.push(last);
 					}
 				}
 			} else {
 				output.add(token);
 			}
 
 			i++;
 		}
 
 		// last operator flush
 		while (!opStack.isEmpty()) {
 			Term op = opStack.pop();
 			output.add(op);
 		}
 
 		return output;
 	}
 
 	private static boolean needPop(Term token, Stack<Term> opStack, List<Term> output) {
 		if (!(token instanceof OpTerm))
 			return false;
 
 		OpTerm currentOp = (OpTerm) token;
 
 		int precedence = currentOp.precedence;
 		boolean leftAssoc = currentOp.leftAssoc;
 
 		OpTerm lastOp = null;
 		if (!opStack.isEmpty()) {
 			Term t = opStack.peek();
 			if (!(t instanceof OpTerm)) {
 				return false;
 			}
 			lastOp = (OpTerm) t;
 		} else {
 			return false;
 		}
 
 		if (leftAssoc && precedence <= lastOp.precedence)
 			return true;
 
 		if (precedence < lastOp.precedence)
 			return true;
 
 		return false;
 	}
 
 	private static boolean isOperator(String token) {
 		if (token == null)
 			return false;
 		return isDelimiter(token);
 	}
 
 	public static List<Term> tokenize(String s) {
 		return tokenize(s, 0, s.length() - 1);
 	}
 
 	private static List<Term> tokenize(String s, int begin, int end) {
 		List<Term> tokens = new ArrayList<Term>();
 
 		String lastToken = null;
 		int next = begin;
 		while (true) {
 			ParseResult r = nextToken(s, next, end);
 			if (r == null)
 				break;
 
 			String token = (String) r.value;
 			if (token.isEmpty())
 				continue;
 
 			// read function call (including nested one)
 			if (token.equals("(") && lastToken != null && !isOperator(lastToken)) {
 				// remove last term and add function term instead
 				tokens.remove(tokens.size() - 1);
 				List<String> functionTokens = new ArrayList<String>();
 				functionTokens.add(lastToken);
 				tokens.add(new FuncTerm(functionTokens));			
 			}
 
 			OpTerm op = OpTerm.parse(token);
 
 			// check if unary operator
 			if (op != null && op.symbol.equals("-")) {
 				Term lastTerm = null;
 				if (!tokens.isEmpty()) {
 					lastTerm = tokens.get(tokens.size() - 1);
 				}
 				
 				if (lastToken == null || lastToken.equals("(") || lastTerm instanceof OpTerm) {
 					op = OpTerm.Neg;
 				}
 			}
 
 			if (op != null)
 				tokens.add(op);
 			else
 				tokens.add(new TokenTerm(token));
 
 			next = r.next;
 			lastToken = token;
 		}
 
 		return tokens;
 	}
 
 	private static ParseResult nextToken(String s, int begin, int end) {
 		if (begin > end)
 			return null;
 
 		// use r.next as a position here (need +1 for actual next)
 		ParseResult r = findNextDelimiter(s, begin, end);
 		if (r.next < begin) {
 			// no symbol operator and white space, return whole string
 			String token = s.substring(begin, end + 1).trim();
 			return new ParseResult(token, end + 1);
 		}
 		
 		if (isAllWhitespaces(s, begin, r.next - 1)) {
 			// check if next token is quoted string
 			if (r.value.equals("\"")) {
 				int p = s.indexOf('"', r.next + 1);
 				if (p < 0) {
 					String quoted = s.substring(r.next);
 					return new ParseResult(quoted, s.length());
 				} else {
 					String quoted = s.substring(r.next, p + 1);
 					return new ParseResult(quoted, p + 1);
 				}
 			}
 			
 			// check whitespace
 			String token = (String) r.value;
 			if (token.trim().isEmpty())
 				return nextToken(s, skipSpaces(s, begin), end);
 
 			// return operator
 			int len = token.length();
 			return new ParseResult(token, r.next + len);
 		} else {
 			// return term
 			String token = s.substring(begin, r.next).trim();
 			return new ParseResult(token, r.next);
 		}
 	}
 
 	private static boolean isAllWhitespaces(String s, int begin, int end) {
 		if (end < begin)
 			return true;
 
 		for (int i = begin; i <= end; i++)
 			if (s.charAt(i) != ' ' && s.charAt(i) != '\t')
 				return false;
 
 		return true;
 	}
 
 	private static ParseResult findNextDelimiter(String s, int begin, int end) {
 		// check parens, comma and operators
 		ParseResult r = new ParseResult(null, -1);
 		min(r, "\"", s.indexOf('"', begin), end);
 		min(r, "(", s.indexOf('(', begin), end);
 		min(r, ")", s.indexOf(')', begin), end);
 		min(r, ",", s.indexOf(',', begin), end);
 		for (OpTerm op : OpTerm.values()) {
 			// check alphabet keywords
 			if (op.isAlpha)
 				continue;
 			
 			min(r, op.symbol, s.indexOf(op.symbol, begin), end);
 		}
 		
 		// check white spaces
 		// tabs are removed by ExpressionParser.parse, so it processes space only.
 		min(r, " ", s.indexOf(' ', begin), end);
 		return r;
 	}
 
 	private static boolean isDelimiter(String s) {
 		String d = s.trim();
 
 		if (d.equals("(") || d.equals(")") || d.equals(","))
 			return true;
 
 		for (OpTerm op : OpTerm.values())
 			if (op.symbol.equals(s))
 				return true;
 
 		return false;
 	}
 
 	private static void min(ParseResult r, String symbol, int p, int end) {
 		if (p < 0)
 			return;
 
 		boolean change = p >= 0 && p <= end && (r.next == -1 || p < r.next);
 		if (change) {
 			r.value = symbol;
 			r.next = p;
 		}
 	}
 
 	private static boolean isDelimiter(Term t) {
 		if (t instanceof OpTerm || t instanceof FuncTerm)
 			return true;
 
 		if (t instanceof TokenTerm) {
 			String text = ((TokenTerm) t).getText();
 			return text.equals("(") || text.equals(")");
 		}
 
 		return false;
 	}
 
 	private static interface Term {
 	}
 
 	public static class TokenTerm implements Term {
 		private String text;
 
 		public TokenTerm(String text) {
 			this.text = text;
 		}
 
 		@Override
 		public String toString() {
 			return getText();
 		}
 
 		public String getText() {
 			return text;
 		}
 
 	}
 
 	public static enum OpTerm implements Term {
 		Add("+", 500), Sub("-", 500), Mul("*", 510), Div("/", 510), Neg("-", 520, false, true, false),
 		Gte(">=", 410), Lte("<=", 410), Gt(">", 410), Lt("<", 410), Eq("==", 400), Neq("!=", 400),
 		And("and", 310, true, false, true), Or("or", 300, true, false, true), Not("not", 320, false, true, true),
 		Comma(",", 200), 
 		From("from", 100, true, false, true), union("union", 110, false, true, true),
 		;
 
 		OpTerm(String symbol, int precedence) {
 			this(symbol, precedence, true, false, false);
 		}
 
 		OpTerm(String symbol, int precedence, boolean leftAssoc, boolean unary, boolean isAlpha) {
 			this.symbol = symbol;
 			this.precedence = precedence;
 			this.leftAssoc = leftAssoc;
 			this.unary = unary;
 			this.isAlpha = isAlpha;
 		}
 
 		public String symbol;
 		public int precedence;
 		public boolean leftAssoc;
 		public boolean unary;
 		public boolean isAlpha;
 
 		public static OpTerm parse(String token) {
 			for (OpTerm t : values())
 				if (t.symbol.equals(token))
 					return t;
 
 			return null;
 		}
 
 		@Override
 		public String toString() {
 			return symbol;
 		}
 	}
 
 	public static class FuncTerm implements Term {
 		private List<String> tokens;
 
 		public FuncTerm(List<String> tokens) {
 			this.tokens = tokens;
 		}
 
 		@Override
 		public String toString() {
 			return "func term(" + tokens + ")";
 		}
 
 		public List<String> getTokens() {
 			return tokens;
 		}
 	}
 
 	public static int skipSpaces(String text, int position) {
 		int i = position;
 
 		while (i < text.length() && text.charAt(i) == ' ')
 			i++;
 
 		return i;
 	}
 }
