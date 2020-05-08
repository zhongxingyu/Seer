 /*
 	StandardObjectWriter.java
 
 	Author: David Fogel
 	Copyright 2008 David Fogel
 	All rights reserved.
 */
 
 package net.jsonout.support;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.Writer;
 
 import net.jsonout.*;
 
 /**
  * StandardObjectWriter
  * 
  * Comment here.
  */
 public class StandardObjectWriter implements ObjectWriter, MemberWriter {
 	// *** Class Members ***
 	private enum State {closed, open, name, value}
 	
 	// *** Instance Members ***
 	private Writer out;
 	private State state;
 	private StandardValueWriter valueWriter;
 	private boolean hasMembers;
 	private int depth;
 
 	// *** Constructors ***
 	public StandardObjectWriter() {
 		state = State.closed;
 	}
 
 	// *** ObjectWriter Methods ***
 	public ValueWriter member(String name) throws IOException {
 		member();
 		name(name);
 		return value();
 	}
 	
 	public void member(String name, JSONValue value) throws IOException {
 		member();
 		name(name);
 		value(value);
 	}
 	
 	public MemberWriter member() throws IOException {
 		throwIfClosed();
 		
 		closeMember();
 		
 		if (hasMembers)
 			writeMemberSeparator();
 		
 		state = State.name;
 		
 		return this;
 	}
 	
 	public void space(String whitespace) throws IOException {
 		throwIfClosed();
 		
 		closeMember();
 		
 		StandardJSONWriter.checkWhitespace(whitespace);
 		
 		out.write(whitespace);
 	}
 	
 	public void close() throws IOException {
 		if (state == State.closed)
 			return;
 		
 		closeMember();
 		
 		out.write('}');
 	}
 	
 	// *** MemberWriter Methods ***
 	public void name(String name) throws IOException {
 		name().write(name);
 	}
 	
 	public Writer name() throws IOException {
 		throwIfClosed();
 		
 		// this shouldn't happen, but:
 		if (state == State.open)
 			member();
 		
 		if (state == State.value)
 			throw new IllegalStateException("Attempt to write object member name without completing previous value");
 		
 		// state must be "name"
 		if (valueWriter == null)
 			valueWriter = new StandardValueWriter();
 		valueWriter.open(out, depth + 1);
 		
 		return valueWriter.string();
 	}
 	
 	public OutputStream base64Name() throws IOException {
 		// TODO - maybe wrap the above "name()" method?
 		return null;
 	}
 	
 	public ValueWriter value() throws IOException {
 		throwIfClosed();
 		
 		if (state == State.open || valueWriter == null)
 			throw new IllegalStateException("Attempt to write object member value without first writing a name");
 		
 		if (state == State.name) {
 			if (valueWriter.isOpen())
 				valueWriter.close();
 			writeNameValueSeparator();
 		}
 		
 		state = State.value; // redundant?
 		
 		valueWriter.open(out, depth + 1);
 		
 		return valueWriter;
 	}
 	
 	public void value(JSONValue value) throws IOException {
 		value.writeTo(value());
 	}
 
 	// *** Public Methods ***
 	public void open(Writer out, int depth) throws IOException {
 		this.out = out;
 		this.depth = depth;
 		hasMembers = false;
 		
 		state = State.open;
 		
		if (depth > 0)
			out.write('\n');
 		for (int i = 0 ; i < depth ; i++)
 			out.write("  ");
		if (depth > 0)
			out.write(" ");
 		
 		out.write('{');
 	}
 
 	// *** Protected Methods ***
 
 	// *** Package Methods ***
 
 	// *** Private Methods ***
 	private void writeNameValueSeparator() throws IOException {
 		out.write(':');
 	}
 	
 	private void writeMemberSeparator() throws IOException {
 		out.write(',');
 		out.write('\n');
		for (int i = 0 ; i < depth ; i++)
			out.write("  ");
		if (depth > 0)
 			out.write("  ");
		else
			out.write(" ");
 	}
 	
 	private void closeMember() throws IOException {
 		if (state == State.name) {
 			if (valueWriter.isOpen())
 				valueWriter.close();
 			writeNameValueSeparator();
 			valueWriter.open(out, depth + 1);
 			valueWriter.nul(); // in this case I guess we assume the user wants a "null" value;
 			state = State.value;
 		}
 		
 		if (state == State.value) {
 			if (valueWriter.isOpen())
 				valueWriter.close();
 			hasMembers = true;
 			state = State.open;
 		}
 	}
 	
 	private void throwIfClosed() {
 		if (state == State.closed)
 			throw new IllegalStateException("ObjectWriter is closed");
 	}
 
 	// *** Private Classes ***
 }
