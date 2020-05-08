 package concurrent.primes;
 
 public class CalculatePrimeNumbers implements Runnable{
 	private Counter counter;
 	private Counter primesCount;
 	private int MAX_CALCULATION = 100;
 	private static final byte SLEEPING = 5;
 
 	public CalculatePrimeNumbers(int MAX){
 		this.MAX_CALCULATION = MAX;
 		initializer();
 	}
 	
 	public CalculatePrimeNumbers() {
 		initializer();
 	}
 	
 	public void initializer(){
 		counter = new Counter(2);
 		primesCount = new Counter();
 	}
 	
 	@Override
 	public void run() {
 			while(counter.getCounter() < MAX_CALCULATION){
				synchronized (counter) {
					if(isPrime(counter.getCounter())){
						primesCount.setCounter();
						System.out.println(Thread.currentThread().getName() + " :  " + counter.getCounter());
					}
					counter.setCounter();// count++ but synchronized
 				}
				
 				try {
 					Thread.sleep(SLEEPING);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
			System.out.println(primesCount.getCounter());
 	}
 	
 	private boolean isPrime(int number){
 		boolean isPrime = true;
 		
 		if(number % 2 == 0){
 			return !isPrime;
 		}
 		
 		for (int n = 3; (n*n) <= number; n+=2) {
 			if(number % n == 0){
 				return !isPrime;
 			}
 		}
 		return isPrime;
 	}
 
 	public static void main(String[] args) {
 		CalculatePrimeNumbers cpn = new CalculatePrimeNumbers();
 		Thread t  = new Thread(cpn,"Th1");
 		Thread t2 = new Thread(cpn,"Th2");
 		t.start();t2.start(); // init thread
 	}
 
 }
 
 class Counter{
 	private int counter;
 	public Counter() {		
 	}
 	public Counter(int n) {
 		this.counter = n;
 	}
 	public synchronized int getCounter() {
 		return counter;
 	}
 
 	public synchronized void setCounter() {
 		this.counter++;
 	}
 }
