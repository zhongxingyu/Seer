 package ecologylab.generic;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Iterator;
 
 import ecologylab.xml.FieldAccessor;
 
 /**
  * Iterates through a Collection of things, and then through an Iterator
  * of such (nested) Collections of things.
  * Provides flat access to all members.
  * 
  * @author andruid
  *
  * @param <I>   Class that we iterate over.
  * @param <O>   Class of objects that are applied in the context of what we iterate over.
  *          This typically starts as this, but shifts as we iterate through 
  *          the nested Collection of Iterators.
  */
 public class ClassAndCollectionIterator<I extends FieldAccessor, O extends Iterable<I>>
 implements Iterator<O>
 {
   private Iterator<I> iterator;
   private Iterator<O> collection;
   private O root;
   private O currentObject;
 
   public ClassAndCollectionIterator(O firstObject)
   {
     root = firstObject;
     this.iterator  = firstObject.iterator();
   }
 
   public O next() 
   {
     try
     {
       if (collection != null)
         return nextInCollection();
       
       if (iterator.hasNext())
       {
         I firstNext = iterator.next(); 
         if(firstNext.isCollection())
         {
           collection = (Iterator<O>) firstNext.getField().get(root);
           return nextInCollection();
         }
         O next = (O) firstNext.getField().get(root);
         currentObject = next;
        return next;
       }
     } catch (IllegalArgumentException e) {
       e.printStackTrace();
     } catch (IllegalAccessException e) {
       e.printStackTrace();
     }
     return null;
   }
 
   private O nextInCollection() 
   {
     if (!collection.hasNext()) {
       collection = null;
       return next();
     } 
     O next = collection.next();
     currentObject = next;
     return next;
   }
 
   public O currentObject()
   {
     return currentObject;
   }
 
   public void remove() 
   {
     throw new UnsupportedOperationException();
   }
 
   public boolean hasNext()
   {    
     return iterator.hasNext() || (collection != null && collection.hasNext());
   }
 }
