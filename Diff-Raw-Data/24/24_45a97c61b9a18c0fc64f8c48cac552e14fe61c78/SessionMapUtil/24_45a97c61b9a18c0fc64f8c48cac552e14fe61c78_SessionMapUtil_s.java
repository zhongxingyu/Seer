 package org.knetwork.webapp.util;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.Hashtable;
 import java.util.Map;
 import java.util.UUID;
 
 import javax.servlet.http.HttpSession;
 
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.util.EntityUtils;
 
 public class SessionMapUtil {
 
 	private static final String WHITEBOARD = "WHITEBOARD";
 	private static final String TOKBOX = "TOKBOX";
 	private static final String SESSION_TITLE = "SESSION_TITLE";
 
	public static Map<String, Map<String, String>> sessionMap = new Hashtable<String, Map<String, String>>();
 
 	public static String initWhiteboardSession(HttpSession session, String learningSessionId,
 			String username, String title, String joinOrCreate, String prefix) {
 		StringBuilder builder = new StringBuilder();
 		builder.append(prefix + "/whiteboard/workplace");
 
 		try {
 
 			URL url = new URL(
 					prefix+"/api/"
 							+ joinOrCreate + "?sessionId=" + learningSessionId
 							+ "&title=" + title + "&username=" + username);
 			URLConnection conn = url.openConnection();
 
 			BufferedReader in = new BufferedReader(new InputStreamReader(
 					conn.getInputStream()));
 			String inputLine;
 
 			while ((inputLine = in.readLine()) != null) {
 				builder.append(inputLine);
 			}
 			in.close();
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		System.out.println(builder.toString());
 		session.setAttribute("whiteboardJoinUrl", builder.toString());
 		return builder.toString();
 	}
 	
 	public static String initWhiteboardSessionForChat(HttpSession session, String orgId,
 			String username, String title, String joinOrCreate, String prefix) {
 		StringBuilder builder = new StringBuilder();
 		builder.append(prefix + "/whiteboard/chat");
 
 		try {
 
 			String urlPath = 
 					prefix+"/api/chat?orgId=" + orgId
 					+ "&title=" + title + "&username=" + username;
 
 			HttpClient httpclient = new DefaultHttpClient();
 			HttpGet httpget = new HttpGet(urlPath);
 			HttpResponse response = httpclient.execute(httpget);
 			HttpEntity entity = response.getEntity();
 			if (entity != null) {
 			    long len = entity.getContentLength();
 			    if (len != -1 && len < 2048) {
 			    	builder.append(EntityUtils.toString(entity));
 			    }
 			}
 
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		System.out.println(builder.toString());
 		session.setAttribute("whiteboardJoinUrl", builder.toString());
 		return builder.toString();
 	}
 	
 	public static synchronized String generateLearningSessionId() {
 		String uuid = UUID.randomUUID().toString();
 		addLearningSessionToMap(uuid);
 		return uuid;
 	}
 
 	public static void addLearningSessionToMap(String learningSessionId) {
 		if (!isLearningSessionMapped(learningSessionId)) {
 			Map<String, String> apiSessionMap = new Hashtable<String, String>();
 			apiSessionMap.put(WHITEBOARD, "");
 			apiSessionMap.put(TOKBOX, "");
 			sessionMap.put(learningSessionId, apiSessionMap);
 		}
 	}
 	
 	public static void setSessionTitle(String learningSessionId, String sessionTitle) {
 		if (isLearningSessionMapped(learningSessionId)) {
 			Map<String, String> apiSessionMap = sessionMap.get(learningSessionId);
 			apiSessionMap.put(SESSION_TITLE, sessionTitle);
 			sessionMap.put(learningSessionId, apiSessionMap);
 		}
 	}
 	
 	public static String getSessionTitle(String learningSessionId) {
 		if (isLearningSessionMapped(learningSessionId)) {
 			Map<String, String> apiSessionMap = sessionMap.get(learningSessionId);
 			return apiSessionMap.get(SESSION_TITLE);
 		} else {
 			return "";
 		}
 	}
 	
 	private static boolean isLearningSessionMapped(String learningSessionId) {
 		return sessionMap.get(learningSessionId) != null;
 	}
 
 	public static boolean tokboxSessionExists(String learningSessionId) {
 		return getTokboxSessionId(learningSessionId) != null;
 	}
 
 	public static String getTokboxSessionId(String learningSessionId) {
 		if (!isLearningSessionMapped(learningSessionId)) {
 			addLearningSessionToMap(learningSessionId);
 			return null;
 		} else {
 			return sessionMap.get(learningSessionId).get(TOKBOX);
 		}
 	}
 
 	public static void addTokboxSessionId(String learningSessionId,
 			String tokboxSessionId) {
 		sessionMap.get(learningSessionId).put(TOKBOX, tokboxSessionId);
 	}
 }
