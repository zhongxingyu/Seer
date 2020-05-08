 package v.server;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.util.List;
 
 /**
  * Clase que se encarga de establecer los Collections de los
  * entities  a null antes de retornar a la GUI.
  **/
 public class Converter<M> {
 
 	@SuppressWarnings({"rawtypes"})
 	public M convertObject(M object) {
 		Class<?> clazz = object.getClass();
 		String mn;
 		String fn;
 		Field[] fields = clazz.getDeclaredFields();
 		for(Field f: fields){
 			fn = f.getName();
 			if(f.getType().getName().equals("java.util.List")){
 				mn = "set" + fn.substring(0, 1).toUpperCase() + fn.substring(1);
 				Class[] params = {f.getType()};
 				try {
 					Method  setter = clazz.getMethod(mn, params);
 					setter.invoke(object, (Object)null);
 				} catch (Exception e) {
 					//ignore
 				}
 			}
 		}
 		return object;
 	}
 	
 	public List<M> convertObjects(List<M> objects) {
 		for(M o: objects){
 			convertObject(o);
 		}
 		return objects;
 	}
 }
