 
 public class Waiter {
 	
 	//State enum
 	private enum State {
 		HUNGRY, EATING, THINKING
 	}
 
 	private State[] state;
 	boolean[] leftHungry, rightHungry;
 	private int philoNum;
 	
 	//Waiter constructor
 	public Waiter(int philoNum){
 		this.philoNum = philoNum;
 		state  = new State[philoNum];
		leftHungry = new boolean[philoNum];
		rightHungry = new boolean[philoNum];
 		for(int i = 0; i < philoNum; i++) {
 			state[i] = State.THINKING;
 			leftHungry[i] = false;
 			rightHungry[i] = false;
 		}
 	}
 
 	//Request to eat
 	public synchronized void pickup(int i) throws InterruptedException {
 		setState(i,State.HUNGRY);
 		test(i);
 		if(state[i] != State.EATING)
 			this.wait();
 		rightHungry[leftPhilo(i)] = false;
 	    leftHungry[rightPhilo(i)] = false;
 	}
 
 	//Signal for having finished eating
 	public synchronized void putdown(int i) {
 		setState(i,State.THINKING);
 		test(leftPhilo(i));
 		if (state[leftPhilo(i)] == State.HUNGRY)
 	         leftHungry[i] = true;
 		test(rightPhilo(i));
 		if (state[rightPhilo(i)] == State.HUNGRY)
 	         rightHungry[i] = true;
 	}
 	
 	//Test for permission to eat/availability of chopsticks
 	private synchronized void test(int i) {
 		if  ((state[rightPhilo(i)] != State.EATING) && 
 		           (state[i] == State.HUNGRY) &&
 		           (state[leftPhilo(i)] != State.EATING) &&
 		           !leftHungry[i] && !rightHungry[i]) {
 			state[i] = State.EATING;
 		    this.notifyAll();
 		}
 	}
 
 	//State getter
 	public State getState(int i) {
 		return state[i];
 	}
 
 	public void setState(int i, State newState) {
 		state[i] = newState;
 		sanityCheck();
 	}
 
 	private int leftPhilo(int i) {
 		return (i + philoNum - 1) % philoNum;
 	}
 	
 	private int rightPhilo(int i) {
 		return (i + 1) % philoNum;
 	}
 	
 	//Borrowed from CWRU EECS338 example solution
 	//Makes sure we don't have two consecutive philosophers eating
     private void sanityCheck() {
         for(int i = 0; i < philoNum; i++) {
             if(state[i] == State.EATING &&
                (state[ leftPhilo(i)] == State.EATING ||
                 state[rightPhilo(i)] == State.EATING))
             {
                 throw new IllegalStateException();
             }
         }
     }
 }
