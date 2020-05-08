 package nexi.sengoku.easy;
 
 import org.apache.log4j.BasicConfigurator;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 
 import com.gargoylesoftware.htmlunit.html.HtmlPage;
 
 public class SengokuEasy {
 
 	private static final Logger logger = Logger.getLogger(SengokuEasy.class);
 
 	public static void main (String... args) throws Exception {
 		logger.info("Lawrence testing checkin");
 		BasicConfigurator.configure();
 		Logger.getRootLogger().setLevel(Level.DEBUG);
 		Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(Level.ERROR);
 		Logger.getLogger("org.apache.http").setLevel(Level.ERROR);
 		Logger.getLogger("nexi.sengoku.easy.Auth").setLevel(Level.ERROR);
 
 		logger.info("logging in to yahoo");
 		HtmlPage page = new Auth().loginToYahooWithRetry();
 		logger.info("logged in to yahoo");
 
 		logger.info("logging in to world server");
		HtmlPage worldsPage = page.getAnchorByText("���`�ॹ���`��").click();
 		logger.info("logged in to world server");
 		
 		World world9 = new World(9, worldsPage.getUrl().toString());
 		world9.load();
 		
 		Thread.sleep(30000L);
 	}
 }
