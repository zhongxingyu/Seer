 package org.suite.doer;
 
 import org.parser.Operator;
 import org.parser.Parser;
 import org.suite.Context;
 import org.util.Util;
 
 public class TermParser extends Parser {
 
 	public static enum TermOp implements Operator {
 		NEXT__("#", Assoc.RIGHT), //
 		IS____(" :- ", Assoc.RIGHT), //
 		INDUCE(" => ", Assoc.RIGHT), //
 		LET___(" >> ", Assoc.RIGHT), //
 		APPLY_(" | ", Assoc.LEFT), //
 		JOIN__(" . ", Assoc.LEFT), //
 		OR____(";", Assoc.RIGHT), //
 		AND___(",", Assoc.RIGHT), //
 		BLOR__(" || ", Assoc.RIGHT), //
		BLAND_(" && ", Assoc.RIGHT), //
 		LE____(" <= ", Assoc.RIGHT), //
 		LT____(" < ", Assoc.RIGHT), //
 		GE____(" >= ", Assoc.RIGHT), //
 		GT____(" > ", Assoc.RIGHT), //
 		NOTEQ_(" != ", Assoc.RIGHT), //
 		EQUAL_(" = ", Assoc.RIGHT), //
 		PLUS__(" + ", Assoc.RIGHT), //
 		MINUS_(" - ", Assoc.LEFT), //
 		MULT__(" * ", Assoc.RIGHT), //
 		DIVIDE(" / ", Assoc.LEFT), //
 		MODULO(" % ", Assoc.LEFT), //
 		POWER_("^", Assoc.RIGHT), //
 		BRACES("{", Assoc.LEFT), //
 		SEP___(" ", Assoc.RIGHT), //
 		ITEM__("/", Assoc.RIGHT), //
 		COLON_(":", Assoc.RIGHT), //
 		;
 
 		public final String name;
 		public final Assoc assoc;
 		public int precedence;
 
 		private TermOp(String name, Assoc associativity) {
 			this.name = name;
 			this.assoc = associativity;
 		}
 
 		static {
 			int precedence = 0;
 			for (TermOp operator : values())
 				operator.precedence = ++precedence;
 		}
 
 		public String getName() {
 			return name;
 		}
 
 		public Assoc getAssoc() {
 			return assoc;
 		}
 
 		public int getPrecedence() {
 			return precedence;
 		}
 
 		public static TermOp find(String name) {
 			for (TermOp operator : values())
 				if (Util.equals(operator.name, name))
 					return operator;
 			return null;
 		}
 	}
 
 	public TermParser() {
 		super(TermOp.values());
 	}
 
 	public TermParser(Context context) {
 		super(context, TermOp.values());
 	}
 
 }
