 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 
 public class Philosopher implements Runnable {
 	
 	Waiter waiter;
 	int identifier;
 	int numEat = 0;
 	int numThink = 0;
 	long elapsed,elapEat;
 	Boolean stopped = false;
 	
 	//Constructor
 	public Philosopher(Waiter waiter, int identifier){
 		this.waiter = waiter;
 		this.identifier = identifier;
 	}
 
 	//Thread operations while not interrupted
 	@Override
 	public void run() {
 		String req, eat, finish;
 		
 		while(!stopped){
 			try {
 				think();
 				req = getClockTime();
 				waiter.pickup(identifier);
 				eat = getClockTime();
 				eat();
 				finish = getClockTime();
 				printEat(identifier,numThink,elapEat,req,eat,finish);
 				waiter.putdown(identifier);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}		
 	}
 
 	//Think and print elapsed thinking time
 	private void think() throws InterruptedException {
 		numThink++;
 		long start = System.currentTimeMillis();
 		sleepTime(500);
 		elapsed = System.currentTimeMillis() - start;
 		printThink(identifier,numThink,elapsed);
 	}
 	
 	//Eat and print elapsed eating time
 	private void eat() throws InterruptedException {
 		numEat++;
 		long start = System.currentTimeMillis();
 		sleepTime(500);
 		elapEat = System.currentTimeMillis() - start;
 	}
 	
 	//Obtain clock time in String in "hh:mm" format
 	private static String getClockTime() {
 		Date date = new Date();
 		//Must use a new SimpleDateFormat for each clock time request, as SimpleDateFormat is not thread-safe. 
 		SimpleDateFormat sdf = new SimpleDateFormat("hh:mm");
 		String stringDate = sdf.format(date);
 		sdf = null;
 		date = null;
 		return stringDate;
 	}
 	
 	//Sleep for seemingly random amount of time
 	//Used in think() and eat() methods
 	private void sleepTime(int time) throws InterruptedException {
 		long ms = (long)(time*Math.random());
 		Thread.sleep(ms);
 	}
 	
 	//Formats time data regarding thinking to be printed
 	private void printThink(int i, int count, long time) {
		System.out.println(count + ": Philosopher " + i + " has been thinking for " + time/1000 + " seconds.");
 	}
 	
 	//Formats time data regarding eating to be printed
 	private void printEat(int i, int count, long time, String reqTime, String eatTime, String finishTime) {
 		System.out.println(count + ": Philosopher " + i + " requested to eat at " + reqTime + ", started eating at "
				+ eatTime + ", finished eating at " + finishTime + ", and ate for " + time/1000 + " seconds.");
 	}
 	
     public Boolean getStop() {
         return stopped;
     }
 
     public void setStop(Boolean stop) {
         this.stopped = stop;
    }  
 }
