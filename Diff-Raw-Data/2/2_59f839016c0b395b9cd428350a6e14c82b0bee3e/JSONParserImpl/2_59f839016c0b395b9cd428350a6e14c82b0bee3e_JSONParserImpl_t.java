 package org.i5y.json.stream.impl;
 
 import java.io.IOException;
 import java.io.Reader;
 import java.io.StringReader;
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.util.Stack;
 
 import org.i5y.json.stream.JSONEvent;
 import org.i5y.json.stream.JSONParser;
 
public class JSONParserImpl implements JSONParser {
 
 	private JSONEvent current = JSONEvent.INPUT_START;
 	private String errorMessage = null;
 	private char currentChar = Character.MAX_HIGH_SURROGATE;
 	boolean inputConsumed = false;
 	private String currentValue = null;
 
 	private final Reader reader;
 
 	// Expectation that the default would limit depth to 10? Could use array to
 	// speed up...
 	private Stack<Wrapper> wrappers = new Stack<Wrapper>();
 
 	private enum Wrapper {
 		Object, Array
 	};
 
 	public JSONParserImpl(String jsonText) {
 		this(new StringReader(jsonText));
 	}
 
 	public JSONParserImpl(Reader reader) {
 		this.reader = reader;
 	}
 
 	@Override
 	public JSONEvent next() {
 		switch (current) {
 		case INPUT_START: {
 			if (skipWhitespace(false)) {
 				if (currentChar == '{') {
 					current = JSONEvent.OBJECT_START;
 					wrappers.push(Wrapper.Object);
 					break;
 				}
 				if (currentChar == '[') {
 					current = JSONEvent.ARRAY_START;
 					wrappers.push(Wrapper.Array);
 					break;
 				}
 				current = JSONEvent.ERROR;
 				errorMessage = "Expected '{' or '['. Got: '" + currentChar
 						+ "'";
 				System.out.println(errorMessage);
 
 			} else {
 				current = JSONEvent.INPUT_END;
 			}
 			break;
 		}
 		case INPUT_END: {
 			throw new IllegalStateException(); // Programming Error (and catches
 												// infite loops/recursion which
 												// doesn't properly check the
 												// End Case)
 		}
 		case OBJECT_START: {
 			if (skipWhitespace(true)) {
 				if (currentChar == '}') {
 					current = JSONEvent.OBJECT_END;
 					break;
 				}
 				if (currentChar != '"') {
 					current = JSONEvent.ERROR;
 					errorMessage = "Expected '\"'. Got: '" + currentChar + "'";
 					System.out.println(errorMessage);
 					break;
 				}
 				if (readString()) {
 					current = JSONEvent.PROPERTY_NAME;
 				}
 			}
 			break;
 		}
 		case PROPERTY_NAME: {
 			if (skipWhitespace(true)) {
 				if (currentChar != ':') {
 					current = JSONEvent.ERROR;
 					errorMessage = "Expected ':'. Got: '" + currentChar + "'";
 					System.out.println(errorMessage);
 					break;
 				}
 				if (skipWhitespace(true)) {
 					matchValue();
 					break;
 				}
 			}
 			break;
 		}
 		case BOOLEAN_LITERAL:
 		case NUMBER_LITERAL:
 		case STRING_LITERAL: {
 			if (currentChar == ',' || currentChar == '}' || currentChar == ']'
 					|| skipWhitespace(true)) {
 				Wrapper head = wrappers.peek();
 				if (currentChar == ',') {
 					if (Wrapper.Object == head) {
 						if (skipWhitespace(true)) {
 							if (readString()) {
 								current = JSONEvent.PROPERTY_NAME;
 								break;
 							}
 						}
 					} else {
 						if (skipWhitespace(true)) {
 							matchValue();
 							break;
 						}
 					}
 				}
 				if (Wrapper.Object == head) {
 					if (currentChar == '}') {
 						current = JSONEvent.OBJECT_END;
 						wrappers.pop();
 						break;
 					}
 				} else {
 					if (currentChar == ']') {
 						current = JSONEvent.ARRAY_END;
 						wrappers.pop();
 						break;
 					}
 				}
 				current = JSONEvent.ERROR;
 				errorMessage = "Expected ',' or '}'. Got: '" + currentChar
 						+ "'";
 				System.out.println(errorMessage);
 			}
 			break;
 		}
 		case ARRAY_START: {
 			if (skipWhitespace(true)) {
 				if (currentChar == ']') {
 					current = JSONEvent.ARRAY_END;
 					break;
 				}
 				matchValue();
 				break;
 			}
 			break;
 		}
 		case OBJECT_END:
 		case ARRAY_END: {
 			boolean charactersRemaining = skipWhitespace(false);
 			if (wrappers.isEmpty()) {
 				if (charactersRemaining) {
 					if (currentChar == '{') {
 						current = JSONEvent.OBJECT_START;
 						wrappers.push(Wrapper.Object);
 						break;
 					}
 					if (currentChar == '[') {
 						current = JSONEvent.ARRAY_START;
 						wrappers.push(Wrapper.Array);
 						break;
 					}
 					current = JSONEvent.ERROR;
 					errorMessage = "Expected '{' or '['. Got: '" + currentChar
 							+ "'";
 					System.out.println(errorMessage);
 					break;
 				} else {
 					current = JSONEvent.INPUT_END;
 					break;
 				}
 			} else {
 				Wrapper head = wrappers.peek();
 				if (charactersRemaining) {
 					if (currentChar == ',') {
 						if (Wrapper.Object == head) {
 							if (skipWhitespace(true)) {
 								if (readString()) {
 									current = JSONEvent.PROPERTY_NAME;
 									break;
 								}
 							}
 						} else {
 							if (skipWhitespace(true)) {
 								matchValue();
 								break;
 							}
 						}
 					}
 					if (Wrapper.Object == head) {
 						if (currentChar == '}') {
 							current = JSONEvent.OBJECT_END;
 							wrappers.pop();
 							break;
 						}
 					} else {
 						if (currentChar == ']') {
 							current = JSONEvent.ARRAY_END;
 							wrappers.pop();
 							break;
 						}
 					}
 					current = JSONEvent.ERROR;
 					errorMessage = "Expected ',' or '}'. Got: '" + currentChar
 							+ "'";
 					System.out.println(errorMessage);
 				} else {
 					wrappers.pop();
 					current = JSONEvent.INPUT_END;
 					break;
 				}
 			}
 		}
 		default:
 			throw new IllegalStateException();
 		}
 		return current;
 	}
 
