 package il.ac.shenkar.in.bl;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Stack;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Bitmap;
 import android.graphics.drawable.BitmapDrawable;
 import android.graphics.drawable.Drawable;
 import android.util.Log;
 import android.widget.Toast;
 import il.ac.shenkar.cadan.MapManager;
 import il.ac.shenkar.cadan.MessageHalper;
 import il.ac.shenkar.cadan.R;
 import il.ac.shenkar.cadan.ViewModel;
 import il.ac.shenkar.common.CampusInConstant;
 import il.ac.shenkar.common.CampusInEvent;
 import il.ac.shenkar.common.CampusInMessage;
 import il.ac.shenkar.common.CampusInUser;
 import il.ac.shenkar.common.CampusInUserLocation;
 import il.ac.shenkar.in.dal.CloudAccessObject;
 import il.ac.shenkar.in.dal.DataAccesObjectCallBack;
 import il.ac.shenkar.in.dal.IDataAccesObject;
 import il.ac.shenkar.in.dal.Model;
 
 /**
  * the Controller Object is a singleton object which responsible to keep the
  * data of the Program consistentive with the DB to get instance of controller
  * you will have to supply with Context
  * 
  * @author Jacob
  * 
  */
 public class Controller implements ICampusInController {
 	private static Controller instance = null;
 	private Boolean viewModelIsUpdating = false;
 	private Context context;
 	private IDataAccesObject cloudAccessObject;
 	private ViewModel viewModel;
 	private CampusInUser currentUser;
 	private MapManager mapManager;
 	private Boolean updatingViewModel = false;
 	private Boolean updateAgainViewModel = false;
 	/**
 	 * this List will hold all of the Events we want to save to the cloud only
 	 * if the save is successful the event object will be thrown from the list
 	 */
 	private List<CampusInEvent> saveToCLoudEventQue;
 
 	private Controller(Context context) {
 		// private c'tor
 		this.context = context;
 		cloudAccessObject = CloudAccessObject.getInstance();
 		viewModel = new ViewModel(context);
 		saveToCLoudEventQue = new ArrayList<CampusInEvent>();
 		// get the current logged in user
 
 		cloudAccessObject
 				.loadCurrentCampusInUser(new DataAccesObjectCallBack<CampusInUser>() {
 
 					@Override
 					public void done(CampusInUser retObject, Exception e) {
 
 						if (e == null && retObject != null) {
 							currentUser = retObject;
 						}
 
 					}
 				});
 
 	}
 
 	public static Controller getInstance(Context context) {
 		if (instance == null)
 			instance = new Controller(context);
 		return instance;
 	}
 
 	@Override
 	public void getCurrentUserFriendList(
 			final ControllerCallback<List<CampusInUser>> callBack) {
 		MessageHalper.showProgressDialog("gettig " + currentUser.getFirstName()
 				+ " friends", context);
 		if (callBack != null)
 			callBack.done(
 					new ArrayList<CampusInUser>(viewModel.getAllFriends()),
 					null);
 	}
 
 	@Override
 	public void getCurrentUser(final ControllerCallback<CampusInUser> callBack) {
 		if (currentUser != null)
 			callBack.done(currentUser, null);
 		else {
 			cloudAccessObject
 					.loadCurrentCampusInUser(new DataAccesObjectCallBack<CampusInUser>() {
 
 						@Override
 						public void done(CampusInUser retObject, Exception e) {
 							if (retObject != null && e == null) {
 								// load is a success -> return the recived
 								// Object
 								callBack.done(retObject, null);
 							} else {
 								// en error occered -> retutn the Exeptiom
 								callBack.done(null, e);
 							}
 
 						}
 					});
 		}
 	}
 
 	@Override
 	public void getCurrentUserAllEvents(
 			ControllerCallback<List<CampusInEvent>> callBack) {
 
 		// in the mean tiime just a dummy implementation to display some data in
 		// the list
 		// TODO: yaki - later on to bring the real list of events from the
 		// relevant object
 		ArrayList<CampusInEvent> toReturn = new ArrayList<CampusInEvent>();
 		CampusInEvent currEvent;
 		if (callBack != null)
 			callBack.done(
 					new ArrayList<CampusInEvent>(viewModel.getAllEvents()),
 					null);
 
 		// for (int i=0; i<40; i++)
 		// {
 		// currEvent = new CampusInEvent();
 		// currEvent.setDate(new GregorianCalendar());
 		// currEvent.setDescription("description " + i);
 		// currEvent.setHeadLine("Title "+i);
 		// toReturn.add(currEvent);
 		// }
 		//
 		// callBack.done(toReturn, null);
 
 	}
 
 	@Override
 	public void sendMessage(CampusInMessage message,
 			ControllerCallback<Integer> callBack) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void saveEvent(CampusInEvent toAdd,
 			final ControllerCallback<String> callBack) {
 		// TODO: to draw the event to the user interface before saving it to the
 		// cloud
 		if (toAdd != null) {
 			// add the toAdd Object to the EventQue;
 			saveToCLoudEventQue.add(toAdd);
 			for (final CampusInEvent event : saveToCLoudEventQue) {
 				cloudAccessObject.sendEvent(event,
 						new DataAccesObjectCallBack<String>() {
 
 							@Override
 							public void done(String retObject, Exception e) {
 								if (retObject != null) {
 									saveToCLoudEventQue.remove(event);
 									// viewModel
 									callBack.done(retObject, null);
 								} else
 									callBack.done(null, e);
 							}
 						});
 			}
 		} else {
 			callBack.done(null, new NullPointerException(
 					"the Event you tried to save is null"));
 		}
 	}
 
 	@Override
 	public void updateViewModel(final ControllerCallback<Integer> callBack) {
 		Log.i("CampusIn","Start the updateing process of the view model");
 		if (!updatingViewModel) {
 			Log.i("CampusIn", "updatingViewModel was false");
 			updatingViewModel = true;
 			viewModel
 					.updateViewModelInBackground(new DataAccesObjectCallBack<Integer>() {
 
 						@Override
 						public void done(Integer retObject, Exception e) {
 							if (callBack != null)
 								callBack.done(retObject, e);
 							Log.i("CampusIn","Finish updating the view model");
 							invokeViewModelUpdated();
 							updatingViewModel=false;
 							if (updateAgainViewModel) {
 								Log.i("CampusIn"," update again the view model");
 								updateAgainViewModel=false;
 								updateViewModel(null);
 							}
 						}
 					});
 		} else
 			updateAgainViewModel = true;
 
 	}
 
 	private void invokeViewModelUpdated() {
 		Intent inti = new Intent();
 		inti.setAction(CampusInConstant.VIEW_MODEL_UPDATED);
 		context.sendBroadcast(inti);
 	}
 
 	@Override
 	public void drawAllEvents(MapManager manager) {
 		Collection<CampusInEvent> events = viewModel.getAllEvents();
 		for (CampusInEvent toDraw : events) {
 			mapManager.addOrUpdateEventMarker(toDraw);
 		}
 	}
 
     public void setMapManager(MapManager mapManager)
     {
 	this.mapManager = mapManager;
     }
 
     @Override
     public void getCurrentUserFriendsLocationList(ControllerCallback<List<CampusInUserLocation>> callBack)
     {
 	if (callBack != null)
 	    callBack.done(new ArrayList<CampusInUserLocation>(viewModel.getAllFriendsLocation()), null);
     }
 
     @Override
     public CampusInEvent getEvent(String eventId)
     {
 	return eventId == null ? null : viewModel.getEventById(eventId);
     }
 
     public void addEventToLocalMap(CampusInEvent toAdd)
     {
 	viewModel.addEvent(toAdd);
     }
 
     @Override
     public void getAllCumpusInUsers(final ControllerCallback<List<CampusInUser>> callback)
     {
 	if (callback != null)
 	{
 	    cloudAccessObject.getAllCumpusInUsers(new DataAccesObjectCallBack<List<CampusInUser>>()
 	    {
 		@Override
 		public void done(List<CampusInUser> retObject, Exception e)
 		{
 		    callback.done(retObject, e);
 		}
 	    });
 	}
 	else
 	    return;
     }
 
     @Override
     public void addFriendsToCurrentUserFriendList(List<CampusInUser> friendsTOAdd)
     {
 	// for now i add it one by one
 	// Cadan need to implement a method to save bulk of friends all at one
 	if (friendsTOAdd != null)
 	{
 	    for (CampusInUser user : friendsTOAdd)
 	    {
 		cloudAccessObject.addFriendToFriendList(user, null);
 	    }
 	}
     }
 
     @Override
     public void removeFriendsFromCurrentUserFriendList(List<CampusInUser> friendsToRemove)
     {
 	if (friendsToRemove != null)
 	{
 	    for (CampusInUser user : friendsToRemove)
 	    {
 		cloudAccessObject.removeFriendFromFriendList(user, null);
 	    }
 	}
	}
 
     @Override
     public Drawable getFreindProfilePicture(String parseId, int width, int height)
     {
 	Drawable retPic = viewModel.getUserProfilePicture(parseId);
 	if (retPic != null)
 	    try
 	    {
 		return resizePic(retPic, width, width);
 	    }
 	    catch (Exception e)
 	    {
 		// TODO Auto-generated catch block
 		return context.getResources().getDrawable(R.drawable.com_facebook_profile_picture_blank_portrait);
 	    }
 	return context.getResources().getDrawable(R.drawable.com_facebook_profile_picture_blank_portrait);
     }
 
     private Drawable resizePic(Drawable paramDrawable, int paramInt1, int paramInt2) throws Exception
     {
 	Bitmap localBitmap = ((BitmapDrawable) paramDrawable).getBitmap();
 	return new BitmapDrawable(context.getResources(), Bitmap.createScaledBitmap(localBitmap, paramInt1, paramInt2, true));
     }
 
     @Override
     public CampusInUser getCampusInUser(String parseId)
     {
 	return viewModel.getCampusInUser(parseId);
     }
     @Override
     public void navigateToEvent(CampusInEvent event)
     {
 	if (event != null)
 	{
 	    // navigate to the location
 	    mapManager = MapManager.getInstance(null, 0); /*
 							   * in this point the
 							   * map is already
 							   * initialized so i
 							   * pass dummy params
 							   */
 	    mapManager.moveCameraToEvent(event.getParseId());
 	}
     }
 
 }
