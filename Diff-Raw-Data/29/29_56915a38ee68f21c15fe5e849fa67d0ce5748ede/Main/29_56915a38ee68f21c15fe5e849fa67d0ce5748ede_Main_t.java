 package training.concurrency;
 
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 public class Main {
 
 	public static void main(String[] args) throws ExecutionException, InterruptedException {
 		
 		ExecutorService pool = Executors.newFixedThreadPool(10);
 		Logger logger = new Logger();
 		pool.execute(logger);
 
		while (true) {
 			pool.execute(new Fibonacci(logger));
 		}
 
 //		pool.shutdown();
 //		pool.awaitTermination(10, TimeUnit.SECONDS);
 //
 //		System.out.println("done");
 	}
 
 }
