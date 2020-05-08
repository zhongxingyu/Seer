 package fetcher;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 import common.dal.IImageStore;
 import common.dal.IKeywordsStore;
 import common.dal.Image;
 import common.dal.StoreFactory;
 
 import fetcher.dal.IImageSource;
 import fetcher.dal.InstagramClient;
 import fetcher.dal.InstagramSource;
 import fetcher.dal.TwitterClient;
 import fetcher.dal.TwitterSource;
 
 
 public class MainController {
 	public static void main(String[] args) {
 		new MainController();
 	}
 
 	/**
 	 * Interval between fetches
 	 */
 	private final int INTERVAL = 5;
 	
 	/**
 	 * How many pictures do we need in the database?
 	 */
 	private final int LIMIT = 100;
 
 	/**
 	 * Date format for log output
 	 */
 	private final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
 
 	/**
 	 * Log output
 	 */
 	protected void log(String message) {
 		System.out.println("(" + DATE_FORMAT.format(new java.util.Date()) + ") " + message);
 	}
 
 	/**
 	 * Application main
 	 */
 	public MainController() {
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
 		sources.add(new InstagramSource(new InstagramClient()));
 
 		IImageStore imageStore = StoreFactory.getImageStore();
 		IKeywordsStore keywordsStore = StoreFactory.getKeywordsStore();
 		List<String> keywords = keywordsStore.getKeywords();
 		
 		int numImagesForEachKeyword = (int)(LIMIT/keywords.size()/sources.size());
 		
 		while (true) {
 
 			String sourceClass = "";
 			
 			for (IImageSource source : sources) {
 				sourceClass = source.getClass().getName();
 				log("Starting to fetch from " + source.getClass().getName());
 					
 				try {
 					for (String keyword : keywords) {
 						log("Fetching for keyword `" + keyword + "`...");
 						List<Image> images = source.getByKeyword(keyword, numImagesForEachKeyword);
 						log("Finished. Got " + images.size() + " images. Inserting them to the database:");
						
						int count = 0;
 						for (Image image : images) {
							if (imageStore.insert(image)) {
								count++;
							}
 						}
						
						log("Done. Inserted " + count + " images to the database. The others failed or were duplicates.");
 				}
 
 
 				} catch (Exception e) {
 					e.printStackTrace();
 					log("Could not fetch from `" + sourceClass + "`. Trying again in " + INTERVAL + " minutes.");
 				}
 			}
 
 			try {
 				log("Going to sleep for " + INTERVAL + " minutes. (abort with Ctrl+c)");
 				Thread.sleep(INTERVAL * 60 * 1000);
 			} catch (InterruptedException e) {}
 		}
 	}
 }
