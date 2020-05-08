 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 class prime_console{
 
 	static int masterAlive=1;
 	static ThreadGroup MyThGroup=new ThreadGroup("Primes");
 
 	public static class Watcher extends Thread{
 
 		public void run(){
 			while (masterAlive==1){
 				try {
 					Watcher.sleep(1000);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 				
 				System.out.println("Threads in " + Thread.currentThread().getThreadGroup().getName() + " : "+Integer.toString(Thread.currentThread().getThreadGroup().activeCount()));
 				System.out.println("Threads in "+ MyThGroup.getName() +": "+Integer.toString(MyThGroup.activeCount()));
 				System.out.println(Runtime.getRuntime().freeMemory()/1024/2014+ " MB free of " + Runtime.getRuntime().totalMemory()/1024/2014 + " MB Total, MAX is " + Runtime.getRuntime().maxMemory()/1024/2014 + " MB");
 			}
 			System.out.println("Watcher done!");
 		}	
 
 	}
 	public static class MyThread extends Thread{
 
 		double n;
 
 		MyThread(ThreadGroup tg, String n){
 			super(tg,n);
 			this.n=Double.valueOf(n);
 		}
 
 		public void run(){
 			is_prime(n);
 		}
 
 		public void is_prime(double n){
 			boolean prim=true;
 			double j;
 	        for (j=2;j<=Math.sqrt(n);j++){
 	                if (n % j==0){
 	                        prim=false;
 	                        break;
 	                }
 	        }
 	        if (prim){
 	                System.out.println(BigDecimal.valueOf(n));
 	        }
 		}
 
 	}
 
 	public static void main (String[] args) throws InterruptedException{
 
 		Watcher w=new Watcher();
 		w.start();
 
 		double min;
 		double max;
 
 		ArrayList<Thread> threads=new ArrayList<Thread>();
 		
 		if (args.length>0){
 
 			min=Double.valueOf(args[0]);
 			max=Double.valueOf(args[1]);
 		}
 		else{
 			min=Double.valueOf("1");
             max=Double.valueOf("100");
         }
 
 		double i;
 		for (i=min;i<=max;i++){
 			MyThread t=new MyThread(MyThGroup,String.valueOf(i));
 			if (MyThGroup.activeCount()<10){
				t.start();
 				//we just add tasks that do not get to be immediately joined
 				threads.add(t);
 			}
 			else{
 				try {
 					//more than 10 runners, we join immediately
 					t.join();
 //					System.out.println("Joining directly:"+t.getName());
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		
 		//here we join all threads not previously joined
 		Iterator<Thread> itr=threads.iterator();
 		while (itr.hasNext()){
//			Thread crt_t=itr.next();
//			crt_t.join();
//			System.out.println("Joining later:"+crt_t.getName());
 			itr.next().join();
 		}
 				
 		masterAlive=0;
 		w.join();
 		System.out.println("Master done!");
 	}
 
}
