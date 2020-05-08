 /*
  * Copyright (c) 2002-2008 "Neo Technology,"
  *     Network Engine for Objects in Lund AB [http://neotechnology.com]
  *
  * This file is part of Neo4j.
  * 
  * Neo4j is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  * 
  * You should have received a copy of the GNU Affero General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.neo4j.util.index;
 
 import org.neo4j.api.core.NeoService;
 import org.neo4j.api.core.Node;
 import org.neo4j.util.btree.KeyEntry;
 
 /**
  * A index implementation using {@link org.neo4j.util.btree.BTree BTree}. 
  * This index can only hold a single value for each unique key.
  * <p>
  * Note: this implementation is not thread safe (yet).
  */
 // not thread safe yet
 public class SingleValueIndex extends AbstractIndex
 {
 	/**
 	 * Creates/loads a single value index. The <CODE>underlyingNode</CODE> can 
 	 * either be a new (just created) node or a node that already represents a 
 	 * previously created index.
 	 *
	 * @param name The name of the index or null if index already
	 * created (using specified underlying node)
 	 * @param underlyingNode The underlying node representing the index
 	 * @param neo The embedded neo instance
 	 * @throws IllegalArgumentException if the underlying node is a index with
 	 * a different name set or the underlying node represents a different 
 	 * index
 	 */
 	public SingleValueIndex( String name, Node underlyingNode, NeoService neo )
 	{
 		super( name, underlyingNode, neo );
 	}
 
 	@Override
     protected void addOrReplace( KeyEntry entry, long value )
     {
 		entry.setValue( value );
     }
 
 	@Override
     protected void addOrReplace( Node entryNode, long value )
     {
 	    entryNode.setProperty( AbstractIndex.INDEX_VALUES, value );
     }
 
 	@Override
     protected String getIndexType()
     {
 	    return "single";
     }
 
 	@Override
     protected long getSingleValue( KeyEntry entry )
     {
 		return (Long) entry.getValue();
     }
 
 	@Override
     protected long getSingleValue( Node entry )
     {
 		return (Long) entry.getProperty( AbstractIndex.INDEX_VALUES );
     }
 
 	@Override
     protected long[] getValues( KeyEntry entry )
     {
 		return new long[] { (Long) entry.getValue() };
     }
 
 	@Override
     protected long[] getValues( Node entry )
     {
 		return new long[] { (Long) entry.getProperty( 
 			AbstractIndex.INDEX_VALUES ) };
     }
 
 	@Override
     protected boolean removeAllOrOne( KeyEntry entry, long value )
     {
 	    return true;
     }
 
 	@Override
     protected boolean removeAllOrOne( Node entry, long value )
     {
 	    return true;
     }
 }
