 package com.mmounirou.spoty4j.xml;
 
 import static org.fest.assertions.Assertions.assertThat;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import javax.ws.rs.WebApplicationException;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.google.common.collect.ImmutableList;
 import com.mmounirou.spoty4j.core.Album;
 import com.mmounirou.spoty4j.xml.AlbumSearchResultProvider.AlbumSearchResult;
 
 public class AlbumSearchResultProviderTest
 {
 
 	private AlbumSearchResultProvider m_albumSearchResultProvider;
 
 	@Before
 	public void createAlbumSearchResultProvider() throws Exception
 	{
 		m_albumSearchResultProvider = new AlbumSearchResultProvider();
 	}
 
 	@After
 	public void tearDown() throws Exception
 	{
 	}
 
 	@Test
 	public void testIsReadable()
 	{
 		assertThat(m_albumSearchResultProvider.isReadable(AlbumSearchResult.class, null, null, null)).isTrue();
 	}
 
 	@Test
 	public void testIsNotReadable()
 	{
		assertThat(m_albumSearchResultProvider.isReadable(Object.class, null, null, null)).isFalse();
 	}
 
 	@Test
 	public void testReadFrom() throws WebApplicationException, IOException
 	{
 		InputStream entityStream = AlbumSearchResultProviderTest.class.getResourceAsStream("/search-album.xml");
 		AlbumSearchResult result = m_albumSearchResultProvider.readFrom(AlbumSearchResult.class, null, null, null, null, entityStream);
 
 		ImmutableList<Album> albums = result.getAlbums();
 		assertThat(albums).hasSize(2);
 		Album album = albums.get(0);
 
 		assertThat(album.getHref()).isEqualTo("spotify:album:1zCNrbPpz5OLSr6mSpPdKm");
 		assertThat(album.getName()).isEqualTo("Greatest Hits");
 		assertThat(album.getArtist().getHref()).isEqualTo("spotify:artist:7jy3rLJdDQY21OgRLCZ9sD");
 		assertThat(album.getArtist().getName()).isEqualTo("Foo Fighters");
 		assertThat(album.getId()).isEqualTo("884977373295");
 		assertThat(album.getPopularity()).isEqualTo(0.83412);
 	}
 
 }
