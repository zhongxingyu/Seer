 package nexi.sengoku.easy;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.List;
 
 import nexi.sengoku.easy.Resources.Wood;
 
 import org.apache.log4j.Logger;
 
 import com.gargoylesoftware.htmlunit.FailingHttpStatusCodeException;
 import com.gargoylesoftware.htmlunit.WebClient;
 import com.gargoylesoftware.htmlunit.html.HtmlPage;
 
 public class World {
 
 	private static final Logger logger = Logger.getLogger(World.class);
 
 	private final String worldsPageString;
 	private final int world;
 
 	private volatile Context context;
 
 	private volatile long wood;
 	private volatile long woodMax;
 	private volatile long cloth;
 	private volatile long clothMax;
 	private volatile long iron;
 	private volatile long ironMax;
 	private volatile long wheat;
 	private volatile long wheatMax;
 
 	public World(int world, String worldsPageString) {
 		this.world = world;
 		this.worldsPageString = worldsPageString;
 	}
 
 	public String getBaseUrl() {
 		return String.format("http://w%03d.sengokuixa.jp", world);
 	}
 	public void load() throws FailingHttpStatusCodeException, MalformedURLException, IOException {
 		WebClient webClient = Client.newWebClient();
 
 		logger.debug(String.format("%s&wd=w%03d", worldsPageString, world));
 		HtmlPage page = (HtmlPage) webClient.getPage(String.format("%s&wd=w%03d", worldsPageString, world));
 		logger.debug(webClient.getCookieManager().getCookies());
 
 		WebClient villageClient = Client.newWebClient(webClient.getCookieManager());
 
 		page = (HtmlPage) villageClient.getPage(getBaseUrl() + "/village.php");
 		logger.debug(page.asXml());
 		
		wood = Long.parseLong(page.getElementById("wood").getNodeValue());
		woodMax = Long.parseLong(page.getElementById("wood_max").getNodeValue());
		cloth = Long.parseLong(page.getElementById("cloth").getNodeValue());
		clothMax = Long.parseLong(page.getElementById("cloth_max").getNodeValue());
		iron = Long.parseLong(page.getElementById("iron").getNodeValue());
		ironMax = Long.parseLong(page.getElementById("iron_max").getNodeValue());
		wheat = Long.parseLong(page.getElementById("wheat").getNodeValue());
		wheatMax = Long.parseLong(page.getElementById("wheat_max").getNodeValue());
 		
 		page = (HtmlPage) villageClient.getPage(getBaseUrl() + "/facility/unit_status.php?dmo=all");
 		logger.info(page.asXml());
 	}
 	
 	public List<General> getGenerals() {
 		return null;
 	}
 
 	public List<Village> getVillages() {
 		return null;
 	}
 	
 	public GeneralMarket getGeneralMarket() {
 		return null;
 	}
 	
 	public Map getMap() {
 		return null;
 	}
 }
