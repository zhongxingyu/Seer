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
 import org.apache.lucene.search.IndexSearcher;
 import org.neo4j.api.core.Node;
 import org.neo4j.impl.transaction.xaframework.XaCommand;
 import org.neo4j.impl.transaction.xaframework.XaLogicalLog;
 import org.neo4j.impl.transaction.xaframework.XaTransaction;
 import org.neo4j.util.index.LuceneCommand.AddCommand;
 import org.neo4j.util.index.LuceneCommand.RemoveCommand;
 
 class LuceneTransaction extends XaTransaction
 {
     private final Map<String,Map<Object,Set<Long>>> txIndexed = 
         new HashMap<String,Map<Object,Set<Long>>>();
     private final Map<String,Map<Object,Set<Long>>> txRemoved = 
         new HashMap<String,Map<Object,Set<Long>>>();
 
     private final LuceneDataSource luceneDs;
 
     private final Map<String,List<AddCommand>> addCommandMap = 
         new HashMap<String,List<AddCommand>>();
     private final Map<String,List<RemoveCommand>> removeCommandMap = 
         new HashMap<String,List<RemoveCommand>>();
 
     LuceneTransaction( int identifier, XaLogicalLog xaLog,
         LuceneDataSource luceneDs )
     {
         super( identifier, xaLog );
         this.luceneDs = luceneDs;
     }
 
     void index( Node node, String key, Object value )
     {
         insert( node, key, value, txRemoved, txIndexed );
     }
 
     void removeIndex( Node node, String key, Object value )
     {
         insert( node, key, value, txIndexed, txRemoved );
     }
 
     private void insert( Node node, String key, Object value,
         Map<String,Map<Object,Set<Long>>> toRemoveFrom,
         Map<String,Map<Object,Set<Long>>> toInsertInto )
     {
         delFromIndex( node, key, value, toRemoveFrom );
         Map<Object,Set<Long>> keyIndex = toInsertInto.get( key );
         if ( keyIndex == null )
         {
             keyIndex = new HashMap<Object,Set<Long>>();
             toInsertInto.put( key, keyIndex );
         }
         Set<Long> nodeIds = keyIndex.get( value );
         if ( nodeIds == null )
         {
             nodeIds = new HashSet<Long>();
         }
         nodeIds.add( node.getId() );
         keyIndex.put( value, nodeIds );
     }
 
     private boolean delFromIndex( Node node, String key, Object value,
         Map<String,Map<Object,Set<Long>>> map )
     {
         Map<Object,Set<Long>> keyIndex = map.get( key );
         if ( keyIndex == null )
         {
             return false;
         }
         Set<Long> nodeIds = keyIndex.get( value );
         if ( nodeIds != null )
         {
             return nodeIds.remove( node.getId() );
         }
         return false;
     }
 
     Set<Long> getDeletedNodesFor( String key, Object value )
     {
         Map<Object,Set<Long>> keyIndex = txRemoved.get( key );
         if ( keyIndex != null )
         {
             Set<Long> nodeIds = keyIndex.get( value );
             if ( nodeIds != null )
             {
                 return nodeIds;
             }
         }
         return Collections.emptySet();
     }
 
     Set<Long> getNodesFor( String key, Object value )
     {
         Map<Object,Set<Long>> keyIndex = txIndexed.get( key );
         if ( keyIndex != null )
         {
             Set<Long> nodeIds = keyIndex.get( value );
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
     {
         // command added either through addCommand or injectCommand
         if ( command instanceof AddCommand )
         {
             AddCommand addCommand = (AddCommand) command;
             List<AddCommand> list = addCommandMap.get( addCommand.getKey() );
             if ( list == null )
             {
                 list = new ArrayList<AddCommand>();
                 addCommandMap.put( addCommand.getKey(), list );
             }
             list.add( addCommand );
         }
         else if ( command instanceof RemoveCommand )
         {
             RemoveCommand removeCommand = (RemoveCommand) command;
             List<RemoveCommand> list = removeCommandMap.get( removeCommand
                 .getKey() );
             if ( list == null )
             {
                 list = new ArrayList<RemoveCommand>();
                 removeCommandMap.put( removeCommand.getKey(), list );
             }
             list.add( removeCommand );
         }
         else
         {
            throw new RuntimeException( "Unkown command: " + command );
         }
     }
 
     @Override
     protected void doCommit()
     {
         for ( String key : removeCommandMap.keySet() )
         {
             IndexSearcher searcher = luceneDs.acquireIndexSearcher( key );
             try
             {
                 if ( searcher != null ) // meaning such a index exist
                 {
                     List<RemoveCommand> commands = removeCommandMap.get( key );
                     for ( RemoveCommand cmd : commands )
                     {
                         String value = cmd.getValue();
                         long id = cmd.getNodeId();
                         luceneDs.deleteDocumentUsingReader( searcher, id, 
                             value );
                         luceneDs.invalidateCache( key, value );
                     }
                 }
             }
             finally
             {
                 luceneDs.releaseIndexSearcher( key, searcher );
                 luceneDs.removeIndexSearcher( key );
             }
         }
         for ( String key : addCommandMap.keySet() )
         {
             IndexWriter writer = luceneDs.getIndexWriter( key );
             try
             {
                 List<AddCommand> commands = addCommandMap.get( key );
                 for ( AddCommand cmd : commands )
                 {
                     indexWriter( writer, cmd.getNodeId(), key, cmd.getValue() );
                     luceneDs.invalidateCache( key, cmd.getValue() );
                 }
             }
             catch ( Exception e )
             {
                 throw new RuntimeException( "Unable to update lucene index", 
                     e );
             }
             finally
             {
                 luceneDs.releaseAndRemoveWriter( key, writer );
                 luceneDs.removeIndexSearcher( key );
             }
         }
     }
 
     @Override
     protected void doPrepare()
     {
         for ( String key : txIndexed.keySet() )
         {
             Map<Object,Set<Long>> addIndex = txIndexed.get( key );
             for ( Object object : addIndex.keySet() )
             {
                 for ( long id : addIndex.get( object ) )
                 {
                     addCommand( new AddCommand( id, key, object.toString() ) );
                 }
             }
         }
         for ( String key : txRemoved.keySet() )
         {
             Map<Object,Set<Long>> removeIndex = txRemoved.get( key );
             for ( Object object : removeIndex.keySet() )
             {
                 for ( long id : removeIndex.get( object ) )
                 {
                     addCommand( new RemoveCommand( id, key, 
                         object.toString() ) );
                 }
             }
         }
     }
 
     @Override
     protected void doRollback()
     {
         // TODO Auto-generated method stub
         addCommandMap.clear();
         removeCommandMap.clear();
         txIndexed.clear();
         txRemoved.clear();
     }
 
     @Override
     public boolean isReadOnly()
     {
         return false;
     }
 }
