 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
 public class Fib {
 	public static BigInteger fib(int n) {
 		return n < 2 ? BigInteger.ONE : fib(n - 1).add(fib(n - 2));
 	}
 
 	public static BigInteger randomFib() {
 		return fib(new Random().nextInt(40));
 	}
 
 	public static void main(String[] args) {
 		ExecutorService svc = Executors.newFixedThreadPool(10);
 		
		final Logger logger = new Logger();
		svc.submit(logger);
		
 		List<Callable<Void>> tasks = new ArrayList<>();
 		for (int i = 0; i < 20; i++) {
 			tasks.add(new Callable<Void>() {
 				public Void call() throws Exception {
					logger.log(Thread.currentThread().getName() + ": " + randomFib());
 					return null;
 				}
 			});
 		}
 
 		try {
 			for (Future<Void> future : svc.invokeAll(tasks)) {
 				future.get();
 			}
 		} catch (InterruptedException e) {
 		} catch (ExecutionException e) {
 		}
 
 		svc.shutdown();
 	}
 }
