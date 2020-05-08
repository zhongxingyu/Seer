 package de.saschahlusiak.hrw.dienststatus.model;
 
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Stack;
 
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.apache.http.HttpResponse;
 import org.apache.http.StatusLine;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.xml.sax.Attributes;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 import android.content.Context;
 import android.os.AsyncTask;
 import android.util.Log;
 import android.view.View;
 import android.widget.Toast;
 import de.saschahlusiak.hrw.dienststatus.R;
 import de.saschahlusiak.hrw.dienststatus.news.NewsListActivity;
 import de.saschahlusiak.hrw.dienststatus.news.NewsListActivity.RefreshTask;
 
 public class NewsProvider {
 	private static final String CONTENT_URL = "http://portal.hs-weingarten.de/xml/web/rechenzentrum-intranet/aktuelles/-/101_INSTANCE_Ao9p?startpage=true&language=de&articleId=@articleId@&groupId=@groupId@&cur=@cur@&portletInstance=@portletInstanceName@&showIframe=@showIframe@&isArticle=@isArticle@&internet=true";
 	private static final String tag = NewsProvider.class.getSimpleName();
 	
 	public interface OnNewNewsItem {
 		public void onNewNewsItem(NewsItem item);
 	}
 	
 	private static class MyParser extends DefaultHandler {
 		OnNewNewsItem mListener;
 		Stack<String> current;
 		String s;
 		NewsItem news;
 		boolean inContent;
 
 		MyParser(OnNewNewsItem mListener) {
 			current = null;
 			news = null;
 			inContent = false;
 			this.mListener = mListener;
 		}
 
 		@Override
 		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
 			if (localName.equals("xml-dataset"))
 				news = new NewsItem();
 
 			if (inContent) {
 				int i;
 				s += "<" + localName;
 				for (i = 0; i < attributes.getLength(); i++) {
 					s += " " + attributes.getLocalName(i);
 					s += "=\"" + attributes.getValue(i) + "\"";
 				}
 				current.push(s);
 			}
 			s = "";
 
 			if (localName.equals("xml-data-1")
 			 || localName.equals("xml-data-2")
 			 || localName.equals("xml-data-3")
 			 || localName.equals("xml-data-4")) {
 				inContent = true;
 				current = new Stack<String>();
 			}
 		}
 
 		@Override
 		public void characters(char[] ch, int start, int length)
 				throws SAXException {
 			s += String.copyValueOf(ch, start, length);
 		}
 
 		@Override
 		public void endElement(String uri, String localName, String qName)
 				throws SAXException {
 			if (localName.equals("xml-dataset")) {
 				mListener.onNewNewsItem(news);
 				news = null;
 				current = null;
 			} else if (localName.equals("xml-data-1")) {
 				news.header = s;
 				current = null;
 				inContent = false;
 			} else if (localName.equals("xml-data-2")) {
 				news.title = s;
 				current = null;
 				inContent = false;
 			} else if (localName.equals("xml-data-3")) {
 				news.pictureURL = s;
 				current = null;
 				inContent = false;
 			} else if (localName.equals("xml-data-4")) {
 				news.teaser = s;
 				current = null;
 				inContent = false;
 			} else if (inContent) {
 				/* we are inside an element, record subelements */
 				if (s.equals("")) {
 					s = current.pop() + " />";
 				} else {
 					s = current.pop() + ">" + s + "</" + localName + ">";
 				}
 			}
 		}
 	}
 	public static String fetchNews(Context context, OnNewNewsItem mListener) {
 		try {
 			DefaultHttpClient client = new DefaultHttpClient();
 			final HttpResponse resp = client.execute(new HttpGet(CONTENT_URL));
 
 			final StatusLine status = resp.getStatusLine();
 			if (Thread.interrupted())
 				return context.getString(R.string.cancelled);
 			if (status.getStatusCode() != 200) {
 				Log.d(tag,
 						"HTTP error, invalid server status code: "
 								+ resp.getStatusLine());
 
 				return context.getString(R.string.invalid_http_status, resp.getStatusLine());
 			}
 
 			SAXParserFactory spf = SAXParserFactory.newInstance();
 			SAXParser sp = spf.newSAXParser();
 			if (Thread.interrupted())
 				return context.getString(R.string.cancelled);
 			sp.parse(resp.getEntity().getContent(), new MyParser(mListener));
 		} catch (UnknownHostException e) { 
 			Log.e(tag, e.getMessage());
 			return context.getString(R.string.connection_error);
 		} catch (Exception e) {
 			Log.e(tag, e.getMessage());
 			if (Thread.interrupted())
 				return context.getString(R.string.cancelled);
 			return context.getString(R.string.connection_error);
 		}
 
 		return null;
 	}
 }
