 
 public class DiningPhilosophers {
 	
 	//Default values for philosopher number and duration
 	public static final int PHILOSOPHERS = 5;
 	public static final int DURATION = 5000;
 	
 	public static void main(String[] args) throws InterruptedException{
 		
 		int philosophers = PHILOSOPHERS;
 		int duration = DURATION;
 		
 		//Parse user-provided values
 		try{
			philosophers = Integer.parseInt(args[0]); //Allows user to specify number of philosophersd
 			duration = 1000*Integer.parseInt(args[1]); //Allows user to enter duration of run in seconds
 		} catch (NumberFormatException e) {
 			System.out.println("Please input two integer arguments. The first is the number of philosophers at the table. The second" +
 					" respresents how long the philosophers will eat.");
 		}
 		
 		//WAITER! WAITER!
 		Waiter waiter = new Waiter(philosophers);
 		
 		//Gimme some philosophers to put at this table
 		Philosopher[] philos = new Philosopher[philosophers];
 		for(int i = 0; i < philosophers; i++){
 			philos[i] = new Philosopher(waiter,i);
 		}
 		
 		//Wait for philosophers to eat and think
 		try{
 			Thread.sleep(duration);
 		} catch (InterruptedException e){}
 		
 		//These philosophers have better places to be.
 		for(int i = 0; i < philosophers; i++){
 			philos[i].setStop(true);
 		}
 	}
 
 }
