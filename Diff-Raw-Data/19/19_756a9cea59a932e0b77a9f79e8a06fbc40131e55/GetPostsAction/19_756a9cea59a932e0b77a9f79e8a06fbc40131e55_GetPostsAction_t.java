 package posts;
 
 import java.util.*;
 
 import org.jboss.internal.soa.esb.addressing.helpers.EPRHelper;
 import org.jboss.soa.esb.MarshalException;
 import org.jboss.soa.esb.UnmarshalException;
 import org.jboss.soa.esb.actions.AbstractActionLifecycle;
 import org.jboss.soa.esb.addressing.EPR;
 import org.jboss.soa.esb.addressing.MalformedEPRException;
 import org.jboss.soa.esb.couriers.Courier;
 import org.jboss.soa.esb.couriers.CourierException;
 import org.jboss.soa.esb.couriers.CourierFactory;
 import org.jboss.soa.esb.helpers.ConfigTree;
 import org.jboss.soa.esb.message.Body;
 import org.jboss.soa.esb.message.Message;
 
 public class GetPostsAction extends AbstractActionLifecycle
 {
 
 	protected ConfigTree _config;
 	
 	public GetPostsAction(ConfigTree config) {
 		_config = config;
 	}
 	
 	public Message request(Message message) {
 		 
 		HashMap requestMap = new HashMap();
 		
 		int userId = ((Integer)message.getBody().get("userId")).intValue();
 	    String token = (String)message.getBody().get("token");
 	    long current = ((Long)message.getBody().get("current")).longValue();
 	    long expiration = ((Long)message.getBody().get("expiration")).longValue();
 	    int currentUserId = ((Integer)message.getBody().get("currentUserId")).intValue();
 	    int friend = ((Integer)message.getBody().get("friend")).intValue();
 
 		requestMap.put("getPosts.userId", userId);
 		requestMap.put("getPosts.token", token);
 		requestMap.put("getPosts.current", current);
 		requestMap.put("getPosts.expiration", expiration);
 		requestMap.put("getPosts.currentUserId", currentUserId);
 		requestMap.put("getPosts.friend", friend);
 
 		message.getBody().add(requestMap);
 		
 		System.out.println("\n\n\n\nGetPostsAction request\n\n\n");
 	    
 	    return message;  
 	}
 	
 	public Message response(Message message) {
 		  
 		Map responseMsg = (Map) message.getBody().get(Body.DEFAULT_LOCATION);
 		Iterator it = responseMsg.keySet().iterator();
 		List<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
 		List<String> postsPhotosIds = new ArrayList<String>();
 		List<String> postsUsersIds = new ArrayList<String>();
 		// falhou autenticaçao
 		if(responseMsg.keySet().size()==0){
 			message.getBody().add("");
 		}
 		try{
 			while(it.hasNext()){
 				HashMap<String, Object> post = new HashMap<String, Object>();
				Object fromUserId = responseMsg.get(it.next());
				post.put("fromUserId", fromUserId);
				if (!postsUsersIds.contains(fromUserId.toString()) && !fromUserId.toString().equals("-1"))
					postsUsersIds.add(fromUserId.toString());
 				post.put("id", responseMsg.get(it.next()));
 				Object photoId = responseMsg.get(it.next());
 				post.put("photoId", photoId);
 				if (!postsPhotosIds.contains(photoId.toString()) && !photoId.toString().equals("-1"))
 					postsPhotosIds.add(photoId.toString());
 				post.put("private", responseMsg.get(it.next()));
 				post.put("read", responseMsg.get(it.next()));
 				post.put("text", responseMsg.get(it.next()));
				post.put("toUserId", responseMsg.get(it.next()));
 				list.add(post);
 			}
 		} catch(NoSuchElementException ex){
 			HashMap<String, Object> temp = new HashMap<String, Object>();
 			temp.put("id",0);
 			list.add(temp);
 		}
 		message.getBody().add("posts", list);
		System.out.println("\n\n"+postsUsersIds+"\n\n");
 		message.getBody().add("postsPhotosIds", postsPhotosIds);
 		message.getBody().add("postsUsersIds", postsUsersIds);
 		
 		/*List<String> postsPhotosIds = new ArrayList<String>();
 		it = list.iterator();
 		while(it.hasNext()) {
 			String postPhotoId = (String)((Map)it.next()).get("photoId");
 			if (!postsPhotosIds.contains(postPhotoId) && !postPhotoId.equals("-1"))
 				postsPhotosIds.add(postPhotoId);
 		}
 		message.getBody().add("postsPhotosIds", postsPhotosIds);*/
 		
 		return message;  
 	}
 	
 	public Message xml(Message message) {
 		String xmlEPR;
 		try {
 			xmlEPR = EPRHelper.toXMLString(message.getHeader().getCall().getReplyTo());
 			message.getBody().add("reply", xmlEPR);
 		}
 		catch (MarshalException e) {e.printStackTrace();}
 		return message; 
 	}
 
 	public Message sendToMWS(Message message) {
 		String xml = (String) message.getBody().get("reply");
 		EPR jmsepr;
 		try {
 			jmsepr = (EPR) EPRHelper.fromXMLString(xml);
 			message.getHeader().getCall().setTo(jmsepr);
 			Courier courier;
 			courier = CourierFactory.getCourier(jmsepr);
 			courier.deliver(message);
 		}
 		catch (UnmarshalException e) {e.printStackTrace();}
 		catch (CourierException e) {e.printStackTrace();} 
 		catch (MalformedEPRException e) {e.printStackTrace();}
 		return null;
 	}
 
 }
