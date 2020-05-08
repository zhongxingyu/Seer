 package il.technion.ewolf.server.fetchers;
 
 
 import il.technion.ewolf.WolfPack;
 import il.technion.ewolf.WolfPackLeader;
 import il.technion.ewolf.socialfs.Profile;
 import il.technion.ewolf.socialfs.SocialFS;
 import il.technion.ewolf.socialfs.UserID;
 import il.technion.ewolf.socialfs.UserIDFactory;
 import il.technion.ewolf.socialfs.exception.ProfileNotFoundException;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.google.inject.Inject;
 
 public class WolfpacksFetcher implements JsonDataFetcher {
 	private final SocialFS socialFS;
 	private final WolfPackLeader socialGroupsManager;
 	private final UserIDFactory userIDFactory;
 
 	@Inject
 	public WolfpacksFetcher(SocialFS socialFS, WolfPackLeader socialGroupsManager, UserIDFactory userIDFactory) {
 		this.socialFS = socialFS;
 		this.socialGroupsManager = socialGroupsManager;
 		this.userIDFactory = userIDFactory;
 	}
 
	/*!
	 * The parameters should be a pair of strings:
	 * 	First string:		strUid
	 * 	Second string:	strUid value
 	 */
 	@Override
 	public Object fetchData(String... parameters) throws ProfileNotFoundException {
		if(parameters.length != 2) {
 			return null;
 		}
 
		String strUid = parameters[1];
 		List<WolfPack> wgroups = socialGroupsManager.getAllSocialGroups();
 		List<String> groups = new ArrayList<String>();
 
 		if (strUid.equals("my")) {
 			for (WolfPack w : wgroups) {
 				groups.add(w.getName());
 			}
 		} else {
 			UserID uid = userIDFactory.getFromBase64(strUid);
 			Profile profile;
 			profile = socialFS.findProfile(uid);
 			for (WolfPack w : wgroups) {
 				if (w.getMembers().contains(profile)) {
 					groups.add(w.getName());
 				}
 			}
 		}
 		return groups;
 	}
 }
