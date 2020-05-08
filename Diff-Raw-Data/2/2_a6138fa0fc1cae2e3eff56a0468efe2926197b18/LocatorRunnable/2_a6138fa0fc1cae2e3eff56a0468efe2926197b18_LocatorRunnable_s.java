 package com.wwsean08.locator;
 
 import java.net.URL;
 
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.SAXParserFactory;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.xml.sax.Attributes;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 import org.xml.sax.helpers.DefaultHandler;
 
 public class LocatorRunnable implements Runnable{
 	String name;
 	String sender;
 	private final String key = "get your own";
 	private final String lookup = "http://api.ipinfodb.com/v3/ip-country/?key=" + key + "&format=xml&ip="; 
 	public LocatorRunnable(String playerName, String senderName){
 		name = playerName;
 		sender = senderName;
 	}
 	@Override
 	public void run() {
		if(Bukkit.getServer().matchPlayer(name) !=null){
 			final Player player = Bukkit.getServer().matchPlayer(name).get(0);
 			String request = lookup + player.getAddress().getAddress().getHostAddress();
 			try {
 				SAXParserFactory factory = SAXParserFactory.newInstance();
 				SAXParser saxParser = factory.newSAXParser();
 				DefaultHandler handler = new DefaultHandler() {
 					boolean country = false;
 					public void startElement(String uri, String localName,String qName, Attributes attributes) throws SAXException {
 						if (qName.equalsIgnoreCase("countryName")) 
 							country = true;
 					}
 
 					public void endElement(String uri, String localName, String qName) throws SAXException {
 					}	//unneeded and unused
 
 					public void characters(char ch[], int start, int length) throws SAXException {
 						if (country) {
 							String location = new String(ch, start, length);
 							if(sender == null){
 								Bukkit.getServer().getLogger().info(ChatColor.GRAY + player.getName() + " is located in " + location.toLowerCase() + " according to their ip address");
 							}else
 								Bukkit.getPlayer(sender).sendMessage(ChatColor.GRAY + player.getName() + " is located in " + location.toLowerCase() + " according to their ip address");
 							country = false;
 						}
 					}
 				};
 				saxParser.parse(new InputSource(new URL(request).openStream()), handler);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}else
 			if(sender == null)
 				Bukkit.getServer().getLogger().info(ChatColor.GRAY + "There is no player by the name of " + name);
 			else
 				Bukkit.getPlayer(sender).sendMessage(ChatColor.GRAY + "There is no player by the name of " + name);
 	}
 }
