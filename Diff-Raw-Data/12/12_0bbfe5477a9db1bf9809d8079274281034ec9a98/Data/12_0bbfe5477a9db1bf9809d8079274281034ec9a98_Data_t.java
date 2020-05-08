 package com.pk.addits.data;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.StringReader;
 import java.net.URL;
 import java.net.URLConnection;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.apache.http.util.ByteArrayBuffer;
 import org.xml.sax.Attributes;
 import org.xml.sax.InputSource;
 import org.xml.sax.helpers.DefaultHandler;
 import org.xmlpull.v1.XmlPullParser;
 import org.xmlpull.v1.XmlPullParserFactory;
 
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.content.Context;
 import android.graphics.Point;
 import android.net.ConnectivityManager;
 import android.net.NetworkInfo;
 import android.os.Environment;
 import android.text.Html;
 import android.text.format.DateUtils;
 import android.util.Log;
 import android.util.Xml;
 import android.view.Display;
 
 import com.pk.addits.FragmentArticle.ArticleContent;
 import com.pk.addits.FragmentArticle.CommentFeed;
 import com.pk.addits.models.Article;
 
 public class Data
 {
 	public static final String API_KEY_YOUTUBE = "AIzaSyCkM13XkYpzqEyjRX11F8IoiHLmd1TrKoU";
 	
 	public static final String PREFS_TAG = "AndroidDissectedPreferences";
 	public static final String PREF_TAG_LAST_UPDATE_CHECK_TIME = "Last Update Check Time";
 	public static final String PREF_TAG_FIRST_TIME = "First Time";
 	public static final String PACKAGE_TAG = "com.pk.addits";
 	public static final String FEED_TAG = "feed.xml";
 	public static final String FEED_URL = "http://addits.androiddissected.com/feed/";
 	public static final String MAIN_URL = "http://addits.androiddissected.com/";
 	public static final String TEMP_TAG = "temporary.xml";
 	
 	public static final Integer CONTENT_TYPE_TEXT = 1;
 	public static final Integer CONTENT_TYPE_IMAGE = 2;
 	public static final Integer CONTENT_TYPE_VIDEO = 3;
 	public static final Integer CONTENT_TYPE_APP = 4;
 	
 	public static int getWidthByPercent(Context context, double percent)
 	{
 		Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
 		Point size = new Point();
 		display.getSize(size);
 		int width = (int) (size.x * percent);
 		return width;
 	}
 	
 	public static int getHeightByPercent(Context context, double percent)
 	{
 		Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
 		Point size = new Point();
 		display.getSize(size);
 		int height = (int) (size.y * percent);
 		return height;
 	}
 	
 	public static void downloadFeed()
 	{
 		File sdCard = Environment.getExternalStorageDirectory();
 		File dir = new File(sdCard.getAbsolutePath() + "/Android/data/" + PACKAGE_TAG);
 		dir.mkdirs();
 		File file = new File(dir, TEMP_TAG);
 		
 		// Establish Connection
 		try
 		{
 			URL updateURL = new URL(FEED_URL);
 			URLConnection conn = updateURL.openConnection();
 			InputStream is = conn.getInputStream();
 			is = conn.getInputStream();
 			BufferedInputStream bis = new BufferedInputStream(is);
 			ByteArrayBuffer baf = new ByteArrayBuffer(50);
 			
 			int current = 0;
 			while ((current = bis.read()) != -1)
 			{
 				baf.append((byte) current);
 			}
 			
 			final String s = new String(baf.toByteArray());
 			
 			FileOutputStream f = new FileOutputStream(file);
 			f.write(s.getBytes());
 			f.close();
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	public static void writeFeed()
 	{
 		File sdCard = Environment.getExternalStorageDirectory();
 		File dir = new File(sdCard.getAbsolutePath() + "/Android/data/" + PACKAGE_TAG);
 		dir.mkdirs();
 		File tempFile = new File(dir, TEMP_TAG);
 		
 		dir.mkdirs();
 		File file = new File(dir, FEED_TAG);
 		tempFile.renameTo(file);
 	}
 	
 	public static Article[] retrieveFeed()
 	{
 		int count = 0;
 		
 		try
 		{
 			File sdCard = Environment.getExternalStorageDirectory();
 			File dir = null;
 			dir = new File(sdCard.getAbsolutePath() + "/Android/data/" + PACKAGE_TAG + "/" + FEED_TAG);
 			FileInputStream istr = new FileInputStream(dir);
 			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
 			factory.setNamespaceAware(false);
 			
 			XmlPullParser xrp = factory.newPullParser();
 			xrp.setInput(istr, "UTF-8");
 			xrp.next();
 			int eventType = xrp.getEventType();
 			
 			while (eventType != XmlPullParser.END_DOCUMENT)
 			{
 				if (eventType == XmlPullParser.START_TAG)
 				{
 					String elemName = xrp.getName();
 					if (elemName.equals("article"))
 						count++;
 				}
 				eventType = xrp.next();
 			}
 		}
 		catch (Exception e)
 		{
 			Log.w("[Feed Count] XML Parse Error", e);
 		}
 		
 		Article[] Feeeeedz = new Article[count];
 		
 		try
 		{
 			File sdCard = Environment.getExternalStorageDirectory();
 			File dir = null;
 			dir = new File(sdCard.getAbsolutePath() + "/Android/data/" + PACKAGE_TAG + "/" + FEED_TAG);
 			FileInputStream istr = new FileInputStream(dir);
 			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
 			factory.setNamespaceAware(false);
 			
 			XmlPullParser xrp = factory.newPullParser();
 			xrp.setInput(istr, "UTF-8");
 			xrp.next();
 			int eventType = xrp.getEventType();
 			
 			int feedCount = 0;
 			while (eventType != XmlPullParser.END_DOCUMENT)
 			{
 				if (eventType == XmlPullParser.START_TAG)
 				{
 					String elemName = xrp.getName();
 					if (elemName.equals("article"))
 					{
 						// Attributes
 						String Title = xrp.getAttributeValue(null, "title");
 						String Description = xrp.getAttributeValue(null, "description");
 						String Content = xrp.getAttributeValue(null, "content");
 						String CommentFeed = xrp.getAttributeValue(null, "commentfeed");
 						String Author = xrp.getAttributeValue(null, "author");
 						String Date = xrp.getAttributeValue(null, "date");
 						String Category = xrp.getAttributeValue(null, "category");
 						String Image = xrp.getAttributeValue(null, "image");
 						String URL = xrp.getAttributeValue(null, "url");
 						boolean Favorite = Boolean.parseBoolean(xrp.getAttributeValue(null, "favorite"));
 						boolean Read = Boolean.parseBoolean(xrp.getAttributeValue(null, "read"));
 						
 						Feeeeedz[feedCount] = new Article(feedCount, Title, Description, Content, CommentFeed, Author, Date, Category, Image, URL, Favorite, Read);
 						feedCount++;
 					}
 				}
 				eventType = xrp.next();
 			}
 			
 		}
 		catch (Exception e)
 		{
 			Log.w("[Feed] XML Parse Error", e);
 		}
 		
 		return Feeeeedz;
 	}
 	
 	public static void downloadCommentFeed(String feedURL)
 	{
 		File sdCard = Environment.getExternalStorageDirectory();
 		File dir = new File(sdCard.getAbsolutePath() + "/Android/data/" + PACKAGE_TAG);
 		dir.mkdirs();
 		File file = new File(dir, TEMP_TAG);
 		
 		// Establish Connection
 		try
 		{
 			URL updateURL = new URL(feedURL);
 			URLConnection conn = updateURL.openConnection();
 			InputStream is = conn.getInputStream();
 			is = conn.getInputStream();
 			BufferedInputStream bis = new BufferedInputStream(is);
 			ByteArrayBuffer baf = new ByteArrayBuffer(50);
 			
 			int current = 0;
 			while ((current = bis.read()) != -1)
 			{
 				baf.append((byte) current);
 			}
 			
 			final String s = new String(baf.toByteArray());
 			
 			FileOutputStream f = new FileOutputStream(file);
 			f.write(s.getBytes());
 			f.close();
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	public static List<CommentFeed> retrieveCommentFeed(Context context)
 	{
 		List<CommentFeed> comments = new ArrayList<CommentFeed>();
 		
 		try
 		{
 			File sdCard = Environment.getExternalStorageDirectory();
 			File dir = new File(sdCard.getAbsolutePath() + "/Android/data/" + PACKAGE_TAG + "/" + TEMP_TAG);
 			FileInputStream istr = new FileInputStream(dir);
 			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
 			factory.setNamespaceAware(false);
 			
 			XmlPullParser xrp = factory.newPullParser();
 			xrp.setInput(istr, "UTF-8");
 			xrp.next();
 			int eventType = xrp.getEventType();
 			
 			// Attributes
 			String Creator = "";
 			String Content = "";
 			String Date = "";
 			
 			// Flags
 			boolean itemActive = false;
 			
 			while (eventType != XmlPullParser.END_DOCUMENT)
 			{
 				if (eventType == XmlPullParser.START_TAG)
 				{
 					String elemName = xrp.getName();
 					if (!itemActive && elemName.equals("item"))
 					{
 						itemActive = true;
 					}
 					else if (itemActive && elemName.equals("dc:creator"))
 					{
 						if (xrp.next() == XmlPullParser.TEXT)
 							Creator = xrp.getText();
 					}
 					else if (itemActive && elemName.equals("content:encoded"))
 					{
 						if (xrp.next() == XmlPullParser.TEXT)
 							Content = android.text.Html.fromHtml(xrp.getText()).toString();
 					}
 					else if (itemActive && elemName.equals("pubDate"))
 					{
 						if (xrp.next() == XmlPullParser.TEXT)
 							Date = xrp.getText();
 					}
 				}
 				else if (eventType == XmlPullParser.END_TAG && xrp.getName().equals("item"))
 				{
 					itemActive = false;
 					
 					comments.add(new CommentFeed(Creator, Content, Date));
 				}
 				eventType = xrp.next();
 			}
 			
 		}
 		catch (Exception e)
 		{
 			Log.w("[CommentFeed] XML Parse Error", e);
 		}
 		
 		// Return Combination
 		return comments;
 	}
 	
 	public static List<ArticleContent> generateArticleContent(String Content)
 	{
 		List<ArticleContent> contentList = new ArrayList<ArticleContent>();
 		XmlPullParser xrp = Xml.newPullParser();
 		
 		try
 		{
 			xrp.setInput(new StringReader(Content));
 			int eventType = xrp.getEventType();
 			
 			String p_text = "";
 			StringBuilder builder = new StringBuilder();
 			boolean p_active = false;
 			boolean img_active = false;
 			
 			while (eventType != XmlPullParser.END_DOCUMENT)
 			{
 				try
 				{
 					if (eventType == XmlPullParser.START_TAG)
 					{
 						String elemName = xrp.getName();
 						if (!p_active && elemName.equalsIgnoreCase("p"))
 						{
 							p_active = true;
 							builder = new StringBuilder();
							//builder.append("<p>");
 						}
 						else if (p_active)
 						{
 							if (elemName.equalsIgnoreCase("em"))
 								builder.append("<em>");
 							else if (elemName.equalsIgnoreCase("strong"))
 								builder.append("<strong>");
 							else if (elemName.equalsIgnoreCase("br"))
 								builder.append("<br />");
 							else if (elemName.equalsIgnoreCase("a"))
 							{
 								builder.append("<a ");
 								int numAtribs = xrp.getAttributeCount();
 								for (int i = 0; i < numAtribs; i++)
 								{
 									if (xrp.getAttributeName(i).equalsIgnoreCase("href"))
 									{
 										builder.append(" href=\"" + xrp.getAttributeValue(i) + "\"");
 									}
 								}
 								builder.append(">");
 							}
 							else if (elemName.equalsIgnoreCase("img"))
 							{
 								img_active = true;
 								String imgSource = "";
 								int numAtribs = xrp.getAttributeCount();
 								for (int i = 0; i < numAtribs; i++)
 								{
 									if (xrp.getAttributeName(i).equalsIgnoreCase("src"))
 									{
 										imgSource = xrp.getAttributeValue(i).trim();
 										break;
 									}
 								}
 								
 								contentList.add(new ArticleContent(Data.CONTENT_TYPE_IMAGE, imgSource));
 								
 							}
 							else if (elemName.equalsIgnoreCase("button"))
 								p_active = false; // Nope.
 						}
 					}
 					else if (eventType == XmlPullParser.END_TAG)
 					{
 						String elemName = xrp.getName();
 						if (p_active && elemName.equalsIgnoreCase("p"))
 						{
							//builder.append("</p>");
 							if (img_active)
 							{
 								if (Html.fromHtml(builder.toString()).toString().trim().length() > 0)
 									contentList.add(new ArticleContent(Data.CONTENT_TYPE_TEXT, builder.toString() + "\n\n"));
 							}
 							else
 								contentList.add(new ArticleContent(Data.CONTENT_TYPE_TEXT, builder.toString() + "\n\n"));
 							
 							p_active = false;
 							img_active = false;
 						}
 						else if (p_active)
 						{
 							if (elemName.equalsIgnoreCase("em"))
 								builder.append("</em>");
 							else if (elemName.equalsIgnoreCase("strong"))
 								builder.append("</strong>");
 							else if (elemName.equalsIgnoreCase("a"))
 								builder.append("</a>");
 						}
 					}
 					else if (eventType == XmlPullParser.TEXT)
 					{
 						if (p_active)
 						{
 							builder.append(xrp.getText());
 						}
 					}
 					eventType = xrp.next();
 				}
 				catch (Exception e)
 				{
 					Log.v("HAX", "xrp.next() error");
 				}
 			}
 		}
 		catch (Exception e)
 		{
 			Log.e("Error Parsing Content", e.getMessage(), e);
 		}
 		
 		return contentList;
 	}
 	
 	public static List<ArticleContent> generateArticleContent2(String Content)
 	{
 		final List<ArticleContent> contentList = new ArrayList<ArticleContent>();
 		String sContent = "<article>" + Content + "</article>";
 		
 		try
 		{
 			SAXParserFactory factory = SAXParserFactory.newInstance();
 			SAXParser saxParser = factory.newSAXParser();
 			
 			DefaultHandler handler = new DefaultHandler()
 			{
 				StringBuilder sb;
 				StringBuilder tsb;
 				
 				boolean p_active = false;
 				boolean h2_active = false;
 				boolean bsalary = false;
 				
 				public void startElement(String uri, String localName, String qName, Attributes attributes)
 				{
 					System.out.println("Start Element :" + qName);
 					contentList.add(new ArticleContent(Data.CONTENT_TYPE_TEXT, "[" + qName + "]"));
 					
 					if (qName.equalsIgnoreCase("p"))
 					{
 						sb = new StringBuilder();
 						tsb = new StringBuilder();
 						sb.append("<p>");
 						p_active = true;
 					}
 					else if (qName.equalsIgnoreCase("h2"))
 					{
 						sb = new StringBuilder();
 						tsb = new StringBuilder();
 						sb.append("<h2>");
 						h2_active = true;
 					}
 					else if (p_active)
 					{
 						int length = attributes.getLength();
 						if (qName.equalsIgnoreCase("img"))
 						{
 							String imgSource = "";
 							for (int i = 0; i < length; i++)
 							{
 								if (attributes.getQName(i).equalsIgnoreCase("src"))
 								{
 									imgSource = attributes.getValue(i).trim();
 									break;
 								}
 							}
 							
 							contentList.add(new ArticleContent(Data.CONTENT_TYPE_IMAGE, imgSource));
 							// p_active = false;
 						}
 						else if (qName.equalsIgnoreCase("iframe"))
 						{
 							String ID = "";
 							for (int i = 0; i < length; i++)
 							{
 								if (attributes.getQName(i).equalsIgnoreCase("src"))
 								{
 									String pattern = "(?<=watch\\?v=|/videos/|embed\\/)[^#\\&\\?]*";
 									
 									Pattern compiledPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
 									Matcher matcher = compiledPattern.matcher(attributes.getValue(i).trim());
 									StringBuilder IDB = new StringBuilder();
 									
 									while (matcher.find())
 									{
 										IDB.append(matcher.group());
 									}
 									
 									ID = IDB.toString();
 									break;
 								}
 							}
 							
 							contentList.add(new ArticleContent(Data.CONTENT_TYPE_VIDEO, ID));
 							p_active = false;
 						}
 						else if (!qName.equalsIgnoreCase("div") && !qName.equalsIgnoreCase("button"))
 						{
 							System.out.println("HAX :" + qName);
 							sb.append("<" + qName);
 							tsb.append("<" + qName);
 							for (int i = 0; i < length; i++)
 							{
 								sb.append(" " + attributes.getQName(i));
 								sb.append("=\"" + attributes.getValue(i) + "\"");
 								tsb.append(" " + attributes.getQName(i));
 								tsb.append("=\"" + attributes.getValue(i) + "\"");
 							}
 							sb.append(">");
 							tsb.append(">");
 						}
 					}
 					else if (h2_active)
 					{
 						int length = attributes.getLength();
 						
 						sb.append("<" + qName);
 						tsb.append("<" + qName);
 						for (int i = 0; i < length; i++)
 						{
 							sb.append(" " + attributes.getQName(i));
 							sb.append("=\"" + attributes.getValue(i) + "\"");
 							tsb.append(" " + attributes.getQName(i));
 							tsb.append("=\"" + attributes.getValue(i) + "\"");
 						}
 						sb.append(">");
 						tsb.append(">");
 					}
 				}
 				
 				public void endElement(String uri, String localName, String qName)
 				{
 					contentList.add(new ArticleContent(Data.CONTENT_TYPE_TEXT, "[/" + qName + "]"));
 					System.out.println("End Element :" + qName);
 					
 					if (p_active)
 					{
 						if (qName.equalsIgnoreCase("p") && (sb.length() - tsb.length()) > 0)
 						{
 							sb.append("</p>");
 							contentList.add(new ArticleContent(Data.CONTENT_TYPE_TEXT, sb.toString().trim()));
 							p_active = false;
 						}
 						else
 						{
 							sb.append("</" + qName + ">");
 						}
 					}
 					else if (h2_active)
 					{
 						if (qName.equalsIgnoreCase("h2") && (sb.length() - tsb.length()) > 0)
 						{
 							sb.append("</h2>");
 							contentList.add(new ArticleContent(Data.CONTENT_TYPE_TEXT, sb.toString().trim()));
 							h2_active = false;
 						}
 						else
 						{
 							sb.append("</" + qName + ">");
 						}
 					}
 				}
 				
 				public void characters(char ch[], int start, int length)
 				{
 					
 					if (sb != null && p_active)
 					{
 						System.out.println("P : " + new String(ch, start, length));
 						for (int i = start; i < start + length; i++)
 						{
 							sb.append(ch[i]);
 						}
 					}
 					
 					if (sb != null && h2_active)
 					{
 						System.out.println("H2 : " + new String(ch, start, length));
 						for (int i = start; i < start + length; i++)
 						{
 							sb.append(ch[i]);
 						}
 					}
 					
 					if (bsalary)
 					{
 						System.out.println("Salary : " + new String(ch, start, length));
 						bsalary = false;
 					}
 					
 				}
 				
 			};
 			
 			saxParser.parse(new InputSource(new StringReader(sContent)), handler);
 			
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 		
 		return contentList;
 	}
 	
 	public static String pullLinks(String text)
 	{
 		ArrayList<String> links = new ArrayList<String>();
 		String link = "";
 		try
 		{
 			String regex = "\\(?\\b(http://|www[.])[-A-Za-z0-9+&@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&@#/%=~_()|]";
 			Pattern p = Pattern.compile(regex);
 			Matcher m = p.matcher(text);
 			while (m.find())
 			{
 				String urlStr = m.group();
 				if (urlStr.startsWith("(") && urlStr.endsWith(")"))
 				{
 					urlStr = urlStr.substring(1, urlStr.length() - 1);
 				}
 				String format = urlStr.substring(urlStr.length() - 4);
 				String lformat = urlStr.substring(urlStr.length() - 5);
 				if (format.equalsIgnoreCase(".png") || format.equalsIgnoreCase(".jpg") || format.equalsIgnoreCase(".gif"))
 					links.add(urlStr);
 				else if (lformat.equalsIgnoreCase(".jpeg") || format.equalsIgnoreCase(".webp"))
 					links.add(urlStr);
 			}
 		}
 		catch (Exception e)
 		{
 			Log.w("ImageFeed URL Parse Error", e);
 		}
 		
 		if (links.size() > 0)
 		{
 			link = links.get(0).toString();
 			link = link.replace("[", "").replace("]", "").replace("-150x150", "");
 		}
 		return link;
 	}
 	
 	public static boolean containsLinks(String text)
 	{
 		ArrayList<String> links = new ArrayList<String>();
 		
 		try
 		{
 			String regex = "\\(?\\b(http://|www[.])[-A-Za-z0-9+&@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&@#/%=~_()|]";
 			Pattern p = Pattern.compile(regex);
 			Matcher m = p.matcher(text);
 			while (m.find())
 			{
 				String urlStr = m.group();
 				if (urlStr.startsWith("(") && urlStr.endsWith(")"))
 				{
 					urlStr = urlStr.substring(1, urlStr.length() - 1);
 				}
 				links.add(urlStr);
 			}
 		}
 		catch (Exception e)
 		{
 			Log.w("containsLinks() Parse Error", e);
 		}
 		
 		if (links.size() > 0)
 			return true;
 		else
 			return false;
 	}
 	
 	/*
 	 * public static void overwriteFeedXML(Feed[] Feeeeedz) { File sdCard = Environment.getExternalStorageDirectory(); File dir = new File(sdCard.getAbsolutePath() + "/Android/data/" + PACKAGE_TAG);
 	 * dir.mkdirs(); File file = new File(dir, FEED_TAG);
 	 * 
 	 * try { StringBuilder XMLbuilder = new StringBuilder(); XMLbuilder.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" + "<feed>\n");
 	 * 
 	 * for (int x = 0; x < Feeeeedz.length; x++) { int sID = Feeeeedz[x].getID(); String sTitle = Feeeeedz[x].getTitle(); String sDescription = Feeeeedz[x].getDescription(); String sContent =
 	 * Feeeeedz[x].getContent(); String sCommentFeed = Feeeeedz[x].getCommentFeed(); String sAuthor = Feeeeedz[x].getAuthor(); String sDate = Feeeeedz[x].getDate(); String sCategory =
 	 * Feeeeedz[x].getCategory(); String sImage = Feeeeedz[x].getImage(); String sURL = Feeeeedz[x].getURL(); boolean sFavorite = Feeeeedz[x].isFavorite(); boolean sRead = Feeeeedz[x].isRead();
 	 * 
 	 * // Fix "&" Sign sTitle = sTitle.replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;").replace(">", "&gt;"); sDescription = sDescription.replace("&", "&amp;").replace("\"",
 	 * "&quot;").replace("<", "&lt;").replace(">", "&gt;"); sContent = sContent.replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;").replace(">", "&gt;"); sCommentFeed =
 	 * sCommentFeed.replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;").replace(">", "&gt;"); sAuthor = sAuthor.replace("&", "&amp;").replace("\"", "&quot;").replace("<",
 	 * "&lt;").replace(">", "&gt;"); sDate = sDate.replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;").replace(">", "&gt;"); sCategory = sCategory.replace("&", "&amp;").replace("\"",
 	 * "&quot;").replace("<", "&lt;").replace(">", "&gt;"); sImage = sImage.replace("&", "&amp;").replace("\"", "&quot;").replace("<", "&lt;").replace(">", "&gt;"); sURL = sURL.replace("&",
 	 * "&amp;").replace("\"", "&quot;").replace("<", "&lt;").replace(">", "&gt;");
 	 * 
 	 * XMLbuilder.append("	<article\n" + "		id=\"" + sID + "\"\n" + "		title=\"" + sTitle + "\"\n" + "		description=\"" + sDescription + "\"\n" + "		content=\"" + sContent + "\"\n" +
 	 * "		commentfeed=\"" + sCommentFeed + "\"\n" + "		author=\"" + sAuthor + "\"\n" + "		date=\"" + sDate + "\"\n" + "		category=\"" + sCategory + "\"\n" + "		image=\"" + sImage + "\"\n" + "		url=\""
 	 * + sURL + "\"\n" + "		favorite=\"" + sFavorite + "\"\n" + "		read=\"" + sRead + "\" />\n\n"); }
 	 * 
 	 * XMLbuilder.append("</feed>"); String XML = XMLbuilder.toString();
 	 * 
 	 * FileOutputStream f = new FileOutputStream(file); f.write(XML.getBytes()); f.close(); } catch (Exception e) { Log.w("XML Write Error", e); } }
 	 */
 	
 	public static boolean isNetworkConnected(Context context)
 	{
 		ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
 		NetworkInfo ni = cm.getActiveNetworkInfo();
 		if (ni == null)
 		{
 			return false;
 		}
 		else
 			return true;
 	}
 	
 	public static void deleteTempFile()
 	{
 		File sdCard = Environment.getExternalStorageDirectory();
 		File dir = new File(sdCard.getAbsolutePath() + "/Android/data/" + PACKAGE_TAG);
 		File file = new File(dir, TEMP_TAG);
 		
 		file.delete();
 	}
 	
 	@SuppressLint("SimpleDateFormat")
 	public static String parseDate(Context context, String mDate)
 	{
 		SimpleDateFormat tFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
 		Date date = new Date();
 		try
 		{
 			date = tFormat.parse(mDate);
 		}
 		catch (ParseException e)
 		{
 			e.printStackTrace();
 		}
 		
 		final CharSequence ago = DateUtils.getRelativeDateTimeString(context, date.getTime(), System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS, 0);
 		
 		return ago.toString();
 	}
 	
 	@SuppressLint("SimpleDateFormat")
 	public static String parseRelativeDate(String mDate)
 	{
 		SimpleDateFormat tFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
 		Date date = new Date();
 		try
 		{
 			date = tFormat.parse(mDate);
 		}
 		catch (ParseException e)
 		{
 			e.printStackTrace();
 		}
 		
 		final CharSequence ago = DateUtils.getRelativeTimeSpanString(date.getTime(), System.currentTimeMillis(), DateUtils.SECOND_IN_MILLIS, 0);
 		
 		return ago.toString();
 	}
 	
 	@SuppressLint("SimpleDateFormat")
 	public static boolean isNewerDate(String nDate, String oDate)
 	{
 		SimpleDateFormat tFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");
 		Date newDate = new Date();
 		Date oldDate = new Date();
 		
 		try
 		{
 			newDate = tFormat.parse(nDate);
 		}
 		catch (ParseException e)
 		{
 			e.printStackTrace();
 		}
 		try
 		{
 			oldDate = tFormat.parse(oDate);
 		}
 		catch (ParseException e)
 		{
 			e.printStackTrace();
 		}
 		
 		return oldDate.before(newDate);
 	}
 }
