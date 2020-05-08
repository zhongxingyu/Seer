 package dk.itu.ecdar.text.generator.framework;
 
 /**
  * Base class for controller implementations.
  * 
  * The controller handles input and notifies the automata
  * about it.
  */
 public abstract class IController {
 	
 	ITIOA[] automata;
 	
	// TODO: find a nicer solution for singleton behavior. Maybe just remove abstract from IController?
	protected static IController controllerInstance;
	
	public static IController getInstance() {
		return controllerInstance;
	}
	
 	public IController(ITIOA[] automata) {
 		this.automata = automata;
 	}
 	
 	/**
 	 * Notifies the controller about some input
 	 * @param input Some input
 	 */
 	public void notify(String input) {
 		for (ITIOA a: automata)
 			a.notify(input);
 	}
 	
 	/**
 	 * Executes the controller.
 	 * 
 	 * Synchrony hypothesis: every automaton is started at the same time.
 	 */
 	public void run() {
 		for (ITIOA a: automata)
 			a.run();
 	}
 	
 	@Override
 	public String toString() {
 		return getClass().getSimpleName();
 	}
 }
