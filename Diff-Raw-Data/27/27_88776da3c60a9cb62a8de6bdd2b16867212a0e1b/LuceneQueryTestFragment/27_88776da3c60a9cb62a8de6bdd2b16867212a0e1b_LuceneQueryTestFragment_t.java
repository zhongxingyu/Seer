 package de.cosmocode.lucene.fragments;
 
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.analysis.KeywordAnalyzer;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Field;
 import org.apache.lucene.document.Field.Index;
 import org.apache.lucene.document.Field.Store;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.index.IndexWriter.MaxFieldLength;
 import org.apache.lucene.queryParser.ParseException;
 import org.apache.lucene.queryParser.QueryParser;
 import org.apache.lucene.search.IndexSearcher;
 import org.apache.lucene.search.Query;
 import org.apache.lucene.search.ScoreDoc;
 import org.apache.lucene.search.TopDocs;
 import org.apache.lucene.store.Directory;
 import org.apache.lucene.store.RAMDirectory;
 import org.junit.AfterClass;
 import org.junit.Assert;
 import org.junit.BeforeClass;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import de.cosmocode.junit.UnitProvider;
 import de.cosmocode.lucene.LuceneQuery;
 import de.cosmocode.lucene.LuceneQueryTest;
 
 /**
  * <p> This is an abstract Test class that implements no test itself.
  * It can only be executed by an {@link LuceneQueryTest}.
  * It sets up a dummy Lucene search directory in which the resulting queries
  * can be tested with the method {@link #assertEquals(String, LuceneQuery)}.
  * </p>
  * 
  * @author Oliver Lorenz
  */
 public abstract class LuceneQueryTestFragment implements UnitProvider<LuceneQuery> {
     
     public static final Directory DIRECTORY = new RAMDirectory();
     public static final Analyzer ANALYZER = new KeywordAnalyzer();
     
     /** A helper for wildcard queries, different then WILDCARD2. */
     public static final String WILDCARD1 = "arg";
     /** A helper for wildcard queries, different then WILDCARD1. */
     public static final String WILDCARD2 = "hex";
     /** A helper for wildcard queries. yields different results then WILDCARD1 and WILDCARD2. */
     public static final String WILDCARD3 = "foo";
     
     /** A helper for fuzzy queries.
      * Also yields a result for wildcard queries, so it can be used for wildcard + fuzzy. */
     public static final String FUZZY1 = "truf";
     /** A helper for fuzzy queries. yields different results then FUZZY1 and FUZZY3. */
     public static final String FUZZY2 = "arf1";
     /** A helper for fuzzy queries. yields different results then FUZZY1 AND FUZZY2. */
     public static final String FUZZY3 = "fooba";
     
     public static final String ARG1 = "arg1";
     public static final boolean ARG2 = true;
     public static final String ARG3 = "arg3";
     public static final int ARG4 = 20;
     
     public static final String DEFAULT_FIELD = "default_field";
     public static final String FIELD1 = "field1";
     public static final String FIELD2 = "field2";
     
     
     private static final Logger LOG = LoggerFactory.getLogger(LuceneQueryTestFragment.class);
     
     private static final Object[] ARGS = {
         ARG1, ARG2, ARG3, ARG4, "hexff00aa", "hex559911", "foobar", "truffel",
         WILDCARD1, WILDCARD2, WILDCARD3, FUZZY1, FUZZY2, FUZZY3
     };
     
     
     @Override
     public LuceneQuery unit() {
         return LuceneQueryTest.unitProvider().unit();
     }
     
     /**
      * <p> Asserts that two queries return the same result from lucene.
      * The first query is a hand-made control query, the second is the 
      * LuceneQuery that should be tested.
      * </p>
      * 
      * @param expected the control Query
      * @param actual the generated LuceneQuery
      */
     protected void assertEquals(final String expected, final LuceneQuery actual) {
         final String expectedString = expected;
         final String actualString = actual.getQuery();
         
         final QueryParser parser = new QueryParser(DEFAULT_FIELD, ANALYZER);
         final Query queryExpected;
         final Query queryActual;
         
         try {
             queryExpected = parser.parse(expectedString);
         } catch (ParseException e) {
             throw new IllegalArgumentException("Expected query is illegal", e);
         }
         try {
             queryActual = parser.parse(actualString);
         } catch (ParseException e) {
             throw new IllegalArgumentException("Actual query is illegal", e);
         }
         
         final List<?> docExpected;
         final List<?> docActual;
         try {
             docExpected = search(queryExpected, 100);
             docActual = search(queryActual, 100);
         } catch (IOException e) {
             throw new IllegalStateException("low level IOException", e);
         }
         
         if (!docExpected.equals(docActual)) {
             LOG.debug("Original queries:");
             LOG.debug("\texpected={}", expectedString);
             LOG.debug("\t  actual={}", actualString);
             LOG.debug("Parsed queries:");
             LOG.debug("\texpected={}", queryExpected);
             LOG.debug("\t  actual={}", queryActual);
             LOG.debug("Expected result: {}", docExpected);
             LOG.debug("  Actual result: {}", docActual);
             LOG.debug("End");
         }
         final String errorMsg = "Expected query: " + expectedString + ", but is: " + actualString;
         Assert.assertTrue(errorMsg, docExpected.equals(docActual));
     }
     
     /**
      * Searches for the given query and returns a list that has at most "max" items.
      * @param query the query to search for
      * @param max the maximum number of items in the returned list
      * @return a list with the found Documents
      * @throws IOException if lucene throws an exception 
      */
     protected List<String> search(final Query query, final int max) throws IOException {
         final List<String> docList = new LinkedList<String>();
         final IndexSearcher searcher = new IndexSearcher(DIRECTORY);
         final TopDocs docs = searcher.search(query, max);
         if (docs.totalHits > 0) {
             for (final ScoreDoc doc : docs.scoreDocs) {
                 docList.add(searcher.doc(doc.doc).get("name"));
             }
         }
         return docList;
     }
     
     /**
      * <p> Creates a lucene document that store the given value
      * in a field named "key".
      * </p>
      * <p> This ensures that a search for "key:value"
      * returns the generated document.
      * If the key is DEFAULT_FIELD then a search for "value"
      * returns the generated document.
      * </p>
      * 
      * @param keyValues name and value of the fields, alternating (key, value, key, value, ...)
      * @return a new document with a field set to the given values
      */
     protected static Document createDocument(final String... keyValues) {
         final Document doc = new Document();
         String key = null;
         for (String keyOrValue : keyValues) {
             if (key == null) {
                 key = keyOrValue;
                 continue;
             }
             
            final Field field = new Field(key, keyOrValue, Store.YES, Index.TOKENIZED);
             doc.add(field);
             key = null;
         }
        doc.add(new Field("empty", "empty", Store.NO, Index.TOKENIZED));
         return doc;
     }
     
     /**
      * Creates a new Lucene Index with some predefined field and arguments.
      * @throws IOException if creating the index failed
      */
     @BeforeClass
     public static void createLuceneIndex() throws IOException {
         final IndexWriter writer = new IndexWriter(DIRECTORY, ANALYZER, MaxFieldLength.UNLIMITED);
         
         final String[] fields = new String[] {FIELD1, FIELD2, DEFAULT_FIELD};
         
         for (final String field : fields) {
             final String fieldName = field == DEFAULT_FIELD ? "arg" : field + "_arg";
             
             for (int i = 0; i < ARGS.length; ++i) {
                 writer.addDocument(createDocument("name", fieldName + (i + 1), field, ARGS[i].toString()));
                 
                 if (i == 0) continue;
                 
                 final String[] subArgs = new String[2 * (i + 1)];
                 final StringBuilder name = new StringBuilder();
                 name.append(fieldName);
                 for (int j = 0; j <= i; ++j) {
                     subArgs[2 * j] = field;
                     subArgs[2 * j + 1] = ARGS[j].toString();
                     name.append(j + 1);
                 }
                 subArgs[2 * i] = "name";
                 subArgs[2 * i + 1] = name.toString();
                 writer.addDocument(createDocument(subArgs));
             }
         }
         
         writer.close();
     }
     
     /**
      * Cleans the index up again, to ensure an empty lucene index in the next test.
      * @throws IOException if cleaning the index failed
      * @throws ParseException should not happen
      */
     @AfterClass
     public static void cleanLuceneIndex() throws IOException, ParseException {
         final IndexWriter writer = new IndexWriter(DIRECTORY, ANALYZER, MaxFieldLength.UNLIMITED);
         writer.deleteDocuments(new QueryParser(DEFAULT_FIELD, ANALYZER).parse("+empty:empty"));
         writer.close();
     }
 
 }
