 package uk.co.tfd.sm.integration;
 
 import java.io.IOException;
 import java.net.ConnectException;
 import java.net.MalformedURLException;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class IntegrationServer {
 
 	private static final Logger LOGGER = LoggerFactory
 			.getLogger(IntegrationServer.class);
 	public static final String BASEURL = "http://localhost:8080";
 	private static boolean started = false;
 
 	public static void start() throws IOException {
 
 		if (!started) {
 			if (isStarted(0)) {
 				LOGGER.info("App already Running on port 8080");
 			} else {
				throw new IllegalStateException(
						"Please start integration test server on port 8080");
				/*
				 * Removed to prevent a cyclic dependency and build order issues.
 				File f = new File("target/integrationserver");
 				FileUtils.deleteDirectory(f.getAbsoluteFile());
 				System.setProperty("sling.home", f.getAbsolutePath());
 				NakamuraMain.main(new String[] { "-f", "target/integrationserver.log" });
 				LOGGER.info("Started App");
 				if (!isStarted(60000)) {
 				}
				*/
 			}
 			started = true;
 		}
 	}
 
 	private static boolean isStarted(int timeout) {
 		try {
 			long endTime = System.currentTimeMillis() + timeout + 1000;
 			while (System.currentTimeMillis() < endTime) {
 				try {
 					DefaultHttpClient client = new DefaultHttpClient();
 					HttpGet get = new HttpGet(BASEURL + "/system/console");
 					HttpResponse response = client.execute(get);
 					if (response.getStatusLine().getStatusCode() == 401) {
 						LOGGER.info("Server up, got 401 for admin interface");
 						return true;
 					} else {
 						LOGGER.info(
 								"Server not up, got {} for admin interface ",
 								response.getStatusLine().getStatusCode());
 						return false;
 					}
 				} catch (ConnectException e) {
 					if (System.currentTimeMillis() > endTime) {
 						return false;
 					} else {
 						LOGGER.info("Failed {} ", e.getMessage());
 						Thread.sleep(1000);
 					}
 				}
 			}
 		} catch (InterruptedException e) {
 			LOGGER.error(e.getMessage(), e);
 		} catch (MalformedURLException e) {
 			LOGGER.error(e.getMessage(), e);
 		} catch (IOException e) {
 			LOGGER.error(e.getMessage(), e);
 		} finally {
 		}
 		return false;
 	}
 
 }
