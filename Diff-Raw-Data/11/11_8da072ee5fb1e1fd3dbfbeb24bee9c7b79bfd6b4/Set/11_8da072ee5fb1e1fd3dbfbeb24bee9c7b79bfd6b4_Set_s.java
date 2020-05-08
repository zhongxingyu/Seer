 package net.avh4.data.datum.transact.commands;
 
 import net.avh4.data.datum.primitives.Id;
 import net.avh4.data.datum.store.DatumStore;
 import net.avh4.data.datum.transact.Command;
 import net.avh4.data.datum.transact.TransactionException;
 
 public class Set implements Command {
     private final Id entity;
     private final String action;
     private final String value;
 
     public Set(Id entity, String action, String value) {
         this.entity = entity;
         this.action = action;
         this.value = value;
     }
 
     @Override public DatumStore execute(DatumStore store) throws TransactionException {
         final String entityId = this.entity.id();
        store = store.set(entityId, action, value);
         final String oldValue = store.get(entityId, action);
         if (oldValue != null) {
             store = store.removeIndex(action, oldValue, entityId);
         }
         store = store.addIndex(action, value, entityId);
         return store;
     }
 
     @Override public DatumStore resolveTempIds(DatumStore store) {
         store = entity.resolve(store);
         return store;
     }
 }
