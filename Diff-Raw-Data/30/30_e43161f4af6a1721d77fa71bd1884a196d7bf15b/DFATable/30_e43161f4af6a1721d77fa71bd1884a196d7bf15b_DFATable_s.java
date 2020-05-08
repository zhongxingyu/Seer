 package Source;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.Set;
 
 /**
  * @author Sam
  * This is the DFA Table, which is basically a Hashmap with a Key of (State, character) and value of (State)
  */
 @SuppressWarnings("serial")
 public class DFATable extends HashMap<StateCharacter, State> {
 	//<From, To>
 	private HashMap<StateCharacter, State> ourTable = new HashMap<StateCharacter, State>() ; //new HashMap<StateCharacter, State>[100];
 	private HashMap<State, ArrayList<State>> tmpDFA = new HashMap<State, ArrayList<State>>();
 	private DFAState currentState;
 	private DFAState initialState;
 	private ArrayList<Character> langList;
 	private ArrayList<DFAState> dfaStateList = new ArrayList<DFAState>();
 	
 
 	/*
 	 * e-closure(s)
 	 * Set of NFA States reachable from NFA state s on e-transition alone
 	 * 
 	 * Algorithm:
 	 * Get some state S
 	 * for every state adjacent to state S that has a e-transition:
 	 * 
 	 */
 	
 	/*
 	 * eclosuer(t)
 	 * Set of NFA staates reachable from some NFA State s in set T on 
 	 * e-transitions alone
 	*/
 	
 	/*
 	 * move(T, a)
 	 * Set of NFA states to which there is a transition on input symbol
 	 * a from some state s in T
 	 */
 	//
 	
 	
 	public DFATable() {
 		super();
 		// TODO :: Everything about DFA Table
 	}
 	
 	public State getStartState() {
 		return dfaStateList.get(0);
 	}
 	
 	/**
 	 * Creates a DFA and then the DFATable from the NFA
 	 * @param n
 	 */
 	public DFATable (NFA n) {
 		
 		
 		
 		//Get all the characters for the language
 		State s = n.entry;
 		this.langList = generateAlphabet(s);
 		
		System.out.println(" AlphabeT: [");
 		for(Character c : langList) {
 			System.out.print("'"+c + "' ");
 		}
 		System.out.println("]\n");
 		
 		recurseStatesWill(s);	
 		
 		
 
 		//ArrayList<State> eClousers = getEpsilonAdjList(s);
 	
 		//System.out.println(dfaStates);
 		for(DFAState st : this.dfaStateList) {
 			//recurseStates(st);
 			//System.out.println(st + " is linked to:");
 			Iterator<Entry<Character,State>> myIt = st.getCharEdges().entrySet().iterator();
 			while(myIt.hasNext()){
 				Entry<Character,State> myEnt = myIt.next();
 				//System.out.println(myEnt.getValue() + " on " + myEnt.getKey());
 				this.put(new StateCharacter(st, myEnt.getKey()), myEnt.getValue());
 			}
 		}
 	}
 	
 	public void recurseStatesWill(State s) {
 		if(s.visited)
 			return;
 		ArrayList<State> eClousers = getEpsilonAdjList(s);
 		DFAState currentState = new DFAState();
 		currentState.setAdjacentList(eClousers);
 		dfaStateList.add(currentState);
 		recurseWillAgain(eClousers);
 		
 		
 	}
 	
 	public void recurseWillAgain(ArrayList<State> dfa){
 		ArrayList<State> tmpList;
 		DFAState tmpDFAState = new DFAState();
 		//ArrayList<State> tmpDFA = new ArrayList
 		for(char c : this.langList) {
 			tmpList = new ArrayList<State>();
 			for(State ss: dfa) {
 				
 				for(Entry<Character, State> cc : ss.getCharEdges().entrySet()) {
 					if(c == cc.getKey()) {
 						tmpList.add(cc.getValue());
 						
 					}
 				}
 				
 					ss.visited = true;
 			}
 			tmpDFAState = new DFAState();
 			ArrayList<State> tmpEClose = new ArrayList<State>();
 			for(State tmpState : tmpList) {
 				tmpEClose = getEpsilonAdjList(tmpState);
 				//tmpEClose.add(tmpDFAState);
 				
 				if(tmpDFAState.getAdjacentList() == null){
 					tmpDFAState.setAdjacentList(tmpEClose);
 				}
 				else{
 					for(State toAdd:tmpEClose){
 					if(!doesContain(tmpDFAState.getAdjacentList(),toAdd)){
 						tmpDFAState.append(toAdd);
 					}
 				}
 				}
 				
 				//tmpDFAState.setAdjacentList(tmpEClose);
 				//tmpDFAState.addepsilonEdge(tmpDFAState);
 				
 				
 			}
 			if(!davidEquals(this.dfaStateList, tmpDFAState)) {
 				
 				for(State n:tmpDFAState.getAdjacentList()){
 					if(n.isFinal){
 						tmpDFAState.isFinal = true;
 						tmpDFAState.tokenName = n.tokenName;
 					}
 				}
 				
 				if(tmpDFAState.isFinal){
 					//check for char edges, stop if there are none
 					State theFinalOne = new State();
 					for (State m:tmpDFAState.getAdjacentList()){
 						if(m.isFinal){
 							theFinalOne = m;
 						}
 					}
 					this.dfaStateList.add(tmpDFAState);
 					createLink(dfa,c,tmpDFAState);
 					if(checkTransitions(tmpDFAState)){
 						recurseWillAgain(tmpDFAState.getAdjacentList());
 					}
 				
 				}
 				else{
 					createLink(dfa,c,tmpDFAState);
 
 				this.dfaStateList.add(tmpDFAState);
 				if(checkTransitions(tmpDFAState)){
 					recurseWillAgain(tmpDFAState.getAdjacentList());
 				}				
 				}
 				
 			}
 			else {
 				if(tmpDFAState.getAdjacentList() != null){
 				DFAState toLinkTo = davidGet(this.dfaStateList,tmpDFAState);
 				createLink(dfa,c,toLinkTo);
 			}}
 		}
 	
 	}
 	private ArrayList<Character> generateAlphabet(State start) {
 		
 		ArrayList<Character> characters = new ArrayList<Character>();
 		if(start.visited) {
 			return characters;
 		}
 		
 		HashMap<Character, State> alphabetListAdjacent = start.getCharEdges();
 //		System.out.println("GENALPH: "+start.getCharEdges().keySet());
 		if(alphabetListAdjacent.size() > 0) {
 			for(Entry<Character, State> entry: alphabetListAdjacent.entrySet()) {
 				if(!doesContain(characters, entry.getKey())) {
 					characters.add(entry.getKey());
 				}
 				ArrayList<Character> charactersSub = generateAlphabet(entry.getValue());
 				entry.getValue().visited = true;
 				for(char ch : charactersSub) {
 					if(!doesContain(characters, ch)) {
 						characters.add(ch);
 					}
 				}
 
 			}
 			return characters;
 		}
 		else {
 			ArrayList<State> eList = start.getEpsEdges();
 //			for(State state : eList) {state.halfVisited = false;}
 			for(State state : eList) {
 				if (state.halfVisited) continue;
 				state.halfVisited = true;
 				ArrayList<Character> charactersSub = generateAlphabet(state);
 				state.visited = true;
 				for(char ch : charactersSub) {
 					if(!doesContain(characters, ch)) {
 						characters.add(ch);
 					}
 				}
 			}
 			return characters;
 		}
 	
 
 	}
 	
 	
 	
 	
 	/**
 	 * 
 	 * @param s
 	 * @return
 	 */
 	private ArrayList<State> getEpsilonAdjList(State s) {
 		ArrayList<State> eClosuerForS = s.getEpsEdges();
 		ArrayList<State> adjEEdges = new ArrayList<State>();
 		
 		//Find the states that A connects to
 		for(State a : eClosuerForS) {
 			if(!doesContain(adjEEdges, a)) {
 				adjEEdges.add(a);
 			}
 			ArrayList<State> allEEdges = moveEpislon(a);
 			for(State sa : allEEdges) {
 				if(!doesContain(adjEEdges, sa)) {
 					adjEEdges.add(sa);
 					adjEEdges = findMore(sa,adjEEdges);
 				}
 			}
 			
 			
 		}
 		if(!doesContain(adjEEdges,s)){
 			adjEEdges.add(s);
 		}
 		return adjEEdges;
 	}
 	
 	public ArrayList<State> findMore(State sa, ArrayList<State> adjEEdges){
 		ArrayList<State> saClosures = sa.getEpsEdges();
 		for(State m:saClosures){
 			if(!doesContain(adjEEdges,m)){
 				adjEEdges.add(m);
 				adjEEdges = findMore(m,adjEEdges);
 			}
 		}
 		return adjEEdges;
 	}
 	
 	/**
 	 * Finds all transition states from this state
 	 * @param state
 	 * @param c
 	 * @return
 	 */
 	private ArrayList<State> getCharAdjList(State state, char c) {
 		HashMap<Character, State> alphabetListAdjacent = 
 				state.getCharEdges();
 		
 		ArrayList<State> rList = new ArrayList<State>();
 		
 		
 		for(Entry<Character, State> e : alphabetListAdjacent.entrySet()) {
 			//See if there is a char transition
 			if(e.getKey() == c) {
 				//DFAState dState = new DFAState();
 				//dState.setEpsEdges(e.getValue().getEpsEdges());
 				rList.add(e.getValue());
 			}
 			
 		}
 		
 		return rList;
 	}
 
 	
 	
 	/**
 	 * Determins if the State K is within the State List s
 	 * @param s
 	 * @param k
 	 * @return
 	 */
 	private boolean doesContain(ArrayList<State> s, State k) {
 		for(State state : s) {
 			if(state.equals(k)) 
 				return true;
 		}
 		
 		return false;
 	}
 	
 	
 	private boolean doesContain(ArrayList<DFAState> s, DFAState k) {
 		for(State state : s) {
 			for(State subState : state.getAdjacentList()) {
 				if(state.equals(k)) 
 					return true;
 				}
 			}
 			
 			
 			
 		
 		return false;
 	}
 
 	
 	private boolean doesContain(ArrayList<Character> s, char c) {
 		
 		for(char ch : s) {
 			if(ch == c)
 				return true;
 		}
 		
 		return false;
 	}
 	
 	
 		/**
 	 * This function finds all epislon moves from this state
 	 * @param state
 	 * @return
 	 */
 	private ArrayList<State> moveEpislon(State state) {
 		ArrayList<State> rList = state.getEpsEdges();
 		if(rList.size() < 1) {
 			return rList;
 		}
 	
 		for(State s : rList) {
 			if(!doesContain(rList, s)) {
 			//if(!rList.contains(s)) {
 				rList.add(s);
 				ArrayList<State> t = moveEpislon(s);
 				if(t.size() > 0) {
 					rList.addAll(t);
 				}
 			}
 		}
 		if(!doesContain(rList, state)) {
 			rList.add(state);
 		}
 		
 		return rList;
 	}
 	
 	
 
 	public boolean davidEquals(ArrayList<DFAState> dfaStates, DFAState proposedState){
 		for(State m:dfaStates){
 			if(customEquals(proposedState,(DFAState)m)){
 				return true;
 			}
 		}
 		
 		
 		return false;
 	}
 	
 	public boolean customEquals(DFAState obj, DFAState obj2){
 		DFAState toCompareTo = (DFAState)obj;
 		if ( (toCompareTo.getAdjacentList() == null) || (obj.getAdjacentList() == null) )
 			return toCompareTo.getAdjacentList() == obj.getAdjacentList();
 		
 		for(State m: toCompareTo.getAdjacentList()){
 			if((!obj2.getAdjacentList().contains(m))||(obj2.getAdjacentList().size() != toCompareTo.getAdjacentList().size())){
 				return false;
 			}
 		}
 		return true;
 	}
 	
 	public DFAState davidGet(ArrayList<DFAState> dfaStates, DFAState proposedState){
 		for(State m:dfaStates){
 			if(customEquals(proposedState,(DFAState)m)){
 				return (DFAState)m;
 			}
 		}
 		
 			return null;
 		
 		
 	}
 
 	
 	public void createLink(ArrayList<State> dfa,char c, DFAState tmpDFAState){
 		DFAState toFind = new DFAState();
 		toFind.setAdjacentList(dfa);
 		DFAState real = davidGet(dfaStateList, toFind);
 		real.addCharEdge(c, tmpDFAState);
 	}
 	
 	public boolean checkTransitions(DFAState state){
 		for(char c : this.langList) {
 			Iterator<Entry<Character,State>> myIt = state.getCharEdges().entrySet().iterator();
 			boolean didIFindMyChar = false;
 			while(myIt.hasNext()){
 				Entry<Character,State> myEnt = myIt.next();
 				if(myEnt.getKey() == c){
 					didIFindMyChar = true;
 				}
 			}
 			if(didIFindMyChar == false){
 				return true;
 			}
 		}
 		
 		return false;
 	}
 	
 }
