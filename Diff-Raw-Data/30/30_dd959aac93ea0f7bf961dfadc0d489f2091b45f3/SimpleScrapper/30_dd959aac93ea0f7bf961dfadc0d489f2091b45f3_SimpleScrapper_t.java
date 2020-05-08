 package server;
 
 import org.webharvest.definition.ScraperConfiguration;
 import org.webharvest.runtime.Scraper;
import org.xml.sax.InputSource;
 
 
 public class SimpleScrapper {
 	static Scraper scrapper;
 	
 	public static void main(String[] args){
 		try {
		//TODO Move wizzairScrapperConfig.xml to classpath resourses
			
			InputSource in =  new InputSource(SimpleScrapper.class.
					getClassLoader().getResourceAsStream("wizzairScrapperConfig.xml"));
			
 			ScraperConfiguration config = 
					new ScraperConfiguration(in);
			
 			
 			scrapper = new Scraper(config, "/");
			scrapper.addVariableToContext("fromAirport", "IEV");
			scrapper.addVariableToContext("toAirport", "GRO");
			scrapper.addVariableToContext("fromDate", "10-11-2013");
			scrapper.addVariableToContext("paxAdult", "1");
			scrapper.addVariableToContext("paxChild", "0");
			scrapper.addVariableToContext("fromInfant", "0");
			scrapper.setDebug(true);
 			scrapper.execute();

			System.out.println("WizzAir club prices " + scrapper.getContext().getVar("pricesWAClub"));
			System.out.println("No club prices " + scrapper.getContext().getVar("pricesNoClub"));
		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} 
 	}
 
 }
