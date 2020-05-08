 package uk.gov.bcc.mobile.service;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
import java.io.OutputStreamWriter;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.Map;
 
 public class MobileApiRequestService {
 
 	public Map<String, String> getMobileApiResponse() {
 
 		return null;
 	}
 	
 	public static void main(String[] args) {
 		MobileApiRequestService re= new MobileApiRequestService();
 		re.getHttpConnection();
 	}
 
	/**
	 * This is used to communicate with the rest api.
	 * TODO-Error handling request time out and api not available. or give up after some time.
	 * @return
	 */
 	public boolean getHttpConnection() {
 		URL url = null;
 		
 		String reqeust="<loginTokenMobileRequest><userTokenId></userTokenId><applicationTokenId>DC4F01193B4</applicationTokenId><userid>mobile.test2@mailinator.com</userid><password>password1</password></loginTokenMobileRequest>";
 
 		HttpURLConnection urlConnection = null;
 		try {
 			url = new URL("http://sb0073308.addm.ads.brm.pri:8080/api/Customer/Login");
 			urlConnection = (HttpURLConnection) url.openConnection();
			urlConnection.setDoInput(true);
 			urlConnection.setDoOutput(true);
 			urlConnection.setChunkedStreamingMode(0);
 			urlConnection.setRequestMethod("POST");
 			urlConnection.setRequestProperty("Accept-Charset", "UTF-8");
 			urlConnection.setRequestProperty("Content-Type", "application/xml");
 			urlConnection.connect();
			OutputStream os = new BufferedOutputStream(urlConnection.getOutputStream());
			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os));
			writer.write(reqeust);
			
			writer.close();
			os.close();

			urlConnection.connect();
 
 			InputStream in = new BufferedInputStream(urlConnection.getInputStream());
 			System.out.println(in.toString());
 			
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} finally {
 			urlConnection.disconnect();
 		}
 		return true;
 	}
 }
