 package com.xitxer.uateam.notification.core.parser;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import com.xitxer.uateam.notification.core.model.ReleaseEntry;
 import com.xitxer.uateam.notification.core.parser.exceptions.PageNotAvailableException;
 import com.xitxer.uateam.notification.core.parser.exceptions.message.ParseMessages;
 import com.xitxer.uateam.notification.core.parser.sitesource.SiteSource;
 
 public class RecentReleasesParser extends BaseParser {
 
 	private static final String QUERY_DIV_FRESHRELEASE = "div.freshrelease";
 	private static final String QUERY_DIV_ONLINE_CODE = "div#online_code param[name=flashvars]";
 
 	private static final String ATTR_VALUE = "value";
 
 	private static final String REGEXP_WATCH_ONLINE_FILE = ".*file\\=((http|https|ftp)\\://[a-zA-Z0-9\\-\\.]+\\.[a-zA-Z]{2,3}/[a-zA-Z0-9\\-\\._/\\\\]+\\.mp4).*";
 
 	public RecentReleasesParser(SiteSource siteSource) {
 		super(siteSource);
 	}
 
 	public List<ReleaseEntry> get() throws Exception {
 		RecentReleaseEntryFiller filler = new RecentReleaseEntryFiller();
 		List<ReleaseEntry> episodeEntries = new ArrayList<ReleaseEntry>();
 		ReleaseEntry releaseEntry;
 		for (Element element : check(getRootPage().select(QUERY_DIV_FRESHRELEASE), ParseMessages.COULD_NOT_SELECT_WITH_CSS_QUERY
 				+ QUERY_DIV_FRESHRELEASE)) {
 			if (filler.parse(element)) {
 				releaseEntry = filler.refresh();
 				episodeEntries.add(releaseEntry);
 			}
 		}
 		return episodeEntries;
 	}
 
 	public ReleaseEntry parseReleaseLinks(ReleaseEntry releaseEntry) throws PageNotAvailableException {
 		try {
 			Elements elements = getSubPage(releaseEntry.getDetailsLink()).select(QUERY_DIV_ONLINE_CODE);
 			if (elements.size() > 0) {
 				String stringWithFilePath = elements.first().attr(ATTR_VALUE);
 				if (stringWithFilePath != null) {
 					Pattern pattern = Pattern.compile(REGEXP_WATCH_ONLINE_FILE);
 					Matcher matcher = pattern.matcher(stringWithFilePath);
 					if (matcher.find()) {
 						releaseEntry.setWatchOnlineLink(matcher.group(1));
 					}
 				}
 			}
 			return releaseEntry;
 		} catch (Exception e) {
 			throw new PageNotAvailableException("Sub page not found or bad: " + releaseEntry.getDetailsLink(), e);
 		}
 	}
 
	public List<ReleaseEntry> parseReleaseLinks(List<ReleaseEntry> releaseEntries) {
 		for (ReleaseEntry entry : releaseEntries) {
 			try {
 				parseReleaseLinks(entry);
 			} catch (Exception exception) {
 				// we eat this Exception, due to we want to bring data to user
 			}
 		}
 		return releaseEntries;
 	}
 }
