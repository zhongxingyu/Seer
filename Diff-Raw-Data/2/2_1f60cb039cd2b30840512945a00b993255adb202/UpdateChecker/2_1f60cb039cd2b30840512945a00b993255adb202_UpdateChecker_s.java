 package musician101.controlcreativemode.util;
 
 import java.io.InputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import musician101.controlcreativemode.ControlCreativeMode;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 /**
  * Checks the RSS feed for an update.
  * 
  * @author Musician101
  */
 public class UpdateChecker 
 {
 	private ControlCreativeMode plugin;
 	private URL filesFeed;
 	private String version;
 	private String link;
 	
 	/**
 	 * @param plugin References the main class.
	 * @param url The URL to the files.rss
 	 */
 	public UpdateChecker(ControlCreativeMode plugin, String url)
 	{
 		this.plugin = plugin;
 		try
 		{
 			this.filesFeed = new URL(url);
 		}
 		catch (MalformedURLException e)
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * @return True if there's an update available.
 	 */
 	public boolean updateNeeded()
 	{
 		try
 		{
 			InputStream input = this.filesFeed.openConnection().getInputStream();
 			Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(input);
 			Node latestFile = document.getElementsByTagName("item").item(0);
 			NodeList children = latestFile.getChildNodes();
 			version = children.item(1).getTextContent().replaceAll("[a-zA-Z ]", "");
 			link = children.item(3).getTextContent();
 			
 			if (Double.parseDouble(plugin.getDescription().getVersion()) < Double.parseDouble(this.version))
 				return true;
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 		return false;
 	}
 	
 	/**
 	 * @return the version of newest release.
 	 */
 	public String getVersion()
 	{
 		return this.version;
 	}
 	
 	/**
 	 * @return the link to the RSS feed.
 	 */
 	public String getLink()
 	{
 		return this.link;
 	}
 }
