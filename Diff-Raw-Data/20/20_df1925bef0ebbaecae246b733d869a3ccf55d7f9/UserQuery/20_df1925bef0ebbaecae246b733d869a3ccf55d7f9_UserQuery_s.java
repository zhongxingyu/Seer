 package ch.ethz.inf.systems.ptremp.healthbank.REST.user;
 
 import java.io.IOException;
 import java.net.URLDecoder;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.regex.Pattern;
 
 import javax.servlet.Servlet;
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.annotation.WebServlet;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.codec.binary.StringUtils;
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
 
 /**
  * Servlet implementation class UserQuery
  * In this servlet we implement the functionality to query for user information. 
  * These information manly contain profile information. 
  * This servlet only allows GET requests
  * 
  * @author Patrick Tremp
  */
 @WebServlet(
 		description = "Query for other users and get information about them", 
 		urlPatterns = { 
 				"/UserQuery", 
 				"/userquery", 
 				"/uq"
 		})
 public class UserQuery extends HttpServlet {
 	
 	private static final long serialVersionUID = -6550320453080211602L;
 
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
 	 * Main Constructor
      * @see HttpServlet#HttpServlet()
      */
     public UserQuery() {
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
 	 * The GET request allows the caller to query profile information of different users. There are three possibilities
 	 * on how to call this servlet. One is via an ID of a user, that will return the information about this particular
 	 * user if the ID was found in the DB. A second is via a query string, that will search in the DB for according
 	 * users using different patterns for lower case, upper case and wildcard searches. And the last is via a circleID. This
 	 * last query will return all the users the current user has in one of his circles (only user icon and name is returned).
 	 * 
 	 * For a successful call the following parameters need to be present in the URL:
 	 * Call via ID:
 	 * - credentials: This is a credentials string combining the password and user name in a hashed form for security.
 	 * - session: This is the current session key of the user
 	 * - id: The object id to search a user with
 	 * - (callback: (optional) For JSONP requests, one can add the callback parameter, which will result in a JSONP response from the server)
 	 * 
 	 * Call via query:
 	 * - credentials: This is a credentials string combining the password and user name in a hashed form for security.
 	 * - session: This is the current session key of the user
 	 * - query: A query string to search a user with
 	 * - (callback: (optional) For JSONP requests, one can add the callback parameter, which will result in a JSONP response from the server)
 	 * 
 	 * Query all users in your circles:
 	 * - credentials: This is a credentials string combining the password and user name in a hashed form for security.
 	 * - session: This is the current session key of the user
 	 * - circle: Set this parameter to a value with at least one character to query for all users in your circles
 	 * - (callback: (optional) For JSONP requests, one can add the callback parameter, which will result in a JSONP response from the server)
 	 * 
 	 * The resulting JSON string will have the user information in its 'values' field.
 	 * 
 	 * 
 	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
 	 */
 	@SuppressWarnings("unchecked")
 	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 		response.setContentType("application/json");
 		response.addHeader("Access-Control-Allow-Origin", "*");
 		String errorMessage = "";
 		String result = "";
 		boolean wasError = false;
 		boolean isLoggedIn = true;
 		int foundItemsCount = 0;
 		
 		// check parameters
 		String callback = request.getParameter("callback");
 		String credentials = request.getParameter("credentials");
 		String session = request.getParameter("session");
 		String query = request.getParameter("query");
 		String userId = request.getParameter("id");
 		String circle = request.getParameter("circle");
 		if(credentials==null || credentials.length()<2 || session==null || session.length()<4){
 			errorMessage = "\"Please provide the parameters 'credentials' and 'session' with the request.\"";
 			wasError = true;
 		}
 		if(!wasError){
 			credentials = URLDecoder.decode(credentials, "UTF-8");
 			session = URLDecoder.decode(session, "UTF-8");
 			credentials = StringUtils.newStringUtf8(Base64.decodeBase64(credentials));
 			if(query!=null && query.length()>0){query = URLDecoder.decode(query, "UTF-8");}
 			if(userId!=null && userId.length()>0){userId = URLDecoder.decode(userId, "UTF-8");}
 			if(circle!=null && circle.length()>0){circle = URLDecoder.decode(circle, "UTF-8");}
 			
 			// check DB connection
 			if(connector==null || !connector.isConnected()){
 				manager.reconnect();
 			}
 			
 			// check if user is logged in
 			try {
 				if(manager.isUserLoggedIn(session, credentials)){
 					BasicDBList list = new BasicDBList();
 					DBCursor res;
 					JSONArray resArray = new JSONArray();
 					JSONParser parser = new JSONParser();
 					DBObject obj;
 					JSONObject resObj;
 					if(userId!=null && userId.length()>0){ // search via id
 						boolean found = false;
 						ArrayList<String> circles = manager.getCirclesIAmInOfAUser(manager.getUserID(credentials), userId);
 						if(circles.size()>0){
 							BasicDBList and = new BasicDBList();
 							and.add(new BasicDBObject("userID", userId));
 							and.add(new BasicDBObject("app", "profile"));
 							DBObject queryObject = new BasicDBObject("$and", and);
 							res = (DBCursor) connector.query(MongoDBConnector.RECORDS_COLLECTION_NAME, queryObject);
 							if(res!=null && res.hasNext()){
 								DBObject aRecord = res.next();
 								BasicDBList circlesGarantedAccess = (BasicDBList) aRecord.get("circles");
 								if(circlesGarantedAccess!=null && !circlesGarantedAccess.isEmpty()){
 									found=false;
 									for (int i=0;i<circlesGarantedAccess.size();i++) { 
 										for(int j=0;j<circles.size();j++){
 											if(circlesGarantedAccess.get(i).toString().contains(circles.get(j))){ 
 												found = true; 
 												break;
 											}
 										}
 										if(found){break;}
 									} 
 									if(found){ // we are allowed to see this record
 										resObj = (JSONObject) parser.parse(aRecord.toString());
 										HashMap<String, Object> items = new HashMap<String, Object>();
 										items.put("username", resObj.get("username"));
 										items.put("firstname", resObj.get("firstname"));
 										items.put("companyname", resObj.get("companyname"));
 										items.put("lastname", resObj.get("lastname"));
 										items.put("userId", resObj.get("_id"));
 										Object s = resObj.get("street");
 										if(s!=null){ items.put("street", s.toString());}
 										s = resObj.get("code");
 										if(s!=null){ items.put("code", s.toString());}
 										s = resObj.get("city");
 										if(s!=null){ items.put("city", s.toString());}
 										s = resObj.get("country");
 										if(s!=null){ items.put("country", s.toString());}
 										s = resObj.get("phoneP");
 										if(s!=null){ items.put("privPhone", s.toString());}
 										s = resObj.get("phoneM");
 										if(s!=null){ items.put("mobPhone", s.toString());}
 										s = resObj.get("phoneW");
 										if(s!=null){ items.put("workPhone", s.toString());}
 										s = resObj.get("emailP");
 										if(s!=null){ items.put("privMail", s.toString());}
 										s = resObj.get("emailW");
 										if(s!=null){ items.put("workMail", s.toString());}
 										s = resObj.get("nationality");
 										if(s!=null){ items.put("nationality", s.toString());}
 										s = resObj.get("birthday");
 										if(s!=null){ items.put("birthday", s.toString());}
 										s = resObj.get("spouse");
 										if(s!=null){ items.put("spouse", s.toString());}
 										s = resObj.get("insurance");
 										if(s!=null){ items.put("insurance", s.toString());}
 										s = resObj.get("gender");
 										if(s!=null){ items.put("gender", s.toString());}
 										s = resObj.get("userIcon");
 										if(s!=null){ items.put("userIcon", s.toString());}
 										resArray.add(new JSONObject(items));
 										resObj = new JSONObject();
 										resObj.put("users", resArray);
 										result = resObj.toJSONString();	
 									}
 								} 
 							}
 						}
 						if(result.length()==0){
 							list = new BasicDBList();
 							list.add(new ObjectId(userId));
 							res = (DBCursor) connector.query(MongoDBConnector.USER_COLLECTION_NAME, new BasicDBObject("_id", new BasicDBObject("$in", list)));
 							if(res!=null && res.hasNext()){
 								obj = res.next();
 								resObj = (JSONObject) parser.parse(obj.toString());
 								HashMap<String, Object> items = new HashMap<String, Object>();
 								items.put("username", resObj.get("username"));
 								items.put("firstname", resObj.get("firstname"));
 								items.put("lastname", resObj.get("lastname"));
 								items.put("companyname", resObj.get("companyname"));
 								Object s = resObj.get("userIcon");
 								if(s!=null){ items.put("userIcon", s.toString());}
 								resArray.add(new JSONObject(items));
 								resObj = new JSONObject();
 								resObj.put("users", resArray);
 								result = resObj.toJSONString();	
 							} else {
 								errorMessage = "\"No user found with this id. \"";
 								wasError = true;
 							}
 						}
 						
 					} else if(query!=null && query.length()>0){ // search via query
 						try{
 							ObjectId newId = new ObjectId(query);
 							list.add(newId);
 						} catch (IllegalArgumentException e) {
 							// Just do not add this to the list if it is not an id
 						}
 						query = query.replace("*", "\\w*");
 						Pattern regex = Pattern.compile("\\w*"+query+"\\w*");
 						list.add(regex);
 						regex = Pattern.compile("\\w*"+query.toLowerCase()+"\\w*");
 						list.add(regex);
 						regex = Pattern.compile("\\w*"+query.toUpperCase()+"\\w*");
 						list.add(regex);
 						regex = Pattern.compile("\\w*"+query.replace(query.charAt(0), query.substring(0, 1).toUpperCase().charAt(0))+"\\w*");
 						list.add(regex);
 						DBObject firstname = new BasicDBObject("firstname", new BasicDBObject("$in", list));
 						DBObject companyname = new BasicDBObject("companyname", new BasicDBObject("$in", list));
 						DBObject lastname = new BasicDBObject("lastname", new BasicDBObject("$in", list));
 						DBObject username = new BasicDBObject("username", new BasicDBObject("$in", list));
 						DBObject id = new BasicDBObject("_id", new BasicDBObject("$in", list));
 						BasicDBList or = new BasicDBList();
 						or.add(firstname);
 						or.add(lastname);
 						or.add(companyname);
 						or.add(username);
 						or.add(id);
 						DBObject queryObject = new BasicDBObject("$or", or);
 						res = (DBCursor) connector.query(MongoDBConnector.USER_COLLECTION_NAME, queryObject);
 						if(res!=null && res.size()>0){
 							while(res.hasNext()){
 								obj = res.next();
 								resObj = (JSONObject) parser.parse(obj.toString());
 								HashMap<String, Object> items = new HashMap<String, Object>();
 								items.put("userIcon", resObj.get("userIcon"));
 								items.put("firstname", resObj.get("firstname"));
 								items.put("lastname", resObj.get("lastname"));
 								items.put("companyname", resObj.get("companyname"));
 								items.put("userId", resObj.get("_id"));
 								resArray.add(new JSONObject(items));
 								foundItemsCount++;
 							}
 						} 
 						if(foundItemsCount>0){
 							resObj = new JSONObject();
 							resObj.put("users", resArray);
 							result = resObj.toJSONString();						
 						} else {
 							errorMessage = "\"No user found with these credentials. Did you provide the correct sessionKey and credentials?\"";
 							wasError = true;
 						}
 					} else if(circle!=null && circle.length()>0){ // search users in current users circles
 						list.add(manager.getUserID(credentials));
 						res = (DBCursor) connector.query(MongoDBConnector.CIRCLES_COLLECTION_NAME, new BasicDBObject("userID", new BasicDBObject("$in", list))); // get all ID's first
 						if(res!=null && res.hasNext()){
 							ArrayList<String> userIDs = new ArrayList<String>();
 							DBObject aCircle = null;
 							while(res.hasNext()){
 								aCircle = res.next();
 								BasicDBList usersInCircle = (BasicDBList) aCircle.get("users");
 								if(usersInCircle!=null && !usersInCircle.isEmpty()){
 									for (int i=0;i<usersInCircle.size();i++) { 
 										resObj = (JSONObject) parser.parse(usersInCircle.get(i).toString());
 										Object s = resObj.get("userId");
 										if(s!=null){ 
 											if(!userIDs.contains(s.toString())){
 												userIDs.add(s.toString());
 											}
 										}
 									}
 								}
 							}
 							// now get the information about each user
 							for(int i=0;i<userIDs.size();i++){
 								list = new BasicDBList();
 								list.add(new ObjectId(userIDs.get(i)));
 								res = (DBCursor) connector.query(MongoDBConnector.USER_COLLECTION_NAME, new BasicDBObject("_id", new BasicDBObject("$in", list)));
 								if(res!=null && res.hasNext()){
 									DBObject aUser = res.next();
 									resObj = (JSONObject) parser.parse(aUser.toString());
 									HashMap<String, Object> items = new HashMap<String, Object>();
 									Object s;
 									s = resObj.get("lastname");
 									if(s!=null){ items.put("lastname", s.toString());}
 									s = resObj.get("firstname");
 									if(s!=null){ items.put("firstname", s.toString());}
 									s = resObj.get("userIcon");
 									if(s!=null){ items.put("userIcon", s.toString());}
 									s = resObj.get("_id");
 									if(s!=null){ items.put("userId", s.toString());}
 									resArray.add(new JSONObject(items));
 									foundItemsCount++;
 								}
 							}
 							if(foundItemsCount>0){
 								resObj = new JSONObject();
 								resObj.put("users", resArray);
 								result = resObj.toJSONString();						
 							} else {
 								errorMessage = "\"No user found with these credentials. Did you provide the correct sessionKey and credentials?\"";
 								wasError = true;
 							}
 						}
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
 				response.getOutputStream().println(callback+"( { \"result\": \"success\", \"loggedOut\": \""+!isLoggedIn+"\", \"message\": \"Here are the results.\", \"values\" : "+result+" } );");
 				if(MongoDBConnector.DEBUG){System.out.println("Gave user query answer to user with sessionKey: "+session+"!");}
 			}
 			else {
 				response.getOutputStream().println(callback+"( { \"result\": \"failed\", \"loggedOut\": \""+!isLoggedIn+"\", \"error\": "+errorMessage+" } );");
 				if(MongoDBConnector.DEBUG){System.out.println("There was an error: "+errorMessage+"!");}
 			}
 		} else {
 			if(!wasError) {
 				response.getOutputStream().println("{ \"result\": \"success\", \"loggedOut\": \""+!isLoggedIn+"\", \"message\": \"Here are the results.\", \"values\" : "+result+" }");
 				if(MongoDBConnector.DEBUG){System.out.println("Gave user query answer to user with sessionKey: "+session+"!");}
 			}
 			else {
 				response.getOutputStream().println("{ \"result\": \"failed\", \"loggedOut\": \""+!isLoggedIn+"\", \"error\": "+errorMessage+" }");
 				if(MongoDBConnector.DEBUG){System.out.println("There was an error: "+errorMessage+"!");}
 			}
 		} 
 	}
 
 }
