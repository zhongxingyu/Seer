 package org.atlasapi.media.entity;
 
 import static com.google.common.base.Preconditions.checkArgument;
 
 import org.atlasapi.application.v3.SourceRestriction;
 import org.atlasapi.application.v3.SourceStatus;
 
 import com.google.common.base.Function;
 import com.google.common.base.Splitter;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Iterables;
 import com.metabroadcast.common.base.Maybe;
 import com.metabroadcast.common.intl.Countries;
 import com.metabroadcast.common.intl.Country;
 
 public enum Publisher {
     BBC("BBC", "bbc.co.uk", Countries.GB, SourceStatus.AVAILABLE_DISABLED, SourceRestriction.NONE, true),
     C4("Channel 4", "channel4.com", Countries.GB, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     HULU("Hulu", "hulu.com", Countries.US, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     YOUTUBE("YouTube", "youtube.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.NONE, false),
     TED("TED", "ted.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     VIMEO("VIMEO", "vimeo.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     ITV("ITV", "itv.com", Countries.GB, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false), 
     BLIP("blip.tv", "blip.tv", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false), 
     DAILYMOTION("Dailymotion", "dailymotion.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false), 
     FLICKR("Flickr", "flickr.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false), 
     FIVE("Five", "five.tv", Countries.GB, SourceStatus.UNAVAILABLE, SourceRestriction.NONE, false),
     SEESAW("SeeSaw", "seesaw.com", Countries.GB, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     TVBLOB("TV Blob", "tvblob.com", Countries.IT, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     ICTOMORROW("ICTomorrow", "ictomorrow.co.uk", Countries.GB, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     HBO("HBO", "hbo.com", Countries.US, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     ITUNES("iTunes", "itunes.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
    MSN_VIDEO("MSN Video", "video.uk.msn.com", Countries.GB, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     PA("PA", "pressassociation.com", Countries.GB, SourceStatus.AVAILABLE_DISABLED, SourceRestriction.NONE, false),
     PA_SERIES_SUMMARIES("PA Series Summaries", "summaries.pressassociation.com", Countries.GB, SourceStatus.UNAVAILABLE, SourceRestriction.NONE, false),
     RADIO_TIMES("Radio Times", "radiotimes.com", Countries.GB, SourceStatus.UNAVAILABLE, SourceRestriction.NONE, false),
     PREVIEW_NETWORKS("Preview Networks", "previewnetworks.com", Countries.GB, SourceStatus.UNAVAILABLE, SourceRestriction.NONE, false),
     ARCHIVE_ORG("Archive.org", "archive.org", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.NONE, false),
     WORLD_SERVICE("BBC World Service Archive", "wsarchive.bbc.co.uk", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     BBC_REDUX("BBC Redux", "bbcredux.com", Countries.GB, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     METABROADCAST("MetaBroadcast", "metabroadcast.com", Countries.ALL, SourceStatus.AVAILABLE_DISABLED, SourceRestriction.NONE, true),
     DBPEDIA("DBpedia", "dbpedia.org", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     LOVEFILM("LoveFilm", "lovefilm.com", Countries.GB, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     BBC_PRODUCTS("BBC Commercial Availability", "products.bbc.co.uk", Countries.GB, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     MUSIC_BRAINZ("MusicBrainz", "musicbrainz.org", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.NONE, false),
     EMI_PUB("EMI Music Publishing", "emimusicpub.com", Countries.GB, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     EMI_MUSIC("EMI Music", "emimusic.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     BBC_KIWI("BBC Kiwi", "kiwi.bbc.co.uk", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     CANARY("Canary", "canary.metabroadcast.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     THESPACE("The Space", "thespace.org", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     VOILA("Voila", "voila.metabroadcast.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     MAGPIE("Magpie", "magpie.metabroadcast.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     LONDON_ALSO("London ALSO", "london.metabroadcast.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     BBC_MUSIC("BBC Music","music.bbc.co.uk", Countries.GB, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     SPOTIFY("Spotify", "spotify.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     SOUNDCLOUD("Soundcloud", "soundcloud.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     RDIO("rdio", "rdio.com",Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     AMAZON_UK("Amazon UK", "amazon.co.uk", Countries.GB, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     BBC_RD_TOPIC("BBC R&D Live Topics", "live-topics.prototype0.net", Countries.GB, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     PA_FEATURES("PA Features", "features.pressassociation.com", Countries.GB, SourceStatus.UNAVAILABLE, SourceRestriction.NONE, false),
     PA_PEOPLE("PA People Profiles", "people.pressassociation.com", Countries.GB, SourceStatus.UNAVAILABLE, SourceRestriction.NONE, false),
     BT("BT", "bt.com", Countries.GB, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     BT_FEATURED_CONTENT("BT Featured Content", "featured.bt.com", Countries.GB, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     NETFLIX("Netflix", "gb.netflix.com", Countries.GB, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     YOUVIEW("YouView", "youview.com", Countries.GB, SourceStatus.UNAVAILABLE, SourceRestriction.NONE, false),
     FACEBOOK("Facebook Graph API", "graph.facebook.com", Countries.ALL, SourceStatus.AVAILABLE_DISABLED, SourceRestriction.NONE, false),
     SCRAPERWIKI("ScraperWiki", "scraperwiki.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     SVERIGES_RADIO("Sveriges Radio", "sverigesradio.se", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     TALK_TALK("Talk Talk", "talktalk.net", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.NONE, false),
     KANDL_TOPICS("kandl topics", "kandl.metabroadcast.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     THE_SUN("The Sun", "thesun.co.uk", Countries.GB, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     ADAPT_BBC_PODCASTS("Adapt BBC Podcasts", "bbc-podcasts.adapt.fm", Countries.GB, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     YURI("yuri", "yuri.metabroadcast.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     COYOTE("coyote", "coyote.metabroadcast.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     BBC_NITRO("BBC Nitro", "nitro.bbc.co.uk", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     AMAZON_UNBOX("Amazon Unbox GB (Lovefilm)", "unbox.amazon.co.uk", Countries.GB, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     METABROADCAST_PICKS("MetaBroadcast Picks", "picks.metabroadcast.com", Countries.GB, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     C4_PMLSD("Channel 4 PMLSD", "pmlsd.channel4.com", Countries.GB, SourceStatus.UNAVAILABLE, SourceRestriction.NONE, false),
     C4_PMLSD_P06("Channel 4 PMLSD", "p06.pmlsd.channel4.com", Countries.GB, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     WIKIPEDIA("Wikipedia", "wikipedia.org", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.NONE, false),
     C5_TV_CLIPS("Channel 5 TV Clips", "c5tvclips.metabroadcast.com", Countries.GB, SourceStatus.UNAVAILABLE, SourceRestriction.NONE, false),
     METABROADCAST_SIMILAR_CONTENT("MetaBroadcast Similar Content", "similarcontent.metabroadcast.com", Countries.GB, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     ROVI_AR ("ROVI Arabic", "ar.rovicorp.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     ROVI_AZ ("ROVI Azerbaijani", "az.rovicorp.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     ROVI_BG ("ROVI Bulgarian", "bg.rovicorp.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     ROVI_BM ("ROVI Bambara", "bm.rovicorp.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     ROVI_BN ("ROVI Bengali", "bn.rovicorp.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     ROVI_BS ("ROVI Bosnian", "bs.rovicorp.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     ROVI_CA ("ROVI Catalan", "ca.rovicorp.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     ROVI_CS ("ROVI Czech", "cs.rovicorp.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     ROVI_CY ("ROVI Welsh", "cy.rovicorp.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     ROVI_DA ("ROVI Danish", "da.rovicorp.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     ROVI_DE ("ROVI German", "de.rovicorp.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     ROVI_EL ("ROVI Greek", "el.rovicorp.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     ROVI_EN_US ("ROVI English", "en_us.rovicorp.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     ROVI_EN_GB ("ROVI English", "en_gb.rovicorp.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     ROVI_ES ("ROVI Spanish", "es.rovicorp.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     ROVI_ET ("ROVI Estonian", "et.rovicorp.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     ROVI_EU ("ROVI Basque", "eu.rovicorp.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     ROVI_FA ("ROVI Persian", "fa.rovicorp.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     ROVI_FI ("ROVI Finnish", "fi.rovicorp.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     ROVI_FR_FR ("ROVI French", "fr_fr.rovicorp.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     ROVI_FR_CA ("ROVI French", "fr_ca.rovicorp.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     ROVI_GA ("ROVI Irish", "ga.rovicorp.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     ROVI_GD ("ROVI Scottish", "gd.rovicorp.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     ROVI_GL ("ROVI Galician", "gl.rovicorp.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     ROVI_HE ("ROVI Hebrew", "he.rovicorp.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     ROVI_HI ("ROVI Hindi", "hi.rovicorp.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     ROVI_HR ("ROVI Croatian", "hr.rovicorp.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     ROVI_HU ("ROVI Hungarian", "hu.rovicorp.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     ROVI_IS ("ROVI Icelandic", "is.rovicorp.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     ROVI_IT ("ROVI Italian", "it.rovicorp.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     ROVI_IU ("ROVI Inuktitut", "iu.rovicorp.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     ROVI_JA ("ROVI Japanese", "ja.rovicorp.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     ROVI_KA ("ROVI Georgian", "ka.rovicorp.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     ROVI_KO ("ROVI Korean", "ko.rovicorp.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     ROVI_KU ("ROVI Kurdish", "ku.rovicorp.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     ROVI_LB ("ROVI Luxembourgish", "lb.rovicorp.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     ROVI_LT ("ROVI Lithuanian", "lt.rovicorp.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     ROVI_LV ("ROVI Latvian", "lv.rovicorp.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     ROVI_MN ("ROVI Mongolian", "mn.rovicorp.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     ROVI_NL ("ROVI Dutch", "nl.rovicorp.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     ROVI_NO ("ROVI Norwegian", "no.rovicorp.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     ROVI_PA ("ROVI Panjabi", "pa.rovicorp.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     ROVI_PL ("ROVI Polish", "pl.rovicorp.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     ROVI_PT ("ROVI Portuguese", "pt.rovicorp.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     ROVI_RO ("ROVI Romanian", "ro.rovicorp.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     ROVI_RU ("ROVI Russian", "ru.rovicorp.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     ROVI_SE ("ROVI Northern Sami", "se.rovicorp.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     ROVI_SK ("ROVI Slovak", "sk.rovicorp.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     ROVI_SL ("ROVI Slovene", "sl.rovicorp.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     ROVI_SQ ("ROVI Albanian", "sq.rovicorp.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     ROVI_SR ("ROVI Serbian", "sr.rovicorp.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     ROVI_SV ("ROVI Swedish", "sv.rovicorp.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     ROVI_TA ("ROVI Tamil", "ta.rovicorp.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     ROVI_TH ("ROVI Thai", "th.rovicorp.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     ROVI_TL ("ROVI Tagalog", "tl.rovicorp.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     ROVI_TR ("ROVI Turkish", "tr.rovicorp.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     ROVI_UK ("ROVI Ukrainian", "uk.rovicorp.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     ROVI_VI ("ROVI Vietnamese", "vi.rovicorp.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     ROVI_ZH ("ROVI Chinese", "zh.rovicorp.com", Countries.ALL, SourceStatus.UNAVAILABLE, SourceRestriction.ADMIN_ONLY, false),
     ;
 
     private static final Splitter CSV_SPLITTER = Splitter.on(',').trimResults();
 
     private final String key;
     private final Country country;
     private final String title;
     private final SourceStatus defaultStatus;
     private final SourceRestriction restriction;
     private final boolean enabledWithNoApiKey;
 
     Publisher(String title, String key, Country country, SourceStatus defaultStatus, SourceRestriction restriction, boolean enabledWithNoApiKey) {
         this.title = title;
         this.key = key;
         this.country = country;
         this.defaultStatus = defaultStatus;
         this.restriction = restriction;
         this.enabledWithNoApiKey = enabledWithNoApiKey;
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
     
     public SourceRestriction restriction() {
         return restriction;
     }
     
     public boolean enabledWithNoApiKey() {
         return enabledWithNoApiKey;
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
