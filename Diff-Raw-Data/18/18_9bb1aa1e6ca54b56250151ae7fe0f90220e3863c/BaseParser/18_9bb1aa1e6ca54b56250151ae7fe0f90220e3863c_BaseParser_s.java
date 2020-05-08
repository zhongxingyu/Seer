 package com.xitxer.uateam.notification.core.parser;
 
 import org.jsoup.nodes.Document;
 import org.jsoup.select.Elements;
 
 import com.google.common.base.Preconditions;
 import com.xitxer.uateam.notification.core.parser.exceptions.HtmlLayoutChangedException;
 import com.xitxer.uateam.notification.core.parser.sitesource.SiteSource;
 
 public class BaseParser {
 	public static final String TAG_A = "a";
 
 	public static final String ATTR_HREF = "href";
 	public static final String ATTR_SRC = "src";
 
 	private final SiteSource siteSource;
 
 	public BaseParser(SiteSource siteSource) {
 		super();
 		this.siteSource = Preconditions.checkNotNull(siteSource);
 	}
 
 	protected Elements check(Elements elements, String errorMessage) throws HtmlLayoutChangedException {
 		if (elements.isEmpty()) {
 			throw new HtmlLayoutChangedException(errorMessage);
 		}
 		return elements;
 	}
 
 	protected Document getRootPage() throws Exception {
 		return siteSource.getRootPage();
 	}
 
 	protected SiteSource getSiteSource(){
 		return siteSource;
 	}
 
 	protected Document getSubPage(final String subUrl) throws Exception {
 		return siteSource.getSubPage(subUrl);
 	}
 }
