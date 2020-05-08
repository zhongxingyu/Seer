 package de.unisiegen.informatik.bs.alvis.sync.datatypes;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Stack;
 
 import de.unisiegen.informatik.bs.alvis.primitive.datatypes.GraphicalRepresentation;
 import de.unisiegen.informatik.bs.alvis.primitive.datatypes.PCBoolean;
 import de.unisiegen.informatik.bs.alvis.primitive.datatypes.PCInteger;
 import de.unisiegen.informatik.bs.alvis.primitive.datatypes.PCObject;
 
 /**
  * Pseudo code implementation of a semaphore
  * 
  * @author Jan Bauerdick
  * 
  */
 
 public class PCSemaphore extends PCObject {
 
	protected static final String TYPENAME = "Semaphore";
 
 	/**
 	 * Semaphore's counter. Semaphore will lock when 0
 	 */
 	private PCInteger counter;
 
 	public PCSemaphore() {
 		counter = new PCInteger(1); //mutex
 		commandsforGr = new ArrayList<Stack<Object>>();
 		commandsforGr.add(new Stack<Object>());
 	}
 
 	public PCSemaphore(PCInteger counter) {
 		this.counter = counter;
 		commandsforGr = new ArrayList<Stack<Object>>();
 		commandsforGr.add(new Stack<Object>());
 	}
 
 	public PCSemaphore(PCInteger counter, GraphicalRepresentationSemaphore gr) {
 		allGr.add(gr);
 		this.counter = counter;
 		commandsforGr = new ArrayList<Stack<Object>>();
 		commandsforGr.add(new Stack<Object>());
 	}
 
 	public PCInteger getCounter() {
 		return counter;
 	}
 
 	public void setCounter(PCInteger counter) {
 		this.counter = counter;
 	}
 
 	public static String getTypename() {
 		return TYPENAME;
 	}
 	
 	public boolean isGraphical(GraphicalRepresentationSemaphore s) {
 		for (GraphicalRepresentation gr : this.allGr) {
 			if (gr == s) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Decrease semaphore, thread will possibly lock
 	 * @param t Actor who calls this
 	 */
 	public synchronized void P(PCThread t) {
 		try {
 			counter._dec_();
 			if (counter._equal_(new PCInteger(0)).getLiteralValue()) {
 				t.setBlocked(new PCBoolean(true));
 			}
 			while (counter._lessOrEqual_(new PCInteger(0)).getLiteralValue()) {
 				wait();
 			}
 			// counter--;
 			t.setBlocked(new PCBoolean(false));
 		} catch (InterruptedException e) {
 		}
 	}
 
 	/**
 	 * Increase semaphore, if counter > 0 a thread will be unlocked
 	 */
 	public synchronized void V() {
 		counter._inc_();
 		notify();
 	}
 
 	@Override
 	public String toString() {
 		return TYPENAME + ": " + counter;
 	}
 
 	public PCObject set(String memberName, PCObject value) {
 		// TODO remove this method
 		if (memberName.equals("counter")) {
 			counter = ((PCInteger) value);
 			return this;
 		} else {
 			return null;
 		}
 	}
 
 	@Override
 	public boolean equals(PCObject toCheckAgainst) {
 		if (toCheckAgainst instanceof PCSemaphore) {
 			PCSemaphore s = (PCSemaphore) toCheckAgainst;
 			if (s.getCounter() == getCounter()) {
 				return true;
 			} else {
 				return false;
 			}
 		} else {
 			return false;
 		}
 	}
 
 	public PCObject get(String memberName) {
 		// TODO remove this method
 		if (memberName.equals("counter")) {
 			return counter;
 		} else {
 			return null;
 		}
 	}
 	
 	@Override
 	public List<String> getMethods() {
 		String[] methods = { "P", "V" };
 		return Arrays.asList(methods);
 	}
 	
 	@Override
 	public List<String> getMembers() {
 		String[] members = {};
 		return Arrays.asList(members);
 	}
 
 	@Override
 	public HashMap<String, String> getHelp() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
 	@Override
 	protected void runDelayedCommands() {
 		for (GraphicalRepresentation gr : allGr) {
 			if (!this.commandsforGr.get(0).isEmpty()) {
 				GraphicalRepresentationSemaphore grs = (GraphicalRepresentationSemaphore) gr;
 				grs.setState(grs.getState());
 			}
 		}
 		this.commandsforGr.get(0).clear();
 	}
 
 }
