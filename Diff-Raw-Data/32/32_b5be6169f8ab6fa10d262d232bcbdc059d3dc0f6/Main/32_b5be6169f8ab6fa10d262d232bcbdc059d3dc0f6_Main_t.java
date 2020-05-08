 package net.rssucker;
 
 
 import com.sun.syndication.io.XmlReader;
 
 import java.io.IOException;
 import java.net.URL;
 import java.util.List;
 
 public class Main {
 
	public static void main(String[] args) throws IOException, InterruptedException {
 //        System.out.println("Starting RSSucker...");
 		Config config = new Config(args);
 		List<Feed> feeds = new FeedFactory().createFrom(config);
 //        System.out.println("FeedCount: "+feeds.size());
		if (config.isVerbose()) System.out.println("Checking for new Items\n");
 
 		for (Feed feed : feeds) {
			try {
				feed.parse(new XmlReader(new URL(feed.getConfig().getAddress())));
				for (Item item : feed.getNewItems()) {
					try {
						if (config.isVerbose()) System.out.println("Trying to download " + item);
						UrlDownload.fileDownload(item.getUrl(), config.getDownloadLocation());
						feed.getConfig().setEpisode(item.getEpisode());
					} catch (NullPointerException e) {
						System.out.println("Error while getting: " + item.getUrl());
					}
 				}
			} catch (IOException ioEx) {
				System.out.println("Error while getting feed: " + feed.getConfig().getName() + " URL: " + feed.getConfig().getAddress());
				if (config.isVerbose()) ioEx.printStackTrace();
 			}
			Thread.sleep(2000L);
 		}
 		config.write();
 	}
 
 }
