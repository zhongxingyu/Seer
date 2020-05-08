 package edu.usu.cs.pddl.domain.incomplete;
 
 import java.util.BitSet;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 import edu.usu.cs.pddl.domain.LiteralInstance;
 
 public class Proposition {
 	
 	private static int propositionIndex = 0; 
 	private static Map<String, Integer> propositionIndices = new HashMap<String, Integer>();
 	private static Map<Integer, Proposition> propositionIndexMap = new HashMap<Integer, Proposition>();
 	
 	private String name;
 	private int hash;
 	private boolean hashInitialized = false;
 	
 	public Proposition(String name) {
 		this.name = name;
 		if(propositionIndices.get(name) == null){
 			int index = propositionIndex;
 			propositionIndex++;
 			propositionIndices.put(name, index);
 			propositionIndexMap.put(index, this);
 		}
 	}
 	
 	public static Integer getPropositionIndex(String name) {
 		Integer index =propositionIndices.get(name); 
 		return index;
 	}
 	
 	public static Proposition getPropositionFromIndex(Integer index){
 		return propositionIndexMap.get(index);
 	}
 	
	
 	
 	public Integer getIndex(){
 		return getPropositionIndex(name);
 	}
 	
 	public static Proposition getPropositionFromIndex(LiteralInstance literalInstance) {
 		String name = literalInstance.toString().replace('(', ' ').replace(')', ' ').trim();
 		return getPropositionFromIndex(name);
 	}
 	public static Proposition getPropositionFromIndex(String name) {
 		Integer index = propositionIndices.get(name); 
 		if(index != null){
 			return propositionIndexMap.get(index);		
 		}
 		else{
 			Proposition p = new Proposition(name);
 			propositionIndexMap.put(getPropositionIndex(name), p);
 			return p;
 		}
 	}		
 	
 
 	/**
 	 * Creates a proposition from a literal instance.
 	 * @param literalInstance
 	 */
 	public Proposition(LiteralInstance literalInstance) {
 		this.setName(literalInstance.toString().replace('(', ' ').replace(')', ' ').trim());
 		if(propositionIndices.get(this.name) == null){
 			propositionIndices.put(this.name, propositionIndex++);
 			propositionIndexMap.put(propositionIndex-1, this);
 		}
 	}
 	
 	public String getName() {
 		return name;
 	}
 	public void setName(String name) {
 		this.name = name;
 	}
 	
 	@Override
 	public boolean equals(Object o) {
 		if(!(o instanceof Proposition)) {
 			return false;
 		}
 		return getName().equals(((Proposition)o).getName());
 	}
 	
 	@Override
 	public String toString() {
 		return getName();
 	}
 	
 	@Override
 	public int hashCode() {
 		if(hashInitialized) {
 			return hash;
 		}
 		
 		hash = getName().hashCode();
 		hashInitialized = true;
 		
 		return hash;
 	}
 	
 	public static int getNodeHash(Set<Proposition> props) {
 		BitSet bitSet = new BitSet(32);
 		
 		int size = Math.min(propositionIndexMap.size(), 32);
 		
 		for(int i = 0; i < size; i++) {
 			if(props.contains(propositionIndexMap.get(i))) {
 				bitSet.set(i);
 			}
 		}
 		
 		return bitSetToInt(bitSet);
 	}
 	
 	private static int bitSetToInt(BitSet bitSet) {
 	    int temp = 0;
 
 	    for (int i = 0; i < 32; i++) {
 	    	if (bitSet.get(i)) {
 	    		temp |= 1 << i;
 	    	}
 	    }
 
 	    return temp;
 	}
 }
