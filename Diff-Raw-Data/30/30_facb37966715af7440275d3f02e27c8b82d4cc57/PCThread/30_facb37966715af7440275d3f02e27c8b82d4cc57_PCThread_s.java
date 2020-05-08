 package de.unisiegen.informatik.bs.alvis.sync.datatypes;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Stack;
 
 import de.unisiegen.informatik.bs.alvis.primitive.datatypes.GraphicalRepresentation;
 import de.unisiegen.informatik.bs.alvis.primitive.datatypes.PCBoolean;
 import de.unisiegen.informatik.bs.alvis.primitive.datatypes.PCInteger;
 import de.unisiegen.informatik.bs.alvis.primitive.datatypes.PCList;
 import de.unisiegen.informatik.bs.alvis.primitive.datatypes.PCObject;
 
 /**
  * Pseudo code implementation of a thread
  * 
  * @author Jan Bauerdick
  * 
  */
 
 public class PCThread extends PCObject implements Runnable {
 
	protected static final String TYPENAME = "Thread";
 
 	private PCScenario scenario;
 	private PCList<PCSemaphore> semas;
 	private PCList<PCCondition> conds;
 	private PCList<PCInteger> ints;
 	private PCList<PCBoolean> bools;
 
 	private PCBoolean doStep;
 	private PCBoolean isBlocked;
 	private PCInteger currentLine;
 
 	public PCThread() {
 		semas = new PCList<PCSemaphore>();
 		conds = new PCList<PCCondition>();
 		ints = new PCList<PCInteger>();
 		bools = new PCList<PCBoolean>();
 		doStep = new PCBoolean(false);
 		isBlocked = new PCBoolean(false);
 		currentLine = new PCInteger(0);
 		commandsforGr = new ArrayList<Stack<Object>>();
 		commandsforGr.add(new Stack<Object>());
 	}
 
 	public PCThread(GraphicalRepresentationThread gr) {
 		allGr.add(gr);
 		if (gr != null) {
 			isBlocked = new PCBoolean(gr.getBlocked());
 		}
 		semas = new PCList<PCSemaphore>();
 		conds = new PCList<PCCondition>();
 		ints = new PCList<PCInteger>();
 		bools = new PCList<PCBoolean>();
 		doStep = new PCBoolean(false);
 		isBlocked = new PCBoolean(false);
 		currentLine = new PCInteger(0);
 		commandsforGr = new ArrayList<Stack<Object>>();
 		commandsforGr.add(new Stack<Object>());
 	}
 
 	public void setScenario(PCScenario scenario) {
 		this.scenario = scenario;
 	}
 
 	public PCScenario getScenario() {
 		return scenario;
 	}
 
 	public void setSemas(PCList<PCSemaphore> semas) {
 		this.semas = semas;
 	}
 
 	public PCList<PCSemaphore> getSemas() {
 		return semas;
 	}
 
 	public void setConds(PCList<PCCondition> conds) {
 		this.conds = conds;
 	}
 
 	public PCList<PCCondition> getConds() {
 		return conds;
 	}
 
 	public void setInts(PCList<PCInteger> ints) {
 		this.ints = ints;
 	}
 
 	public PCList<PCInteger> getInts() {
 		return ints;
 	}
 
 	public void setBools(PCList<PCBoolean> bools) {
 		this.bools = bools;
 	}
 
 	public PCList<PCBoolean> getBools() {
 		return bools;
 	}
 
 	public PCBoolean isDoStep() {
 		return doStep;
 	}
 
 	public void setDoStep(PCBoolean doStep) {
 		this.doStep = doStep;
 	}
 
 	public PCBoolean isBlocked() {
 		return isBlocked;
 	}
 
 	public void setBlocked(PCBoolean isBlocked) {
 		this.isBlocked = isBlocked;
 	}
 
 	public PCInteger getCurrentLine() {
 		return currentLine;
 	}
 
 	public void setCurrentLine(PCInteger currentLine) {
 		this.currentLine = currentLine;
 	}
 
 	@Override
 	protected void runDelayedCommands() {
 		for (GraphicalRepresentation gr : allGr) {
 			if (!this.commandsforGr.get(0).isEmpty()) {
 				((GraphicalRepresentationThread) gr).setBlocked(isBlocked
 						.getLiteralValue());
 			}
 		}
 		this.commandsforGr.get(0).clear();
 	}
 	
 	public boolean isGraphical(GraphicalRepresentationThread t) {
 		for (GraphicalRepresentation gr : this.allGr) {
 			if (gr == t) {
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	@Override
 	public List<String> getMembers() {
 		String[] members = {"semas", "conds", "ints", "bools"};
 		return Arrays.asList(members);
 	}
 	
 	public List<String> getMethods() {
 		String[] methods = {"run", "equal", "notEqual"};
 		return Arrays.asList(methods);
 	}
 
 	/**
 	 * Perform a single step
 	 */
 	public synchronized void step() {
 		if (!doStep.getLiteralValue() && !isBlocked.getLiteralValue()) {
 			doStep = new PCBoolean(true);
 			notify();
 		}
 	}
 
 	/**
 	 * Highlight a line in source
 	 * 
 	 * @param line
 	 */
 	public synchronized void showline(PCInteger line) {
 		currentLine = line;
 		doStep = new PCBoolean(false);
 		while (!doStep.getLiteralValue()) {
 			try {
 				wait();
 			} catch (InterruptedException e) {
 
 			}
 		}
 	}
 
 	@Override
 	public String toString() {
 		return TYPENAME + ": " + (isBlocked.getLiteralValue() ? "" : "not")
 				+ " blocked on line " + currentLine;
 	}
 
 	public PCObject set(String memberName, PCObject value) {
 		// TODO remove this method
 		if (memberName.equals("doStep")) {
 			PCBoolean b = (PCBoolean) value;
 			doStep = b;
 			return this;
 		} else if (memberName.equals("isBlocked")) {
 			PCBoolean b = (PCBoolean) value;
 			isBlocked = b;
 			return this;
 		} else if (memberName.equals("currentLine")) {
 			PCInteger i = (PCInteger) value;
 			currentLine = i;
 			return this;
 		}
 		return null;
 	}
 
 	@Override
 	public boolean equals(PCObject toCheckAgainst) {
 		if (toCheckAgainst instanceof PCThread) {
 			PCThread a = (PCThread) toCheckAgainst;
 			if (a.isBlocked() == isBlocked() && a.isDoStep() == isDoStep()
 					&& a.getCurrentLine() == getCurrentLine()) {
 				return true;
 			} else {
 				return false;
 			}
 		} else {
 			return false;
 		}
 	}
 
 	@Override
 	public void run() {
 		// TODO Auto-generated method stub
 		//Hier die Algo-Methode reinfummeln
 	}
 
 	@Override
 	public HashMap<String, String> getHelp() {
 		HashMap<String, String> help = new HashMap<String, String>();
 		return help;
 	}
 
 }
