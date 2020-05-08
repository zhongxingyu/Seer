 package com.thoughtworks.qdox.xml;
 
 import java.io.PrintWriter;
 import java.io.Writer;
 import java.util.Stack;
 
 /**
  * Simple XML XmlHandler.
  */
 public class TextXmlHandler implements XmlHandler {
 
 	// This avoids having to deal with SAX, and finding a fully-fledged
	// SAX-based handler.
 
 	//~~~( Constants )~~~
 
 	static final String XML_PREAMBLE = "<?xml version=\"1.0\"?>";
 
 	//~~~( Member variables )~~~
 
 	private PrintWriter out;
 	private String indentPrefix;
 
 	private Stack nodeStack = new Stack();
 	private boolean onNewLine = true;
 
 	//~~~( Constructors )~~~
 
 	public TextXmlHandler(Writer out, String indentPrefix) {
 		this.out = new PrintWriter(out);
 		this.indentPrefix = indentPrefix;
 	}
 
 	public TextXmlHandler(Writer out) {
 		this(out, "");
 	}
 
 	//~~~( Implement XmlHandler )~~~
 
 	public void startDocument() {
 		out.println(XML_PREAMBLE);
 	}
 
 	public void startElement(String name) {
 		if (!onNewLine) {
 			out.println();
 		}
 		indent(nodeStack.size());
 		out.print('<');
 		out.print(name);
 		out.print('>');
 		onNewLine = false;
 		nodeStack.push(name);
 	}
 
 	public void addContent(String text) {
 		char[] chars = text.toCharArray();
 		for (int i = 0; i < chars.length; i++) {
 			switch (chars[i]) {
 				case '<':	out.print("&lt;");	break;
 				case '>':	out.print("&gt;");	break;
 				case '&':	out.print("&amp;"); break;
 				default:	out.print(chars[i]);
 			}
 		}
 	}
 
 	public void endElement() {
 		if (nodeStack.isEmpty()) {
 			throw new IllegalStateException();
 		}
 		String name = (String) nodeStack.pop();
 		if (onNewLine) {
 			indent(nodeStack.size());
 		}
 		out.println("</" + name + ">");
 		onNewLine = true;
 	}
 
 	public void endDocument() {
 		out.flush();
 	}
 
 	//~~~( Support methods )~~~
 
 	private void indent(int level) {
 		for (int i = 0; i < level; i++) {
 			out.print(indentPrefix);
 		}
 	}
 
 }
