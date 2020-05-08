 /*
  * Copyright 2013 Robert Philipp
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 package org.freezedry.difference;
 
 import org.freezedry.persistence.PersistenceEngine;
 import org.freezedry.persistence.containers.Pair;
 import org.freezedry.persistence.keyvalue.KeyValueBuilder;
 import org.freezedry.persistence.keyvalue.renderers.PersistenceRenderer;
 import org.freezedry.persistence.tree.InfoNode;
 import org.freezedry.persistence.writers.KeyValueMapWriter;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.*;
 import java.util.regex.Pattern;
 
 /**
  * Calculates the difference between two object of the same type, and lists the flattened properties that differ. Also
  * provides access to flatten objects into key-value pairs.
  *
  * By calling {@link #listOrderIgnored()} any differences in collections or arrays that are purely caused by the ordering
  * are ignored. For example, if we have a list, numbers=(1,2,3,4,5), in the reference object, and then we compare it to
  * the modified object where the list, numbers=(2,4,3,1,5), the difference calculator would report no difference. This
  * works for multi-dimensional collections or arrays, and for list containing objects that contains list, etc.
  *
  * @author Robert Philpp
  *         10/7/13, 1:27 PM
  */
 public class ObjectDifferenceCalculator {
 
 	private static Logger LOGGER = LoggerFactory.getLogger( ObjectDifferenceCalculator.class );
 
 	private static Pattern LIST_PATTERN = Pattern.compile( "(.)*(\\[[\\d]+\\])+(.)*" );
 
 	private final KeyValueMapWriter mapWriter;
 	private final PersistenceEngine persistenceEngine = new PersistenceEngine().withPersistNullValues();
 	private boolean isListOrderIgnored = false;
 
 	/**
 	 * Constructs a basic key-value writer that uses the specified renderers and separator.
 	 * @param renderers The mapping between the {@link Class} represented by an {@link org.freezedry.persistence.tree.InfoNode} and
 	 * the {@link org.freezedry.persistence.keyvalue.renderers.PersistenceRenderer} used to create the key-value pair.
 	 * @param arrayRenderer The {@link org.freezedry.persistence.keyvalue.renderers.PersistenceRenderer} used to create key-value pairs for
 	 * {@link org.freezedry.persistence.tree.InfoNode}s that represent an array.
 	 * @param keySeparator The separator between the flattened elements of the key
 	 * @see org.freezedry.persistence.keyvalue.AbstractKeyValueBuilder#getRenderer(Class)
 	 */
 	public ObjectDifferenceCalculator( final Map<Class<?>, PersistenceRenderer> renderers,
 									   final PersistenceRenderer arrayRenderer,
 									   final String keySeparator )
 	{
 		mapWriter = new KeyValueMapWriter( renderers, arrayRenderer, keySeparator );
 	}
 
 	/**
 	 * Constructs a basic key-value writer that uses the default renderers and specified separator.
 	 * @param keySeparator The separator between the flattened elements of the key
 	 */
 	public ObjectDifferenceCalculator( final String keySeparator )
 	{
 		mapWriter = new KeyValueMapWriter( keySeparator );
 	}
 
 	/**
 	 * Constructs a basic key-value writer that uses the default renderers and separator.
 	 */
 	public ObjectDifferenceCalculator()
 	{
 		mapWriter = new KeyValueMapWriter();
 	}
 
 	/**
 	 * Constructs a key-value writer using the specified key-value list builder
 	 * @param builder The {@link org.freezedry.persistence.keyvalue.KeyValueBuilder} used to flatten the semantic model
 	 */
 	public ObjectDifferenceCalculator( final KeyValueBuilder builder )
 	{
 		mapWriter = new KeyValueMapWriter( builder );
 	}
 
 	/**
 	 * Sets this difference calculator to ignore the ordering of lists when comparing objects. When ignoring order,
 	 * the difference calculator checks only that all the elements of a list match. This works for multidimensional lists
 	 * or objects contained in, and containing, lists.
 	 * @return This {@link org.freezedry.difference.ObjectDifferenceCalculator} for chaining
 	 */
 	public ObjectDifferenceCalculator listOrderIgnored()
 	{
 		this.isListOrderIgnored = true;
 		return this;
 	}
 
 	/**
 	 * Sets this difference calculator to care the ordering of lists when comparing objects.
 	 * @return This {@link org.freezedry.difference.ObjectDifferenceCalculator} for chaining
 	 */
 	public ObjectDifferenceCalculator listOrderMatters()
 	{
 		this.isListOrderIgnored = false;
 		return this;
 	}
 
 	/**
 	 * If set to {@code true} then tells the {@link org.freezedry.persistence.PersistenceEngine} to persist null values.
 	 * By default the {@link org.freezedry.persistence.PersistenceEngine} does not persist null values.
 	 * @param isPersistNullValues Whether or not to persist fields that have null values
 	 */
 	public void setPersistNullValues( final boolean isPersistNullValues )
 	{
 		persistenceEngine.setPersistNullValues( isPersistNullValues );
 	}
 
 	/**
 	 * Calculates the difference between the two specified objects and returns the difference as key-value pairs. The
 	 * {@link Difference} object holds the value of the {@code object}'s property and the {@code referenceObjects}'s property
 	 * that differ.
 	 * @param modifiedObject The new object to compare against the reference object
 	 * @param referenceObject The reference object against which to compare the object
 	 * @param <T> The object's and the reference object's type.
 	 * @return A {@link Map} that holds the flattened property names of all the properties that are different between the
 	 * object and the reference object. For each entry in the map, the {@link Difference} holds the object's property
 	 * value and the reference object's property value
 	 */
 	public final < T > Map< String, Difference > calculateDifference( final T modifiedObject, final T referenceObject )
 	{
 		final Map< String, Object > objectMap = flattenObject( modifiedObject );
 		final Map< String, Object > referenceObjectMap = flattenObject( referenceObject );
 
 		final Set< String > keys = new LinkedHashSet<>();
 		keys.addAll( objectMap.keySet() );
 		keys.addAll( referenceObjectMap.keySet() );
 
 		final Map< String, Difference > difference = new LinkedHashMap<>();
 		for( String key : keys )
 		{
 			final Object modifiedValue = objectMap.get( key );
 			final Object referenceValue = referenceObjectMap.get( key );
 			if( ( modifiedValue != null && referenceValue != null && !modifiedValue.toString().equals( referenceValue.toString() ) ) ||
 				( modifiedValue != null && referenceValue == null ) ||
 				( modifiedValue == null && referenceValue != null ) )
 			{
 				difference.put( key, new Difference( modifiedValue, referenceValue ) );
 			}
 		}
 
 		if( isListOrderIgnored )
 		{
 			ignoreListOrder( difference );
 		}
 
 		return difference;
 	}
 
 	/**
 	 * Flattens the object into key-value pairs.
 	 * @param object The object to flatten
 	 * @return The flattened version of the object
 	 * @see KeyValueBuilder
 	 */
 	public final Map< String, Object > flattenObject( final Object object )
 	{
 		final InfoNode rootNode = persistenceEngine.createSemanticModel( object );
 		return mapWriter.createMap( rootNode );
 	}
 
 	/**
 	 * Removes differences where the difference is only due to the place within a list the difference occurs.
 	 * @param differences A map holding differences between two objects and the keys representing the flattened field names
 	 * @return The map with differences removed that are only due to their placement in a list
 	 */
 	private Map< String, Difference > ignoreListOrder( final Map< String, Difference > differences )
 	{
 		LOGGER.debug( "Ignoring the order of lists" );
 		final Map< String, Map< String, Difference > > categories = classifyLists( differences );
 		for( Map.Entry< String, Map< String, Difference > > entry : categories.entrySet() )
 		{
 			// differences, even though they're lists, that have only one element are different regardless of
 			// whether order matters, so, in that case, we don't check that difference for list ordering
 			if( entry.getValue().size() > 1 )
 			{
 				LOGGER.trace( "Comparing lists: " + entry.getKey() );
 				// todo if one-dimensional list, then just use the map< value, count > method to compare lists
 
 				// create the value tree (pair.first) and reference tree (pair.second)
 				final Pair< Group, Group > trees = createTree( entry.getKey(), entry.getValue() );
 
 				// compare the trees, returning the names of lists that are the same
 				final List< String > groupNames = getGroupNames( entry.getKey(), entry.getValue().keySet() );
 				removeEquivalentLists( trees.getFirst(), trees.getSecond(), groupNames, differences );
 			}
 		}
 		return differences;
 	}
 
 	/**
 	 * Removes lists from the difference map that are equivalent when order is not taken into account.
 	 * @param values The root group (node) holding the values, or modified values
 	 * @param referenceValues The root group (node) holding the reference values
 	 * @param groupNames The names of the groups in the value (and reference value) trees
 	 * @param differences The raw differences as calculated by the {@link #calculateDifference(Object, Object)} method
 	 *                    before order is taken into account.
 	 */
 	private void removeEquivalentLists( final Group values,
 										final Group referenceValues,
 										final List<String> groupNames,
 										final Map<String, Difference> differences )
 	{
 		// todo make more efficient by remove group names for which the lists are equivalent
 		for( String groupName : groupNames )
 		{
 			final Group value = values.findGroup( groupName );
 			if( value != null )
 			{
 				for( String groupName2 : groupNames )
 				{
 					final Group referenceValue = referenceValues.findGroup( groupName2 );
 					if( referenceValue != null && value.equivalentValues( referenceValue ) )
 					{
 						// remove the list from the differences map
 						for( Map.Entry< String, Set< String > > entry : value.getValues().entrySet() )
 						{
 							for( String key : entry.getValue() )
 							{
 								differences.remove( key );
 								LOGGER.debug( "Removed (only difference in order): " + key );
 							}
 						}
 
 						// remove the nodes, since they are the same
 						value.getParent().removeChild( value );
 						referenceValue.getParent().removeChild( referenceValue );
 
 						break;
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * Classifies the lists into groups representing the same field. The categories are represented by the key in
 	 * the outer return map, which is the group name with the indexes changed to wild cards (i.e. [1] or [2] -> [*])
 	 * @param differences The object differences
 	 * @return a map whose keys are the group name patterns, and whose values are the differences.
 	 */
 	private Map< String, Map< String, Difference > > classifyLists( final Map< String, Difference > differences )
 	{
 		final Map< Pattern, Map< String, Difference > > mainGroups = new LinkedHashMap<>();
 
 		// finds the list-patterns and classifies each of the field names accordingly
 		for( Map.Entry< String, Difference > entry : differences.entrySet() )
 		{
 			// for keys that fit the list pattern, we classify them in groups that represent the same fields
 			if( LIST_PATTERN.matcher( entry.getKey() ).matches() )
 			{
 				// see if any of the patterns match the current key, if not, then we add the new pattern to the map
 				// along with the key and its value
 				boolean isClassified = false;
 				for( Map.Entry< Pattern, Map< String, Difference > > classEntry : mainGroups.entrySet() )
 				{
 					if( classEntry.getKey().matcher( entry.getKey() ).matches() )
 					{
 						classEntry.getValue().put( entry.getKey(), entry.getValue() );
 						isClassified = true;
 						break;
 					}
 				}
 				if( !isClassified )
 				{
 					final Map< String, Difference > difference = new HashMap<>();
 					difference.put( entry.getKey(), entry.getValue() );
 					final Pattern pattern = Pattern.compile( entry.getKey().replaceAll( "\\[[\\d]+\\]", "\\\\[[\\\\d]+\\\\]" ) );
 					mainGroups.put( pattern, difference );
 				}
 			}
 		}
 
 		// convert the indexes for the regex patterns (the group names) to "[*]"
 		final Map< String, Map< String, Difference > > groups = new HashMap<>( mainGroups.size() );
 		for( Map.Entry< Pattern, Map< String, Difference > > entry : mainGroups.entrySet() )
 		{
 			final String key = entry.getKey().pattern().replaceAll( Pattern.quote( "\\[[\\d]+\\]" ), "[*]" );
 			groups.put( key, entry.getValue() );
 		}
 		return groups;
 	}
 
 	/**
 	 * Creates the value and reference trees representing the lists. The first elements in the pairs contain the value
 	 * tree, and the second elements contain the reference-value tree.
 	 * @param category The list category
 	 * @param differences The object differences
 	 * @return the value and reference trees representing the lists. The first elements in the pairs contain the value
 	 * tree, and the second elements contain the reference-value tree.
 	 */
 	private Pair< Group, Group > createTree( final String category, final Map< String, Difference > differences )
 	{
 		// split the difference objects into values and reference values
 		final Map< String, Object > values = new HashMap<>();
 		final Map< String, Object > referenceValues = new HashMap<>();
 		for( Map.Entry< String, Difference > entry : differences.entrySet() )
 		{
 			values.put( entry.getKey(), entry.getValue().getObject() );
 			referenceValues.put( entry.getKey(), entry.getValue().getReferenceObject() );
 		}
 
 		// the number of groups give the dimensions of the lists, and is used as the max
 		// levels of the tree as a stopping condition
 		final int numGroups = calcListDimensions( category );
 		if( numGroups < 1 )
 		{
 			return null;
 		}
 
 		// create the tree for the values
 		final Group root = new Group( category );
 		createGroup( root, 1, numGroups, category, values );
 
 		// create the tree for the reference values
 		final Group referenceRoot = new Group( category );
 		createGroup( referenceRoot, 1, numGroups, category, referenceValues );
 
 		return new Pair<>( root, referenceRoot );
 	}
 
 	/**
 	 * Calculates the number of unique indexes for the next dimension of the multidimensional array starting from the
 	 * dimension represented by the specified name.
 	 * @param name The name representing the current dimension of the multi-dimensional array
 	 * @param keys The flattened field names representing the entire multi-dimensional array
 	 * @return the number of unique indexes for the next dimension of the multidimensional array starting from the
 	 * dimension represented by the specified name.
 	 */
 	private int getNumGroups( final String name, final Set< String > keys )
 	{
 		final int openBracket = name.lastIndexOf( "[" );
 
 		// root node, no dimensions
 		if( openBracket == -1 )
 		{
 			return -1;
 		}
 		final Set< Integer > indexes = new HashSet<>();
 		for( String key: keys )
 		{
 			final int closeBracket = key.indexOf( "]", openBracket + 1 );
 			indexes.add( Integer.valueOf( key.substring( openBracket + 1, closeBracket ) ) );
 		}
 		return Collections.max( indexes ) + 1;
 	}
 
 	/**
 	 * Returns the group names with the indexes filled in (i.e. the wild card is replaced). For example, if the name
 	 * is {@code name[0][*]} and there are 3 groups within that name found in the keys, then the following list of names
 	 * will be returned {@code name[0][0], name[0][1], name[0][2]}.
 	 * @param name The name that serves as a template (i.e name[0][*])
 	 * @param keys The keys to use to determine the number of groups at this dimension
 	 * @return the group names with the indexes filled in (i.e. the wild card is replaced)
 	 */
 	private List< String > getGroupNames( final String name, final Set< String > keys )
 	{
 		final List< String > indexes = new ArrayList<>();
 		getGroupNames( name, keys, indexes );
 		return indexes;
 
 //		final List< String > indexes = new ArrayList<>();
 //
 //		final int index = name.indexOf( "[*]" );
 //		final String truncated = index > 0 ? name.substring( 0, name.indexOf( "[*]" )+3 ) : name;
 //
 //		final int numGroups = getNumGroups( truncated, keys );
 //		for( int i = 0; i < numGroups; ++i )
 //		{
 //			indexes.add( name.replaceFirst( "\\*", Integer.toString( i ) ) );
 //		}
 //		return indexes;
 	}
 
 	private void getGroupNames( final String name, final Set< String > keys, final List< String > indexes )
 	{
 		final int index = name.indexOf( "[*]" );
 		final int lastIndex = name.lastIndexOf( "[*]" );
 		final String truncated = index > 0 ? name.substring( 0, name.indexOf( "[*]" )+3 ) : name;
 		final int numGroups = getNumGroups( truncated, keys );
 		for( int i = 0; i < numGroups; ++i )
 		{
 			final String indexedName = name.replaceFirst( "\\*", Integer.toString( i ) );
 			indexes.add( indexedName );
 			if( index < lastIndex )
 			{
 				getGroupNames( indexedName, keys, indexes );
 			}
 		}
 	}
 
 	/**
 	 * Creates the tree (recursively) of groups, each representing an element of a list (which itself may contain lists).
 	 * The leaf groups are the actual values of the lists.
 	 * @param parent The parent group to which the newly created groups are added.
 	 * @param currentLevel The current level of within the tree
 	 * @param maxLevel The max number of levels the tree will have (the dimension of the lists, i.e. name[i][j] has two dimensions)
 	 * @param groupName The name of the group (which will be something like name[*], or name[1][*], etc)
 	 * @param values The key-values representing the lists
 	 */
 	private void createGroup( final Group parent, final int currentLevel, final int maxLevel, final String groupName, final Map< String, Object > values )
 	{
 		final List< String > groupNames = getGroupNames( groupName, values.keySet() );
 		if( currentLevel < maxLevel )
 		{
 			for( String name : groupNames )
 			{
 				final Group group = new Group( name );
 				parent.addChild( group );
 				createGroup( group, currentLevel+1, maxLevel, name, values );
 			}
 		}
 		else
 		{
 			// leaf node, add the lists
 			for( Map.Entry< String, Object > entry : values.entrySet() )
 			{
 				if( groupNames.contains( entry.getKey() ) )
 				{
					final String value = ( entry.getValue() == null ? null : entry.getValue().toString() );
					parent.addValue( entry.getKey(), value );
 				}
 			}
 		}
 	}
 
 	/**
 	 * Calculates the dimensions of the list (i.e. name[*][*] has dimensions of 2)
 	 * @param category The category name
 	 * @return The number of dimensions in the list
 	 */
 	private int calcListDimensions( final String category )
 	{
 		int count = 0;
 		int index = 0;
 		while( ( index = category.indexOf( "[*]", index+3 ) ) > 0 )
 		{
 			++count;
 		}
 		return count;
 	}
 
 	/**
 	 * Class representing the difference between the object's and the reference object's value.
 	 */
 	public final static class Difference {
 
 		private final Object object;
 		private final Object referenceObject;
 
 		/**
 		 * The holds the value of a property of the object and the reference object
 		 * @param modifiedObject The value of the object's property
 		 * @param referenceObject The value of the reference object's property
 		 */
 		public Difference( final Object modifiedObject, final Object referenceObject )
 		{
 			this.object = modifiedObject;
 			this.referenceObject = referenceObject;
 		}
 
 		/**
 		 * @return The object's value
 		 */
 		public Object getObject()
 		{
 			return object;
 		}
 
 		/**
 		 * @return The reference object's value
 		 */
 		public Object getReferenceObject()
 		{
 			return referenceObject;
 		}
 
 		/**
 		 * @return a string representation of the difference
 		 */
 		@Override
 		public String toString()
 		{
 			return "Modified Object: " + (object == null ? "[null]" : object.toString()) + "; " +
 					"Reference Object: " + (referenceObject == null ? "[null]" : referenceObject.toString());
 		}
 	}
 }
