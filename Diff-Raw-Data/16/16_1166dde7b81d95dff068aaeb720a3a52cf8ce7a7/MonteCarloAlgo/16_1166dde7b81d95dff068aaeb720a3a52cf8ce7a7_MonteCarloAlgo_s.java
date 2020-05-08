 package haw.po.la.cliff;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 /**
  * 
  * @author Bjoern Kulas
  * 
  */
 
 public class MonteCarloAlgo implements Algo {
 
 	/*
 	 * Untested class
 	 * 
 	 * TODO If algo works, then combine arrays and use a map
 	 * 
 	 * ATTENTION: The class, that is using an object of this class, is to be in
 	 * charge of control the start and end of each episode
 	 */
 	private Environment env;
 	private double epsilon;
 
 	/*
 	 * x,y -> Pos a -> action
 	 */
 	private double[][][] q;// [x][y][a]
 	private ArrayList<Double>[][][] returns;// [x][y][a]
 	private double[][][] policy;// [x][y][a]
 
 	private double explore;
 	private double exploit;
 	private double policyInit;
 
 	private ArrayList<Pair<Position, Direction>> pairList;
 	private List<Position> statelist;
 	private int episodeCounter;
 	private int lost;
 	private int win;
 
 	/**
 	 * default epsilon = 0.4
 	 * 
 	 * @param env
 	 *            Environment
 	 */
 	public MonteCarloAlgo(Environment env) {
		this(0.4, env);
 	}
 
 	public MonteCarloAlgo(double epsilon, Environment env) {
 		this.epsilon = epsilon;
 		this.env = env;
 		this.explore = this.epsilon / Direction.values().length;
 		this.exploit = (1 - this.epsilon)
 				+ (this.epsilon / Direction.values().length);
 		this.policyInit = 1.0 / Direction.values().length;
 		
 	this.lost=0;
 	this.win =0;
 		
 		initArrays();
 	}
 
 	@SuppressWarnings("unchecked")
 	/**
 	 * create and initialize the arrays: q, returns and policy
 	 */
 	private void initArrays() {
 		this.q = new double[this.env.getWidth()][this.env.getHeigth()][Direction
 				.values().length];
 		this.returns = new ArrayList[this.env.getWidth()][this.env.getHeigth()][Direction
 				.values().length];
 		this.policy = new double[this.env.getWidth()][this.env.getHeigth()][Direction
 				.values().length];
 
 		for (int w = 0; w < this.env.getWidth(); w++) {
 			for (int h = 0; h < this.env.getHeigth(); h++) {
 				for (int a = 0; a < Direction.values().length; a++) {
 					q[w][h][a] = 0.0;// arbitrary value = 0.0
 					returns[w][h][a] = new ArrayList<Double>();// empty list
 					policy[w][h][a] = this.policyInit;// 0,25 --> 25%
 				}
 			}
 		}
 	}
 
 	/**
 	 * use this method before a new episode is starting
 	 */
 	@Override
 	public void startEpisode() {
 		this.pairList = new ArrayList<Pair<Position, Direction>>();
 		this.statelist = new ArrayList<Position>();
 		System.out.println("\n\n\t\tNEW EPSIODE\n\n");
 	}
 
 	/**
 	 * use this method to declare the end of an episode
 	 */
 	@Override
 	public void endEpisode() {
 		this.episodeCounter++;
 		System.out.println("\n\n\t\tEND EPSIODE " + this.episodeCounter
				+ "\n\tWIN: "+this.win+"\tLOST: "+this.lost+"\tW/L-RATIO: "+(double)(this.win/this.lost)+"\n");
 		
 
 		double[] sortedActionEvaluation = new double[4];
 
 		// for each pair (s,a) in episode
 		for (int i = 0; i < this.pairList.size(); i++) {
 
 			Position initialPos = new Position(	this.pairList.get(i).first().x(), this.pairList.get(i)	.first().y());
 			Direction dir = this.pairList.get(i).second();
 
 			// c) pre work: save each state in episode
 			if (!this.statelist.contains(initialPos)) {
 				this.statelist.add(initialPos);
 			}
 
 			// B) set Q(s,a) <-- average(returns(s,a))
 			this.q[initialPos.x()][initialPos.y()][dir.ordinal()] = getAvg(this.returns[initialPos
 					.x()][initialPos.y()][dir.ordinal()]);
 
 		}
 
 		// C) for each state in episode:
 		for (int i = 0; i < this.statelist.size(); i++) {
 
 			// for each s in the episode
 			// save action evaluations of q(s,a) for this state
 			double[] actionEvaluations = this.q[this.statelist.get(i).x()][this.statelist.get(i).y()];
 
 			// array clone
 			sortedActionEvaluation = actionEvaluations.clone(); 
 
 			// sort the cloned array of action evaluations
 			Arrays.sort(sortedActionEvaluation);
 
 			// save  highest  evaluationValue
 			double bestActionEvaluation = sortedActionEvaluation[3];
 
 			
 			//create an array, where the index of actions can be saved
 			int[] actionIndex = new int[4];
 			// initialize actionIndex-Array with -1, cause -1 is no index 
 			for (int a = 0; a < actionIndex.length; a++) {
 				actionIndex[a] = -1;
 			}
 
 			// set Policy: explore probability
 			int indexOfActionIndex = -1;
 			for (int a = 0; a < actionEvaluations.length; a++) {
 				
 				//check which evaluation is not the best evaluation
 				if (this.q[this.statelist.get(i).x()][this.statelist.get(i).y()][a] != bestActionEvaluation) {
 					// policy(s,a) <- epsilon/A(s), if a!=a*
 					this.policy[this.statelist.get(i).x()][this.statelist.get(i).y()][a] = this.explore;
 				} else {
 					indexOfActionIndex++;
 					actionIndex[indexOfActionIndex] = a;// save index of best rated action in actionIndex-Array
 					
 				}
 			}
 
 			if (indexOfActionIndex != -1) {// index of one or more better rated actions saved
 
 				int numOfBestActions = indexOfActionIndex+1;
 		
 				// create an array where the only the index of best rated actions will be saved
 				int[] bestActionIndex = new int[numOfBestActions];
 
 				
 				//save the index of the best rated action
 				bestActionIndex[0]=actionIndex[indexOfActionIndex];
 				
 				
 				//check if only one index of best rated action saved
 				if (numOfBestActions == 1) {
 					this.policy[this.statelist.get(i).x()][this.statelist.get(i).y()][bestActionIndex[0]] = this.exploit;
 				} else {// more than one best rated action saved
 					
 					
 					
 					int indexbestActionArray = 0;
 
 					for (int j = 0; j < actionIndex.length; j++) {
 						//save the index of the best rated actions
 						if (actionIndex[j] != -1) {
 							bestActionIndex[indexbestActionArray] = actionIndex[j];
 							indexbestActionArray++;
 						}
 					}
 					
 					// set randomNumber to choose one of the best rated actions 
 					int randNumber = (int) (Math.random() * numOfBestActions);
 
 					
 					//debug
 					if(randNumber>=numOfBestActions){
 						System.out.println("ERROR 3");
 					}
 					
 					
 					for (int j = 0; j < bestActionIndex.length; j++) {
 						if (randNumber == j) {
 							this.policy[this.statelist.get(i).x()][this.statelist.get(i).y()][bestActionIndex[j]] = this.exploit;
 						} else {
 							this.policy[this.statelist.get(i).x()][this.statelist.get(i).y()][bestActionIndex[j]] = this.explore;
 						}
 
 					}
 				}
 
 			}
 					
 		}
 
 		this.pairList = null;
 		this.statelist = null;
 
 	}
 
 	@Override
 	public void learn(Position initialPos, Direction dir,
 			Position resultingPos, Double reward) {
 		Pair<Position, Direction> firstPair = new Pair<Position, Direction>(
 				initialPos, dir);
 		
 		
 		//if(resultingPos==this.env.getFinishPosition()){
 			if(reward==20.0){
 			this.win++;
 		}
 		if(this.env.getCliffPositions().contains(resultingPos)){
 			this.lost++;
 		}
 		
 		
 		// for each pair (s,a) in episode
 		// first occurrence
 		if (!this.pairList.contains(firstPair)) {
 			this.pairList.add(firstPair);
 			// Append r to returns(s,a)
 			this.returns[initialPos.x()][initialPos.y()][dir.ordinal()]
 					.add(reward);
 //			System.out.println("\t\t\nFROM: "+initialPos+"\t"+dir+"\tto: "+resultingPos+"\t get REWARD: " + reward);
 		}
 	}
 
 	@Override
 	public Direction getDirection(Position pos) {
 
 		Direction[] dirArray = new Direction[1000];
 		int dirArrayIndex = 0;
 		Direction returnDir = null;
 
 		/*
 		 * fill an 1000 slot array by res of 0.1 % the array shows probability
 		 * of actions
 		 */
 		for (int a = 0; a < this.policy[pos.x()][pos.y()].length; a++) {
 			int index = 0;
 			Direction aAction = Direction.values()[a];
 
 			index = (int) (this.policy[pos.x()][pos.y()][a] * 1000);
 
 			
 			
 			for (int i = dirArrayIndex; i < (index + dirArrayIndex); i++) {
 				dirArray[i] = aAction;
 			}
 			dirArrayIndex = dirArrayIndex + index;
 		
 		}
 
 		/*
 		 * now let the probability actrandom between 0 and 999 set the "best"
 		 * direction
 		 */
		returnDir = dirArray[(int) (Math.random() * 1000)];
 
 		return returnDir;
 	}
 
 	/**
 	 * get average value over all values of arrayList
 	 * 
 	 * @param arrayList
 	 *            an arrayList contains value of type double
 	 * @return an average value of all values are in parameter arrayList
 	 */
 	private double getAvg(ArrayList<Double> arrayList) {
 		double avg = 0.0;
 		int counter = 0;
 
 		for (Double rewardsOfStateActionPair : arrayList) {
 			avg += rewardsOfStateActionPair;
 			counter++;
 		}
 
 		avg = avg / counter;
 		return avg;
 	}
 
 }
