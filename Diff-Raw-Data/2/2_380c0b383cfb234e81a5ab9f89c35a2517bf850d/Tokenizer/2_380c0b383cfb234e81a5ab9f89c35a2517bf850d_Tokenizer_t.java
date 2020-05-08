 package Generator;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Scanner;
 
 import Generator.Token.op_code;
 
 /** 
  * Tokenizes Regex and CharacterClass specifications.
  * 
  * @author eric
  *
  */
 public class Tokenizer {
 	String code;
 	int pos;
 	boolean potentialEpsilon; 	// Alerts the tokenizer of incoming epsilon match
 	boolean regexMode;
 	List<String> ids;
 	
 	static String[] keywords = {
 								 "(", ")", "|", "*", "+", "$", "\\",	// Regex Keywords
 								 ".", "[", "]", "^", "-", "IN"			// CharacterClass Keywords
 							   };
 	
 	static String[] skipper = { " ", "\t", "\n", "\r" };
 	
 	public Tokenizer(String in) {
 		code = in;
 		pos = 0;
 		potentialEpsilon = true;
 		regexMode = true;
 		ids = new LinkedList<String>();
 	}
 	
 	public int pos() {
 		return pos;
 	}
 	
 	public List<String> ids() {
 		return ids;
 	}
 	
 	public void tokenize(String in) {
 		code = in;
 		reset();
 	}
 	
 	public void reset() {
 		pos = 0;
 		potentialEpsilon = true;
 		regexMode = true;
 //		ids.clear();  //<-- Couldn't hurt to save these
 	}
 	
 	public Token peek(int n) {
 		// Save state
 		int _pos = pos;
 		boolean _potentialEpsilon = potentialEpsilon;
 		boolean _regexMode = regexMode;
 		
 		// Peek ahead
 		Token nextToken = null;
		while(n-- > 0) {
 			nextToken = next();
 		}
 		
 		// Restore state
 		pos = _pos;
 		potentialEpsilon = _potentialEpsilon;
 		regexMode = _regexMode;
 		
 		return nextToken;
 	}
 	
 	public Token peek() {
 		return peek(1);
 	}
 	
 	public boolean hasNext() {
 		return peek().operand != op_code.eoi;
 	}
 	
 	public Token next() {
 		if(pos == code.length()) {
 			if(potentialEpsilon) {
 				potentialEpsilon = false;
 				return new Token(op_code.epsilon, "");
 			} else {
 				return new Token(op_code.eoi, "");
 			}
 		}
 		// Consume whitespace
 		skip();
 		// Next keyword
 		String op = nextKeyword();
 		// Parse token
 		Token token;
 		switch (op) {
 			case "(" :
 				if(regexMode) {
 					potentialEpsilon = true;
 					token = new Token(op_code.left_paren, op);
 					pos++;
 				} else {
 					potentialEpsilon = false;
 					token = new Token(op_code.cls_char, op);
 					pos++;
 				}
 				break;
 			case ")" :
 				if(regexMode) {
 					if(potentialEpsilon) {
 						potentialEpsilon = false;
 						token = new Token(op_code.epsilon, "");
 					} else {
 						token = new Token(op_code.right_paren, op);
 						pos++;
 					}
 				} else {
 					potentialEpsilon = false;
 					token = new Token(op_code.cls_char, op);
 					pos++;
 				}
 				break;
 			case "|" :
 				if(regexMode) {
 					if(potentialEpsilon) {
 						potentialEpsilon = false;
 						token = new Token(op_code.epsilon, "");
 					} else {
 						potentialEpsilon = true;
 						token = new Token(op_code.or, op);
 						pos++;
 					}
 				} else {
 					potentialEpsilon = false;
 					token = new Token(op_code.cls_char, op);
 					pos++;
 				}
 				break;
 			case "*" :
 				if(regexMode) {
 					token = new Token(op_code.star, op);
 					pos++;
 				} else {
 					potentialEpsilon = false;
 					token = new Token(op_code.cls_char, op);
 					pos++;
 				}
 				break;
 			case "+" :
 				if(regexMode) {
 					token = new Token(op_code.plus, op);
 					pos++;
 				} else {
 					potentialEpsilon = false;
 					token = new Token(op_code.cls_char, op);
 					pos++;
 				}
 				break;
 			case "$" :
 				if(regexMode) {
 					potentialEpsilon = false;
 					String id = findId();
 					token = new Token(op_code.id, id);
 					pos += id.length();
 				} else {
 					potentialEpsilon = false;
 					token = new Token(op_code.cls_char, op);
 					pos++;
 				}
 				break;
 			case "[" :
 				if(regexMode) {
 					regexMode = false;
 					potentialEpsilon = true;
 					token = new Token(op_code.left_brac, op);
 					pos++;
 				} else {
 					potentialEpsilon = false;
 					token = new Token(op_code.cls_char, op);
 					pos++;
 				}
 				break;
 			case "]" :
 				if(!regexMode) {
 					if(potentialEpsilon) {
 						potentialEpsilon = false;
 						token = new Token(op_code.epsilon, "");
 					} else {
 						regexMode = true;
 						token = new Token(op_code.right_brac, op);
 						pos++;
 					}
 				} else {
 					potentialEpsilon = false;
 					token = new Token(op_code.re_char, op);
 					pos++;
 				}
 				break;
 			case "." :
 				if(regexMode) {
 					potentialEpsilon = false;
 					token = new Token(op_code.match_all, op);
 					pos++;
 				} else {
 					potentialEpsilon = false;
 					token = new Token(op_code.cls_char, op);
 					pos++;
 				}
 				break;
 			case "-" :
 				if(!regexMode) {
 					token = new Token(op_code.range, op);
 					pos++;
 				} else {
 					potentialEpsilon = false;
 					token = new Token(op_code.re_char, op);
 					pos++;
 				}
 				break;
 			case "^" :
 				if(!regexMode) {
 					potentialEpsilon = false;
 					token = new Token(op_code.exclude, op);
 					pos++;
 				} else {
 					potentialEpsilon = false;
 					token = new Token(op_code.re_char, op);
 					pos++;
 				}
 				break;
 			case "IN" :
 				if(regexMode) {
 					token = new Token(op_code.in, op);
 					pos += 2;
 				} else {
 					potentialEpsilon = false;
 					token = new Token(op_code.cls_char, "I");
 					pos++;
 				}
 				break;
 			case "\\" :
 				if(regexMode) {
 					token = new Token(op_code.re_char, code.substring(pos+1,pos+2));
 				} else {
 					token = new Token(op_code.cls_char, code.substring(pos+1,pos+2));
 				}
 				potentialEpsilon = false;
 				pos += 2;
 				break;
 			default :
 				if(regexMode) {
 					token = new Token(op_code.re_char, op);
 				} else {
 					token = new Token(op_code.cls_char, op);
 				}
 				potentialEpsilon = false;
 				pos++;
 		}
 		// Consume trailing whitespace
 		skip();
 		return token;
 	}
 	
 	private void skip() {
 		if(pos == code.length() || !regexMode) {
 			return;
 		} else if(isSpace(code.substring(pos,pos+1))) {
 			pos += 1;
 			skip();
 		}
 	}
 	
 	private boolean isSpace(String in) {
 		for(String s : skipper) {
 			if(in.equals(s)) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	private String findId() {
 		// Find existing match
 		String id = "";
 		for(String i : ids) {
 			int match = code.indexOf(i, pos);
 			if(match == pos) {
 				id = i;
 			}
 		}
 		// Create new identifier greedily
 		if(id.isEmpty()) {
 			Scanner S = new Scanner(code.substring(pos));
 			id = S.next();
 			S.close();
 			// Add to list of ids
 			ids.add(id);
 		}
 		
 		return id;
 	}
 	
 	private String nextKeyword() {
 		String keyword = code.substring(pos,pos+1);
 		// For matching keywords with length > 1
 		for(String k : keywords) {
 			int dist = code.substring(pos).indexOf(k);
 			if(dist == 0) {
 				keyword = k;
 			}
 		}
 		return keyword;
 	}
 }
