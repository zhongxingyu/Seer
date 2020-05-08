 package cn.edu.zju.plex.tdd.main;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.log4j.Logger;
 import cn.edu.zju.plex.tdd.dao.DB4Tdd;
 import cn.edu.zju.plex.tdd.entity.RssFeed;
 import cn.edu.zju.plex.tdd.entity.RssNews;
 import cn.edu.zju.plex.tdd.module.RssNewsCrawler;
 import cn.edu.zju.plex.tdd.module.RssNewsParser;
 import cn.edu.zju.plex.tdd.module.RssNewsRmDup;
 import cn.edu.zju.plex.tdd.tools.ImageFetcher;
 import cn.edu.zju.plex.tdd.tools.TvfantasySplitUtil;
 
 /**
  * crawl & parse rssNews
  * 
  * @author plex
  */
 public class RssNewsJob implements Runnable {
 
 	private static final Logger LOG = Logger.getLogger(RssNewsJob.class);
 	private final int EIGHT_HOUR = 8 * 60 * 60 * 1000;
 	private RssNewsCrawler crawler = new RssNewsCrawler();
 	private RssNewsParser parser = new RssNewsParser();
 	private static final Pattern ImagePatt = Pattern.compile(
 			".*(\\.(bmp|gif|jpe?g|png|tiff?|ico))$", Pattern.CASE_INSENSITIVE);
 
 	private void fetchRssUpdates() {
 		ArrayList<RssFeed> rssFeeds = DB4Tdd.getRssFeedList();
 		for (RssFeed rf : rssFeeds) {
 			Date latestUpdate = null;
 			for (RssNews rssnews : crawler.fetchUpdate(rf)) {
 				if (latestUpdate == null) {
 					latestUpdate = rssnews.getPubDate();
 				}
 				DB4Tdd.insertRssNews(rssnews);
 			}
 			if (latestUpdate != null) {
 				rf.setLastUpdate(latestUpdate);
 				DB4Tdd.updateRssFeedLastUpdateTime(rf);
 			}
 		}
 	}
 
 	private void parseRssNews() {
 		while (true) {
 			List<RssNews> rssNewsToParse = DB4Tdd.getRssNewsToParse(30);
 			LOG.info("Get " + rssNewsToParse.size() + " rss items to parse...");
 
 			if (rssNewsToParse.size() == 0) {
 				LOG.info("RssNews parse work temporally done");
 				break;
 			} else {
 				for (RssNews rssNews : rssNewsToParse) {
 					if (rssNews.getLink().matches(
 							"http://tvfantasy.net/.{10}/newsletter[^#]+")) {
 						TvfantasySplitUtil.splite(rssNews);
 						continue;
 					}
 					rssNews = parser.parse(rssNews);
 					DB4Tdd.updateParsedRssNews(rssNews);
 				}
 			}
 		}
 
 	}
 
 	private void rmDump() {
 		int timeLen = 172800000;// two days
 		while (true) {
 			List<RssNews> list = DB4Tdd.getRssNewsToMerge(timeLen);
 			RssNews[] rssNewsToMerge = new RssNews[list.size()];
 			list.toArray(rssNewsToMerge);
 
 			if (rssNewsToMerge.length < 2) {
 				LOG.info("RssNews merge work temply done");
 				break;
 			} else {
 				RssNewsRmDup.deals(rssNewsToMerge);
 				for (RssNews rssNews : rssNewsToMerge)
 					if (rssNews.getDelegate() != 0)
 						DB4Tdd.updateDelegate(rssNews);
 				LOG.info("merge rss news:" + rssNewsToMerge.length);
 			}
 
 		}
 	}
 
 	private void downloadImages(String rootPath) {
 		while (true) {
 			List<RssNews> list = DB4Tdd.getRssNewsToDownloadImages();
 			LOG.info("Loop for downloading images, RssNews count:"
 					+ list.size());
 
 			if (list.size() <= 0) {
 				LOG.info("download RssNews images work temply done");
 				break;
 			} else {
 				for (RssNews rssNews : list) {
 
 					String[] images = rssNews.getImages().split(";");
 					int idx = 0;
 					for (int i = 0; i < images.length; i++) {
 						Matcher m = ImagePatt.matcher(images[i]);
 						if (m.find()) {
 							if (images[i].startsWith(".")
 									|| images[i].startsWith("/")) {
 								// TODO
 							}
 							boolean success = ImageFetcher.saveimage(images[i],
 									rootPath + "news-" + rssNews.getId() + "-"
 											+ idx + m.group(1));
 							if (success)
 								idx++;
 							else
 								LOG.warn("fail downloading:" + images[i]);
 						} else
 							LOG.debug("invalid image url" + images[i]);
 						if (idx >= 4) {
 							break;
 						}
 					}
 					LOG.info("get image count:" + idx);
 					if (idx > 0)
 						DB4Tdd.updateRssNewsImageCount(rssNews.getId(), idx + 1);
 					else
 						DB4Tdd.updateRssNewsImageCount(rssNews.getId(), 0);
 				}
 				LOG.info("download images for rss news:" + list.size());
 			}
 
 		}
 	}
 
 	@Override
 	public void run() {
 		while (true) {
 			LOG.info("Loop start for RssNewsJob");
 			try {
 				// LOG.info("开始下载rss更新");
 				// fetchRssUpdates();
 
 				LOG.info("开始解析rss_news");
 				parseRssNews();
 
 				LOG.info("开始去重");
 				rmDump();
 
 				LOG.info("下載圖片");
 				downloadImages("d:/tmp/images/");
 			} catch (Throwable t) {
 				LOG.error(t);
 				LOG.error(t.getCause());
 			} finally {
 				LOG.info("Loop over for RssNewsJob");
 				try {
 					Thread.sleep(EIGHT_HOUR);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 
 	public static void main(String[] args) {
 		new Thread(new RssNewsJob(), "RssNewsJob").start();
 	}
 }
