 package org.geworkbench.engine.management;
 
 import org.geworkbench.bison.ReferenceImplMap;
 
 /**
  * Factory implementation to obtain concrete instances of <code>bison</code>
  * datatypes
  * @author manjunath at genomecenter dot columbia dot edu
  */
 
 public class BisonFactory {
 
     public static <T> T createInstance(Class<T> klass){
         T instance = null;

         try {
             Class<T> implType = ReferenceImplMap.getDefaultImplementationMap().
                                getDefaultImplementation(klass);
System.out.println(implType + " ? " + klass);


	if(implType!=null){

             instance = implType.newInstance();
}else{
	System.out.println(implType + " is null " + klass);
}
        } catch (InstantiationException ie) {ie.printStackTrace();
        } catch (Exception iae) {iae.printStackTrace();
}
 
         return instance;
     }
 
     public static Object createInstance(String klass){
         Object instance = null;
         try {
             Class clazz = Class.forName(klass);
             Class implType = ReferenceImplMap.getDefaultImplementationMap().
                                getDefaultImplementation(clazz);
        

	if(implType!=null){

             instance = implType.newInstance();
}else{
	 
}
        } catch (InstantiationException ie) {ie.printStackTrace();
        } catch (Exception iae) {iae.printStackTrace();
}
 
         return instance;
     }
 }
