 /*
  License:
 
  blueprint-sdk is licensed under the terms of Eclipse Public License(EPL) v1.0
  (http://www.eclipse.org/legal/epl-v10.html)
 
 
  Distribution:
 
  Repository - https://github.com/lempel/blueprint-sdk.git
  Blog - http://lempel.egloos.com
  */
 
 package blueprint.sdk.google.gcm;
 
 import java.io.IOException;
 import java.io.OutputStream;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * Sends push message to GCM.
  * 
  * @author Sangmin Lee
  * @since 2013. 12. 3.
  */
 public class GcmSender {
 	public static String GCM_URL = "https://android.googleapis.com/gcm/send";
 
 	protected String apiKey;
 
 	/**
 	 * @param apiKey
 	 *            API key for GCM
 	 */
 	public GcmSender(String apiKey) {
 		this.apiKey = apiKey;
 	}
 
 	/**
 	 * Send to GCM
 	 * 
 	 * @param json
 	 *            JSON message to send
 	 * @param retries
 	 *            Number of retry attempts. Can be 0.
 	 * @return
 	 * @throws MalformedURLException
 	 *             Wrong GCM_URL value
 	 * @throws IOException
 	 *             I/O error with GCM_URL
 	 */
 	public GcmResponse send(String json, int retries) throws MalformedURLException, IOException {
 		GcmResponse result = send(json);
 
 		int interval = 1;
 		while (result.needRetry()) {
 			try {
 				Thread.sleep(interval);
 			} catch (InterruptedException ignored) {
 			}
 
 			result = send(json);
 
 			// simplified exponential back-off
 			interval *= 2;
 		}
 
 		return result;
 	}
 
 	private GcmResponse send(String json) throws IOException, MalformedURLException {
 		HttpURLConnection http = (HttpURLConnection) new URL(GCM_URL).openConnection();
		http.setRequestMethod("POST");
 		http.addRequestProperty("Authorization", "key=" + apiKey);
 		http.addRequestProperty("Content-Type", "application/json");
 
 		http.setDoOutput(true);
 		OutputStream os = http.getOutputStream();
 		os.write(json.getBytes());
 		os.close();
 
 		http.connect();
 
 		GcmResponse result = new GcmResponse();
 		result.code = http.getResponseCode();
 		result.message = http.getResponseMessage();
 
 		return result;
 	}
 
 	/**
 	 * Send to GCM
 	 * 
 	 * @param regId
 	 *            client's registration id
 	 * @param data
 	 *            data to send
 	 * @param retries
 	 *            Number of retry attempts. Can be 0.
 	 * @return
 	 * @throws MalformedURLException
 	 *             Wrong GCM_URL value
 	 * @throws IOException
 	 *             I/O error with GCM_URL
 	 */
 	public GcmResponse send(String regId, Map<String, String> data, int retries) throws MalformedURLException,
 			IOException {
 		StringBuilder builder = new StringBuilder();
 		builder.append("{\"registration_ids\":[\"").append(regId).append("\"],\"data\":{");
 
 		Set<String> keySet = data.keySet();
 		int count = 0;
 		for (String key : keySet) {
 			builder.append("\"").append(key).append("\":\"").append(data.get(key)).append("\"");
 			if (count++ != data.size()) {
 				builder.append(",");
 			}
 		}
 
 		builder.append("}}");
 
 		return send(builder.toString(), retries);
 	}
 }
