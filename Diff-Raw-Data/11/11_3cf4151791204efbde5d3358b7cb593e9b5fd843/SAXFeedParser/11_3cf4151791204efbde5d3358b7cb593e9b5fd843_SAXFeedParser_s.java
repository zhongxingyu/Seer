 package net.wagnerism.podcast;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.xml.sax.Attributes;
 
 import android.sax.Element;
 import android.sax.EndElementListener;
 import android.sax.EndTextElementListener;
 import android.sax.RootElement;
 import android.sax.StartElementListener;
 import android.util.Log;
 import android.util.Xml;
 
 public class SAXFeedParser extends AbstractFeedParser {
 	
 	private Episode currentEpisode = new Episode();
 	private List<Episode> episodes = new ArrayList<Episode>();
 	
 	private RootElement root = new RootElement("rss");
 	private Element channel = root.getChild("channel");
 	private Element item = channel.getChild(ITEM);
 	
 	public SAXFeedParser(String feedUrl) {
 		super(feedUrl);
 		
 		item.setEndElementListener(new EndElementListener() {
 			public void end() {
 				Log.v(TAG, "Adding "+currentEpisode.toString());
				episodes.add(currentEpisode);
 			}
 		});
 		
 		item.getChild(TITLE).setEndTextElementListener(new EndTextElementListener() {
 			public void end(String body) {
 				currentEpisode.setTitle(body);
 			}
 		});
 		
 		item.getChild(DESCRIPTION).setEndTextElementListener(new EndTextElementListener() {
 			public void end(String body) {
 				currentEpisode.setDescription(body);
 			}
 		});
 		
 		item.getChild(LINK).setEndTextElementListener(new EndTextElementListener() {
 			public void end(String body) {
 				currentEpisode.setBlogUrl(body);
 			}
 		});
 		
 		item.getChild(ENCLOSURE).setStartElementListener(new StartElementListener() {
 			public void start(Attributes attr) {
 				String url = attr.getValue("url");
 				currentEpisode.setAudioUrl(url);
 			}
 		});
 		
 		
 	}
 	
 	public List<Episode> parse() {
 		
 		try	{
 			Xml.parse(this.getInputStream(), Xml.Encoding.UTF_8, root.getContentHandler());
 		} catch(Exception e) {
 			Log.e(TAG, "Unable to parse the rss feed: "+e.getMessage());
 		}
 		
 		return episodes;
 	}
 
 }
