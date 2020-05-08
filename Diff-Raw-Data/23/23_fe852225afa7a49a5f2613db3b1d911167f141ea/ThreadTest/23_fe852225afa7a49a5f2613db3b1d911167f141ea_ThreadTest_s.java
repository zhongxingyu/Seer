
 
 public class ThreadTest {	
 	
 	public static void main(String args[]) {
 		int numberOfThreads = 5;
 		Thread t[] = new Thread[numberOfThreads];
 		System.out.println("Beginning thread test:");
 		for (int i = 0; i < numberOfThreads - 1; i++) {
 		   t[i] = new Thread(new Hello(i));
 		   t[i].start();
 		}
 	}
 }
 
  class Hello implements Runnable {
 	int i = 0;
 	int numberOfTimesRun = 0;
 	
 	Hello(int id) {
 		i = id;
 	}
 	public void run() {
 		for (int j=0; j < 10; j++) {
 			System.out.print(
 				"Hello #" + i + 
 				" numberOfTimesRun=" + numberOfTimesRun++);
 			try {Thread.sleep(
 					(int)(Math.random()*1000));
 			    } // try
 			catch (Exception e){};
 		} // for
 		System.out.println("Hello #" + i + " is done!");
 	}
 }
