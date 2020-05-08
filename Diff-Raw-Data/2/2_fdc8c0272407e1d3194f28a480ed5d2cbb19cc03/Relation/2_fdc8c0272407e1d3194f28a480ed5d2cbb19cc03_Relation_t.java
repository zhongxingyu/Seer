 /******************************************************************************
   Math utility classes
   Copyright (C) 2012 Sylvain Halle
   
   This program is free software; you can redistribute it and/or modify
   it under the terms of the GNU Lesser General Public License as published by
   the Free Software Foundation; either version 3 of the License, or
   (at your option) any later version.
   
   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.
   
   You should have received a copy of the GNU Lesser General Public License along
   with this program; if not, write to the Free Software Foundation, Inc.,
   51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
  ******************************************************************************/
 package ca.uqac.info.util;
 
 import java.util.*;
 
 /**
  * Implementation of the mathematical concept of a relation, that is,
  * a map <i>K</i> &rarr; 2<sup>V</sup> from the set
  * of pre-image values <i>P</i> to a set of values of type <i>V</i>
  * @author sylvain
  *
  * @param <K> The domain type
  * @param <V> The image type
  */
 public class Relation<K,V> extends HashMap<K,Set<V>>
 {
 
   /**
    * 
    */
   private static final long serialVersionUID = 1L;
   
   public Relation()
   {
     super();
   }
   
   /**
    * Associates a single value to a key
    * @param key
    * @param value
    */
   public void put(K key, V value)
   {
     Set<V> v = new HashSet<V>();
     v.add(value);
     put(key, v);
   }
   
   /**
    * Adds a new value to the existing values associated to
    * some key. That is, while {@link put()} overwrites, <tt>add()</tt>
    * appends.
    * @param key
    * @param value
    */
   public void add(K key, V value)
   {
   	if (!containsKey(key))
  		put(key, value);
   	else
   	{
   		Set<V> vals = get(key);
   		vals.add(value);
   		put(key, vals);
   	}
   }
   
   /**
    * Adds a set of new values to the existing values associated to
    * some key. That is, while {@link put()} overwrites, <tt>add()</tt>
    * appends.
    * @param key
    * @param value
    */
   public void add(K key, Set<V> values)
   {
   	if (!containsKey(key))
   		put(key, values);
   	else
   	{
   		Set<V> vals = get(key);
   		vals.addAll(values);
   		put(key, vals);
   	}
   }
 
   /**
    * Adds keys and values from a map to another
    * @param to The destination map
    * @param from The map to add stuff from
    */
   public void fuseFrom(Relation<K,V> from)
   {
     if (from == null)
       return;
     Set<K> keys = from.keySet();
     for (K k : keys)
     {
       Set<V> vals = from.get(k);
       if (!containsKey(k))
       {
         put(k, vals);
         continue;
       }
       Set<V> cur_vals = get(k);
       cur_vals.addAll(vals);
       put(k, cur_vals);
     }
   }
 }
