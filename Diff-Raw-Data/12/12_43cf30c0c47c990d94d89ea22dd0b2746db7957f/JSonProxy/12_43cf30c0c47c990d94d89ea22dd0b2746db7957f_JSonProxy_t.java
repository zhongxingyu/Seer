 package pl.edu.agh.io.coordinator.utils.net;
 
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.NameValuePair;
 import org.apache.http.StatusLine;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.entity.UrlEncodedFormEntity;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.entity.StringEntity;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.message.BasicHeader;
 import org.apache.http.message.BasicNameValuePair;
 import org.apache.http.protocol.HTTP;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import pl.edu.agh.io.coordinator.resources.Group;
 import pl.edu.agh.io.coordinator.resources.Layer;
 import pl.edu.agh.io.coordinator.resources.LayerDiff;
 import pl.edu.agh.io.coordinator.resources.MapItem;
 import pl.edu.agh.io.coordinator.resources.Message;
 import pl.edu.agh.io.coordinator.resources.User;
 import pl.edu.agh.io.coordinator.resources.UserItem;
 import pl.edu.agh.io.coordinator.resources.UserState;
 import android.util.Log;
 import android.util.Pair;
 
 public class JSonProxy implements IJSonProxy {
 	private static JSonProxy instance;
 	private static long sessionID = -1;
 
 	private JSonProxy() {
 
 	}
 
 	public static synchronized JSonProxy getInstance() {
 		if (instance == null)
 			instance = new JSonProxy();
 		return instance;
 	}
 
 	@Override
 	public synchronized boolean login(String userName, String password) {
 		Map<String, String> paramsInString = new HashMap<String, String>();
 		paramsInString.put("login", userName);
 		paramsInString.put("password", password);
 		JSONObject params = new JSONObject(paramsInString);
 		System.out.println(params);
 		String jsonString = getJSonString("login", params);
 		JSONObject jsonObject;
 		try {
 			jsonObject = new JSONObject(jsonString);
 			String exception = jsonObject.getString("exception");
 			if (exception.equals("null"))
 				sessionID = jsonObject.getLong("retval");
 			else
 				sessionID = -1;
 		} catch (JSONException e) {
 			e.printStackTrace();
 			sessionID = -1;
 		}
 		return (sessionID != -1);
 	}
 
 	@Override
 	public synchronized void logout() {
 
 	}
 
 	private String getJSonString(String methodName, JSONObject params) {
 		StringBuilder builder = new StringBuilder();
 		HttpClient client = new DefaultHttpClient();
 		HttpPost httpPost = new HttpPost(
 				"http://io.wojtasskorcz.eu.cloudbees.net/" + methodName);
 
 		try {
			/*
			 * StringEntity stringEntity = new StringEntity("data=" +
			 * URLEncoder.encode(params.toString(), "UTF-8"));
			 * stringEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,
			 * "application/x-www-form-urlencoded"));
			 */
			StringEntity stringEntity = new StringEntity(params.toString());
 			stringEntity.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,
					"application/json"));
 			httpPost.setEntity(stringEntity);
 		} catch (UnsupportedEncodingException e1) {
 			e1.printStackTrace();
 		}
 
 		try {
 			HttpResponse response = client.execute(httpPost);
 			StatusLine statusLine = response.getStatusLine();
 			int statusCode = statusLine.getStatusCode();
 			if (statusCode == 200) {
 				HttpEntity entity = response.getEntity();
 				InputStream content = entity.getContent();
 				BufferedReader reader = new BufferedReader(
 						new InputStreamReader(content));
 				String line;
 				while ((line = reader.readLine()) != null) {
 					builder.append(line);
 				}
 			} else {
 				Log.e("err:", "Failed to download file");
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return builder.toString();
 	}
 
 	@Override
 	public LayerDiff getDiff(Layer layer) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Set<Layer> getLayers() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void addToLayer(Layer layer, MapItem data) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void removeFromLayer(Layer layer, MapItem data) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void updateSelfState(UserState userState) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void addItemToUser(User user, UserItem item) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void removeItemFromUser(User user, UserItem item) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public Set<User> getUsers() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Group getGroups() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void createGroup(String groupName) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void addToGroup(Group group, String user) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void removeFromGroup(Group group, String user) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void sendMessage(String message) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public Set<Message> getMessages() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 }
