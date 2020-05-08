 import java.util.ArrayList;
 import java.util.Map;
 
 
public class TableRow{
 	//Map of strings to tableRows, transitions
 	private Map<String,ArrayList<TableRow>> successorStates;
 	private boolean accept;
 	private String name;
 	private String type;
 	
 	public TableRow(Map<String,ArrayList<TableRow>> nextStates, String n, String t){
 		setSuccessorStates(nextStates);
 		setName(n);
 		type = t;
 		accept = false;
 	}
 	
 	public ArrayList<TableRow> getNextState(String c){
 		return getSuccessorStates().get(c);
 	}
 	
 	public boolean accept(){
 		return accept;
 	}
 	
 	public String toString(){
 		return "State: "+getName()+" :: Accept: "+accept;
 	}
 	
 	/**
 	 * 
 	 * @param val
 	 */
 	public void setAccept(boolean val){
 		accept = val;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 	
 	public String getType() {
 		return type;
 	}
 	
 	public void setType(String type) {
 		this.type = type;
 	}
 
 	public Map<String,ArrayList<TableRow>> getSuccessorStates() {
 		return successorStates;
 	}
 
 	public void setSuccessorStates(Map<String, ArrayList<TableRow>> nextStates) {
 		this.successorStates = nextStates;
 	}
 }
