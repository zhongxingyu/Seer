 package il.ac.tau.team3.spcomm;
 
 import il.ac.tau.team3.addressQuery.MapsQueryLocation;
 import il.ac.tau.team3.common.GeneralPlace;
 import il.ac.tau.team3.common.GeneralUser;
 import il.ac.tau.team3.common.PlaceAndUser;
 import il.ac.tau.team3.common.SPGeoPoint;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import android.util.Log;
 
 public class SPComm {
 
     private static final String GET_FOR_OBJECT_USERS  = "users";
     private static final String GET_FOR_OBJECT_PLACES = "places";
     private static final String GET_USER_BY_ACCOUNT = "getuserbymail";
     private static final String GET_PLACES_OF_OWNER = "placesbyowner";  
    private static final String GET_PLACES_OF_JOINER = "placesbyjoiner";
     private static final String POST_FOR_OBJECT_NEW_PLACE  = "updateplacebylocation";
     private static final String POST_FOR_OBJECT_ADD_JOINER = "addjoiner";
     private static final String POST_FOR_OBJECT_REMOVE_JOINER = "removejoiner";
     private static final String POST_FOR_OBJECT_DELETE_PLACE = "deleteplace";
     private static final String POST_FOR_OBJECT_UPDATE_USER_BY_NAME = "updateuserbyname";
     
     
     private SPGenComm com = new SPGenComm();
     
     private Map<String, String> getParameters(double latitude, double longitude, int radius)
     {
         Map<String, String> parameters = new HashMap<String, String>();
         parameters.put("latitude" , String.valueOf(latitude));
         parameters.put("longitude", String.valueOf(longitude));
         parameters.put("radius"   , String.valueOf(radius));        
         
         return  parameters;
     }
     
     
     public void requestGetPlaces(double latitude, double longitude, int radius, ICommHandler<GeneralPlace[]> callback)
     {
         Map<String, String> parameters = getParameters(latitude, longitude, radius);
         
         
         String request = GET_FOR_OBJECT_PLACES;
         
         //requestGet(parameters, responseType, request);
         com.requestGet(parameters, GeneralPlace[].class, request, callback);
         
 
     }
     
     
     public void requestGetUsers(double latitude, double longitude, int radius, ICommHandler<GeneralUser[]> callback)
     {
         Map<String, String> parameters = getParameters(latitude, longitude, radius);
         
         
         String request = GET_FOR_OBJECT_USERS;
         
         //requestGet(parameters, responseType, request);
         com.requestGet(parameters, GeneralUser[].class, request, callback);
         
 
     }
     
     public void requestGetUserByAccount(String account, ICommHandler<Long> callback)
     {
         Map<String, String> parameters = new HashMap<String, String>();
         parameters.put("account" , String.valueOf(account));
         
         
         String request = GET_USER_BY_ACCOUNT;
         
         //requestGet(parameters, responseType, request);
         com.requestGet(parameters, Long.class, request, callback);
         
 
     }
     
     public void requestPostRegister(GeneralPlace to ,GeneralUser user, boolean[] praysWishes, ICommHandler<String> callback)
     {
     	PlaceAndUser pau = new PlaceAndUser(user, to,praysWishes);
         com.requestPost(pau, String.class, POST_FOR_OBJECT_ADD_JOINER, callback);
     }
     
     public void requestPostNewPlace(GeneralPlace place, ICommHandler<Long> callback)
     {
     	
     	com.requestPost(place, Long.class, POST_FOR_OBJECT_NEW_PLACE, callback);
         
         
     }
     
     public void removeJoiner(GeneralPlace place, GeneralUser joiner,boolean[] praysWishes, ICommHandler<Void> callback)	{
     	PlaceAndUser pau = new PlaceAndUser(joiner, place,praysWishes);
     	
     	com.requestPost(pau, Void.class, POST_FOR_OBJECT_REMOVE_JOINER, callback);
     }
     
     public void deletePlace(GeneralPlace place, ICommHandler<String> callback)	{
     	com.requestPost(place, String.class, POST_FOR_OBJECT_DELETE_PLACE, callback);
     }
     
     public void updateUserByName(GeneralUser user, ICommHandler<Long> callback){
     	com.requestPost(user, Long.class, POST_FOR_OBJECT_UPDATE_USER_BY_NAME, callback);
 	 }
     
     
     
     public void searchForAddress(String address, ICommHandler<MapsQueryLocation> callback)	{
     	Map<String, String> parameters = new HashMap<String, String>();
     	parameters.put("address", address);
     	parameters.put("sensor", "false");
     	final String URL = "http://maps.googleapis.com/maps/api/geocode/json"; 
     	com.requestGet(parameters, MapsQueryLocation.class, URL, callback);
     }
     
     public void getAddressObj(double latitude, double longitude, ICommHandler<MapsQueryLocation> callback)	{
     	Map<String, String> parameters = new HashMap<String, String>();
     	parameters.put("latlng", String.valueOf(latitude)+","+String.valueOf(longitude));
     	parameters.put("sensor", "false");
     	final String URL = "http://maps.googleapis.com/maps/api/geocode/json"; 
     	com.requestGet(parameters, MapsQueryLocation.class, URL, callback);
     }
 
 
	public void requestGetOwnerPlaces(long ownerId, ICommHandler<GeneralPlace[]> callback)
     {
         Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("id" , String.valueOf(ownerId));
         
         
         String request = GET_PLACES_OF_OWNER;
         
         com.requestGet(parameters, GeneralPlace[].class, request, callback);
 		
 	};
	
	public void requestGetJoinerPlaces(long joinerId, ICommHandler<GeneralPlace[]> callback)
    {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("id" , String.valueOf(joinerId));
        
        
        String request = GET_PLACES_OF_JOINER;
        
        com.requestGet(parameters, GeneralPlace[].class, request, callback);
		
	};
     
     
     
     
        
     
 }
