 package ceid.netcins.exo.frontend.handlers;
 
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Map;
 import java.util.Set;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import ceid.netcins.exo.CatalogService;
 import ceid.netcins.exo.user.FriendRequest;
 
 /**
  * 
  * @author <a href="mailto:loupasak@ceid.upatras.gr">Andreas Loupasakis</a>
  * @author <a href="mailto:ntarmos@cs.uoi.gr">Nikos Ntarmos</a>
  * @author <a href="mailto:peter@ceid.upatras.gr">Peter Triantafillou</a>
  * 
  * "eXO: Decentralized Autonomous Scalable Social Networking"
  * Proc. 5th Biennial Conf. on Innovative Data Systems Research (CIDR),
  * January 9-12, 2011, Asilomar, California, USA.
  * 
  */
 public class GetFriendRequestsHandler extends AbstractHandler {
	private static final String FriendRequestsTag = "eXO::Friend_Requests";
 	private static final long serialVersionUID = -7350932922284839640L;
 
 	public GetFriendRequestsHandler(CatalogService catalogService,
 			Hashtable<String, Hashtable<String, Object>> queue) {
 		super(catalogService, queue);
 	}
 
 	@Override
 	public void doPost(HttpServletRequest request,
 			HttpServletResponse response) throws ServletException {
 		if (prepare(request, response) == RequestState.FINISHED)
 			return;
 		Map<String, FriendRequest[]> ret = new Hashtable<String, FriendRequest[]>();
 		Set<FriendRequest> frReqs = new HashSet<FriendRequest>();
 		if (catalogService.getUser().getPendingIncomingFReq().size() > 0) {
 			frReqs.addAll(catalogService.getUser().getPendingIncomingFReq().values());
 			ret.put(FriendRequestsTag, frReqs.toArray(new FriendRequest[1]));
 		} else
 			ret.put(FriendRequestsTag, new FriendRequest[]{});
 		sendStatus(response, RequestStatus.SUCCESS, ret);
 	}
 }
