 package ceid.netcins.frontend;
 
 import java.io.IOException;
import java.util.HashSet;
 import java.util.Hashtable;
import java.util.List;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import rice.p2p.commonapi.Id;
 import ceid.netcins.CatalogService;
 import ceid.netcins.json.Json;
 import ceid.netcins.user.Friend;
 
 public class GetFriendUIDsHandler extends CatalogFrontendAbstractHandler {
 
 	private static final long serialVersionUID = 5244547683415352126L;
 
 	public GetFriendUIDsHandler(CatalogService catalogService,
 			Hashtable<String, Object> queue) {
 		super(catalogService, queue);
 	}
 
 	@Override
 	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		List<Friend> friends = catalogService.getUser().getFriends();
		HashSet<Id> friendIDs = new HashSet<Id>();
		if (friends != null)
			for (Friend fr : friends)
				friendIDs.add(fr.getUID());

 		response.setContentType("application/json");
 		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().write(Json.toString(friendIDs));
 	}
 }
