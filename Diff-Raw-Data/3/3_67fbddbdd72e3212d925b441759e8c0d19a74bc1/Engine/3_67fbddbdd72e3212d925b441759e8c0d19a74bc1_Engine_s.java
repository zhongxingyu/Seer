  package environment;
 
 import java.util.ArrayList;
 import java.util.Hashtable;
 import java.util.Random;
 
 import control.CircleFish;
 import control.FishAI;
 
 public class Engine implements Runnable {
 	public final RuleSet rules;
 	private WorldState backState;
 	private WorldState frontState;
 	private Object stateLock;
 	private ArrayList<FishAI> controllers;
 	private Random rng;
 	private Hashtable <FishState, FishAI> newFish;
 
 	public Engine() {
 		this.rules = RuleSet.dflt_rules();
 		frontState = new WorldState(0);
 		stateLock = new Object();
 		controllers = new ArrayList<FishAI>();
 		rng = new Random();
 		newFish = new Hashtable<FishState, FishAI>();
 		//Add initial fish to the simulation
 	}
 
 	public WorldState getState(long ID) {
 		WorldState rtn = null;
 
 		//Busy-wait until a new state is available
 		while(frontState.seqID <= ID){
 			Thread.yield();
 		}
 
 		synchronized(stateLock) {
 			rtn = frontState;
 		}
 
 		return rtn;
 	}
 
 	private void flipStates() {
 		synchronized(stateLock) {
 			frontState = backState;
 		}
 	}
 
 	private void moveFish() {
 		synchronized (controllers) {
 			for (FishAI ai : controllers) {
 				for (Fish f : ai.myFish) {
 					synchronized (f.requested_state) {
 						FishState old_fs = frontState.get_state(f);
 						FishState requested_fs = f.requested_state;
 						Vector pos = old_fs.getPosition();
 						Vector dir = requested_fs.getRudderVector();
 
 						if (old_fs == null || requested_fs == null) {
 							System.err.println("Oh noes!");
 						}
 
 						double speed = requested_fs.getSpeed();
 						double x = pos.x + speed * dir.x;
 						double y = pos.y + speed * dir.y;
 
 						// TODO: better collision handling
 		    			if (x > rules.x_width || x < 0) {
 		    				x = pos.x;
 		    			}
 		    			if (y > rules.y_width || y < 0) {
 		    				y = pos.y;
 		    			}
 
 		    			FishState new_fs = old_fs.clone();
 		    			new_fs.position = new Vector(x, y);
 		    			new_fs.rudderDirection = dir;
 		    			new_fs.speed = speed;
 		    			//System.out.println("Moving fish " + f.id + " to " + new_fs.getPosition() +
 		    			//", old pos was " + pos + ", speed was " + speed + ", dir was " + dir);
 		    			backState.fish_states.put(f, new_fs);
 	    			}
 	    		}
 	    	}
     	}
     }
 
 	private void collideFish() {
 		synchronized(controllers) {
 			for (FishState f1 : backState.fish_states.values()) {
 				for (FishState f2 : backState.fish_states.values()) {
                     if(f1 == f2 || !f1.alive || !f2.alive) continue;
 
					double dist = Math.sqrt(Math.pow(2, (f1.position.x - f2.position.x)) + Math.pow(2, (f1.position.y - f2.position.y)));
 					if(dist < f1.getRadius() + f2.getRadius()){
 						if(f1.nutrients > f2.nutrients){
 							f2.alive = false;
 							f1.nutrients += (f2.nutrients * 0.8);
 						}else if(f2.nutrients > f1.nutrients){
 							f1.alive = false;
 							f2.nutrients += (f1.nutrients * 0.8);
 						}else{
 							if(Math.random() > 0.5){
 								f2.alive = false;
 								f1.nutrients += (f2.nutrients * 0.8);
 							}else{
 								f1.alive = false;
 								f2.nutrients += (f1.nutrients * 0.8);
 							}
 						}
 					}
 
 				}
 			}
 		}
 	}
 
     private void decayFish() {
     	for (FishState fs : backState.fish_states.values()) {
 			// -1 per fish per round
     		// In the future, we might want to make this dependent on fish size
     		fs.nutrients -= 1;
 
     		// -1 per unit of speed
     		fs.nutrients -= (int) fs.speed;
 
     		if (fs.nutrients <= 0) {
     			fs.alive = false;
     		}
     	}
     }
 
     private void spawnFish() {
         synchronized(newFish){
             for (FishState fs : newFish.keySet()) {
                 Fish f = new Fish();
                 FishAI ai = newFish.get(fs);
                 // TODO: thread safety!
                 ai.myFish.add(f);
                 backState.fish_states.put(f, fs);
             }
             newFish.clear();
         }
     }
 
     /* Temporary function - will need to remove later */
     public void add () {
     	synchronized (controllers) {
 	    	if (controllers.isEmpty()) {
 	    		FishAI ai = new CircleFish(this);
 	    		controllers.add(ai);
 	    		Thread ai_thread = new Thread(ai);
 	    		ai_thread.start();
 	    	}
 	    	FishAI ai = controllers.get(0);
 	    	FishState fs = new FishState();
 	    	fs.position = new Vector(rng.nextInt(rules.x_width - 150)+75,
 	    			rng.nextInt(rules.y_width - 150) + 75);
 	    	fs.rudderDirection = new Vector(0, 0);
 	    	fs.speed = 0;
 
             synchronized(newFish){
                 newFish.put(fs, ai);
             }
     	}
     }
 
     public void run() {
         long numStates = 0;
         add();
         while(true) {
         	System.out.println("iteration - state id is " + numStates);
         	try {
 				Thread.sleep(100);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 			backState = new WorldState(numStates++);
 
 			//Calculate the next state from frontState into the backState
 			moveFish();
 
             collideFish();
 
             decayFish();
 
 			spawnFish();
 
 			//Push backState to be the new frontState
 			flipStates();
 		}
 	}
 }
 
