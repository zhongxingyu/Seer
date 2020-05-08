 package ch14.ex09;
 
 public class ActiveShortRunnable implements Runnable{
 	public void run() {
		// ZƂ邽10񂾂[v
 		for (int i = 0; i < 10; i++) {
 			try {
 				Thread.sleep(1000);
 			} catch (InterruptedException e) {
 				return;
 			}
 		}
 	}
 }
