 /*  
  *   This file is part of the computer assignment for the
  *   Information Retrieval course at KTH.
  * 
  *   First version:  Johan Boye, 2010
  *   Second version: Johan Boye, 2012
  *   Additions: Hedvig Kjellström, 2012
  */  
 
 
 package ir;
 
 import java.util.LinkedList;
 import java.util.HashMap;
 import java.util.*;
 import java.lang.Math;
 import java.io.*;
 
 /**
  *   Implements an inverted index as a Hashtable from words to PostingsLists.
  */
 public class HashedIndex implements Index {
 
     /** The index as a hashtable. */
     private HashMap<String,PostingsList> index = new HashMap<String,PostingsList>();
 	private HashMap<Integer,Integer> length = new HashMap<Integer,Integer>();
 	private HashMap<String, Double> wordCount = new HashMap<String,Double>();
 	String ownWord;
 
 
     /**
      *  Inserts this token in the index.
      */
     public void insert( String token, int docID, int offset ) {
 		if(getPostings(token)!=null){
 			PostingsList pL = getPostings(token);
 			pL.add(docID, offset);
 			index.put(token, pL);
 			
 		}
 		else{
 			PostingsList firstPl = new PostingsList();
 			firstPl.add(docID, offset);
 			index.put(token, firstPl);
 		}
 		if(length.get(docID)!=null) {
 			int tmpLength=length.get(docID);
 			tmpLength++;
 			length.remove(docID);
 			length.put(docID, tmpLength);
 		}
 		else length.put(docID, 1);
     }
 	
 	
 
     /**
      *  Returns the postings for a specific term, or null
      *  if the term is not in the index.
      */
     public PostingsList getPostings( String token ) {
 	  return index.get(token);
     }
 
 
     /**
      *  Searches the index for postings matching the query.
      */
     public PostingsList search( Query query, int queryType, int rankingType ) {
 		int i = 0;
 		LinkedList<String> searchterms = query.terms;
 		PostingsList temp = new PostingsList();	
 		LinkedList<PostingsList> PostingsList = new LinkedList();
 		while(i <searchterms.size()){
 			if((temp=getPostings(searchterms.get(i)))!=null){
 				ownWord = searchterms.get(i);
 				PostingsList.add(temp);
 				i++;
 			}
 			else{return null;}
 		}	
 		PostingsList answer = new PostingsList();
 		answer = callIntersect(PostingsList, queryType);
 		return answer;
     }
 
 	// Depending on querytype, this method calls the right method.
 	public PostingsList callIntersect(LinkedList<PostingsList> terms, int queryType ){
 		PostingsList result = new PostingsList();
 		if(terms.size() > 0 && queryType!=Index.RANKED_QUERY) {
 				result = terms.getFirst();
 		}
 		int i = 1;
 		if(queryType==Index.INTERSECTION_QUERY) {i--;Collections.sort(terms);}
 		while(i<terms.size() && queryType!=Index.RANKED_QUERY){
 			if(queryType==Index.PHRASE_QUERY) {
 				result = Positionalntersect(result, terms.get(i));
 				i++;
 			}
 			else if(queryType==Index.INTERSECTION_QUERY) {	
 				result = intersect(result, terms.get(i));
 				i++;
 			}
 		}
 		if(queryType==Index.RANKED_QUERY){
 				result = rankedRetrieval(terms);
 				Collections.sort(result.getList());
 				/* We receive and ordered LinkedList<Posting> in ascending order
 				 * based on their score. We then call synonym to go through the top
 				 * scoring documents. 
 				*/
 
 				Synonyms(result.getList());
 		}
 		return result;
 	}
 	
 	/* Goes through the top scoring documents from rankedRetrieval.
 	 * For each document it reads in all occurences of every word 
 	 * and stores the result in a HashMap<String, Integer> where
 	 * the Integer represents the total occurence of a word through
 	 * all the documents it searches. It then ouputs the words
 	 * occuring the most frequently to the stdout.
 	 */
 
 	public void Synonyms(LinkedList<Posting> docs)
 	{
 		double docScore = 0;
 		int size = 0;
 		if(docs.size()>10) size = 10;
 		else size = docs.size();
		for(int i = 0;i<10;i++){
 			Posting doc = docs.get(i);
 			try{
 			File f = new File(docIDs.get( "" + doc.docID ));
 			Reader reader = new FileReader( f );
 			SimpleTokenizer tok = new SimpleTokenizer( reader );
 			int offset = 0;
 			while ( tok.hasMoreTokens() ) {
 				String word = tok.nextToken();
 				if(word.length()>3 && !word.equals(ownWord)){
 					int wordSize = word.length();
 					docScore = doc.score;
 					insertIntoWordCount(word, wordSize, docScore);
 				}
 			}
 			reader.close();
 			}
 			catch ( IOException e ) {
 			
 			}
 		}
 		Iterator it = wordCount.entrySet().iterator();
 		double count0 = 0;
 		double count1 = 0;
 		double count2 = 0;
 		String synonym0 = "";
 		String synonym1 = "";
 		String synonym2 = "";
 		while(it.hasNext()){
 			Map.Entry pair = (Map.Entry) it.next();
 			double value = (Double)pair.getValue();
 			if(value>=count2){
 				count0 = count1;
 				synonym0 = synonym1;
 				count1 = count2;
 				synonym1 = synonym2;
 				synonym2  = (String)pair.getKey();
 				count2 = value;
 			}
 			else if(value>=count1){
 				count0 = count1;
 				synonym0  = synonym1;
 				synonym1  = (String)pair.getKey();
 				count1 = value;
 			}
 			else if(value>=count0){
 				synonym0  = (String)pair.getKey();
 				count0 = value;
 			}
 		}
 		System.out.println("Nr1: " + synonym2 + " " + count2 + ", Nr2: " + synonym1 +" "+ count1	+ ", Nr3; "  + synonym0+" "+ count0);
 		wordCount.clear();
 	}
 	
 	/* Helper function to Synonyms. */
 	public void insertIntoWordCount(String word, int wordSize, double docScore){
 		double score = 0;
 		if(!wordCount.containsKey(word)){
 			score = docScore*wordSize;
 			wordCount.put(word, score);
 		}
 		else{
 			double count = wordCount.get(word);
 			count += docScore*wordSize;
 			wordCount.put(word, count);
 		}
 	}
 	
 	public PostingsList rankedRetrieval(LinkedList<PostingsList> terms){
 		PostingsList answer = new PostingsList();
 		for(int j = 0; j<terms.size();j++){
 			PostingsList q = terms.get(j);
 			double idf = Math.log10((double) length.size()/(double)q.getDocumentFrequency());
 			for(int i = 0;i < q.size(); i++){
 				q.getPosting(i).score = 0;
 				double tfidf = q.getPosting(i).getFrequency() * idf;
 				q.getPosting(i).score += tfidf*idf;
 				q.getPosting(i).score = (double)q.getPosting(i).score/(double)length.get(q.getPosting(i).docID);
 				answer.addRanked(q.getPosting(i));
 			}
 		}
 		return answer;
 	}
 	
 	public PostingsList Positionalntersect(PostingsList first, PostingsList second){
 		PostingsList answer = new PostingsList();
 		int i = 0;
 		int j = 0;
 		Posting firstPosting, secondPosting;
 		while(i < first.size() && j < second.size()){
 			firstPosting=first.getPosting(i);
 			secondPosting=second.getPosting(j);
 			if(firstPosting.docID == secondPosting.docID){
 				ListIterator<Integer> pp1 = firstPosting.offsets.listIterator();
 				while(pp1.hasNext()){
 					int pp1offset = pp1.next();
 					ListIterator<Integer> pp2 = secondPosting.offsets.listIterator();
 					while(pp2.hasNext()){
 						int pp2offset = pp2.next();
 						if((pp2offset-pp1offset)==1){
 							answer.add(firstPosting.docID, pp2offset);
 						}
 					}
 				}
 				i++;
 				j++;
 			}
 			else if(firstPosting.docID < secondPosting.docID){
 				i++;
 			}
 			else j++;
 		}
 		return answer;
 	}
 
 
 	public PostingsList intersect(PostingsList first, PostingsList second){
 		PostingsList answer = new PostingsList();
 		int i = 0;
 		int j = 0;
 		Posting firstPosting, secondPosting;
 		ListIterator<Integer> of;
 		while(i < first.size() && j < second.size()){
 			firstPosting=first.getPosting(i);
 			secondPosting=second.getPosting(j);
 			if(firstPosting.docID == secondPosting.docID){
 				answer.add(firstPosting.docID, 0);
 				i++;
 				j++;
 			}
 			else if(firstPosting.docID < secondPosting.docID){
 				i++;
 			}
 			else j++;
 		}
 		return answer;
 	}
 
     /**
      *  No need for cleanup in a HashedIndex.
      */
     public void cleanup() {
     }
 }
