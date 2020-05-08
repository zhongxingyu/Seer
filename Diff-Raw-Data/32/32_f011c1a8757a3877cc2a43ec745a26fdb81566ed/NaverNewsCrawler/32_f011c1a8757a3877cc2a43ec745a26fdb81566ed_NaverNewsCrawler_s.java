 /*
  * @(#)DaumNewsCrawler.java $version 2013. 10. 25.
  */
 
 package com.infoc.crawler;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.joda.time.DateTime;
 import org.joda.time.DateTimeZone;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.base.Strings;
 import com.infoc.domain.Article;
 import com.infoc.enumeration.ArticleSection;
 import com.infoc.service.ContentsAnalysisService;
 import com.infoc.util.RSSCrawler;
 import com.sun.syndication.feed.synd.SyndEntry;
 
 
 public class NaverNewsCrawler implements NewsCrawler {
 	private static final Logger LOG = LoggerFactory.getLogger(NaverNewsCrawler.class);
 	private static String TODAY = "http://news.search.naver.com/newscluster/rss.nhn?type=0&rss_idx=3";
 	private static String POLITICS = "http://news.search.naver.com/newscluster/rss.nhn?type=0&rss_idx=4";
 	private static String ECON = "http://news.search.naver.com/newscluster/rss.nhn?type=0&rss_idx=5";
 	private static String SOCIETY = "http://news.search.naver.com/newscluster/rss.nhn?type=0&rss_idx=6";
 	private static String CULTURE = "http://news.search.naver.com/newscluster/rss.nhn?type=0&rss_idx=7";
 	private static String ENT = "http://news.search.naver.com/newscluster/rss.nhn?type=0&rss_idx=10";
 	private static String SPORT = "http://news.search.naver.com/newscluster/rss.nhn?type=0&rss_idx=11";
 	private static String IT = "http://news.search.naver.com/newscluster/rss.nhn?type=0&rss_idx=9";
 
 	private List<Article> articleList = new ArrayList<>();
 	
 	@Override
 	public List<Article> createArticlList() {
 		LOG.debug("get RSS from Naver.");
 		
 		createListBySection(TODAY, ArticleSection.TODAY);
 		createListBySection(POLITICS, ArticleSection.POLITICS);
 		createListBySection(ECON, ArticleSection.ECON);
 		createListBySection(SOCIETY, ArticleSection.SOCIETY);
		createListBySection(CULTURE, ArticleSection.CULTURE);
		createListBySection(ENT, ArticleSection.ENT);
		createListBySection(SPORT, ArticleSection.SPORT);
		createListBySection(IT, ArticleSection.IT);
 
 		return this.articleList;
 	}
 	
 	private void createListBySection(String rssUrl, ArticleSection section) {
 		for (SyndEntry item : RSSCrawler.getArticleList(rssUrl)) {
 			Article article = parseRSSItem(item, section);
 			if (article == null) {
 				continue;
 			}
 			this.articleList.add(article);
 		}
 	}
 
 	private Article parseRSSItem(SyndEntry rssItem, ArticleSection section) {
 		// Skip the news written in only English.
 		if (!rssItem.getTitle().matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣]+.*")) {
 			return null;
 		}
 
 		Article article = new Article();
 		article.setSection(section);
 		article.setAuthor(rssItem.getAuthor());
 		article.setLink(rssItem.getLink());
 		article.setPubDate(new DateTime(rssItem.getPublishedDate(),	DateTimeZone.forID("Asia/Seoul")));
 		article.setTitle(ContentsAnalysisService.clearInvalidWords(rssItem.getTitle()));
 		if (Strings.isNullOrEmpty(article.getTitle()) || article.getTitle().length() < 5) {
 			return null;
 		}
 		
 		article.createContentsFromLink();
 		if (Strings.isNullOrEmpty(article.getContents())) {
 			article.setContents(rssItem.getDescription().getValue());
 		}
 
 		article.setContents(ContentsAnalysisService.clearInvalidWords(article.getContents()));
 
 		return article;
 	}
 	
 }
