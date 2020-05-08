 package jDistsim.utils.ioc;
 
 import java.util.Iterator;
 
 /**
  * Author: Jirka Pénzeš
  * Date: 26.10.12
 * Time: 23:45
  */
 public interface IObjectContainer<TObject> {
 
    <V extends TObject> void bind(Class<V> classType, TObject object);
 
    <V extends TObject> V get(Class<V> classType);
 
    Iterator<TObject> listObjects();
 }
