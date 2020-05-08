 package org.simplesql.parser.tree;
 
 public class BOOLEAN extends TERM {
 
 	boolean val;
 
 	public BOOLEAN(String val) {
 		super(TYPE.BOOLEAN);
 		try {
 			this.val = Boolean.parseBoolean(val);
 		} catch (Exception excp) {
 			int i = Integer.parseInt(val);
 			this.val = i == 1;
 		}
 	}
 
 	public boolean getVal() {
 		return val;
 	}
 
 }
