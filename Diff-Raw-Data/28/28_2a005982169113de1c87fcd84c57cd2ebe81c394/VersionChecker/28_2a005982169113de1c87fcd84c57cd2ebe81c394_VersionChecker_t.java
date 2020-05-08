 package com.cyprias.invisibilityviewer;
 
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.bukkit.event.Event;
 import org.bukkit.event.HandlerList;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 public class VersionChecker {
 	private String curseRSS;
 	private JavaPlugin plugin;
 	//private Logger log = Logger.getLogger("Minecraft");
 
 	private String pluginName;
 
 	public VersionChecker(JavaPlugin plugin, String curseRSS) {
 		this.plugin = plugin;
 		this.curseRSS = curseRSS;
 		this.pluginName = plugin.getName();
 	}
 
 	public void retreiveVersionInfo(Object... args) {
 		getVersionInfoTask task = new getVersionInfoTask(this);
 		int taskID = plugin.getServer().getScheduler().scheduleAsyncDelayedTask(plugin, task, 0L);
 		task.setId(taskID);
 		task.setArgs(args);
 	}
 
 	public static int compareVersions(String a, String b){
 		String[] aParts = a.split("\\.");
 		String[] bParts = b.split("\\.");
 
 		int aInt, bInt;
 		
 		int i=0;
 		while (aParts.length > i && bParts.length > i) {
 			aInt = Integer.parseInt(aParts[i]);
 			bInt = Integer.parseInt(bParts[i]);
 			if (aInt != bInt){
 				return aInt - bInt;
 			}
 			i+=1;
 		}
 		return 0;
 	}
 	
 	List<versionInfo> versions = new ArrayList<versionInfo>();
 
 	public static class versionInfo {
 		private String title;
 		private String link;
 		private String description[];
 		private String pubDate;
 
 		public versionInfo(String title2, String link2, String pubDate2, String description2) {
 			this.title = title2;
 			this.link = link2;
 			this.pubDate = pubDate2;
 			description2 = description2.replaceAll("\\<.*?>", "");
 			this.description = description2.split("\\r?\\n");
 		}
 
 		public String getTitle() {
 			return this.title;
 		}
 
 		public String getLink() {
 			return this.link;
 		}
 
 		public String getPubDate() {
 			return this.pubDate;
 		}
 
 		public String[] getDescription() {
 			return this.description;
 		}
 	}
 
 	private class getVersionInfoTask implements Runnable {
 		private VersionChecker me;
 		private Object[] args;
 
 
 		public getVersionInfoTask(VersionChecker me2) {
 			this.me = me2;
 		}
 		public void setArgs(Object... args) {
 			this.args = args;
 		}
 
 		
 		private int taskID;
 		public void setId(int n) {
 			this.taskID = n;
 		}
 
 		@Override
 		public void run() {
 			try {
 				URL url = new URL(me.curseRSS);
 				Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(url.openConnection().getInputStream());
 				doc.getDocumentElement().normalize();
 				NodeList nodes = doc.getElementsByTagName("item");
 
 				Element element, subElement;
 				NodeList subNodes;
 
 				//log.info(ChatColor.WHITE + "getVersionInfoTask run B getLenth: " + nodes.getLength());
 
 				if (nodes.getLength() > 0) {
 					versions.clear();
 
 					String title, link, pubDate, description;
 					for (int v = 0; v < nodes.getLength(); v++) {
 
 						element = (Element) nodes.item(v);
 						subNodes = element.getElementsByTagName("title");
 						subElement = (Element) subNodes.item(0);
 						title = subElement.getChildNodes().item(0).getNodeValue();
 
 						subNodes = element.getElementsByTagName("link");
 						subElement = (Element) subNodes.item(0);
 						link = subElement.getChildNodes().item(0).getNodeValue();
 
 						subNodes = element.getElementsByTagName("pubDate");
 						subElement = (Element) subNodes.item(0);
 						pubDate = subElement.getChildNodes().item(0).getNodeValue();
 
 						subNodes = element.getElementsByTagName("description");
 						subElement = (Element) subNodes.item(0);
 						description = subElement.getChildNodes().item(0).getNodeValue();
 						versions.add(new versionInfo(title, link, pubDate, description));
 					}
 				}
 
 			} catch (Exception localException) {
 			}
 			VersionCheckerEvent event = new VersionCheckerEvent(me.plugin.getName(), versions, this.args);
 			me.plugin.getServer().getPluginManager().callEvent(event);
 		}
 	}
 
 	public static class VersionCheckerEvent extends Event {
 		private static final HandlerList handlers = new HandlerList(); 
 		public HandlerList getHandlers() { return handlers; }
 		public static HandlerList getHandlerList() { return handlers; }
 		 
 		private List<versionInfo> versions;
 
 		private String pluginName;
 		private Object[] args;
 		public VersionCheckerEvent(String pluginName, List<versionInfo> versions, Object... args) {
 			this.pluginName = pluginName;
 			this.versions = versions;
 			this.args = args;
 		}
 
 		public String getPluginName() {
 			return this.pluginName;
 		}
 
 		public int getVersionCount() {
 			return versions.size();
 		}
 
 		public versionInfo getVersionInfo(int index) {
			if (versions.size() >= (index+1))
				return versions.get(index);
			
			return null;
 		}
 
 		public Object[] getArgs(){
 			return this.args;
 		}
 		
 	}
 	
 
 	
 }
