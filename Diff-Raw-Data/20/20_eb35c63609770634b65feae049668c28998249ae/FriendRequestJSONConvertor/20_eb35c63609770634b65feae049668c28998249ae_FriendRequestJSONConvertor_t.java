 package ceid.netcins.exo.frontend.json;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.jetty.util.ajax.JSON.Output;
 
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
 public class FriendRequestJSONConvertor extends ContentFieldJSONConvertor {
	private static final String UIDTag = "UID";
	private static final String ScreenameTag = "ScreenName";
 
 	public FriendRequestJSONConvertor() {
 	}
 
 	@Override
 	@SuppressWarnings("rawtypes")
 	public Object fromJSON(Map arg0) {
 		throw new RuntimeException("The thing that shouldn't be!");
 	}
 
 	@Override
 	public void toJSON(Object arg0, Output arg1) {
 		if (arg0 == null) {
 			arg1.add(null);
 			return;
 		}
 		FriendRequest frReq = (FriendRequest)arg0;
 		HashMap<String, Object> ret = new HashMap<String, Object>();
 		ret.put(UIDTag, frReq.getUID());
 		ret.put(ScreenameTag, frReq.getScreenName());
 		arg1.add(ret);
 	}
 }
