 package nl.digitalica.skydivekompasroos.test;
 
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.UUID;
 
 import nl.digitalica.skydivekompasroos.CanopyType;
 import nl.digitalica.skydivekompasroos.Manufacturer;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import android.test.AndroidTestCase;
 
 /**
  * Checks URL's in the kompasroos app.
  * 
  * This only works if there is an INTERNET permission in the main app file.
  * 
  * @author robbert
  * 
  */
 public class UrlChecker extends AndroidTestCase {
 
 	/**
 	 * Check all URLs in the canopy list
 	 * 
 	 * not named test... to avoid automatic running
 	 */
 	public void xtestCanopyUrls() {
 		List<CanopyType> canopies = CanopyType.getAllCanopyTypesInList(getContext());
 		StringBuilder errors = new StringBuilder();
 		for (CanopyType c : canopies) {
 			errors.append(checkValidUrl(c.name, c.url));
 			errors.append(checkValidUrl(c.name, c.dropZoneUrl()));
 		}
 		assertTrue(errors.toString(), errors.length() == 0);
 	}
 
 	/**
 	 * Check all URLs in the manufacturers list
 	 * 
 	 * not named test... to avoid automatic running
 	 */
 	public void xtestManufacturerUrls() {
 		HashMap<UUID, Manufacturer> manufacturers = Manufacturer
 				.getManufacturerHash(getContext());
 		StringBuilder errors = new StringBuilder();
 		for (Manufacturer m : manufacturers.values()) {
 			errors.append(checkValidUrl(m.name, m.url));
 		}
 		assertTrue(errors.toString(), errors.length() == 0);
 	}
 
 	/**
 	 * Checks an URL is valid: it should give a response
 	 * 
 	 * @param name
 	 * @param url
 	 */
 	protected String checkValidUrl(String name, String url) {
 		String error = "";
 		HttpClient httpClient = new DefaultHttpClient();
 		HttpResponse httpResponse;
 		if (url != null && !url.equals("")) {
 			HttpGet httpGet = new HttpGet(url);
 
 			String hostname = httpGet.getURI().getHost();
 			try {
 				InetAddress.getByName(hostname);
 			} catch (UnknownHostException e1) {
 				error = "DNS error " + hostname + ": ";
 				e1.printStackTrace();
 			}
 			if (error.equals(""))
 				try {
 					httpResponse = httpClient.execute(httpGet);
 					int status = httpResponse.getStatusLine().getStatusCode();
					if (status != 200)
 						error = "Page not found or moved ("
 								+ Integer.toString(status) + ") ";
 				} catch (Exception e) {
 					error = "Network error ";
 					e.printStackTrace();
 				}
 		}
 		if (!error.equals(""))
 			error += " in url for " + name + " " + url + "\n";
 		return error;
 	}
}
