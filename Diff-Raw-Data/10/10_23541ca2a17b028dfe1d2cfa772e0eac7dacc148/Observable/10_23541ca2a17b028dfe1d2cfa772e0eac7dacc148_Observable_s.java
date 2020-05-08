 package de.htwg.se.battleship.util.observer;
 
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * Observsable Class
  * 
  * @author aullik
  */
 public class Observable implements IObservable {
 
     /**
      * List of Observers
      */
     private final List<IObserver> subscribers = new ArrayList<IObserver>(1);
 
     @Override
     public void addObserver(IObserver s) {
         subscribers.add(s);
     }
 
     @Override
     public void removeObserver(IObserver s) {
         subscribers.remove(s);
     }
 
     @Override
     public void removeAllObservers() {
         subscribers.clear();
     }
 
     @Override
     public void notifyObservers() {
         notifyObservers(null);
     }
 
     @Override
     public void notifyObservers(Event e) {
         for (Iterator<IObserver> iter = subscribers.iterator(); iter.hasNext();) {
             IObserver observer = iter.next();
 
             if (e == null) {
                 observer.update(e);
                 continue;
             }
 
             try {
                 /* This method is not the best solution, but avoids many instanceofs within the update method */
                 Method update = observer.getClass().getMethod("update", e.getClass());
                 update.invoke(observer, e);
             } catch (Exception e1) {
                 observer.update(e);
             }
         }
     }
 }
