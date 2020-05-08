 package ir.exercise1.textindexer.model;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class WeightedInvertedIndex
 {
 	/**
 	 * The names of the documents in the index.
 	 */
 	private List<String> documentNames = new ArrayList<String>();
 	
 	/**
 	 * The length of the documents in the index.
 	 */
 	private Map<Integer, Integer> documentLengths = new HashMap<Integer, Integer>();
 	
 	private Map<Integer, String> classNames = new HashMap<Integer, String>();
 	
 	private Map<String, WeightedPostingList> index = new HashMap<String, WeightedPostingList>();
 	private List<String> tokens = new ArrayList<String>();
 	
 	/**
 	 * Stores the total length of all documents in the index.
 	 */
	private int totalDocumentLength = 0;
 	
 
 	public int addDocument(String documentName)
 	{
 		documentNames.add(documentName);
 		
 		return documentNames.size()-1;
 	}
 	
 	public void setDocument(Integer index, String documentName)
 	{
 		documentNames.set(index, documentName);
 	}
 	
 	public String getDocumentName(int index)
 	{
 		return documentNames.get(index);
 	}
 	
 	public void setDocumentCount(Integer documentCount)
 	{
 		for (int i = 0; i < documentCount; i++) {
 			documentNames.add("");
 		}
 	}
 	
 	public int getDocumentCount()
 	{
 		return documentNames.size();
 	}
 	
 	public void addClassName(Integer documentId, String className)
 	{
 		classNames.put(documentId, className);
 	}
 	
 	public String getClassName(Integer documentId)
 	{
 		return classNames.get(documentId);
 	}
 	
 	public Map<Integer, String> getClassNames()
 	{
 		return classNames;
 	}
 	
 	public void addToken(String token, int documentId, double tfIdf)
 	{
 		WeightedPostingList postingList;
 		
 		if (!index.containsKey(token)) {
 			postingList = new WeightedPostingList();
 			index.put(token, postingList);
 			tokens.add(token);
 		} else {
 			postingList = index.get(token);
 		}
 		
 		postingList.setPosting(documentId, new WeightedPosting(tfIdf));
 	}
 	
 	public Integer getTokenCount()
 	{
 		return tokens.size();
 	}
 	
 	public void setPostingList(String token, WeightedPostingList postingList)
 	{
 		tokens.add(token);
 		index.put(token, postingList);
 	}
 	
 	public boolean hasPostingList(String token)
 	{
 		if (index.containsKey(token) && index.get(token).getSize() > 0) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	public WeightedPostingList getPostingList(String token)
 	{
 		return index.get(token);
 	}
 	
 	public String getToken(int index)
 	{
 		return tokens.get(index);
 	}
 	
 	public List<String> getTokens()
 	{
 		return tokens;
 	}
 	
 	public int getIndexSize()
 	{
 		return index.size();
 	}
 	
 	public Integer getDocumentLength(int index) {
 		return documentLengths.get(index);
 	}
 
 	public Map<Integer, Integer> getDocumentLengths() {
 		return documentLengths;
 	}
 	
 	/**
 	 * Returns the length of the given document.
 	 * 
 	 * @param documentId The ID of the document
 	 * 
 	 * @return The length of the document
 	 */
 	public Integer getDocumentLength(Integer documentId)
 	{
 		return documentLengths.get(documentId);
 	}
 
 	public void setDocumentLengths(Map<Integer, Integer> documentLengths)
 	{
 		this.documentLengths = documentLengths;
 	}
 	
 	/**
 	 * Sets the document length for the given document.
 	 * 
 	 * @param documentId     The ID of the document
 	 * @param documentLength The length of the document
 	 */
 	public void addDocumentLength(Integer documentId, Integer documentLength)
 	{
 		documentLengths.put(documentId, documentLength);
		if (documentLength > 0) {
			totalDocumentLength = totalDocumentLength + documentLength;
		}
 	}
 
 	public int getTotalDocumentLength()
 	{
 		return totalDocumentLength;
 	}
 
 	public void setTotalDocumentLength(int totalDocumentLength)
 	{
 		this.totalDocumentLength = totalDocumentLength;
 	}	
 }
