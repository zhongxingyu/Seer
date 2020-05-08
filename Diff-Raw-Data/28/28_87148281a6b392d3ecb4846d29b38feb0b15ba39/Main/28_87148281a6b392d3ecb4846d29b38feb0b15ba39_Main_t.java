 package rezend.musix;
 
 import rezend.musix.core.ProductDAO;
 import rezend.musix.core.Product;
 import rezend.musix.core.LinksList;
 import rezend.musix.core.ProductParser;
 import rezend.musix.crawler.CrawlerControl;
 
 /**
  * This application contains a web crawler that fetches every link from a
  * reference website. Links are processed in parallel so that those that
  * corresponds to product page are extracted and have their data stored to a
  * MySQL database. Products' price are exhibit as graphics. Therefore, an OCR
  * process is applied to extract the values.
  * 
  * Since it depends on OCR to obtain the relevant data, the success rate depends
  * on the quality of the graphics representing the values. This way, the
 * efficiency of this application cannot be fully guaranteed. Fine-tunning may
 * be required.
  * 
  * Crawler+producer and consumer are executed in different threads. They can run
  * indefinitely. However, a limit of products is set so that the application can
  * proceed retrieving and showing the stored data.
  * 
  * @author Rafael Rezende
  * 
  */
 public class Main {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 
 		// creation of the unique instance containing the list of links
 		LinksList pList = LinksList.getInstance();
 		// connect to database and create table if not exists
 		ProductDAO.connect();
 
 		// starts the web crawler in parallel
 		CrawlerControl.startCrawler();

 		// routine to consume the links from the list of links
		for (int productCounter = 0; productCounter < 200;) {
 			String urlStr = pList.get();
 			Product prod = ProductParser.parseProductFromURL(urlStr);
 			if (prod == null)
 				System.out.println("No product found in the page: " + urlStr);
 			else {
 				ProductDAO.addProduct(prod);
 				productCounter++;
 			}
 		}
 
 		/* example of how a product is processed and stored */
		// Product prod = ProductParser
		// .parseProductFromURL("http://www.musik-produktiv.ch/audio-technica-m3-in-ear-system.aspx");
		// ProductDAO.addProduct(prod);

		System.out.println("\nPrint values from the database:\n");
 		
 		ProductDAO.printProducts();
		
		System.exit(0);
 	}
 }
