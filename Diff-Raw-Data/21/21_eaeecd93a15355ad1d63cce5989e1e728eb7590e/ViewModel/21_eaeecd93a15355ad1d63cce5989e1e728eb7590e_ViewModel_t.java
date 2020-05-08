 package il.ac.shenkar.cadan;
 
 import il.ac.shenkar.common.CampusInConstant;
 import il.ac.shenkar.common.CampusInEvent;
 import il.ac.shenkar.common.CampusInMessage;
 import il.ac.shenkar.common.CampusInUser;
 import il.ac.shenkar.common.CampusInUserLocation;
 import il.ac.shenkar.in.bl.Controller;
 import il.ac.shenkar.in.dal.*;
 
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.concurrent.ConcurrentHashMap;
 
 import android.R.integer;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.drawable.Drawable;
 import android.os.AsyncTask;
 
 public class ViewModel
 {
     private Context context;
     private IDataAccesObject dao = null;
 
     private Boolean updateEventDone = false;
     private Boolean updateUsersDone = false;
     private Boolean updateMesagesDone = false;
     private Boolean updateUsersLocationDone = false;
 
     // TODO -remove it the context should be in he controller.
     public ViewModel(Context context)
     {
 	super();
 	this.context = context;
 	dao = CloudAccessObject.getInstance();
 	loadCurrentUser();
     }
 
     private CampusInUser currentUser = null;
 
     private Drawable currentUserProfilePic;
     private HashMap<String, CampusInMessage> messages = new HashMap<String, CampusInMessage>();
     private HashMap<String, CampusInEvent> meeting = new HashMap<String, CampusInEvent>();
     private HashMap<String, CampusInEvent> tests = new HashMap<String, CampusInEvent>();
     private HashMap<String, CampusInEvent> lesons = new HashMap<String, CampusInEvent>();
     private HashMap<String, CampusInUserLocation> friendsLocation = new HashMap<String, CampusInUserLocation>();
     private HashMap<String, CampusInUser> friendsHash = new HashMap<String, CampusInUser>();
     private HashMap<String, CampusInEvent> allEvents = new HashMap<String, CampusInEvent>();
 	private HashMap<String,Drawable> friendsProfilePictures=new HashMap<String, Drawable>();
 
     /*
      * this method update the view model, this method is synchronized in order
      * to prevent data integrity.All the updated is being done
      */
     private synchronized void updateViewModel()
     {
     	//clearHashes();
 	dao.getEvents(new DataAccesObjectCallBack<List<CampusInEvent>>()
 	{
 
 	    @Override
 	    public void done(List<CampusInEvent> retObject, Exception e)
 	    {
 		if (e == null && retObject != null)
 		{
 		    meeting.clear();
 		    tests.clear(); 
 		    lesons.clear();
 		    allEvents.clear();
 		    for (CampusInEvent campusInEvent : retObject)
 		    {
 			addEvent(campusInEvent);
 		    }
 		    // Controller.getInstance(context).drawAllEvents(null);
 		    updateEventDone = true;
 		}
 
 	    }
 	});
 	dao.getMessages(new DataAccesObjectCallBack<List<CampusInMessage>>() {
 		
 		@Override
 		public void done(List<CampusInMessage> retObject, Exception e) {
 			messages.clear();
 			for (CampusInMessage campusInMessage : retObject) {
 				messages.put(campusInMessage.getParseId(), campusInMessage);
 			}
 			 updateMesagesDone = true;
 			
 		}
 	});
 
 	// the location and the campus in user are connected this is the reason
 	// it is not safe to it asynchronous
 	dao.getCurrentCampusInUserFriends(new DataAccesObjectCallBack<List<CampusInUser>>()
 	{
 
 	    @Override
 	    public void done(List<CampusInUser> retObject, Exception e)
 	    {
 
 		if (e == null && retObject != null)
 		{
 		    friendsHash.clear();
 		    for (CampusInUser campusInUser : retObject)
 		    {
 			friendsHash.put(campusInUser.getParseUserId(),
 				campusInUser);
 		    }
 			//done in lazy loading
			//getfacebookProfilePictures();
 		    updateUsersDone = true;
 		}
 		dao.getUsersLocationInBackground(new DataAccesObjectCallBack<List<CampusInUserLocation>>()
 		{
 
 		    @Override
 		    public void done(List<CampusInUserLocation> retObject,
 			    Exception e)
 		    {
 			friendsLocation.clear();
 			if (e == null && retObject != null)
 			{
 			    for (CampusInUserLocation campusInUserLocation : retObject)
 			    {
 				friendsLocation.put(campusInUserLocation
 					.getUser().getParseUserId(),
 					campusInUserLocation);
 			    }
 			    updateUsersLocationDone = true;
 			}
 
 		    }
 		});
 
 	    }
 	});
 
 	// update the messages
 	dao.getMessages(new DataAccesObjectCallBack<List<CampusInMessage>>()
 	{
 
 	    @Override
 	    public void done(List<CampusInMessage> retObject, Exception e)
 	    {
 		if (e == null && retObject != null)
 		{
 		    messages = new HashMap<String, CampusInMessage>();
 		    for (CampusInMessage campusInMessage : retObject)
 		    {
 			messages.put(campusInMessage.getParseId(),
 				campusInMessage);
 		    }
 		}
 
 	    }
 	});
     }
     
     private void clearHashes()
     {
     	//clear the hash table in order to remove old events
     	allEvents.clear();
     	meeting.clear();
     	tests.clear();
     	lesons.clear();
     	messages.clear();
     	friendsLocation.clear();
     	friendsHash.clear();
     }
     private void resetUpdateflags()
     {
 	updateEventDone = false;
 	updateUsersDone = false;
 	updateUsersLocationDone = false;
     }
 	private void getfacebookProfilePictures()
 	{
 		friendsProfilePictures.clear();
 		for (final CampusInUser friend: friendsHash.values()) {
 			dao.getFriendProfilePicture(friend.getFaceBookUserId(), new DataAccesObjectCallBack<Drawable>() {
 				
 				@Override
 				public void done(Drawable retObject, Exception e) {
 					friendsProfilePictures.put(friend.getParseUserId(), retObject);
 					
 				}
 			});
 			
 		}
 	}
     // this method will cerate a new thread in order to update the view mpdel in
     // case a thread is allready doing it the current thread will
     // sleep since
     public void updateViewModelInBackground(
 	    final DataAccesObjectCallBack<Integer> callBack)
     {
 
 	// new UpdateViewModelInBackground().execute(callBack);
 	AsyncTask<Integer, Integer, Integer> at = new AsyncTask<Integer, Integer, Integer>()
 	{
 
 	    @Override
 	    protected Integer doInBackground(Integer... params)
 	    {
 		synchronized (this)
 		{
 
 		    updateViewModel();
 		    while (!updateEventDone || !updateUsersDone
 			    || !updateUsersLocationDone || !updateMesagesDone)
 		    {
 			try
 			{
 			    wait(50);
 			} catch (InterruptedException e)
 			{
 			    // TODO Auto-generated catch block
 			    e.printStackTrace();
 			}
 
 		    }
 		}
 		resetUpdateflags();
 		return 0;
 	    }
 
 	    @Override
 	    protected void onPostExecute(Integer result)
 	    {
 		// TODO Auto-generated method stub
 		super.onPostExecute(result);
 		callBack.done(0, null);
 	    }
 	};
 	if (at != null)
 	    at.execute();
     }
 
     private void loadCurrentUser()
     {
 	dao.loadCurrentCampusInUser(new DataAccesObjectCallBack<CampusInUser>()
 	{
 
 	    @Override
 	    public void done(CampusInUser retObject, Exception e)
 	    {
 		if (e == null && retObject != null)
 		    currentUser = retObject;
 			FacebookServices.getPictureForFacebookId(currentUser.getFaceBookUserId(),"width=400&height=300",new DataAccesObjectCallBack<Drawable>() {
 				
 				@Override
 				public void done(Drawable retObject, Exception e) {
 					if(retObject!=null && e==null)
 						currentUserProfilePic=retObject;
 					
 				}
 			});
 	    }
 	});
     }
 
     public Drawable getCurrentUserProfilePic() {
 		return currentUserProfilePic;
 	}
     /*
      * After finish update the view model obj an intent will invoke to update
      * the receivers. (they will update the view with the changes.)
      */
     // TODO-I think the best location will be in the controller.
     public Collection<CampusInEvent> getAllEvents()
     {
 	return allEvents.values();
     }
 
     public Collection<CampusInMessage> getAllMessages()
     {
 	if (messages != null)
 	    return messages.values();
 	return new LinkedList<CampusInMessage>();
     }
 
     public Collection<CampusInUserLocation> getAllFriendsLocation()
     {
 
 	if (friendsLocation != null)
 	    return friendsLocation.values();
 	return new LinkedList<CampusInUserLocation>();
     }
 
     public Collection<CampusInUser> getAllFriends()
     {
 	if (friendsHash != null)
 	{
 	    return friendsHash.values();
 	}
 	return new LinkedList<CampusInUser>();
     }
 
     /**
      * add the event to the correct Hash Map
      * 
      * @param toAdd
      */
     public void addEvent(CampusInEvent toAdd)
     {
 	allEvents.put(toAdd.getParseId(), toAdd);
 	if (toAdd != null)
 	{
 	    switch (toAdd.getEventType())
 	    {
 	    case CLASS:
 		lesons.put(toAdd.getParseId(), toAdd);
 		break;
 	    case TEST:
 		tests.put(toAdd.getParseId(), toAdd);
 		break;
 	    case MEETING:
 		meeting.put(toAdd.getParseId(), toAdd);
 		break;
 	    default:
 		break;
 	    }
 	}
 
     }
 
     public class UpdateViewModelInBackground extends
 	    AsyncTask<DataAccesObjectCallBack<Integer>, Integer, Integer>
     {
 	DataAccesObjectCallBack<Integer> callBack;
 
 	@Override
 	protected Integer doInBackground(
 		DataAccesObjectCallBack<Integer>... params)
 	{
 	    if (params != null && params.length > 0)
 		callBack = params[0];
 	    updateViewModel();
 	    return 0;
 	}
 
 	@Override
 	protected void onPostExecute(Integer result)
 	{
 	    if (callBack != null)
 		callBack.done(0, null);
 	}
 
     }
     public CampusInEvent getEventById(String idToGet)
     {
 	return idToGet == null ? null : allEvents.get(idToGet);
     }
     public Drawable getUserProfilePicture(final String parseUserId)
 	{
 		if(friendsProfilePictures.containsKey(parseUserId))
 			return friendsProfilePictures.get(parseUserId);
 		//the picture does not exist, lets try to get it for the next time.
 		CampusInUser toGet=friendsHash.get(parseUserId);
 		if(toGet!=null)
 		{
 			dao.getFriendProfilePicture(toGet.getFaceBookUserId(), new DataAccesObjectCallBack<Drawable>() {
 				
 				@Override
 				public void done(Drawable retObject, Exception e) {
 					if(retObject!=null && e==null)
 						friendsProfilePictures.put(parseUserId, retObject);
 					
 				}
 			});
 		}
 		//for now return null, next time the picture will be in the cash
 		return null;
 	}
 	public CampusInUser getCampusInUser(String parseId)
 	{
 		if(friendsHash.containsKey(parseId))
 			return friendsHash.get(parseId);
 		return null;
 	}
 	public CampusInMessage getMessageById(String id)
 	{
 		if(messages.containsKey(id))
 			return messages.get(id);
 		return null;
 	}
 	public CampusInUser getCurrentUser()
 	{
 		return currentUser;
 	}
 	public CampusInUserLocation getUserfriendLocation(String id)
 	{
 		return friendsLocation.get(id);
 	}
 
 }
