 package template;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public class timeClass {
	private Map<Integer,Integer> timeM = new HashMap<Integer, Integer>();
 	
 	public timeClass(){
 	}
 
 	public void addKeyValue(int key, Integer value){
 		timeM.put(key, value);
 	}
 	
 	public Integer getValue(int key){
 		return timeM.get(key);
 	}
 	
 }
