 package recommender.dataaccess;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.terrier.matching.ResultSet;
 import org.terrier.querying.Manager;
 import org.terrier.querying.SearchRequest;
 import org.terrier.structures.MetaIndex;
 import org.terrier.utility.ApplicationSetup;
 
 import recommender.beans.IRStory;
 import recommender.utils.RecommenderException;
 
 public class RetrievalManager {
 	
 	private TerrierManager terrierManager;
 	
 	
 	/**
 	 * Default Manager
 	 */
 	public RetrievalManager(TerrierManager tm) {
 		this.terrierManager = tm;
 	}
 	
 	public TerrierManager getTerrierManager() {
 		return terrierManager;
 	}
 	
 	public void setTerrierManager(TerrierManager terrierManager) {
 		this.terrierManager = terrierManager;
 	}
 	
 	/**
 	 * Converts a ResultSet to a List of IRStory
 	 * @param rs ResultSet with result of query
 	 * @return List of Stories
 	 * @throws RecommenderException
 	 */
 	private List<IRStory> getStoriesFromResultSet(ResultSet rs) throws RecommenderException {
 		List<IRStory> result = null;
 		
 		try
 		{
 			result = new ArrayList<IRStory>();
 			StoryDAO storyDAO = new StoryDAO();
 			MetaIndex metaIndex = terrierManager.getMetaIndex();
 			String[] docnos = metaIndex.getItems("docno", rs.getDocids());
 			
 			for(String code : docnos) {
 				result.add(storyDAO.loadStory(code, true));
 			}
 		}
 		catch(Exception ex) {
 			ex.printStackTrace();
 			throw new RecommenderException(RecommenderException.MSG_ERROR_TERRIER_CONVERSION);
 		}
 		
 		return result;
 	}
 	
 	
 	
 	
 	/**
 	 * Uses Terrier to search for a query
 	 * @param query Query to search
 	 * @return List of Stories
 	 * @throws RecommenderException
 	 */
 	public List<IRStory> searchStories(String query) throws RecommenderException {
 		return this.getStoriesFromResultSet(this.search(query));
 	}
 	
 	
 	
 	/**
 	 * Uses Terrier to search for a query
 	 * @param query Query to search
 	 * @param results Results per page
 	 * @return List of Stories
 	 * @throws RecommenderException
 	 */
 	public List<IRStory> searchStories(String query, int results) throws RecommenderException {
 		return this.getStoriesFromResultSet(this.search(query, results));
 	}
 	
 	
 	
 	
 	/**
 	 * Uses Terrier to search for a query
 	 * @param query Query to search
 	 * @param start Number of the first result
 	 * @param results Results per page
 	 * @return List of Stories
 	 * @throws RecommenderException
 	 */
 	public List<IRStory> searchStories(String query, Integer start, Integer results) throws RecommenderException {
 		return this.getStoriesFromResultSet(this.search(query, start, results));
 	}
 	
 	
 	
 	
 	/**
 	 * Uses Terrier to search for a query
 	 * @param query Query to search
 	 * @return ResultSet with the results of the query
 	 * @throws RecommenderException
 	 */
 	public ResultSet search(String query) throws RecommenderException {
 		return this.search(query, null, null);
 	}
 	
 	
 	
 	
 	/**
 	 * Uses Terrier to search for a query
 	 * @param query Query to search
 	 * @param results Results per page
 	 * @return ResultSet with the results of the query
 	 * @throws RecommenderException
 	 */
 	public ResultSet search(String query, int results) throws RecommenderException {
 		return this.search(query, null, results);
 	}
 	
     
 	
 	
 	/**
 	 * Uses Terrier to search for a query
 	 * @param query Query to search
 	 * @param start Number of the first result
 	 * @param results Results per page
 	 * @return ResultSet with the results of the query
 	 * @throws RecommenderException
 	 */
     public ResultSet search(String query, Integer start, Integer results) throws RecommenderException {
     	ResultSet rs = null;
     	
     	try
     	{
 			Manager manager = terrierManager.getManager();
 			
			SearchRequest search = manager.newSearchRequest("queryId0", query);
 			search.addMatchingModel("Matching", ApplicationSetup.getProperty("trec.model", "PL2"));
 			
 			if(results != null) {
 				if(start == null) { start = new Integer(0); }
 				
 				search.setControl("start", String.valueOf(start));
 				search.setControl("end", String.valueOf(start + results - 1));
 			}
 			
 			manager.runPreProcessing(search);
             manager.runMatching(search);
             manager.runPostProcessing(search);
             manager.runPostFilters(search);
             
             rs = search.getResultSet();
     	}
     	catch(Exception ex) {
     		ex.printStackTrace();
     		
     		throw new RecommenderException(RecommenderException.MSG_ERROR_TERRIER_RETRIEVAL);
     	}
     	
     	return rs;
     }
 	
 	public ResultSet superSearch(String query, Integer start, Integer results) throws RecommenderException {
     	ResultSet rs = null;
     	
     	try
     	{
 			Manager manager = terrierManager.getManager();
 			
 			SearchRequest search = manager.newSearchRequest("queryId0", query);
 			search.addMatchingModel("Matching", ApplicationSetup.getProperty("trec.model", "PL2F"));
 			
 			if(results != null) {
 				if(start == null) { start = new Integer(0); }
 				
 				search.setControl("start", String.valueOf(start));
 				search.setControl("end", String.valueOf(start + results - 1));
 			}
 			
 			manager.runPreProcessing(search);
             manager.runMatching(search);
             manager.runPostProcessing(search);
             manager.runPostFilters(search);
             
             rs = search.getResultSet();
     	}
     	catch(Exception ex) {
     		ex.printStackTrace();
     		
     		throw new RecommenderException(RecommenderException.MSG_ERROR_TERRIER_RETRIEVAL);
     	}
     	
     	return rs;
     }
 }
