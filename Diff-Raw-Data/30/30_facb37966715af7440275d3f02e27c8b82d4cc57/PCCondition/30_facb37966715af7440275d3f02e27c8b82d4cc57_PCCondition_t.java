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
  * Pseudo code implementation of a condition
  * 
  * @author Jan Bauerdick
  * 
  */
 public class PCCondition extends PCObject {
 
	public static final String TYPENAME = "Condition";
 
 	private PCScenario scenario;
 	private PCThread thread;
 
 	/**
 	 * Semaphore which locks the monitor & number of waiting threads
 	 */
 	private PCSemaphore sema;
 	private PCInteger waiting;
 
 	public PCCondition() {
 		sema = new PCSemaphore();
 		waiting = new PCInteger(0);
 		commandsforGr = new ArrayList<Stack<Object>>();
 		commandsforGr.add(new Stack<Object>());
 	}
 
 	public PCCondition(PCInteger waiting, PCSemaphore sema) {
 		this.waiting = waiting;
 		this.sema = sema;
 		commandsforGr = new ArrayList<Stack<Object>>();
 		commandsforGr.add(new Stack<Object>());
 	}
 
 	public PCCondition(PCInteger waiting, PCSemaphore sema,
 			GraphicalRepresentationCondition gr) {
 		this(waiting, sema);
 		allGr.add(gr);
 	}
 
 	public PCSemaphore getSema() {
 		return sema;
 	}
 
 	public void setSema(PCSemaphore sema) {
 		this.sema = sema;
 	}
 
 	public PCInteger getWaiting() {
 		return waiting;
 	}
 
 	public void setWaiting(PCInteger waiting) {
 		this.waiting = waiting;
 	}
 
 	public static String getTypename() {
 		return TYPENAME;
 	}
 
 	@Override
 	protected void runDelayedCommands() {
 		for (GraphicalRepresentation gr : allGr) {
 			if (!this.commandsforGr.get(0).isEmpty()) {
 				((GraphicalRepresentationCondition) gr).setState(waiting
 						.getLiteralValue());
 			}
 		}
 		this.commandsforGr.get(0).clear();
 	}
 
 	public boolean isGraphical(GraphicalRepresentationCondition c) {
 		for (GraphicalRepresentation gr : this.allGr) {
 			if (gr == c) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Wait for condition
 	 * 
 	 * @param a
 	 *            Actor who has to wait
 	 */
 	public void wait(PCThread t) {
 		synchronized (this) {
 			sema.V();
 			try {
 				waiting._inc_();
 				t.setBlocked(new PCBoolean(true));
 				wait();
 			} catch (InterruptedException e) {
 
 			}
 			waiting._dec_();
 			t.setBlocked(new PCBoolean(false));
 		}
 		sema.P(t);
 	}
 
 	/**
 	 * Signals that condition is free
 	 */
 	public synchronized void signal() {
 		notify();
 	}
 
 	/**
 	 * Signals that condition is free to all waiting threads
 	 */
 	public void signalAll() {
 		notifyAll();
 	}
 
 	public PCObject get(String memberName) {
 		// TODO remove this method
 		if (memberName.equals("semaphore")) {
 			return sema;
 		} else if (memberName.equals("waiting")) {
 			return waiting;
 		} else {
 			return null;
 		}
 	}
 
 	@Override
 	public String toString() {
 		int i = ((PCInteger) sema.get("counter")).getLiteralValue();
 		return TYPENAME + ": " + waiting + " waiting, monitor "
 				+ (i <= 0 ? "locked." : "unlocked.");
 	}
 
 	public PCObject set(String memberName, PCObject value) {
 		// TODO remove this method
 		if (memberName.equals("semaphore")) {
 			PCSemaphore s = (PCSemaphore) value;
 			sema = s;
 			return this;
 		} else if (memberName.equals("waiting")) {
 			PCInteger i = (PCInteger) value;
 			waiting = i;
 			return this;
 		}
 		return null;
 	}
 
 	@Override
 	public boolean equals(PCObject toCheckAgainst) {
 		if (toCheckAgainst instanceof PCCondition) {
 			PCCondition c = (PCCondition) toCheckAgainst;
 			if (c.getWaiting() == getWaiting() && c.getSema().equals(sema)) {
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
 		String[] methods = { "wait", "signal", "signalAll" };
 		return Arrays.asList(methods);
 	}
 
 	@Override
 	public List<String> getMembers() {
 		String[] members = { "sema", "waiting" };
 		return Arrays.asList(members);
 	}
 
 	@Override
 	public HashMap<String, String> getHelp() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 }
