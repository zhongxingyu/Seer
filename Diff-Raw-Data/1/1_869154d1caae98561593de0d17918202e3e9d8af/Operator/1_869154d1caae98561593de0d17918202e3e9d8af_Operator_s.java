 package org.pasut.persister.operators;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.pasut.persister.GsonMongoMapper;
 
 import com.mongodb.BasicDBObjectBuilder;
 
 public abstract class Operator {
 	public abstract void perform(BasicDBObjectBuilder builder);
 	
 	private static final GsonMongoMapper mapper = new GsonMongoMapper();
 	private final static Set<Class<?>> WRAPPER_TYPES = getWrapperTypes();
 	protected static boolean isWrapperType(Class<?> clazz){
         return WRAPPER_TYPES.contains(clazz);
     }
 	
 	protected Object wrap(Object value){
 		if(isWrapperType(value.getClass())) return value;
 		return mapper.toDbObject(value);
 	}
 	
 	private static HashSet<Class<?>> getWrapperTypes(){
         HashSet<Class<?>> ret = new HashSet<Class<?>>();
         ret.add(Boolean.class);
         ret.add(Character.class);
         ret.add(Byte.class);
         ret.add(Short.class);
         ret.add(Integer.class);
         ret.add(Long.class);
         ret.add(Float.class);
         ret.add(Double.class);
         ret.add(Void.class);
         ret.add(String.class);
         return ret;
     }  
 }
