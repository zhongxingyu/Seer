 package philosopher;
 
 import state.State;
 import state.ThinkingState;
 import behaviour.BehaviourPattern;
 
 public class Philosopher implements Runnable {
 
 	private static int id = 0;
 
 	private int myId;
 	private State state;
 	private BehaviourPattern behaviour;
 	private Token left;
 	private Token right;
 	private boolean sittingAtTable = false;
 
 	private Philosopher(Token left, Token right) {
 		this.state = ThinkingState.create(this);
 		this.left = left;
 		this.right = right;
 		this.myId = id;
 		id++;
 	}
 
 	public static Philosopher create(Token left, Token right) {
 		return new Philosopher(left, right);
 	}
 
 	@Override
 	public void run() {
 		while (sittingAtTable) {
			this.waitWithTime(PTOMonitor.getInstance().getWaitingTime());
 			this.changeState();
 		}
 	}
 
 	private synchronized void waitWithTime(long wait) {
 		try {
 			Thread.sleep(wait);
 		} catch (InterruptedException e) {
 		}
 	}
 
 	public void start() {
 		this.setSittingAtTable(true);
 		Thread thread = new Thread(this);
 		thread.start();
 	}
 
 	public void stop() {
 		this.setSittingAtTable(false);
 	}
 
 	private void changeState() {
 		System.out.println("statewechsel");
 		this.getBehaviour().changeState(this);
 	}
 
 	public void reportMeThinking() {
 		PTOMonitor.getInstance().addThinking(this);
 	}
 
 	public void reportMeEating() {
 		PTOMonitor.getInstance().addEating(this);
 	}
 
 	/*********** Getter and Setter ***********/
 
 	public State getState() {
 		return state;
 	}
 
 	public void setState(State state) {
 		this.state = state;
 	}
 
 	public Token getLeft() {
 		return left;
 	}
 
 	public void setLeft(Token left) {
 		this.left = left;
 	}
 
 	public Token getRight() {
 		return right;
 	}
 
 	public void setRight(Token right) {
 		this.right = right;
 	}
 
 	private static void setId(int id) {
 		Philosopher.id = id;
 	}
 
 	public int getMyId() {
 		return myId;
 	}
 
 	public void setMyId(int myId) {
 		this.myId = myId;
 	}
 
 	public BehaviourPattern getBehaviour() {
 		return behaviour;
 	}
 
 	public void setBehaviour(BehaviourPattern behaviour) {
 		this.behaviour = behaviour;
 	}
 
 	public boolean isSittingAtTable() {
 		return sittingAtTable;
 	}
 
 	public void setSittingAtTable(boolean sittingAtTable) {
 		this.sittingAtTable = sittingAtTable;
 	}
 }
