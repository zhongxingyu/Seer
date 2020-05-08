 /*
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 
 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package org.gengar.util.collection;
 
 import java.util.*;
 
 public abstract class
 	AMapOfCollections<
 		KeyType,
 		ValueType,
 		CollectionType extends Collection<ValueType> >
 	implements
 	IMapOfCollections<
 		KeyType,
 		ValueType,
 		CollectionType >
 {
 
 	protected ICollectionFactory<ValueType, CollectionType> $collection_factory;
 
 	protected Map<KeyType, CollectionType> $underlying_map;
 
 	public AMapOfCollections(
 			final IMapFactory<KeyType, CollectionType, Map<KeyType,CollectionType>> _m_factory,
 			final ICollectionFactory<ValueType, CollectionType> _c_factory )
 	{
 		this( _m_factory.make(), _c_factory );
 	}
 
 	// primary constructor
 	public AMapOfCollections(
 			final Map<KeyType,CollectionType> _map,
 			final ICollectionFactory<ValueType, CollectionType> _c_factory )
 	{
 		$collection_factory = _c_factory;
 		$underlying_map = _map;
 	}
 
 	@Override
 	public boolean add(KeyType _key, ValueType _value) {
 
 		CollectionType collection = $underlying_map.get( _key );
 
 		if( null == collection ) {
 
 			collection = $collection_factory.make();
 			$underlying_map.put( _key, collection );
 		}
 
 		return collection.add( _value );
 	}

 	@Override
	public boolean remove(KeyType _key, ValueType _value) {
 
 		CollectionType collection = $underlying_map.get( _key );
 
 		if( null == collection ) {
 			return false;
 		}
 
 		return collection.remove( _value );
 	}
 
 	@Override
 	public Set<KeyType> removeAll(ValueType _value) {
 
 		Set<KeyType> removed_from_keys = new HashSet<KeyType>();
 
 		boolean removed = false;
 
 		for( Map.Entry<KeyType, CollectionType> _entry : $underlying_map.entrySet() ) {
 
 			removed = _entry.getValue().remove( _value );
 
 			if( removed ) {
 				removed_from_keys.add( _entry.getKey() );
 			}
 		}
 
 		return removed_from_keys;
 	}
 
 	@Override
 	public int size() {
 		return $underlying_map.size();
 	}
 
 	@Override
 	public boolean isEmpty() {
 		return $underlying_map.isEmpty();
 	}
 
 	@Override
 	public boolean containsKey(Object _key) {
 		return $underlying_map.containsKey( _key );
 	}
 
 	/**
 	 * Determines if any of the collections in this mapping contain a value that matches the provided value.
 	 * Note that the value may exist multiple times, but only one occurrence is required for this method to return true.
 	 * Additional occurrences have no effect.
 	 * @param _value The value to check each of the collections in this map for.
 	 * @return true if this value exists anywhere in any of the collections contained in this map. False if none do.
 	 */
 	@Override
 	public boolean containsValue(Object _value) {
 
 		for( CollectionType child : $underlying_map.values() ) {
 			if( child.contains( _value ) ) {
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	@Override
 	public CollectionType get(Object _key) {
 		return $underlying_map.get( _key );
 	}
 
 	@Override
 	public CollectionType put(KeyType _key, CollectionType _value) {
 		return $underlying_map.put( _key, _value );
 	}
 
 	@Override
 	public CollectionType remove(Object _key) {
 		return $underlying_map.remove( _key );
 	}
 
 	@Override
 	public void putAll(Map<? extends KeyType, ? extends CollectionType> _m ) {
 		$underlying_map.putAll( _m );
 	}
 
 	@Override
 	public void clear() {
 		$underlying_map.clear();
 	}
 
 	@Override
 	public Set<KeyType> keySet() {
 		return $underlying_map.keySet();
 	}
 
 	@Override
 	public Collection<CollectionType> values() {
 		return $underlying_map.values();
 	}
 
 	@Override
 	public Set<Entry<KeyType, CollectionType>> entrySet() {
 		return $underlying_map.entrySet();
 	}
 }
