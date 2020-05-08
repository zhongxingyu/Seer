 package nl.astraeus.persistence;
 
 import org.prevayler.Transaction;
 
 import java.io.Serializable;
 import java.util.*;
 
 /**
  * StoreModelTransaction
  * <p/>
  * User: rnentjes
  * Date: 7/20/11
  * Time: 12:58 PM
  */
 public final class SimpleTransaction implements Serializable, Transaction {
 
 	private static final long serialVersionUID = 1L;
 
     static class Action implements Serializable {
         private static final long serialVersionUID = 1L;
 
         public boolean remove = false;
         public SimpleModel model;
 
         private Action(boolean remove, SimpleModel model) {
             this.remove = remove;
             this.model = model;
         }

        public boolean isClass(Class<? extends SimpleModel> cls) {
            return cls.equals(model.getClass());
        }
     }
 
     private List<Action> actions = new LinkedList<Action>();
 
     SimpleTransaction() {}
 
     void store(SimpleModel... models) {
         for (SimpleModel model : models) {
             SimpleStore.get().setLastUpdateField(model);
             SimpleStore.get().setSavedField(model, true);
 
             Action action = new Action(false, model);
 
             actions.add(action);
         }
     }
     
     void remove(SimpleModel... models) {
         for (SimpleModel model : models) {
             SimpleStore.get().setSavedField(model, false);
 
             Action action = new Action(true, model);
 
             actions.add(action);
         }
     }
 
     Collection<Action> getActions() {
         return actions;
     }
 
     /*
     Collection<SimpleModel> getStored() {
         return store;
     }
 
     Collection<SimpleModel> getRemoved() {
         return remove;
     }*/
 
 	public void executeOn(Object prevalentSystem, Date ignored) {
         PrevalentSystem ps = (PrevalentSystem)prevalentSystem;
         
         for (Action action : actions) {
             if (action.remove) {
                 ps.remove(action.model);
             } else {
                 ps.store(action.model);
             }
         }
 	}
 
     public boolean hasChanges() {
         return !actions.isEmpty();
     }
 }
