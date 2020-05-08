 /**
  * The MIT License (MIT)
  * 
  * Copyright (c) 2013 Twuni
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy of
  * this software and associated documentation files (the "Software"), to deal in
  * the Software without restriction, including without limitation the rights to
  * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
  * the Software, and to permit persons to whom the Software is furnished to do so,
  * subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in all
  * copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
  * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
  * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
  * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
  * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */
 package org.twuni.twoson;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Stack;
 
 public class JSONParser {
 
 	private static enum Event {
 		NONE,
 		OBJECT,
 		ARRAY,
 		OBJECT_KEY
 	}
 
 	private static void burn( byte [] buffer ) {
 		for( int i = 0; i < buffer.length; i++ ) {
 			buffer[i] = 0;
 		}
 	}
 
 	private static void burn( char [] buffer ) {
 		for( int i = 0; i < buffer.length; i++ ) {
 			buffer[i] = 0;
 		}
 	}
 
 	private static boolean isDigit( char c ) {
 		return '0' <= c && c <= '9';
 	}
 
 	private final InputStream in;
 	private final JSONEventListener listener;
 	private byte [] buffer;
 	private int offset;
 
 	private int size;
 
 	private final Stack<Event> scope = new Stack<Event>();
 
 	public JSONParser( InputStream in, JSONEventListener listener ) {
 		this.in = in;
 		this.listener = listener;
 	}
 
 	private double atof( char firstDigit ) throws IOException {
 		double a = 0;
 		int e = 0;
 		char c = firstDigit;
 		boolean negative = false;
 		if( c == '-' ) {
 			negative = true;
 			c = nextChar();
 		}
 		while( isDigit( c ) ) {
 			a = a * 10 + ( c - '0' );
 			c = nextChar();
 		}
 		if( c == '.' ) {
 			c = nextChar();
 			while( isDigit( c ) ) {
 				a = a * 10 + ( c - '0' );
 				e--;
 				c = nextChar();
 			}
 		}
 		if( c == 'e' || c == 'E' ) {
 			int sign = 1;
 			int x = 0;
 			c = nextChar();
 			if( c == '+' ) {
 				// Valid
 			} else if( c == '-' ) {
 				sign = -1;
 			} else {
 				throw new IOException( "Invalid number format" );
 			}
 			c = nextChar();
 			while( isDigit( c ) ) {
 				x = x * 10 + c - '0';
 				c = nextChar();
 			}
 			e += x * sign;
 		}
 		while( e > 0 ) {
 			a *= 10;
 			e--;
 		}
 		while( e < 0 ) {
 			a *= 0.1;
 			e++;
 		}
 		offset--;
 		return negative ? -a : a;
 	}
 
 	private boolean isFinished() {
 		return size <= 0;
 	}
 
 	private char nextChar() throws IOException {
 		if( buffer == null ) {
 			buffer = new byte [64 * 1024];
 			size = in.read( buffer, 0, buffer.length );
 		}
 		if( offset >= size ) {
 			offset = 0;
 			size = in.read( buffer, 0, buffer.length );
 		}
 		if( isFinished() ) {
 			return '\0';
 		}
 		char c = (char) buffer[offset];
 		offset++;
 		return c;
 	}
 
 	public void read() throws IOException {
 
 		try {
 
 			scope.push( Event.NONE );
 			for( char c = nextChar(); c != '\0'; c = nextChar() ) {
 
 				switch( c ) {
 
 					case '{':
 						scope.push( Event.OBJECT );
 						listener.onBeginObject();
 						break;
 
 					case '}':
 						switch( scope.peek() ) {
 							case OBJECT:
 								scope.pop();
 								if( Event.OBJECT_KEY.equals( scope.peek() ) ) {
 									scope.pop();
 								}
 								listener.onEndObject();
 								break;
 							default:
 								throw new IllegalFormatException( c, scope.peek().toString() );
 						}
 						break;
 
 					case '[':
 						scope.push( Event.ARRAY );
 						listener.onBeginArray();
 						break;
 
 					case ']':
 						switch( scope.peek() ) {
 							case ARRAY:
 								scope.pop();
 								if( Event.OBJECT_KEY.equals( scope.peek() ) ) {
 									scope.pop();
 								}
 								listener.onEndArray();
 								break;
 							default:
 								throw new IllegalFormatException( c, scope.peek().toString() );
 						}
 						break;
 
 					case ',':
 						switch( scope.peek() ) {
 							case ARRAY:
 							case OBJECT:
 								break;
 							default:
 								throw new IllegalFormatException( c, scope.peek().toString() );
 						}
 						break;
 
 					case ':':
 						switch( scope.peek() ) {
 							case OBJECT_KEY:
 								break;
 							default:
 								throw new IllegalFormatException( c, scope.peek().toString() );
 						}
 						break;
 
 					case ' ':
 					case '\t':
 					case '\r':
 					case '\n':
 						// Ignore whitespace.
 						break;
 
 					case '"':
 						int j;
 						// FIXME: Read-ahead only works properly if the right anchor exists within
 						// the
 						// buffer
 						int skip = 0;
 						for( j = offset; j < buffer.length; j++ ) {
 							if( buffer[j] == '\\' ) {
 								skip++;
 								j++;
 								continue;
 							}
 							if( buffer[j] == '"' ) {
 								break;
 							}
 						}
 						char [] string = new char [j - offset - skip];
 						skip = 0;
 						for( int a = 0; a < string.length + skip; a++ ) {
 							if( buffer[offset + a] == '\\' ) {
 								skip++;
 								continue;
 							}
 							string[a - skip] = (char) buffer[offset + a];
 						}
 						offset = j + 1;
 						switch( scope.peek() ) {
 							case OBJECT:
 								listener.onObjectKey( string );
 								burn( string );
 								scope.push( Event.OBJECT_KEY );
 								break;
 							case OBJECT_KEY:
 								listener.onString( string );
 								burn( string );
 								scope.pop();
 								break;
 							default:
 								listener.onString( string );
 								burn( string );
 								break;
 						}
 						break;
 
 					case 't':
 						nextChar();// 'r'
 						nextChar();// 'u'
 						nextChar();// 'e'
 						listener.onBoolean( true );
 						switch( scope.peek() ) {
 							case OBJECT_KEY:
 								scope.pop();
 								break;
 							default:
 								break;
 						}
 						break;
 
 					case 'f':
 						nextChar();// 'a'
 						nextChar();// 'l'
 						nextChar();// 's'
 						nextChar();// 'e'
 						listener.onBoolean( false );
 						switch( scope.peek() ) {
 							case OBJECT_KEY:
 								scope.pop();
 								break;
 							default:
 								break;
 						}
 						break;
 
 					case 'n':
 						nextChar();// 'u'
 						nextChar();// 'l'
 						nextChar();// 'l'
 						listener.onNull();
 						switch( scope.peek() ) {
 							case OBJECT_KEY:
 								scope.pop();
 								break;
 							default:
 								break;
 						}
 						break;
 
 					case '-':
 					case '0':
 					case '1':
 					case '2':
 					case '3':
 					case '4':
 					case '5':
 					case '6':
 					case '7':
 					case '8':
 					case '9':
 						double f = atof( c );
						double d = ( f < 0 ? -1 : 1 ) * ( f - (int) f );
						if( d < 0.00001 ) {
 							listener.onInteger( (int) f );
 						} else {
 							listener.onFloat( (float) f );
 						}
 						switch( scope.peek() ) {
 							case OBJECT_KEY:
 								scope.pop();
 								break;
 							default:
 								break;
 						}
 						break;
 
 					default:
 						throw new IllegalFormatException( c, scope.peek().toString() );
 
 				}
 
 			}
 
 		} finally {
 			scope.clear();
 			burn( buffer );
 			buffer = null;
 		}
 
 	}
 
 }
