 /*******************************************************************************
  * Copyright 2010 Nathan Kopp
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 package org.d2.plugins.lucene;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.analysis.standard.StandardAnalyzer;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.index.CorruptIndexException;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.index.IndexWriterConfig;
 import org.apache.lucene.index.Term;
 import org.apache.lucene.queryParser.ParseException;
 import org.apache.lucene.queryParser.QueryParser;
 import org.apache.lucene.search.BooleanClause;
 import org.apache.lucene.search.BooleanClause.Occur;
 import org.apache.lucene.search.BooleanQuery;
 import org.apache.lucene.search.FuzzyQuery;
 import org.apache.lucene.search.NumericRangeQuery;
 import org.apache.lucene.search.PrefixQuery;
 import org.apache.lucene.search.Query;
 import org.apache.lucene.search.ScoreDoc;
 import org.apache.lucene.search.TermQuery;
 import org.apache.lucene.search.TermRangeQuery;
 import org.apache.lucene.search.TopScoreDocCollector;
 import org.apache.lucene.search.WildcardQuery;
 import org.apache.lucene.store.Directory;
 import org.apache.lucene.store.FSDirectory;
 import org.apache.lucene.store.LockObtainFailedException;
 import org.apache.lucene.store.NRTCachingDirectory;
 import org.apache.lucene.util.Version;
 import org.d2.Bucket;
 import org.d2.index.DocBuilderAbstract;
 import org.d2.pluggable.Indexer;
 import org.d2.query.D2NumericRangeTerm;
 import org.d2.query.D2Query;
 import org.d2.query.D2QueryNode;
 import org.d2.query.D2RangeTerm;
 import org.d2.query.D2Term;
 import org.nkts.util.Util;
 
 
 public class LuceneIndexer implements Indexer
 {
     private Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_36);
     private File indexDir;
     
     private DocBuilderAbstract docBuilder;
     
     LuceneManager manager;
     
     private Boolean stopThread = false;
     
     private Directory index;
     
     public LuceneIndexer(String rootFolder, Bucket bucket) throws CorruptIndexException, LockObtainFailedException, IOException
     {
         indexDir = new File(rootFolder+"/"+bucket.getName()+"/index");
         indexDir.mkdirs();
         
         //index = FSDirectory.open(indexDir);
        Directory fsDir = FSDirectory.open(new File("/path/to/index"));
         index = new NRTCachingDirectory(fsDir, 5.0, 60.0);
         
         manager = new LuceneManager(index);
         this.docBuilder = createDocBuilder();
     }
 
     private DocBuilderAbstract createDocBuilder()
     {
         return new LuceneDocBuilder();
     }
 
     public synchronized void indexObject(Object obj)
     {
         try
         {
             Document d = (Document)docBuilder.toDocument(obj);
             manager.updateDocument(obj, d);
             
         }
         catch (CorruptIndexException e)
         {
             throw Util.wrap(e);
         }
         catch (IOException e)
         {
             throw Util.wrap(e);
         }
         finally
         {
             try
             {
 //                closeWriter();
             }
             catch(Exception e)
             {
                 throw Util.wrap(e);
             }
         }
     }
 
     public List<String> findIdByQuery(D2Query query)
     {
         LuceneReaderAndSearcher searcher = null;
         try
         {
             searcher = manager.getSearcher();
             List<String> pList = new ArrayList<String>();
             
             TopScoreDocCollector  collector = TopScoreDocCollector.create(20, true);
             searcher.getSearcher().search(makeLuceneQuery(query), collector);
             ScoreDoc[] hits = collector.topDocs().scoreDocs;
     
             for (int i = 0; i < hits.length; i++)
             {
                 Document doc = searcher.getSearcher().doc(hits[i].doc);
                 pList.add(doc.get("id"));
             }
             
             return pList;
         }
         catch (IOException e)
         {
             throw Util.wrap(e);
         }
         finally
         {
             manager.releaseSearcher(searcher);
         }
     }
 
     
     
     // ======================================================================
     // ======================================================================
     
     /**
      * TODO stop the background thread and lock everything (including all reads)
      */
     public void rebuildIndex(Collection<Object> objList)
     {
         try
         {
             manager.close();
             manager = new LuceneManager(index);
             
             
             //writer = new IndexWriter(directory, analyzer, true, IndexWriter.MaxFieldLength.LIMITED);
             IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_36, analyzer);
             if(index instanceof NRTCachingDirectory) config.setMergeScheduler(((NRTCachingDirectory)index).getMergeScheduler());
             IndexWriter writer = new IndexWriter(manager.getIndex(), config);
             for(Object obj : objList)
             {
                 Document d = (Document)docBuilder.toDocument(obj);
                 writer.addDocument(d);
             }
     
             //writer.optimize();
             writer.commit();
             
             manager.close();
             manager = new LuceneManager(index);
         }
         catch (CorruptIndexException e)
         {
             throw Util.wrap(e);
         }
         catch (IOException e)
         {
             throw Util.wrap(e);
         }
     }
     
     // ======================================================================
     // ======================================================================
     
     protected Query makeLuceneQuery(D2Query query)
     {
         try
         {
             BooleanQuery lquery = new BooleanQuery();
             for(D2QueryNode node : query.getNodes())
             {
                 if(node instanceof D2Term)
                 {
                     D2Term term = (D2Term)node;
                     switch(term.getType())
                     {
                         case EXACT:
                             lquery.add(new TermQuery(new Term(term.getField(), handleBlanks(term.getValue()))),  getLuceneOccur(node));
                             break;
                         case FUZZY:
                             lquery.add(new FuzzyQuery(new Term(term.getField(), term.getValue())),  getLuceneOccur(node));
                             break;
                         case PREFIX:
                             lquery.add(new PrefixQuery(new Term(term.getField(), term.getValue())),  getLuceneOccur(node));
                             break;
                         case WILDCARD:
                             lquery.add(new WildcardQuery(new Term(term.getField(), term.getValue())),  getLuceneOccur(node));
                             break;
                         case PARSED:
                             QueryParser parser = new QueryParser(Version.LUCENE_36, term.getField(), analyzer);
                             Query luceneChild = parser.parse(term.getValue());
                             lquery.add(luceneChild,  getLuceneOccur(node));
                             break;
                         default:
                             throw new RuntimeException("query type not found: "+term.getType());
                     }
                 }
                 else if (node instanceof D2RangeTerm)
                 {
                     D2RangeTerm term = (D2RangeTerm)node;
                     lquery.add(new TermRangeQuery(term.getField(), term.getValue1(), term.getValue2(), term.isInclusive(), term.isInclusive()),  getLuceneOccur(node));
                 }
                 else if (node instanceof D2NumericRangeTerm)
                 {
                     D2NumericRangeTerm term = (D2NumericRangeTerm)node;
                     if(Double.class.isAssignableFrom(term.getType()))       lquery.add(NumericRangeQuery.newDoubleRange(term.getField(), (Double)term.getValue1(), (Double)term.getValue2(), term.isInclusive(), term.isInclusive()),  getLuceneOccur(node));
                     else if(Long.class.isAssignableFrom(term.getType()))    lquery.add(NumericRangeQuery.newLongRange(term.getField(), (Long)term.getValue1(), (Long)term.getValue2(), term.isInclusive(), term.isInclusive()),  getLuceneOccur(node));
                     else if(Float.class.isAssignableFrom(term.getType()))   lquery.add(NumericRangeQuery.newFloatRange(term.getField(), (Float)term.getValue1(), (Float)term.getValue2(), term.isInclusive(), term.isInclusive()),  getLuceneOccur(node));
                     else if(Integer.class.isAssignableFrom(term.getType())) lquery.add(NumericRangeQuery.newIntRange(term.getField(), (Integer)term.getValue1(), (Integer)term.getValue2(), term.isInclusive(), term.isInclusive()),  getLuceneOccur(node));
                 }
                 else if (node instanceof D2Query)
                 {
                     D2Query child = (D2Query)node;
                     Query luceneChild = makeLuceneQuery(child);
                     lquery.add(luceneChild, getLuceneOccur(child));
                 }
             }
             
 //            System.out.println("LUCENE QUERY: "+lquery.toString());
             
             return lquery;
         }
         catch (ParseException e)
         {
             throw Util.wrap(e);
         }
     }
     
     private static String handleBlanks(String input)
     {
         if(Util.isBlank(input)) return LuceneDocBuilder.MAGIC_BLANK_VALUE;
         else return input;
     }
 
     private static Occur getLuceneOccur(D2QueryNode node)
     {
         switch(node.getOccurs())
         {
             case MUST: return BooleanClause.Occur.MUST;
             case SHOULD: return BooleanClause.Occur.SHOULD;
             case MUST_NOT: return BooleanClause.Occur.MUST_NOT;
         }
         throw new RuntimeException("bad occurs: "+node.getOccurs());
     }
 
     // ======================================================================
     // ======================================================================
     
     
     
     public void close()
     {
         try
         {
             stopThread = true;
             manager.close();
             synchronized(stopThread) { stopThread.notifyAll(); }
         }
         catch(Exception e)
         {
             Util.wrap(e);
         }
     }
     
 
 
     public DocBuilderAbstract getDocBuilder()
     {
         return docBuilder;
     }
 
     @Override
     public void deleteDocument(String id)
     {
         try
         {
             manager.getWriter().deleteDocuments(new Term("id", id));
         }
         catch(Throwable t)
         {
             throw Util.wrap(t);
         }
 
     }
 
     @Override
     public void resetLocks()
     {
         
         try
         {
             if ((new File(indexDir+"/write.lock").delete()))
             {
                 System.out.println(indexDir+"/write.lock deleted");
             }
         }
         catch(Exception e)
         {
             e.printStackTrace();
         }
         
     }
 
     
     
 
     
 }
