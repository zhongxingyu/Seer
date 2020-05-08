 package org.oha7.dispdriver;
 
 import org.oha7.dispdriver.annotations.ProgId;
 import org.oha7.dispdriver.impl.ComProxy;
 
 /**
  * access registered COM Objects from java
 * @author mike
  *
  */
 public class Factory {
 	
 	
 	/**
 	 * CoCreate a com object with registered ProgId
 	 * @param <T> IUnknown derived interface
 	 * @param clazz - class of IUnkown derived Interface that is requested
 	 * @return ComProxy implementing interface T
 	 * 
 	 * the returned ComProxy MUST be released after usage
 	 */
 	public static <T> T createObject(Class<T> clazz) {
 		
 		ProgId progId = clazz.getAnnotation(ProgId.class);
 		if ( progId == null )
 			return null;
 		
 		return ComProxy.newInstance(progId.value(), clazz);
 	}	
 		
 	/**
 	 * call GetActiveObject using the CLSID of the corresponding ProgId of Param T
 	 * @param <T> IUnknown derived Interface with registered ProgId
 	 * @param clazz - class of IUnkown derived Interface that is requested
 	 * @return ComProxy implementing interface T
 	 * 
 	 * the returned ComProxy MUST be released after usage
 	 */
 	public static <T> T getObject(Class<T> clazz) { 
 		
 		ProgId progId = clazz.getAnnotation(ProgId.class);
 		if ( progId == null )
 			return null;
 		
 		return ComProxy.runningInstance(progId.value(), clazz);
 	}
 }
