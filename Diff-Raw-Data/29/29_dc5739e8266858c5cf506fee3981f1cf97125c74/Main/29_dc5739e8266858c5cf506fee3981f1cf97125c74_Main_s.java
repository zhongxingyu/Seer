 /*
 Programers: 			Keith Acostsa and Elijah Wright
 Date: 					10/27/2012
 Program Description: 	We programmed show how dining 
 						philosophers react in threading 
 						situations in Java.
  */
 
 public class Main {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
		// Create tasks

 		DiningPhilosophers philoAction = new DiningPhilosophers();
 		
 		Runnable aristotle = new Aristotle(philoAction);
 		Runnable socrates = new Socrates(philoAction);
 		Runnable kant = new Kant(philoAction);
 		Runnable marx = new Marx(philoAction);
 		Runnable plato = new Plato(philoAction);
 
 		// Create threads
 		Thread thread1 = new Thread(aristotle);
 		Thread thread2 = new Thread(socrates);
 		Thread thread3 = new Thread(kant);
 		Thread thread4 = new Thread(marx);
 		Thread thread5 = new Thread(plato);
 
 		// Start threads
 		thread1.start();
 		System.out.println("Aristotle starts");
 		thread2.start();
 		System.out.println("Socrates starts");
 		thread3.start();
 		System.out.println("Kant starts");
 		thread4.start();
 		System.out.println("Marx starts");
 		thread5.start();
 		System.out.println("Plato starts");
 	}
 
 }
