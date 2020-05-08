 package net.rssucker;
 
 
 import com.sun.syndication.io.XmlReader;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.List;
 
 public class Main {
 
	public static void main(String[] args) throws IOException {
 //        System.out.println("Starting RSSucker...");
 		Config config = new Config(args);
 		List<Feed> feeds = new FeedFactory().createFrom(config);
 //        System.out.println("FeedCount: "+feeds.size());
 
 		for (Feed feed : feeds) {
			feed.parse(new XmlReader(new URL(feed.getConfig().getAddress())));
			for (Item item : feed.getNewItems()) {
				try {
					if(config.isVerbose())System.out.println("Trying to download "+item);
					UrlDownload.fileDownload(item.getUrl(), config.getDownloadLocation());
					feed.getConfig().setEpisode(item.getEpisode());
				} catch (NullPointerException e) {
					System.out.println("Error while getting: " + item.getUrl());
 				}
 			}
 		}
 		config.write();


 	}
 
 }
