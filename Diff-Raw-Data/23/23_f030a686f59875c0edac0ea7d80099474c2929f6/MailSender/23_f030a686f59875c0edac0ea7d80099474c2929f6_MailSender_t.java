 package de.boomboxbeilstein.android2;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.http.NameValuePair;
 import org.apache.http.message.BasicNameValuePair;
 
 import android.util.Log;

import com.google.gson.JsonParseException;

 import de.boomboxbeilstein.android2.utils.GsonFactory;
 import de.boomboxbeilstein.android2.utils.Web;
 
 public class MailSender {
 	private static final String URL = InfoProvider.URL + "mail.php?is-ajax=true";
 	private static final String TAG = "MailSender";
 	
 	public String type;
 	public String name;
 	public String subject;
 	public String message;
 	
 	public boolean send() {
 		List<NameValuePair> nvps = new ArrayList<NameValuePair>();
 		nvps.add(new BasicNameValuePair("type", type));
 		nvps.add(new BasicNameValuePair("name", name));
 		nvps.add(new BasicNameValuePair("subject", subject));
 		nvps.add(new BasicNameValuePair("message", message));
 		try {
 			String str = Web.post(URL, nvps);
 			SendMailResponse response = GsonFactory.createGson().fromJson(str, SendMailResponse.class);
 			return response.wasSuccessful();
 		} catch (IOException e) {
 			Log.e(TAG, "Error sending mail");
 			e.printStackTrace();
 			return false;
		} catch (JsonParseException e) {
 			Log.e(TAG, "Error sending mail");
 			e.printStackTrace();
 			return false;
 		}
 	}
 }
