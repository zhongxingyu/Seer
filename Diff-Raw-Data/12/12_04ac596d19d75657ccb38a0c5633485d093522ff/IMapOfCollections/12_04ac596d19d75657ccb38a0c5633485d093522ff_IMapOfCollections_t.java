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
 
 import java.util.Collection;
 import java.util.Map;
 import java.util.Set;
 
 public interface
 	IMapOfCollections<
 		KeyType,
 		ValueType,
 		CollectionType extends Collection<ValueType>
 	> extends
 	Map<
 		KeyType,
 		CollectionType
 	>
 {
 	/**
 	 * Adds the given value to the collection mapped to the specified key.
 	 * Creates that mapping if necessary.
 	 * @param _key The key that maps to the collection into which to insert the value.
 	 * @param _value The value to insert
 	 * @return The result of the underlying collection's add method.
 	 */
 	public boolean add(KeyType _key, ValueType _value);
 
 	/**
 	 * Removes the specified value from the collection mapped to the provided key.
 	 * @param _key The key identifying the collection from which to remove.
 	 * @param _value The value to remove from the specified collection.
 	 * @return The removed value, or null if the value wasn't present
 	 */
	public boolean remove_from(KeyType _key, ValueType _value);
 
 	/**
 	 * Removes all occurrences of the provided value across all collections contained in this map.
 	 * @param _value The value to remove.
 	 * @return A set containing all of the keys that mapped to a collection which formerly contained the value.
 	 */
 	public Set<KeyType> removeAll(ValueType _value);
 
 }
