 /*
  * AlJazeeraArticleParser.java
  * Copyright (C) 2011 Ty Overby
  * All rights reserved.
  */
 
 package com.subitarius.instance.server.parse;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 
 import com.google.common.collect.Lists;
 import com.google.inject.Inject;
 import com.google.inject.Provider;
 import com.subitarius.domain.Article;
 import com.subitarius.domain.ArticleUrl;
 import com.subitarius.domain.Team;
 import com.subitarius.util.http.RobotsExclusionException;
 import com.subitarius.util.http.SimpleHttpClient;
 
 final class AlJazeeraArticleParser implements ArticleParser {
 	private static final DateFormat DATE_FORMAT = new SimpleDateFormat(
 			"dd MMM yyyy");
 
 	private final Provider<Team> teamProvider;
 
 	private final SimpleHttpClient httpClient;
 
 	@Inject
 	private AlJazeeraArticleParser(Provider<Team> teamProvider,
 			SimpleHttpClient httpClient) {
 		this.teamProvider = teamProvider;
 		this.httpClient = httpClient;
 	}
 
 	@Override
 	public Article parse(ArticleUrl articleUrl) throws ArticleParseException {
 		try {
 			String title;
 			Date date;
 			List<String> paragraphs = Lists.newArrayList();
 
 			// setup
 			String url = articleUrl.getUrl();
 			InputStream stream = httpClient.doGet(url + "?print=true");
 			Document document = Jsoup.parse(stream, null, url);
 
 			// title
 			title = document.select("#DetailedTitle").text();
 
 			// date
			String dateStr = document.select("#cphBody_lblDate").text();
 			try {
 				date = DATE_FORMAT.parse(dateStr);
 			} catch (ParseException px) {
 				throw new ArticleParseException(articleUrl, px);
 			}
 
 			// paragraphs
 			for (Element e : document.select("#tdTextContent>p")) {
 				paragraphs.add(e.text().replace('\u00a0', ' '));
 			}
 
 			return new Article(teamProvider.get(), articleUrl, title, null,
 					date, paragraphs);
 		} catch (IOException iox) {
 			throw new ArticleParseException(articleUrl, iox);
 		} catch (RobotsExclusionException rex) {
 			throw new ArticleParseException(articleUrl, rex);
 		}
 	}
 }
