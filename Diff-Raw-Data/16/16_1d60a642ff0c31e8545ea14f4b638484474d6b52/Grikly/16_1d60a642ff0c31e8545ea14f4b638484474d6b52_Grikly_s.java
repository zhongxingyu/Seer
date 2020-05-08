 package com.grikly;
 
 import java.io.File;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.ws.rs.core.MultivaluedMap;
 
 import com.grikly.model.Card;
 import com.grikly.model.Contact;
 import com.grikly.model.LoginModel;
 import com.grikly.model.NewUser;
 import com.grikly.model.SendCard;
 import com.grikly.model.User;
 import com.grikly.request.HttpBuilder;
 import com.grikly.request.Request;
 import com.sun.jersey.core.util.Base64;
 import com.sun.jersey.core.util.MultivaluedMapImpl;
 
 public class Grikly{
 
 	private final String apiKey;
 	private byte[] authInfo;
 	
 	
 	/**
 	 * Grikly Default Constructor
 	 * @author Mario Dennis
 	 * @param apiKey user API Key
 	 */
 	public Grikly (String apiKey)
 	{
 		this.apiKey = apiKey;
 	}//end constructor method
 	
 	
 	
 	/**
 	 * Gets API key.
 	 * @author Mario Dennis
 	 */
 	public String getApiKey ()
 	{
 		return apiKey;
 	}//end getApiKey method
 	
 	
 	/**
 	 * Check if user is authenticated
 	 * @author Mario Dennis
 	 * @return boolean
 	 */
 	public boolean isAuthed ()
 	{
 		if (authInfo == null)
 			return false;
 		else 
 			return true;
 	}//end isAuthed method
 	
 	
 	/**
 	 * Add the credentials of the user to make authenticated 
 	 * requests.
 	 * @author Mario Dennis
 	 * @param email
 	 * @param password
 	 */
 	public void addValidUserCredential (String email,String password)
 	{
 		String credential = email + ":" + password;
 		try 
 		{
 			authInfo = Base64.encode(credential.getBytes("UTF-8"));
 		} 
 		catch (UnsupportedEncodingException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}//end addValidUserCredential method
 	
 	
 	
 	/**
 	 * Fetch User Asynchronously.
 	 * @author Mario Dennis
 	 * @param userId
 	 * @param response
 	 */
 	public void fetchUser (int userId, ResponseListener<User> response)
 	{
 		if (userId <= 0)
 			throw new IllegalArgumentException("User id must be greater than zero");
 		
 		if (response == null)
 			throw new NullPointerException("Null Argument Supplied");
 
 		HttpBuilder<Integer, User> builder = new HttpBuilder<Integer, User>(User.class, getApiKey());
 		
 		builder.setPath(String.format("Users/%d", userId));
 		
 		//add authInfo if supplied 
 		if (isAuthed())
 			builder.setAuthInfo(authInfo);
 		
 		Request<Integer, User> request =  builder.buildHttpGet();
 		
 		GriklyClient<Integer, User> client = new GriklyClient<Integer, User>(request, response);
 		client.execute();
 	}//end fetchUser method
 	
 	
 	/**
 	 * Get User.
 	 * @author Mario Dennis
 	 * @param userId
 	 * @return User
 	 */
 	public User getUser (int userId)
 	{
 		if (userId <= 0)
 			throw new IllegalArgumentException("User id must be greater than zero");
 		
 		HttpBuilder<Integer, User> builder = new HttpBuilder<Integer, User>(User.class, getApiKey());
 		builder.setPath(String.format("Users/%d", userId));
 		
 		//add authInfo if supplied 
 		if (isAuthed())
 			builder.setAuthInfo(authInfo);
 		Request<Integer, User> request = builder.buildHttpGet();
 		
 		return request.execute();
 	}//end getUser method 
 	
 	
 	
 	
 	/**
 	 * Fetch Card Asynchronously.
 	 * @author Mario Dennis
 	 * @param cardId
 	 * @param response
 	 */
 	public void fetchCard (int cardId,ResponseListener<Card> response)
 	{
 		if (cardId <= 0)
 			throw new IllegalArgumentException("User id must be greater than zero");
 		
 		if (response == null)
 			throw new NullPointerException("Null Argument Supplied");
 		
 		HttpBuilder<Integer, Card> builder = new HttpBuilder<Integer, Card>(Card.class, getApiKey());
 		builder.setPath(String.format("Cards/%d", cardId));
 		
 		//add authInfo if supplied 
 		if (isAuthed())
 			builder.setAuthInfo(authInfo);
 		
 		Request<Integer, Card> request = builder.buildHttpGet();
 		GriklyClient<Integer, Card> client = new GriklyClient<Integer, Card>(request, response);
 		client.execute();
 	}//end emailExist method
 	
 	
 	/**
 	 * Get Card
 	 * @author Mario Dennis
 	 * @param cardId
 	 * @return Card
 	 */
 	public Card getCard (int cardId)
 	{
 		if (cardId <= 0)
 			throw new IllegalArgumentException("Card Id must be greater than zero");
 		
 		HttpBuilder<Integer, Card> builder = new HttpBuilder<Integer, Card>(Card.class, getApiKey());
 		builder.setPath(String.format("Cards/%d", cardId));
 		
 		//add authInfo if supplied 
 		if (isAuthed())
 			builder.setAuthInfo(authInfo);
 		
 		Request<Integer, Card> request = builder.buildHttpGet();
 		
 		return request.execute();
 	}//end getCard method
 	
 	
 	
 	/**
 	 * Check if email exists Asynchronously.
 	 * @author Mario Dennis
 	 */
 	public void emailExist (String email, ResponseListener<Boolean> response)
 	{
 		if (email == null || response == null)
 			throw new NullPointerException("Null Argument Supplied");
 		
		MultivaluedMap<String, String> queryMap = new MultivaluedMapImpl();
		queryMap.add("Email", email);
 		HttpBuilder<String, Boolean> builder = new HttpBuilder<String, Boolean>(Boolean.class,getApiKey())
													.setPath("Account/EmailExist")
													.addQueryParam(queryMap);
 		//add authInfo if supplied 
 		if (isAuthed())
 			builder.setAuthInfo(authInfo);
 		
 		 Request<String, Boolean> request = builder.buildHttpGet();
 		GriklyClient<String, Boolean> client = new GriklyClient<String, Boolean>(request, response);
 		client.execute();
 	}//end emailExist method
 	
 	
 	/**
 	 *  Check if email exists 
 	 * @author Mario Dennis
 	 * @return Boolean
 	 */
 	public boolean emailExist (String email)
 	{
 		if (email == null)
 			throw new NullPointerException("Null Argument Supplied");
 		
		MultivaluedMap<String, String> queryMap = new MultivaluedMapImpl();
		queryMap.add("Email", email);
		
 		HttpBuilder<String, Boolean> builder = new HttpBuilder<String, Boolean> (Boolean.class,getApiKey());
		builder.addQueryParam(queryMap);
		builder.setPath("Account/EmailExist");
 		
 		//add authInfo if supplied 
 		if (isAuthed())
 			builder.setAuthInfo(authInfo);
 		
 		Request<String, Boolean> request = builder.buildHttpGet();
 		return request.execute();
 	}//end isEmailExist method
 	
 	
 	
 	/**
 	 * Create Card Asynchronously.
 	 * @param card
 	 * @param response
 	 */
 	public void createCard (Card card,ResponseListener<Card> response)
 	{
 		if (card == null || response == null)
 			throw new NullPointerException("Null Argument Supplied");
 		
 		HttpBuilder<Card, Card> builder = new HttpBuilder<Card, Card>(Card.class, getApiKey());
 		builder.setModel(card);
 		builder.setPath("Cards");
 		
 		//add authInfo if supplied 
 		if (isAuthed());
 			builder.setAuthInfo(authInfo);
 			
 		Request<Card, Card> request = builder.buildHttpPost();
 		GriklyClient<Card, Card> client = new GriklyClient<Card, Card>(request, response);
 		client.execute();
 	}//end createCard method
 	
 	
 	/**
 	 * Create card.This method requires user credentials to 
 	 * authorize action.
 	 * @author Mario Dennis
 	 * @param card
 	 * @return
 	 */
 	public Card createCard (Card card)
 	{
 		if (card == null)
 			throw new NullPointerException("Null Argument Supplied");
 		
 		HttpBuilder<Card, Card> builder = new HttpBuilder<Card, Card>(Card.class, getApiKey());
 		builder.setModel(card);
 		builder.setPath("Cards");
 		
 		//add authInfo if supplied
 		if (isAuthed())
 			builder.setAuthInfo(authInfo);
 		
 		Request<Card, Card> request = builder.buildHttpPost();
 		return request.execute();
 	}//end createCard method
 	
 	
 	
 	/**
 	 * Delete Card Asynchronously.
 	 * @author Mario Dennis
 	 * @param cardId
 	 * @param response
 	 */
 	public void deleteCard (int cardId, ResponseListener<Card> response)
 	{
 		if (cardId <= 0)
 			throw new IllegalArgumentException("Card Id must be greater than 0");
 		
 		if (response == null)
 			throw new NullPointerException("Null Argument Supplied");
 		
 		HttpBuilder<Card, Card> builder  = new HttpBuilder<Card, Card>(Card.class, getApiKey());
 		builder.setPath(String.format("Cards/%d", cardId));
 		
 		//add authInfo if supplied 
 		if (isAuthed())
 			builder.setAuthInfo(authInfo);
 		
 		Request<Card, Card> request = builder.buildHttpDelete();
 		GriklyClient<Card, Card> client = new GriklyClient<Card, Card>(request, response);
 		client.execute();
 	}//end deleteCard method 
 	
 	
 	/**
 	 * Delete Card. This method requires user credentials to 
 	 * authorize action.
 	 * @param cardId
 	 */
 	public Card deleteCard (int cardId)
 	{
 		if (cardId <= 0)
 			throw new IllegalArgumentException("Card Id must be greater than 0");
 		
 		HttpBuilder<Card, Card> builder = new HttpBuilder<Card, Card>(Card.class, getApiKey());
 		builder.setPath(String.format("Cards/%d", cardId));
 		
 		//add authInfo if supplied 
 		if (isAuthed())
 			builder.setAuthInfo(authInfo);
 		
 		Request<Card, Card> request = builder.buildHttpDelete();
 		return request.execute();
 	}//end deleteCard method
 	
 	/**
 	 * Get a valid User by Email and Password Asynchronously..
 	 * @author Mario Dennis
 	 * @param model
 	 * @param response
 	 */
 	public void fetchValidUser (LoginModel model,ResponseListener<User> response)
 	{
 		if (model == null || response == null)
 			throw new NullPointerException("Null Argument Supplied");
 		
 		HttpBuilder<LoginModel, User> builder = new HttpBuilder<LoginModel, User>(User.class, getApiKey());
 		builder.setModel(model);
 		builder.setPath("Account/Login");
 		
 		//add authInfo if supplied 
 		if (isAuthed())
 			builder.setAuthInfo(authInfo);
 		
 		Request<LoginModel, User> request = builder.buildHttpPost();
 		GriklyClient<LoginModel, User> client = new GriklyClient<LoginModel, User>(request, response);
 		client.execute();
 	}//end fetchValidUser method
 	
 	
 	/**
 	 * Get a valid User by Email and Password ..
 	 * @author Mario Dennis
 	 * @param model
 	 * @param response
 	 */
 	public User getValidUser (LoginModel model)
 	{
 		if (model == null)
 			throw new NullPointerException("Null Argument Supplied");
 		
 		HttpBuilder<LoginModel, User> builder = new HttpBuilder<LoginModel, User>(User.class, getApiKey());
 		builder.setModel(model);
 		builder.setPath("Account/Login");
 		
 		//add authInfo if supplied 
 		if (isAuthed())
 			builder.setAuthInfo(authInfo);
 		
 		Request<LoginModel, User> request = builder.buildHttpPost();
 		return request.execute();
 	}//end getValidUser method
 	
 	
 	
 	/**
 	 * Registers new User Asynchronously..
 	 * @param newUser 
 	 * @param response
 	 */
 	public void register (NewUser newUser,ResponseListener<User> response)
 	{
 		if (newUser == null || response == null)
 			throw new NullPointerException("Null Argument Supplied");
 		
 		HttpBuilder<NewUser, User> builder = new HttpBuilder<NewUser, User> (User.class,getApiKey());
 		builder.setModel(newUser);
 		builder.setPath("Account/Register");
 		
 		//add authInfo if supplied
 		if(isAuthed())
 			builder.setAuthInfo(authInfo);
 		
 		
 		Request<NewUser, User> request = builder.buildHttpPost();
 		GriklyClient<NewUser, User> client = new GriklyClient<NewUser, User>(request, response);
 		client.execute();
 	}//end register method
 	
 
 	/**
 	 * Registers new User
 	 * @author Mario Dennis
 	 * @param newUser
 	 * @return User
 	 */
 	public User register (NewUser newUser)
 	{
 		if (newUser == null)
 			throw new NullPointerException("Null Argument Supplied");
 		
 		HttpBuilder<NewUser, User> builder = new HttpBuilder<NewUser, User>(User.class, getApiKey());
 		builder.setModel(newUser);
 		builder.setPath("Account/Register");
 		
 		//add authInfo if supplied
 		if (isAuthed())
 			builder.setAuthInfo(authInfo);
 		
 		Request<NewUser, User> request = builder.buildHttpPost();
 		return request.execute();
 	}//end register method
 	
 	
 	
 	/**
 	 * Update card Asynchronously.
 	 * @author Mario Dennis
 	 * @param card
 	 * @param response
 	 */
 	public void updateCard (Card card,ResponseListener<Card> response)
 	{
 		if (card == null || response == null)
 			throw new NullPointerException("Null Argument Supplied");
 		
 		HttpBuilder<Card, Card> builder = new HttpBuilder<Card, Card>(Card.class, getApiKey());
 		builder.setModel(card);
 		builder.setPath(String.format("Cards/%d", card.getCardId()));
 		
 		//add authInfo if supplied
 		if (isAuthed())
 			builder.setAuthInfo(authInfo);
 		
 		Request<Card, Card> request = builder.buildHttpPut();
 		GriklyClient<Card, Card> client = new GriklyClient<Card, Card>(request, response);
 		client.execute();
 	}//end updateCard method 
 	
 	
 	/**
 	 * Update card Asynchronously.
 	 * @author Mario Dennis
 	 * @param card
 	 * @param response
 	 */
 	public Card updateCard (Card card)
 	{
 		if (card == null)
 			throw new NullPointerException("Null Argument Supplied");
 		
 		HttpBuilder<Card,Card> builder = new HttpBuilder<Card, Card>(Card.class, getApiKey());
 		builder.setModel(card);
 		builder.setPath(String.format("Card/%d", card.getCardId()));
 		
 		//add authInfo if supplied
 		if (isAuthed())
 			builder.setAuthInfo(authInfo);
 		
 		Request<Card, Card> request = builder.buildHttpPut();
 		
 		return request.execute();
 	}//end updateCard method 
 	
 	
 	
 	/**
 	 * Sets a Default card for User
 	 * @author Mario Dennis
 	 * @param userId
 	 * @param cardId
 	 * @param response
 	 */
 	public void  updateUserDefaultCard (int userId,int cardId,ResponseListener<Card> response)
 	{
 		if (userId <= 0 || cardId <= 0)
 			throw new IllegalArgumentException("Id must be greater than zero");
 		
 		if (response == null)
 			throw new NullPointerException("Null ResponseListener instance supplied");
 		StringBuffer url = new StringBuffer("Users/");
 		url.append(userId);
 		url.append("/DefaultCard?cardId=");
 		url.append(cardId);
 		
 		HttpBuilder<Card, Card> builder = new HttpBuilder<Card, Card>(Card.class, getApiKey());
 		builder.setPath(url.toString());
 		
 		if (isAuthed())
 			builder.setAuthInfo(authInfo);
 		
 		Request<Card, Card> request = builder.buildHttpPost();
 		GriklyClient<Card, Card> client = new GriklyClient<Card, Card> (request,response);
 		client.execute();
 	}//end setUserDefaultCard method
 	
 	
 	/**
 	 * Sets a Default card for User
 	 * @author Mario Dennis
 	 * @param userId
 	 * @param cardId
 	 * @param response
 	 */
 	public void updateUserDefaultCard(int userId,int cardId)
 	{
 		if (userId <= 0 || cardId <= 0)
 			throw new IllegalArgumentException("Id must be greater than zero");
 		
 		StringBuffer url = new StringBuffer("Users/");
 		url.append(userId);
 		url.append("/DefaultCard?cardId=");
 		url.append(cardId);
 		
 		HttpBuilder<Card, Card> builder = new HttpBuilder<Card, Card>(Card.class, getApiKey());
 		builder.setPath(url.toString());
 		
 		if (isAuthed())
 			builder.setAuthInfo(authInfo);
 		
 		Request<Card, Card> request = builder.buildHttpPost();
 		request.execute();
 	}//end setUserDefaultCard method
 	
 	
 	
 	/**
 	 * Send Card Asynchronously.
 	 * @author Mario Dennis
 	 * @param cardId
 	 * @param model
 	 * @param response
 	 */
 	public void sendCard (int cardId,SendCard model,ResponseListener<Card> response)
 	{
 		if (cardId <= 0)
 			throw new IllegalArgumentException("Id must be greater than 0");
 		
 		if ( model == null || response == null)
 			throw new NullPointerException("Null Argument Supplied");
 		
 		HttpBuilder<SendCard, Card> builder = new HttpBuilder<SendCard, Card>(Card.class, getApiKey());
 		builder.setModel(model);
 		builder.setPath(String.format("Card/%d/",cardId));
 		
 		//add authInfo if supplied
 		if (isAuthed())
 			builder.setAuthInfo(authInfo);
 		
 		Request<SendCard, Card> request = builder.buildHttpDelete();
 		GriklyClient<SendCard, Card> client = new GriklyClient<SendCard, Card>(request, response);
 		client.execute();
 	}//end sendCard method
 	
 	
 	public List<Card> getContact (String searchString,int page)
 	{
 		if (searchString ==  null)
 			throw new NullPointerException("Null argument supplied");
 		
 		if (page <= 0)
 			throw new IllegalArgumentException("page must be greater than 0");
 		
 		MultivaluedMap<String, String> queryMap = new MultivaluedMapImpl();
 		queryMap.add("searchText", searchString);
 		queryMap.add("page", new Integer(1).toString());
 
 		@SuppressWarnings("unchecked")
 		HttpBuilder<String, ArrayList<Card>> builder = new HttpBuilder<String, ArrayList<Card>>((Class<ArrayList<Card>>) new ArrayList<Card>().getClass(), getApiKey());
 		builder.addQueryParam(queryMap);
 		builder.setPath("Contacts");
 		
 		if (isAuthed())
 			builder.setAuthInfo(authInfo);
 		
 		Request<String, ArrayList<Card>> request = builder.buildHttpGet();
 		return request.execute();
 	}//end getContact method
 	
 	
 	public void getContact (String searchString,int page,ResponseListener<ArrayList<Card>> response)
 	{
 		if (searchString ==  null)
 			throw new NullPointerException("Null argument supplied");
 		
 		if (page <= 0)
 			throw new IllegalArgumentException("page must be greater than 0");
 		
 		MultivaluedMap<String, String> queryMap = new MultivaluedMapImpl();
 		queryMap.add("searchText", searchString);
 		queryMap.add("page", new Integer(1).toString());
 
 		@SuppressWarnings("unchecked")
 		HttpBuilder<String, ArrayList<Card>> builder = new HttpBuilder<String, ArrayList<Card>>((Class<ArrayList<Card>>) new ArrayList<Card>().getClass(), getApiKey());
 		builder.addQueryParam(queryMap);
 		builder.setPath("Contacts");
 		
 		if (isAuthed())
 			builder.setAuthInfo(authInfo);
 		
 		Request<String, ArrayList<Card>> request = builder.buildHttpGet();
 		GriklyClient<String, ArrayList<Card>> client = new GriklyClient<String, ArrayList<Card>>(request, response);
 		client.execute();
 	}//end getContact method
 	
 	
 	
 	
 	/**
 	 * Create Contact Asynchronously.
 	 * @author Mario Dennis
 	 * @param contact
 	 * @return Contact
 	 */
 	public void createContact(Contact contact,ResponseListener<Contact> response)
 	{
 		if (contact == null)
 			throw new NullPointerException("Null argument supplied");
 		
 		HttpBuilder<Contact, Contact> builder = new HttpBuilder<Contact, Contact>(Contact.class, getApiKey()); 
 		builder.setPath("Contacts");
 		builder.setModel(contact);
 		
 		if (isAuthed())
 			builder.setAuthInfo(authInfo);
 		
 		Request<Contact, Contact> request = builder.buildHttpPost();
 		GriklyClient<Contact, Contact> client = new GriklyClient<Contact, Contact>(request, response);
 		client.execute();
 	}//end createContact method 
 	
 	
 	
 	/**
 	 * Create Contact
 	 * @author Mario Dennis
 	 * @param contact
 	 * @return Contact
 	 */
 	public Contact createContact(Contact contact)
 	{
 		if (contact == null || contact == null)
 			throw new NullPointerException("Null argument supplied");
 		
 		HttpBuilder<Contact, Contact> builder = new HttpBuilder<Contact, Contact>(Contact.class, getApiKey()); 
 		builder.setPath("Contacts");
 		builder.setModel(contact);
 		
 		if (isAuthed())
 			builder.setAuthInfo(authInfo);
 		
 		Request<Contact, Contact> request = builder.buildHttpPost();
 		return request.execute();
 	}//end createContact method 
 	
 	
 	/**
 	 * Update Contact Asynchronously.
 	 * @author Mario Dennis
 	 * @param contact
 	 * @return Contact
 	 */
 	public void updateContact (Contact contact,ResponseListener<Contact> response)
 	{
 		if (contact == null)
 			throw new NullPointerException("Null argument supplied");
 		
 		HttpBuilder<Contact, Contact> builder = new HttpBuilder<Contact, Contact>(Contact.class, getApiKey());
 		builder.setModel(contact);
 		builder.setPath(String.format("Contacts/%d", contact.getCardId()));
 		
 		if (isAuthed())
 			builder.setAuthInfo(authInfo);
 		
 		Request<Contact, Contact> request = builder.buildHttpPut();
 		GriklyClient<Contact, Contact> client = new GriklyClient<Contact, Contact>(request, response);
 		client.execute();
 	}//end updateContact method
 	
 	
 	/**
 	 * Update Contact.
 	 * @author Mario Dennis
 	 * @param contact
 	 * @return Contact
 	 */
 	public Contact updateContact (Contact contact)
 	{
 		if (contact == null)
 			throw new NullPointerException("Null argument supplied");
 		
 		HttpBuilder<Contact, Contact> builder = new HttpBuilder<Contact, Contact>(Contact.class, getApiKey());
 		builder.setModel(contact);
 		builder.setPath(String.format("Contacts/%d", contact.getCardId()));
 		
 		if (isAuthed())
 			builder.setAuthInfo(authInfo);
 		
 		Request<Contact, Contact> request = builder.buildHttpPut();
 		return request.execute();
 	}//end updateContact method
 	
 	
 	public int uploadProfileImage (int userId,File file,String contentType)
 	{
 		if (file == null || contentType == null)
 			throw new NullPointerException("Null Argument Supplied");
 		
 		HttpBuilder<String, Integer> builder = new HttpBuilder<String, Integer>(Integer.class, getApiKey());
 		builder.setPath(String.format("Users/%d/ProfileImage", userId));
 		
 		if (isAuthed())
 			builder.setAuthInfo(authInfo);
 		
 		Request<String, Integer> request = builder.buildMultiPartRequest(file, contentType);
 		return request.execute();
 	}//end uploadProfileImage method
 	
 	
 	/**
 	 * 
 	 * @param contactId
 	 * @return
 	 */
 	public Contact deleteContact (int contactId)
 	{
 		if (contactId <= 0)
 			throw new IllegalArgumentException("contactId must be greater that zero");
 		
 		HttpBuilder<Contact, Contact> builder = new HttpBuilder<Contact, Contact>(Contact.class, getApiKey());
 		builder.setPath(String.format("Contacts/%d", contactId));
 		
 		if (isAuthed())
 			builder.setAuthInfo(authInfo);
 		
 		Request<Contact, Contact> request = builder.buildHttpDelete();
 		return request.execute();
 	}//end deleteContact method
 }//end  Grikly class
