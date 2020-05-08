 package edu.cmu.lti.oaqa.openqa.hellobioqa.retrieval;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Scanner;
 
 import org.apache.solr.common.SolrDocument;
 import org.apache.solr.common.SolrDocumentList;
 import org.apache.uima.UimaContext;
 import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
 import org.apache.uima.resource.ResourceInitializationException;
 import org.jsoup.Jsoup;
 
 import edu.cmu.lti.oaqa.core.provider.solr.SolrWrapper;
 import edu.cmu.lti.oaqa.cse.basephase.retrieval.AbstractRetrievalStrategist;
 import edu.cmu.lti.oaqa.framework.data.Keyterm;
 import edu.cmu.lti.oaqa.framework.data.RetrievalResult;
 import edu.cmu.lti.oaqa.openqa.hello.retrieval.SimpleSolrRetrievalStrategist;
 import edu.cmu.lti.oaqa.openqa.hellobioqa.keyterm.QueryExpansion;
 
 public class HuskyRetrievalStrategist extends AbstractRetrievalStrategist {
 
   protected Integer hitListSize = 50;
 
   protected double tfidfWeight = 0.5;
 
   protected SolrWrapper wrapper;
 
   protected static final double TFIDF_WEIGHT = 0.10;
 
   /**
    * This method returns the distribution (counts) of each word in the query in a hashmap
    * 
    * @param query
    * @param doc
    * @return
    */
   public HashMap<String, Integer> getDocQueryDist(String query, SolrDocument doc) {
     // prepare the HashMap for result
     HashMap<String, Integer> distMap = new HashMap<String, Integer>();
     String[] queryTerms = query.toLowerCase().split("\\s");
     for (String term : queryTerms) {
       distMap.put(term, 0);
     }
 
     String htmlText = (String) ((ArrayList) doc.getFieldValue("text")).get(0);
     String cleanedText = Jsoup.parse(htmlText).text();
     String[] cleanedTextTokens = cleanedText.toLowerCase().split("\\s");
 
     for (String textToken : cleanedTextTokens) {
       if (distMap.containsKey(textToken)) {
         distMap.put(textToken, distMap.get(textToken) + 1);
       }
     }
 
     return distMap;
   }
 
   /**
    * This method gets the inversed document frequency of a term
    * 
    * @param query
    * @param docs
    * @return
    */
   public HashMap<String, Double> getIdf(String query, SolrDocumentList docs) {
     HashMap<String, Double> idfMap = new HashMap<String, Double>();
     String[] queryTerms = query.toLowerCase().split("\\s");
     for (String term : queryTerms) {
       idfMap.put(term, 0.0);
     }
 
     // get document frequency
     for (SolrDocument doc : docs) {
       String htmlText = (String) ((ArrayList) doc.getFieldValue("text")).get(0);
       String cleanedText = Jsoup.parse(htmlText).text();
       for (String term : queryTerms) {
         if (cleanedText.indexOf(term, 0) != -1) {
           idfMap.put(term, idfMap.get(term) + 1.0);
         }
       }
     }
 
     // inverse the document frequency
     double totalDocNum = docs.size();
     for (String term : idfMap.keySet()) {
       idfMap.put(term, Math.log(totalDocNum / (idfMap.get(term) + 1.0)));
     }
 
     return idfMap;
   }
 
   public void tfidfRescore(HashMap<String, Double> tfidfMap) {
     double min = Double.MAX_VALUE;
     double max = Double.MIN_VALUE;
 
     // get max and min
     for (String key : tfidfMap.keySet()) {
       double currTFIDF = tfidfMap.get(key);
       if (currTFIDF > max) {
         max = currTFIDF;
       }
       if (currTFIDF < min) {
         min = currTFIDF;
       }
     }
 
     // normalize the score
     for (String key : tfidfMap.keySet()) {
       double currTFIDF = tfidfMap.get(key);
       double newTFIDF = (currTFIDF - min) / (max - min);
       tfidfMap.put(key, newTFIDF);
     }
   }
 
   
   protected String formulateQuery(List<Keyterm> keyterms) {
     StringBuffer result = new StringBuffer();
     for (Keyterm keyterm : keyterms) {
       result.append(keyterm.getText() + " ");
     }
     String query = result.toString();
     System.out.println(" QUERY: " + query);
     return query;
   }
   
   /*
   protected String formulateQuery(List<Keyterm> keyterms) {
     // Query Expansion
     QueryExpansion qe = new QueryExpansion();
     List<Keyterm> expanded = qe.expand(keyterms);
     
     StringBuffer result = new StringBuffer();
     for (Keyterm keyterm : expanded) {
       result.append(keyterm.getText() + " ");
     }
     String query = result.toString();
     System.out.println("[UPDATED] QUERY: " + query);
     return query;
   }*/
 
   @Override
   public void collectionProcessComplete() throws AnalysisEngineProcessException {
     super.collectionProcessComplete();
     wrapper.close();
   }
 
   @Override
   public void initialize(UimaContext aContext) throws ResourceInitializationException {
     super.initialize(aContext);
     try {
       this.hitListSize = (Integer) aContext.getConfigParameterValue("hit-list-size");
     } catch (ClassCastException e) { // all cross-opts are strings?
       this.hitListSize = Integer.parseInt((String) aContext
               .getConfigParameterValue("hit-list-size"));
     }
 
     try {
       Integer rawWeight = (Integer) aContext.getConfigParameterValue("tfidf-weight");
       this.tfidfWeight = (double) rawWeight * 0.01;
     } catch (ClassCastException e) { // all cross-opts are strings?
       // this.tfidfWeight = Double.parseDouble((String) aContext
       // .getConfigParameterValue("tfidf-weight"));
       // this.tfidfWeight = Float.parseFloat((String) aContext
       // .getConfigParameterValue("tfidf-weight"));
       Integer rawWeight = Integer.parseInt((String) aContext
               .getConfigParameterValue("tfidf-weight"));
       this.tfidfWeight = (double) rawWeight * 0.01;
     }
 
     System.err.printf("Using tfidf weight %.4f, hit-list-size %d ... \n", tfidfWeight, hitListSize);
 
     String serverUrl = (String) aContext.getConfigParameterValue("server");
     Integer serverPort = (Integer) aContext.getConfigParameterValue("port");
     Boolean embedded = (Boolean) aContext.getConfigParameterValue("embedded");
     String core = (String) aContext.getConfigParameterValue("core");
     try {
       this.wrapper = new SolrWrapper(serverUrl, serverPort, embedded, core);
     } catch (Exception e) {
       throw new ResourceInitializationException(e);
     }
   }
 
   
   @Override
   protected List<RetrievalResult> retrieveDocuments(String question, List<Keyterm> keyterms) {
    QueryExpansion qe = new QueryExpansion();
    List<Keyterm> expanded = qe.expand(keyterms);
    String query = formulateQuery(expanded);
    //String query = formulateQuery(keyterms);
     return retrieveDocuments(query);
   }
 
   private List<RetrievalResult> retrieveDocuments(String query) {
     List<RetrievalResult> result = new ArrayList<RetrievalResult>();
     try {
       System.err.printf("Running query with %d hitListSize ... \n", this.hitListSize);
       SolrDocumentList docs = wrapper.runQuery(query, hitListSize);
 
       // remove the duplicates from query
       String[] queryTokens = query.toLowerCase().split("\\s");
       HashMap<String, Integer> tokensMap = new HashMap<String, Integer>();
       for (String q : queryTokens) {
         tokensMap.put(q, 1);
       }
       String uniqueQuery = "";
       for (String q : tokensMap.keySet()) {
         uniqueQuery += q + " ";
       }
       uniqueQuery = uniqueQuery.trim();
 
       // get inversed document frequency
       System.err.println("Calling getIdf ... ");
       HashMap<String, Double> idfMap = getIdf(uniqueQuery, docs);
       HashMap<String, Double> tfidfMap = new HashMap<String, Double>();
       HashMap<String, Float> originalScoreMap = new HashMap<String, Float>();
 
       for (SolrDocument doc : docs) {
         // RetrievalResult r = new RetrievalResult((String) doc.getFieldValue("id"),
         // (Float) doc.getFieldValue("score"), query);
 
         // System.out.println("Text value: \n" + doc.getFieldValue("text"));
         System.err.println("Calling dist ... ");
         HashMap<String, Integer> distMap = getDocQueryDist(uniqueQuery, doc);
         originalScoreMap.put((String) doc.getFieldValue("id"), (Float) doc.getFieldValue("score"));
 
         double totalTFIDF = 0.0;
         // String[] queryTerms = uniqueQuery.toLowerCase().split("\\s");
         for (String token : queryTokens) {
           double tf = distMap.get(token);
           double idf = idfMap.get(token);
           totalTFIDF += tf * idf;
           System.err.printf("Term: %s, tf: %f, idf: %f, tf*idf: %f ... \n", token, tf, idf, tf
                   * idf);
         }
 
         // stores the tfidf for this document and this query for reranking
         tfidfMap.put((String) doc.getFieldValue("id"), totalTFIDF);
         System.err.printf("tf*idf for doc %s : %f, score is %f \n", doc.getFieldValue("id"),
                 totalTFIDF, (Float) doc.getFieldValue("score"));
 
         // result.add(r);
         System.out.println(doc.getFieldValue("id"));
       }
 
       // tf*idf reranking
       tfidfRescore(tfidfMap);
 
       // final interpolation
       for (String docId : tfidfMap.keySet()) {
         double originalScore = originalScoreMap.get(docId);
         double tfidfScore = tfidfMap.get(docId);
         double finalScore = originalScore * (1.0 - tfidfWeight) + tfidfScore * tfidfWeight;
         RetrievalResult r = new RetrievalResult(docId, (float) finalScore, query);
         result.add(r);
       }
 
     } catch (Exception e) {
       System.err.println("Error retrieving documents from Solr: " + e);
     }
     return result;
   }
 }
