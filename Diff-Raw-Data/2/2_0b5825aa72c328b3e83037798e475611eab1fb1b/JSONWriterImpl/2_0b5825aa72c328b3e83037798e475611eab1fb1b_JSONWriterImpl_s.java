 package org.i5y.json.stream.impl;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.util.ArrayDeque;
 import java.util.Deque;
 
 import org.i5y.json.stream.JSONWriter;
 
 class JSONWriterImpl implements JSONWriter {
 
 	public void write(String str) {
 		try {
 			writer.write(str);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public void write(char ch) {
 		try {
 			writer.write(ch);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	private final Writer writer;
 
 	private enum Event {
 		INPUT_START, INPUT_END, OBJECT_START, OBJECT_END, ARRAY_START, ARRAY_END, PROPERTY_NAME, LITERAL
 	}
 
 	private Event lastEvent = Event.INPUT_START;
 
 	private Deque<Wrapper> wrappers = new ArrayDeque<Wrapper>(10);
 
 	private enum Wrapper {
 		Object, Array
 	};
 
 	public JSONWriterImpl(final Writer writer) {
 		this.writer = writer;
 	}
 
 	@Override
 	public JSONWriter startObject() {
 		if (wrappers.isEmpty()) {
 			write('{');
 			wrappers.push(Wrapper.Object);
 			lastEvent = Event.OBJECT_START;
 			return this;
 		} else if (startValue("{")) {
 			wrappers.push(Wrapper.Object);
 			lastEvent = Event.OBJECT_START;
 			return this;
 		}
 		throw new IllegalStateException();
 	}
 
 	private boolean startValue(String insert) {
 		if (wrappers.peek() == Wrapper.Object) {
 			if (lastEvent == Event.PROPERTY_NAME) {
 				write( ":");
 				write( insert);
 				return true;
 			}
 		} else if (wrappers.peek() == Wrapper.Array) {
 			if (lastEvent == Event.ARRAY_END || lastEvent == Event.OBJECT_END
 					|| lastEvent == Event.LITERAL
 					|| lastEvent == Event.PROPERTY_NAME) {
 				write( ",");
 				write( insert);
 				return true;
 			} else if (lastEvent == Event.ARRAY_START) {
 				write( insert);
 				return true;
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public JSONWriter endObject() {
 		if (wrappers.peek() == Wrapper.Object
 				&& (lastEvent == Event.OBJECT_START
 						|| lastEvent == Event.ARRAY_END
 						|| lastEvent == Event.OBJECT_END
 						|| lastEvent == Event.LITERAL || lastEvent == Event.PROPERTY_NAME)) {
 			wrappers.pop();
 			write('}');
 			lastEvent = Event.OBJECT_END;
 			return this;
 		}
 		throw new IllegalStateException();
 	}
 
 	@Override
 	public JSONWriter propertyName(String name) {
 		if (wrappers.peek() == Wrapper.Object) {
 			if (lastEvent == Event.ARRAY_END || lastEvent == Event.OBJECT_END
 					|| lastEvent == Event.LITERAL
 					|| lastEvent == Event.PROPERTY_NAME) {
 				write( ",\"");
 				write( name);
 				write( "\"");
 				lastEvent = Event.PROPERTY_NAME;
 				return this;
 			} else if (lastEvent == Event.OBJECT_START) {
 				write( "\"");
 				write( name);
 				write( "\"");
 				lastEvent = Event.PROPERTY_NAME;
 				return this;
 			}
 		}
 		throw new IllegalStateException();
 	}
 
 	private JSONWriter rawProperty(String name, String encodedLiteral)
 			 {
 		if (wrappers.peek() == Wrapper.Object) {
 			if (lastEvent == Event.ARRAY_END || lastEvent == Event.OBJECT_END
 					|| lastEvent == Event.LITERAL
 					|| lastEvent == Event.PROPERTY_NAME) {
 				write( ",\"");
 				write( name);
 				write( "\":");
 				write( encodedLiteral);
 				lastEvent = Event.LITERAL;
 				return this;
 			} else if (lastEvent == Event.OBJECT_START) {
 				write( "\"");
 				write( name);
 				write( "\":");
 				write( encodedLiteral);
 				lastEvent = Event.LITERAL;
 				return this;
 			}
 		}
 		throw new IllegalStateException();
 	}
 
 	@Override
 	public JSONWriter property(String name, String literal)
 			 {
 		return rawProperty(name, literal);
 	}
 
 	@Override
 	public JSONWriter property(String name, int literal) {
 		return rawProperty(name, "" + literal);
 	}
 
 	@Override
 	public JSONWriter property(String name, long literal) {
 		return rawProperty(name, "" + literal);
 	}
 
 	@Override
 	public JSONWriter property(String name, BigInteger literal)
 			 {
 		return rawProperty(name, "" + literal);
 	}
 
 	@Override
 	public JSONWriter property(String name, float literal)
 			 {
 		return rawProperty(name, "" + literal);
 	}
 
 	@Override
 	public JSONWriter property(String name, double literal)
 			 {
 		return rawProperty(name, "" + literal);
 	}
 
 	@Override
 	public JSONWriter property(String name, BigDecimal literal)
 			 {
 		return rawProperty(name, "" + literal);
 	}
 
 	@Override
 	public JSONWriter property(String name, boolean literal)
 			 {
 		return rawProperty(name, Boolean.toString(literal));
 	}
 
 	@Override
 	public JSONWriter nullProperty(String name) {
 		return rawProperty(name, "null");
 	}
 
 	@Override
 	public JSONWriter literal(String literal) {
 		StringBuilder sb = new StringBuilder(literal.length() + 2);
 		sb.append('\"');
 		for (int i = 0; i < literal.length(); i++) {
 			char c = literal.charAt(i);
 			switch (c) {
 			case '\"': sb.append("\\\""); break;
 			case '\\': sb.append("\\\\"); break;
			case '/': sb.append("\\/"); break;
 			case '\b': sb.append("\\b"); break;
 			case '\f': sb.append("\\f"); break;
 			case '\n': sb.append("\\n"); break;
 			case '\r': sb.append("\\r"); break;
 			case '\t': sb.append("\\t"); break;
 			default:
 				if (c >= 0 && c <= 0x1f) {
 					// Need to encode...
 					sb.append("\\u");
 					String hex = Integer.toHexString(c);
 					switch (hex.length()) {
 					case 1: sb.append('0');
 					case 2: sb.append('0');
 					case 3: sb.append('0');
 					}
 					sb.append(hex);
 				} else {
 					// For the moment, ignore the high codepoint stuff
 					sb.append(c);
 				}
 			}
 
 		}
 		sb.append('\"');
 		if (startValue(sb.toString())) {
 			lastEvent = Event.LITERAL;
 			return this;
 		}
 		throw new IllegalStateException();
 	}
 
 	@Override
 	public JSONWriter literal(int literal) {
 		if (startValue(Integer.toString(literal))) {
 			lastEvent = Event.LITERAL;
 			return this;
 		}
 		throw new IllegalStateException();
 	}
 
 	@Override
 	public JSONWriter literal(long literal) {
 		if (startValue(Long.toString(literal))) {
 			lastEvent = Event.LITERAL;
 			return this;
 		}
 		throw new IllegalStateException();
 	}
 
 	@Override
 	public JSONWriter literal(BigInteger literal) {
 		if (startValue(literal.toString())) {
 			lastEvent = Event.LITERAL;
 			return this;
 		}
 		throw new IllegalStateException();
 	}
 
 	@Override
 	public JSONWriter literal(float literal) {
 		if (startValue(Float.toString(literal))) {
 			lastEvent = Event.LITERAL;
 			return this;
 		}
 		throw new IllegalStateException();
 	}
 
 	@Override
 	public JSONWriter literal(double literal) {
 		if (startValue(Double.toString(literal))) {
 			lastEvent = Event.LITERAL;
 			return this;
 		}
 		throw new IllegalStateException();
 	}
 
 	@Override
 	public JSONWriter literal(BigDecimal literal) {
 		if (startValue(literal.toString())) {
 			lastEvent = Event.LITERAL;
 			return this;
 		}
 		throw new IllegalStateException();
 	}
 
 	@Override
 	public JSONWriter nullLiteral() {
 		if (startValue("null")) {
 			lastEvent = Event.LITERAL;
 			return this;
 		}
 		throw new IllegalStateException();
 	}
 
 	@Override
 	public JSONWriter literal(boolean literal) {
 		if (startValue(Boolean.toString(literal))) {
 			lastEvent = Event.LITERAL;
 			return this;
 		}
 		throw new IllegalStateException();
 	}
 
 	@Override
 	public JSONWriter startArray() {
 		if (wrappers.isEmpty()) {
 			write('[');
 			wrappers.push(Wrapper.Array);
 			lastEvent = Event.ARRAY_START;
 			return this;
 		} else if (startValue("[")) {
 			wrappers.push(Wrapper.Array);
 			lastEvent = Event.ARRAY_START;
 			return this;
 		}
 		throw new IllegalStateException();
 	}
 
 	@Override
 	public JSONWriter endArray(){
 		if (wrappers.peek() == Wrapper.Array
 				&& (lastEvent == Event.ARRAY_START
 						|| lastEvent == Event.ARRAY_END
 						|| lastEvent == Event.OBJECT_END
 						|| lastEvent == Event.LITERAL || lastEvent == Event.PROPERTY_NAME)) {
 			wrappers.pop();
 			write(']');
 			lastEvent = Event.ARRAY_END;
 			return this;
 		}
 		throw new IllegalStateException();
 	}
 
 	@Override
 	public void flush()throws IOException{
 		writer.flush();
 	}
 
 	@Override
 	public void close() throws IOException{
 		flush();
 		writer.close();
 	}
 
 }
