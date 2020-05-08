 package com.zw.markov;
 import java.util.ArrayList;
 import java.util.List;
 import com.zw.ws.Activity;
 import com.zw.ws.AtomService;
 import com.zw.ws.FreeServiceFinder;
 import com.zw.ws.FreeServiceFinderImpl;
 import com.zw.ws.ActivityFlow;
 import com.zw.ws.ReCompositor;
 import com.zw.ws.ReCompositorImpl;
 
 public class MarkovState extends ActivityFlow {
 	
 	public MarkovState() {
 		super();
 		this.id = MarkovInfo.getNextFreeStateID();
 		freeServiceFinder = new FreeServiceFinderImpl();
 		reCompositor = new ReCompositorImpl();
 		init();
 	}
 	
 	public MarkovState(MarkovState state) {
 		super();
 		this.id = MarkovInfo.getNextFreeStateID();
 		freeServiceFinder = new FreeServiceFinderImpl();
 		reCompositor = new ReCompositorImpl();
 	}
 	
 	public MarkovState init() {
 		nextToDoActivity = null;
 		//System.out.println("before init, nextToDoActivity:" + nextToDoActivity);
 		currFailed = false;
 
 		currFinished = true;
 		nextStepTimeCost = -1;
 		nextToDoActivities = new ArrayList<Activity>();
 		for (int i = 0; i < super.getActivitySize(); i++) {
 			if (super.activities.get(i).getX() < 1) {
 				currFinished = false;
 			}
 			if (super.activities.get(i).getX() < 0) {
 				currFailed = true;
 				this.currGlobalState = MarkovInfo.S_FAILED;
 			}
 			if ((super.activities.get(i).getX() < 1) 
 					&& isPrefixActivitiesFinished(super.activities.get(i).getNumber())) {
 				nextToDoActivities.add(super.activities.get(i));
 				double timeCostTemp = 0;
 				if (super.activities.get(i).getX() >=0) {
 					timeCostTemp = (1 - super.activities.get(i).getX())
 						* super.activities.get(i).getBlindService().getQos().getExecTime();
 				} else {
 //					System.out.println(this.getId());
 //					System.out.println(super.activities.get(i).getNumber());
 					timeCostTemp = super.activities.get(i).getBlindService().getQos().getExecTime();
 				}
 				if (nextStepTimeCost > timeCostTemp || nextStepTimeCost == -1) {
 					nextStepTimeCost = timeCostTemp;
 					nextToDoActivity = super.activities.get(i);
 				}
 			}
 		}
 
 		if (nextStepTimeCost > MarkovInfo.TIME_STEP) {
 			nextStepTimeCost = MarkovInfo.TIME_STEP;
 		}
 		if (currFinished && currFailed) {
 			nextStepTimeCost = 0;
 			this.currGlobalState = MarkovInfo.S_FAILED;
 		}
 		if (currFinished && !currFailed) {
 			nextStepTimeCost = 0;
 			this.currGlobalState = MarkovInfo.S_SUCCEED;
 		}
 		if (!currFinished && !currFailed) {
 			this.currGlobalState = MarkovInfo.S_UNKNOWN;
 		}
 		
 		return this;
 	}
 	
 	public long getId() {
 		return id;
 	}
 	
 	public MarkovState clone() {
 		MarkovState stateNew = new MarkovState(this);
 		stateNew.graph = new int[graph.length][graph.length]; //super graph
 		for (int i = 0; i < graph.length; i++) {
 			for (int j = 0; j < graph.length; j++) {
 				stateNew.graph[i][j] = graph[i][j];
 			}
 		}
 		stateNew.activities = new ArrayList<Activity>(); //super activities
 		for (Activity at : super.activities) {
 			stateNew.activities.add(at.clone());
 		}
 		stateNew.services = this.services;
 		stateNew.currGlobalState = this.currGlobalState;
 		stateNew.currTotalTimeCost = this.currTotalTimeCost;
 		stateNew.nextStepTimeCost = this.nextStepTimeCost;
 		stateNew.currFailed = this.currFailed;
 		stateNew.currFinished = this.currFinished;
 		stateNew.redoTimeCost = this.redoTimeCost;
 		stateNew.nextToDoActivities = new ArrayList<Activity>();
 		for (Activity at : this.nextToDoActivities) {
 			stateNew.nextToDoActivities.add(at.clone());
 		}
 		stateNew.nextToDoActivity = (this.nextToDoActivity == null) ? null : this.nextToDoActivity.clone();
 		stateNew.replaceNewService = (this.replaceNewService == null) ? null : this.replaceNewService.clone(); 
 		stateNew.replaceAction = (this.replaceAction == null) ? null : this.replaceAction;
 		
 		stateNew.freeServiceFinder = this.freeServiceFinder; // Mark, this is not clone()
 		stateNew.reCompositor = this.reCompositor;
 		
 		return stateNew;
 	}
 	
 	public List<MarkovState> nextStates(int opNumber) {
 		List<MarkovState> states = new ArrayList<MarkovState>();
 		switch (opNumber) {
 		case MarkovInfo.A_NO_ACTION:
 			if (this.isCurrFailed()) {
 				states.add(this);
 			} else {
 				states.add(this.clone());
 				states.add(this.clone());
 				states = aStepNoAction(states);
 			}
 			return states;
 		case MarkovInfo.A_RE_DO:
 			if (this.isCurrFailed()) {
 				//this.getFailedActivity().addRedoCount();
 				states.add(this.clone());
 				states.add(this.clone());
 				states = aStepReDo(states);
 				return states;
 			} else {
 				return null;
 			}
 		case MarkovInfo.A_REPLACE:
 			if (this.isCurrFailed()) {
 				states.add(this.clone());
 				states.add(this.clone());
 				states = aStepReplace(states);
 				return states;
 			} else {
 				return null;
 			}
 		case MarkovInfo.A_RE_COMPOSITE:
 			if (this.isCurrFailed()) {
 				MarkovState state = reCompositor.recomposite(this);
				//System.out.println("state:" + state);
				if (state == null) {
					return null;
				}
 				state.init();
 				states.add(state.clone());
 				states.add(state.clone());
 				states = aStepReComposite(states);
 				return states;
 			} else {
 				return null;
 			}
 			
 		default:
 			return null;
 		}
 	}
 	
 	
 	public Activity getFailedActivity() {
 		for (int i = 0; i < nextToDoActivities.size(); i++) {
 			if (nextToDoActivities.get(i).getX() < 0) {
 				return nextToDoActivities.get(i);
 			}
 		}
 		return nextToDoActivity;
 	}
 
 	public String toString() {
 		String res = "[State " + String.format("%3d", this.id) + ":";
 		res += " Global_state=";
 		String stateText = "";
 		switch (currGlobalState) {
 		case MarkovInfo.S_UNKNOWN:
 			stateText += "UNKNOW";
 			break;
 		case MarkovInfo.S_FAILED:
 			stateText += "FAILED";
 			break;
 		case MarkovInfo.S_SUCCEED:
 			stateText += "SUCCEED";
 			break;
 		case MarkovInfo.S_DELAYED:
 			stateText += "DELAYED";
 			break;
 		case MarkovInfo.S_PRICE_UP:
 			stateText += "PRICE_UP";
 			break;
 		default:
 			break;
 		}
 		stateText = String.format("%-8s", stateText);
 		res += stateText;
 		res += ", currTimeCost=" + String.format("%7.2f", currTotalTimeCost) + ", nextTimeCost=" 
 				+ String.format("%6.2f", nextStepTimeCost) + " (";
 		for (Activity at : super.activities) {
			//System.out.println("at:" + at.getBlindService());
 			res += "A" + String.format("%1s", at.getNumber()) + ".s=" + at.getBlindService().getNumber() 
 					+  " x=" + String.format("%5.2f", at.getX()) + ", ";
 		}
 		res = res.trim() + ")]";
 		return res;
 	}
 
 	public int getCurrGlobalState() {
 		return currGlobalState;
 	}
 
 	public double getCurrTotalTimeCost() {
 		return currTotalTimeCost;
 	}
 	
 	public void addCurrTotalTimeCost(double cost) {
 		this.currTotalTimeCost += cost;
 	}
 	
 	public double getNextStepTimeCost() {
 		return nextStepTimeCost;
 	}
 
 	public boolean isCurrFailed() {
 		return currFailed;
 	}
 
 	public boolean isCurrFinished() {
 		return currFinished;
 	}
 
 	public double getRedoTimeCost() {
 		return redoTimeCost;
 	}
 
 	public Activity getNextToDoActivity() {
 		return nextToDoActivity;
 	}
 
 	public List<Activity> getNextToDoActivities() {
 		return nextToDoActivities;
 	}
 
 	public ReplaceAction getReplaceAction() {
 		return replaceAction;
 	}
 
 	public AtomService getNextFreeService() {
 		return replaceNewService;
 	}
 
 	public void setCurrGlobalState(int currGlobalState) {
 		this.currGlobalState = currGlobalState;
 	}
 	
 	public Activity getActivity(int activityNumber) {
 		return super.getActivity(activityNumber);
 	}
 
 	public void setActivity(Activity activity) {
 		for (int i = 0; i < super.activities.size(); i++) {
 			if (super.activities.get(i).getNumber() == activity.getNumber()) {
 				super.activities.set(i, activity);
 			}
 		}
 	}
 	
 	private long id;
 	private int currGlobalState;
 	private double currTotalTimeCost; 
 	private double nextStepTimeCost;
 	private boolean currFailed;
 	private boolean currFinished;
 	private double redoTimeCost;
 	
 	private Activity nextToDoActivity;
 	private List<Activity> nextToDoActivities;
 	private ReplaceAction replaceAction;
 	private AtomService replaceNewService;
 	private FreeServiceFinder freeServiceFinder;
 	private ReCompositor reCompositor;
 	
 	private List<MarkovState> aStepNoAction(List<MarkovState> states) {
 
 		for (Activity at : this.nextToDoActivities) {
 			for (int i = 0; i < states.size(); i++) {
 				Activity runActivity = states.get(i).getActivity(at.getNumber());
 				runActivity.addX(nextStepTimeCost / runActivity.getBlindService().getQos().getExecTime());
 			}
 		}
 		states.get(0).addCurrTotalTimeCost(nextStepTimeCost);
 		states.get(1).addCurrTotalTimeCost(nextStepTimeCost);
 		states.get(1).getActivity(this.nextToDoActivity.getNumber()).setX(-1); //Mark
 		for (int i = 0; i < states.size(); i++) {
 			states.get(i).init();
 		}
 
 		return states;
 	}
 	
 	private List<MarkovState> aStepReDo(List<MarkovState> states) {
 
 		for (Activity at : this.nextToDoActivities) {
 			for (int i = 0; i < states.size(); i++) {
 				Activity runActivity = states.get(i).getActivity(at.getNumber());
 				if (runActivity.getX() < 0) {
 					redoTimeCost = Math.abs(runActivity.getX() * runActivity.getBlindService()
 							.getQos().getExecTime());
 				}
 				runActivity.addX(nextStepTimeCost / runActivity.getBlindService().getQos().getExecTime());
 			}
 		}
 		states.get(0).addCurrTotalTimeCost(nextStepTimeCost);
 
 		states.get(1).addCurrTotalTimeCost(nextStepTimeCost);
 		states.get(1).getActivity(this.nextToDoActivity.getNumber()).setX(-1); //Mark
 
 		for (int i = 0; i < states.size(); i++) {
 			states.get(i).init();
 		} 
 
 		return states;
 	}
 
 	private List<MarkovState> aStepReplace(List<MarkovState> states) {
 		
 		for (Activity at : this.nextToDoActivities) {
 			Activity runActivity = states.get(0).getActivity(at.getNumber());
 			if (runActivity.getX() < 0) { //这里是假设只同时出现1个结点故障时
 				replaceNewService = freeServiceFinder.nextFreeService(runActivity.getNumber());
 				if (replaceNewService == null) {
					//System.err.println("Candidate service is all used.");
 					return null;
 				}
 				freeServiceFinder.setServiceUsed(replaceNewService.getNumber());
 
 				states.get(0).getActivity(at.getNumber()).setBlindService(replaceNewService);
 				states.get(0).getActivity(at.getNumber()).setX(1);
 				states.get(0).addCurrTotalTimeCost(replaceNewService.getQos().getExecTime());
 				states.get(1).getActivity(at.getNumber()).setBlindService(replaceNewService);
 				states.get(1).getActivity(at.getNumber()).setX(-1);
 				states.get(1).addCurrTotalTimeCost(replaceNewService.getQos().getExecTime());
 				break;
 			}
 		}
 		for (int i = 0; i < states.size(); i++) {
 			states.get(i).init();
 		}
 		//System.out.println("states:" + states);
 		return states;
 	}
 	
 	private List<MarkovState> aStepReComposite(List<MarkovState> states) {
 		return aStepNoAction(states);
 	}
 	
 	public AtomService getReplaceNewService() {
 		return (replaceNewService);
 	}
 	
 	
 	private boolean isPrefixActivitiesFinished(int currActivityNumber) {
 		List<Integer> prefixActivityNumbers = super.getPrefixActivityNumbers(currActivityNumber);
 		if (prefixActivityNumbers != null && !prefixActivityNumbers.isEmpty()) {
 			for (Integer it : prefixActivityNumbers) {
 				if (super.getActivity(it).getX() != 1) {
 					return false;
 				}
 			}
 		}
 		return true;
 	}
 
 	public FreeServiceFinder getFreeServiceFinder() {
 		return freeServiceFinder;
 	}
 
 	public ReCompositor getReCompositor() {
 		return reCompositor;
 	}
 	
 }
