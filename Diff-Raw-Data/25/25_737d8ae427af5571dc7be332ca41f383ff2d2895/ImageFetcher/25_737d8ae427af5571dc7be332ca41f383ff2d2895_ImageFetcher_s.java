 package bll.admin;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import dal.admin.IImageSource;
 import dal.admin.IImageStore;
 import dal.admin.IKeywordsStore;
 import dal.admin.Image;
 import dal.admin.StoreFactory;
 import dal.admin.TwitterClient;
 import dal.admin.TwitterSource;
 
 
 public class ImageFetcher {
 	public static void main(String[] args) {
 		new ImageFetcher();
 	}
 
 	/**
 	 * Interval between fetches
 	 */
 	private final int INTERVAL = 5;
 
 	/**
 	 * Date format for log output
 	 */
 	private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
 
 	/**
 	 * Log output
 	 */
 	protected void log(String message) {
 		System.out.println("(" + DATE_FORMAT.format(new Date()) + ") " + message);
 	}
 


 	/**
 	 * Application main
 	 */
 	public ImageFetcher() {
 		System.out.println(
 			"-------------------------------------------------------\n" +
 			" Automatic image fetcher \n" +
 			"-   -    -    -    -    -    -    -    -    -    -    -\n" +
 			"\n" +
 			"This application will fetch images from Instagram and\n" +
 			"Twitter once every " + INTERVAL + " minutes. The result of\n"+
 			"each fetch is shown in this window:\n\n");
 
 		List<IImageSource> sources = new ArrayList<IImageSource>();
 		sources.add(new TwitterSource(new TwitterClient()));
 		// sources.add(new InstagramSource(new InstagramClient()));   // <-- To be implemented...
 
 		IImageStore imageStore = StoreFactory.getImageStore();
 		IKeywordsStore keywordsStore = StoreFactory.getKeywordsStore();
 		List<String> keywords = keywordsStore.getKeywords();
 		
		try {
			while (true) {
				
 				for (IImageSource source : sources) {
 					log("Starting to fetch from " + source.getClass().getName());
 					
 					for (String keyword : keywords) {
 						log("Fetching for keyword `" + keyword + "`...");
 						List<Image> images = source.getByKeyword(keyword, 100);
 						log("Finished. Got " + images.size() + " images. Inserting them to the database:");
 						for (Image image : images) {
 							imageStore.insert(image);
 						}
 					}
 				}
 
 				log("Going to sleep for " + INTERVAL + " minutes. (abort with Ctrl+c)");
 				Thread.sleep(INTERVAL * 60 * 1000);
			}
		} catch (InterruptedException e) {}
 	}
 }
