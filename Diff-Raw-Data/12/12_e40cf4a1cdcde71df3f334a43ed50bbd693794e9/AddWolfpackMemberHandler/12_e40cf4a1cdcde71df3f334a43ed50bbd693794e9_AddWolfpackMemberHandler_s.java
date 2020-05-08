 package il.technion.ewolf.server.jsonDataHandlers;
 
 import static il.technion.ewolf.server.EWolfResponse.RES_BAD_REQUEST;
 import static il.technion.ewolf.server.EWolfResponse.RES_GENERIC_ERROR;
 import static il.technion.ewolf.server.EWolfResponse.RES_INTERNAL_SERVER_ERROR;
 import static il.technion.ewolf.server.EWolfResponse.RES_NOT_FOUND;
 import static il.technion.ewolf.server.EWolfResponse.RES_SUCCESS;
 import il.technion.ewolf.ewolf.WolfPack;
 import il.technion.ewolf.server.EWolfResponse;
 import il.technion.ewolf.server.cache.ICache;
 import il.technion.ewolf.server.cache.ICacheWithParameter;
 import il.technion.ewolf.socialfs.Profile;
 import il.technion.ewolf.stash.exception.GroupNotFoundException;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonElement;
 import com.google.inject.Inject;
 
 public class AddWolfpackMemberHandler implements IJsonDataHandler {
 
 	private final ICacheWithParameter<Profile, String> profilesCache;
 	private final ICache<Map<String, WolfPack>> wolfpacksCache;
 
 	@Inject
 	public AddWolfpackMemberHandler(ICacheWithParameter<Profile, String> profilesCache,
 			ICache<Map<String, WolfPack>> wolfpacksCache) {
 		this.profilesCache = profilesCache;
 		this.wolfpacksCache = wolfpacksCache;
 	}
 
 	private static class JsonReqAddWolfpackMemberParams {
 		List<String> wolfpackNames;
 		List<String> userIDs;
 	}
 
 	static class AddWolfpackMemberResponse extends EWolfResponse {
 		List<EWolfResponse> wolfpacksResult;
 		List<EWolfResponse> usersResult;
 		public AddWolfpackMemberResponse(String result) {
 			super(result);
 		}
 
 		public AddWolfpackMemberResponse(String result, String errorMessage) {
 			super(result, errorMessage);
 		}
 
 		public AddWolfpackMemberResponse(String result, List<EWolfResponse> wolfpacksResult,
 				List<EWolfResponse> usersResult) {
 			super(result);
 			this.usersResult = usersResult;
 			this.wolfpacksResult = wolfpacksResult;
 		}
 	}
 
 	/**
 	 * @param	jsonReq	serialized object of JsonReqAddWolfpackMemberParams class
 	 * @return	"success" or error message
 	 */
 	@Override
 	public EWolfResponse handleData(JsonElement jsonReq) {
 		Gson gson = new Gson();
 		JsonReqAddWolfpackMemberParams jsonReqParams;
 		try {
 			jsonReqParams = gson.fromJson(jsonReq, JsonReqAddWolfpackMemberParams.class);
 		} catch (Exception e) {
 			e.printStackTrace();
 			return new AddWolfpackMemberResponse(RES_BAD_REQUEST);
 		}
 
 		if (jsonReqParams.wolfpackNames == null || jsonReqParams.wolfpackNames.isEmpty() || 
 				jsonReqParams.userIDs==null || jsonReqParams.userIDs.isEmpty()) {
 			return new AddWolfpackMemberResponse(RES_BAD_REQUEST, 
 					"Must specify both wolfpack name and user ID.");
 		}
 
 		List<EWolfResponse> wolfpacksResult = new ArrayList<EWolfResponse>();
 		List<EWolfResponse> usersResult = new ArrayList<EWolfResponse>();
 
 		List<Profile> profiles = new ArrayList<Profile>();
 		for (String userID : jsonReqParams.userIDs) {
 			if (userID == null) continue;
 
 			Profile p = profilesCache.get(userID);
 			if (p == null) {
 				usersResult.add(new EWolfResponse(RES_NOT_FOUND,
 						"User with the given ID wasn't found."));
 				continue;
 			}
 
 			profiles.add(p);
 			usersResult.add(new EWolfResponse());
 		}
 
		Map<String, WolfPack> wallReadersMap = wolfpacksCache.get();
		WolfPack wallReaders = wallReadersMap.get("wall-readers");
 
 		for (String wolfpackName : jsonReqParams.wolfpackNames) {
			WolfPack socialGroup = wallReadersMap.get(wolfpackName);
 			if (socialGroup == null) {
 				wolfpacksResult.add(new EWolfResponse(RES_NOT_FOUND,
 						"Given wolfpack wasn't found."));
 				continue;
 			}
 			try {
 				for (Profile profile : profiles) {
					socialGroup.addMember(profile);
 					wallReaders.addMember(profile);
 				}
 				wolfpacksResult.add(new EWolfResponse());
 			} catch (GroupNotFoundException e) {
 				e.printStackTrace();
 				wolfpacksResult.add(new EWolfResponse(RES_INTERNAL_SERVER_ERROR));
 			}
 		}
 
 		for (EWolfResponse res : wolfpacksResult) {
 			if (res.getResult() != RES_SUCCESS) {
 				return new AddWolfpackMemberResponse(RES_GENERIC_ERROR, wolfpacksResult, usersResult);
 			}
 		}
 		for (EWolfResponse res : usersResult) {
 			if (res.getResult() != RES_SUCCESS) {
 				return new AddWolfpackMemberResponse(RES_GENERIC_ERROR, wolfpacksResult, usersResult);
 			}
 		}
 		return new AddWolfpackMemberResponse(RES_SUCCESS);
 	}
 
 }
