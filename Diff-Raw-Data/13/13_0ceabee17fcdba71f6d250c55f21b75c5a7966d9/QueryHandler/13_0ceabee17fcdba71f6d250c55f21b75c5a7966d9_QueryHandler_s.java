 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.TreeSet;
 import java.util.logging.Logger;
 
 
 public class QueryHandler {
 
     /** Remembers the documents IDs used to create the index.**/
     private DataSet dataSet;
     private Logger logger = Logger.getLogger(QueryHandler.class.getName());
 
     public QueryHandler(DataSet dataSet){
         this.dataSet = dataSet;
     }
 
     public ArrayList<String> retrieveDocumentsForQuery(Query query) {
 
         switch(query.getType()){
         case NOT:
             return handleNOTQuery(query);
         case AND:
             return handleANDQuery(query);
         case OR:
             return handleORQuery(query);
         case PHRASE:
             return handlePhraseQuery(query);
         case PROXIMITY:
             return handleProximityQuery(query);
         }
 
         logger.log(Config.LOG_LEVEL, "Query type " + query.getType() + " not recognized!");
 
         return null;
     }
 
 
     /**
      * It searches from every term given in the query and for each matched document
      * and term it creates a  map from DocIds to another Map<Term, Positions>. 
      * 
      * @param query The query object
      * @return a Map from DocIds to another map of terms to positions. 
      */
     private HashMap<String, HashMap<String, TreeSet<Integer>>>
             getDocIdsMatchingPhrase(Query query) {
         ArrayList<String> terms = query.getTerms();
         HashMap<String, HashMap<String, TreeSet<Integer>>> intersect =
                 new HashMap<String, HashMap<String, TreeSet<Integer>>>();
 
         for(String term : terms){
             TreeSet<DocIdEntry> docIDs = dataSet.getDocIdEntrySet(term);
             Iterator<DocIdEntry> iterator = docIDs.iterator();
 
             while(iterator.hasNext()){
                 DocIdEntry currEntry = iterator.next();
                 
                 HashMap<String, TreeSet<Integer>> termToPos = intersect.get(currEntry.getDocId());
                 if(termToPos == null) {
                     termToPos = new HashMap<String, TreeSet<Integer>>();
                     intersect.put(currEntry.getDocId(), termToPos);
                 }
 
                 termToPos.put(term, currEntry.getPositions());
             }
         }
         return intersect;
     }
 
 
     /**
      * Answers proximity queries
      *
      * @param query
      * @return A list of documents that map the query
      */
     private ArrayList<String> handleProximityQuery(Query query) {
 
         int distance = query.getProximityWindow();    
         HashMap<String, HashMap<String, TreeSet<Integer>>>
         intersect = getDocIdsMatchingPhrase(query);
 
 
         return null;
     }
 
     /**
      * Answers phrase queries
      *
      * @param query The PHRASE query that needs to be answered.
      * @return The list of documents that correspond to the query.
      */
     private ArrayList<String> handlePhraseQuery(Query query) {
         HashMap<String, HashMap<String, TreeSet<Integer>>> commonDocumentEntries =
                 getDocIdsMatchingPhrase(query);
         ArrayList<String> matchingDocs = new ArrayList<String>();
         
         // Filters the documents that contain the whole phrase.
         for (String documentId : commonDocumentEntries.keySet()) {
             TreeSet<Integer> positions =
                     commonDocumentEntries.get(documentId).get(query.getTerm(0));
             
             for (int i = 1; i < query.getTerms().size(); i++) {
                 ArrayList<Integer> incrementedPositions = new ArrayList<Integer>();
                 
                 // Increment the positions from the previous term.
                 for (Iterator<Integer> it = positions.iterator(); it.hasNext(); ) {
                     incrementedPositions.add(it.next() + 1);
                 }
                 positions = commonDocumentEntries.get(documentId).get(query.getTerm(i));
                 positions.retainAll(incrementedPositions);
             }
             
             if (!positions.isEmpty()) {
                 matchingDocs.add(documentId);
             }
         }
         
         return matchingDocs;
     }
 
     /**
      * It answers queries that contain only NOT operators
      *
      * Queries can be of form: A NOT B. The result returned
      * is of all documents containing A and that do not contain B.
      * In case the query is: NOT A. The method returns
      * all documents that do not contain A.
      *
      * @param query The NOT Query that needs to be answered
      * @return A list of documents that map the query
      */
     public ArrayList<String> handleNOTQuery(Query query){
 
         TreeSet<String> matchingDocs = new TreeSet<String>();
 
         ArrayList<String> terms = query.getTerms();
         for(String term:terms){
             TreeSet<String> matchedDocs = dataSet.getDocIdSet(term);
             matchingDocs.addAll(matchedDocs);
         }
 
         if(matchingDocs.isEmpty()){
             matchingDocs = dataSet.getDocSet();
         }
 
         ArrayList<String> notTerms = query.getNotTerms();
         for(String notTerm : notTerms){
             matchingDocs.removeAll(dataSet.getDocIdSet(notTerm));
         }
 
 
         return new ArrayList<String>(matchingDocs);
     }
 
     /**
      * Answers queries that contain only AND operators
      *
      * @param query
      * @return A list of documents that map the query
      */
     public ArrayList<String> handleANDQuery(Query query) {
 
         // TODO: We could define different orders. For each permutation, we could have an order.
         // We first add the list for the first term, and then intersect with the others.
 

         TreeSet<String> matchingDocs =
             new TreeSet<String>(dataSet.getDocIdSet(query.getTerm(0)));
 
         for (String term: query.getTerms()) {
             matchingDocs.retainAll(dataSet.getDocIdSet(term));
         }
 
         return new ArrayList<String>(matchingDocs);
     }
 
     /**
      * Answers queries that contain only OR operators
      *
      * @param query
      * @return A list of documents that map the query
      */
     public ArrayList<String> handleORQuery(Query query) {
 
         // TODO: We could define different orders. For each permutation, we could have an order.
         // We first add the list for the first term, and then union with the others.
 
         TreeSet<String> matchingDocs = new TreeSet<String>();
 
         for (String term: query.getTerms()) {
             matchingDocs.addAll(dataSet.getDocIdSet(term));
         }
 
         return new ArrayList<String>(matchingDocs);
     }
 }
