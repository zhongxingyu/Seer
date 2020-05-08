 package org.atlasapi.media.entity;
 
 import static com.google.common.base.Preconditions.checkArgument;
 
 import org.atlasapi.application.SourceStatus;
 
 import com.google.common.base.Function;
 import com.google.common.base.Splitter;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Iterables;
 import com.metabroadcast.common.base.Maybe;
 import com.metabroadcast.common.intl.Countries;
 import com.metabroadcast.common.intl.Country;
 
 public enum Publisher {
     
     BBC("BBC", "bbc.co.uk", Countries.GB, SourceStatus.AVAILABLE_ENABLED),
     C4("Channel 4", "channel4.com", Countries.GB, SourceStatus.UNAVAILABLE),
     HULU("Hulu", "hulu.com", Countries.US, SourceStatus.AVAILABLE_ENABLED),
     YOUTUBE("YouTube", "youtube.com", Countries.ALL, SourceStatus.AVAILABLE_ENABLED),
     TED("TED", "ted.com", Countries.ALL, SourceStatus.AVAILABLE_ENABLED),
     VIMEO("VIMEO", "vimeo.com", Countries.ALL, SourceStatus.AVAILABLE_ENABLED),
     ITV("ITV", "itv.com", Countries.GB, SourceStatus.UNAVAILABLE), 
     BLIP("blip.tv", "blip.tv", Countries.ALL, SourceStatus.AVAILABLE_ENABLED), 
     DAILYMOTION("Dailymotion", "dailymotion.com", Countries.ALL, SourceStatus.AVAILABLE_ENABLED), 
     FLICKR("Flickr", "flickr.com", Countries.ALL, SourceStatus.AVAILABLE_ENABLED), 
     FIVE("Five", "five.tv", Countries.GB, SourceStatus.UNAVAILABLE),
     SEESAW("SeeSaw", "seesaw.com", Countries.GB, SourceStatus.AVAILABLE_ENABLED),
     TVBLOB("TV Blob", "tvblob.com", Countries.IT, SourceStatus.AVAILABLE_ENABLED),
     ICTOMORROW("ICTomorrow", "ictomorrow.co.uk", Countries.GB, SourceStatus.AVAILABLE_ENABLED),
     HBO("HBO", "hbo.com", Countries.US, SourceStatus.AVAILABLE_ENABLED),
     ITUNES("iTunes", "itunes.com", Countries.ALL, SourceStatus.UNAVAILABLE),
     MSN_VIDEO("MSN Video", "video.uk.msn.com", Countries.GB, SourceStatus.AVAILABLE_ENABLED),
     PA("PA", "pressassociation.com", Countries.GB, SourceStatus.UNAVAILABLE),
     RADIO_TIMES("Radio Times", "radiotimes.com", Countries.GB, SourceStatus.UNAVAILABLE),
     PREVIEW_NETWORKS("Preview Networks", "previewnetworks.com", Countries.GB, SourceStatus.UNAVAILABLE),
     ARCHIVE_ORG("Archive.org", "archive.org", Countries.ALL, SourceStatus.AVAILABLE_ENABLED),
     WORLD_SERVICE("BBC World Service Archive", "wsarchive.bbc.co.uk", Countries.ALL, SourceStatus.UNAVAILABLE),
     BBC_REDUX("BBC Redux", "bbcredux.com", Countries.GB, SourceStatus.UNAVAILABLE),
     METABROADCAST("MetaBroadcast", "metabroadcast.com", Countries.ALL, SourceStatus.AVAILABLE_ENABLED),
     DBPEDIA("DBpedia", "dbpedia.org", Countries.ALL, SourceStatus.AVAILABLE_ENABLED),
     LOVEFILM("LoveFilm", "lovefilm.com", Countries.GB, SourceStatus.UNAVAILABLE),
     BBC_PRODUCTS("BBC Commercial Availability", "products.bbc.co.uk", Countries.GB, SourceStatus.UNAVAILABLE),
     MUSIC_BRAINZ("MusicBrainz", "musicbrainz.org", Countries.ALL, SourceStatus.AVAILABLE_ENABLED),
     EMI_PUB("EMI Music Publishing", "emimusicpub.com", Countries.GB, SourceStatus.UNAVAILABLE),
     THESPACE("The Space", "thespace.org", Countries.ALL, SourceStatus.AVAILABLE_ENABLED),
     VOILA("Voila", "voila.metabroadcast.com", Countries.ALL, SourceStatus.UNAVAILABLE),
     MAGPIE("Magpie", "magpie.metabroadcast.com", Countries.ALL, SourceStatus.UNAVAILABLE),
     LONDON_ALSO("London ALSO", "london.metabroadcast.com", Countries.ALL, SourceStatus.UNAVAILABLE),
     BBC_MUSIC("BBC Music","music.bbc.co.uk", Countries.GB, SourceStatus.UNAVAILABLE),
     SPOTIFY("Spotify", "spotify.com", Countries.ALL, SourceStatus.UNAVAILABLE),
     SOUNDCLOUD("Soundcloud", "soundcloud.com", Countries.ALL, SourceStatus.UNAVAILABLE),
     RDIO("rdio", "rdio.com",Countries.ALL, SourceStatus.UNAVAILABLE),
     AMAZON_UK("Amazon UK", "amazon.co.uk", Countries.GB, SourceStatus.UNAVAILABLE),
    BBC_RD_TOPIC("BBC R&D Live Topics", "http://live-topics.prototype0.net/", Countries.GB, SourceStatus.UNAVAILABLE),
     FACEBOOK("Facebook Graph API", "graph.facebook.com", Countries.ALL, SourceStatus.AVAILABLE_ENABLED);
 
     private static final Splitter CSV_SPLITTER = Splitter.on(',').trimResults();
 
     private final String key;
     private final Country country;
     private final String title;
     private final SourceStatus defaultStatus;
 
     Publisher(String title, String key, Country country, SourceStatus defaultStatus) {
         this.title = title;
         this.key = key;
         this.country = country;
         this.defaultStatus = defaultStatus;
     }
 
     public String title() {
         return title;
     }
 
     public String key() {
         return key;
     }
 
     public Country country() {
         return country;
     }
 
     public static Maybe<Publisher> fromKey(String key) {
         for (Publisher publisher : Publisher.values()) {
             if (key.equals(publisher.key())) {
                 return Maybe.just(publisher);
             }
         }
         return Maybe.nothing();
     }
 
     @Override
     public String toString() {
     	return key();
     }
 
     public static Function<Publisher, String> TO_KEY = new Function<Publisher, String>() {
         @Override
         public String apply(Publisher from) {
             return from.key();
         }
     };
 
     public static Function<String, Publisher> FROM_KEY = new Function<String, Publisher>() {
         @Override
         public Publisher apply(String key) {
             Maybe<Publisher> found = fromKey(key);
             checkArgument(found.hasValue(), "Not a valid publisher key: " + key);
             return found.requireValue();
         }
     };
 
     public static ImmutableList<Publisher> fromCsv(String csv) {
         return ImmutableList.copyOf(Iterables.transform(CSV_SPLITTER.split(csv), FROM_KEY));
     }
 
     public SourceStatus getDefaultSourceStatus() {
         return defaultStatus;
     }
     
     private static final ImmutableSet<Publisher> ALL = ImmutableSet.copyOf(values());
     
     public static final ImmutableSet<Publisher> all() {
         return ALL;
     }
 }
