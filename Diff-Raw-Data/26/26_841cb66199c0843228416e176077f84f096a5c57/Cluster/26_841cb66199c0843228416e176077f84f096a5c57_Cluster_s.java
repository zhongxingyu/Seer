 package domain.cluster;
 
 import java.io.Serializable;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.TreeMap;
 
 import domain.collection.ClusteredRankedCollection;
 import domain.collection.document.RankedDocument;
 import service.vector.DocumentVector;
 
 public class Cluster implements Serializable, Comparable<Cluster>{
 
 	private static final long serialVersionUID = 3080499589097159977L;
 	private int _clusterID;
 	private DocumentVector _centroid = null;
 	private LinkedList<RankedDocument> _documents;
 	
 	public enum Rank {BM25, DISTANCE}
 	
 	public Cluster (int clusterID){
 		_clusterID = clusterID;
 		_documents = new LinkedList<RankedDocument>();
 	}
 	
 	public RankedDocument poll() {
 		return _documents.pollFirst();
 	}
 	
 	public void add(RankedDocument doc){
 		_documents.add(doc);
 	}
 	
 	private DocumentVector calculateCentroid() {
 		if (_centroid != null)
 			return _centroid;
 		DocumentVector centroid = new DocumentVector();
 		for (RankedDocument doc : _documents)
 			if (doc != null)
 				centroid = doc.getVector().add(centroid);
 		centroid.divideBy(new Double(_documents.size()));
 		_centroid = centroid;
 		return centroid;
 	}
 	
 	public DocumentVector getCentroid(boolean refresh) {
 		if (refresh == true || _centroid == null) {
 			_centroid = null;
 			calculateCentroid();
 		}
 		return _centroid;
 	}
 	
 	public String getName() {
 		return "Cluster-" + this.getID();
 	}
 	
 	/**
 	 * Return numberOfDocument from this Cluster in descending order of distance from the centroid
 	 * @param numberOfDocument
 	 * @return
 	 */
 	public java.util.Collection<RankedDocument> subListByDistance(int numberOfDocument) {
 		TreeMap<Double, RankedDocument> result = new TreeMap<Double, RankedDocument>();
 			
 		for (RankedDocument doc : _documents) {
 			result.put(doc.getVector().getDistanceFromVector(_centroid), doc);
 		}
 
 		LinkedList<RankedDocument> finalResult = new LinkedList<RankedDocument>();
 		int x = 1;
 		while (x < numberOfDocument && result.size() > 0) {
 			finalResult.add(result.pollFirstEntry().getValue());
 			x++;
 		}
 		
 		return finalResult;
 	}
 	
 	public java.util.Collection<RankedDocument> subListByBM25(int numberOfDocuments, ClusteredRankedCollection clusterCollection, List<String> terms) {
 		//TODO implements BM25 ranking within a cluster
 		double b = 0.75;
 		double k1 = 1.2;
 		
 		int totalDoc = clusterCollection.getIndex().size();
 		int clusterSize = _documents.size() ;
 		
 		if( clusterSize == 0 ){
 			clusterSize = 1;
 		}
 		double idf = Math.log10(totalDoc/clusterSize);
 		if(idf == 0){
 			idf = 1;
 		}
 		double avgLength = clusterCollection.getAverageLength(); 
 		LinkedList<RankedDocument> finalResult = new LinkedList<RankedDocument>();
 		for(String term : terms){
 			for(RankedDocument doc : _documents){
 				double tf = clusterCollection.getTf(doc, term);
 				double docLength = doc.getLength();
 				Double score  = ( ( tf*(k1+1) )/ (tf+ k1*(1-b+b*(docLength/avgLength) ) ) );
 				doc.setRank(score);
 				finalResult.add(doc);
 			}
 		}
 		
 		Collections.sort(finalResult);
 		return finalResult;
 	}
 	
 	public java.util.Collection<RankedDocument> getBest(Rank rank, int numberOfDocuments, ClusteredRankedCollection clusterCollection, List<String> terms) {
 		if (rank == Rank.DISTANCE)
 			return subListByDistance(numberOfDocuments);
 		if (rank == Rank.BM25)
 			return subListByBM25(numberOfDocuments, clusterCollection, terms);
 		return null;
 	}
 	
 	public LinkedList<RankedDocument> getDocumentsAndClean() {
 		LinkedList<RankedDocument> result = _documents;
 		_documents = new LinkedList<RankedDocument>();
 		return result;
 	}
 
 	public void setCentroid(DocumentVector centroid){
 		_centroid = centroid;
 	}
 	/**
 	 * 
 	 * @return a Document Vector that represent as the centroid of the cluster
 	 */
 	public DocumentVector getCentroid(){
 		return this.getCentroid(false);
 	}
 	
 	/**
 	 * 
 	 * @return a Map<Integer,DocumentVector> that contains document ID associated to a Document Vector
 	 */
 	public LinkedList<RankedDocument> getDocuments(){
 		return _documents;
 	}
 	
 	/**
 	 * compute the centroid of the cluster
 	 */
 	public void findCentroid(){
 		this.calculateCentroid();
 	}
 
 	public int getID(){
 		return _clusterID;
 	}
 
 	@Override
	public int compareTo(Cluster o) {
		System.err.println("the unimplemented compareTo method was called :S");
		return 0;
 	}
 }
