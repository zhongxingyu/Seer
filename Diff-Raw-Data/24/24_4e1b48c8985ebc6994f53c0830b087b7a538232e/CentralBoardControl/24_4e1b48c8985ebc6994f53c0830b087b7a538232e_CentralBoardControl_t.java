 package elfville.server.controller;
 
 import elfville.protocol.*;
 
 import elfville.server.CurrentUserProfile;
 import elfville.server.Database;
 import elfville.server.model.*;
 import elfville.protocol.Response.Status;
 
 /*
  * Control all requests (POST/GET/DELETE) to Central Board
  */
 public class CentralBoardControl extends Controller {
 
 	public static CentralBoardResponse getAllPosts(CentralBoardRequest inM, CurrentUserProfile currentUser) {
 		
 		CentralBoardResponse resp= new CentralBoardResponse(Status.FAILURE, "failure", null);
 		User user = database.userDB.findUserByModelID(currentUser.getCurrentUserId());
 		
 		if (user == null) {
 			return resp;
 		}
 		
 		Elf elf= user.getElf();
 		
 		resp.posts= ControllerUtils.buildPostList(database.postDB.getCentralPosts(), elf);
 		resp.status= Status.SUCCESS;
 		return resp;
 	}
 
 	public static Response addPost(PostCentralBoardRequest postRequest,
 			CurrentUserProfile currentUser) {
 		Response resp = new Response(Status.FAILURE);
 
 		User user = database.userDB.findUserByModelID(currentUser
 				.getCurrentUserId());
 		if (user == null) {
 			return resp;
 		}
 
 		if (postRequest.post == null) {
 			return resp;
 		}
 
 		// make sure that we actually get some content to post
 		if (postRequest.post.title == null || postRequest.post.title.equals("")
 				|| postRequest.post.content == null
 				|| postRequest.post.content.equals("")) {
 			return resp;
 		}
 
 		// yay we can actually post!
 		Elf elf = user.getElf();
 		Post post = new Post(postRequest.post, elf);
 		database.postDB.insert(post);
 		resp.status = Status.SUCCESS;
 		return resp;
 	}
 
 	public static Response votePost(VoteRequest r,
 			CurrentUserProfile currentUser) {
 		Response resp = new Response(Status.FAILURE);
 
 		User user = Database.DB.userDB.findUserByModelID(currentUser
 				.getCurrentUserId());
 		if (user == null) {
 			return resp;
 		}
 
 		Elf e = user.getElf();
 		if (e == null) {
 			return resp;
 		}
 
 		Post post = Database.DB.postDB.findByEncryptedModelID(r.modelID);
 
 		if (post == null) {
 			return resp;
 		}
 
 		if (r.upsock && post.upsock(e)) {
 			resp.status = Response.Status.SUCCESS;
		} else if (!r.upsock && post.downsock(e)) {
 			resp.status = Response.Status.SUCCESS;
		} else {
			resp.message = "You already voted on this post.";
 		}
 
 		return resp;
 	}
 
 	public static Response deletePost(DeleteCentralBoardRequest req,
 			CurrentUserProfile currentUser) {
 		Response resp = new Response(Status.FAILURE);
 
 		User user = Database.DB.userDB.findUserByModelID(currentUser
 				.getCurrentUserId());
 		if (user == null) {
 			return resp;
 		}
 
 		Elf elf = user.getElf();
 		if (elf == null) {
 			return resp;
 		}
 
 		if (req.post == null) {
 			return resp;
 		}
 
 		Post post = Database.DB.postDB.findByEncryptedModelID(req.post.modelID);
 
 		if (post == null) {
 			return resp;
 		}
 
 		// make sure the person trying to delete the post created the post
 		if (!post.getElf().equals(elf)) {
 			return resp;
 		}
 
 		Database.DB.postDB.delete(post);
 
 		resp.status = Status.SUCCESS;
 		return resp;
 	}
 }
