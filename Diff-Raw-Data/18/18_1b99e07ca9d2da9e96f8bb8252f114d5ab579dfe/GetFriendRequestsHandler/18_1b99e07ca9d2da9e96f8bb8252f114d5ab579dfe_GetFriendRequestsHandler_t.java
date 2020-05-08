 package ceid.netcins.frontend;
 
 import java.io.IOException;
 import java.util.Hashtable;
import java.util.Set;
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
 		response.setContentType("application/json");
 		response.setStatus(HttpServletResponse.SC_OK);
		Vector<Object> res = new Vector<Object>();
		res.add(RequestStatusSuccessTag);
		Hashtable<Id, FriendRequest> friendRequests = catalogService.getUser().getPendingIncomingFReq();
		Set<Id> friendReqIDs = friendRequests.keySet();
		res.add(friendReqIDs.toArray());
		response.getWriter().write(Json.toString(res.toArray()));
 	}
 }
