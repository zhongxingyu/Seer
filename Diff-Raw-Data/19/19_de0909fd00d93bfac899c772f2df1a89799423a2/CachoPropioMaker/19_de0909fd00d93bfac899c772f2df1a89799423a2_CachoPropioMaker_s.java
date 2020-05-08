 package plan;
 
 import models.User;
 import models.UserChunk;
 import models.UserChunks;
 import models.Video;
 
 public class CachoPropioMaker {
 	
 	private final User planRequester;
 	private final 	Video video;
 	
 	
 
 	public CachoPropioMaker(User planRequester, Video video) {
 		super();
 		this.planRequester = planRequester;
 		this.video = video;
 	}
 
 
 
 	public UserChunks makeCacho(int from, UserChunks planRequesterChunks) {
 		
 		UserChunks uc = new UserChunks(planRequester);
 
 		for(int i = from; i<video.chunks.size();i++){
 			if(planRequesterChunks.hasChunk(i)){
 				uc.chunks.add(new UserChunk(i));
 			} else {
 				return uc;
 			}
 		}
 		/*
 		 * si el requester tiene todo el video...
 		 */
 		return uc;
 	}
 
 }
