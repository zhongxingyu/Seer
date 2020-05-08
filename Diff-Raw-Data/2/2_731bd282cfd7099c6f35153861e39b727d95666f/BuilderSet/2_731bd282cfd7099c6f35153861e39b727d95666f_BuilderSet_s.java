 package collections.builders;
 
 import java.util.HashSet;
 import java.util.Set;
 
 /**
 * * This {@code BuilderSet} can be used to quickly construct a set from another collection or as a base
  * class to simplify building a set that self populate from a custom type.
  * <p/>
  * It is constructed with a {@link Builder} and a backing {@link Set}.
  * <p/>
  * The {@link Builder#build()} method must be implemented to provide the logic that will be used to build each element
  * to be contain within the set. The {@link Builder#build()} method will be repeatedly called until it returns
  * {@code null}.
  * <p/>
  * The backing set is the actual set that will hold the built elements. If no backing set is supplied then a
  * {@link HashSet} will be used.
  * <p/>
  * Example:
  * <code>
  *      Set<String> set = new TreeSet<>();
  *      set.add("one");
  *      set.add("two");
  *      set.add("three");
  * <p/>
  *      final Iterator<String> numbers = Arrays.asList("four", "five", "six").iterator();
  * <p/>
  *      Set<String> builderSet = new BuilderSet<>(new Builder<String>() {
  * <p/>
  *          private int i = 0;
  * <p/>
  *          public String build() {
  * <p/>
  *              if (numbers.hasNext()) return numbers.next();
  * <p/>
  *              return null;
  *          }
  *      }, set); // [five, four, one, six, three, two]
  * </code>
  *
  * @author Karl Bennett
  *
  * @param <E> the generic type of the sets elements.
  */
 public class BuilderSet<E> extends BuilderCollection<E> implements Set<E> {
 
     /**
      * Instantiate a new {@code BuilderSet} that will use the supplied {@link collections.builders.Builder} to build it's elements and
      * the supplied backing {@link java.util.Set} to hold it's elements.
      *
      * @param builder    the builder used to build the entries for the new map.
      * @param set the set that will be used to hold the built elements.
      */
     public BuilderSet(Builder<E> builder, Set<E> set) {
         super(builder, set);
     }
 
     /**
      * Instantiate a new {@code BuilderSet} that will use the supplied {@link Builder} to build it's elements.
      *
      * @param builder the builder used to build the entries for the new map.
      */
     public BuilderSet(Builder<E> builder) {
         this(builder, new HashSet<E>());
     }
 }
