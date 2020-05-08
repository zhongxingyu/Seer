 package controllers;
 
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.swing.JOptionPane;
 
 import com.braintreegateway.BraintreeGateway;
 import com.braintreegateway.CreditCard;
 import com.braintreegateway.Customer;
 import com.braintreegateway.CustomerRequest;
 import com.braintreegateway.Environment;
 import com.braintreegateway.Result;
 import com.braintreegateway.ValidationErrors;
 
 import play.Play;
 import play.i18n.Messages;
 import play.mvc.*;
 import play.data.validation.*;
 import play.data.validation.Error;
 import models.*;
 
 //TODO Refactor some of the code  like verifyCC does that and may make a purchase also
 /**
  * This app is designed to be stand alone or accessed from other websites.<br>
  * It uses the BrainTree payment service via the JAVA API<br>
  * Currently supports:<br>
  * 1) 	Registering Customers <br>
  * 2)   Entering credit card info into the BrainTree vault. <br>
  * 3)   Purchasing subscriptions using vault credit card.
  * <br><br>!!!!!!!!!!!! NO CC INFO IS STORED IN THIS APP !!!!!!!!!!!!!!!<br>
  * This makes PCI compliance very simple.<br><br>
  * Typical use would be for a website to log in a user and use this site to add customers and make purchases via BrainTree.<br>
  * The website passes the username as an html parameter and this app links subscription purchases to the user.<br>
  * If a "username" param is passed, we know a user is logged in. If the user has a customerId then he is a BrainTree customer. <br>
  * Play supports sharing data models enabling this app to access user data from the website,<br>
  * and the website to access subscription data.
  * <br><br>
  * To support stand alone operation this app can create users and uses the built in PLAY user authentication.<br.<br>
  * 
  * TODO:<br>
  * 1)	Transaction history<br>
  * 2)	Multiple credit cards<br>
  * 3)	Switch to MySQL db<br>
  * 
  */
 public class Application extends Controller {
 	/** Only if the framework ID is "prod" will we be live. (set using command line "play id" or "play run --%prod")<br>
 	 *  any other ID we will be using the BrainTree sandbox */
 	//TODO  this logic is backwards   make it right !!!!!!!!
 	//TODO  play id's   null   mem DB fake BT     'test'  mem db real BT    'prod'  MySql real BT
 	public static final boolean PROD_MODE 	= Play.id != "prod"? true : false;
 	//TODO move session vars to db or memcache. Load balancing will not work with session vars
 	/** The following are mostly stored in seesion vars<br>
 	 *  NOTE: do not store sensitive data in session vars<br>
 	 *  the action the user is currently persorming */
 	public static final String ACTION 		= "action";
 	/** buying something*/
 	public static final String BUY 			= "buy";
 	/** we are registering a customer with BrainTree*/
 	public static final String REGISTER 	= "register";
 	/** the db id of the currently selected subscription*/
 	public static final String SUB_ID 		= "subId";
 	/** the type of subscription (identifies the BrainTree plan)*/
 	public static final String SUB_TYPE 	= "subType";
 	/** a description of the currently selected subscription*/
 	public static final String SUB_DESC 	= "subDescr";
 	/** if the user is registered with BrainTree, then he has a customer ID*/
 	public static final String CUSTOMER 	= "customerId";
 	/** renderArgs has the complete User record, the session has the username */
 	public static final String USER 		= "user";
 	/** we just logged in or out */
 	public static final String LOG 			= "log";
 	/** we just logged in or out */
 	public static final String LOG_IN		= "login";
 	/** we just logged in or out */
 	public static final String LOG_OUT		= "logout";
 	/** extra flash info*/
 	public static final String FLASH 		= "flash";
 	/** Copy all fields, even sensitive info, from one user to another<br>
 	 *  Use with discretion.
 	 */
 	private static final boolean COPY_ALL 	= true;
 	
 	/**
 	 * Called to validate if a user is logged in.<br>
 	 * If a username html param was passed in then he is validated and logged in.<br>
 	 * If true rendering hash map.
 	 */
 	@Before
     static void addUser() {
 		User user = null;
     	//check the querystring
     	String qs = Http.Request.current().querystring;
 		if (qs != null){	//check for username 
 			String[] ss = qs.split("=");
 			if (ss.length > 1)
 				if (ss[0].toLowerCase().equals("username")) {
 					qs = ss[1];
 					//see if the user exists
 					user = User.find("byUsername", qs).first();
 					if(user != null){
 						session.clear();
				        session.put(LOG, LOG_OUT);
 						renderArgs.put(USER, user);
 					}
 				} 
 		}
 		
 	
  		String sCust = session.get(CUSTOMER);
         user = connected();
         if(user != null) {
         	if ((user.customerId == null) && sCust != null)
         		user.customerId = sCust;
     		session.put(CUSTOMER, user.customerId);
             session.put(USER, user.userName);
         	renderArgs.put(USER, user);
         } else 
         	if (Http.Request.current().actionMethod.equals("register"))
         		return;
         if (Http.Request.current().path.equals("/application/buysubcription")) {
         	return;
         }
         if (Http.Request.current().path.equals("/subscriptions/list")) {
         	return;
         }
         if(Http.Request.current().action.contains("Application.verifyCC"))
         	return;
         
 				
     	if (qs == null || qs.equals("")) {
     		return;
     	}	
     	//this is a response from BrainTree
     	if (qs.toLowerCase().contains("http_status=200&"))
 			verifyCC(qs,null);
     	if (qs.toLowerCase().contains("sparams=http_status"))
     		return;
     	
 		//it's an existing connected user
     	if (user != null)
     		flash.success("Welcome, " + user.firstName);
     	if (Http.Request.current().actionMethod.equals("buy"))
     		return;
     	if (Http.Request.current().actionMethod.equals("cancelPurchase"))
     		return;
         if(session.get(LOG) != null) 
         	if(session.get(LOG).equals(LOG_OUT)) {
         		session.put(LOG, null);
         		return;
         	}	
     	Subscriptions.index();
     }
 	
     /**
      * If a user is currently logged in, then return that user object from the db,<br>
      * Users get validated at the page level and the session level.<br>
      * @return		Logged in user or null if no user validated.
      */
     static User connected() {
     	User user = null;
     	user = renderArgs.get(USER, User.class);
         if(user != null) {
             return user;
         }
         String username = session.get(USER);
         if(username != null) {
             return User.find("byUsername", username).first();
         } 
         return null;
     }
     
 	/**
 	 * We tried to put a cc into the vault and got a response from BrainTree.<br>
 	 * Now we have to confirm we sent a cc request.<br>
 	 * If we we also were buying a subscription, then buy it if the cc made it into the vault.
 	 * 
 	 * @param sParams		The html querystring
 	 */
 	public static void verifyCC(String sParams, String sToken) {
 		Result<CreditCard> result = null;
 		User user = connected();
 
 		if (sToken == null) {
 			if (PROD_MODE) {
 				if ((sParams != null) && (!sParams.equals(""))) {
 					try {
 						result = BrainTree.gateway.transparentRedirect().confirmCreditCard(sParams);
 					} catch(Exception e){
 						flash.error("BrainTree exception verifying Credit card %s", e.getLocalizedMessage());
 					}	
 				} else {	//this should never happen
 					flash.put(FLASH,"querystring was empty, this is a problem");
 				}
 			} else {
 				//Not live, so Make a fake result
 				result = new Result();
 			}
 			//We got an answer to our confirmation
 			try {
 				//Get the token of the cc in the vault
 				sToken = BrainTree.DisplayResult(result);
 				String[] sParse = sToken.split("\n");
 				if (sParse.length > 0) {
 					sToken = sParse[0];
 					user.token = sToken;
 					user.save();
 				} else {
 					if (result != null)
 						flash.error(result.toString()+"\nSomething went wrong token:\n%s", sToken);
 					else
 						flash.error("Result null\nSomething went wrong token:\n%s", sToken);
 				}
 			} catch (Exception e) {
 				flash.error("Unable to reach " + e.getMessage() + "\nTry again later");
 				render();
 			}
 			//Something went wrong, false will make the webpage display error stuff
 			if (!result.isSuccess()) {
 				sParams = "false";
 				render("@verifyCC",sParams, sToken);
 			}
 			
 			//We have successfully put the card in the vault
 			sParams = "Your credit card is securely stored.";
 			flash.success("Thank you, %s\n ", connected().firstName);
 		}
 		
 		
 		flash.success("");
 		//This makes the web page display good stuff
 		sParams = "true";
 		String sSession = session.get(ACTION);
 		//We were just putting a cc in the vault
 		if ((sSession == null) || !sSession.equals(BUY)) {
 			if (sToken == null)
 				sToken = "No token found";
 			render("@verifyCC",sParams, sToken);
 		}
 		
 		//We had to save a cc on the way to buying a subscription, now we buy the subscription		
 		session.put(ACTION, null);
 		//This shouldn't happen
 		if (session.get(SUB_ID) == null) {
 			flash.error("I don't know what subscription you wanted to buy");
 			render("@verifyCC",false, sToken);
 		}
 		Long subId = Long.parseLong(session.get(SUB_ID));
 		session.put(SUB_ID, null);
  		Subscription sub = Subscription.findById(subId);
 		//if we found the subscription, then buy it
 		if (sub == null) {
 			flash.error("I can't find the subscription you wanted to buy %s",subId);
 			render("@verifyCC",false, sToken);
 		}
 		//tell BrainTree to buy the subscription
 		sSession = BrainTree.BuySubscription(sToken, sub.type);
 		//The response contains true or false for success
 		String[] ss = sSession.split("\n");
 		//and the token used, transaction id, etc...
 		if (ss.length > 0) {
 			sParams = ss[0];
 			for (int i = 1; i < ss.length; i++)
 				sToken += "  "+ss[i];
 		} else 
 			sParams = "false";
 		//let's see the results
 		flash.success("You have just bought a subscription");
 		render("@verifyCC",sParams, sToken);
 	}
 	
     /**
      * If a user is logged in then display the Subscriptions main page (Subscriptions index.html),<br> 
      * otherwise display the application login page (Application index.html)
      */
     /*
 	public static void index() {
     	User user = connected();
         if((user != null) && ((user.customerId == null) || (user.subId == null))) 
             Subscriptions.index();
         index(true);
     }
     */
 	
     public static void index(boolean bConnected) {
         List<Subscription> subscriptions = null;
        	subscriptions = Subscription.all().fetch();
        	Subscription subPlat = subscriptions.get(0);
        	Subscription subGold = subscriptions.get(1);
        	Subscription subSilv = subscriptions.get(2);
        	render(subPlat,subGold,subSilv,bConnected);
     }
 
     /**
      * The user wants to buy a subscription.
      * @param sub	The selected subscription 
      */
     public static void buySubcription(Subscription sub) {
     	//If it's a logged in user go to the BrainTree index page
 		if(connected() != null) 
 			BrainTree.index();
 
 		//
  		Subscriptions.SubEnum type = Subscriptions.subType.plat12;
 		if (sub.descr.contains("Platinum") ) 
 			type = Subscriptions.subType.plat12;
 
 		if (sub.descr.contains("Gold")  ) 
 			type = Subscriptions.subType.gold12;
 
 		if (sub.descr.contains("Silver")  ) 
 			type = Subscriptions.subType.silv12;
 		//we are buying and what we are buying
 		session.put(ACTION, BUY);
 		session.put(SUB_DESC, sub.descr);
 		session.put(SUB_ID, type.ordinal());
 		session.put(SUB_TYPE, type.toString());
     	switch(type) {
     		case silv12:
     			session.put(SUB_TYPE, type.toString());
    				flash.success("Register with our Subscription service before purchasing the Silver Subscription");
    				register(connected());
     			break;
     		case gold12:	
     			session.put(SUB_TYPE, type.toString());
    				flash.success("Register with our Subscription service before purchasing the Gold Subscription");
    				register(connected());
     			break;
     		case plat12:
     			session.put(SUB_TYPE, type.toString());
    				flash.success("Register with our Subscription service before purchasing the Platinum Subscription");
    				register(connected());
     			break;
     		default:
     			flash.error("This never happened to me before");
     			session.put(ACTION, null);
     			session.put(SUB_DESC, null);
     			session.put(SUB_ID, null);
     			session.put(SUB_TYPE, null);
     			index(false);
     			break;
     	}
     }
     
     /**
      * Display the register user page (Application register.html)
      * @param user 	data to fill form in
      */
     public static void register(User user) {
     	User connected = connected();
     	if (connected != null) {
     		if (connected.customerId != null)
         		try {
         			int custId = Integer.parseInt(connected.customerId);
         			session.put(CUSTOMER, connected.customerId);
         			//TODO validate custID with BrainTree
         			if (custId != 0)
         				custId = 0;
         		} catch(Exception e){
         			connected.customerId = null;
         			//we can just continue because this user needs to register
         		}
         		if (connected.customerId != null)
         			if (connected.customerId.equals(session.get(CUSTOMER))) {
         				flash.success("%s , you are already registered with customer ID %s", connected.firstName,connected.customerId);
         				Subscriptions.index();
         			} 
         	
     		user.copy(connected,COPY_ALL);
     		user.id = connected.id;
     	}	
     	if (user.website == null)
     		user.website = "http://";
     	if (user.lang == null)
     		user.lang = "en";
     	session.put(REGISTER, REGISTER);
 		renderArgs.put(USER, user);
     	flash.success("Here is where you register for our payment service");
         render("@register");
     }
     
     /**
      * Called when the register user page posts.<br>
      * If the entered password matches the registered user then display the Subscriptions main page (Subscriptions index.html),<br> 
      * otherwise display the register user page (Application register.html).<br>
      * @param user 				should be the logged in user
      * @param phonenum			phone number entered by user
      */
     public static void saveUser(@Valid User user, String phonenum) {
         validation.required(phonenum);
         if (!isPhoneNumberValid(phonenum))
         	validation.equals(phonenum, "").message("Phone number format incorrect");
         if(validation.hasErrors()) {
         	render("@register", user, phonenum);
         } else
         	user.phone = phonenum;
        
         //all data on the form is valid default registering with BrainTree did not work
         boolean bSuccess = false;
         if (PROD_MODE) {
 	        Result<Customer> result = BrainTree.MakeCustomer(user);
 	        if (result != null)
 	        	bSuccess = result.isSuccess();  
 	        if (bSuccess)
 	        	user.customerId = result.getTarget().getId();
         } else {
         	//just make up an id
         	user.customerId = "666";
         	bSuccess = true;
         }
         
         //try again
 	    if (!bSuccess) 
 	    	render("@register", user, phonenum);
 	    
     	session.put(REGISTER, null);
     	user.setMessages(true);
         session.put(CUSTOMER, user.customerId);
         session.put(USER, user.userName);
         User connected = connected();
         if (connected != null) {
         	connected.copy(user,COPY_ALL);
         	connected.save();
         } else {
         	user.save();
         }
         //we have a new user, or an existing user and they are registered with BrainTree
         flash.success("Welcome, " + user.firstName+"    "+UserMessages.messages.get("0", "  ID: "+session.get(CUSTOMER)));
         Subscriptions.index();
     }
     
     /** 
     * Validate phone number using Java reg ex. 
     * This method checks if the input string is a valid phone number. 
     * @param phoneNumber 	Phone number to validate 
     * @return boolean: 		true if phone number is valid, false otherwise. 
     */
     public static boolean isPhoneNumberValid(String phoneNumber){  
     	boolean isValid = false;  
     	/* Phone Number formats: (nnn)nnn-nnnn; nnnnnnnnnn; nnn-nnn-nnnn 
     	    ^\\(? : May start with an option "(" . 
     	    (\\d{3}): Followed by 3 digits. 
     	    \\)? : May have an optional ")" 
     	    [- ]? : May have an optional "-" after the first 3 digits or after optional ) character. 
     	    (\\d{3}) : Followed by 3 digits. 
     	     [- ]? : May have another optional "-" after numeric digits. 
     	     (\\d{4})$ : ends with four digits. 
     	 
     	         Examples: Matches following <a href="http://mylife.com">phone numbers</a>: 
     	         (123)456-7890, 123-456-7890, 1234567890, (123)-456-7890 
     	 
     	*/  
     	//Initialize reg ex for phone number.   
     	String expression = "^\\(?(\\d{3})\\)?[- ]?(\\d{3})[- ]?(\\d{4})$";  
     	CharSequence inputStr = phoneNumber;  
     	Pattern pattern = Pattern.compile(expression);  
     	Matcher matcher = pattern.matcher(inputStr);  
     	if(matcher.matches()){  
     	isValid = true;  
     	}  
     	return isValid;  
     	}
     
         
     /**
      * Find the first registered user that matches the login attempt.<br>
      * If successful, save user name in session hashmap & display the Subscriptions main page (Subscriptions index.html),<br> 
      * otherwise display the application login page (Application index.html).
      * @param		username		Entered on login page 
      * @param		password		Entered on login page
      */		
     public static void login(String username, String password) {
         User user = User.find("byUsernameAndPassword", username, password).first();
         if(user == null) {
         	user = User.find("byUsername", username).first();
         	if(user == null) 
         		flash.error("Login Failed: user %s  Does not exist",username);
         	else 
         		flash.error("Login Failed: Password incorrect");
         	user = null;
         }
         if(user != null) {
         	session.put(ACTION, 	null);
         	session.put(REGISTER, 	null);
         	session.put(SUB_DESC, 	null);
         	session.put(SUB_ID, 	null);
             
         	session.put(USER, 		user.userName);
             session.put(CUSTOMER, 	user.customerId);
             renderArgs.put(USER, 	user);
             flash.success("Welcome, %s  cust ID: %s subID:", user.firstName, user.customerId);
             session.put(LOG, LOG_IN);
             Subscriptions.index();         
         }
         index(true);
     }
     
     /**
      * Delete the session and go to login page (Application index.html)
      */
     public static void logout() {
         session.clear();
         session.put(LOG, LOG_OUT);
         index(false);
     }
 
 }
