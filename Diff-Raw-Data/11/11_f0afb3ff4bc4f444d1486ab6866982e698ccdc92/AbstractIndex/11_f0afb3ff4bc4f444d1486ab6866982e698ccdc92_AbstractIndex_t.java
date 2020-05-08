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
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import org.neo4j.api.core.Direction;
 import org.neo4j.api.core.EmbeddedNeo;
 import org.neo4j.api.core.NeoService;
 import org.neo4j.api.core.Node;
 import org.neo4j.api.core.Relationship;
 import org.neo4j.api.core.RelationshipType;
 import org.neo4j.api.core.Transaction;
 import org.neo4j.util.btree.BTree;
 import org.neo4j.util.btree.KeyEntry;
 
 /**
  * A "multi" index implementation using {@link org.neo4j.util.btree.BTree BTree}
  * that can index multiple nodes per key. They key is checked for equality 
  * using both <CODE>hashCode</CODE> and <CODE>equal</CODE> methods. 
  * <p>
  * Note: this implementation is not thread safe (yet).
  */
 // not thread safe yet
 abstract class AbstractIndex implements Index
 {
 	public static enum RelTypes implements RelationshipType
 	{
 		INDEX_ENTRY,
 	}
 	
 	private static final Object GOTO_NODE = Long.MIN_VALUE;
 	
 	private static final String INDEX_NAME = "index_name";
 	private static final String INDEX_KEY = "index_key";
 	private static final String INDEX_TYPE = "index_type";
 
 	protected static final String INDEX_VALUES = "index_values";
 	
 	private final Node underlyingNode;
 	private BTree bTree;
 	private String name;
 	private NeoService neo;
 
 	protected abstract String getIndexType();
 	protected abstract void addOrReplace( KeyEntry entry, long value );
 	protected abstract void addOrReplace( Node entryNode, long value );
 	protected abstract boolean removeAllOrOne( KeyEntry entry, long value );
 	protected abstract boolean removeAllOrOne( Node entry, long value );
 	protected abstract long[] getValues( KeyEntry entry );
 	protected abstract long[] getValues( Node entry );
 	protected abstract long getSingleValue( KeyEntry entry );
 	protected abstract long getSingleValue( Node entry );
 	
 	/**
 	 * Creates/loads a index. The <CODE>underlyingNode</CODE> can either
 	 * be a new (just created) node or a node that already represents a 
 	 * previously created index.
 	 *
	 * @param name The unique name of the index
 	 * @param underlyingNode The underlying node representing the index
 	 * @param neo The embedded neo instance
 	 * @throws IllegalArgumentException if the underlying node is a index with
 	 * a different name set or wrong index type.
 	 */
 	public AbstractIndex( String name, Node underlyingNode, NeoService neo )
 	{
 		if ( underlyingNode == null || neo == null )
 		{
 			throw new IllegalArgumentException( 
 				"Null parameter underlyingNode=" + underlyingNode +
 				" neo=" + neo );
 		}
 		this.underlyingNode = underlyingNode;
 		this.neo = neo;
 		Transaction tx = neo.beginTx();
 		try
 		{
 			if ( underlyingNode.hasProperty( INDEX_NAME ) )
 			{
 				String storedName = (String) underlyingNode.getProperty( 
 					INDEX_NAME );
 				if ( !storedName.equals( name ) )
 				{
 					throw new IllegalArgumentException( "Name of index " + 
 						"for node=" + underlyingNode.getId() + "," + 
 						storedName + " is not same as passed in name=" + 
 						name );
 				}
 				if ( !getIndexType().equals( underlyingNode.getProperty( 
 					INDEX_TYPE ) ) )
 				{
 					throw new IllegalArgumentException( "This index is " + 
 						"not a " + getIndexType() + " value index" );
 				}
 				this.name = name;
 			}
 			else
 			{
 				underlyingNode.setProperty( INDEX_NAME, name );
 				underlyingNode.setProperty( INDEX_TYPE, getIndexType() );
 				this.name = name;
 			}
 			Relationship bTreeRel = underlyingNode.getSingleRelationship( 
 				org.neo4j.util.btree.BTree.RelTypes.TREE_ROOT, 
 				Direction.OUTGOING );
 			if ( bTreeRel != null )
 			{
 				bTree = new BTree( neo, bTreeRel.getEndNode() );
 			}
 			else
 			{
 				Node bTreeNode = neo.createNode();
 				underlyingNode.createRelationshipTo( bTreeNode, 
 					org.neo4j.util.btree.BTree.RelTypes.TREE_ROOT );
 				bTree = new BTree( neo, bTreeNode );
 			}
 			tx.success();
 		}
 		finally
 		{
 			tx.finish();
 		}
 	}
 	
 	public String getName()
 	{
 		return this.name;
 	}
 	
 	protected Node getUnderlyingNode()
 	{
 		return underlyingNode;
 	}
 	
 	/**
 	 * Creates a index mapping for <CODE>indexKey</CODE> to 
 	 * <CODE>nodeToIndex</CODE>. This multi index implementation does  
 	 * handle multiple nodes for the same key. It also compares keys using the 
 	 * <CODE>equals</CODE> method.
 	 * 
 	 * @param nodeToIndex the node to index with the specified key
 	 * @param indexKey the key
 	 */
 	public void index( Node nodeToIndex, Object indexKey )
 	{
 		if ( indexKey == null ) 
 		{
 			throw new IllegalArgumentException( "Null index key" );
 		}
 		if ( nodeToIndex == null )
 		{
 			throw new IllegalArgumentException( "Null node" );
 		}
 		Transaction tx = neo.beginTx();
 		try
 		{
 			int hashCode = indexKey.hashCode();
 			long value = nodeToIndex.getId();
 			KeyEntry entry = bTree.addIfAbsent( hashCode, value );
 			if ( entry != null )
 			{
 				entry.setKeyValue( indexKey );
 			}
 			else
 			{
 				entry = bTree.getAsKeyEntry( hashCode );
 				Object goOtherNode = entry.getKeyValue();
 				Node bucketNode = null;
 				if ( !goOtherNode.equals( GOTO_NODE ) )
 				{
 					Object prevValue = entry.getValue();
 					Object prevKey = entry.getKeyValue();
 					if ( prevKey.equals( indexKey ) )
 					{
 						addOrReplace( entry, value );
 						tx.success();
 						return;
 					}
 					entry.setKeyValue( GOTO_NODE );
 					bucketNode = neo.createNode();
 					entry.setValue( bucketNode.getId() );
 					Node prevEntry = neo.createNode();
 					bucketNode.createRelationshipTo( prevEntry, 
 						RelTypes.INDEX_ENTRY );
 					prevEntry.setProperty( INDEX_KEY, prevKey );
 					prevEntry.setProperty( INDEX_VALUES, prevValue );
 					Node newEntry = neo.createNode();
 					bucketNode.createRelationshipTo( newEntry, 
 						RelTypes.INDEX_ENTRY );
 					newEntry.setProperty( INDEX_KEY, indexKey );
 					newEntry.setProperty( INDEX_VALUES, value );
 				}
 				else
 				{
 					bucketNode = neo.getNodeById( (Long) entry.getValue() );
 					for ( Relationship rel : bucketNode.getRelationships( 
 						RelTypes.INDEX_ENTRY, Direction.OUTGOING ) )
 					{
 						Node entryNode = rel.getEndNode();
 						if ( entryNode.getProperty( INDEX_KEY ).equals( 
 							indexKey ) )
 						{
 							addOrReplace( entryNode, value );
 							tx.success();
 							return;
 						}
 					}
 					Node newEntry = neo.createNode();
 					bucketNode.createRelationshipTo( newEntry, 
 						RelTypes.INDEX_ENTRY );
 					newEntry.setProperty( INDEX_KEY, indexKey );
 					newEntry.setProperty( INDEX_VALUES, value );
 				}
 			}
 			tx.success();
 		}
 		finally
 		{
 			tx.finish();
 		}
 	}
 	/**
 	 * Removes a index mapping between a node and a key. If the specified 
 	 * mapping doesn't exist this method will quietly return.
 	 * 
 	 * @param nodeToRemove the node that is mapped to the index key
 	 * @param indexKey the index key
 	 */
 	public void remove( Node nodeToRemove, Object indexKey )
 	{
 		if ( indexKey == null ) 
 		{
 			throw new IllegalArgumentException( "Null index key" );
 		}
 		if ( nodeToRemove == null )
 		{
 			throw new IllegalArgumentException( "Null node" );
 		}
 		Transaction tx = neo.beginTx();
 		try
 		{
 			int hashCode = indexKey.hashCode();
 			KeyEntry entry = bTree.getAsKeyEntry( hashCode );
 			if ( entry != null )
 			{
 				Object goOtherNode = entry.getKeyValue();
 				if ( !goOtherNode.equals( GOTO_NODE ) )
 				{
 					if ( goOtherNode.equals( indexKey ) )
 					{
 						if ( removeAllOrOne( entry, nodeToRemove.getId() ) )
 						{
 							entry.remove();
 						}
 					}
 				}
 				else
 				{
 					Node bucketNode = neo.getNodeById( 
 						(Long) entry.getValue() );
 					for ( Relationship rel : bucketNode.getRelationships( 
 						RelTypes.INDEX_ENTRY, Direction.OUTGOING ) )
 					{
 						Node entryNode = rel.getEndNode();
 						if ( entryNode.getProperty( INDEX_KEY ).equals( 
 							indexKey ) )
 						{
 							if ( removeAllOrOne( entryNode, 
 								nodeToRemove.getId() ) )
 							{
 								rel.delete();
 								entryNode.delete();
 							}
 						}
 					}
 				}
 			}
 			tx.success();
 		}
 		finally
 		{
 			tx.finish();
 		}
 	}
 	
 	/**
 	 * Public only for testing purposes. Validates the interal b-tree index and 
 	 * if any error is found a runtime exception is thrown.
 	 */
 	public void validate()
 	{
 		bTree.validateTree();
 	}
 	
 	/**
 	 * Retuns a iterable over all nodes mapped to the specified key or 
 	 * empty iterable if no node is mapped to the specified key.
 	 * 
 	 * @param indexKey the key
 	 * @return the node mapped to the key
 	 */
 	public IndexHits<Node> getNodesFor( Object indexKey )
 	{
 		if ( indexKey == null ) 
 		{
 			throw new IllegalArgumentException( "Null index key" );
 		}
 		Transaction tx = neo.beginTx();
 		try
 		{
 			int hashCode = indexKey.hashCode();
 			KeyEntry entry = bTree.getAsKeyEntry( hashCode );
 			if ( entry != null )
 			{
 				Object goOtherNode = entry.getKeyValue();
 				if ( !goOtherNode.equals( GOTO_NODE ) )
 				{
 					if ( goOtherNode.equals( indexKey ) )
 					{
 						long[] nodeIds = getValues( entry );
 						Node[] nodes = new Node[nodeIds.length];
 						for ( int i = 0; i < nodeIds.length; i++ )
 						{
 							nodes[i] = neo.getNodeById( nodeIds[i] );
 						}
 						tx.success();
 						return new SimpleIndexHits<Node>(
 						    Arrays.asList( nodes ), nodes.length );
 					}
 				}
 				else
 				{
 					Node bucketNode = neo.getNodeById( 
 						(Long) entry.getValue() );
 					for ( Relationship rel : bucketNode.getRelationships( 
 						RelTypes.INDEX_ENTRY, Direction.OUTGOING ) )
 					{
 						Node entryNode = rel.getEndNode();
 						if ( entryNode.getProperty( INDEX_KEY ).equals( 
 							indexKey ) )
 						{
 							long[] nodeIds = getValues( entryNode );
 							Node[] nodes = new Node[nodeIds.length];
 							for ( int i = 0; i < nodeIds.length; i++ )
 							{
 								nodes[i] = neo.getNodeById( nodeIds[i] );
 							}
 							tx.success();
 							return new SimpleIndexHits<Node>(
 							    Arrays.asList( nodes ), nodes.length );
 						}
 					}
 				}
 			}
 			return new SimpleIndexHits<Node>(
 			    Collections.<Node>emptyList(), 0 );
 		}
 		finally
 		{
 			tx.finish();
 		}
 	}
 	
 	public Node getSingleNodeFor( Object indexKey )
     {
 		if ( indexKey == null ) 
 		{
 			throw new IllegalArgumentException( "Null index key" );
 		}
 		Transaction tx = neo.beginTx();
 		try
 		{
 			int hashCode = indexKey.hashCode();
 			KeyEntry entry = bTree.getAsKeyEntry( hashCode );
 			if ( entry != null )
 			{
 				Object goOtherNode = entry.getKeyValue();
 				if ( !goOtherNode.equals( GOTO_NODE ) )
 				{
 					if ( goOtherNode.equals( indexKey ) )
 					{
 						long nodeId = getSingleValue( entry );
 						Node node = neo.getNodeById( nodeId );
 						tx.success();
 						return node;
 					}
 				}
 				else
 				{
 					Node bucketNode = neo.getNodeById( 
 						(Long) entry.getValue() );
 					for ( Relationship rel : bucketNode.getRelationships( 
 						RelTypes.INDEX_ENTRY, Direction.OUTGOING ) )
 					{
 						Node entryNode = rel.getEndNode();
 						if ( entryNode.getProperty( INDEX_KEY ).equals( 
 							indexKey ) )
 						{
 							long nodeId = getSingleValue( entryNode );
 							Node node = neo.getNodeById( nodeId );
 							tx.success();
 							return node;
 						}
 					}
 				}
 			}
 			return null;
 		}
 		finally
 		{
 			tx.finish();
 		}
     }
 	
 	/**
 	 * Deletes this index in a single (or the current) transaction including 
 	 * the underlying node.
 	 */
 	public void drop()
 	{
 		for ( KeyEntry entry : bTree.entries() )
 		{
 			Object goOtherNode = entry.getKeyValue();
 			if ( goOtherNode.equals( GOTO_NODE ) )
 			{
 				Node bucketNode = neo.getNodeById( 
 					(Long) entry.getValue() );
 				for ( Relationship rel : bucketNode.getRelationships( 
 					RelTypes.INDEX_ENTRY, Direction.OUTGOING ) )
 				{
 					Node entryNode = rel.getEndNode();
 					rel.delete();
 					entryNode.delete();
 				}
 				bucketNode.delete();
 			}
 		}
 		bTree.delete();
 		underlyingNode.delete();
 	}
 
     /**
      * Deletes this index in a single (or the current) transaction including 
      * the underlying node.
      */
     public void clear()
     {
         for ( KeyEntry entry : bTree.entries() )
         {
             Object goOtherNode = entry.getKeyValue();
             if ( goOtherNode.equals( GOTO_NODE ) )
             {
                 Node bucketNode = neo.getNodeById( 
                     (Long) entry.getValue() );
                 for ( Relationship rel : bucketNode.getRelationships( 
                     RelTypes.INDEX_ENTRY, Direction.OUTGOING ) )
                 {
                     Node entryNode = rel.getEndNode();
                     rel.delete();
                     entryNode.delete();
                 }
                 bucketNode.delete();
             }
         }
         bTree.delete();
         Node bTreeNode = neo.createNode();
         underlyingNode.createRelationshipTo( bTreeNode, 
             org.neo4j.util.btree.BTree.RelTypes.TREE_ROOT );
         bTree = new BTree( neo, bTreeNode );
     }
     
 	/**
 	 * Deletes this index using a commit interval including the underlying node.
 	 * 
 	 * @parram commitInterval number of index removes before a new transaction
 	 * is started.
 	 */
 	public void drop( int commitInterval )
 	{
         int count = 0;
 		for ( KeyEntry entry : bTree.entries() )
 		{
 			Object goOtherNode = entry.getKeyValue();
 			if ( goOtherNode.equals( GOTO_NODE ) )
 			{
 				Node bucketNode = neo.getNodeById( 
 					(Long) entry.getValue() );
 				for ( Relationship rel : bucketNode.getRelationships( 
 					RelTypes.INDEX_ENTRY, Direction.OUTGOING ) )
 				{
 					Node entryNode = rel.getEndNode();
 					rel.delete();
 					entryNode.delete();
 				}
 				bucketNode.delete();
 			}
             count++;
             if ( count >= commitInterval )
             {
                 try
                 {
                     ((EmbeddedNeo) neo).getConfig().getTxModule().
                         getTxManager().getTransaction().commit();
                 }
                 catch ( Exception e )
                 {
                     throw new RuntimeException( e );
                 }
                 neo.beginTx();
                 count = 0;
             }
 		}
 		bTree.delete( commitInterval );
 		underlyingNode.delete();
 	}
 
 	/**
 	 * Not supported yet, throws <CODE>UnsupportedOperationException</CODE>.
 	 */
 	public Iterable<Node> values()
     {
         return new IndexIterator( this, bTree, neo );
     }
     
     private static class IndexIterator implements Iterable<Node>,
         Iterator<Node>
     {
         private Iterator<Node> currentNodes;
         private final Iterator<KeyEntry> bTreeIterator;
         private final NeoService neo;
         private final AbstractIndex index;
         
         private IndexIterator( AbstractIndex index, BTree bTree, 
             NeoService neo )
         {
             this.index = index;
             this.bTreeIterator = bTree.entries().iterator();
             this.neo = neo;
         }
         
         public boolean hasNext()
         {
             if ( currentNodes != null && currentNodes.hasNext() )
             {
                 return true;
             }
             Transaction tx = neo.beginTx();
             try
             {
                 if ( bTreeIterator.hasNext() )
                 {
                     nextKeyEntry();
                     return hasNext();
                 }
             }
             finally
             {
                 tx.finish();
             }
             return false;
         }
         
         private void nextKeyEntry()
         {
             KeyEntry entry = bTreeIterator.next();
             Object goOtherNode = entry.getKeyValue();
             if ( !goOtherNode.equals( GOTO_NODE ) )
             {
                 long[] nodeIds = index.getValues( entry );
                 Node[] nodes = new Node[nodeIds.length];
                 for ( int i = 0; i < nodeIds.length; i++ )
                 {
                     nodes[i] = neo.getNodeById( nodeIds[i] );
                 }
                 currentNodes = Arrays.asList( nodes ).iterator();
             }
             else
             {
                 Node bucketNode = neo.getNodeById( 
                     (Long) entry.getValue() );
                 List<Node> nodeList = new ArrayList<Node>();
                 for ( Relationship rel : bucketNode.getRelationships( 
                     RelTypes.INDEX_ENTRY, Direction.OUTGOING ) )
                 {
                     Node entryNode = rel.getEndNode();
                     long[] nodeIds = index.getValues( entryNode );
                     Node[] nodes = new Node[nodeIds.length];
                     for ( int i = 0; i < nodeIds.length; i++ )
                     {
                         nodes[i] = neo.getNodeById( nodeIds[i] );
                     }
                     nodeList.addAll( Arrays.asList( nodes ) );
                 }
                 currentNodes = nodeList.iterator();
             }
         }
 
         public Node next()
         {
             if ( currentNodes == null || !currentNodes.hasNext() )
             {
                 nextKeyEntry();
             }
             return currentNodes.next();
         }
 
         public void remove()
         {
             throw new UnsupportedOperationException();
         }
 
         public Iterator<Node> iterator()
         {
             return this;
         }
     }
 }
