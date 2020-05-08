 import java.util.Random;
 
 public class DiningPhilosophers {
 	String name;
 	int i;
 	int count;
 	int N = 5;
 	int LEFT = (i + N - 1) % N;
 	int RIGHT = (i + 1) % N;
 	int THINKING = 0;
 	int HUNGRY = 1;
 	int EATING = 2;
 
 	int mutex = 10;
 	int[] semaphore = new int[N];
 	int[] state = new int[N];
 	String[] nameArray = new String[N];
 
 	boolean[] blockedArray = new boolean[N];
 	boolean blocked = false;
 
 	public DiningPhilosophers() {
 
 	}
 
 	public void DiningPhilosopherRun(int inI, String Name) {
 
 		i = inI;
 		nameArray[i] = Name;
 
 		System.out.println("Philosopher:" + nameArray[i]);
 
 	}
 
 	public void take_forks(int inI) {
 
 		i = inI;
 
 		down(mutex);
 
 		if (blocked == true) {
 			state[inI] = HUNGRY;
 
 			System.out.println("Semaphore:" + nameArray[inI] + " "
 					+ semaphore[inI] + " Hungry " + state[inI]);
 
 			test(inI);
 
 			up(mutex);
 		}
 
 		if (blocked = true) {
 			down(semaphore[inI]);
 		}
 
 	}
 
 	public void put_forks(int inI) {
 
 		i = inI;
 		
 		down(mutex);
 		state[inI] = THINKING;
 
 		System.out.println("Semaphore: " + nameArray[inI] + " "
 				+ semaphore[inI] + " Thinking " + state[inI]);
 
 		test(LEFT);
 		test(RIGHT);
 		up(mutex);
 
 	}
 
 	private void test(int inI) {
 
 		i = inI;
 		
 		if (state[inI] == HUNGRY && state[LEFT] != EATING
 				&& state[RIGHT] != EATING) {
 			state[inI] = EATING;
 			up(semaphore[inI]);
 		}
 	}
 
 	public void think() {
 
 		Random thinkRand = new Random();
 		System.out.println("Philosopher " + i + " " + nameArray[i]
 				+ " is thinking.");
 
 		try {
			Thread.sleep(thinkRand.nextInt(40));
 			System.out.println("Philosopher " + i + " " + nameArray[i]
 					+ " is Hungry.");
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public void eat(int inI) {
 
 		Random eatRand = new Random();
 		System.out.println("Philosopher " + inI + " " +  nameArray[inI] + " is eating.");
 
 		try {
 			Thread.sleep(eatRand.nextInt(40));
 			System.out
 					.println("Philosopher " + inI + " " + nameArray[inI]
 							+ " is done eating." + " Has ate this many times: "
							+ count);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
		semaphore[i] = 0;
		count++;
 	}
 
 	private void down(int inMutex) {
 
 		// Handles the semaphores critical region.
 		if (inMutex == 10 && semaphore[i] == 0 && blockedArray[i] == true) {
 
 			mutex = 20;
 
 		} else if (inMutex == 20) {
 
 			if (mutex == 20) {
 				System.out.println(nameArray[i] + " is waiting");
 			}
 
 			blocked = true;
 
 		}
 
 		if (inMutex < 5) {
 			for (int i = 0; i < blockedArray.length; i++) {
 				blockedArray[i] = false;
 			}
 			blockedArray[i] = true;
 		}
 	}
 
 	private void up(int inMutex) {
 
 		// Handles the semaphores critical region.
 		if (inMutex == 20) {
 
 			mutex = 10;
 
 		}
 
 		if (inMutex < 5) {
 
 			semaphore[i] = 1;
 
 		}
 	}
 }
