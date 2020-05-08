 package com.zw.markov.alg;
 
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Queue;
 import java.util.Set;
 import com.zw.markov.Markov;
 import com.zw.markov.MarkovAction;
 import com.zw.markov.MarkovRecord;
 import com.zw.markov.MarkovState;
 
 public class LayerMarkovBackward {
 	
 	public LayerMarkovBackward(MarkovState state) {
 		this.state = state;
 		generateLayerRecords();
 		initTree();
 		initMarkovInfo();
 	}
 	
 	private final static String LOG_FILE_NAME = "markov_output\\markov_log.txt";
 	private Queue<MarkovState> queue;
 	private Queue<MarkovState> queueTemp;
 	private List<List<MarkovRecord>> allLayerRecords;
 	private List<MarkovRecord> oneLayerRecords;
 	private MarkovState state;
 	
 	private int stateSize;
 	private int actionSize;
 	private double[] utility;
 	private double[][] reward_t;
 	private double[] reward_n;
 	private double[][][] posibility;
 	private double[][][] timeCost;
 	private double[][][] priceCost;
 	
 	private MarkovState[] states;
 	private MarkovAction[] actions;
 	
 	private ActionNode[] actionNodes;
 	private StateNode[] stateNodes;
 	
 	private void generateLayerRecords() {
 		allLayerRecords = new ArrayList<List<MarkovRecord>>();
 		state.setCurrGlobalState(Markov.S_UNKNOWN);
 		state.getActivity(0).setX(-1);
 		state.init();
 		
 		queueTemp = new LinkedList<MarkovState>();
 		queueTemp.offer(state);
 		Set<MarkovState> stateSet = new HashSet<MarkovState>();
 		for (int i = 0; i < 3; i++) {
 			queue = queueTemp;
 			queueTemp = new LinkedList<MarkovState>();
 			oneLayerRecords = new ArrayList<MarkovRecord>();
 			while (!queue.isEmpty()) {
 				state = queue.poll();
 				if (!stateSet.contains(state)) {
 					stateSet.add(state);
 					List<MarkovRecord> records = Markov.noAction(state);
 					addToRecords(records);
 					records = Markov.terminate(state);
 					addToRecords(records);
 					records = Markov.redo(state);
 					addToRecords(records);
 					records = Markov.replace(state);  
 					addToRecords(records);
 					records = Markov.reComposite(state);
 					addToRecords(records);
 				}
 			}
 			allLayerRecords.add(oneLayerRecords);
 		}
 		stateSize++;
 		actionSize++;
 	}
 
 	
 	private void addToRecords(List<MarkovRecord> records){
 		if (records != null && !records.isEmpty()) {
 			oneLayerRecords.addAll(records);
 			for (MarkovRecord rd : records) {
 				queueTemp.offer(rd.getStateAfter());
 				if (rd.getStateBefore().getId() > stateSize) {
 					stateSize = (int) rd.getStateBefore().getId();
 				}
 				if (rd.getStateAfter().getId() > stateSize) {
 					stateSize = (int) rd.getStateAfter().getId();
 				}
 				if (rd.getAction().getId() > actionSize) {
 					actionSize = (int) rd.getAction().getId();
 				}
 			}
 		}
 	}
 
 	public void printRecords() {
 		System.out.println("records size=" + allLayerRecords.size());
 		System.out.println(" StateBefore \t Action \t StateAfter \t Posibility \t Time \t Cost");
 		for (int i = 0; i < allLayerRecords.size(); i++) {
 			System.out.println("Layer " + i);
 			for (MarkovRecord rd : allLayerRecords.get(i)) {
 				System.out.println(rd.toString());
 			}
 		}
 	}
 	
 	public void writeRecords() {
 		System.out.println("records size=" + allLayerRecords.size());
 		try {
 		FileWriter writer = new FileWriter(LOG_FILE_NAME);
 		for (int i = 0; i < allLayerRecords.size(); i++) {
 			writer.append(i + "\n");
 			for (MarkovRecord rd : allLayerRecords.get(i)) {
 				System.out.println(rd.toString());
 				writer.append(rd.toString() + "\n");
 			}
 		}
 		writer.close();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	
 	
 	private void initMarkovInfo() {
 		states = new MarkovState[stateSize];
 		actions = new MarkovAction[actionSize];
 		
 		for (int i = 0; i < allLayerRecords.size(); i++) {
 			for (MarkovRecord rd : allLayerRecords.get(i)) {
 				states[(int) rd.getStateBefore().getId()] = rd.getStateBefore();
 				states[(int) rd.getStateAfter().getId()] = rd.getStateAfter();
 				actions[(int) rd.getAction().getId()] = rd.getAction();
 		 	}
 		}
 		
 		utility = new double[stateSize];
 		reward_n = new double[stateSize];
 		reward_t = new double[stateSize][actionSize];
 		posibility = new double[stateSize][actionSize][stateSize];
 		timeCost = new double[stateSize][actionSize][stateSize];
 		priceCost = new double[stateSize][actionSize][stateSize];
 		
 		for (int i = 0; i < stateSize; i++) {
 			if (!stateNodes[i].hasChild()) {
 				utility[i] = reward_n[i] = getReward(states[i]);
 			} else {
 				utility[i] = reward_n[i] = - Double.MAX_VALUE;
 			}
 		}
		// Fix the bug: the last layer's stateAfter is the leaf, should be getReward
		for (MarkovRecord rd : allLayerRecords.get(allLayerRecords.size()-1)) {
			utility[rd.getStateAfter().getId()] = getReward(rd.getStateAfter());
		}
 		
 		for (List<MarkovRecord> rds : allLayerRecords) { 
 			for (MarkovRecord rd : rds) {
 				reward_t[rd.getStateBefore().getId()][rd.getAction().getId()] = - rd.getPriceCost();
 				posibility[rd.getStateBefore().getId()][rd.getAction().getId()][rd.getStateAfter().getId()] = rd.getPosibility();
 				timeCost[rd.getStateBefore().getId()][rd.getAction().getId()][rd.getStateAfter().getId()] = rd.getTimeCost();
 				priceCost[rd.getStateBefore().getId()][rd.getAction().getId()][rd.getStateAfter().getId()] = rd.getPriceCost();
 			}
 		}
 	}
 	
 
 	
 	public double getBestChose() {
 		for (int i = stateSize-1; i >= 0; i--) {
			if (stateNodes[i].hasChild()) { //Fix the bug: rewrite utility[i]
 				utility[i] = max(i);
			}
 		}
 		return utility[0];
 	}
 	
 	private double max(int i) {
 		double res = - Double.MAX_VALUE;
 		int actionId = -1;
 		for (int a : stateNodes[i].getChildren()) {
 			double temp = reward_t[i][a];
 			for (int j : actionNodes[a].getChildren()) {
 				temp += utility[j] * posibility[i][a][j];
 			}
 		
 			if (res < temp) {
 				res = temp;
 				actionId = a;
 			}
 		}
		System.out.println("state:" + i + " do " + actionId + " utility=" + String.format("%.2f", res));
 		return res;
 	}
 	
 	private void initTree() {
 		stateNodes = new StateNode[stateSize];
 		actionNodes = new ActionNode[actionSize];
 		for (List<MarkovRecord> rds : allLayerRecords) { 
 			for (MarkovRecord rd : rds) {
 				if (stateNodes[rd.getStateAfter().getId()] == null) {
 					stateNodes[rd.getStateAfter().getId()] = new StateNode(rd.getStateAfter().getId());
 				}
 				if (stateNodes[rd.getStateBefore().getId()] == null) {
 					stateNodes[rd.getStateBefore().getId()] = new StateNode(rd.getStateBefore().getId());
 				}
 				if (actionNodes[rd.getAction().getId()] == null) {
 					actionNodes[rd.getAction().getId()] = new ActionNode(rd.getAction().getId());
 				}
 				actionNodes[rd.getAction().getId()].setParent(rd.getStateBefore().getId());
 				if (!actionNodes[rd.getAction().getId()].getChildren().contains(rd.getStateAfter().getId())) {
 					actionNodes[rd.getAction().getId()].addChild(rd.getStateAfter().getId());
 				}
 				stateNodes[rd.getStateAfter().getId()].setParent(rd.getAction().getId());
 				if (!stateNodes[rd.getStateBefore().getId()].getChildren().contains(rd.getAction().getId())) {
 					stateNodes[rd.getStateBefore().getId()].addChild(rd.getAction().getId());
 				}
 				
 			}
 		}
 		
 	}
 	
 	public void printTree() {	
 		for (int i = 0; i < stateNodes.length; i++) {
 			if (stateNodes[i] != null) {
 				System.out.println(stateNodes[i]);
 			}
 		}
 		for (int i = 0; i < actionNodes.length; i++) {
 			if (actionNodes[i] != null) {
 				System.out.println(actionNodes[i]);
 			}
 		}
 	}
 	
 	private double getReward(MarkovState state) {
 		switch (state.getCurrGlobalState()) {
 		case Markov.S_SUCCEED:
 			return ( 1000 );
 		case Markov.S_DELAYED:
 			return (100);
 		case Markov.S_PRICE_UP:
 			return (100);
 		
 		case Markov.S_FAILED:
 			return (-1000);
 		case Markov.S_UNKNOWN:
 			return (-100);
 		default:
 			return 0;
 		}
 	}
 }
