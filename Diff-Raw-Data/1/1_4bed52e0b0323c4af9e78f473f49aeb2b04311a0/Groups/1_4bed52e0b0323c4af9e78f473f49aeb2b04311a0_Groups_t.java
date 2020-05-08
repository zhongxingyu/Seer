 package vitro.util;
 import java.util.*;
 
 /**
 * Groups provides a set of utility methods for filtering
 * and manipulating collections.
 *
 * @author John Earnest
 **/
 public class Groups {
 
 	private static final Random rand = new Random();
 
 	/**
 	* Extract an element from a Collection with a given class.
 	* 
 	* @param  c the desired Class.
 	* @param  source the Collection to filter.
 	* @return the first Object in source that is an instance of c.
 	**/
 	public static <S, C extends S> S firstOfType(Class<C> c, Collection<S> source) {
 		for(S a : source) {
 			if (c.isInstance(a)) { return a; }
 		}
 		return null;
 	}
 
 	/**
 	* Extract elements from a Collection with a given class.
 	* 
 	* @param  c the desired Class.
 	* @param  source the Collection to filter.
 	* @return a List of Objects from source that are instances of c.
 	**/
 	public static <S, C extends S> List<S> ofType(Class<C> c, Collection<S> source) {
 		List<S> ret = new ArrayList<S>();
 		for(S a : source) {
 			if (c.isInstance(a)) { ret.add(a); }
 		}
 		return ret;
 	}
 
 	/**
 	* Check a Collection for elements with a given class.
 	* 
 	* @param  c the desired Class.
 	* @param  source the Collection to examine.
 	* @return true if there is an Object in source that is an instance of c.
 	**/
 	public static <S, C extends S> boolean containsType(Class<C> c, Collection<S> source) {
 		for(S a : source) {
 			if (c.isInstance(a)) { return true; }
 		}
 		return false;
 	}
 
 	/**
 	* Extract a random element from a Collection.
 	*
 	* @param  source the Collection to filter.
 	* @return a random Object from source.
 	**/
 	public static <S> S any(Collection<S> source) {
 		// this is not the most efficient approach,
 		// but it's simple. Improve it later if necessary.
		if (source.size() < 1) { return null; }
 		List<S> group = new ArrayList<S>(source);
 		return group.get(rand.nextInt(group.size()));
 	}
 
 	/**
 	* Extract the first element from a Collection.
 	* The first element returned by the collection's iterator is considered 'first'.
 	* 
 	* @param  source the Collection to filter.
 	* @return the first Object from source.
 	**/
 	public static <S> S first(Collection<S> source) {
 		if (source.size() < 1) { return null; }
 		Iterator<S> iterator = source.iterator();
 		return iterator.next();
 	}
 }
