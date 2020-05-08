 package com.github.norwae.whatiread.data;
 
 import java.io.Serializable;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.json.JSONArray;
 import org.json.JSONException;
 import org.json.JSONObject;
 
 public class BookInfo implements Serializable {
 
 	private static final long serialVersionUID = 2317828706327659235L;
 
 	private String title;
 	private String subtitle;
 	private String series;
 	private String description;
 	private String publisher;
 
 	private URI thumbnail;
 	private URI thumbnailSmall;
 
 	private int pageCount;
 
 	private boolean addition;
 
 	private List<String> authors;
 	private String comment;
 
 	private String isbn;
 
 	public static List<BookInfo> parseJSONMulti(String jsonString)
 			throws JSONException, URISyntaxException {
 		List<BookInfo> infos = new ArrayList<BookInfo>();
 		JSONObject json = new JSONObject(jsonString);
 		JSONArray items = json.getJSONArray("items");
 
 		if (items != null) {
 			for (int i = 0; i < items.length(); i++) {
 				BookInfo info = new BookInfo();
 
 				info.parseJSON(items.getJSONObject(i));
 				
 				infos.add(info);
 			}
 		}
 		return infos;
 	}
 
 	public void parseJSON(String jsonString) throws JSONException,
 			URISyntaxException {
 		JSONObject json = new JSONObject(jsonString);
 
 		parseJSON(json);
 	}
 	
 
 	public String quoteJSON() throws JSONException {
 		JSONObject json = new JSONObject();
 		json.putOpt("comment", comment);
 		json.putOpt("isbn", isbn);
 		
 		JSONObject volume = new JSONObject();
 		json.put("volumeInfo", volume);
 		volume.putOpt("title", title);
 		volume.putOpt("subtitle", subtitle);
 		volume.putOpt("series", series);
 		volume.putOpt("description", description);
 		volume.putOpt("publisher", publisher);
 		volume.putOpt("pageCount", pageCount);
 		
 		if (authors != null && !authors.isEmpty()) {
 			JSONArray array = new JSONArray();
 			volume.put("authors", array);
 			for (String author : authors) {
 				array.put(author);
 			}
 		}
 		
 		return json.toString();
 	}
 
 	private void parseJSON(JSONObject obj) throws JSONException,
 			URISyntaxException {
 		comment = obj.optString("comment");
 		isbn = obj.getString("isbn");
 		addition = !Boolean.TRUE.equals(obj.optBoolean("known"));
 
 		obj = obj.getJSONObject("volumeInfo");
 
 		title = obj.optString("title");
 		subtitle = obj.optString("subtitle");
 		series = obj.optString("series");
 		description = obj.optString("description");
 		publisher = obj.optString("publisher");
 		pageCount = obj.optInt("pageCount");
 
 		JSONArray array = obj.optJSONArray("authors");
 		if (array != null) {
 			authors = new ArrayList<String>(array.length());
 			for (int i = 0; i < array.length(); i++) {
 				authors.add(array.getString(i));
 			}
 		}
 
 		obj = obj.optJSONObject("imageLinks");
 		if (obj != null) {
			String url = obj.getString("thumbnail");
 
 			if (url != null && !url.isEmpty()) {
 				thumbnail = new URI(url);
 			}
 
			url = obj.getString("smallThumbnail");
 
 			if (url != null && !url.isEmpty()) {
 				thumbnailSmall = new URI(url);
 			}
 		}
 	}
 
 	public String getTitle() {
 		return title;
 	}
 
 	public void setTitle(String title) {
 		this.title = title;
 	}
 
 	public String getSubtitle() {
 		return subtitle;
 	}
 
 	public void setSubtitle(String subtitle) {
 		this.subtitle = subtitle;
 	}
 
 	public String getSeries() {
 		return series;
 	}
 
 	public void setSeries(String series) {
 		this.series = series;
 	}
 
 	public String getDescription() {
 		return description;
 	}
 
 	public void setDescription(String description) {
 		this.description = description;
 	}
 
 	public String getPublisher() {
 		return publisher;
 	}
 
 	public void setPublisher(String publisher) {
 		this.publisher = publisher;
 	}
 
 	public List<String> getAuthors() {
 		return authors;
 	}
 
 	public void setAuthors(List<String> authors) {
 		this.authors = authors;
 	}
 
 	public String getComment() {
 		return comment;
 	}
 
 	public void setComment(String comment) {
 		this.comment = comment;
 	}
 
 	public String getIsbn() {
 		return isbn;
 	}
 
 	public URI getThumbnail() {
 		return thumbnail;
 	}
 
 	public URI getThumbnailSmall() {
 		return thumbnailSmall;
 	}
 
 	public int getPageCount() {
 		return pageCount;
 	}
 
 	public boolean isAddition() {
 		return addition;
 	}
 
 
 }
