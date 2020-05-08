 package com.github.yukinoraru.ToggleInventory;
 
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 public class UpdateChecker {
 
 	private ToggleInventory plugin;
 	private URL filesFeed;
 
 	private String version, link;
 
 	public String getVersion() {
 		return version;
 	}
 
 	public String getLink() {
 		return link;
 	}
 
 	public UpdateChecker(ToggleInventory plugin, String url){
 		this.plugin = plugin;
 
 		try {
 			this.filesFeed = new URL(url);
 		} catch (MalformedURLException e) {
			e.printStackTrace();
 		}
 	}
 
 	public boolean updateNeeded(){
 		try {
 			InputStream input = this.filesFeed.openConnection().getInputStream();
 			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input);
 
 			Node latestFile = document.getElementsByTagName("item").item(0);
 			NodeList children = latestFile.getChildNodes();
 
			this.version = children.item(1).getTextContent().replaceAll("[a-zA-Z ]", "");
 			this.link = children.item(3).getTextContent();
 
			//plugin.getLogger().info(this.version + " " + this.link);
 
			if(!plugin.getDescription().getVersion().equals(this.version)){
 				return true;
 			}
 
 		} catch (Exception e) {
			e.printStackTrace();
 		}
 		return false;
 	}
 
 }
