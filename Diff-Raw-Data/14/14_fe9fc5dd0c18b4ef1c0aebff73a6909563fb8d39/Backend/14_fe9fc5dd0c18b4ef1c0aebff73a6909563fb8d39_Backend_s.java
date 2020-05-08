 package se.chalmers.krogkollen.backend;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import android.content.Context;
 
 import com.parse.Parse;
 import com.parse.ParseException;
 import com.parse.ParseObject;
 import com.parse.ParseQuery;
 
 import se.chalmers.krogkollen.pub.IPub;
 import se.chalmers.krogkollen.pub.Pub;
 import se.chalmers.krogkollen.utils.StringConverter;
 /**
  * A singleton backend handling the connection between the client and the server
  * @author Jonathan Nilsfors
  *
  */
 public class Backend implements IParseBackend{
 	private static Backend instance = null;
 	/**
 	 * Private constructor preventing accessibility 
 	 */
	private Backend(){

	}
 	/**
 	 * Returns the instance for this singleton
 	 * @return the instance
 	 */
 	public static Backend getInstance(){
 		if(instance == null){
 			instance = new Backend();
 		}
 		return instance;
 	}
 	/**
 	 * Initializes the connection to the server
 	 * @param context the conext
	 * @param applicationID of the appliaction
	 * @param clientKey of the client
 	 */
 	public static void init(Context context, String applicationID, String clientKey){
 		Parse.initialize(context, applicationID, clientKey);
 	}
 	@Override
 	public List<IPub> getAllPubs() throws NoBackendAccessException {
 		//Instantiates the list to be returned
 		List<IPub> tempPubList = new ArrayList<IPub>();
 		//Fetches the requested query from the server
 		ParseQuery<ParseObject> query = ParseQuery.getQuery("Pub");
 		//Declares a List to be able to handle the query 
 		List <ParseObject> tempList;
 		try {
 			//Done to simplify the handling of the query
 			//Makes it possible to handle as a java.util.List
 			tempList = query.find();
 			for(ParseObject object : tempList){
 				tempPubList.add(this.convertParseObjecttoIPub(object));
 			}
 		} catch (com.parse.ParseException e1) {
 			throw new NoBackendAccessException(e1.getMessage());
 		}
 		return tempPubList;
 	}
 
 	@Override
 	public void updateQueueTime(IPub pub, int newQueueTime)
 			throws NoBackendAccessException, NotFoundInBackendException {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void updateBackendPub(IPub pub) throws NoBackendAccessException,
 	NotFoundInBackendException {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public int getQueueTime(IPub pub) throws NoBackendAccessException,
 	NotFoundInBackendException {
 		ParseObject object = new ParseObject("Pub");
 		
 		try{
 			object = ParseQuery.getQuery("Pub").get(pub.getID());
 		} catch(ParseException e) {
 			throw new NotFoundInBackendException(e.getMessage());
 		}
 		// TODO Auto-generated method stub
 		return object.getInt("queueTime");
 	}
 
 	@Override
 	public IPub getPubFromID(String id) throws NoBackendAccessException,
 	NotFoundInBackendException {
 		ParseObject object = new ParseObject("Pub");
 
 		try {
 			object = ParseQuery.getQuery("Pub").get(id);
 		} catch (ParseException e) {
 			throw new NoBackendAccessException(e.getMessage());
 		}
 		return this.convertParseObjecttoIPub(object);
 	}
 
 	@Override
 	public void addRatingVote(int rating) throws NoBackendAccessException,
 	NotFoundInBackendException {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void removeRatingVote(int rating) throws NoBackendAccessException,
 	NotFoundInBackendException {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public int getPositiveRating(IPub pub) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public int getNegativeRating(IPub pub) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public Date getLatestUpdatedTimestamp(IPub pub) throws NotFoundInBackendException {
 	ParseObject object = new ParseObject("Pub");
 		
 		try{
 			object = ParseQuery.getQuery("Pub").get(pub.getID());
 		} catch(ParseException e) {
 			throw new NotFoundInBackendException(e.getMessage());
 		}
 		// TODO Auto-generated method stub
 		return object.getUpdatedAt();
 	}
 
 	/**
 	 * A method for converting a ParseObject to an IPub
 	 * @param object the ParseObject
 	 * @return the IPub representation of the ParseObject
 	 */
 	public IPub convertParseObjecttoIPub(ParseObject object){
 		int hourFourDigit = StringConverter.convertStringToFragmentedInt(object.getString("openingHours"), 5);
 		return new Pub(
 				object.getString("name"),
 				object.getString("description"),
 				object.getDouble("latitude"),
 				object.getDouble("longitude"),
 				object.getInt("ageRestriction"),
 				object.getInt("entranceFee"),
 				(hourFourDigit/100),
 				(hourFourDigit%100),
 				object.getInt("posRate"),
 				object.getInt("negRate"),
 				object.getInt("queueTime"),
 				object.getObjectId());
 	}
 
 
 }
