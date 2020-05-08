 package ir.exercise1.textindexer.indexer;
 
 import ir.exercise1.textindexer.collection.CollectionInterface;
 import ir.exercise1.textindexer.document.ClassDocument;
 import ir.exercise1.textindexer.model.InvertedIndex;
 import ir.exercise1.textindexer.model.Posting;
 import ir.exercise1.textindexer.model.WeightedInvertedIndex;
 import ir.exercise1.textindexer.model.WeightedPosting;
 import ir.exercise1.textindexer.model.PostingList;
 import ir.exercise1.textindexer.model.WeightedPostingList;
 import ir.exercise1.textindexer.tokenizer.Tokenizer;
 import ir.exercise1.textindexer.writer.file.IndexFileWriterInterface;
 
 import java.util.Iterator;
 import java.util.Map;
 
 public class TextIndexer implements IndexerInterface
 {
 	/**
 	 * Maximum number of documents to index.
 	 */
 	private int maxDocumentCount;
 	
 	/**
 	 * The lower bound of the frequency threshold.
 	 */
 	private double lowerThreshold = 0;
 
 	/**
 	 * The upper bound of the frequency threshold.
 	 */
 	private double upperThreshold = 1000;
 
 	/**
 	 * If TRUE stemming is used.
 	 */
 	private boolean stemming = true;
 
 	private CollectionInterface collection;
 	private Tokenizer tokenizer;
 	
 	public TextIndexer()
 	{
 	}
 
 	/**
 	 * Constructor.
 	 *
 	 * @param collection
 	 *
 	 * @return
 	 */
 	public TextIndexer(CollectionInterface collection, Tokenizer tokenizer)
 	{
 		this(collection, tokenizer, -1);
 	}
 	
 	public TextIndexer(CollectionInterface collection, Tokenizer tokenizer, int maxDocumentCount)
 	{
 		this.collection 	  = collection;
 		this.tokenizer  	  = tokenizer;
 		this.maxDocumentCount = maxDocumentCount;
 	}
 
 	/**
 	 * Sets the lower bound for frequency thresholding.
 	 *
 	 * @param lowerThreshold The lower threshold
 	 */
 	public void setLowerThreshold(double lowerThreshold)
 	{
 		this.lowerThreshold = lowerThreshold;
 	}
 
 	/**
 	 * Returns the lower bound for frequency thresholding.
 	 *
 	 * @return The lower threshold
 	 */
 	public double getLowerThreshold()
 	{
 		return lowerThreshold;
 	}
 
 	/**
 	 * Sets the upper bound for frequency thresholding.
 	 *
 	 * @param upperThreshold The upper threshold
 	 */
 	public void setUpperThreshold(double upperThreshold)
 	{
 		this.upperThreshold = upperThreshold;
 	}
 
 	/**
 	 * Returns the upper bound for frequency thresholding.
 	 *
 	 * @return The upper threshold
 	 */
 	public double getUpperThreshold()
 	{
 		return upperThreshold;
 	}
 
 	/**
 	 * Sets if stemming should be used.
 	 *
 	 * @param stemming TRUE if stemming should be used, FALSE otherwise
 	 */
 	public void setStemming(boolean stemming)
 	{
 		this.stemming = stemming;
 	}
 
 	/**
 	 * Returns if stemming should be used.
 	 *
 	 * @return TRUE if stemming should be used, FALSE otherwise
 	 */
 	public boolean getStemming()
 	{
 		return stemming;
 	}
 
 	public void buildIndex(IndexFileWriterInterface arffWriter)
 	{
 		InvertedIndex index = new InvertedIndex();
 		tokenizer.setInvertedIndex(index);
 		
 		WeightedInvertedIndex weightedIndex = new WeightedInvertedIndex();
 		
 		int documentIndexCounter = 0;
 		while (collection.hasNext()) {
 			documentIndexCounter++;
 
 			ClassDocument document = (ClassDocument) collection.next();
 			
 			int documentId = index.addDocument(document.getName());
 			weightedIndex.addDocument(document.getName());
 			weightedIndex.addClassName(documentId, document.getClassName());
 			tokenizer.tokenize(document, documentId);
			weightedIndex.addDocumentLength(documentId, index.getDocumentLength(documentId));
 			
 			document = null;
 			
 			if (maxDocumentCount > 0 && maxDocumentCount == documentIndexCounter) {
 				System.out.println("I've indexed "+documentIndexCounter+" documents and I'm going to lay down now.");
 				break;
 			}
 		}
 				
 		double avgTokensPerDoc = index.getIndexSize() / index.getDocumentCount();
 
 		System.out.println("\nSome statistics:");
 		System.out.println(index.getDocumentCount() + " documents");
 		System.out.println(index.getIndexSize() + " tokens");
 		System.out.println(avgTokensPerDoc + " avg. # tokens per document");
 
 		System.out.println("Starting computation of TF.IDF weights.");
 		computeWeights(index, weightedIndex);
 		weightedIndex.setDocumentLengths(index.getDocumentLengths());
 		// We don't need the normal index anymore, hopefully JVM garbage collects it
 		index = null;
 		collection = null;
 
 		System.out.println("\nStarting to write index.");
 		arffWriter.buildIndexFile(weightedIndex, getStemming(), getLowerThreshold(), getUpperThreshold());
 	}
 
 	public void computeWeights(InvertedIndex index, WeightedInvertedIndex weightedIndex)
 	{
 		//df = # of docs in the collection that contains the term
 		//idf = log(collectionSize / df)
 		//tf = # of occurance of the term in document
 		//tf-idf = tf x idf
 		
 		WeightedPostingList weightedPostingList;
 		WeightedPosting weightedPosting;
 		
 		for (int row = 0; row < index.getTokens().size(); row++) {
 			String token = index.getTokens().get(row);
 			PostingList postingList = index.getPostingList(token);
 			
 			weightedPostingList = new WeightedPostingList();
 			weightedIndex.setPostingList(token, weightedPostingList);
 
 			int df = postingList.getDocumentFrequency();
 
 			double idf = Math.log(collection.getDocumentCount()/df);
 
 			Iterator<Map.Entry<Integer, Posting>> iterator = postingList.getDocuments().entrySet().iterator();
 
 			while (iterator.hasNext()) {
 				Map.Entry<Integer, Posting> document = iterator.next();
 				Posting posting = document.getValue();
 				double tf = Math.log(1 + posting.getCount());
 				
 				if (tf >= lowerThreshold && tf <= upperThreshold) {
 					weightedPosting = new WeightedPosting(tf*idf);
 					weightedPostingList.setPosting(document.getKey(), weightedPosting);
 				}
 			}
 		}
 	}
 }
