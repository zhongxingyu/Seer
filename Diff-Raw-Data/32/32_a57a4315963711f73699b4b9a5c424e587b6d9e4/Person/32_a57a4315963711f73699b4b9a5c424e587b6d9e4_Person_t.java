 package ch.almana.android.unibas.perssearch.model;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.text.TextUtils;
 import ch.almana.android.unibas.perssearch.helper.Logger;
 
 public class Person {
 
 	public static final Person EMPTY = new Person();
 
 	public static final String NOT_GIVEN = "not given";
 
 	public static final String KEY_NAME = "displayname";
 	public static final String KEY_FAMILY_NAME = "sn";
 	public static final String KEY_GIVEN_NAME = "givenname";
 	public static final String KEY_MAIL_WORK = "mail";
 	public static final String KEY_MAIL_HOME = "unibaschhomemail";
 	public static final String KEY_MAIL_INSTITUTE = "mailalternateaddress";
 	public static final String KEY_PHONE_WORK = "telephonenumber";
 	public static final String KEY_FAX_WORK = "facsimiletelephonenumber";
 	public static final String KEY_FAX_HOME = "unibaschhomefax";
 	public static final String KEY_PHONE_HOME = "homephone";
 	public static final String KEY_PHONE_MOBILE = "unibaschhomemobile";
 	public static final String KEY_ADDRESS_WORK = "postaladdress";
 	public static final String KEY_ADDRESS_HOME = "homepostaladdress";
 	public static final String KEY_STUDENT_TYPE = "edupersonaffiliation";
 	public static final String KEY_WEBPAGE = "labeleduri";
 
 	public static enum FieldTypes {
 		MAIL_WORK, MAIL_HOME, MAIL_INSTITUTE, PHONE_WORK, PHONE_MOBILE, PHONE_HOME, FAX_WORK, FAX_HOME, ADDRESS_WORK, ADDRESS_HOME, WEBPAGE
 	};
 
 	private static final String ADDRESS_SEPARATOR = "$";
 
 
 	private JSONObject jsonObject;
 
 	private String jsonString;
 
 	private String streetCityWork;
 
 	private String phoneWork;
 
 	private String phoneHome;
 
 	private String phoneMobile;
 
 	public Person(String jsonString) {
 		super();
 		try {
 			this.jsonObject = new JSONObject(jsonString);
 		} catch (JSONException e) {
 			jsonObject = new JSONObject();
 			Logger.e("Cannot initalise person from sting " + jsonString, e);
 		}
 		this.jsonString = jsonString;
 	}
 
 	private Person() {
 		try {
 			this.jsonObject = new JSONObject("{ \"displayname\":\"Not found\"}");
 		} catch (JSONException e) {
 			Logger.e("Cannot initalise empty person", e);
 		}
 	}
 
 	private String getJsonEntry(String keyName) {
 		try {
 			return jsonObject.getString(keyName);
 		} catch (JSONException e) {
 			return NOT_GIVEN;
 		}
 	}
 
 	private String getJsonEntryPhone(String keyName) {
 		try {
 			String d = jsonObject.getString(keyName);
 			int idx = d.lastIndexOf("E");
 			if (idx > -1) {
 				StringBuilder s = new StringBuilder();
 				s.append(d.substring(0, 1));
 				s.append(d.substring(2, d.lastIndexOf("E")));
 				d = s.toString();
 			}
 			idx = d.indexOf(" ");
 			if (idx == -1) {
 				int lenght = d.length();
 				StringBuilder s = new StringBuilder(d);
 				if (lenght == 11) {
 					s.insert(2, " ").insert(5, " ").insert(9, " ").insert(12, " ");
 					s.insert(0, "+");
 				} else if (lenght == 9) {
 					s.insert(2, " ").insert(6, " ").insert(9, " ");
 					s.insert(0, "0");
 				}
 				d = s.toString();
 			}
 			return d;
 		} catch (Exception e) {
 			return NOT_GIVEN;
 		}
 	}
 
 	public String getName() {
 		return getJsonEntry(KEY_NAME);
 	}
 
 	public String getFamilyName() {
 		return getJsonEntry(KEY_FAMILY_NAME);
 	}
 
 	public String getGivenName() {
 		return getJsonEntry(KEY_GIVEN_NAME);
 	}
 
 	public String getEmailWork() {
 		return getJsonEntry(KEY_MAIL_WORK);
 	}
 	public String getEmailHome() {
 		return getJsonEntry(KEY_MAIL_HOME);
 	}
 
 	public String getEmailInstitute() {
 		return getJsonEntry(KEY_MAIL_INSTITUTE);
 	}
 
 	public String getPhoneWork() {
 		if (phoneWork == null) {
 			phoneWork = getJsonEntryPhone(KEY_PHONE_WORK);
 		}
 		return phoneWork;
 	}
 
 	public String getPhoneHome() {
 		if (phoneHome == null) {
 			phoneHome = getJsonEntryPhone(KEY_PHONE_HOME);
 		}
 		return phoneHome;
 	}
 
 	public String getPhoneMobile() {
 		if (phoneMobile == null) {
 			phoneMobile = getJsonEntryPhone(KEY_PHONE_MOBILE);
 		}
 		return phoneMobile;
 	}
 
 	public String getAddressWork() {
 		return formatAddress(KEY_ADDRESS_WORK);
 	}
 
 	private String formatAddress(String keyAddressWork) {
 		String adr = getJsonEntry(keyAddressWork);
 		if (adr != null) {
 			adr = adr.replace(ADDRESS_SEPARATOR, "\n");
 		}
 		return adr;
 	}
 
 	public String getAddressHome() {
 		return formatAddress(KEY_ADDRESS_HOME);
 	}
 
 	public String getStudentType() {
 		return getJsonEntry(KEY_STUDENT_TYPE);
 	}
 
 	public String getWebpage() {
 		return getJsonEntry(KEY_WEBPAGE);
 	}
 
 	public String[] getValuesAarry() {
 		String ID = "1";// getId();
 		String intentData = jsonString;
 		return new String[] { ID, getName(), getDesciption(), intentData, };
 	}
 
 	public String getDesciption() {
 		String description = getStudentType();
 		if (description == NOT_GIVEN || TextUtils.isEmpty(description)) {
 			String adr = getJsonEntry(KEY_ADDRESS_WORK);
 			int idx = adr.indexOf(ADDRESS_SEPARATOR);
 			if (idx > 0) {
 				description = adr.substring(0, idx);
 			}
 		}
 		if (description == NOT_GIVEN || TextUtils.isEmpty(description)) {
 			description = getEmailWork();
 		}
 		return description;
 	}
 
 	public String getId() {
 		return Integer.toString(jsonString.hashCode());
 	}
 
 	public String getJsonString() {
 		return jsonString;
 	}
 
 	@Override
 	public String toString() {
 		StringBuilder sb = new StringBuilder();
 		JSONArray keys = jsonObject.names();
 		for (int i = 0; i < keys.length(); i++) {
 			try {
 				String key = keys.getString(i);
 				sb.append(key).append(" -> ").append(getJsonEntry(key)).append("\n");
 			} catch (JSONException e) {
 				sb.append(e.getMessage());
 				Logger.w("Error unmarshalling json", e);
 			}
 		}
 		return sb.toString();
 	}
 
 	public List<FieldTypes> getNonblankFields() {
 		ArrayList<FieldTypes> fields = new ArrayList<FieldTypes>();
 		if (hasField(getEmailWork())) {
 			fields.add(FieldTypes.MAIL_WORK);
 		}
 		if (hasField(getEmailHome())) {
 			fields.add(FieldTypes.MAIL_HOME);
 		}
 		if (hasField(getEmailInstitute())) {
 			fields.add(FieldTypes.MAIL_INSTITUTE);
 		}
 		if (hasField(getPhoneWork())) {
 			fields.add(FieldTypes.PHONE_WORK);
 		}
 		if (hasField(getPhoneMobile())) {
 			fields.add(FieldTypes.PHONE_MOBILE);
 		}
 		if (hasField(getPhoneHome())) {
 			fields.add(FieldTypes.PHONE_HOME);
 		}
 		if (hasField(getFaxWork())) {
 			fields.add(FieldTypes.FAX_WORK);
 		}
 		if (hasField(getFaxHome())) {
 			fields.add(FieldTypes.FAX_HOME);
 		}
 		if (hasField(getAddressWork())) {
 			fields.add(FieldTypes.ADDRESS_WORK);
 		}
 		if (hasField(getAddressHome())) {
 			fields.add(FieldTypes.ADDRESS_HOME);
 		}
 		if (hasField(getWebpage())) {
 			fields.add(FieldTypes.WEBPAGE);
 		}
 		return fields;
 	}
 
 	public static boolean hasField(String value) {
 		return value != null && !TextUtils.isEmpty(value.trim()) && !value.equals(Person.NOT_GIVEN);
 	}
 
 	public String getFaxWork() {
 		return getJsonEntry(KEY_FAX_WORK);
 	}
 
 	public String getFaxHome() {
 		return getJsonEntry(KEY_FAX_HOME);
 	}
 
 	public String getStreetCityWork() {
 		if (streetCityWork == null) {
 			String a = getAddressWork();
 			int idx = a.lastIndexOf("\n", a.lastIndexOf("\n") - 1);
 			streetCityWork = a.substring(idx + 1, a.length());
 		}
 		return streetCityWork;
 	}
 
 	public int getUserId() {
 		// FIXME do something clever
 		return getEmailWork().hashCode();
 	}
 
 	public String getVcard() {
 		StringBuilder sb = new StringBuilder();
 		sb.append("BEGIN:VCARD\n");
 		sb.append("VERSION:3.0\n");
 		sb.append("N:");
 		String getFamilyName = getFamilyName();
 		if (!NOT_GIVEN.equals(getFamilyName)) {
 			sb.append(getFamilyName);
 		}
 		sb.append(";");
 		String givenName = getGivenName();
 		if (!NOT_GIVEN.equals(givenName)) {
 			sb.append(givenName);
 		}
 		sb.append(";;;\n");
 		addVcardLine(sb, "EMAIL", "WORK", getEmailWork());
 		addVcardLine(sb, "EMAIL", "HOME", getEmailHome());
 		addVcardLine(sb, "EMAIL", "Institute", getEmailWork());
 		addVcardLine(sb, "ADR", "WORK", getAddressWork());
 		addVcardLine(sb, "ADR", "HOME", getAddressHome());
 		addVcardLine(sb, "TEL", "WORK;TYPE=FAX", getFaxWork());
 		addVcardLine(sb, "TEL", "HOME;TYPE=FAX", getFaxHome());
 		addVcardLine(sb, "TEL", "WORK", getPhoneWork());
 		addVcardLine(sb, "TEL", "HOME", getPhoneHome());
 		addVcardLine(sb, "TEL", "CELL", getPhoneMobile());
 		addVcardLine(sb, "URL", null, getWebpage());
 		sb.append("END:VCARD\n");
 		return sb.toString();
 	}
 
 	private void addVcardLine(StringBuilder sb, String kind, String type, String value) {
 		if (value != null && !NOT_GIVEN.equals(value)) {
 			sb.append(kind);
 			if (type != null) {
 				sb.append(";TYPE=").append(type);
 			}
 			if ("ADR".equals(kind)) {
				value = value.replaceAll("\n", ";");
 			}
			sb.append(":").append(value);
 			sb.append("\n");
 		}
 	}
 
 }
