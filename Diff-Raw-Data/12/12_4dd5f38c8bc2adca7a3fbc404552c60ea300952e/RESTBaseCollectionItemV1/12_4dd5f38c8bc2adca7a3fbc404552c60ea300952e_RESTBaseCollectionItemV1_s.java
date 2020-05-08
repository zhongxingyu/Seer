 package org.jboss.pressgang.ccms.rest.v1.collections.base;
 
 import org.jboss.pressgang.ccms.rest.v1.entities.base.RESTBaseEntityV1;
 
 public abstract class RESTBaseCollectionItemV1<T extends RESTBaseEntityV1<T, U, V>, U extends RESTBaseCollectionV1<T, U, V>,
         V extends RESTBaseCollectionItemV1<T, U, V>> {
     public static final Integer UNCHANGED_STATE = 0;
     public static final Integer ADD_STATE = 1;
     public static final Integer REMOVE_STATE = 2;
 
     protected static final Integer MIN_STATE = 0;
     private static final Integer MAX_STATE = 2;
 
     private Integer state = 0;
 
     public abstract T getItem();
 
     public abstract void setItem(final T item);
 
     public abstract V clone(boolean deepCopy);
 
     protected boolean validState(final Integer state) {
         return state != null && state >= MIN_STATE && state <= MAX_STATE;
     }
 
     public Integer getState() {
         return state;
     }
 
     public void setState(final Integer state) {
         if (!validState(state)) this.state = UNCHANGED_STATE;
         else this.state = state;
     }
 
     @Override
     public boolean equals(final Object o) {
         if (o == null) return false;
         if (this == o) return true;
         if (!(o instanceof RESTBaseCollectionItemV1<?, ?, ?>)) return false;
 
         final RESTBaseCollectionItemV1<?, ?, ?> item = (RESTBaseCollectionItemV1<?, ?, ?>) o;
 
         if (getItem() == null && item.getItem() != null) return false;
         if (getItem() != null && item.getItem() == null) return false;
         if (getItem() != null && item.getItem() != null && !getItem().equals(item.getItem())) return false;
 
         if (state == null && item.getState() != null) return false;
         if (state != null && item.getState() == null) return false;
         if (state != null && item.getState() != null && !state.equals(item.getState())) return false;
 
         return true;
     }
 
     public void cloneInto(final RESTBaseCollectionItemV1<T, U, V> clone, boolean deepCopy) {
         clone.state = state;
 
         if (deepCopy) {
             clone.setItem(getItem() == null ? null : getItem().clone(deepCopy));
         } else {
             clone.setItem(getItem());
         }
     }
 
     public boolean returnIsRemoveItem() {
         return state.equals(REMOVE_STATE);
     }
 
     public boolean returnIsAddItem() {
         return state.equals(ADD_STATE);
     }
 }
