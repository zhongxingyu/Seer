 package edu.mwdb.project;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.sql.Connection;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.lucene.analysis.CharArraySet;
 import org.apache.lucene.analysis.StopFilter;
 import org.apache.lucene.analysis.TokenStream;
 import org.apache.lucene.analysis.standard.*;
 import org.apache.lucene.index.CorruptIndexException;
 
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.index.Term;
 import org.apache.lucene.index.TermEnum;
 import org.apache.lucene.index.TermFreqVector;
 
 import org.apache.lucene.search.IndexSearcher;
 import org.apache.lucene.search.Query;
 import org.apache.lucene.search.ScoreDoc;
 import org.apache.lucene.search.TermQuery;
 import org.apache.lucene.store.Directory;
 import org.apache.lucene.util.Bits;
 import org.apache.lucene.util.BytesRef;
 import org.apache.lucene.util.Version;
  
 
 public class Task1cCompDocs {
 
 
 	public void findKWSimilarDocs(String personNum) {
 		String fileName = null;
 		String rankingMethod = "tfidf";
 		double[][] documentMatrix = null;
 		Hashtable<Integer, VectorRanking> Similarities = new Hashtable<Integer, VectorRanking>();
 
 		try {
 			DblpData db = new DblpData();
 			Directory theIndex = db.createAllDocumentIndex();
 			IndexReader reader2 = IndexReader.open(theIndex);
 
 			IndexSearcher searcher = new IndexSearcher(reader2);
 
 				
 			
 				//ArrayList<TermFrequency> termVector = getAuthorKeywordVector1(personNum,searcher, reader2, rankingMethod);
 				Directory theIndextoAuthors = db.createAuthorDocumentIndex();
 				
 				TermFreqVector keywordVector = getAuthorKeywordVector(personNum, theIndextoAuthors);
 				
 				List<String> allterms =	db.getAllTermsInIndex(theIndex, "doc");
 				Map<String, Integer> wordPosMap = findMap(allterms);
 				
 				double[] authorKeyword = Utility.getAlignedTFIDFVector(keywordVector,wordPosMap,reader2);
 
 				
 				
 				Map<Integer, TermFreqVector> allDocFreqVectors =  getDocTermFrequencies(theIndex);
 				
 				documentMatrix =  getDocumentMatrix(theIndex, allterms);
 				
 				Similarities =  getSimilarity(theIndex, authorKeyword, wordPosMap);
 				
 				
 				if (!Similarities.isEmpty()){
 					// Create an array containing the elements from hashtable Similarities
 					VectorRanking[] rankingArray =	doRanking(Similarities);	
 
 					displayRanking(rankingArray, reader2);
 
 					Similarities.clear();
 
 
 				}						// end if similarities is non-empty
 				else { System.out.println("0 total matching documents");}
 
 				
 			
 				
 				
 				
 
 //				keywordVector = getAuthorKeywordPFVector(personNum,searcher, reader2);
 
 
 
 				reader2.close();
 				
 			
 			} catch (Exception e) {
 				System.out.println(" caught a " + e.getClass()
 						+ "\n with message: " + e.getMessage());
 			}
 		}					// end main of task1c
 
 	
 	
 	
 	public void findDifferentiationSimilarDocs(String personNum, String divVectorType) {
 		String fileName = null;
 		String rankingMethod = divVectorType;
 		double[][] documentMatrix = null;
 		Hashtable<Integer, VectorRanking> Similarities = new Hashtable<Integer, VectorRanking>();
 
 		try {
 			DblpData db = new DblpData();
 			Directory theIndex = db.createAllDocumentIndex();
 			IndexReader reader2 = IndexReader.open(theIndex);
 
 			//ArrayList<TermFrequency> termVector = getAuthorKeywordVector1(personNum,searcher, reader2, rankingMethod);
 			Directory theIndex2 = db.createAuthorDocumentIndex();
 
 
 			List<String> allterms =	db.getAllTermsInIndex(theIndex, "doc");
 			LinkedHashMap<String, Integer> wordPosMap = (LinkedHashMap<String, Integer>) findMap(allterms);
 
 			//		double[] authorKeyword = Utility.getAlignedTFIDFVector(keywordVector,wordPosMap,reader2);
 
 
 			double[] authorDiscriminationVector = null;
 			if (divVectorType.equalsIgnoreCase("PF")){
 				authorDiscriminationVector = getAlignedAuthorKeywordPFVector(personNum, theIndex2, wordPosMap);
 			}
 			else {
 				authorDiscriminationVector =  getAlignedAuthorTfidf2KeywordVector(personNum, wordPosMap);
 			}
 
 			Map<Integer, TermFreqVector> allDocFreqVectors =  getDocTermFrequencies(theIndex);
 
 			documentMatrix =  getDocumentMatrix(theIndex, allterms);
 
 			Similarities =  getSimilarity(theIndex, authorDiscriminationVector, wordPosMap);
 
 
 			if (!Similarities.isEmpty()){
 				// Create an array containing the elements from hashtable Similarities
 				VectorRanking[] rankingArray =	doRanking(Similarities);	
 
 				displayRanking(rankingArray, reader2);
 
 				Similarities.clear();
 
 
 			}						// end if similarities is non-empty
 			else { System.out.println("0 total matching documents");}
 
 
 
 
 
 
 			reader2.close();
 
 
 		} catch (Exception e) {
 			System.out.println(" caught a " + e.getClass()
 					+ "\n with message: " + e.getMessage());
 		}
 	}					// end main of task1c
 
 	
 	public double[] getAlignedAuthorKeywordPFVector(String personNum, Directory  luceneIndex, LinkedHashMap<String, Integer> wordPosMap) throws Exception{
 			
 
 		HashMap<String,Double> allPFs = Utility.getPF(personNum);
 		
 		/* Get Author's Differentiation Vector */			
 			TermFreqVector authorTermFreqVector = getAuthorKeywordVector(personNum, luceneIndex);
 				double[] alignedVector = new double[wordPosMap.keySet().size()];
 				
 				String termTexts[] = authorTermFreqVector.getTerms();
 				int termFreqs[] = authorTermFreqVector.getTermFrequencies();
 				
 				for(int i=0; i<termTexts.length; i++){
 					if(!wordPosMap.containsKey(termTexts[i])){
 						System.out.println(termTexts[i]);
 					}
 					int j = wordPosMap.get(termTexts[i]);
 					if(j != -1){
 						Double pfFactor = allPFs.get(termTexts[i]);
 						alignedVector[j] = termFreqs[i]*pfFactor;
 					}
 				}
 				return alignedVector;
 		}
 
 	public double[] getAlignedAuthorTfidf2KeywordVector(String personNum,  LinkedHashMap<String, Integer> wordPosMap) throws Exception{
 		
 		double[] alignedVector = new double[wordPosMap.keySet().size()];
 		DblpData db = new DblpData();
 		
 		 Map<String, Double> authorTFIDF2 = db.getTFIDF2Vector(personNum);
 		 
 		 for (Map.Entry<String, Double> tfidf2Entry : authorTFIDF2.entrySet()){
 			 String term = tfidf2Entry.getKey();
 			 int termIndex = wordPosMap.get(term);
 			 alignedVector[termIndex] = tfidf2Entry.getValue();
 			  }
 		 return alignedVector; 
 		 
 	}
 	
 	public TermFreqVector getAuthorKeywordVector(String authorId,Directory luceneIndexDir) throws CorruptIndexException, IOException
 	{	DblpData db = new DblpData();
 	 Map<String, TermFreqVector>  allAuthors = db.getAuthorTermFrequencies(luceneIndexDir);
 	 TermFreqVector tfv = allAuthors.get(authorId);
 	 return tfv;
 	}
 /*
  *  replacement method that creates a single author key word vector by accessing db directly
  */
 
 	public Map<String, Float> getAuthorKeywordVector2(String authorId,Directory indexDirectory,String rankMethod) throws CorruptIndexException, IOException{
 		CharArraySet stopWordsCharArrSet;
 		TokenStream docStream;
 		TokenStream keywords;
 		IndexReader reader3 = IndexReader.open(indexDirectory);
 
 		int noOfDocs = reader3.numDocs();
 		Map<String,Float> termFreq = new HashMap<String, Float>();
 		Map<String,Float> termFinalFreq = new HashMap<String, Float>();
 		Map<String,Float> idfGivenAuthMap = new HashMap<String, Float>();
 
 		List<Map<String, Float>> listFreqMaps = new ArrayList<Map<String, Float>>();
 		List<Map<String, Float>> listTFIDFMaps = new ArrayList<Map<String, Float>>();
 		
 		DblpData db = new DblpData();
 		Utility utilityObj = new Utility();
 		List<String> abstracts =  db.getAbstractsByAuthor(authorId);
 		String allRowData = db.combineAbstracts(abstracts);
 		
 		//Creating the Character Array Set from the list of stop words
 		stopWordsCharArrSet = new CharArraySet(Version.LUCENE_36, utilityObj.createStopWordsSet(), true);
 		
 		//Creating a token stream from the abstract got from the DB for the given paperId
 			docStream = new StandardTokenizer(Version.LUCENE_36, new StringReader(allRowData));
 
 		//Creating the Keywords of a given abstract
 			keywords = new StopFilter(Version.LUCENE_36, docStream ,stopWordsCharArrSet);
 
 			termFreq = utilityObj.createTF(keywords, allRowData);
 		
 		
 		//Calling the method createTFIDF to create TF-IDF vector output
 		Map<String,Float> idfMap = utilityObj.createTFIDF(noOfDocs, indexDirectory, termFreq,"TF-IDF");
 		
 		return idfMap;
 		
 			 
 			}
 		
 		private HashMap<String, Double> getTopLatentSemantics(String personNum){
 			HashMap<String, Double> top5 = null;
 			//top5 = Task1a.latentSemantics(personNum);
 			return top5;
 		}
 
 		
 		private VectorRanking[] doRanking(Hashtable<Integer,VectorRanking> Similarities){
 			// Create an array containing the elements from hashtable Similarities
 
 			Collection<VectorRanking> ranking = Similarities.values();
 			VectorRanking[] rankingArray = (VectorRanking[])ranking.toArray(new VectorRanking[Similarities.size()]);
 			Arrays.sort(rankingArray, Collections.reverseOrder());
 			return rankingArray;
 		}	
 	
 		
 
 		/*
 		 * Helper method displays ranking of observations/documents
 		 */
 		private  void displayRanking(VectorRanking[] rankingArray, IndexReader reader2) throws IOException{
 		System.out.println("Similar documents");
 		System.out.println();
 		System.out.printf(" \t "  + "PaperID"  + "\t" + " CosineSimilarity"); 
 		System.out.println();
 
 			final int HITS_PER_PAGE = 50;
 			; 
 			int end = Math.min(rankingArray.length, HITS_PER_PAGE);
 			for (int i = 0; i < end; i++) {
 				int docNum = rankingArray[i].documentID;
 
 				System.out.printf(" RANK " + "\t" +(i + 1) + " " + reader2.document(docNum).get("paperid")+ "\t" + " SCORE %10.7f",
 						rankingArray[i].howSimilar);
 				System.out.println();
 
 			}
 		}
 		
 		
 
 		public Map<Integer, TermFreqVector> getDocTermFrequencies(Directory luceneIndexDir){
 			IndexReader reader;
 			Map<Integer, TermFreqVector> docTermFrequencies = new HashMap<Integer, TermFreqVector>();
 			try {
 				reader = IndexReader.open(luceneIndexDir);
 				for (int i = 0; i < reader.maxDoc(); i++) {
 					TermFreqVector tfv = reader.getTermFreqVector(i, "doc");
 					docTermFrequencies.put(i, tfv);
 				}
 				reader.close();
 				 
 			} catch (CorruptIndexException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			} catch (IOException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 			return docTermFrequencies;
 		}
 	
 	public Map<String, Integer> findMap(List<String> completeKeywordList){
 		int termIdx = 0;
 		Map<String, Integer> allKeywordsPosMap = new LinkedHashMap<String, Integer>();
 		for(String kw:completeKeywordList){
 			allKeywordsPosMap.put(kw, termIdx++);
 		}
 		return allKeywordsPosMap;
 	}
 	/*	
 	public double[] getAlignedTFIDF2Vector (TermFreqVector atfv, Map<String, Integer> wordPosMap, Directory LuceneIndexDir, Directory notAllAuthorsIndex, String authorID){
 		IndexReader readerNotAuthor  = IndexReader.open(notAllAuthorsIndex);
 		IndexReader readerAllAuthors = IndexReader.open(LuceneIndexDir);
 		Integer numDocs = readerAllAuthors.numDocs() - readerNotAuthor.numDocs();
 		
 	}
 	*/
 	
 	public double[][] getDocumentMatrix(Directory luceneIndexDir, List<String> completeKeywordList) throws Exception{
 		Utility util = new Utility();
 		DblpData db = new DblpData();
 		SimilarityAnalyzer sv = new SimilarityAnalyzer();
 		
 
 		IndexReader reader = IndexReader.open(luceneIndexDir);
 		double[][] documentMatrix = new double[reader.maxDoc()][completeKeywordList.size()];
 		
 		int termIdx = 0;
 		Map<String, Integer> allKeywordsPosMap = new LinkedHashMap<String, Integer>();
 		for(String kw:completeKeywordList){
 			allKeywordsPosMap.put(kw, termIdx++);
 		}
 
 			try {
 					
 			for (int i = 0; i < reader.maxDoc(); i++) {
 				TermFreqVector tfv = reader.getTermFreqVector(i, "doc");
 				double[] doc1 = util.getAlignedTFIDFVector(tfv,allKeywordsPosMap,reader );
 				
 				documentMatrix[i] = doc1;
 				
 			}
 			reader.close();
 			 
 		} catch (CorruptIndexException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		} catch (IOException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 		return documentMatrix;
 	}
 			
 			public Hashtable<Integer, VectorRanking> getSimilarity(Directory luceneIndexDir, double[] authorskeywords, Map<String, Integer> allKeywordsPosMap) throws Exception{
 				Utility util = new Utility();
 				DblpData db = new DblpData();
 				SimilarityAnalyzer sv = new SimilarityAnalyzer();
 				IndexReader reader = IndexReader.open(luceneIndexDir);
 				Hashtable<Integer, VectorRanking> Similarities = new Hashtable<Integer, VectorRanking>();
 				//double[] docSimilarities = new double[reader.maxDoc()];
 
 //				double[][] documentMatrix = new double[reader.maxDoc()][completeKeywordList.size()];
 				
 				
 					try {
 							
 					for (int i = 0; i < reader.maxDoc(); i++) {
 						TermFreqVector tfv = reader.getTermFreqVector(i, "doc");
 						double[] doc1 = util.getAlignedTermFreqVector(tfv, allKeywordsPosMap);
 						
 						double cosineSim = util.cosineSimilarity(authorskeywords, doc1);
 						//documentMatrix[i] = a1;
 						//docSimilarities[i] = cosineSim;
 						VectorRanking vr = new VectorRanking();
 						vr.howSimilar = cosineSim;
 						vr.documentID = i;
 						Similarities.put(i,vr);
 												
 					}
 					reader.close();
 					 
 				} catch (CorruptIndexException e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				} catch (IOException e1) {
 					// TODO Auto-generated catch block
 					e1.printStackTrace();
 				}
 				return Similarities;
 			}
 
 
 	
 	
 }
 
 
 
 
