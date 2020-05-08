 package ee.phon.ioc.awebrec.client;
 
 import java.io.BufferedReader;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.Map;
 import java.util.Properties;
 
 public class AWebRecSession implements RecSession {
 
 	public static final String CONF_BASE_URL = "base_url";
 	
 	public static final long TIME_BETWEEN_RESULT_POLLING = 100;
 	
 	private String sessionId;
 	
 	private boolean finished = false;
 	
 	private String currentResult = "";
 	
 	private long lastResultQueryTime = 0;
 	
 	private Properties configuration = new Properties();
 
 	
	public void AWebRecSession()  {
 		// set default base URL
 		configuration.setProperty(CONF_BASE_URL, "http://localhost:4567/recognizer");
 	}
 	
 	@Override
 	public synchronized void create() throws IOException, NotAvailableException {
 		Map<String, String> resultMap = makeHttpRequest("POST", "", null);
 		sessionId = resultMap.get("recognizer_session.id");
 		if (sessionId == null) {
 			throw new NotAvailableException();
 		}
 	}
 	
 	@Override
 	public synchronized String getCurrentResult() throws IOException {
 		if (finished) {
 			return currentResult;
 		} else {
 			long time = System.currentTimeMillis();
 			if (time - lastResultQueryTime > TIME_BETWEEN_RESULT_POLLING) {
 				Map<String, String> resultMap = makeHttpRequest("GET", "/" + sessionId, null);
 				processResponse(resultMap);
 			}
 			return currentResult;
 		}
 	}
 
 	private void processResponse(Map<String, String> resultMap) {
 		String errorMessage = resultMap.get("error.message");
 		if (errorMessage != null) {
 			// FIXME
 			throw new RuntimeException(errorMessage);
 		}
 		currentResult = resultMap.get("recognizer_session.result");
 		String finalStr = resultMap.get("recognizer_session.final_result_created_at");
 		finished = finalStr != null;
 		lastResultQueryTime = System.currentTimeMillis();
 	}
 
 	private Map<String, String> makeHttpRequest(String method, String suffix, byte[] body) throws IOException {
		 String urlStr = "http://localhost:4567/recognizer" + suffix;
 		 System.out.println(method + " to " + urlStr);
 		 
 		 URL url = new URL(urlStr);
 		 HttpURLConnection connection =  (HttpURLConnection) url.openConnection();
 		 connection.setRequestMethod(method);
 		 if (body != null) {
 			 connection.setDoOutput(true);
 		 }
 		 connection.setDoInput(true);
 		 connection.connect();
 		 if (body != null) {
 			 OutputStream wr = new DataOutputStream(connection.getOutputStream());
 			  wr.write(body);
 		      wr.flush();
 		      wr.close();
 		 }
 		 InputStream is = connection.getInputStream();
 
 		 Map<String, String> result = XMLUtils.load(new InputStreamReader(is));
 		 System.out.println(result);
 		 return result;
 	}
 
 	@Override
 	public synchronized boolean isFinished() {
 		return finished;
 	}
 
 	@Override
 	public synchronized void sendChunk(byte[] bytes, boolean isLast) throws IOException {
 		Map<String, String> resultMap = makeHttpRequest("PUT", "/" + sessionId, bytes);
 		processResponse(resultMap);
 		if (isLast) {
 			resultMap = makeHttpRequest("PUT", "/" + sessionId + "/end", null);
 			processResponse(resultMap);
 		}
 	}
 
 	public Properties getConfiguration() {
 		return configuration;
 	}
 
 	public void setConfiguration(Properties configuration) {
 		this.configuration = configuration;
 	}
 



 }
