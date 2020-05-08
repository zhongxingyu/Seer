 package edu.uci.ics.crawler4j.crawler;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import org.apache.log4j.Logger;
 import edu.uci.ics.crawler4j.cache.ICacheProvider;
 import edu.uci.ics.crawler4j.crawler.CrawlerController.URLManager;
 import edu.uci.ics.crawler4j.crawler.configuration.ICrawlerSettings;
 import edu.uci.ics.crawler4j.crawler.fetcher.IPageFetcher;
 import edu.uci.ics.crawler4j.crawler.fetcher.PageFetchStatus;
 import edu.uci.ics.crawler4j.extractor.IPageParser;
 import edu.uci.ics.crawler4j.extractor.PageParserManager;
 import edu.uci.ics.crawler4j.frontier.DocID;
 import edu.uci.ics.crawler4j.frontier.IDocIDServer;
 import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
 import edu.uci.ics.crawler4j.url.URLCanonicalizer;
 import edu.uci.ics.crawler4j.url.WebURL;
 
 public class CrawlRunner implements Runnable {
 	private static final Logger logger = Logger.getLogger(CrawlRunner.class);
 	
 	private Thread t;
 	private URLManager urlManager;
 	private ICrawlerSettings config;
 	private IPageFetcher pageFetcher;
 	private IPageVisitValidator visitValidator;
 	private ICacheProvider cacheProvider;
 	private RobotstxtServer robotsChecker;
 	private PageParserManager pageParserManager;
 	private IDocIDServer docIDServer;
 	private volatile boolean finished = false;
 	
 	private final int MAX_CRAWL_DEPTH;
 	
 	public CrawlRunner(ICrawlerSettings config) {
 		this.config = config;
 		this.pageFetcher = config.getPageFetcher();
 		this.visitValidator = config.getPageVisitValidator();
 		this.pageParserManager = config.getPageParserManager();
 		this.docIDServer = config.getCrawlState().getDocIDServer();
 		this.robotsChecker = config.getRobotstxtServer();
 		this.cacheProvider = config.getCacheProvider();
 		MAX_CRAWL_DEPTH = config.getMaxDepth();
 		
 		t = new Thread(this);
 	}
 	
 	public synchronized void start(URLManager urlManager) {
 		this.urlManager = urlManager;
 		t.start();
 	};
 	
 	public synchronized void stop() {
 		finished = true;
 		while (t.isAlive()) {
 			try {
 				t.join();
 			} catch (InterruptedException e) {
 			}
 		}
 	}
 	
 	@Override
 	public void run() {
 		List<WebURL> assignedURLs = new ArrayList<WebURL>(50);
 		while(!finished) {
 			try {
 				assignedURLs.clear();
 				assignedURLs = new ArrayList<WebURL>(50);
 				urlManager.getNextURLs(50, assignedURLs);
 				
 				for (WebURL url : assignedURLs) {
 					processPage(url);
 					urlManager.setProcessed(url);
 				}
 			} catch (Exception e) {
 				logger.error("Error in CrawlRunner!", e);
 			}
 		}
 	}
 	
 	private void processPage(WebURL curURL) {
 		if (curURL == null) {
 			return;
 		}
 		
 		Page page = new Page(curURL);
 		int statusCode = pageFetcher.fetch(page);
 		
 		if (statusCode == PageFetchStatus.OK ||
 				statusCode == PageFetchStatus.NotModified) {
 			Map<String, String> links = null;
 			if (statusCode == PageFetchStatus.NotModified &&
 					cacheProvider != null) {
 				links = cacheProvider.getCachedLinks(page);
 			}
 			
 			if (links == null) {
 				IPageParser parser = pageParserManager.getParser(page);
 				if (parser != null) {
 					parser.parse(page);
 					links = parser.getLinks();
 				}
 			}
 			
 			if (links != null) {
 				queueLinks(page, links);
 			}
 			
 			if (config.getPageVisitedCallback() != null)
 				config.getPageVisitedCallback().visited(page);
 		} else if (statusCode == PageFetchStatus.Moved) {
 			//Handle redirect by getting the new URL and scheduling it (if we haven't visited it yet)
 			if (config.getFollowRedirects()) {
 				String movedToUrl = page.getRedirectedURL();
 				
 				if (movedToUrl == null) {
 					logger.debug("Unable to get redirect url on vist to " + curURL);
 					return;
 				}
 				
 				movedToUrl = URLCanonicalizer.getCanonicalURL(movedToUrl);
 				DocID newdocid = docIDServer.getNewOrExistingDocID(movedToUrl);
 				if (!newdocid.isNew()) {
 					//We already visited the target
 					return;
 				} else {
 					WebURL webURL = new WebURL();
 					webURL.setURL(movedToUrl);
 					webURL.setParentDocid(curURL.getParentDocid());
 					webURL.setDepth(curURL.getDepth());
 					webURL.setDocid(newdocid.getId());
 					if ((visitValidator ==  null || visitValidator.canVisit(webURL.getURL())) &&
 							robotsChecker.allows(webURL.getURL())) {
 						urlManager.schedule(webURL);
 					}
 				}
 			}
 		} else if (statusCode == PageFetchStatus.PageTooBig) {
 			logger.error("Page was bigger than max allowed size: " + curURL.getURL());
 		}
 	}
 	
 	private void queueLinks(final Page page, Map<String, String> links) {
 		List<WebURL> toSchedule = new ArrayList<WebURL>();
 		List<WebURL> toList = new ArrayList<WebURL>();
 		String localUrl = page.getWebURL().getURL();
 		WebURL webURL;
 		String url;
 		for (Map.Entry<String, String> link : links.entrySet()) {
 			url = link.getKey();
 			if (!url.equals(localUrl)) {
 				webURL = new WebURL();
 				webURL.setURL(url);
 				webURL.setName(link.getValue());
 				webURL.setDocid(-1);
 				webURL.setParentDocid(page.getWebURL().getDocid());
 				webURL.setDepth((short) (page.getWebURL().getDepth() + 1));
 				toList.add(webURL);
 				
 				if ((visitValidator ==  null || visitValidator.canVisit(url)) &&
 						robotsChecker.allows(url) &&
 						(MAX_CRAWL_DEPTH == -1 || page.getWebURL().getDepth() < MAX_CRAWL_DEPTH)) {
 					DocID newdocid = docIDServer.getNewOrExistingDocID(url);
 					webURL.setDocid(newdocid.getId());
 					if (newdocid.isNew()) {
 						toSchedule.add(webURL);
 					}
 				}
 			}
 		}
 		urlManager.scheduleAll(toSchedule);
 		page.setURLs(toList);
 	}
 }
