 package com.mmounirou.spotify.commands;
 
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.util.Collection;
 import java.util.Map;
 
 import javax.annotation.Nullable;
 
 import org.apache.commons.lang.StringUtils;
 
 import com.google.common.base.Function;
 import com.google.common.base.Predicate;
 import com.google.common.collect.FluentIterable;
 import com.google.common.collect.Maps;
 import com.google.common.eventbus.EventBus;
 import com.mmounirou.spotify.commands.RunCommand.RunMode;
 import com.mmounirou.spotify.commons.sql.ConnectionUtils;
 import com.mmounirou.spotify.dao.ArtistDao;
 import com.mmounirou.spotify.dao.DBUtils;
 import com.mmounirou.spotify.datamodel.Artists;
 import com.mmounirou.spotify.listener.EventListener.ArtistCommandEndEvent;
 import com.mmounirou.spotify.listener.EventListener.ArtistCommandStartEvent;
 import com.mmounirou.spotify.listener.EventListener.NewArtistEvent;
 import com.mmounirou.spoty4j.api.Search;
 import com.mmounirou.spoty4j.core.Artist;
 
 public class ArtistCommand implements Command
 {
 
 	private static class PrefectMatch implements Predicate<Artist>
 	{
 		private final String strName;
 
 		public PrefectMatch(String name)
 		{
 			strName = name;
 		}
 
 		public boolean apply(@Nullable Artist input)
 		{
 			return StringUtils.equalsIgnoreCase(input.getName(), strName);
 		}
 
 	}
 
 	private Iterable<String> m_strArtists;
 	private EventBus m_eventBus;
 
 	public ArtistCommand(EventBus eventBus, Iterable<String> strArtists)
 	{
 		m_eventBus = eventBus;
 		m_strArtists = strArtists;
 	}
 
 	public void run() throws CommandException
 	{
 		m_eventBus.post(ArtistCommandStartEvent.of(this));
 		
 		Function<Artist, Artists> toDbArtists = new Function<Artist, Artists>()
 		{
 			@Nullable
 			public Artists apply(@Nullable Artist input)
 			{
 				Artists artists = new Artists();
 				artists.setName(input.getName());
 				artists.setUri(input.getHref());
 				return artists;
 			}
 		};
 
 		Function<Artist, String> toHref = new Function<Artist, String>()
 		{
 
 			@Nullable
 			public String apply(@Nullable Artist input)
 			{
 				return input.getHref();
 			}
 		};
 
 		Connection connection = null;
 		try
 		{
 
 			connection = DBUtils.connectToDb();
 			Map<String, Artist> spotifyArtistByHref = Maps.uniqueIndex(fetchSpotifyArtists(m_strArtists), toHref);
 
 			final Collection<Artists> addedArtist = new ArtistDao(connection).addArtistIfNotExist(FluentIterable.from(spotifyArtistByHref.values()).transform(toDbArtists).toImmutableList());
 			Predicate<Artist> newAddedArtistPredicate = new Predicate<Artist>()
 			{
 
 				public boolean apply(@Nullable Artist input)
 				{
					return addedArtist.contains(input.getHref());
 				}
 			};
 
 			fireNewArtists(Maps.filterValues(spotifyArtistByHref, newAddedArtistPredicate).values());
 		}
 		catch ( SQLException e )
 		{
 			throw new CommandException(e);
 		}
 		finally
 		{
 			ConnectionUtils.closeQuietly(connection);
 			m_eventBus.post(ArtistCommandEndEvent.of(this));
 		}
 
 	}
 
 	private void fireNewArtists(Collection<Artist> addedArtist)
 	{
 		for ( Artist artists : addedArtist )
 		{
 			m_eventBus.post(NewArtistEvent.of(artists, RunMode.NORMAL));
 		}
 	}
 
 	private FluentIterable<Artist> fetchSpotifyArtists(Iterable<String> strArtists)
 	{
 		Function<String, Iterable<Artist>> querySpotifyArtist = new Function<String, Iterable<Artist>>()
 		{
 
 			@Nullable
 			public Iterable<Artist> apply(@Nullable String strArtist)
 			{
 				return FluentIterable.from(Search.searchArtist(strArtist)).filter(new PrefectMatch(strArtist));
 			}
 		};
 
 		Predicate<String> notBlankName = new Predicate<String>()
 		{
 
 			public boolean apply(@Nullable String input)
 			{
 				return StringUtils.isNotBlank(input);
 			}
 		};
 
 		//@formatter:off
 		return FluentIterable.from(strArtists)
 					  .filter(notBlankName)
 					  .transformAndConcat(querySpotifyArtist);
 	   //@formatter:on		
 	}
 }
