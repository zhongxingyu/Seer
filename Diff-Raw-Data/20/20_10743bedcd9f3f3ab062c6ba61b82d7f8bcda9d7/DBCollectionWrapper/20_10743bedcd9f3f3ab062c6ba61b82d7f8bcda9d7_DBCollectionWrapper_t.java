 package org.jboss.pressgang.ccms.wrapper.collection;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.jboss.pressgang.ccms.wrapper.DBWrapperFactory;
 import org.jboss.pressgang.ccms.wrapper.base.EntityWrapper;
 import org.jboss.pressgang.ccms.wrapper.collection.base.CollectionEventListener;
 
 public abstract class DBCollectionWrapper<T extends EntityWrapper<T>, U> implements CollectionWrapper<T> {
     private static final Integer NO_STATE = 0;
     private static final Integer ADD_STATE = 1;
     private static final Integer REMOVE_STATE = 2;
 
     private final Map<T, Integer> wrapperItems = new HashMap<T, Integer>();
     private final Collection<U> items;
     private final DBWrapperFactory wrapperFactory;
     private final boolean isRevisionList;
     private Set<CollectionEventListener<U>> listeners = new HashSet<CollectionEventListener<U>>();
 
     protected DBCollectionWrapper(final DBWrapperFactory wrapperFactory, final Collection<U> items, boolean isRevisionList,
             final Class<T> wrapperClass) {
         this.wrapperFactory = wrapperFactory;
         for (final U item : items) {
             wrapperItems.put((T) wrapperFactory.create(item, isRevisionList, wrapperClass), NO_STATE);
         }
         if (items instanceof Set) {
             this.items = new HashSet<U>();
         } else {
             this.items = new ArrayList<U>();
         }
         this.items.addAll(items);
         this.isRevisionList = isRevisionList;
     }
 
     protected DBWrapperFactory getWrapperFactory() {
         return wrapperFactory;
     }
 
     protected Map<T, Integer> getCollection() {
         return wrapperItems;
     }
 
     protected Collection<U> getCollectionItems() {
         return items;
     }
 
     public void registerEventListener(CollectionEventListener<U> listener) {
         listeners.add(listener);
     }
 
     public void resetEventListeners() {
         listeners.clear();
     }
 
     protected Set<CollectionEventListener<U>> getEventListeners() {
         return listeners;
     }
 
     @Override
     public void addItem(T entity) {
         getCollection().put(entity, NO_STATE);
     }
 
     @Override
     @SuppressWarnings("unchecked")
     public void addNewItem(T entity) {
         getCollection().put(entity, ADD_STATE);
         if (!isRevisionList()) {
             final U unwrappedEntity = (U) entity.unwrap();
            if (!getCollectionItems().contains(unwrappedEntity)) {
                getCollectionItems().add(unwrappedEntity);
                notifyOnAddEvent(unwrappedEntity);
            }
         }
     }
 
     @Override
     @SuppressWarnings("unchecked")
     public void addRemoveItem(T entity) {
         getCollection().put(entity, REMOVE_STATE);
         if (!isRevisionList()) {
             final U unwrappedEntity = (U) entity.unwrap();
             getCollectionItems().remove(unwrappedEntity);
             notifyOnRemoveEvent(unwrappedEntity);
         }
     }
 
     @Override
     public void remove(T entity) {
         getCollection().remove(entity);
     }
 
     public List<T> getItems() {
         return new ArrayList<T>(getCollection().keySet());
     }
 
     @Override
     public List<T> getUnchangedItems() {
         final List<T> unchangedItems = new ArrayList<T>();
         for (final Map.Entry<T, Integer> entity : getCollection().entrySet()) {
             if (entity.getValue() == NO_STATE) {
                 unchangedItems.add(entity.getKey());
             }
         }
 
         return unchangedItems;
     }
 
     @Override
     public List<T> getAddItems() {
         final List<T> newItems = new ArrayList<T>();
         for (final Map.Entry<T, Integer> entity : getCollection().entrySet()) {
             if (entity.getValue() == ADD_STATE) {
                 newItems.add(entity.getKey());
             }
         }
 
         return newItems;
     }
 
     @Override
     public List<T> getRemoveItems() {
         final List<T> removeItems = new ArrayList<T>();
         for (final Map.Entry<T, Integer> entity : getCollection().entrySet()) {
             if (entity.getValue() == REMOVE_STATE) {
                 removeItems.add(entity.getKey());
             }
         }
 
         return removeItems;
     }
 
     @Override
     public Collection<U> unwrap() {
         return items;
     }
 
     @Override
     public int size() {
         return wrapperItems.size();
     }
 
     @Override
     public boolean isEmpty() {
         return wrapperItems.isEmpty();
     }
 
     protected boolean isRevisionList() {
         return isRevisionList;
     }
 
     private void notifyOnAddEvent(U entity) {
         for (final CollectionEventListener<U> listener : getEventListeners()) {
             listener.onAddItem(entity);
         }
     }
 
     private void notifyOnRemoveEvent(U entity) {
         for (final CollectionEventListener<U> listener : getEventListeners()) {
             listener.onRemoveItem(entity);
         }
     }
 }
