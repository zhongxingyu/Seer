 package org.snuderl.web;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.snuderl.ApplicationState;
 import org.snuderl.mobilni.NewsMessage;
 
 import android.sax.Element;
 import android.sax.EndElementListener;
 import android.sax.EndTextElementListener;
 import android.sax.RootElement;
 import android.test.IsolatedContext;
 import android.text.format.DateFormat;
 import android.util.Xml;
 
 public class FeedParser {
 	static final String PUB_DATE = "pubDate";
 	static final String DESCRIPTION = "description";
 	static final String LINK = "link";
 	static final String TITLE = "title";
 	static final String ITEM = "item";
 	static final String ID = "guid";
 	static final String CATEGORY = "category";
 	private String lastDate = null;
 	private String firstDate = null;
 	private ApplicationState state;
 	private String location = "";
 	public String FilterInfo = null;
 	public HashSet<String> Categories = new HashSet<String>();
 
 	private List<String> FilterInfo(String s) {
 		List<String> filters = new LinkedList<String>();
 		String[] m = s.split("\n");
 		for (String a : m) {
 			filters.add(a);
 		}
 		return filters;
 
 	}
 
 	final String feedUrl;
 	HashMap<String, String> parameters = new HashMap<String, String>();
 
 	public FeedParser(String feedUrl) {
 		this.feedUrl = feedUrl;
 		state = ApplicationState.GetApplicationState();
 		location = SendCoordinates();
 
 	}
 
 	public void AddParameter(String key, String value) {
 		parameters.put(key, value);
 	}
 
 	protected InputStream getInputStream(String uri) {
 		try {
 			return new URL(uri).openConnection().getInputStream();
 		} catch (IOException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	private String SendCoordinates() {
 		StringBuilder url = new StringBuilder();
 		if (state.GetLocation() != null) {
 			url.append("&Longitude=");
 			url.append(state.Location.getLongitude());
 			url.append("&Latitude=");
 			url.append(state.Location.getLatitude());
 		}
 		return url.toString();
 	}
 
 	public List<NewsMessage> GetNew() {
 		StringBuilder url = new StringBuilder();
 		url.append(feedUrl);
 		url.append("/" + "GetNew");
 		url.append("?");
 		int count = 0;
 		for (String key : parameters.keySet()) {
 			url.append(key);
 			url.append("=");
 			url.append(parameters.get(key));
 			count++;
 			if (count < parameters.size()) {
 				url.append("&");
 			}
 		}
 		if (firstDate != null) {
 			url.append("&firstDate=" + URLEncoder.encode(firstDate));
 		}
 		url.append(location);
 
 		List<NewsMessage> list = parse(url.toString().replace("\n", ""));
 		if (list.size() > 0) {
 			firstDate = list.get(0).Date;
 		}
 
 		return list;
 	}
 
 	public boolean HasNew() {
 		StringBuilder url = new StringBuilder();
 		url.append(feedUrl);
 		url.append("/" + "GetNew");
 		url.append("?");
 		int count = 0;
 		for (String key : parameters.keySet()) {
 			url.append(key);
 			url.append("=");
 			url.append(parameters.get(key));
 			count++;
 			if (count < parameters.size()) {
 				url.append("&");
 			}
 		}
 		if (firstDate != null) {
 			url.append("&firstDate=" + URLEncoder.encode(firstDate));
 		}
 		url.append(location);
 		
 		try{
 		List<NewsMessage> list = parse(url.toString().replace("\n", ""));
 		return list.size() > 0;
 		}
 		catch(Exception e){
 			return false;
 		}
 	}
 
 	public List<NewsMessage> more() {
 		StringBuilder url = new StringBuilder();
 		url.append(feedUrl);
 		url.append("?");
 		int count = 0;
 		for (String key : parameters.keySet()) {
 			url.append(key);
 			url.append("=");
 			url.append(parameters.get(key));
 			count++;
 			if (count < parameters.size()) {
 				url.append("&");
 			}
 		}
 		if (lastDate != null) {
 			url.append("&lastDate=" + URLEncoder.encode(lastDate));
 		}
 		url.append(location);
 
 		List<NewsMessage> list = parse(url.toString().replace("\n", ""));
 
 		return list;
 	}
 
 	public List<NewsMessage> parse() {
 		StringBuilder url = new StringBuilder();
 		url.append(feedUrl);
 		url.append("?");
 		int count = 0;
 		for (String key : parameters.keySet()) {
 			url.append(key);
 			url.append("=");
 			url.append(parameters.get(key));
 			count++;
 			if (count < parameters.size()) {
 				url.append("&");
 			}
 		}
 		url.append(location);
 		List<NewsMessage> list = parse(url.toString().replace("\n", ""));
 		if (list.size() > 0) {
 			firstDate = list.get(0).Date;
 		}
 
 		return list;
 	}
 
 	public List<NewsMessage> parse(String URI) {
 
 		final List<NewsMessage> messages = new ArrayList<NewsMessage>();
 		final NewsMessage currentMessage = new NewsMessage();
 		RootElement root = new RootElement("rss");
 		Element channel = root.getChild("channel");
 		channel.getChild("description").setEndTextElementListener(
 				new EndTextElementListener() {
 
 					public void end(String body) {
 						FilterInfo = body;
 
 					}
 				});
 		Element item = channel.getChild(ITEM);
 		item.setEndElementListener(new EndElementListener() {
 			public void end() {
 				messages.add(currentMessage.copy());
 			}
 		});
 		item.getChild(ID).setEndTextElementListener(
 				new EndTextElementListener() {
 					public void end(String body) {
 						currentMessage.Id = (body);
 					}
 				});
 		item.getChild(TITLE).setEndTextElementListener(
 				new EndTextElementListener() {
 					public void end(String body) {
						currentMessage.Title = (body);
 					}
 				});
 		item.getChild(LINK).setEndTextElementListener(
 				new EndTextElementListener() {
 					public void end(String body) {
 						currentMessage.Link = (body);
 					}
 				});
 		item.getChild(DESCRIPTION).setEndTextElementListener(
 				new EndTextElementListener() {
 					public void end(String body) {
 						currentMessage.Short = (body);
 					}
 				});
 		item.getChild(PUB_DATE).setEndTextElementListener(
 				new EndTextElementListener() {
 					public void end(String body) {
 						currentMessage.Date = (body);
 						lastDate = body;
 					}
 				});
 
 		item.getChild(CATEGORY).setEndTextElementListener(
 				new EndTextElementListener() {
 
 					public void end(String body) {
 						currentMessage.Category = (body);
 						Categories.add(body);
 					}
 				});
 		try {
 			Xml.parse(this.getInputStream(URI), Xml.Encoding.UTF_8,
 					root.getContentHandler());
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 		return messages;
 	}
 }
