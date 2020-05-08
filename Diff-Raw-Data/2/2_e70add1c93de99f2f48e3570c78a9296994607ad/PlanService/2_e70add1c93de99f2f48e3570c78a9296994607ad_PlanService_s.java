 package controllers;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import models.Cacho;
 import models.RetrievalPlan;
 import models.User;
 import models.UserCacho;
 import models.UserCachos;
 import models.Video;
 import net.sf.oval.constraint.NotNull;
 import plan.RetrievalPlanCreator;
 import controllers.response.Ok;
 
 public class PlanService extends BaseService {
 	
 	public static void mock(){
 		
 		User user = new User("yo@yo.com","yo@yo.com","127.127.127.127", 8080, 10002);
 		
 		List<String> chunks = new ArrayList<String>();
 		Map<Integer, String> videoChunks = new HashMap<Integer, String>();
 		videoChunks.put(0, "chunkHash");
 		chunks.add("chunkHash");
 		List<UserCachos> userCachos = new ArrayList<UserCachos>();
 		UserCachos ucs = new UserCachos(user);
 		Cacho cacho = new Cacho(0L, 1000L);
 		ucs.addCacho(cacho);
 		userCachos.add(ucs);
 		Video video = new Video();
 //		Video video = new Video("videoTest", "fileNameTest.mp4", 1000L, chunks, user);
 		video.videoId = "videoTest";
 		video.fileName = "fileNameTest.mp4";
 		video.lenght = 1000L;
 		video.chunks = videoChunks;
 		video.userCachos = userCachos;
 		
 		UserCacho uc = new UserCacho(user, cacho);
 		List<UserCacho> ucList  = new ArrayList<UserCacho>();
 		ucList.add(uc);
 		RetrievalPlan rp = new RetrievalPlan(video, ucList);
 		
 //		jsonOk(rp);
 //		new Video(null, null, 0L, null, null)
 //		jsonOk(new RetrievalPlan(video, ucList));
 //		renderJSON(new RetrievalPlan(video, ucList));
 		renderJSON(new Ok(rp));
 
 	}
 
 	
 	
 	public static void getRetrievalPlan(@NotNull String videoId, @NotNull String userId){
 
 		if(validation.hasErrors()){
 			play.Logger.error("Invalid params: %s", params);
 			jsonError("Invalid params");
 		}
 		
 		play.Logger.info("Retrieval plan requested by user: "+userId+" for video: "+videoId);
 		
 		User planRequester = User.find("email=?", userId).first();
 		Video video = Video.find("videoId=?", videoId).first();
 		
 		if(planRequester == null){
 			play.Logger.error("No existe el planRequester: %s", userId);
 			jsonError("No existe el planRequester "+userId);
 		}
 		
 		if(video == null){
 			play.Logger.error("No existe el video: %s", videoId);
 			jsonError("No existe el video "+videoId);
 		}
 		
 		RetrievalPlan plan = new RetrievalPlanCreator(video, planRequester).generateRetrievalPlan();
 		
 		play.Logger.info("pplan %s", plan.getUserCachos());
 		play.Logger.info("pplan %s", plan.getUserCachos().size());
 		
 		if(plan == null) {
 			jsonError("Unable to ellaborate retrieving plan for video "+video.videoId+" for user "+planRequester.email+" - not enough sources available");
 		} 
 		play.Logger.info("Returning retrieval plan for video: " + videoId);
		jsonOk(plan);
 		renderJSON(new Ok(plan));
 	}
 }
