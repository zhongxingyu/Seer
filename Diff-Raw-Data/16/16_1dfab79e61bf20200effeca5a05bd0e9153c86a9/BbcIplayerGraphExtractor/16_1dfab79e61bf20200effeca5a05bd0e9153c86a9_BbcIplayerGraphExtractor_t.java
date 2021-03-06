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
 
 package org.uriplay.remotesite.bbc;
 
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.xml.bind.JAXBException;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.uriplay.beans.BeanGraphExtractor;
 import org.uriplay.media.entity.Brand;
 import org.uriplay.media.entity.Description;
 import org.uriplay.media.entity.Item;
 import org.uriplay.media.entity.Playlist;
 import org.uriplay.persistence.system.NullRequestTimer;
 import org.uriplay.persistence.system.RemoteSiteClient;
 import org.uriplay.remotesite.ContentExtractor;
 import org.uriplay.remotesite.RemoteSiteRefresher;
 import org.uriplay.remotesite.SiteSpecificAdapter;
 import org.uriplay.remotesite.bbc.SlashProgrammesRdf.SlashProgrammesEpisode;
 import org.uriplay.remotesite.bbc.SlashProgrammesRdf.SlashProgrammesVersion;
 import org.uriplay.remotesite.synd.SyndicationSource;
 
 import com.google.common.collect.Maps;
 import com.sun.syndication.feed.synd.SyndCategory;
 import com.sun.syndication.feed.synd.SyndEntry;
 import com.sun.syndication.feed.synd.SyndLink;
 
 /**
  * {@link BeanGraphExtractor} for BBC iPlayer content.
  *  
  * @author Robert Chatley (robert@metabroadcast.com)
  */
 public class BbcIplayerGraphExtractor implements ContentExtractor<SyndicationSource, Playlist> {
 
 	private static final Log log = LogFactory.getLog(RemoteSiteRefresher.class);
 	
 	private final RemoteSiteClient<SlashProgrammesRdf> episodeClient;
 	private final RemoteSiteClient<SlashProgrammesVersionRdf> versionClient;
 	
 	private final BbcProgrammeGraphExtractor programmeGraphExtractor;
 	
 	private final SiteSpecificAdapter<Description> brandFetcher;
 
 	public BbcIplayerGraphExtractor() throws JAXBException {
 		this(new BbcSlashProgrammesEpisodeRdfClient(), new BbcSlashProgrammesVersionRdfClient(), new BbcProgrammeAdapter());
 	}
 	
 	public BbcIplayerGraphExtractor(RemoteSiteClient<SlashProgrammesRdf> episodeClient, RemoteSiteClient<SlashProgrammesVersionRdf> versionClient, SiteSpecificAdapter<Description> brandFetcher) {
 		this.episodeClient = episodeClient;
 		this.versionClient = versionClient;
 		this.brandFetcher = brandFetcher;
 		this.programmeGraphExtractor = new BbcProgrammeGraphExtractor(new SeriesFetchingBbcSeriesNumberResolver());
 	}
 
 	@SuppressWarnings("unchecked")
 	public Playlist extract(SyndicationSource source) {
 		
 		Playlist playlist = new Playlist();
 		playlist.setCanonicalUri(source.getUri());
 		playlist.setPublisher(BbcProgrammeGraphExtractor.BBC_PUBLISHER);
 		
 		Map<String, Brand> brandLookup = Maps.newHashMap();
 		
 		for (SyndEntry entry : (List<SyndEntry>) source.getFeed().getEntries()) {
 			
 			// remove this clause to include radio content
 			if (isRadioProgramme(entry)) {
 				continue;
 			}
 			
 			String episodeUri = episodeUriFrom(selfLink(entry));
 			if (episodeUri == null || !BbcProgrammeAdapter.SLASH_PROGRAMMES_URL_PATTERN.matcher(episodeUri).matches()) {
 				continue;
 			}
 			
 			SlashProgrammesRdf slashProgrammesEpisode = readSlashProgrammesDataForEpisode(episodeUri);
 
 	
 			
 			Item item = programmeGraphExtractor.extract(new BbcProgrammeSource(episodeUri, slashProgrammesUri(episodeUri).replace(".rdf", ""), slashProgrammesEpisode, slashProgrammesRdf(slashProgrammesEpisode)));
 			
 			SyndLink relatedLink = relatedLinkFrom(entry);
 			String brandUri = brandLinkFrom(relatedLink);
 			
 			if (brandUri == null || brandUri.equals(episodeUri)) {
 				playlist.addItem(item);
 				continue; // no associated brand is specified as being related to this item.
 			}
 			
 			Brand brand = brandLookup.get(brandUri);
 			if (brand == null) {
 				brand = brand(brandUri);
				
				if (brand == null) {
					playlist.addItem(item);
					continue; 
				}
				
 				brandLookup.put(brandUri, brand);
 				playlist.addPlaylist(brand);
 			}
 			
 			brand.addItem(item);
 		}
 		
 		return playlist;
 	}
 	
 	@SuppressWarnings("unchecked")
 	private boolean isRadioProgramme(SyndEntry entry) {
 		List<SyndCategory> categories = entry.getCategories();
 		for (SyndCategory category : categories) {
 			if ("Radio".equalsIgnoreCase(category.getName())) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public SlashProgrammesVersionRdf slashProgrammesRdf(SlashProgrammesRdf episodeRef) {
 		SlashProgrammesEpisode episode = episodeRef.episode();
 		if (episode.versions() == null || episode.versions().isEmpty()) {
 			return null;
 		}
 		return readSlashProgrammesDataForVersion(episode.versions().get(0));
 	}
 	
 	private SlashProgrammesVersionRdf readSlashProgrammesDataForVersion(SlashProgrammesVersion slashProgrammesVersion) {
 		try {
 			return versionClient.get(slashProgrammesUri(slashProgrammesVersion));
 		} catch (Exception e) {
 			log.warn(e);
 			return null;
 		}
 	}
 
 	private SlashProgrammesRdf readSlashProgrammesDataForEpisode(String episodeUri) {
 		try {
 			return episodeClient.get(slashProgrammesUri(episodeUri));
 		} catch (Exception e) {
 			log.warn(e);
 			return null;
 		}
 	}
 
 	private String slashProgrammesUri(String episodeUri) {
 		return episodeUri.replaceAll("/iplayer/episode", "/programmes") + ".rdf";
 	}
 	
 	private String slashProgrammesUri(SlashProgrammesVersion slashProgrammesVersion) {
 		return "http://www.bbc.co.uk" + slashProgrammesVersion.resourceUri().replace("#programme", "") + ".rdf";
 	}
 	
 	private Brand brand(String brandUri) {
		return (Brand) brandFetcher.fetch(brandUri, new NullRequestTimer());
 	}
 
 	private String episodeUriFrom(String selfLink) {
 		Pattern pidPattern = Pattern.compile("http://feeds\\.bbc\\.co\\.uk.*?/b00(.+)");
 		Matcher matcher = pidPattern.matcher(selfLink);
 		if (matcher.find()) {
 			return "http://www.bbc.co.uk/programmes/b00" + matcher.group(1);
 		}
 		log.warn("Could not extract programme id from " + selfLink);
 		return null;
 	}
 
 	private String brandLinkFrom(SyndLink link) {
 		if (link == null) {
 			return null;
 		}
 		return link.getHref().replace("/microsite", "");
 	}
 
 	@SuppressWarnings("unchecked")
 	private SyndLink relatedLinkFrom(SyndEntry entry) {
 		for (SyndLink link : (List<SyndLink>) entry.getLinks()) {
 			if ("related".equals(link.getRel())) {
 				return link;
 			}
 		}
 		return null;
 	}
 
 	@SuppressWarnings("unchecked")
 	private String selfLink(SyndEntry entry) {
 		for (SyndLink link : (List<SyndLink>) entry.getLinks()) {
 			if ("self".equals(link.getRel())) {
 				return link.getHref();
 			}
 		}
 		return null;
 	}
 }
