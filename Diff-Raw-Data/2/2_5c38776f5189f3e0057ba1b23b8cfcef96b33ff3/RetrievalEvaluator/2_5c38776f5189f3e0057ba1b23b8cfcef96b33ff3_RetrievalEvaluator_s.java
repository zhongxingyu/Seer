 /** RetrievalEvaluator.java
  * @author Weston Feely
  */
 package edu.cmu.lti.f13.hw4.hw4_wfeely.casconsumers;
 
 import java.io.IOException;
 import java.util.*;
 
 import org.apache.uima.cas.CAS;
 import org.apache.uima.cas.CASException;
 import org.apache.uima.cas.FSIterator;
 import org.apache.uima.collection.CasConsumer_ImplBase;
 import org.apache.uima.jcas.JCas;
 import org.apache.uima.jcas.cas.FSList;
 import org.apache.uima.resource.ResourceInitializationException;
 import org.apache.uima.resource.ResourceProcessException;
 import org.apache.uima.util.ProcessTrace;
 
 import edu.cmu.lti.f13.hw4.hw4_wfeely.typesystems.Document;
 import edu.cmu.lti.f13.hw4.hw4_wfeely.typesystems.Token;
 import edu.cmu.lti.f13.hw4.hw4_wfeely.utils.Utils;
 
 public class RetrievalEvaluator extends CasConsumer_ImplBase {
 
   /** query id number **/
   public ArrayList<Integer> qIdList;
 
   /** query and text relevant values **/
   public ArrayList<Integer> relList;
 
   /** global vocabulary **/
   public HashMap<String, Integer> vocab;
 
   /** document list **/
   public ArrayList<Doc> docs;
 
   /** max query ID **/
   public int maxQID;
 
   /** query sets list **/
   public ArrayList<QuerySet> querySets;
 
   /** inverse-document-frequency vector **/
   HashMap<String, Double> idf;
 
   public void initialize() throws ResourceInitializationException {
 
     qIdList = new ArrayList<Integer>();
 
     relList = new ArrayList<Integer>();
 
     vocab = new HashMap<String, Integer>();
 
     docs = new ArrayList<Doc>();
 
     maxQID = 0;
 
     querySets = new ArrayList<QuerySet>();
 
     idf = new HashMap<String, Double>();
   }
 
   /**
    * DONE: 1. construct the global word dictionary 2. keep the word frequency for each sentence
    */
   @Override
   public void processCas(CAS aCas) throws ResourceProcessException {
 
     JCas jcas;
     try {
       jcas = aCas.getJCas();
     } catch (CASException e) {
       throw new ResourceProcessException(e);
     }
 
     FSIterator it = jcas.getAnnotationIndex(Document.type).iterator();
 
     if (it.hasNext()) {
       // set up document
       Document doc = (Document) it.next();
       Doc myDoc = new Doc();
       myDoc.text = doc.getText();
       myDoc.queryID = doc.getQueryID();
       myDoc.relevanceValue = doc.getRelevanceValue();
 
       // check queryID against max queryID
       if (myDoc.queryID > maxQID)
         maxQID = myDoc.queryID;
 
       // set up tokenList for this document
       FSList fsTokenList = doc.getTokenList();
       ArrayList<Token> tokenList = Utils.fromFSListToCollection(fsTokenList, Token.class);
 
       // set up word frequency vector for this document
       myDoc.f = new HashMap<String, Integer>();
 
       // add QueryID and RelevanceValue to qId list and rel list
       qIdList.add(doc.getQueryID());
       relList.add(doc.getRelevanceValue());
 
       // update global vocabulary with tokens from this document
       for (Token tok : tokenList) {
         String word = tok.getText();
         Integer freq = tok.getFrequency();
         // put each token into global vocabulary, updating global frequencies
         if (vocab.containsKey(word))
           vocab.put(word, freq + 1);
         else
           vocab.put(word, freq);
         // put each token into document word frequency vector
         myDoc.f.put(word, freq);
       }
 
       // add myDoc to documents array
       docs.add(myDoc);
     }
   }
 
   /**
    * 1. Compute Cosine Similarity and rank the retrieved sentences 2. Compute the MRR metric
    */
   @Override
   public void collectionProcessComplete(ProcessTrace arg0) throws ResourceProcessException,
           IOException {
 
     super.collectionProcessComplete(arg0);
 
     // set up query, result sets
     for (int i = 1; i <= maxQID; i++) {
       // make a new query set
       QuerySet myQuerySet = new QuerySet();
       myQuerySet.qID = i;
       myQuerySet.docSet = new ArrayList<Doc>();
       // loop through docs
       for (Doc myDoc : docs) {
         // add each document in this query set to the list
         if (myDoc.queryID == i) {
           // set query if this doc's relValue is 99
           if (myDoc.relevanceValue == 99)
             myQuerySet.query = myDoc;
           // otherwise, add to docSet
           else
             myQuerySet.docSet.add(myDoc);
         }
       }
       // add this query set to list of querySets
       querySets.add(myQuerySet);
     }
 
     // calculate idf vector
     // get number of documents
     int D = docs.size();
 
     for (String term : vocab.keySet()) {
       // get total number of documents this term occurs in
       int k = 0;
       for (Doc d : docs) {
         if (d.f.keySet().contains(term))
           k++;
       }
       // calculate idf weight and put into idf vector
      double weight = Math.log((double) D / (double) k);
       idf.put(term, weight);
     }
 
     // compute the cosine similarity measure
     for (QuerySet myQuerySet : querySets)
       for (Doc result : myQuerySet.docSet) {
         // DEBUG: print query and document text
         /*
          * System.out.println("Query: " + myQuerySet.query.text); System.out.println("Doc: " +
          * result.text);
          */
         // compute cosine similarity
         result.cosineSimilarity = computeCosineSimilarity(myQuerySet.query.f, result.f);
       }
 
     // compute the rank of retrieved sentences
     for (QuerySet myQuerySet : querySets) {
       myQuerySet.ranking = new ArrayList<Doc>();
       while (myQuerySet.docSet.size() > 0) {
         double bestScore = 0.0;
         int bestIndex = 0;
         for (int j = 0; j < myQuerySet.docSet.size(); j++) {
           double docScore = myQuerySet.docSet.get(j).cosineSimilarity;
           if (docScore > bestScore) {
             bestScore = docScore;
             bestIndex = j;
           }
         }
         myQuerySet.ranking.add(myQuerySet.docSet.get(bestIndex));
         myQuerySet.docSet.remove(bestIndex);
       }
     }
 
     // DEBUG: print full rankings
     /*
      * for (QuerySet myQuerySet : querySets) { Doc query = myQuerySet.query;
      * System.out.println("Query| rel=" + query.relevanceValue + " qid=" + query.queryID + " " +
      * query.text); for (int i = 0; i < myQuerySet.ranking.size(); i++) { Doc myDoc =
      * myQuerySet.ranking.get(i); System.out.println("Document| Score: " + myDoc.cosineSimilarity +
      * " rank=" + (i + 1) + " rel=" + myDoc.relevanceValue + " qid=" + myDoc.queryID + " " +
      * myDoc.text); } System.out.println(); } System.out.println("-------");
      */
 
     // compute the metric:: mean reciprocal rank
     double metric_mrr = compute_mrr();
     System.out.println(" (MRR) Mean Reciprocal Rank ::" + metric_mrr);
   }
 
   /**
    * 
    * @return cosine_similarity
    */
   private double computeCosineSimilarity(Map<String, Integer> queryVector,
           Map<String, Integer> docVector) {
     double cosine_similarity = 0.0;
 
     // compute cosine similarity between two sentences
 
     // calculate tf vectors for query and document
     HashMap<String, Double> tfQuery = new HashMap<String, Double>();
     HashMap<String, Double> tfDoc = new HashMap<String, Double>();
 
     // get maximum frequency terms in each document
     double maxFQuery = 0.0;
     double freqMaxBuffer = 0.0;
     for (String term : queryVector.keySet()) {
       if (queryVector.get(term) > freqMaxBuffer) {
         freqMaxBuffer = queryVector.get(term);
         maxFQuery = queryVector.get(term);
       }
     }
     double maxFDoc = 0.0;
     freqMaxBuffer = 0.0;
     for (String term : docVector.keySet()) {
       if (docVector.get(term) > freqMaxBuffer) {
         freqMaxBuffer = docVector.get(term);
         maxFDoc = docVector.get(term);
       }
     }
 
     // fill in tf vectors
     for (String term : vocab.keySet()) {
       double weight = 0.0;
       int freq = 0;
       // check if this term is in the queryVector
       if (queryVector.keySet().contains(term))
         freq = queryVector.get(term);
       // calculate tf weights (by different strategies)
       // strategy 1: boolean "frequency"
       if (freq > 0)
         weight = 1.0;
       // strategy 2: raw frequency
       // weight = (double) freq;
       // strategy 3: logscale frequency
       // weight = Math.log(freq+1);
       // strategy 4: augmented frequency
       // weight = 0.5 + ((0.5 * (double) freq) / maxFQuery);
       // put tf weight into vector
       tfQuery.put(term, weight);
 
       weight = 0.0;
       freq = 0;
       // check if this term is in the docVector
       if (docVector.keySet().contains(term))
         freq = docVector.get(term);
       // calculate tf weights (by different strategies)
       // strategy 1: boolean "frequency"
       if (freq > 0)
         weight = 1.0;
       // strategy 2: raw frequency
       // weight = (double) freq;
       // strategy 3: logscale frequency
       // weight = Math.log(freq+1);
       // strategy 4: augmented frequency
       // weight = 0.5 + ((0.5 * (double) freq) / maxFDoc);
       // put tf weight into vector
       tfDoc.put(term, weight);
     }
 
     // calculate tf-idf vectors
     HashMap<String, Double> tfidfQuery = new HashMap<String, Double>();
     HashMap<String, Double> tfidfDoc = new HashMap<String, Double>();
 
     for (String term : vocab.keySet()) {
       double weight = 0.0;
       // multiply tf by idf for this term, put into tfidf vector
       weight = tfQuery.get(term) * idf.get(term);
       tfidfQuery.put(term, weight);
       // multiply tf by idf for this term, put into tfidf vector
       weight = tfDoc.get(term) * idf.get(term);
       tfidfDoc.put(term, weight);
       // DEBUG: print tf, idf, and tf-idf vectors for term in query and doc
       /*
        * System.out.print("Term: " + term); System.out.print(", fQuery:" + queryVector.get(term));
        * System.out.print(", tfQuery:" + tfQuery.get(term)); System.out.print(", idf:" +
        * idf.get(term)); System.out.print(", tfidfQuery: " + tfidfQuery.get(term));
        * System.out.print(", fDoc:" + docVector.get(term)); System.out.print(", tfDoc:" +
        * tfDoc.get(term)); System.out.print(", idf:" + idf.get(term));
        * System.out.println(", Doc tf-idf: " + tfidfDoc.get(term));
        */
     }
 
     // calculate dot product of tf-idf vectors
     double dotProduct = 0.0;
     for (String term : vocab.keySet()) {
       dotProduct += tfidfQuery.get(term) * tfidfDoc.get(term);
     }
 
     // calculate norms
     double queryNorm = 0.0;
     for (double w : tfidfQuery.values()) {
       queryNorm += (w * w);
     }
     queryNorm = Math.sqrt(queryNorm);
 
     double docNorm = 0.0;
     for (double w : tfidfDoc.values()) {
       docNorm += (w * w);
     }
     docNorm = Math.sqrt(docNorm);
 
     // calculate cosine similarity
     cosine_similarity = dotProduct / (queryNorm * docNorm);
 
     return cosine_similarity;
   }
 
   /**
    * 
    * @return mrr
    */
   private double compute_mrr() {
     double metric_mrr = 0.0;
 
     // compute Mean Reciprocal Rank (MRR) of the text collection
     metric_mrr = 0.0;
 
     for (QuerySet myQuerySet : querySets) {
       for (int i = 0; i < myQuerySet.ranking.size(); i++) {
         Doc myDoc = myQuerySet.ranking.get(i);
         // find correct answer
         if (myDoc.relevanceValue == 1) {
           // sum up mrr
           metric_mrr += (1.0 / (double) (i + 1));
           // print score and ranking of correct answer to console
           System.out.println("Score: " + myDoc.cosineSimilarity + " rank=" + (i + 1) + " rel="
                   + myDoc.relevanceValue + " qid=" + myDoc.queryID + " " + myDoc.text);
           break;
         }
       }
     }
 
     // average mrr
     metric_mrr = (1.0 / (double) querySets.size()) * metric_mrr;
 
     return metric_mrr;
   }
 
   /**
    * Doc class. Includes document text, word frequency vector, and tf-idf vector for each document.
    * 
    * @author Weston Feely
    */
   private class Doc {
 
     /** document text **/
     public String text;
 
     /** document query ID **/
     public int queryID;
 
     /** document relevance value **/
     public int relevanceValue;
 
     /** document word frequency vector **/
     public HashMap<String, Integer> f;
 
     /** document cosine similarity with query **/
     public double cosineSimilarity;
   }
 
   /**
    * QuerySet class. Includes set of documents belonging to this query set, and the query document
    * itself.
    * 
    * @author Weston Feely
    */
   private class QuerySet {
 
     /** Query ID for this query set **/
     public int qID;
 
     /** Set of documents belonging to this query set **/
     public ArrayList<Doc> docSet;
 
     /** Ranking of documents belonging to this query set, based on cosine similarity **/
     public ArrayList<Doc> ranking;
 
     /** Query document for this query set **/
     public Doc query;
   }
 }
