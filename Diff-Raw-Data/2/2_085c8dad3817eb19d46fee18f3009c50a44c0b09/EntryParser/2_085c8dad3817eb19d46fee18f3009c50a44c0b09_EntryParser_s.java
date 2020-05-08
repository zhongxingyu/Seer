 package thehome;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.logging.Logger;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.nodes.TextNode;
 import org.jsoup.select.Elements;
 
 import com.sun.syndication.feed.synd.SyndEntry;
 import com.sun.syndication.feed.synd.SyndFeed;
 
 public class EntryParser implements Runnable{
 	// XXX yahoo parser for now
 	
 	private static final Logger log = Logger.getLogger(TheHomeServlet.class.getName());
 	
 	private SyndEntry entry;
 	private HashMap<String, String> contents = new HashMap<String, String>(); // XXX TreeMap
 
 	public EntryParser(SyndEntry entry) {
 		this.entry = entry;
 	}
 	
 	public void run() {
 		contents.put("title", entry.getTitle());
 		contents.put("hash", Integer.toString(entry.hashCode()));
		contents.put("time", Long.toString((new Date()).getTime()));
 		try {
 			parseUrl(entry.getLink());
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	public HashMap<String, String> getContents() {
 		return contents;
 	}
 		
 	private void parseUrl(String url) throws IOException {
 		if (url.startsWith("http://rd.yahoo.co.jp")) {
 			url = url.substring(url.indexOf("*")+1);
 		}
 		Document document = Jsoup.connect(url).timeout(1000*20).get(); // XXX constant
 		contents.put("link", findDetailUrl(document));
 		contents.put("summary", findSummary(document));
 	}
 	
 	private String findSummary(Document document) {
 		String summary = "";
 		for (TextNode node: document.select("div#detailHeadline").first().textNodes()) {
 			if (!node.isBlank()) {
 				summary += node.text();
 			}
 			if (summary.length() > 0) break;
 		}
 		
 		return summary;		
 	}
 
 	private String findDetailUrl(Document document) {
 		Elements links = document.select("div#detailHeadline  a[href]");
 		String href = "";
 		ArrayList<String> tmpArr = new ArrayList<String>();
 		for (Element link : links) {
 			href = link.attr("href");
 			if (href.startsWith("http://headlines.yahoo.co.jp")) { // XXX got http://backnumber.dailynews.yahoo.co.jp/?m=7700474&e=food_service_industry
 				if (tmpArr.contains(href)) {
 					break;
 				} else {
 					tmpArr.add(href);
 				}
 			}
 		}
 		return href;		
 	}
 }
