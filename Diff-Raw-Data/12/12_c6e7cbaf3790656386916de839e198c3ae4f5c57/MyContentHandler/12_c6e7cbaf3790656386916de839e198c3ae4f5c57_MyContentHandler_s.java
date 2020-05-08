 package com.derek.cshome.util;
 
 import java.util.Arrays;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 import com.derek.cshome.MainActivity.ELEMENT_ID;
 import com.derek.cshome.util.MyCoursesContentHandler.Item;
 
 import android.renderscript.Element;
 import android.util.Log;
 
 public class MyContentHandler extends DefaultHandler {
 	protected String TAG = "MyContentHandler";
 	protected String url;
 	protected String[] blaceList = {"rss", "channel", "language", "lastBuildDate", "copyright", 
 			"category", "ArrayOfCourse"};
 	protected ELEMENT_ID retriver_id;
 	protected Boolean isCoursesRss;
 	protected String str = "";
 	
 
 
 	protected Item item;
 //	protected List itemList = new ArrayList();
 	protected List<Item> itemList = new ArrayList<Item>();
 
 	public MyContentHandler(String url, ELEMENT_ID retriver_id) {
 		this.retriver_id = retriver_id;
 		TAG = "MyContentHandler-" + retriver_id.toString();
 		isCoursesRss = retriver_id == ELEMENT_ID.COURSES_RSS_ID;
 		
 		Log.d(TAG, "this is" + (isCoursesRss?"":" not") + " a courses rss");
 		this.url = url;
 	}
 
 	
 	public List getItemList() {
 		return itemList;
 	}
 
 	public List<HashMap<String, String>> getItemListMap() {
 		List<HashMap<String, String>> itemListMap = new ArrayList<HashMap<String, String>>();
 		if (itemList == null){
 			Log.e(TAG, "getItemListMap: itemList = null");
 		}
 		for (Item item : itemList) {
 				Log.d(TAG,"getItemListMap: creating element, title: "
 							+ item.getTitle());
 				
 				if (item.getDescription().trim().equals("")){
 					Log.e(TAG, "getItemListMap: Description is empty");
 				} else {
 					Log.d(TAG, "getItemListMap: Description is not empty\n" + item.getDescription());
 				}
 			HashMap<String, String> map = new HashMap<String, String>();
 			map.put("title", item.getTitle());
 			map.put("link", item.getLink());
 			map.put("description", item.getDescription());
 			map.put("guid", item.getGuid());
 			map.put("pubDate", item.getPubDate());
 			map.put("endDate", item.getEndDate());
 			itemListMap.add(map);
 		}
 		return itemListMap;
 	}
 
 
 //	public List<HashMap<String, String>> getItemListMap() {
 //		this(itemList);
 //	}
 
 	@Override
 	public void startElement(String uri, String localName, String qName,
 			Attributes attributes) throws SAXException {
 		// TODO Auto-generated method stub
 		super.startElement(uri, localName, qName, attributes);
 		String attrString = "";
 		for (int i = 0; i < attributes.getLength(); i++) {
 			attrString += attributes.getQName(i) + ":" + attributes.getValue(i)
 					+ " ";
 		}
 		Log.v(TAG, String.format(
 				"startElement uri:%s localName:%s qName:%s attributes: %s",
 				uri, localName, qName, attrString));
 		str = "";
 		if (localName.equals("item")) {
 			// start of new news
 			item = new Item();
 		}
 	}
 	@Override
 	public void startDocument() throws SAXException {
 		// TODO Auto-generated method stub
 		super.startDocument();
 		Log.v(TAG, "startDocument, initialized itemList");
 		itemList = new ArrayList<Item>();
 	}
 	@Override
 	public void endElement(String uri, String localName, String qName)
 			throws SAXException {
 		// TODO Auto-generated method stub
 		super.endElement(uri, localName, qName);
 		Log.v(TAG, String.format("endElement uri:%s localName:%s qName:%s",
 				uri, localName, qName));
 
 		if (item == null){
 			return;
 		}
 		if (localName.equals("item")) {
 			Log.d(TAG, "adding new news to itemList: " + item.getTitle() + "\nDescription: " + item.getDescription());
 			itemList.add(item);
 			item = null;
 		} else if (localName.equals("title")) {
 			item.setTitle(str);
 		} else if (localName.equals("link")) {
 			item.setLink(str);
 		} else if (localName.equals("description")) {
			item.setDescription(str);
 		} else if (localName.equals("guid")) {
 			item.setGuid(str);
 		} else if (localName.equals("pubDate")) {
 			item.setPubDate(str);
 		} else if (localName.equals("endDate")){
 			item.setEndDate(str);
 		} else if ( ! Arrays.asList(blaceList).contains(localName)){
 			Log.w(TAG, "found un-recongnized tag: " + localName);
 		}
 		Log.d(TAG, "itemList.size(): " + itemList.size());
 	}
 
 	@Override
 	public void characters(char[] ch, int start, int length)
 			throws SAXException {
 		// TODO Auto-generated method stub
 		super.characters(ch, start, length);
 		Log.v(TAG,
 				"characters " + new String(ch).substring(start, start + length));
		str = new String(ch).substring(start, start + length);
 	}
 
 	@Override
 	public void endDocument() throws SAXException {
 		// TODO Auto-generated method stub
 		super.endDocument();
 		Log.v(TAG, "endDocument");
 	}
 	
 
 	public class Item {
 
 		private String title, link, description, pubDate;
 		private String guid, endDate;
 
 		public Item() {
 
 		}
 
 		public String getTitle() {
 			return title;
 		}
 
 		public void setTitle(String title) {
 			this.title = title;
 		}
 
 		public String getDescription() {
 			return description;
 		}
 
 		public void setDescription(String description) {
 			this.description = description;
 		}
 
 		public String getGuid() {
 			return guid;
 		}
 
 		public void setGuid(String guid) {
 			this.guid = guid;
 		}
 
 		public String getLink() {
 			return link;
 		}
 
 		public void setLink(String link) {
 			this.link = link;
 		}
 
 		public String getPubDate() {
 			return pubDate;
 		}
 
 		public void setPubDate(String pubDate) {
 			this.pubDate = pubDate;
 		}
 		
 		public String getEndDate() {
 			return endDate;
 		}
 
 		public void setEndDate(String endDate) {
 			this.endDate = endDate;
 		}
 
 		public String toString() {
 			return String.format(
 					"title %s, link %s, description %s, guid %s, pubDate %s, nedDate %s",
 					title, link, description, guid, pubDate, endDate);
 		}
 	}
 }
