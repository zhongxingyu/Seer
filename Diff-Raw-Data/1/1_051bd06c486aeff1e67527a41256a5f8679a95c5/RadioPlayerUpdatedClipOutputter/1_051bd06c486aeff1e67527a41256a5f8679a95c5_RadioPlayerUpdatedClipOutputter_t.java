 package org.atlasapi.feeds.radioplayer.outputting;
 
 import static com.google.common.base.Functions.compose;
 import static com.google.common.base.Preconditions.checkArgument;
 import static com.google.common.collect.Iterables.concat;
 import static com.google.common.collect.Iterables.filter;
 import static com.google.common.collect.Iterables.transform;
 
 import java.util.List;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import nu.xom.Attribute;
 import nu.xom.Element;
 
 import org.atlasapi.feeds.radioplayer.RadioPlayerFeedSpec;
 import org.atlasapi.feeds.radioplayer.RadioPlayerOdFeedSpec;
 import org.atlasapi.feeds.radioplayer.RadioPlayerService;
 import org.atlasapi.media.entity.Clip;
 import org.atlasapi.media.entity.Container;
 import org.atlasapi.media.entity.Described;
 import org.atlasapi.media.entity.Encoding;
 import org.atlasapi.media.entity.Identified;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.Location;
 import org.atlasapi.media.entity.MediaType;
 import org.atlasapi.media.entity.Policy;
 import org.atlasapi.media.entity.Version;
 import org.joda.time.DateTime;
 import org.joda.time.Interval;
 
 import com.google.common.base.Function;
 import com.google.common.base.Functions;
 import com.google.common.base.Optional;
 import com.google.common.base.Predicate;
 import com.google.common.base.Predicates;
 import com.google.common.base.Strings;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableList.Builder;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Ordering;
 import com.google.common.collect.Sets;
 import com.metabroadcast.common.base.MorePredicates;
 import com.metabroadcast.common.intl.Countries;
 import com.metabroadcast.common.intl.Country;
 import com.metabroadcast.common.time.DateTimeZones;
 
 public class RadioPlayerUpdatedClipOutputter extends RadioPlayerXMLOutputter {
     
     private static final String ORIGINATOR = "Metabroadcast";
     private static final String ONDEMAND_LOCATION = "http://www.bbc.co.uk/iplayer/console/";
     private static final DateTime MAX_AVAILABLE_TILL = new DateTime(2037, 01, 01, 0, 0, 0, 0, DateTimeZones.UTC);
     
     private final RadioPlayerGenreElementCreator genreElementCreator = new RadioPlayerGenreElementCreator();
 
     @Override
     protected Element createFeed(RadioPlayerFeedSpec spec, Iterable<RadioPlayerBroadcastItem> items) {
         
         checkArgument(spec instanceof RadioPlayerOdFeedSpec);
         Optional<DateTime> since = ((RadioPlayerOdFeedSpec)spec).getSince();
         Iterable<RadioPlayerBroadcastItem> validItems = filter(items, hasUpdatedAndAvailableClip(since));
         Iterable<Clip> validClips = filter(concat(transform(items, compose(Item.TO_CLIPS, RadioPlayerBroadcastItem.TO_ITEM))), availableAndUpdatedSince(since));
         
         Element epgElem = createElement("epg", EPGSCHEDULE);
         EPGDATATYPES.addDeclarationTo(epgElem);
         XSI.addDeclarationTo(epgElem);
         RADIOPLAYER.addDeclarationTo(epgElem);
         epgElem.addAttribute(new Attribute("xsi:schemaLocation", XSI.getUri(), SCHEMALOCATION));
         epgElem.addAttribute(new Attribute("system", "DAB"));
         epgElem.addAttribute(new Attribute("xml:lang", "http://www.w3.org/XML/1998/namespace", "en"));
 
         Element schedule = createElement("schedule", EPGSCHEDULE);
         schedule.addAttribute(new Attribute("originator", ORIGINATOR));
         schedule.addAttribute(new Attribute("version", "1"));
         schedule.addAttribute(new Attribute("creationTime", DATE_TIME_FORMAT.print(new DateTime(DateTimeZones.UTC))));
 
         schedule.appendChild(scopeElement(getScopeInterval(validClips), spec.getService()));
 
         for (RadioPlayerBroadcastItem item : validItems) {
             Iterable<Element> clipElements = createClipElements(item, spec.getService(), since);
             for (Element clipElement : clipElements) {
                 schedule.appendChild(clipElement);
             }
         }
 
         epgElem.appendChild(schedule);
         return epgElem;
     }
     
     private static Predicate<RadioPlayerBroadcastItem> hasUpdatedAndAvailableClip(Optional<DateTime> since) {
         return MorePredicates.transformingPredicate(TO_CLIPS, MorePredicates.anyPredicate(availableAndUpdatedSince(since)));
     }
     
     public static Predicate<Clip> availableAndUpdatedSince(final Optional<DateTime> since) {
         if (since.isPresent()) {
             return Predicates.and(AUDIO_AND_AVAILABLE, updatedSince(since.get()));
         } else {
             return AUDIO_AND_AVAILABLE;
         }
     }
     
     private Element scopeElement(Interval scopeInterval, RadioPlayerService id) {
         
         Element scope = createElement("scope", EPGSCHEDULE);
         scope.addAttribute(new Attribute("startTime", DATE_TIME_FORMAT.print(scopeInterval.getStart())));
         scope.addAttribute(new Attribute("stopTime", DATE_TIME_FORMAT.print(scopeInterval.getEnd())));
 
         Element service = createElement("serviceScope", EPGSCHEDULE);
         service.addAttribute(new Attribute("id", id.getDabServiceId().replaceAll("_", ".")));
         service.addAttribute(new Attribute("radioplayerId", String.valueOf(id.getRadioplayerId())));
         scope.appendChild(service);
         return scope;
     }
     
     private Interval getScopeInterval(Iterable<Clip> validClips) {
         DateTime start = null;
         DateTime end = null;
         
         for (Clip clip : validClips) {
             Set<Version> versions = clip.getVersions();
             for (Version version : versions) {
                 Set<Encoding> manifestedAs = version.getManifestedAs();
                 for (Encoding encoding : manifestedAs) {
                     Set<Location> availableAt = encoding.getAvailableAt();
                     for (Location location : availableAt) {
                         DateTime availableFrom = location.getPolicy().getAvailabilityStart();
                         if (start == null || start.isAfter(availableFrom)) {
                             start = availableFrom;
                         }
                         
                         DateTime availableUntil = location.getPolicy().getAvailabilityEnd();
                         if (end == null || end.isAfter(availableUntil)) {
                             end = availableUntil;
                         }
                     }
                 }
             }
         }
         
         return new Interval(start, end);
     }
 
     private Iterable<Element> createClipElements(RadioPlayerBroadcastItem broadcastItem, RadioPlayerService id, Optional<DateTime> since) {
         
         Iterable<Clip> clips = filter(broadcastItem.getItem().getClips(), availableAndUpdatedSince(since));
         
         Builder<Element> elements = ImmutableList.builder();
         for (Clip clip : clips) {
             for (Version version : clip.getVersions()) {
                 Element programme = createElement("programme", EPGSCHEDULE);
                 programme.addAttribute(new Attribute("shortId", "0"));
                 
                 programme.addAttribute(new Attribute("id", clip.getCanonicalUri().replace("http://", "crid://")));
         
                 String title = clipTitle(itemTitle(broadcastItem), clip);
                 programme.appendChild(stringElement("mediumName", EPGDATATYPES, MEDIUM_TITLE.truncatePossibleNull(title)));
                 programme.appendChild(stringElement("longName", EPGDATATYPES, LONG_TITLE.truncatePossibleNull(title)));
         
                 programme.appendChild(mediaDescription(stringElement("shortDescription", EPGDATATYPES, SHORT_DESC.truncatePossibleNull(broadcastItem.getItem().getDescription()))));
                 if (!Strings.isNullOrEmpty(broadcastItem.getItem().getImage())) {
                     programme.appendChild(mediaDescription(imageDescriptionElem(broadcastItem.getItem())));
                 }
         
                 for (Element genreElement : genreElementCreator.genreElementsFor(broadcastItem.getItem())) {
                     programme.appendChild(genreElement);
                 }
                 
                 Set<Country> outputCountries = Sets.newHashSet();
                 for (Encoding encoding : version.getManifestedAs()) {
                     for (Location location : encoding.getAvailableAt()) {
                         for (Country country : representedBy(encoding, location)) {
                             if (!outputCountries.contains(country)) {
                                 programme.appendChild(ondemandElement(clip, location, country));
                                 outputCountries.add(country);
                             }
                         }
                     }
                 }
                elements.add(programme);
             }
         }
 
         return elements.build();
     }
     
     private String clipTitle(String itemTitle, Clip clip) {
         if (Strings.isNullOrEmpty(clip.getTitle())) {
             return itemTitle;
         } else {
             return itemTitle + " : " + clip.getTitle();
         }
     }
     
     private String itemTitle(RadioPlayerBroadcastItem broadcastItem) {
         String title = Strings.nullToEmpty(broadcastItem.getItem().getTitle());
         if (broadcastItem.hasContainer()) {
             Container brand = broadcastItem.getContainer();
             if (!Strings.isNullOrEmpty(brand.getTitle())) {
                 String brandTitle = brand.getTitle();
                 if (!brandTitle.equals(title)) {
                     return brandTitle + " : " + title;
                 }
             }
         }
         return title;
     }
     
     private Element mediaDescription(Element childElem) {
         Element descriptionElement = createElement("mediaDescription", EPGDATATYPES);
         descriptionElement.appendChild(childElem);
         return descriptionElement;
     }
     
     private Element imageDescriptionElem(Item item) {
         Element imageElement = createElement("multimedia", EPGDATATYPES);
         imageElement.addAttribute(new Attribute("mimeValue", "image/jpeg"));
         imageElement.addAttribute(new Attribute("url", imageLocationFrom(item)));
         imageElement.addAttribute(new Attribute("width", "86"));
         imageElement.addAttribute(new Attribute("height", "48"));
         return imageElement;
     }
 
     private String imageLocationFrom(Item item) {
         Pattern p = Pattern.compile("(.*)_\\d+_\\d+.jpg");
         Matcher m = p.matcher(item.getImage());
         if (m.matches()) {
             return m.group(1) + "_86_48.jpg";
         }
         return item.getImage();
     }
     
     private final Set<Country> representedBy(Encoding encoding, Location location) {
         Policy policy = location.getPolicy();
         if (policy == null) {
             return ImmutableSet.of();
         }
         Set<Country> countries = Sets.newHashSet();
         if (policy.getAvailableCountries().contains(Countries.ALL)) {
             countries.add(Countries.ALL);
             countries.add(Countries.GB);
         }
         if (policy.getAvailableCountries().contains(Countries.GB)) {
             countries.add(Countries.GB);
         }
         if (policy.getAvailableCountries().isEmpty()) {
             countries.add(Countries.ALL);
         }
         return countries;
     }
     
     private Element ondemandElement(Clip item, Location location, Country country) {
         Element ondemandElement = createElement("ondemand", EPGDATATYPES);
 
         ondemandElement.appendChild(stringElement("player", RADIOPLAYER, ONDEMAND_LOCATION + item.getCurie().substring(item.getCurie().indexOf(":") + 1)));
 
         Policy policy = location.getPolicy();
         if (policy != null) {
              
             // disabled
             // addRestriction(ondemandElement, country);
 
             DateTime availableTill = Ordering.natural().min(policy.getAvailabilityEnd(), MAX_AVAILABLE_TILL);
             DateTime availableFrom = policy.getAvailabilityStart();
             if (availableTill != null && availableFrom != null) {
                 Element availabilityElem = createElement("availability", RADIOPLAYER);
                 Element availabilityScopeElem = createElement("scope", RADIOPLAYER);
                 availabilityScopeElem.addAttribute(new Attribute("startTime", DATE_TIME_FORMAT.print(availableFrom)));
                 availabilityScopeElem.addAttribute(new Attribute("stopTime", DATE_TIME_FORMAT.print(availableTill)));
                 availabilityElem.appendChild(availabilityScopeElem);
                 ondemandElement.appendChild(availabilityElem);
             }
 
         }
 
         return ondemandElement;
     }
     
     private static Predicate<Identified> updatedSince(final DateTime since) {
         return new Predicate<Identified>() {
             @Override
             public boolean apply(Identified input) {
                 return input.getLastUpdated().isAfter(since);
             }
         };
     }
     
     private static final Predicate<Described> AUDIO_MEDIA_TYPE = new Predicate<Described>() {
         @Override
         public boolean apply(Described input) {
             return input.getMediaType().equals(MediaType.AUDIO);
         }
     };
     
     private static final Predicate<Clip> AUDIO_AND_AVAILABLE = Predicates.and(Clip.IS_AVAILABLE, AUDIO_MEDIA_TYPE);
     
     private static final Function<RadioPlayerBroadcastItem, List<Clip>> TO_CLIPS = Functions.compose(Item.TO_CLIPS, RadioPlayerBroadcastItem.TO_ITEM);
 }
