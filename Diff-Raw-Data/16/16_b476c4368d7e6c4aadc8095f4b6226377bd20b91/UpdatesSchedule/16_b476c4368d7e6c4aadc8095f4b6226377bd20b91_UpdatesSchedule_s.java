 package me.arno.blocklog.schedules;
 
 import java.net.URL;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import me.arno.blocklog.util.Util;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 public class UpdatesSchedule implements Runnable {
 	private String current;
 	private String latest;
 	
 	public UpdatesSchedule(String currentVersion) {
 		this.current = currentVersion;
 	}
 
 	@Override
 	public void run() {
 		try {
         	URL url = new URL("http://dev.bukkit.org/server-mods/block-log/files.rss");
             Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(url.openConnection().getInputStream());
             doc.getDocumentElement().normalize();
             NodeList nodes = doc.getElementsByTagName("item");
             Node firstNode = nodes.item(0);
             if (firstNode.getNodeType() == 1) {
                 Element firstElement = (Element) firstNode;
                 NodeList firstElementTagName = firstElement.getElementsByTagName("title");
                 Element firstNameElement = (Element) firstElementTagName.item(0);
                 NodeList firstNodes = firstNameElement.getChildNodes();
                 latest = firstNodes.item(0).getNodeValue().replace("BlockLog", "").trim();
             }
 			
             String[] currentVersion = current.split("\\.");
             String[] latestVersion = latest.split("\\.");
             
             boolean updateAvailable = false;
             
            for(int i=0;i<currentVersion.length;i++) {
            	if(Integer.valueOf(latestVersion[i]) > Integer.valueOf(currentVersion[i]))
            		updateAvailable = true;
             }
 			
 			if(updateAvailable) {
 				Util.sendNotice("BlockLog v" + latest + " is released! You're using BlockLog v" + current);
 				Util.sendNotice("Update BlockLog at http://dev.bukkit.org/server-mods/block-log/");
 			}
         } catch (Exception e) {
         	// This happens when dev.bukkit.org is offline
         }
 	}
 }
