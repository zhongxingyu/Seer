 package com.uni.mannheim.dws.mapper.engine.query;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Scanner;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Future;
 import java.util.regex.Pattern;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.log4j.Logger;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.index.DirectoryReader;
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.index.Term;
 import org.apache.lucene.search.BooleanClause;
 import org.apache.lucene.search.BooleanQuery;
 import org.apache.lucene.search.IndexSearcher;
 import org.apache.lucene.search.ScoreDoc;
 import org.apache.lucene.search.TermQuery;
 import org.apache.lucene.search.TopDocs;
 import org.apache.lucene.search.WildcardQuery;
 import org.apache.lucene.store.FSDirectory;
 
 import com.hp.hpl.jena.query.QuerySolution;
 import com.hp.hpl.jena.query.ResultSet;
 import com.uni.mannheim.dws.mapper.controller.ITupleProcessor;
 import com.uni.mannheim.dws.mapper.engine.index.DBPediaIndexBuilder;
 import com.uni.mannheim.dws.mapper.helper.dataObject.ResultDAO;
 import com.uni.mannheim.dws.mapper.helper.dataObject.SuggestedFactDAO;
 import com.uni.mannheim.dws.mapper.helper.util.Constants;
 import com.uni.mannheim.dws.mapper.helper.util.Utilities;
 import com.uni.mannheim.dws.mapper.wrapper.QueryAPIWrapper;
 
 /**
  * This class is an API for making query over the DBPedia indices
  * 
  * @author Arnab Dutta
  */
 public class QueryEngine
 {
 
     // The top k best matching results,
     private static int TOP_K = Constants.TOPK;
 
     // Default Constructor
     public QueryEngine()
     {
 
     }
 
     // setter for Top K parameter
     public static void setTopK(int topK)
     {
         TOP_K = topK;
     }
 
     // logger
     public static Logger logger = Logger.getLogger(QueryEngine.class.getName());
 
     /**
      * method accepts a user query and fetches over the indexed DBPedia data
      * 
      * @param userQuery the user provided search item
      * @param file
      * @return A List containing the matching DBPedia Entity URI as value
      * @throws Exception
      */
     public static List<ResultDAO> doSearch(String userQuery, File file) throws IOException
     {
         IndexReader reader = null;
         IndexSearcher searcher = null;
 
         Set<String> setURI = new HashSet<String>();
         List<ResultDAO> returnList = new ArrayList<ResultDAO>();
         Map<Integer, List<ResultDAO>> resultMap = new TreeMap<Integer, List<ResultDAO>>();
 
         long start = 0;
 
         TopDocs hits = null;
 
         try {
 
             // start timer
             start = Utilities.startTimer();
 
             // create index reader object
             // reader = IndexReader.open(FSDirectory.open(file));
             reader = DirectoryReader.open(FSDirectory.open(file));
 
             // create index searcher object
             searcher = new IndexSearcher(reader);
 
             // remove any un-necessary punctuation marks from the query
             userQuery = Pattern.compile("['\\s]").matcher(userQuery).replaceAll("");
 
             // frame a query on the surname field
             BooleanQuery subQuery = frameQuery(userQuery, "surname", "uriTextField2");
 
             // execute the search on top results
             hits = searcher.search(subQuery, null, Constants.MAX_RESULTS);
             iterateResult(searcher, setURI, resultMap, hits, userQuery);
 
             // then frame a query on the firstname field
             subQuery = frameQuery(userQuery, "firstname", "uriTextField1");
 
             hits = searcher.search(subQuery, null, Constants.MAX_RESULTS);
             iterateResult(searcher, setURI, resultMap, hits, userQuery);
 
             // still we have no result then perform wild card search
             if (hits.totalHits == 0 || resultMap.size() < TOP_K) {
 
                 hits =
                     searcher.search(new WildcardQuery(new Term("labelSmallField", userQuery.toLowerCase() + "*")),
                         null, Constants.MAX_RESULTS);
 
                 iterateResult(searcher, setURI, resultMap, hits, userQuery);
                 // TODO : Fuzzy even then
 
             }
 
             returnList = filterTopKResults(userQuery, returnList, resultMap);
 
         } catch (Exception ex) {
             logger.debug("NO MATCHING RECORDS FOUND FOR QUERY \"" + userQuery + "\" !! ");
         } finally {
             setURI.clear();
             setURI = null;
             Utilities.endTimer(start, "QUERY \"" + userQuery + "\" ANSWERED IN ");
         }
         return returnList;
     }
 
     /**
      * @param userQuery
      * @param returnList
      * @param resultMap
      * @return
      */
     public static List<ResultDAO> filterTopKResults(String userQuery, List<ResultDAO> returnList,
         Map<Integer, List<ResultDAO>> resultMap)
     {
         // iterate the result map to construct the return list of result data access objects
         for (Entry<Integer, List<ResultDAO>> entry : resultMap.entrySet()) {
             List<ResultDAO> value = entry.getValue();
             Integer key = entry.getKey();
 
             for (ResultDAO dao : value) {
                 if (returnList.size() != TOP_K) {
                     returnList.add(dao);
                     logger.info(dao + "  " + key);
                 } else {
                     return returnList;
                 }
             }
         }
        return null;
     }
 
     /**
      * @param userQuery user input term
      * @param field1 first argument denoting the index field to match to
      * @param field2 second argument denoting the index field to match
      * @return a Boolean query where both the indices should be matched
      */
     public static BooleanQuery frameQuery(String userQuery, String field1, String field2)
     {
         BooleanQuery subQuery = new BooleanQuery();
         subQuery.add(new TermQuery(new Term(field1, userQuery.toLowerCase())), BooleanClause.Occur.MUST);
         subQuery.add(new TermQuery(new Term(field2, userQuery.toLowerCase())), BooleanClause.Occur.MUST);
         return subQuery;
     }
 
     /**
      * @param searcher searcher instance
      * @param setURI a set to identify the unique URI s
      * @param resultMap a result map sorted by best matches
      * @param hits document hits instance
      * @param userQuery the user input coming from web interface or extraction engines
      * @throws IOException
      */
     public static void iterateResult(IndexSearcher searcher, Set<String> setURI,
         Map<Integer, List<ResultDAO>> resultMap, TopDocs hits, String userQuery) throws IOException
     {
         String labelField;
         String uriField;
         String uriTextField;
 
         double score;
         List<ResultDAO> list = null;
 
         // iterate over the results fetched after index search
         for (ScoreDoc scoredoc : hits.scoreDocs) {
             // Retrieve the matched document and show relevant details
             Document doc = searcher.doc(scoredoc.doc);
 
             uriTextField = doc.get("uriFullTextField");
             uriField = doc.get("uriField");
             labelField = doc.get("labelField");
             score = scoredoc.score / hits.getMaxScore();
 
             // only add the unique entries(URI and label combination)
             if (Utilities.checkUniqueness(setURI, uriField)) {
 
                 // the key is the sum of the levenstein edit distances of the query with the label and the query with
                 // the URI. Obviously, the best matching record will have both these edit distances minimum and an
                 // overall minimum score.
                 Integer key =
                     StringUtils.getLevenshteinDistance(userQuery, labelField.toLowerCase())
                         + StringUtils.getLevenshteinDistance(userQuery, uriTextField);
 
                 // Add to the result map, check for existing key, add or update the values accordingly
                 if (resultMap.containsKey(key)) {
                     resultMap.get(key).add(new ResultDAO(uriField, Math.round(score * 100)));
                 } else {
                     list = new ArrayList<ResultDAO>();
                     // logger.info(new ResultDAO(uriField, Math.round(score * 100)));
                     list.add(new ResultDAO(uriField, Math.round(score * 100)));
                     resultMap.put(key, list);
                 }
             }
         }
     }
 
     /**
      * wrapper method to spawn the actual search operation being carried out
      * 
      * @param pool
      * @param subjFromTuple
      * @param objFromTuple
      * @return
      * @throws ExecutionException
      * @throws InterruptedException
      */
     public static List<List<ResultDAO>> performSearch(ExecutorService pool, final String subjFromTuple,
         final String objFromTuple) throws InterruptedException, ExecutionException
     {
         List<List<ResultDAO>> retList = new ArrayList<List<ResultDAO>>();
 
         // The idea is we parallely process the two queries simultaneously and receive back the results
         // to the main thread i.e here. This is not possible with Thread class.
         Future<List<ResultDAO>> subjTask = pool.submit(new QueryAPIWrapper(subjFromTuple));
         Future<List<ResultDAO>> objTask = pool.submit(new QueryAPIWrapper(objFromTuple));
 
         // receive back the results from the two thread runners
         // and add them to the return collection
         retList.add(subjTask.get());
         retList.add(objTask.get());
 
         return retList;
     }
 
     /**
      * takes a list of subject and object from the DBPedia and tries to find all possible set of predicates connecting
      * these two two entities (subject and object)
      * 
      * @param subList argument 1 of {@link List } of {@link ResultDAO}
      * @param objList argument 2 of {@link List } of {@link ResultDAO}
      * @param actualPredicateFromIE
      */
     public static void fetchPredicates(final List<ResultDAO> subList, final List<ResultDAO> objList,
         final String actualPredicateFromIE)
     {
 
         List<String> possibleSubjs = new ArrayList<String>();
         List<String> possibleObjs = new ArrayList<String>();
         String sparqlQuery = null;
         List<QuerySolution> listQuerySols = new ArrayList<QuerySolution>();
 
         ResultSet results = null;
         String matchedProp = null;
 
         // take into consideration only those candidates having a score more than 80%
         for (int listCounter = 0; listCounter < subList.size(); listCounter++) {
             if (subList.get(listCounter).getScore() > Constants.THRESHOLD_SCORE) {
                 possibleSubjs.add(subList.get(listCounter).getFieldURI());
             } else {
                 break; // no need to iterate further, since the rest values are less than the desired score
             }
         }
 
         for (int listCounter = 0; listCounter < objList.size(); listCounter++) {
             if (objList.get(listCounter).getScore() > Constants.THRESHOLD_SCORE) {
                 possibleObjs.add(objList.get(listCounter).getFieldURI());
             } else {
                 break; // no need to iterate further, since the rest values are less than the desired score
             }
         }
 
         // we only take all possible subjects and objects if the score is more than te treshold and try to see from them
         // what possible
         // predicates we have
         for (String subj : possibleSubjs) {
             for (String obj : possibleObjs) {
 
                 sparqlQuery = "select ?predicates where {<" + subj + "> ?predicates <" + obj + ">} ";
                 /*
                  * "UNION {<" + obj + "> ?predicates <" + subj + ">}}";
                  */
                 logger.debug(sparqlQuery);
 
                 // fetch the result set
                 results = SPARQLEndPointQueryAPI.queryDBPediaEndPoint(sparqlQuery);
 
                 while (results.hasNext()) {
                     listQuerySols.add(results.nextSolution());
                 }
             }
         }
 
         logger.debug("'" + actualPredicateFromIE + "'" + " matches => ");
 
         // if we have some results proceed
         for (QuerySolution querySol : listQuerySols) {
             matchedProp = querySol.get("predicates").toString();
             logger.debug(matchedProp + "  ");
 
             // update the count for all such possibilities for a given predicate
             updatePredicateMap(actualPredicateFromIE, matchedProp);
         }
 
     }
 
     /**
      * @param actualPredicateFromIE
      * @param matchedProp
      */
     public static void updatePredicateMap(final String actualPredicateFromIE, String matchedProp)
     {
         HashMap<String, Integer> propertyVsCountMap = null;
         // the map contains the key already, then just update its value map
         if (ITupleProcessor.predicateSurfaceFormsMap.containsKey(actualPredicateFromIE)) {
             // update the count of this property first
             int countIEPredicate = ITupleProcessor.iePredicatesCountMap.get(actualPredicateFromIE);
             ITupleProcessor.iePredicatesCountMap.put(actualPredicateFromIE, countIEPredicate + 1);
 
             // retrieve the whole map which is the value against "actualPredicateFromIE"
             propertyVsCountMap = ITupleProcessor.predicateSurfaceFormsMap.get(actualPredicateFromIE);
 
             // if the current key is occurring in this map, increment its value
             if (propertyVsCountMap.containsKey(matchedProp)) {
                 int value = propertyVsCountMap.get(matchedProp);
                 propertyVsCountMap.put(matchedProp, value + 1);
 
                 // add also keep a global count of the occurrence of this "matchedProp"
                 int dbPediaPropCount = ITupleProcessor.dbPediaPredicatesCountMap.get(matchedProp);
                 ITupleProcessor.dbPediaPredicatesCountMap.put(matchedProp, dbPediaPropCount + 1);
 
             } else { // add this new key as a possible prediction of the surface form
                 propertyVsCountMap.put(matchedProp, 1);
                 ITupleProcessor.dbPediaPredicatesCountMap.put(matchedProp, 1);
             }
         } else { // add a new entry i.e a new predicate from IE data set
             propertyVsCountMap = new HashMap<String, Integer>();
             propertyVsCountMap.put(matchedProp, 1);
             ITupleProcessor.predicateSurfaceFormsMap.put(actualPredicateFromIE, propertyVsCountMap);
             ITupleProcessor.iePredicatesCountMap.put(actualPredicateFromIE, 1);
             ITupleProcessor.dbPediaPredicatesCountMap.put(matchedProp, 1);
         }
     }
 
     // stand alone test point
     public static void main(String[] ar) throws Exception
     {
 
         // flag to determine if u need to recompute the indices
         if (Constants.INDEX_AGAIN) {
             DBPediaIndexBuilder.indexer();
         }
         // create File object of our index directory
         File file = new File(Constants.DBPEDIA_ENT_INDEX_DIR);
 
         doSearch(Constants.SAMPLE_QUERY, file);
         // doLookUpSearch("agentcontributedtocreativework");
     }
 
     /**
      * fetch from file system the learnt the relationships
      * 
      * @param predicate
      * @return
      */
     public static List<ResultDAO> doLookUpSearch(final String predicate)
     {
         List<ResultDAO> returnList = new ArrayList<ResultDAO>();
         File file = new File(Constants.PREDICATE_FREQ_FILEPATH);
         Scanner sc;
         try {
             sc = new Scanner(file);
 
             while (sc.hasNextLine()) {
                 String[] parts = sc.nextLine().split("->");
 
                 if (parts[0].contains(predicate)) {
                     String[] elem;
                     String[] match;
 
                     if (parts.length > 1) {
                         elem = parts[1].split(",");
                     } else {
                         elem = parts[0].split(",");
                     }
 
                     match = elem[0].split("~");
                     double topScore = Double.parseDouble(match[1]);
                     returnList.add(new ResultDAO(match[0], Math.round(topScore / topScore * 100)));
                     logger.debug(match[0] + "   " + match[1]);
                     if (elem.length > 1) {
                         match = elem[1].split("~");
                         returnList.add(new ResultDAO(match[0],
                             Math.round(Double.parseDouble(match[1]) / topScore * 100)));
                         logger.debug(match[0] + "   " + match[1]);
                     }
                 }
             }
             sc.close();
         } catch (FileNotFoundException e) {
             logger.error(e.getMessage());
         }
 
         return returnList;
     }
 }
