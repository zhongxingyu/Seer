 package com.mmounirou.spotiboard.spotify;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.TimeUnit;
 
 import javax.annotation.Nullable;
 
 import org.apache.commons.digester.Digester;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.StringUtils;
 import org.xml.sax.SAXException;
 
 import com.google.common.base.Charsets;
 import com.google.common.base.Function;
 import com.google.common.base.Joiner;
 import com.google.common.base.Predicate;
 import com.google.common.collect.FluentIterable;
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.common.collect.Sets;
 import com.mmounirou.spotiboard.SpotiBoard;
 import com.mmounirou.spotiboard.billboard.Track;
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.WebResource;
 
 public class SpotifyHrefQuery
 {
 	private static final int QUERY_LIMIT_BY_SECONDS = 100;
 	private TrackCache m_trackCache;
 
 	public SpotifyHrefQuery(TrackCache trackCache)
 	{
 		this.m_trackCache = trackCache;
 	}
 
 	public Map<Track, String> getTrackHrefs(Set<Track> tracks) throws SpotifyException
 	{
 		Map<Track, String> result = Maps.newLinkedHashMap();
 		int queryCount = 0;
 
 		for (Track track : tracks)
 		{
 			String strHref = m_trackCache.get(track);
 			if (strHref == null)
 			{
				if ((queryCount % QUERY_LIMIT_BY_SECONDS) == 0)
 				{
 					try
 					{
 						Thread.sleep(TimeUnit.SECONDS.toMillis(1));
 					} catch (InterruptedException e)
 					{
 						// DO nothing
 					}
 				}
 
 				try
 				{
 					Client client = Client.create();
 					WebResource resource = client.resource("http://ws.spotify.com");
 					String strXmlResult = resource.path("search/1/track").queryParam("q", track.getSong()).get(String.class);
 
 					List<XTracks> xtracks = parseResult(strXmlResult);
 					if (xtracks.isEmpty())
 					{
 						SpotiBoard.LOGGER.warn(String.format("no spotify song for %s:%s", track.getArtist(), track.getSong()));
 					} else
 					{
 						strHref = findBestMatchingTrack(xtracks, track).getHref();
 						m_trackCache.put(track, strHref);
 						queryCount++;
 					}
 				} catch (IOException e)
 				{
 					throw new SpotifyException(e);
 				} catch (SAXException e)
 				{
 					throw new SpotifyException(e);
 				}
 			}
 			if (strHref != null)
 			{
 				result.put(track, strHref);
 			}
 		}
 		return ImmutableMap.copyOf(result);
 	}
 
 	private XTracks findBestMatchingTrack(List<XTracks> xtracks, final Track track)
 	{
 		if (xtracks.size() == 1)
 		{
 			return xtracks.get(0);
 		}
 
 		final Set<String> artistNames = split(track.getArtist(), new String[] { "Featuring", "Feat\\.", "&" });
 
 		// find with the perfect artist name
 		List<XTracks> withArtistName = Lists.newArrayList(Iterables.filter(xtracks, new Predicate<XTracks>()
 		{
 			@Override
 			public boolean apply(@Nullable XTracks xtrack)
 			{
 				return artistNames.contains(xtrack.getArtistName().trim());
 			}
 		}));
 
 		// Try with artist name is contained (featuring in artist name)
 		if (withArtistName.isEmpty())
 		{
 			withArtistName = Lists.newArrayList(Iterables.filter(xtracks, new Predicate<XTracks>()
 			{
 				@Override
 				public boolean apply(@Nullable final XTracks xtrack)
 				{
 					return !FluentIterable.from(artistNames).filter(new Predicate<String>()
 					{
 
 						@Override
 						public boolean apply(@Nullable String artistName)
 						{
 							return StringUtils.containsIgnoreCase(xtrack.getArtistName(), artistName);
 						}
 					}).isEmpty();
 				}
 			}));
 		}
 
 		// no match found use all result
 		if (withArtistName.isEmpty())
 		{
 			XTracks usedTrack = xtracks.get(0);
 			SpotiBoard.LOGGER.warn(String.format("no perfect match found for %s:%s (%s) use more popular song %s:%s", track.getArtist(), track.getSong(),
 					Joiner.on(",").join(artistNames), usedTrack.getArtistName(), usedTrack.getTrackName()));
 			withArtistName = xtracks;
 		}
 
 		return xtracks.get(0);
 
 	}
 
 	private static Set<String> split(String artist, String[] separators)
 	{
 		Set<String> result = Sets.newLinkedHashSet();
 		result.add(artist);
 
 		int previousSize = 0;
 		do
 		{
 			previousSize = result.size();
 			Set<String> temp = Sets.newHashSet(result);
 			result.clear();
 			for (String elmt : temp)
 			{
 				for (String separator : separators)
 				{
 					List<String> splitted = Arrays.asList(elmt.split(separator));
 					if (splitted.size() > 1)
 					{
 						result.addAll(splitted);
 					}
 				}
 			}
 			if (result.isEmpty())
 			{
 				result = temp;
 			}
 
 		} while (previousSize != result.size());
 
 		return Sets.newHashSet(Iterables.transform(result, new Function<String, String>()
 		{
 
 			@Override
 			@Nullable
 			public String apply(@Nullable String input)
 			{
 				return input.trim();
 			}
 		}));
 	}
 
 	private static List<XTracks> parseResult(String strResult) throws IOException, SAXException
 	{
 		Digester digester = new Digester();
 		List<XTracks> result = Lists.newArrayList();
 		digester.push(result);
 		addRules(digester);
 		digester.parse(IOUtils.toInputStream(strResult, Charsets.UTF_8));
 		return result;
 	}
 
 	private static void addRules(Digester digester)
 	{
 		digester.addObjectCreate("tracks/track", XTracks.class);
 		digester.addSetNext("tracks/track", "add");
 		digester.addSetProperties("tracks/track");
 		digester.addBeanPropertySetter("tracks/track/name", "trackName");
 		digester.addBeanPropertySetter("tracks/track/artist/name", "artistName");
 		digester.addBeanPropertySetter("tracks/track/album/availability/territories", "availability");
 	}
 
 	public static void main(String[] args)
 	{
 		System.out.println(split("Gabry Ponte & Sophia Del Carmen Featuring Pitbull Feat. Jay-Z", new String[] { "Featuring", "Feat\\.", "&" }));
 	}
 
 }
