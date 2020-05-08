 package ag.ifpb.sct.ws.service;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.net.URLConnection;
 
 import ag.facebook.graph.action.PostPhoto;
 import ag.facebook.graph.http.FacebookRequestException;
 import ag.facebook.graph.object.PostId;
 import ag.ifpb.sct.ws.model.Image;
 
 public class FacebookServiceRunnable implements Runnable {
 	private final String TOKEN = "CAACEdEose0cBALv1ae2evnNQkPqb" +
 			"zHX9nw55SYLc7aKRIDQLAbBoCtxDPkIPZBVASyMLI698dQ3V5gXgr8TK4zcs0ZA6qxOE" +
 			"IaFy2iZBFnMHM8ubnC5pCwZB8kBYBbocA09NpWZBPiOh1vaufEZClUUGXZCFV0XK2w2z" +
 			"uevi7JrjwfLR0uGA97Yw0tB8vxkdywZD";
 	private final Image image;
 	
 	public FacebookServiceRunnable(Image image) {
 	  this.image = image;
   }
 
 	@Override
 	public void run() {
 		try {
 			//
 			ByteArrayInputStream input = new ByteArrayInputStream(image.getData());
 			//
 			PostPhoto.PostPhotoValue value = new PostPhoto.PostPhotoValue();
 			value.setLength(image.getLength());
 			value.setMessage("Testando aplicativo para evento IFPB.");
 			value.setMimeType(URLConnection.guessContentTypeFromName(image.getName()));
 			value.setStream(input);
 			// 
 			PostPhoto postPhoto = new PostPhoto(TOKEN, "265529076922102", value);
 			PostId postId = postPhoto.execute();
 			//
 			System.out.println("PostId: " + postId);
 		}
 		catch (FacebookRequestException e) {
			e.printStackTrace();
 		}
 	}
 
 }
