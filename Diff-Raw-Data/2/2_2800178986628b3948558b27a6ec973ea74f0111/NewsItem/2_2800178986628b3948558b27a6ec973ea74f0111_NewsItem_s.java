 package vs.piratenpartei.ch.app.news;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Locale;
 
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserException;
 
 import android.util.Log;
 import android.util.Xml;
 
 public class NewsItem {
 	private String _title;
 	private String _link;
 	private String _comments;
 	private Date _publishDate;
 	private String _creator;
 	private ArrayList<String> _categories = new ArrayList<String>();
 	private String _description;
 	private String _content;
 	
 	public NewsItem()
 	{
 	}
 
 	public static ArrayList<NewsItem> readFeed(InputStream pIn) throws XmlPullParserException, IOException, ParseException
 	{
 		ArrayList<NewsItem> result = new ArrayList<NewsItem>();
 
 		try 
 		{
 			XmlPullParser parser = Xml.newPullParser();
 			parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
 			parser.setInput(pIn, null);
 			parser.nextTag();
 			parser.nextTag();
 			parser.require(XmlPullParser.START_TAG, null, "channel");
 			while(parser.next() != XmlPullParser.END_TAG)
 			{
 				if(parser.getEventType() != XmlPullParser.START_TAG)
 				{
 					continue;
 				}
 				String name = parser.getName();
 				Log.i("[PPVS App]NewsItem.readFeed()", name);
 				if(name.equals("item"))
 				{
 					result.add(NewsItem.readItem(parser));
 				}
 				else
 				{
 					NewsItem.skip(parser);
 				}
 			}
 		}
 		finally
 		{
 			pIn.close();
 		}
 		
 		return result;
 	}
 	
 	private static NewsItem readItem(XmlPullParser pParser) throws XmlPullParserException, IOException, ParseException
 	{
 		NewsItem result = new NewsItem();
 		pParser.require(XmlPullParser.START_TAG, null, "item");
 		while(pParser.next() != XmlPullParser.END_TAG)
 		{
 			if(pParser.getEventType() != XmlPullParser.START_TAG)
 			{
 				continue;
 			}
 			String name = pParser.getName();
 			Log.i("[PPVS App]NewsItem.readItem()", name);
 			if(name.equals("title"))
 			{
 				result._title = NewsItem.readText(pParser, "title");
 			}
 			else if(name.equals("link"))
 			{
 				result._link = NewsItem.readText(pParser, "link");
 			}
 			else if(name.equals("comments"))
 			{
 				result._comments = NewsItem.readText(pParser, "comments");
 			}
 			else if(name.equals("pubDate"))
 			{
				DateFormat df = new SimpleDateFormat("EEE, dd MMM yyyy kk:mm:ss z", Locale.getDefault());
 				result._publishDate = df.parse(NewsItem.readText(pParser, "pubDate"));
 			}
 			else if(name.equals("dc:creator"))
 			{
 				result._creator = NewsItem.readText(pParser, "dc:creator");
 			}
 			else if(name.equals("category"))
 			{
 				result._categories.add(NewsItem.readText(pParser, "category"));
 			}
 			else if(name.equals("description"))
 			{
 				result._description = NewsItem.readText(pParser, "description");
 			}
 			else if(name.equals("content:encoded"))
 			{
 				result._content = NewsItem.readText(pParser, "content:encoded");
 			}
 			else
 			{
 				NewsItem.skip(pParser);
 			}
 		}
 		return result;
 	}
 	
 	private static void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
 	    if (parser.getEventType() != XmlPullParser.START_TAG) {
 	        throw new IllegalStateException();
 	    }
 	    int depth = 1;
 	    while (depth != 0) {
 	        switch (parser.next()) {
 	        case XmlPullParser.END_TAG:
 	            depth--;
 	            break;
 	        case XmlPullParser.START_TAG:
 	            depth++;
 	            break;
 	        }
 	    }
 	 }
 	
 	private static String readText(XmlPullParser pParser, String pTag) throws XmlPullParserException, IOException
 	{
 		pParser.require(XmlPullParser.START_TAG, null, pTag);
 		String result = pParser.nextText();
 		pParser.require(XmlPullParser.END_TAG, null, pTag);
 		return result;
 	}
 	
 	public String getTitle()
 	{
 		return this._title;
 	}
 	
 	public String getLink()
 	{
 		return this._link;
 	}
 	
 	public String getComments()
 	{
 		return this._comments;
 	}
 	
 	public Date getPublishDate()
 	{
 		return this._publishDate;
 	}
 	
 	public String getCreator()
 	{
 		return this._creator;
 	}
 	
 	public ArrayList<String> getCategories()
 	{
 		return this._categories;
 	}
 	
 	public String getDescription()
 	{
 		return this._description;
 	}
 	
 	public String getContent()
 	{
 		return this._content;
 	}
 }
