 package com.exp.rss;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.List;
 
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 
 import com.sun.syndication.feed.synd.SyndEntry;
 import com.sun.syndication.feed.synd.SyndFeed;
 import com.sun.syndication.fetcher.FetcherException;
 import com.sun.syndication.fetcher.impl.FeedFetcherCache;
 import com.sun.syndication.fetcher.impl.HashMapFeedInfoCache;
 import com.sun.syndication.fetcher.impl.HttpURLFeedFetcher;
 import com.sun.syndication.io.FeedException;
 
 @Controller
 public class RssController {
 
 	private URL url[] = new URL[4];
 	private SyndFeed syndFeeds[]; // = new SyndFeed[4];
 	private ModelAndView mav = new ModelAndView();
 
 	private FeedFetcherCache cache = HashMapFeedInfoCache.getInstance();
 	private HttpURLFeedFetcher feedFetcher = new HttpURLFeedFetcher(cache);
 	private List<ContentModel> items = new ArrayList<ContentModel>();
 
 	@RequestMapping(value = "/rss", method = RequestMethod.GET)
 	public ModelAndView getFeedInRss() throws IllegalArgumentException,
 			FeedException, IOException, FetcherException {
 
		if (null == syndFeeds) {
 			syndFeeds = new SyndFeed[4];
 
 			try {
 				url[0] = new URL("http://rss.kauppalehti.fi/rss/omaraha.jsp");
 				url[1] = new URL(
 						"http://rss.kauppalehti.fi/rss/yritysuutiset.jsp");
 				url[2] = new URL("http://rss.kauppalehti.fi/rss/auto.jsp");
 				url[3] = new URL("http://rss.kauppalehti.fi/rss/luetuimmat.jsp");
 
 				for (int j = 0; j < syndFeeds.length; j++) {
					syndFeeds[j] = feedFetcher.retrieveFeed(url[3]);
 
 					// SyndFeed feed = feedFetcher.retrieveFeed(url[j]);
 					// if (null == feed || feed.getEntries().size() <= 0) {
 					// SyndFeedInput input = new SyndFeedInput();
 					// XmlReader xmlReader = new XmlReader(url[j]);
 					// XmlReader.setDefaultEncoding("UTF-8");
 					// feed = input.build(xmlReader);
 
 					// }
 
 					@SuppressWarnings("unchecked")
 					List<SyndEntry> entries = syndFeeds[j].getEntries();
 
 					for (int i = 0; i < entries.size(); ++i) {
 						ContentModel content = new ContentModel();
 						SyndEntry syndEntry = (SyndEntry) entries.get(i);
 
 						String desc = syndEntry.getDescription().getValue();
 						content.setUrl(syndEntry.getUri());
 						content.setData(desc);
 						content.setNewsDate(syndEntry.getPublishedDate());
 						content.setTitle(syndEntry.getTitle());
 						items.add(content);
 					}
 				}
 				Collections.sort(items, new Comparator<ContentModel>() {
 					public int compare(ContentModel m2, ContentModel m1) {
 						return m1.getNewsDate().compareTo(m2.getNewsDate());
 					}
 				});
 				mav.setViewName("rssViewer");
 				mav.addObject("feedContent", items);
 
 			} catch (MalformedURLException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
		}
 
 		return mav;
 
 	}
 
 }
