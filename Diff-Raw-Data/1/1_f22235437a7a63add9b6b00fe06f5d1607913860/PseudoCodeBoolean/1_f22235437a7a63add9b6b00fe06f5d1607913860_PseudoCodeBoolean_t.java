 package de.unisiegen.informatik.bs.alvis.primitives;
 
 import java.util.ArrayList;
 import java.util.Stack;
 
 /**
  * Wrapper Class around Java boolean
  * 
  * @author Dominik Dingel
  * 
  */
 
 public class PseudoCodeBoolean extends Datatype {
 	protected static final String TYPENAME = "Boolean";
 
 	private boolean value;
 
 	/**
 	 * create new Boolean from literal
 	 * 
 	 * @param literal
 	 */
 	public PseudoCodeBoolean(boolean literal) {
 		commandsforGr = new ArrayList<Stack<Object>>();
 		commandsforGr.add(new Stack<Object>());
 		this.setLiteralValue(literal);
 	}
 
 	/**
 	 * create new Boolean from another Boolean
 	 * 
 	 * @param toSetFrom
 	 */
 	public PseudoCodeBoolean(PseudoCodeBoolean toSetFrom) {
 		commandsforGr = new ArrayList<Stack<Object>>();
 		commandsforGr.add(new Stack<Object>());
 		this.setValue(toSetFrom);
 	}
 
 	/**
 	 * creates new Boolean from graphicalrepresentation
 	 * 
 	 * @param gr
 	 */
 	public PseudoCodeBoolean(GraphicalRepresentation gr) {
 		commandsforGr = new ArrayList<Stack<Object>>();
 		commandsforGr.add(new Stack<Object>());
 		allGr.add(gr);
 		value = ((GraphicalRepresentationBoolean) gr).isSet();
 	}
 
 	/**
 	 * get literal (java native) value
 	 * 
 	 * @return boolean value
 	 */
 	public boolean getLiteralValue() {
 		return value;
 	}
 
 	/**
 	 * set the inner member + change the outer representations
 	 * 
 	 * @param value
 	 *            to set
 	 */
 	public void setLiteralValue(boolean value) {
 		this.value = value;
 		// operating in batchMode
 		if (isInBatchRun) {
 			commandsforGr.get(0).push(Boolean.valueOf(value));
 		} else {
 			for (GraphicalRepresentation gr : allGr) {
 				((GraphicalRepresentationBoolean) gr).set(this.value);
 			}
 		}
 	}
 
 	/**
 	 * @param value
 	 *            to set
 	 */
 	public void setValue(PseudoCodeBoolean value) {
 		this.setLiteralValue(value.getLiteralValue());
 	}
 
 	@Override
 	public String toString() {
 		String result = new String();
 		if(this.value) {
 			result += "true";
 		}
 		else {
 			result += "false";
 		}
 		return result;
 	}
 
 	@Override
 	public Datatype set(String memberName, Datatype value) {
 		if (memberName.isEmpty()) {
 			this.setValue((PseudoCodeBoolean) value);
 		}
 		return null;
 	}
 	
 	@Override
 	protected void runDelayedCommands() {
 		for (GraphicalRepresentation gr : allGr) {
 			((GraphicalRepresentationBoolean) gr)
 					.set(((Boolean) this.commandsforGr.get(0).pop())
 							.booleanValue());
 		}
		this.commandsforGr.get(0).clear();
 	}
 
 	@Override
 	public boolean equals(Datatype toCheckAgainst) {
 		if(((PseudoCodeBoolean) toCheckAgainst).getLiteralValue() == this.value) {
 			return true;
 		}
 		return false;
 	}
 
 }
