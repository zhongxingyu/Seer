 public class Plato implements Runnable {
 	// Variable to store method for multiple runs.
 	DiningPhilosophers methodPhilosopher = null;
 
 	public Plato() {
 
 	}
 
 	public Plato(DiningPhilosophers philoAction) {
 		methodPhilosopher = philoAction;
 	}
 
 	// Variable to store philosopher.
 	int philosopher = 4;
 
 	boolean dining = true;
 
 	public void run() {
 		// Run method to eat, think.
 		methodPhilosopher.DiningPhilosopherRun(philosopher, "Plato");
 		while (true) {
 			methodPhilosopher.think();
 			methodPhilosopher.take_forks(philosopher);
 			methodPhilosopher.eat(philosopher);
 			methodPhilosopher.put_forks(philosopher);
 		}
 	}
 }
