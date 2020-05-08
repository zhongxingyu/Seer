 package main;
 import info.AuthInfo;
 import info.PostDetailsInfo;
 
 import java.util.*;
 
 import java.sql.Timestamp;
 
 import javax.jws.WebMethod;
 import javax.jws.WebService;
 import javax.xml.soap.SOAPEnvelope;
 import javax.xml.soap.SOAPException;
 
 import org.jboss.soa.esb.client.ServiceInvoker;
 import org.jboss.soa.esb.couriers.FaultMessageException;
 import org.jboss.soa.esb.listeners.message.MessageDeliverException;
 import org.jboss.soa.esb.message.Body;
 import org.jboss.soa.esb.message.Message;
 import org.jboss.soa.esb.message.format.MessageFactory;
 import org.jboss.soa.esb.services.registry.RegistryException;
 
 import containers.PostsContainer;
 
 @WebService
 public class Methods {
 	
 	@WebMethod
 	public AuthInfo loginUser(String email, String password, long current) {
 		System.setProperty("javax.xml.registry.ConnectionFactoryClass","org.apache.ws.scout.registry.ConnectionFactoryImpl");
 		Message esbMessage = MessageFactory.getInstance().getMessage();
 		
 		esbMessage.getBody().add("email", email);
 		esbMessage.getBody().add("password", password);
 		esbMessage.getBody().add("current", current);
 		
 		try {
 			ServiceInvoker si = new ServiceInvoker("Login_User_Service", "send");
 			Message retMessage = si.deliverSync(esbMessage, 10000L);
 
 			return new AuthInfo(Integer.parseInt((String)retMessage.getBody().get("id")),
 					(String)retMessage.getBody().get("token"), 
 					Long.parseLong((String)retMessage.getBody().get("expiration")));
 		}
 		catch (MessageDeliverException e) {e.printStackTrace();}
 		catch (FaultMessageException e) {e.printStackTrace();}
 		catch (RegistryException e) {e.printStackTrace();}
 		
 		return null;
 	}
 	
 	@WebMethod
 	public AuthInfo createUser(String name, String email, String password, long current) {
 		System.setProperty("javax.xml.registry.ConnectionFactoryClass","org.apache.ws.scout.registry.ConnectionFactoryImpl");
 		Message esbMessage = MessageFactory.getInstance().getMessage();
 		
 		esbMessage.getBody().add("name", name);
 		esbMessage.getBody().add("email", email);
 		esbMessage.getBody().add("password", password);
 		esbMessage.getBody().add("current", current);
 	
 		try {
 			ServiceInvoker si = new ServiceInvoker("Create_User_Service", "send");
 			Message retMessage = si.deliverSync(esbMessage, 10000L);
 			
 			return new AuthInfo(Integer.parseInt((String)retMessage.getBody().get("id")),
 					(String)retMessage.getBody().get("token"), 
 					Long.parseLong((String)retMessage.getBody().get("expiration")));
 		}
 		catch (MessageDeliverException e) {e.printStackTrace();}
 		catch (FaultMessageException e) {e.printStackTrace();}
 		catch (RegistryException e) {e.printStackTrace();}
 		
 		return null;
 	}
 	
 	@WebMethod
 	public PostsContainer getPosts(long current, int currentUserId, int friend,
 			int userId, String token, long expiration) {
 		System.setProperty("javax.xml.registry.ConnectionFactoryClass","org.apache.ws.scout.registry.ConnectionFactoryImpl");
 		Message esbMessage = MessageFactory.getInstance().getMessage();
 		
 		List<HashMap<String, Object>> posts = null;
 		List<PostDetailsInfo> list = new ArrayList<PostDetailsInfo>();
 		try {
 			// Get user posts
 			esbMessage.getBody().add("userId", userId);
 			esbMessage.getBody().add("token", token);
 			esbMessage.getBody().add("expiration", expiration);
 			esbMessage.getBody().add("current", current);
 			esbMessage.getBody().add("currentUserId", currentUserId);
 			esbMessage.getBody().add("friend", friend);
 			
 			ServiceInvoker si = new ServiceInvoker("Get_Posts_Service", "Start");
 			Message retMessage = si.deliverSync(esbMessage, 10000L);
 			
 			posts = (List<HashMap<String, Object>>)retMessage.getBody().get("posts");
 			if(posts.size() == 0){
 				return new PostsContainer(list);
 			}
 			else{
 				// falhou autenticaçao
 				try{
 					if(((Integer)posts.get(0).get("id")).intValue() == 0){
 						list.add(new PostDetailsInfo());
 						return new PostsContainer(list);
 					}
 				}
 				catch(ClassCastException e){}
 			}
 						
 			// Get posts photos
 			/*List<String> photoIds = new ArrayList<String>();
 			Iterator it = posts.iterator();
 			while(it.hasNext()) {
 				String postPhotoId = (String)((Map)it.next()).get("photoId");
 				if (!photoIds.contains(postPhotoId) && !postPhotoId.equals("-1"))
 					photoIds.add(postPhotoId);
 			}
 			
 			esbMessage.getBody().add("userId", userId);
 			esbMessage.getBody().add("token", token);
 			esbMessage.getBody().add("expiration", expiration);
 			esbMessage.getBody().add("current", current);
 			esbMessage.getBody().add("postsPhotosIds", photoIds);
 			
 			si = new ServiceInvoker("Get_Photos_Service", "send");
 			retMessage = si.deliverSync(esbMessage, 10000L);*/
 			HashMap<String, HashMap<String, Object>> photos = (HashMap<String, HashMap<String, Object>>)retMessage.getBody().get("postsPhotos");
 			// falhou autenticaçao
 			if(photos.containsKey("0")){
 				list.add(new PostDetailsInfo());
 				return new PostsContainer(list);
 			}
 			
 			// Get users
 			/*List<String> userIds = new ArrayList<String>();
 			Iterator it = posts.iterator();
 			while(it.hasNext()) {
 				String postUserId = (String)((Map)it.next()).get("fromUserId");
 				if (!userIds.contains(postUserId))
 					userIds.add(postUserId);
 			}
 			
 			esbMessage.getBody().add("userId", userId);
 			esbMessage.getBody().add("token", token);
 			esbMessage.getBody().add("expiration", expiration);
 			esbMessage.getBody().add("current", current);
 			esbMessage.getBody().add("userIds", userIds);
 			
 			si = new ServiceInvoker("Get_Users_Service", "send");
 			retMessage = si.deliverSync(esbMessage, 10000L);*/
 			HashMap<String, HashMap<String, Object>> users = (HashMap<String, HashMap<String, Object>>)retMessage.getBody().get("postsUsers");
 			// falhou autenticaçao
 			if(users.containsKey("0")){
 				list.add(new PostDetailsInfo());
 				return new PostsContainer(list);
 			}
 			
 			// Get user photos
 			List<HashMap<String, Object>> temp = new ArrayList<HashMap<String, Object>>(users.values());
 			List<String> userPhotoIds = new ArrayList<String>();
 			Iterator it = temp.iterator();
 			while(it.hasNext()) {
 				String userPhotoId = (String)((Map)it.next()).get("photoId");
 				if (!userPhotoIds.contains(userPhotoId) && !userPhotoId.equals("-1"))
 					userPhotoIds.add(userPhotoId);
 			}
 			
 			esbMessage.getBody().add("userId", userId);
 			esbMessage.getBody().add("token", token);
 			esbMessage.getBody().add("expiration", expiration);
 			esbMessage.getBody().add("current", current);
 			esbMessage.getBody().add("postsPhotosIds", userPhotoIds);
 			
 			si = new ServiceInvoker("Get_Photos_Service", "send");
 			retMessage = si.deliverSync(esbMessage, 10000L);
 			HashMap<String, HashMap<String, Object>> usersPhotos = (HashMap<String, HashMap<String, Object>>)retMessage.getBody().get("postsPhotos");
 			// falhou autenticaçao
 			if(usersPhotos.containsKey("0")){
 				list.add(new PostDetailsInfo());
 				return new PostsContainer(list);
 			}
 			it = posts.iterator();
 			while(it.hasNext()){
 				HashMap<String,Object> tempPost = (HashMap<String, Object>) it.next();
 				PostDetailsInfo postDetails = new PostDetailsInfo();
 				postDetails.setPostId(Integer.parseInt((String)tempPost.get("id")));
 				postDetails.setPostText((String)tempPost.get("text"));				
 				postDetails.setPostPrivate(Boolean.parseBoolean((String)tempPost.get("private")));
 				int postPhotoId = Integer.parseInt((String)tempPost.get("photoId"));
 				postDetails.setPostPhotoId(postPhotoId);
 				if(postPhotoId!=-1)
 					postDetails.setPostPhotoName((String)photos.get(postPhotoId+"").get("name"));
 				int postUserId = Integer.parseInt((String)tempPost.get("fromUserId"));
 				postDetails.setUserId(postUserId);
				System.out.println(users.get(postUserId+""));
 				postDetails.setUserName((String)users.get(postUserId+"").get("name"));
 				int userPhotoId = Integer.parseInt((String)users.get(postUserId+"").get("photoId"));
 				postDetails.setUserPhotoId(userPhotoId);
 				if(userPhotoId!=-1){
 					postDetails.setUserPhotoName((String)usersPhotos.get(userPhotoId+"").get("name"));
 				}
 				list.add(postDetails);
 			}
 			return new PostsContainer(list);
 			
 		}
 		catch (MessageDeliverException e) {e.printStackTrace();}
 		catch (FaultMessageException e) {e.printStackTrace();}
 		catch (RegistryException e) {e.printStackTrace();}
 		
 		return null;
 	}
 }
