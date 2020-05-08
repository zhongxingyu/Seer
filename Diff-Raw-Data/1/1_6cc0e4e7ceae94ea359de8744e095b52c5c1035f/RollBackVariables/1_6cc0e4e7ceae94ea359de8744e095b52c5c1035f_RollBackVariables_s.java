 package simulation.model;
 
 public class RollBackVariables<K> {
 
 	private K value = null;
 
 	public RollBackVariables(K value) {
 	}
 
 	public K getValue() {
 		if (value == null)
 			throw new RuntimeException("value not set");
 		return value;
 	}
 
 	/**
 	 * 
 	 * @return the stored value as a long
 	 */
 	public long getLongValue() {
 		Long l = (Long) getValue();
 		return l;
 	}
 
 	public void setValue(K value) {
 		this.value = value;
 	}
 
 }
