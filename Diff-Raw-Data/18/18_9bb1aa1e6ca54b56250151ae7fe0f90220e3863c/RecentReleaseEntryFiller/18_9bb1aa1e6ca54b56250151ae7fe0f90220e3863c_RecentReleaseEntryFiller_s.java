 package com.xitxer.uateam.notification.core.parser;
 
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import com.xitxer.uateam.notification.core.model.ReleaseEntry;
 import com.xitxer.uateam.notification.core.parser.exceptions.HtmlLayoutChangedException;
 
 public class RecentReleaseEntryFiller {
 
 	private static final String TAG_A = "a";
 	private static final String TAG_DIV = "div";
 	private static final String TAG_P = "P";
 	private static final String TAG_SPAN = "span";
 
 	private static final String REGEXP_REALEASE_NUMBERS = "\\.";
 
 	private static final String QUERY_RELEASE_ICON = "img[src].category_icon";
 
 	private ReleaseEntry episodeEntry = new ReleaseEntry();
 
 	public boolean parse(Element rootElement) throws HtmlLayoutChangedException {
 		try {
 			Elements elements = rootElement.children();
 
 			Element element = elements.get(0);
 			if (TAG_DIV.equalsIgnoreCase(element.tagName())) {
 				try {
 					String[] releaseNumberAndSeasonNumber = element.text().split(REGEXP_REALEASE_NUMBERS);
 					episodeEntry.setSeason(Integer.parseInt(releaseNumberAndSeasonNumber[0]));
 					episodeEntry.setEpisode(Integer.parseInt(releaseNumberAndSeasonNumber[1]));
 				} catch (Exception e) {
 					// do nothing because can be not series release
 				}
 			}
 
			elements = elements.select(TAG_P).get(0).children();
 
 			Elements titleElements = elements.select(TAG_SPAN);
 			episodeEntry.setGroup(titleElements.get(0).text());
 			episodeEntry.setRelease(titleElements.get(1).text());
 
 			Elements linkElements = elements.select(TAG_A);
 			episodeEntry.setGroupLink(linkElements.get(0).attr(BaseParser.ATTR_HREF));
 			episodeEntry.setDetailsLink(linkElements.get(1).attr(BaseParser.ATTR_HREF));
 
			episodeEntry.setIconLink(elements.select(QUERY_RELEASE_ICON).get(0).attr(BaseParser.ATTR_SRC));
 
 			return true;
 		} catch (Exception e) {
 			HtmlLayoutChangedException exception = new HtmlLayoutChangedException("Unknown change: " + e.toString());
 			exception.setStackTrace(e.getStackTrace());
 			throw exception;
 		}
 	}
 
 	public ReleaseEntry refresh() {
 		ReleaseEntry old = episodeEntry;
 		episodeEntry = new ReleaseEntry();
 		return old;
 	}
 }
