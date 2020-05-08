 package org.jboss.pressgang.ccms.rest.v1.collections.base;
 
 import static org.jboss.pressgang.ccms.rest.v1.collections.base.RESTBaseCollectionItemV1.ADD_STATE;
 import static org.jboss.pressgang.ccms.rest.v1.collections.base.RESTBaseCollectionItemV1.REMOVE_STATE;
 import static org.jboss.pressgang.ccms.rest.v1.collections.base.RESTBaseCollectionItemV1.UNCHANGED_STATE;
 import static org.jboss.pressgang.ccms.rest.v1.collections.base.RESTBaseUpdateCollectionItemV1.UPDATE_STATE;
 
 import java.util.ArrayList;
 import java.util.List;
 import org.jboss.pressgang.ccms.rest.v1.entities.base.RESTBaseEntityV1;
 
 /**
  * @author Matthew Casperson
  * 
  * @param <T> The REST entity type
  * @param <U> The REST Collection type
  */
 @SuppressWarnings("serial")
 abstract public class RESTBaseCollectionV1<T extends RESTBaseEntityV1<T, U, V>, U extends RESTBaseCollectionV1<T, U, V>, V extends RESTBaseCollectionItemV1<T, U, V>> {
     private Integer size = null;
     private String expand = null;
     private Integer startExpandIndex = null;
     private Integer endExpandIndex = null;
 
     public abstract List<V> getItems();
 
     public abstract void setItems(final List<V> items);
 
     protected abstract void addItem(final T item, final Integer state);
     
     /**
      * Get a collection of REST entities wrapped as collection items that have a particular state
      * 
      * @param states Defines the list of states that an entity can be in to be returned
      * @return A collection that holds all the REST entities included in the states collection
      */
     public List<V> returnCollectionItemsWithState(final List<Integer> states) {
         if (states == null)
             throw new IllegalArgumentException("states cannot be null");
 
         final List<V> retValue = new ArrayList<V>();
 
         for (final V item : getItems()) {
             if (states.contains(item.getState()))
                 retValue.add(item);
         }
 
         return retValue;
     }
     
     /**
      * 
      * @return A collection of deleted items
      */
     public List<V> returnDeletedCollectionItems() {
         return returnCollectionItemsWithState(new ArrayList<Integer>() {
             {
                 add(REMOVE_STATE);
             }
         });
     }
     
     /**
      * 
      * @return A collection of added items
      */
     public List<V> returnAddedCollectionItems() {
         return returnCollectionItemsWithState(new ArrayList<Integer>() {
             {
                 add(ADD_STATE);
             }
         });
     }
     
     /**
      * 
      * @return A collection of existing items
      */
     public List<V> returnExistingCollectionItems() {
         return returnCollectionItemsWithState(new ArrayList<Integer>() {
             {
                 add(UNCHANGED_STATE);
             }
         });
     }
     
     /**
      * 
      * @return A collection of existing and added items
      */
     public List<V> returnExistingAndAddedCollectionItems() {
         return returnCollectionItemsWithState(new ArrayList<Integer>() {
             {
                 add(UNCHANGED_STATE);
                 add(ADD_STATE);
             }
         });
     }
     
     /**
      * 
      * @return A collection of added and deleted items
      */
     public List<V> returnDeletedAndAddedCollectionItems() {
         return returnCollectionItemsWithState(new ArrayList<Integer>() {
             {
                 add(REMOVE_STATE);
                 add(ADD_STATE);
             }
         });
     }
 
     /**
      * Get a collection of REST entities that have a particular state
      * 
      * @param states Defines the list of states that an entity can be in to be returned
      * @return A collection that holds all the REST entities included in the states collection
      */
     public List<T> returnItemsWithState(final List<Integer> states) {
         if (states == null)
             throw new IllegalArgumentException("states cannot be null");
 
         final List<T> retValue = new ArrayList<T>();
 
         for (final V item : getItems()) {
             if (states.contains(item.getState()))
                 retValue.add(item.getItem());
         }
 
         return retValue;
     }
 
     /**
      * 
      * @return A collection of deleted items
      */
     public List<T> returnDeletedItems() {
         return returnItemsWithState(new ArrayList<Integer>() {
             {
                 add(REMOVE_STATE);
             }
         });
     }
 
     /**
      * 
      * @return A collection of added items
      */
     public List<T> returnAddedItems() {
         return returnItemsWithState(new ArrayList<Integer>() {
             {
                 add(ADD_STATE);
             }
         });
     }
     
     /**
      * 
      * @return A collection of existing items
      */
     public List<T> returnExistingItems() {
         return returnItemsWithState(new ArrayList<Integer>() {
             {
                 add(UNCHANGED_STATE);
             }
         });
     }
     
     /**
      * Get any items that were added or unchanged. This is commonly used when
      * getting items that should be displayed to the end user.
      * @return A collection of added and existing items
      */
     public List<T> returnExistingAndAddedItems() {
         return returnItemsWithState(new ArrayList<Integer>() {
             {
                 add(UNCHANGED_STATE);
                 add(ADD_STATE);
             }
         });
     }
     
     /**
      * Get any items that were added or deleted. This is commonly used when
      * getting items that will change the database.
      * @return A collection of added and existing items
      */
     public List<T> returnDeletedAndAddedItems() {
         return returnItemsWithState(new ArrayList<Integer>() {
             {
                 add(REMOVE_STATE);
                 add(ADD_STATE);
             }
         });
     }
 
     public List<T> returnItems() {
         final List<T> items = new ArrayList<T>();
 
         if (this.getItems() != null) {
             for (final V item : this.getItems()) {
                 final T entity = item.getItem();
 
                 if (entity != null) {
                     items.add(entity);
                 }
             }
         }
 
         return items;
     }
 
     /**
      * It is possible that a client has sent up a collection that asks to add and remove the same child item in a collection.
      * This method, combined with the ignoreDuplicatedAddRemoveItemRequests() method, will weed out any duplicated requests.
      */
     public void removeInvalidChangeItemRequests() {
         /* ignore attempts to add/remove/update null items and items with invalid states */
         if (this.getItems() != null) {
             final List<V> items = new ArrayList<V>(this.getItems());
 
             for (final V item : items) {
                 if (item.getItem() == null) {
                     this.getItems().remove(item);
                 } else if (item.getState() != null && item.getState() == UNCHANGED_STATE) {
                     this.getItems().remove(item);
                } else if (item.getItem().getId() == null && item.getState() != ADD_STATE) {
                    this.getItems().remove(item);
                 } else if (item.getState() != null && !item.validState(item.getState())) {
                     this.getItems().remove(item);
                 }
             }
 
             ignoreDuplicatedChangeItemRequests();
         }
     }
 
     /**
      * This method will clear out any child items that are marked for both add and remove, or duplicated add and remove
      * requests. Override this method to deal with collections where the children are not uniquely identified by only their id.
      * 
      * This shouldn't occur when using the REST API through Java but may occur if a request is sent through a generic browser.
      */
     protected void ignoreDuplicatedChangeItemRequests() {
         if (this.getItems() != null) {
             final List<V> items = new ArrayList<V>(this.getItems());
 
             /* on the second loop, remove any items that are marked for both add and remove is separate items */
             for (int i = 0; i < items.size(); ++i) {
                 final V child1 = items.get(i);
                 final T childItem1 = child1.getItem();
                 
                 // New Entity so ignore it
                 if (childItem1.getId() == null)
                     continue;
 
                 /* at this point we know that either add1 or remove1 will be true, but not both */
                 final boolean add1 = child1.getState() == ADD_STATE;
                 final boolean remove1 = child1.getState() == REMOVE_STATE;
                 final boolean update1 = child1.getState() == UPDATE_STATE;
 
                 /* Loop a second time, looking for duplicates */
                 for (int j = i + 1; j < items.size(); ++j) {
                     final V child2 = items.get(j);
                     final T childItem2 = child2.getItem();
 
                     // New Entity so ignore it
                     if (childItem2.getId() == null)
                         continue;
                     
                     /* Check the PropertyTags for uniqueness and their value as well as their IDs */
                     if (childItem1.getId().equals(childItem2.getId())) {
                         final boolean add2 = child2.getState() == ADD_STATE;
                         final boolean remove2 = child2.getState() == REMOVE_STATE;
                         final boolean update2 = child2.getState() == UPDATE_STATE;
 
                         /* check for double add, double remove, double update, and remove one instance */
                         if ((add1 && add2) || (remove1 && remove2) || (update1 && update2))
                             this.getItems().remove(child1);
 
                         /* check for double add, double remove, add and remove, remove and add */
                         if ((add1 && remove2) || (remove1 && add2) || (update1 && remove2) || (update2 && remove1)
                                 || (update1 && add2) || (update2 && add1)) {
                             this.getItems().remove(child1);
                             this.getItems().remove(child2);
                         }
                     }
                 }
             }
         }
     }
 
     public void cloneInto(final RESTBaseCollectionV1<T, U, V> dest, final boolean deepCopy) {
         dest.size = this.size;
         dest.expand = this.expand;
         dest.startExpandIndex = this.startExpandIndex;
         dest.endExpandIndex = this.endExpandIndex;
 
         if (this.getItems() != null) {
             dest.setItems(new ArrayList<V>());
             if (deepCopy) {
                 for (final V item : this.getItems())
                     dest.getItems().add(item.clone(deepCopy));
             } else {
                 dest.getItems().addAll(this.getItems());
             }
         }
     }
 
     public Integer getSize() {
         return size;
     }
 
     public void setSize(final Integer size) {
         this.size = size;
     }
 
     public String getExpand() {
         return expand;
     }
 
     public void setExpand(final String expand) {
         this.expand = expand;
     }
 
     public Integer getStartExpandIndex() {
         return startExpandIndex;
     }
 
     public void setStartExpandIndex(final Integer startExpandIndex) {
         this.startExpandIndex = startExpandIndex;
     }
 
     public Integer getEndExpandIndex() {
         return endExpandIndex;
     }
 
     public void setEndExpandIndex(final Integer endExpandIndex) {
         this.endExpandIndex = endExpandIndex;
     }
 
     public void addItem(final T item) {
         addItem(item, UNCHANGED_STATE);
     }
 
     public void addNewItem(final T item) {
         addItem(item, ADD_STATE);
     }
 
     public void addRemoveItem(final T item) {
         addItem(item, REMOVE_STATE);
     }
 }
