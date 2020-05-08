 package wsc;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.rmi.RemoteException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Vector;
 
 import javax.xml.namespace.QName;
 import javax.xml.rpc.ServiceException;
 
 import org.apache.axis.client.Call;
 import org.apache.axis.client.Service;
 import org.apache.log4j.Logger;
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import app.Mediator;
 import app.model.User;
 
 /**
  * WSClient is a client of Auction House Service. WSClient interrogates the
  * web service in order to obtain/update relevant information like: relevant
  * users logged in, connection info when current user logs in/out.
  *
  * Once the user has logged in successfully WSClient's role is to maintain a
  * consistent "data base" of relevant users (users that offer/want the products
  * this user want/offers).
  *
  * @author Andreea HODEA, Liviu CHIRCU; IDP - AuctionHouse 2013
  *
  */
 public class WSClientMediatorImpl implements WSClientMediator {
 
 
 	private Logger logger;
 	private Call call = null;
 	private String url;
 	private Map<String, User> relevantUsers;
 
 	public WSClientMediatorImpl(String url, Mediator med) {
 
 		this.relevantUsers = new HashMap<String, User>();
 		this.url = url;
 	}
 
 	public void initLogger() {
 		logger = Logger.getLogger(WSClientMediatorImpl.class.getName());
 
 		try {
 			URL endpoint = new URL(url);
 			logger.debug("New URL: " + url);
 			Service service = new Service();
 
 			call = (Call)service.createCall();
 			call.setTargetEndpointAddress(endpoint);
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		} catch (ServiceException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public boolean register(String username, String pass, String type,
 			String products) {
 
 		boolean ret = false;
 
 		call.setOperationName(new QName("register"));
 		Object[] params = new Object[]{username, pass, type, products};
 		try {
 			Object r = call.invoke(params);
 			if (r == null) {
 				logger.error("No results");
 			}
 			else {
 				ret = (boolean)r;
 			}
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		}
 
 		logger.info("Register result: " + ret);
 		return ret;
 	}
 
 	public Vector<String> logIn(String username, String password, String type,
 						 String listenIp, int listenPort) {
 
 		boolean ret = false;
 		Vector<String> myProds = null;
 
 		call.setOperationName(new QName("logIn"));
 		
 		try {
 			Object r = call.invoke(new Object[] { username, password, type,
 												  listenIp + ":" + listenPort });
 			if (r == null) {
 				logger.error("No results");
 			}
 			else {
 				ret = (boolean)r;
 				if (ret)
 					/* Fetch relevant users */
 					myProds = getInterestedUsers(username, type, 
 							String.format("%s:%s", listenIp, listenPort));
 			}
 
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		}
 
 		logger.info("Authentication for " + username + ": " + ret);
 		return myProds;
 	}
 	
 	public boolean logOut(String username) {
 		
 		boolean ret = false;
 
 		call.setOperationName(new QName("logOut"));
 		Object[] params = new Object[] {username};
 		try {
 			Object r = call.invoke(params);
 			if (r == null) {
 				logger.error("No results");
 			}
 			else {
 				ret = (boolean)r;
 			}
 
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		}
 
 		logger.info("Logout for " + username + ": " + ret);		
 		return ret;
 	}
 
 	private Vector<String> getProductsFromJSON(JSONObject info) {
 		Vector<String> prods = new Vector<String>();
 		try {
 			JSONArray products = (JSONArray)info.get("products");
 
 			int n = products.length();
 			for (int i = 0; i < n; i++)
 				prods.add((String)products.get(i));
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 		return prods;
 	}
 
 	@SuppressWarnings("unchecked")
 	/* Return products of current user and store relevant users. */
 	private Vector<String> getInterestedUsers(String username, String type,
 			String connInfo) {
 		String result = null;
 		Vector<String> myProducts = new Vector<String>();
 
 		call.setOperationName(new QName("getDB"));
 		
 		try {
 			Object r = call.invoke(new Object[] { username, type, connInfo });
 			if (r == null) {
 				logger.error("No results");
 			}
 			else {
 				result = (String)r;
 			}
 
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		}
 
 		logger.info("Interested users: " + result);
 
 		if (result == null)
 			return null;
 		
 		String self = username;
 		try {
 			JSONObject users = new JSONObject(result);
 			
 			Iterator<String> it = users.keys();
 			
 			while (it.hasNext()) {
 				String name = (String)it.next();
 				JSONObject info = (JSONObject)users.get(name);
 				if (name.equals(self))
 					// it's me, store my products in myProducts
 					myProducts.addAll(getProductsFromJSON(info));
 				else {
 					// it's a user relevant for me, store info of this user
 					String listenIp = (String)info.get("ip");
 					Integer listenPort = (Integer)info.get("port");
 					Vector<String> currProds = getProductsFromJSON(info);
 					relevantUsers.put(name,
 							new User(name, listenIp, listenPort, currProds));
 				}
 			}
 		} catch (JSONException e) {
 			e.printStackTrace();
 		}
 
 		return myProducts;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public void testGetDb() {
 		call.setOperationName(new QName("getDB"));
 		try {
 			Object r = call.invoke(new Object[]{"me", "buyer", "127.0.0.1:60001"});
 			if (r == null) {
 				System.out.println("No results");
 			}
 			else {
 				JSONObject users = new JSONObject((String)r);
 				Iterator<String> it = users.keys();
 				while (it.hasNext()) {
 					String username = (String)it.next();
 					System.out.println("Username: " + username);
 					try {
 						JSONArray products = (JSONArray)users.get(username);
 						int n = products.length();
 						for (int i = 0; i < n; i++)
 							System.out.println(products.get(i));
 					} catch (JSONException e) {
 						e.printStackTrace();
 					}
 				}
 			}
 		} catch (RemoteException e) {
 			e.printStackTrace();
 		} catch (JSONException e1) {
 			e1.printStackTrace();
 		}
 	}
 
 	public boolean handleLogoutEvent(String userName) {
 		return false;
 	}
 
 	public Map<String, User> getRelevantUsers() {
 		return relevantUsers;
 	}
 
 	public Map<String, User> getRelevantUsers(String product) {
 		Map<String, User> someUsers = new HashMap<String, User>();
 		for (Map.Entry<String, User> e : relevantUsers.entrySet())
 			if (e.getValue().contains(product))
 				someUsers.put(e.getKey(), e.getValue());
		return someUsers;
 	}
 
 	@Override
 	public void addRelevant(User user) {
 		relevantUsers.put(user.getName(), user);	
 	}
 
 	@Override
 	public void removeRelevant(String user) {
 		relevantUsers.remove(user);
 	}
 }
