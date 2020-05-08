 package com.nerds;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.InputStreamReader;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.List;
 
 @SuppressWarnings("serial")
 class AntScriptBreak extends Exception{}
 
 @SuppressWarnings("serial")
 class AntScriptContinue extends Exception{}
 
 public class AntScriptCore {
 	public static int VERSION_MAJOR = 0;
 	public static int VERSION_MINOR = 1;
 	
 	private String source;
 	private int curPos;
 	private AntScriptMemory mem = new AntScriptMemory();
 	private int errorCode = 0;
 	private static final int CONDITIONED = 0;
 	private static final int NOT_CONDITIONED = 1;
 	private int _state = CONDITIONED;
 	public static final int ERR_OK = 0;
 	public static final int ERR_SYNTAX = 1;
 	public static final int ERR_INVALID_PARAM = 2;
 	public static final int ERR_NO_SUCH_FUNC = 3;
 	public static final int ERR_NO_SUCH_VAR = 4;
 	public static final int ERR_TYPE_MISMATCH = 5;
 	public static final int ERR_IO = 6;
 	public static final int ERR_USER = 7;
 	
 	
 	private class Token {
 		static final int TOKEN_NUMBER = 0;
 		static final int TOKEN_STRING = 1;
 		static final int TOKEN_IDENT = 2;
 		static final int TOKEN_LCURL = 3;
 		static final int TOKEN_RCURL = 4;
 		static final int TOKEN_LPAREN = 5;
 		static final int TOKEN_RPAREN = 6;
 		static final int TOKEN_PLUS = 7;
 		static final int TOKEN_MINUS = 8;
 		static final int TOKEN_MUL = 9;
 		static final int TOKEN_DIV = 10;
 		static final int TOKEN_EQ = 11;
 		static final int TOKEN_NEQ = 12;
 		static final int TOKEN_GT = 13;
 		static final int TOKEN_GTE = 14;
 		static final int TOKEN_LT = 15;
 		static final int TOKEN_LTE = 16;
 		static final int TOKEN_AND = 17;
 		static final int TOKEN_OR = 18;
 		static final int TOKEN_NOT = 19;
 		static final int TOKEN_IF = 20;
 		static final int TOKEN_ELSE = 21;
 		static final int TOKEN_WHILE = 22;
 		static final int TOKEN_COMMA = 23;
 		static final int TOKEN_AT = 24;
 		static final int TOKEN_SEMICOLON = 25;
 		static final int TOKEN_CONTINUE = 26;
 		static final int TOKEN_BREAK = 27;
 		static final int TOKEN_COMMENT = 28;
 		static final int NULL_TOKEN = -1;
 		private String text;
 		private int token;
 		public Token(int tokenType, String text){
 			this.text = text;
 			this.token = tokenType;
 		}
 	}
 	
 	private char see(){
 		return this.source.charAt(curPos);
 	}
 	private char consume(){
 		return this.source.charAt(curPos++);
 	}
 	private String seeMany(int n){
 		String rst = "";
 		for(int i = 0;i<n;i++){
 			rst += this.source.charAt(curPos+i);
 		}
 		return rst;
 	}
 	private String consumeMany(int n){
 		String rst = "";
 		for(int i = 0;i<n;i++){
 			rst += this.source.charAt(curPos+i);
 			
 		}
 		curPos+=n;
 		return rst;
 	}
 	private void skip_whitespace(){
 		try {
 			while(true){
 				if(seeMany(1).matches("[\\ \\n\\r\\t]")){
 					consumeMany(1);
 				} else if(seeMany(1).equals("#")){
 					while(!seeMany(1).matches("[\\n\\r]")){
 						consumeMany(1);
 					}
 					consumeMany(1);
 				} else {
 					break;
 				}
 			}
 		} catch(Exception e) {
 			
 		}
 	}
 	private Token token_ident(){
 		try {
 			String rst = "";
 			if((see()+"").matches("[a-zA-Z_]")){
 				rst += consume();
 			} else {
 				return null;
 			}
 			while((see()+"").matches("[a-zA-Z0-9_]")){
 				rst += consume();
 			}
 			return new Token(Token.TOKEN_IDENT, rst);
 		} catch(Exception e) {	}
 		return null;
 	}
 	private Token token_string(){
 		try {
 			String rst = "";
 			if(seeMany(1).equals("'")){
 				consume();
 			} else {
 				return null;
 			}
 			while(!(seeMany(2).charAt(0) != '\\' && seeMany(2).charAt(1) == '\'')){
 				rst += consume();
 			}
 			rst += consume();
 			if(seeMany(1).equals("'")){
 				consume();
 			} else {
 				return null;
 			}
 			return new Token(Token.TOKEN_STRING, rst);
 		} catch(Exception e) { }
 		return null;
 	}
 	private Token token_number(){
 		try {
 			String rst = "";
 			if((see()+"").matches("[1-9]")){
 				rst += consume();
 			} else {
 				return null;
 			}
 			while((see()+"").matches("[0-9]")){
 				rst += consume();
 			}
 			return new Token(Token.TOKEN_NUMBER, rst);
 		} catch(Exception e) {	}
 		return null;
 	}
 	private Token token_plus(){
 		try{
 			if(seeMany(1).equals("+")){
 				return new Token(Token.TOKEN_PLUS, consumeMany(1));
 			}
 		} catch(Exception e) {	}
 		return null;
 	}
 	private Token token_minus(){
 		try {
 			if(seeMany(1).equals("-")){
 				return new Token(Token.TOKEN_MINUS, consumeMany(1));
 			}
 		} catch(Exception e) {	}
 		return null;
 	}
 	private Token token_mul(){
 		try{
 			if(seeMany(1).equals("*")){
 				return new Token(Token.TOKEN_MUL, consumeMany(1));
 			}
 		} catch(Exception e) {	}
 		return null;
 	}
 	private Token token_div(){
 		try{
 			if(seeMany(1).equals("/")){
 				return new Token(Token.TOKEN_DIV, consumeMany(1));
 			}
 		} catch(Exception e) {	}
 		return null;
 	}
 	private Token token_lparen(){
 		try{
 			if(seeMany(1).equals("(")){
 				return new Token(Token.TOKEN_LPAREN, consumeMany(1));
 			}
 		} catch(Exception e) {	}
 		return null;
 	}
 	private Token token_rparen(){
 		try{
 			if(seeMany(1).equals(")")){
 				return new Token(Token.TOKEN_RPAREN, consumeMany(1));
 			}
 		} catch(Exception e) {	}
 		return null;
 	}
 	private Token token_lcurl(){
 		try{
 			if(seeMany(1).equals("{")){
 				return new Token(Token.TOKEN_LCURL, consumeMany(1));
 			}
 		} catch(Exception e) {	}
 		return null;
 	}
 	private Token token_rcurl(){
 		try{
 			if(seeMany(1).equals("}")){
 				return new Token(Token.TOKEN_RCURL, consumeMany(1));
 			}
 		} catch(Exception e) {	}
 		return null;
 	}
 	private Token token_while(){
 		try{
 			if(seeMany(5).equals("while")){
 				return new Token(Token.TOKEN_WHILE, consumeMany(5));
 			}
 		} catch(Exception e) {	}
 		return null;
 	}
 	private Token token_if(){
 		try{
 			if(seeMany(2).equals("if")){
 				return new Token(Token.TOKEN_IF, consumeMany(2));
 			}
 		} catch(Exception e){
 			
 		}
 		return null;
 	}
 	private Token token_else(){
 		try{
 			if(seeMany(4).equals("else")){
 				return new Token(Token.TOKEN_ELSE, consumeMany(4));
 			}
 		} catch(Exception e){
 			
 		}
 		return null;
 	}
 	private Token token_semicolon(){
 		try{
 			if(seeMany(1).equals(";")){
 				return new Token(Token.TOKEN_SEMICOLON, consumeMany(1));
 			}
 		} catch(Exception e) {	}
 		return null;
 	}
 	private Token token_eq(){
 		try{
 			if(seeMany(1).equals("=")){
 				return new Token(Token.TOKEN_EQ, consumeMany(1));
 			}
 		} catch(Exception e) {	}
 		return null;
 	}
 	private Token token_neq(){
 		try{
 			if(seeMany(2).equals("!=")){
 				return new Token(Token.TOKEN_NEQ, consumeMany(2));
 			}
 		} catch(Exception e) {	}
 		return null;
 	}
 	private Token token_gt(){
 		try{
 			if(seeMany(1).equals(">")){
 				return new Token(Token.TOKEN_GT, consumeMany(1));
 			}
 		} catch(Exception e) {	}
 		return null;
 	}
 	private Token token_lt(){
 		try{
 			if(seeMany(1).equals("<")){
 				return new Token(Token.TOKEN_LT, consumeMany(1));
 			}
 		} catch(Exception e) {	}
 		return null;
 	}
 	private Token token_gte(){
 		try{
 			if(seeMany(2).equals(">=")){
 				return new Token(Token.TOKEN_GTE, consumeMany(2));
 			}
 		} catch(Exception e) {	}
 		return null;
 	}
 	private Token token_lte(){
 		try{
 			if(seeMany(2).equals("<=")){
 				return new Token(Token.TOKEN_LTE, consumeMany(2));
 			}
 		} catch(Exception e) {	}
 		return null;
 	}
 	private Token token_at(){
 		try{
 			if(seeMany(1).equals("@")){
 				return new Token(Token.TOKEN_AT, consumeMany(1));
 			}
 		} catch(Exception e) {	}
 		return null;
 	}
 	private Token token_comma(){
 		try{
 			if(seeMany(1).equals(",")){
 				return new Token(Token.TOKEN_COMMA, consumeMany(1));
 			}
 		} catch(Exception e) {	}
 		return null;
 	}
 	private Token token_or(){
 		try{
 			if(seeMany(2).equals("or")){
 				return new Token(Token.TOKEN_OR, consumeMany(2));
 			}
 		} catch(Exception e) {	}
 		return null;
 	}
 	private Token token_and(){
 		try{
 			if(seeMany(3).equals("and")){
 				return new Token(Token.TOKEN_AND, consumeMany(3));
 			}
 		} catch(Exception e) {	}
 		return null;
 	}
 	private Token token_not(){
 		try{
 			if(seeMany(3).equals("not")){
 				return new Token(Token.TOKEN_NOT, consumeMany(3));
 			}
 		} catch(Exception e) {	}
 		return null;
 	} 
 	private Token token_break(){
 		try{
 			if(seeMany(5).equals("break")){
 				return new Token(Token.TOKEN_BREAK, consumeMany(5));
 			}
 		} catch(Exception e) {	}
 		return null;
 	} 
 	private Token token_continue(){
 		try{
 			if(seeMany(8).equals("continue")){
 				return new Token(Token.TOKEN_CONTINUE, consumeMany(8));
 			}
 		} catch(Exception e) {	}
 		return null;
 	} 
 	private Token nextToken(){
 		Token t = _nextToken();
 		//if(t==null) System.out.println("null"); else System.out.println(t.text);
 		return t == null ? new Token(Token.NULL_TOKEN, "") : t;
 	}
 	private Token _nextToken(){
 		Token t;
 
 		skip_whitespace();
 		t = token_semicolon();
 		if(t != null){
 			return t;
 		}
 		t = token_comma();
 		if(t != null){
 			return t;
 		}
 		t = token_at();
 		if(t != null){
 			return t;
 		}
 		t = token_not();
 		if(t != null){
 			return t;
 		}
 		t = token_or();
 		if(t != null){
 			return t;
 		}
 		t = token_and();
 		if(t != null){
 			return t;
 		}
 		t = token_plus();
 		if(t != null){
 			return t;
 		}
 		t = token_minus();
 		if(t != null){
 			return t;
 		}
 		t = token_mul();
 		if(t != null){
 			return t;
 		}
 		t = token_div();
 		if(t != null){
 			return t;
 		}
 		t = token_eq();
 		if(t != null){
 			return t;
 		}
 		t = token_neq();
 		if(t != null){
 			return t;
 		}
 		t = token_gt();
 		if(t != null){
 			return t;
 		}
 		t = token_gte();
 		if(t != null){
 			return t;
 		}
 		t = token_lt();
 		if(t != null){
 			return t;
 		}
 		t = token_lte();
 		if(t != null){
 			return t;
 		}
 		t = token_lparen();
 		if(t != null){
 			return t;
 		}
 		t = token_rparen();
 		if(t != null){
 			return t;
 		}
 		t = token_lcurl();
 		if(t != null){
 			return t;
 		}
 		t = token_rcurl();
 		if(t != null){
 			return t;
 		}
 		t = token_if();
 		if(t != null){
 			return t;
 		}
 		t = token_else();
 		if(t != null){
 			return t;
 		}
 		t = token_while();
 		if(t != null){
 			return t;
 		}
 		t = token_break();
 		if(t != null){
 			return t;
 		}
 		t = token_continue();
 		if(t != null){
 			return t;
 		}
 		t = token_ident();
 		if(t != null){
 			return t;
 		}
 		t = token_number();
 		if(t != null){
 			return t;
 		}
 		t = token_string();
 		if(t != null){
 			return t;
 		}
 	
 		return null;
 	}
 	
 	private Object expr_atom() throws Exception { 
 		int fallback = this.curPos;
 		Object v;
 		Token t = nextToken();
 		switch(t.token){
 			case Token.TOKEN_NUMBER:
 				if(_state == CONDITIONED)
 					return new Long(t.text);
 				else 
 					return false;
 			case Token.TOKEN_STRING:
 				if(_state == CONDITIONED)
 					return t.text;
 				else
 					return false;
 			case Token.TOKEN_IDENT:
 				try {
 					if(_state == CONDITIONED)
 						return this.mem.vars.get(t.text);
 					else 
 						return false;
 				} catch(Exception e){
 					setErrorCode(AntScriptCore.ERR_NO_SUCH_VAR);
 					System.err.println("Runtime Error: No such variable: " + t.text);
 				}
 				break;
 			case Token.TOKEN_LPAREN:
 				v = expr();
 				if(nextToken().token == Token.TOKEN_RPAREN) {
 					if(_state == CONDITIONED)
 						return v;
 					else 
 						return false;
 				}
 				break;
 			case Token.TOKEN_AT: 
 				Token func_name = nextToken();
 				if(func_name.token == Token.TOKEN_IDENT){
 					if(nextToken().token == Token.TOKEN_LPAREN) {
 						ArrayList<Object> params = new ArrayList<Object>();
 						Token tt;
 						while(true){
 							try {
 								v = expr();
 								params.add(v);
 							}catch(Exception e){ }
 							
 							tt = nextToken();
 							if (tt.token == Token.TOKEN_RPAREN) {
 								// Call
 								if(_state == CONDITIONED) {
 									AntScriptFunction func;
 									try{
 										func = (AntScriptFunction)this.mem.vars.get(func_name.text);
 										Object[] args = new Object[params.size()];
 										params.toArray(args); 
 										return func.body(args);
 									} catch(Exception e){
 										setErrorCode(AntScriptCore.ERR_NO_SUCH_FUNC);
 										System.err.println("Runtime Error: No such function: " + func_name.text);
 										
 									}
 								} else {
 									return false;
 								}
 							} else if (tt.token == Token.TOKEN_COMMA) {
 								continue;
 							} 
 							break;
 						}
 					}
 				}
 				break;
 			default:
 		}
 
 		this.curPos = fallback;
 		return null;
 	}
 	private Object expr_mul() throws Exception {
 		int fallback = this.curPos;
 		Object v = expr_atom();
 		if(v != null){
 			fallback = this.curPos;
 			Token t = nextToken();
 			if(t.token == Token.TOKEN_MUL){
 				Object vv = expr_mul();
 				if(vv != null) {
 					if(_state == CONDITIONED)
 						if(v instanceof Long){
 							if(vv instanceof Long ) {
 								return (Object)((Long)v * (Long)vv);
 							} else {
 								setErrorCode(AntScriptCore.ERR_TYPE_MISMATCH);
 								System.err.println("Runtime Error: Type mismatch.");
 								throw new Exception("Type mismatch.");
 							}
 						} else {
 							setErrorCode(AntScriptCore.ERR_TYPE_MISMATCH);
 							System.err.println("Runtime Error: Type mismatch.");
 							throw new Exception("Type mismatch.");
 						}
 					else
 						return false;
 				}
 			} else if(t.token == Token.TOKEN_DIV){
 				Object vv = expr_mul();
 				if(vv != null) {
 					if(_state == CONDITIONED)
 						if(v instanceof Long){
 							if(vv instanceof Long) {
 								return (Object)((Long)v / (Long)vv);
 							} else {
 								setErrorCode(AntScriptCore.ERR_TYPE_MISMATCH);
 								System.err.println("Runtime Error: Type mismatch.");
 								throw new Exception("Type mismatch.");
 							}
 						} else {
 							setErrorCode(AntScriptCore.ERR_TYPE_MISMATCH);
 							System.err.println("Runtime Error: Type mismatch.");
 							throw new Exception("Type mismatch.");
 						}
 					else
 						return false;
 				}
 			}
 			this.curPos = fallback;
 			return v;
 		}
 		this.curPos = fallback;
 		return null;
 	}
 	private Object expr_add() throws Exception {
 		int fallback = this.curPos;
 		Object v = expr_mul(); 
 		if(v != null){
 			fallback = this.curPos;
 			Token t = nextToken();
 			if(t.token == Token.TOKEN_PLUS){
 				Object vv = expr_add();
 				if(vv != null) {
 					if(_state == CONDITIONED)
 						if(v instanceof Long){
 							if(vv instanceof Long) {
 								return (Object)((Long)v + (Long)vv);
 							} else if(vv instanceof String) {
 								return (Object)((Long)v + (String)vv);
 							} else {
 								setErrorCode(AntScriptCore.ERR_TYPE_MISMATCH);
 								System.err.println("Runtime Error: Type mismatch.");
 								throw new Exception("Type mismatch.");
 							}
 						} else if (v instanceof String) { 
 							if(vv instanceof Long) {
 								return (Object)((String)v + (Long)vv);
 							} else if(vv instanceof String) {
 								return (Object)((String)v + (String)vv);
 							} else {
 								setErrorCode(AntScriptCore.ERR_TYPE_MISMATCH);
 								System.err.println("Runtime Error: Type mismatch.");
 								throw new Exception("Type mismatch.");
 							}	
 						} else {
 							setErrorCode(AntScriptCore.ERR_TYPE_MISMATCH);
 							System.err.println("Runtime Error: Type mismatch.");
 							throw new Exception("Type mismatch.");
 						}
 					else
 						return false;
 				}
 			} else if(t.token == Token.TOKEN_MINUS){
 				Object vv = expr_add();
 				if(vv != null) {
 					if(_state == CONDITIONED)
 						if(v instanceof Long){
 							if(vv instanceof Long) {
 								return (Object)((Long)v - (Long)vv);
 							} else {
 								setErrorCode(AntScriptCore.ERR_TYPE_MISMATCH);
 								System.err.println("Runtime Error: Type mismatch.");
 								throw new Exception("Type mismatch.");
 							}
 						} else {
 							setErrorCode(AntScriptCore.ERR_TYPE_MISMATCH);
 							System.err.println("Runtime Error: Type mismatch.");
 							throw new Exception("Type mismatch.");
 						}
 					else
 						return false;
 				}
 			}
 			this.curPos = fallback;
 			return v;
 		}
 		this.curPos = fallback;
 		return null;
 	}
 	private Object expr_comp() throws Exception {
 		int fallback = this.curPos;
 		Object v = expr_add(); 
 		if(v != null){
 			fallback = this.curPos;
 			Token t = nextToken();
 			if(t.token == Token.TOKEN_EQ){
 				Object vv = expr_comp();
 				if(vv != null) {
 					if(_state == CONDITIONED)
 						if(v instanceof Long){
 							if(vv instanceof Long) { 
 								return (Object)(v.equals(vv));
 							} else if(vv instanceof String) {
 								return (Object)( ((Long)v).toString().equals((String)vv));
 							} else {
 								setErrorCode(AntScriptCore.ERR_TYPE_MISMATCH);
 								System.err.println("Runtime Error: Type mismatch.");
 								throw new Exception("Type mismatch.");
 							}
 						} else if (v instanceof String) { 
 							if(vv instanceof Long) {
 								return (Object)(((String)v).equals(((Long)vv).toString()));
 							} else if(vv instanceof String) {
 								return (Object)(((String)v).equals((String)vv));
 							} else {
 								setErrorCode(AntScriptCore.ERR_TYPE_MISMATCH);
 								System.err.println("Runtime Error: Type mismatch.");
 								throw new Exception("Type mismatch.");
 							}	
 						} else {
 							setErrorCode(AntScriptCore.ERR_TYPE_MISMATCH);
 							System.err.println("Runtime Error: Type mismatch.");
 							throw new Exception("Type mismatch.");
 						}
 					else
 						return false;
 				}
 			} else if(t.token == Token.TOKEN_NEQ){
 				Object vv = expr_comp();
 				if(vv != null) {
 					if(_state == CONDITIONED)
 						if(v instanceof Long){
 							if(vv instanceof Long) {
 								return (Object)((Long)v != (Long)vv);
 							} else if(vv instanceof String) {
 								return (Object)(! ((Long)v).toString().equals((String)vv));
 							} else {
 								setErrorCode(AntScriptCore.ERR_TYPE_MISMATCH);
 								System.err.println("Runtime Error: Type mismatch.");
 								throw new Exception("Type mismatch.");
 							}
 						} else if (v instanceof String) { 
 							if(vv instanceof Long) {
 								return (Object)(!((String)v).equals(((Long)vv).toString()));
 							} else if(vv instanceof String) {
 								return (Object)(!((String)v).equals((String)vv));
 							} else {
 								setErrorCode(AntScriptCore.ERR_TYPE_MISMATCH);
 								System.err.println("Runtime Error: Type mismatch.");
 								throw new Exception("Type mismatch.");
 							}	
 						} else {
 							setErrorCode(AntScriptCore.ERR_TYPE_MISMATCH);
 							System.err.println("Runtime Error: Type mismatch.");
 							throw new Exception("Type mismatch.");
 						}
 					else
 						return false;
 				}
 			} else if(t.token == Token.TOKEN_GT){
 				Object vv = expr_comp();
 				if(vv != null) {
 					if(_state == CONDITIONED)
 						if(v instanceof Long){
 							if(vv instanceof Long) {
 								return (Object)((Long)v > (Long)vv);
 							} else {
 								setErrorCode(AntScriptCore.ERR_TYPE_MISMATCH);
 								System.err.println("Runtime Error: Type mismatch.");
 								throw new Exception("Type mismatch.");
 							}
 						} else {
 							setErrorCode(AntScriptCore.ERR_TYPE_MISMATCH);
 							System.err.println("Runtime Error: Type mismatch.");
 							throw new Exception("Type mismatch.");
 						}
 					else 
 						return false;
 				}
 			} else if(t.token == Token.TOKEN_GTE){
 				Object vv = expr_comp();
 				if(vv != null) {
 					if(_state == CONDITIONED)
 						if(v instanceof Long){
 							if(vv instanceof Long) {
 								return (Object)((Long)v >= (Long)vv);
 							} else {
 								setErrorCode(AntScriptCore.ERR_TYPE_MISMATCH);
 								System.err.println("Runtime Error: Type mismatch.");
 								throw new Exception("Type mismatch.");
 							}
 						} else {
 							setErrorCode(AntScriptCore.ERR_TYPE_MISMATCH);
 							System.err.println("Runtime Error: Type mismatch.");
 							throw new Exception("Type mismatch.");
 						}
 					else
 						return false;
 				}
 			} else if(t.token == Token.TOKEN_LT){
 				Object vv = expr_comp();
 				if(vv != null) {
 					if(_state == CONDITIONED)
 						if(v instanceof Long){
 							if(vv instanceof Long) {
 								return (Object)((Long)v < (Long)vv);
 							} else {
 								setErrorCode(AntScriptCore.ERR_TYPE_MISMATCH);
 								System.err.println("Runtime Error: Type mismatch.");
 								throw new Exception("Type mismatch.");
 							}
 						} else {
 							setErrorCode(AntScriptCore.ERR_TYPE_MISMATCH);
 							System.err.println("Runtime Error: Type mismatch.");
 							throw new Exception("Type mismatch.");
 						}
 					else
 						return false;
 				}
 			} else if(t.token == Token.TOKEN_LTE){
 				Object vv = expr_comp();
 				if(vv != null) {
 					if(_state == CONDITIONED)
 						if(v instanceof Long){
 							if(vv instanceof Long) {
 								return (Object)((Long)v <= (Long)vv);
 							} else {
 								setErrorCode(AntScriptCore.ERR_TYPE_MISMATCH);
 								System.err.println("Runtime Error: Type mismatch.");
 								throw new Exception("Type mismatch.");
 							}
 						} else {
 							setErrorCode(AntScriptCore.ERR_TYPE_MISMATCH);
 							System.err.println("Runtime Error: Type mismatch.");
 							throw new Exception("Type mismatch.");
 						}
 					else
 						return false;
 				}
 			}
 			this.curPos = fallback;
 			return v;
 		} 
 		this.curPos = fallback;
 		return null;
 	}
 	private Object expr_logical() throws Exception {
 		int fallback = this.curPos;
 		Object v = expr_comp(); 
 		if(v != null){
 			fallback = this.curPos;
 			Token t = nextToken();
 			if(t.token == Token.TOKEN_AND){
 				Object vv = expr_mul();
 				if(vv != null) {
 					if(_state == CONDITIONED)
 						if(v instanceof Long){
 							if(vv instanceof Long) {
 								return (Object)((Boolean)v && (Boolean)vv);
 							} else {
 								setErrorCode(AntScriptCore.ERR_TYPE_MISMATCH);
 								System.err.println("Runtime Error: Type mismatch.");
 								throw new Exception("Type mismatch.");
 							}
 						} else {
 							setErrorCode(AntScriptCore.ERR_TYPE_MISMATCH);
 							System.err.println("Runtime Error: Type mismatch.");
 							throw new Exception("Type mismatch.");
 						}
 					else
 						return false;
 				}
 			} else if(t.token == Token.TOKEN_OR){
 				Object vv = expr_mul();
 				if(vv != null) {
 					if(_state == CONDITIONED)
 						if(v instanceof Long || v instanceof Boolean){
 							if(vv instanceof Long || v instanceof Boolean) {
 								return (Object)((Boolean)v || (Boolean)vv);
 							} else {
 								setErrorCode(AntScriptCore.ERR_TYPE_MISMATCH);
 								System.err.println("Runtime Error: Type mismatch.");
 								throw new Exception("Type mismatch.");
 							}
 						} else {
 							setErrorCode(AntScriptCore.ERR_TYPE_MISMATCH);
 							System.err.println("Runtime Error: Type mismatch.");
 							throw new Exception("Type mismatch.");
 						}
 					else
 						return false;
 				}
 			} 
 			this.curPos = fallback;
 			return v;
 		}
 		this.curPos = fallback;
 		return null;
 	}
 	private Object expr_not() throws Exception {
 		int fallback = this.curPos;
 		Token t = nextToken();
 		Object v;
 		if(t.token == Token.TOKEN_NOT) {
 			v = expr_logical(); 	
 			if(v != null) {
 				v = !(Boolean)v;
 			}
 		} else {
 			this.curPos = fallback;
 			v = expr_logical();
 		}
 		return v;
 		
 	}
 	private Object expr() throws Exception{
 		int fallback = this.curPos;
 		Object v = expr_not();
 		if(v != null){
 			return v;
 		}
 		this.curPos = fallback;
 		//setErrorCode(AntScriptCore.ERR_SYNTAX);
 		//System.err.println("Syntax Error: Expression is invalid.");
 		throw new Exception("");
 	}
 	private boolean stmt_assign(){
 		int fallback = this.curPos;
 		try{
 			Token ident;
 			ident = nextToken();
 			if(ident.token == Token.TOKEN_IDENT ) {
 				if(nextToken().token == Token.TOKEN_EQ){ 
 					if(_state == CONDITIONED)
 						this.mem.vars.put(ident.text, expr());
 					return true;
 					
 				}
 			}
 		} catch(Exception e){ }
 		this.curPos = fallback;
 		return false;
 	}
 	private boolean stmt_if () throws AntScriptBreak, AntScriptContinue {
 		int fallback = this.curPos;
 		Exception dl = null;
 		int old_state;
 		try{
 			if(nextToken().token == Token.TOKEN_IF ) {
 				Object v;
 				v = expr();
 				if(v != null) {
 					if(nextToken().token == Token.TOKEN_LCURL) {
 						old_state = _state;
 						if(_state == CONDITIONED) _state = (Boolean) is_true(v) ? CONDITIONED : NOT_CONDITIONED;
 						try{
 							stmt();
 						} catch(AntScriptContinue e) {
 							if(_state == CONDITIONED)
 								dl = e;
 						} catch(AntScriptBreak e){
 							if(_state == CONDITIONED) 
 								dl = e;
 						}
 						_state = old_state;
 						
 						if(nextToken().token == Token.TOKEN_RCURL) {
 							fallback = this.curPos;
 							
 							if(nextToken().token == Token.TOKEN_ELSE) {
 								fallback = this.curPos;
 								Token t = nextToken();
 								if (t.token == Token.TOKEN_LCURL) {
 									old_state = _state;
 									if(_state == CONDITIONED) _state = !is_true(v) ? CONDITIONED : NOT_CONDITIONED;
 									try{
 										stmt();
 									} catch(AntScriptContinue e) {
 										if(_state == CONDITIONED)
 											dl = e;
 									} catch(AntScriptBreak e){
 										if(_state == CONDITIONED) 
 											dl = e;
 									}
 									_state = old_state;
 									
 									if(nextToken().token == Token.TOKEN_RCURL) {
 										if(dl != null) throw dl;
 										return true;
 									} else {
 										setErrorCode(AntScriptCore.ERR_SYNTAX);
 										System.err.println("Syntax Error: curly bracket(}) is missing.");
 									}
 								} else if(t.token == Token.TOKEN_IF) {
 									this.curPos = fallback;
 									Boolean tb = stmt_if();
 									if (tb) if(dl != null) throw dl;
 									return tb;
 								} else {
 									this.curPos = fallback;
 									return false;
 								}
 							} else {
 								// No else statement
 								this.curPos = fallback;
 								if(dl != null) throw dl;
 								return true;	
 							}
 						} else {
 							setErrorCode(AntScriptCore.ERR_SYNTAX);
 							System.err.println("Syntax Error: curly bracket(}) is missing.");
 						}
 						
 					} else {
 						setErrorCode(AntScriptCore.ERR_SYNTAX);
 						System.err.println("Syntax Error: curly bracket({) is missing.");
 					}
 				}
 			}
 		} 
 		catch(AntScriptBreak e) { throw e; }
 		catch(AntScriptContinue e) { throw e; }
 		catch(Exception e){ }
 		this.curPos = fallback;
 		return false;
 	}
 	private boolean is_true(Object v) {
 		if(v instanceof Long){
 			return (Long)v > 0;
 		} else if(v instanceof String){
 			return ((String)v).length() > 0;
 		} else {
 			return (Boolean)v;
 		}
 	}
 	private boolean stmt_while() {
 		int fallback = this.curPos;
 		int while_f = 0;
 		int old_state;
 		Object v;
 		if (nextToken().token == Token.TOKEN_WHILE) {
 			while_f = this.curPos;
 			while(true) {
 				this.curPos = while_f;	
 				try {
 					v = expr();
 				} catch (Exception e) { v = false; /* to do: error msg */ }
 				
 				if(nextToken().token == Token.TOKEN_LCURL) {
 					old_state = _state;
 					if(_state == CONDITIONED) _state = is_true(v) ? CONDITIONED : NOT_CONDITIONED;
 					try {
 						stmt();
 					} catch (AntScriptContinue e) {
 						continue;
 					} catch (AntScriptBreak e) {
 						break;
 					}
 					_state = old_state;
 					
 					if(nextToken().token == Token.TOKEN_RCURL) {
 						if(!is_true(v)) return true;
 						
 					} else {
 						setErrorCode(AntScriptCore.ERR_SYNTAX);
 						System.err.println("Syntax Error: curly bracket(}) is missing.");
 						break;
 					}
 				} else {
 					setErrorCode(AntScriptCore.ERR_SYNTAX);
 					System.err.println("Syntax Error: curly bracket({) is missing.");
 					break;
 				}
 				
 			}
 		}
 		this.curPos = fallback;
 		return false;
 	}
 	private boolean stmt_break() throws AntScriptBreak {
 		int fallback = this.curPos;
 		if(nextToken().token == Token.TOKEN_BREAK) {
 			if(_state == CONDITIONED)
 				throw new AntScriptBreak();
 			return true;
 		}
 		this.curPos = fallback;
 		return false;
 	}
 	private boolean stmt_continue() throws AntScriptContinue {
 		int fallback = this.curPos;
 		if(nextToken().token == Token.TOKEN_CONTINUE) {
 			if(_state == CONDITIONED)
 				throw new AntScriptContinue(); 
 			return true;
 		}
 		this.curPos = fallback;
 		return false;
 	}
 	private boolean stmt_expr(){
 		int fallback = this.curPos;
 		Object v;
 		try {
 			v = expr();
 			if (v != null){
 				return true;
 			}
 		} catch(Exception e){
 		}
 		this.curPos = fallback;
 		return false;
 	}
	private void semi_colon(){
 		int fallback = this.curPos;
 		try {
 			if(nextToken().token == Token.TOKEN_SEMICOLON) {
			//}else {
			//	System.err.println("Syntax Error: semi-colon(;) is missing.");
 				return;
 			}
 		}catch(Exception e){
 			
 		}
 		this.curPos = fallback;
 		return;
 	}
 	private void stmt() throws AntScriptBreak, AntScriptContinue{
 		Exception dl = null;
 		while(true){
			if(stmt_assign()){ semi_colon(); continue; }
 			try {
 				if(stmt_if()) continue;
 			} catch(AntScriptBreak e){
				semi_colon();
 				if(_state == CONDITIONED && dl == null)
 					dl = e;
 			} catch(AntScriptContinue e){
				semi_colon();
 				if(_state == CONDITIONED && dl == null)
 					dl = e;
 			}
 			
 			if(stmt_while()) continue;
			if(stmt_expr()) {semi_colon(); continue; }
 			try {
 				stmt_break();
 			} catch(AntScriptBreak e){
				semi_colon();
 				if(_state == CONDITIONED && dl == null)
 					dl = e;
 			}
 			try {
 				stmt_continue();
 			} catch(AntScriptContinue e){
				semi_colon();
 				if(_state == CONDITIONED && dl == null)
 					dl = e;
 			}
			semi_colon();
 			break;
 		}
 		if(dl != null) {
 			if(dl instanceof AntScriptBreak) {
 				throw (AntScriptBreak)dl;
 			} else if(dl instanceof AntScriptContinue) {
 				throw (AntScriptContinue)dl;
 			}
 		}
 	}
 	
 	public void addVariable(String name, Object value) {
 		this.mem.vars.put(name, value);
 	}
 	public void addFunction(String name, AntScriptFunction func) {
 		this.mem.vars.put(name, func);
 	}
 	public Object getVariable(String name){
 		return this.mem.vars.get(name);
 	}
 	
 	public boolean run(String source) throws AntScriptException{
 		this.source = source;
 		this.curPos = 0;
 		this.errorCode = AntScriptCore.ERR_OK;
 		this._state = AntScriptCore.CONDITIONED;
 		try {
 			stmt();
 		} catch (AntScriptContinue e) {
 			setErrorCode(AntScriptCore.ERR_SYNTAX);
 			System.err.println("Syntax Error: Illegal continue.");
 		} catch (AntScriptBreak e) {
 			setErrorCode(AntScriptCore.ERR_SYNTAX);
 			System.err.println("Syntax Error: Illegal break.");
 		}
 		return this.errorCode == AntScriptCore.ERR_OK;
 	}
 	public boolean runFile(String path) throws AntScriptException, Exception{
 		String source = "";
 		String tmp;
 		FileInputStream fis = new FileInputStream(path);
 
 		BufferedReader br = new BufferedReader(new InputStreamReader(fis));
 		while((tmp = br.readLine()) != null){
 			source += tmp + '\n';
 		}
 		
 		return run(source);
 	}
 	
 	public int getErrorCode(){
 		return this.errorCode;
 	}
 	public void setErrorCode(int errorCode) {
 		this.errorCode = errorCode;
 	}
 	
 	public boolean loadJar(String path, String classpath, boolean justTry) {
 		File file = new File(path);
 		try {    
 			URL[] jarfile = {new URL("jar", "","file:" + file.getAbsolutePath()+"!/") };    
 			URLClassLoader cl = URLClassLoader.newInstance(jarfile);   
 			Class userRuntime = cl.loadClass(classpath);
 			final Object obj = userRuntime.newInstance();
 			final Method[] methods = userRuntime.getDeclaredMethods();
 			for(int i = 0;i < methods.length; i ++ ){
 				final int ii = i;
 				addFunction(methods[i].getName(), new AntScriptFunction() {
 					public Object body(Object[] args){
 						try {
 							return methods[ii].invoke(obj, args);
 						} catch (IllegalArgumentException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						} catch (IllegalAccessException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						} catch (InvocationTargetException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 						return new Long(0);
 					}
 				});
 			}
 		} catch (MalformedURLException e) {
 			if(justTry) return false;
 			e.printStackTrace();
 		} catch (SecurityException e) {
 			if(justTry) return false;
 			e.printStackTrace();
 		} catch (IllegalArgumentException e) {
 			if(justTry) return false;
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			if(justTry) return false;
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			if(justTry) return false;
 			e.printStackTrace();
 		} catch (InstantiationException e) {
 			if(justTry) return false;
 			e.printStackTrace();
 		}
 		return true;
 	}
 	
 }
