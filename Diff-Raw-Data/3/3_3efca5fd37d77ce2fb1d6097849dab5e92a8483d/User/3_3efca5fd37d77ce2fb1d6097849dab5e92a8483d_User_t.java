 package de.quiz.User;
 
 
 
 import javax.servlet.AsyncContext;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 import de.fhwgt.quiz.application.Player;
 
 /**
  * this class saves the userdata at register as preparation for the DBM to write
  * it into the database.
  * 
  * @author Patrick Na
  */
 public class User implements IUser {
 
 	private HttpSession session;
 	private String userID;
 	private String name;
 	private Player playerObject;
 	private int wsID;
 	private HttpServletRequest request;
 	private HttpServletResponse response;
 	private AsyncContext asyncCo;
 
 
 	/**
 	 * default constructor of User object
 	 * 
 	 */
 	public User() {
 	}
 
 	/**
 	 * constructor of User object
 	 * 
 	 */
 	public User(String id) {
 
 		this.name = id;
 	}
 
 	/**
 	 * constructor of User object
 	 * 
 	 */
 	public User(String id, String name, HttpSession session, Player player) {
 
 		this.userID = id;
 		this.session = session;
 		this.name = name;
 		this.playerObject = player;
 	}
 
 	/*
 	 * ################################## setter
 	 * ##################################
 	 */
 
 	/**
 	 * set user session
 	 * 
 	 * @param session
 	 *            the user's session
 	 */
 	public void setSession(HttpSession session) {
 
 		this.session = session;
 	}
 
 	/**
 	 * set the name
 	 * 
 	 * @param _name
 	 *            the user's name
 	 */
 	public void setName(String _name) {
 		name = _name;
 	}
 	
     /**
      * set user id
      * 
      * @param id	the user's id/alias
      */
 	public void setID(String id) {
 
 		this.userID = id;
 	}
 	
     /**
      * set the player object
      * 
      * @param _player
      */
     public void setPlayerObject(Player _player) {
     	playerObject = _player;
     }
     /**
      * set the player request
      * 
      * @param HttpServletRequest
      */
     public void setRequest(HttpServletRequest request) {
     	this.request = request;
     }
     /**
      * set the player response
      * 
      * @param HttpServletResponse
      */
     public void setResponse(HttpServletResponse response) {
     	this.response = response;
    	this.response.setCharacterEncoding("UTF-8");
     }
     /**
      * set the AsnycContext
      * 
      * @param asyncCo
      */
 	public void setAsyncCo(AsyncContext asyncCo){
 		this.asyncCo = asyncCo;
 	}
     /**
      * set the websocket id
      * 
      * @param id
      */
     public void setWSID (int id) {
     	wsID = id;
     }
 
 	/*
 	 * ################################## getter
 	 * ##################################
 	 */
 
 	/**
 	 * getter for session
 	 * 
 	 * @return HttpSession
 	 */
 	public HttpSession getSession() {
 		return session;
 	}
 
 	/**
 	 * getter for the user ID
 	 * 
 	 * @return String
 	 */
 	public String getUserID() {
 		return userID;
 	}
 	/**
 	 * getter for the user's request
 	 * 
 	 * @return HttpServletRequest
 	 */
 	public HttpServletRequest getRequest() {
 		return request;
 	}
 	/**
 	 * getter for the user's response
 	 * 
 	 * @return HttpServletResponse
 	 */
 	public HttpServletResponse getResponse() {
 		return response;
 	}
     /**
      * getter for the AsyncContext
      * 
 	 * @return AsyncContext
 	 */
 	public AsyncContext getAsyncCo(){
 		return asyncCo;
 	}
 	
 	
 	/**
 	 * getter for the user's name
 	 * 
 	 * @return String
 	 */
 	public String getName() {
 		return name;
 	}
 	
     /**
      * getter for the player object
      * 
      *  @return Player
      */
     public Player getPlayerObject () {
     	return playerObject;
     }
     
     /**
      * getter for the websocket id
      * 
      * @return int
      */
     public int getWSID () {
     	return wsID;
     }
 
 	// methods
 
 	/**
 	 * checks if all fields are equal in order to check User objects for
 	 * equality
 	 */
 	public boolean equals(Object object) {
 		IUser user = (IUser) object;
 
 		if (this.name.equals(user.getName()) &&
 				
 		this.userID.equals(user.getUserID())) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * checks if two user objects have the same ID
 	 */
 	public boolean hasSameID(Object object) {
 
 		IUser user = (IUser) object;
 
 		if (this.userID.equals(user.getUserID())) {
 			return true;
 		}
 		return false;
 	}
 }
