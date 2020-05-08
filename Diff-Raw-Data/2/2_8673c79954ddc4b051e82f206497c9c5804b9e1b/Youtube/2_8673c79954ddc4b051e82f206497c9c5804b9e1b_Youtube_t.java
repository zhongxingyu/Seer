 package modules;
 
 import static org.jibble.pircbot.Colors.*;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 import main.Message;
 import main.NoiseModule;
 
 /**
  * Youtube
  *
  * @author Michael Mrozek
  *         Created Jun 17, 2009.
  */
 public class Youtube extends NoiseModule {
 	private static final String COLOR_ERROR = RED;
 	private static final String COLOR_INFO = PURPLE;
 	
	@Command(".*https?://www.youtube.com/(?:watch\\?v=|user/.*\\#p/u/[0-9]+/)([A-Za-z0-9_-]{11}).*")
 	public void youtube(Message message, String videoID) {
 		try {
 			final DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
 			Document doc = db.parse(new InputSource(new URL("http://gdata.youtube.com/feeds/api/videos/" + videoID).openStream()));
 			final NodeList entryList = doc.getElementsByTagName("entry");
 
 			// There should be exactly one entry for every video ID
 			if(entryList.getLength() == 0) {
 				this.bot.sendMessage(COLOR_ERROR + "Unable to find Youtube video with ID " + videoID);
 			} else if(entryList.getLength() != 1) {
 				this.bot.sendMessage(COLOR_ERROR + "Found " + entryList.getLength() + " videos with ID " + videoID);
 			}
 			
 			final Node entry = entryList.item(0);
 			
 			String author = null, title = null;
 			int duration = 0, viewCount = 0;
 			
 			final NodeList entryRootNodes = entry.getChildNodes();
 			for(int i = 0; i < entryRootNodes.getLength(); i++) {
 				final Node entryRootNode = entryRootNodes.item(i);
 				if(entryRootNode.getNodeName().equals("author")) {
 					final NodeList authorNodes = entryRootNode.getChildNodes();
 					for(int j = 0; j < authorNodes.getLength(); j++) {
 						final Node authorNode = authorNodes.item(j);
 						if(authorNode.getNodeName().equals("name")) {
 							author = authorNode.getTextContent();
 							continue;
 						}
 					}
 				} else if(entryRootNode.getNodeName().equals("media:group")) {
 					final NodeList mediaNodes = entryRootNode.getChildNodes();
 					for(int j = 0; j < mediaNodes.getLength(); j++) {
 						final Node mediaNode = mediaNodes.item(j);
 						if(mediaNode.getNodeName().equals("media:title")) {
 							title = mediaNode.getTextContent();
 							continue;
 						} else if(mediaNode.getNodeName().equals("yt:duration")) {
 							duration = Integer.parseInt(mediaNode.getAttributes().getNamedItem("seconds").getNodeValue());
 							continue;
 						}
 					}
 				} else if(entryRootNode.getNodeName().equals("yt:statistics")) {
 					viewCount = Integer.parseInt(entryRootNode.getAttributes().getNamedItem("viewCount").getNodeValue());
 					continue;
 				}
 			}
 
 			if(author != null  && title != null) {
 				this.bot.sendMessage(COLOR_INFO +  title + " (posted by " + author + ", " + duration + " seconds, " + viewCount + " views)");
 			} else {
 				this.bot.sendMessage(COLOR_ERROR + "Problem parsing Youtube data");
 			}
 		} catch(ParserConfigurationException e) {
 			this.bot.sendMessage(COLOR_ERROR + "Unable to parse Youtube data");
 			e.printStackTrace();
 		} catch(MalformedURLException e) {
 			this.bot.sendMessage(COLOR_ERROR + "Unable to contact Youtube");
 			e.printStackTrace();
 		} catch(SAXException e) {
 			this.bot.sendMessage(COLOR_ERROR + "Unable to contact Youtube");
 			e.printStackTrace();
 		} catch(IOException e) {
 			this.bot.sendMessage(COLOR_ERROR + "Unable to contact Youtube");
 			e.printStackTrace();
 		}
 	}
 	
 	@Override public String getFriendlyName() {return "Youtube";}
 	@Override public String getDescription() {return "Outputs information about any youtube URLs posted";}
 	@Override public String[] getExamples() {
 		return new String[] {
 				"http://www.youtube.com/watch?v=Yu_moia-oVI"
 		};
 	}
 	@Override public String getOwner() {return "Morasique";}
 }