 	boolean decimal = false;
 
 	private final void matchValue() {
 		if (currentChar == '{') {
 			current = JSONEvent.OBJECT_START;
 			wrappers.push(Wrapper.Object);
 			currentValue = null;
 		} else if (currentChar == '[') {
 			current = JSONEvent.ARRAY_START;
 			wrappers.push(Wrapper.Array);
 			currentValue = null;
 		} else if (currentChar == '\"') {
 			if (readString()) {
 				current = JSONEvent.STRING_LITERAL;
 			}
 		} else if (currentChar == 't') {
 			if (readNext('r') && readNext('u') && readNext('e')) {
 				current = JSONEvent.BOOLEAN_LITERAL;
 				currentBoolean = true;
 			}
 		} else if (currentChar == 'f') {
 			if (readNext('a') && readNext('l') && readNext('s')
 					&& readNext('e')) {
 				current = JSONEvent.BOOLEAN_LITERAL;
 				currentBoolean = false;
 			}
 		} else if (currentChar == '-'
 				|| (currentChar >= '0' && currentChar <= '9')) {
 			decimal = false;
 			StringBuffer integerSB = new StringBuffer();
 			integerSB.append(currentChar);
 			while (nextChar(true) && currentChar != ' ' && currentChar != '\t'
 					&& currentChar != '\r' && currentChar != '\n'
 					&& currentChar != ',' && currentChar != ']'
 					&& currentChar != '}') {
 				integerSB.append(currentChar);
 				if (currentChar == '.' || currentChar == '-') {
 					decimal = true;
 				}
 			}
 			currentValue = integerSB.toString();
 			if (!inputConsumed) {
 				System.out.println(currentValue);
 				current = JSONEvent.NUMBER_LITERAL;
 			}
 		} else {
 			current = JSONEvent.ERROR;
 		}
 	}
 
 	private boolean currentBoolean = false;
 
 	private boolean readNext(char expected) {
 		if (nextChar(true)) {
 			return currentChar == expected;
 		} else {
 			current = JSONEvent.ERROR;
 			errorMessage = "Expected '" + expected + "'. Got: '" + currentChar
 					+ "'";
 		}
 		return false;
 	}
 
