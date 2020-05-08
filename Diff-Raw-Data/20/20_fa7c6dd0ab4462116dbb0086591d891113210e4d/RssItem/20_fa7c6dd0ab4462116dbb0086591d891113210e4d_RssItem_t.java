 package mhyhre.lentaduringday.rss;
 
 import java.util.*;
 import java.text.*;
 import java.net.*;
 import java.io.*;
 import javax.xml.parsers.*;
 import org.w3c.dom.*;
 
 import android.annotation.SuppressLint;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.util.Log;
 
 public class RssItem {
 
 	private String title;
 	private String description;
 	private Date pubDate;
 	private String category;
 	private Bitmap image;
 
 	public RssItem(String title, String description, Date pubDate, String category, Bitmap image) {
 		this.title = title;
 		this.description = description;
 		this.pubDate = pubDate;
 		this.category = category;
 		this.image = image;
 	}
 
 	public String getTitle() {
 		return this.title;
 	}
 
 	public String getCategory() {
 		return this.category;
 	}
 
 	public String getDescription() {
 		return this.description;
 	}
 
 	public String getPubDate() {
		DateFormat df = new SimpleDateFormat("d LLLL y, H:m", new Locale("ru", "RU"));
		
		String result = "";

		try {
			result = df.format(pubDate);
        } catch (Exception e) {
            Log.e("MHYHRE", "Exception:" + e);
        }
		return result;
 	}
 
 	public Bitmap getImage() {
 		return image;
 	}
 
 

 	public static ArrayList<RssItem> getRssItems(String feedUrl) {
 
 		ArrayList<RssItem> rssItems = new ArrayList<RssItem>();
 
 		try {
 
 			URL url = new URL(feedUrl);
 			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
 
 			if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
 				InputStream is = conn.getInputStream();
 
 				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
 				DocumentBuilder db = dbf.newDocumentBuilder();
 
 				Document document = db.parse(is);
 				Element element = document.getDocumentElement();
 
 				NodeList nodeList = element.getElementsByTagName("item");
 
 				if (nodeList.getLength() > 0) {
 					for (int i = 0; i < nodeList.getLength(); i++) {
 
 						Element entry = (Element) nodeList.item(i);
 
 						Element _titleE = (Element) entry.getElementsByTagName("title").item(0);
 						Element _descriptionE = (Element) entry.getElementsByTagName("description").item(0);
 
 						Element _pubDateE = (Element) entry.getElementsByTagName("pubDate").item(0);
 						Element _categoryE = (Element) entry.getElementsByTagName("category").item(0);
 						Element _pictureE = (Element) entry.getElementsByTagName("enclosure").item(0);
 
 						String _title = _titleE.getFirstChild().getNodeValue();
 						String _description = getCharacterDataFromElement(_descriptionE);
 						
 
 						// Parse date & time
 						String rssDate = _pubDateE.getFirstChild().getNodeValue();
 						Date _date = null;
						DateFormat formatter = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", new Locale("en", "EN"));
 
 						try {
 							_date = formatter.parse(rssDate);
 
 						} catch (ParseException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 
 						String _category = _categoryE.getFirstChild().getNodeValue();
 
 						// Image loading
 						Bitmap _image = getBitmapFromURL(_pictureE.getAttributes().getNamedItem("url").getTextContent());
 
 						// Add news
 						RssItem rssItem = new RssItem(_title, _description, _date, _category, _image);
 						rssItems.add(rssItem);
 					}
 				}
 
 			} else {
 				Log.e("MHYHRE", "HttpURLConnection.HTTP_is not OK");
 			}
 		} catch (Exception e) {
 			Log.e("MHYHRE", "Connection: " + e);
 			e.printStackTrace();
 		}
 
 		return rssItems;
 	}
 
 	public static Bitmap getBitmapFromURL(String src) {
 		try {
 			URL url = new URL(src);
 			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
 			connection.setDoInput(true);
 			connection.connect();
 			InputStream input = connection.getInputStream();
 			Bitmap myBitmap = BitmapFactory.decodeStream(input);
 			return myBitmap;
 		} catch (IOException e) {
 			e.printStackTrace();
 			Log.e("Exception", e.getMessage());
 			return null;
 		}
 	}
 	
 	public static String getCharacterDataFromElement(Element e) {
 
 	    NodeList list = e.getChildNodes();
 	    String data;
 
 	    for(int index = 0; index < list.getLength(); index++){
 	        if(list.item(index) instanceof CharacterData){
 	            CharacterData child = (CharacterData) list.item(index);
 	            data = child.getData();
 
 	            if(data != null && data.trim().length() > 0)
 	                return child.getData();
 	        }
 	    }
 	    return "";
 	}
 
 }
