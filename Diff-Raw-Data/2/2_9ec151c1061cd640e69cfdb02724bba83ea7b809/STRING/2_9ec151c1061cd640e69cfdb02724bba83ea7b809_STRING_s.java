 package org.simplesql.parser.tree;
 
 public class STRING extends TERM {
 
 	final String val;
 
 	public STRING(String val) {
 		super(TYPE.STRING);
 
 		// trim and remove starting and ending ' or " characters.
 		String locVal = val.trim();
 
 		if (locVal.startsWith("\'") && locVal.endsWith("\'")
 				|| (locVal.startsWith("\"") && locVal.endsWith("\""))) {
			locVal = locVal.substring(1, locVal.length() - 2);
 		}
 
 		this.val = locVal;
 		setValue(locVal);
 	}
 
 	public String getVal() {
 		return val;
 	}
 
 }
