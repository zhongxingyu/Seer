 /*
 	BasicElementWriter.java
 
 	Copyright 2004-2007 David Fogel
 	All rights reserved.
 */
 
 // *** package ***
 package net.markout.support;
 
 import java.io.IOException;
 import java.io.Writer;
 import java.util.List;
 
 import net.markout.*;
 import net.markout.types.*;
 
 // *** imports ***
 
 public class BasicElementWriter implements ElementWriter, ContentWriter {
 	// *** Class Members ***
 	
 	private static final XMLString COMMENT_START = new XMLString("<!--");
 	private static final XMLString COMMENT_END = new XMLString("-->");
 	
 	private static final XMLString PI_START = new XMLString("<?");
 	private static final XMLString PI_END = new XMLString("?>");
 	
 	private static final XMLString TAG_CLOSE_EMPTY = new XMLString("/>");
 	private static final XMLString END_TAG_OPEN = new XMLString("</");
 	
 	private enum State {CLOSED, OPEN_NEEDS_SPACE, OPEN, CONTENT, TEXT, CHILD}
 
 	// *** Instance Members ****
 	
 	private XMLChunkWriter theWriter;
 	
 	private TextWriter theTextWriter;
 	
 	private BasicElementWriter theCurrentChild;
 	
 	private Name theName;
 	
 	private ElementNameStack theElementNameStack;
 	
 	private State theState;
 
 	// *** Constructors ***
 	
 	public BasicElementWriter(XMLChunkWriter out) {
 		
 		theWriter = out;
 		
 		theTextWriter = null;
 		
 		theCurrentChild = null;
 		
 		theName = null;
 		
 		theElementNameStack = null;
 		
 		theState = State.CLOSED;
 	}
 
 	// *** ElementWriter Methods ***
 	
 	// --- Writer State ---
 	public List<Name> getElementNameStack() {
 		
 		if (theState == State.CLOSED)
			throw new IllegalStateException("ElementNameStack accessed on a closed ElementWriter.");
 		
 		return theElementNameStack;
 	}
 	
 	public Name getName() {
 		
 		if (theState == State.CLOSED)
			throw new MalformedXMLException("getName called on closed ElementWriter.");
 		
 		return theName;
 	}
 	
 	// --- Attributes ---
 	public void attributes(Attributes attributes) throws IOException {
 		
 		attributes.writeTo(this);
 	}
 	
 	public void attribute(Name name, AttValue value) throws IOException {
 		
 		switch(theState) {
 		case CLOSED:
			throw new MalformedXMLException("Attribute written on a closed ElementWritter");
 		
 		case CONTENT:
 		case TEXT:
 		case CHILD:
 			throw new MalformedXMLException("Attribute written on an ElementWriter with content.");
 			
 		case OPEN_NEEDS_SPACE:
			theWriter.write(XMLChar.SPACE_CHAR); // need minimum whitespace between attribtues
 		}
 		
 		theState = State.OPEN_NEEDS_SPACE;
 		
 		theWriter.write(name);
 		theWriter.write(XMLChar.EQUALS_CHAR);
 		theWriter.write(value);
 	}
 	
 	public void space(Whitespace space) throws IOException {
 		
 		// There are two general categories for when space() is called,
 		// within the open element tag, and within content.
 		// notice the return after case OPEN.
 		
 		switch(theState) {
 		case CLOSED:
 			throw new MalformedXMLException("Space written on a closed ElementWritter");
 			
 		case OPEN_NEEDS_SPACE:
 			theState = State.OPEN;
 			//fall through
 		case OPEN:
 			theWriter.write(space);
 			return;
 		
 		case TEXT:
 			theTextWriter.close();
 			break;
 			
 		case CHILD:
 			theCurrentChild.close();
 			break;
 		}
 		
 		theState = State.CONTENT;
 		
 		theWriter.write(space);
 	}
 	
 	// --- Content ---
 	public ContentWriter content() throws IOException {
 		
 		switch(theState) {
 		case CLOSED:
 			throw new MalformedXMLException("Content written on closed ElementWriter.");
 			
 		case CONTENT:
 		case TEXT:
 		case CHILD:
 			return this; // already has content, return the ContentWriter view
 		
 		case OPEN_NEEDS_SPACE:
 			//theWriter.write(XMLChar.SPACE_CHAR); // actually don't want this
 		}
 		
 		theWriter.write(XMLChar.GREATER_THAN_CHAR);
 		
 		theState = State.CONTENT;
 		
 		return this;
 	}
 	
 	// *** ContentWriter Methods ***
 	
 	// --- Escaped Text Content ---
 	public void text(Text text) throws IOException {
 		
 		prepareForContent();
 		
 		theWriter.write(text);
 	}
 	
 	public void text(String text) throws IOException {
 		
 		text().write(text);
 	}
 	
 	public Writer text() throws IOException {
 		
 		switch(theState) {
 		case CLOSED:
 		case OPEN:
 		case OPEN_NEEDS_SPACE:
 			content();
 			break;
 			
 		case TEXT:
 			return theTextWriter;
 		
 		case CHILD:
 			theCurrentChild.close();
 			break;
 		}
 		
 		theState = State.TEXT;
 		
 		if (theTextWriter == null)
 			theTextWriter = new TextWriter(theWriter);
 		
 		theTextWriter.open();
 		
 		return theTextWriter;
 	}
 	
 	// --- Character Data Content---
 	public void characters(CharData charData) throws IOException {
 		
 		prepareForContent();
 		
 		theWriter.write(charData);
 	}
 	
 	public void cdata(CData cdata) throws IOException {
 		
 		prepareForContent();
 		
 		theWriter.write(cdata);
 	}
 	
 	// --- Reference Content ---
 	public void reference(CharRef charRef) throws IOException {
 		
 		prepareForContent();
 		
 		theWriter.write(charRef);
 	}
 	
 	public void reference(Name entityName) throws IOException {
 
 		prepareForContent();
 		
 		theWriter.write(XMLChar.AMPERSAND_CHAR);
 		theWriter.write(entityName);
 		theWriter.write(XMLChar.SEMICOLON_CHAR);
 	}
 	
 	// --- Element Content ---
 	public ElementWriter elementWriter(Name elementName) throws IOException {
 		
 		prepareForContent();
 		
 		theState = State.CHILD;
 		
 		if (theCurrentChild == null)
 			theCurrentChild = new BasicElementWriter(theWriter);
 		
 		theCurrentChild.open(elementName, theElementNameStack);
 		
 		return theCurrentChild;
 	}
 	
 	public ContentWriter element(Name elementName) throws IOException {
 		
 		return elementWriter(elementName).content();
 	}
 	
 	public ContentWriter element(Name elementName, Attributes attributes) throws IOException {
 		
 		elementWriter(elementName);
 		if (attributes != null)
 			attributes.writeTo(theCurrentChild);
 		return theCurrentChild.content();
 	}
 	
 	public void emptyElement(Name elementName) throws IOException {
 		
 		elementWriter(elementName);
 		
 		theCurrentChild.close();
 		
 		theState = State.CONTENT;
 	}
 	
 	public void emptyElement(Name elementName, Attributes attributes) throws IOException {
 		
 		elementWriter(elementName);
 		if (attributes != null)
 			attributes.writeTo(theCurrentChild);
 		
 		theCurrentChild.close();
 		
 		theState = State.CONTENT;
 	}
 	
 	// --- Misc Content ---
 	public void comment(Comment c) throws IOException {
 		
 		prepareForContent();
 		
 		theWriter.write(COMMENT_START);
 		theWriter.write(c);
 		theWriter.write(COMMENT_END);
 	}
 	
 	public void pi(Target target, Instruction instruction) throws IOException {
 		
 		prepareForContent();
 		
 		theWriter.write(PI_START);
 		theWriter.write(target);
 		if (instruction != null) {
 			theWriter.write(XMLChar.SPACE_CHAR);
 			theWriter.write(instruction);
 		}
 		theWriter.write(PI_END);
 	}
 
 	// *** Public Methods ***
 	
 	public void open(Name elementName) throws IOException {
 		open(elementName, null);
 	}
 	
 	public void open(Name elementName, List<Name> parentElementNames) throws IOException {
 		
 		if (theState != State.CLOSED)
 			throw new MalformedXMLException("ElementWriter already opened.");
 		theState = State.OPEN_NEEDS_SPACE;
 		
 		theName = elementName;
 		if (theElementNameStack == null || theElementNameStack.getParent() != parentElementNames)
 			theElementNameStack = new ElementNameStack(parentElementNames, elementName);
 		else
 			theElementNameStack.set(elementName);
 		
 		theWriter.write(XMLChar.LESS_THAN_CHAR);
 		theWriter.write(theName);
 	}
 	
 	public void close() throws IOException {
 		
 		switch(theState) {
 		case CLOSED:
 			return;// It's okay to call close() more than once?
 		
 		case OPEN_NEEDS_SPACE:
 			theWriter.write(XMLChar.SPACE_CHAR); // need minimum whitespace before close
 			theState = State.OPEN;
 			break;
 			
 		case TEXT:
 			theTextWriter.close();
 			theState = State.CONTENT;
 			break;
 			
 		case CHILD:
 			theCurrentChild.close();
 			theState = State.CONTENT;
 			break;
 		}
 		
 		switch(theState) {
 		case OPEN:
 			theWriter.write(TAG_CLOSE_EMPTY);
 			break;
 			
 		case CONTENT:
 			theWriter.write(END_TAG_OPEN);
 			theWriter.write(theName);
 			theWriter.write(XMLChar.GREATER_THAN_CHAR);
 			break;
 		}
 		
 		theState = State.CLOSED;
 		
 		theWriter.flush();
 		
 		theName = null;
 	}
 
 	// *** Protected Methods ***
 
 	// *** Package Methods ***
 
 	// *** Private Methods ***
 	
 	private void prepareForContent() throws IOException {
 		switch(theState) {
 		case CLOSED:
 		case OPEN:
 		case OPEN_NEEDS_SPACE:
 			content();
 			break;
 			
 		case TEXT:
 			theTextWriter.close();
 			break;
 		
 		case CHILD:
 			theCurrentChild.close();
 			break;
 		}
 		
 		theState = State.CONTENT;
 	}
 
 	// *** Private Classes ***
 
 }
 
 
 
 
 
 
 // end
