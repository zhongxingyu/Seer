 package org.jboss.pressgang.ccms.contentspec.wrapper.collection;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javassist.util.proxy.ProxyObject;
 import org.jboss.pressgang.ccms.contentspec.provider.RESTProviderFactory;
 import org.jboss.pressgang.ccms.contentspec.proxy.collection.RESTCollectionV1ProxyHandler;
 import org.jboss.pressgang.ccms.contentspec.wrapper.RESTWrapperFactory;
 import org.jboss.pressgang.ccms.contentspec.wrapper.base.EntityWrapper;
 import org.jboss.pressgang.ccms.rest.v1.collections.base.RESTBaseCollectionItemV1;
 import org.jboss.pressgang.ccms.rest.v1.collections.base.RESTBaseCollectionV1;
 import org.jboss.pressgang.ccms.rest.v1.entities.base.RESTBaseEntityV1;
 import org.jboss.pressgang.ccms.utils.common.CollectionUtilities;
 
 public abstract class RESTCollectionWrapper<T extends EntityWrapper<T>, U extends RESTBaseEntityV1<U, V, ?>,
         V extends RESTBaseCollectionV1<U, V, ?>> implements CollectionWrapper<T> {
     private final RESTProviderFactory providerFactory;
     private final RESTWrapperFactory wrapperFactory;
     private final Map<T, Integer> entities = new HashMap<T, Integer>();
     private final V collection;
 
     public RESTCollectionWrapper(final RESTProviderFactory providerFactory, final V collection, boolean isRevisionCollection) {
         this.providerFactory = providerFactory;
         wrapperFactory = providerFactory.getWrapperFactory();
         this.collection = collection;
        if (collection.getItems() != null) {
            for (final RESTBaseCollectionItemV1<U, V, ?> item : collection.getItems()) {
                entities.put((T) getWrapperFactory().create(item.getItem(), isRevisionCollection), item.getState());
            }
         }
     }
 
     public RESTCollectionWrapper(final RESTProviderFactory providerFactory, final V collection, boolean isRevisionCollection,
             final Class<T> wrapperClass) {
         this.providerFactory = providerFactory;
         wrapperFactory = providerFactory.getWrapperFactory();
         this.collection = collection;
 
         if (collection.getItems() != null) {
             for (final RESTBaseCollectionItemV1<U, V, ?> item : collection.getItems()) {
                 entities.put(getWrapperFactory().create(item.getItem(), isRevisionCollection, wrapperClass), item.getState());
             }
         }
     }
 
     public RESTCollectionWrapper(final RESTProviderFactory providerFactory, final V collection, boolean isRevisionCollection,
             final RESTBaseEntityV1<?, ?, ?> parent) {
         this.providerFactory = providerFactory;
         wrapperFactory = providerFactory.getWrapperFactory();
         this.collection = collection;
         for (final RESTBaseCollectionItemV1<U, V, ?> item : collection.getItems()) {
             entities.put((T) getWrapperFactory().create(item.getItem(), isRevisionCollection, parent), item.getState());
         }
     }
 
     public RESTCollectionWrapper(final RESTProviderFactory providerFactory, final V collection, boolean isRevisionCollection,
             final RESTBaseEntityV1<?, ?, ?> parent, final Class<T> wrapperClass) {
         this.providerFactory = providerFactory;
         wrapperFactory = providerFactory.getWrapperFactory();
         this.collection = collection;
         for (final RESTBaseCollectionItemV1<U, V, ?> item : collection.getItems()) {
             entities.put(getWrapperFactory().create(item.getItem(), isRevisionCollection, parent, wrapperClass), item.getState());
         }
     }
 
     protected RESTWrapperFactory getWrapperFactory() {
         return wrapperFactory;
     }
 
     protected RESTProviderFactory getProviderFactory() {
         return providerFactory;
     }
 
     protected V getCollection() {
         return collection;
     }
 
     protected Map<T, Integer> getEntities() {
         return entities;
     }
 
     public List<T> getItems() {
         return new ArrayList<T>(entities.keySet());
     }
 
     @Override
     public List<T> getUnchangedItems() {
         final List<T> unchangedItems = new ArrayList<T>();
         for (final Map.Entry<T, Integer> entity : entities.entrySet()) {
             if (entity.getValue() == RESTBaseCollectionItemV1.UNCHANGED_STATE) {
                 unchangedItems.add(entity.getKey());
             }
         }
 
         return unchangedItems;
     }
 
     @Override
     public List<T> getAddItems() {
         final List<T> newItems = new ArrayList<T>();
         for (final Map.Entry<T, Integer> entity : entities.entrySet()) {
             if (entity.getValue() == RESTBaseCollectionItemV1.ADD_STATE) {
                 newItems.add(entity.getKey());
             }
         }
 
         return newItems;
     }
 
     @Override
     public List<T> getRemoveItems() {
         final List<T> removeItems = new ArrayList<T>();
         for (final Map.Entry<T, Integer> entity : entities.entrySet()) {
             if (entity.getValue() == RESTBaseCollectionItemV1.REMOVE_STATE) {
                 removeItems.add(entity.getKey());
             }
         }
 
         return removeItems;
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public void addItem(T entity) {
         getCollection().addItem(getEntity(entity));
         getEntities().put(entity, RESTBaseCollectionItemV1.UNCHANGED_STATE);
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public void addNewItem(T entity) {
         getCollection().addNewItem(getEntity(entity));
         getEntities().put(entity, RESTBaseCollectionItemV1.ADD_STATE);
     }
 
     @SuppressWarnings("unchecked")
     @Override
     public void addRemoveItem(T entity) {
         getCollection().addRemoveItem(getEntity(entity));
         getEntities().put(entity, RESTBaseCollectionItemV1.REMOVE_STATE);
     }
 
     @Override
     public V unwrap() {
         return collection;
     }
 
     @SuppressWarnings("unchecked")
     protected U getEntity(T entity) {
         return (U) entity.unwrap();
     }
 
     @Override
     public int size() {
         return getCollection().getItems() == null ? 0 : getCollection().getItems().size();
     }
 
     @Override
     public boolean isEmpty() {
         return getCollection().getItems() == null ? true : getCollection().getItems().isEmpty();
     }
 
     @Override
     public void remove(T entity) {
         if (entity == null) return;
         final Object unwrappedEntity = entity.unwrap();
         // Get the original non proxied collection
         final RESTBaseCollectionV1<?, ?, ?> baseCollection = getCollection();
         final RESTBaseCollectionV1<?, ?, ?> collection = (baseCollection instanceof ProxyObject) ? ((RESTCollectionV1ProxyHandler<?, ?,
                 ?>) ((ProxyObject) baseCollection)).getCollection() : baseCollection;
         // Remove the item if it's found
         final List<? extends RESTBaseCollectionItemV1<?, ?, ?>> originalItems = CollectionUtilities.toArrayList(collection.getItems());
         for (final RESTBaseCollectionItemV1<?, ?, ?> item : originalItems) {
             if (unwrappedEntity.equals(item.getItem())) {
                 collection.getItems().remove(item);
             }
         }
 
         getItems().remove(entity);
     }
 }
