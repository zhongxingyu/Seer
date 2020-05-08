 package org.atlasapi.feeds.lakeview;
 
 import static org.atlasapi.feeds.lakeview.LakeviewContentFetcher.EPISODE_NUMBER_ORDERING;
 
 import java.util.Comparator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import nu.xom.Attribute;
import nu.xom.Comment;
 import nu.xom.Document;
 import nu.xom.Element;
 
 import org.atlasapi.feeds.xml.XMLNamespace;
 import org.atlasapi.media.TransportType;
 import org.atlasapi.media.channel.ChannelResolver;
 import org.atlasapi.media.entity.Brand;
 import org.atlasapi.media.entity.Broadcast;
 import org.atlasapi.media.entity.ChildRef;
 import org.atlasapi.media.entity.Content;
 import org.atlasapi.media.entity.Encoding;
 import org.atlasapi.media.entity.Episode;
 import org.atlasapi.media.entity.Identified;
 import org.atlasapi.media.entity.Location;
 import org.atlasapi.media.entity.Policy.Platform;
 import org.atlasapi.media.entity.Series;
 import org.atlasapi.media.entity.Version;
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormatter;
 import org.joda.time.format.ISODateTimeFormat;
 
 import com.google.common.annotations.VisibleForTesting;
 import com.google.common.base.Function;
 import com.google.common.base.Functions;
 import com.google.common.base.Predicates;
 import com.google.common.base.Strings;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Ordering;
 import com.google.common.collect.Sets;
 import com.metabroadcast.common.time.Clock;
 import com.metabroadcast.common.time.SystemClock;
 
 public class LakeviewFeedCompiler {
 
     private static final Ordering<Broadcast> TRANSMISSION_ORDERING = Ordering.from(new Comparator<Broadcast>() {
                 @Override
                 public int compare(Broadcast o1, Broadcast o2) {
                     return o1.getTransmissionTime().compareTo(o2.getTransmissionTime());
                 }
             });
     private static final DateTimeFormatter DATETIME_FORMAT = ISODateTimeFormat.dateTimeNoMillis();
     private static final XMLNamespace LAKEVIEW = new XMLNamespace("", "http://schemas.microsoft.com/Lakeview/2013/07/01/ingestion");
     private static final String PROVIDER_NAME = "Channel 4";
     private static final String LOCALE = "en-GB";
     private static final String PROVIDER_ID = "0x484707D1";
     private static final String XBOX_ONE_PROVIDER_ID = "25148946";
     private static final Pattern HIERARCHICAL_URI_PATTERN
         = Pattern.compile("http://www.channel4.com/programmes/[a-z0-9\\-]+(\\/.*)?");
 
     private static final String ID_PREFIX = "http://channel4.com/en-GB";
     private static final String C4_PROG_BASE = "http://www.channel4.com/programmes/";
     private static final String C4_API_BASE = "https://xbox.channel4.com/pmlsd/";
     
     private static final String SERIES_ID_PREFIX = ID_PREFIX + "/TVSeries/";
     private static final String EPISODE_ID_PREFIX = ID_PREFIX + "/TVEpisode/";
     
     private final Clock clock;
 	private ChannelResolver channelResolver;
 	private boolean genericTitlesEnabled;
 	private final boolean addXboxOneAvailability;
 
     public LakeviewFeedCompiler(ChannelResolver channelResolver, Clock clock, boolean genericTitlesEnabled, boolean addXboxOneAvailability) {
         this.clock = clock;
         this.channelResolver = channelResolver;
         this.genericTitlesEnabled = genericTitlesEnabled;
         this.addXboxOneAvailability = addXboxOneAvailability;
     }
 
     public LakeviewFeedCompiler(ChannelResolver channelResolver, boolean genericTitlesEnabled, 
             boolean addXboxOneAvailability) {
         this(channelResolver, new SystemClock(), genericTitlesEnabled, addXboxOneAvailability);
     }
 
     public Document compile(List<LakeviewContentGroup> contents) {
 
         Element feed = createElement("Feed", LAKEVIEW);
         feed.addAttribute(new Attribute("ProviderName", PROVIDER_NAME));
 
         //This is specified. Don't use lastUpdated....
         String lastModified = DATETIME_FORMAT.print(clock.now());
 
         for (LakeviewContentGroup contentGroup : contents) {
 
             List<Element> groupElements = elementsForGroup(lastModified, contentGroup);
             
             for (Element element : groupElements) {
                 feed.appendChild(element);
             }
             
         }
 
         return new Document(feed);
     }
 
     private List<Element> elementsForGroup(String lastModified, LakeviewContentGroup contentGroup) {
         DateTime brandPublicationDate = null;
         DateTime brandEndDate = null;
         int addedSeasons = 0;
         
         List<Element> elements = Lists.newLinkedList();
         Set<String> seenItems = Sets.newHashSet();
         
         if (contentGroup.isFlattened()) {
             for (Episode episode : contentGroup.episodes()) {
                 DateTime publicationDate = orginalPublicationDate(episode);
                 if(publicationDate != null) {
                     elements.add(createEpisodeElem(episode, contentGroup.brand(), null, publicationDate, lastModified));
                     brandPublicationDate = publicationDate.isBefore(brandPublicationDate) ? publicationDate : brandPublicationDate;
                     brandEndDate = latestOf(publicationDate, brandEndDate);
                 }
             }
         } else {
             Map<String,Episode> episodeSeriesIndex = Maps.uniqueIndex(contentGroup.episodes(), Identified.TO_URI);
             
             Function<ChildRef, Episode> childRefToEpisode = Functions.compose(Functions.forMap(episodeSeriesIndex, null), ChildRef.TO_URI);
             for (Series series : contentGroup.series()) {
                 DateTime seriesPublicationDate = null;
                 
                 int addedEpisodes = 0;
                 for (Episode episode : sortedSeriesEpisodes(series.getChildRefs(),childRefToEpisode)) {
                     DateTime publicationDate = orginalPublicationDate(episode);
                     if (publicationDate != null) {
                         Element epElem = createEpisodeElem(episode, contentGroup.brand(), series, 
                                 publicationDate, lastModified);
                         String itemId = itemId(epElem);
                         if (!seenItems.contains(itemId)) {
                             elements.add(epElem);
                             addedEpisodes++;
                             seenItems.add(itemId);
                             seriesPublicationDate = earliestOf(publicationDate, seriesPublicationDate);
                             brandEndDate = latestOf(publicationDate, brandEndDate);
                         }
                     }
                 }
                 
                 if(seriesPublicationDate != null) {
                     addedSeasons++;
                     elements.add(elements.size() - addedEpisodes, createSeriesElem(series, contentGroup.brand(), seriesPublicationDate, lastModified));
                     brandPublicationDate = seriesPublicationDate.isBefore(brandPublicationDate) ? seriesPublicationDate : brandPublicationDate;
                 }
             }
         }
         
         Element brandElem = null;
         if(brandPublicationDate != null) {
             brandElem = createBrandElem(contentGroup.brand(), brandPublicationDate, brandEndDate, lastModified, contentGroup, addedSeasons);
             if(brandElem != null) {
                 elements.add(0, brandElem);
             }
         }
         
         return brandElem != null ? elements : ImmutableList.<Element>of();
     }
    
     private String itemId(Element epElem) {
         return epElem.getFirstChildElement("ItemId", LAKEVIEW.getUri()).getValue();
     }
 
     public ImmutableList<Episode> sortedSeriesEpisodes(ImmutableList<ChildRef> childRefs, Function<ChildRef, Episode> childRefToEpisode) {
         Iterable<Episode> episodes = Iterables.filter(Iterables.transform(childRefs, childRefToEpisode),Predicates.notNull());
         return EPISODE_NUMBER_ORDERING.immutableSortedCopy(episodes);
     }
 
     private DateTime latestOf(DateTime publicationDate, DateTime brandEndDate) {
         return brandEndDate == null || publicationDate.isAfter(brandEndDate) ? publicationDate : brandEndDate;
     }
 
     private DateTime earliestOf(DateTime publicationDate, DateTime seriesPublicationDate) {
         return seriesPublicationDate == null || publicationDate.isBefore(seriesPublicationDate) ? publicationDate : seriesPublicationDate;
     }
 
     private Element createBrandElem(Brand brand, DateTime originalPublicationDate, DateTime brandEndDate, String lastModified, LakeviewContentGroup contentGroup, int addedSeasons) {
         Element element = createElement("TVSeries", LAKEVIEW);
         String providerMediaId = brandAtomUri(findHierarchicalUri(brand));
         String brandId = brandId(brand);
         addIdElements(element, brandId, brandId.replaceAll(SERIES_ID_PREFIX, ""));
         element.appendChild(stringElement("Title", LAKEVIEW, Strings.isNullOrEmpty(brand.getTitle()) ? "EMPTY BRAND TITLE" : brand.getTitle()));
         
         appendCommonElements(element, brand, originalPublicationDate, lastModified, providerMediaId, null);
         if (addedSeasons > 0) {
             element.appendChild(stringElement("TotalNumberOfSeasons", LAKEVIEW, String.valueOf(addedSeasons)));
         }
         
         if (brand.getPresentationChannel() != null && channelResolver.fromKey(brand.getPresentationChannel()).hasValue()) {
             element.appendChild(stringElement("Network", LAKEVIEW, channelResolver.fromKey(brand.getPresentationChannel()).requireValue().getTitle()));
         } else {
             List<Broadcast> broadcasts = extractBroadcasts(contentGroup.episodes());
             if (!broadcasts.isEmpty()) {
                 element.appendChild(stringElement("Network", LAKEVIEW, extractNetwork(broadcasts)));
             } else {
                 return null;
             }
         }
 
         if (brandEndDate != null) {
             element.appendChild(stringElement("EndYear", LAKEVIEW, String.valueOf(brandEndDate.getYear())));
         }
         
         return element;
     }
 
     private String extractNetwork(List<Broadcast> broadcasts) {
         return channelResolver.fromUri(TRANSMISSION_ORDERING.min(broadcasts).getBroadcastOn()).requireValue().getTitle();
     }
 
     Element createSeriesElem(Series series, Brand parent, DateTime originalPublicationDate, String lastModified) {
         Element element = createElement("TVSeason", LAKEVIEW);
         String applicationSpecificData = seriesAtomUri(findHierarchicalUri(series));
         String seriesId = seriesId(series);
         String providerMediaId = findHierarchicalUri(series).replaceAll(C4_PROG_BASE, "").replaceAll("/episode-guide/", "/");
         addIdElements(element, seriesId, providerMediaId);
         
         if (genericTitlesEnabled) {
             if (series.getSeriesNumber() != null) {
                 element.appendChild(stringElement("Title", LAKEVIEW, String.format("Series %d", series.getSeriesNumber())));
             } else if (!Strings.isNullOrEmpty(series.getTitle())) {
                 element.appendChild(stringElement("Title", LAKEVIEW, series.getTitle()));
             } else {
                 element.appendChild(stringElement("Title", LAKEVIEW, parent.getTitle()));
             }
         } else if (Strings.isNullOrEmpty(series.getTitle()) || series.getTitle().matches("(?i)series \\d+")) {
             element.appendChild(stringElement("Title", LAKEVIEW, String.format("%s Series %s", parent.getTitle(), series.getSeriesNumber())));
         } else {
             element.appendChild(stringElement("Title", LAKEVIEW, series.getTitle()));
         }
         
         appendCommonElements(element, series, originalPublicationDate, lastModified, applicationSpecificData, null);
         
         element.appendChild(stringElement("SeasonNumber", LAKEVIEW, String.valueOf(series.getSeriesNumber())));
         element.appendChild(stringElement("SeriesId", LAKEVIEW, brandId(parent)));
         
         return element;
     }
 
     Element createEpisodeElem(Episode episode, Brand container, Series series, DateTime originalPublicationDate, String lastModified) {
         Element element = createElement("TVEpisode", LAKEVIEW);
        
        Comment comment = new Comment("Atlas ID: " + episode.getCanonicalUri());
        element.appendChild(comment);
        
         String assetId = extractAssetId(episode);
        String applicationSpecificData = episodeAtomUri(brandAtomUri(findHierarchicalUri(container)), assetId);
         
         String providerMediaId;
         if (series != null) {
             providerMediaId = findHierarchicalUri(series).replaceAll(C4_PROG_BASE, "").replaceAll("/episode-guide/", "/") + "#" + assetId;
         } else {
             providerMediaId = brandId(container).replaceAll(SERIES_ID_PREFIX, "") + "#" + assetId;
         }
         addIdElements(element, EPISODE_ID_PREFIX + providerMediaId, providerMediaId);
         
         if (genericTitlesEnabled) {
             if (episode.getEpisodeNumber() != null) {
                 element.appendChild(stringElement("Title", LAKEVIEW, String.format("Episode %d", episode.getEpisodeNumber())));
             } else if (!Strings.isNullOrEmpty(episode.getTitle())) {
                 element.appendChild(stringElement("Title", LAKEVIEW, episode.getTitle()));
             } else {
                 element.appendChild(stringElement("Title", LAKEVIEW, container.getTitle()));
             }
         } else if (Strings.isNullOrEmpty(episode.getTitle()) || episode.getTitle().matches("(?i)(series \\d+)? episode \\d+")) {
             element.appendChild(stringElement("Title", LAKEVIEW, String.format("%s Series %s Episode %s", container.getTitle(), episode.getSeriesNumber(), episode.getEpisodeNumber())));
         } else {
             element.appendChild(stringElement("Title", LAKEVIEW, episode.getTitle()));
         }
 
         Element instances = createElement("Instances", LAKEVIEW);
         Element videoInstance = createElement("VideoInstance", LAKEVIEW);
         
         Element availabilities = createElement("Availabilities", LAKEVIEW);
         Element availability = createAvailabilityElement(episode, "Xbox360", PROVIDER_ID);
         
         if (addXboxOneAvailability) {
             availabilities.appendChild(createAvailabilityElement(episode, "XboxOne", XBOX_ONE_PROVIDER_ID));
         }
         
         availabilities.appendChild(availability);
         videoInstance.appendChild(availabilities);
         videoInstance.appendChild(stringElement("ResolutionFormat", LAKEVIEW, "SD"));
         videoInstance.appendChild(stringElement("DeliveryFormat", LAKEVIEW, "Streaming"));
         videoInstance.appendChild(stringElement("PrimaryAudioLanguage", LAKEVIEW, "en-GB"));
         videoInstance.appendChild(stringElement("VideoInstanceType", LAKEVIEW, "Full"));
         
         instances.appendChild(videoInstance);           
 
         appendCommonElements(element, episode, originalPublicationDate, lastModified, applicationSpecificData, instances);
         
         element.appendChild(stringElement("EpisodeNumber", LAKEVIEW, String.valueOf(episode.getEpisodeNumber())));
         element.appendChild(stringElement("DurationInSeconds", LAKEVIEW, String.valueOf(duration(episode))));
         element.appendChild(stringElement("SeriesId", LAKEVIEW, brandId(container)));
         if (episode.getSeriesRef() != null) {
             element.appendChild(stringElement("SeasonId", LAKEVIEW, seriesId(series)));
         }
         
         return element;
     }
 
     private Element createAvailabilityElement(Episode episode, String platform, String titleId) {
         Element availability = createElement("Availability", LAKEVIEW);
         availability.appendChild(stringElement("DistributionRight", LAKEVIEW, "Free"));
         availability.appendChild(stringElement("StartDateTime", LAKEVIEW, extractFirstAvailabilityDate(episode).toString(DATETIME_FORMAT)));
         availability.appendChild(stringElement("EndDateTime", LAKEVIEW, extractLastAvailabilityDate(episode).toString(DATETIME_FORMAT)));
         availability.appendChild(stringElement("Platform", LAKEVIEW, platform));
         availability.appendChild(stringElement("TitleId", LAKEVIEW, titleId));
         return availability;
     }
 
     private void addIdElements(Element element, String id, String providerMediaId) {
         element.appendChild(stringElement("ItemId", LAKEVIEW, id));
         element.appendChild(stringElement("ProviderMediaId", LAKEVIEW, providerMediaId));
     }
     
     private DateTime orginalPublicationDate(Episode episode) {
         DateTime broadcastDate = extractFirstBroadcastDate(extractBroadcasts(ImmutableList.of(episode)));
         if(broadcastDate != null) {
             return broadcastDate;
         }
         
         DateTime availabilityDate = extractFirstAvailabilityDate(episode);
         if(availabilityDate != null) {
             return availabilityDate;
         }
         return null;
     }
     
     private DateTime extractFirstAvailabilityDate(Episode episode) {
         for (Version version : episode.getVersions()) {
             for (Encoding encoding : version.getManifestedAs()) {
                 for (Location location : encoding.getAvailableAt()) {
                     if(location.getPolicy() != null && Platform.XBOX.equals(location.getPolicy().getPlatform()) && location.getPolicy().getAvailabilityStart() != null) {
                         return location.getPolicy().getAvailabilityStart();
                     }
                 }
             }
         }
         return null;
     }
     
     private DateTime extractLastAvailabilityDate(Episode episode) {
         for (Version version : episode.getVersions()) {
             for (Encoding encoding : version.getManifestedAs()) {
                 for (Location location : encoding.getAvailableAt()) {
                     if(location.getPolicy() != null && Platform.XBOX.equals(location.getPolicy().getPlatform()) && location.getPolicy().getAvailabilityStart() != null) {
                         return location.getPolicy().getAvailabilityEnd();
                     }
                 }
             }
         }
         return null;
     }
     
     private DateTime extractFirstBroadcastDate(List<Broadcast> broadcasts) {
         if(broadcasts.isEmpty()) {
             return null;
         }
         return TRANSMISSION_ORDERING.min(broadcasts).getTransmissionTime();
     }
 
     private List<Broadcast> extractBroadcasts(Iterable<Episode> episodes) {
         List<Broadcast> broadcasts = Lists.newArrayList();
         for (Episode episode : episodes) {
             for (Version version : episode.getVersions()) {
                 broadcasts.addAll(version.getBroadcasts());
             }
         }
         return broadcasts;
     }
 
     private String extractAssetId(Episode episode) {
         for (Version version : episode.getVersions()) {
             for (Encoding encoding : version.getManifestedAs()) {
                 for (Location location : encoding.getAvailableAt()) {
                     if(location.getTransportType().equals(TransportType.LINK)
                     		&& location.getPolicy() != null 
                     		&& Platform.XBOX.equals(location.getPolicy().getPlatform())) {
                         Pattern pattern = Pattern.compile(".*asset/(\\d+)$");
                         Matcher matcher = pattern.matcher(location.getUri());
                         if(matcher.matches()) {
                             return matcher.group(1);                            
                         }
                     }
                 }
             }
         }
         return "NONE";
     }
 
     private void appendCommonElements(Element element, Content content, DateTime originalPublicationDate, String lastModified, String applicationSpecificData,
     		Element instances) {
         
         if(!Strings.isNullOrEmpty(content.getDescription())) {
             element.appendChild(stringElement("Description", LAKEVIEW, content.getDescription()));
         }
         
         if(applicationSpecificData != null) {
             element.appendChild(stringElement("ApplicationSpecificData", LAKEVIEW, applicationSpecificData));
         }
         
         element.appendChild(stringElement("LastModifiedDate", LAKEVIEW, lastModified));
         element.appendChild(stringElement("ApplicableLocale", LAKEVIEW, LOCALE));
         
         if(content instanceof Brand && content.getImage() != null) {
             
             Element imageElem = createElement("Image", LAKEVIEW);
             imageElem.appendChild(stringElement("ImagePurpose", LAKEVIEW, "BoxArt"));
             imageElem.appendChild(stringElement("Url", LAKEVIEW, content.getImage()));
             
             Element imagesElement = createElement("Images", LAKEVIEW);
             imagesElement.appendChild(imageElem);
             element.appendChild(imagesElement);
         }
         
         if(!content.getGenres().isEmpty()) {
             Element genres = createElement("Genres", LAKEVIEW);
             for (String genre : content.getGenres()) {
                 if(genre.startsWith("http://www.channel4.com")) {
                     genres.appendChild(stringElement("Genre", LAKEVIEW, C4GenreTitles.title(genre)));
                 }
             }
             element.appendChild(genres);
         }
         
         Element pc = createElement("ParentalControl", LAKEVIEW);
         pc.appendChild(stringElement("HasGuidance", LAKEVIEW, String.valueOf(true)));
         element.appendChild(pc);
         element.appendChild(stringElement("PublicWebUri", LAKEVIEW, String.format("%s.atom", webUriRoot(content))));
         
         if(instances != null) {
         	element.appendChild(instances);
         }
         
         element.appendChild(stringElement("OriginalPublicationDate", LAKEVIEW, originalPublicationDate.toString(DATETIME_FORMAT)));
     }
 
     private String webUriRoot(Content content) {
         if (content instanceof Episode) {
             return findHierarchicalUri((Episode)content);
         }
         return content.getCanonicalUri();
     }
     
     private String brandId(Brand brand) {
         // TVSeries
         return idFrom("TVSeries", findHierarchicalUri(brand).replaceAll(C4_PROG_BASE, ""));
     }
 
     private String seriesId(Series series) {
         // TVSeason
         return idFrom("TVSeason", findHierarchicalUri(series).replaceAll(C4_PROG_BASE, "").replaceAll("/episode-guide/", "/"));
     }
 
     private String episodeId(Episode episode) {
         // TVEpisode
         String episodeUri = findHierarchicalUri(episode);
         return idFrom("TVEpisode", episodeUri.replaceAll(C4_PROG_BASE, "").replaceAll("/episode-guide/(series-\\d+)/(episode-\\d+)", "-$1-$2"));
     }
     
     private String idFrom(String type, String id) {
         return String.format("%s/%s/%s", ID_PREFIX, type, id);
     }
     
     public String brandAtomUri(String brandUri) {
     	return String.format("%s%s/4od.atom", C4_API_BASE, brandUri.replaceAll(C4_PROG_BASE, ""));
     }
     
     public String seriesAtomUri(String seriesUri) {
     	return String.format("%s%s/4od.atom#%s", C4_API_BASE, seriesUri.replaceAll(C4_PROG_BASE, "").replaceAll("/episode-guide.*", ""), seriesUri.replaceAll(C4_PROG_BASE + ".*/episode-guide/", ""));
     }
     
     @VisibleForTesting
    public String episodeAtomUri(String brandAtomUri, String assetId) {
    	return String.format("%s#%s", brandAtomUri, assetId);
     }
     
     private static String findHierarchicalUri(Identified id) {
         if (isHierarchicalUri(id.getCanonicalUri())) {
             return id.getCanonicalUri();
         }
         for (String alias : id.getAliasUrls()) {
             if (isHierarchicalUri(alias)) {
                 return alias;
             }
         }
         throw new IllegalStateException(id + " : no hierarchical uri");
     }
 
     private static boolean isHierarchicalUri(String uri) {
         return HIERARCHICAL_URI_PATTERN.matcher(uri).matches();
     }
 
     private Integer duration(Episode episode) {
         for (Version version : episode.getVersions()) {
             Integer duration = version.getDuration();
             if (duration != null) {
                 return duration;
             }
         }
         return null;
     }
 
     protected Element stringElement(String name, XMLNamespace ns, String value) {
         Element elem = createElement(name, ns);
         elem.appendChild(value);
         return elem;
     }
 
     protected Element createElement(String name, XMLNamespace ns) {
         Element elem = new Element(name, ns.getUri());
         if (!LAKEVIEW.equals(ns)) {
             elem.setNamespacePrefix(ns.getPrefix());
         }
         return elem;
     }
 
 }
