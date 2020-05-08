 package the.umbautologin;
 
 import java.io.*;
 import java.net.*;
 import java.security.KeyManagementException;
 import java.security.NoSuchAlgorithmException;
 import java.security.cert.X509Certificate;
 import java.util.Map;
 
 import javax.net.ssl.HostnameVerifier;
 import javax.net.ssl.HttpsURLConnection;
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.SSLSession;
 import javax.net.ssl.TrustManager;
 import javax.net.ssl.X509TrustManager;
 
 import android.util.Log;
 
 /**
  * This code is based on sbautologin code from the android app.
  * 
  * Released under the GPL
  * 
  * @author Joseph Paul Cohen 2012
  * old author was sbautologin
  * 
  */
 public class UMBWifi
 {
     private static final String TAG = "umbAutoLogin";
 
     private static final int timeoutms = 20000;
    private static final int numTries = 6;
     
     
     public UMBWifi()
     {
     }
     
     /**
      * Attempts to log in to UMB Wifi
      * 
      * @return true if login was performed. false it you were already logged in and no login is required.
      * @throws Exception if login failed.
      */
     public boolean login(String test_url, String uName, String uPwd) throws Exception
     {
     	
     	Log.d(TAG, "In UMBWifi Login API");
     	
     	LoginState state = new LoginState();
     	
     	
     	try{
     	
         state.testURL = new URL(test_url);
         
         int tries = 0;
         while (state.responseCode == 0 && tries <= numTries){
         	tries++;
 	    	try{
 	    		state.currentStep = "Testing for redirect try: " + tries;
 	    		testAndGetRedirect(state);  
 	    	}catch(IOException e){
 	    		
 	    		Log.e(TAG, "Failed testAndGetRedirect: " + e.getMessage());
 	    		e.printStackTrace();
 	    	}
         }
        
       
         
         
         state.currentStep = "Reading response code";
         Log.d(TAG, "ResponseCode:" + state.responseCode);
        if(true && state.responseCode == 307)
         {
 
             String htmlOfLoginPage = getHtmlOfLoginPage(state);
 
             // parse the form info out of the HTML
             Log.d(TAG, "Parsing UMB login page...");
             HtmlForm formInfo = new HtmlForm(state.redirectURL, htmlOfLoginPage);
 
             state.currentStep = "Parsing login page";
             
             
             // prepare to submit the form
             Log.d(TAG, "Signing in...");
             
             formInfo.parameters.put("uName", uName);
             formInfo.parameters.put("uPwd", uPwd);
             
             StringBuilder sb = new StringBuilder();
             for(Map.Entry<String, String> entry : formInfo.parameters.entrySet())
             {
                 sb.append(URLEncoder.encode(entry.getKey(), "UTF-8") + '='
                         + URLEncoder.encode(entry.getValue(), "UTF-8") + '&');
             }
             
             String postdata = sb.substring(0, sb.length() - 1);
             
             
             
             // send the request
             
             state.currentStep = "Sending credentials";
             
             state.loginConnection = getTrustingSocketContext().getSocketFactory().createSocket(formInfo.actionUrl.getHost(), 443);
             state.loginConnection.setSoTimeout(timeoutms);
             BufferedWriter sendlogin = new BufferedWriter(new OutputStreamWriter(state.loginConnection.getOutputStream()));
             
             sendlogin.write("POST /authUser.php HTTP/1.1\n");
             sendlogin.write("Host: " + formInfo.actionUrl.getHost() +"\n");
             sendlogin.write("User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.6; rv:11.0) Gecko/20100101 Firefox/11.0\n");
             sendlogin.write("Accept: text/html\n");
             sendlogin.write("Referer: " + state.redirectURL + "\n");
             sendlogin.write("Content-Type: application/x-www-form-urlencoded\n");
             sendlogin.write("Content-Length: " + postdata.length() + "\n\n");
             sendlogin.write(postdata);
             sendlogin.flush();
             
             //Log.d(TAG, "what was posted:" + postdata);
             
             BufferedReader islogin = new BufferedReader(new InputStreamReader(state.loginConnection.getInputStream()));
             
 
             state.currentStep = "Reading login response";
       
             String response = "";
             String toparse = "";
             while((toparse = islogin.readLine()) != null){
             	response += toparse;
             	
                 if (response.contains("HTTP/1.1 302")){
                 	// it worked
                 	 islogin.close();
                      state.loginConnection.close();
                      break;
                 }
             	
             	//fail=2 means wrong password
                 if (response.contains("fail=2")){
                 	state.currentStep = "";
                 	islogin.close();
                     state.loginConnection.close();
                 	throw new Exception("Error: fail=2, Check your username and password");
                 }
                 
                 //fail=0 some other error
                 if (response.contains("fail=0")){
                 	state.currentStep = "";
                 	islogin.close();
                     state.loginConnection.close();
                 	throw new Exception("Error: fail=0, Some internal error happened");
                 }
             }
             islogin.close();
             state.loginConnection.close();
             Log.d(TAG, "response-login:" + response);
             
             
             
             
             
             
             
 
 
             state.currentStep = "Testing your connection";
             
             
             // try to connect to the Internet again to see if it worked
             HttpURLConnection con = (HttpURLConnection) state.testURL.openConnection();
             con.setReadTimeout(timeoutms);
             con.setDoInput(true);
             con.setDoOutput(false);
             con.setRequestMethod("GET");
             state.responseCode = con.getResponseCode();
             
             if(state.responseCode == HttpURLConnection.HTTP_OK || state.responseCode == 302){
             	
                 Log.d(TAG, "SUCCESS: Logged into UMB campus network ~ Donate to Joseph Paul Cohen?");
                 return(true);
            } else {
             	
                 Log.e(TAG, "Error: Sign in Failed HTTP status code "+ state.responseCode);
                 throw new Exception("Error: Sign in Failed HTTP status code "+ state.responseCode);
             }
         } else if(state.responseCode == HttpURLConnection.HTTP_OK || state.responseCode == 302)
         {
             Log.d(TAG, "You are already connected to the Internet.");
             return(false);
         } else
         {
             Log.e(TAG, "Unknown error: HTTP status code " + state.responseCode);
             throw new Exception("Unknown error: HTTP status code " + state.responseCode);
         }
     	}
     	catch (Exception e){
     		throw new Exception("Step: \"" + state.currentStep + "\" resulted in " + e.getMessage(), e);
     	}
     }
 
     
     void testAndGetRedirect(LoginState state) throws IOException{
     	
     	// disable the automatic following of redirects
         // a 3xx response can be used to determine whether or not the computer
         // is already connected to the Internet
         HttpURLConnection.setFollowRedirects(false);
         
         // try to visit a website
         Log.d(TAG, "Attempting to visit [" + state.testURL + "]...");
         //currentStep = "Testing connection #2";
         
         Socket s = new Socket(state.testURL.getHost(), 80);
         s.setSoTimeout(timeoutms);
 
         Log.d(TAG, "Getting OUT for new Socket");
         BufferedWriter os = new BufferedWriter(new OutputStreamWriter(s.getOutputStream()));
         
         os.write("GET / HTTP/1.1\n");
         os.write("Host: google.com\n");
         os.write("User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10.6; rv:11.0) Gecko/20100101 Firefox/11.0\n");
         os.write("Accept: text/html\n\n\n");
         os.flush();
         
         
         Log.d(TAG, "Getting IN for new Socket");
         BufferedReader is = new BufferedReader(new InputStreamReader(s.getInputStream()));
         
         Log.d(TAG, "Reading IN for new Socket");
         String toparse;
         while((toparse = is.readLine()) != null){
 
         	if (toparse.contains("HTTP/1.1 307")){
         		state.responseCode = 307;
         		
         		// do we already have the location
         		if (!state.redirectUrlStr.equals("")){
         			is.close();
         			return;
         		}
         	}
         	
         	if (toparse.contains("HTTP/1.1 200")){
         		state.responseCode = HttpURLConnection.HTTP_OK;
         		is.close();
         		return;
         	}
         	
         	if (toparse.contains("Location:")){
         		state.redirectUrlStr = toparse.replace("Location:", "");
         		
         		// do we already have the response code
         		if (state.responseCode != 0){
         			is.close();
         			return;
         		}
         	}
         	
         	// thats all we care about
         }
         
         is.close();
     }
     
     String getHtmlOfLoginPage(LoginState state) throws IOException, NoSuchAlgorithmException, KeyManagementException{
     	
 		//////////////////////////
 		// fake out ssl
 		getTrustingSocketContext();
 
 		state.currentStep = "Fetching login page";
 		
 		
 		state.redirectURL = new URL(state.redirectUrlStr);
 		Log.d(TAG, "Downloading UMB login page [" + state.redirectURL + "]...");
 		HttpsURLConnection conn = (HttpsURLConnection) state.redirectURL.openConnection();
 		conn.setReadTimeout(timeoutms);
 		conn.setDoInput(true);
 		conn.setDoOutput(false);
 		conn.setRequestMethod("GET");
 		
 		
 		
 		// get the HTML of the webpage
 		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
 		String line;
 		StringBuilder html = new StringBuilder();
 		while((line = in.readLine()) != null){
 			html.append(line);
 		}
 		in.close();
 		conn.disconnect();
 		
 		return html.toString();
     }
     
     
     
     
     SSLContext getTrustingSocketContext() throws NoSuchAlgorithmException, KeyManagementException{
     	
 		//////////////////////////
 		// fake out ssl
 		TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
 		public java.security.cert.X509Certificate[] getAcceptedIssuers() {
 		return null;
 		}
 		
 		public void checkClientTrusted(X509Certificate[] certs,
 			String authType) {
 		}
 		
 		public void checkServerTrusted(X509Certificate[] certs,
 			String authType) {
 		}
 		} };
 		// Install the all-trusting trust manager
 		SSLContext sc = SSLContext.getInstance("TLS");
 		sc.init(null, trustAllCerts, new java.security.SecureRandom());
 		HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
 		// Create all-trusting host name verifier
 		HostnameVerifier allHostsValid = new HostnameVerifier() {
 		public boolean verify(String hostname, SSLSession session) {
 		return true;
 		}
 		};
 		
 		HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
 		//////////////////////////
 		
 		return sc;
     }
     
     
 }
 
 class LoginState{
 	
 	String currentStep = "Not started";
 
 	URL testURL = null;
 	int responseCode = 0;
 	
 	String redirectUrlStr = "";
 	URL redirectURL = null;
 	
 	
 	Socket firstConnection = null;
 	Socket loginConnection = null;
 }
