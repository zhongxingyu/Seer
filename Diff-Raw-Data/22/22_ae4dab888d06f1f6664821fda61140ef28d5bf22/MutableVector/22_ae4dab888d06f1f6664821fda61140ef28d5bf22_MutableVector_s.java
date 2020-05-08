 package scespet.core;
 
 import gsa.esg.mekon.core.Environment;
 import gsa.esg.mekon.core.EventGraphObject;
 import gsa.esg.mekon.core.Function;
 
 import java.util.*;
 import java.util.logging.Logger;
 
 /**
  * @version $Id$
  * @author: danvan
  */
 public class MutableVector<X> implements VectorStream<X,X> {
     private final static Logger logger = Logger.getLogger(MutableVector.class.getName());
 
     private Environment env;
     private ArrayList<X> values = new ArrayList<X>();
     private ArrayList<HasValue<X>> cells = new ArrayList<HasValue<X>>();
     private ArrayList<Boolean> initialised = new ArrayList<>();
     private Set<X> uniqueness = new HashSet<X>();
     Boolean elementsListenable = null;
 
     final Function nullListenable = new Function() {
         public boolean calculate() {
             return false;
         }
     };
 
     final ReshapeSignal reshaped;
 
     public MutableVector(Iterable<X> initial, Environment env) {
         this(env);
         Iterator<X> iterator = initial.iterator();
         while (iterator.hasNext()) {
             X next = iterator.next();
             add(next);
         }
     }
 
     public MutableVector(Environment env) {
         this.env = env;
         reshaped = new ReshapeSignal(env);
         env.setStickyInGraph(reshaped, true);
     }
 
     public boolean add(X x) {
         if (uniqueness.add(x)) {
             int i = values.size();
             values.add(x);
             Cell<X> cell = new Cell<X>(x);
             cells.add(cell);
             return true;
         }
         return false;
     }
 
     public boolean addAll(Iterable<X> xs) {
         boolean added = false;
         for (X x : xs) {
             added |= add(x);
         }
        if (added) {
            env.wakeupThisCycle(reshaped);
        }
         return added;
     }
 
     public int getSize() {
         return values.size();
     }
 
     public List<X> getKeys() {
         return values;
     }
 
     public List<X> getValues() {
         return values;
     }
 
     public void setInitialised(int i) {
         initialised.set(i, true);
     }
 
     @Override
     public int indexOf(X key) {
         logger.warning("This could be slow");
         int idx = values.indexOf(key);
         if (idx < 0) {
             logger.warning(key+" not found");
         }
         return idx;
     }
 
     public X get(int i) {
         return values.get(i);
     }
 
     public X getKey(int i) {
         return values.get(i);
     }
 
     public EventGraphObject getTrigger(int i) {
         return cells.get(i).getTrigger();
     }
 
     public VectorStream.ReshapeSignal getNewColumnTrigger() {
         return reshaped;
     }
 
     @Override
     public HasValue<X> getValueHolder(int i) {
         return cells.get(i);
     }
 
     private class Cell<X> implements HasValue<X> {
         private final X next;
 
         public Cell(X next) {
             this.next = next;
         }
 
         @Override
         public boolean initialised() {
             return true;
         }
 
         @Override
         public X value() {
             return next;
         }
 
         @Override
         public EventGraphObject getTrigger() {
             if (elementsListenable == null) {
                 elementsListenable = EventGraphObject.class.isAssignableFrom(next.getClass());
             }
             if (elementsListenable) {
                 return (EventGraphObject) next;
             } else {
                 return nullListenable;
             }
         }
     }
 }
