 package org.atlasapi.remotesite.channel4;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.is;
 import static org.hamcrest.Matchers.startsWith;
 
 import java.util.Collections;
 import java.util.List;
 import java.util.Set;
 
 import org.atlasapi.media.TransportType;
 import org.atlasapi.media.entity.Broadcast;
 import org.atlasapi.media.entity.Countries;
 import org.atlasapi.media.entity.Country;
 import org.atlasapi.media.entity.Encoding;
 import org.atlasapi.media.entity.Episode;
 import org.atlasapi.media.entity.Location;
 import org.atlasapi.media.entity.Policy;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.media.entity.Version;
 import org.atlasapi.persistence.logging.NullAdapterLog;
 import org.jmock.integration.junit3.MockObjectTestCase;
 import org.joda.time.DateTime;
 import org.joda.time.LocalDate;
 
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Sets;
 import com.google.common.io.Resources;
 import com.metabroadcast.common.time.DateTimeZones;
 
 public class C4FourOdEpisodesExtractorTest extends MockObjectTestCase {
 
 	private final AtomFeedBuilder fourOdFeed = new AtomFeedBuilder(Resources.getResource(getClass(), "ramsays-kitchen-nightmares-4od.atom"));
 	
 	public void testExtractingEpisodes() throws Exception {
 		
 		List<Episode> episodes = new C4EpisodesExtractor(new NullAdapterLog()).includeBroadcasts().includeOnDemands().extract(fourOdFeed.build());
 
 		Episode firstEpisode = (Episode) Iterables.get(episodes, 0);
 		
 		assertThat(firstEpisode.getCanonicalUri(), is("http://www.channel4.com/programmes/ramsays-kitchen-nightmares/episode-guide/series-1/episode-1"));
		assertThat(firstEpisode.getAliases(), is((Set<String>) ImmutableSet.of("http://www.channel4.com/programmes/ramsays-kitchen-nightmares/4od#2922045", "tag:www.channel4.com,2009:/programmes/ramsays-kitchen-nightmares/4od%232922045")));
 		
 		assertThat(firstEpisode.getCurie(), is("c4:ramsays-kitchen-nightmares-series-1-episode-1"));
 		assertThat(firstEpisode.getTitle(), is("Series 1 Episode 1"));
 		assertThat(firstEpisode.getPublisher(), is(Publisher.C4));
 		assertThat(firstEpisode.getSeriesNumber(), is(1));
 		assertThat(firstEpisode.getEpisodeNumber(), is(1));
 		assertThat(firstEpisode.getDescription(), startsWith("Gordon Ramsay visits Bonapartes in Silsden, West Yorkshire."));
 		assertThat(firstEpisode.getThumbnail(), is("http://www.channel4.com/assets/programmes/images/ramsays-kitchen-nightmares/series-1/ramsays-kitchen-nightmares-s1-20090617160732_200x113.jpg"));
 		assertThat(firstEpisode.getImage(), is("http://www.channel4.com/assets/programmes/images/ramsays-kitchen-nightmares/series-1/ramsays-kitchen-nightmares-s1-20090617160732_625x352.jpg"));
 		assertThat(firstEpisode.getLastUpdated(), is(new DateTime("2010-04-27T09:49:40.803Z", DateTimeZones.UTC)));
 		
 		Version firstEpisodeVersion = Iterables.get(firstEpisode.getVersions(), 0);
 		assertThat(firstEpisodeVersion.getDuration(), is((48 * 60) + 55));
 		assertThat(firstEpisodeVersion.getRating(), is("http://ref.atlasapi.org/ratings/simple/adult"));
 		assertThat(firstEpisodeVersion.getRatingText(), is("Strong language throughout"));
 		assertThat(firstEpisodeVersion.getBroadcasts(), is(Collections.<Broadcast>emptySet()));
 		
 		Encoding firstEpsiodeEncoding = Iterables.get(firstEpisodeVersion.getManifestedAs(), 0); 
 		
 		Location firstEpsiodeLocation = Iterables.get(firstEpsiodeEncoding.getAvailableAt(), 0); 
 		
 		assertThat(firstEpsiodeLocation.getUri(), is("http://www.channel4.com/programmes/ramsays-kitchen-nightmares/4od#2922045"));
 		assertThat(firstEpsiodeLocation.getAliases(), is((Set<String>) ImmutableSet.of("tag:www.channel4.com,2009:/programmes/ramsays-kitchen-nightmares/4od%232922045")));
 
 		assertThat(firstEpsiodeLocation.getTransportType(), is(TransportType.LINK));
 		
 		Policy firstEpisodePolicy = firstEpsiodeLocation.getPolicy();
 		assertThat(firstEpisodePolicy.getAvailabilityStart(), is(new LocalDate(2009, 07, 01).toDateTimeAtStartOfDay()));
 		assertThat(firstEpisodePolicy.getAvailabilityEnd(), is(new LocalDate(2010, 12, 31).plusDays(1).toDateTimeAtStartOfDay()));
 		assertThat(firstEpisodePolicy.getAvailableCountries(), is((Set<Country>) Sets.newHashSet(Countries.GB, Countries.IE)));
 		
 		Episode episodeWithABroadcast = (Episode) Iterables.get(episodes, 4);
 		Version episodeWithABroadcastVersion = Iterables.get(episodeWithABroadcast.getVersions(), 0);
 		Broadcast episodeWithABroadcastBroadcast = Iterables.get(episodeWithABroadcastVersion.getBroadcasts(), 0);
 		assertThat(episodeWithABroadcastBroadcast.getTransmissionTime(), is(new DateTime("2009-06-10T23:05:00.000Z")));
 		assertThat(episodeWithABroadcastBroadcast.getBroadcastOn(), is("http://www.channel4.com/more4"));
 		
 
 	}
 }
