 package org.atlasapi.remotesite.channel4;
 
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.servlet.http.HttpServletResponse;
 
 import org.atlasapi.media.entity.Brand;
 import org.atlasapi.media.entity.Broadcast;
 import org.atlasapi.media.entity.Content;
 import org.atlasapi.media.entity.Episode;
 import org.atlasapi.media.entity.Series;
 import org.atlasapi.media.entity.Version;
 import org.atlasapi.persistence.content.ContentResolver;
 import org.atlasapi.persistence.content.ContentWriter;
 import org.atlasapi.persistence.logging.AdapterLog;
 import org.atlasapi.persistence.logging.AdapterLogEntry;
 import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
 import org.atlasapi.persistence.system.RemoteSiteClient;
 import org.atlasapi.remotesite.FetchException;
 import org.atlasapi.remotesite.channel4.epg.C4SynthesizedItemUpdater;
 import org.jdom.Element;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.metabroadcast.common.http.HttpStatusCodeException;
 import com.metabroadcast.common.media.MimeType;
 import com.sun.syndication.feed.atom.Entry;
 import com.sun.syndication.feed.atom.Feed;
 import com.sun.syndication.feed.atom.Link;
 
 public class C4BrandExtractor {
 
     private static final String BRAND_FLATTENED_NAME = "relation.BrandFlattened";
 
     private static final Pattern BAD_EPISODE_REDIRECT = Pattern.compile("(\\/episode-guide\\/series-\\d+)");
 
     private final C4BrandBasicDetailsExtractor basicDetailsExtractor = new C4BrandBasicDetailsExtractor();
     private final C4SeriesExtractor seriesExtractor;
     
     private final C4EpisodesExtractor fourOditemExtrator;
     private final C4EpisodesExtractor flattenedBrandExtrator;
     
     private final C4EpisodeBroadcastExtractor broadcastExtractor;
     private final C4ClipExtractor clipExtractor;
     private final RemoteSiteClient<Feed> feedClient;
 	private final C4PreviousVersionDataMerger versionMerger;
 
     private final AdapterLog log;
 
     private final C4SynthesizedItemUpdater synthesizedItemUpdater;
 
 	private final ContentWriter contentWriter;
 
     public C4BrandExtractor(RemoteSiteClient<Feed> atomClient, ContentResolver contentResolver, ContentWriter contentWriter, AdapterLog log) {
         feedClient = atomClient;
 		this.contentWriter = contentWriter;
         this.log = log;
         fourOditemExtrator = new C4EpisodesExtractor(log).includeOnDemands();
         flattenedBrandExtrator = new C4EpisodesExtractor(log);
         seriesExtractor = new C4SeriesExtractor(contentResolver, log);
         clipExtractor = new C4ClipExtractor(atomClient, new C4EpisodesExtractor(log).includeOnDemands());
         versionMerger = new C4PreviousVersionDataMerger(contentResolver);
         synthesizedItemUpdater = new C4SynthesizedItemUpdater(contentResolver, contentWriter);
         broadcastExtractor = new C4EpisodeBroadcastExtractor(log);
     }
 
     public void write(Feed source) {
         Brand brand = basicDetailsExtractor.extract(source);
         
         List<Series> allSeries = Lists.newArrayList();
         
         List<Episode> episodes = itemsFor(brand, allSeries);
         
         
 
         Map<String, Episode> onDemandEpisodes = onDemandEpisodes(brand);
 
         for (Episode episode : episodes) {
             Episode odEpisode = onDemandEpisodes.get(episode.getCanonicalUri());
             if (odEpisode != null) {
                 episode.setVersions(odEpisode.getVersions());
             }
             if (equivalentTitles(brand, episode)) {
                 if (episode.getSeriesNumber() != null && episode.getEpisodeNumber() != null) {
                     episode.setTitle("Series " + episode.getSeriesNumber() + " Episode " + episode.getEpisodeNumber());
                 }
             }
             
             if (episode.getImage() == null) {
                 episode.setImage(brand.getImage());
                 episode.setThumbnail(brand.getThumbnail());
             }
         }
 
         populateBroadcasts(episodes, brand);
 
         clipExtractor.fetchAndAddClipsTo(brand, episodes);
 
         contentWriter.createOrUpdate(brand);
         
         for (Series series : allSeries) {
             contentWriter.createOrUpdate(series);
         }
 
         for (Episode episode : episodes) {
 			versionMerger.merge(episode);
 			episode.setContainer(brand);
 			contentWriter.createOrUpdate(episode);
         }
     }
 
 	private boolean equivalentTitles(Brand brand, Episode episode) {
 		String notAlphanumeric = "[^\\d\\w]";
 		return episode.getTitle().replaceAll(notAlphanumeric, "").equals(brand.getTitle().replaceAll(notAlphanumeric, ""));
 	}
 
 	@SuppressWarnings("unchecked")
 	private List<Episode> itemsFor(Brand brand, List<Series> allSeries) {
 		List<Episode> items = Lists.newArrayList();
         Feed possibleEpisodeGuide = readEpisodeGuide(brand);
         
         if (isFlatterned(possibleEpisodeGuide)) {
         	return (List) flattenedBrandExtrator.extract(possibleEpisodeGuide);
         }
         	
 		for (SeriesAndEpisodes seriesAndEpisodes : fetchSeries(brand, possibleEpisodeGuide)) {
             items.addAll((List) seriesAndEpisodes.getEpisodes());
             Series series = seriesAndEpisodes.getSeries();
             series.setParent(brand);
 			allSeries.add(series);
         }
 		return items;
 	}
 
     @SuppressWarnings("unchecked")
 	private boolean isFlatterned(Feed feed) {
     	Iterable<Element> markup = (Iterable<Element>) feed.getForeignMarkup();
     	for (Element element : markup) {
 			if (BRAND_FLATTENED_NAME.equals(element.getName()) && Boolean.valueOf(element.getValue())) {
 				return true;
 			}
     	}
     	return false;
     }
 
 	private static Pattern ID_PATTERN = Pattern.compile("tag:www.channel4.com,\\d+:/programmes/([a-z0-9\\-]+)/episode-guide(?:/series-(\\d+)(?:/episode-(\\d+))?)?");
 
     private List<SeriesAndEpisodes> fetchSeries(Brand brand, Feed episodeGuide) {
 
         Matcher matcher = ID_PATTERN.matcher(episodeGuide.getId());
 
         if (!matcher.matches()) {
             throw new FetchException("Series guide id not recognised: " + episodeGuide.getId());
         }
 
         // the feed has a series number
         if (matcher.group(2) != null) {
             // the feed also has an episode number, this is not the right feed
             // -- read the series feed instead
             if (matcher.group(3) != null) {
                 int seriesNumber = Integer.valueOf(matcher.group(2));
                 return setSeriesTitles(loadSeriesFromFeeds(ImmutableList.of(C4AtomApi.requestForBrand(brand.getCanonicalUri(), "/episode-guide/series-" + seriesNumber + ".atom"))), brand);
             } else {
                 // the feed is a series, pass to extractor
                 return setSeriesTitles(ImmutableList.of(seriesExtractor.extract(episodeGuide)), brand);
             }
         }
         // a real series guide
         return setSeriesTitles(loadSeriesFromFeeds(extractSeriesAtomFeedsFrom(episodeGuide)), brand);
     }
 
     private List<SeriesAndEpisodes> setSeriesTitles(List<SeriesAndEpisodes> seriesAndEpisodes, Brand brand) {
         for (SeriesAndEpisodes seriesAndEpisode : seriesAndEpisodes) {
         	Series series = seriesAndEpisode.getSeries();
 			series.setTitle(brand.getTitle() + " - Series " + series.getSeriesNumber());
         }
         return seriesAndEpisodes;
     }
 
     private List<SeriesAndEpisodes> loadSeriesFromFeeds(List<String> seriesFeeds) {
         List<SeriesAndEpisodes> series = Lists.newArrayList();
         for (String uri : seriesFeeds) {
             try {
                 series.add(seriesExtractor.extract(feedClient.get(uri)));
             } catch (Exception e) {
                 log.record(new AdapterLogEntry(Severity.ERROR).withDescription("Unable to retrieve series: "+uri).withCause(e).withUri(uri).withSource(getClass()));
             }
         }
         return series;
     }
 
    private static final Pattern SERIES_LINK = Pattern.compile("^(https?://api.channel4.com/programmes/[a-z0-9\\-]+/episode-guide/series-\\d+).*(.atom.*)$");
 
     @SuppressWarnings("unchecked")
     private List<String> extractSeriesAtomFeedsFrom(Feed episodeGuide) {
         List<String> seriesUris = Lists.newArrayList();
         for (Entry entry : (List<Entry>) episodeGuide.getEntries()) {
             List<Link> alternateLinks = entry.getOtherLinks();
             for (Link link : alternateLinks) {
                 if (MimeType.APPLICATION_ATOM_XML.toString().equals(link.getType())) {
                     Matcher matcher = SERIES_LINK.matcher(link.getHref());
                     if (matcher.matches()) {
                         String href = matcher.group(1) + matcher.group(2);
                         if (!seriesUris.contains(href)) {
                             seriesUris.add(href);
                         }
                     }
                 }
             }
         }
         return seriesUris;
     }
 
     /**
      * @return The feed if it exists or null if the page is probably a special
      *         case
      */
     private Feed readEpisodeGuide(Brand brand) {
         try {
             return fetch(brand, "/episode-guide.atom");
         } catch (HttpStatusCodeException e) {
             if (e.getStatusCode() == 403) {
                 Matcher matcher = BAD_EPISODE_REDIRECT.matcher(e.getResponse().finalUrl());
                 if (matcher.find()) {
                     try {
                         return fetch(brand, matcher.group(1) + ".atom");
                     } catch (HttpStatusCodeException e1) {
                         return fetchDefaultSeriesOrGiveUp(brand);
                     } catch (Exception e1) {
                     }
                 }
             }
             if (e.getStatusCode() >= 400 && e.getStatusCode() < 500) {
                 return fetchDefaultSeriesOrGiveUp(brand);
             }
             throw new FetchException("could not fetch series guide for " + brand.getCanonicalUri(), e);
         } catch (Exception e) {
             throw new FetchException("could not read episode guide for ", e);
         }
     }
 
     private Feed fetchDefaultSeriesOrGiveUp(Brand brand) {
         try {
             return fetch(brand, "/episode-guide/series-1.atom");
         } catch (Exception e) {
             throw new FetchException("Could not find any series information for " + brand.getCanonicalUri(), e);
         }
     }
 
 	private Map<String, Episode> onDemandEpisodes(Brand brand) {
         try {
             return toMap(fourOditemExtrator.extract(fetch(brand, "/4od.atom")));
         } catch (HttpStatusCodeException e) {
             if (HttpServletResponse.SC_NOT_FOUND == e.getStatusCode()) {
                 return ImmutableMap.of();
             }
             throw new FetchException("could not read on demand info for " + brand.getCanonicalUri(), e);
         } catch (Exception e) {
             throw new FetchException("could not read on demand info for " + brand.getCanonicalUri(), e);
         }
     }
 
     private void populateBroadcasts(List<Episode> episodes, Brand brand) {
         List<Episode> broadcastEpisodes = Lists.newArrayList();
         try {
             Feed epg = fetch(brand, "/epg.atom");
             broadcastEpisodes = broadcastExtractor.extract(epg);
         } catch (Exception e) {
             log.record(new AdapterLogEntry(Severity.WARN).withDescription("Unable to retrieve epg information for brand: " + brand.getCanonicalUri()).withCause(e).withUri(brand.getCanonicalUri()).withSource(getClass()));
         }
 
         for (Episode broadcastEpisode : broadcastEpisodes) {
             for (Episode episode : episodes) {
                 if (episode.getCanonicalUri().equals(broadcastEpisode.getCanonicalUri())) {
                     Broadcast broadcast = broadcastEpisode.getVersions().iterator().next().getBroadcasts().iterator().next();
                     Version version = null;
                     if (episode.getVersions().isEmpty()) {
                         version = new Version();
                         episode.addVersion(version);
                     } else {
                         version = episode.getVersions().iterator().next();
                     }
                     synthesizedItemUpdater.findAndUpdatePossibleSynthesized(broadcast.getId(), episode, brand.getCanonicalUri());
 
                     boolean found = false;
                     for (Broadcast currentBroadcast : version.getBroadcasts()) {
                         if (currentBroadcast.equals(broadcast) || (currentBroadcast.getId() != null && currentBroadcast.getId().equals(broadcast.getId()))) {
                             currentBroadcast.setAliases(broadcast.getAliases());
                             currentBroadcast.setLastUpdated(broadcast.getLastUpdated());
                             found = true;
                         }
                     }
                     if (!found) {
                         version.addBroadcast(broadcast);
                     }
                 }
             }
         }
     }
 
     private static <T extends Content> Map<String, T> toMap(Iterable<T> contents) {
         Map<String, T> lookup = Maps.newHashMap();
         for (T content : contents) {
             lookup.put(content.getCanonicalUri(), content);
         }
         return lookup;
     }
 
     private Feed fetch(Brand brand, String extension) throws Exception {
         String url = C4AtomApi.requestForBrand(brand.getCanonicalUri(), extension);
         return feedClient.get(url);
     }
 }
