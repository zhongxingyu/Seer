 package org.neo4j.rdf.fulltext;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.Reader;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.transaction.SystemException;
 
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.analysis.LowerCaseFilter;
 import org.apache.lucene.analysis.TokenStream;
 import org.apache.lucene.analysis.WhitespaceTokenizer;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Field;
 import org.apache.lucene.document.Field.Index;
 import org.apache.lucene.document.Field.Store;
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.index.Term;
 import org.apache.lucene.queryParser.ParseException;
 import org.apache.lucene.queryParser.QueryParser;
 import org.apache.lucene.search.BooleanQuery;
 import org.apache.lucene.search.Hits;
 import org.apache.lucene.search.IndexSearcher;
 import org.apache.lucene.search.Query;
 import org.apache.lucene.search.Sort;
 import org.apache.lucene.search.TermQuery;
 import org.apache.lucene.search.BooleanClause.Occur;
 import org.apache.lucene.search.highlight.Formatter;
 import org.apache.lucene.search.highlight.Highlighter;
 import org.apache.lucene.search.highlight.QueryScorer;
 import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
 import org.apache.lucene.store.Directory;
 import org.apache.lucene.store.FSDirectory;
 import org.neo4j.api.core.NeoService;
 import org.neo4j.api.core.Node;
 import org.neo4j.rdf.fulltext.PersistentQueue.Entry;
 import org.neo4j.rdf.model.Uri;
 import org.neo4j.util.NeoUtil;
 
 /**
  * A {@link FulltextIndex} using lucene.
  * The query format (see the search method) is a plain lucene query, but with
  * the addition that an AND operator is squeezed in between every word making
  * it and AND search by default, instead of OR.
  */
 public class SimpleFulltextIndex implements FulltextIndex
 {
     /**
      * The literal node id
      */
     private static final String KEY_ID = "id";
     private static final String KEY_INDEX = "index";
     private static final String KEY_PREDICATE = "predicate";
     private static final String KEY_INDEX_SOURCE = "index_source";
     private static final String SNIPPET_DELIMITER = "...";
    private static final String DEFAULT_COMBINE_MODE = "OR";
     
     private LiteralReader literalReader = new SimpleLiteralReader();
     private String directoryPath;
     private String queuePath;
     private Directory directory;
     private Analyzer analyzer = new Analyzer()
     {
         @Override
         public TokenStream tokenStream( String fieldName, Reader reader )
         {
             return new LowerCaseFilter( new WhitespaceTokenizer( reader ) );
         }
     };
     private NeoService neo;
     private NeoUtil neoUtil;
     private Map<Integer, Collection<Object[]>> toIndex =
         Collections.synchronizedMap(
             new HashMap<Integer, Collection<Object[]>>() );
     private PersistentQueue indexingQueue;
     private IndexingThread indexingThread;
     private Formatter highlightFormatter;
     private Set<String> predicateFilter;
     
     public SimpleFulltextIndex( NeoService neo, File storagePath )
     {
         this( neo, storagePath, null );
     }
     
     public SimpleFulltextIndex( NeoService neo, File storagePath,
         Collection<String> predicateFilter )
     {
         this( neo, storagePath, null, null, predicateFilter );
     }
     
     public SimpleFulltextIndex( NeoService neo, File storagePath,
         String highlightPreTag, String highlightPostTag,
         Collection<String> predicateFilter )
     {
         if ( highlightPreTag == null || highlightPostTag == null )
         {
             this.highlightFormatter = new SimpleHTMLFormatter();
         }
         else
         {
             this.highlightFormatter = new SimpleHTMLFormatter(
                 highlightPreTag, highlightPostTag );
         }
         
         this.predicateFilter = predicateFilter == null ? null :
             new HashSet<String>( predicateFilter );
         this.directoryPath = storagePath.getAbsolutePath();
         this.queuePath = this.directoryPath + "-queue";
         this.neo = neo;
         this.neoUtil = new NeoUtil( neo );
         this.indexingQueue = new PersistentQueue( new File( queuePath ) );
         
         try
         {
             createLuceneDirectory();
         }
         catch ( IOException e )
         {
             throw new RuntimeException( e );
         }
         
         this.indexingThread = new IndexingThread();
         this.indexingThread.start();
     }
     
     public void clear()
     {
         shutDown();
         delete();
         try
         {
             createLuceneDirectory();
         }
         catch ( IOException e )
         {
             throw new RuntimeException( e );
         }
         this.indexingQueue = new PersistentQueue( new File( queuePath ) );
         this.indexingThread = new IndexingThread();
         this.indexingThread.start();
     }
     
     private void createLuceneDirectory() throws IOException
     {
         if ( !IndexReader.indexExists( directoryPath ) )
         {
             new File( directoryPath ).mkdirs();
             IndexWriter writer =
                 new IndexWriter( directoryPath, analyzer, true );
             writer.close();
         }
         directory = FSDirectory.getDirectory( directoryPath );
         if ( IndexReader.isLocked( directory ) )
         {
             IndexReader.unlock( directory );
         }
     }
     
     private Directory getDir() throws IOException
     {
         return this.directory;
     }
     
     private IndexWriter getWriter( boolean create ) throws IOException
     {
         return new IndexWriter( getDir(), analyzer, create );
     }
     
     public void index( Node node, Uri predicate, Object literal )
     {
         enqueueCommand( true, node, predicate, literal );
     }
     
     private void enqueueCommand( boolean trueForIndex,
         Node node, Uri predicate, Object literal )
     {
         if ( predicateFilter != null &&
             !predicateFilter.contains( predicate.getUriAsString() ) )
         {
             return;
         }
         
         try
         {
             int key =
                 neoUtil.getTransactionManager().getTransaction().hashCode();
             Collection<Object[]> commands = toIndex.get( key );
             if ( commands == null )
             {
                 commands = new ArrayList<Object[]>();
                 toIndex.put( key, commands );
             }
             commands.add( new Object[] {
                 trueForIndex, node.getId(), predicate.getUriAsString(), literal
             } );
         }
         catch ( SystemException e )
         {
             throw new RuntimeException( e );
         }
     }
     
     protected void safeClose( Object object )
     {
         try
         {
             if ( object != null )
             {
                 if ( object instanceof IndexWriter )
                 {
                     ( ( IndexWriter ) object ).close();
                 }
                 else if ( object instanceof IndexReader )
                 {
                     ( ( IndexReader ) object ).close();
                 }
                 else if ( object instanceof IndexSearcher )
                 {
                     ( ( IndexSearcher ) object ).close();
                 }
                 else
                 {
                     throw new RuntimeException( object.getClass().getName() );
                 }
             }
         }
         catch ( IOException e )
         {
             e.printStackTrace();
         }
     }
     
     private void doIndex( long nodeId, String predicate, Object literal )
     {
         IndexWriter writer = null;
         try
         {
             writer = getWriter( false );
             Document doc = new Document();
             doc.add( new Field( KEY_ID, String.valueOf( nodeId ), Store.YES,
                 Index.UN_TOKENIZED ) );
             doc.add( new Field( KEY_INDEX, getLiteralReader().read( literal ),
                 Store.YES, Index.TOKENIZED ) );
             doc.add( new Field( KEY_PREDICATE, predicate,
                 Store.NO, Index.UN_TOKENIZED ) );
             doc.add( new Field( KEY_INDEX_SOURCE, literal.toString(),
                 Store.YES, Index.UN_TOKENIZED ) );
             writer.addDocument( doc );
         }
         catch ( IOException e )
         {
             throw new RuntimeException( e );
         }
         finally
         {
             safeClose( writer );
         }
     }
     
     public void removeIndex( Node node, Uri predicate, Object literal )
     {
         enqueueCommand( false, node, predicate, literal );
     }
     
     private void doRemoveIndex( long nodeId, String predicate, Object literal )
     {
         IndexReader reader = null;
         IndexSearcher searcher = null;
         try
         {
             reader = IndexReader.open( getDir() );
             BooleanQuery masterQuery = new BooleanQuery();
             masterQuery.add( new TermQuery(
                 new Term( KEY_ID, String.valueOf( nodeId ) ) ), Occur.MUST );
             masterQuery.add( new TermQuery(
                 new Term( KEY_PREDICATE, predicate ) ), Occur.MUST );
             masterQuery.add( new TermQuery(
                 new Term( KEY_INDEX_SOURCE, literal.toString() ) ),
                 Occur.MUST );
             
             searcher = new IndexSearcher( getDir() );
             Hits hits = searcher.search( masterQuery );
             for ( int i = 0; i < hits.length(); i++ )
             {
                 reader.deleteDocument( hits.id( i ) );
             }
         }
         catch ( IOException e )
         {
             throw new RuntimeException( e );
         }
         finally
         {
             safeClose( searcher );
             safeClose( reader );
         }
     }
     
     public Iterable<RawQueryResult> search( String query )
     {
         // Maybe return an iterable with just the hits and the queryresult
         // conversion will be on the fly (because snippet rendering is slow?)
         
         IndexSearcher searcher = null;
         try
         {
             searcher = new IndexSearcher( getDir() );
             List<RawQueryResult> result =
                 new ArrayList<RawQueryResult>();
             Query q = new QueryParser( KEY_INDEX, analyzer ).parse( query );
             Hits hits = searcher.search( q, Sort.RELEVANCE );
             Highlighter highlighter = new Highlighter( highlightFormatter,
                new QueryScorer( q ) );
             for ( int i = 0; i < hits.length(); i++ )
             {
                 Document doc = hits.doc( i );
                 long id = Long.parseLong( doc.get( KEY_ID ) );
                 float score = hits.score( i );
                 String snippet = generateSnippet( doc, highlighter );
                 result.add( new RawQueryResult( neo.getNodeById( id ),
                     score, snippet ) );
             }
             return result;
         }
         catch ( IOException e )
         {
             throw new RuntimeException( e );
         }
         catch ( ParseException e )
         {
             throw new RuntimeException( e );
         }
         finally
         {
             safeClose( searcher );
         }
     }
     
     private String generateSnippet( Document doc, Highlighter highlighter )
     {
         StringBuffer snippet = new StringBuffer();
         for ( Field field : doc.getFields( KEY_INDEX ) )
         {
             String text = field.stringValue();
             TokenStream tokenStream = analyzer.tokenStream( KEY_INDEX,
                 new StringReader( text ) );
             try
             {
                 String fragment = highlighter.getBestFragments(
                     tokenStream, text, 2, SNIPPET_DELIMITER );
                 if ( snippet.length() > 0 )
                 {
                     snippet.append( SNIPPET_DELIMITER );
                 }
                 snippet.append( fragment );
             }
             catch ( IOException e )
             {
                 // TODO
                 continue;
             }
         }
         return snippet.toString();
     }
     
     public LiteralReader getLiteralReader()
     {
         return this.literalReader;
     }
     
     public void setLiteralReader( LiteralReader reader )
     {
         this.literalReader = reader;
     }
     
     public void end( boolean commit )
     {
         try
         {
             end( neoUtil.getTransactionManager().getTransaction().hashCode(),
                 commit );
         }
         catch ( SystemException e )
         {
             throw new RuntimeException( e );
         }
     }
     
     public void end( int txId, boolean commit )
     {
         Collection<Object[]> commands = toIndex.remove( txId );
         if ( commands == null || !commit )
         {
             return;
         }
         
         for ( Object[] command : commands )
         {
             this.indexingQueue.add( command );
             this.indexingThread.hasItems = true;
         }
     }
     
     public boolean queueIsEmpty()
     {
         return !this.indexingThread.hasItems;
     }
     
     public void shutDown()
     {
         indexingThread.halt();
         try
         {
             indexingThread.join();
         }
         catch ( InterruptedException e )
         {
             e.printStackTrace();
         }
         
         indexingQueue.close();
         try
         {
             directory.close();
         }
         catch ( IOException e )
         {
             throw new RuntimeException( e );
         }
     }
     
     private class IndexingThread extends Thread
     {
         private boolean halted;
         private boolean hasItems;
         
         private void halt()
         {
             this.halted = true;
         }
         
         @Override
         public void run()
         {
             while ( !halted )
             {
                 try
                 {
                     hasItems = indexingQueue.hasNext();
                     while ( !halted && hasItems )
                     {
                         Entry entry = indexingQueue.next();
                         Object[] data = entry.data();
                         if ( ( Boolean ) data[ 0 ] )
                         {
                             doIndex( ( Long ) data[ 1 ],
                                 ( String ) data[ 2 ], data[ 3 ] );
                         }
                         else
                         {
                             doRemoveIndex( ( Long ) data[ 1 ],
                                 ( String ) data[ 2 ], data[ 3 ] );
                         }
                         hasItems = indexingQueue.hasNext();
                     }
                     try
                     {
                         long time = System.currentTimeMillis();
                         while ( !halted &&
                             System.currentTimeMillis() - time < 100 )
                         {
                             hasItems = indexingQueue.hasNext();
                             Thread.sleep( 5 );
                         }
                     }
                     catch ( InterruptedException e )
                     {
                         Thread.interrupted();
                     }
                 }
                 catch ( Throwable t )
                 {
                     t.printStackTrace();
                 }
             }
         }
     }
     
     private void delete()
     {
         deleteDir( new File( directoryPath ) );
         new File( queuePath ).delete();
     }
     
     protected void deleteDir( File dir )
     {
         if ( !dir.exists() )
         {
             return;
         }
         
         for ( File child : dir.listFiles() )
         {
             if ( child.isFile() )
             {
                 child.delete();
             }
             else
             {
                 deleteDir( child );
             }
         }
         dir.delete();
     }
 }
