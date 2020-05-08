 package com.couple.controllers;
 
 import static org.springframework.web.bind.annotation.RequestMethod.GET;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.TreeMap;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.codec.digest.DigestUtils;
 import org.apache.http.HttpEntity;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.servlet.ModelAndView;
 
 @Controller
 public class ChoosePartner {
 	private final String api_secret = "ogoSgljHJqUGk9fnbzLa";
 	private String apiURL;
 
 	private String getSIG(Map<String, String> params) {
 		String data = "";
 		for (Map.Entry<String, String> entry : params.entrySet()) {
 			//System.out.println(entry.getKey() + "=" + entry.getValue());
 			data += entry.getKey() + "=" + entry.getValue();
 		}
 		data += api_secret;
 		return DigestUtils.md5Hex(data);
 	}
 
 	private String getParamsAsString(Map<String, String> params) {
 		String data = "";
 		for (Map.Entry<String, String> entry : params.entrySet()) {
 			data += entry.getKey() + "=" + entry.getValue() + "&";
 		}
 		return data;
 	}
 
 	private String getUserInfo()  throws ClientProtocolException, IOException {
 		TreeMap<String, String> params = new TreeMap<String, String>();
 		params.put("api_id", "2857279");
 		params.put("method", "users.get");
 		params.put("format", "json");
 		params.put("uids", "3628886");
 		String result = requestToVK(params);
 		return result;
 	}
 
 	private String friends_get(String viewerID) throws ClientProtocolException, IOException {
 		TreeMap<String, String> params = new TreeMap<String, String>();
 		params.put("api_id", "2857279");
 		params.put("method", "friends.get");
 		params.put("v", "3.0");		
 		params.put("format", "json");
 		params.put("uid", viewerID);	
 		
 		//params.put("name_case", "nom");
 		params.put("count", "20");
 		//params.put("offset", "0");
 		
 		params.put("timestamp", Integer.toString((int) (System.currentTimeMillis() / 1000L)));
 		params.put("random", "40275037");
 		
 		params.put("fields", "uid,first_name,last_name,photo,photo_medium,photo_big");
 		String result = requestToVK(params);
 		return result;
 	}
 
 	private String friends_getAppUsers(String viewerID) throws ClientProtocolException, IOException {
 		TreeMap<String, String> params = new TreeMap<String, String>();
 		params.put("api_id", "2857279");
 		params.put("method", "friends.getOnline");
 		params.put("format", "json");
 		params.put("uid", viewerID);		
 		String result = "friends_getAppUsers: " + requestToVK(params);
 		return result;
 	}
 	
 	private String requestToVK(Map<String, String> params) throws IOException, ClientProtocolException, UnsupportedEncodingException {
 		String requestURL = apiURL + "?";
 		requestURL += getParamsAsString(params);
 		requestURL += "sig=" + getSIG(params);
 		String result = "";
 		HttpClient httpclient = new DefaultHttpClient();
 		HttpGet httpget = new HttpGet(requestURL);
 		HttpResponse response = httpclient.execute(httpget);
 		HttpEntity entity = response.getEntity();
 		if (entity != null) {
 			InputStream instream = entity.getContent();
 			BufferedReader br = new BufferedReader(new InputStreamReader(instream, "utf8"));
 			while (br.ready()) {
 				String str = br.readLine();
 				//System.out.println(str);
 				result += str ;
 			}
 		}
 		return result;
 	}
 
 	@RequestMapping(value = "/", method = GET)
 	public ModelAndView choosePartner(HttpServletRequest request)  throws ClientProtocolException, IOException{
 		ModelAndView res = new ModelAndView();
 		StringBuilder result = new StringBuilder();
 		for (Object key : request.getParameterMap().keySet()) {
 			result.append("<p>" + key.toString() + " = " + request.getParameter(key.toString()) + " </p>");
 		}
 		apiURL = request.getParameter("api_url");
 		String viewerID = request.getParameter("viewer_id");//"2657654";
 		
 		ObjectMapper mapper = new ObjectMapper(); // can reuse, share globally
 		String friendsListAsJSON = friends_get(viewerID);
 		System.out.println("friendsListAsJSON = " + friendsListAsJSON);
 		Map<String,Object> friendsList  = mapper.readValue(friendsListAsJSON, Map.class);
		res.setViewName("choose-partner");
 		res.addObject("friendsList", friendsList);
 		return res;
 	}
 }
