 package modules;
 
 import static org.jibble.pircbot.Colors.*;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.Scanner;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 import main.Message;
 import main.NoiseModule;
 import static main.Utilities.urlEncode;
 
 /**
  * Wikipedia
  *
  * @author Michael Mrozek
  *         Created Jun 16, 2009.
  */
 public class Wikipedia extends NoiseModule {
 	private static final int MAXIMUM_MESSAGE_LENGTH = 400; // Approximately (512 bytes including IRC data)
 	private static final String COLOR_ERROR = RED + REVERSE;
 	
 	@Command("\\.(?:wik|wp) (.*)")
 	public void wikipedia(Message message, String term) {
 		try {
 			final String url = "http://en.wikipedia.org/wiki/" + term.replace(" ", "_");
 			final URLConnection c = new URL("http://mrozekma.com/wikipedia.php?term=" + urlEncode(term.replace(" ", "_"))).openConnection();
 			final Scanner s = new Scanner(c.getInputStream());
 			String text = s.nextLine();
 			
 			if(text.charAt(0) == '!') {
 				this.bot.reply(message, text.substring(1));
 				return;
 			}
 			
 			while(text.length() + url.length() + 4 > MAXIMUM_MESSAGE_LENGTH && text.contains(" ")) {
 				text = text.substring(0, text.lastIndexOf(' '));
 			}
 			if(!text.endsWith("...")) {text += "...";}
 			System.out.println(text.length() + ", " + url.length());
 			
 			this.bot.reply(message, text + " " + url);
 		} catch(IOException e) {
 			this.bot.reply(message, COLOR_ERROR + "Unable to connect to Wikipedia");
 			e.printStackTrace();
 		}
 		
 		/*
 		try {
 			final JSONObject json = getJSON("http://js-wp.dg.cx/json/" + urlEncode(term.replace(" ", "_")));
 			if(json.isNull("text")) {
 				this.bot.reply(message, "No entry for " + term);
 			} else {	
 				String text = json.getString("text");
 				final String url = json.getString("url");
 				
 				while(text.length() + url.length() + 4 > MAXIMUM_MESSAGE_LENGTH && text.contains(" ")) {
 					text = text.substring(0, text.lastIndexOf(' '));
 				}
 				if(!text.endsWith("...")) {text += "...";}
 				System.out.println(text.length() + ", " + url.length());
 				
 				this.bot.reply(message, text + " " + url);
 			}
 		} catch(IOException e) {
 			this.bot.reply(message, COLOR_ERROR + "Unable to connect to Wikipedia");
 			e.printStackTrace();
 		} catch(JSONException e) {
 			this.bot.reply(message, COLOR_ERROR + "Problem parsing Wikipedia response");
 		}
 		*/
 	}
 	
 	@Command("\\.featured")
 	public void featured(Message message) {
 		DocumentBuilder db;
 		try {
 			db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
 			Document doc = db.parse(new InputSource(new URL("http://jeays.net/wikipedia/featured.xml").openStream()));
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
 			e.printStackTrace();
 		} catch(MalformedURLException e) {
 			this.bot.sendMessage(COLOR_ERROR + "Unable to contact Wikipedia");
 			e.printStackTrace();
 		} catch(SAXException e) {
 			this.bot.sendMessage(COLOR_ERROR + "Unable to contact Wikipedia");
 			e.printStackTrace();
 		} catch(IOException e) {
 			this.bot.sendMessage(COLOR_ERROR + "Unable to contact Wikipedia");
 			e.printStackTrace();
 		}
 	}
 
 	@Override public String getFriendlyName() {return "Wikipedia";}
 	@Override public String getDescription() {return "Returns the beginning of the wikipedia article for the specified term";}
 	@Override public String[] getExamples() {
 		return new String[] {
 				".wik _term_ -- Returns the beginning of the wikipedia article for _term_",
 				".wp _term_ -- Same as above"
 		};
 	}
 	@Override public String getOwner() {return "Morasique";}
 }
