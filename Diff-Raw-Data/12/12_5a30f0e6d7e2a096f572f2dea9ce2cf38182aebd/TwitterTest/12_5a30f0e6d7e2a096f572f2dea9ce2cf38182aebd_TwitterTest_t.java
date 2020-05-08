 import java.net.URL;
 import java.util.LinkedList;
 import java.util.List;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 class Entry {
 
 	public int id;
 	public String url;
 	
 	@Override
 	public boolean equals(Object o) {
 		Entry e = (Entry) o;
 		return this.url.equals(e.url);
 	}
 	
 	@Override
 	public int hashCode() {
 		return this.url.hashCode();
 	}
 	
 }
 
 public class TwitterTest {
 
 	static final String apiURL = "http://search.twitter.com/search.atom";
 
 	public static void searchRecentTweets(String hashtag) {
 		try {
 			int pageNum = 1;
			int count = 1;
 			List<Entry> result = new LinkedList<Entry>();
 			outer : while(true) {
 				String queryURL = apiURL + "?q=%23" + hashtag
 						+ "&include_entities=true&rpp=100&result_type=recent&page=" + pageNum;
 				URL url = new URL(queryURL);
 				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
 				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
 				Document doc = dBuilder.parse(url.openStream());
 				NodeList nList = doc.getElementsByTagName("uri");
 				
 				for(int i = 0; i < nList.getLength(); ++i) {
 					Node n = nList.item(i);
 					Entry e = new Entry();
					e.id = count;
 					e.url = n.getTextContent();
 					if(!result.contains(e)) {
 						result.add(e);
						++count;
 					}
 					if(result.size() == 100) {
 						break outer;
 					}
 				}
				if(nList.getLength() < 100) {
					break;				
				}
 				++pageNum;
 			}
 			for(Entry e : result) {
 				System.out.println("Tweet #" + e.id + ": " + e.url);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	public static void main(String[] args) {
 		if (args.length != 1) {
 			System.out.println("Error: please enter one hashtag.");
 			return;
 		}
 
 		String hashtag = args[0];
 		if (hashtag.charAt(0) != '#') {
 			System.out.println("Error: invalid hashtag.");
 			return;
 		}
 
 		searchRecentTweets(hashtag.substring(1));
 	}
 
 }

