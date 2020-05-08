 package com.josephblough.sbt.criteria;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.os.Parcel;
 import android.os.Parcelable;
 import android.util.Log;
 
 public class AwardsSearchCriteria implements Parcelable {
 
     private final static String TAG = "AwardsSearchCriteria";
     
     private final static String SEARCHES_JSON_ARRAY = "searches";
     private final static String NAME_JSON_ELEMENT = "name";
     private final static String DOWLOAD_ALL_JSON_ELEMENT = "download_all";
     private final static String SEARCH_TERM_JSON_ELEMENT = "search_term";
     private final static String AGENCY_JSON_ELEMENT = "agency";
     private final static String COMPANY_JSON_ELEMENT = "company";
     private final static String INSTITUTION_JSON_ELEMENT = "institution";
     private final static String YEAR_JSON_ELEMENT = "year";
     
     public boolean downloadAll;
     public String searchTerm;
     public String agency;
     public String company;
     public String institution;
     public int year;
     
     
     public AwardsSearchCriteria(boolean downloadAll, String searchTerm, String agency, String company, String institution, int year) {
 	this.downloadAll = downloadAll;
 	this.searchTerm = (searchTerm == null) ? null : searchTerm.trim();
 	this.agency = (agency == null) ? null : agency.trim();
 	this.company = (company == null) ? null : company.trim();
 	this.institution = (institution == null) ? null : institution.trim();
 	this.year = year;
     }
     
     public int describeContents() {
 	return 0;
     }
 
     public void writeToParcel(Parcel dest, int flags) {
 	dest.writeInt(downloadAll ? 1 : 0);
 	dest.writeString(searchTerm == null ? "" : searchTerm);
 	dest.writeString(agency == null ? "" : agency);
 	dest.writeString(company == null ? "" : company);
 	dest.writeString(institution == null ? "" : institution);
 	dest.writeInt(year);
     }
 
     public static final Parcelable.Creator<AwardsSearchCriteria> CREATOR = new Parcelable.Creator<AwardsSearchCriteria>() {
 	public AwardsSearchCriteria createFromParcel(Parcel in) {
 	    return new AwardsSearchCriteria(in);
 	}
 	public AwardsSearchCriteria[] newArray(int size) {
             return new AwardsSearchCriteria[size];
         }
     };
     
     private AwardsSearchCriteria(Parcel in) {
 	downloadAll = in.readInt() == 1;
 	searchTerm = in.readString();
 	agency = in.readString();
 	company = in.readString();
 	institution = in.readString();
 	year = in.readInt();
     }
     
     public static Map<String, AwardsSearchCriteria> convertFromJson(final String jsonString) {
 	Map<String, AwardsSearchCriteria> searches = new HashMap<String, AwardsSearchCriteria>();
 	
 	if (jsonString != null && !"".equals(jsonString)) {
 	    try {
 		JSONObject json = new JSONObject(jsonString);
 		JSONArray jsonSearches = json.optJSONArray(SEARCHES_JSON_ARRAY);
 		if (jsonSearches != null) {
 		    int length = jsonSearches.length();
 		    for (int i=0; i<length; i++) {
 			JSONObject jsonSearch = jsonSearches.getJSONObject(i);
			String name = json.getString(NAME_JSON_ELEMENT);
 			AwardsSearchCriteria search = new AwardsSearchCriteria(jsonSearch);
 			searches.put(name, search);
 		    }
 		}
 	    }
 	    catch (JSONException e) {
 		Log.e(TAG, e.getMessage(), e);
 	    }
 	}
 
 	return searches;
     }
     
     public static String convertToJson(final Map<String, AwardsSearchCriteria> criteria) {
 	JSONObject json = new JSONObject();
 	try {
 	    JSONArray jsonSearches = new JSONArray();
 	    for (Entry<String, AwardsSearchCriteria> entry : criteria.entrySet()) {
 		AwardsSearchCriteria search = entry.getValue();
 		JSONObject jsonSearch = search.toJson();
 		jsonSearch.put(NAME_JSON_ELEMENT, entry.getKey());
 
 		jsonSearches.put(jsonSearch);
 	    }
 	    json.put(SEARCHES_JSON_ARRAY, jsonSearches);
 	}
 	catch (JSONException e) {
 	    Log.e(TAG, e.getMessage(), e);
 	}
 	return json.toString();
     }
     
     public AwardsSearchCriteria(final String jsonString) throws JSONException {
 	this(new JSONObject(jsonString));
     }
     
     public AwardsSearchCriteria(final JSONObject json) {
 	try {
 	    downloadAll = json.getBoolean(DOWLOAD_ALL_JSON_ELEMENT);
 	    searchTerm = json.getString(SEARCH_TERM_JSON_ELEMENT);
 	    agency = json.getString(AGENCY_JSON_ELEMENT);
 	    company = json.getString(COMPANY_JSON_ELEMENT);
 	    institution = json.getString(INSTITUTION_JSON_ELEMENT);
 	    year = json.optInt(YEAR_JSON_ELEMENT, 0);
 	}
 	catch (JSONException e) {
 	    Log.e(TAG, e.getMessage(), e);
 	}
     }
     
     public JSONObject toJson() {
 	JSONObject json = new JSONObject();
 	try {
 	    json.put(DOWLOAD_ALL_JSON_ELEMENT, downloadAll);
 	    json.put(SEARCH_TERM_JSON_ELEMENT, searchTerm);
 	    json.put(AGENCY_JSON_ELEMENT, agency);
 	    json.put(COMPANY_JSON_ELEMENT, company);
 	    json.put(INSTITUTION_JSON_ELEMENT, institution);
 	    json.put(YEAR_JSON_ELEMENT, year);
 	}
 	catch (JSONException e) {
 	    Log.e(TAG, e.getMessage(), e);
 	}
 	return json;
     }
 }
