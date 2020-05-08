 package jDistsim.utils.ioc;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 /**
  * Author: Jirka Pénzeš
  * Date: 26.10.12
  * Time: 22:41
  */
 public class ObjectContainer<TObject> implements IObjectContainer<TObject> {
 
     private final Map<Class<TObject>, TObject> objectMap;
 
     public ObjectContainer() {
         objectMap = new HashMap<Class<TObject>, TObject>();
     }
 
     @Override
    public void bind(Class<TObject> classType, TObject object) {
       objectMap.put(classType, object);
     }
 
     @Override
    public TObject get(Class<TObject> classType) {
        return objectMap.get(classType);
     }
 
     @Override
     public Iterator<TObject> listObjects() {
         return objectMap.values().iterator();
     }
 }
