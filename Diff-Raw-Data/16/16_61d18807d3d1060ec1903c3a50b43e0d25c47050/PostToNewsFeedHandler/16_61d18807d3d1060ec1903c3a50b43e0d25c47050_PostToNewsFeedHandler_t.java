 package il.technion.ewolf.server.jsonDataHandlers;
 
 import il.technion.ewolf.ewolf.SocialNetwork;
 import il.technion.ewolf.ewolf.WolfPack;
 import il.technion.ewolf.ewolf.WolfPackLeader;
 import il.technion.ewolf.exceptions.WallNotFound;
 import il.technion.ewolf.posts.TextPost;
 import il.technion.ewolf.stash.exception.GroupNotFoundException;
 
 import java.io.FileNotFoundException;
 
 import com.google.gson.Gson;
 import com.google.gson.JsonElement;
 import com.google.inject.Inject;
import com.google.inject.Provider;
 
 import static il.technion.ewolf.server.jsonDataHandlers.EWolfResponse.*;
 
 public class PostToNewsFeedHandler implements JsonDataHandler {
 	private final WolfPackLeader socialGroupsManager;
 	private final SocialNetwork snet;
	private final Provider<TextPost> textPostProvider;
 
 	@Inject
 	public PostToNewsFeedHandler(SocialNetwork snet, WolfPackLeader socialGroupsManager,
			Provider<TextPost> textPostProvider) {
 		this.snet = snet;
 		this.socialGroupsManager = socialGroupsManager;
		this.textPostProvider = textPostProvider;
 	}

 	private static class JsonReqPostToNewsFeedParams {
 		String wolfpackName;
 		//post text
 		String post;
 	}
 
 	static class PostToNewsFeedResponse extends EWolfResponse {
 		public PostToNewsFeedResponse(String result) {
 			super(result);
 		}
 		public PostToNewsFeedResponse() {
 		}
 	}
 
 	/**
 	 * @param	jsonReq	serialized object of JsonReqCreateWolfpackParams class
 	 * @return	"success" or error message
 	 */
 	@Override
 	public Object handleData(JsonElement jsonReq) {
 		Gson gson = new Gson();
 		JsonReqPostToNewsFeedParams jsonReqParams;
 		try {
 			jsonReqParams = gson.fromJson(jsonReq, JsonReqPostToNewsFeedParams.class);
 		} catch (Exception e) {
 			e.printStackTrace();
 			return new PostToNewsFeedResponse(RES_BAD_REQUEST);
 		}
 		if (jsonReqParams.wolfpackName == null || jsonReqParams.post == null) {
 			return new PostToNewsFeedResponse(RES_BAD_REQUEST +
 					": must specify both wolfpack name and post text");
 		}
 
 		WolfPack socialGroup = socialGroupsManager.findSocialGroup(jsonReqParams.wolfpackName);
 		if (socialGroup == null) {
 			return new PostToNewsFeedResponse(RES_NOT_FOUND + ": given wolfpack wasn't found.");
 		}
 
 		try {
			snet.getWall().publish(textPostProvider.get().setText(jsonReqParams.post), socialGroup);
 		} catch (GroupNotFoundException e) {
 			System.out.println("Wolfpack " + socialGroup + " not found");
 			e.printStackTrace();
 			//TODO check what I should response here?
 			return new PostToNewsFeedResponse(RES_INTERNAL_SERVER_ERROR);
 		} catch (WallNotFound e) {
 			System.out.println("Wall not found");
 			e.printStackTrace();
 			return new PostToNewsFeedResponse(RES_INTERNAL_SERVER_ERROR);
 		} catch (FileNotFoundException e) {
 			System.out.println("File /wall/posts/ not found");
 			e.printStackTrace();
 			return new PostToNewsFeedResponse(RES_INTERNAL_SERVER_ERROR);
 		}
 		return new PostToNewsFeedResponse();
 	}
 
 }
