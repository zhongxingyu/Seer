 package il.technion.ewolf.server.jsonDataHandlers;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import il.technion.ewolf.ewolf.WolfPackLeader;
 import il.technion.ewolf.server.EWolfResponse;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonElement;
 import com.google.inject.Inject;
 
 import static il.technion.ewolf.server.EWolfResponse.*;
 
 public class CreateWolfpackHandler implements JsonDataHandler {
 	private final WolfPackLeader socialGroupsManager;
 
 	@Inject
 	public CreateWolfpackHandler(WolfPackLeader socialGroupsManager) {
 		this.socialGroupsManager = socialGroupsManager;
 	}
 
 	private static class JsonReqCreateWolfpackParams {
 		List<String> wolfpackNames;
 	}
 
 	static class CreateWolfpackResponse extends EWolfResponse {
 		List<EWolfResponse> wolfpacksResult;
 		public CreateWolfpackResponse(String result) {
 			super(result);
			wolfpacksResult = null;
 		}
 		public CreateWolfpackResponse(String result, List<EWolfResponse> wolfpacksResult) {
 			super(result);
 			this.wolfpacksResult = wolfpacksResult;
 		}
 	}
 
 	/**
 	 * @param	jsonReq	serialized object of JsonReqCreateWolfpackParams class
 	 * @return	"success" or error message
 	 */
 	@Override
 	public EWolfResponse handleData(JsonElement jsonReq) {
 		Gson gson = new Gson();
 		JsonReqCreateWolfpackParams jsonReqParams;
 		try {
 			jsonReqParams = gson.fromJson(jsonReq, JsonReqCreateWolfpackParams.class);
 		} catch (Exception e) {
 			return new CreateWolfpackResponse(RES_BAD_REQUEST);
 		}
 
 		if (jsonReqParams.wolfpackNames == null || jsonReqParams.wolfpackNames.isEmpty()) {
			return new CreateWolfpackResponse(RES_BAD_REQUEST + ": must specify wolfpack name/s.");
 		}
 
 		List<EWolfResponse> wolfpacksResult = new ArrayList<EWolfResponse>();
 		for (String wolfpackName : jsonReqParams.wolfpackNames) {
 			if (socialGroupsManager.findSocialGroup(wolfpackName) != null) {
				wolfpacksResult.add(new EWolfResponse(RES_BAD_REQUEST + ": wolfpack already exists"));
 				continue;
 			}
 			try {
 				socialGroupsManager.findOrCreateSocialGroup(wolfpackName);
 			} catch (Exception e) {
 				wolfpacksResult.add(new EWolfResponse(RES_INTERNAL_SERVER_ERROR));
 				continue;
 			}
 			wolfpacksResult.add(new EWolfResponse());
 		}
 		for (EWolfResponse res : wolfpacksResult) {
 			if (res.getResult() != RES_SUCCESS) {
 				return new CreateWolfpackResponse(RES_GENERIC_ERROR, wolfpacksResult);
 			}
 		}
 		return new CreateWolfpackResponse(RES_SUCCESS);
 	}
 
 }
