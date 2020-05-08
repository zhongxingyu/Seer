 package concurrent.countdownlatch;
 
 import java.util.concurrent.CountDownLatch;
 import concurrent.countdownlatch.TimerResolver;
 
 public class CountDown{
 	public static void main(String[] args) {
		CountDownLatch latch = new CountDownLatch(3);		
 		Thread t1 = new Thread(new TimerResolver(1000, latch), "Thread 1 ");
 		Thread t2 = new Thread(new TimerResolver(3000, latch), "Thread 3 ");
 		Thread t3 = new Thread(new TimerResolver(500, latch), "Thread 2 ");
 		
 		t1.start();t2.start();t3.start();
 		try {
 			latch.await(); // Espera a que todos los threads hayan finalizado y luego sigue con el thread main
 			System.out.println("Finished!");
 		} catch (InterruptedException e) {		
 			e.printStackTrace();
 		}
 	}
 }
 
 class TimerResolver implements Runnable{
 	private long wait; 
 	private CountDownLatch latch;
 	public TimerResolver(long wait,CountDownLatch latch) {
 		this.wait = wait;
 		this.latch = latch;
 	}
 	@Override
 	public void run() {
 		try {
 			Thread.sleep(wait);
 		} catch (InterruptedException e) {		
 			e.printStackTrace();
 		}
 		System.out.println(Thread.currentThread().getName() + " is finished");
 		latch.countDown(); // one minus
 	}
 }
 	
