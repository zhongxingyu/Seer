 /*
  * $Id$
  * $Revision$
  * $Date$
  *
  * ====================================================================
  * Copyright (c) 2005, Topicus B.V.
  * All rights reserved.
  */
 package org.apache.wicket.security.util;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * @author marrink
  */
 public class ManyToManyMap
 {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	private static final Iterator EMPTY_ITERATOR = new EmptyIterator();
 
 	private Map mappings;
 
 	/**
 	 * 
 	 */
 	public ManyToManyMap()
 	{
 		mappings = new HashMap();
 	}
 
 	/**
 	 * @param initialCapacity
 	 */
 	public ManyToManyMap(int initialCapacity)
 	{
 		mappings = new HashMap(initialCapacity);
 	}
 
 	/**
 	 * @param initialCapacity
 	 * @param loadFactor
 	 */
 	public ManyToManyMap(int initialCapacity, float loadFactor)
 	{
 		mappings = new HashMap(initialCapacity, loadFactor);
 	}
 
 	/**
 	 * Adds a key value mapping in this map. Since this maps many to many relations no
 	 * previous mappings will be overriddden.
 	 * @param left
 	 * @param right
 	 */
 	public void add(Object left, Object right)
 	{
 		if (left == null)
 			throw new NullPointerException("left must not be null.");
 		if (right == null)
 			throw new NullPointerException("right must not be null.");
 		Set manys = (Set) mappings.get(left);
 		if (manys == null)
 			manys = new HashSet();
 		manys.add(right);
 		mappings.put(left, manys);
 		manys = (Set) mappings.get(right);
 		if (manys == null)
 			manys = new HashSet();
 		manys.add(left);
 		mappings.put(right, manys);
 	}
 
 	/**
 	 * Removes a manny to manny mapping.
 	 * @param left
 	 * @return false if the mapping did not exist, true otherwise
 	 */
 	public boolean remove(Object left, Object right)
 	{
 		Set manys = (Set) mappings.get(left);
 		if (manys != null)
 		{
 			if (manys.remove(right))
 			{
 				if (manys.isEmpty())
 					mappings.remove(left);
 				manys = (Set) mappings.get(right);
 				manys.remove(left);
 				if (manys.isEmpty())
 					mappings.remove(right);
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public Set removeAllMappings(Object leftOrRight)
 	{
 		Set manys = (Set) mappings.remove(leftOrRight);
 		if (manys != null)
 		{
 			Iterator it = manys.iterator();
 			Set temp = null;
 			Object next;
 			while (it.hasNext())
 			{
 				next = it.next();
 				temp = (Set) mappings.get(next);
 				temp.remove(leftOrRight);
 				if (temp.isEmpty())
 					mappings.remove(next);
 			}
 		}
 		return manys;
 	}
 
 	public Set get(Object left)
 	{
 		Set set = (Set) mappings.get(left);
 		if (set == null)
			return Collections.EMPTY_SET;
 		return Collections.unmodifiableSet(set);
 	}
 
 	/**
 	 * @return the number of mapping entries.
 	 */
 	public int size()
 	{
 		return mappings.size();
 	}
 
 	/**
 	 * Returns the number of keys mapped to a value.
 	 * @param value
 	 * @return the number of keys mapped to this value
 	 */
 	public int numberOfmappings(Object value)
 	{
 		Set set = (Set) mappings.get(value);
 		if (set == null)
 			return 0;
 		return set.size();
 	}
 
 	/**
 	 * Check if this map contains a key.
 	 * @param key
 	 * @return true if this map contains the key, false otherwise
 	 */
 	public boolean contains(Object key)
 	{
 		return mappings.containsKey(key);
 	}
 
 	/**
 	 * Check if this map contains a key value mapping.
 	 * @return true if no key value mappings are pressent, false otherwise
 	 */
 	public boolean isEmpty()
 	{
 		return mappings.isEmpty();
 	}
 
 	/**
 	 * Removes all key value mappings.
 	 */
 	public void clear()
 	{
 		mappings.clear();
 	}
 
 	/**
 	 * Returns an <tt>Iterator</tt> over every left and righthand mapping in this map.
 	 * In no particular order.
 	 * @see java.lang.Iterable#iterator()
 	 */
 	public Iterator iterator()
 	{
 		if (mappings.isEmpty())
 			return EMPTY_ITERATOR;
 		return mappings.keySet().iterator();
 	}
 
 	/**
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	public boolean equals(Object obj)
 	{
 		if (obj instanceof ManyToManyMap)
 			return mappings.equals(((ManyToManyMap) obj).mappings);
 		return false;
 	}
 
 	/**
 	 * @see java.lang.Object#hashCode()
 	 */
 	public int hashCode()
 	{
 		return 37 * mappings.hashCode() + 1979;
 	}
 
 	/**
 	 * @see java.lang.Object#toString()
 	 */
 	public String toString()
 	{
 		return mappings.toString();
 	}
 }
