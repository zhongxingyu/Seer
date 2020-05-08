 package ch.unibe.ese.shopnote.share;
 
 import java.util.List;
 
 import android.content.Context;
 import ch.unibe.ese.shopnote.core.BaseActivity;
 import ch.unibe.ese.shopnote.core.Friend;
 import ch.unibe.ese.shopnote.core.FriendsManager;
 import ch.unibe.ese.shopnote.core.Item;
 import ch.unibe.ese.shopnote.core.ItemException;
 import ch.unibe.ese.shopnote.core.ListManager;
 import ch.unibe.ese.shopnote.core.Recipe;
 import ch.unibe.ese.shopnote.core.ShoppingList;
 import ch.unibe.ese.shopnote.share.requests.CreateSharedListRequest;
 import ch.unibe.ese.shopnote.share.requests.FriendRequest;
 import ch.unibe.ese.shopnote.share.requests.GetSharedFriendsRequest;
 import ch.unibe.ese.shopnote.share.requests.Request;
 import ch.unibe.ese.shopnote.share.requests.ShareListRequest;
 import ch.unibe.ese.shopnote.share.requests.UnShareListRequest;
 import ch.unibe.ese.shopnote.share.requests.listchange.ItemRequest;
 import ch.unibe.ese.shopnote.share.requests.listchange.ListChangeRequest;
 import ch.unibe.ese.shopnote.share.requests.listchange.RecipeDescriptionRequest;
 
 /**
  * Gets passed in a RequestSender to listen to its result
  * Updates all neccessary information in the core and the UI
  *
  */
 
 public class AnswerHandler {
 
 	private BaseActivity context;
 	private FriendsManager friendsManager;
 	private SyncManager syncManager;
 	private ListManager listManager;
 	
 	public AnswerHandler(Context context) {
 		this.context = (BaseActivity) context;
 		friendsManager = this.context.getFriendsManager();
 		syncManager = this.context.getSyncManager();
 		listManager = this.context.getListManager();
 	}
 	
 	/**
 	 * Sets answers to requests and tells the Handler to handle them accordingly
 	 * @param requests
 	 */
 	public void setRequests(Request... requests) {
 		for (Request r: requests) {
 			if(r.wasSuccessful()) {
 				setConsequences(r);
 			}
 			if(r.isHandled()) {
 				setHandledConsequences(r);
 			} else {
 				syncManager.addRequest(r);
 			}
 		}
 	}
 	
 	private void setHandledConsequences(Request request) {
 		switch (request.getType()) {
 		case Request.FRIEND_REQUEST:
 			long friendId = ((FriendRequest)request).getFriendId();
 			friendsManager.setFriendHasApp(friendId, false);
 		}
 	}
 
 	/**
 	 * Set consequences for answers on requests
 	 * @param request
 	 */
 	private void setConsequences(Request request) {
 		switch (request.getType()) {
 		
 		// Empty requests are like pings, just say "hello"
 		case Request.EMPTY_REQUEST:
 			return;
 		
 		// Sent a FriendRequest to the server and got the answer if my friend is registered with the app
 		case Request.FRIEND_REQUEST:
 			long friendId = ((FriendRequest)request).getFriendId();
 			friendsManager.setFriendHasApp(friendId, true);
 			return;
 			
 		// ShareListRequest => List has been successfully shared with my friend
 		case Request.SHARELIST_REQUEST:
 			ShareListRequest slRequest = (ShareListRequest)request;
 			if(slRequest.isRecipe()) {
 				Recipe recipe = listManager.getRecipeAt(slRequest.getListId());
 				recipe.setShared(true);
 				for (Item i : recipe.getItemList()) {
 					ItemRequest iRequest = new ItemRequest(context.getMyPhoneNumber(), recipe.getId(), i);
 					iRequest.isRecipe(true);
 					syncManager.addRequest(iRequest);
 				}
 				syncManager.addRequest(new RecipeDescriptionRequest(context.getMyPhoneNumber(), recipe.getId(), recipe.getNotes()));
 			} else {
 				ShoppingList list = listManager.getShoppingList(slRequest.getListId());
 				list.setShared(true);
 				for (Item i : listManager.getItemsFor(list)) {
 					ItemRequest iRequest = new ItemRequest(context.getMyPhoneNumber(), list.getId(), i);
 					iRequest.setBought(i.isBought());
 					syncManager.addRequest(iRequest);
 				}
 			}
 			syncManager.synchronise(context);
 			return;
 			
 		// UnshareListRequest => My friend has been successfully deleted from the sharing list on the server
 		case Request.UNSHARELIST_REQUEST:
 			ShoppingList list2 = listManager.getShoppingList(((UnShareListRequest) request).getListId());
 			if (list2 != null) {
 				if (friendsManager.getSharedFriends(list2).isEmpty()) {
 					list2.setShared(false);
 				}
 			}
 			return;
 			
 		// The server asks me to create a new shared list (one that has been shared by another user)
 		case Request.CREATE_SHARED_LIST_REQUEST:
 			CreateSharedListRequest cslRequest = (CreateSharedListRequest)request;
 			GetSharedFriendsRequest gsfRequest2 = new GetSharedFriendsRequest(context.getMyPhoneNumber(), 0);
 			String listName = cslRequest.getListName();
 			long serverId = cslRequest.getServerListid();
 			long id = 0;
 			if(cslRequest.isRecipe()) {
 				Recipe newRecipe = new Recipe(listName);
 				newRecipe.setShared(true);
 				// Sets temporary server list Id (for incoming items)
 				newRecipe.setServerId(serverId);
 				// Sets local List ID
 				listManager.saveRecipe(newRecipe);
 				id = newRecipe.getId();
 				gsfRequest2.isRecipe(true);
 			} else {
 				ShoppingList newList = new ShoppingList(listName);
 				newList.setShared(true);
 				// Sets temporary server list Id (for incomming items)
 				newList.setServerListId(serverId);	
 				// Sets local List ID
 				listManager.saveShoppingList(newList);
 				id = newList.getId();
 			}
 			gsfRequest2.setLocalListId(id);
 			cslRequest.setLocalListId(id);
 			syncManager.addRequest(request);
 			syncManager.addRequest(gsfRequest2);
 			syncManager.synchronise(context);
 			return;
 			
 		// Get all Participants of a shared list
 		case Request.GET_SHARED_FRIENDS_REQUEST:
 			GetSharedFriendsRequest gsfRequest = (GetSharedFriendsRequest)request;
 			if(gsfRequest.isRecipe()) {
 				Recipe recipe = listManager.getRecipeAt(gsfRequest.getLocalListId());
 				for (Friend friend : friendsManager.getSharedFriends(recipe)) {
 					friendsManager.removeFriendFromRecipe(recipe, friend);
 				}
 				for (String number : ((GetSharedFriendsRequest)request).getFriendNumbers()) {
 					friendsManager.addFriend(new Friend(number, "User"));
 					friendsManager.addFriendToRecipe(recipe, friendsManager.getFriendWithPhoneNr(number));
 				}
 //				if(friendsManager.getSharedFriends(recipe).isEmpty()) {
 //					recipe.setShared(false);
 //				}
 			} else {
 				ShoppingList list3 = listManager.getShoppingList(gsfRequest.getLocalListId());
 				// Drop all friends of a list and add them again if they are still in the list
 				for (Friend friend : friendsManager.getSharedFriends(list3)) {
 					friendsManager.removeFriendFromList(list3, friend);
 				}
 				for (String number : ((GetSharedFriendsRequest)request).getFriendNumbers()) {
 					friendsManager.addFriend(new Friend(number, "User"));
 					friendsManager.addFriendToList(list3,friendsManager.getFriendWithPhoneNr(number));
 				}
 //				if(friendsManager.getSharedFriends(list3).isEmpty()) {
 //					list3.setShared(false);
 //				}
 			}
 			return;
 
 		// Those are the general requests concerning the content of a list (name, items, ...)
 		// See processListChangeRequest() for further details on how to handle them
 		case Request.LIST_CHANGE_REQUEST:
 			processListChangeRequest((ListChangeRequest)request);
 			return;
 		}
 	}
 	
 	/**
 	 * This method processes all requests that want to change the content or the name of a list
 	 * @param request
 	 */
 	private void processListChangeRequest(ListChangeRequest request) {
 		ShoppingList list = new ShoppingList("Error");
 		Recipe recipe = new Recipe("Error");
 		if(request.islistIdPending()) {
 			if (request.isRecipe()) {
 				List<Recipe> recipes = listManager.getRecipes();
 				for (Recipe re : recipes) {
 					if(re.getServerId() == request.getLocalListId()) {
 						recipe = re;
 					}
 				}
 			} else {
 				List<ShoppingList> shoppingLists = listManager.getShoppingLists();
 				for (ShoppingList sl : shoppingLists) {
 					if(sl.getServerListId() == request.getLocalListId()) {
 						list = sl;
 					}
 				}
 			}
 		} else {
 			if(request.isRecipe()) {
 				recipe = listManager.getRecipeAt(request.getLocalListId());
 			} else {
 				list = listManager.getShoppingList(request.getLocalListId());
 			}
 			
 		}
 		if(list == null) {
 			// If the list doesn't exist here, just delete yourself from the server
 			System.err.println("List with id " + request.getLocalListId() + " doesn't exist!");
 			UnShareListRequest uslRequest = new UnShareListRequest(context.getMyPhoneNumber(), context.getMyPhoneNumber(), request.getLocalListId());
 			syncManager.addRequest(uslRequest);
 			return;
 		}
 		if(recipe == null) {
 			// If the recipe doesn't exist here, just delete yourself from the server
 			System.err.println("Recipe with id " + request.getLocalListId() + " doesn't exist!");
 			UnShareListRequest uslRequest = new UnShareListRequest(context.getMyPhoneNumber(), context.getMyPhoneNumber(), request.getLocalListId());
 			uslRequest.isRecipe(true);
 			syncManager.addRequest(uslRequest);
 			return;
 		}
 		switch (request.getSubType()) {
 		
 		// One of your sharing partners has changed the name of the list
 //		case ListChangeRequest.RENAME_LIST_REQUEST:
 //			list.setName(((RenameListRequest)request).getNewName());
 //			listManager.saveShoppingList(list);
 //			return;
 			
 		// One of your sharing partners has added/changed an item in the list
 		case ListChangeRequest.ITEM_REQUEST:
 			Item receivedItem = ((ItemRequest)request).getItem().copy();
 			List<Item> itemlist;
 			
 			if(request.isRecipe()) {
 				itemlist = recipe.getItemList();
 			} else {
 				itemlist = listManager.getItemsFor(list);
 			}
 			
 			Item localItem = receivedItem;
 			for(Item i: itemlist) {
 				if(i.getName().equals(receivedItem.getName())) {
 					localItem = i;
 				}
 			}
 			// set change notification count
 			if (request.isRecipe()) {
 				int changesCountRecipe = recipe.getChangesCount();
 				if (((ItemRequest) request).isDeleted()) {
 					recipe.removeItem(localItem);
 				} else {
 					recipe.addItem(localItem);
 				}
 				changesCountRecipe++;
 				recipe.setChangesCount(changesCountRecipe);
 				listManager.saveRecipe(recipe);
 			} else {
 				int changesCountList = list.getChangesCount();
 				if (((ItemRequest) request).isDeleted()) {
 					listManager.removeItemFromList(localItem, list);
 				} else {
 					localItem.setBought(((ItemRequest) request).isBought());
 					if (itemlist.contains(localItem)) {
 						listManager.updateItemInList(localItem, list);
 					} else {
 						try {
 							listManager.addItemToList(localItem, list);
 						} catch (ItemException e) {
 							// There is already an item with the same name but different unit in the list. so we do nothing here.
 						}
 					}
 				}
 				changesCountList++;
 				list.setChangesCount(changesCountList);
 				listManager.saveShoppingList(list);
 			}
 
 
 			return;
 			
 		case ListChangeRequest.RECIPE_DESCRIPTION_REQUEST:
 			recipe.setNotes(((RecipeDescriptionRequest)request).getDescription());
 			listManager.saveRecipe(recipe);	
 			return;
 			
 		// One of your sharing partners has removed you :(
 		// The server now asks you to set your list to unshared
 		case ListChangeRequest.SET_UNSHARED_REQUEST:
 			if(request.isRecipe()) {
 				recipe.setShared(false);
 				for (Friend friend : friendsManager.getSharedFriends(recipe)) {
 					friendsManager.removeFriendFromRecipe(recipe, friend);
 				}
 			} else {
 				list.setShared(false);
 				for (Friend friend : friendsManager.getSharedFriends(list)) {
 					friendsManager.removeFriendFromList(list, friend);
 				}
 			}
 			return;
 		}
 	}
 
 	/**
 	 * Calls the refresh method on the activity
 	 * If the activity is already destroyed it simply does nothing (except a little memory leak)
 	 * This is the only method which is called on the UI thread after the communication with the server
 	 */
	public void updateUI() {
 		context.isSyncing(false);
 		context.refresh();
 	}
 }
