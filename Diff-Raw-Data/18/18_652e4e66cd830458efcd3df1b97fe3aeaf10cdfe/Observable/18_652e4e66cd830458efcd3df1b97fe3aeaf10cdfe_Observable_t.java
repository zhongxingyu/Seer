 package jDistsim.utils.pattern.observer;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Author: Jirka Pénzeš
  * Date: 28.9.12
  * Time: 15:14
  */
 public class Observable implements IObservable {
 
    private final List<IObserver> observers;
     private boolean changed = false;
 
     public Observable() {
         observers = new ArrayList<IObserver>();
     }
 
     public void addObserver(IObserver observer) {
         observers.add(observer);
     }
 
     public void removeObserver(IObserver observer) {
         observers.remove(observer);
     }
 
    public List<IObserver> removeAllObservers() {
        List<IObserver> result = new ArrayList<>();
        for (IObserver observer : observers)
            result.add(observer);

        observers.clear();
        return result;
    }

     public int countObservers() {
         return observers.size();
     }
 
     public void notifyObservers() {
         notifyObservers(null);
     }
 
     public void notifyObservers(Object argument) {
         if (hasChanged()) {
             for (IObserver observer : observers) {
                 observer.update(this, argument);
             }
         }
         changed = false;
     }
 
     public boolean hasChanged() {
         return changed;
     }
 
     public void setChanged() {
         changed = true;
     }

    public void addObservers(List<IObserver> observerList) {
        for (IObserver observer : observerList)
            addObserver(observer);
    }
 }
