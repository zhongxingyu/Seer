 package eu.play_project.play_platformservices_querydispatcher.bdpl.visitor.realtime;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Stack;
 
 import com.hp.hpl.jena.query.Query;
 
 import eu.play_project.play_platformservices_querydispatcher.types.VariableTypeManager;
 /**
  *Manage unique values for:
  * 		 complex events (CEID)
  * 		 triplestore variable (ViD)
  * 		 Aggregate function db IDs.
  * @author sobermei
  *
  */
 public class UniqueNameManager {
 	private static VariableTypeManager vtm;
 	private long ceid; //Complex event id variable.
 	private long triplestoreVariable; // Represents the current triplestore variable.
 	private long triplestoreVariableStart; // Store first triplestore variable of the query.
 	private long triplestoreVariableEnd; // Store the last triplestore variabe of the query.
 	private long absVariable;
 	private long filterVar;
 	private long aggrDbId;
 	private long resultVar1;
 	private String resultVar1s;
 	private long resultVar2;
 	private String resultVar2s;
 	private Map<String, Boolean> aggrVars;
 	private String windowTime;
 	private Stack<Long> filterVars;
 	
 
 	static UniqueNameManager uniqueNameManger;
 	
 	private UniqueNameManager(){
 		ceid = 0;
 		triplestoreVariable = 0;
 		absVariable = 0;
 		aggrDbId = 0;
 		filterVars = new Stack<Long>();
 		aggrVars = new HashMap<String, Boolean>();
 		vtm = new VariableTypeManager(null);
 		
 	}
 	
 	public static UniqueNameManager getVarNameManager(){
 		if(uniqueNameManger == null){
 			uniqueNameManger = new UniqueNameManager();
 		}
 		return uniqueNameManger;
 	}
 	
 	public static void initVariableTypeManage(Query q){
 		vtm = new VariableTypeManager(q);
 	}
 	
 	public static VariableTypeManager getVariableTypeManage(){
 		if(vtm==null){
 			throw new RuntimeException("Init VariableTypeManager first");
 		}else{
 			return vtm;
 		}
 	}
 	
 	/**
 	 * Sometimes it is necessary to know all triplestore variables of one query.
 	 * With this method the current state will be persisted.
 	 * Now it is possible to redrive all triplestore variables from this point with getAllTripleStoreVariablesOfThisQuery() .
 	 */
 
 	public void newQuery(){
 		triplestoreVariableStart = triplestoreVariableEnd;
 		triplestoreVariableStart++;
 		triplestoreVariable = triplestoreVariableEnd;
 		triplestoreVariable++;
 	}
 	
 	/**
 	 * Generate a list of all triplestore variables of the current query.
 	 * All variables after calling newQuery() are in this list.
 	 * @return Triplestor variables of current Query.
 	 */
 	public List<String> getAllTripleStoreVariablesOfThisQuery(){
 		LinkedList<String> vars = new LinkedList<String>();
 		for (long i = triplestoreVariableStart; i < triplestoreVariableEnd; i++) {
			vars.add("ViD" + i);
 		}
 
 		return vars;
 	}
 	
 
 	public long processNextEvent(){
 
 		if (++triplestoreVariable >  triplestoreVariableEnd) {
 			triplestoreVariableEnd = triplestoreVariable;
 		}
 		return triplestoreVariable;
 	}
 	
 	public void resetTriplestoreVariable(){
 		triplestoreVariable = triplestoreVariableStart;
 	}
 	
 	public long getCurrentSimpleEventNumber() {
 		return triplestoreVariable;
 	}
 	
 	public String getTriplestoreVariable(){
 		return getTriplestoreVariableForEventNr(triplestoreVariable);
 	}
 	
 	private String getTriplestoreVariableForEventNr(long eventNumber){
 		return "ViD" + eventNumber;
 	}
 	
 	
 	
 	public String getNextAbsVariable(){
 		absVariable++;
 		return "AbsVar" + absVariable;
 	}
 	
 	public String getAbsVariable(){
 		return "AbsVar" + absVariable;
 	}
 	
 	public String  getFilterVar() {
 		return "FilterVar" + filterVar + "";
 	}
 	
 	public String  getNextFilterVar() {
 		return "FilterVar" + ++filterVar;
 	}
 	
 	public String getAggrDbId() {
 		return "dbId"+ aggrDbId;
 	}
 
 	public String getNextAggrDbId() {
 		return "dbId"+ aggrDbId++;
 	}
 
 	public String getResultVar1() {
 		if(resultVar1s ==null){
 			return "Result1" + resultVar1;
 		}else{
 			return resultVar1s;
 		}
 	}
 
 	public String getNextResultVar1() {
 		resultVar1s = null;
 		return "Result1" + resultVar1++;
 	}
 
 	public String getResultVar2() {
 		if(resultVar2s ==null){
 			return "Result2" + resultVar2;
 		}else{
 			return resultVar2s;
 		}
 		
 	}
 
 	public String getNextResultVar2() {
 		resultVar2s = null;
 		return "Result2" + resultVar2++;
 	}
 
 	public void setResultVar1(String resultVar1) {
 		this.resultVar1s = resultVar1;
 	}
 
 	public void setResultVar2(String resultVar2) {
 		this.resultVar2s = resultVar2;
 	}
 
 	public String getWindowTime() {
 		return windowTime;
 	}
 
 	public void setWindowTime(String windowTime) {
 		this.windowTime = windowTime;
 	}
 	
 	public String getNextCeid(){
 		ceid++;
 		return "CEID" + ceid;
 	}
 	
 	public String getCeid(){
 		return "CEID" + ceid;
 	}
 	
 }
