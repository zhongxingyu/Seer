 /*******************************************************************************
  * Copyright (C) 2003-2012 Bob Foster. All rights reserved.
  * 
  * This software is provided under the terms of the Apache License, Version 2.0
  * A copy of the license is available at http://www.apache.org/licenses/LICENSE-2.0.html.
  * 
  * Contributors:
  * 
  *    Bob Foster, initial API and implementation.
  *******************************************************************************/
   
 package org.genantics.peggen;
 
 import java.util.LinkedList;
 import java.util.List;
 
 
 /**
  * Hand-written parser for PEG grammars, written in the style
  * of, and as a model for, generated parsers.
  * 
  * @author Bob Foster
  */
 public class Parser {
 	
 	private LinkedList errors;
 	private String error;
 	private Node[] out;
 	private char[] in;
 	private int inpos;
 	private int inend;
 	private int outpos;
 	private int outend;
   private Node lastFail;
 	
 	private static final int INITIAL_OUT_SIZE = 100;
 	
 	public Node[] parseGrammar(String grammar) {
 		char[] buf = grammar.toCharArray();
 		return parseGrammar(buf, 0, buf.length);
 	}
 
 	public Node[] parseGrammar(char[] buf, int start, int length) {
 		out = new Node[INITIAL_OUT_SIZE];
 		outpos = 0;
 		outend = INITIAL_OUT_SIZE;
 		in = buf;
 		inpos = start;
 		inend = start + length;
 		error = null;
 		// ruleGrammar is the start rule
 		// parse could start with any rule
 		if (ruleGrammar(null))
 			return pack();
 		else
 			return null;
 	}
 	
 	/**
 	 * Returns error message if parse failed,
 	 * or null if parse succeeded.
 	 */
 	public List getErrors() {
 		return errors;
 	}
 	
 	/**
 	 * For unit testing.
 	 */
 	void reset(String snippet) {
 		char[] buf = snippet.toCharArray();
 		out = new Node[INITIAL_OUT_SIZE];
 		outpos = 0;
 		outend = INITIAL_OUT_SIZE;
 		in = buf;
 		inpos = 0;
 		inend = buf.length;
 	}
 	
 	/**
 	 * For unit testing.
 	 */
 	Node[] tree() {
 		return out;
 	}
 	
 	/**
 	 * For unit testing.
 	 */
 	int treetop() {
 		return outpos;
 	}
 	
 	/**
 	 * For unit testing.
 	 */
 	int inpos() {
 		return inpos;
 	}
 	
 	boolean ruleGrammar(Node parent) {
 		// Grammar <- Spacing Definition+ (EndOfFile / error)
 		int outstart = outpos;
 		if (sameRule("Grammar")) return out[outstart].success;
 		Node rule = new Node("Grammar", parent, inpos);
 		out[outpos++] = rule;
 		
 		int inmark = inpos;
 		int outmark = outpos;
 		boolean match;
 		match = ruleSpacing(rule);
 		if (match) {
 			match = ruleDefinition(rule);
 			if (match) {
 				do {
 					match = ruleDefinition(rule);
 				} while (match);
 				match = true;
 			}
 		}
 		if (match) {
 			match = ruleEndOfFile(rule);
 			if (!match) {
 				error();
 				match = true;
 				if (match)
 					match = ruleEndOfFile(rule);
 			}
 		}
 		if (!match) {
 			return fail(rule, outstart);
 		}
 		return succeed(rule);
 	}
   
   private static class Error {
     Node rule;
   }
 	
 	private void error() {
 		if (errors == null)
 			errors = new LinkedList();
     int pos = inpos;
     if (lastFail != null && lastFail.offset > pos)
       pos = lastFail.offset;
     errors.add("Parse error at line "+countLines(pos)+":");
 		errors.add(collectErrorString(pos));
     errors.add(indicateCharPos(pos));
 	}
   
   private int countLines(int pos) {
     int line = 1;
     for (int i = 0; i < pos; i++) {
       char c = in[i];
       if (c == '\r' || c == '\n')
         line++;
     }
     return line;
   }
   
   private String indicateCharPos(int pos) {
     StringBuilder sb = new StringBuilder();
    for (int i = Math.min(pos, in.length-1); i >= 0; i--) {
 			char c = in[i];
 			if (c == '\r' || c == '\n')
         break;
       else
         sb.append(' ');
     }
     sb.append('^');
     return sb.toString();
   }
 
 	private String collectErrorString(int pos) {
 		StringBuffer buf = new StringBuffer();
     int start;
    for (start = Math.min(pos, in.length-1); start >= 0; start--) {
 			char c = in[start];
 			if (c == '\r' || c == '\n')
         break;
     }
     if (start > 0)
       start++;
     for (int i = start; i < pos; i++)
       buf.append(in[i]);
 		for (int i = pos; i < inend; i++) {
 			char c = in[i];
 			if (c == '\r' || c == '\n')
         break;
 			else
 				buf.append(c);
 		}
 		return buf.toString();
 	}
 
 	boolean ruleEndOfFile(Node rule) {
 		// EndOfFile~~ <- !.
 		return inpos == inend;
 	}
 
 	boolean ruleDefinition(Node parent) {
 		// Definition <- Identifier DEFSUPPRESS? LEFTARROW Expression
 		int outstart = outpos;
 		if (sameRule("Definition")) return out[outstart].success;
 		Node rule = new Node("Definition", parent, inpos);
 		out[outpos++] = rule;
 		
 		boolean match;
 		match = ruleIdentifier(rule);
 		if (match) {
 			match = ruleDEFSUPPRESS(rule);
 			match = true;
 		}
 		if (match)
 			match = ruleLEFTARROW(rule);
 		if (match)
 			match = ruleExpression(rule);
 			
 		if (!match) return fail(rule, outstart);
 		return succeed(rule);
 	}
 
 	boolean ruleExpression(Node parent) {
 		// Expression~2 <- Sequence (SLASH~ Sequence)*
 		int outstart = outpos;
 		if (sameRule("Expression")) return out[outstart].success;
 		Node rule = new Node("Expression", parent, inpos);
 		out[outpos++] = rule;
 		
 		int count = 0;
 		boolean match;
 		match = ruleSequence(rule);
 		if (match) count++;
 		if (match) {
 			do {
 				int outmark1 = outpos;
 				match = ruleSLASH(rule);
 				outpos = outmark1;
 				if (match) {
 					match = ruleSequence(rule);
 					if (match) count++;
 				}
 			} while (match);
 			match = true;
 		}
 		if (!match) return fail(rule, outstart);
 		rule.remove = count < 2;
 		return succeed(rule);
 	}
 
 	boolean ruleSLASH(Node rule) {
 		// SLASH~~ <- ('/' / '|') Spacing # Changed
 		int inmark = inpos;
 		int outmark = outpos;
 		boolean match = matchChar('/');
 		if (!match)
 			match = matchChar('|');
 		if (match)
 			match = ruleSpacing(rule);
 		if (!match) {
 			outpos = outmark;
 			inpos = inmark;
 			return false;
 		}
 		return true;
 	}
 
 	boolean ruleSequence(Node parent) {
 		// Sequence~2 <- Prefix*
 		int outstart = outpos;
 		if (sameRule("Sequence")) return out[outstart].success;
 		Node rule = new Node("Sequence", parent, inpos);
 		out[outpos++] = rule;
 		
 		int count = 0;
 		boolean match;
 		do {
 			match = rulePrefix(rule);
 			if (match) count++;
 		} while (match);
 		// the next two lines can be omitted in peephole optimization
 		match = true;
 		if (!match) return fail(rule, outstart);
 		rule.remove = count < 2;
 		return succeed(rule);
 	}
 
 	boolean rulePrefix(Node parent) {
 		// Prefix~2 <- (AND / NOT)? Suffix
 		int outstart = outpos;
 		if (sameRule("Prefix")) return out[outstart].success;
 		Node rule = new Node("Prefix", parent, inpos);
 		out[outpos++] = rule;
 		
 		int count = 0;
 		boolean match;
 		match = ruleAND(rule);
 		if (match) count++;
 		if (!match) {
 			match = ruleNOT(rule);
 			if (match) count++;
 		}
 		match = ruleSuffix(rule);
 		if (match) count++;
 		if (!match) return fail(rule, outstart);
 		rule.remove = count < 2;
 		return succeed(rule);
 	}
 
 	boolean ruleAND(Node parent) {
 		// AND <- '&'~ Spacing
 		return singleCharLexRule(parent, '&', "AND");
 	}
 
 	boolean ruleNOT(Node parent) {
 		// NOT <- '!'~ Spacing~
 		// if a rule outputs no child rules, the following optimization is possible
 		return singleCharLexRule(parent, '!', "NOT");
 	}
 
 	boolean ruleSuffix(Node parent) {
 		// Suffix~2 <- SuppressPrimary (QUESTION / STAR / PLUS)?
 		int outstart = outpos;
 		if (sameRule("Suffix")) return out[outstart].success;
 		Node rule = new Node("Suffix", parent, inpos);
 		out[outpos++] = rule;
 		
 		int count = 0;
 		boolean match;
 		int outmark = outpos;
 		int inmark = inpos;
 		match = ruleSuppressPrimary(rule);
 		if (match) count++;
 		if (match) {
 			match = ruleQUESTION(rule);
 			if (match) count++;
 			if (!match) {
 				match = ruleSTAR(rule);
 				if (match) count++;
 			}
 			if (!match) {
 				match = rulePLUS(rule);
 				if (match) count++;
 			}
 			match = true;
 		}
 		
 		if (!match) return fail(rule, outstart);
 		rule.remove = count < 2;
 		return succeed(rule);
 	}
 
 	boolean rulePLUS(Node parent) {
 		// PLUS <- '+'~ Spacing~
 		return singleCharLexRule(parent, '+', "PLUS");
 	}
 
 	boolean ruleQUESTION(Node parent) {
 		// QUESTION <- '?'~ Spacing~
 		return singleCharLexRule(parent, '?', "QUESTION");
 	}
 
 	boolean ruleSTAR(Node parent) {
 		// STAR <- '*'~ Spacing~
 		return singleCharLexRule(parent, '*', "STAR");
 	}
 
 	boolean ruleSuppressPrimary(Node parent) {
 		// SuppressPrimary~2 <- Primary SUPPRESS?
 		int outstart = outpos;
 		if (sameRule("SuppressPrimary")) return out[outstart].success;
 		Node rule = new Node("SuppressPrimary", parent, inpos);
 		out[outpos++] = rule;
 		
 		int count = 0;
 		boolean match;
 		match = rulePrimary(rule);
 		if (match) count++;
 		if (match) {
 			match = ruleSUPPRESS(rule);
 			if (match) count++;
 			match = true;
 		}
 		if (!match) return fail(rule, outstart);
 		
 		rule.remove = count < 2;
 		return succeed(rule);
 	}
 	
 	boolean ruleError(Node parent) {
 		// Error <- ERROR
 		int outstart = outpos;
 		if (sameRule("Error")) return out[outstart].success;
 		Node rule = new Node("Error", parent, inpos);
 		out[outpos++] = rule;
 		
 		boolean match;
 		match = ruleERROR(rule);
 
     if (!match) return fail(rule, outstart);
 		
 		return succeed(rule);
 	}
 	
 	boolean ruleERROR(Node rule) {
 		// ERROR~~ <- 'error' Spacing
 		boolean match;
 		int inmark = inpos;
 		int outmark = outpos;
 		match = matchLiteral("error");
 		if (match)
 			match = ruleSpacing(rule);
 		outpos = outmark;
 		if (!match) {
 			inpos = inmark;
 			return false;
 		}
 		return true;
 	}
 
 	boolean rulePrimary(Node rule) {
 		// Primary~ <- Error
 		// / Identifier !(DEFSUPPRESS? LEFTARROW)
 		// / &'(' Term
 		// / Literal / Class / DOT
 		// this code illustrates the general case for alternatives
 			
 		boolean match = false;
 		int outmark = outpos;
 		int inmark = inpos;
 		match = ruleError(rule);
 		if (!match) {
 			outpos = outmark;
 			inpos = inmark;
 			match = ruleIdentifier(rule);
 			if (match) {
 				int outmark1 = outpos;
 				int inmark1 = inpos;
 				match = ruleDEFSUPPRESS(rule);
 				match = true;
 				if (match)
 					match = ruleLEFTARROW(rule);
 				match = !match;
 				outpos = outmark1;
 				inpos = inmark1;
 			}
 		}
 		if (!match) {
 			outpos = outmark;
 			inpos = inmark;
 			match = matchChar('(');
 			outpos = outmark;
 			inpos = inmark;
 			if (match) {
 				match = ruleTerm(rule);
 			}
 		}
 		if (!match) {
 			outpos = outmark;
 			inpos = inmark;
 			match = ruleLiteral(rule);
 		}
 		if (!match) {
 			outpos = outmark;
 			inpos = inmark;
 			match = ruleClass(rule);
 		}
 		if (!match) {
 			outpos = outmark;
 			inpos = inmark;
 			match = ruleDOT(rule);
 		}
 		if (!match) {
 			outpos = outmark;
 			inpos = inmark;
 			match = ruleConsume(rule);
 		}
 		if (!match) {
 			outpos = outmark;
 			inpos = inmark;
 			return false;
 		}
 		return true;
 	}
 	
 	boolean ruleConsume(Node parent) {
 		// Consume <- ':' Identifier
 		int outstart = outpos;
 		//if (sameRule("Consume")) return out[outstart].success;
 		Node rule = new Node("Consume", parent, inpos);
 		out[outpos++] = rule;
 		
 		boolean match = false;
 		int inmark = inpos;
 		int outmark = outpos;
 		match = matchChar(':');
 		if (match) {
 			match = ruleIdentifier(rule);
 		}
 		if (match) {
 			rule.collect = true;
 			StringBuffer buf = new StringBuffer();
 			for (int i = rule.offset + 1; i < inpos; i++) {
 				buf.append(in[i]);
 			}
 			rule.name = buf.toString().intern();
 		}
 		if (!match) return fail(rule, outstart);
 		
 		return succeed(rule);
 	}
 	
 	boolean ruleTerm(Node parent) {
 		// Term <- OPEN~ Expression CLOSE~
 		int outstart = outpos;
 		if (sameRule("Term")) return out[outstart].success;
 		Node rule = new Node("Term", parent, inpos);
 		out[outpos++] = rule;
 		
 		boolean match = false;
 		int inmark = inpos;
 		int outmark = outpos;
 		match = ruleOPEN(rule);
 		outpos = outmark;
 		if (match) {
 			match = ruleExpression(rule);
 		}
 		if (match) {
 			int outmark1 = outpos;
 			match = ruleCLOSE(rule);
 			outpos = outmark1;
 		}
 		if (!match) return fail(rule, outstart);
 		
 		return succeed(rule);
 	}
 
 	boolean ruleDOT(Node parent) {
 		// DOT <- '.'~ Spacing~
 		return singleCharLexRule(parent, '.', "DOT");
 	}
 	
 	private boolean singleCharLexRule(Node parent, char c, String name) {
 		// optimized rule delays node creation
 		// note match code generation pattern
 		int outstart = outpos;
 		if (sameRule(name)) return out[outstart].success;
 		
 		boolean match = false;
 		int inmark = inpos;
 		int outmark = outpos;
 		match = matchChar(c);
 		if (match) {
 			int outmark1 = outpos;
 			match = ruleSpacing(parent);
 			outpos = outmark1;
 		}
 		if (!match) {
 			outpos = outmark;
 			inpos = inmark;
 			return false;
 		}
 
 		Node rule = new Node(name, parent, inmark);
 		out[outpos++] = rule;
 		return succeed(rule);
 	}
 
 	boolean ruleClass(Node parent) {
 		// Class <- '['~ (!(']' / Char '-]') Range)* ']'~ Spacing
 		int outstart = outpos;
 		if (sameRule("Class")) return out[outstart].success;
 		Node rule = new Node("Class", parent, inpos);
 		out[outpos++] = rule;
 		
 		boolean match;
 		match = matchChar('[');
 		if (match) {
 			do {
 				int inmark1 = inpos;
 				int outmark1 = outpos;
 				match = matchChar(']');
 				if (!match) {
 					inpos = inmark1;
 					match = ruleChar(rule);
 					if (match) {
 						match = matchLiteral("-]");
 					}
 				}
 				inpos = inmark1;
 				outpos = outmark1;
 				match = !match;
 				if (match) {
 					match = ruleRange(rule);
 				}
 			} while (match);
 			match = true;
 		}
 		if (match) {
 			match = matchChar(']');
 		}
 		if (match) {
 			match = ruleSpacing(rule);
 		}
 		if (!match) return fail(rule, outstart);
 		
 		return succeed(rule);
 	}
 
 	boolean ruleRange(Node parent) {
 		// Range <- Char '-' Char / Char
 		int outstart = outpos;
 		if (sameRule("Range")) return out[outstart].success;
 		Node rule = new Node("Range", parent, inpos);
 		out[outpos++] = rule;
 		
 		int outmark = outpos;
 		int inmark = inpos;
 		boolean match;
 		match = ruleChar(rule);
 		if (match) {
 			match = matchChar('-');
 		}
 		if (match) {
 			match = ruleChar(rule);
 		}
 		if (!match) {
 			outpos = outmark;
 			inpos = inmark;
 			match = ruleChar(rule);
 		}
 		if (!match) return fail(rule, outstart);
 		
 		return succeed(rule);
 	}
 
 	boolean ruleChar(Node parent) {
 		// Char <- '\\' [nrt'"\[\]\\]
 		// / '\\' [0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f][0-9A-Fa-f] # Added
 		// / '\\' [0-2][0-7][0-7]
 		// / '\\' [0-7][0-7]?
 		// / !'\\' .
 		int outstart = outpos;
 		if (sameRule("Char")) return out[outstart].success;
 		Node rule = new Node("Char", parent, inpos);
 		out[outpos++] = rule;
 		
 		// factored x a / x b / x c / x d / !x e
 		// to x (a / b / c / d) / !x e
 		int inmark = inpos;
 		boolean match = matchChar('\\');
 		if (match) {
 			int inmark1 = inpos;
 			match = matchSet("nrt'\"[]\\");
 			if (!match) {
 				inpos = inmark1;
 				match = matchRange("09AFaf");
 				if (match)
 					match = matchRange("09AFaf");
 				if (match)
 					match = matchRange("09AFaf");
 				if (match)
 					match = matchRange("09AFaf");
 			}
 			if (!match) {
 				inpos = inmark1;
 				match = matchRange("02");
 				if (match)
 					match = matchRange("07");
 				if (match)
 					match = matchRange("07");
 			}
 			if (!match) {
 				inpos = inmark1;
 				match = matchRange("07");
 				if (match)
 					matchRange("07");
 			}
 		}
 		if (!match) {
 			inpos = inmark;
 			match = matchChar('\\');
 			match = !match;
 			if (match)
 				if (match = inpos < inend)
 					inpos++;
 		}
 		if (!match) return fail(rule, outstart);
 		
 		return succeed(rule);
 	}
 
 	boolean ruleLiteral(Node parent) {
 		// Literal <- [']~ (!['] Char)* [']~ Spacing
 		// / ["]~ (!["] Char)* ["]~ Spacing
 		int outstart = outpos;
 		if (sameRule("Literal")) return out[outstart].success;
 		Node rule = new Node("Literal", parent, inpos);
 		out[outpos++] = rule;
 		
 		int inmark = inpos;
 		int outmark = outpos;
 		boolean match;
 		match = matchChar('\'');
 		if (match) {
 			do {
 				int inmark1 = inpos;
 				match = matchChar('\'');
 				inpos = inmark1;
 				match = !match;
 				if (match)
 					match = ruleChar(rule);
 			} while (match);
 			match = true;
 		}
 		if (match) {
 			match = matchChar('\'');
 		}
 		if (!match) {
 			inpos = inmark;
 			outpos = outmark;
 			match = matchChar('"');
 			if (match) {
 				do {
 					int inmark2 = inpos;
 					match = matchChar('"');
 					inpos = inmark2;
 					match = !match;
 					if (match) {
 						match = ruleChar(rule);
 					}
 				} while (match);
 				match = true;
 			}
 			if (match) {
 				match = matchChar('"');
 			}
 		}
 		if (match) {
 			match = ruleSpacing(rule);
 		}
 		// note that failure is an implied alternative
 		// so is error recovery (just before fail test)
 		if (!match) {
 			inpos = inmark;
 			outpos = outmark;
 			return fail(rule, outstart);
 		}
 		return succeed(rule);
 	}
 
 	boolean ruleCLOSE(Node parent) {
 		// CLOSE~ <- ')'~ Spacing~
 		return singleCharLexRule(parent, ')', "CLOSE");
 	}
 
 	boolean ruleOPEN(Node parent) {
 		// OPEN~ <- '('~ Spacing~
 		return singleCharLexRule(parent, '(', "OPEN");
 	}
 
 	boolean ruleSUPPRESS(Node parent) {
 		// SUPPRESS <- '~'~ Spacing~
 		int outstart = outpos;
 		if (sameRule("SUPPRESS")) return out[outstart].success;
 		
 		int inmark = inpos;
 		int outmark = outpos;
 		boolean match;
 		match = matchChar('~');
 		if (match) {
 			int outmark1 = outpos;
 			match = ruleSpacing(parent);
 			outpos = outmark1;
 		}
 		if (!match) {
 			outpos = outmark;
 			inpos = inmark;
 			return false;
 		}
 		Node rule = new Node("SUPPRESS", parent, inmark);
 		out[outpos++] = rule;
 		
 		return succeed(rule);
 	}
 
 	boolean ruleLEFTARROW(Node parent) {
 		// LEFTARROW~~ <- ('<-' / '=') Spacing # Changed
 		// note match code generation pattern
 		int outstart = outpos;
 		if (sameRule("STAR")) return out[outstart].success;
 		
 		boolean match = false;
 		int outmark = outpos;
 		int inmark = inpos;
 		
 		match = matchLiteral("<-");
 		if (!match) {
 			if (match = inpos < inend && in[inpos] == '=')
 				inpos++;
 		}
 		if (match) {
 			match = ruleSpacing(parent);
 		}
 		if (!match) {
 			outpos = outmark;
 			inpos = inmark;
 			return false;
 		}
 
 		return true;
 	}
 
 	boolean ruleDEFSUPPRESS(Node parent) {
 		// DEFSUPPRESS <- SUPPRESS (SUPPRESS / NUM)?
 		int outstart = outpos;
 		if (sameRule("DEFSUPPRESS")) return out[outstart].success;
 		Node rule = new Node("DEFSUPPRESS", parent, inpos);
 		out[outpos++] = rule;
 		
 		boolean match = ruleSUPPRESS(rule);
 		if (match) {
 			match = ruleSUPPRESS(rule);
 			if (!match) {
 				match = ruleNUM(rule);
 			}
 			match = true;
 		}
 		if (!match) return fail(rule, outstart);
 		return succeed(rule);
 	}
 	
 	boolean ruleNUM(Node parent) {
 		// NUM <- [0-9]+ Spacing~
 		int outstart = outpos;
 		if (sameRule("NUM")) return out[outstart].success;
 		Node rule = new Node("NUM", parent, inpos);
 		out[outpos++] = rule;
 		
 		boolean match;
 		match = matchRange("09");
 		if (match) {
 			do {
 				match = matchRange("09");
 			} while (match);
 			match = true;
 		}
 		if (match) {
 			int outmark = outpos;
 			match = ruleSpacing(rule);
 			outpos = outmark;
 		}
 		if (!match) return fail(rule, outstart);
 		return succeed(rule);
 	}
 
 	boolean ruleIdentifier(Node parent) {
 		// Identifier <- IdentStart~ IdentCont~* Spacing~
 		int outstart = outpos;
 		if (sameRule("Identifier")) return out[outstart].success;
 		
 		int inmark = inpos;
 		int outmark = outpos;
 		boolean match = ruleIdentStart(parent);
 		if (match) {
 			do {
 				int outmark1 = outpos;
 				match = ruleIdentCont(parent);
 				outpos = outmark1;
 			} while (match);
 			match = true;
 		}
 		if (match) {
 			match = ruleSpacing(parent);
 		}
 		if (!match) {
 			outpos = outmark;
 			inpos = inmark;
 			return false;
 		}
 		
 		// note the node must be created with inmark NOT inpos!!!!!
 		Node rule = new Node("Identifier", parent, inmark);
 		out[outpos++] = rule;
 		return succeed(rule);
 	}
 
 	boolean ruleIdentStart(Node parent) {
 		// IdentStart~ <- [a-zA-Z_]
 		boolean match;
 		match = matchRange("azAZ");
 		if (!match)
 			match = matchChar('_');
 		if (!match) return false;
 		return true;
 	}
 
 	boolean ruleIdentCont(Node parent) {
 		// IdentCont~ <- IdentStart / [0-9]
 		int inmark = inpos;
 		int outmark = outpos;
 		boolean match = ruleIdentStart(parent);
 		if (!match)
 			match = matchRange("09");
 		if (!match) {
 			outpos = outmark;
 			inpos = inmark;
 			return false;
 		}
 		return true;
 	}
 
 	boolean ruleSpacing(Node rule) {
 		// Spacing~~ <- (Space / Comment)*
 		boolean match;
 		do {
 			match = ruleSpace(rule);
 			if (!match)
 				match = ruleComment(rule);
 		} while (match);
 		return true;
 	}
 
 	boolean ruleSpace(Node rule) {
 		// Space~~ <- ' ' / '\t' / EndOfLine
 		int inmark = inpos;
 		int outmark = outpos;
 		boolean match = matchChar(' ');
 		if (!match) {
 			match = matchChar('\t');
 		}
 		if (!match) {
 			match = ruleEndOfLine(rule);
 		}
 		if (!match) {
 			inpos = inmark;
 			outpos = outmark;
 			return false;
 		}
 		return true;
 	}
 
 	boolean ruleComment(Node rule) {
 		// Comment~~ <- '#' (!(EndOfLine / EndOfFile) .)* (EndOfLine / EndOfFile)
 		int inmark = inpos;
 		boolean match;
 		match = matchChar('#');
 		if (match) {
 			do {
 				int inmark1 = inpos;
 				{
 					match = ruleEndOfLine(rule);
 					if (!match)
 						match = ruleEndOfFile(rule);
 				}
 				inpos = inmark1;
 				match = !match;
 				if (match) {
 					match = matchAny();
 				}
 			} while (match);
 			match = true;
 		}
 		if (match) {
 			match = ruleEndOfLine(rule);
 			if (!match) {
 				match = ruleEndOfFile(rule);
 			}
 		}
 		if (!match) {
 			inpos = inmark;
 			return false;
 		}
 		return true;
 	}
 
 	private boolean matchAny() {
 		if (inpos < inend) {
 			inpos++;
 			return true;
 		}
 		return false;
 	}
 
 	boolean ruleEndOfLine(Node rule) {
 		// EndOfLine~~ <- '\r\n' / '\n' / '\r'
 		int inmark = inpos;
 		int outmark = outpos;
 		boolean match = matchLiteral("\r\n");
 		if (!match)
 			match = matchLiteral("\n");
 		if (!match)
 			match = matchLiteral("\r");
 		if (!match) {
 			outpos = outmark;
 			inpos = inmark;
 			return false;
 		}
 		return true;
 	}
 
 	private boolean succeed(Node rule) {
 		rule.success = true;
 		rule.length = inpos - rule.offset;
 		rule.nextout = outpos;
     if (lastFail != null && rule.offset >= lastFail.offset)
       lastFail = null;
 		return true;
 	}
 	
 	private boolean fail(Node rule, int outstart) {
 		outpos = outstart;
 		inpos = rule.offset;
     if (lastFail == null || rule.offset > lastFail.offset)
       lastFail = rule;
 		return false;
 	}
 	
 	private boolean sameRule(String name) {
 		ensureOut();
 		Node node = out[outpos];
 		if (node != null && node.name == name && node.offset == inpos) {
 			outpos = node.nextout;
 			inpos = node.offset + node.length;
 			return true;
 		}
 		return false;
 	}
 	
 	private void ensureOut() {
 		if (outpos == out.length) {
 			Node[] tmp = new Node[outpos<<1];
 			System.arraycopy(out,0,tmp,0,outpos);
 			out = tmp;
 		}
 	}
 
 	private Node[] pack() {
 		out = Node.pack(out, outpos);
 		outpos = out.length;
 		return out;
 	}
 	
 	private boolean matchSet(String set) {
 		if (inpos == inend) return false;
 		if (set.indexOf(in[inpos]) >= 0) {
 			inpos++;
 			return true;
 		}
 		return false;
 	}
 	
 	private final boolean matchChar(char c) {
 		if (inpos == inend || in[inpos] != c) return false;
 		inpos++;
 		return true;
 	}
 
 	private boolean matchRange(String range) {
 		if (inpos == inend) return false;
 		int inmark = inpos;
 		char c = in[inpos];
 		for (int i = 0, n = range.length(); inpos < inend && i < n; i += 2) {
 			if (range.charAt(i) <= c && c <= range.charAt(i+1)) {
 				inpos++;
 				return true;
 			}
 		}
 		inpos = inmark;
 		return false;
 	}
 	
 	private boolean matchLiteral(String literal) {
 		int inmark = inpos;
 		int i = 0;
 		int len = literal.length();
 		while (inpos < inend && i < len && literal.charAt(i) == in[inpos]) {
 			i++;
 			inpos++;
 		}
 		if (i < literal.length()) {
 			inpos = inmark;
 			return false;
 		}
 		return true;
 	}
 }
 
