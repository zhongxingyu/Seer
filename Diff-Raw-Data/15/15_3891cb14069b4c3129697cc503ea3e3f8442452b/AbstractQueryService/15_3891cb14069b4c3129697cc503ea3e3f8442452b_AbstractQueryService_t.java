 package uk.ac.ebi.chemet.service.query;
 
 import org.apache.log4j.Logger;
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.analysis.TokenStream;
 import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.index.Term;
 import org.apache.lucene.queryParser.ParseException;
 import org.apache.lucene.queryParser.QueryParser;
 import org.apache.lucene.search.*;
 import org.apache.lucene.store.Directory;
 import org.apache.lucene.util.Version;
 
 import uk.ac.ebi.interfaces.identifiers.Identifier;
 import uk.ac.ebi.service.index.LuceneIndex;
 import uk.ac.ebi.service.query.QueryService;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.*;
 import java.util.prefs.Preferences;
 
 /**
  * AbstractQueryService - 23.02.2012 <br/>
  * <p/>
  * Provides a base for which other lucene query services can build upon. This
  * class provides a lot of utility methods for building queries, accessing score docs
  * and field values.
  *
  * @author johnmay
  * @author $Author$ (this version)
  * @version $Rev$
  */
 public abstract class AbstractQueryService<I extends Identifier>
         implements QueryService<I> {
 
     private static final Logger LOGGER = Logger.getLogger(AbstractQueryService.class);
 
     private Document[] documents;
     private Directory directory;
     private Analyzer analyzer;
     private IndexReader reader;
     private IndexSearcher searcher;
 
     private LuceneIndex index;
 
     private Map<String, QueryParser> parserMap = new HashMap<String, QueryParser>();
 
     private int max = Preferences.userNodeForPackage(AbstractQueryService.class).getInt("default.max.results", 100);
     private float minSimilarity = 0.5f; // for fuzzy queries
 
 
     public AbstractQueryService(LuceneIndex index) {
 
         this.index = index;
 
         try {
             analyzer = index.getAnalyzer();
             setDirectory(index.getDirectory());
             searcher = new IndexSearcher(directory, true);
         } catch (IOException ex) {
             LOGGER.error("Could not create query service", ex);
         }
     }
 
     /**
      * Access the search used by the service
      *
      * @return
      */
     public IndexSearcher getSearcher() {
         return searcher;
     }
 
     /**
      * Set the directory for the index, this will also open the index
      * reader
      *
      * @param directory
      *
      * @throws IOException
      */
     public void setDirectory(Directory directory) throws IOException {
         if (reader != null) {
             reader.close();
         }
         this.directory = directory;
         reader = IndexReader.open(directory, true);
         documents = new Document[reader.numDocs()];
     }
 
     /**
      * Set the anlayzer for the index
      *
      * @param analyzer
      */
     public void setAnalyzer(Analyzer analyzer) {
         this.analyzer = analyzer;
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void setMaxResults(int max) {
         this.max = max;
     }
 
 
     /**
      * Accessor for the stored max results to fetch
      *
      * @return number of results
      */
     public int getMaxResults() {
         return max;
     }
 
     /**
      * @inheritDoc
      */
     @Override
     public void setMinSimilarity(float minSimilarity) {
         this.minSimilarity = minSimilarity;
     }
 
     /**
      * Accessor for the stored minimum similarity
      *
      * @return percentage of similarity
      */
     public float getMinSimilarity() {
         return minSimilarity;
     }
 
 
     /**
      * @inheritDoc
      */
     @Override
     public boolean isAvailable() {
         return directory != null
                 && analyzer != null
                 && index != null
                 && index.isAvailable();
     }
 
     /**
      * Access the analyzer for this service
      *
      * @return instance of the analyzer (or null if initialization failed)
      */
     public Analyzer getAnalyzer() {
         return analyzer;
     }
 
     /**
      * Convenience method to access the first score document for
      * a given query. If multiple documents are found then an warning
      * is logged.
      *
      * @param query search-able query
      *
      * @return the first score document for the query
      */
     public ScoreDoc first(Query query) {
 
         ScoreDoc[] scoreDocs = search(query, TopScoreDocCollector.create(5, true));
 
         if (scoreDocs.length > 1) {
             LOGGER.warn("Expected a single hit");
         }
 
         return scoreDocs.length > 0 ? scoreDocs[0] : null;
 
     }
 
     /**
      * Search the index with the provided query. A new TopScoreDocCollector
      * is created using and constrained using the value of
      * {@see getMaxResults()}.
      * If an exception occurs an empty array of ScoreDoc's is returned.
      *
      * @param query search-able query
      *
      * @return the score documents for the query
      */
     public ScoreDoc[] search(Query query) {
         return search(query, TopScoreDocCollector.create(max, true));
     }
 
     /**
      * Search the index with the provided query and TopScoreDocCollector.
      * If an exception occurs an empty array of ScoreDoc's is returned
      *
      * @param query     search-able query
      * @param collector the TopScoreDocCollector to use
      *
      * @return the score documents
      */
     public ScoreDoc[] search(Query query, TopScoreDocCollector collector) {
 
         try {
             searcher.search(query, collector);
             return collector.topDocs().scoreDocs;
         } catch (IOException ex) {
             LOGGER.warn("Unable to search");
         }
 
         return new ScoreDoc[0];
 
     }
 
     /**
      * Access the binary value of the field with the given name. If multiple value's exist
      * this method will return the first value added.
      *
      * @param scoreDoc scored document to access the value for
      * @param field    name of the field to retrieve the value from
      *
      * @return binary value of the field
      *
      * @throws IOException
      */
     public byte[] binaryValue(ScoreDoc scoreDoc, String field) throws IOException {
         return getDocument(scoreDoc).getBinaryValue(field);
     }
 
     /**
      * Access the string values of the field with the given name. If multiple value's exist
      * this method will return the first value added.
      *
      * @param scoreDoc scored document to access the value for
      * @param field    name of the field to retrieve the value from
      *
      * @return string values of the field
      *
      * @throws IOException
      */
     public byte[][] binaryValues(ScoreDoc scoreDoc, String field) throws IOException {
         return getDocument(scoreDoc).getBinaryValues(field);
     }
 
 
     /**
      * Access the string value of the field with the given name. If multiple value's exist
      * this method will return the first value added.
      *
      * @param scoreDoc scored document to access the value for
      * @param field    name of the field to retrieve the value from
      *
      * @return string value of the field
      *
      * @throws IOException
      */
     public String value(ScoreDoc scoreDoc, String field) throws IOException {
         return getDocument(scoreDoc).get(field);
     }
 
 
     /**
      * Access the string values of the field with the given name.
      *
      * @param scoreDoc scored document to access the value for
      * @param field    name of the field to retrieve the value from
      *
      * @return string values of the field
      *
      * @throws IOException
      */
     public String[] values(ScoreDoc scoreDoc, String field) throws IOException {
         return getDocument(scoreDoc).getValues(field);
     }
 
 
     /**
      * Access the document corresponding to the provided score doc. this method
      * buffers access to the index reader allowing simple document retrieval
      *
      * @param document the score to to retrieve the {@see Document} for
      *
      * @return instance of the Document for the provided ScoreDoc
      *
      * @throws IOException thrown if no document was found
      */
     public Document getDocument(ScoreDoc document) throws IOException {
 
         int index = document.doc;
 
         if (documents[index] == null) {
             documents[index] = reader.document(index);
         }
 
         return documents[index];
     }
 
     /**
      * Access the query parse for the specified term
      *
      * @param term the field to get the parser for
      *
      * @return query parser for the given term
      */
     public QueryParser getParser(Term term) {
         return getParser(term.field());
     }
 
     /**
      * Access the query parse for the specified field
      *
      * @param field the field to get the parser for
      *
      * @return query parser for the given field
      */
     public QueryParser getParser(String field) {
         if (!parserMap.containsKey(field)) {
             parserMap.put(field,
                           new QueryParser(Version.LUCENE_34, field, analyzer));
         }
         return parserMap.get(field);
     }
 
     /**
      * Parse the query for the given term/field. This method
      * will use the appropriate query parser to construct
      * a query from the 'query' parameter. This allows
      * flexibility when creating a query and parsing of more
      * complex searches:
      * <p/>
      * <pre>{@code
      *      // variable length query
      *      parse("start of a name*", NameService.NAME);
      * }</pre>
      *
      * @param query string query
      * @param term  the field to search
      *
      * @return searchable query
      */
     public Query parse(String query, Term term) {
 
         QueryParser parser = getParser(term);
 
         try {
             return parser.parse(query);
         } catch (ParseException ex) {
             LOGGER.error("Could not parse query " + query, ex);
         }
 
         return new TermQuery(term.createTerm(query));
 
     }
 
     /**
      * Construct a non-approximate query without using the QueryParser. This is useful
      * when you want to search an field that is analyzed and maintain space's. The
      * token stream is converted into a boolean 'Must Occur' query. For most simple
      * queries this method can be used.
      *
      * @param text text to construct the query for
      * @param term the field to search the text in
      *
      * @return searchable query
      */
     public Query construct(String text, Term term) {
         return construct(text, term, false);
     }
 
     /**
      * Construct a query without using the QueryParser. This is useful when you want
      * to search an field that is analyzed and maintain space's. The token stream is
      * converted into a boolean 'Must Occur' query. For most simple queries this
      * method can be used. The approximate flag allows construction of approximate
      * {@see FuzzyMatch} queries for each token. The similarity for the fuzzy match
      * can be set via the {@see setMinSimilarity(float)} method.
      *
      * @param text        text to construct the query for
      * @param term        the field to search the text in
      * @param approximate whether to use approximate search
      *
      * @return searchable query
      */
     public Query construct(String text, Term term, boolean approximate) {
 
         StringReader reader = new StringReader(text);
         TokenStream stream = analyzer.tokenStream(term.field(), reader);
 
         BooleanQuery query = new BooleanQuery();
 
         CharTermAttribute termAttribute = stream.getAttribute(CharTermAttribute.class);
 
         try {
             while (stream.incrementToken()) {
 
                 Term termToken = term.createTerm(termAttribute.toString());
 
                 Query subQuery = approximate
                         ? new FuzzyQuery(termToken, getMinSimilarity())
                         : new TermQuery(termToken);
 
                 query.add(subQuery, BooleanClause.Occur.MUST);
 
             }
         } catch (IOException ex) {
             LOGGER.error("Could not constructing query ", ex);
         }
 
         return query;
     }
 
 
     /**
      * Convenience method that allows retrieval of the a value in the
      * given term/field for the provided identifier. An example would be accessing
      * the preferred name for an identifier.
      * <p/>
      * <pre>{@code
      *      return firstValue(identifier, PreferredNameService.PREFERRED_NAME);
      * }</pre>
      *
      * @param identifier identifier to search for
      * @param term       the term/field to access
      *
      * @return value stored in the field for the identifier (empty string it
      *         not found)
      */
     public String firstValue(Identifier identifier, Term term) {
         return firstValue(construct(identifier.getAccession(), IDENTIFIER),
                           term.field());
     }
 
     /**
      * Convenience method that allows retrieval of the all values in the
      * given term for the provided identifier. An example would be accessing
      * all synonyms for an identifier.
      * <p/>
      * <pre>{@code
      *      return firstValues(identifier, SynonymService.SYNONYM);
      * }</pre>
      *
      * @param identifier identifier to search for
      * @param term       the term/field to access
      *
      * @return collection of values
      */
     public Collection<String> firstValues(Identifier identifier, Term term) {
         return firstValues(construct(identifier.getAccession(), IDENTIFIER),
                            term.field());
     }
 
     /**
      * Convenience method to access the value for the specified field in
      * the first document returned by the query. If no values are found
      * an empty array is returned. If multiple are found the first is
      * returned.
      *
      * @param query the search query
      * @param term  field to access
      *
      * @return binary value for specified field in the first document
      */
     public byte[] firstBinaryValue(Query query, Term term) {
         return firstBinaryValue(query, term.field());
     }
 
     /**
      * Access the value for the specified field in the first document
      * returned by the query. If no values are found an empty array is
      * returned. If multiple are found the first is returned.
      *
      * @param query the search query
      * @param field field to access
      *
      * @return binary value for specified field in the first document
      */
     public byte[] firstBinaryValue(Query query, String field) {
 
         ScoreDoc scoreDoc = first(query);
 
         try {
 
             byte[] value = binaryValue(scoreDoc, field);
 
             if (value != null)
                 return value;
 
         } catch (Exception ex) {
             LOGGER.error("Could not access field value " + field + " in service " + getClass(), ex);
         }
 
         return new byte[0];
 
     }
 
     /**
      * Access the value for the specified field in the first document
      * returned by the query. If no values are found an empty string is
      * returned. If multiple are found the first is returned.
      *
      * @param query the search query
      * @param term  term to access
      *
      * @return value for specified field in the first document
      */
     public String firstValue(Query query, Term term) {
         return firstValue(query, term.field());
     }
 
     /**
      * Access the value for the specified field in the first document
      * returned by the query. If no values are found an empty string is
      * returned. If multiple are found the first is returned.
      *
      * @param query the search query
      * @param field field to access
      *
      * @return value for specified field in the first document
      */
     public String firstValue(Query query, String field) {
 
         ScoreDoc scoreDoc = first(query);
 
         try {
 
             String value = value(scoreDoc, field);
 
             if (value != null)
                 return value;
 
         } catch (Exception ex) {
             LOGGER.error("Could not access field value " + field + " in service " + getClass(), ex);
         }
 
         return "";
 
     }
 
     /**
      * Convenience method to access the all-values for the specified field in
      * the first document returned by the query. If no values are found an empty
      * collection is returned.
      *
      * @param query the search query
      * @param term  term to access
      *
      * @return all values for specified field in the first document
      */
     public Collection<String> firstValues(Query query, Term term) {
         return firstValues(query, term.field());
     }
 
     /**
      * Access the all-values for the specified field in the first document
      * returned by the query. If no values are found an empty collection is
      * returned.
      *
      * @param query the search query
      * @param field field to access
      *
      * @return all values for specified field in the first document
      */
     public Collection<String> firstValues(Query query, String field) {
 
         ScoreDoc scoreDoc = first(query);
 
         List<String> values = new NonNullList<String>(5);
 
         if (scoreDoc == null)
             return values;
 
         try {
             for (String value : values(scoreDoc, field)) {
                 values.add(value);
             }
         } catch (Exception ex) {
             LOGGER.error("Could not access field value " + field + " in service " + getClass(), ex);
         }
 
         return values;
 
     }
 
     /**
      * Convenience method to access the values for the specified field for all score
      * documents returned by the query. If multiple values exists for the term only the
      * first value will be returned. To access multi-value fields for each document
      * please use {@see allValues(Query, Term)}.Null values are suppressed and
      * not returned, if no values match and empty collection is returned.
      *
      * @param query search query
      * @param term  the term to access the value for
      *
      * @return ranked list of the first value of the specified field for each
      *         document returned by the query
      */
     public Collection<String> values(Query query, Term term) {
         return values(query, term.field());
     }
 
     /**
      * Access the values for the specified field for all score documents
      * returned by the query. If multiple values exists for the field only the
      * first value will be returned. To access multi value fields for each document
      * please use {@see allValues(Query, Term)}. Null values are suppressed and
      * not returned, if no values match and empty collection is returned.
      *
      * @param query search query
      * @param field the field to access the value for
      *
      * @return ranked list of the first value of the specified field for each
      *         document returned by the query
      */
     public Collection<String> values(Query query, String field) {
 
         ScoreDoc[] scoreDocs = search(query);
         List<String> values = new NonNullList<String>(scoreDocs.length);
 
         try {
             for (ScoreDoc scoreDoc : scoreDocs) {
                 values.add(value(scoreDoc, field));
             }
         } catch (Exception ex) {
             LOGGER.error("Could not access field value " + field + " in service " + getClass(), ex);
         }
 
         return values;
 
     }
 
     /**
      * Convenience method to access using a given term. This method allows
      * access to the values for the specified term. If multiple values exists
      * all values for that term will be added to the returned collection.
      * Null values are suppressed and
      * not returned, if no values match and empty collection is returned.
      *
      * @param query search query
      * @param term  the field term to retrieve the values for
      *
      * @return Aggregated collection of values from single or multi-value fields
      */
     public Collection<String> allValues(Query query, Term term) {
         return allValues(query, term.field());
     }
 
     /**
      * Access the values for the specified field. If multiple values exists
      * all values for that field will be added to the returned collection.
      * Null values are suppressed and
      * not returned, if no values match and empty collection is returned.
      *
      * @param query search query
      * @param field the field to retrieve the values for
      *
      * @return Aggregated collection of values from single or multi-value fields
      */
     public Collection<String> allValues(Query query, String field) {
 
         ScoreDoc[] scoreDocs = search(query);
         List<String> values = new NonNullList<String>(scoreDocs.length);
 
         try {
             for (ScoreDoc scoreDoc : scoreDocs) {
                 for (String value : values(scoreDoc, field)) {
                     values.add(value);
                 }
             }
         } catch (Exception ex) {
             LOGGER.error("Could not access field value " + field + " in service " + getClass(), ex);
         }
 
         return values;
 
     }
 
     /**
      * Access a collection of identifiers for the given query. If you
      * have duplicate identifier fields in your index this method's
      * return may contain duplicate also. The identifier's are constructed
      * from the {@see QueryService#IDENTIFIER} field an subsequently if
      * that field is missing an empty set is returned.
      *
      * @param query the query to search for
      *
      * @return Ranked list of identifiers for the given query
      */
     public Collection<I> getIdentifiers(Query query) {
 
         Collection<I> identifiers = new ArrayList<I>();
         I base = getIdentifier();
 
         for (String value : values(query, IDENTIFIER)) {
             I identifier = (I) base.newInstance();
             identifier.setAccession(value);
            identifiers.add(identifier);
         }
 
         return identifiers;
 
     }
 
 
     /**
      * Simple utility class that will suppress null elements. An
      * alternative would be the Constraints available in Guava
      * but that implementation throws an NullPointerException
      * if an null is attempted to be added
      *
      * @param <O>
      */
     class NonNullList<O> extends ArrayList<O> {
 
         public NonNullList() {
             super();
         }
 
         public NonNullList(int capacity) {
             super(capacity);
         }
 
         @Override
         public boolean add(O o) {
             if (o != null) return super.add(o);
             return false;
         }
 
         @Override
         public boolean addAll(Collection<? extends O> c) {
             boolean changed = false;
             for (O o : c)
                 changed = add(o) || changed;
             return changed;
         }
     }
 
 }
