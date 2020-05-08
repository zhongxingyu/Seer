 /*
  * Copyright (c) 2009-2011 Daniel Oom, see license.txt for more info.
  */
 
 package game.world;
 
 import game.entities.EntityType;
 import game.entities.IEntity;
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.LinkedList;
 
 /**
  * A special container used for entities in the world.
  * Supports some extra iterators.
  */
 public class WorldContainer implements Iterable<IEntity> {
   private final LinkedList<IEntity> list;
 
   public WorldContainer() {
     list = new LinkedList<>();
   }
 
   /**
    * Returns the number of entities in this container.
    * @return the number of entities as an int, greater than or equal to zero
    */
   public int size() {
     return list.size();
   }
 
   /**
    * Iterator for all entities in the container.
    * @return an iterator for all entities in the container
    */
   @Override
   public Iterator<IEntity> iterator() {
     return list.iterator();
   }
 
   /**
    * Special iterator that iterates over entities with a specific type.
    * @param key the type to iterate over
    * @return an iterable over entities with the specific type
    */
   public Iterable<IEntity> iterate(final EntityType key) {
    final Iterator<IEntity> it =
      new SkipsIterator(list.iterator(), new EntityType[] { key });

     return new Iterable<IEntity>() {
       @Override
       public Iterator<IEntity> iterator() {
        return it;
       }
     };
   }
 
   /**
    * Special iterator that iterates over entities with the specified types.
    * @param keys the types to iterate over
    * @return an iterable over entities with the specified type
    */
   public Iterable<IEntity> iterate(final EntityType[] keys) {
    final Iterator<IEntity> it = new SkipsIterator(list.iterator(), keys);

     return new Iterable<IEntity>() {
       @Override
       public Iterator<IEntity> iterator() {
        return it;
       }
     };
   }
 
   /**
    * Adds the entitiy to the end of the collections.
    * @param obj the entity to add
    */
   public void addLast(IEntity obj) {
     list.addLast(obj);
   }
 
   /**
    * Adds an collection of entities to the end of the collection.
    * @param objs the entities to add
    * @throws NullPointerException if objs is null
    */
   public void addLastAll(Collection<IEntity> objs) {
     list.addAll(objs);
   }
 
   /**
    * Inserts an entity to the beginning of the collection.
    * @param obj the entity to insert
    */
   public void addFirst(IEntity obj) {
     list.addFirst(obj);
   }
 
   /**
    * Inserts an collection of entities to the beginning of the collection. Order
    * is preserved.
    * @param objs the entities to add
    * @throws NullPointerException if objs is null
    */
   public void addFirstAll(LinkedList<IEntity> objs) {
     list.addAll(0, objs);
   }
 
   /**
    * Special iterator that can skip certain entities.
    */
   private class SkipsIterator implements Iterator<IEntity> {
     private final Iterator<IEntity> internal;
     private final EntityType[] keys;
 
     private IEntity next;
 
     /**
      * Creates a new skipping iterator around an other iterator.
      * @param iter the iterator to wrap, not null
      * @param keys the entity types to iterate over, not null
      */
     public SkipsIterator(Iterator<IEntity> iter, EntityType[] keys) {
       assert (iter != null);
       assert (keys != null);
 
       internal = iter;
       this.keys = keys;
     }
 
     /**
      * Check if the iterator can offer more entities.
      * Worst case is when no entities match the keys, time complexity O(n).
      * Since the internal iterator will be exahusted. But amortized it's O(1).
      * @return {@code true} if the iteration has more entities
      */
     @Override
     public boolean hasNext() {
       internalNext();
 
       return next != null;
     }
 
     /**
      * Return the next entity with correct type from the iterator.
      * @return the next entity from the iterator
      */
     @Override
     public IEntity next() {
       internalNext();
 
       IEntity tmp = next;
       // Set next to null to force internalNext on next call
       next = null;
 
       return tmp;
     }
 
     /**
      * Remove the entity previously returned by next from the underlying
      * collection.
      */
     @Override
     public void remove() {
       internal.remove();
       next = null;
     }
 
     /**
      * Advance the internal iterator until a entity with correct type is found
      * and cache the result. Does nothing if next is not equal to null.
      */
     private void internalNext() {
       if (next == null) {
         while (internal.hasNext()) {
           next = internal.next();
 
           // We only use very small arrays here, binary search would probably be
           // slower because of larger constant.
           for (int i = 0; i < keys.length; ++i) {
             if (keys[i] == next.getType()) {
               return;
             }
           }
         }
 
         // Couldn't find a matching IEntity
         next = null;
       }
     }
   }
 }
