 package il.ac.shenkar.in.dal;
 
 import il.ac.shenkar.common.CampusInConstant;
 import il.ac.shenkar.common.CampusInEvent;
 import il.ac.shenkar.common.CampusInEvent.CampusInEventType;
 import il.ac.shenkar.common.CampusInLocation;
 import il.ac.shenkar.common.CampusInMessage;
 import il.ac.shenkar.common.CampusInUser;
 import il.ac.shenkar.common.CampusInUserLocation;
 import il.ac.shenkar.common.ParsingHelper;
 import il.ac.shenkar.common.PersonalSettings;
 import il.ac.shenkar.in.bl.Controller;
 
 import java.security.acl.LastOwnerException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Currency;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 
 import android.R.bool;
 import android.animation.ArgbEvaluator;
 import android.graphics.drawable.Drawable;
 import android.net.rtp.RtpStream;
 import android.text.method.DateTimeKeyListener;
 import android.util.Log;
 import android.widget.Toast;
 
 import com.facebook.Request;
 import com.facebook.Response;
 import com.facebook.model.GraphUser;
 import com.google.android.gms.internal.cm;
 import com.google.android.gms.maps.model.LatLng;
 import com.parse.FindCallback;
 import com.parse.ParseException;
 import com.parse.ParseFacebookUtils;
 import com.parse.ParseObject;
 import com.parse.ParseQuery;
 import com.parse.ParseUser;
 import com.parse.SaveCallback;
 
 public class CloudAccessObject implements IDataAccesObject
 {
     // single tone
     private static CloudAccessObject instace;
     private CampusInUser curentCampusInUser = null;
     private Date messagesLastUpdate;
     private ParseObject parseCurrentCampusInUser = null;
     private Drawable profilePic = null;
     private Date userFriendsToClassLastUpdate;
     private Date userFriendsToClassLocationLastUpdate;
     private Date usersLocationLastUpdate;
     private Boolean isLoading = false;
     private HashMap<String, CampusInUser> userTotalFriendsList = new HashMap<String, CampusInUser>();
     private HashMap<String, CampusInUserLocation> usersLocationList = new HashMap<String, CampusInUserLocation>();
     private HashMap<String, CampusInUser> allUsersList = new HashMap<String, CampusInUser>();
     private HashMap<String, Drawable> friendsProfilePictures=new HashMap<String, Drawable>();
 
     private CloudAccessObject()
     {
 
     }
 
     public static CloudAccessObject getInstance()
     {
 	if (instace == null)
 	    instace = new CloudAccessObject();
 	return instace;
     }
 
     @Override
     public void getEvents(final DataAccesObjectCallBack<List<CampusInEvent>> callBack)
     {
 	// load the currentCampusiN user just in case it wasnt loaded yet.
 	loadCurrentCampusInUser(new DataAccesObjectCallBack<CampusInUser>()
 	{
 
 	    @Override
 	    public void done(CampusInUser retObject, Exception e)
 	    {
 		if (e == null)
 		{
 		    ParseQuery<ParseObject> ownerQuery = ParseQuery.getQuery("Event");
 		    ownerQuery.whereEqualTo("ownerParseId", curentCampusInUser.getParseUserId());
 
 		    ParseQuery<ParseObject> recieverQuery = ParseQuery.getQuery("Event");
 		    recieverQuery.whereEqualTo("recivers", curentCampusInUser.getParseUserId());
 
 		    ParseQuery<ParseObject> isPublicQuery = ParseQuery.getQuery("Event");
 		    isPublicQuery.whereEqualTo("isPublic", true);
 
 		    List<ParseQuery<ParseObject>> queries = new ArrayList<ParseQuery<ParseObject>>();
 		    queries.add(isPublicQuery);
 		    queries.add(recieverQuery);
 		    queries.add(ownerQuery);
 		    ParseQuery<ParseObject> orQuery = ParseQuery.or(queries);
 
 		    // this will set the date as and logic ---> (date &&
 		    // (ownerId||reciverId||isPublic))
 		    orQuery.whereGreaterThanOrEqualTo("date", new Date());
 
 		    orQuery.findInBackground(new FindCallback<ParseObject>()
 		    {
 
 			@Override
 			public void done(List<ParseObject> resList, ParseException e)
 			{
 			    List<CampusInEvent> eventList = new ArrayList<CampusInEvent>();
 			    if (e == null)
 			    {
 				for (ParseObject parseEvent : resList)
 				{
 
 				    eventList.add(createCampusInEventFromParseObj(parseEvent));
 				}
 			    }
 			    if (callBack != null)
 				callBack.done(eventList, e);
 			}
 		    });
 
 		}
 
 	    }
 	});
 
     }
     
  
 
     private CampusInEvent createCampusInEventFromParseObj(ParseObject parseObj)
     {
 	CampusInEvent event = null;
 	if (parseObj != null)
 	{
 	    event = new CampusInEvent();
 	    event.setParseId(parseObj.getObjectId());
 	    event.setEventType(parseObj.getString("type"));
 	    event.setLocation(new CampusInLocation());
 	    // event.setDate(parseObj.getDate("date"));
 	    event.setHeadLine(parseObj.getString("title"));
 	    event.setDescription(parseObj.getString("description"));
 	    event.getLocation().setMapLocation(new LatLng(parseObj.getDouble("lat"), parseObj.getDouble("long")));
 	    event.getLocation().setLocationName(parseObj.getString("locationName"));
 	    event.setGlobal(parseObj.getBoolean("isPublic"));
 	    event.setOwnerId(parseObj.getString("ownerParseId"));
 
 	    ArrayList<Object> reciverId = (ArrayList<Object>) parseObj.getList("recivers");
 	    if (reciverId != null)
 	    {
 		event.setReciversList(new ArrayList<String>());
 		for (Object reciver : parseObj.getList("recivers"))
 		{
 		    event.getReceiversId().add(reciver.toString());
 		}
 	    }
 	    else
 	    {
 		event.setReciversList((ArrayList<String>) null);
 	    }
 
 	    // set the date
 	    Date date = parseObj.getDate("date");
 	    Calendar cal = new GregorianCalendar();
 	    cal.setTime(date);
 	    event.setDate(cal);
 
 	}
 	return event;
     }
 
     @Override
     public void getMessages(final DataAccesObjectCallBack<List<CampusInMessage>> callBack)
     {
     	// load the currentCampusiN user just in case it wasnt loaded yet.
     	loadCurrentCampusInUser(new DataAccesObjectCallBack<CampusInUser>()
     	{
 
     	    @Override
     	    public void done(CampusInUser retObject, Exception e)
     	    {
     		if (e == null)
     		{
     		    ParseQuery<ParseObject> ownerQuery = ParseQuery.getQuery("Message");
     		    ownerQuery.whereEqualTo("ownerParseId", curentCampusInUser.getParseUserId());
 
     		    ParseQuery<ParseObject> recieverQuery = ParseQuery.getQuery("Message");
     		    recieverQuery.whereEqualTo("recivers", curentCampusInUser.getParseUserId());
 
 
     		    List<ParseQuery<ParseObject>> queries = new ArrayList<ParseQuery<ParseObject>>();
     		    queries.add(recieverQuery);
     		    queries.add(ownerQuery);
     		    ParseQuery<ParseObject> orQuery = ParseQuery.or(queries);
 
     		    // this will set the date as and logic ---> (date &&
     		    // (ownerId||reciverId||isPublic))
     		    if(messagesLastUpdate!=null)
     		    	orQuery.whereGreaterThanOrEqualTo("createdAt", messagesLastUpdate);
 
     		    orQuery.findInBackground(new FindCallback<ParseObject>()
     		    {
 
     			@Override
     			public void done(List<ParseObject> resList, ParseException e)
     			{
     			    List<CampusInMessage> messagesList = new ArrayList<CampusInMessage>();
     			    if (e == null)
     			    {
     				for (ParseObject parseMessage : resList)
     				{
 
     					messagesList.add(createCampusInMessageFromParseObj(parseMessage));
     				}
     			    }
     			    if (callBack != null)
     				callBack.done(messagesList, e);
     			}
     		    });
 
     		}
 
     	    }
     	});
     }
     
     private CampusInMessage createCampusInMessageFromParseObj(ParseObject parseObj)
     {
     	CampusInMessage message=null;
     	if(parseObj!=null)
     	{
     		message=new CampusInMessage();
     		message.setParseId(parseObj.getObjectId());
     		message.setContent(parseObj.getString("content"));
     		message.setReadInRadius(parseObj.getInt("readInRadius"));
     		message.setSenderFullName(parseObj.getString("senderName"));
     		CampusInLocation loc=new CampusInLocation();
     		loc.setLocationName(parseObj.getString("locationName"));
     		loc.setMapLocation(new LatLng(parseObj.getDouble("lat"), parseObj.getDouble("long")));
     		message.setLocation(loc);
     		message.setOwnerId(parseObj.getString("ownerParseId"));
     		 ArrayList<Object> reciversId = (ArrayList<Object>) parseObj.getList("recivers");
     		 message.setReceiverId(new ArrayList<String>());
     		    if (reciversId != null)
     		    {
 	    			for (Object reciver : reciversId)
 	    			{
 	    				message.getReceiverId().add(reciver.toString());
 	    			}
     		    }
     	}
     	return message;
     }
 
     @Override
     public void sendMessage(CampusInMessage message, final DataAccesObjectCallBack<Integer> callBack)
     {
     	if(message!=null)
     	{
     		    final ParseObject theMessage = new ParseObject("Message");
     		    theMessage.put("content", message.getContent());
     		    theMessage.put("senderName", message.getSenderFullName());
     		    theMessage.put("readInRadius", message.getReadInRadius());
     		    theMessage.put("locationName", message.getLocation().getLocationName());
     		    theMessage.put("lat", message.getLocation().getMapLocation().latitude);
     		    theMessage.put("long", message.getLocation().getMapLocation().longitude);
     		    theMessage.put("ownerParseId", message.getOwnerId());
     		    if (message.getReceiverId() != null)
     		    {
 	    			for (String reciverId : message.getReceiverId())
 	    			{
 	    				theMessage.add("recivers", reciverId);
 	    			}
 	    		}
     		    else
     		    theMessage.add("recivers", message.getOwnerId());
     		    theMessage.saveInBackground(new SaveCallback()
     		    {
 
     			@Override
     			public void done(ParseException e)
     			{
     			    if (callBack != null)
     			    {
     				    Log.i("fmefvce", "the Event ParseId is: " + theMessage.getObjectId());
     				    	callBack.done(0, e);
     				}
     			}
     		    });
     		}	
     	}
     @Override
     public void sendEvent(final CampusInEvent event, final DataAccesObjectCallBack<String> callback)
     {
 	if (event != null)
 	{
 	    final ParseObject theEvent = new ParseObject("Event");
 	    theEvent.put("title", event.getHeadLine());
 	    theEvent.put("description", event.getDescription());
 	    theEvent.put("date", event.getDate());
 	    theEvent.put("locationName", event.getLocation().getLocationName());
 	    theEvent.put("lat", event.getLocation().getMapLocation().latitude);
 	    theEvent.put("long", event.getLocation().getMapLocation().longitude);
 	    theEvent.put("isPublic", event.isGlobal());
 	    theEvent.put("ownerParseId", event.getOwnerId());
 	    theEvent.put("type", event.getEventType().toString()); // yaki
 								   // -toString
 								   // is called
 								   // only for
 								   // debugging
 	    // if the event is global the Receiver List will be empty
 	    if (event.getReceiversId() != null)
 	    {
 		for (String reciverId : event.getReceiversId())
 		{
 		    theEvent.add("recivers", reciverId);
 		}
 	    }
 	    else
 		theEvent.add("recivers", event.getOwnerId());
 	    theEvent.saveInBackground(new SaveCallback()
 	    {
 
 		@Override
 		public void done(ParseException e)
 		{
 		    if (callback != null)
 		    {
 			if (e == null)
 			{
 			    Log.i("fmefvce", "the Event ParseId is: " + theEvent.getObjectId());
 			    event.setParseId(theEvent.getObjectId());
 			    Controller.getInstance(null).addEventToLocalMap(event);
 			    callback.done(theEvent.getObjectId(), e);
 			}
 			else
 			    callback.done(null, e);
 		    }
 
 		}
 	    });
 
 	}
 
     }
     /*
      * get all users location from the cloud
      * 
      * @see
      * il.ac.shenkar.in.dal.IDataAccesObject#getUsersInBackground(il.ac.shenkar
      * .in.dal.DataAccesObjectCallBack)
      */
     @Override
     public void getUsersLocationInBackground(final DataAccesObjectCallBack<List<CampusInUserLocation>> callBack)
     {
 	getCurrentUserFriendsToScool(true, new DataAccesObjectCallBack<List<ParseObject>>()
 	{
 
 	    @Override
 	    public void done(List<ParseObject> retObject, Exception e)
 	    {
 		if (e == null && retObject != null)
 		{
 		    for (ParseObject parseObject : retObject)
 		    {
 
 			CampusInUserLocation u = getCampusInUserLocationFromParseObject(parseObject);
 			usersLocationList.put(u.getUser().getParseUserId(), u);
 		    }
 		}
 		getCustomFriendsListAndLocation(true, new DataAccesObjectCallBack<List<ParseObject>>()
 		{
 
 		    @Override
 		    public void done(List<ParseObject> retObject, Exception e2)
 		    {
 			if (e2 == null && retObject != null)
 			{
 			    for (ParseObject parseObject : retObject)
 			    {
 				CampusInUserLocation u = getCampusInUserLocationFromParseObject(parseObject);
 				usersLocationList.put(u.getUser().getParseUserId(), u);
 			    }
 			}
 			if (callBack != null)
 			{
 			    callBack.done(new ArrayList<CampusInUserLocation>(usersLocationList.values()), e2);
 			}
 		    }
 		});
 	    }
 	});
     }
 
     /*
      * Get Parse obj and return CampusInObj
      */
     private CampusInUserLocation getCampusInUserLocationFromParseObject(ParseObject parseObject)
     {
 	CampusInUserLocation localCampusInUserLocation = null;
 	if (parseObject != null)
 	{
 	    ParseObject location = parseObject.getParseObject("location");
 	    localCampusInUserLocation = new CampusInUserLocation();
 	    localCampusInUserLocation.setUser(new CampusInUser());
 	    localCampusInUserLocation.getUser().setParseUserId(parseObject.getString("parseUserId"));
 	    localCampusInUserLocation.getUser().setFaceBookUserId(parseObject.getString("facebookId"));
 	    localCampusInUserLocation.getUser().setFirstName(parseObject.getString("firstName"));
 	    localCampusInUserLocation.getUser().setLastName(parseObject.getString("lastName"));
 	    localCampusInUserLocation.getUser().setTrend(parseObject.getString("trend"));
 	    localCampusInUserLocation.getUser().setYear(parseObject.getString("year"));
 	    if (location != null)
 	    {
 		LatLng localLatLng = new LatLng(location.getDouble("lat"), location.getDouble("long"));
 		localCampusInUserLocation.setLocation(new CampusInLocation());
 		localCampusInUserLocation.getLocation().setMapLocation(localLatLng);
 		localCampusInUserLocation.getLocation().setLocationName(location.getString("name"));
 	    }
 
 	    /*
 	     * localCampusInUserLocation.getLocation().setDate(
 	     * paramParseObject.getUpdatedAt());
 	     */}
 	return localCampusInUserLocation;
     }
 
     private void loadParseCurrentCampusInUser(final DataAccesObjectCallBack<ParseObject> callBack)
     {
 	if (this.parseCurrentCampusInUser != null)
 	{
 	    if (callBack != null)
 		callBack.done(this.parseCurrentCampusInUser, null);
 	    return;
 	}
 	ParseQuery<ParseObject> localParseQuery = ParseQuery.getQuery("CampusInUser");
 	localParseQuery.whereEqualTo("parseUserId", ParseUser.getCurrentUser().getObjectId());
 	localParseQuery.include("location");
 	List<ParseObject> retList = null;
 	try
 	{
 	    retList = localParseQuery.find();
 	}
 	catch (ParseException e)
 	{
 	    // TODO Auto-generated catch block
 	    e.printStackTrace();
 	}
 	if (retList != null && retList.size() == 1)
 	{
 	    CloudAccessObject.this.parseCurrentCampusInUser = retList.get(0);
 	    if (callBack != null)
 		callBack.done(parseCurrentCampusInUser, null);
 	}
 	else
 	{
 	    if (callBack != null)
 		callBack.done(null, new Exception("Current campus in user is not yet in the cloude"));
 	}
     }
 
     /*
      * add cumpus in user to the friend list of the current user(non-Javadoc)
      * 
      * @see
      * il.ac.shenkar.in.dal.IDataAccesObject#addFriendToFriendList(il.ac.shenkar
      * .common.CampusInUser, il.ac.shenkar.in.dal.DataAccesObjectCallBack)
      */
     public void addFriendToFriendList(final CampusInUser userToAdd, final DataAccesObjectCallBack<Integer> callBack)
     {
 	if (userToAdd == null)
 	{
 	    callBack.done(Integer.valueOf(1), new Exception("User to add is null"));
 	    return;
 	}
 	loadCurrentCampusInUser(new DataAccesObjectCallBack<CampusInUser>()
 	{
 
 	    @Override
 	    public void done(CampusInUser retObject, Exception e)
 	    {
 
 		if (e == null && retObject != null)
 		{
 		    // user in the same trend and year are friends by default
 		    if (retObject.getTrend().equals(userToAdd.getTrend()) && retObject.getYear().equals(userToAdd.getYear()))
 		    {
 			callBack.done(0, e);
 			return;
 		    }
 		}
 		ParseQuery<ParseObject> query = ParseQuery.getQuery("CampusInUser");
 		query.whereEqualTo("parseUserId", userToAdd.getParseUserId());
 		query.findInBackground(new FindCallback<ParseObject>()
 		{
 
 		    @Override
 		    public void done(List<ParseObject> retList, ParseException arg1)
 		    {
 			if (retList.size() != 1)
 			{
 			    callBack.done(Integer.valueOf(1), new Exception("This user is not exist in CampusIN"));
 			    return;
 			}
 			final ParseObject localParseObject = (ParseObject) retList.get(0);
 			loadParseCurrentCampusInUser(new DataAccesObjectCallBack<ParseObject>()
 			{
 
 			    @Override
 			    public void done(ParseObject retObject, Exception e)
 			    {
 				if (e == null && retObject != null)
 				{
 				    retObject.getRelation("friends").add(localParseObject);
 				    retObject.saveInBackground(new SaveCallback()
 				    {
 
 					@Override
 					public void done(ParseException e3)
 					{
 					    if (callBack != null)
 						callBack.done(Integer.valueOf(0), e3);
 					}
 				    });
 				}
 			    }
 			});
 		    }
 		});
 		{
 
 		}
 
 	    }
 	});
 
     }
 
     @Override
     public void loadCurrentCampusInUser(final DataAccesObjectCallBack<CampusInUser> callBack)
     {
 	if (this.curentCampusInUser == null)
 	{
 	    // load the parse object that contain the campus in user
 	    // this is a bad solusion but since the thread is managed by parse
 	    // synchronized block will not work here.
 	    // The idle loop will heaped only once so it is not so bad.
 	    while (isLoading)
 	    {
 	    }
 	    isLoading = true;
 	    // the tread that was sleep should check again if the currentuser
 	    // wass loaded.
 	    if (this.curentCampusInUser != null)
 	    {
 		isLoading = false;
 		callBack.done(curentCampusInUser, null);
 		return;
 	    }
 	    loadParseCurrentCampusInUser(new DataAccesObjectCallBack<ParseObject>()
 	    {
 
 		@Override
 		public void done(ParseObject retObj, Exception e)
 		{
 		    // if the obj was found then it is not the first time
 		    if (e == null && retObj != null)
 		    {
 			// fetch rhe data from the parse obj
 			curentCampusInUser = fromParseObjToCampusInUser(retObj);
 			isLoading = false;
 			// return the callback
 			if (callBack != null)
 			    callBack.done(curentCampusInUser, e);
 			return;
 		    }
 		    // if nothing was found or an error accured than craete the
 		    // cumpusInUser with minimum data
 		    // later when the user will login the data should be insert
 		    else
 		    {
 
 			curentCampusInUser = new CampusInUser();
 			FacebookServices.makeMeRequest(ParseFacebookUtils.getSession(), new Request.GraphUserCallback()
 			{
 			    public void onCompleted(GraphUser paramAnonymousGraphUser, Response paramAnonymousResponse)
 			    {
 				if (paramAnonymousGraphUser == null)
 				{
 				    callBack.done(null, new Exception("Could not load data from the cloud"));
 				}
 				CloudAccessObject.this.curentCampusInUser.setFaceBookUserId(paramAnonymousGraphUser.getId());
 				CloudAccessObject.this.curentCampusInUser.setFirstName(paramAnonymousGraphUser.getFirstName());
 				CloudAccessObject.this.curentCampusInUser.setLastName(paramAnonymousGraphUser.getLastName());
 				CloudAccessObject.this.curentCampusInUser.setParseUserId(ParseUser.getCurrentUser().getObjectId());
 				isLoading = false;
 				if (callBack != null)
 				    callBack.done(CloudAccessObject.this.curentCampusInUser, null);
 			    }
 			});
 		    }
 		}
 
 	    });
 
 	}
 	else
 	{
 	    isLoading = false;
 	    callBack.done(curentCampusInUser, null);
 	}
     }
 
     private CampusInUser fromParseObjToCampusInUser(ParseObject obj)
     {
 	CampusInUser user = new CampusInUser();
 
 	// TODO maybe throw an exception
 	if (obj != null)
 	{
 	    user.setFaceBookUserId(obj.getString("facebookId"));
 	    user.setTrend(obj.getString("trend"));
 	    user.setFirstName(obj.getString("firstName"));
 	    user.setLastName(obj.getString("lastName"));
 	    user.setParseUserId(obj.getString("parseUserId"));
 	    user.setYear(obj.getString("year"));
 	    user.setStatus(obj.getString("status"));
 	}
 	return user;
     }
 
     @Override
     public void putCurrentCampusInUserInbackground(final CampusInUser currentCampusInUser, final DataAccesObjectCallBack<Integer> callBack)
     {
 	ParseQuery<ParseObject> query = ParseQuery.getQuery("CampusInUser");
 	query.whereEqualTo("parseUserId", currentCampusInUser.getParseUserId());
 	query.findInBackground(new FindCallback<ParseObject>()
 	{
 
 	    @Override
 	    public void done(List<ParseObject> retObj, ParseException e)
 	    {
 		if (e == null && retObj != null)
 		{
 		    // create new one
 		    if (retObj.size() == 0)
 		    {
 			final ParseObject currentUserParseObject = new ParseObject("CampusInUser");
 			currentUserParseObject.put("firstName", currentCampusInUser.getFirstName());
 			currentUserParseObject.put("lastName", currentCampusInUser.getLastName());
 			currentUserParseObject.put("facebookId", currentCampusInUser.getFaceBookUserId());
 			currentUserParseObject.put("parseUserId", currentCampusInUser.getParseUserId());
 			currentUserParseObject.put("trend", currentCampusInUser.getTrend());
 			currentUserParseObject.put("year", currentCampusInUser.getYear());
 			currentUserParseObject.put("status", " ");
 			currentUserParseObject.saveInBackground(new SaveCallback()
 			{
 			    public void done(ParseException e)
 			    {
 
 			    }
 			});
 		    }
 		    callBack.done(Integer.valueOf(0), e);
 		}
 
 	    }
 	});
 
     }
 
     public void getProfilePicture(final DataAccesObjectCallBack<Drawable> callBack)
     {
 	if (this.profilePic != null)
 	{
 	    callBack.done(this.profilePic, null);
 	    return;
 	}
 	FacebookServices.getPictureForFacebookId(this.curentCampusInUser.getFaceBookUserId(), new DataAccesObjectCallBack<Drawable>()
 	{
 	    @Override
 	    public void done(Drawable retPic, Exception e)
 	    {
 		if (e == null)
 		{
 		    CloudAccessObject.this.profilePic = retPic;
 		    callBack.done(CloudAccessObject.this.profilePic, e);
 		}
 	    }
 	});
     }
 
     /*
      * get all the friend to school of the current user
      */
     private void getCurrentUserFriendsToScool(final Boolean location, final DataAccesObjectCallBack<List<ParseObject>> callBack)
     {
 	loadCurrentCampusInUser(new DataAccesObjectCallBack<CampusInUser>()
 	{
 
 	    @Override
 	    public void done(CampusInUser retObject, Exception e)
 	    {
 		ParseQuery<ParseObject> query = ParseQuery.getQuery("CampusInUser");
 		query.whereEqualTo("trend", curentCampusInUser.getTrend());
 		query.whereEqualTo("year", curentCampusInUser.getYear());
 		// not me
 		query.whereNotEqualTo("parseUserId", curentCampusInUser.getParseUserId());
 		if (userFriendsToClassLastUpdate != null && !location)
 		{
 		    query.whereGreaterThanOrEqualTo("createdAt", userFriendsToClassLastUpdate);
 		}
 		else if (userFriendsToClassLocationLastUpdate != null && location)
 		{
 		    query.whereGreaterThanOrEqualTo("updatedAt", userFriendsToClassLocationLastUpdate);
 
 		}
 		if (location)
 		    query.include("location");
 		query.findInBackground(new FindCallback<ParseObject>()
 		{
 
 		    @Override
 		    public void done(List<ParseObject> retList, ParseException e)
 		    {
 			if (e == null)
 			{
 			    if (callBack != null)
 			    {
 				if (!location)
 				    userFriendsToClassLastUpdate = new Date();
 				else
 				    userFriendsToClassLocationLastUpdate = new Date();
 				callBack.done(retList, e);
 			    }
 			}
 		    }
 		});
 	    }
 	});
 
     }
 
     @Override
     public void getAllCumpusInUsers(final DataAccesObjectCallBack<List<CampusInUser>> callBack)
     {
 	// first load
 	ParseQuery<ParseObject> query = ParseQuery.getQuery("CampusInUser");
 	//dont return me, and my friends to school since by default they are my friend and i cannot remove them
     query.whereNotEqualTo("parseUserId", curentCampusInUser.getParseUserId());
 	query.findInBackground(new FindCallback<ParseObject>()
 	{
 
 	    @Override
 	    public void done(List<ParseObject> retList, ParseException e)
 	    {
 		if (e == null && retList != null)
 		{
 		    for (ParseObject parseObject : retList)
 		    {
 			CampusInUser u = fromParseObjToCampusInUser(parseObject);
 			allUsersList.put(u.getParseUserId(), u);
 		    }
 		    if (callBack != null)
 		    {
 			callBack.done(new ArrayList<CampusInUser>(allUsersList.values()), e);
 		    }
 		}
 
 	    }
 	});
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * il.ac.shenkar.in.dal.IDataAccesObject#getAllCampusInUsersStartWith(java
      * .lang.String, il.ac.shenkar.in.dal.DataAccesObjectCallBack)
      */
     @Override
     public void getAllCampusInUsersStartWith(String startWith, final DataAccesObjectCallBack<List<CampusInUser>> callBack)
     {
 
 	// build the or query that will contain to queries
 	ParseQuery<ParseObject> firstNameStartWith = ParseQuery.getQuery("CampusInUser");
 	firstNameStartWith.whereStartsWith("firstName", startWith);
 
 	ParseQuery<ParseObject> lastNameStartWith = ParseQuery.getQuery("CampusInUser");
 	lastNameStartWith.whereStartsWith("lastName", startWith);
 
 	ArrayList<ParseQuery<ParseObject>> orList = new ArrayList<ParseQuery<ParseObject>>();
 	orList.add(lastNameStartWith);
 	orList.add(firstNameStartWith);
 
 	// search in the cloud
 	ParseQuery<ParseObject> mainQuery = ParseQuery.or(orList);
 	mainQuery.findInBackground(new FindCallback<ParseObject>()
 	{
 
 	    @Override
 	    public void done(List<ParseObject> retList, ParseException e)
 	    {
 		List<CampusInUser> returnList = new ArrayList<CampusInUser>();
 		if (e == null && retList != null)
 		{
 		    for (ParseObject parseObject : retList)
 		    {
 			returnList.add(fromParseObjToCampusInUser(parseObject));
 		    }
 		}
 		callBack.done(returnList, e);
 	    }
 	});
     }
 
     /*
      * return all the friends of the current user
      * 
      * @see
      * il.ac.shenkar.in.dal.IDataAccesObject#getCurrentCampusInUserFriends(il
      * .ac.shenkar.in.dal.DataAccesObjectCallBack)
      */
     @Override
     public void getCurrentCampusInUserFriends(final DataAccesObjectCallBack<List<CampusInUser>> callBack)
     {
 	// first get all the friends that in the same class
 	getCurrentUserFriendsToScool(false, new DataAccesObjectCallBack<List<ParseObject>>()
 	{
 
 	    @Override
 	    public void done(List<ParseObject> friendToClass, Exception e)
 	    {
 		// add the result to the return list
 
 		if (e == null && friendToClass != null)
 		{
 		    // add to the hash map the returned friend -
 		    for (ParseObject friend : friendToClass)
 		    {
 			CampusInUser u = fromParseObjToCampusInUser(friend);
 			userTotalFriendsList.put(u.getParseUserId(), u);
 		    }
 		    getCustomFriendsListAndLocation(false, new DataAccesObjectCallBack<List<ParseObject>>()
 		    {
 
 			@Override
 			public void done(List<ParseObject> retObject, Exception e2)
 			{
 			    if (e2 == null && retObject != null)
 			    {
 				for (ParseObject user : retObject)
 				{
 				    CampusInUser u = fromParseObjToCampusInUser(user);
 				    userTotalFriendsList.put(u.getParseUserId(), u);
 				}
 			    }
 			    if (callBack != null)
 				callBack.done(new ArrayList<CampusInUser>(userTotalFriendsList.values()), e2);
 			}
 		    });
 
 		}
 
 	    }
 	});
     }
 
     private void getCustomFriendsListAndLocation(final Boolean location, final DataAccesObjectCallBack<List<ParseObject>> callBack)
     {
 	loadParseCurrentCampusInUser(new DataAccesObjectCallBack<ParseObject>()
 	{
 
 	    @Override
 	    public void done(ParseObject retObject, Exception e)
 	    {
 		if (e == null && retObject != null)
 		{
 		    // the query in custom friend can't
 		    // be done by date because we don't know the date which the
 		    // user was added to the relation
 		    ParseQuery<ParseObject> query = retObject.getRelation("friends").getQuery();
 		    if (location)
 			query.include("location");
 		    query.findInBackground(new FindCallback<ParseObject>()
 		    {
 
 			@Override
 			public void done(List<ParseObject> friendInList, ParseException e2)
 			{
 
 			    if (e2 == null && friendInList != null)
 			    {
 				if (callBack != null)
 				    callBack.done(friendInList, e2);
 
 			    }
 			}
 		    });
 		}
 
 	    }
 	});
     }
 
     @Override
     public void updateLocation(final CampusInLocation location, final DataAccesObjectCallBack<Integer> callBack)
     {
 	ParseQuery<ParseObject> localParseQuery = ParseQuery.getQuery("CampusInUser");
 	localParseQuery.whereEqualTo("parseUserId", ParseUser.getCurrentUser().getObjectId());
 	localParseQuery.include("location");
 	localParseQuery.findInBackground(new FindCallback<ParseObject>()
 	{
 
 	    @Override
 	    public void done(List<ParseObject> retObjectList, ParseException e)
 	    {
 		if (e == null && retObjectList != null && retObjectList.size() == 1)
 		{
 		    ParseObject retObject = retObjectList.get(0);
 		    ParseObject loc = retObject.getParseObject("location");
 		    // location is not exist- need to create it
 		    if (loc == null)
 		    {
 			loc = new ParseObject("location");
 			loc.put("name", location.getLocationName());
 			loc.put("lat", location.getMapLocation().latitude);
 			loc.put("long", location.getMapLocation().longitude);
 			retObject.put("location", loc);
 		    }
 		    else
 		    {
 			loc.remove("name");
 			loc.remove("lat");
 			loc.remove("long");
 			loc.put("name", location.getLocationName());
 			loc.put("lat", location.getMapLocation().latitude);
 			loc.put("long", location.getMapLocation().longitude);
 		    }
 		    retObject.saveInBackground(new SaveCallback()
 		    {
 
 			@Override
 			public void done(ParseException e2)
 			{
 			    if (callBack != null)
 				callBack.done(0, e2);
 
 			}
 		    });
 		}
 
 	    }
 
 	});
 
     }
 
     @Override
     public void removeFriendFromFriendList(final CampusInUser userToRemove, final DataAccesObjectCallBack<Integer> callBack)
     {
 	if (userToRemove == null)
 	{
 	    callBack.done(Integer.valueOf(1), new Exception("User to add is null"));
 	    return;
 	}
 	loadCurrentCampusInUser(new DataAccesObjectCallBack<CampusInUser>()
 	{
 
 	    @Override
 	    public void done(CampusInUser retObject, Exception e)
 	    {
 		if (e == null && retObject != null)
 		{
 		    // user in the same trend and year are friends by default
 		    if (retObject.getTrend().equals(userToRemove.getTrend()) && retObject.getYear().equals(userToRemove.getYear()))
 		    {
 			if (callBack != null)
 			    callBack.done(1, new Exception("User from the same class can't be removed"));
 			return;
 		    }
 		}
 
 		ParseQuery<ParseObject> query = ParseQuery.getQuery("CampusInUser");
 		query.whereEqualTo("parseUserId", userToRemove.getParseUserId());
 		query.findInBackground(new FindCallback<ParseObject>()
 		{
 
 		    @Override
 		    public void done(List<ParseObject> retList, ParseException arg1)
 		    {
 			if (retList.size() != 1)
 			{
 			    callBack.done(Integer.valueOf(1), new Exception("This user is not exist in CampusIN"));
 			    return;
 			}
 			final ParseObject localParseObject = (ParseObject) retList.get(0);
 			loadParseCurrentCampusInUser(new DataAccesObjectCallBack<ParseObject>()
 			{
 
 			    @Override
 			    public void done(ParseObject retObject, Exception e)
 			    {
 				if (e == null && retObject != null)
 				{
 				    retObject.getRelation("friends").remove(localParseObject);
 				    if (userTotalFriendsList != null)
 				    {
 					// remove from the cash
 					userTotalFriendsList.remove(userToRemove.getParseUserId());
 					usersLocationList.remove(userToRemove.getParseUserId());
 				    }
 				    retObject.saveInBackground(new SaveCallback()
 				    {
 
 					@Override
 					public void done(ParseException e3)
 					{
 					    if (callBack != null)
 						callBack.done(Integer.valueOf(0), e3);
 					}
 				    });
 				}
 			    }
 			});
 		    }
 		});
 		{
 
 		}
 	    }
 	});
     }
     @Override
 	public  void getFriendProfilePicture(final String facebookId,
 			final DataAccesObjectCallBack<Drawable> callback) {
 		if(friendsProfilePictures.containsKey(facebookId))
 			if(callback!=null)
 			{
 				callback.done(friendsProfilePictures.get(facebookId), null);
 				return;
 			}
 		FacebookServices.getPictureForFacebookId(
 				facebookId,
 				new DataAccesObjectCallBack<Drawable>() {
 					@Override
 					public void done(Drawable retPic, Exception e) {
 						if (e == null && retPic!=null) {
 							CloudAccessObject.this.friendsProfilePictures.put(facebookId, retPic);
 							callback.done(retPic, e);
 							return;
 						}
 					}
 				});
 		
 		
 	}
 
 	@Override
 	public void updateCurrentUserStatus(
 			DataAccesObjectCallBack<Integer> callback, final String status) {
 		loadParseCurrentCampusInUser(new DataAccesObjectCallBack<ParseObject>() {
 			
 			@Override
 			public void done(ParseObject retObject, Exception e) {
 				if(retObject!=null && e==null)
 				{
 					if(status!=null)
 					{
 					retObject.remove("status");
 					retObject.put("status", status);
 					retObject.saveInBackground();
 					}
 				}
 				
 			}
 		});
 		
 	}
 }
 
