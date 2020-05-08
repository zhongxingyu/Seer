 package com.openaussearchdroid;
 
 import java.io.IOException;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import android.graphics.Bitmap;
 
 public class RepSearch
 {
 	private final String url;
 	private JSONObject resultJson;
 	private String resultRaw;
 	private String memData;
 	private String fullName;
 	private String dateEntered;
 	private String party;
 	private String personID;
 	private String imgLoc;
 	private Bitmap repImage;
 
 	private String searchKey = "";
 	private String baseUrlPath = "http://www.openaustralia.org";
 
 	public RepSearch(String url)
 	{
 		this.url = url;
 	}
 
 	public RepSearch(String url, String searchKey)
 	{
 		this.url = url;
 		this.searchKey = searchKey;
 	}
 	public RepSearch(String url, String searchKey, String baseUrlPath)
 	{
 		this.url = url;
 		this.searchKey = searchKey;
 		this.baseUrlPath = baseUrlPath;
 	}
 
 	public void fetchSearchResult() throws IOException
 	{
 		this.resultRaw = Utilities.getDataFromUrl(this.url, "url");
 	}
 	public void setJsonResultFromRawResult() throws JSONException
 	{
 		this.resultJson = new JSONObject(this.resultRaw);
 	}
 	public void fetchSearchResultAndSetJsonResult() throws IOException, JSONException
 	{
 		fetchSearchResult();
 		setJsonResultFromRawResult();
 	}
 
 	public void setMemdata()
 	{
 		this.memData = this.fullName + dateEntered + party;
 	}
 
 	public void setFullName() throws JSONException
 	{
 		this.fullName = "Name: " + this.resultJson.getString("full_name") + "\n";
 	}
 
 	public void setDateEntered() throws JSONException
 	{
 		this.dateEntered = "Date Elected: " + this.resultJson.getString("entered_house") + "\n";
 	}
 
 	public void setParty() throws JSONException
 	{
 		this.party = "Party: " + this.resultJson.getString("party") + "\n";
 	}
 
 	public void setPersonID() throws JSONException
 	{
 		this.personID = this.resultJson.getString("person_id");
 	}
 
 	public void setImgLoc() throws JSONException
 	{
 		this.imgLoc = this.baseUrlPath + this.resultJson.getString("image");
 	}
 
 	public void fetchAndSetRepImage() throws IOException
 	{
 		this.repImage = Utilities.fetchImage(this.imgLoc);
 	}
 
 	public void setPwnieImageUrl()
 	{
		this.imgLoc = "http://d1b.org/other/pub/tests/android/open_aus_search/pwnie.png";
 	}
 
 	public void setFromJsonResultMemData() throws JSONException
 	{
 		this.setPersonID();
 		this.setFullName();
 		this.setDateEntered();
 		this.setParty();
 		this.setMemdata();
 	}
 
 	public String getResultRaw()
 	{
 		return this.resultRaw;
 	}
 	public JSONObject getResultJson()
 	{
 		return this.resultJson;
 	}
 	public Bitmap getRepImage()
 	{
 		return this.repImage;
 	}
 	public String getFullName()
 	{
 		return this.fullName;
 	}
 	public String getImgLoc()
 	{
 		return this.imgLoc;
 	}
 	public String getDateEntered()
 	{
 		return this.dateEntered;
 	}
 	public String getPersonID()
 	{
 		return this.personID;
 	}
 	public String getParty()
 	{
 		return this.party;
 	}
 	public String getMemData()
 	{
 		return this.memData;
 	}
 	public String getSearchKey()
 	{
 		return this.searchKey;
 	}
 }