 	private final boolean skipWhitespace(boolean errorOnEnd) {
 		if (!nextChar(errorOnEnd)) {
 			return false;
 		}
 		while (currentChar == ' ' || currentChar == '\t' || currentChar == '\r'
 				|| currentChar == '\n') {
 			if (!nextChar(errorOnEnd)) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	private final boolean nextChar(boolean errorOnEnd) {
 		try {
 			int next = reader.read();
 			if (next < 0) {
 				inputConsumed = true;
 				if (errorOnEnd) {
 					current = JSONEvent.ERROR;
 					errorMessage = "Unexpected End of Stream.";
 					System.out.println(errorMessage);
 				}
 				return false;
 			} else {
 				currentChar = (char) next;
 				return true;
 			}
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	private final boolean readString() {
 		StringBuilder sb = new StringBuilder();
 		boolean oddSequentialSlashes = false;
 		if (nextChar(true)) {
 			while (oddSequentialSlashes || currentChar != '"') {
 				if (currentChar == '\\') {
 					oddSequentialSlashes = !oddSequentialSlashes;
 				} else {
 					oddSequentialSlashes = false;
 				}
 				sb.append(currentChar);
 				if (!nextChar(true)) {
 					return false;
 				}
 			}
 			currentValue = sb.toString();
 		} else {
 			return false;
 		}
 		return true;
 	}
 
 	@Override
 	public JSONEvent current() {
 		return current;
 	}
 
 	@Override
 	public void advance() {
 		next();
 	}
 
 	@Override
 	public JSONEvent skip(int skipNumber) {
 		for (int i = 0; i < skipNumber - 1; i++) {
 			advance();
 		}
 		return next();
 	}
 
 	@Override
 	public JSONEvent skipEnclosing() {
 		if (wrappers.isEmpty()) {
 			throw new IllegalStateException();
 		}
 		while (!(current == JSONEvent.ARRAY_END || current == JSONEvent.OBJECT_END)) {
 			advance();
 		}
 		return current();
 	}
 
 	@Override
 	public String string() {
 		return currentValue;
 	}
 
 	@Override
 	public String errorMessage() {
 		return errorMessage;
 	}
 
 	@Override
 	public boolean asBoolean() {
 		if (current != JSONEvent.BOOLEAN_LITERAL)
 			throw new IllegalStateException();
 		return currentBoolean;
 	}
 
 	@Override
 	public Precision precision() {
 		if (current != JSONEvent.NUMBER_LITERAL) {
 			throw new IllegalStateException();
 		}
 		if (decimal) {
 			return Precision.BIG_DECIMAL;
 		} else {
 			BigInteger INT_MIN = BigInteger.valueOf(Integer.MIN_VALUE);
 			BigInteger INT_MAX = BigInteger.valueOf(Integer.MAX_VALUE);
 			BigInteger LONG_MIN = BigInteger.valueOf(Long.MIN_VALUE);
 			BigInteger LONG_MAX = BigInteger.valueOf(Long.MAX_VALUE);
 			BigInteger bi = new BigDecimal(currentValue).toBigInteger();
 			if (bi.compareTo(INT_MIN) >= 0 && bi.compareTo(INT_MAX) <= 0) {
 				return Precision.INT;
 			} else if (bi.compareTo(LONG_MIN) >= 0
 					&& bi.compareTo(LONG_MAX) <= 0) {
 				return Precision.LONG;
 			} else {
 				return Precision.BIG_INTEGER;
 			}
 		}
 	}
 
 	@Override
 	public int asInt() {
 		if (current != JSONEvent.NUMBER_LITERAL)
 			throw new IllegalStateException();
 		return new BigDecimal(currentValue).intValue();
 	}
 
 	@Override
 	public long asLong() {
 		if (current != JSONEvent.NUMBER_LITERAL)
 			throw new IllegalStateException();
 		return new BigDecimal(currentValue).longValue();
 	}
 
 	@Override
 	public float asFloat() {
 		if (current != JSONEvent.NUMBER_LITERAL)
 			throw new IllegalStateException();
 		return new BigDecimal(currentValue).floatValue();
 	}
 
 	@Override
 	public double asDouble() {
 		if (current != JSONEvent.NUMBER_LITERAL)
 			throw new IllegalStateException();
 		return new BigDecimal(currentValue).doubleValue();
 	}
 
 	@Override
 	public BigInteger asBigInteger() {
 		if (current != JSONEvent.NUMBER_LITERAL)
 			throw new IllegalStateException();
 		return new BigDecimal(currentValue).toBigInteger();
 	}
 
 	@Override
 	public BigDecimal asBigDecimal() {
 		if (current != JSONEvent.NUMBER_LITERAL)
 			throw new IllegalStateException();
 		return new BigDecimal(currentValue);
 	}
 
 }
