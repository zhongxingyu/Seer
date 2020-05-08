 package controllers;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.persistence.Query;
 
 import models.User;
 import models.helpers.UserConnectionHelper;
 import play.Logger;
 import play.data.validation.Error;
 import play.data.validation.Valid;
 import play.db.jpa.JPA;
 import play.mvc.With;
 import DTO.UserDTO;
 import DTO.UserSummaryDTO;
 import assemblers.UserAssembler;
 import assemblers.UserSummaryAssembler;
 import controllers.oauth2.AccessTokenFilter;
 
 /**
  * 
  * @author Alex Jarvis axj7@aber.ac.uk
  */
 @With(JSONRequestTypeFilter.class)
 public class Users extends AccessTokenFilter {
 	
 	/**
 	 * Returns all users that the current authorised user is connected to
 	 * (not all users in the system).
 	 */
 	public static void index() {
 		User authUser = userAuth.getAuthorisedUser();
 		renderJSON(UserSummaryAssembler.writeDTOs(authUser));
 	}
     
 	/**
 	 * Creates a new user in the system.
 	 * 
 	 * @param userDTO
 	 */
     public static void create(@Valid UserDTO user) {
     	if (validation.hasErrors()) {
     		for (Error error : validation.errors()) {
     			Logger.debug(error.getKey() + " : " + error.message());
     		}
 			error(400, "Validation Errors");
 		}
     	
     	if (user != null) {
 	    	// Check for existing users
 	    	User checkUser = User.find("byEmail", user.email).first();
 			if (checkUser != null) {
 				error(400, "Email already exists");
 			}
 	    	
 	    	// Create user
 			UserDTO newUserDTO = UserAssembler.createUser(user);
 			response.status = 201;
 			renderJSON(newUserDTO);
     	} else {
     		error(500, "User is null");
     	}
     }
     
     /**
      * Shows information on a user in the system.
      * 
      * Special behaviour is applied if the user is the authorised user
      * and only users that the authorised user is connected to are allowed
      * to be shown.
      * 
      * @param id
      */
     public static void show(String id) {
     	
     	User authUser = userAuth.getAuthorisedUser();
     	
 		if (id.equals(authUser.id.toString()) || id.equals(authUser.email)) {
 			renderJSON(UserAssembler.writeDTO(authUser, true));
     	} else {
     		// get the user, first by ID and then by email
     		boolean emailID = true;
     		try {
    			Long longID = new Long(id);
     			emailID = false;
     		} catch (NumberFormatException e) {
     			Logger.debug("User id is not a number, maybe an email");
     		}
     		
     		User user = null;
     		if (emailID) {
     			user = User.find("byEmail", id).first();
     		} else {
    			user = User.findById(new Long(id));
     		}
     		
     		// is the user connected?
     		if (user != null) {
 		    	
 	    		if (UserConnectionHelper.isUsersConnected(authUser, user)) {
 	    			renderJSON(UserAssembler.writeDTO(user, false));
 	    		} else {
 	    			error(400, "User not connected");
 	    		}
     		} else {
     			notFound();
     		}
     	}
     }
     
     /**
      * Update
      * TODO: revisit to enable updating via email id.
      * 
      * @param user
      */
     public static void update(Long id, @Valid UserDTO user) {
     	
     	// Only able to update the authorised user
     	if (id.equals(userAuth.getAuthorisedUser().id)) {
     		
 	    	if (validation.hasErrors()) {
 	    		for (Error error : validation.errors()) {
 	    			Logger.debug(error.getKey() + " : " + error.message());
 	    		}
 				error(400, "Validation Errors");
 			}
 	    	
 	    	// Check for existing emails
 	    	User checkUser = User.find("byEmail", user.email).first();
 			if (checkUser != null && !id.equals(checkUser.id)) {
 				error(400, "Email already exists");
 			}
 	    	
 	    	// Update user
 	    	user.id = id;
 	    	renderJSON(UserAssembler.updateUser(user));
 	    	
     	} else {
     		badRequest();
     	}
     }
     
     
     
     /**
      * 
      * @param id
      */
     public static void addUserRequest(String id) {
     	User authUser = userAuth.getAuthorisedUser();
     	User otherUser = getNonAuthorisedUser(id);
     	if (otherUser != null) {
     		boolean success = UserConnectionHelper.createUserConnectionRequest(authUser, otherUser);
     		if (success) {
     			renderJSON("");
     		}
     	}
     	badRequest();
     }
     
     /**
      * 
      * @param id
      */
     public static void acceptUserRequest(String id) {
     	User authUser = userAuth.getAuthorisedUser();
     	User otherUser = getNonAuthorisedUser(id);
     	if (otherUser != null) {
     		boolean success = UserConnectionHelper.removeUserConnectionRequest(otherUser, authUser);
     		if (success) {
     			UserConnectionHelper.createUserConnection(authUser, otherUser);
     			renderJSON("");
     		}
     	}
     	badRequest();
     }
     
     /**
      * 
      * @param id
      */
     public static void declineUserRequest(String id) {
     	User authUser = userAuth.getAuthorisedUser();
     	User otherUser = getNonAuthorisedUser(id);
     	if (otherUser != null) {
     		boolean success = UserConnectionHelper.removeUserConnectionRequest(otherUser, authUser);
     		if (success) {
     			renderJSON("");
     		}
     	}
     	badRequest();
     }
     
     /**
      * For now, just search by firstname
      * @param search
      */
     public static void searchUser(String search) {
     	search = search.trim();
     	String firstName = search;
     	String lastName = search;
     	
     	String[] names = search.split("\\s");
     	if (names.length >= 2) {
     		firstName = names[0];
     		lastName = names[1];
     	}
     	
     	// TODO: move this query away from the controller
     	List<User> userResults = User.find("select u from User u "
     			+ "where LOWER(u.firstName) like LOWER(?) "
     			+ "or LOWER(u.lastName) like LOWER(?)", firstName+"%", lastName+"%").fetch();
     	
     	if (userResults != null && userResults.size() > 0) {
     		List<UserSummaryDTO> userSummaryList = new ArrayList<UserSummaryDTO>();
     		for (User user : userResults) {
     			userSummaryList.add(UserSummaryAssembler.writeRestrictedDTO(user));
     		}
     		renderJSON(userSummaryList);
     	} else {
     		error(404, "Not found");
     	}
     
     }
     
     /**
      * 
      * @param id
      * @return
      */
     private static User getNonAuthorisedUser(String id) {
     	User authUser = userAuth.getAuthorisedUser();
     	
     	// If the id is the authorised user (trying to add themselves)
     	if (id.equals(authUser.id.toString()) || id.equals(authUser.email)) {
     		badRequest();
     	} else {
     		
     		// determine the ID type
     		boolean emailID = true;
     		try {
    			Long longID = new Long(id);
     			emailID = false;
     		} catch (NumberFormatException e) {
     			Logger.debug("User id is not a number, maybe an email");
     		}
     		// get the User
     		User otherUser = null;
     		if (emailID) {
     			otherUser = User.find("byEmail", id).first();
     		} else {
    			otherUser = User.findById(new Long(id));
     		}
     		if (otherUser != null) {
     			return otherUser;
     		}
     	}
     	
     	return null;
     }
 
 }
