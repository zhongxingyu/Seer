 package com.mmounirou.spotify.commands;
 
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.util.Collection;
 import java.util.Map;
 import java.util.Set;
 
 import javax.annotation.Nullable;
 
 import com.google.common.base.Function;
 import com.google.common.base.Predicate;
 import com.google.common.collect.FluentIterable;
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.Maps;
 import com.google.common.eventbus.EventBus;
 import com.mmounirou.spotify.commons.sql.ConnectionUtils;
 import com.mmounirou.spotify.dao.AlbumDao;
 import com.mmounirou.spotify.dao.ArtistDao;
 import com.mmounirou.spotify.dao.DBUtils;
 import com.mmounirou.spotify.datamodel.Albums;
 import com.mmounirou.spotify.datamodel.Artists;
 import com.mmounirou.spotify.listener.EventListener.NewAlbumEvent;
 import com.mmounirou.spotify.listener.EventListener.RunCommandEndEvent;
 import com.mmounirou.spotify.listener.EventListener.RunCommandStartEvent;
 import com.mmounirou.spoty4j.core.Album;
 import com.mmounirou.spoty4j.core.Artist;
 
 public class RunCommand implements Command
 {
 
 	private final EventBus m_eventBus;
 	private final RunMode m_runMode;
 
 	public enum RunMode
 	{
 		TEST,
 		LEARN,
 		NORMAL, 
 		LIST;
 	}
 
 	public RunCommand(EventBus eventBus, RunMode runMode)
 	{
 		m_eventBus = eventBus;
 		m_runMode = runMode;
 	}
 
 	public void run() throws CommandException
 	{
 		m_eventBus.post(RunCommandStartEvent.of(this));
 
 		Function<Album, Albums> toDbAlbum = new Function<Album, Albums>()
 		{
 
 			@Nullable
 			public Albums apply(@Nullable Album input)
 			{
 				Albums albums = new Albums();
 				albums.setName(input.getName());
 				albums.setUri(input.getHref());
 				return albums;
 			}
 		};
 
 		Function<Album, String> toHref = new Function<Album, String>()
 		{
 
 			@Nullable
 			public String apply(@Nullable Album input)
 			{
 				return input.getHref();
 			}
 		};
 
 		Connection connection = null;
 		try
 		{
 			connection = DBUtils.connectToDb();
 			ArtistDao artistDao = new ArtistDao(connection);
 			AlbumDao albumDao = new AlbumDao(connection);
 
			Map<String, Album> albumById =Maps.uniqueIndex(fetchAlbums(artistDao.all()).toImmutableSet(), toHref);
 			final Set<String> existingAlbumsHref = albumDao.exist(ImmutableList.copyOf(albumById.keySet())).toImmutableSet();
 
 			Predicate<Album> newAlbumPredicate = new Predicate<Album>()
 			{
 				
 				public boolean apply(@Nullable Album arg0)
 				{
 					return !existingAlbumsHref.contains(arg0.getHref());
 				}
 			};
 			Collection<Album> newAlbums = Maps.filterValues(albumById,newAlbumPredicate).values();
 
 			if ( getRunMode() == RunMode.LEARN || getRunMode() == RunMode.NORMAL )
 			{
 				albumDao.addAlbums(FluentIterable.from(newAlbums).transform(toDbAlbum).toImmutableList());
 			}
 
 			fireNewAlbum(newAlbums);
 
 		}
 		catch ( SQLException e )
 		{
 			throw new CommandException(e);
 		}
 		finally
 		{
 			ConnectionUtils.closeQuietly(connection);
 			m_eventBus.post(RunCommandEndEvent.of(this));
 
 		}
 
 	}
 
 	private void fireNewAlbum(Iterable<Album> newAlbums)
 	{
 		for ( Album albums : newAlbums )
 		{
 			m_eventBus.post(NewAlbumEvent.of(albums, getRunMode()));
 		}
 	}
 
 	private FluentIterable<Album> fetchAlbums(Iterable<Artists> artists) throws CommandException
 	{
 		Function<Artists, Artist> fetchArtist = new Function<Artists, Artist>()
 		{
 
 			@Nullable
 			public Artist apply(@Nullable Artists artist)
 			{
 				// TODO wrap around a try catch
 				return new Artist(artist.getUri(), artist.getName()).fetch();
 			}
 		};
 
 		Function<Artist, Iterable<Album>> getAlbums = new Function<Artist, Iterable<Album>>()
 		{
 
 			@Nullable
 			public Iterable<Album> apply(@Nullable Artist artist)
 			{
 				// TODO wrap around a try catch
 				return artist.getAlbums();
 			}
 		};
 
 		return FluentIterable.from(artists).transform(fetchArtist).transformAndConcat(getAlbums);
 
 	}
 
 	public RunMode getRunMode()
 	{
 		return m_runMode;
 	}
 }
