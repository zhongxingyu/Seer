 package org.eclipse.jst.jsf.common.internal.policy;
 
 import java.util.List;
 
 /**
  * Provides a list of order, enablement-modifiable objects.
  * 
  * @author cbateman
  * 
  */
 public abstract class OrderedListProvider
 {
     private List<OrderableObject> _orderableObjects;
 
     /**
      * 
      * @return the list of ordered objects
      */
     public final List<OrderableObject> getOrderedObjects()
     {
         if (_orderableObjects == null)
         {
             _orderableObjects = createAndPopulateOrderedObjects();
         }
         return _orderableObjects;
     }
     
     /**
      * <p>Clears the orderableObjects collection so that the next call
      * of getOrderedObjects() </p>
      */
     public final void resetOrderedObjects()
     {
         _orderableObjects = null;
     }
 
     /**
      * Move the object one toward the front of the list
      * 
      * @param object
      */
     public final void moveUp(final OrderableObject object)
     {
         final List<OrderableObject>     list = getOrderedObjects();
         final int index = list.indexOf(object);
         if (index > 0)
         {
             OrderableObject item = list.remove(index);
             list.add(index - 1, item);
         }
     }
 
     /**
      * Move the object one toward the end
      * @param object
      */
     public final void moveDown(final OrderableObject object)
     {
         final List<OrderableObject>     list = getOrderedObjects();
         int index = list.indexOf(object);
         if (index < list.size() - 1)
         {
             OrderableObject item = list.remove(index);
             list.add(index + 1, item);
         }
     }
 
     /**
      * @return a new list orderable objects populate in their initial order
      * The list must be modifiable.
      */
     protected abstract List<OrderableObject> createAndPopulateOrderedObjects();
 
     /**
      * Proxy object for ordering in list
      * 
      * @author cbateman
      * 
      */
    public static class OrderableObject implements Cloneable
     {
         private boolean _enabled;
         private Object  _object;
 
         /**
          * @param object
          * @param enabled
          */
         public OrderableObject(final Object object, final boolean enabled)
         {
             _object = object;
             _enabled = enabled;
         }
 
        private OrderableObject(final OrderableObject copyMe)
        {
            _object = copyMe._object;
            _enabled = copyMe._enabled;
        }

         /**
          * @return true if this object is enabled
          */
         public boolean isEnabled()
         {
             return _enabled;
         }
 
         /**
          * @param enabled
          */
         public void setEnabled(boolean enabled)
         {
             _enabled = enabled;
         }
 
         /**
          * @return the object in the ordered list
          */
         public Object getObject()
         {
             return _object;
         }
 
         /**
          * @param object
          */
         public void setObject(Object object)
         {
             _object = object;
         }
 
         @Override
        public OrderableObject clone()
        {
            return new OrderableObject(this);
        }

        @Override
         public boolean equals(Object obj)
         {
             if (obj instanceof OrderableObject)
             {
                 if (_enabled == ((OrderableObject)obj)._enabled)
                 {
                     Object other = ((OrderableObject)obj)._object;
                     if (other != null)
                     {
                         return other.equals(_object);
                     }
                     return _object == null;
                 }
             }
             return false;
         }
 
         @Override
         public int hashCode()
         {
             int enabledCode = _enabled ? 0xcccccccc : 0x33333333;
             if (_object == null)
             {
                 return enabledCode;
             }
             return _object.hashCode() ^ enabledCode;
         }
     }
 }
