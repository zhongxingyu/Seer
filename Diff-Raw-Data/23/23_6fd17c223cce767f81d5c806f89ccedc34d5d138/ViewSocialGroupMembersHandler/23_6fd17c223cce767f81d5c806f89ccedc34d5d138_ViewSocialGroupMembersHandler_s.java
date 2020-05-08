 package il.technion.ewolf.server;
 
 import il.technion.ewolf.WolfPackLeader;
 import il.technion.ewolf.socialfs.Profile;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import org.apache.http.HttpException;
 import org.apache.http.HttpRequest;
 import org.apache.http.HttpResponse;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.protocol.HttpContext;
 import org.apache.http.protocol.HttpRequestHandler;
 
 import com.google.gson.Gson;
 import com.google.inject.Inject;
 
 public class ViewSocialGroupMembersHandler implements HttpRequestHandler {
 	WolfPackLeader socialGroupsManager = null;
 	
 	private class JsonProfile {
 		@SuppressWarnings("unused")
 		private String name;
 		@SuppressWarnings("unused")
 		private String id;
 		
 		private JsonProfile(String name, String id) {
 			this.name = name;
 			this.id = id;
 		}
 	}
 	@Inject
 	public ViewSocialGroupMembersHandler(WolfPackLeader socialGroupsManager) {
 		this.socialGroupsManager = socialGroupsManager;
 	}
 
 	//XXX req of type GET with "/viewSocialGroupMembers/{GroupName}" URI
 	@Override
 	public void handle(HttpRequest req, HttpResponse res,
 			HttpContext context) throws HttpException, IOException {
 		
 		String reqURI = req.getRequestLine().getUri();
 		String[] splitedURI = reqURI.split("/");
		String socialGroupName = splitedURI[splitedURI.length];
 		List<Profile> profiles = socialGroupsManager.findSocialGroup(socialGroupName).getMembers();
 		
 		Gson gson = new Gson();
 		List<ViewSocialGroupMembersHandler.JsonProfile> lst = new ArrayList<ViewSocialGroupMembersHandler.JsonProfile>();
 		for (Profile profile: profiles) {
 			lst.add(new JsonProfile(profile.getName(), profile.getUserId().toString()));
 		}
 		String json = gson.toJson(lst, lst.getClass());
 		res.setEntity(new StringEntity(json));
 		
 		//TODO move adding headers to response intercepter
 		res.addHeader("Server", "e-WolfNode");
 		res.addHeader("Date",Calendar.getInstance().getTime().toString());
 	}
 
 }
