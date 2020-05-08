 package ch.unibe.ese.shopnote.server.core;
 
 import java.util.ArrayList;
 
 import ch.unibe.ese.shopnote.server.database.NeodatisDatabaseManager;
 import ch.unibe.ese.shopnote.server.database.SQLiteDatabaseManager;
 import ch.unibe.ese.shopnote.share.requests.EmptyRequest;
 import ch.unibe.ese.shopnote.share.requests.GetSharedFriendsRequest;
 import ch.unibe.ese.shopnote.share.requests.Request;
 import ch.unibe.ese.shopnote.share.requests.ShareListRequest;
 import ch.unibe.ese.shopnote.share.requests.UnShareListRequest;
 import ch.unibe.ese.shopnote.share.requests.CreateSharedListRequest;
 import ch.unibe.ese.shopnote.share.requests.listchange.EmptyListChangeRequest;
 import ch.unibe.ese.shopnote.share.requests.listchange.ListChangeRequest;
 
 /**
  * Forwarding and filtering of Requests
  *
  */
 public class RequestHandler {
 
 	private SQLiteDatabaseManager dbManager;
 	private NeodatisDatabaseManager odbManager;
 	
 	public RequestHandler() {
 		this.dbManager = new SQLiteDatabaseManager();
 		this.odbManager = new NeodatisDatabaseManager(dbManager);
 		dbManager.setOdbManager(odbManager);
 	}
 	
 	/**
 	 * Slices up the request packet into single requests
 	 * @param requests
 	 * @return Answer requests
 	 */
 	public Request[] handle(Request[] requests) {
 		ArrayList<Request> answers = new ArrayList<Request>();
 		for (Request r : requests) {
 			Request[] answer = this.handle(r);
 			for(Request a : answer) {
 				answers.add(a);
 			}
 		}
 		return answers.toArray(new Request[answers.size()]);
 	}
 
 	/**
 	 * This method handles one single request
 	 * There is the possibility that it returns multiple requests
 	 * @param request
 	 * @return Array of Requests
 	 */
 	public Request[] handle(Request request) {
 		switch (request.getType()) {
 		// Empty requests are like a ping, they have no real purpose, it's just a "hello"
 		case Request.EMPTY_REQUEST:
 			return returnRequests(request);
 		
 		// User registers itself on the server
 		// Responsible: SQLiteDatabaseManager for creating a userId
 		//		NeodtaisDatabaseManager for creating a new user container
 		case Request.REGISTER_REQUEST:
 			System.out.println("\tRegister request");
 			this.dbManager.addUser(request);
 			return returnRequests(request);
 		
 		// User asks if friend has the app
 		// Responsible: SQLiteDatabaseManager
 		case Request.FRIEND_REQUEST:
 			System.out.println("\tFriend request");
 			if(this.dbManager.findUser(request)>-1)
 				request.setSuccessful();
 			return new Request[]{request};
 			
 		// User wants to share a list with his friend
 		// Responsible: SQLiteDatabaseManager for creating new entries in the sharedList table
 		// 		NeodatisDatabaseManager for putting a new Create shared list to the friends container
 		case Request.SHARELIST_REQUEST:
 			System.out.println("\tShareList request");
 			this.dbManager.shareList((ShareListRequest)request);
 			return returnRequests(request);
 			
 		// User wants to remove a friend from a shared list
 		// Responsible: SQLiteDatabaseManager to remove him from the list
 		case Request.UNSHARELIST_REQUEST:
 			System.out.println("\tUnShareList request");
 			this.dbManager.unShareList((UnShareListRequest)request);
 			return returnRequests(request);
 		
 		// A user shared a list and the NeodatisDatabaseManager put a request in his friends container
 		// This is the answer of the friend: he created a list and sends his localListId to the server
 		// Responsible: SQLiteDatabaseManager to match his local list id with the server list id for 
 		// further ListChangeRequests
 		case Request.CREATE_SHARED_LIST_REQUEST:
 			System.out.println("\tCreate Share List Request answer");
 			this.dbManager.assignLocalToServerListId((CreateSharedListRequest)request);
 			return returnRequests(new EmptyRequest(request.getPhoneNumber()));
 		
 		// A user asks you for all participants of a shared list
 		// You get them by asking the database for all shared users of his local List id
 		case Request.GET_SHARED_FRIENDS_REQUEST:
 			GetSharedFriendsRequest gsfRequest = (GetSharedFriendsRequest)request;
 			long listId = gsfRequest.getLocalListId();
 			System.out.println("\tGet Shared Friends for LocalListID " + listId);
 			EmptyListChangeRequest elcRequest = new EmptyListChangeRequest(request.getPhoneNumber(), listId);
 			elcRequest.isRecipe(gsfRequest.isRecipe());
 			ArrayList<User> sharedwith2 = dbManager.getSharedUsers(elcRequest);
 			int senderId2 = dbManager.findUser(request);
 			for(User u : sharedwith2) {
 				if(u.getUserId() != senderId2) {
 					gsfRequest.addFriendNumber(dbManager.getNumberOfUser(u.getUserId()));
 				}
 			}
 			gsfRequest.setSuccessful();
 			return returnRequests(gsfRequest);
 			
 		// This are general Requests that ask the user to rename his shopping list, add a new Item, ...
 		// They don't need to be processed by the server, but he needs to translate the localListId of 
 		//	the sending user to the LocalListId of the receiving users
 		case Request.LIST_CHANGE_REQUEST:
 			System.out.println("\tGeneral list change requests");
 			int senderId = dbManager.findUser(request);
 			ArrayList<User> sharedwith = dbManager.getSharedUsers((ListChangeRequest)request);
 			for(User u:sharedwith) {
 				// Cannot send requests to myself
 				if(u.getUserId() != senderId) {
 					ListChangeRequest copy = ((ListChangeRequest)request).getCopy();
 					copy.setLocaListId(u.getLocalListid());
 					copy.setListIdPending(u.isPending());
 					odbManager.storeRequest(copy, u.getUserId());
 				}
 			}
 			return returnRequests(new EmptyRequest(request.getPhoneNumber()));
 			
 		default:
 			return returnRequests(new EmptyRequest(request.getPhoneNumber()));
 		}
 
 	}
 	
 	/**
 	 * Returns all requests for the author of the input paramter request (everything from his container)
 	 * @param request
 	 * @return all requests in the users container
 	 */
 	public Request[] returnRequests(Request request) {
 		int userId = dbManager.findUser(request);
 		ArrayList<Request> requests = odbManager.getRequestsForUser(userId);
 		requests.add(request);
		System.out.println("\tUser received " + requests.size() + " answers");
 		return requests.toArray(new Request[requests.size()]);
 	}
 
 }
