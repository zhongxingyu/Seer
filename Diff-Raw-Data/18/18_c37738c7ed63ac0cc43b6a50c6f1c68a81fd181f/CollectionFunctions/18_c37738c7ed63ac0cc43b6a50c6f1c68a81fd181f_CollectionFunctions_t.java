 package com.logicalpractice.collections;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.hamcrest.Matcher;
 
 import com.logicalpractice.collections.support.CapturingProxy;
 import com.logicalpractice.collections.support.MethodCapture;
 import com.logicalpractice.collections.typed.Typed;
 
 /**
  * Main set of utility functions for Collections.
  * <p>
  * There are two groups of methods, first is the actual utility methods, such as
 * {@link #select(Object, Matcher) select()} and {@link #collect(Object)} and the
  * the support methods such as {@link #where(Class)} and {@link #from(Iterable)}
  * with can only be used in combination with the utility methods.
  * </p>
  * <p>
  * Typicaly each utility method comes with three versions. In order of
  * complexity: one that accepts a {@link Script} object, second that can be used
  * in conjunction with a clause function like {@link #where(Class)}, a third
  * that seemingly doesn't even accept a collection as an argument these need to
  * be used in conjunction with a source method such as {@link #from(Iterable)}
  * </p>
  * @author gareth
  */
 public class CollectionFunctions {
 
    private static final ThreadLocal<Iterable<?>> localItems = new ThreadLocal<Iterable<?>>();
 
    /**
     * Select matching items from Iterable items by evalulating the Script object
     * for each element.
     * @param <T>
     *           Type of Iterable elements
     * @param <V>
     *           Type of the value used for the matcher
     * @param items
     *           None null instance of Iterable
     * @param script
     *           None null instance of Script that excepts type T and returns a
     *           Value of type V
     * @param matcher
     *           Hamcrest matcher that if evalulated to true will result in the
     *           element being returned in the result
     * @return A list of selected elements, note that the list will be
     *         independant of the source items and that items will not be changed
     *         during this operation.
     */
    public static <T, V> List<T> select(Iterable<T> items, Script<V> script, Matcher<V> matcher) {
       List<T> result = new LinkedList<T>();
 
       for (T item : items) {
          try {
             if (matcher.matches(script.evaluate(item))) {
                result.add(item);
             }
          } catch (Exception e) {
             launderException(e);
             assert false; // unreachable
          }
       }
       return result;
    }
 
    /**
     * Select matching items from Iterable items using a where clause.
     * <p>
     * This method is similar in operation to {@link #select(Object, Matcher)}
     * except that it will accept an empty untyped list with out any problems.
     * The only real down side is that is requires an extra parameter, which
     * given you may need to
     * </p>
     * Example:
     * 
     * <pre>
     * select(listOfPeople, where(Person.class).getFirstName(), equalsIgnoreCase(&quot;smith&quot;));
     * </pre>
     * 
     * @param <T>
     *           Type of Iterable elements
     * @param <V>
     *           Type of the value used for the matcher
     * @param items
     *           None null instance of Iterable
     * @param whereclause
     *           return value from the where(ClassName.class) script like thing,
     *           the actual value isn't particalarly interesting, as it will just
     *           be thrown away.
     * @param script
     *           None null instance of Script that excepts type T and returns a
     *           Value of type V
     * @param matcher
     *           Hamcrest matcher that if evalulated to true will result in the
     *           element being returned in the result
     * @return A list of selected elements, note that the list will be
     *         independant of the source items and that items will not be changed
     *         during this operation.
     */
    public static <T, V> List<T> select(Iterable<T> items, V whereclause, Matcher<V> matcher) {
       List<T> result = new LinkedList<T>();
       CapturingProxy<V> proxy = getCurrentCapture(whereclause);
 
       for (T item : items) {
          try {
             if (matcher.matches(proxy.replay(item))) {
                result.add(item);
             }
          } catch (Exception e) {
             launderException(e);
             assert false;
          }
       }
       return result;
    }
 
    /**
     * Select matching items from Iterable items by evalulating the current chain
     * of CapturingProxy's.
     * <p>
     * This method is designed to be used in conjunction with the source method
     * {@link #from(Iterable)}. <br/> For example:
     * </p>
     * 
     * <pre>
     *    import static com.logicalpractice.collections.CollectionFunctions.* ;
     *    import static org.hamcrest.Matchers.* ;
     *    
     *    List&lt;Person&gt; output = select(from(listOfPeople).getLastName(), equalTo(&quot;Smith&quot;));
     * </pre>
     * 
     * <p>
     * There are a number of limitations with this seemly magic method. If the
     * runtime type of the source list (listOfPeople in the above example) then
     * an IllegalArgumentException is raised (by from()).
     * </p>
     * <p>
     * There are two ways of providing runtime type information to the
     * CollectionFunctions, first is to supply a none empty list, the first
     * element will be used as the prototype for CapturingProxy. An Alternative
     * is to use an implementation of {@link Typed}, Typed implementations of
     * the collection classes can be obtained with via
     * {@link CollectionUtils.typedCollection(java.util.Collection)},
     * {@link CollectionUtils.typedCollection(java.util.List)} and
     * {@link CollectionUtils.typedSet(java.util.Set)}
     * </p>
     * 
     * <pre>
     *    import static com.logicalpractice.collections.CollectionUtils.typed ;
     *    import static com.logicalpractice.collections.CollectionFunctions.* ;
     *    import static org.hamcrest.Matchers.* ;
     *    
     *    List&lt;Person&gt; output = select(from(typed(listOfPeople,Person.class)).getLastName(), equalTo(&quot;Smith&quot;));
     * </pre>
     * 
     * @param <T>
     *           Type of Iterable elements
     * @param <V>
     *           Type of the value used for the matcher
     * @param value
     *           Value returned by {from(Iterable)} it's value will be ignored
     * @param matcher
     *           Hamcrest matcher that if evalulated to true will result in the
     *           element being returned in the result
     * @return A list of selected elements, note that the list will be
     *         independant of the source items and that items will not be changed
     *         during this operation.
     */
    @SuppressWarnings("unchecked")
    public static <T, V> List<T> select(V value, Matcher<V> matcher) {
       try {
          return (List<T>) select(localItems.get(), value, matcher);
       } finally {
          localItems.remove();
       }
    }
 
    public static <T> List<T> select(Iterable<T> items, Matcher<T> matcher){
       List<T> result = new LinkedList<T>() ;
       for (T item : items) {
          if( matcher.matches(item)){
             result.add(item);
          }
       }
       return result;
    }
 
    public static <T> List<T> select(Iterable<T> items, Predicate<T> predicate){
       List<T> result = new LinkedList<T>();
       
       for (T item : items) {
          if( predicate.evaluate(item)){
             result.add(item);
          }
       }
       return result;
    }
    
    /**
     * Maps one collection to another new collection using the Script to
     * transform each value in the source collection into a value in 
     * the destination.
     * @param <T> Type of the source list
     * @param <V> type of the destination list
     * @param items Non-null Source collection
     * @param script
     * @return a list of transformed values, the order of the elements will be in
     *         iteration order of the source list.
     */
    public static <T, V> List<V> collect(Iterable<T> items, Script<V> script) {
       List<V> result = new LinkedList<V>();
 
       for (T item : items) {
          try {
             result.add(script.evaluate(item));
          } catch (Exception e) {
             launderException(e);
             assert false; // unreachable
          }
       }
       return result;
    }
 
   /**
    * 
    * @param <T>
    * @param <V>
    * @param items
    * @param fromclause should be used as from(YourClass.class).getProperty() the actual value 
    *                   past in is ignored.
    * @return a list of transformed values, the order of the elements will be in
    *         iteration order of the source list.
    */
   public static <T, V> List<V> collect(Iterable<T> items, V fromclause) {
       List<V> result = new LinkedList<V>();
      CapturingProxy<V> proxy = getCurrentCapture(fromclause);
 
       for (T item : items) {
          try {
             result.add(proxy.replay(item));
          } catch (Exception e) {
             launderException(e);
             assert false;
          }
       }
       return result;
    }
 
    @SuppressWarnings("unchecked")
    public static <V> List<V> collect(V value) {
       try {
          return collect(localItems.get(), value);
       } finally {
          localItems.remove();
       }
    }
    
    public static <T, V> void remove(Iterable<T> items, Script<V> script, Matcher<V> matcher) {
       for (Iterator<T> it = items.iterator(); it.hasNext();) {
          T item = it.next();
          try {
             if (matcher.matches(script.evaluate(item))) {
                it.remove();
             }
          } catch (Exception e) {
             launderException(e);
             assert false; // unreachable
          }
       }
    }
    
    public static <T, V> void remove(Iterable<T> items, V whereclause, Matcher<V> matcher) {
       CapturingProxy<V> proxy = getCurrentCapture(whereclause);
 
       for (Iterator<T> it = items.iterator(); it.hasNext();) {
          T item = it.next();
          try {
             if (matcher.matches(proxy.replay(item))) {
                it.remove();
             }
          } catch (Exception e) {
             launderException(e);
             assert false;
          }
       }
    }
 
    public static <V> void remove(V value, Matcher<V> matcher) {
       try {
          remove(localItems.get(), value, matcher);
       } finally {
          localItems.remove();
       }
    }
 
    
    /**
     * Provides the where clause for {@link #select(Iterable, Object, Matcher)}.
     * Returns an instance of the type provided, this proxy will attempt to
     * record the subsequent method invokations in order for them to be replayed
     * later by the {@link #select(Iterable, Object, Matcher)}.
     * <p>
     * {@link MethodCapture.capture(cls)} provides the proxy and it in turn uses
     * CGLIB to implement it. There are some rescitions to the use of
     * MethodCapture please refer it's documentation for more information.
     * </p>
     * @param <T>
     *           type of the instance
     * @param cls
     *           none null class
     * @return Capturing instance of cls
     * @throws IllegalArgumentException
     *            if cls is null
     */
    @SuppressWarnings("cast")
    public final static <T> T where(Class<T> cls) {
       if (cls == null) {
          throw new IllegalArgumentException("Null cls parameter for where(Class). cls is required");
       }
       return (T) MethodCapture.capture(cls);
    }
 
    /**
     * by() is just an alais for {@link #where(Class)}, but is included
     * to provide a more logical syntax.
     * 
     * @param <T>
     * @param cls non-null class
     * @return Capturing instance of cls
     * @see #where(Class)
     */
    public final static <T> T by(Class<T> cls){
       return where(cls);
    }
    
    /**
     * from source method, to be used in conjunction with
     * {@link #select(Object, Matcher)}.
     * <p>
     * It is important to understand that this method uses a ThreadLocal to pass
     * that actual collection to the select method. If the caller fails to call
     * {@link #select(Object, Matcher)} then the reference will be left hanging,
     * and will prevent the collection from being garbage collected.
     * </p>
     * <p>
     * This method must obtain the runtime type of the contents of the Iterable.
     * There are two ways of providing runtime type information to the
     * CollectionFunctions, first is to supply a none empty list, the first
     * element will be used as the prototype for CapturingProxy. An Alternative
     * is to use an implementation of {@link Typed}, Typed implementations of
     * the collection classes can be obtained with via
     * {@link CollectionUtils.typedCollection(java.util.Collection)},
     * {@link CollectionUtils.typedCollection(java.util.List)},
     * {@link CollectionUtils.typedSet(java.util.Set)} and
     * {@link CollectionUtils.typed(java.lang.Iterable)}.
     * </p>
     * <p>
     * {@link MethodCapture.capture(cls)} provides the proxy and it in turn uses
     * CGLIB to implement it. There are some rescitions to the use of
     * MethodCapture please refer it's documentation for more information.
     * </p>
     * @param <T>
     * @param items
     * @return
     */
    @SuppressWarnings("cast")
    public final static <T> T from(Iterable<T> items) {
       Class<T> type = typeOfIterable(items);
       localItems.set(items);
       return (T) MethodCapture.capture(type);
    }
 
    @SuppressWarnings("unchecked")
    private static final <V> CapturingProxy<V> getCurrentCapture(V value) {
       return (CapturingProxy<V>) MethodCapture.clearAndReturn();
    }
 
    @SuppressWarnings("unchecked")
    private static final <T> Class<T> typeOfIterable(Iterable<T> items) {
       if (items instanceof Typed) {
          return ((Typed) items).type();
       }
 
       Iterator<T> it = items.iterator();
 
       if (!it.hasNext()) {
          throw new IllegalArgumentException("Unable to use from() clause on empty collections");
       }
 
       return (Class<T>) it.next().getClass();
    }
 
    private static final void launderException(Exception e) {
       if (e instanceof RuntimeException) {
          throw (RuntimeException)e;
       } else if (e instanceof InvocationTargetException) {
          if (e.getCause() instanceof RuntimeException) {
             throw (RuntimeException) e.getCause();
          }
          throw new RuntimeException(e);
       }
       throw new RuntimeException(e);
 
    }
 
 }
