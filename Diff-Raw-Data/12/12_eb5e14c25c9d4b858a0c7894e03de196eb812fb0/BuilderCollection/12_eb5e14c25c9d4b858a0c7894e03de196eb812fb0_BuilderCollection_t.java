 package collections.builders;
 
 import java.util.Collection;
 import java.util.Iterator;
 
 /**
  * This {@code BuilderCollection} can be used to quickly construct a collection from another collection or as a base
  * class to simplify building a collection that self populates from a custom type.
  * <p/>
  * It is constructed with a {@link Builder} and a backing {@link Collection}.
  * <p/>
  * The {@link Builder#build()} method must be implemented to provide the logic that will be used to build each element
  * to be contain within the collection. The {@link Builder#build()} method will be repeatedly called until it returns
  * {@code null}.
  * <p/>
  * The backing collection is the actual collection that will hold the built elements.
  * <p/>
  * Example:
  * <code>
  *      Collection<String> collection = new Vector<>();
  *      collection.add("one");
  *      collection.add("two");
  *      collection.add("three");
  * <p/>
  *      final Iterator<String> numbers = Arrays.asList("four", "five", "six").iterator();
  * <p/>
  *      Collection<String> builderCollection = new BuilderCollection<>(new Builder<String>() {
  * <p/>
  *          private int i = 0;
  * <p/>
  *          public String build() {
  * <p/>
  *              if (numbers.hasNext()) return numbers.next();
  * <p/>
  *              return null;
  *          }
  *      }, collection); // [one, two, three, four, five, six]
  * </code>
  *
  * @author Karl Bennett
  *
  * @param <E> the generic type of the collections elements.
  */
 public class BuilderCollection<E> implements Collection<E> {
 
     private final Collection<E> collection;
 
 
     /**
      * Instantiate a new {@code BuilderCollection} that will use the supplied {@link Builder} to build it's elements and
      * the supplied backing {@link Collection} to hold it's elements.
      *
     * @param builder    the builder used to build the elements for the new collection.
      * @param collection the collection that will be used to hold the built elements.
      */
     public BuilderCollection(Builder<E> builder, Collection<E> collection) {
 
         if (null == builder) {
 
             throw new IllegalArgumentException(getClass().getName() + "(Builder,Collection) builder must not be null.");
         }
 
         if (null == collection) {
 
             throw new IllegalArgumentException(getClass().getName() + "(Builder,Collection) collection must not be null.");
         }
 
         this.collection = collection;
 
         for (E element = builder.build(); null != element; element = builder.build()) {
 
             this.collection.add(element);
         }
     }
 
 
     /**
      * {@inheritDoc}
      */
     @Override
     public int size() {
 
         return collection.size();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean isEmpty() {
 
         return collection.isEmpty();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean contains(Object element) {
 
         return collection.contains(element);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Iterator<E> iterator() {
 
         return collection.iterator();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Object[] toArray() {
 
         return collection.toArray();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public <T> T[] toArray(T[] array) {
 
         return collection.toArray(array);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean add(E element) {
 
         return collection.add(element);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean remove(Object element) {
 
         return collection.remove(element);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean containsAll(Collection<?> elements) {
 
         return collection.containsAll(elements);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean addAll(Collection<? extends E> elements) {
 
         return collection.addAll(elements);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean removeAll(Collection<?> elements) {
 
         return collection.removeAll(elements);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean retainAll(Collection<?> elements) {
 
         return collection.retainAll(elements);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void clear() {
 
         collection.clear();
     }
 
     @Override
     public boolean equals(Object o) {
 
         if (this == o) return true;
 
         if (o == null || getClass() != o.getClass()) return false;
 
         BuilderCollection that = (BuilderCollection) o;
 
         return collection.equals(that.collection);
 
     }
 
     @Override
     public int hashCode() {
 
         return collection.hashCode();
     }
 
     @Override
     public String toString() {
 
         return collection.toString();
     }
 }
