 package xdi2.connector.personal.api;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.net.URL;
 import java.net.URLConnection;
 import java.net.URLEncoder;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.http.HttpException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import xdi2.core.xri3.impl.XRI3Segment;
 
 public class PersonalApi {
 
 	private static final Logger log = LoggerFactory.getLogger(PersonalApi.class);
 
 	private String appId;
 	private String appSecret;
 	private String scope;
 	private String update;
 	private HttpClient httpClient;
 
 	public PersonalApi() {
 
 		this.appId = null;
 		this.appSecret = null;
 		this.httpClient = new DefaultHttpClient();
 	}
 
 	public PersonalApi(String appId, String appSecret) {
 
 		this.appId = appId;
 		this.appSecret = appSecret;
 		this.httpClient = new DefaultHttpClient();
 	}
 
 	public void init() {
 
 	}
 
 	public void destroy() {
 
 		this.httpClient.getConnectionManager().shutdown();
 	}
 
 	public String startOAuth(HttpServletRequest request, String redirectUri, XRI3Segment userXri) throws IOException {
 
 		String client_id = this.appId;
 		if (redirectUri == null) redirectUri = uriWithoutQuery(request.getRequestURL().toString());
 		String state = userXri.toString();
 		String url = "https://api-sandbox.personal.com/oauth/authorize?client_id="+URLEncoder.encode(client_id, "UTF-8")+"&response_type=code&redirect_uri="+URLEncoder.encode(redirectUri, "UTF-8")+"&scope="+URLEncoder.encode(this.scope, "UTF-8")+"&update="+this.update+"&state="+URLEncoder.encode(state, "UTF-8");
 
 		// done
 
 		log.debug("OAuth URI: " + url);
 		return url;
 	}
 
 	public StringBuffer postit(String code,String reqURL) throws IOException{
 		BufferedReader rd = null;
 		StringBuffer sb = new StringBuffer();
 
 		String postURL = "https://api-sandbox.personal.com/oauth/access_token";
 		// Construct data
 		String data = URLEncoder.encode("grant_type", "UTF-8") + "=" + URLEncoder.encode("authorization_code", "UTF-8");
 		data += "&" + URLEncoder.encode("code", "UTF-8") + "=" + URLEncoder.encode(code, "UTF-8");
 		data += "&" + URLEncoder.encode("client_id", "UTF-8") + "=" + URLEncoder.encode(this.appId, "UTF-8");
 		data += "&" + URLEncoder.encode("client_secret", "UTF-8") + "=" + URLEncoder.encode(this.appSecret, "UTF-8");
 		data += "&" + URLEncoder.encode("redirect_uri", "UTF-8") + "=" + URLEncoder.encode(reqURL, "UTF-8");
 		// Send data
 		URL url = new URL(postURL);
 		URLConnection conn = url.openConnection();
 		conn.setDoOutput(true);
 		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=UTF-8");
 		OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
 		wr.write(data);
 		wr.flush();
 
 		// Get the response
 		rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
 		String line;
 		while ((line = rd.readLine()) != null)
 		{
 			sb.append(line);
 		}
 		wr.close();
 		rd.close();
 
 		return sb;
 	}
 
 	public void checkState(HttpServletRequest request, XRI3Segment userXri) throws IOException {
 
 		String state = request.getParameter("state");
 
 		if (state == null) {
 
 			log.warn("No OAuth state received.");
 			return;
 		}
 
 		if (! userXri.toString().equals(state)) throw new IOException("Invalid state: " + state);
 
 		log.debug("State OK");
 	}
 
 	public String exchangeCodeForAccessToken(HttpServletRequest req) throws IOException, HttpException {
 
 		String code = req.getParameter("code");
 		StringBuffer sb = postit(code,uriWithoutQuery(req.getRequestURL().toString()));
 
 		JSONObject jObject;
 		String accessToken = null;
 		try {
 			jObject = new JSONObject(sb.toString());
 			accessToken = jObject.getString("access_token");
 
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			log.debug("Error In method exchangeCodeForAccessToken: " + e.toString());
 		}
 
 		log.debug("Access Token: " + accessToken);
 		//log.debug("req url: "+req.getRequestURL().toString());
 		return accessToken;
 	}
 
 	public void revokeAccessToken(String accessToken) throws IOException, JSONException {
 
 		if (accessToken == null) throw new NullPointerException();
 
 		throw new UnsupportedOperationException("Not implemented.");
 	}
 
 	public String getit(String url, String aToken, String secure_pass) throws IOException {
 		//String url = "http://api.rottentomatoes.com/api/public/v1.0/movies.json?apikey=xrb8fprukkstk5g94v26jhuz";
 		String charset = "UTF-8";
 		String result = null;
 
 		URLConnection conn = new URL(url).openConnection();
 
 		conn.setRequestProperty("Authorization", "Bearer " + aToken);
 
 		InputStream response = conn.getInputStream();
 		// GET request starts
 		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
 		StringBuffer sb = new StringBuffer();
 		String line;
 		while ((line = rd.readLine()) != null)
 		{
 			sb.append(line);
 		}
 		rd.close();
 		result = sb.toString();
 
 		log.debug("Headers: " + conn.getHeaderFields().toString());
 		log.debug("Body: " + result);
 
 		return result;
 	}
 
 	public JSONObject getUser(String accessToken) throws IOException, JSONException {
 
 		if (accessToken == null) throw new NullPointerException();
 
 		log.debug("Retrieving User for Access Token '" + accessToken + "'");
 
 		String url = "https://api-sandbox.personal.com/api/v1/gems/?client_id="+this.appId;
 		String res = getit(url,accessToken,null);
 
 		List<String> encInstanceId = new ArrayList<String>();
 		try {
 			JSONObject jj = new JSONObject(res);
 			org.json.JSONArray gemContents = jj.getJSONArray("gems");
 
 			for (int i=0; i<gemContents.length(); i++)
 				encInstanceId.add(URLEncoder.encode(gemContents.getJSONObject(i).getString("gem_instance_id"), "UTF-8"));
 		} catch (JSONException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		String gemData=null;
 		JSONObject gemObject = new JSONObject();
 		for (String i:encInstanceId)
 		{
 			url = "https://api-sandbox.personal.com/api/v1/gems/"+i+"/?client_id="+this.appId;
 			String secure_pass = this.appSecret;
 			gemData = getit(url,accessToken,secure_pass);
 			try {
 				JSONObject tempObj = new JSONObject(gemData).getJSONObject("gem");
 				//nameGemObject = tempObj;
 				gemObject.put(tempObj.getJSONObject("info").getString("gem_template_id"), tempObj.getJSONObject("data"));
 			} catch (JSONException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 
 
 		}
 
 		log.debug("User: " + gemObject);
 		return gemObject;
 	}
 
 	private static String uriWithoutQuery(String url) {
 
 		return url.contains("?") ? url.substring(url.indexOf("?")) : url;
 	}
 
 	public String getAppId() {
 
 		return this.appId;
 	}
 
 	public void setAppId(String appId) {
 
 		this.appId = appId;
 	}
 
 	public String getAppSecret() {
 
 		return this.appSecret;
 	}
 
 	public void setAppSecret(String appSecret) {
 
 		this.appSecret = appSecret;
 	}
 
 	public String getScope() {
 
 		return this.scope;
 	}
 
	public void setScope(String appSecret) {
 
		this.scope = appSecret;
 	}
 
 	public String getUpdate() {
 
 		return this.update;
 	}
 
	public void setUpdate(String appSecret) {
 
		this.update = appSecret;
 	}
 }
