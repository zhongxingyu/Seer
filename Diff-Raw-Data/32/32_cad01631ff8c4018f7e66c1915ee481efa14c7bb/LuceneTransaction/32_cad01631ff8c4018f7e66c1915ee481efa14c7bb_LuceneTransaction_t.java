 /*
  * Copyright (c) 2002-2009 "Neo Technology,"
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
 package org.neo4j.index.lucene;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.lucene.document.Document;
 import org.apache.lucene.index.IndexWriter;
 import org.neo4j.graphdb.Node;
 import org.neo4j.index.lucene.LuceneCommand.AddCommand;
 import org.neo4j.index.lucene.LuceneCommand.RemoveCommand;
 import org.neo4j.kernel.impl.transaction.xaframework.XaCommand;
 import org.neo4j.kernel.impl.transaction.xaframework.XaLogicalLog;
 import org.neo4j.kernel.impl.transaction.xaframework.XaTransaction;
 
 class LuceneTransaction extends XaTransaction
 {
     private final Map<String,TxCache> txIndexed = new HashMap<String,TxCache>();
     private final Map<String,TxCache> txRemoved = new HashMap<String,TxCache>();
 
     private final LuceneDataSource luceneDs;
 
     private final Map<String,List<LuceneCommand>> commandMap = 
         new HashMap<String,List<LuceneCommand>>();
 
     LuceneTransaction( int identifier, XaLogicalLog xaLog,
         LuceneDataSource luceneDs )
     {
         super( identifier, xaLog );
         this.luceneDs = luceneDs;
     }
 
     void index( Node node, String key, Object value )
     {
         insert( node, key, value, txRemoved, txIndexed );
         queueCommand( new AddCommand( node.getId(), key, value.toString() ) );
     }
 
     void removeIndex( Node node, String key, Object value )
     {
         insert( node, key, value, txIndexed, txRemoved );
         queueCommand( new RemoveCommand( node != null ? node.getId() : null,
             key, value != null ? value.toString() : null ) );
     }
 
     private void queueCommand( LuceneCommand command )
     {
         String key = command.getKey();
         List<LuceneCommand> commands = commandMap.get( key );
         if ( commands == null )
         {
             commands = new ArrayList<LuceneCommand>();
             commandMap.put( key, commands );
         }
         commands.add( command );
     }
     
     private void insert( Node node, String key, Object value,
         Map<String,TxCache> toRemoveFrom, Map<String,TxCache> toInsertInto )
     {
         delFromIndex( node, key, value, toRemoveFrom );
         
         TxCache keyIndex = toInsertInto.get( key );
         if ( keyIndex == null )
         {
             keyIndex = new TxCache();
             toInsertInto.put( key, keyIndex );
         }
         keyIndex.add( node != null ? node.getId() : null, value );
     }
 
     private boolean delFromIndex( Node node, String key, Object value,
         Map<String,TxCache> map )
     {
         TxCache keyIndex = map.get( key );
         if ( keyIndex == null )
         {
             return false;
         }
         
         boolean modified = false;
         if ( node != null )
         {
             Long nodeId = node.getId();
             if ( value != null )
             {
                 keyIndex.remove( nodeId, value );
             }
             else
             {
                 keyIndex.remove( nodeId );
             }
         }
         else
         {
             keyIndex.remove();
         }
         return modified;
     }
 
     Set<Long> getDeletedNodesFor( String key, Object value )
     {
         TxCache keyIndex = txRemoved.get( key );
         LazyMergedSet<Long> result = null;
         if ( keyIndex != null )
         {
             result = new LazyMergedSet<Long>();
             result.add( keyIndex.map.get( value ) );
             // the 'null' value represents those removed with
             // removeIndex( Node, String )
             result.add( keyIndex.map.get( null ) );
         }
         return result != null && result.get() != null ? result.get() :
                 Collections.<Long>emptySet();
     }
     
     boolean getIndexDeleted( String key )
     {
         TxCache keyIndex = txRemoved.get( key );
         return keyIndex != null ? keyIndex.all : false;
     }
     
     Set<Long> getNodesFor( String key, Object value )
     {
         TxCache keyIndex = txIndexed.get( key );
         if ( keyIndex != null )
         {
             Set<Long> nodeIds = keyIndex.map.get( value );
             if ( nodeIds != null )
             {
                 return nodeIds;
             }
         }
         return Collections.emptySet();
     }
     
     protected LuceneDataSource getDataSource()
     {
         return this.luceneDs;
     }
     
     private void indexWriter( IndexWriter writer, long nodeId, String key,
         Object value )
     {
         Document document = new Document();
         this.luceneDs.fillDocument( document, nodeId, key, value );
         try
         {
             writer.addDocument( document );
         }
         catch ( IOException e )
         {
             throw new RuntimeException( e );
         }
     }
 
     @Override
     protected void doAddCommand( XaCommand command )
    { // we override inject command and manage our own in memory command list
    }
    
    @Override
    protected void injectCommand( XaCommand command )
     {
         queueCommand( ( LuceneCommand ) command );
     }
 
     @Override
     protected void doCommit()
     {
         luceneDs.getWriteLock();
         try
         {
             for ( Map.Entry<String, List<LuceneCommand>> entry :
                 this.commandMap.entrySet() )
             {
                 String key = entry.getKey();
                 IndexWriter writer = luceneDs.getIndexWriter( key );
                 for ( LuceneCommand command : entry.getValue() )
                 {
                     if ( command instanceof AddCommand )
                     {
                         indexWriter( writer, command.getNodeId(), key,
                             command.getValue() );
                     }
                     else if ( command instanceof RemoveCommand )
                     {
                         luceneDs.deleteDocumentsUsingWriter(
                             writer, command.getNodeId(), command.getValue() );
                     }
                     else
                     {
                         throw new RuntimeException( "Unknown command type " +
                             command + ", " + command.getClass() );
                     }
                     luceneDs.invalidateCache( key, command.getValue() );
                 }
                 luceneDs.removeWriter( key, writer );
                 luceneDs.invalidateIndexSearcher( key );
             }
         }
         finally
         {
             luceneDs.releaseWriteLock();
         }
     }
 
     @Override
     protected void doPrepare()
     {
        for ( Map.Entry<String, List<LuceneCommand>> entry :
            commandMap.entrySet() )
         {
            for ( LuceneCommand command : entry.getValue() )
             {
                addCommand( command );
             }
         }
     }
 
     @Override
     protected void doRollback()
     {
         // TODO Auto-generated method stub
         commandMap.clear();
         txIndexed.clear();
         txRemoved.clear();
     }
 
     @Override
     public boolean isReadOnly()
     {
         return false;
     }
     
     static class LazyMergedSet<T>
     {
         private Set<T> set;
         private int count;
         
         private void add( Set<T> setOrNull )
         {
             if ( setOrNull == null )
             {
                 return;
             }
             
             if ( this.count == 0 )
             {
                 this.set = setOrNull;
             }
             else
             {
                 if ( count == 1 )
                 {
                     this.set = new HashSet<T>( this.set );
                 }
                 this.set.addAll( setOrNull );
             }
             this.count++;
         }
         
         private Set<T> get()
         {
             return this.set;
         }
     }
     
     private static class TxCache
     {
         private final Map<Object, Set<Long>> map =
             new HashMap<Object, Set<Long>>();
         private final Map<Long, Set<Object>> reverseMap =
             new HashMap<Long, Set<Object>>();
         boolean all;
         
         void add( Long nodeId, Object value )
         {
             if ( nodeId == null && value == null )
             {
                 all = true;
                 return;
             }
             
             Set<Long> ids = map.get( value );
             if ( ids == null )
             {
                 ids = new HashSet<Long>();
                 map.put( value, ids );
             }
             ids.add( nodeId );
             
             Set<Object> values = reverseMap.get( nodeId );
             if ( values == null )
             {
                 values = new HashSet<Object>();
                 reverseMap.put( nodeId, values );
             }
             values.add( value );
         }
         
         void remove( Long nodeId, Object value )
         {
             Set<Long> ids = map.get( value );
             if ( ids != null )
             {
                 ids.remove( nodeId );
             }
             
             Set<Object> values = reverseMap.get( nodeId );
             if ( values != null )
             {
                 values.remove( value );
             }
         }
         
         void remove( Long nodeId )
         {
             Set<Object> values = reverseMap.get( nodeId );
             if ( values == null )
             {
                 return;
             }
             
             for ( Object value : values.toArray() )
             {
                 Set<Long> ids = map.get( value );
                 if ( ids != null )
                 {
                     ids.remove( nodeId );
                 }
                 reverseMap.remove( value );
             }
         }
         
         void remove()
         {
             map.clear();
             reverseMap.clear();
         }
         
         Iterable<Long> getNodesForValue( Object value )
         {
             return map.get( value );
         }
     }
 }
