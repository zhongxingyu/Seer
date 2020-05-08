 package informatik.hawhamburg.teamnahme;
 
 import java.util.Iterator;
 import java.util.HashMap;
 import java.util.NoSuchElementException;
 import java.util.Set;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Moritz
  * Date: 12.04.13
  * Time: 21:18
  * To change this template use File | Settings | File Templates.
  */
 public class AssociationHashMap<P, E> implements Association<P, E> {
 
     protected HashMap<P, E> storage = new HashMap<P, E>();
 
 
     /**
      * Add the given entity with the given primary
      *
      * @param entity
      * @param primary
      * @return current association for chaining
      * @throws informatik.hawhamburg.teamnahme.AssociationException
      *
      */
     @Override
     public Association add(E entity, P primary) throws AssociationException {
 
         if(storage.containsValue(entity)) {
             throw new AssociationException("Element already associated. Duplicated elements not allowed.");
         }
 
         if(primary == null) {
             throw new AssociationException("primary key cannot be null.");
         }
 
         if(entity == null) {
             throw new AssociationException("Entity cannot be null.");
         }
 
         storage.put(primary, entity);
 
         return this;
     }
 
     /**
      * Get the entity for the given primary
      * Throws an AssociationException if no association exists to the given primary
      *
      * @param primary
      * @return The requested object
      * @throws informatik.hawhamburg.teamnahme.AssociationException
      *
      */
     @Override
     public E get(P primary) throws AssociationException {
         // check if primary exists in storage
         if(!has(primary)) {
             throw new AssociationException("Element not found.");
         }
 
         return storage.get(primary);
     }
 
     /**
      * Checks if an association exists to the given primary
      *
      * @param primary
      * @return boolean
      */
     @Override
     public boolean has(P primary) throws AssociationException {
         // check for empty primary
         if(primary == null) {
             throw new AssociationException("Primary must be given.");
         }
 
         return storage.containsKey(primary);
     }
 
     /**
      * Return the amount of entities associated
      *
      * @return amount
      */
     @Override
     public int size() {
         return storage.size();
     }
 
     /**
      * Removes the entity with the given primary key
      *
      * @param primary
      * @return current association for chaining
      */
     @Override
     public Association remove(P primary) throws AssociationException {
         if(!has(primary)) {
             throw new AssociationException("Element not found.");
         }
 
         storage.remove(primary);
 
         return this;
     }
 
     /**
      * Returns an iterator over a set of elements of type T.
      *
      * @return an Iterator.
      */
     @Override
     public Iterator<E> iterator() {
         return new Iterator<E>() {
 
             protected int readCursor = 0;
 
             protected P[] pa = (P[]) AssociationHashMap.this.storage.keySet().toArray();
 
             @Override
             public boolean hasNext() {
                return pa.length != readCursor;
             }
 
             @Override
             public E next() throws NoSuchElementException {
                 if(!hasNext()) {
                     throw new NoSuchElementException("Out of bound.");
                 }
 
                 P primary = pa[readCursor++];
 
                 try {
                     if(!AssociationHashMap.this.has(primary)) {
                         throw new NoSuchElementException("Association changed !");
                     }
                 } catch (AssociationException e) {
                     throw new NoSuchElementException("Corrupted primary array initialization");
                 }
 
                 return AssociationHashMap.this.storage.get(primary);
             }
 
             @Override
             public void remove() {
                 throw new UnsupportedOperationException();
             }
         };
     }
 }
