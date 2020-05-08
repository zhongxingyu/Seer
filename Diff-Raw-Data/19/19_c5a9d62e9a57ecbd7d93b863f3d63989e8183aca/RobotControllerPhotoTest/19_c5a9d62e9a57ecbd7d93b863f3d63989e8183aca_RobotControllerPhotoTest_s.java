 package edu.umn.robotcontrol.test;
 
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.Date;
 
 import javax.imageio.ImageIO;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.ByteArrayEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 public class RobotControllerPhotoTest {
 
 	/**
 	 * Post a new photo to the endpoint every 2 seconds
 	 * 
 	 * @param args
 	 * @throws IOException 
 	 * @throws ClientProtocolException 
 	 * @throws URISyntaxException 
 	 * @throws InterruptedException 
 	 */
 	public static void main(String[] args) throws ClientProtocolException, IOException, URISyntaxException, InterruptedException {
 		System.out.println("Posting a new photo every 2 seconds");
 		
 		while(true){			
 			// Grab a sample photo
 			URL samplePhotoURL = ClassLoader.getSystemResource("samplephoto.jpg");
 			File photoFile = new File(samplePhotoURL.toURI());
 			HttpClient client = new DefaultHttpClient();
 			HttpPost httpPost = new HttpPost(
					TestProperties.baseurl + "/control/control/photo");
 			
 			// Write the time on it so that we can see that the photo changed.
 			BufferedImage img = ImageIO.read(photoFile);
 			char[] chars = new Date().toString().toCharArray();
 			img.getGraphics().drawChars(chars, 0, chars.length, 20, 20);
 			ByteArrayOutputStream baos = new ByteArrayOutputStream();
 			ImageIO.write(img, "jpg", baos);
 			baos.flush();
 			
 			// Write the photo to the REST endpoint
 			byte[] bytes = baos.toByteArray();
 			httpPost.setEntity(new ByteArrayEntity(bytes));
 			System.out.println("Posting photo: " + String.valueOf(chars));
 			HttpResponse response = client.execute(httpPost);
 			HttpEntity entity = response.getEntity();
 			InputStream inStream = entity.getContent();
 			// Apache IOUtils makes this pretty easy :)
 			System.out.println(IOUtils.toString(inStream));
 			inStream.close();
 			Thread.sleep(2000);
 		}
 
 	}
 
 }
