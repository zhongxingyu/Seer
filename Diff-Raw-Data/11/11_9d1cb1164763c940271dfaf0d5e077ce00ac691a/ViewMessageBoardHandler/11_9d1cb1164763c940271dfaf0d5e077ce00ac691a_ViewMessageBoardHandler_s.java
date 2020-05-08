 package il.technion.ewolf.server;
 
 import il.technion.ewolf.SocialNetwork;
 import il.technion.ewolf.exceptions.WallNotFound;
 import il.technion.ewolf.kbr.Key;
 import il.technion.ewolf.posts.Post;
 import il.technion.ewolf.posts.TextPost;
 import il.technion.ewolf.socialfs.Profile;
 import il.technion.ewolf.socialfs.SocialFS;
 import il.technion.ewolf.socialfs.UserID;
 import il.technion.ewolf.socialfs.exception.ProfileNotFoundException;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 
 import org.apache.http.HttpException;
 import org.apache.http.HttpRequest;
 import org.apache.http.HttpResponse;
 import org.apache.http.HttpStatus;
 import org.apache.http.entity.FileEntity;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.protocol.HttpContext;
 import org.apache.http.protocol.HttpRequestHandler;
 
 import com.google.gson.Gson;
 import com.google.inject.Inject;
 
 public class ViewMessageBoardHandler implements HttpRequestHandler {
 	private SocialNetwork snet;
 	private SocialFS socialFS;
 	
 	@SuppressWarnings("unused")
 	private class JsonPost {
 		private String postID;
 		private Long timestamp;
 		private String text;
 		
 		private JsonPost(String postID, Long timestamp, String text) {
 			this.postID = postID;
 			this.timestamp = timestamp;
 			this.text = text;
 		}
 	}
 	
 	@Inject
 	public ViewMessageBoardHandler(SocialNetwork snet, SocialFS socialFS) {
 		this.snet = snet;
 		this.socialFS = socialFS;
 	}
 
 	//XXX req of type GET with "/viewMessageBoard/{UserID}" URI
 	@Override
 	public void handle(HttpRequest req, HttpResponse res,
 			HttpContext context) throws HttpException, IOException {
 		//TODO move adding headers to response intercepter
 		res.addHeader("Server", "e-WolfNode");
 		res.addHeader("Date",Calendar.getInstance().getTime().toString());
 		
 		//get user ID from URI
 		String reqURI = req.getRequestLine().getUri();
 		String[] splitedURI = reqURI.split("/");
 		String strUid = splitedURI[splitedURI.length];
 		UserID uid = new UserID(Key.fromString(strUid));
 
 		Profile profile = null;
 		try {
 			profile = socialFS.findProfile(uid);			
 		} catch (ProfileNotFoundException e) {
 			// TODO Auto-generated catch block
 			System.out.println("Profile with UserID" + uid + "not found");
 			res.setStatusCode(HttpStatus.SC_NOT_FOUND);
 			res.setEntity(new FileEntity(new File("404.html"),"text/html"));
 			return;
 		}
 		
 		List<Post> posts = null;
 		try {
 			posts = snet.getWall(profile).getAllPosts();
 		} catch (WallNotFound e) {
 			System.out.println("Wall not found");
 			res.setStatusCode(HttpStatus.SC_INTERNAL_SERVER_ERROR);
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		Gson gson = new Gson();
 		List<ViewMessageBoardHandler.JsonPost> lst = new ArrayList<ViewMessageBoardHandler.JsonPost>();
 		for (Post post: posts) {
 			lst.add(new JsonPost(post.getPostId().toString(), post.getTimestamp(), ((TextPost)post).getText()));
 		}
 		String json = gson.toJson(lst, lst.getClass());
 		res.setEntity(new StringEntity(json));
 	}
 
 }
