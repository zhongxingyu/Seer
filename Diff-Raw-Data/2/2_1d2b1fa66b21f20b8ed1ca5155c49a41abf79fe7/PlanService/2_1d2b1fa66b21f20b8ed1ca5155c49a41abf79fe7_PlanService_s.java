 package controllers;
 
 import models.RetrievalPlan;
 import models.User;
 import models.Video;
 import net.sf.oval.constraint.NotNull;
 import plan.RetrievalPlanCreator;
 
 public class PlanService extends BaseService {
 
 	public static void getRetrievalPlan(@NotNull String videoId, @NotNull String userId){
 
 		if(validation.hasErrors()){
 			play.Logger.error("Invalid params: %s", params);
 			jsonError("Invalid params");
 		}
 		
 		play.Logger.info("Retrieval plan requested by user: "+userId+" for video: "+videoId);
 		
		User planRequester = User.find("userId=?", userId).first();
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
 		
 		if(plan == null) {
 			jsonError("Unable to ellaborate retrieving plan for video "+video.videoId+" for user "+planRequester.email+" - not enough sources available");
 		} 
 		play.Logger.info("Returning retrieval plan for video: " + videoId);
 		jsonOk(plan);
 	}
 }
