 /*
  * Copyright 2013 Odysseus Software GmbH
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.musicmount.builder.impl;
 
 import java.io.File;
 
 import org.junit.Assert;
 import org.junit.Test;
 import org.musicmount.builder.model.Album;
 import org.musicmount.builder.model.AlbumArtist;
 import org.musicmount.builder.model.Library;
 import org.musicmount.builder.model.TrackArtist;
 
 public class LibraryParserTest {
 	@Test
 	public void testSampleAlbum() throws Exception {
 		File inputFolder = new File(getClass().getResource("/sample-album").toURI());
 		Library library = new LibraryParser(new SimpleAssetParser()).parse(inputFolder, new AssetStore("test"));
 		
 		// aif and wav samples are skipped; expect one album with three tracks (m4a-aac, m4a-alac and mp3)
 		
 		Assert.assertEquals(1, library.getAlbumArtists().size());
 		Assert.assertEquals(1, library.getTrackArtists().size());
 		Assert.assertEquals(1, library.getAlbums().size());
 		
 		Album album = library.getAlbums().get(0);
 		Assert.assertEquals("Sample Album", album.getTitle());
 		Assert.assertEquals("Sample Artist", album.getArtist().getTitle());
 		Assert.assertEquals(1, album.getDiscs().size());
 		Assert.assertTrue(album.getDiscs().containsKey(1));
 		Assert.assertEquals(3, album.getDiscs().get(1).getTracks().size());
 
 		Assert.assertEquals("Sample - M4A (AAC)", album.getDiscs().get(1).getTracks().get(0).getTitle());
 		Assert.assertEquals(Integer.valueOf(1), album.getDiscs().get(1).getTracks().get(0).getTrackNumber());
 		Assert.assertEquals(Integer.valueOf(2013), album.getDiscs().get(1).getTracks().get(0).getYear());
 		Assert.assertEquals(album, album.getDiscs().get(1).getTracks().get(0).getAlbum());
 		Assert.assertTrue(album.getDiscs().get(1).getTracks().get(0).isArtworkAvailable());
 		Assert.assertFalse(album.getDiscs().get(1).getTracks().get(0).isCompilation());
 		
 		Assert.assertEquals("Sample - M4A (ALAC)", album.getDiscs().get(1).getTracks().get(1).getTitle());
 		Assert.assertEquals(Integer.valueOf(2), album.getDiscs().get(1).getTracks().get(1).getTrackNumber());
 		Assert.assertEquals(Integer.valueOf(2013), album.getDiscs().get(1).getTracks().get(1).getYear());
 		Assert.assertEquals(album, album.getDiscs().get(1).getTracks().get(1).getAlbum());
 		Assert.assertTrue(album.getDiscs().get(1).getTracks().get(1).isArtworkAvailable());
 		Assert.assertFalse(album.getDiscs().get(1).getTracks().get(1).isCompilation());
 		
 		Assert.assertEquals("Sample - MP3", album.getDiscs().get(1).getTracks().get(2).getTitle());
 		Assert.assertEquals(Integer.valueOf(3), album.getDiscs().get(1).getTracks().get(2).getTrackNumber());
 		Assert.assertEquals(Integer.valueOf(2013), album.getDiscs().get(1).getTracks().get(2).getYear());
 		Assert.assertEquals(album, album.getDiscs().get(1).getTracks().get(1).getAlbum());
 		Assert.assertTrue(album.getDiscs().get(1).getTracks().get(2).isArtworkAvailable());
 		Assert.assertFalse(album.getDiscs().get(1).getTracks().get(2).isCompilation());
 	}
 
 	@Test
 	public void testSampleLibrary() throws Exception {
 		File inputFolder = new File(getClass().getResource("/sample-library").toURI());
 		Library library = new LibraryParser(new SimpleAssetParser()).parse(inputFolder, new AssetStore("test"));
 
 		Assert.assertEquals(4, library.getAlbumArtists().size());
 		Assert.assertEquals(6, library.getTrackArtists().size());
		Assert.assertEquals(4, library.getAlbums().size());
 
 		/*
 		 * verify album artists
 		 */
 		AlbumArtist albumArtist1 = library.getAlbumArtists().get("Album Artist");
 		Assert.assertNotNull(albumArtist1);
 		Assert.assertEquals(1, albumArtist1.albumsCount());
 		Album albumArtistAlbum = albumArtist1.getAlbums().get("Album Artist - Album");
 		Assert.assertFalse(albumArtistAlbum.isCompilation());
 		Assert.assertNotNull(albumArtistAlbum);
 		Assert.assertEquals(4, albumArtistAlbum.getTracks().size());
 
 		AlbumArtist albumArtist2 = library.getAlbumArtists().get("Track Artist");
 		Assert.assertNotNull(albumArtist2);
 		Assert.assertEquals(1, albumArtist2.albumsCount());
 		Album trackArtistAlbum = albumArtist2.getAlbums().get("Track Artist - Album");
 		Assert.assertFalse(trackArtistAlbum.isCompilation());
 		Assert.assertNotNull(trackArtistAlbum);
 		Assert.assertEquals(2, trackArtistAlbum.getTracks().size());
 		
 		AlbumArtist albumArtist3 = library.getAlbumArtists().get("Some Artist");
 		Assert.assertNotNull(albumArtist3);
 		Assert.assertEquals(1, albumArtist3.albumsCount());
 		Album someArtistCompilation = albumArtist3.getAlbums().get("Some Artist - Compilation");
 		Assert.assertNotNull(someArtistCompilation);
 		Assert.assertTrue(someArtistCompilation.isCompilation());
 		Assert.assertEquals(2, someArtistCompilation.getTracks().size());
 		
 		AlbumArtist albumArtist4 = library.getAlbumArtists().get(null); // "Various Artists"
 		Assert.assertNotNull(albumArtist4);
		Assert.assertEquals(1, albumArtist4.albumsCount());
 		Album variousArtistsAlbum = albumArtist4.getAlbums().get("Various Artists - Album");
 		Assert.assertTrue(variousArtistsAlbum.isCompilation());
 		Assert.assertNotNull(variousArtistsAlbum);
 		Assert.assertEquals(3, variousArtistsAlbum.getTracks().size());
 
 		/*
 		 * verify track artists 
 		 */
 		TrackArtist trackArtist1 = library.getTrackArtists().get("Album Artist");
 		Assert.assertNotNull(trackArtist1);
 		Assert.assertEquals(1, trackArtist1.albumsCount());
 		Assert.assertTrue(trackArtist1.getAlbums().contains(albumArtistAlbum));
 
 		TrackArtist trackArtist2 = library.getTrackArtists().get("Track Artist");
 		Assert.assertNotNull(trackArtist2);
 		Assert.assertEquals(2, trackArtist2.albumsCount());
 		Assert.assertTrue(trackArtist2.getAlbums().contains(albumArtistAlbum));
 		Assert.assertTrue(trackArtist2.getAlbums().contains(trackArtistAlbum));
 
 		TrackArtist trackArtist3 = library.getTrackArtists().get("Some Artist");
 		Assert.assertNotNull(trackArtist3);
 		Assert.assertEquals(1, trackArtist3.albumsCount());
 		Assert.assertTrue(trackArtist3.getAlbums().contains(someArtistCompilation));
 
 		TrackArtist trackArtist4 = library.getTrackArtists().get("Various Artist 1");
 		Assert.assertNotNull(trackArtist4);
 		Assert.assertEquals(1, trackArtist4.albumsCount());
 		Assert.assertTrue(trackArtist4.getAlbums().contains(variousArtistsAlbum));
 
 		TrackArtist trackArtist5 = library.getTrackArtists().get("Various Artist 2");
 		Assert.assertNotNull(trackArtist5);
 		Assert.assertEquals(1, trackArtist5.albumsCount());
 		Assert.assertTrue(trackArtist5.getAlbums().contains(variousArtistsAlbum));
 
 		TrackArtist trackArtist6 = library.getTrackArtists().get(null);
 		Assert.assertNotNull(trackArtist6);
		Assert.assertEquals(1, trackArtist6.albumsCount());
 		Assert.assertTrue(trackArtist6.getAlbums().contains(variousArtistsAlbum));
 	}

 }
