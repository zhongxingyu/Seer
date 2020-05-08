 // Copyright (c) 2004-2013, Benoit PERROT.
 // All rights reserved.
 //
 // Redistribution and use in source and binary forms, with or without
 // modification, are permitted provided that the following conditions are
 // met:
 //
 //     * Redistributions of source code must retain the above copyright
 //       notice, this list of conditions and the following disclaimer.
 //
 //     * Redistributions in binary form must reproduce the above
 //       copyright notice, this list of conditions and the following
 //       disclaimer in the documentation and/or other materials provided
 //       with the distribution.
 //
 //     * Neither the name of the White Hole Project nor the names of its
 //       contributors may be used to endorse or promote products derived
 //       from this software without specific prior written permission.
 //
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 // "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 // LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 // A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 // HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 // SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 // LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 // DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 // THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 // (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 // OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 package org.whitehole.infra.json;
 
 import java.io.Closeable;
 import java.io.IOException;
 import java.io.Reader;
 import java.math.BigDecimal;
 
 import org.whitehole.infra.io.InfinitelyBufferedReader;
 import org.whitehole.infra.json.stream.JsonGenerator;
 import org.whitehole.infra.json.stream.JsonLocation;
 import org.whitehole.infra.json.stream.JsonParsingException;
 
 public interface JsonReader extends Closeable {
 
 	public void close() throws IOException;
 
 	public JsonStructure read() throws Exception;
 
 	public JsonArray readArray() throws Exception;
 
 	public JsonObject readObject() throws Exception;
 
 	//
 	static class Impl implements JsonReader {
 
 		static class Token {
 
 			enum Code {
 				UNKNOWN, END, DOT, LEFT_PARENTHESIS, RIGHT_PARENTHESIS, LEFT_BRACKET, RIGHT_BRACKET, LEFT_BRACE, RIGHT_BRACE, COMMA, COLUMN, NULL, FALSE, TRUE, STRING, NUMBER
 			}
 
 			public Token(Code _code, String _word, int _line, int _column) {
 				code = _code;
 				word = _word;
 				line = _line;
 				column = _column;
 			}
 
 			public final Code code;
 			public final String word;
 			public final int line;
 			public final int column;
 		}
 
 		static class Lexer implements Closeable {
 
 			private final InfinitelyBufferedReader _r;
 
 			private int _currentLine = 1;
 			private int _currentColumn = 0;
 
 			public Lexer(InfinitelyBufferedReader r) {
 				_r = r;
 			}
 
 			public void close() throws IOException {
 				_r.close();
 			}
 
 			public boolean read(Token.Code c) throws IOException {
 				return read().code == c;
 			}
 
 			static class CharacterPredicates {
 
 				public static boolean isBlank(int c) {
 					return c == ' ' || c == '\t' || c == '\n' || c == '\r';
 				}
 
 				public static boolean isDecimalDigit(int c) {
 					return '0' <= c && c <= '9';
 				}
 
 				public static boolean isHexDigit(int c) {
 					return ('0' <= c && c <= '9') || ('a' <= c && c <= 'f') || ('A' <= c && c <= 'F');
 				}
 
 				public static boolean isLetter(int c) {
 					return ('a' <= c && c <= 'z') || ('A' <= c && c <= 'Z') || c == '_';
 				}
 			}
 
 			public Token read() throws IOException {
 				int c = _r.read();
 				++_currentColumn;
 
 				// Skip blanks
 				while (CharacterPredicates.isBlank(c)) {
 					if (c == '\n') {
 						++_currentLine;
 						_currentColumn = 0;
 					}
 					c = _r.read();
 					++_currentColumn;
 				}
 
 				// Scan
 				switch (c) {
 					case -1:
 						return new Token(Token.Code.END, null, _currentLine, _currentColumn);
 					case '.':
 						return new Token(Token.Code.DOT, ".", _currentLine, _currentColumn);
 					case '(':
 						return new Token(Token.Code.LEFT_PARENTHESIS, "(", _currentLine, _currentColumn);
 					case ')':
 						return new Token(Token.Code.LEFT_PARENTHESIS, ")", _currentLine, _currentColumn);
 					case '{':
 						return new Token(Token.Code.LEFT_BRACE, "{", _currentLine, _currentColumn);
 					case '}':
 						return new Token(Token.Code.RIGHT_BRACE, "}", _currentLine, _currentColumn);
 					case '[':
 						return new Token(Token.Code.LEFT_BRACKET, "[", _currentLine, _currentColumn);
 					case ']':
 						return new Token(Token.Code.RIGHT_BRACKET, "]", _currentLine, _currentColumn);
 					case ',':
 						return new Token(Token.Code.COMMA, ",", _currentLine, _currentColumn);
 					case ':':
 						return new Token(Token.Code.COLUMN, ":", _currentLine, _currentColumn);
 					case '"':
 					case '\'': {
 						final int column = _currentColumn;
 						int delimiter = c;
 
 						final StringBuilder b = new StringBuilder();
 
 						c = _r.peek();
 						while (c != -1 && c != delimiter) {
 							b.append((char) _r.read());
 							++_currentColumn;
 							c = _r.peek();
 						}
 
 						if (c == delimiter) {
 							_r.read();
 							++_currentColumn;
 							return new Token(Token.Code.STRING, b.toString(), _currentLine, column);
 						}
 						else {
 							throw new JsonParsingException("unterminated string", new JsonLocation.Impl(column, _currentLine));
 						}
 					}
 					default: {
 						final int column = _currentColumn;
 						final StringBuilder b = new StringBuilder();
 						b.append((char) c);
 						if (CharacterPredicates.isLetter(c)) {
 							c = _r.peek();
 							while (c != -1 && (CharacterPredicates.isDecimalDigit(c) || CharacterPredicates.isLetter(c))) {
 								b.append((char) _r.read());
 								++_currentColumn;
 								c = _r.peek();
 							}
 
 							final String s = b.toString();
 							if (s.equals("null"))
 								return new Token(Token.Code.NULL, s, _currentLine, column);
 							else if (s.equals("false"))
 								return new Token(Token.Code.FALSE, s, _currentLine, column);
 							else if (s.equals("true"))
 								return new Token(Token.Code.TRUE, s, _currentLine, column);
 						}
 						else if (CharacterPredicates.isDecimalDigit(c)) {
 							if (c == '0' && _r.peek() == 'x') {
 								b.append((char) _r.read());
 								++_currentColumn;
 								c = _r.peek();
 								while (c != -1 && CharacterPredicates.isHexDigit(c)) {
 									b.append((char) _r.read());
 									++_currentColumn;
 									c = _r.peek();
 								}
 							}
 							else {
 								c = _r.peek();
 								while (c != -1 && CharacterPredicates.isDecimalDigit(c)) {
 									b.append((char) _r.read());
 									++_currentColumn;
 									c = _r.peek();
 								}
 							}
 							return new Token(Token.Code.NUMBER, b.toString(), _currentLine, column);
 						}
 						return new Token(Token.Code.UNKNOWN, b.toString(), _currentLine, column);
 					}
 				}
 			}
 		}
 
 		static class Parser {
 
 			private void reportSyntaxError(Token t) throws JsonParsingException {
 				throw new JsonParsingException("syntax error on token " + "'" + t.word + "' (" + t.code.toString() + ")", new JsonLocation.Impl(t.column,
 						t.line));
 			}
 
 			private JsonGenerator _g;
 
 			public void parse(Lexer l, JsonGenerator g) throws Exception {
 				_g = g;
 				Token t = l.read();
 				if (isFirstOfValue(t.code))
 					parseValue(null, l, t);
 				else
 					reportSyntaxError(t);
 			}
 
 			// Object ::= '{' ( Pair ( ',' Pair )* )? '}'
 			//
 			static private boolean isFirstOfObject(Token.Code code) {
 				return code == Token.Code.LEFT_BRACE;
 			}
 
 			private void parseObject(String name, Lexer l) throws Exception {
 				if (name == null)
 					_g.writeStartObject();
 				else
 					_g.writeStartObject(name);
 
 				Token t = l.read();
 				if (isFirstOfPair(t.code)) {
 					parsePair(l, t);
 					t = l.read();
 					while (t.code == Token.Code.COMMA) {
 						parsePair(l, l.read());
 						t = l.read();
 					}
 				}
 
 				if (t.code != Token.Code.RIGHT_BRACE)
 					reportSyntaxError(t);
 
 				_g.writeEnd();
 			}
 
 			// Pair ::= String ':' Value
 			//
 			static private boolean isFirstOfPair(Token.Code code) {
 				return code == Token.Code.STRING;
 			}
 
 			private void parsePair(Lexer l, Token name) throws Exception {
 				if (isFirstOfPair(name.code)) {
 					final Token t = l.read();
 					if (t.code == Token.Code.COLUMN)
 						parseValue(name.word, l, l.read());
 					else
 						reportSyntaxError(t);
 				}
 				else
 					reportSyntaxError(name);
 			}
 
 			// Array ::= '[' ( Value ( ',' Value )* )? ']'
 			//
 			static private boolean isFirstOfArray(Token.Code code) {
 				return code == Token.Code.LEFT_BRACKET;
 			}
 
 			private void parseArray(String name, Lexer l) throws Exception {
 				if (name == null)
 					_g.writeStartArray();
 				else
 					_g.writeStartArray(name);
 
 				Token t = l.read();
 				if (isFirstOfValue(t.code)) {
 
 					parseValue(null, l, t);
 
 					t = l.read();
 					while (t.code == Token.Code.COMMA) {
 
 						parseValue(null, l, l.read());
 
 						t = l.read();
 					}
 				}
 
 				if (t.code != Token.Code.RIGHT_BRACKET)
 					reportSyntaxError(t);
 
 				_g.writeEnd();
 			}
 
 			// Value ::= Literal | Object | Array
 			//
 			static private boolean isFirstOfValue(Token.Code code) {
 				return isFirstOfLiteral(code) || isFirstOfObject(code) || isFirstOfArray(code);
 			}
 
 			private void parseValue(String name, Lexer l, Token lookahead) throws Exception {
 				if (isFirstOfLiteral(lookahead.code))
 					parseLiteral(name, lookahead);
 				else if (isFirstOfObject(lookahead.code))
 					parseObject(name, l);
 				else if (isFirstOfArray(lookahead.code))
 					parseArray(name, l);
 				else
 					reportSyntaxError(lookahead);
 			}
 
 			// Literal ::= null | boolean | string | number
 			//
 			static private boolean isFirstOfLiteral(Token.Code code) {
 				return code == Token.Code.NULL || code == Token.Code.FALSE || code == Token.Code.TRUE || code == Token.Code.STRING || code == Token.Code.NUMBER;
 			}
 
 			private void parseLiteral(String name, Token t) throws Exception {
 				switch (t.code) {
 					case NULL:
 						if (name == null)
 							_g.writeNull();
 						else
 							_g.writeNull(name);
 						break;
 					case FALSE:
 						if (name == null)
 							_g.write(false);
 						else
 							_g.write(name, false);
 						break;
 					case TRUE:
 						if (name == null)
 							_g.write(true);
 						else
 							_g.write(name, true);
 						break;
 					case STRING:
 						if (name == null)
 							_g.write(t.word);
 						else
 							_g.write(name, t.word);
 						break;
 					case NUMBER:
 						if (name == null)
 							_g.write(new BigDecimal(t.word));
 						else
							_g.write(new BigDecimal(t.word));
 						break;
 					default:
 						reportSyntaxError(t); // Should not be there
 						break;
 				}
 			}
 
 		}
 
 		//
 
 		public Impl(Reader r) {
 			_l = new Lexer(new InfinitelyBufferedReader(r));
 		}
 
 		private final Lexer _l;
 
 		public JsonStructure read() throws Exception {
 			final Parser p = new Parser();
 			final JsonGenerator.Builder b = new JsonGenerator.Builder();
 			p.parse(_l, b);
 			return b.get();
 		}
 
 		public JsonArray readArray() throws Exception {
 			return (JsonArray) read();
 		}
 
 		public JsonObject readObject() throws Exception {
 			return (JsonObject) read();
 		}
 
 		public void close() throws IOException {
 			_l.close();
 		}
 	}
 }
