 /*************************************************************************
  > File Name: Ranker.java
  > Author: Xiao Cui
  > Mail: xc432@nyu.edu
  > Modified Time: Fri Feb 22 16:44:00 2013
  > Add ranking function: vector space model, language model(query likehood)
  > phrase, numviews, linear model.
  ************************************************************************/
 
 package edu.nyu.cs.cs2580;
 
 import java.util.Vector;
 import java.util.Scanner;
 import java.lang.Math;
 import java.util.HashMap;
 import java.util.Collections;
 
 class Ranker {
   private Index _index;
   private HashMap < String, Integer > doc_frequency = new HashMap< String, Integer >();
   //private HashMap < String, Integer > query_frequency = new HashMap< String, Integer>();
   private String mark = "hw1.1-";
     
   public Ranker(String index_source){
     _index = new Index(index_source);
     
   }
 
   public Vector < ScoredDocument > runquery(String query, String ranker_type){
     Vector < ScoredDocument > retrieval_results = new Vector < ScoredDocument > ();
     for (int i = 0; i < _index.numDocs(); ++i){
       retrieval_results.add(runquery(query, i, ranker_type));
     }
     Collections.sort(retrieval_results, new ScoreCompare());
     
       
     /*for(ScoredDocument s: retrieval_results){
           Writer.getInstance().writeToFile(ranker_type, s.asString()+"\n", mark);
     }*/
     //write to file
     //writer.writeTofile(results, ranker_type);
     return retrieval_results;
   }
 
   public ScoredDocument runquery(String query, int did, String ranker_type){
 
     // Build query vector
     Scanner s = new Scanner(query);
 
     Vector < String > qv = new Vector < String > ();
     while (s.hasNext()){
       String term = s.next();
       qv.add(term);
     }
 
     // Get the document vector. For hw1, you don't have to worry about the
     // details of how index works.
     Document d = _index.getDoc(did);
     Vector < String > dv = d.get_title_vector();
     Vector < String > db = d.get_body_vector();
       
     /*get current document term and frequency*/
     for(int i = 0; i < dv.size(); i++){
         if(doc_frequency.containsKey(dv.get(i))){
             doc_frequency.put(dv.get(i), doc_frequency.get(dv.get(i))+1);
         }else{
             doc_frequency.put(dv.get(i), 1);
         }
     }
       
     for(int i = 0; i < db.size(); i++){
       if(doc_frequency.containsKey(db.get(i))){
             doc_frequency.put(db.get(i), doc_frequency.get(db.get(i))+1);
        }else{
             doc_frequency.put(db.get(i), 1);
        }
     }
     double score = 0.0;
 	if(ranker_type.equals("vsm")){
 	   score = vectorSpaceModel(qv, did);
 	   return new ScoredDocument(did, d.get_title_string(), score);
 	}else if(ranker_type.equals("ql")){
 	   score = languageModel(qv, did);
 	   return new ScoredDocument(did, d.get_title_string(), score);
 	}else if(ranker_type.equals("phrase")){
 	   score = phraseRanker(qv, did);
        return new ScoredDocument(did, d.get_title_string(), score);
 	}else if(ranker_type.equals("linear")){
         score = linearModel(qv, did);
         mark = "hw1.2-";
         return new ScoredDocument(did, d.get_title_string(), score);
     }else if(ranker_type.equals("numviews")){
         score = num_views(did);
         return new ScoredDocument(did, d.get_title_string(), score);
     }else{
         return new ScoredDocument(did, d.get_title_string(), score);
     }
  }
 
   /*  Similarity(Q, D) = cosine(theta)D
    *  Similarity(Q, D) = Zigma Weight(Q)*Weight(Term)/Zigma Sqrt(Weight(Q)*Weight(Q))* Sqrt(Weight(Term)*Weight(Term))
    *  term_weight vector --- store all terms' weight of a doc
    *  quert_weight vector --- store query terms' weight of a doc
    *
    */
     
   public double vectorSpaceModel(Vector < String > qv, int did){
       Document d = _index.getDoc(did);
       double query_w = 0.0;
       double weight = 0.0;
       int doc_num = _index.numDocs();
       
       double IDF = 0.0;
       double all_termw = 0.0;
       double all_queryw = 0.0;
       double all_dot_product = 0.0;
       double cosine = 0.0;
       
       
       Vector < Double > term_weight = new Vector < Double >();
       Vector < Double > query_weight = new Vector < Double >();
       Vector < String > db = d.get_body_vector();
       
       for (int i = 0; i < db.size(); ++i){
         int doc_f = Document.documentFrequency(db.get(i));
         IDF = 1+Math.log((double)(doc_num/doc_f))/Math.log((double) 2);
         int term_f = 0;
         
         if(doc_frequency.containsKey(db.get(i))){
            term_f = doc_frequency.get(db.get(i));
         }
         weight  = term_f*IDF;
         term_weight.add(weight);
         
         for(int j = 0; j < qv.size(); ++j){
            if(db.get(i).equals((qv.get(j)))){
                 query_w += IDF;
                query_weight.add(query_w);
           }
        }
         query_weight.add(0.0);
     }
       
       for(int i = 0; i < term_weight.size(); i++){
           if(term_weight.get(i) != 0.0){
               all_termw += Math.pow(term_weight.get(i),2.0);
           }
       }
       all_termw = Math.sqrt(all_termw);
       
       for(int i = 0; i < query_weight.size(); i++){
           if(query_weight.get(i) != 0.0){
               all_queryw += Math.pow(query_weight.get(i),2.0);
           }
       }
       all_queryw = Math.sqrt(all_queryw);
       
       for(int i = 0; i < term_weight.size(); i++){
           all_dot_product += term_weight.get(i)*query_weight.get(i);
       }
       
       if((all_queryw*all_termw) != 0){
           cosine = all_dot_product/(all_termw*all_queryw);
       }else{
           cosine = 0.0;
       }
       return cosine;
   }
   /*Language model*/
   public double languageModel(Vector< String > qv, int did){
        Document d = _index.getDoc(did);
        Vector < String > db = d.get_body_vector();
        Vector < String > dv = d.get_title_vector();
        int size = db.size() + dv.size();
       
 	   double score = 0.0;
 	   double lambda = 0.5;
 
        for(int i = 0; i < qv.size(); i++){
            int count = 0;
            if(doc_frequency.containsKey(qv.get(i))){
                count = doc_frequency.get(qv.get(i));
            }
            double termlike = (double)Document.termFrequency(qv.get(i)) / (double)Document.termFrequency();
 		   double doclike = (double) count/ (double)size; // doc terms
 		   score += Math.log((1 - lambda)*doclike + lambda*termlike);
 	   }
       score = Math.pow(Math.E, score);
 	   return score;
   }
   
   /*phrase rank: check qv[i]qv[i+1] match*/
   public double phraseRanker(Vector < String > qv, int did){
 	  Document d = _index.getDoc(did);
       Vector < String > db = d.get_body_vector();
 	  double score = 0.0;
 	  if(qv.size() == 1){
           if(doc_frequency.containsKey(qv.get(0))){
 		     score = doc_frequency.get(qv.get(0));
           }else{
               score = 0.0;
           }
 	  }else{
          for(int i = 0; i <qv.size()- 1; i++){
 			 for(int j = 0; j<db.size() - 1; j++){
 				 if(db.get(j).equals(qv.get(i)) && db.get(j+1).equals(qv.get(i+1)))
 					 score = score + 1;
 			 }
 		 }
 
 	  }
 	  return score;
   }
   
     /*numviews: just return the number of views as score*/
    public double num_views(int did){
        double score = 0.0;
        Document d = _index.getDoc(did);
        score = d.get_numviews();
        return score;
    }
     
     /*Simple implement as combination of vsm+ql+phrase+views*/
   public double linearModel(Vector < String > qv, int did){
 	  // score = 0.55*cos+0.4*ql+0.0499*phrase+0.0001numviews
 	  double score = 0.0;
       score += 0.55*vectorSpaceModel(qv, did) + 0.4*languageModel(qv, did) + 0.0499*phraseRanker(qv, did) + 0.0001*num_views(did);
 	  return score;
   }
   
     
    
   /*implement a writer class
    has function write(results, ranker_type)  --- write results to csv after rank;
    can use the same function for evaluate.
    
   public void writeToCSV(Vector < ScoredDocument > results, String ranker_type){
       String finlename = "../results" + "hw1.1-" + ranker_type + ".tsv";
       try{
           File file = new File(filename);
           if(!file.exists()){
               
           }
       }
 
   }*/
 
 }
