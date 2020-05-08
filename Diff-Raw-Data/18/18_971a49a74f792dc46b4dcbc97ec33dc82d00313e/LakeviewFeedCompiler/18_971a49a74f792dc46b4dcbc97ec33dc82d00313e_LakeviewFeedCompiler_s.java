 package org.atlasapi.feeds.lakeview;
 
 import java.util.Collection;
 import java.util.Comparator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import nu.xom.Document;
 import nu.xom.Element;
 
 import org.atlasapi.feeds.xml.XMLNamespace;
 import org.atlasapi.media.TransportType;
 import org.atlasapi.media.entity.Brand;
 import org.atlasapi.media.entity.Broadcast;
 import org.atlasapi.media.entity.Channel;
 import org.atlasapi.media.entity.Content;
 import org.atlasapi.media.entity.Encoding;
 import org.atlasapi.media.entity.Episode;
 import org.atlasapi.media.entity.Location;
 import org.atlasapi.media.entity.Series;
 import org.atlasapi.media.entity.Version;
 import org.joda.time.DateTime;
 import org.joda.time.format.DateTimeFormatter;
 import org.joda.time.format.ISODateTimeFormat;
 
 import com.google.common.base.Strings;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableList.Builder;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Ordering;
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
     private static final XMLNamespace LAKEVIEW = new XMLNamespace("", "http://schemas.microsoft.com/Lakeview/2011/06/13/ingestion");
     private static final String PROVIDER_ID = "0x484707D1";
     
     private final Clock clock;
 
     public LakeviewFeedCompiler(Clock clock) {
         this.clock = clock;
     }
 
     public LakeviewFeedCompiler() {
         this(new SystemClock());
     }
 
     public Document compile(List<LakeviewContentGroup> contents) {
 
         Element feed = createElement("Feed", LAKEVIEW);
 
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
         
         ImmutableMap.Builder<Element, List<Element>> seriesEpisodesElems = ImmutableMap.builder();
         for (Entry<Series, Collection<Episode>> seriesEpisodes : contentGroup.contents().entrySet()) {
             
             DateTime seriesPublicationDate = null;
             
             ImmutableList.Builder<Element> episodeEntries = ImmutableList.builder();
             for (Episode episode : seriesEpisodes.getValue()) {
                 DateTime publicationDate = orginalPublicationDate(episode);
                 if(publicationDate != null) {
                     episodeEntries.add(createEpisodeElem(episode, contentGroup.brand(), publicationDate, lastModified));
                     seriesPublicationDate = earliestOf(publicationDate, seriesPublicationDate);
                     brandEndDate = latestOf(publicationDate, brandEndDate);
                 }
             }
             
             if(seriesPublicationDate != null) {
                 seriesEpisodesElems.put(createSeriesElem(seriesEpisodes.getKey(), contentGroup.brand(), seriesPublicationDate, lastModified), episodeEntries.build());
                 brandPublicationDate = seriesPublicationDate.isBefore(brandPublicationDate) ? seriesPublicationDate : brandPublicationDate;
             }
         }
         
         Builder<Element> elements = ImmutableList.builder();
         
         if(brandPublicationDate != null) {
             Element brandElem = createBrandElem(contentGroup.brand(), brandPublicationDate, brandEndDate, lastModified, contentGroup);
             if(brandElem != null) {
                 appendElements(elements, brandElem, seriesEpisodesElems.build());
             }
         }
         
         return elements.build();
     }
 
     private DateTime latestOf(DateTime publicationDate, DateTime brandEndDate) {
         return brandEndDate == null || publicationDate.isAfter(brandEndDate) ? publicationDate : brandEndDate;
     }
 
     private DateTime earliestOf(DateTime publicationDate, DateTime seriesPublicationDate) {
         return seriesPublicationDate == null || publicationDate.isBefore(seriesPublicationDate) ? publicationDate : seriesPublicationDate;
     }
 
     private void appendElements(Builder<Element> elements, Element brandElem, Map<Element, List<Element>> seriesEpisodeElements) {
         elements.add(brandElem);
         for (Entry<Element, List<Element>> seriesEpisodes : seriesEpisodeElements.entrySet()) {
             elements.add(seriesEpisodes.getKey()).addAll(seriesEpisodes.getValue());
         }
     }
     
     private List<Episode> episodes(LakeviewContentGroup contentGroup) {
         return ImmutableList.copyOf(Iterables.concat(contentGroup.contents().values()));
     }
 
     private Element createBrandElem(Brand brand, DateTime originalPublicationDate, DateTime brandEndDate, String lastModified, LakeviewContentGroup contentGroup) {
         Element element = createElement("TVSeries", LAKEVIEW);
         element.appendChild(stringElement("Provider", LAKEVIEW, PROVIDER_ID));
         element.appendChild(stringElement("ItemId", LAKEVIEW, brandId(brand.getCanonicalUri())));
         element.appendChild(stringElement("Title", LAKEVIEW, Strings.isNullOrEmpty(brand.getTitle()) ? "EMPTY BRAND TITLE" : brand.getTitle()));
         
        appendCommonElements(element, brand, originalPublicationDate, lastModified);
         element.appendChild(stringElement("TotalNumberOfSeasons", LAKEVIEW, String.valueOf(contentGroup.contents().keySet().size())));
         
         if (brand.getPresentationChannel() != null && Channel.fromKey(brand.getPresentationChannel()).hasValue()) {
             element.appendChild(stringElement("Network", LAKEVIEW, Channel.fromKey(brand.getPresentationChannel()).requireValue().title()));
         } else {
             List<Broadcast> broadcasts = extractBroadcasts(episodes(contentGroup));
             if (!broadcasts.isEmpty()) {
                 element.appendChild(stringElement("Network", LAKEVIEW, extractNetwork(broadcasts)));
             } else {
                 return null;
             }
         }
 
         if(brandEndDate != null) {
             element.appendChild(stringElement("EndYear", LAKEVIEW, String.valueOf(brandEndDate.getYear())));
         }
         
         return element;
     }
 
     private String extractNetwork(List<Broadcast> broadcasts) {
         return Channel.fromUri(TRANSMISSION_ORDERING.min(broadcasts).getBroadcastOn()).requireValue().title();
     }
 
     private Element createSeriesElem(Series series, Brand parent, DateTime originalPublicationDate, String lastModified) {
         Element element = createElement("TVSeason", LAKEVIEW);
         element.appendChild(stringElement("Provider", LAKEVIEW, PROVIDER_ID));
         element.appendChild(stringElement("ItemId", LAKEVIEW, seriesId(series.getCanonicalUri())));
         
         if(Strings.isNullOrEmpty(series.getTitle()) || series.getTitle().matches("(?i)series \\d+")) {
             element.appendChild(stringElement("Title", LAKEVIEW, String.format("%s Series %s", parent.getTitle(), series.getSeriesNumber())));
         } else {
             element.appendChild(stringElement("Title", LAKEVIEW, series.getTitle()));
         }
         
        appendCommonElements(element, series, originalPublicationDate, lastModified);
         
         element.appendChild(stringElement("SeasonNumber", LAKEVIEW, String.valueOf(((Series) series).getSeriesNumber())));
         element.appendChild(stringElement("SeriesId", LAKEVIEW, brandId(((Series) series).getParent().getUri())));
         
         return element;
     }
 
     private Element createEpisodeElem(Episode episode, Brand container, DateTime originalPublicationDate, String lastModified) {
         Element element = createElement("TVEpisode", LAKEVIEW);
         element.appendChild(stringElement("Provider", LAKEVIEW, PROVIDER_ID));
         element.appendChild(stringElement("ItemId", LAKEVIEW, episodeId(episode.getCanonicalUri())));
         
         if(Strings.isNullOrEmpty(episode.getTitle()) || episode.getTitle().matches("(?i)(series \\d+)? episode \\d+")) {
             element.appendChild(stringElement("Title", LAKEVIEW, String.format("%s Series %s Episode %s", container.getTitle(), episode.getSeriesNumber(), episode.getEpisodeNumber())));
         } else {
             element.appendChild(stringElement("Title", LAKEVIEW, episode.getTitle()));
         }
         
        appendCommonElements(element, episode, originalPublicationDate, lastModified);

        element.appendChild(stringElement("ApplicationSpecificData", LAKEVIEW, extract4OdId(episode)));
         
         element.appendChild(stringElement("EpisodeNumber", LAKEVIEW, String.valueOf(episode.getEpisodeNumber())));
         element.appendChild(stringElement("DurationInSeconds", LAKEVIEW, String.valueOf(duration(episode))));
         element.appendChild(stringElement("SeriesId", LAKEVIEW, brandId(episode.getContainer().getUri())));
         element.appendChild(stringElement("SeasonId", LAKEVIEW, seriesId(episode.getSeriesRef().getUri())));
         
         return element;
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
                     if(location.getPolicy() != null && location.getPolicy().getAvailabilityStart() != null) {
                         return location.getPolicy().getAvailabilityStart();
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
 
     private String extract4OdId(Episode episode) {
         for (Version version : episode.getVersions()) {
             for (Encoding encoding : version.getManifestedAs()) {
                 for (Location location : encoding.getAvailableAt()) {
                     if(location.getTransportType().equals(TransportType.LINK)) {
                         Pattern pattern = Pattern.compile(".*#(\\d+)$");
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
 
    private void appendCommonElements(Element element, Content content, DateTime originalPublicationDate, String lastModified) {
         
         if(!Strings.isNullOrEmpty(content.getDescription())) {
             element.appendChild(stringElement("Description", LAKEVIEW, content.getDescription()));
         }
         
         element.appendChild(stringElement("IsUserGenerated", LAKEVIEW, "false"));
         
         if(content instanceof Brand && content.getImage() != null) {
             
             Element imageElem = createElement("Image", LAKEVIEW);
             imageElem.appendChild(stringElement("ImagePurpose", LAKEVIEW, "BoxArt"));
             imageElem.appendChild(stringElement("Url", LAKEVIEW, content.getImage()));
             
             Element imagesElement = createElement("Images", LAKEVIEW);
             imagesElement.appendChild(imageElem);
             element.appendChild(imagesElement);
         }
         
         element.appendChild(stringElement("LastModifiedDate", LAKEVIEW, lastModified));
         
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
         element.appendChild(stringElement("PublicWebUri", LAKEVIEW, String.format("%s.atom", content.getCanonicalUri())));

         element.appendChild(stringElement("OriginalPublicationDate", LAKEVIEW, originalPublicationDate.toString(DATETIME_FORMAT)));
     }
 
     private static final String ID_PREFIX = "http://channel4.com/en-GB";
     private static final String C4_PROG_BASE = "http://www.channel4.com/programmes/";
     
     private String brandId(String brandUri) {
         return String.format("%s/TVSeries/%s", ID_PREFIX, brandUri.replaceAll(C4_PROG_BASE, ""));
     }
 
     private String seriesId(String seriesUri) {
         return String.format("%s/TVSeason/%s", ID_PREFIX, seriesUri.replaceAll(C4_PROG_BASE, "").replaceAll("/episode-guide/", "-"));
     }
 
     private String episodeId(String episodeUri) {
         return String.format("%s/TVEpisode/%s", ID_PREFIX, episodeUri.replaceAll(C4_PROG_BASE, "").replaceAll("/episode-guide/(series-\\d+)/(episode-\\d+)", "-$1-$2"));
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
