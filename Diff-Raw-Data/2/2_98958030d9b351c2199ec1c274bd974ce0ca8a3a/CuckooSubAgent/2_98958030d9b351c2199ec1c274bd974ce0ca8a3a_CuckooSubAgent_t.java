 package erekspeed;
 
 import ch.idsia.agents.Agent;
 import ch.idsia.benchmark.mario.engine.sprites.Mario;
 import ch.idsia.benchmark.mario.environments.Environment;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.BitSet;
 import java.util.HashMap;
 import java.util.Random;
 
 /**
  * Created by IntelliJ IDEA.
  * User: espeed
  * Date: Aug 6, 2009
  * Time: 12:02:37 PM
  * My agent for the contest.
  */
 public abstract class CuckooSubAgent implements Agent, Serializable {
 	protected String name;
 	public byte[][] mergedObservation;
 	public BitSet mergedObservationBit;
 	protected int rfheight, rfwidth;
 
 	static final long serialVersionUID = 8633319344130163029L;
 
 	public ArrayList<ActionWrapper> actionMap = generateActionMap();
 
 	public ArrayList<MapWrapper> sequentialActions;
 
 	public static CuckooSubAgent getNewAgent() {
 		return CuckooSubAgent.getNewAgent(0);
 	}
 
 	public static CuckooSubAgent getNewAgent(int cap) {
 		switch (ErekSpeedCuckooAgent.AGENT) {
 			case RANDOM:
 				return new CuckooSubRandomAgent(cap);
 			case FJA:
 				return new CuckooSubFJAAgent(cap);
 			case FBJA:
 				return new CuckooSubFBJAAgent(cap);
 			case FBJTA:
 				return new CuckooSubFBJTAAgent(cap);
 		}
 		return new CuckooSubRandomAgent(cap);
 	}
 
 	//TODO: Possibly use ESCA for generating random numbers to ensure different sub agents get different numbers.
 	protected Random random;
 
 	public HashMap<MapWrapper, boolean[]> solution;
 
 	protected float fitness;
 	public String info;
 
 	public CuckooSubAgent(int capacity) {
 		setName("Cuckoo Random Sub Agent");
 		solution = new HashMap<MapWrapper, boolean[]>(capacity);
 		sequentialActions = new ArrayList<MapWrapper>(capacity);
 		random = new Random();
 	}
 
 	public CuckooSubAgent() {
 		this(0);
 	}
 
 	public float getFitness() {
 		return fitness;
 	}
 
 	public void setFitness(float fitness) {
 		this.fitness = fitness;
 	}
 
 	// Generates a hashmap which contains every possible combination of buttons
 	// This allows me to choose random states fairly easily for nest generation
 
 	protected ArrayList<ActionWrapper> generateActionMap() {
 		ArrayList<ActionWrapper> map = new ArrayList<ActionWrapper>();
 
 		// A simple array so that I can use foreach
 		boolean[] vals = {true, false};
 
 		// Determines the current integer mapping.
 		// int count = 0;
 
 		// Nested for loops go through each of the important buttons
 		for (boolean left : vals) {
 			ActionWrapper tList = new ActionWrapper(Environment.numberOfKeys);
 
 			tList.add(Mario.KEY_LEFT, left);
 
 			for (boolean right : vals) {
 				// If we're pressing left we don't want to go right
 				// hence the boolean logic.
 				if (left && right)
 					continue;
 				tList.add(Mario.KEY_RIGHT, right);
 
 				for (boolean down : vals) {
 					// If we're pressing left or right we don't want
 					// to press down, similar to before.
 					if ((left || right) && down)
 						continue;
 
 					tList.add(Mario.KEY_DOWN, down);
 					for (boolean jump : vals) {
 						// Jump and Speed below it fit with any other combination
 						// So these loops are regular.
 						tList.add(Mario.KEY_JUMP, jump);
 						for (boolean speed : vals) {
 							tList.add(Mario.KEY_SPEED, speed);
 
 							// I clone it so the rest of the for loops don't mess
 							// up what's in the HashMap.  There's a warning here
 							// due to generics and clone()
 							map.add(tList.clone());
 						}
 					}
 				}
 			}
 		}
 
 	//	System.out.println(map.size());
 		return map;
 	}
 
 	public void giveIntermediateReward(float r) {
 		//TODO: Should I do something here?
 	}
 
 	@Override
 	public void setObservationDetails(int rfWidth, int rfHeight, int egoRow,
 			int egoCol) {
 		rfheight = rfHeight;
 		rfwidth = rfWidth;
 		MapWrapper.rfheight = rfheight;
 		MapWrapper.rfwidth = rfwidth;
 		
 	}
 
 	public void reset() {
 		// Do nothing here
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public void integrateObservation(Environment environment) {
 		mergedObservationBit = environment.getMergedObservationZZBit(3, 3); // Intermediate
		if(environment.getMarioMode() != 0)
			mergedObservationBit.set(environment.getReceptiveFieldHeight()*environment.getReceptiveFieldWidth()*13 + environment.getMarioMode()-1);
 		//info = environment.getEvaluationInfoAsString();
 	}
 
 	public abstract boolean[] getAction();
 }
