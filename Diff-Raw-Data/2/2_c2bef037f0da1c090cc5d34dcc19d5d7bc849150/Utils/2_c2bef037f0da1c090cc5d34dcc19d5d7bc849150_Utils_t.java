 package deadlineserver;
 
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.net.HttpURLConnection;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.zip.GZIPInputStream;
 
 import javax.servlet.http.HttpServletResponse;
 
 import org.json.simple.JSONArray;
 import org.json.simple.JSONObject;
 import deadlineserver.models.*;
 
 import com.google.android.gcm.server.Constants;
 import com.google.android.gcm.server.Message;
 import com.google.android.gcm.server.Result;
 import com.google.android.gcm.server.Sender;
 import com.googlecode.objectify.Objectify;
 import com.googlecode.objectify.ObjectifyService;
 import com.googlecode.objectify.Query;
 
 public class Utils
 {
 	public static final Logger log = Logger.getLogger(Utils.class.getName());
 	
 	public static void registerObjectifyClasses()
 	{
 		try
 		{
 			ObjectifyService.register(Deadline.class);
 		}
 		catch (IllegalArgumentException e)
 		{ }
 		try
 		{
 			ObjectifyService.register(DUser.class);
 		}
 		catch (IllegalArgumentException e)
 		{ }
 		try
 		{
 			ObjectifyService.register(Subscription.class);
 		}
 		catch (IllegalArgumentException e)
 		{ }
 		try
 		{
 			ObjectifyService.register(PendingMessage.class);
 		}
 		catch (IllegalArgumentException e)
 		{ }
 	}
 	
 	public static String getDataFromUrl(String textUrl)
 	{
 		try		
 		{
 			URL url = new URL(textUrl);
 			URI uri = null;
 		    try
 			{
 				uri = new URI(url.getProtocol(), url.getHost(), url.getPath(), url.getQuery(), null);
 			} catch (URISyntaxException e)
 			{
 				log.severe("URISyntaxException in url : " + textUrl);
 				e.printStackTrace();
 				return "";
 			}
 			HttpURLConnection con = (HttpURLConnection) uri.toURL().openConnection();
 			con.setReadTimeout(10000);
 			con.setConnectTimeout(10000);
 			con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/A.B (KHTML, like Gecko) Chrome/X.Y.Z.W Safari/A.B.");
 			
 			Reader r = null;
 			
 			if ("gzip".equals(con.getContentEncoding())) {
 				r = new InputStreamReader(new GZIPInputStream(con.getInputStream()));
 			}
 			else
 			{
 				r = new InputStreamReader(con.getInputStream());
 			}
 			
 			StringBuilder buf = new StringBuilder();
 			while (true) {
 			  int ch = r.read();
 			  if (ch < 0)
 			    break;
 			  buf.append((char) ch);
 			}
 			con.disconnect();
 			
 			//System.out.println("GETDATAFROMURL for "+ textUrl + " returned :"+ buf.toString());
 			
 			
 			return buf.toString();
 		}
 		catch (IOException E)
 		{
 			log.warning(E.getMessage());
 			log.warning("IOException : Did not successfully fetch data from : " + textUrl);
 			return "";
 		}
 		catch (Exception E)
 		{
 			log.warning(E.getMessage());
 			log.warning("Unknown Exception : Did not successfully fetch data from : " + textUrl);
 			return "";
 		}
 	}
 	
 	public static void updateRegId(String regId, String canonicalRegId, Objectify ofy)
 	{
 		log.info("Updating " + regId + " to " + canonicalRegId);
 		DUser oldUser = ofy.query(DUser.class).filter("regId",regId).get();
 		if(oldUser==null){
 			log.info("Device "+regId+" not found. Updated");
 			return;
 		}
 		oldUser.regId=canonicalRegId;
 		log.info("Updated");
 	}
 	
 	public static void unregisterRegId(String regId, Objectify ofy)
 	{
 		DUser oldUser = ofy.query(DUser.class).filter("regId",regId).get();
 		if(oldUser!=null){
 			ofy.delete(oldUser);
 			log.info("Unregistered device "+regId);
 			return;
 		}
 		log.info("Device "+regId+" not found. Already unregistered");
 		return;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public static JSONObject getDeadlineJSON(Deadline d, String subscription){
 		JSONObject jDeadline=new JSONObject();
 		jDeadline.put("title", d.title);
 		jDeadline.put("dueDate", d.dueDate.toString());
 		jDeadline.put("description", d.description);
 		jDeadline.put("attachmentUrl", d.attachmentUrl);
 		jDeadline.put("additionalInfo", d.additionalInfo);
 		jDeadline.put("subscription", subscription);
 		jDeadline.put("id", d.id.toString());
 		jDeadline.put("updated", d.updated);
 		return jDeadline;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public static JSONObject getAllDeadlinesJSON(DUser oldUser, Objectify ofy){
 		
 		JSONObject jResult=new JSONObject();
 		
 		JSONArray jDeadlinesResult = new JSONArray();
 		for(int i=0;i<oldUser.subscriptions.size();i++){
 			
 			JSONObject jSubscription =new JSONObject();
 			JSONArray jDeadlines = new JSONArray();
 			
 			Query<Deadline> q=ofy.query(Deadline.class).filter("subscription",oldUser.subscriptions.get(i));			
 			
 			Subscription s=ofy.get(oldUser.subscriptions.get(i));
 			
 			jSubscription.put("subscription",s.name);
 
 			for(Deadline d:q) {
 				jDeadlines.add(getDeadlineJSON(d, s.name));
 			}
 			
 			jSubscription.put("deadlines",jDeadlines);
 			jDeadlinesResult.add(jSubscription);
 		}
 		jResult.put("DEADLINES", jDeadlinesResult);
 		
 		JSONArray jSubscriptionsResult = new JSONArray();
 		for(int i=0;i<oldUser.subscriptions.size();i++){
 			
 			JSONObject jSubscription =new JSONObject();
 			jSubscription.put("id", ofy.get(oldUser.subscriptions.get(i)).id);
 			jSubscription.put("name", ofy.get(oldUser.subscriptions.get(i)).name);
 			jSubscriptionsResult.add(jSubscription);
 		}
 		jResult.put("SUBSCRIPTIONS", jSubscriptionsResult);		
 		return jResult;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public static JSONArray getAllSubscriptionsJSON(Objectify ofy){
 		
 		JSONArray jResult = new JSONArray();
 		Query<Subscription> q=ofy.query(Subscription.class);			
 		for(Subscription s: q){
 			JSONObject jSubscription =new JSONObject();
 			jSubscription.put("id", s.id);
 			jSubscription.put("name", s.name);
 			jResult.add(jSubscription);
 		}
 		return jResult;
 	}
 	
 	public static void sendMessage(String regId, JSONArray addedDeadlines) throws IOException {
 		
		Sender sender=new Sender(Keys.gcmKey);
 		log.setLevel(Level.ALL);
 		log.info("Sending message to device " + regId);
 		Utils.registerObjectifyClasses();
 		//resp.setContentType("application/json");
 		Objectify ofy = ObjectifyService.begin();
 		DUser oldUser=ofy.query(DUser.class).filter("regId",regId).get();
 		if(oldUser==null){
 			 //resp.getWriter().println("{\"error\":0, \"errorstr\":\"User not registered.\"}");
 		}
 		Message message = new Message.Builder().addData("addedDeadlines",addedDeadlines.toJSONString()).build();
 		log.info(message.getData().toString());
 		Result result;
 		try {
 			log.info(regId);
 			log.info(message.toString());
 			result = sender.send(message, regId,5);
 		} catch (IOException e) {
 			log.severe("Exception posting " + message);
 			log.severe(e.getMessage());
 			//resp.setStatus(500);
 			return;
 		}
 		if (result == null) {
 			//resp.setStatus(500);
 			return;
 		}
 		if (result.getMessageId() != null) {
 			log.info("Succesfully sent message to device " + regId);
 			String canonicalRegId = result.getCanonicalRegistrationId();
 			if (canonicalRegId != null) {
 				log.info("Device "+regId+" has more than one registration id's");
 				Utils.updateRegId(regId, canonicalRegId, ofy);
 			}
 		}else {
 			String error = result.getErrorCodeName();
 			if (error.equals(Constants.ERROR_NOT_REGISTERED)) {
 				log.info("Device "+regId+" has uninstalled the app. Unregistering it");
 				Utils.unregisterRegId(regId, ofy);
 			} else {
 				log.severe("Error sending message to device " + regId+ ": " + error);
 				if(error.compareToIgnoreCase("Unavailable")==0){
 					sendMessage(regId, addedDeadlines);
 				}
 			}
 		}
 	}
 }
