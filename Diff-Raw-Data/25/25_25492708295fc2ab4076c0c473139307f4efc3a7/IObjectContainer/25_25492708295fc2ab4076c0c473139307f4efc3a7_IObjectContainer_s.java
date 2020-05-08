 package jDistsim.utils.ioc;
 
 import java.util.Iterator;
 
 /**
  * Author: Jirka Pénzeš
  * Date: 26.10.12
 * Time: 22:41
  */
 public interface IObjectContainer<TObject> {
 
    public void bind(Class<TObject> classType, TObject object);
 
    public TObject get(Class<TObject> classType);
 
    public Iterator<TObject> listObjects();
 }
