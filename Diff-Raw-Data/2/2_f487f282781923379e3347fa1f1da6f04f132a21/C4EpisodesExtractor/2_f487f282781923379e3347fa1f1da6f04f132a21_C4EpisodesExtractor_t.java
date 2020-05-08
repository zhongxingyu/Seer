 /* Copyright 2009 Meta Broadcast Ltd
 
 Licensed under the Apache License, Version 2.0 (the "License"); you
 may not use this file except in compliance with the License. You may
 obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 implied. See the License for the specific language governing
 permissions and limitations under the License. */
 
 package org.atlasapi.remotesite.channel4;
 
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.atlasapi.media.TransportSubType;
 import org.atlasapi.media.TransportType;
 import org.atlasapi.media.entity.Clip;
 import org.atlasapi.media.entity.Countries;
 import org.atlasapi.media.entity.Country;
 import org.atlasapi.media.entity.Encoding;
 import org.atlasapi.media.entity.Episode;
 import org.atlasapi.media.entity.Item;
 import org.atlasapi.media.entity.Location;
 import org.atlasapi.media.entity.MediaType;
 import org.atlasapi.media.entity.Policy;
 import org.atlasapi.media.entity.Publisher;
 import org.atlasapi.media.entity.Restriction;
 import org.atlasapi.media.entity.Specialization;
 import org.atlasapi.media.entity.Version;
 import org.atlasapi.persistence.logging.AdapterLog;
 import org.atlasapi.persistence.logging.AdapterLogEntry;
 import org.atlasapi.persistence.logging.AdapterLogEntry.Severity;
 import org.atlasapi.query.content.PerPublisherCurieExpander;
 import org.atlasapi.remotesite.ContentExtractor;
 import org.jdom.Attribute;
 import org.jdom.Element;
 import org.joda.time.DateTime;
 import org.joda.time.Duration;
 
 import com.google.common.base.Strings;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.metabroadcast.common.time.DateTimeZones;
 import com.sun.syndication.feed.atom.Entry;
 import com.sun.syndication.feed.atom.Feed;
 import com.sun.syndication.feed.atom.Link;
 
 public class C4EpisodesExtractor implements ContentExtractor<Feed, List<Episode>> {
     
 	public static final String DC_AGE_RATING = "dc:relation.AgeRating";
 	public static final String DC_GUIDANCE = "dc:relation.Guidance";
 	public static final String DC_TERMS_AVAILABLE = "dcterms:available";
 	public static final String DC_TX_DATE = "dc:date.TXDate";
 	public static final String DC_START_TIME = "dc:relation.TXStartTime";
 	public static final String DC_TX_CHANNEL = "dc:relation.TXChannel";
 	public static final String EPISODE_TITLE_TEMPLATE = "Series %s Episode %s";
 	public static final String DC_EPISODE_NUMBER = "dc:relation.EpisodeNumber";
 	public static final String DC_SERIES_NUMBER = "dc:relation.SeriesNumber";
 	private static final Pattern CLIP_ID_PATTERN = Pattern.compile("tag:www\\.channel4\\.com,\\d+:clip\\/(.+)");
	private static final String EMBED_CODE = "<object id='flashObj' width='480' height='290' classid='clsid:D27CDB6E-AE6D-11cf-96B8-444553540000' codebase='http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab#version=9,0,47,0'><param name='movie' value='http://c.brightcove.com/services/viewer/federated_f9/86700592001?isVid=1&amp;isUI=1&amp;publisherID=1213940598' /><param name='bgcolor' value='#000000' /><param name='flashVars' value='videoId=%VIDEOID%&amp;playerID=86700592001&amp;domain=embed&amp;' /><param name='base' value='http://admin.brightcove.com' /><param name='seamlesstabbing' value='false' /><param name='allowFullScreen' value='true' /><param name='swLiveConnect' value='true' /><param name='allowScriptAccess' value='always' /><embed src='http://c.brightcove.com/services/viewer/federated_f9/86700592001?isVid=1&amp;isUI=1&amp;publisherID=1213940598' bgcolor='#000000' flashVars='videoId=%VIDEOID%&amp;playerID=86700592001&amp;domain=embed&amp;' base='http://admin.brightcove.com' name='flashObj' width='480' height='290' seamlesstabbing='false' type='application/x-shockwave-flash' allowFullScreen='true' swLiveConnect='true' allowScriptAccess='always' pluginspage='http://www.macromedia.com/shockwave/download/index.cgi?P1_Prod_Version=ShockwaveFlash'></embed></object>";
 
 	private static final Pattern AVAILABILTY_RANGE_PATTERN = Pattern.compile("start=(.*); end=(.*); scheme=W3C-DTF");
 
 	public static Map<String, String> CHANNEL_LOOKUP = channelLookup();
 	private final AdapterLog log;
 	
 	public C4EpisodesExtractor(AdapterLog log) {
         this.log = log;
 	}
 	
 	private static Map<String, String> channelLookup() {
 		Map<String, String> channelLookup = Maps.newHashMap();
 		channelLookup.put("C4", "http://www.channel4.com");
 		channelLookup.put("M4", "http://www.channel4.com/more4");
 		channelLookup.put("E4", "http://www.e4.com");
 		return channelLookup;
 	}
 	
 	private boolean include4odInfo = false;
 	
 	public C4EpisodesExtractor includeOnDemands() {
 		this.include4odInfo = true;
 		return this;
 	}
 	
 	@Override
 	@SuppressWarnings("unchecked")
 	public List<Episode> extract(Feed source) {
 		return (List) extractItems(source, true);
 	}
 	
 	@SuppressWarnings("unchecked")
 	public List<Clip> extractClips(Feed source) {
 		return (List) extractItems(source, false);
 	}
 
 	@SuppressWarnings("unchecked")
 	private List<Item> extractItems(Feed source, boolean isEpisode) {
 		List<Item> items = Lists.newArrayList();
 		
 		for (Entry entry : (List<Entry>) source.getEntries()) {
 			Map<String, String> lookup = C4AtomApi.foreignElementLookup(entry);
 
 			
 			Item episode = isEpisode ? createEpisode(source, entry, lookup) : createClip(source, entry, lookup);
 			
 			if (episode == null) {
 				continue;
 			}
 			
 			episode.setLastUpdated(new DateTime(source.getUpdated(), DateTimeZones.UTC));
 			
 			
 			episode.setDescription(description(entry));
 			
 			Element mediaGroup = include4odInfo ? C4AtomApi.mediaGroup(entry) : C4AtomApi.mediaContent(entry);
 			
 			if (mediaGroup != null) {
 				Element thumbnail = mediaGroup.getChild("thumbnail", C4AtomApi.NS_MEDIA_RSS);
 				if (thumbnail != null) {
 					Attribute thumbnailUri = thumbnail.getAttribute("url");
 					C4AtomApi.addImages(episode, thumbnailUri.getValue());
 				}
 			}
 			
 			Version version = versionFor(entry, episode, mediaGroup, lookup);
 		
 			if (version != null) {
 				episode.addVersion(version);
 			}
 			items.add(episode);
 		}
 		return items;
 	}
 	
 	
 	private Clip createClip(Feed source, Entry entry, Map<String, String> lookup) {
 
 		String clipUri = C4AtomApi.clipUri(entry);
 		
 		if (clipUri == null) {
 		    log.record(new AdapterLogEntry(Severity.WARN).withDescription("Unable to derive URI for C4 clip with id: " + entry.getId()).withSource(this.getClass()));
 		    return null;
 		}
 		
 		Clip clip = new Clip(clipUri, PerPublisherCurieExpander.CurieAlgorithm.C4.compact(clipUri), Publisher.C4);
 
 		String fourOdUri = C4AtomApi.fourOdUri(entry);
 		if (fourOdUri != null) {
 			clip.addAlias(fourOdUri);
 		}
 
 		clip.setTitle(title(entry));
 		clip.setMediaType(MediaType.VIDEO);
 		clip.setIsLongForm(false);
 		return clip;
 	}
 
 	private Item createEpisode(Feed source, Entry entry, Map<String, String> lookup) {
 		
 		String feedTitle = Strings.nullToEmpty(source.getTitle());
 
 		Integer seriesNumber = C4AtomApi.readAsNumber(lookup, DC_SERIES_NUMBER);
 		Integer episodeNumber = C4AtomApi.readAsNumber(lookup, DC_EPISODE_NUMBER);
 		
 		String itemUri = C4AtomApi.canonicalUri(entry);
 		
 		if (itemUri == null) {
 			// fall back to hacking the uri out of the feed
 			itemUri = extarctUriFromLink(source, entry);
 		}
 		if (itemUri == null) {
 		    log.record(new AdapterLogEntry(Severity.WARN).withDescription("Unable to derive URI for c4 episode with id: " + entry.getId()).withSource(this.getClass()));
 		    return null;
 		}
 		
 		Episode episode = new Episode(itemUri, PerPublisherCurieExpander.CurieAlgorithm.C4.compact(itemUri), Publisher.C4);
 
 		String fourOdUri = C4AtomApi.fourOdUri(entry);
 		if (fourOdUri != null) {
 			episode.addAlias(fourOdUri);
 		}
 		
 		
 		if (C4AtomApi.isAnEpisodeId(entry.getId())) {
 			episode.addAlias(entry.getId());
 		}
 
 		episode.setTitle(title(entry));
 		
 		if ((Strings.isNullOrEmpty(episode.getTitle()) || feedTitle.startsWith(episode.getTitle())) && episodeNumber != null && seriesNumber != null) {
 			episode.setTitle(String.format(EPISODE_TITLE_TEMPLATE , seriesNumber, episodeNumber));
 		}
 		
 		episode.setEpisodeNumber(episodeNumber);
 		episode.setSeriesNumber(seriesNumber);
 		episode.setIsLongForm(true);
 		episode.setMediaType(MediaType.VIDEO);
 		episode.setSpecialization(Specialization.TV);
 		return episode;
 	}
 
 	public Version versionFor(Entry entry, Item episode, Element mediaGroup, Map<String, String> lookup) {
 		Set<Country> availableCountries = null;
 		if (mediaGroup != null) {
 			Element restriction = mediaGroup.getChild("restriction", C4AtomApi.NS_MEDIA_RSS);
 			if (restriction != null && restriction.getValue() != null) {
 				availableCountries = Countries.fromDelimtedList(restriction.getValue());
 			}
 		}
 		
 		
 		String uri = C4AtomApi.fourOdUri(entry);
 		if (uri == null) {
 			uri = C4AtomApi.clipUri(entry);
 		}
 		return version(uri, entry.getId(), lookup, availableCountries, new DateTime(entry.getUpdated(), DateTimeZones.UTC));
 	}
 
 	@SuppressWarnings("unchecked")
 	private String extarctUriFromLink(Feed source, Entry entry) {
 		for (Link link : (List<Link>) entry.getOtherLinks()) {
 			Matcher matcher = C4AtomApi.SERIES_AND_EPISODE_NUMBER_IN_ANY_URI.matcher(link.getHref());
 			if (matcher.find()) {
 				return C4AtomApi.episodeUri(C4AtomApi.webSafeNameFromAnyFeedId(source.getId()), Integer.valueOf(matcher.group(1)), Integer.valueOf(matcher.group(2)));	
 			}
 		}
 		return null;
 	}
 	
 	private String description(Entry entry) {
 		com.sun.syndication.feed.atom.Content description = entry.getSummary();
 		if (description == null) {
 			return null;
 		}
 		return description.getValue();
 	}
 	
 	private String title(Entry entry) {
         com.sun.syndication.feed.atom.Content title = entry.getTitleEx();
         if (title == null) {
             return null;
         }
         return title.getValue();
     }
 	
 	private Location location(String uri, String locationId, Map<String, String> lookup, Set<Country> availableCountries, DateTime lastUpdated) {
 		Location location = new Location();
 		location.setUri(uri);
 		location.addAlias(locationId);
 		location.setTransportType(TransportType.LINK);
 		location.setLastUpdated(lastUpdated);
 		
 		// The feed only contains available content
 		location.setAvailable(true);
 		
 		String availability = lookup.get(DC_TERMS_AVAILABLE);
 		
 		if (availability != null) {
 			Matcher matcher = AVAILABILTY_RANGE_PATTERN.matcher(availability);
 			if (!matcher.matches()) {
 				throw new IllegalStateException("Availability range format not recognised, was " + availability);
 			}
 			String txDate = lookup.get(DC_TX_DATE);
             Policy policy = new Policy()
 				.withAvailabilityStart(new DateTime(Strings.isNullOrEmpty(txDate) ? matcher.group(1) : txDate))
 				.withAvailabilityEnd(new DateTime(matcher.group(2)));
 				
 			if (availableCountries != null) {
 				policy.setAvailableCountries(availableCountries);
 			}
 			location.setPolicy(policy);
 		}
 		return location;
 	}
 	
 	private Location embedLocation(String embedId, Location linkLocation) {
         Location location = new Location();
         location.setTransportType(TransportType.EMBED);
         location.setTransportSubType(TransportSubType.BRIGHTCOVE);
         location.setLastUpdated(linkLocation.getLastUpdated());
         location.setEmbedId(embedId);
         location.setEmbedCode(EMBED_CODE.replaceAll("%VIDEOID%", embedId));
         
         // The feed only contains available content
         location.setAvailable(linkLocation.getAvailable());
         if (linkLocation.getPolicy() != null) {
             location.setPolicy(linkLocation.getPolicy().copy());
         }
 
         return location;
     }
 
 
 	private Version version(String uri, String locationId, Map<String, String> lookup, Set<Country> availableCountries, DateTime lastUpdated) {
 		Version version = new Version();
 		Duration duration = C4AtomApi.durationFrom(lookup);
 		
 		if (duration != null) {
 			version.setDuration(duration);
 		}
 		
 		Integer ageRating = lookup.get(DC_AGE_RATING) != null ? Integer.parseInt(lookup.get(DC_AGE_RATING)) : null;
 		String guidance = lookup.get(DC_GUIDANCE);
 
 		if (ageRating != null && ageRating > 0 && guidance != null) {
 			version.setRestriction(Restriction.from(ageRating, guidance));
 		} else {
 			version.setRestriction(Restriction.from(guidance));
 		}
 		
 		if (include4odInfo) {
 			Encoding encoding = new Encoding();
 			Location location = location(uri, locationId, lookup, availableCountries, lastUpdated);
             encoding.addAvailableAt(location);
 			
 			Matcher matcher = CLIP_ID_PATTERN.matcher(locationId);
 			if (matcher.matches()) {
 			    Location embedLocation = embedLocation(matcher.group(1), location);
 			    encoding.addAvailableAt(embedLocation);
 			}
 			
 			version.addManifestedAs(encoding);
 		}
 				
 		return version.getBroadcasts().isEmpty() && version.getManifestedAs().isEmpty() ? null : version;
 	}
 	
 }
