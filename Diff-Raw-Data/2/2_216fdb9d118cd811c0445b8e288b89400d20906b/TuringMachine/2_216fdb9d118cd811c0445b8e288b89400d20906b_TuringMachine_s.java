 package de.lucaswerkmeister.code.turing;
 
 public abstract class TuringMachine extends Thread {
 	protected final Memory memory;
 	protected static final char BLANK = '☐';
 	private boolean wasStarted = false;
 	protected int state;
 
 	public TuringMachine(String input) {
 		memory = new Memory(BLANK, input);
 	}
 
 	public void run() {
 		wasStarted = true;
 		state = 0;
 		while (state != -1)
 			step(memory.read());
 	}
 
 	protected abstract void step(char input);
 
 	public String getResult() throws IllegalAccessException {
 		if (isAlive() || !wasStarted)
 			throw new IllegalAccessException("Not yet finished");
		return memory.getContent().replaceAll("(^" + BLANK + ")|(" + BLANK + "$)", "");
 	}
 }
