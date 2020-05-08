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
 	private final Image image;
	private final String TOKEN = "CAACEdEose0cBAKDocHyC" +
			"4C0WFaUgPSIwKag1zn2O9YXhhQFBLqCF3LxLBI9oR2nBf1KK6" +
			"nxwrT7oKd4t92SOROJUtoUNADZAA7p0VZAhKURHmnBZArzebeTQCh" +
			"LiZCBycGZAVaUtK1JKLwBUkxO6BZAnV8Kui0tOqZCH0ennRGacY5t" +
			"7cAzgGKc81YboaZAKkN02PpWh2iK0oAZDZD";
 	
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
 			System.out.println(e.getErrorJson());
 		}
 	}
 
 }
