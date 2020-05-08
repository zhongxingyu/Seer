 package org.eclipse.recommenders.codesearch.rcp.index.searcher;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.FieldSelector;
 import org.apache.lucene.index.CorruptIndexException;
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.index.Term;
 import org.apache.lucene.queryParser.ParseException;
 import org.apache.lucene.queryParser.QueryParser;
 import org.apache.lucene.search.FieldCache;
 import org.apache.lucene.search.IndexSearcher;
 import org.apache.lucene.search.MatchAllDocsQuery;
 import org.apache.lucene.search.Query;
 import org.apache.lucene.search.ScoreDoc;
 import org.apache.lucene.search.TopDocs;
 import org.apache.lucene.store.Directory;
 import org.eclipse.recommenders.codesearch.rcp.index.AbstractIndex;
 import org.eclipse.recommenders.codesearch.rcp.index.Fields;
 import org.eclipse.recommenders.codesearch.rcp.index.termvector.ITermVectorConsumable;
 import org.eclipse.recommenders.utils.Tuple;
 
 import com.google.common.annotations.VisibleForTesting;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 
 // TODO: Tobias, what is the meaning of a code searcher index? Is it actually an index?
 public class CodeSearcherIndex extends AbstractIndex implements ITermVectorConsumable {
     private final QueryParser parser;
     private IndexReader reader;
 
     public CodeSearcherIndex(final Directory directory) throws IOException {
         super(directory);
         reader = IndexReader.open(directory);
         parser = new QueryParser(getVersion(), Fields.FULL_TEXT, getAnalyzer());
         parser.setLowercaseExpandedTerms(false);
         parser.setAllowLeadingWildcard(true);
     }
 
     @VisibleForTesting
     public List<Document> getDocuments() throws IOException {
         final MatchAllDocsQuery allDocsQuery = new MatchAllDocsQuery();
         return search(allDocsQuery, null);
     }
 
     public List<Document> search(final String queryString) throws IOException, ParseException {
         return search(queryString, null);
     }
 
     public List<Document> search(final String queryString, final FieldSelector selector) throws IOException,
             ParseException {
         final Query query = parser.parse(queryString);
         return search(query, selector);
     }
 
     public List<Document> search(final Query query, final FieldSelector selector) throws IOException {
        return search(query, selector, reader.numDocs());
     }
 
    public List<Document> search(final Query query, final FieldSelector selector, final int i) throws IOException {
        final Tuple<TopDocs, IndexSearcher> docs = lenientSearch(query, i);
         final IndexSearcher searcher = docs.getSecond();
         final ScoreDoc[] scoreDocs = docs.getFirst().scoreDocs;
         final List<Document> result = toList(searcher, scoreDocs, selector);
         searcher.close();
         return result;
     }
 
     /**
      * caller is responsible for closing the searcher
      */
     public Tuple<TopDocs, IndexSearcher> lenientSearch(final Query query) throws IOException {
         renewReader();
         final IndexSearcher searcher = new IndexSearcher(reader);
         final TopDocs docs = searcher.search(query, reader.numDocs());
         return Tuple.newTuple(docs, searcher);
     }
 
     /**
      * caller is responsible for closing the searcher
      */
     public Tuple<TopDocs, IndexSearcher> lenientSearch(final Query query, final int maxHits) throws IOException {
         renewReader();
         final IndexSearcher searcher = new IndexSearcher(reader);
         final TopDocs docs = searcher.search(query, maxHits);
         return Tuple.newTuple(docs, searcher);
     }
 
     @Override
     public Set<String> getTermVector(final String[] fieldNames) {
         renewReader();
         final Set<String> result = Sets.newHashSet();
         for (final String field : fieldNames) {
             try {
                 final String[] values = FieldCache.DEFAULT.getStrings(reader, field);
                 result.addAll(Lists.newArrayList(values));
             } catch (final IOException e) {
                 // XXX: Activator.logError(e);
             }
         }
         result.remove(null);
         return result;
     }
 
     private void renewReader() {
         try {
             final IndexReader newReader = IndexReader.openIfChanged(reader);
             if (newReader != null) {
                 // reader was reopened
                 reader.close();
                 reader = newReader;
             }
         } catch (final Exception ex) {
             // XXX: Activator.logError(ex);
         }
     }
 
     private static List<Document> toList(final IndexSearcher searcher, final ScoreDoc[] scoreDocs,
             final FieldSelector selector) throws CorruptIndexException, IOException {
         final List<Document> result = new ArrayList<Document>(scoreDocs.length);
         for (final ScoreDoc doc : scoreDocs) {
             if (selector != null) {
                 result.add(searcher.doc(doc.doc, selector));
             } else {
                 result.add(searcher.doc(doc.doc));
             }
         }
         return result;
     }
 
     public QueryParser getParser() {
         return parser;
     }
 
     public int getDocCount(final Term t) throws IOException {
         return reader.docFreq(t);
     }
 
 }
