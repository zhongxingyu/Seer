 package edu.mwdb.project;
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.AbstractMap;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import matlabcontrol.MatlabConnectionException;
 import matlabcontrol.MatlabProxy;
 import matlabcontrol.MatlabProxyFactory;
 import matlabcontrol.MatlabProxyFactoryOptions;
 
 import org.apache.lucene.analysis.TokenStream;
 import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Field;
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.index.Term;
 import org.apache.lucene.index.TermFreqVector;
 import org.apache.lucene.search.DefaultSimilarity;
 import org.apache.lucene.search.Similarity;
 import org.apache.lucene.store.Directory;
 
 
 public class Utility {
 
 	private static File stopWordsFile;
 	
 	public static File getStopWordsFile(){
 		if(stopWordsFile == null){
 			
 			stopWordsFile = new File("./src/StopWords.txt");
 		}
 		return stopWordsFile;
 	}
 	
 	
 	/*
 	 * Method to get the DB Connection.
 	 */
 	private static Connection con;
 	public Connection getDBConnection()
 	{
 		if (con != null)
 			return con;
 		
 		String dbUrl = "jdbc:mysql://localhost:3306/dblp";
 		
 		try 
 		{
 			Class.forName("com.mysql.jdbc.Driver");
 			con = DriverManager.getConnection(dbUrl,"root","password");
 		}
 
 		catch(ClassNotFoundException e) {
 			e.printStackTrace();
 		}
 
 		catch(SQLException e) {
 			e.printStackTrace();
 		}
 		return con;
 	}
 
 	/*
 	 * Method to create a Character Set to store the Stop words List.
 	 */
 	public Set<char[]> createStopWordsSet()
 	{
 		Set<char[]> stopWordsSet = new HashSet<char[]>();
 		try
 		{
 			FileInputStream fstream = new FileInputStream(getStopWordsFile());
 			DataInputStream in = new DataInputStream(fstream);
 			BufferedReader br = new BufferedReader(new InputStreamReader(in));
 
 			String strLine;
 			char[] charline;
 
 			while ((strLine = br.readLine()) != null) 	
 			{
 				charline = strLine.toCharArray();
 				stopWordsSet.add(charline);
 			}
 			in.close();
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 		return stopWordsSet;
 	}
 
 	/*
 	 * Method to calculate the TF for a given set of Keywords.
 	 */
 	public Map<String,Float> createTF(TokenStream keywords,String rowData) throws IOException
 	{
 		Map<String,Float> termFreq = new HashMap<String,Float>();
 		List<String> keywordsList = new ArrayList<String>(); 
 		while(keywords.incrementToken())
 		{
 			keywordsList.add(keywords.getAttribute(CharTermAttribute.class).toString());
 		}
 		String keyword="";
 		String[] rowDataArr = rowData.split("[ ]+");
 		int noOfWords = rowDataArr.length;
 		float counter = 0;
 		try 
 		{
 			for(int j=0;j<keywordsList.size();j++)
 			{
 				keyword = keywordsList.get(j);
 				counter=0;
 				for (int i=0;i<keywordsList.size();i++)
 				{
 					if(keywordsList.get(i).equalsIgnoreCase(keyword))
 					{
 						counter++;
 					}
 				}
 				termFreq.put(keyword.toLowerCase(), counter/(float)noOfWords);
 			}
 		}
 		catch (Exception e) 
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return termFreq;
 	}
 
 	public Map<String,Float> createNewTF(TokenStream keywords,String rowData) throws IOException
 	{
 
 		Map<String,Float> termFreq = new HashMap<String,Float>();
 		try
 		{
 			// remove any '\n' characters that may occur  
 			String temp = rowData.replaceAll("[\\n]", " ");  
 
 			// replace any grammatical characters and split the String into an array  
 			String[] splitter = temp.replaceAll("[.,?!:;/]", "").split(" ");  
 
 			// intialize an int array to hold count of each word  
 			int[] counter= new int[splitter.length];
 
 			// loop through the sentence  
 			for(int i =0; i< splitter.length; i++)
 			{
 
 				// hold current word in the sentence in temp variable  
 				temp = splitter[i];  
 
 				// inner loop to compare current word with those in the sentence  
 				// incrementing the counter of the adjacent int array for each match  
 				for (int k=0; k< splitter.length; k++)
 				{  
 
 					if(temp.equals(splitter[k]))  
 					{  
 						counter[k]++;
 					}  
 				}
 			}  
 
 			// populate the map  
 			for (int i=0; i< splitter.length; i++)  
 			{  
 				termFreq.put(splitter[i].toLowerCase(), (float)counter[i]/splitter.length);  
 			}
 		}
 		catch (Exception e) 
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return termFreq;
 	}
 
 
 	public Map<String,Float> countijvalue(String keyword,List<String> rowData) throws IOException
 	{
 		Map<String,Float> termFreq = new HashMap<String,Float>();
 		float counter = 0;
 		try 
 		{
 			for(int i=0;i<rowData.size();i++)
 			{
 				String[] rowDataArr = rowData.get(i).split("[ ]+");
 				for(int j=0;j<rowDataArr.length;j++)
 				{
 					if(rowDataArr[j].equalsIgnoreCase(keyword))
 					{
 						counter++;
 					}
 				}
 			}
 			termFreq.put(keyword, counter);
 		}
 		catch (Exception e) 
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return termFreq;
 	}
 
 	public Map<String,Float> createauthorTF(TokenStream keywords,String rowData) throws IOException
 	{
 		Map<String,Float> termFreq = new HashMap<String,Float>();
 		List<String> keywordsList = new ArrayList<String>(); 
 		while(keywords.incrementToken())
 		{
 			keywordsList.add(keywords.getAttribute(CharTermAttribute.class).toString());
 		}
 		String keyword="";
 		float counter = 0;
 		try 
 		{
 			for(int j=0;j<keywordsList.size();j++)
 			{
 				keyword = keywordsList.get(j);
 				counter=0;
 				for (int i=0;i<keywordsList.size();i++)
 				{
 					if(keywordsList.get(i).equalsIgnoreCase(keyword))
 					{
 						counter++;
 					}
 				}
 				termFreq.put(keyword.toLowerCase(), counter);
 			}
 		}
 		catch (Exception e) 
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return termFreq;
 	}
 
 	/*
 	 * Method to calculate the weighted count of a given set of Keywords.
 	 */
 	public Map<String,Float> createTF(TokenStream keywords,String rowData,float weight) throws IOException
 	{
 		Map<String,Float> termFreq = new HashMap<String,Float>();
 		List<String> keywordsList = new ArrayList<String>(); 
 		while(keywords.incrementToken())
 		{
 			keywordsList.add(keywords.getAttribute(CharTermAttribute.class).toString());
 		}
 		String keyword="";
 		float counter = 0;
 		try 
 		{
 			for(int j=0;j<keywordsList.size();j++)
 			{
 				keyword = keywordsList.get(j);
 				counter=0;
 				for (int i=0;i<keywordsList.size();i++)
 				{
 					if(keywordsList.get(i).equalsIgnoreCase(keyword))
 					{
 						counter++;
 					}
 				}
 				termFreq.put(keyword.toLowerCase(), counter*weight);
 			}
 		}
 		catch (Exception e) 
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return termFreq;
 	}
 
 	
 
 	
 	
 	/*
 	 * Method to generate the TF and the TF-IDF vector mapping
 	 */
 	public Map<String,Float> createTFIDF(int noOfDocs, Directory indexDir, Map<String,Float> termFreq, String weightOption) throws IOException
 	{
 		IndexReader iReader = IndexReader.open(indexDir);
 
 		int docFreq = 0;
 		Similarity similarity = new DefaultSimilarity();
 		float tf = 0;
 		float idf = 0;
 		float tfidf=0;
 
 		Map<String,Float> idfMap = new HashMap<String, Float>();
 
 		for(Map.Entry<String, Float> keyword: termFreq.entrySet()){
 			Term t = new Term("doc",keyword.getKey().toLowerCase());
 			docFreq = iReader.docFreq(t);
 			idf = similarity.idf(docFreq, noOfDocs);
 			tf = keyword.getValue();
 			tfidf = tf*idf;
 			idfMap.put(keyword.getKey(), tfidf);
 			//System.out.println("TF-IDF: " + "{" + keyword.getKey() + "," + tf*idf + "}");
 		}
 
 		//termFreq = sortByComparator(termFreq);
 		idfMap = sortByComparator(idfMap);
 
 		/*if(arg.equalsIgnoreCase("TF"))
 		{
 			for(Map.Entry<String, Float> keyword: termFreq.entrySet()){
 				tf = keyword.getValue();
 				System.out.println("TF: " + "{" + keyword.getKey() + "," + tf + "}");
 			}
 		}
 
 		if(arg.equalsIgnoreCase("TF-IDF") || arg.equalsIgnoreCase("TF-IDF2"))
 		{
 			for(Map.Entry<String, Float> keyword: idfMap.entrySet()){
 				tf = keyword.getValue();
 				if(arg.equalsIgnoreCase("TF-IDF"))
 					System.out.println("****TF-IDF****: " + "{" + keyword.getKey() + "," + keyword.getValue() + "}");
 				else
 					System.out.println("****TF-IDF2****: " + "{" + keyword.getKey() + "," + keyword.getValue() + "}");
 			}
 		}*/
 		//System.out.println("IDF Map is: " + idfMap);
 		return idfMap;
 	}
 
 	/**
 	 * Calculate Cosine Similarity between 2 vectors. Calculated by: a*b/|a||b|
 	 * @param a
 	 * @param b
 	 * @return the cosine similarity between the two vectors "a" and "b"
 	 */
 	public double cosineSimilarity(double[] a, double[] b) throws Exception {
 		if (a.length != b.length)
 			throw new Exception("Both vectors length should match");
 		
 		// a*b = multiply each a[i] by b[i], then sum all the results
 		// |a| = square each a[i] and then get the square root
 		double numerator = 0;
 		double magnitudeA = 0; // |a|
 		double magnitudeB = 0; // |b|
 		for (int i=0; i<a.length; i++) {
 			numerator += a[i]*b[i];
 			magnitudeA += Math.pow(a[i], 2);
 			magnitudeB += Math.pow(b[i], 2);
 		}
 		
 		magnitudeA = Math.sqrt(magnitudeA);
 		magnitudeB = Math.sqrt(magnitudeB);
 		double denominator = magnitudeA*magnitudeB;
 		
 		return numerator/denominator;
 	}
 
 	/*
 	 * Method to sort the vector maps in descending order
 	 */
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	public Map<String,Float> sortByComparator(Map<String,Float> inputMap) {
 
 		LinkedList linkedList = new LinkedList(inputMap.entrySet());
 		List list = linkedList;
 		Collections.sort(list, new Comparator() 
 		{
 			public int compare(Object ele1, Object ele2)
 			{
 				return ((Comparable) ((Map.Entry) (ele2)).getValue()).compareTo(((Map.Entry) (ele1)).getValue());
 			}
 		}
 		);
 		Map sortedMap = new LinkedHashMap();
 		for (Iterator it = list.iterator(); it.hasNext();) {
 			Map.Entry entry = (Map.Entry)it.next();
 			sortedMap.put(entry.getKey(), entry.getValue());
 		}
 		return sortedMap;
 	}
 	
 	public static double[] getAlignedTFIDFVector(TermFreqVector authorTermFreqVector, Map<String, Integer> allKeywordsPosMap, IndexReader docIndexReader) throws IOException{
 		double[] alignedTFVector = getAlignedTermFreqVector(authorTermFreqVector, allKeywordsPosMap);
 		for(String term : authorTermFreqVector.getTerms()){
 			int idx = allKeywordsPosMap.get(term);
 			alignedTFVector[idx] *= getIDF(docIndexReader, term);
 		}
 		return alignedTFVector;
 	}
 	
 	public static double getIDF(IndexReader reader, String termName) throws IOException
 	{
 		return Math.log(reader.numDocs()/ ((double)reader.docFreq(new Term("doc", termName))));
 	}
 	
 	public static double[] getAlignedTermFreqVector(TermFreqVector authorTermFreqVector, Map<String, Integer> allKeywordsPosMap){
 		double[] alignedVector = new double[allKeywordsPosMap.keySet().size()];
 		String termTexts[] = authorTermFreqVector.getTerms();
 		int termFreqs[] = authorTermFreqVector.getTermFrequencies();
 		for(int i=0; i<termTexts.length; i++){
 			if(!allKeywordsPosMap.containsKey(termTexts[i])){
 				System.out.println(termTexts[i]);
 			}
 			int j = allKeywordsPosMap.get(termTexts[i]);
 			if(j != -1){
 				alignedVector[j] = termFreqs[i];
 			}
 		}
 		return alignedVector;
 	}
 	
 	/**
 	 * Given an authorId, computes the PF for all the keywords
 	 * @param authorId
 	 * @return a HashMap that contains the terms as the key and the pf as the value.
 	 * @throws Exception
 	 */
 	public static HashMap<String,Double> getPF(String authorId) throws Exception {
 		DblpData dblp = new DblpData();
 		List<String> allWords = dblp.getAllTermsInIndex(dblp.createAllDocumentIndex(), "doc");
 		HashMap<Integer,HashMap> forwardIndex = dblp.getForwardAndInversePaperKeywIndex()[0];
 		HashSet<Integer> paperIdsByCoauthors = dblp.getPaperIdsFromCoauthorExcludingSelf(authorId);
 		Set<Integer> paperIdsByCoauthorsAndSelf = dblp.getPaperIdsFromCoauthorAndSelf(authorId);
 		
 		double R = paperIdsByCoauthors.size();
 		double N = paperIdsByCoauthorsAndSelf.size();
 		
 		HashMap<String,Double> retVal = new HashMap<String,Double>(allWords.size());
 		for (String word : allWords) {
   			// Calculate the number of coauthor papers not containing the keyword
   			double r_ij = 0;
   			for (int paperId : paperIdsByCoauthors) {
   				if (!forwardIndex.get(paperId).containsKey(word))
   					r_ij++;
   			}
   			
   			// Calculate number of papers in coauthor_and_self(ai) not containing the keyword
   			double n_ij = 0;
   			for (int paperId : paperIdsByCoauthorsAndSelf) {
   				if (!forwardIndex.get(paperId).containsKey(word))
   					n_ij++;
   			}
   			
   			retVal.put(word, doFormulaPF(R, N, r_ij, n_ij));
 		}
 		
 		return retVal;
 	}
 	
 	/**
 	 * Calculates the PF value
 	 * @param R - ||coauthor_papers_of_author||
 	 * @param N - ||coauthor_and_self_of_author||
 	 * @param r - number of coauthor_papers_of_author not containing keyword
 	 * @param n - number of coauthor_and_self_of_author not containing keyword
 	 * @return
 	 */
 	private static double doFormulaPF(Double R, Double N, Double r, Double n) {
 		/* compute the formula for PF Model for this term tempTerm */
		Double leftPart = Math.log((r / (R - r)) / ((n - r) / (N - n - R + r)));
 		Double rightPart = Math.abs((r / R) - ((n - r) / (N - R)));				// can be negative so need abs
 		return (leftPart * rightPart);
 	}
 }
