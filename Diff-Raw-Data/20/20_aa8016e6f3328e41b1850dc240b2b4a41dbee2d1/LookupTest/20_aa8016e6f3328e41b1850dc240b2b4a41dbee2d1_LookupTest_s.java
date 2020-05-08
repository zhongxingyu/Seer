 package com.mmounirou.spoty4j.api;
 
 import static org.fest.assertions.Assertions.assertThat;
 
 import org.junit.Test;
 
 import com.mmounirou.spoty4j.core.Album;
 import com.mmounirou.spoty4j.core.Artist;
 import com.mmounirou.spoty4j.core.Track;
 
 public class LookupTest
 {
 	@Test
 	public void testFetchArtist()
 	{
 		Artist artist = Lookup.fetchArtist(new Artist("spotify:artist:094nOQ29vLC8FjZ3PhnM2u", ""));
 		assertThat(artist.getId()).isNull();
 		assertThat(artist.getName()).isEqualTo("Brymo");
 		assertThat(artist.getHref()).isNull();
 		assertThat(artist.getPopularity()).isEqualTo(0);
 
 		assertThat(artist.getAlbums()).hasSize(12);
 		assertThat(artist.getAlbums().get(0).getHref()).isEqualTo("spotify:album:1ErGlCrnA6cRLkE6zMbcSR");
 	}
 
 	@Test
 	public void testFetchAlbum()
 	{
 		Album album = Lookup.fetchAlbum(new Album("spotify:album:1ErGlCrnA6cRLkE6zMbcSR", ""));
 		assertThat(album.getHref()).isNull();
 		assertThat(album.getId()).isEqualTo("859708943221");
 		assertThat(album.getName()).isEqualTo("Go Hard");
 		assertThat(album.getPopularity()).isEqualTo(0);
 		assertThat(album.getReleased()).isEqualTo(2012);
 		assertThat(album.getTracks()).hasSize(1);
 		assertThat(album.getTracks().get(0).getHref()).isEqualTo("spotify:track:6ujUHJot6Je4G3BswkFaix");
 		assertThat(album.getArtist().getHref()).isEqualTo("spotify:artist:094nOQ29vLC8FjZ3PhnM2u");
 	}
 
 	@Test
 	public void testFetchTrack()
 	{
 		Track track = Lookup.fetchTrack(new Track("spotify:track:6ujUHJot6Je4G3BswkFaix", ""));
 		assertThat(track.getHref()).isNull();
 		assertThat(track.getId()).isEqualTo("TCABJ1235521");
 		assertThat(track.getName()).isEqualTo("Go Hard");
		assertThat(track.getLength()).isEqualTo(201.562);
 		assertThat(track.getDiskNumber()).isEqualTo(0);
 		assertThat(track.getTrackNumber()).isEqualTo(1);
		assertThat(track.getPopularity()).isEqualTo(0.16414);
 		assertThat(track.getAlbum().getHref()).isEqualTo("spotify:album:1ErGlCrnA6cRLkE6zMbcSR");
 		assertThat(track.getArtist().getHref()).isEqualTo("spotify:artist:094nOQ29vLC8FjZ3PhnM2u");
 	}
 
 }
