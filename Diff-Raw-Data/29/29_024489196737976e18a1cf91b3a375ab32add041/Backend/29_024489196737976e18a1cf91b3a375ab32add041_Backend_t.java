 package se.chalmers.krogkollen.backend;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import android.content.Context;
 import com.parse.Parse;
 import com.parse.ParseException;
 import com.parse.ParseObject;
 import com.parse.ParseQuery;
 import com.parse.SaveCallback;
 import se.chalmers.krogkollen.pub.IPub;
 import se.chalmers.krogkollen.pub.Pub;
 import se.chalmers.krogkollen.utils.StringConverter;
 /**
  * A singleton backend handling the connection between the client and the server
  * Uses parse.com as backend provider
  * 
  * @author Jonathan Nilsfors
  * @author Oskar Karrman
  *
  */
 public class Backend implements IParseBackend{
 	private static Backend instance = null;
 	/**
 	 * Private constructor preventing accessibility 
 	 */
 	private Backend(){}
 	
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
 	 * @param applicationID the application ID of the appliaction
 	 * @param clientKey the client key
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
 	public int getQueueTime(IPub pub) throws NoBackendAccessException, NotFoundInBackendException {
 		ParseObject object = new ParseObject("Pub");
 		
 		try{
 			object = ParseQuery.getQuery("Pub").get(pub.getID());
 		} catch(ParseException e) {
 			if (	e.getCode() == ParseException.INVALID_KEY_NAME ||
 					e.getCode() == ParseException.OBJECT_NOT_FOUND) {
 				throw new NotFoundInBackendException(e.getMessage());
 			} else {
 				throw new NoBackendAccessException(e.getMessage());
 			}
 		}
 		
 		return object.getInt("queueTime");
 		
 		// TODO is this method done?
 	}
 
 	@Override
 	public IPub getPubFromID(String id) throws NoBackendAccessException, NotFoundInBackendException {
 		ParseObject object = new ParseObject("Pub");
 
 		try {
 			object = ParseQuery.getQuery("Pub").get(id);
 		} catch (ParseException e) {
 			if (	e.getCode() == ParseException.INVALID_KEY_NAME ||
 					e.getCode() == ParseException.OBJECT_NOT_FOUND) {
 				throw new NotFoundInBackendException(e.getMessage());
 			} else {
 				throw new NoBackendAccessException(e.getMessage());
 			}
 		}
 		return this.convertParseObjecttoIPub(object);
 	}
 
 	@Override
 	public int getPositiveRating(IPub pub) throws NotFoundInBackendException, NoBackendAccessException {
 		ParseObject object = new ParseObject("Pub");
 		
 		try{
 			object = ParseQuery.getQuery("Pub").get(pub.getID());
 		} catch(ParseException e) {
 			if(e.getCode() == ParseException.INVALID_KEY_NAME ||
 			   e.getCode() == ParseException.OBJECT_NOT_FOUND) {
 				throw new NotFoundInBackendException(e.getMessage());
 			}
 			else{
 				throw new NoBackendAccessException(e.getMessage());
 			}
 		}
 		// TODO Auto-generated method stub
 		return object.getInt("posRate");
 	}
 
 	@Override
 	public int getNegativeRating(IPub pub) throws NotFoundInBackendException, NoBackendAccessException {
 		ParseObject object = new ParseObject("Pub");
 		
 		try{
 			object = ParseQuery.getQuery("Pub").get(pub.getID());
 		} catch(ParseException e) {
 			if(e.getCode() == ParseException.INVALID_KEY_NAME ||
 			   e.getCode() == ParseException.OBJECT_NOT_FOUND) {
 				throw new NotFoundInBackendException(e.getMessage());
 			}
 			else{
 				throw new NoBackendAccessException(e.getMessage());
 			}
 		}
 		// TODO Auto-generated method stub
 		return object.getInt("negRate");
 	}
 
 	@Override
 	public Date getLatestUpdatedTimestamp(IPub pub) throws NoBackendAccessException, NotFoundInBackendException {
 		ParseObject object = new ParseObject("Pub");
 		
 		try{
 			object = ParseQuery.getQuery("Pub").get(pub.getID());
 		} catch(ParseException e) {
 			throw new NotFoundInBackendException(e.getMessage());
 		}
 		// TODO Is this method done?
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
 
 	@Override
 	public void addRatingVote(IPub pub, int rating) throws NoBackendAccessException, NotFoundInBackendException {
 		
 		// Create a pointer to an object of class Pub
 		ParseObject tempPub = ParseObject.createWithoutData("Pub", pub.getID());
 		
 		if (rating > 0) {
 			tempPub.increment("posRate");
 		} else {
 			tempPub.increment("negRate");
 		}
 		
 		// Save
		try {
			tempPub.save();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*tempPub.saveInBackground(new SaveCallback() {
 			public void done(ParseException e ) {
 				if (e == null) {
 					// TODO should we do something here?
 				} else {
 					// TODO notify user
 				}
 			}
		});*/
 	}
 
 	@Override
 	public void removeRatingVote(IPub pub, int rating) throws NoBackendAccessException, NotFoundInBackendException {
 		
 		// Create a pointer to an object of class Pub
 		ParseObject tempPub = ParseObject.createWithoutData("Pub", pub.getID());
 		
 		// TODO This part can cause problems if a rating is updated after the pub was last refreshed
 		if (rating > 0) {
			tempPub.put("posRate", pub.getPositiveRating());
 		} else {
			tempPub.put("negRate", pub.getNegativeRating());
 		}
 		
 		// Save
		try {
			tempPub.save();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*tempPub.saveInBackground(new SaveCallback() {
 			public void done(ParseException e ) {
 				if (e == null) {
 					// TODO should we do something here?
 				} else {
 					// TODO notify user
 				}
 			}
		});*/
 	}
 }
