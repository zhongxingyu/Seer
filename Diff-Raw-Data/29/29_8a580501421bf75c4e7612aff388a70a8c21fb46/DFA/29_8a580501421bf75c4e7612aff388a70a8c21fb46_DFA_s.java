 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Queue;
 import java.util.Set;
 import java.util.Stack;
 
 public class DFA {
 	private DFAState start;
 	private DFAState curr;
 	private List<NFAState> nfaStates;
 	private List<Set<Character>> transitionsSeen;
 	private NFAState focus;
 	private List<NFAState> visited;
 	
 	public DFA() {}
 	
 	public DFA(Set<Character> chars) {}
 	
 	/**
 	 * Constructor that builds a DFA out of an NFA.
 	 * @param nfa
 	 */
 	public DFA(NFA nfa) {
 		nfaToDfa(nfa);
 	}
 	
 	public DFAState getStartState() {
 		return start;
 	}
 	
 	public void setStartState(DFAState newStart) {
 		start = newStart;
 	}
 	
 	/**
 	 * Takes in an NFA and converts it to a DFA
 	 * @param nfa
 	 */
 	public void nfaToDfa(NFA nfa) {
 		focus = nfa.getStartState();
 		nfaStates = nfa.getAllStates();
 		transitionsSeen = new ArrayList<Set<Character>>();
 		visited = new ArrayList<NFAState>();
		curr = DFAState.builder().build();  // necessary?
 		
 		start = DFAState.builder()
 				.setFirstId(nfaStates.indexOf(focus))
 				.build();
 		this.setStartState(start);
 		curr = start;
 		
 		/* Execute until you've visited every NFAState */
		while(visited.size() != nfaStates.size()) {		
 			explore(focus);
 		}
 	}
 	
 	/**
 	 * A recursive helper method for nfaToDfa
 	 * 
 	 * @param focus
 	 * @return
 	 */
 	public void explore(NFAState focus) {
 		if(focus.getNextStates().isEmpty()) {
			System.out.println("whaaat");
			System.out.println(this.getAllStates().size());
 			return;
 		}
 		
 		List<NFAState> nextStates = focus.getNextStates();
 
 		for(NFAState nfaState: nextStates) {
 			/* 
 			 * Check each adjacent state and see if its transition is epsilon.
 			 * If it is, add the id of the NFAState to the DFAState. If the
 			 * If the NFA state is accept, transfer that to the new DFA>
 			 */
 			if(nfaState.getTransition() == null) {
 				curr.addToIdList(nfaStates.indexOf(nfaState)); // test this hard
 				
 				if(nfaState.isAccept()) {
 					curr.setAccept(true);
 				}
 				if(nfaState.getIsStart()) {
 					curr.setIsStart(true);
 				}
 			}
 			else {
 				/*
 				 * If you haven't seen that transition yet, add it to the DFA
 				 */
 				if(!transitionsSeen.contains(nfaState.getTransition())) {
 					System.out.println(nfaState.getTransition());
 					curr.addNext(DFAState.builder()
 							.setAccept(false)
 							.setTransition(nfaState.getTransition())
 							.build());
 					System.out.println("wef: " + this.getAllStates().size());
 					transitionsSeen.add(nfaState.getTransition());
 					curr = curr.next(nfaState.getTransition());
 					curr.addToIdList(nfaStates.indexOf(nfaState));
 					System.out.println(curr.getIdList().size());
 					
 					if(nfaState.isAccept()) {
 						curr.setAccept(true);
 					}
 					if(nfaState.getIsStart()) {
 						curr.setIsStart(true);
 					}
 				}
 				else {
 					/*
 					 * If you've seen a transition before, but it wasn't the last
 					 * transition modified in your DFA
 					 */
 					for(DFAState state : this.getAllStates()) {
 						if(state.getTransition() == nfaState.getTransition()) {
 							curr = state;
 							curr.addToIdList(nfaStates.indexOf(focus));
 							
 							if(focus.isAccept()) {
 								curr.setAccept(true);
 							}
 							if(focus.getIsStart()) {
 								curr.setIsStart(true);
 							}
 						}
 					}
 				}
 			}
 		}
 		
 		for(NFAState nextState : focus.getNextStates()) {
 			focus = nextState;
			visited.add(nextState);
 			explore(focus);
 		}
 	}
 	
 	/**
 	 * @return A list of all of the DFAStates in the DFA
 	 */
 	public List<DFAState> getAllStates() {
 		List<DFAState> allStates = new ArrayList<DFAState>();
 		
 		Set<DFAState> discovered = new HashSet<DFAState>();
 		List<DFAState> nextStatesToExplore = new ArrayList<DFAState>();
 		DFAState temp = this.getStartState();
 		nextStatesToExplore.add(temp);
 		
 		for(DFAState state : temp.getNextStates()) {
 			nextStatesToExplore.add(state);
 		}
 		
 		while(!nextStatesToExplore.isEmpty()) {
 			temp = nextStatesToExplore.remove(nextStatesToExplore.size()-1);
 			allStates.add(temp);
 			
 			for(DFAState nextState : temp.getNextStates()) {
 				if((!nextStatesToExplore.contains(nextState))
 						&& (!discovered.contains(nextState))) {
 					nextStatesToExplore.add(nextState);
 				}
 			}
 			
 			discovered.add(temp);
 		}
 		
 		return allStates;
 	}
 
 }
