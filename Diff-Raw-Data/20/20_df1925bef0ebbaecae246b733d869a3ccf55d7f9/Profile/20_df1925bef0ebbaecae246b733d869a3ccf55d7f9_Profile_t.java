 package ch.ethz.inf.systems.ptremp.healthbank.REST.user;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URLDecoder;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 
 import javax.servlet.Servlet;
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.codec.binary.StringUtils;
 import org.apache.tomcat.util.http.fileupload.FileItem;
 import org.apache.tomcat.util.http.fileupload.FileUploadException;
 import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
 import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
 import org.bson.types.ObjectId;
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 import org.json.simple.parser.JSONParser;
 import org.json.simple.parser.ParseException;
 
 import ch.ethz.inf.systems.ptremp.healthbank.db.MongoDBConnector;
 import ch.ethz.inf.systems.ptremp.healthbank.exceptions.IllegalQueryException;
 import ch.ethz.inf.systems.ptremp.healthbank.exceptions.NotConnectedException;
 import ch.ethz.inf.systems.ptremp.healthbank.logic.CoreManager;
 
 import com.mongodb.BasicDBList;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 import com.mongodb.gridfs.GridFS;
 import com.mongodb.gridfs.GridFSInputFile;
 
 /**
  * Servlet implementation class Profile
  * In this servlet we implement the functionality to query information about the provided user via
  * a GET request, edit its values via a POST request and delete the account via a DELETE request.
  * The profile contains all information about the user including the user icon. 
  * 
  * @author Patrick Tremp
  */
 @WebServlet(
 		description = "Get and Set information about a user", 
 		urlPatterns = { 
 				"/Profile", 
 				"/profile", 
 				"/User", 
 				"/user"
 		})
 public class Profile extends HttpServlet {
 
 	private static final long serialVersionUID = -2817543681075077536L;
 
 	/**
 	 *  Instance of the {@link MongoDBConnector}, which is responsible for the connection to the DB
 	 */
 	private MongoDBConnector connector; 
 	
 	/**
 	 * Instance of the {@link CoreManager}, which is responsible for some core functionalities used
 	 * in several different servlet.
 	 */
 	private CoreManager manager;
        
     /**
      * Main constructor
      * @see HttpServlet#HttpServlet()
      */
     public Profile() {
         super();
         if(connector == null ) { 
         	connector = MongoDBConnector.getInstance();
         }
 		if(manager == null) {
 			manager = new CoreManager();
 		}
     }
 
 	/**
 	 * This method will initialize the {@link MongoDBConnector} and {@link CoreManager} it they have not yet
 	 * been initialized via constructor. 
 	 * 
 	 * @see Servlet#init(ServletConfig)
 	 */
 	public void init(ServletConfig config) throws ServletException {
 		if(connector == null ) { 
         	connector = MongoDBConnector.getInstance();
         }
 		if(manager == null) {
 			manager = new CoreManager();
 		}
 	}
 
 	/**
 	 * The GET request allows the caller to query all the profile data of the provided user. 
 	 * 
 	 * For a successful call the following parameters need to be present in the URL:
 	 * - credentials: This is a credentials string combining the password and user name in a hashed form for security.
 	 * - session: This is the current session key of the user
 	 * - (callback: (optional) For JSONP requests, one can add the callback parameter, which will result in a JSONP response from the server)
 	 * 
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 * 
 	 * The user data is stored in the 'values.user' field of the returning JSON
 	 */
 	@SuppressWarnings("unchecked")
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		response.setContentType("application/json");
 		response.addHeader("Access-Control-Allow-Origin", "*");
 		String errorMessage = "";
 		String userData = "\"\":\"\"";
 		boolean wasError = false;
 		boolean isLoggedIn = true;
 		
 		// check parameters
 		String callback = request.getParameter("callback");
 		String credentials = request.getParameter("credentials");
 		String session = request.getParameter("session");
 		if(credentials==null || credentials.length()<2 || session==null || session.length()<4){
 			errorMessage = "\"Please provide the parameters 'credentials' and 'session' with the request.\"";
 			wasError = true;
 		}
 		if(!wasError){
 			credentials = URLDecoder.decode(credentials, "UTF-8");
 			session = URLDecoder.decode(session, "UTF-8");
 			credentials = StringUtils.newStringUtf8(Base64.decodeBase64(credentials));
 			
 			// check DB connection
 			if(connector==null || !connector.isConnected()){
 				manager.reconnect();
 			}
 			
 			// check if user is logged in
 			try {
 				if(manager.isUserLoggedIn(session, credentials)){
 					BasicDBList list = new BasicDBList();
 					list.add(credentials.substring(0, credentials.lastIndexOf(':')));
 					DBCursor res = (DBCursor) connector.query(MongoDBConnector.USER_COLLECTION_NAME, new BasicDBObject("username", new BasicDBObject("$in", list)));
 					if(res==null || res.size()==0){
 						errorMessage = "\"No user found with these credentials. Did you provide the correct sessionKey and credentials?\"";
 						wasError = true;
 					}
 					if(!wasError){
 						JSONParser parser = new JSONParser();
 						JSONArray resArray = new JSONArray();
 						while(res.hasNext()){
 							DBObject obj = res.next();
 							JSONObject resObj = (JSONObject) parser.parse(obj.toString());
 							resArray.add(resObj);
 						}
 						
 						/*BasicDBList and = new BasicDBList();
 						list = new BasicDBList();
 						String userid = manager.getUserID(credentials);
 						list.add(userid);
 						and.add(new BasicDBObject("userID", new BasicDBObject("$in", list)));
 						and.add(new BasicDBObject("app", "profile"));
 						DBObject queryObject = new BasicDBObject("$and", and);
 						res = (DBCursor) connector.query(MongoDBConnector.RECORDS_COLLECTION_NAME, queryObject);
 						if(res.hasNext()){
 							BasicDBObject resList = (BasicDBObject) res.next().get("circles"); 
 							if(resList!=null){
 								JSONObject resObj = (JSONObject) parser.parse(resList.toString());
 								resArray.add(resObj);
 							}
 						}*/
 						
 						JSONObject result = new JSONObject();
 						result.put("user", resArray);
 						userData = result.toJSONString();
 						/*
 						 * This is deprecated code:
 						 * 
 							JSONParser parser = new JSONParser();
 							DBObject obj = res.next();
 							JSONObject resObj = (JSONObject) parser.parse(obj.toString());
 							userData = "\"username\" : \""+resObj.get("username")+"\", \"firstname\" : \""+resObj.get("firstname")+"\", \"lastname\" : \""+resObj.get("lastname")+"\"";
 							Object s = resObj.get("street");
 							if(s!=null){ userData += ", \"street\" : \""+s.toString()+"\"";}
 							s = resObj.get("code");
 							if(s!=null){ userData += ", \"code\" : \""+s.toString()+"\"";}
 							s = resObj.get("city");
 							if(s!=null){ userData += ", \"city\" : \""+s.toString()+"\"";}
 							s = resObj.get("country");
 							if(s!=null){ userData += ", \"country\" : \""+s+"\"";}
 							s = resObj.get("phoneP");
 							if(s!=null){ userData += ", \"privPhone\" : \""+s.toString()+"\"";}
 							s = resObj.get("phoneM");
 							if(s!=null){ userData += ", \"mobPhone\" : \""+s.toString()+"\"";}
 							s = resObj.get("phoneW");
 							if(s!=null){ userData += ", \"workPhone\" : \""+s.toString()+"\"";}
 							s = resObj.get("emailP");
 							if(s!=null){ userData += ", \"privMail\" : \""+s.toString()+"\"";}
 							s = resObj.get("emailW");
 							if(s!=null){ userData += ", \"workMail\" : \""+s.toString()+"\"";}
 							s = resObj.get("nationality");
 							if(s!=null){ userData += ", \"nationality\" : \""+s.toString()+"\"";}
 							s = resObj.get("birthday");
 							if(s!=null){ userData += ", \"birthday\" : \""+s.toString()+"\"";}
 							s = resObj.get("height");
 							if(s!=null){ userData += ", \"height\" : \""+s.toString()+"\"";}
 							s = resObj.get("weight");
 							if(s!=null){ userData += ", \"weight\" : \""+s.toString()+"\"";}
 							s = resObj.get("spouse");
 							if(s!=null){ userData += ", \"spouse\" : \""+s.toString()+"\"";}
 							s = resObj.get("insurance");
 							if(s!=null){ userData += ", \"insurance\" : \""+s.toString()+"\"";}
 							s = resObj.get("gender");
 							if(s!=null){ userData += ", \"gender\" : \""+s.toString()+"\"";}
 							s = resObj.get("userIcon");
 							if(s!=null){ userData += ", \"userIcon\" : \""+s.toString()+"\"";}
 							s = resObj.get("type");
 							if(s!=null){ userData += ", \"type\" : \""+s.toString()+"\"";}
 							s = resObj.get("allowResearch");
 							if(s!=null){ userData += ", \"allowResearch\" : \""+s.toString()+"\"";}
 							s = resObj.get("_id");
 							if(s!=null){ userData += ", \"_id\" : \""+s.toString()+"\"";}
 						*/
 					}
 				} else {
 					errorMessage = "\"Either your session timed out or you forgot to send me the session and credentials.\"";
 					wasError = true;
 					isLoggedIn = false;
 				}
 			} catch (IllegalQueryException e) {
 				errorMessage = "\"There was an error. Did you provide the correct username and password? Error: "+e.getMessage()+"\"";
 				wasError = true;
 			} catch (NotConnectedException e) {
 				errorMessage = "\"We lost connection to the DB. Please try again later. Sorry for that.\"";
 				wasError = true;
 			} catch (ParseException e) {
 				errorMessage = "\"There was an error. Did you provide the correct username and password? Error: "+e.getMessage()+"\"";
 				wasError = true;
 			}
 		}
 				
 		
 		if(callback!=null && callback.length()>0){
 			callback = URLDecoder.decode(callback, "UTF-8");
 			if(!wasError) {
 				response.getOutputStream().println(callback+"( { \"result\": \"success\", \"loggedOut\": \""+!isLoggedIn+"\", \"message\": \"Here are the user's data.\", \"values\" : "+userData+" } );");
 				if(MongoDBConnector.DEBUG){System.out.println("Gave profile info to user with sessionKey: "+session+"!");}
 			}
 			else {
 				response.getOutputStream().println(callback+"( { \"result\": \"failed\", \"loggedOut\": \""+!isLoggedIn+"\", \"error\": "+errorMessage+" } );");
 				if(MongoDBConnector.DEBUG){System.out.println("There was an error: "+errorMessage+"!");}
 			}
 		} else {
 			if(!wasError) {
 				response.getOutputStream().println("{ \"result\": \"success\", \"loggedOut\": \""+!isLoggedIn+"\", \"message\": \"Here are the user's data.\", \"values\" : "+userData+" }");
 				if(MongoDBConnector.DEBUG){System.out.println("Gave profile info to user with sessionKey: "+session+"!");}
 			}
 			else {
 				response.getOutputStream().println("{ \"result\": \"failed\", \"loggedOut\": \""+!isLoggedIn+"\", \"error\": "+errorMessage+" }");
 				if(MongoDBConnector.DEBUG){System.out.println("There was an error: "+errorMessage+"!");}
 			}
 		} 
 	}
 
 	/**
 	 * The POST request allows the caller to edit information of the provided user. There are two ways of calling this request.
 	 * Either via a multipart request for updating the user icon, or via a 'normal' request for editing user information. To change
 	 * the password of a user, the caller may use the 'normal' edit request with parameter 'password'
 	 * 
 	 * For a successful call the following parameters need to be present in the URL:
 	 * Edit user info:
 	 * - credentials: This is a credentials string combining the password and user name in a hashed form for security.
 	 * - session: This is the current session key of the user
 	 * - (callback: (optional) For JSONP requests, one can add the callback parameter, which will result in a JSONP response from the server)
 	 * (The following parameter do not need to be present all together. Calls with one, multiple or all the parameters are allowed)
 	 * - gender: The gender of the user "Mr" or "Mrs"
 	 * - firstname: The first name of the user
 	 * - lastname: The surname of the user
	 * - companyname: The name of the company the user represents
	 * - descr: A description of the company the user represents
 	 * - street: The street and number the user is living
 	 * - code: The postal code of the city the user lives in
 	 * - city: The name of the city the user lives in
 	 * - country: The country the user lives in
 	 * - phoneP: The private phone number of the user
 	 * - phoneW: The phone number at work of the user
 	 * - phoneM: The mobile phone number of the user
 	 * - emailP: The private email address of the user
 	 * - emailW: The work email address of the user
 	 * - nationality: The user's nationality
 	 * - spouse: The spouse of the user
 	 * - insurance: The insurance company the user is registered with
 	 * - password: For updating the password, use this parameter
 	 * - birthday:  The date of birth of the user
 	 * - height: The height of the user in cm
 	 * - weight: The weight of the user in kg
 	 * - allowResearch: Wheater or not the user allows research companies to inspect his account ('y' or 'n')
 	 * 
 	 * Multipart call:
 	 * - credentials: This is a credentials string combining the password and user name in a hashed form for security.
 	 * - session: This is the current session key of the user
 	 * - image data of a file upload such as filecontent, filname, content type, etc. 
 	 * 
 	 * 
 	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
 	 * 
 	 * TODO: 
 	 * - refactor to not have to deal with all these parameters at the same time, but directly with JSON strings
 	 */
 	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		response.setContentType("application/json");
 		response.addHeader("Access-Control-Allow-Origin", "*");
 		String errorMessage = "";
 		boolean wasError = false;
 		boolean isLoggedIn = true;
 		String session = "", credentials = "";
 		
 		// check DB connection
 		if(!connector.isConnected()){
 			manager.reconnect();
 		}
 
 		HashMap<Object, Object> data = new HashMap<Object, Object>();
 		// START USERICON
 		if (ServletFileUpload.isMultipartContent(request)) {
 			try {
 				String userIcon = "";
                 //Create GridFS object
 				GridFS fs = new GridFS( connector.getRootDatabase(), MongoDBConnector.USER_COLLECTION_NAME);
 				List<FileItem> items = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);
 		        for (FileItem item : items) {
 		            if (!item.isFormField()) {
 		                // Process form file field (input type="file").
 		                InputStream filecontent = item.getInputStream();
 		                String filename = item.getName();
 		                filename = URLDecoder.decode(filename, "UTF-8");
 
 		                //Save image into database
 		                GridFSInputFile in = fs.createFile( filecontent );
 		                in.setFilename(filename);
 		                in.setContentType(item.getContentType());
 		                in.save();
 		                userIcon = filename;
 		            } else {
 		            	if(item.getFieldName().equals("session")){
 		            		session = item.getString();
 		            	} else if(item.getFieldName().equals("credentials")){
 		            		credentials = item.getString();
 		            	}
 		            }
 		        }
 		        
 		        credentials = URLDecoder.decode(credentials, "UTF-8");
 				credentials = StringUtils.newStringUtf8(Base64.decodeBase64(credentials));
 				session = URLDecoder.decode(session, "UTF-8");
 		        BasicDBList list = new BasicDBList();
 				list.add(credentials.substring(0, credentials.lastIndexOf(':')));
 		        
 				if(!manager.isUserLoggedIn(session, credentials)){
 					errorMessage = "\"Profile doPost: You need to be logged in to use this service\"";
 					wasError = true;
 					isLoggedIn = false;
 				} else {
 			        // OK it seems like we could save the new image successfully. Hence delete the old one to reduce space consumption
 					DBCursor res = (DBCursor) connector.query(MongoDBConnector.USER_COLLECTION_NAME, new BasicDBObject("username", new BasicDBObject("$in", list)));
 					if(res==null || !res.hasNext()){
 						errorMessage = "\"No user found with these credentials. Did you provide the correct sessionKey and credentials?\"";
 						wasError = true;
 					}
 					if(!wasError){
 						JSONParser parser = new JSONParser();
 						DBObject obj = res.next();
 						JSONObject resObj = (JSONObject) parser.parse(obj.toString());
 						if(resObj.keySet().contains("userIcon")){
 							String s = resObj.get("userIcon").toString();
 							if(s!=null){ 
 								fs.remove(s);
 							}
 						}
 					}
 					data.put("userIcon", userIcon);
 					if(!wasError){
 						connector.update(MongoDBConnector.USER_COLLECTION_NAME, new BasicDBObject("username", new BasicDBObject("$in", list)), new BasicDBObject("$set", data));
 
 						// update profile record as well
 						BasicDBList and = new BasicDBList();
 						list = new BasicDBList();
 						String userid = manager.getUserID(credentials);
 						list.add(userid);
 						and.add(new BasicDBObject("userID", new BasicDBObject("$in", list)));
 						and.add(new BasicDBObject("app", "profile"));
 						DBObject queryObject = new BasicDBObject("$and", and);
 						connector.update(MongoDBConnector.RECORDS_COLLECTION_NAME, queryObject, new BasicDBObject("$set", data));
 					}
 				}
 			} catch (FileUploadException | ParseException e) {
 		        errorMessage = "\"Profile doPost: Cannot parse multipart request...\"";
 				wasError = true;
 		    } catch (IllegalQueryException e) {
 		    	errorMessage = "\"Profile doPost: There was an illegal query to the DB...\"";
 				wasError = true;
 			} catch (NotConnectedException e) {
 				errorMessage = "\"Profile doPost: Not connected to the DB...\"";
 				wasError = true;
 			}
 			if(!wasError) {
 				response.getOutputStream().println("{ \"result\": \"success\", \"loggedOut\": \""+!isLoggedIn+"\", \"message\": \"Entry stored successfully\" }");
 				if(MongoDBConnector.DEBUG){System.out.println("Updated user icon of user with sessionKey "+session);}
 			}
 			else {
 				response.getOutputStream().println("{ \"result\": \"failed\", \"loggedOut\": \""+!isLoggedIn+"\", \"error\" : \""+errorMessage+"\" }");
 				if(MongoDBConnector.DEBUG){System.out.println("There was an error: "+errorMessage);}
 			}
 		} // FINISH USERICON
 		else {
			String firstname = "", lastname="", companyname="";
 			
 			// check parameters user specific parameters
 			String callback = request.getParameter("callback");
 			credentials = request.getParameter("credentials");
 			session = request.getParameter("session");
 			if(credentials==null || credentials.length()<2 || session==null || session.length()<4){
 				errorMessage = "\"Profile doPost: Please provide the parameters 'pw' and 'username' with the request.\"";
 				wasError = true;
 			}
 			if(!wasError){
 				credentials = URLDecoder.decode(credentials, "UTF-8");
 				session = URLDecoder.decode(session, "UTF-8");
 				credentials = StringUtils.newStringUtf8(Base64.decodeBase64(credentials));
 				
 				// check profile specific parameters
 				String gender = request.getParameter("gender");
 				firstname = request.getParameter("firstname");
 				lastname = request.getParameter("lastname");
				companyname = request.getParameter("companyname");
				String descr = request.getParameter("descr");
 				String street = request.getParameter("street");
 				String code = request.getParameter("code");
 				String city = request.getParameter("city");
 				String country = request.getParameter("country");
 				String phoneP = request.getParameter("phoneP");
 				String phoneM = request.getParameter("phoneM");
 				String phoneW = request.getParameter("phoneW");
 				String emailP = request.getParameter("emailP");
 				String emailW = request.getParameter("emailW");
 				String nationality = request.getParameter("nationality");
 				String spouse = request.getParameter("spouse");
 				String insurance = request.getParameter("insurance");
 				String password = request.getParameter("password");
 				String birthday = request.getParameter("birthday");
 				String height = request.getParameter("height");
 				String weight = request.getParameter("weight");
 				String allowResearch = request.getParameter("allowResearch");
 				gender = (gender!=null)?URLDecoder.decode(gender, "UTF-8"):null;
 				firstname = (firstname!=null)?URLDecoder.decode(firstname, "UTF-8"):null;
 				lastname = (lastname!=null)?URLDecoder.decode(lastname, "UTF-8"):null;
				companyname = (companyname!=null)?URLDecoder.decode(companyname, "UTF-8"):null;
				descr = (descr!=null)?URLDecoder.decode(descr, "UTF-8"):null;
 				street = (street!=null)?URLDecoder.decode(street, "UTF-8"):null;
 				code = (code!=null)?URLDecoder.decode(code, "UTF-8"):null;
 				city = (city!=null)?URLDecoder.decode(city, "UTF-8"):null;
 				country = (country!=null)?URLDecoder.decode(country, "UTF-8"):null;
 				phoneP = (phoneP!=null)?URLDecoder.decode(phoneP, "UTF-8"):null;
 				phoneM = (phoneM!=null)?URLDecoder.decode(phoneM, "UTF-8"):null;
 				phoneW = (phoneW!=null)?URLDecoder.decode(phoneW, "UTF-8"):null;
 				emailP = (emailP!=null)?URLDecoder.decode(emailP, "UTF-8"):null;
 				emailW = (emailW!=null)?URLDecoder.decode(emailW, "UTF-8"):null;
 				nationality = (nationality!=null)?URLDecoder.decode(nationality, "UTF-8"):null;
 				spouse = (spouse!=null)?URLDecoder.decode(spouse, "UTF-8"):null;
 				insurance = (insurance!=null)?URLDecoder.decode(insurance, "UTF-8"):null;
 				birthday = (birthday!=null)?URLDecoder.decode(birthday, "UTF-8"):null;
 				height = (height!=null)?URLDecoder.decode(height, "UTF-8"):null;
 				weight = (weight!=null)?URLDecoder.decode(weight, "UTF-8"):null;
 				allowResearch = (allowResearch!=null)?URLDecoder.decode(allowResearch, "UTF-8"):null;
 				
 				String md5 = "";
 				if(password!=null){
 					password = URLDecoder.decode(password, "UTF-8");
 					password = StringUtils.newStringUtf8(Base64.decodeBase64(password));
 					password = password.substring(password.indexOf(":")+1);
 					md5 = org.apache.commons.codec.digest.DigestUtils.md5Hex(password);
 				}
 				
 				
 				// check if user is logged in and save the new value
 				try {
 					if(!manager.isUserLoggedIn(session, credentials)){
 						errorMessage = "\"Profile doPost: You need to be logged in to use this service\"";
 						wasError = true;
 						isLoggedIn = false;
 					} else {
 		                BasicDBList list = new BasicDBList();
 		                String username = credentials.substring(0, credentials.lastIndexOf(':'));
 						list.add(username);
 						
 						if(gender!=null && gender.length()>0){data.put("gender", gender);}
 						if(firstname!=null && firstname.length()>0){data.put("firstname", firstname);}
 						if(lastname!=null && lastname.length()>0){data.put("lastname", lastname);}
						if(companyname!=null && companyname.length()>0){data.put("companyname", companyname);}
						if(descr!=null && descr.length()>0){data.put("descr", descr);}
 						if(street!=null && street.length()>0){data.put("street", street);}
 						if(code!=null && code.length()>0){data.put("code", code);}
 						if(city!=null && city.length()>0){data.put("city", city);}
 						if(country!=null && country.length()>0){data.put("country", country);}
 						if(phoneP!=null && phoneP.length()>0){data.put("phoneP", phoneP);}
 						if(phoneM!=null && phoneM.length()>0){data.put("phoneM", phoneM);}
 						if(phoneW!=null && phoneW.length()>0){data.put("phoneW", phoneW);}
 						if(emailP!=null && emailP.length()>0){data.put("emailP", emailP);}
 						if(emailW!=null && emailW.length()>0){data.put("emailW", emailW);}
 						if(nationality!=null && nationality.length()>0){data.put("nationality", nationality);}
 						if(spouse!=null && spouse.length()>0){data.put("spouse", spouse);}
 						if(insurance!=null && insurance.length()>0){data.put("insurance", insurance);}
 						if(password!=null && password.length()>0){data.put("password", md5);}
 						if(birthday!=null && birthday.length()>0){data.put("birthday", birthday);}
 						if(height!=null && height.length()>0){data.put("height", height);}
 						if(weight!=null && weight.length()>0){data.put("weight", weight);}
 						if(allowResearch!=null && allowResearch.length()>0){
 							data.put("allowResearch", allowResearch);
 							if(allowResearch.equals("y")){
 								// Add research circle to user
 								HashMap<Object, Object> researchData = new HashMap<Object, Object>();
 								researchData.put("name", "Research");
 								researchData.put("descr", "All the records you allow researchers to process. This might help to cure a diseases");
 								SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
 								researchData.put("timedate", dateFormat.format(new Date()));
 								researchData.put("userID", manager.getUserID(credentials));
 								researchData.put("color", "#930");
 								connector.insert(MongoDBConnector.CIRCLES_COLLECTION_NAME, researchData);
 							} else if(allowResearch.equals("n")) {
 								// remove research circle from user
 								BasicDBList and = new BasicDBList();
 								and.add(new BasicDBObject("userID", manager.getUserID(credentials)));
 								and.add(new BasicDBObject("name", "Research"));
 								DBObject queryObject = new BasicDBObject("$and", and);
 								
 								// remove circle from all records.
 								DBCursor cir = (DBCursor) connector.query(MongoDBConnector.CIRCLES_COLLECTION_NAME, queryObject);
 								if(cir!=null && cir.hasNext()){
 									DBObject obj = cir.next();
 									ObjectId cirId = (ObjectId) obj.get("_id");
 									DBCursor recs = (DBCursor) connector.query(MongoDBConnector.RECORDS_COLLECTION_NAME, new BasicDBObject("circles", new BasicDBObject("circle", cirId.toString())));
 									if(recs != null && recs.hasNext()){
 										while(recs.hasNext()){
 											obj = recs.next();
 											ObjectId recId = (ObjectId) obj.get("_id");
 											connector.update(MongoDBConnector.RECORDS_COLLECTION_NAME, new BasicDBObject("_id", recId), new BasicDBObject("$pull", new BasicDBObject("circles", new BasicDBObject("circle", cirId))));
 										}
 									}
 								}
 								
 								// finally delete the circle itself								
 								connector.delete(MongoDBConnector.CIRCLES_COLLECTION_NAME, queryObject);
 							}
 						}
 						
 						connector.update(MongoDBConnector.USER_COLLECTION_NAME, new BasicDBObject("username", new BasicDBObject("$in", list)), new BasicDBObject("$set", data));
 						
 						// query for the record that contains the profile, if it does not exist, create it first. else update it
 						
 						DBCursor user = (DBCursor) connector.query(MongoDBConnector.USER_COLLECTION_NAME, new BasicDBObject("username", new BasicDBObject("$in", list)));
 						BasicDBObject myUser = (BasicDBObject) user.next();
 
 						BasicDBList and = new BasicDBList();
 						list = new BasicDBList();
 						String userid = manager.getUserID(credentials);
 						list.add(userid);
 						and.add(new BasicDBObject("userID", new BasicDBObject("$in", list)));
 						and.add(new BasicDBObject("app", "profile"));
 						DBObject queryObject = new BasicDBObject("$and", and);
 						DBCursor res = (DBCursor) connector.query(MongoDBConnector.RECORDS_COLLECTION_NAME, queryObject);
 						DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
 						data.put("timedate", dateFormat.format(new Date())); // time of last update
 						data.put("username", username);
 						if(res==null || !res.hasNext()){ // we need to insert one
 							data.put("userID", userid);
 							data.put("name", "Profile");
 							data.put("descr", "Profile information of user "+userid);
 							data.put("app", "profile");
 							data.remove("allowResearch");
 							String userIcon = (String) myUser.get("userIcon");
 							if(userIcon != null){
 								data.put("userIcon", userIcon);
 							}
 							ObjectId recordid = (ObjectId) connector.insert(MongoDBConnector.RECORDS_COLLECTION_NAME, data);
 							list = new BasicDBList();
 							list.add(username);
 							connector.update(MongoDBConnector.USER_COLLECTION_NAME, new BasicDBObject("username", new BasicDBObject("$in", list)), new BasicDBObject("$set", new BasicDBObject("profileRecordId", recordid.toString())));
 						} else {
 							String userIcon = (String) myUser.get("userIcon");
 							if(userIcon != null){
 								data.put("userIcon", userIcon);
 							}
 							connector.update(MongoDBConnector.RECORDS_COLLECTION_NAME, queryObject, new BasicDBObject("$set", data));
 						}
 					}
 				} catch (NotConnectedException e) {
 					errorMessage = "\"Profile doPost: We lost connection to the DB. Please try again later. Sorry for that.\"";
 					wasError = true;
 				} catch (IllegalQueryException e) {
 					errorMessage = "\"Profile doPost: There was an internal error. Please contact an administrator and provide him/her with this message: "+e.getMessage()+"\"";
 					wasError = true;
 				} catch (IllegalArgumentException e){
 					errorMessage = "\"Profile doPost: There was an internal error. Please contact an administrator and provide him/her with this message: "+e.getMessage()+"\"";
 					wasError = true;
 				}
 			}
 			
 		
 			// Finally give the user feedback
 			if(callback!=null && callback.length()>0){
 				callback = URLDecoder.decode(callback, "UTF-8");
 				if(!wasError) {
 					response.getOutputStream().println(callback+"( { \"result\": \"success\", \"loggedOut\": \""+!isLoggedIn+"\", \"message\": \"Entry stored successfully\" } );");
 					if(MongoDBConnector.DEBUG){System.out.println("Updated user info of user with sessionKey "+session);}
 				}
 				else {
 					response.getOutputStream().println(callback+"( { \"result\": \"failed\", \"loggedOut\": \""+!isLoggedIn+"\", \"error\" : \""+errorMessage+"\" } );");
 					if(MongoDBConnector.DEBUG){System.out.println("There was an error: "+errorMessage);}
 				}
 			} else {
 				if(!wasError) {
 					response.getOutputStream().println("{ \"result\": \"success\", \"loggedOut\": \""+!isLoggedIn+"\", \"message\": \"Entry stored successfully\" }");
 					if(MongoDBConnector.DEBUG){System.out.println("Updated user info of user with sessionKey "+session);}
 				}
 				else {
 					response.getOutputStream().println("{ \"result\": \"failed\", \"loggedOut\": \""+!isLoggedIn+"\", \"error\" : \""+errorMessage+"\" }");
 					if(MongoDBConnector.DEBUG){System.out.println("There was an error: "+errorMessage);}
 				}
 			} 
 		}
 	}
 
 }
