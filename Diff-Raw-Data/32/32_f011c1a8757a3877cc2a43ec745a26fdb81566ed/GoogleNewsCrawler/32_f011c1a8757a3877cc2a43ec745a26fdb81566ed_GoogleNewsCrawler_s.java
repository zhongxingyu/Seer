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
 
 public class GoogleNewsCrawler implements NewsCrawler {
 	private static final Logger LOG = LoggerFactory.getLogger(GoogleNewsCrawler.class);
 
 	private static String TODAY = "https://news.google.co.kr/news/feeds?hl=ko&output=rss"; // 주요기사
 	private static String POLITICS = "https://news.google.co.kr/news/feeds?hl=ko&topic=p&output=rss";
 	private static String ECON = "https://news.google.co.kr/news/feeds?hl=ko&topic=b&output=rss";
 	private static String SOCIETY = "https://news.google.co.kr/news/feeds?hl=ko&topic=y&output=rss";
 	private static String CULTURE = "https://news.google.co.kr/news/feeds?hl=ko&topic=l&output=rss";
 	private static String ENT = "https://news.google.co.kr/news/feeds?hl=ko&topic=r&output=rss";
 	private static String SPORT = "https://news.google.co.kr/news/feeds?hl=ko&topic=s&output=rss";
 	private static String IT = "https://news.google.co.kr/news/feeds?hl=ko&topic=t&output=rss";
 
 	private List<Article> articleList = new ArrayList<>();
 
 	@Override
 	public List<Article> createArticlList() {
 		LOG.debug("get RSS from Google.");
 
 		createListBySection(TODAY, ArticleSection.TODAY);
		//createListBySection(POLITICS, ArticleSection.POLITICS);
		//createListBySection(ECON, ArticleSection.ECON);
		//createListBySection(SOCIETY, ArticleSection.SOCIETY);
 		//createListBySection(CULTURE, ArticleSection.CULTURE);
 		//createListBySection(ENT, ArticleSection.ENT);
 		//createListBySection(SPORT, ArticleSection.SPORT);
 		//createListBySection(IT, ArticleSection.IT);
 
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
 		Article article = new Article();
 		article.setSection(section);
 		article.setLink(rssItem.getLink());
 		article.setPubDate(new DateTime(rssItem.getPublishedDate(), DateTimeZone.forID("Asia/Seoul")));
 		parseTitleAuthor(rssItem.getTitle(), article);
 		
 		// if title is empty or too short, hard to analysis. So let's skip
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
 
 	private void parseTitleAuthor(String title, Article article) {
 		int idx = title.lastIndexOf("-");
 		article.setTitle(ContentsAnalysisService.clearInvalidWords(title.substring(0, idx).trim()));
 		article.setAuthor(title.substring(idx + 1, title.length()).trim());
 	}
 }
