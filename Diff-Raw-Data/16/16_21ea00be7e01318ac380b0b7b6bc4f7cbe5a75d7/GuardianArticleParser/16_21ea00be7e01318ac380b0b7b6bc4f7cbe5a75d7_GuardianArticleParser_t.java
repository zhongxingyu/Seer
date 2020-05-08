 /*
  * GuardianArticleParser.java
  * Copyright (C) 2011 Meyer Kizner, Ty Overby
  * All rights reserved.
  */
 
 package com.prealpha.extempdb.server.parse;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.Namespace;
 import org.jdom.filter.Filter;
 import org.jdom.input.DOMBuilder;
 import org.w3c.tidy.Tidy;
 
 import com.google.inject.Inject;
 import com.prealpha.extempdb.server.http.HttpClient;
 import com.prealpha.extempdb.server.http.RobotsExclusionException;
 import com.prealpha.extempdb.server.util.XmlUtils;
 
 class GuardianArticleParser extends AbstractArticleParser {
 	/*
 	 * Package visibility for unit testing.
 	 */
 	static final DateFormat DATE_FORMAT = new SimpleDateFormat(
 			"EEEEE d MMMMM yyyy");
 
 	private final HttpClient httpClient;
 
 	private final Tidy tidy;
 
 	private final DOMBuilder builder;
 
 	@Inject
 	public GuardianArticleParser(HttpClient httpClient, Tidy tidy,
 			DOMBuilder builder) {
 		this.httpClient = httpClient;
 		this.tidy = tidy;
 		this.builder = builder;
 	}
 
 	@Override
 	public ProtoArticle parse(String url) throws ArticleParseException {
 		try {
 			Map<String, String> params = Collections.emptyMap();
 			InputStream stream = httpClient.doGet(url, params);
 			return getFromHtml(stream);
 		} catch (IOException iox) {
 			throw new ArticleParseException(iox);
 		} catch (RobotsExclusionException rex) {
 			throw new ArticleParseException(rex);
 		}
 	}
 
 	private ProtoArticle getFromHtml(InputStream html)
 			throws ArticleParseException {
 		org.w3c.dom.Document doc = tidy.parseDOM(html, null);
 		doc.removeChild(doc.getDoctype());
 		Document document = builder.build(doc);
 
 		Namespace namespace = document.getRootElement().getNamespace();
 
 		// get the title
 		Filter titleElementFilter = XmlUtils.getElementFilter("div", "id",
 				"main-article-info");
 		Element titleElement = (Element) (document
 				.getDescendants(titleElementFilter)).next();
 		Element heading = titleElement.getChild("h1", namespace);
 		String title = heading.getValue();
 
		// get the byline, if there is one
		// http://www.guardian.co.uk/world/feedarticle/9475892
		String byline;
 		Filter bylineElementFilter = XmlUtils.getElementFilter("a", "class",
 				"contributor");
		Iterator<?> bylineIterator = document
				.getDescendants(bylineElementFilter);
		if (bylineIterator.hasNext()) {
			Element bylineElement = (Element) bylineIterator.next();
			byline = bylineElement.getValue();
		} else {
			byline = null;
		}
 
 		/*
 		 * Get the date. The actual date is in a non-standard <time> element,
 		 * which JTidy doesn't like and seems to remove. So we use the parent
 		 * element and parse out the date. For example, the parent element might
 		 * contain "The Guardian, Thursday 27 January 2011". So we split() on
 		 * the comma and take the second fragment for parsing.
 		 */
 		Filter dateElementFilter = XmlUtils.getElementFilter("li", "class",
 				"publication");
 		Element dateElement = (Element) (document
 				.getDescendants(dateElementFilter)).next();
 		String dateString = dateElement.getValue().split(",")[1].trim();
 		Date date;
 		try {
 			date = DATE_FORMAT.parse(dateString);
 		} catch (ParseException px) {
 			throw new ArticleParseException(px);
 		}
 
 		// get the body text
 		Filter bodyElementFilter = XmlUtils.getElementFilter("div", "id",
 				"article-wrapper");
 		Iterator<?> i1 = document.getDescendants(bodyElementFilter);
 		List<String> paragraphs = new ArrayList<String>();
 		while (i1.hasNext()) {
 			Element bodyElement = (Element) i1.next();
 
 			Filter paragraphFilter = XmlUtils.getElementFilter("p", null, null);
 			Iterator<?> i2 = bodyElement.getDescendants(paragraphFilter);
 			while (i2.hasNext()) {
 				Element paragraph = (Element) i2.next();
 				String text = paragraph.getValue().trim();
 
 				if (!text.isEmpty()) {
 					paragraphs.add(text);
 				}
 			}
 		}
 
 		return new ProtoArticle(title, byline, date, paragraphs);
 	}
 }
