 package elfville.server.controller;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import elfville.protocol.*;
 
 import elfville.server.CurrentUserProfile;
 import elfville.server.model.*;
 import elfville.protocol.Response.Status;
 
 /*
  * Control all requests (POST/GET/DELETE) to Central Board
  */
 public class CentralBoardControl extends Controller {
 
 	public static CentralBoardResponse getAllPosts(CentralBoardRequest inM) {
 		//TODO: add some checks here
 		CentralBoardResponse outM = new CentralBoardResponse(Status.SUCCESS,
 				"whatever", ControllerUtils.buildPostList(database.postDB
 						.getCentralPosts()));
 		outM.message = "Valentine's day surprise!";
 		return outM;
 	}
 
 	public static Response addPost(PostCentralBoardRequest postRequest,
 			CurrentUserProfile currentUser) {
 		//TODO: add some checks here
 		User user = database.userDB.findUserByModelID(currentUser
 				.getCurrentUserId());
 		Response resp= new Response(Status.FAILURE);
 		if (user == null) {
 			return resp;
 		}
 		
 		if(postRequest.post == null){
 			return resp;
 		}
 		
		//make sure that we actually get some content to post
		if(postRequest.post.title == null || postRequest.post.title.equals("") ||
				postRequest.post.content == null || postRequest.post.content.equals("")){
 			return resp;
 		}
 		
 		//yay we can actually post!
 		Elf elf = user.getElf();
 		Post post = new Post(postRequest.post, elf);
 		database.postDB.insert(post);
 		resp.status = Status.SUCCESS;
 		return resp;
 	}
 
 }
