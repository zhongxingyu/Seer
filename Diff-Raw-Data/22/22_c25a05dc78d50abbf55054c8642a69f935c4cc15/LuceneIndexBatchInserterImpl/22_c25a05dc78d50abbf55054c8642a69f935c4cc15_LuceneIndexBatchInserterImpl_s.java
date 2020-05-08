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
 
 import java.io.File;
 import java.io.IOException;
 import java.io.Reader;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.analysis.LowerCaseFilter;
 import org.apache.lucene.analysis.TokenStream;
 import org.apache.lucene.analysis.WhitespaceTokenizer;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Field;
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.index.Term;
 import org.apache.lucene.index.IndexWriter.MaxFieldLength;
 import org.apache.lucene.search.Hits;
 import org.apache.lucene.search.IndexSearcher;
 import org.apache.lucene.search.Query;
 import org.apache.lucene.search.TermQuery;
 import org.apache.lucene.store.Directory;
 import org.apache.lucene.store.FSDirectory;
 import org.neo4j.commons.iterator.IterableWrapper;
 import org.neo4j.graphdb.Node;
 import org.neo4j.index.IndexHits;
 import org.neo4j.index.IndexService;
 import org.neo4j.index.Isolation;
 import org.neo4j.index.impl.SimpleIndexHits;
 import org.neo4j.kernel.impl.batchinsert.BatchInserter;
 import org.neo4j.kernel.impl.util.ArrayMap;
 import org.neo4j.kernel.impl.util.FileUtils;
 
 /**
  * The implementation of {@link LuceneIndexBatchInserter}.
  */
 public class LuceneIndexBatchInserterImpl implements LuceneIndexBatchInserter
 {
     private final String storeDir;
     private final BatchInserter inserter;
 
     private final ArrayMap<String,IndexWriterContext> indexWriters = 
         new ArrayMap<String,IndexWriterContext>( 6, false, false );
     private final ArrayMap<String,IndexSearcher> indexSearchers = 
         new ArrayMap<String,IndexSearcher>( 6, false, false );
 
     private final Analyzer fieldAnalyzer = new Analyzer()
     {
         @Override
         public TokenStream tokenStream( String fieldName, Reader reader )
         {
             return new LowerCaseFilter( new WhitespaceTokenizer( reader ) );
         }
     };
     
     private IndexService asIndexService;
     
     /**
      * @param inserter the {@link BatchInserter} to use.
      */
     public LuceneIndexBatchInserterImpl( BatchInserter inserter )
     {
         this.inserter = inserter;
         this.storeDir = fixPath( inserter.getStore() + "/" + getDirName() );
         this.asIndexService = new AsIndexService();
     }
     
     protected String getDirName()
     {
         return LuceneIndexService.DIR_NAME;
     }
     
     private String fixPath( String dir )
     {
         String store = FileUtils.fixSeparatorsInPath( dir );
         File directories = new File( dir );
         if ( !directories.exists() )
         {
             if ( !directories.mkdirs() )
             {
                 throw new RuntimeException( "Unable to create directory path["
                     + storeDir + "] for Lucene index store." );
             }
         }
         return store;
     }
     
     private Directory instantiateDirectory( String key ) throws IOException
     {
         return FSDirectory.open( new File( storeDir + "/" + key ) );
     }
     
     private IndexWriterContext getWriter( String key, boolean allowCreate )
     {
         IndexWriterContext writer = indexWriters.get( key );
        if ( writer == null && allowCreate )
         {
             try
             {
                 Directory dir = instantiateDirectory( key );
                 IndexWriter indexWriter = new IndexWriter( dir, fieldAnalyzer,
                     MaxFieldLength.UNLIMITED );
                 
                 // TODO We should tamper with this value and see how it affects
                 // the general performance. Lucene docs says rather >10 for
                 // batch inserts
 //                indexWriter.setMergeFactor( 15 );
                 writer = new IndexWriterContext( indexWriter );
             }
             catch ( IOException e )
             {
                 throw new RuntimeException( e );
             }
             indexWriters.put( key, writer );
         }
         return writer;
     }
     
     private IndexSearcher getSearcher( String key )
     {
         IndexWriterContext writer = getWriter( key, false );
         if ( writer == null )
         {
             return null;
         }
         
         try
         {
             IndexSearcher oldSearcher = indexSearchers.get( key );
             IndexSearcher result = oldSearcher;
             if ( oldSearcher == null || writer.modifiedFlag )
             {
                 if ( oldSearcher != null )
                 {
                     oldSearcher.getIndexReader().close();
                     oldSearcher.close();
                 }
                 IndexReader newReader = writer.writer.getReader();
                 result = new IndexSearcher( newReader );
                 indexSearchers.put( key, result );
                 writer.modifiedFlag = false;
             }
             return result;
         }
         catch ( IOException e )
         {
             throw new RuntimeException( e );
         }
     }
     
     public void index( long node, String key, Object value )
     {
         IndexWriterContext writer = getWriter( key, true );
         Document document = new Document();
         fillDocument( document, node, key, value );
         try
         {
             writer.writer.addDocument( document );
             writer.modifiedFlag = true;
         }
         catch ( IOException e )
         {
             throw new RuntimeException( e );
         }
     }
     
     protected void fillDocument( Document document, long nodeId, String key,
         Object value )
     {
         document.add( new Field( LuceneIndexService.DOC_ID_KEY,
             String.valueOf( nodeId ), Field.Store.YES,
             Field.Index.NOT_ANALYZED ) );
         document.add( new Field( LuceneIndexService.DOC_INDEX_KEY,
             value.toString(), Field.Store.NO, getIndexStrategy() ) );
     }
     
     protected Field.Index getIndexStrategy()
     {
         return Field.Index.NOT_ANALYZED;
     }
     
     public void shutdown()
     {
         try
         {
             for ( IndexSearcher searcher : indexSearchers.values() )
             {
                 searcher.close();
             }
             indexSearchers.clear();
             optimize();
             for ( IndexWriterContext writer : indexWriters.values() )
             {
                 writer.writer.close();
             }
             indexWriters.clear();
         }
         catch ( IOException e )
         {
             throw new RuntimeException( e );
         }
     }
 
     public IndexHits<Long> getNodes( String key, Object value )
     {
         Set<Long> nodeSet = new HashSet<Long>();
         try
         {
             Query query = formQuery( key, value );
             IndexSearcher searcher = getSearcher( key );
             if ( searcher == null )
             {
                 return new SimpleIndexHits<Long>(
                     Collections.<Long>emptyList(), 0 );
             }
             Hits hits = searcher.search( query );
             for ( int i = 0; i < hits.length(); i++ )
             {
                 Document document = hits.doc( i );
                 long id = Long.parseLong( document.getField(
                     LuceneIndexService.DOC_ID_KEY ).stringValue() );
                 nodeSet.add( id );
             }
         }
         catch ( IOException e )
         {
             throw new RuntimeException( e );
         }
         return new SimpleIndexHits<Long>( nodeSet, nodeSet.size() );
     }
     
     protected Query formQuery( String key, Object value )
     {
         return new TermQuery( new Term( LuceneIndexService.DOC_INDEX_KEY, 
             value.toString() ) );
     }
     
     public void optimize()
     {
         try
         {
             for ( IndexWriterContext writer : indexWriters.values() )
             {
                 writer.writer.optimize( true );
                 writer.modifiedFlag = true;
             }
         }
         catch ( IOException e )
         {
             throw new RuntimeException( e );
         }
     }
 
     public long getSingleNode( String key, Object value )
     {
         Iterator<Long> nodes = getNodes( key, value ).iterator();
         long node = nodes.hasNext() ? nodes.next() : -1;
         if ( nodes.hasNext() )
         {
             throw new RuntimeException( "More than one node for " +
                 key + "=" + value );
         }
         return node;
     }
 
     public IndexService getIndexService()
     {
         return asIndexService;
     }
     
     private class AsIndexService implements IndexService
     {
         public IndexHits<Node> getNodes( String key, Object value )
         {
             IndexHits<Long> ids = LuceneIndexBatchInserterImpl.this.getNodes(
                 key, value );
             Iterable<Node> nodes = new IterableWrapper<Node, Long>( ids )
             {
                 @Override
                 protected Node underlyingObjectToObject( Long id )
                 {
                     return inserter.getGraphDbService().getNodeById( id );
                 }
             };
             return new SimpleIndexHits<Node>( nodes, ids.size() );
         }
 
         public Node getSingleNode( String key, Object value )
         {
             long id =
                 LuceneIndexBatchInserterImpl.this.getSingleNode( key, value );
             return id == -1 ? null : inserter.getGraphDbService().getNodeById( id );
         }
 
         public void index( Node node, String key, Object value )
         {
             LuceneIndexBatchInserterImpl.this.index( node.getId(), key, value );
         }
 
         public void removeIndex( Node node, String key, Object value )
         {
             throw new UnsupportedOperationException();
         }
 
         public void removeIndex( Node node, String key )
         {
             throw new UnsupportedOperationException();
         }
         
         public void removeIndex( String key )
         {
             throw new UnsupportedOperationException();
         }
         
         public void setIsolation( Isolation level )
         {
             throw new UnsupportedOperationException();
         }
 
         public void shutdown()
         {
             LuceneIndexBatchInserterImpl.this.shutdown();
         }
     }
     
     private static class IndexWriterContext
     {
         private final IndexWriter writer;
         private boolean modifiedFlag;
         
         IndexWriterContext( IndexWriter writer )
         {
             this.writer = writer;
             this.modifiedFlag = true;
         }
     }
 }
