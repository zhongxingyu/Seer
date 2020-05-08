 package com.teamsierra.csc191.api.controller;
 
 import com.lambdaworks.crypto.SCryptUtil;
 import com.teamsierra.csc191.api.exception.GenericUserException;
 import com.teamsierra.csc191.api.model.AppointmentType;
 import com.teamsierra.csc191.api.model.GenericModel.UserType;
 import com.teamsierra.csc191.api.model.StylistAvailability;
 import com.teamsierra.csc191.api.model.User;
 import com.teamsierra.csc191.api.repository.AppointmentTypeRepository;
 import com.teamsierra.csc191.api.repository.StylistAvailabilityRepository;
 import com.teamsierra.csc191.api.repository.UserRepository;
 import com.teamsierra.csc191.api.resources.ResourceHandler;
 import com.teamsierra.csc191.api.util.Availability;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.hateoas.Resource;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.MediaType;
 import org.springframework.http.ResponseEntity;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestBody;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import javax.servlet.http.HttpServletRequest;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * User: scott
  * Date: 9/14/13
  * Time: 10:21 AM
  */
 
 @Controller
 @RequestMapping("/users")
 public class UserController extends GenericController
 {
     private static final Log L = LogFactory.getLog(UserController.class);
 
     private UserRepository userRepository;
     private StylistAvailabilityRepository sar;
     private AppointmentTypeRepository apptTypeRepo;
 
     @Autowired
     public UserController(UserRepository userRepo,
     		StylistAvailabilityRepository stylistAvailRepo,
     		AppointmentTypeRepository appointmentTypeRepo)
     {
         userRepository = userRepo;
         sar = stylistAvailRepo;
         apptTypeRepo = appointmentTypeRepo;
     }
 
     /**
      * A method to retrieve all of the users that the current user has access to.
      *
      * Usage: GET call to /users.
      * 	Client - returns their own user object from the db.
      * 	Stylist - returns all active users in the db.
      * 	Admin - returns all users in the db.
      *
      * Input:
      * 	-Headers: authType, authToken
      *
      * Return: List<Resource<User>>. Will throw an exception if this list is empty
      * 	or null.
      *
      * 	format - Json format of the resource with a link to each individual user and,
      * 		in the case of a stylist or admin, a link to their availability as well.
      * 		Fields may have a value of null if they are empty.
      *
      * 	the following is an example of what would be returned if the user was an Admin
      * 	and there were currently two users in the database, one admin and one client:
      * {
      * 	{
      * 		"links":
      * 		[
      * 			{
      * 				"rel": "self",
      * 				"href": ".../users/{userID}"
      * 			},
      * 			{
      * 				"rel": "availability"
      * 				"href": ".../availability/{userID}"
      * 			}
      * 		]
      * 		"id": "{userID}",
      * 		"type": "ADMIN",
      * 		"firstName": "Kyle",
      * 		"lastName": "Matz",
      * 		"email": "kmatz4b@gmail.com",
      * 		"avatarURL": "somePic.gif",
      * 		"phone": "9162223333",
      * 		"active": true
      * 	},
      * 	{
      * 		"links":
      * 		[
      * 			{
      * 				"rel": "self",
      * 				"href": ".../users/{userID}"
      * 			}
      * 		]
      * 		"id": "{userID}",
      * 		"type": "CLIENT",
      * 		"firstName": "John",
      * 		"lastName": "Smith",
      * 		"email": "iFailAtCreativity@yahoo.com",
      * 		"avatarURL": "somePic.gif",
      * 		"phone": "9164445555",
      * 		"active": false
      *	}
      * }
      *
      * @param request requires headers "authType" and "authToken"
      * @return
      * @throws GenericUserException see message for why exception was thrown
      */
     @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
     public ResponseEntity<List<Resource<User>>> getUsers(HttpServletRequest request) throws GenericUserException
     {
         try
         {
             this.setRequestControllerState(request);
         }
         catch(Exception e)
         {
             throw new GenericUserException(e.getMessage(), HttpStatus.BAD_REQUEST);
         }
 
         List<User> users = null;
 
         switch(authType)
         {
             case CLIENT: users = new ArrayList<User>();
                 User user = userRepository.findByToken(authToken);
                 if(user != null)
                 {
                     users.add(user);
                 }
 
                 break;
             case STYLIST: users = userRepository.findAllActive();
                 break;
             case ADMIN: users = userRepository.findAll();
                 break;
             default: throw new GenericUserException("Unknown authType header. "
                     + "Exception generated in call to getUsers().",
                     HttpStatus.BAD_REQUEST);
         }
 
         if(users != null && !users.isEmpty())
         {
             List<Resource<User>> resource = new ArrayList<Resource<User>>();
             for(User u : users)
             {
                 resource.add(ResourceHandler.createResource(u));
             }
 
             return new ResponseEntity<List<Resource<User>>>(resource, HttpStatus.OK);
         }
         else
         {
             throw new GenericUserException("No user(s) found in the database. "
                     + "Exception generated in call to getUsers()",
                     HttpStatus.NOT_FOUND);
         }
     }
 
     /**
      * Adds a new user to the database after validating the fields. Only Admins
      * can create new users and these users must be either Stylists or Admins.
      *
      * NOTE: "active" is assumed to be false if not included.
      *
      * Required fields: type, firstName, lastName, email, password
      * Optional fields: phone, avatarURL, authId, token, active, hairColor,
      * 	hairLength
      * Null fields: id, OauthId, token (should be null, throws an exception if
      * 	it is not)
      *
      * Usage: POST call to /users.
      * 	Client - will throw an exception
      * 	Stylist - will throw an exception
      * 	Admin - no other case where an exception will be thrown.
      *
      * Input:
      * 	-Headers: authType
      * 	-RequestBody: a Json formatted User model with the required fields for
      * 		a user filled out.
      * 		example:
      * 		{
      * 			"type": "ADMIN",
      * 			"firstName": "name",
      * 			"lastName": "lastname",
      * 			"email": "something@somethingElse.com",
      * 			"password": "password"
      * 		}
      *
      * Return: Resource<User>. Will throw an exception if the specified
      * 	fields for the user fail to pass validation.
      *
      * 	format - Json format of the resource with a link to the user and
      * 		a link to their availability as well. Fields may have a value
      * 		of null if they are empty.
      *
      * 	the following is an example of what would be returned:
      * 	stylist/admin:
      * 	{
      * 		"links":
      * 		[
      * 			{
      * 				"rel": "self",
      * 				"href": ".../users/{userID}"
      * 			},
      * 			{
      * 				"rel": "availability"
      * 				"href": ".../availability/{userID}"
      * 			}
      * 		]
      * 		"id": "{userID}",
      * 		"type": "ADMIN",
      * 		"firstName": "Kyle",
      * 		"lastName": "Matz",
      * 		"email": "kmatz4b@gmail.com",
      * 		"avatarURL": "somePic.gif",
      * 		"phone": "9162223333",
      * 		"active": true
      * 	}
      *
      * 	client:
      * 	{
      * 		"links":
      * 		[
      * 			{
      * 				"rel": "self",
      * 				"href": ".../users/{userID}"
      * 			}
      * 		]
      * 		"id": "{userID}",
      * 		"type": "CLIENT",
      * 		"firstName": "John",
      * 		"lastName": "Smith",
      * 		"email": "iFailAtCreativity@yahoo.com",
      * 		"avatarURL": "somePic.gif",
      * 		"phone": "9164445555",
      * 		"active": false
      *	}
      *
      * @param request
      * @param user
      * @return
      * @throws GenericUserException
      */
     @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
     public ResponseEntity<Resource<User>> addUser(HttpServletRequest request,
                                                   @RequestBody User user) throws GenericUserException
     {
         try
         {
             this.setRequestControllerState(request);
         }
         catch(Exception e)
         {
             throw new GenericUserException(e.getMessage(), HttpStatus.BAD_REQUEST);
         }
 
         if(authType == UserType.ADMIN)
         {
             // required fields
             String error = "";
             if(!isValidType(user.getType()))
             {
                 error  += "Invalid user type. Type should be either stylist or admin.\n";
             }
             if(user.getFirstName() == null || !isValidName(user.getFirstName()))
             {
                 error += "Invalid first name. Names should consist of only unicode letters.\n";
             }
             if(user.getLastName() == null || !isValidName(user.getLastName()))
             {
                 error += "Invalid last name. Names should consist of only unicode letters.\n";
             }
             if(user.getEmail() == null || !isValidEmail(user.getEmail()))
             {
                 error += "Invalid email.\n";
             }
             if(user.getPassword() == null || !isValidPassword(user.getPassword()))
             {
                 error += "Invalid password.\n";
             }
 
             //optional fields
             if(user.getAvatarURL() != null)
             {
                 if(!isValidAvatarURL(user.getAvatarURL()))
                 {
                     error += "Invalid avatarURL. The avatarURL should be an image URL ending in .png, .jpg, or .gif.\n";
                 }
             }
             if(user.getPhone() != null)
             {
                 if(!isValidPhoneNumber(user.getPhone()))
                 {
                     error += "Invalid phone number. The phone number should consist of 10 digits.\n";
                 }
             }
 
             //fields that should be null
             if(user.getId() != null)
             {
                 error += "User ID should be null when creating a new user. To update an"
                         + "existing user use \".../users/{userID} PUT\".\n";
             }
             if(user.getOauthId() != null)
             {
                 error += "OauthId should be null as this is not used for stylist"
                         + "or admin users.\n";
             }
             if(user.getToken() != null)
             {
                 error += "Token should be null. This field is set by the interceptor.\n";
             }
 
             if(error.equals(""))
             {
                 user.setPassword(encryptPassword(user.getPassword()));
 
                 userRepository.insert(user);
                 StylistAvailability sa = new StylistAvailability();
                 sa.setStylistID(user.getId());
                 sa.setAvailability(new Availability());
                 sar.insert(sa);
                 
                 //TODO remove if front end gets appointment types working
                 //adds the stylist to all appointment types
                 List<AppointmentType> appointmentTypes = apptTypeRepo.findByCriteria(new AppointmentType());
                 if(appointmentTypes != null && appointmentTypes.size() != 0)
                 {
 	                String userID = user.getId();
 	                try
 	                {
 		                for(AppointmentType at : appointmentTypes)
 		                {
 		                	apptTypeRepo.addStylistToType(at.getId(), userID);
 		                }
 	                }
 	                catch(Exception e)
 	                {
 	                	throw new GenericUserException("Exception generated while adding appointmentTypes"
 	                			+ " to the new user.", HttpStatus.INTERNAL_SERVER_ERROR);
 	                }
                 }
                 // end TODO
 
                 Resource<User> resource = ResourceHandler.createResource(user);
                 return new ResponseEntity<Resource<User>>(resource, HttpStatus.CREATED);
             }
             else
             {
                 throw new GenericUserException(error + "Exception generated in call to addUser().",
                         HttpStatus.BAD_REQUEST);
             }
         }
         else
         {
             throw new GenericUserException("Admin credentials are required to create a new user. "
                     + "Exception generated in call to addUser().",
                     HttpStatus.FORBIDDEN);
         }
     }
 
     /**
      * Retrieves a specific user from the database by their id.
      *
      * Usage: GET call to /users/{userID}.
      * 	Client - will throw an exception if trying to access a different client
      * 		user, however they are free to get active stylists or admins.
      * 	Stylist - will throw an exception if the user's active field is false 
      * 	Admin - no other case where an exception will be thrown.
      *
      * Input:
      * 	-PathVariable: userID
      * 	-Headers: authType, authToken
      *
      * Return: Resource<User>. Will throw an exception if the specified
      * 	user does not exist in the database.
      *
      * 	format - Json format of the resource with a link to the user and,
      * 		in the case of a stylist or admin, a link to their availability as well.
      * 		Fields may have a value of null if they are empty.
      *
      * 	the following is an example of what would be returned:
      * 	stylist/admin:
      * 	{
      * 		"links":
      * 		[
      * 			{
      * 				"rel": "self",
      * 				"href": ".../users/{userID}"
      * 			},
      * 			{
      * 				"rel": "availability"
      * 				"href": ".../availability/{userID}"
      * 			}
      * 		]
      * 		"id": "{userID}",
      * 		"type": "ADMIN",
      * 		"firstName": "Kyle",
      * 		"lastName": "Matz",
      * 		"email": "kmatz4b@gmail.com",
      * 		"avatarURL": "somePic.gif",
      * 		"phone": "9162223333",
      * 		"active": true
      * 	}
      *
      * 	client:
      * 	{
      * 		"links":
      * 		[
      * 			{
      * 				"rel": "self",
      * 				"href": ".../users/{userID}"
      * 			}
      * 		]
      * 		"id": "{userID}",
      * 		"type": "CLIENT",
      * 		"firstName": "John",
      * 		"lastName": "Smith",
      * 		"email": "iFailAtCreativity@yahoo.com",
      * 		"avatarURL": "somePic.gif",
      * 		"phone": "9164445555",
      * 		"active": false
      *	}
      *
      * @param userID
      * @param request
      * @return
      * @throws GenericUserException
      */
     @RequestMapping(value = "/{userID}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
     public ResponseEntity<Resource<User>> getUser(@PathVariable String userID,
                                                   HttpServletRequest request) throws GenericUserException
     {
         try
         {
             this.setRequestControllerState(request);
         }
         catch(Exception e)
         {
             throw new GenericUserException(e.getMessage(), HttpStatus.BAD_REQUEST);
         }
 
         User user;
 
         switch(authType)
         {
             case CLIENT: user = userRepository.findById(userID);
                 if(user == null)
                 {
                     throw new GenericUserException("Unable to find the requested user in the database.",
                             HttpStatus.NOT_FOUND);
                 }
 
                 if(!user.isActive())
                 {
                     user = null;
                 }
 
                 if(user != null && user.getType() == UserType.CLIENT)
                 {
                     User curUser = userRepository.findByToken(authToken);
 
                     if(curUser == null || !userID.equals(curUser.getId()))
                     {
                         throw new GenericUserException("You do not have the credentials to get the"
                                 + "user specified.", HttpStatus.UNAUTHORIZED);
                     }
                 }
                 break;
             case STYLIST: user = userRepository.findById(userID);
                 if(user == null)
                 {
                     throw new GenericUserException("Unable to find the requested user in the database.",
                             HttpStatus.NOT_FOUND);
                 }
 
                 if(!user.isActive())
                 {
                     user = null;
                 }
                 break;
             case ADMIN: user = userRepository.findById(userID);
                 break;
             default: throw new GenericUserException("Unknown authType header. "
                     + "Exception generated in call to getUser().",
                     HttpStatus.BAD_REQUEST);
         }
 
         if(user != null)
         {
             return new ResponseEntity<Resource<User>>(ResourceHandler.createResource(user), HttpStatus.OK);
         }
         else
         {
             throw new GenericUserException("User not found in the database or is inactive. "
                     + "Exception generated in call to getUser().",
                     HttpStatus.NOT_FOUND);
         }
     }
 
     /**
      * Retrieves the current user from the database by their token.
      *
      * Usage: GET call to /users/me.
      *
      * Input:
      * 	-Headers: authToken
      *
      * Return: Resource<User>. Will throw an exception if the specified
      * 	user does not exist in the database.
      *
      * 	format - Json format of the resource with a link to the user and,
      * 		in the case of a stylist or admin, a link to their availability as well.
      * 		Fields may have a value of null if they are empty.
      *
      * 	the following is an example of what would be returned:
      * 	stylist/admin:
      * 	{
      * 		"links":
      * 		[
      * 			{
      * 				"rel": "self",
      * 				"href": ".../users/{userID}"
      * 			},
      * 			{
      * 				"rel": "availability"
      * 				"href": ".../availability/{userID}"
      * 			}
      * 		]
      * 		"id": "{userID}",
      * 		"type": "ADMIN",
      * 		"firstName": "Kyle",
      * 		"lastName": "Matz",
      * 		"email": "kmatz4b@gmail.com",
      * 		"avatarURL": "somePic.gif",
      * 		"phone": "9162223333",
      * 		"active": true
      * 	}
      *
      * 	client:
      * 	{
      * 		"links":
      * 		[
      * 			{
      * 				"rel": "self",
      * 				"href": ".../users/{userID}"
      * 			}
      * 		]
      * 		"id": "{userID}",
      * 		"type": "CLIENT",
      * 		"firstName": "John",
      * 		"lastName": "Smith",
      * 		"email": "iFailAtCreativity@yahoo.com",
      * 		"avatarURL": "somePic.gif",
      * 		"phone": "9164445555",
      * 		"active": false
      *	}
      *
      * @param request
      * @return
      * @throws GenericUserException
      */
     @RequestMapping(value = "/me/", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
     public ResponseEntity<Resource<User>> getCurrentUser(HttpServletRequest request) throws GenericUserException
     {
         try
         {
             this.setRequestControllerState(request);
         }
         catch(Exception e)
         {
             throw new GenericUserException(e.getMessage(), HttpStatus.BAD_REQUEST);
         }
 
         User user = userRepository.findByToken(authToken);
 
         if(user != null)
         {
             return new ResponseEntity<Resource<User>>(ResourceHandler.createResource(user), HttpStatus.OK);
         }
         else
         {
             throw new GenericUserException("Unable to find you in the database, not"
                     + "really sure how you managed to get this exception.",
                     HttpStatus.NOT_FOUND);
         }
     }
 
     @RequestMapping(value = "/me/", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
     public ResponseEntity<Resource<User>> updateCurrentUser(HttpServletRequest request,
                                                             @RequestBody User user) throws GenericUserException
     {
         try
         {
             this.setRequestControllerState(request);
         }
         catch(Exception e)
         {
             throw new GenericUserException(e.getMessage(), HttpStatus.BAD_REQUEST);
         }
 
         String error = "";
 
         User curUser = userRepository.findByToken(authToken);
 
         if(user != null)
         {
             switch(curUser.getType())
             {
                 case CLIENT: error += updateUser(user, curUser);
                     break;
                 case STYLIST: error += updateStylist(user, curUser);
                     break;
                 case ADMIN: // updateAdmin only allows for the additional option to activate
                     // or deactivate a user, don't want that here.
                     error += updateStylist(user, curUser);
                     break;
             }
 
             if(error.equals(""))
             {
                 userRepository.save(curUser);
                 return new ResponseEntity<Resource<User>>(ResourceHandler.createResource(curUser), HttpStatus.ACCEPTED);
             }
             else
             {
                 throw new GenericUserException(error + "Exception generated in call to updateUser().",
                         HttpStatus.BAD_REQUEST);
             }
         }
         else
         {
             throw new GenericUserException("Unable to find you in the database, not"
                     + "really sure how you managed to get this exception.",
                     HttpStatus.NOT_FOUND);
         }
     }
 
     /**
      * Retrieves all of the active stylists (including admins) from the
      * database.
      *
      * Usage: GET call to /users/me.
      *
      * Input:
      * 	-none
      *
      * Return: List<Resource<User>>. Will throw an exception if the specified
      * 	user does not exist in the database.
      *
      * 	format - Json format of the resource with a link to the user and,
      * 		in the case of a stylist or admin, a link to their availability as well.
      * 		Fields may have a value of null if they are empty.
      *
      * 	the following is an example of what would be returned:
      * {
      * 	{
      * 		"links":
      * 		[
      * 			{
      * 				"rel": "self",
      * 				"href": ".../users/{userID}"
      * 			},
      * 			{
      * 				"rel": "availability"
      * 				"href": ".../availability/{userID}"
      * 			}
      * 		]
      * 		"id": "{userID}",
      * 		"type": "ADMIN",
      * 		"firstName": "Kyle",
      * 		"lastName": "Matz",
      * 		"email": "kmatz4b@gmail.com",
      * 		"avatarURL": "somePic.gif",
      * 		"phone": "9162223333",
      * 		"active": true
      * 	},
      * 	{
      * 		"links":
      * 		[
      * 			{
      * 				"rel": "self",
      * 				"href": ".../users/{userID}"
      * 			}
      * 		]
      * 		"id": "{userID}",
      * 		"type": "CLIENT",
      * 		"firstName": "John",
      * 		"lastName": "Smith",
      * 		"email": "iFailAtCreativity@yahoo.com",
      * 		"avatarURL": "somePic.gif",
      * 		"phone": "9164445555",
      * 		"active": false
      *	}
      * }
      *
      * @param request
      * @return
      * @throws GenericUserException
      */
     @RequestMapping(value = "/stylists", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
     public ResponseEntity<List<Resource<User>>> getStylists() throws GenericUserException
     {
         List<User> stylists = userRepository.findAllActiveByGroup(UserType.STYLIST);
         stylists.addAll(userRepository.findAllActiveByGroup(UserType.ADMIN));
 
         if(stylists != null && !stylists.isEmpty())
         {
             List<Resource<User>> stylistResources = new ArrayList<Resource<User>>();
 
             for(User u : stylists)
             {
                 stylistResources.add(ResourceHandler.createResource(u));
             }
 
             return new ResponseEntity<List<Resource<User>>>(stylistResources,
                     HttpStatus.OK);
         }
         else
         {
             throw new GenericUserException("No stylists found in the database.",
                     HttpStatus.NOT_FOUND);
         }
     }
     
     @RequestMapping(value = "/clients", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
     public ResponseEntity<List<Resource<User>>> getClients() throws GenericUserException
     {
        List<User> clients = userRepository.findAllActiveByGroup(UserType.CLIENT);
 
         if(clients != null && !clients.isEmpty())
         {
             List<Resource<User>> clientResources = new ArrayList<Resource<User>>();
 
             for(User u : clients)
             {
                 clientResources.add(ResourceHandler.createResource(u));
             }
 
             return new ResponseEntity<List<Resource<User>>>(clientResources,
                     HttpStatus.OK);
         }
         else
         {
             throw new GenericUserException("No clients found in the database.",
                     HttpStatus.NOT_FOUND);
         }
     }
     
     @RequestMapping(value = "/teapot/", method = RequestMethod.GET)
     public void findTeapot() throws GenericUserException
     {
     	String teapot = "                       (\n" +
     					"            _           ) )\n" +
     			        "         _,(_)._        ((\n" +
     			        "    ___,(_______).        )\n" +
     			        "  ,'__.   /       \\    /\\_\n" +
     			        " /,' /  |\"\"|       \\  /  /\n" +
     			        "| | |   |__|       |,'  /\n" +
     			        " \\`.|                  /\n" +
     			        "  `. :           :    /\n" +
     			        "    `.            :.,'\n" +
     			        "      `-.________,-'\n";
     	throw new GenericUserException(teapot, HttpStatus.I_AM_A_TEAPOT);
     }
 
     /**
      * Updates a user based on the fields provided. Will only check and validate
      * fields that the specific user is allowed to edit, other fields will be
      * ignored (with no notification).
      *
      * Modifiable fields by user type:
      * 	CLIENT: phone, hairColor, hairLength
      * 	STYLIST: avatarURL, email, firstName, lastName, phone,
      * 		hairColor, hairLength
      *	ADMIN: avatarURL, email, firstName, lastName, phone
      *		hairColor, hairLength
      *
      * including any additional fields will not throw an error, but will have
      * no affect.
      *
      * NOTE: The "active" field should always be included as booleans are assumed
      * 	to be false if not included.
      *
      * Usage: PUT call to /users/{userID}.
      * 	Client - allowed edit their own user, will throw an exception otherwise
      * 	Stylist - allowed to edit their own user, will thrown and exception otherwise
      * 	Admin - can edit any existing user
      *
      * Input:
      * 	-PathVariable: userID
      * 	-Headers: authType, authToken
      * 	-RequestBody: a Json formatted User model which only requires the fields that
      * 		are being updated to be present.
      *
      * 		example of changing first name and phone number:
      * 		{
      * 			"firstName": "name",
      * 			"phone": "1234567890",
      * 			"active": true
      * 		}
      *
      * Return: Resource<User>. Will throw an exception if the specified
      * 	fields for the user fail to pass validation or if the user does not exist.
      *
      * 	format - Json format of the resource with a link to each individual user and,
      * 		in the case of a stylist or admin, a link to their availability as well.
      * 		Fields may have a value of null if they are empty.
      *
      * 	the following is an example of what would be returned:
      * 	stylist/admin:
      * 	{
      * 		"links":
      * 		[
      * 			{
      * 				"rel": "self",
      * 				"href": ".../users/{userID}"
      * 			},
      * 			{
      * 				"rel": "availability"
      * 				"href": ".../availability/{userID}"
      * 			}
      * 		]
      * 		"id": "{userID}",
      * 		"type": "ADMIN",
      * 		"firstName": "Kyle",
      * 		"lastName": "Matz",
      * 		"email": "kmatz4b@gmail.com",
      * 		"avatarURL": "somePic.gif",
      * 		"phone": "9162223333",
      * 		"active": true
      * 	}
      *
      * 	client:
      * 	{
      * 		"links":
      * 		[
      * 			{
      * 				"rel": "self",
      * 				"href": ".../users/{userID}"
      * 			}
      * 		]
      * 		"id": "{userID}",
      * 		"type": "CLIENT",
      * 		"firstName": "John",
      * 		"lastName": "Smith",
      * 		"email": "iFailAtCreativity@yahoo.com",
      * 		"avatarURL": "somePic.gif",
      * 		"phone": "9164445555",
      * 		"active": false
      *	}
      *
      * @param userID
      * @param request
      * @param user
      * @return
      * @throws GenericUserException
      */
     @RequestMapping(value = "/{userID}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
     public ResponseEntity<Resource<User>> updateUser(@PathVariable String userID,
                                                      HttpServletRequest request,
                                                      @RequestBody User user) throws GenericUserException
     {
         try
         {
             this.setRequestControllerState(request);
         }
         catch(Exception e)
         {
             throw new GenericUserException(e.getMessage(), HttpStatus.BAD_REQUEST);
         }
 
         User curUser;
         String error = "";
 
         switch(authType)
         {
             case CLIENT: curUser = userRepository.findByToken(authToken);
 
                 if(curUser == null)
                 {
                     throw new GenericUserException("Unable to find your user credentials in the database.",
                             HttpStatus.FORBIDDEN);
                 }
 
                 if(!curUser.getId().equals(userID))
                 {
                     throw new GenericUserException("A client only has access to their own user from the repository. "
                             + "Exception generated in call to updateUser().",
                             HttpStatus.BAD_REQUEST);
                 }
 
                 error += updateUser(user, curUser);
                 break;
             case STYLIST: curUser = userRepository.findByToken(authToken);
 
                 if(curUser == null)
                 {
                     throw new GenericUserException("Unable to find your user credentials in the database.",
                             HttpStatus.FORBIDDEN);
                 }
 
                 if(!curUser.getId().equals(userID))
                 {
                     throw new GenericUserException("A stylist only has access to their own user from the repository. "
                             + "Exception generated in call to updateUser().",
                             HttpStatus.BAD_REQUEST);
                 }
 
                 error += updateStylist(user, curUser);
                 break;
             case ADMIN: curUser = userRepository.findById(userID);
 
                 if(curUser == null)
                 {
                     throw new GenericUserException("Unable to find requested user in the database.",
                             HttpStatus.NOT_FOUND);
                 }
 
                 error += updateAdmin(user, curUser);
                 break;
             default: throw new GenericUserException("Unknown authType header. "
                     + "Exception generated in call to updateUser().",
                     HttpStatus.BAD_REQUEST);
         }
 
         if(error.equals(""))
         {
             userRepository.save(curUser);
             return new ResponseEntity<Resource<User>>(ResourceHandler.createResource(curUser),
                     HttpStatus.ACCEPTED);
         }
         else
         {
             throw new GenericUserException(error + "Exception generated in call to updateUser().",
                     HttpStatus.BAD_REQUEST);
         }
     }
 
     /**
      * Method to update a stylist user. Only editable fields are checked and
      * changed if valid. When a field fails validation an error message with
      * the error is returned, if no errors the string will be empty ("").
      *
      * @param user the user model with the updated fields
      * @param curUser the user model currently in the db
      * @return
      * @throws GenericUserException
      */
     private String updateUser(User user, User curUser)
     {
         String error = "";
         if(user.getFirstName() != null)
         {
             if(isValidName(user.getFirstName()))
             {
                 curUser.setFirstName(user.getFirstName());
             }
             else
             {
                 error += "Invalid first name. Names should consist of only unicode letters.\n";
             }
         }
         if(user.getLastName() != null)
         {
             if(isValidName(user.getLastName()))
             {
                 curUser.setLastName(user.getLastName());
             }
             else
             {
                 error += "Invalid last name. Names should consist of only unicode letters.\n";
             }
         }
         if(user.getAvatarURL() != null)
         {
             if(isValidAvatarURL(user.getAvatarURL()))
             {
                 curUser.setAvatarURL(user.getAvatarURL());
             }
             else
             {
                 error += "Invalid avatarURL. The avatarURL should be an image URL ending in .png, .jpg, or .gif.\n";
             }
         }
         if(user.getPhone() != null)
         {
             if(isValidPhoneNumber(user.getPhone()))
             {
                 curUser.setPhone(user.getPhone());
             }
             else
             {
                 error += "Invalid phone number. The phone number should consist of 10 digits.\n";
             }
         }
         if(user.getEmail() != null)
         {
             if(isValidEmail(user.getEmail()))
             {
                 curUser.setEmail(user.getEmail());
             }
             else
             {
                 error += "Invalid email.\n";
             }
         }
         if(user.getHairColor() != null)
         {
             curUser.setHairColor(user.getHairColor());
         }
         if(user.getHairLength() != null)
         {
             curUser.setHairLength(user.getHairLength());
         }
 
         return error;
     }
 
     private String updateStylist(User user, User curUser)
     {
         String error = "";
         if(user.getPassword() != null)
         {
             if(isValidPassword(user.getPassword()))
             {
                 curUser.setPassword(encryptPassword(user.getPassword()));
             }
             else
             {
                 error += "Invalid password.\n";
             }
         }
         error += updateUser(user, curUser);
         return error;
     }
 
     private String updateAdmin(User user, User curUser)
     {
         String error = "";
         
         //Admins cannot deactivate themselves
         if(!curUser.getId().equals(id))
         {
         	curUser.setActive(user.isActive());
         }
 
         UserType type = curUser.getType();
         if(type == UserType.ADMIN || type == UserType.STYLIST)
         {
             error += updateStylist(user, curUser);
         }
         else
         {
             error += updateUser(user, curUser);
         }
 
         return error;
     }
 
     /**
      * Validates the user type when an admin creates a user.
      * Returns true if the field is valid, otherwise false.
      *
      * @param type
      * @return
      */
     private boolean isValidType(UserType type)
     {
         if(type == UserType.STYLIST || type == UserType.ADMIN)
         {
             return true;
         }
 
         return false;
     }
 
     /**
      * Validates the name fields (firstName, lastName) of a user.
      * Returns true if the field is valid, otherwise false.
      *
      * @param name
      * @return
      */
     private boolean isValidName(String name)
     {
         return name.matches("^\\p{L}+$"); // one or more of any unicode character
     }
 
     /**
      * Validates the email field of a user.
      * Returns true if the field is valid, otherwise false.
      *
      * @param email
      * @return
      */
     private boolean isValidEmail(String email)
     {
         return email.matches("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
     }
 
     /**
      * Validates the avatarURL field of a user.
      * Returns true if the field is valid, otherwise false.
      *
      * @param avatarURL
      * @return
      */
     private boolean isValidAvatarURL(String avatarURL)
     {
     	if(avatarURL.length() > 0)
     	{
     		return true;
     	}
         return false;//avatarURL.matches("[^\\s]+(\\.(?i)(png|gif|jpg))$"); // one or more characters followed by .png, .gif, or .jpg
     }
 
     /**
      * Validates the phone field of a user.
      * Returns true if the field is valid, otherwise false.
      *
      * @param phoneNumber
      * @return
      */
     private boolean isValidPhoneNumber(String phoneNumber)
     {
         return phoneNumber.matches("^\\d{10}$"); // 10 digits
     }
 
     /**
      * Validates the password field of a user.
      * Returns true if the field is valid, otherwise false.
      *
      * @param password
      * @return
      */
     private boolean isValidPassword(String password)
     {
         if(password.length() > 0)
         {
             return true;
         }
 
         return false;
     }
 
     /**
      * Takes the original password and returns the encrypted
      * version of the password that is to be stored in the db.
      *
      * @param password
      * @return
      */
     private String encryptPassword(String password)
     {
         int N = 16384;
         int r = 8;
         int p = 1;
         return SCryptUtil.scrypt(password, N, r, p);
     }
 }
