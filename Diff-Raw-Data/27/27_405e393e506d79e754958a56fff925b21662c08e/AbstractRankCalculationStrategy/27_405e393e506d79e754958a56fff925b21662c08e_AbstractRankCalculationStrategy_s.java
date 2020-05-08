 package indeece;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 
 import util.BinaryHeap;
 import util.PriorityQueue.Position;
 
 public abstract class AbstractRankCalculationStrategy implements ICosineRankCalculationStrategy {
 
 	public boolean UseFastCosineStrategy = false;
 	
 	@Override
 	public void calculateVectorNorms(Index indexObject) {
 		HashMap<String , PostingList > entries = indexObject.getIndexedTerms();
 	
 		float idf,weight;
 
 		for( String currentKey : entries.keySet())
 		{
 			PostingList postingList = entries.get(currentKey);
 			idf = getIdf(indexObject.getIndexedDocsNumber(), postingList.size());
 			
 			postingList.setTermIdf(idf);
 			for ( PostingList.Item currentItem : postingList)
 			{	
 				weight = getDocumentTermWeight( currentItem.getFrequency(), idf); // calculating the weight component for the current item
 				
 				currentItem.getDoc().addToNorm(weight);
 			}
 		}
 		for ( Doc docIt: Indeece.getCorpus())
 		{
 			// finalizing the vector norm
 			docIt.finalizeVectorNorm();
 			//System.out.println( "Title : "+ docIt.getTitle() + " Vector Norm : "+ docIt.getVectorNorm());
 		}
 	}
 
 	public BinaryHeap caculateCosineScore(
 			Model model , 
 			HashMap<String, Integer> queryTermFrequencyMapping
 			
 	)
 	{
 		if ( UseFastCosineStrategy)
 			return  caculateFastCosineScore( model , queryTermFrequencyMapping);
 		
 		HashMap<Doc,Float> scoresMap = new HashMap<Doc,Float>();
 		BinaryHeap resultHeap = new BinaryHeap();
 		float termWeight = 0, docWeightNorm = 0, queryWeightNorm = 0;
 		//Iterate over all terms in the query
 		for ( String currentTerm : queryTermFrequencyMapping.keySet())
 		{
 			//Calculate the dot product of the query and the documents, and return the term's weight
 			//Determine the score added to the similarity of each document
 			//indexed under this token and update the length of the
 			//query vector with the square of the weight for this token.
 		    termWeight = updateDotProduct(model.index, scoresMap,currentTerm, queryTermFrequencyMapping.get(currentTerm));
 			//Update the query weight norm
 			queryWeightNorm += (float) Math.pow(termWeight,2.0);
 		}
 		// Finalize the length of the query vector by taking the square-root of the
 	    // final sum of squares of its token weights.
 		queryWeightNorm = (float) Math.sqrt(queryWeightNorm);
 
 		float score = 0;
 		for ( Doc currentDoc :scoresMap.keySet())
 		{
 			docWeightNorm = currentDoc.getVectorNorm();
 			if(docWeightNorm==0)
 			{
 				System.out.println("ZERO WEIGHT NORM");
 				score = 0;
 			}
 			else if(termWeight!=0)
 			{
 				//Normalize score for the lengths of the two document vectors
 				score = scoresMap.get(currentDoc) / (queryWeightNorm*docWeightNorm);
 			}
 			//Insert result into heap
 			resultHeap.insert(model.CreateResult(currentDoc, score));
 		}		
 		return resultHeap;
 	}
 
 	public BinaryHeap caculateFastCosineScore(
 			Model model , 
 			HashMap<String, Integer> queryTermFrequencyMapping
 	)
 	{
 		HashMap<Doc,Float> scoresMap = new HashMap<Doc,Float>();
 		BinaryHeap resultHeap = new BinaryHeap();
 		float termWeight = 0, docWeightNorm = 0;
 		final int queryTermFrequency = 1;
 		//Iterate over all terms in the query
 		for ( String currentTerm : queryTermFrequencyMapping.keySet())
 		{
 			//Calculate the dot product of the query and the documents, and return the term's weight
 			//Determine the score added to the similarity of each document
 			//indexed under this token and update the length of the
 			//query vector with the square of the weight for this token.
 		    updateDotProduct(model.index, scoresMap,currentTerm, queryTermFrequency);
 			//Update the query weight norm
 		}
 		
 		// Following is not needed
 		// Finalize the length of the query vector by taking the square-root of the
 	    // final sum of squares of its token weights.
 		//queryWeightNorm = (float) Math.sqrt(queryWeightNorm);
 		float score = 0;
 		for ( Doc currentDoc :scoresMap.keySet())
 		{
 			docWeightNorm = currentDoc.getVectorNorm();
 			if(docWeightNorm==0)
 			{
 				System.out.println("ZERO WEIGHT NORM");
 				score = 0;
 			}
			else if(termWeight!=0)
			{
				//Normalize score for the lengths of the two document vectors
				score = (float)((double)scoresMap.get(currentDoc) / (double)(docWeightNorm));
			}
			
 			//Insert result into heap
 			resultHeap.insert(model.CreateResult(currentDoc, score));
 		}		
 		return resultHeap;
 	}
 
 	private float updateDotProduct(
 			Index  index, 
 			HashMap<Doc, Float> scoresMap,
 			String currentTerm, 
 			int termFrequency ) {
 		//Get the posting list of the current term
 		PostingList currentTermList = index.getPostingList(currentTerm);   
 		if(currentTermList==null)
 		{
 			//Term is not found in the Indexes
 			return 0;
 		}
 
 		//Get the term Idf from the posting list
 		float termIdf = currentTermList.getTermIdf();
 
 		//Calculate the term component of the query 
 		//float termWeight = (1 +  (float) Math.log10((double)termFrequency)) * termIdf ;
 		float termWeight = getQueryTermWeight(termFrequency, termIdf);
 
 		//float termWeight = (float)termFrequency * termIdf ;
 
 		Iterator<PostingList.Item> docIter = currentTermList.iterator();
 
 		Doc currentDoc;
 		PostingList.Item item;
 		float documentWeight, currentScore = 0;
 
 		//Iterate over all documents that are in the query
 		while(docIter.hasNext()) {
 			item = docIter.next();
 			currentDoc = item.getDoc();
			//documentWeight = (float) (1 + Math.log10((double)item.getFrequency())) * termIdf;
			//documentWeight = (float) item.getFrequency() * termIdf;

 			documentWeight = getDocumentTermWeight (item.getFrequency() , termIdf);
 			if(scoresMap.containsKey(currentDoc))
 			{	
 				currentScore = scoresMap.get(currentDoc);
 			}
 
 			//Update the score of each document that has the currentTerm
 			scoresMap.put(currentDoc, currentScore + (documentWeight * termWeight) );
 		}
 
 		return termWeight; 
 	}
 
 	public abstract String getStrategyName();
 	
 	protected float getIdf(int totalNoOfDocuments, int documentFrequency) {
 		try
 		{
			return (float) Math.log10((double)totalNoOfDocuments/documentFrequency); //Calculate term idf
 		}
 		catch( Exception ex)
 		{
 			return 0f;
 		}
 	}
 
 	protected  abstract float getQueryTermWeight( float termFrequency ,float termIdf);
 
 	protected  abstract float getDocumentTermWeight( float termFrequency ,float termIdf);
 }
