 package twitterClassifier;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.lucene.analysis.Analyzer;
 import org.apache.lucene.analysis.standard.StandardAnalyzer;
 import org.apache.lucene.document.Document;
 import org.apache.lucene.document.Field;
 import org.apache.lucene.document.Field.Index;
 import org.apache.lucene.document.Field.Store;
 import org.apache.lucene.index.CorruptIndexException;
 import org.apache.lucene.index.IndexReader;
 import org.apache.lucene.index.IndexWriter;
 import org.apache.lucene.index.IndexWriter.MaxFieldLength;
 import org.apache.lucene.index.IndexWriterConfig;
 import org.apache.lucene.index.Term;
 import org.apache.lucene.index.TermDocs;
 import org.apache.lucene.index.TermEnum;
 import org.apache.lucene.util.Version;
 import org.apache.lucene.search.CachingWrapperFilter;
 import org.apache.lucene.search.DocIdSetIterator;
 import org.apache.lucene.search.Filter;
 import org.apache.lucene.search.QueryWrapperFilter;
 import org.apache.lucene.search.TermQuery;
 import org.apache.lucene.search.DocIdSet; 
 import org.apache.lucene.store.LockObtainFailedException;
 
 public class LuceneClassifier extends TwitterClassifier {
 
 	private boolean topFeatures;
 	private boolean overfit;
 	private Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_34);
 	private HashMap<String, double[]> wordSet;
 	private String curSentiment;
     protected double categoryRatio;
 	
 	public LuceneClassifier(String trainingFile, String sentiment){
 		super();
 		// build the index
 		super.buildIndex(trainingFile);
 		this.curSentiment = sentiment;
 		this.overfit = false;
 		this.topFeatures = false;
 		
 		try {
 			trainClassifier(trainingFile);
 		} 
 		catch (Exception e) {
 			System.out.println("Caught IO Exception in LuceneClassifier Constructor");
 			e.printStackTrace();
 		}
 	}
 	
 	public LuceneClassifier(String trainingFile, String sentiment, boolean overfit, boolean topFeatureExtract){
 		// build the index
		super();
 		buildIndex(trainingFile);
 		this.curSentiment = sentiment;
 		this.overfit = overfit;
 		this.topFeatures = topFeatureExtract;
 		try {
 			trainClassifier(trainingFile);
 		} 
 		catch (Exception e) {
 			System.out.println("Caught IO Exception in LuceneClassifier Constructor");
 			e.printStackTrace();
 		}
 		
 	}
 		
 	@Override
 	public boolean classify(String query) throws Exception {
 		IndexReader reader = null;
 		IndexWriter writer = null;
 		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_34, this.analyzer);
 		
 		try {
 			writer = new IndexWriter(index, config);
 			// create a new doc and add it to the collection
 			Document doc = new Document();
 			doc.add(new Field("text", query, Store.NO, Index.ANALYZED));
 			writer.addDocument(doc);
 			writer.commit();
 			writer.optimize();
 			writer.close();
 			double docRatio = this.categoryRatio;
 			reader = IndexReader.open(index);
 			// get all the terms in the index
 			TermEnum terms = reader.terms();
 			while(terms.next()){
 				Term t = terms.term();
 				TermDocs termDocs = reader.termDocs(t);
 				String word = t.text();
 				// get the probabilities for this word if it exists
 				if(wordSet.containsKey(word)){
 					double[] probs = wordSet.get(word);
 					if(probs[1] == 0.0){
 						// this word is a very good indicator so boost up its ranking 
 						docRatio *= (probs[0]/ .00001);
 					}
 					else{
 						docRatio *= probs[0] / probs[1];
 					}
 				}
 			}
 			return (docRatio > 1.0);
 		} catch (CorruptIndexException e) {
 			System.out.println("ERROR: Corrupt index in classify");
 			return false;
 		} catch (LockObtainFailedException e) {
 			System.out.println("ERROR: Could not obtain lock for index in classify");			
 			return false;
 		} catch (IOException e) {
 			System.out.println("ERROR: Caught IO exception in classify");
 			return false;
 		}
 		finally{
 			if(writer != null && IndexWriter.isLocked(index)){
 				IndexWriter.unlock(index);
 				writer.rollback();
 				writer.close();
 			}
 			if(reader != null){
 				reader.close();
 			}
 		}
 		
 	}
 
 	@Override
 	protected void trainClassifier(String trainingFile) throws Exception{
 		wordSet = new HashMap<String, double[]>();
 		IndexReader reader = null;
 		try {
 			reader = IndexReader.open(index);
 			Set<Integer> matchedDocs = getMatchingDocs(reader, this.curSentiment);
 			double matchedDocSize = matchedDocs.size();
 			double nDocs = reader.numDocs();
 			
 			// compute the ratio of matched docs to total docs
 			this.categoryRatio = matchedDocSize/nDocs;
 			
 			// grab all the individual terms out of all matched docs. 
 			TermEnum docTerms = reader.terms();
 			
 			double nWords = 0;
 			double nUniqueWords = 0;
 			while(docTerms.next()){
 				double nWordsInCat = 0;
 				double nWordsNotInCat = 0;
 				Term t = docTerms.term();
 				TermDocs termDocs = reader.termDocs(t);
 				
 				while(termDocs.next()){
 					int docId = termDocs.doc();
 					int freq = termDocs.freq();
 					
 					if(matchedDocs.contains(docId)){
 						nWordsInCat += freq;
 					}
 					else{
 						nWordsNotInCat += freq;
 					}
 					nWords += freq;
 					nUniqueWords++;
 				}
 				double pWord[] = new double[2];
 				if(wordSet.containsKey(t.text())){
 					pWord = wordSet.get(t.text());
 				}
 				pWord[0] += (double)nWordsInCat;
 				pWord[1] += (double)nWordsNotInCat;
 				wordSet.put(t.text(), pWord);
 			}
 			
 			// normalize the values in the pWords array to get probabilities instead 
 			// of raw numbers
 			for(String term : wordSet.keySet()){
 				double[] probs = wordSet.get(term);
 				for(int i=0; i< probs.length; i++){
 					if(overfit){
 						probs[i] = ((probs[i]+1) / (nWords + nUniqueWords)); 
 					}
 					else{
 						probs[i] /= nWords;
 					}
 				}
 			}
 			
 			if(topFeatures){
 				InfoGainSelector selector = new InfoGainSelector();
 				selector.setWordProbs(wordSet);
 				selector.setpCategory(matchedDocSize/nDocs);
 				this.wordSet  = (HashMap<String, double[]>)selector.getFeatures();
 			}		
 			
 			reader.close();
 		} 
 		catch (CorruptIndexException e) {
 			System.out.println("ERROR: Caught Corrupt Index exception during training");
 			e.printStackTrace();
 		} 
 		catch (IOException e) {
 			System.out.println("ERROR: Caught IO Exception during training");
 			e.printStackTrace();
 		}
 		finally{
 			if(reader != null){
 				reader.close();
 			}
 		}
 	}
 	
 	// return the set of ID's that match the given category
 	private Set<Integer> getMatchingDocs(IndexReader reader, String category){
 	    Set<Integer> matchedDocIds = new HashSet<Integer>();
 	    try {
 	    	Filter categoryFilter = new CachingWrapperFilter(
 			      new QueryWrapperFilter(new TermQuery(
 			      new Term("sentiment", category))));
 		
 		    DocIdSet docIdSet;
 		    docIdSet = categoryFilter.getDocIdSet(reader);
 		    DocIdSetIterator docIdSetIterator;
 		    docIdSetIterator = docIdSet.iterator();
 		    int docId;
 
 			while ((docId = docIdSetIterator.nextDoc()) != DocIdSetIterator.NO_MORE_DOCS) {
 				matchedDocIds.add(docId);
 			}
 		} catch (IOException e) {
 			System.out.println("Caught IO Exception in getMachingDOcs");
 			e.printStackTrace();
 		}
 	    return matchedDocIds;
 	}
 }
