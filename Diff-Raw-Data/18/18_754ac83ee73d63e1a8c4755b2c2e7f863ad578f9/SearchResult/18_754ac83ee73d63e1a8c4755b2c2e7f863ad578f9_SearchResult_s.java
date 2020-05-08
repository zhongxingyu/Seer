 package at.fakeroot.sepm.shared.client.serialize;
 
 import java.util.ArrayList;
 import java.io.Serializable;
 
 /**
  * Class that stores the search result in an ArrayList of ClientGeoObjects
  * @author AC
  * */
 
 public class SearchResult implements Serializable {
 	/// default serial version id
 	private static final long serialVersionUID = 1L;
 
 	private ArrayList<ClientGeoObject> result=null;
 	private String errMsg = null;
 	private boolean hasMore = false;
 	
 	/// time the search took in milliseconds
 	private long duration;
 	
 	/**
 	 * StandardConstruction. Is required for the serialization
 	 */
 	public SearchResult() {
 		result=new ArrayList<ClientGeoObject>();
 	}
 	
 	/**
 	 * Constructor
 	 * @param maxCount int limit for the results which are displayed (relevant for GUI ResultInfoBox)
 	 */
 	public SearchResult(boolean hasMore){
 	}
 
 	/**
 	 * Method used for adding a result element to the result list
 	 * @param object ClientGeoObject element to be added 
 	 * */
 	public void addResultToList(ClientGeoObject object) {
 		result.add(object);
 	}
 
 	/**
 	 * Getter for the result Arraylist
 	 * @return ArrayList<ClientGeoObject> search results 
 	 * */
 	public ArrayList<ClientGeoObject> getResults() {
 		return result;
 	}
 
 	/**
 	 * Setter for the number of hits for a search in the DB
 	 * @param totalHits int number of hits
 	 */
 	public void setHasMore(boolean hasMore) {
 		this.hasMore = hasMore;
 	}
 	
 	/**
 	 * returns true if there are more results than 
 	 * @return
 	 */
 	public boolean hasMore() {
 		return hasMore;
 	}
 	
 	/**
 	 * returns true if the search caused an error (if the error message was previously set)
 	 * @return true if the search failed
 	 */
 	public boolean hasError() {
 		return errMsg != null;
 	}
 	
 	/**
 	 * returns the error message if an error occured
 	 * @return error message
 	 */
 	public String getErrorMessage() {
 		return errMsg;
 	}
 	
 	/**
 	 * sets an error message that can be received using getErrorMessage() or hasError()  
 	 * @param msg error message
 	 */
 	public void setErrorMessage(String msg) {
 		errMsg = msg;
 	}
 	
 	/**
 	 * sets the duration of the search
 	 * @param millis duration in milliseconds
 	 */
 	public void setDuration(long millis) {
 		duration = millis;
 	}
 	
 	/**
 	 * returns the duration of the search
 	 * @return duration in milliseconds
 	 */
 	public long getDuration() {
 		return duration;
 	}
 }
