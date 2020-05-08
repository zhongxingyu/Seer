 /*
  * Copyright (C) 2008 TranceCode Software
  *
  * This library is free software; you can redistribute it and/or modify it under
  * the terms of the GNU Lesser General Public License as published by the Free
  * Software Foundation; either version 2.1 of the License, or (at your option)
  * any later version.
  * 
  * This library is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with this library; if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA 
  *
  * $Id$
  */
 package org.trancecode.core;
 
 import java.util.Map;
 
 import com.google.common.collect.Maps;
 
 
 /**
  * @author Herve Quiroz
  * @version $Revision$
  */
 public final class CollectionUtil
 {
 	private CollectionUtil()
 	{
 		// No instantiation
 	}
 
 
 	public static <K, V> Map<K, V> newSmallWriteOnceMap()
 	{
 		// TODO
 		return Maps.newLinkedHashMap();
 	}
 
 
 	public static <K, V> Map<K, V> merge(final Map<K, V> map1, final Map<K, V> map2)
 	{
 		final Map<K, V> map = Maps.newHashMapWithExpectedSize(map1.size() + map2.size());
 		map.putAll(map1);
 		map.putAll(map2);
 
 		return map;
 	}
 
 
 	public static <K, V> Map<K, V> copyAndPut(final Map<K, V> map1, final K key, final V value)
 	{
		final Map<K, V> map = Maps.newHashMap(map1);
 		map.put(key, value);
 
 		return map;
 	}
 
 
 	public static <E, P> E apply(
 		final E initialElement, final Iterable<P> parameters, final BinaryFunction<E, E, P> function)
 	{
 		E currentElement = initialElement;
 		for (final P parameter : parameters)
 		{
 			currentElement = function.evaluate(currentElement, parameter);
 		}
 
 		return currentElement;
 	}
 }
