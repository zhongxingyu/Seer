 package com.eseteam9.shoppyapp.handlers;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import android.content.Context;
 import android.widget.Toast;
 
 import com.eseteam9.shoppyapp.Notification;
 import com.eseteam9.shoppyapp.shoppingclasses.Item;
 import com.eseteam9.shoppyapp.shoppingclasses.Items;
 import com.eseteam9.shoppyapp.shoppingclasses.ShoppingList;
 import com.eseteam9.shoppyapp.shoppingclasses.ShoppingLists;
 import com.eseteam9.shoppyapp.shoppingclasses.User;
 import com.eseteam9.shoppyapp.shoppingclasses.Users;
 import com.parse.*;
 /**
  * This class communicates between the Online Database and the User.
  * 
  * @author S�bastien Broggi, Sammer Puran, Marc Schneiter, Lukas Galliker
  */
 public class OnlineDatabaseHandler {
 	Context context;
 	Notification[] notifications;
 	
 	public OnlineDatabaseHandler(Context context){
 		this.context = context;
 		Parse.initialize(context, "siN9uAfK3is01V4Yyad62BztutNZN761smpPFdhQ", "zWJquwUQlEw9NHvBuZFFdpMrMIQoIXRy8CjslwY3"); 
 	}
 	
 	//USER
 	public void putUser(User user){
 		ParseObject onlineUser = new ParseObject("Users");
 		onlineUser.put("name", user.name());
 		onlineUser.put("key", user.email());
 		onlineUser.saveEventually();
 	}
 	
 	public void getUser(final String key){
 		ParseQuery<ParseObject> query = ParseQuery.getQuery("Users");
 		//if (!new UserHandler(context).existsUser(key)){
 		if (!Users.existsUserByEmail(context, key)){
 			query.whereEqualTo("key", key);
 			query.findInBackground(new FindCallback<ParseObject>() {
 			    public void done(List<ParseObject> users, ParseException e) {
 			        if (e == null) {
 				        if (users.size()>0){
 				        	ParseObject userObject = users.get(0);
 				            new User(context, userObject.getString("name"), key);
 			        	}
 			        	else
 			        		Toast.makeText(context, "No such user found", Toast.LENGTH_SHORT).show();
 			        	
 			        }
 			        else
 			        	Toast.makeText(context, "No connection. Please try again later", Toast.LENGTH_SHORT).show();
 			    }
 			});
 		}
 	}
 	
 	//LISTS
 	public void putList(final ShoppingList list, final String friendEmail){
 		final ParseObject listObject = new ParseObject("List");
 		listObject.put("title", list.title());
 		listObject.put("owner", Users.getOwner(context).email());
 		listObject.saveEventually(new SaveCallback() {
 			@Override
 			public void done(ParseException e) {
 				if (e == null) {
 			    	final String listKey = listObject.getObjectId();
 			    	list.onlineKey(listKey);
 			    	putItems(listKey, list.id());
 			    	shareList(listKey, friendEmail);
 			    	shareList(listKey, Users.getOwner(context).email());
 				}else
 					Toast.makeText(context, "There was an error. Please try again later", Toast.LENGTH_SHORT).show();
 			}
 		});
 	}
 	
 	protected void getList(String listKey) {
 		ParseQuery<ParseObject> query = ParseQuery.getQuery("List");
 		query.getInBackground(listKey, new GetCallback<ParseObject>() {
 			  public void done(ParseObject parseList, ParseException e) {
 				    if (e == null) {
 				    	ShoppingList list = new ShoppingList(context, parseList.getString("title"));
 				    	list.onlineKey(parseList.getObjectId());
 				    	list.timestamp(parseList.getUpdatedAt());
 					    getListItems(parseList.getObjectId(), list.id());
 				     }
 				 }
 		});
 	}
 	
 	public void shareList(final String onlineKey, final String email){
 		ParseObject shareList = new ParseObject("SharedList");
 		
 		shareList.put("userKey", email);
 		shareList.put("listKey", onlineKey);
 		
 		shareList.saveInBackground(new SaveCallback() {
 	        public void done(ParseException e) {
 	            if (e == null) {
 	            	Toast.makeText(context, "List ist now shared", Toast.LENGTH_SHORT).show();
 	            }else
 	            	Toast.makeText(context, "There was an Error. Please try again later", Toast.LENGTH_SHORT).show();
 	        }
 	    });
 	}
 	
 	public void getSharedLists(){
 		ParseQuery<ParseObject> query = ParseQuery.getQuery("SharedList");
 		User me = Users.getOwner(context);
 		query.whereEqualTo("userKey", me.email());
 		query.findInBackground(new FindCallback<ParseObject>() {
 			  public void done(List<ParseObject> parseLists, ParseException e) {
 				    if (e == null) {
 				      for (int i=0; i < parseLists.size(); i++){
 				    	  ParseObject sharedListObject = parseLists.get(i);
 				    	  String key = sharedListObject.getString("listKey");
 				    	  if(!ShoppingLists.existsOnlineKey(context, key))
 				    		  getList(key);
 				    	  else
 				    		  syncList(ShoppingLists.getByOnlineKey(context, key));				    	  
 				      }
 				      Toast.makeText(context, "Lists Refreshed", Toast.LENGTH_SHORT).show();
 				 }
 			  }
 		});
 	}
 	
 	public void syncList(final ShoppingList list) {
 		ParseQuery<ParseObject> query = ParseQuery.getQuery("List");
 		if (list.onlineKey() != "0"){
 			query.getInBackground(list.onlineKey(), new GetCallback<ParseObject>() {
 				  public void done(ParseObject parseList, ParseException e) {
 					    if ((e == null) && (parseList != null)) {
 				    		Date lastUpdated = parseList.getUpdatedAt();
 				    		if (lastUpdated.after(list.timestamp())){
 					    		list.title(parseList.getString("title"));
 					    		list.timestamp(parseList.getUpdatedAt());
 					    		syncItems(list.id(), list.onlineKey());
 				    		}					    
 					     }
 					 }
 			});
 		}
 	}
 	
 	
 	//ITEMS
 	public void putItems(final String listKey, final int listId){
 		Item[] items = Items.getByListId(context, listId);
 		List<ParseObject> parseItems = new ArrayList<ParseObject>();
 		
 	    for (int i = 0; i < items.length; i++ ){
 	    	ParseObject item = new ParseObject("Item");
 	    	item = parseItem(items[i]);
 	    	item.put("list", listKey);
 	    	parseItems.add(i, item);
 	    }
 	    
 	    //Save Items and update local OnlineKey (to Change!)
 	    ParseObject.saveAllInBackground(parseItems, new SaveCallback() {
             public void done(ParseException e) {
                 if (e == null)
                 	getListItems(listKey, listId);
             }
         });  
 	}
 	
 	public void getListItems(final String onlineKey, final int listId){
 		ParseQuery<ParseObject> query = ParseQuery.getQuery("Item");
 		query.whereEqualTo("list", onlineKey);
 		query.findInBackground(new FindCallback<ParseObject>() {
 		  public void done(List<ParseObject> parseItems, ParseException e) {
 		    if (e == null) {
 		      Items.deleteByListId(context, listId);
 		      Item item;
 		      for (int i=0; i < parseItems.size(); i++){
 		    	  ParseObject itemObject = parseItems.get(i);
 		    	  item = new Item(context, listId, itemObject.getString("name"), itemObject.getString("quantity"));
 		    	  item.onlineKey(itemObject.getObjectId());
 		    	  item.bought(itemObject.getBoolean("bought"));
 		    	  item.timestamp(itemObject.getUpdatedAt());
 		      }
 			Toast.makeText(context, "List downloaded", Toast.LENGTH_SHORT).show();
 		    }
 		  }
 		});
 	}
 	
 	public void checkItem(String itemKey, final boolean status){
 		ParseQuery<ParseObject> query = ParseQuery.getQuery("Item");
 		 
 		query.getInBackground(itemKey, new GetCallback<ParseObject>() {
 		  public void done(ParseObject itemObject, ParseException e) {
 		    if (e == null) {
 		      itemObject.put("bought", status);
 		      itemObject.saveInBackground();
 		    }
 		  }
 		});
 	}
 	
 	public void updateItem(String listKey, final Item item){
 		ParseQuery<ParseObject> query = ParseQuery.getQuery("Item");
 		 
 		query.getInBackground(item.onlineKey(), new GetCallback<ParseObject>() {
 		  public void done(ParseObject itemObject, ParseException e) {
 		    if (e == null) {
 		      itemObject.put("bought", item.bought());
 		      itemObject.put("name", item.name());
 		      itemObject.put("quantity", item.quantity());
 		      itemObject.saveInBackground();
 		    }
 		  }
 		});
 	}
 	
 	public void deleteItem(String itemKey){
 		ParseQuery<ParseObject> query = ParseQuery.getQuery("Item");
 		 
 		query.getInBackground(itemKey, new GetCallback<ParseObject>() {
 		  public void done(ParseObject itemObject, ParseException e) {
 		    if (e == null) {
 		      itemObject.deleteEventually();
 		    }
 		  }
 		});
 	}
 	
 	public ParseObject parseItem(Item item){
 		ParseObject itemObject = new ParseObject("Item");
 		itemObject.put("name", item.name());
 		itemObject.put("quantity", item.quantity());
 		itemObject.put("bought", item.bought());
 		return itemObject;
 	}
 	
 	private void syncItems(final int listId, final String listKey) {
 		ParseQuery<ParseObject> query = ParseQuery.getQuery("Item");
 		query.whereEqualTo("list", listKey);
 			query.findInBackground(new FindCallback<ParseObject>() {
 				  public void done(List<ParseObject> parseItems, ParseException e) {
 					    if (e == null) {
 					    	Item item;
 					    	for (int i=0; i < parseItems.size(); i++){
 					    		ParseObject parseItem = parseItems.get(i);
 					    		String itemKey = parseItem.getObjectId();					    		
 					    		if(!Items.existsOnlineKey(context, itemKey)){
 					    			item = new Item(context, listId, parseItem.getString("name"), parseItem.getString("quantity"));
 					    			item.onlineKey(parseItem.getObjectId());
 					    		}
 					    		else
 					    			item = Items.getByOnlineKey(context, itemKey);
 					    	
 					    	    item.bought(parseItem.getBoolean("bought"));
 					    	    item.timestamp(parseItem.getUpdatedAt());
 					    	}
 					     }
 					 }
 			});	
 	}
 	
 	
 	//NOTIFICATIONS
 	public static void notify(int notificationId, User user){
 			ParseObject onlineUser = new ParseObject("Notifications");
 			onlineUser.put("userKey", user.email());
 			onlineUser.put("notification", notificationId);
 			onlineUser.saveEventually();
 	}
 
 	public Notification[] getNotifications(String email) {
 		ParseQuery<ParseObject> query = ParseQuery.getQuery("Notification");
 		query.whereEqualTo("userKey", email);
 		query.findInBackground(new FindCallback<ParseObject>() {
 		    public void done(List<ParseObject> parseItems, ParseException e) {
 		        if (e == null) {
 		        	notifications = new Notification[parseItems.size()];
 				    for (int i=0; i < parseItems.size(); i++){
 				    	  ParseObject itemObject = parseItems.get(i);
 				    	  Notification notification = new Notification(itemObject.getInt("notification"),
 					    		  						itemObject.getString("userKey"));
 					      notifications[i] = notification;
 				      }
 		        }
 		        
 		    }
 		});
 		return notifications;	
 	}
 	
 	
 }
