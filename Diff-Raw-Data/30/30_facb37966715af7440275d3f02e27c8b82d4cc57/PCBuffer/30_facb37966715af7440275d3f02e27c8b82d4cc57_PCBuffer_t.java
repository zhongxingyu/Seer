 package de.unisiegen.informatik.bs.alvis.sync.datatypes;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Stack;
 
 import de.unisiegen.informatik.bs.alvis.primitive.datatypes.GraphicalRepresentation;
 import de.unisiegen.informatik.bs.alvis.primitive.datatypes.PCInteger;
 import de.unisiegen.informatik.bs.alvis.primitive.datatypes.PCObject;
 
 /**
  * Pseudo code implementation of a buffer
  * 
  * @author Jan Bauerdick
  * 
  */
 
 public class PCBuffer extends PCObject {
 	
	public static final String TYPENAME = "Buffer";
 	
 	/**
 	 * Capacity of the buffer & uses spaces
 	 */
 	private PCInteger capacity;
 	private PCInteger used;
 	
 	public PCBuffer() {
 		capacity = new PCInteger(0);
 		used = new PCInteger(0);
 		commandsforGr = new ArrayList<Stack<Object>>();
 		commandsforGr.add(new Stack<Object>());
 	}
 	
 	public PCBuffer(PCInteger capacity) {
 		this.capacity = capacity;
 		used = new PCInteger(0);
 		commandsforGr = new ArrayList<Stack<Object>>();
 		commandsforGr.add(new Stack<Object>());
 	}
 	
 	public PCBuffer(PCInteger capacity, GraphicalRepresentationBuffer gr) {
 		this.capacity = capacity;
 		used = new PCInteger(0);
 		commandsforGr = new ArrayList<Stack<Object>>();
 		commandsforGr.add(new Stack<Object>());
 		allGr.add(gr);
 	}
 	
 	public PCInteger getCapacity() {
 		return capacity;
 	}
 
 	public void setCapacity(PCInteger capacity) {
 		this.capacity = capacity;
 	}
 
 	public PCInteger getUsed() {
 		return used;
 	}
 
 	public void setUsed(PCInteger used) {
 		this.used = used;
 	}
 	
 	public static String getTypename() {
 		return TYPENAME;
 	}
 	
 	public boolean isGraphical(GraphicalRepresentationBuffer b) {
 		for (GraphicalRepresentation gr : this.allGr) {
 			if (gr == b) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * Insert an item/use a free space
 	 */
 	public void insert() {
 		if (used._less_(new PCInteger(capacity.getLiteralValue() - 1)).getLiteralValue()) {
 			used._inc_();
 		}
 	}
 	
 	/**
 	 * Remove an item/free a used space
 	 */
 	public void remove() {
 		if (used._greater_(new PCInteger(0)).getLiteralValue()) {
 			used._dec_();
 		}
 	}
 
 	@Override
 	public String toString() {
 		return TYPENAME + ": capacity=" + capacity + ", in use=" + used; 
 	}
 
 	public PCObject set(String memberName, PCObject value) {
 		// TODO remove this method
 		if (memberName.equals("capacity")) {
 			capacity = (PCInteger) value;
 			return this;
 		} else if (memberName.equals("used")) {
 			used = (PCInteger) value;
 			return this;
 		} else {
 			return null;
 		}
 	}
 	
 	public PCObject get(String memberName) {
 		// TODO remove this method
 		if (memberName.equals("capacity")) {
 			return capacity;
 		} else if (memberName.equals("used")) {
 			return used;
 		} else {
 			return null;
 		}
 	}
 
 	@Override
 	public boolean equals(PCObject toCheckAgainst) {
 		if (toCheckAgainst instanceof PCBuffer) {
 			PCBuffer b = (PCBuffer) toCheckAgainst;
 			if (b.getCapacity() == getCapacity() && b.getUsed() == getUsed()) {
 				return true;
 			} else {
 				return false;
 			}
 		} else {
 			return false;
 		}
 	}
 	
 	@Override
 	public List<String> getMethods() {
 		String[] methods = { "insert", "remove" };
 		return Arrays.asList(methods);
 	}
 	
 	@Override
 	protected void runDelayedCommands() {
 		for (GraphicalRepresentation gr : allGr) {
 			if (!this.commandsforGr.get(0).isEmpty()) {
 				GraphicalRepresentationBuffer grb = (GraphicalRepresentationBuffer) gr;
 				for (int i = 0; i < grb.getUsage(); i++) {
 					grb.setFull();
 				}
 			}
 		}
 		this.commandsforGr.get(0).clear();
 	}
 	
 	@Override
 	public List<String> getMembers() {
 		String[] members = {"used", "capacity"};
 		return Arrays.asList(members);
 	}
 	
 	@Override
 	public HashMap<String, String> getHelp() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 }
