 package org.atlasapi.media.entity;
 
 import static com.google.common.base.Preconditions.checkArgument;
 
 import com.google.common.base.Function;
 import com.google.common.base.Preconditions;
 import com.google.common.base.Splitter;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Iterables;
 import com.metabroadcast.common.base.Maybe;
 import com.metabroadcast.common.intl.Countries;
 import com.metabroadcast.common.intl.Country;
 
 public enum Publisher {
     
 	BBC("BBC", "bbc.co.uk", Countries.GB),
     C4("Channel 4", "channel4.com", Countries.GB),
     HULU("Hulu", "hulu.com", Countries.US),
     YOUTUBE("YouTube", "youtube.com", Countries.ALL),
     TED("TED", "ted.com", Countries.ALL),
     VIMEO("VIMEO", "vimeo.com", Countries.ALL),
     ITV("ITV", "itv.com", Countries.GB), 
     BLIP("blip.tv", "blip.tv", Countries.ALL), 
     DAILYMOTION("Dailymotion", "dailymotion.com", Countries.ALL), 
     FLICKR("Flickr", "flickr.com", Countries.ALL), 
     FIVE("Five", "five.tv", Countries.GB),
 	SEESAW("SeeSaw", "seesaw.com", Countries.GB),
     TVBLOB("TV Blob", "tvblob.com", Countries.IT),
     ICTOMORROW("ICTomorrow", "ictomorrow.co.uk", Countries.GB),
     HBO("HBO", "hbo.com", Countries.US),
     ITUNES("iTunes", "itunes.com", Countries.ALL),
     MSN_VIDEO("MSN Video", "video.uk.msn.com", Countries.GB),
     PA("PA", "pressassociation.com", Countries.GB),
     RADIO_TIMES("Radio Times", "radiotimes.com", Countries.GB),
     PREVIEW_NETWORKS("Preview Networks", "previewnetworks.com", Countries.GB),
     ARCHIVE_ORG("Archive.org", "archive.org", Countries.ALL),
 	WORLD_SERVICE("BBC World Service Archive", "wsarchive.bbc.co.uk", Countries.ALL),
	BBC_REDUX("BBC Redux", "bbcredux.com", Countries.GB);
     METABROADCAST("MetaBroadcast", "metabroadcast.com", Countries.ALL);
 	
     private static final Splitter CSV_SPLITTER = Splitter.on(',').trimResults();
     public static final int MAX_KEY_LENGTH = 20;
     
 	private final String key;
     private final Country country;
     private final String title;
 
     Publisher(String title, String key, Country country) {
         this.title = title;
         Preconditions.checkArgument(key.length() <= MAX_KEY_LENGTH);
         this.key = key;
         this.country = country;
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
         for (Publisher publisher: Publisher.values()) {
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
     
     public static Function<Publisher,String> TO_KEY = new Function<Publisher, String>() {
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
 }
