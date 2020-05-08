 package logic;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.http.Header;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.client.methods.HttpPost;
 import org.apache.http.client.methods.HttpRequestBase;
 import org.apache.http.entity.mime.MultipartEntity;
 import org.apache.http.entity.mime.content.ByteArrayBody;
 import org.apache.http.entity.mime.content.ContentBody;
 import org.apache.http.entity.mime.content.FileBody;
 import org.apache.http.entity.mime.content.StringBody;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.impl.conn.PoolingClientConnectionManager;
 import org.apache.http.impl.conn.SchemeRegistryFactory;
 import org.yaml.snakeyaml.reader.StreamReader;
 
 import play.Logger;
 
 import utils.Constants;
 import utils.StringUtils;
 
 /**
  * Page crawler - test for correct login - load the latest xml file from website
  * 
  * @author mopa
  * 
  */
 public class Crawler {
 
 	private String username;
 	private String password;
 	private String sessionId;
 	public String period;
 	private HttpClient client;
 	
 	public Crawler(String username, String password) {
 		//TODO vorsichtshalber .....
 		this.username = username;
 		this.password = password;
 		
 		this.sessionId = getSessionId();
 	}
 	
 	public boolean exportFileToWeb(String dataStr) {
		this.username = "test005";
		this.password = "snake";
 		Logger.info("exportFileToWeb");
 		if (checkLogin()) {
 			Logger.info("loginCheck");
 			HttpPost httppost = new HttpPost(Constants.simulate);
 			httppost.addHeader("Cookie", this.sessionId);			
 			
 			File fl = new File("input.xml");
 			try {
 				FileUtils.writeStringToFile(fl, dataStr);
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			FileBody f = new FileBody(fl);
 			
 			MultipartEntity mp = new MultipartEntity();
 			mp.addPart("xml", f);
 			
 			httppost.setEntity(mp);
 			HttpResponse response = doRequest(httppost, true);
 			String responseStr = StringUtils.toString(response);
 			Logger.info("file Upload Response: %s", responseStr);
 			
 			HttpGet g = new HttpGet("http://www.iwi.hs-karlsruhe.de/scs/result/info.jsp");
 			g.addHeader("Cookie", this.sessionId);
 			HttpResponse response2 = doRequest(g, true);
 			String responseStr2 = StringUtils.toString(response2);
 			Logger.info("resp2: %s", responseStr2);
 			return responseStr2.contains("Here you can see your results from");
 		}
 		return false;
 	}
 
 	/**
 	 * load the latest xml file
 	 * 
 	 * @return
 	 */
 	public String importFileFromWeb() {
 		if (checkLogin()) {
 			HttpPost httppost = new HttpPost(Constants.simulate);
 			httppost.addHeader("Cookie", this.sessionId);
 			StringBody res = null;
 			StringBody per = null;
 			try {
 				res = new StringBody("result");
 				per = new StringBody(this.period);
 			} catch (UnsupportedEncodingException e) {
 				e.printStackTrace();
 			}
 			MultipartEntity reqEntity = new MultipartEntity();
 			reqEntity.addPart("source", res);
 			reqEntity.addPart("period", per);
 			httppost.setEntity(reqEntity);
 			HttpResponse response = doRequest(httppost, true);
 			String responseStr = StringUtils.toString(response);
 			if (responseStr.contains("<title>Simulation result</title>")) {
 				Logger.info("simulate erfolgreich", 0);
 				HttpGet httpget = new HttpGet(Constants.xmlFile);
 				httpget.setHeader("Cookie", sessionId);
 				HttpResponse fileResp = doRequest(httpget, false);
 				return StringUtils.toString(fileResp);
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * check for correct login data
 	 * 
 	 * @return
 	 */
 	public boolean checkLogin() {
 		HttpPost httppost = new HttpPost(buildLoginUrl());
 		httppost.addHeader("Cookie", this.sessionId);
 		httppost.addHeader("Content-Type", "application/x-www-form-urlencoded");
 		String response = StringUtils.toString(doRequest(httppost, true));
 		Pattern pattern = Pattern.compile("<option selected value=\\\"([0-9]{1,2})\\\">[0-9]{1,2}</option>");
 		Matcher matcher = pattern.matcher(response);
 		if (matcher.find()) {
 			this.period = matcher.group(1);
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * at first get sessionId
 	 * 
 	 * @return
 	 */
 	private String getSessionId() {
 		HttpGet httpget = new HttpGet(Constants.startPage);
 		HttpResponse response = doRequest(httpget, true);
 		String sessionId = response.getFirstHeader("Set-Cookie").getValue();
 		return sessionId.substring(0, sessionId.indexOf(";"));
 	}
 
 	/**
 	 * do http request with redirection
 	 * 
 	 * @param request
 	 * @return
 	 */
 	private HttpResponse doRequest(HttpRequestBase request, boolean redirect) {
 		HttpResponse response = null;
 		client = new DefaultHttpClient();
 		try {
 			response = client.execute(request);
 		} catch (IOException e) {
 			return null;
 		}
 		if (redirect) {
 			client.getConnectionManager().shutdown();
 			if (response.getStatusLine().getStatusCode() == 302) {
 				HttpGet httpget = new HttpGet(response.getFirstHeader("Location").getValue());
 				httpget.setHeader("Cookie", this.sessionId);
 				response = doRequest(httpget, true);
 			}
 		}
 		return response;
 	}
 
 	/**
 	 * build the login url with username and password
 	 * 
 	 * @return
 	 */
 	private String buildLoginUrl() {
 		return Constants.authUrl + this.username + "&j_password=" + this.password + "&btnSubmit=Login";
 	}
 
 	/**
 	 * only for testing
 	 * 
 	 * @param headers
 	 */
 	@SuppressWarnings("all")
 	private void printHeaders(Header[] headers) {
 		for (Header h : headers) {
 			Logger.info("%s : %s", h.getName(), h.getValue());
 		}
 	}
 }
