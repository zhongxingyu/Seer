 package com.dmillerw.brainfuckInterpreter;
 
 public class Token {
 
 	public static final char DATA_INC = '>';
 	public static final char DATA_DEC = '<';
 	public static final char BYTE_INC = '+';
 	public static final char BYTE_DEC = '-';
 	public static final char BYTE_IN = ',';
 	public static final char BYTE_OUT = '.';
 	public static final char BRACKET_OPEN = '[';
 	public static final char BRACKET_CLOSE = ']';
 	
 	public static char getToken(int id) {
 		if (id == 0) {
 			return DATA_INC;
 		} else if (id == 1) {
 			return DATA_DEC;
 		} else if (id == 2) {
 			return BYTE_INC;
 		} else if (id == 3) {
 			return BYTE_DEC;
 		} else if (id == 4) {
			return BYTE_IN;
		} else if (id == 5) {
 			return BYTE_OUT;
 		} else if (id == 6) {
 			return BRACKET_OPEN;
 		} else if (id == 7) {
 			return BRACKET_CLOSE;
 		}
 		
 		return BYTE_INC;
 	}
 	
 }
