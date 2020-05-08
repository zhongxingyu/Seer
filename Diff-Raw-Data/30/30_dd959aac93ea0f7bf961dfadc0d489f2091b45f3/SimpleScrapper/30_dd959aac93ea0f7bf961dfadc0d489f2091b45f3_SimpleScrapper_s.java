 package server;
 
import java.io.FileNotFoundException;

 import org.webharvest.definition.ScraperConfiguration;
 import org.webharvest.runtime.Scraper;
 
 
 public class SimpleScrapper {
 	static Scraper scrapper;
 	
 	public static void main(String[] args){
 		try {
 			ScraperConfiguration config = 
					new ScraperConfiguration("i:/MyJava/WizzAirScrapper/resources/wizzairScrapperConfig.xml");
 			
 			scrapper = new Scraper(config, "/");
			scrapper.setDebug(false);
 			scrapper.execute();
			System.out.println(scrapper.getContext().getVar("WAsearch"));
		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} 
 	}
 
 }
