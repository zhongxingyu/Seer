 package com.teamboid.twitterapi.media;
 
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
import java.util.Map.Entry;
 
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.InputStreamBody;
 import org.apache.http.entity.mime.content.StringBody;
 import org.scribe.builder.api.FlickrApi;
 import org.scribe.model.OAuthRequest;
 import org.scribe.model.Response;
 import org.scribe.model.Verb;
 
 import com.teamboid.twitterapi.client.RequestHandler;
 import com.teamboid.twitterapi.client.Twitter;
 import com.teamboid.twitterapi.client.TwitterException;
 import com.teamboid.twitterapi.status.StatusUpdate;
 import com.teamboid.twitterapi.status.entity.media.MediaEntity;
 import com.teamboid.twitterapi.status.entity.url.UrlEntity;
 
 public class FlickrMediaService extends ExternalMediaService {
 
 	@Override
 	public String getServiceName() {
 		return "Flickr";
 	}
 
 	@Override
 	public String[] getSupportedUrls() {
 		/*return new String[]{ // TODO
 			"http://flic.kr/p/*",
 			"http://flickr.com/photos/* / *"
 		};*/
 		return new String[]{};
 	}
 
 	@Override
 	public MediaEntity uploadFile(StatusUpdate tweet, Twitter tw,
 			RequestHandler client, InputStream file) throws TwitterException {
 		if(this.authorizedService == null) throw new TwitterException("Not Authorized");
 		if(this.authToken == null) throw new TwitterException("Not Authorized");
 		try{
			
 			OAuthRequest ar = new OAuthRequest(Verb.POST, "http://api.flickr.com/services/upload/");
 			MultipartEntity entity = new MultipartEntity();
			
			ar.addQuerystringParameter("description", attribution);
			ar.addQuerystringParameter("title", tweet.getStatus());
			
 			entity.addPart("description", new StringBody(attribution));
 			entity.addPart("title", new StringBody( tweet.getStatus() ));
			entity.addPart("photo", new InputStreamBody(file, "BOIDUPLOAD.jpg"));
 			
 	        ar.addHeader(entity.getContentType().getName(), entity.getContentType().getValue());
 	        
 			this.authorizedService.signRequest(authToken, ar);
 			
			ByteArrayOutputStream out = new ByteArrayOutputStream();
 	        entity.writeTo(out);
 	        ar.addPayload(out.toByteArray());
 			
 			Response r = ar.send();
 			
 			String b = r.getBody();
 			if(b.contains("<rsp stat=\"fail\">")){
 				throw new TwitterException("Could not Upload to Flickr. Flickr told us: " + b);
 			} else{
 				String id = b.substring( b.indexOf("<photoid>") + "<photoid>".length(), b.indexOf("</photoid>") );
 			
 				String url = "http://flic.kr/p/" + encodeBase58(Long.parseLong(id));
 				ExternalMediaEntity ema = new ExternalMediaEntity(url);
 				return ema;
 			}
 		}catch(Exception e){
 			e.printStackTrace();
 			return null;
 		}
 	}
 	
 	public String getUserName() throws TwitterException{
 		if(this.authToken == null) throw new TwitterException("This requires an OAuth Token");
 		if(this.authorizedService == null) throw new TwitterException("This requires an Authorized Service");
 		String raw = authToken.getRawResponse();
 		int e = raw.indexOf(",", raw.indexOf("username="));
 		return raw.substring( raw.indexOf("username=") + "username=".length(), e == -1 ? raw.length() : e );
 	}
 	
 	// From: http://www.flickr.com/groups/api/discuss/72157616713786392/
 	// Thanks to frog23-net
 	
 	protected static String alphabetString = "123456789abcdefghijkmnopqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ";
 	protected static char[] alphabet = alphabetString.toCharArray();
 	protected static int base_count = alphabet.length;
 	
 	public static String encodeBase58(long num){
 		String result = "";
 		long div;
 		int mod = 0;
 		
 		while (num >= base_count) {
 			div = num/base_count;
 			mod = (int)(num-(base_count*(long)div));
 			result = alphabet[mod] + result;
 			num = (long)div;
 		}
 		if (num>0){
 			result = alphabet[(int)num] + result;
 		}
 		return result;
 	}
 	
 	public static long decodeBase58(String link){
 			long result= 0;
 			long multi = 1;
 			while (link.length() > 0) {
 				String digit = link.substring(link.length()-1);
 				result = result + multi * alphabetString.lastIndexOf(digit);
 				multi = multi * base_count;
 				link = link.substring(0, link.length()-1);
 			}
 			return result;
 	}
 
 	@Override
 	public MediaEntity getEntity(UrlEntity in) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
 	public Class<?> getOAuthService(){
 		return FlickrApi.class;
 	}
 
 }
