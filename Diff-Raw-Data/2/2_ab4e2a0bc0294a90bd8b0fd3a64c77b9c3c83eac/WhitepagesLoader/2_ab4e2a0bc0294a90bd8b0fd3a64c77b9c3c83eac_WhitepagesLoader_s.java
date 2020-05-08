 package com.sk.impl2;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import com.sk.api.NameComparison;
 import com.sk.parse.Extractor;
 import com.sk.parse.OuterLoader;
 import com.sk.parse.PagingLoader;
 import com.sk.parse.Parsers;
 import com.sk.web.IOUtil;
 import com.sk.web.Request;
 
 public class WhitepagesLoader extends OuterLoader {
 
 	private static final String BASE_URL = "http://www.whitepages.com/name/%s-%s/%d";
 	private static final int NUM_PAGES = 10;
 	private static final String RESULT_SELECTOR = "li.serp-list-item > a";
 	static final String SITE_KEY = "whitepages";
 
 	private final int page;
 	private final String[] names;
 	private final String url;
 
 	private Document document;
 
 	public WhitepagesLoader(String first, String last) {
 		this(new String[] { first, last }, 1);
 	}
 
 	private WhitepagesLoader(String[] names, int page) {
 		this.names = names;
 		this.page = page;
 		this.url = String.format(BASE_URL, formatNameForSearchString(names[0]),
				formatNameForSearchString(names[1]));
 	}
 
 	private String formatNameForSearchString(String name) {
 		return IOUtil.urlEncode(name.replaceAll("[^A-Za-z]", ""));
 	}
 
 	@Override
 	protected boolean loadStopSearching() {
 		init();
 		for (Element result : getResultContainers()) {
 			if (!checkName(result))
 				return true;
 		}
 		return false;
 	}
 
 	@Override
 	protected List<Extractor> getExtractors() {
 		List<Extractor> ret = new ArrayList<>();
 		boolean stop = false;
 		for (Element resultContainer : getResultContainers()) {
 			if (!checkName(resultContainer)) {
 				stop = true;
 				break;
 			}
 			String url = getUrl(resultContainer);
 			try {
 				ret.add(new WhitepagesPersonLoader(url));
 			} catch (MalformedURLException ignored) {
 			}
 		}
 		stopPaging.set(stop);
 		return ret;
 	}
 
 	private String getUrl(Element resultContainer) {
 		return resultContainer.absUrl("href");
 	}
 
 	private String getName(Element resultContainer) {
 		return resultContainer.select("p.name").text();
 	}
 
 	private boolean checkName(Element resultContainer) {
 		NameComparison nameUtil = NameComparison.get();
 		String name = getName(resultContainer);
 		String[] parts = nameUtil.parseName(name);
 		return nameUtil.isSameName(parts, names);
 	}
 
 	private Elements getResultContainers() {
 		init();
 		return document.select(RESULT_SELECTOR);
 	}
 
 	@Override
 	protected PagingLoader createNextPage() {
 		init();
 		if (page >= NUM_PAGES)
 			return null;
 		if (stopPaging.get())
 			return null;
 		return new WhitepagesLoader(names, page + 1);
 	}
 
 	@Override
 	protected Request getRequest() {
 		try {
 			Request request = new Request(url, "GET");
 			request.addRandomUserAgent();
 			return request;
 		} catch (MalformedURLException ignored) {
 			return null;
 		}
 	}
 
 	@Override
 	protected void parse(URL source, String data) {
 		document = Parsers.parseHTML(data, source.toExternalForm());
 	}
 
 }
