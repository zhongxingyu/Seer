 import java.util.ArrayList;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 
 public class StateTable {
 
 	private static ArrayList<tableRow> stateTable;
 	private tableRow currState;
 	private ArrayList<tableRow> NFAState; 
 	private boolean accepted;
 	private tableRow acceptedState;
 	private String tokenGenerated;
 	
 	private enum TableType {NFA,DFA};
 	private tableRow removedRow; //storage for addState's remove state
 	
 	public StateTable(){
 		stateTable = new ArrayList<tableRow>(0);
 		currState = null;
 		NFAState = new ArrayList<tableRow>(0); 
 		accepted = false;
 		acceptedState = null;
 		tokenGenerated = "";
 	}
 	
 	/**
 	 * @param index into stateTable
 	 * @return tableRow object at that index
 	 */
 	public tableRow getTableRow(int i){
 		return stateTable.get(i);
 	}
 	
 	/**
 	 * @param name of the table row to return
 	 * @return a matching tableRow
 	 */
 	public tableRow getTableRowbyName(String name){
 		for (tableRow row : stateTable){
 			if (row.name.compareTo(name) == 0){
 				return row;
 			}
 		}
 		return null;
 	}
 	
 	/** 
 	 * @param tableRow to lookup in stateTable
 	 * @return index of tableRow in stateTable, or null if not
 	 */
 	public int getIndexOf(tableRow t){
 		return stateTable.indexOf(t);
 	}
 	
 	
 	
 	/**
 	 * @param map - transitions
 	 * @param name - the title of the state
 	 * @param index - if table size is less than index, it will REPLACE the current table entry
 	 * @return boolean if a row is replaced true is returned an the replaced row is stored in a removedRow
 	 */
 	public boolean addState(Map<String, tableRow> map, String name,int index){
 		tableRow newRow = new tableRow(map, name); //create
 		boolean replace = false;
 		
 		if (stateTable.size() < index && index >=0){ //append at index
 			stateTable.add(index, newRow);
 		}
 		else if(stateTable.size() > index){ //REPLACE CURRENT TABLEROW AT INDEX
 			removedRow = stateTable.remove(index);//stores removed row
 			stateTable.add(index, newRow);
 			replace = true;
 		}
 		else if(index < 0){ //append to end
 			stateTable.add(newRow);
 		}
 		return replace;
 	}
 	
 	/**
 	 * @return a list of all the transition maps that are currently in the stateTable
 	 */
 	public ArrayList<Set<Entry<String, tableRow>>> getSuccessorStates(){
 		Set<Entry<String, tableRow>> rowvalues;
 		ArrayList<Set<Entry<String,tableRow>>> values = new ArrayList<Set<Entry<String,tableRow>>>(0);
 		
 		for (tableRow row : stateTable){
 			rowvalues = row.successorStates.entrySet();
 			values.add(rowvalues);
 		}
 		return values;
 	}
 	
 	/**
 	 * @return list of all strings that are legal transitions in the table
 	 */
 	public ArrayList<ArrayList<String>> getSuccessorTransitions(){
 		ArrayList<ArrayList<String>> values = new ArrayList<ArrayList<String>>();
 		
 		for (tableRow row : stateTable){
 			values.add((ArrayList<String>) row.successorStates.keySet());
 		}
 		
 		return values;
 	}
 	
 	/**performs the NFA state walking, checking for epsilon transitions and accepting states
 	 * 
 	 * @param string to lookup
 	 */
 	public void NFAlookUp(String c){
 		ArrayList<tableRow> next = new ArrayList<tableRow>(0);
 		tableRow nextState;
 		
 		for (tableRow state : NFAState){//
 			nextState = stateTable.get(stateTable.indexOf(state)).getNextState(c);
 			if (nextState != null){
 				next.add(nextState);
 			}
 		}
 		for (tableRow state : next){//follows epsilon transitions
 			nextState = stateTable.get(stateTable.indexOf(state)).getNextState("@");
 			if(nextState != null){
 				next.add(nextState);
 			}
 		}
 		
 		for(tableRow state : next){//checks for accepting state
 			if(stateTable.get(stateTable.indexOf(state)).accept()){
 				accepted = true;
 				acceptedState = state;
 			}
 		}
 		NFAState = next;
 		
 	}
 	
 	//this will tell us if the symbol has a valid translation from the currentState to another state in the table
 	public void DFAlookUp(String c){
 		tableRow nextState; //state table index
 		boolean val = false;
 		nextState = stateTable.get(stateTable.indexOf(currState)).getNextState(c);
 		if (nextState != null){
 			currState = nextState;
 		}
 		if (currState.accept()){
 			accepted = true;
 		}
 		
 	}
 	
 	/**
 	 * represents a single state in the automaton 
 	 */
 	public class tableRow{
 		//Map of strings to tableRows, transitions
 		private Map<String,tableRow> successorStates;
 		private boolean accept;
 		private String name;
 		
 		public tableRow(Map<String,tableRow> nextStates, String n){
 			successorStates = nextStates;
 			name = n;
 			accept = false;
 		}
 		
 		public tableRow getNextState(String c){
 			return successorStates.get(c);
 		}
 		
 		public boolean accept(){
 			return accept;
 		}
 		
 		public void setAccept(boolean val){
 			accept = val;
 		}
 		
 		
 	}
 }
