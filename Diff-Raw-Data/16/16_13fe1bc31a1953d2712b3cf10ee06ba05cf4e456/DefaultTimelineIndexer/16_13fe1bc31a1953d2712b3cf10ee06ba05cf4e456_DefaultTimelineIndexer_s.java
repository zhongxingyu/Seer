 /**
  * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
  *
  * This program is licensed to you under the Apache License Version 2.0,
  * and you may not use this file except in compliance with the Apache License Version 2.0.
  * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the Apache License Version 2.0 is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
  */
 package org.sonatype.timeline;
 
 import java.io.IOException;
 import java.text.ParseException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.lucene.analysis.standard.StandardAnalyzer;
 import org.apache.lucene.document.DateTools;
 import org.apache.lucene.document.DateTools.Resolution;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Field;
 import org.apache.lucene.index.ExtendedIndexWriter;
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.index.IndexWriter.MaxFieldLength;
 import org.apache.lucene.index.SerialMergeScheduler;
 import org.apache.lucene.index.Term;
 import org.apache.lucene.search.BooleanClause.Occur;
 import org.apache.lucene.search.BooleanQuery;
 import org.apache.lucene.search.ConstantScoreRangeQuery;
 import org.apache.lucene.search.IndexSearcher;
 import org.apache.lucene.search.Query;
 import org.apache.lucene.search.Sort;
 import org.apache.lucene.search.SortField;
 import org.apache.lucene.search.TermQuery;
 import org.apache.lucene.search.TopFieldDocs;
 import org.apache.lucene.store.Directory;
 import org.apache.lucene.store.FSDirectory;
 import org.codehaus.plexus.component.annotations.Component;
 import org.codehaus.plexus.component.annotations.Requirement;
 import org.codehaus.plexus.logging.Logger;
 import org.codehaus.plexus.personality.plexus.lifecycle.phase.Startable;
 import org.codehaus.plexus.personality.plexus.lifecycle.phase.StartingException;
 import org.codehaus.plexus.personality.plexus.lifecycle.phase.StoppingException;
 
 @Component( role = TimelineIndexer.class )
 public class DefaultTimelineIndexer
     implements TimelineIndexer, Startable
 {
     private static final String TIMESTAMP = "_t";
 
     private static final String TYPE = "_1";
 
     private static final String SUBTYPE = "_2";
 
     private static final Resolution TIMELINE_RESOLUTION = Resolution.SECOND;
 
     @Requirement
     private Logger logger;
 
     private Directory directory;
 
     private IndexReader indexReader;
 
     private ExtendedIndexWriter indexWriter;
 
     private IndexSearcher indexSearcher;
 
     private Object indexLock = new Object();
 
     protected Logger getLogger()
     {
         return logger;
     }
 
     public void configure( TimelineConfiguration configuration )
         throws TimelineException
     {
         try
         {
             boolean newIndex = true;
 
             synchronized ( this )
             {
                 if ( directory != null )
                 {
                     directory.close();
                 }
 
                 directory = FSDirectory.getDirectory( configuration.getIndexDirectory() );
 
                 if ( IndexReader.indexExists( directory ) )
                 {
                     if ( IndexWriter.isLocked( directory ) )
                     {
                         IndexWriter.unlock( directory );
                     }
 
                     newIndex = false;
                 }
 
                 indexWriter =
                     new ExtendedIndexWriter( directory, new StandardAnalyzer(), newIndex, MaxFieldLength.LIMITED );
 
                 closeIndexWriter();
             }
         }
         catch ( Exception e )
         {
            throw new TimelineException( "Fail to configure timeline index!", e );
         }
 
     }
 
     public void start()
         throws StartingException
     {
     }
 
     public void stop()
         throws StoppingException
     {
         try
         {
             synchronized ( this )
             {
                closeIndexReaderAndSearcher();

                 closeIndexWriter();
 
                 if ( directory != null )
                 {
                     directory.close();
                 }
             }
         }
         catch ( IOException e )
         {
             throw new StoppingException( "Unable to stop Timeline!", e );
         }
     }
 
     protected IndexWriter getIndexWriter()
         throws IOException
     {
         synchronized ( indexLock )
         {
             if ( indexWriter == null )
             {
                 indexWriter =
                     new ExtendedIndexWriter( directory, new StandardAnalyzer(), false, MaxFieldLength.LIMITED );
 
                 indexWriter.setRAMBufferSizeMB( 2 );
 
                 indexWriter.setMergeScheduler( new SerialMergeScheduler() );
             }
 
             return indexWriter;
         }
     }
 
     protected void closeIndexWriter()
         throws IOException
     {
         synchronized ( indexLock )
         {
             if ( indexWriter != null )
             {
                 indexWriter.commit();
 
                 indexWriter.close();
 
                 indexWriter = null;
             }
         }
     }
 
     protected boolean isIndexWriterDirty()
     {
         if ( indexWriter == null )
         {
             return false;
         }
         else
         {
             return indexWriter.hasUncommitedChanges();
         }
     }
 
     protected IndexReader getIndexReader()
         throws IOException
     {
         synchronized ( indexLock )
         {
             if ( indexReader == null || ( !indexReader.isCurrent() && !isIndexWriterDirty() ) )
             {
                 if ( indexReader != null )
                 {
                     indexReader.close();
                 }
 
                 indexReader = IndexReader.open( directory );
             }
 
             return indexReader;
         }
     }
 
     protected IndexSearcher getIndexSearcher()
         throws IOException
     {
         synchronized ( indexLock )
         {
             if ( indexSearcher == null || getIndexReader() != indexSearcher.getIndexReader() )
             {
                 if ( indexSearcher != null )
                 {
                     indexSearcher.close();
 
                     // the reader was supplied explicitly
                     indexSearcher.getIndexReader().close();
                 }
 
                 indexSearcher = new IndexSearcher( getIndexReader() );
             }
 
             return indexSearcher;
         }
     }
 
     protected void closeIndexReaderAndSearcher()
         throws IOException
     {
         synchronized ( indexLock )
         {
             if ( indexSearcher != null )
             {
                 indexSearcher.getIndexReader().close();
 
                 indexSearcher.close();
 
                 indexSearcher = null;
             }
 
             if ( indexReader != null )
             {
                 indexReader.close();
 
                 indexReader = null;
             }
         }
     }
 
     public void add( TimelineRecord record )
         throws TimelineException
     {
         IndexWriter writer = null;
 
         try
         {
             synchronized ( this )
             {
                 writer = getIndexWriter();
 
                 writer.addDocument( createDocument( record ) );
 
                 writer.commit();
             }
         }
         catch ( IOException e )
         {
             throw new TimelineException( "Fail to add a record to the timeline index", e );
         }
     }
 
     public void addAll( TimelineResult res )
         throws TimelineException
     {
         IndexWriter writer = null;
 
         try
         {
             synchronized ( this )
             {
                 writer = getIndexWriter();
 
                 for ( TimelineRecord rec : res )
                 {
                     writer.addDocument( createDocument( rec ) );
                 }
 
                 writer.commit();
             }
         }
         catch ( IOException e )
         {
             throw new TimelineException( "Fail to add a record to the timeline index", e );
         }
     }
 
     private Document createDocument( TimelineRecord record )
     {
         Document doc = new Document();
 
         doc.add( new Field( TIMESTAMP, DateTools.timeToString( record.getTimestamp(), TIMELINE_RESOLUTION ),
             Field.Store.YES, Field.Index.NOT_ANALYZED ) );
 
         doc.add( new Field( TYPE, record.getType(), Field.Store.YES, Field.Index.NOT_ANALYZED ) );
 
         doc.add( new Field( SUBTYPE, record.getSubType(), Field.Store.YES, Field.Index.NOT_ANALYZED ) );
 
         for ( String key : record.getData().keySet() )
         {
             doc.add( new Field( key, record.getData().get( key ), Field.Store.YES, Field.Index.ANALYZED ) );
         }
 
         return doc;
     }
 
     private Query buildQuery( long from, long to, Set<String> types, Set<String> subTypes )
     {
         if ( isEmptySet( types ) && isEmptySet( subTypes ) )
         {
             return new ConstantScoreRangeQuery( TIMESTAMP, DateTools.timeToString( from, TIMELINE_RESOLUTION ),
                 DateTools.timeToString( to, TIMELINE_RESOLUTION ), true, true );
         }
         else
         {
             BooleanQuery result = new BooleanQuery();
 
             result.add( new ConstantScoreRangeQuery( TIMESTAMP, DateTools.timeToString( from, TIMELINE_RESOLUTION ),
                 DateTools.timeToString( to, TIMELINE_RESOLUTION ), true, true ), Occur.MUST );
 
             if ( !isEmptySet( types ) )
             {
                 BooleanQuery typeQ = new BooleanQuery();
 
                 for ( String type : types )
                 {
                     typeQ.add( new TermQuery( new Term( TYPE, type ) ), Occur.SHOULD );
                 }
 
                 result.add( typeQ, Occur.MUST );
             }
             if ( !isEmptySet( subTypes ) )
             {
                 BooleanQuery subTypeQ = new BooleanQuery();
 
                 for ( String subType : subTypes )
                 {
                     subTypeQ.add( new TermQuery( new Term( SUBTYPE, subType ) ), Occur.SHOULD );
                 }
 
                 result.add( subTypeQ, Occur.MUST );
             }
 
             return result;
         }
     }
 
     private boolean isEmptySet( Set<String> set )
     {
         return set == null || set.size() == 0;
     }
 
     public TimelineResult retrieve( long fromTime, long toTime, Set<String> types, Set<String> subTypes, int from,
                                     int count, TimelineFilter filter )
         throws TimelineException
     {
         try
         {
             synchronized ( this )
             {
                 IndexSearcher searcher = getIndexSearcher();
 
                 if ( searcher.maxDoc() == 0 )
                 {
                     return TimelineResult.EMPTY_RESULT;
                 }
 
                 TopFieldDocs topDocs =
                     searcher.search( buildQuery( fromTime, toTime, types, subTypes ), null, searcher.maxDoc(),
                         new Sort( new SortField( TIMESTAMP, SortField.LONG, true ) ) );
 
                 if ( topDocs.scoreDocs.length == 0 )
                 {
                     return TimelineResult.EMPTY_RESULT;
                 }
 
                 return new IndexerTimelineResult( searcher, topDocs, filter, from, count );
             }
         }
         catch ( IOException e )
         {
             throw new TimelineException( "Failed to retrieve records from the timeline index!", e );
         }
     }
 
     public static class IndexerTimelineResult
         extends TimelineResult
     {
         private IndexSearcher searcher;
 
         private TopFieldDocs hits;
 
         private TimelineFilter filter;
 
         private int docNumberToSkip;
 
         private int docNumberToReturn;
 
         private int i;
 
         private int returned;
 
         public IndexerTimelineResult( final IndexSearcher searcher, final TopFieldDocs hits,
                                       final TimelineFilter filter, final int docNumberToSkip,
                                       final int docNumberToReturn )
         {
             this.searcher = searcher;
 
             this.hits = hits;
 
             this.filter = filter;
 
             this.docNumberToSkip = docNumberToSkip;
 
             this.docNumberToReturn = docNumberToReturn;
 
             this.i = 0;
 
             this.returned = 0;
         }
 
         // for testing
         protected int getLength()
         {
             return hits.scoreDocs.length;
         }
 
         @Override
         protected TimelineRecord fetchNextRecord()
         {
             if ( i >= hits.scoreDocs.length || returned >= docNumberToReturn )
             {
                 return null;
             }
 
             try
             {
                 Document doc = searcher.doc( hits.scoreDocs[i++].doc );
 
                 TimelineRecord data = buildData( doc );
 
                 if ( filter != null && !filter.accept( data ) )
                 {
                     return fetchNextRecord();
                 }
 
                 // skip the unneeded stuff
                 // Warning: this means we skip the needed FILTERED stuff out!
                 if ( docNumberToSkip > 0 )
                 {
                     docNumberToSkip--;
 
                     return fetchNextRecord();
                 }
 
                 returned++;
 
                 return data;
             }
             catch ( IOException e )
             {
                 return null;
             }
         }
 
         @SuppressWarnings( "unchecked" )
         protected TimelineRecord buildData( Document doc )
         {
             long timestamp = -1;
 
             String tsString = doc.get( TIMESTAMP );
 
             if ( tsString != null )
             {
                 // legacy indexes will have nulls here
                 try
                 {
                     timestamp = DateTools.stringToTime( tsString );
                 }
                 catch ( ParseException e )
                 {
                     // leave it -1
                 }
             }
 
             // legacy indexes will have nulls here
             String type = doc.get( TYPE );
 
             // legacy indexes will have nulls here
             String subType = doc.get( SUBTYPE );
 
             Map<String, String> data = new HashMap<String, String>();
 
             TimelineRecord result = new TimelineRecord( timestamp, type, subType, data );
 
             for ( Field field : (List<Field>) doc.getFields() )
             {
                 if ( !field.name().startsWith( "_" ) )
                 {
                     data.put( field.name(), field.stringValue() );
                 }
             }
 
             return result;
         }
     }
 
     public int purge( long fromTime, long toTime, Set<String> types, Set<String> subTypes )
         throws TimelineException
     {
         try
         {
             synchronized ( this )
             {
                 IndexSearcher searcher = getIndexSearcher();
 
                 if ( searcher.maxDoc() == 0 )
                 {
                     return 0;
                 }
 
                 Query q = buildQuery( fromTime, toTime, types, subTypes );
 
                 // just to know how many will we delete, will not actually load 'em up
                 TopFieldDocs topDocs =
                     searcher.search( q, null, searcher.maxDoc(), new Sort( new SortField( TIMESTAMP, SortField.LONG,
                         true ) ) );
 
                 if ( topDocs.scoreDocs.length == 0 )
                 {
                     return 0;
                 }
 
                 IndexWriter writer = getIndexWriter();
 
                 writer.deleteDocuments( q );
 
                 writer.commit();
 
                 writer.optimize();
 
                 return topDocs.scoreDocs.length;
             }
         }
         catch ( IOException e )
         {
             throw new TimelineException( "Failed to purge records from the timeline index!", e );
         }
     }
 }
