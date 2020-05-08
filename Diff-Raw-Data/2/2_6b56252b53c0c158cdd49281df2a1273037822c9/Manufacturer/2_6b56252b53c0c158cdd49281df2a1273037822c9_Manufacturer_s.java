 package nl.digitalica.skydivekompasroos;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.UUID;
 
 import org.xmlpull.v1.XmlPullParserException;
 
 import android.content.Context;
 import android.content.res.Resources;
 import android.content.res.XmlResourceParser;
 import android.util.Log;
 
 public class Manufacturer {
 
 	final public static String EVERYOTHERMANUFACTURERIDSTRING = "FC2ACF4C-3E94-4401-92B3-70D41C20546B";
 
 	public UUID id;
 	public String name;
 	public String countryCode;
 	public String url;
 	private String remarks;
 	private String remarks_nl;
 
 	public Manufacturer(UUID mId, String mName, String mCountryCode,
 			String mUrl, String mRemarks, String mRemarks_nl) {
 		this.id = mId;
 		this.name = mName;
 		this.countryCode = mCountryCode;
 		this.url = mUrl;
 		this.remarks = mRemarks;
 		this.remarks_nl = mRemarks_nl;
 	}
 
 	public Manufacturer(String mName, String mCountryCode) {
 		this(UUID.randomUUID(), mName, mCountryCode, null, null, null);
 	}
 
 	/***
 	 * Returns a hash containing all manufacturers by name
 	 * 
 	 * @return
 	 */
 	static public HashMap<UUID, Manufacturer> getManufacturerHash(
 			Context context) {
 		XmlResourceParser manufacturersParser = context.getResources().getXml(
 				R.xml.manufacturers);
 		int eventType = -1;
 
 		HashMap<UUID, Manufacturer> manufacturerHashMap = new HashMap<UUID, Manufacturer>();
 		while (eventType != XmlResourceParser.END_DOCUMENT) {
 			if (eventType == XmlResourceParser.START_TAG) {
 				String strName = manufacturersParser.getName();
 				if (strName.equals("manufacturer")) {
 					String manufacturerIdString = manufacturersParser
 							.getAttributeValue(null, "id");
 					UUID manufacturerId = UUID.fromString(manufacturerIdString);
 					String manufacturerName = manufacturersParser
 							.getAttributeValue(null, "name");
 					String manufacturerUrl = manufacturersParser
 							.getAttributeValue(null, "url");
 					String manufacturerRemarks = manufacturersParser
 							.getAttributeValue(null, "remarks");
 					String manufacturerRemarks_nl = manufacturersParser
 							.getAttributeValue(null, "remarks_nl");
 					String manufacturerCountryCode = manufacturersParser
 							.getAttributeValue(null, "countrycode");
 					Manufacturer manufacturer = new Manufacturer(
 							manufacturerId, manufacturerName,
 							manufacturerCountryCode, manufacturerUrl,
 							manufacturerRemarks, manufacturerRemarks_nl);
 					manufacturerHashMap.put(manufacturer.id, manufacturer);
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
 
 		// add every other manufacturer
 		Manufacturer eom = everyOtherManufacturer();
 		manufacturerHashMap.put(eom.id, eom);
 
 		// return the result
 		return manufacturerHashMap;
 	}
 
 	/***
 	 * Return the full name of the country. Note we allow multipe countries
 	 * codes and will translate to multiple countries this is for Icarus (nz and
 	 * es)
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
 	 * Return the full name of a single country, in the current language
 	 * 
 	 * TODO: make sure we return different language if needed (use string
 	 * array?)
 	 * 
 	 * @param countryCode
 	 * @return
 	 */
 	private String country(String countryCode) {
 		String trimmedCountryCode = countryCode.trim();
 		boolean dutch = Calculation.isLanguageDutch();
 		if (trimmedCountryCode.equals("us"))
 			return dutch ? "Verenigde Staten" : "United States";
 		if (trimmedCountryCode.equals("sa"))
 			return dutch ? "Zuid Afrika" : "South Africa";
 		if (trimmedCountryCode.equals("de"))
 			return dutch ? "Duitsland" : "Germany";
 		if (trimmedCountryCode.equals("fr"))
 			return dutch ? "Frankrijk" : "France";
 		if (trimmedCountryCode.equals("nz"))
			return dutch ? "Nieuw Zeeland" : "New Sealand";
 		if (trimmedCountryCode.equals("es"))
 			return dutch ? "Spanje" : "Spain";
 		Log.e(KompasroosBaseActivity.LOG_TAG, "Unknown country code: "
 				+ countryCode);
 		return countryCode;
 	}
 
 	/***
 	 * Return remarks in current locale
 	 * 
 	 * @return
 	 */
 	public String remarks() {
 		boolean dutch = Calculation.isLanguageDutch();
 		return remarks(dutch);
 	}
 
 	/***
 	 * Return remarks in Dutch or English
 	 * 
 	 * @param inDutch
 	 * @return
 	 */
 	public String remarks(boolean inDutch) {
 		return inDutch ? this.remarks_nl : this.remarks;
 	}
 
 	/**
 	 * Returns the ID of the special 'catch all' manufacturer
 	 * 
 	 * @return
 	 */
 	public static UUID everyOtherManufactuerId() {
 		return UUID.fromString(EVERYOTHERMANUFACTURERIDSTRING);
 	}
 
 	/**
 	 * Returns the special 'catch all' manufacturer for 'every other
 	 * manufacturer'
 	 * 
 	 * @return
 	 */
 	public static Manufacturer everyOtherManufacturer() {
 		String name;
 		if (Calculation.isLanguageDutch())
 			name = "Elke andere fabrikant";
 		else
 			name = "Every other manufacturer";
 		return new Manufacturer(everyOtherManufactuerId(), name, null, null,
 				null, null);
 	}
 
 }
