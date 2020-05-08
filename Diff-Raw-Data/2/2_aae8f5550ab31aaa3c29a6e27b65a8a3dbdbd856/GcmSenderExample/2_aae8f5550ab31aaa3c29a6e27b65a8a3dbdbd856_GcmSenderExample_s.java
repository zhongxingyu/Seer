 /*
  License:
 
  blueprint-sdk is licensed under the terms of Eclipse Public License(EPL) v1.0
  (http://www.eclipse.org/legal/epl-v10.html)
 
 
  Distribution:
 
  Repository - https://github.com/lempel/blueprint-sdk.git
  Blog - http://lempel.egloos.com
  */
 
 package blueprint.sdk.google.gcm.example;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.HashMap;
 
 import org.apache.log4j.Logger;
 
 import blueprint.sdk.google.gcm.GcmResponse;
 import blueprint.sdk.google.gcm.GcmResponseDetail;
 import blueprint.sdk.google.gcm.GcmSender;
 
 /**
  * Example of GcmSender
  * 
  * @author Sangmin Lee
  * @since 2013. 12. 3.
  */
 public class GcmSenderExample {
 	private static final Logger L = Logger.getLogger(GcmSenderExample.class);
 
 	public static void main(String[] args) throws MalformedURLException, IOException {
 		// set your apiKey
 		String apiKey = "XXX";
 		// set registration id of android clients
 		String[] regIds = { "XXX", "YYY", "ZZZ" };
 
 		HashMap<String, String> map = new HashMap<String, String>();
 		map.put("test", "Message from GcmSenderExample");
 
		GcmSender.GCM_URL = "http://localhost:8080/gcmProxy";
		// GcmSender.GCM_URL = "http://localhost:8080/gcmDummy";
 		GcmSender sender = new GcmSender(apiKey);
 		GcmResponse response = sender.send(regIds, map, 10);
 
 		if (response.code == 200) {
 			if (response.results.size() == regIds.length) {
 				for (int i = 0; i < regIds.length; i++) {
 					GcmResponseDetail result = response.results.get(i);
 
 					if (result.success) {
 						L.info("\tredId = " + regIds[i] + ", message id = " + result.message);
 					} else {
 						L.info("\tredId = " + regIds[i] + ", error message = " + result.message);
 					}
 				}
 			} else {
 				L.error("wrong response. number of reg ids are mismatched.");
 			}
 		} else {
 			L.error("response code = " + response.code);
 		}
 	}
 }
