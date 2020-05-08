 package com.evervoid.utils;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * BiMap is a wrapper containing two maps. It allows for fast lookup on both the "key" and "value" (interchangeable).
  * 
  * @param <T1>
  *            The Type of the "key"
  * @param <T2>
  *            The Type of the "value"
  */
 public class BiMap<T1, T2>
 {
 	/**
 	 * The first map, containing key -> value
 	 */
 	private final Map<T1, T2> aMap1;
 	/**
 	 * The second map, containing value -> key
 	 */
 	private final Map<T2, T1> aMap2;
 
 	/**
 	 * Default constructor; initializes empty maps.
 	 */
 	public BiMap()
 	{
 		aMap1 = new HashMap<T1, T2>();
 		aMap2 = new HashMap<T2, T1>();
 	}
 
 	/**
 	 * Generates a BiMap from standard map. This creates a shallow copy of the map elements.
 	 * 
 	 * @param pMap
 	 *            The map to be converted.
 	 */
 	public BiMap(final Map<T1, T2> pMap)
 	{
 		aMap1 = new HashMap<T1, T2>();
 		aMap2 = new HashMap<T2, T1>();
 		for (final T1 key : pMap.keySet()) {
 			// for ever pair (k, v), add the mapping
 			put(key, pMap.get(key));
 		}
 	}
 
 	/**
 	 * Clears all entries from the BiMap.
 	 */
 	public void clear()
 	{
 		aMap1.clear();
 		aMap2.clear();
 	}
 
 	/**
 	 * Verifies whether the specified object is contained within either sets.
 	 * 
 	 * @param obj
 	 *            The object of interest
 	 * @return true if and only if the object is contained within one or both of the sets.
 	 */
 	public boolean contains(final Object obj)
 	{
 		return aMap1.containsKey(obj) || aMap2.containsKey(obj);
 	}
 
 	/**
 	 * Returns the object associated with the (t -> obj) entry in the "value" map.
 	 * 
 	 * @param t
 	 *            The object which will be used as the key in lookup.
 	 * @return The object corresponding to the entry (t -> obj) in the "value" map. Null if no such element exists.
 	 */
 	public T1 get1(final T2 t)
 	{
 		return aMap2.get(t);
 	}
 
 	/**
 	 * Returns the object associated with the (t -> obj) entry in the "key" map.
 	 * 
 	 * @param t
 	 *            The object to be used as a key in lookup.
 	 * @return The object corresponding to the entry (t -> obj) in the "key" map. Null if no such element exists.
 	 */
 	public T2 get2(final T1 t)
 	{
 		return aMap1.get(t);
 	}
 
 	/**
 	 * Chooses an element at random from the "key" set.
 	 * 
 	 * @return A random element contained in the key set. Null if the set is empty.
 	 */
 	public T1 getRandom1()
 	{
 		return MathUtils.getRandomElement(aMap1.keySet());
 	}
 
 	/**
 	 * Chooses an element at random from the "value" set.
 	 * 
 	 * @return A random element contained in the key set. Null if the set is empty.
 	 */
 	public T2 getRandom2()
 	{
 		return MathUtils.getRandomElement(aMap2.keySet());
 	}
 
 	/**
 	 * Checks whether both maps are empty.
 	 * 
 	 * @return true if both maps are empty, false otherwise.
 	 */
 	public boolean isEmpty()
 	{
 		// if they are not both empty, something is horrible wrong
 		return aMap1.isEmpty() || aMap2.isEmpty();
 	}
 
 	/**
 	 * Returns a shallow copy of the "key" elements
 	 * 
 	 * @return The set containing all "key" elements
 	 */
 	public Set<T1> keySet1()
 	{
 		return aMap1.keySet();
 	}
 
 	/**
 	 * Returns a shallow copy of the "values" set.
 	 * 
 	 * @return The set containing all "value" elements
 	 */
 	public Set<T2> keySet2()
 	{
 		return aMap2.keySet();
 	}
 
 	/**
 	 * Creates a entries mapping (obj1->obj2) and (obj2->obj1).
 	 * 
 	 * @param obj1
 	 *            The "key" of the pair.
 	 * @param obj2
 	 *            The "value" of the pair.
 	 */
 	public void put(final T1 obj1, final T2 obj2)
 	{
 		aMap1.put(obj1, obj2);
 		aMap2.put(obj2, obj1);
 	}
 
 	/**
 	 * Removes all instances of obj from the BiMap.
 	 * 
 	 * @param obj
 	 *            The object to be removed.
 	 */
 	public void remove(final Object obj)
 	{
 		if (aMap1.containsKey(obj)) {
 			aMap2.remove(aMap1.get(obj));
 			aMap1.remove(obj);
 		}
 		else if (aMap2.containsKey(obj)) {
 			aMap1.remove(aMap2.get(obj));
 			aMap2.remove(obj);
 		}
 	}
 
 	/**
 	 * Returns the size of the key set, which will be the same as the size of the value set.
 	 * 
	 * @return The size of the BiMap.
 	 */
 	public int size()
 	{
 		return aMap1.size();
 	}
 }
