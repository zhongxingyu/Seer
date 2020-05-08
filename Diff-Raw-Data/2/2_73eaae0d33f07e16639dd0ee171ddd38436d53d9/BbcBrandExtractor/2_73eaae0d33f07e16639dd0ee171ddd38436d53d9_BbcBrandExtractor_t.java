 package org.atlasapi.remotesite.bbc;
 
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.atlasapi.media.entity.Brand;
 import org.atlasapi.media.entity.Content;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.Playlist;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.media.entity.Series;
 import org.atlasapi.persistence.logging.AdapterLog;
 import org.atlasapi.persistence.logging.AdapterLogEntry;
 import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
 import org.atlasapi.remotesite.bbc.SlashProgrammesRdf.SlashProgrammesBase;
 import org.atlasapi.remotesite.bbc.SlashProgrammesRdf.SlashProgrammesContainerRef;
 import org.atlasapi.remotesite.bbc.SlashProgrammesRdf.SlashProgrammesSeriesContainer;
 import org.atlasapi.remotesite.bbc.SlashProgrammesRdf.SlashProgrammesSeriesRef;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Lists;
 
 public class BbcBrandExtractor  {
 
 	// Some Brands have a lot of episodes, if there are more than this number we
 	// only look at the most recent episodes
 	private static final int MAX_EPISODES = 1000;
 
 	private static final BbcProgrammesGenreMap genreMap = new BbcProgrammesGenreMap();
 	private static final Pattern IMAGE_STEM = Pattern.compile("^(.+)_[0-9]+_[0-9]+\\.[a-zA-Z]+$");
 
 	private final BbcProgrammeAdapter subContentExtractor;
 	private final AdapterLog log;
 
 	public BbcBrandExtractor(BbcProgrammeAdapter subContentExtractor, AdapterLog log) {
 		this.subContentExtractor = subContentExtractor;
 		this.log = log;
 	}
 	
 	public Series extractSeriesFrom(SlashProgrammesSeriesContainer rdfSeries) {
 		Series series = new Series();
 		populatePlaylistAttributes(series, rdfSeries);
 		List<String> episodeUris = episodesFrom(rdfSeries.episodeResourceUris());
 		addDirectlyIncludedEpisodesTo(series, episodeUris);
     	return series;
 	}
 
 	public Brand extractBrandFrom(SlashProgrammesContainerRef brandRef) {
 		Brand brand = new Brand();
 		populatePlaylistAttributes(brand, brandRef);
 
 		List<String> episodes = brandRef.episodes == null ? ImmutableList.<String>of() : episodesFrom(brandRef.episodeResourceUris());
 		addDirectlyIncludedEpisodesTo(brand, episodes);
 		
 		if (brandRef.series != null) {
 			for (SlashProgrammesSeriesRef seriesRef : brandRef.series) {
 				String seriesPid = BbcFeeds.pidFrom(seriesRef.resourceUri());
 				if (seriesPid == null) {
 					log.record(new AdapterLogEntry(Severity.WARN).withSource(getClass()).withUri(seriesRef.resourceUri()).withDescription("Could not extract PID from series ref " + seriesRef.resourceUri() + " for brand with uri " + brand.getCanonicalUri()));
 					continue;
 				}
 				String uri = "http://www.bbc.co.uk/programmes/" + seriesPid;
 				Series series = (Series) subContentExtractor.fetch(uri);
 				if (series == null || series.getContentType() == null || brand == null || brand.getContentType() == null) {
 					log.record(new AdapterLogEntry(Severity.WARN).withSource(getClass()).withUri(uri).withDescription("Could not load series with uri " + uri + " for brand with uri " + brand.getCanonicalUri()));
 					continue;
 				}
 				if (!series.getContentType().equals(brand.getContentType())) {
 					series.setContentType(brand.getContentType());
 				}
 				for (Item item : series.getItems()) {
					if(brand.getContentType() != null && !brand.getContentType().equals(item.getContentType())) {
 						item.setContentType(brand.getContentType());
 					}
 					brand.addItem(item);
 				}
 			}
 		}
 		return brand;
 	}
 
 	private void addDirectlyIncludedEpisodesTo(Playlist playlist, List<String> episodes) {
 		for (String episodeUri : mostRecent(episodes)) {
 			Content found = subContentExtractor.fetch(episodeUri);
 			if (!(found instanceof Item)) {
 				log.record(new AdapterLogEntry(Severity.WARN).withUri(episodeUri).withSource(getClass()).withDescription("Expected Item for PID: " + episodeUri));
 				continue;
 			} 
 			playlist.addItem((Item) found); 
 		}
 	}
 
 	private List<String> episodesFrom(List<String> uriFragments) {
 		List<String> uris = Lists.newArrayListWithCapacity(uriFragments.size());
 		for (String uri : uriFragments) {
 			String pid = BbcFeeds.pidFrom(uri);
 			if (pid == null) {
 				log.record(new AdapterLogEntry(Severity.WARN).withUri(uri).withSource(getClass()).withDescription("Could not extract PID from: " + uri));
 				continue;
 			}
 			uris.add("http://www.bbc.co.uk/programmes/" + pid);
 		}
 		return uris;
 	}
 
 	private <T> List<T> mostRecent(List<T> episodes) {
 		if (episodes.size() < MAX_EPISODES) {
 			return episodes;
 		}
 		return episodes.subList(episodes.size() - MAX_EPISODES, episodes.size());
 	}
 
 	private void populatePlaylistAttributes(Playlist container, SlashProgrammesBase brandRef) {
 		String brandUri = brandRef.uri();
 		container.setCanonicalUri(brandUri);
 		container.setCurie(BbcUriCanonicaliser.curieFor(brandUri));
 		container.setPublisher(Publisher.BBC);
 		container.setTitle(brandRef.title());
 		if (brandRef.getMasterbrand() != null) {
 			container.setContentType(BbcMasterbrandContentTypeMap.lookup(brandRef.getMasterbrand().getResourceUri()).valueOrNull());
 		}
 		if (brandRef.getDepiction() != null) {
 			Matcher matcher = IMAGE_STEM.matcher(brandRef.getDepiction().resourceUri());
 			if (matcher.matches()) {
 				String base = matcher.group(1);
 				container.setImage(base + BbcProgrammeGraphExtractor.FULL_IMAGE_EXTENSION);
 				container.setThumbnail(base + BbcProgrammeGraphExtractor.THUMBNAIL_EXTENSION);
 			}
 		}
 		container.setGenres(genreMap.map(brandRef.genreUris()));
 		container.setDescription(brandRef.description());
 	}
 }
