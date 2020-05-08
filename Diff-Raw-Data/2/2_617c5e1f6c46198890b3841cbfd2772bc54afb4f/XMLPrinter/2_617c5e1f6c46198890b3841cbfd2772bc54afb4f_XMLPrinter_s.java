 /*
  * [The "New BSD" license]
  * Copyright (c) 2012 The Board of Trustees of The University of Alabama
  * All rights reserved.
  *
  * See LICENSE for details.
  */
 package edu.ua.eng.software.mallard;
 
 import java.util.concurrent.ConcurrentMap;
 import edu.ua.eng.software.mallard.util.LineRange;
 
 /**
  * @author	Blake Bassett <rbbassett@crimson.ua.edu>
  * @version	$Id: XMLPrinter.java 1 2012-05-06 17:16:22Z blkbsstt $
  */
 public class XMLPrinter
 {
 	public static String getXML(ConcurrentMap<String,Method> methods) {
 		XMLPrinter xp = new XMLPrinter(methods);
 		return xp.getXML();
 	}
 	
 	private XMLPrinter(ConcurrentMap<String,Method> methods) {
 		startTag("methods", CLOSED);
 		for (String qname : methods.keySet()) {
 			Method method = methods.get(qname);
 			startTag("method", OPEN);
 			methodAttr(method);
 			callingAttr(method);
 			closeStartTag();
 			startTag("callers", CLOSED);
 			for (Method caller : method.getCallers()) {
 				startTag("caller", OPEN);
 				methodAttr(caller);
 				closeStartTag();
 				endTag("caller");
 			}
 			endTag("callers");
 			startTag("callees", CLOSED);
 			for (String cname : method.getCallees()) {
 				startTag("callee", OPEN);
 				addAttr("qname", cname);
 				closeStartTag();
 				endTag("callee");
 			}
 			endTag("callees");
 			endTag("method");
 		}
 		endTag("methods");
 	}
 	
 	public String getXML() { return output.toString(); };
 	
 	private void startTag(String tag, boolean closed) {
 		indent();
 		output.append("<" + tag);
 		if (closed) closeStartTag();
 		++indentLevel;
 	}
 	
 	private void closeStartTag() {
 		output.append(">\n");
 	}
 	
 	private void endTag(String tag) {
 		--indentLevel;
 		indent();
 		output.append("</" + tag + ">\n");
 	}
 	
 	private void indent() {
 		for(int i = 0; i < indentLevel; ++i) output.append(indent);
 	}
 	
 	private void methodAttr(Method method) {
 		addAttr("qname", method.getQualifiedName());
 		addAttr("filename", method.getFilename());
 		
 		LineRange range = method.getLineRange();
 		addAttr("startLine", range.getStartLine());
 		addAttr("endLine", range.getEndLine());
 	}
 	
 	private void callingAttr(Method method) {
 		addAttr("ncallers", method.getCallers().size());
 		addAttr("ncallees", method.getCallees().size());
 	}
 	
 	private void addAttr(String key, String value) {
 		output.append(" " + key + "=\"" + value + "\"");
 	}
 	
 	private void addAttr(String key, int value) {
		output.append(" " + key + "=" + value);
 	}
 	
 	private int indentLevel = 0;
 	private String indent = "\t";
 	private StringBuilder output = new StringBuilder();
 	private final boolean CLOSED = true;
 	private final boolean OPEN = false;
 }
