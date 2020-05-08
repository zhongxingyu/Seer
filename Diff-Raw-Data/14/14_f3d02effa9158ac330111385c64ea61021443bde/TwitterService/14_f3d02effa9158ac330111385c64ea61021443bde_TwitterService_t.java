 package com.alta189.deskbin.image;
 
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 import java.util.List;
 
 import javax.imageio.ImageIO;
 
 import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.io.IOUtils;
 
 import com.alta189.deskbin.auth.AuthenticationException;
 
 import twitter4j.Twitter;
 import twitter4j.TwitterException;
 import twitter4j.TwitterFactory;
 import twitter4j.auth.AccessToken;
 import twitter4j.media.ImageUpload;
 import twitter4j.media.ImageUploadFactory;
 import twitter4j.media.MediaProvider;
 
 public class TwitterService implements ImageService {
 
 	public static final String NAME = "Twitter";
 	public static final String FILENAME = "capture.png";
 	private static final String CONSUMER_KEY = "Z1lKR21hZFl0UUJRU2IzOEU4Mktrdw==";
 	private static final String CONSUMER_SECRET = "RkdoQng1am1nbWVwNGtOdXd4UDVwRUhIcUVxUm1FNmx1dXJpUXQ0VklZ";
 	private final Twitter twitterService;
 	private MediaProvider imageService;
 
 	public TwitterService(MediaProvider provider, AccessToken accessToken) throws AuthenticationException{
 		this(accessToken);
 		imageService = provider;
 	}
 
 	public TwitterService(AccessToken accessToken) throws AuthenticationException{
 		twitterService = new TwitterFactory().getInstance();
 		twitterService.setOAuthConsumer(new String(Base64.decodeBase64(CONSUMER_KEY)),new String(Base64.decodeBase64(CONSUMER_SECRET)));
 		if (accessToken==null){
 			throw new AuthenticationException("No authentication token! Did you sign in and authorize us?");
 		} else
 			twitterService.setOAuthAccessToken(accessToken);
 		try {
 			twitterService.verifyCredentials();
 		} catch (TwitterException e) {
 			if (e.getStatusCode()==401)
 				throw new AuthenticationException("You need to reauthorize your account!");
 			else
 				throw new AuthenticationException("Twitter is down!");
 		}
 		imageService = MediaProvider.TWITTER;
 	}
 
 	@Override
 	public String getName() {
		return NAME;
 	}
 
 	@Override
 	public String upload(BufferedImage image) throws ImageServiceException {
 		try {
 			ImageUpload upload = new ImageUploadFactory().getInstance(imageService, twitterService.getAuthorization());
 			ByteArrayOutputStream baos = new ByteArrayOutputStream();
 			ImageIO.write(image, "png", baos);
 			InputStream input = IOUtils.toInputStream(IOUtils.toString(baos.toByteArray(),"UTF-8"),"UTF-8");
 			return upload.upload(FILENAME,input);
 		} catch (Exception e){
 			throw new ImageServiceException(e);
 		}
 	}
 
 	@Override
	public String upload(List<BufferedImage> image)	throws ImageServiceException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 }
