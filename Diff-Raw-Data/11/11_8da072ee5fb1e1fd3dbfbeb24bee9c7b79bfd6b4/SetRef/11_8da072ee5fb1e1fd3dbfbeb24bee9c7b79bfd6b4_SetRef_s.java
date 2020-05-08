 package net.avh4.data.datum.transact.commands;
 
 import net.avh4.data.datum.primitives.Id;
 import net.avh4.data.datum.store.DatumStore;
 import net.avh4.data.datum.transact.Command;
 import net.avh4.data.datum.transact.TransactionException;
 
 public class SetRef implements Command {
     private final Id entity;
     private final String action;
     private final Id ref;
 
     public SetRef(Id entity, String action, Id ref) {
         this.entity = entity;
         this.action = action;
         this.ref = ref;
     }
 
     @Override public DatumStore execute(DatumStore store) throws TransactionException {
         final String entityId = entity.id();
         final String value = ref.id();
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
         store = ref.resolve(store);
         return store;
     }
 }
