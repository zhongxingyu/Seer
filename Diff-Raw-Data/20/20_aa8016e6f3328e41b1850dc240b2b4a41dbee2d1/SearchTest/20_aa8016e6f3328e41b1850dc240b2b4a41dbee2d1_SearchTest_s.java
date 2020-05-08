 package com.mmounirou.spoty4j.api;
 
 import static org.fest.assertions.Assertions.assertThat;
 
 import org.junit.Test;
 
 import com.google.common.collect.ImmutableList;
 import com.mmounirou.spoty4j.core.Album;
 import com.mmounirou.spoty4j.core.Artist;
 import com.mmounirou.spoty4j.core.Track;
 
 public class SearchTest
 {
 
 	@Test
 	public void testSearchAlbum()
 	{
 		ImmutableList<Album> searchAlbum = Search.searchAlbum("Empire mates State of Mind");
 		assertThat(searchAlbum).hasSize(1);
 		assertThat(searchAlbum.get(0).getHref()).isEqualTo("spotify:album:2PWHJ3VkEFZKUgtM5NEC2X");
 		assertThat(searchAlbum.get(0).getId()).isEqualTo("700261810690");
 		assertThat(searchAlbum.get(0).getName()).isEqualTo("Empire Mates State of Mind");
		assertThat(searchAlbum.get(0).getPopularity()).isEqualTo(0.51735);
 		assertThat(searchAlbum.get(0).getReleased()).isEqualTo(0);
 		assertThat(searchAlbum.get(0).getTracks()).isEmpty();
 
 	}
 
 	@Test
 	public void testSearchArtist()
 	{
 		ImmutableList<Artist> artists = Search.searchArtist("brymo");
 		assertThat(artists).hasSize(1);
 		assertThat(artists.get(0).getName()).isEqualTo("Brymo");
 		assertThat(artists.get(0).getHref()).isEqualTo("spotify:artist:094nOQ29vLC8FjZ3PhnM2u");
 	}
 
 	@Test
 	public void testSearchTrack()
 	{
 		ImmutableList<Track> tracks = Search.searchTrack("Pakurumo");
		assertThat(tracks).hasSize(1);
 		assertThat(tracks.get(0).getHref()).isEqualTo("spotify:track:5YgyvTZmdkIfFOnj8ri8mZ");
 		assertThat(tracks.get(0).getDiskNumber()).isEqualTo(0);
 		assertThat(tracks.get(0).getId()).isEqualTo("ushm81288000");
 		assertThat(tracks.get(0).getName()).isEqualTo("Pakurumo");
		assertThat(tracks.get(0).getPopularity()).isEqualTo(0.44105);
		assertThat(tracks.get(0).getTrackNumber()).isEqualTo(4);
 		assertThat(tracks.get(0).getAlbum().getHref()).isEqualTo("spotify:album:4o0rVyqZMIvO3PCgbchcgo");
 		assertThat(tracks.get(0).getArtist().getHref()).isEqualTo("spotify:artist:3tVQdUvClmAT7URs9V3rsp");
 
 	}
 
 }
