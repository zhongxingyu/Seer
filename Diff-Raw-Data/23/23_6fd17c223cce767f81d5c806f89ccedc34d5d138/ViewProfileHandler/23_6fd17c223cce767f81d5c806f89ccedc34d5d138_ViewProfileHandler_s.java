 package il.technion.ewolf.server;
 
 import il.technion.ewolf.kbr.Key;
 import il.technion.ewolf.socialfs.Profile;
 import il.technion.ewolf.socialfs.SocialFS;
 import il.technion.ewolf.socialfs.UserID;
 import il.technion.ewolf.socialfs.exception.ProfileNotFoundException;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Calendar;
 
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
 
 public class ViewProfileHandler implements HttpRequestHandler {
 	private SocialFS socialFS;
 	
 	private class JsonProfile {
 		@SuppressWarnings("unused")
 		private String name;
 		@SuppressWarnings("unused")
 		private String id;
 		
 		private JsonProfile(String name, String id) {
 			this.name = name;
 			this.id = id;
 		}
 	}
 	
 	@Inject
 	public ViewProfileHandler(SocialFS socialFS) {		
 		this.socialFS = socialFS;
 		
 	}
 
 	//XXX req of type GET with "/viewProfile/{UserID}" URI
 	@Override
 	public void handle(HttpRequest req, HttpResponse res,
 			HttpContext context) throws HttpException, IOException {
 		//TODO move adding headers to response intercepter
 		res.addHeader("Server", "e-WolfNode");
 		res.addHeader("Date",Calendar.getInstance().getTime().toString());
 		
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
 
 		Gson gson = new Gson();
 		String json = gson.toJson(new JsonProfile(profile.getName(), profile.getUserId().toString()));
 		res.setEntity(new StringEntity(json));
 	}
 }
