 package nl.digitalica.skydivekompasroos.test;
 
 import java.io.IOException;
 import java.util.HashMap;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.client.ClientProtocolException;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.protocol.BasicHttpContext;
 import org.apache.http.protocol.HttpContext;
 
 import android.net.Uri;
 
 import nl.digitalica.skydivekompasroos.Manufacturer;
 
 import junit.framework.TestCase;
 
 public class Manufacturer_test extends TestCase {
 
 	// public void testUrls() {
 	// final String NOERROR = "";
 	// HashMap<String, Manufacturer> manufacturers = Manufacturer
 	// .getManufacturerHash();
 	// StringBuilder errors = new StringBuilder();
 	// for (HashMap.Entry<String, Manufacturer> manufacturer : manufacturers
 	// .entrySet()) {
 	// if (!manufacturer.getValue().url.equals("")) {
 	// HttpClient httpClient = new DefaultHttpClient();
 	// HttpContext localContext = new BasicHttpContext();
 	// HttpGet httpGet = new HttpGet(manufacturer.getValue().url);
 	// String error = NOERROR;
 	// try {
 	// HttpResponse response = httpClient.execute(httpGet,
 	// localContext);
 	// } catch (ClientProtocolException e) {
 	// error = "ClientProtocolException: "
 	// + manufacturer.getValue().url;
 	// } catch (IOException e) {
 	// // TODO Auto-generated catch block
 	// error = "IO: " + manufacturer.getValue().url;
 	// }
 	// if (!error.equals(NOERROR))
 	// errors.append(error);
 	// }
 	// }
 	// if (errors.length() == 0)
 	// assertTrue(true);
 	// else
 	// fail(errors.toString());
 	//
 	// }
 
 	public void testCountry() {
 		// sanity check and get full names
 		String frFullName = assertFullNameForCountryCode("fr");
 		String usFullName = assertFullNameForCountryCode("us");
 
 		// check combined name
 		String usfrFullName = assertFullNameForCountryCode("us, fr");
 		assertEquals(usFullName + ", " + frFullName, usfrFullName);
 
 		// simple sanity check on rest
 		assertFullNameForCountryCode("de");
 		assertFullNameForCountryCode("sa");
 	}
 
 	private String assertFullNameForCountryCode(String code) {
 		Manufacturer m = new Manufacturer("testManufacturer", code, null, null);
 		String countryFullName = m.countryFullName();
 		assertNotNull(countryFullName);
 		assertFalse("Full name equals code for " + code,
 				countryFullName.equals(code));
 		assertFalse("Full name empty for " + code, countryFullName.equals(""));
 		return countryFullName;
 	}
 
	public void testCountryFullNameForNull() {
		Manufacturer testManufacturer = new Manufacturer("testManufacturer",
				null, null, null);
		assertNull(testManufacturer.countryFullName());
	}

 }
