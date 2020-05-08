 /*
  *   This file is part of the computer assignment for the
  *   Information Retrieval course at KTH.
  *
  *   First version:  Hedvig Kjellström, 2012
  */
 
 package ir;
 
 import java.util.LinkedList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.StringTokenizer;
 
 public class Query {
 
     /* Constants. */
     public static final double ALPHA = 0.1;
     public static final double BETA = (1 - ALPHA);
 
     public LinkedList<String> terms = new LinkedList<String>();
     public HashMap<String, Double> weights = new HashMap<String, Double>();
 
     /**
      *  Creates a new empty Query
      */
     public Query() {
     }
 
     /**
      *  Creates a new Query from a string of words
      */
     public Query( String queryString  ) {
         StringTokenizer tok = new StringTokenizer( queryString );
         while ( tok.hasMoreTokens() ) {
             String token = tok.nextToken();
             terms.add( token);
             weights.put( token, new Double(1) );
         }
         normalize_query();
     }
 
     private void normalize_query()
     {
         for(String term : terms) 
         {
             weights.put(term, weights.get(term) / terms.size());
         }
     }
     /**
      *  Returns the number of terms
      */
     public int size() {
         return terms.size();
     }
 
     /**
      *  Returns a shallow copy of the Query
      */
     public Query copy() {
         Query queryCopy = new Query();
         queryCopy.terms = (LinkedList<String>) terms.clone();
         queryCopy.weights = (HashMap<String, Double>) weights.clone();
         return queryCopy;
     }
 
     /**
      *  Expands the Query using Relevance Feedback
      */
     public void relevanceFeedback( PostingsList results, boolean[] docIsRelevant, Indexer indexer ) {
         // results contain the ranked list from the current search
         // docIsRelevant contains the users feedback on which of the 10 first hits are relevant
 
         /* Multiply every original term with ALPHA. */
         for(String term : terms)
         {
             weights.put(term, weights.get(term) * ALPHA);
         }
 
         double numberOfRelevantDocs = 0;
         for(int i = 0; i < docIsRelevant.length; i++)
         {
             if(docIsRelevant[i]) numberOfRelevantDocs++;
         }
         double relevantDocsConstant = 1 / numberOfRelevantDocs;
 
         for(int i = 0; i < docIsRelevant.length; i++)
         {
             if(docIsRelevant[i])
             {
                 int docID = results.get(i).docID;
                 HashSet<String> docTerms = indexer.index.terms.get(docID);
                 int size = indexer.index.docLengths.get(""+docID);
                 for(String term : docTerms)
                 {
                     PostingsList pl = indexer.index.getPostings(term);
                     double numberOfDocs = indexer.index.getNumberOfDocs();
                    if(numberOfDocs / MegaIndex.IE_THRESHOLD < pl.size()) 
                     {
                         continue;
                     }
                     // GET THE TF
                     double tf = 0;
                     for( PostingsEntry pe : pl.list)
                     {
                         if(pe.docID == docID)
                         {
                             tf = pe.offsets.size();
                             break;
                         }
                     }
                     tf = tf / size; // Normalize
 
                     // ROCCHIO
                     double termScore = tf * BETA * relevantDocsConstant;
                     if(!terms.contains(term))
                     {
                         terms.addLast(term);
                         weights.put(term, termScore);
                     }
                     else
                     {
                         weights.put(term, weights.get(term) + termScore);
                     }
                 }
             }
         }
     }
 }
