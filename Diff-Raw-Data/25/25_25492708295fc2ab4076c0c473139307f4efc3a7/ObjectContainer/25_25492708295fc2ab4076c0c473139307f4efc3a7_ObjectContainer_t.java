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
    public  <V extends TObject> void bind(Class<V> classType, TObject object) {
        objectMap.put((Class<TObject>) classType, object);
     }
 
     @Override
    public <V extends TObject> V get(Class<V> classType) {
        return (V) objectMap.get(classType);
     }
 
     @Override
     public Iterator<TObject> listObjects() {
         return objectMap.values().iterator();
     }
 }
