 // NotificacionesController
 
 // controlador para los casos de uso de notificaciones:
 // configurar, listar, etc.
 
 package georeduy.client.controllers;
 
 // imports
 
 import georeduy.client.activities.ChatActivity;
 import georeduy.client.activities.GCMActivity;
 import georeduy.client.activities.MapaActivity;
 import georeduy.client.activities.R;
 import georeduy.client.activities.VisitDetailActivity;
 import georeduy.client.activities.VisitsListActivity;
 import georeduy.client.model.ChatMessage;
 import georeduy.client.model.ChatRoom;
 import georeduy.client.model.Event;
 import georeduy.client.model.RetailStore;
 import georeduy.client.model.Site;
 import georeduy.client.model.Tag;
 import georeduy.client.model.UserNotificationTag;
 import georeduy.client.model.UserNotificationsTypes;
 import georeduy.client.model.Visit;
 import georeduy.client.util.CommonUtilities;
 import georeduy.client.util.GeoRedClient;
 import georeduy.client.util.OnCompletedCallback;
 
 import java.lang.reflect.Type;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import android.annotation.TargetApi;
 import android.app.Notification;
 import android.app.NotificationManager;
 import android.app.PendingIntent;
 import android.content.Context;
 import android.content.Intent;
 
 import com.google.gson.Gson;
 import com.google.gson.reflect.TypeToken;
 
 public class NotificationsController
 {
 	// *******************
 	// instancia singleton
 	// *******************
 	
 	private static NotificationsController _instance = null;
 	
 	private Map<String, ChatRoom> _chatRooms = new HashMap<String, ChatRoom>();
 	
 	private List<Site> _newSites = new ArrayList<Site>();
 	private List<RetailStore> _newStores = new ArrayList<RetailStore>();
 	private List<Event> _newEvents = new ArrayList<Event>();
 	
 	private UserNotificationsTypes _userNotiTypes;
 	private List<Tag> _userTags;
 	
 	private List<String> _oldSitesId = new ArrayList<String>();
 	
 	private double _latitudCurrent = 0;
 	private double _longitudCurrent = 0;
 	
 	private int notiId = 10;
 	
 	// *************
 	// constructores
 	// *************
 	
 	private NotificationsController () {
 		
 	}
 	
 	public static NotificationsController getInstance() {
 		if (_instance == null) {
 			_instance = new NotificationsController ();
 		}
 		
 		return _instance;
 	}
 
 	// *******
 	// mtodos
 	// *******
 	
 	// obtener datos.
 	
 	public void getSomething () {
 	}
 	
 	public void handleNotification(Context context, Site site) {
 		_newSites.add(site);
 		
     	notifyIfInterested(context, site);
 		
 		Intent intent = new Intent(CommonUtilities.NEW_SITE_ACTION);
         context.sendBroadcast(intent);
 	}
 	
 	public void handleNotification(Context context, RetailStore store) {
 		_newStores.add(store);
 		
 		Intent intent = new Intent(CommonUtilities.NEW_STORE_ACTION);
         context.sendBroadcast(intent);
 	}
 	
 	public void handleNotification(Context context, Event event) {
 		_newEvents.add(event);
 		
 		Intent intent = new Intent(CommonUtilities.NEW_EVENT_ACTION);
         context.sendBroadcast(intent);
 	}
 	
 	public void handleNotification(Context context, Visit visit) {
 		Intent chatIntent = new Intent (context, VisitDetailActivity.class);
         chatIntent.putExtra (VisitsListActivity.EXTRA_VISIT_ID, visit.getId());	 
     	
         if (_userNotiTypes != null && _userNotiTypes.isNotitype1_contactsVisits())
         	generateNotification(context, notiId++, visit.getRealUser().getUserName() + " visited a site near you: ", visit.getRealSite().getName(), chatIntent);
 	}
 	
 	public void handleNotification(Context context, ChatMessage message) {
 		ChatRoom chatRoom;
 		if (_chatRooms.containsKey(message.getFromUserId())) {
 			chatRoom = _chatRooms.get(message.getFromUserId());
 		} else {
 			chatRoom = new ChatRoom(message.getFromUserId());
 			_chatRooms.put(message.getFromUserId(), chatRoom);
 		}
 		
 		chatRoom.addMessage(message);
 		
 		Intent intent = new Intent(CommonUtilities.DISPLAY_CHAT_MESSAGE_ACTION);
         intent.putExtra(CommonUtilities.EXTRA_CHAT_MESSAGE, message.getMessage());
         intent.putExtra(CommonUtilities.EXTRA_CHAT_USER_ID, message.getFromUserId());
         context.sendBroadcast(intent);
         
 		//TODO: Notificar al usuario de que llego un nuevo mensaje
         
         Intent chatIntent = new Intent (context, ChatActivity.class);
         chatIntent.putExtra (CommonUtilities.EXTRA_USER_ID, message.getFromUserId());	 
         chatIntent.putExtra (CommonUtilities.EXTRA_USER_NAME, message.getFromUserName());
     	
         generateNotification(context, 0, message.getFromUserName() + " says:", message.getMessage(), chatIntent);
 	}
 	
 	public void sendMessage(ChatMessage message, OnCompletedCallback callback) {
 		ChatRoom chatRoom;
 		if (_chatRooms.containsKey(message.getToUserId())) {
 			chatRoom = _chatRooms.get(message.getToUserId());
 		} else {
 			chatRoom = new ChatRoom(message.getToUserId());
 			_chatRooms.put(message.getToUserId(), chatRoom);
 		}
 		
 		chatRoom.addMessage(message);
 		
 		Map <String, String> params = new HashMap <String, String>();
         Gson gson = new Gson();
         
 		params.put ("messageInfo", gson.toJson(message));
 		
     	GeoRedClient.PostAsync("/Notifications/SendMessage", params, callback);	
 	}
 
 	public ChatRoom getChatRoom(String partnerUserId) {
 		ChatRoom chatRoom;
 		if (_chatRooms.containsKey(partnerUserId)) {
 			chatRoom = _chatRooms.get(partnerUserId);
 		} else {
 			chatRoom = new ChatRoom(partnerUserId);
 			_chatRooms.put(partnerUserId, chatRoom);
 		}
 		
 		return chatRoom;
     }
 	
 	// obtiene la configuracin de tipos de notificaciones del usuario.
 	
 	public void getNotificationsTypesConfiguration (final OnCompletedCallback callback) {
 		Map <String, String> params = new HashMap <String, String>();
     	GeoRedClient.GetAsync("/Notifications/UserConfig/GetTypes", params, new OnCompletedCallback() {
 			
 			@Override
 			public void onCompleted(String response, String error) {
 				if (error == null) {
 					Gson gson = new Gson();
 					_userNotiTypes = gson.fromJson (response, UserNotificationsTypes.class);
 				}
 				
 				if (callback != null)
 					callback.onCompleted(response, error);
 			}
 		});	
 	}
 	
 	// obtiene la configuracin de etiquetas de notificaciones del usuario.
 	
 	public void getNotificationsTagsConfiguration (final OnCompletedCallback callback) {
 		Map <String, String> params = new HashMap <String, String>();
     	GeoRedClient.GetAsync("/Notifications/UserConfig/GetTags", params, new OnCompletedCallback() {
 			
 			@Override
 			public void onCompleted(String response, String error) {
 				if (error == null) {
 					Gson gson = new Gson();
 		        	Type listType = new TypeToken <ArrayList <Tag>>() {}.getType();
 					_userTags = gson.fromJson (response, listType);
 				}
 				
 				if (callback != null)
 					callback.onCompleted(response, error);
 			}
 		});	
 	}
 	
 	// obtiene la configuracin de tipos de notificaciones del usuario.
 	
 	public void setNotificationsTypesConfiguration (UserNotificationsTypes notitypes, OnCompletedCallback callback) {
 		Map <String, String> params = new HashMap <String, String>();
 		
 		_userNotiTypes = notitypes;
 		
 		// agregar parmetros
         Gson gson = new Gson();
 		params.put ("notitypesInfo", gson.toJson (notitypes));
 		
     	GeoRedClient.PostAsync("/Notifications/UserConfig/SetTypes", params, callback);	
 	}
 	
 	// obtiene la configuracin de etiquetas de notificaciones del usuario.
 	
 	public void setNotificationsTagsConfiguration (List <Tag> tags, OnCompletedCallback callback) {
 		Map <String, String> params = new HashMap <String, String>();
 		
 		_userTags = tags;
 		
 		// agregar parmetros
         Gson gson = new Gson();
 		params.put ("tagsInfo", gson.toJson (tags));
 		
     	GeoRedClient.PostAsync("/Notifications/UserConfig/SetTags", params, callback);	
 	}
 	
 	@TargetApi(16)
     private static void generateNotification(Context context, int id, String title, String message, Intent notificationIntent) {
         int icon = R.drawable.ic_stat_gcm;
         long when = System.currentTimeMillis();
 
         // set intent so it does not start a new activity
         notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                 Intent.FLAG_ACTIVITY_SINGLE_TOP);
         PendingIntent intent =
                 PendingIntent.getActivity(context, 0, notificationIntent, 0);
         
         Notification notification = new Notification.Builder(context)
         .setContentTitle(title)
         .setContentText(message).setSmallIcon(icon)
         .setContentIntent(intent)
         .setWhen(when)
         .build();
         
         NotificationManager notificationManager = (NotificationManager)
                 context.getSystemService(Context.NOTIFICATION_SERVICE);
         
         notification.flags |= Notification.FLAG_AUTO_CANCEL;
         notificationManager.notify(id, notification);
     }
 	
 	public List<Site> getNewSites() {
 		List<Site> sites = _newSites;  
 		_newSites = new ArrayList<Site>();
 		
 		return sites;
 	}
 	
 	public List<RetailStore> getNewStores() {
 		List<RetailStore> stores = _newStores;  
 		_newStores = new ArrayList<RetailStore>();
 		
 		return stores;
 	}
 
 	public List<Event> getNewEvents() {
 		List<Event> events = _newEvents;  
 		_newEvents = new ArrayList<Event>();
 		
 		return events;
 	}
 	
 	public void notifyIfInterested(final Context context, final Site site) {
 		if (!_oldSitesId.contains(site.getId()) && _userNotiTypes != null && _userNotiTypes.isNotitype4_sites()) {
 			for (Tag tag : site.getTags()) {
 				for (Tag userTag : _userTags) {
 					if (userTag.equals(tag) && userTag.isChecked()) {
 						if (CommonUtilities.distance(_longitudCurrent, _latitudCurrent, site.getCoordinates()[0], site.getCoordinates()[1]) <= CommonUtilities.BROADCAST_RANGE) {
 							_oldSitesId.add(site.getId());
 							Intent mapIntent = new Intent (context, MapaActivity.class);
 							generateNotification(context, notiId++, "New site " + site.getName(), site.getDescription(), mapIntent);	
 						}
						return;
 					}
 				}
 			}
 		}
 	}
 	
 	public void setCurrentLocation(double latitude, double longitude) {
 		_latitudCurrent = latitude;
 		_longitudCurrent = longitude;
 		
 		Map <String, String> params = new HashMap <String, String>();
 		params.put ("latitude", Double.toString(latitude));
 		params.put ("longitude", Double.toString(longitude));
 		
     	GeoRedClient.GetAsync("/Notifications/LocationChange", params, null);	
 	}
 }
