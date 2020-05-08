 package ca.uwaterloo.joos;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Logger;
 
 /**
  * 
  * In progress.
  * 
  * Holds a transition table constructed after reading an lr1
  * transition file. Rules are extracted as string arrays
  * through the method getTransition()
  * 
  * @author Matt Baker
  * 
  * 
  *
  */
 
 public class TransitionTable {
 	Logger logger = Main.getLogger();
 	//Holds a table containing the LR(1) transition rules 
 	//for a given CFG.
 	
 	enum LRStruct {
 		BOF,
 		Terminals,
 		NonTerminals,
 		StartState,
 		ProductionRules,
 		LRStates,
 		LRTransitions,
 		LRRules,
 		EOF
 	}
 
 	//TODO Clean
 	private String startState;
 	private String lRStates;
 	private String lRTransitions;
 	private Set<String> terminals;
 	private Set<String> nonTerminals;
 	private List<String[]>productionRules;
 	
 	
 	
 	private Map <Integer, Map<String, Action>> transitionRules;
 	
 	public String getLRTransitions(){
 		//TODO Temporary method for testing remove when finished
 		return lRTransitions.toString();
 	}
 	public TransitionTable(File lr1){
 		//Init
 		this.terminals = new HashSet<String>();
 		this.nonTerminals = new HashSet<String>();
 		this.productionRules = new ArrayList<String[]>();
 		this.transitionRules = new HashMap<Integer, Map<String, Action>>();
 		try {
 			parseFile(lr1);
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	private void parseFile(File lr1) throws IOException{
 		System.out.print("PARSING LR1");
 		FileInputStream inputSteam;
 		BufferedReader	reader = null;
 		int				remainLines = 0;
 		
 		try {
 			inputSteam = new FileInputStream(lr1);
 			reader = new BufferedReader(new InputStreamReader(inputSteam));
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 			logger.warning("TransitionTable.parseFile(): COULD NOT FIND lr1 FILE: " + lr1.toString());
 		}
 		
 		LRStruct stage = LRStruct.BOF;
 		while(reader.ready() && stage != LRStruct.EOF){
 			String line = reader.readLine();
 			String[] split = line.split("\\s");
 			logger.fine(line);
 			
 			
 			if(remainLines == 0) {
 				stage = LRStruct.values()[stage.ordinal() + 1];
 				logger.fine("TransitionTable.parseFile(): STATECHANGE " + stage.toString());
 				if(stage == LRStruct.StartState) this.startState = split[0];
 				else if(stage == LRStruct.LRStates)this.lRStates = split[0];
 				else remainLines = Integer.parseInt(split[0]);
 				continue;
 			}
 			remainLines--;
 			
 			// Parse the line
 			switch(stage) {
 			case Terminals:
 				this.terminals.add(split[0]);
 				break;
 			case NonTerminals:
 				break;
 			case StartState:
 				this.startState = (split[0]);
 				break;
 			case ProductionRules:

 				this.productionRules.add(split);
 				break;
 			case LRStates:
 				this.lRStates = split[0];
 				break;
 			case LRTransitions:
 				this.parseTransition(split);
 				break;
 			case BOF:
 			case EOF:
 			default:
 				break;
 			}
 			
 			
 		}
 	}
 
 	private void parseTransition(String[] split) {
 		//Adds to the parse hash 
		Map<String, Action> transitionRule  = transitionRules.get(split[0]);
 		Action action = null;
 		if (transitionRule == null){
 			transitionRule = new HashMap<String, Action>();
 			this.transitionRules.put(Integer.parseInt(split[0]), transitionRule);
 		}
 		if (split[2].equals("reduce")){
 			ProductionRule nrule = new ProductionRule(this.productionRules.get(Integer.parseInt(split[3]))); //TODO no parseInt...
 			action = new ActionReduce(nrule);
 		}
 		else{
 			action = new ActionShift(Integer.parseInt(split[3]));
 		}
 		String[] rule = {split[2], split[3]}; 
 		transitionRule.put(split[1], action);
 		
 		
 	}
 	
 	/**
 	 * Used to get the next transition action for the parser.
 	 * @param state The current state of the parser
 	 * @param Token The lookahead token
 	 * @return A string array containing the transition rule and the next state
 	 */
 	
 	public Action getTransition(int state, String Token){
 		//TODO replace second param with token type
 		Action ret = null;
 		Map<String, Action> gt = this.transitionRules.get(state);
 		if (gt == null){
			logger.warning("TransitionTable.getTransition(): NULL Transition Rule map returned for state" + state);
 			return null;
		}	
		gt.get(Token);
 		return ret;
 	}
 	
 	abstract class Action{
 		abstract int getInt();
 		abstract int getState();		//Test Method. REMOVE
 		abstract void printRule(); 	//Test Method. REMOVE
 	}
 	
 	class ActionReduce extends Action{//TODO change print to log
 		ProductionRule rule;
 		public int getState(){
 			logger.warning("Action<Reduce>: ERROR: getState called on a reduce action");
 			System.exit(-2);
 			return 0;
 		}
 		
 		public ActionReduce(ProductionRule inrule){
 			rule = inrule;
 		}
 		public ProductionRule getRule(){
 			return rule;
 		}
 		
 		public int getInt(){
 			//Returns the number of symbols to pop off the stack.
 			return rule.righthand.length;
 		}
 		
 		public void printRule(){
 			System.out.print("Action<Reduce>: Rule: " + rule.getLefthand() + " ");
 			for (int i = 0; i < rule.righthand.length; i++){
 				System.out.print(rule.getRighthand()[i] + " ");
 			}
 		}
 	}
 
 	class ActionShift extends Action{
 		int nstate;
 		public ActionShift(int nextstate){
 			this.nstate = nextstate;
 		}
 		public int getState(){
 			return nstate;
 		}
 		public int getInt(){
 			//Returns the state to transition to.
 			return nstate;
 		}
 		public void printRule(){
 			logger.warning("Action<Shift>: printRule called on a SHIFT action");
 			System.exit(-2);
 		}
 	}
 
 
 	class ProductionRule{
 		String lefthand;
 		String[] righthand;
 		
 		public ProductionRule(String[] inrule){
 			lefthand = inrule[0];
 			righthand = new String[inrule.length - 1];
 			
 			for (int i = 1; i < inrule.length; i++){
 				righthand[i - 1] = inrule[i];
 			}
 		}
 		
 		public String getLefthand(){
 			return lefthand;
 		}
 		
 		public String[] getRighthand(){
 			return righthand;
 		}
 		
 	}
 }
