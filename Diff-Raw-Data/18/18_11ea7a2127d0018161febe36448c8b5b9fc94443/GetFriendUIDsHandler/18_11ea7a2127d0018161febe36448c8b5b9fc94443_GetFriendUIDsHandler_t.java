 package ceid.netcins.frontend;
 
 import java.io.IOException;
 import java.util.Hashtable;
import java.util.Vector;
 
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
 		response.setContentType("application/json");
 		response.setStatus(HttpServletResponse.SC_OK);
		Vector<Object> res = new Vector<Object>();
		res.add(RequestStatusSuccessTag);
		Hashtable<Id, Friend> friends = catalogService.getUser().getFriends();
		res.add(friends.keySet().toArray());
		response.getWriter().write(Json.toString(res.toArray()));
 	}
 }
