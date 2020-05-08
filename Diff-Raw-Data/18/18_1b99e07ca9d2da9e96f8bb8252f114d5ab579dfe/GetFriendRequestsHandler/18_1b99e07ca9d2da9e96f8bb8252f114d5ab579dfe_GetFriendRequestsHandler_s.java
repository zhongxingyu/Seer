 package ceid.netcins.frontend;
 
 import java.io.IOException;
import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Vector;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import rice.p2p.commonapi.Id;
 import ceid.netcins.CatalogService;
 import ceid.netcins.json.Json;
 import ceid.netcins.user.FriendRequest;
 
 public class GetFriendRequestsHandler extends CatalogFrontendAbstractHandler {
 
 	private static final long serialVersionUID = -7350932922284839640L;
 
 	public GetFriendRequestsHandler(CatalogService catalogService,
 			Hashtable<String, Object> queue) {
 		super(catalogService, queue);
 	}
 
 	@Override
 	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		Vector<FriendRequest> friendRequests = catalogService.getUser().getPendingIncomingFReq();
		HashSet<Id> friendReqIDs = new HashSet<Id>();
		if (friendRequests != null)
			for (FriendRequest fr : friendRequests)
				friendReqIDs.add(fr.getUID());

 		response.setContentType("application/json");
 		response.setStatus(HttpServletResponse.SC_OK);
		response.getWriter().write(Json.toString(friendReqIDs));
 	}
 }
