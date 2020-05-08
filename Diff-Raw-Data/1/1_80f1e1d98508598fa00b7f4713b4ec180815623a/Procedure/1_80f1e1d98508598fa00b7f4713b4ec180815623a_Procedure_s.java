 import java.util.LinkedList;
 import java.util.Map;
 import java.util.TreeMap;
 
 
 class Procedure implements Comparable<Procedure>{
 	String name; 
 	String routAtrr;
//	String routTable;		// TODO Disabled, not set yet. null all the time right now
 	TreeMap<String, Integer> attr = new TreeMap<String, Integer>();
 	LinkedList<String> attrCdt = new LinkedList<String>();		// attribute candidate 
 	LinkedList<Integer> attrCdtVal = new LinkedList<Integer>();
 	Design root;
 	int frequency;
 	
 	public void setFreq(int f){
 		frequency = f;
 	}
 
 	public Procedure(String sname, Design d){
 		name = sname;
 		root = d;
 	}
 /*	Disabled
 	public Procedure(String sname, String ra, String rt){
 		name = sname;
 		routAtrr = ra;
 		routTable = rt;
 	}
 */
 	public void clearRouteAtrr(){
 		routAtrr = "unknown";
 	}
 	
 	public boolean addAttr(String col, String table){
 		String s = col + " @ " + table;
 		s = s.toLowerCase();
 		if(attr.containsKey(s)){
 			int val = attr.remove(s);
 			attr.put(s, val + 1);
 			return false;
 		}
 		else{
 			attr.put(s, 1);
 			return true;
 		}
 	}
 
 	// a valid routing attribute is a partition attribute of a table
 	boolean isValidRoutAttr(String s){
 		for(Table t : root.getPartitionList()){
 			if(s.equalsIgnoreCase(t.getPartAttr() + " @ " + t.name))
 				return true;
 		}
 		return false;
 	}
 
 	public String getRoutAtrr(){
 		int i;
 		int val;
 		String key;
 		Map.Entry<String, Integer> me;
 		
 		if(routAtrr != null)
 			return routAtrr;
 		
 		while((me = attr.pollFirstEntry())!= null){
 			key = me.getKey();
 			val = me.getValue();
 			for(i = 0; i < attrCdt.size(); i++){
 				if(val >= attrCdtVal.get(i))
 					break;
 			}
 			attrCdt.add(i, key);
 			attrCdtVal.add(i, val);
 		}
 			
 		for(String s : attrCdt){
 			if(isValidRoutAttr(s)){
 				routAtrr = s;
 				break;
 			}
 		}
 
 		if(routAtrr == null){
 			if(attrCdt.size() != 0)
 				routAtrr = attrCdt.getFirst() + " [Not a valid routing parameter]";
 			else
 				routAtrr = "No attribute Candidates!";
 		}
 		attr = null;	// free attr TreeMap
 		return routAtrr;
 	}
 	
 	public int compareTo(Procedure p){	
 		// compare base on frequency "descending order"
 		int ret = p.frequency - frequency;
 		return (ret > 0)? 1 :
 			(ret < 0)? -1 : 0; 
 	}
 	
 	public String toString(){
 		return name;
 	}
 }
