 /*******************************************************************************
  * Copyright (c) 2009 xored software, Inc.  
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html  
  *
  * Contributors:
  *     xored software, Inc. - initial API and Implementation (Vladimir Belov)
  *******************************************************************************/
 
 package org.eclipse.dltk.javascript.ast;
 
 import java.util.HashMap;
 
 import org.eclipse.dltk.javascript.parser.JSParser;
 
 public class Keywords {
 
 	public static final String FUNCTION = "function";
 
 	public static final String NULL = "null";
 
 	public static final String RETURN = "return";
 
 	public static final String IF = "if";
 
 	public static final String ELSE = "else";
 
 	public static final String SWITCH = "switch";
 
 	public static final String CASE = "case";
 
 	public static final String BREAK = "break";
 
 	public static final String CONTINUE = "continue";
 
 	public static final String DEFAULT = "default";
 
 	public static final String FOR = "for";
 
 	public static final String EACH = "each";
 
 	public static final String WHILE = "while";
 
 	public static final String DO = "do";
 
 	public static final String VAR = "var";
 
 	public static final String CONST = "const";
 
 	public static final String IN = "in";
 
 	public static final String NEW = "new";
 
 	public static final String TRUE = "true";
 
 	public static final String FALSE = "false";
 
 	public static final String TRY = "try";
 
 	public static final String CATCH = "catch";
 
 	public static final String FINALLY = "finally";
 
 	public static final String INSTANCEOF = "instanceof";
 
 	public static final String TYPEOF = "typeof";
 
 	public static final String THIS = "this";
 
 	public static final String THROW = "throw";
 
 	public static final String EXPORT = "export";
 
 	public static final String IMPORT = "import";
 
 	public static final String WITH = "with";
 
 	public static final String DELETE = "delete";
 
 	public static final String GET = "get";
 
 	public static final String SET = "set";
 
 	public static final String VOID = "void";
 
 	public static final String XML = "xml";
 
 	public static final String NAMESPACE = "namespace";
 
 	public static final String YIELD = "yield";
 
 	//
 	//
 	//
 	//
 
 	public static final char SPACE_CHAR = ' ';
 
 	// public static final String NEW_LINE = "\r\n";
 
 	//
 	//
 	//
 
 	public static final char LP = '(';
 
 	public static final char RP = ')';
 
 	public static final char LC = '{';
 
 	public static final char RC = '}';
 
 	public static final char LB = '[';
 
 	public static final char RB = ']';
 
 	//
 	//
 	//
 
 	public static final char DOT = '.';
 
 	public static final char COMMA = ',';
 
 	public static final char SEMI = ';';
 
 	public static final char COLON = ':';
 
 	//
 	//
 	//
 
 	public static final char STRING_QUOTE = '"';
 
 	//
 	//
 	//
 
 	public static final char ASSIGN = '=';
 
 	public static final String ADDASS = "+=";
 	public static final String SUBASS = "-=";
 	public static final String MULASS = "*=";
 	public static final String DIVASS = "/=";
 	public static final String MODASS = "%="; // it does not present in
 	// JavaScript
 	// 1.5 Reference
 
 	public static final String XORASS = "^=";
 	public static final String ANDASS = "&=";
 	public static final String ORASS = "|=";
 	public static final String SHLASS = "<<=";
 	public static final String SHRASS = ">>=";
 	public static final String SHUASS = ">>>=";
 
 	//
 	//
 	//
 
 	public static final String INC = "++";
 	public static final String DEC = "--";
 
 	public static final char ADD = '+';
 	public static final char SUB = '-';
 	public static final char MUL = '*';
 	public static final char DIV = '/';
 	public static final char MOD = '%';
 	public static final String LAND = "&&";
 	public static final String LOR = "||";
 	public static final char NOT = '!';
 
 	public static final char OR = '|';
 	public static final char XOR = '^';
 	public static final char AND = '&';
 	public static final char INV = '~';
 
 	public static final String SHL = "<<";
 	public static final String SHR = ">>";
 	public static final String SHU = ">>>";
 
 	public static final String EQ = "==";
 	public static final String NEQ = "!=";
 	public static final String SAME = "===";
 	public static final String NSAME = "!==";
 
 	public static final String LTE = "<=";
 	public static final char LT = '<';
 	public static final String GTE = ">=";
 	public static final char GT = '>';
 
 	public static final char HOOK = '?';
 
 	private final static HashMap<Integer, String> map = new HashMap<Integer, String>();
 
 	static {
 		map.put(Integer.valueOf(JSParser.FUNCTION), Keywords.FUNCTION);
 		map.put(Integer.valueOf(JSParser.NULL), Keywords.NULL);
 		map.put(Integer.valueOf(JSParser.RETURN), Keywords.RETURN);
 		map.put(Integer.valueOf(JSParser.IF), Keywords.IF);
 		map.put(Integer.valueOf(JSParser.ELSE), Keywords.IF);
 		map.put(Integer.valueOf(JSParser.SWITCH), Keywords.SWITCH);
 		map.put(Integer.valueOf(JSParser.CASE), Keywords.CASE);
 		map.put(Integer.valueOf(JSParser.BREAK), Keywords.BREAK);
 		map.put(Integer.valueOf(JSParser.CONTINUE), Keywords.CONTINUE);
 		map.put(Integer.valueOf(JSParser.DEFAULT), Keywords.DEFAULT);
 		map.put(Integer.valueOf(JSParser.FOR), Keywords.FOR);
 		map.put(Integer.valueOf(JSParser.EACH), Keywords.EACH);
 		map.put(Integer.valueOf(JSParser.WHILE), Keywords.WHILE);
 		map.put(Integer.valueOf(JSParser.DO), Keywords.DO);
 		map.put(Integer.valueOf(JSParser.VAR), Keywords.VAR);
 		map.put(Integer.valueOf(JSParser.CONST), Keywords.CONST);
 		map.put(Integer.valueOf(JSParser.IN), Keywords.IN);
 		map.put(Integer.valueOf(JSParser.NEW), Keywords.NEW);
 		map.put(Integer.valueOf(JSParser.TRUE), Keywords.TRUE);
 		map.put(Integer.valueOf(JSParser.FALSE), Keywords.FALSE);
 		map.put(Integer.valueOf(JSParser.TRY), Keywords.TRY);
 		map.put(Integer.valueOf(JSParser.CATCH), Keywords.CATCH);
 		map.put(Integer.valueOf(JSParser.FINALLY), Keywords.FINALLY);
 		map.put(Integer.valueOf(JSParser.INSTANCEOF), Keywords.INSTANCEOF);
 		map.put(Integer.valueOf(JSParser.TYPEOF), Keywords.TYPEOF);
 		map.put(Integer.valueOf(JSParser.THIS), Keywords.THIS);
 		map.put(Integer.valueOf(JSParser.THROW), Keywords.THROW);
 		map.put(Integer.valueOf(JSParser.EXPORT), Keywords.EXPORT);
 		map.put(Integer.valueOf(JSParser.IMPORT), Keywords.IMPORT);
 		map.put(Integer.valueOf(JSParser.WITH), Keywords.WITH);
 		map.put(Integer.valueOf(JSParser.DELETE), Keywords.DELETE);
 		map.put(Integer.valueOf(JSParser.GET), Keywords.GET);
 		map.put(Integer.valueOf(JSParser.SET), Keywords.SET);
 		map.put(Integer.valueOf(JSParser.VOID), Keywords.VOID);
 		map.put(Integer.valueOf(JSParser.WXML), Keywords.XML);
 		map.put(Integer.valueOf(JSParser.NAMESPACE), Keywords.NAMESPACE);
 		map.put(Integer.valueOf(JSParser.YIELD), Keywords.YIELD);
 
 		map.put(Integer.valueOf(JSParser.ASSIGN), String
 				.valueOf(Keywords.ASSIGN));
 
 		map.put(Integer.valueOf(JSParser.ADDASS), Keywords.ADDASS);
 		map.put(Integer.valueOf(JSParser.SUBASS), Keywords.SUBASS);
 		map.put(Integer.valueOf(JSParser.MULASS), Keywords.MULASS);
 		map.put(Integer.valueOf(JSParser.DIVASS), Keywords.DIVASS);
 		map.put(Integer.valueOf(JSParser.MODASS), Keywords.MODASS);
 		map.put(Integer.valueOf(JSParser.XORASS), Keywords.XORASS);
 		map.put(Integer.valueOf(JSParser.ANDASS), Keywords.ANDASS);
 		map.put(Integer.valueOf(JSParser.ORASS), Keywords.ORASS);
 		map.put(Integer.valueOf(JSParser.SHLASS), Keywords.SHLASS);
 		map.put(Integer.valueOf(JSParser.SHRASS), Keywords.SHRASS);
 		map.put(Integer.valueOf(JSParser.SHUASS), Keywords.SHUASS);
 
 		//
 		//
 		//
 
 		map.put(Integer.valueOf(JSParser.PINC), Keywords.INC);
 		map.put(Integer.valueOf(JSParser.PDEC), Keywords.DEC);
 		map.put(Integer.valueOf(JSParser.INC), Keywords.INC);
 		map.put(Integer.valueOf(JSParser.DEC), Keywords.DEC);
 		map.put(Integer.valueOf(JSParser.NEG), String.valueOf(Keywords.SUB));
 		map.put(Integer.valueOf(JSParser.POS), String.valueOf(Keywords.ADD));
 
 		map.put(Integer.valueOf(JSParser.ADD), String.valueOf(Keywords.ADD));
 		map.put(Integer.valueOf(JSParser.SUB), String.valueOf(Keywords.SUB));
 		map.put(Integer.valueOf(JSParser.DIV), String.valueOf(Keywords.DIV));
 		map.put(Integer.valueOf(JSParser.MUL), String.valueOf(Keywords.MUL));
 		map.put(Integer.valueOf(JSParser.MOD), String.valueOf(Keywords.MOD));
 
 		map.put(Integer.valueOf(JSParser.LAND), Keywords.LAND);
 		map.put(Integer.valueOf(JSParser.LOR), Keywords.LOR);
 		map.put(Integer.valueOf(JSParser.NOT), String.valueOf(Keywords.NOT));
 
 		map.put(Integer.valueOf(JSParser.OR), String.valueOf(Keywords.OR));
 		map.put(Integer.valueOf(JSParser.XOR), String.valueOf(Keywords.XOR));
 		map.put(Integer.valueOf(JSParser.AND), String.valueOf(Keywords.AND));
 		map.put(Integer.valueOf(JSParser.INV), String.valueOf(Keywords.INV));
 
 		map.put(Integer.valueOf(JSParser.SHL), Keywords.SHL);
 		map.put(Integer.valueOf(JSParser.SHR), Keywords.SHR);
 		map.put(Integer.valueOf(JSParser.SHU), Keywords.SHU);
 
 		map.put(Integer.valueOf(JSParser.EQ), Keywords.EQ);
 		map.put(Integer.valueOf(JSParser.NEQ), Keywords.NEQ);
 		map.put(Integer.valueOf(JSParser.LTE), Keywords.LTE);
 		map.put(Integer.valueOf(JSParser.LT), String.valueOf(Keywords.LT));
 		map.put(Integer.valueOf(JSParser.GTE), Keywords.GTE);
 		map.put(Integer.valueOf(JSParser.GT), String.valueOf(Keywords.GT));
 		map.put(Integer.valueOf(JSParser.SAME), Keywords.SAME);
 		map.put(Integer.valueOf(JSParser.NSAME), Keywords.NSAME);
 
 		map.put(Integer.valueOf(JSParser.INSTANCEOF), Keywords.INSTANCEOF);
 		map.put(Integer.valueOf(JSParser.TYPEOF), Keywords.TYPEOF);
 		map.put(Integer.valueOf(JSParser.IN), Keywords.IN);
 	}
 
 	public static String fromToken(int token) {
 		Integer tokenValue = Integer.valueOf(token);
 		if (map.containsKey(tokenValue)) {
 			return map.get(tokenValue);
 		}
 		throw new IllegalArgumentException(Integer.toString(token));
 	}
 
 	private static String charToHexString(char ch) {
 		return Integer.toHexString(ch).toUpperCase();
 	}
 
 	public static String encodeString(String s, char quoteChar) {
 
 		StringBuffer buffer = new StringBuffer();
 
 		for (int i = 0; i < s.length(); i++) {
 			char ch = s.charAt(i);
 
 			switch (ch) {
 			case '\t':
 				buffer.append("\\t");
 				break;
 
 			case '\b':
 				buffer.append("\\b");
 				break;
 
 			case '\f':
 				buffer.append("\\f");
 				break;
 
 			case '\r':
 				buffer.append("\\r");
 				break;
 
 			case '\n':
 				buffer.append("\\n");
 				break;
 
 			case '\\':
 				buffer.append("\\\\");
 				break;
 
 			default:
 				if (ch == quoteChar) {
 					buffer.append("\\");
 					buffer.append(quoteChar);
 				} else if (ch > 0xfff) {
 					buffer.append("\\u" + charToHexString(ch));
 				} else if (ch > 0xff) {
 					buffer.append("\\u0" + charToHexString(ch));
 				} else if (ch > 0x7f) {
 					buffer.append("\\u00" + charToHexString(ch));
 				} else
 					buffer.append(ch);
 			}
 		}
 		return buffer.toString();
 	}
 
 	public static boolean isKeyword(String value) {
		return value != null && value.length() != 0 && map.containsValue(value);
 	}
}
