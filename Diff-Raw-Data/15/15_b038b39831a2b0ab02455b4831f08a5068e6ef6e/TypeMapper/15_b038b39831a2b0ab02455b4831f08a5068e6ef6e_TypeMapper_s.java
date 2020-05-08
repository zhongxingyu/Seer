 package unif.type;
 
 /** TypeMapper.java
 	*
 	* @author <a href="mailto:joque@me.com">Jose Ghislain Quenum</a>
 	* @version 0.9 - 11/2009
 */
 
 import java.util.Map;
 import java.util.HashMap;
 
 public final class TypeMapper{
 	private final HashMap<ComplexTypeImpl, Type> mappings;
 	private static TypeMapper mapper = new TypeMapper();
 	
 	private TypeMapper(){
 		mappings = new HashMap<ComplexTypeImpl, Type>();
 	}
 	
 	public static TypeMapper getReference(){
 		return mapper;
 	}
 	
 	//Caution! this will erase any previous entry with the same key
 	public void addEntry(final ComplexTypeImpl leftHandType, final Type rightHandType){
 			mappings.put(leftHandType, rightHandType);
 	}
 	
 	public Type getValue(final ComplexTypeImpl leftHandType){
 		return mappings.get(leftHandType);
 	}
 	
 	//this is the checkMapping1 in the initial implementation
 	public boolean checkDirectMapping(final ComplexTypeImpl leftHandType, final Type rightHandType){
 		
 		for(Map.Entry<ComplexTypeImpl,Type> substEntry: mappings.entrySet()){
 			final ComplexTypeImpl entryKey = substEntry.getKey();
 			final Type entryValue = substEntry.getValue();
			//do the check and return the result
 		}
		
		//should return the actual result of the method
		return false;
 	}
 	
 }
