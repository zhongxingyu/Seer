 package org.atlasapi.remotesite.channel4;
 
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.atlasapi.media.entity.Described;
 import org.jdom.Element;
 import org.jdom.Namespace;
 import org.joda.time.Duration;
 
 import com.google.common.base.Preconditions;
 import com.google.common.base.Splitter;
 import com.google.common.base.Strings;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.sun.syndication.feed.atom.Entry;
 import com.sun.syndication.feed.atom.Feed;
 import com.sun.syndication.feed.atom.Link;
 
 public class C4AtomApi {
 	
 	public static final Namespace NS_MEDIA_RSS = Namespace.getNamespace("http://search.yahoo.com/mrss/");
 
     public static final String DC_DURATION = "dc:relation.Duration";
 	private static final String PROGRAMMES_BASE = "http://www.channel4.com/programmes/";
 	
 	private static final String WEB_SAFE_NAME_PATTERN = "[a-z0-9\\-]+";
 	private static final String FEED_ID_PREFIX_PATTERN = "tag:www.channel4.com,\\d{4}:/programmes/";
 	
 	public static final Pattern SERIES_AND_EPISODE_NUMBER_IN_ANY_URI = Pattern.compile("series-(\\d+)/episode-(\\d+)");
 	
 	public static final Pattern CANONICAL_BRAND_URI_PATTERN = Pattern.compile(String.format("%s(%s)", Pattern.quote(PROGRAMMES_BASE), WEB_SAFE_NAME_PATTERN));
 	private static final Pattern CANONICAL_EPISODE_URI_PATTERN = Pattern.compile(String.format("%s%s/episode-guide/series-\\d+/episode-\\d+", Pattern.quote(PROGRAMMES_BASE), WEB_SAFE_NAME_PATTERN));
 
 	private static final Pattern ANY_FEED_ID_PATTERN = Pattern.compile(String.format("%s(%s)/.*", FEED_ID_PREFIX_PATTERN, WEB_SAFE_NAME_PATTERN));
 
 	private static final Pattern BRAND_PAGE_ID_PATTERN = Pattern.compile(String.format("%s%s", FEED_ID_PREFIX_PATTERN, WEB_SAFE_NAME_PATTERN));
 	private static final Pattern SERIES_PAGE_ID_PATTERN = Pattern.compile(String.format("%s%s/episode-guide/series-\\d+", FEED_ID_PREFIX_PATTERN, WEB_SAFE_NAME_PATTERN));
 	private static final Pattern EPISODE_PAGE_ID_PATTERN = Pattern.compile(String.format("%s%s/episode-guide/series-\\d+/episode-\\d+", FEED_ID_PREFIX_PATTERN, WEB_SAFE_NAME_PATTERN));
 
 	private static final String API_BASE_URL = "http://api.channel4.com/programmes/";
 	private static final String ATOZ_BASE_URL = "http://api.channel4.com/programmes/atoz/";
 
 	private static final Pattern IMAGE_PATTERN = Pattern.compile("(http.+?)\\d+x\\d+(\\.[a-zA-Z]+)");
 	
 	private static final String IMAGE_SIZE = "625x352";
 	private static final String THUMBNAIL_SIZE = "200x113";
 	
     public static final Pattern SLOT_PATTERN = Pattern.compile("tag:.*,\\d{4}:slot/(\\d+)");
 	
 	
 	public static void addImages(Described content, String anImage) {
		if (! Strings.isNullOrEmpty(anImage)) {
 			Matcher matcher = IMAGE_PATTERN.matcher(anImage);
 			if (matcher.matches()) {
 				content.setThumbnail(matcher.group(1) + THUMBNAIL_SIZE + matcher.group(2));
 				content.setImage((matcher.group(1) + IMAGE_SIZE + matcher.group(2)));
 			}
 		}
 	}
 
 	public static boolean isACanonicalBrandUri(String brandUri) {
 		return CANONICAL_BRAND_URI_PATTERN.matcher(brandUri).matches();
 	}
 
 	public static String requestForBrand(String brandCanonicalUri, String extension) {
 		return createBrandRequest(extractWebSafeNameFromBrandUri(brandCanonicalUri), extension);
 	}
 
 	private static String extractWebSafeNameFromBrandUri(String brandCanonicalUri) {
 		Matcher matcher = CANONICAL_BRAND_URI_PATTERN.matcher(brandCanonicalUri);
 		Preconditions.checkArgument(matcher.matches(), "Not a valid brand URI: " + brandCanonicalUri);
 		return matcher.group(1);
 	}
 
 	public static String createBrandRequest(String webSafeName, String extension) {
 		return API_BASE_URL + webSafeName + extension;
 	}
 	
 	public static String createAtoZRequest(String webSafeName, String extension) {
         return ATOZ_BASE_URL + webSafeName + extension;
     }
 
 	public static boolean isACanonicalEpisodeUri(String href) {
 		return CANONICAL_EPISODE_URI_PATTERN.matcher(href).matches();
 	}
 
 	public static String webSafeNameFromAnyFeedId(String id) {
 		Matcher matcher = ANY_FEED_ID_PATTERN.matcher(id);
 		if (matcher.matches()) {
 			return matcher.group(1);
 		}
 		return null;
 	}
 
 	public static String episodeUri(String webSafeBrandName, int seriesNumber, int episodeNumber) {
 		return seriesUriFor(webSafeBrandName, seriesNumber) + "/episode-" + episodeNumber;
 	}
 
 	public static String seriesUriFor(String webSafeBrandName, int seriesNumber) {
 		return PROGRAMMES_BASE + webSafeBrandName + "/episode-guide/series-" + seriesNumber;
 	}
 	
 	@SuppressWarnings("unchecked")
     public static String canonicalUri(Entry entry) {
         List<Link> links = entry.getAlternateLinks();
         
         for (Link link : links) {
             String href = link.getHref();
             if (C4AtomApi.isACanonicalEpisodeUri(href)) {
                 return href;
             }
         }
         
         links = entry.getOtherLinks();
         for (Link link : links) {
             String href = link.getHref();
             if (C4AtomApi.isACanonicalEpisodeUri(href)) {
                 return href;
             }
         }
         
         return null;
     }
 	
 	@SuppressWarnings("unchecked")
     public static Map<String, String> foreignElementLookup(Entry entry) {
         return foreignElementLookup((Iterable<Element>) entry.getForeignMarkup());
     }
 
     public static Map<String, String> foreignElementLookup(Iterable<Element> foreignMarkup) {
         Map<String, String> foreignElementLookup = Maps.newHashMap();
         for (Element element : foreignMarkup) {
             foreignElementLookup.put(element.getNamespacePrefix() + ":" + element.getName(), element.getText());
         }
         return foreignElementLookup;
     }
     
     public static Integer readAsNumber(Map<String, String> lookup, String key) {
         String value = lookup.get(key);
         if (Strings.isNullOrEmpty(value)) {
             return null;
         }
         return Integer.valueOf(value);
     }
     
     @SuppressWarnings("unchecked")
     public static String fourOdUri(Entry entry) {
         List<Link> links = entry.getAlternateLinks();
         
         for (Link link : links) {
             String href = link.getHref();
             if (href.contains("4od#")) {
                 return href;
             }
         }
         return null;
     }
     
     public static Duration durationFrom(Map<String, String> lookup) {
         String durationString = lookup.get(DC_DURATION);
         if (durationString == null) {
             return null;
         }
         List<String> parts = Lists.newArrayList(Splitter.on(":").split(durationString));
         int duration = 0;
         for (String part : parts) {
             duration = (duration * 60) + Integer.valueOf(part);
         }
         return Duration.standardSeconds(duration);
     }
 
 	public static boolean isABrandFeed(Feed source) {
 		return BRAND_PAGE_ID_PATTERN.matcher(source.getId()).matches();
 	}
 
 	public static boolean isASeriesFeed(Feed source) {
 		return SERIES_PAGE_ID_PATTERN.matcher(source.getId()).matches();
 	}
 
 	public static boolean isAnEpisodeId(String id) {
 		return EPISODE_PAGE_ID_PATTERN.matcher(id).matches();
 	}
 
 	public static String clipUri(Entry entry) {
 		 Element mediaGroup = mediaGroup(entry);
 		 if (mediaGroup == null) {
 			 return null;
 		 }
 		 Element player = mediaGroup.getChild("player", NS_MEDIA_RSS);
 		 if (player == null) {
 			 return null;
 		 }
 		 return player.getAttributeValue("url");
 	}
 	
 	@SuppressWarnings("unchecked")
 	public static Element mediaGroup(Entry syndEntry) {
 		for (Element element : (List<Element>) syndEntry.getForeignMarkup()) {
 			if (NS_MEDIA_RSS.equals(element.getNamespace()) && "group".equals(element.getName())) {
 				return element;
 			}
 		}
 		return null;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public static Element mediaContent(Entry syndEntry) {
 		for (Element element : (List<Element>) syndEntry.getForeignMarkup()) {
 			if (NS_MEDIA_RSS.equals(element.getNamespace()) && "content".equals(element.getName())) {
 				return element;
 			}
 		}
 		return null;
 	}
 }
