 package nl.digitalica.skydivekompasroos;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.xmlpull.v1.XmlPullParserException;
 
 import android.content.Context;
 import android.content.res.Resources;
 import android.content.res.XmlResourceParser;
 import android.util.Log;
 
 public class Manufacturer {
 
 	public String name;
 	public String countryCode;
 	public String url;
 	public String remarks;
 
 	public Manufacturer(String mName, String mCountryCode, String mUrl,
 			String mRemarks) {
 		this.name = mName;
 		this.countryCode = mCountryCode;
 		this.url = mUrl;
 		this.remarks = mRemarks;
 	}
 
 	/***
 	 * Returns a hash containing all manufacturers by name
 	 * 
 	 * @return
 	 */
 	static public HashMap<String, Manufacturer> getManufacturerHash(
 			Context context) {
 		XmlResourceParser manufacturersParser = context.getResources().getXml(
 				R.xml.manufacturers);
 		int eventType = -1;
 
 		HashMap<String, Manufacturer> manufacturerHashMap = new HashMap<String, Manufacturer>();
 		while (eventType != XmlResourceParser.END_DOCUMENT) {
 			if (eventType == XmlResourceParser.START_TAG) {
 				String strName = manufacturersParser.getName();
 				if (strName.equals("manufacturer")) {
 					String manufacturerName = manufacturersParser
 							.getAttributeValue(null, "name");
 					String manufacturerUrl = manufacturersParser
 							.getAttributeValue(null, "url");
 					String manufacturerRemarks = manufacturersParser
 							.getAttributeValue(null, "remarks");
 					String manufacturerCountryCode = manufacturersParser
 							.getAttributeValue(null, "countrycode");
 					Manufacturer manufacturer = new Manufacturer(
 							manufacturerName, manufacturerCountryCode,
 							manufacturerUrl, manufacturerRemarks);
 					manufacturerHashMap.put(manufacturer.name, manufacturer);
 				}
 
 			}
 			try {
 				eventType = manufacturersParser.next();
 			} catch (XmlPullParserException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 		return manufacturerHashMap;
 
 	}
 
 	/***
 	 * Return the full name of the country. Note we allow multipe countries
 	 * codes and will translate to multiple countries this is for Icarus (us and
 	 * fr)
 	 * 
 	 * @return
 	 */
 	public String countryFullName() {
		if (this.countryCode == null)
			return null;
 		StringBuilder countries = new StringBuilder();
 		String[] countryCodes = this.countryCode.split(",");
 		for (String code : countryCodes) {
 			if (countries.length() > 0)
 				countries.append(", ");
 			countries.append(country(code));
 		}
 		return countries.toString();
 	}
 
 	/***
 	 * Return the full name of a single country
 	 * 
	 * TODO: make sure we return different language if needed (use string
	 * array?)
 	 * 
 	 * @param countryCode
 	 * @return
 	 */
 	private String country(String countryCode) {
 		String trimmedCountryCode = countryCode.trim();
 		if (trimmedCountryCode.equals("us"))
 			return "Verenigde Staten";
 		if (trimmedCountryCode.equals("sa"))
 			return "Zuid Afrika";
 		if (trimmedCountryCode.equals("de"))
 			return "Duitsland";
 		if (trimmedCountryCode.equals("fr"))
 			return "Frankrijk";
		Log.e(KompasroosBaseActivity.LOG_TAG, "Unknown country code: "
				+ countryCode);
 		return countryCode;
 	}
 
 }
