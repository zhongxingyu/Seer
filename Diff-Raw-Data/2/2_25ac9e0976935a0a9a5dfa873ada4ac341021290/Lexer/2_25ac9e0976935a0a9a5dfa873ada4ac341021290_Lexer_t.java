 // vi:ai:noet sta sw=4 ts=4 sts=0
 package edu.kit.pp.minijava;
 
 import edu.kit.pp.minijava.tokens.*;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.PushbackReader;
 import java.io.Reader;
 
 
 public class Lexer {
 	private PushbackReader _reader;
 
 	public Lexer(Reader reader) throws IOException {
 		_reader = new PushbackReader(new BufferedReader(reader));
 	}
 
 	public Token next() throws IOException {
 		while (true) {
 			int c = read();
 			switch (c) {
 			case ' ':
 			case '\n':
 			case '\r':
 			case '\t':
 				break;
 			case '/':
 				c = read();
 				switch (c) {
 				case '*':
 					skipComment();
 					break;
 				case '=':
 					return operator("/=");
 				default:
 					unread(c);
 					return operator("/");
 				}
 				break;
 			case '!':
 				return operator(ifThenElse('=', "!=", "!"));
 			case '(':
 				return operator("(");
 			case ')':
 				return operator(")");
 			case '*':
 				return operator(ifThenElse('=', "*=", "*"));
 			case '+':
 				c = read();
 				switch (c) {
 				case '+': return operator("++");
 				case '=': return operator("+=");
 				default:
 					unread(c);
 					return operator("+");
 				}
 			case ',':
 				return operator(",");
 			case '-':
 				c = read();
 				switch (c) {
 				case '-': return operator("--");
 				case '=': return operator("-=");
 				default:
 					unread(c);
 					return operator("-");
 				}
 			case '.':
 				return operator(".");
 			case ':':
 				return operator(":");
 			case ';':
 				return operator(";");
 			case '<':
 				c = read();
 				switch (c) {
 				case '<':
 					return operator(ifThenElse('=', "<<=", "<<"));
 				case '=':
 					return operator("<=");
 				default:
 					unread(c);
 					return operator("<");
 				}
 			case '=':
 				return operator(ifThenElse('=', "==", "="));
 			case '>':
 				c = read();
 				switch (c) {
 				case '>':
 					c = read();
 					switch (c) {
 					case '>':
 						return operator(ifThenElse('=', ">>>=", ">>>"));
 					case '=':
 						return operator(">>=");
 					default:
 						unread(c);
 						return operator(">>");
 					}
 				case '=':
 					return operator(">=");
 				default:
 					unread(c);
 					return operator(">");
 				}
 			case '?':
 				return operator("?");
 			case '%':
 				return operator(ifThenElse('=', "%=", "%"));
 			case '&':
 				c = read();
 				switch (c) {
 				case '&':
 					return operator("&&");
 				case '=':
 					return operator("&=");
 				default:
 					unread(c);
 					return operator("&");
 				}
 			case '[':
 				return operator("[");
 			case ']':
 				return operator("]");
 			case '^':
 				return operator(ifThenElse('=', "^=", "^"));
 			case '{':
 				return operator("{");
 			case '}':
 				return operator("}");
 			case '~':
 				return operator("~");
 			case '|':
 				c = read();
 				switch (c) {
 				case '|':
 					return operator("||");
 				case '=':
 					return operator("|=");
 				default:
 					unread(c);
 					return operator("|");
 				}
 			case -1: return eof(); /*return new Token("EOF");*/
 			default:
 				if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || c == '_') {
 					return lexIdentifier(c);
 				}
 				else if (c >= '0' && c <= '9') {
 					return lexInteger(c);
 				}
 				else {
 					return new Token("MIST");
 				}
 			}
 		}
 	}
 
 	private void skipComment() throws IOException {
 		while (true) {
 			int c = read();
 			switch (c) {
 			case '*':
 				c = read();
 				if (c == '/') return;
 			case -1: return;
 			default: ;
 			}
 		}
 	}
 
 	private Token lexIdentifier(int c) throws IOException {
 		StringBuffer name = new StringBuffer();
 		name.append((char) c);
 		while (true) {
 			c = read();
 			if ((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') || c == '_' || (c >= '0' && c <= '9')) {
 				name.append((char) c);
 			}
 			else {
 				unread(c);
 				return identifier(name.toString());
 			}
 		}
 	}
 
 	private Token lexInteger(int c) throws IOException {
 		StringBuffer name = new StringBuffer();
 		name.append((char) c);
 		if (c == '0') {
 			return integerLiteral(name.toString());
 		}
 		else {
 			while (true) {
 				c = read();
 				if (c >= '0' && c <= '9') {
 					name.append((char) c);
 				}
 				else {
 					unread(c);
 					return integerLiteral(name.toString());
 				}
 			}
 		}
 	}
 
 	private int read() throws IOException {
 		return _reader.read();
 	}
 
 	private void unread(int c) throws IOException {
 		_reader.unread(c);
 	}
 
 	private String ifThenElse(int c, String t1, String t2) throws IOException {
 		int n = read();
 		if (n == c) return t1;
 		else unread(n);
 		return t2;
 	}
 
         private Token keyword(String s) {
 	    return new Keyword(s);
         }
 
         private Token operator(String s) {
             return new Operator(s);
         }
 
         private Token identifier(String s) {
 	    return new Identifier(s);
         }
 
 	private Token integerLiteral(String s) {
	    return new IntegerLiteral(s);
 	}
 
 	private Token eof() {
 	    return new Eof();
 	}
 }
