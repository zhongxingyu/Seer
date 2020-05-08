 package modules;
 
 import static org.jibble.pircbot.Colors.*;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLDecoder;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Element;
 import org.jsoup.nodes.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 import debugging.Log;
 
 import main.Message;
 import main.NoiseModule;
 import static main.Utilities.*;
 
 /**
  * Wikipedia
  *
  * @author Michael Mrozek
  *         Created Jun 16, 2009.
  */
 public class Wikipedia extends NoiseModule {
 	private static final int MAXIMUM_MESSAGE_LENGTH = 400; // Approximately (512 bytes including IRC data)
 	private static final String COLOR_WARNING = YELLOW;
 	private static final String COLOR_ERROR = RED + REVERSE;
 	
 	@Command("\\.(?:wik|wp) (.+)")
 	public void wikipedia(Message message, String term) {
 		if(term.isEmpty()) { // Should be impossible
 			this.bot.sendMessage(COLOR_ERROR + "Missing term");
 			return;
 		}
 		
 		sendEntry(term, "http://en.wikipedia.org/wiki/" + urlEncode(fixTitle(term)));
 	}
 	
 	@Command(".*((?:http:\\/\\/en\\.wikipedia\\.org|https:\\/\\/secure\\.wikimedia\\.org\\/wikipedia(?:\\/commons|\\/en))\\/wiki\\/((?:\\S+)(?::[0-9]+)?(?:\\/|\\/(?:[\\w#!:.?+=&%@!\\-\\/]))?)).*")
 	public void wikipediaLink(Message message, String url, String term) {
 		sendEntry(urlDecode(term).replace("_", " "), url);
 	}
 	
 	private static String fixTitle(String term) {
 		String fixedTerm = term.replace(" ", "_");
 		if(Character.isLowerCase(fixedTerm.charAt(0)))
 			fixedTerm = Character.toUpperCase(fixedTerm.charAt(0)) + fixedTerm.substring(1);
 		return fixedTerm;
 	}
 	
 	/*
 	private static String normalizeTitle(String term) throws IOException, JSONException {
 		final JSONObject data = getJSON("http://en.wikipedia.org/w/api.php?action=query&titles=" + fixTitle(term) + "&redirects&format=json");
 		final JSONObject query = data.getJSONObject("query");
 		if(query.has("redirects"))
 			return query.getJSONArray("redirects").getJSONObject(0).getString("to");
 		else if(query.has("normalized"))
 			return query.getJSONArray("normalized").getJSONObject(0).getString("to");
 		else
 			return term;
 	}
 	*/
 
 	private String encoded(final String s) {
 		try {
 			final byte bytes[] = s.getBytes("UTF8");
 			return new String(bytes, "ISO8859_1");
 		} catch (UnsupportedEncodingException e) {
 			this.bot.sendMessage(COLOR_ERROR + "He looks like a fuckin' loser.");
 			return s;
 		}
 	}
 
 	private String selectEntryText(final String term, final String url, final Document doc) {
 		Element el = null;
 		if (el == null && term.contains("File:")) { // Image description on a Commons page
			el = doc.select("th#fileinfotpl_desc + td p").first();
 		}
 		if (el == null) { // First paragraph of any other page
 			doc.select("div#bodyContent > p").first();
 		}
 		return el == null ? "" : el.text();
 	}
 	
 	private void sendEntry(final String term, final String url) {
 		final Document doc;
 		try {
 			doc = Jsoup.connect(url).get();
 		} catch(IOException e) {
 			if(e.getMessage().contains("404 error loading URL"))
 				this.bot.sendMessage(COLOR_WARNING + "No entry for " + term);
 			else
 				this.bot.sendMessage(COLOR_ERROR + "Unable to connect to Wikipedia: " + e.getMessage());
 			return;
 		}
 		
 		String text = encoded(selectEntryText(term, url, doc));
 		while(text.length() + url.length() + 8 > MAXIMUM_MESSAGE_LENGTH && text.contains(" ")) {
 			text = text.substring(0, text.lastIndexOf(' '));
 		}
 		if(!text.endsWith("...")) {text += "\u00e2\u0080\u00a6";}
 		text += " \u00e2\u0080\u0094 " + url;
 		this.bot.sendMessage(text);
 	}
 	
 	@Command("\\.featured")
 	public void featured(Message message) {
 		try {
 			final DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
 			org.w3c.dom.Document doc = db.parse(new InputSource(new URL("http://jeays.net/wikipedia/featured.xml").openStream()));
 			final NodeList entryList = doc.getElementsByTagName("item");
 
 			if(entryList.getLength() != 1) {
 				this.bot.sendMessage(COLOR_ERROR + "Found " + entryList.getLength() + " items");
 				return;
 			}
 			
 			final Node itemNode = entryList.item(0);
 			final NodeList itemChildren = itemNode.getChildNodes();
 			for(int i = 0; i < itemChildren.getLength(); i++) {
 				final Node titleNode = itemChildren.item(i);
 				if(titleNode.getNodeName().equals("title")) {
 					final String title = titleNode.getTextContent();
 					final String[] titles = title.split(": ", 2);
 					if(titles.length != 2) {
 						this.bot.sendMessage(COLOR_ERROR + "Can't split title");
 						return;
 					}
 					
 					wikipedia(message, titles[1]);
 					return;
 				}
 			}
 			
 			this.bot.sendMessage(COLOR_ERROR + "No title found");
 		} catch(ParserConfigurationException e) {
 			this.bot.sendMessage(COLOR_ERROR + "Unable to parse Wikipedia data");
 			Log.e(e);
 		} catch(MalformedURLException e) {
 			this.bot.sendMessage(COLOR_ERROR + "Unable to contact Wikipedia");
 			Log.e(e);
 		} catch(SAXException e) {
 			this.bot.sendMessage(COLOR_ERROR + "Unable to contact Wikipedia");
 			Log.e(e);
 		} catch(IOException e) {
 			this.bot.sendMessage(COLOR_ERROR + "Unable to contact Wikipedia");
 			Log.e(e);
 		}
 	}
 
 	@Override public String getFriendlyName() {return "Wikipedia";}
 	@Override public String getDescription() {return "Returns the beginning of the wikipedia article for the specified term";}
 	@Override public String[] getExamples() {
 		return new String[] {
 				".wik _term_ -- Returns the beginning of the wikipedia article for _term_",
 				".wp _term_ -- Same as above",
 				".featured -- Show the wikipedia definition for the current featured article"
 		};
 	}
 }
