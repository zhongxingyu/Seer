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
 import java.net.URISyntaxException;
 
 import org.junit.Assert;
 import org.junit.Test;
 import org.musicmount.builder.model.Album;
 import org.musicmount.builder.model.AlbumArtist;
 import org.musicmount.builder.model.ArtistType;
 import org.musicmount.builder.model.Track;
 
 public class SimpleResourceLocatorTest {
 	@Test
 	public void testGetAlbumCollectionPath() {
 		SimpleResourceLocator resourceLocator = new SimpleResourceLocator(null, false, false);
		Assert.assertEquals("albumArtists/10/01-albums.json", resourceLocator.getAlbumCollectionPath(new AlbumArtist(0x1001, "foo")));
 	}
 
 	Album createAlbum(long albumId, AlbumArtist artist, boolean artworkAvailable) {
 		Track track = new Track(null, null, artworkAvailable, false, null, null, null, null, null, null);
 		Album album = new Album(albumId, null);
 		album.getTracks().add(track);
 		track.setAlbum(album);
 		if (artist != null) {
 			album.setArtist(artist);
 			artist.getAlbums().put(album.getTitle(), album);
 		}
 		return album;
 	}
 	
 	@Test
 	public void testGetAlbumImagePath() {
 		Album album;
 		
 		SimpleResourceLocator resourceLocator = new SimpleResourceLocator(null, false, false);
 		album = createAlbum(0x1001, null, true);
 		Assert.assertEquals("albums/10/01/artwork.jpg", resourceLocator.getAlbumImagePath(album, ImageType.Artwork));
 		Assert.assertEquals("albums/10/01/tile.jpg", resourceLocator.getAlbumImagePath(album, ImageType.Tile));
 		Assert.assertEquals("albums/10/01/thumbnail.png", resourceLocator.getAlbumImagePath(album, ImageType.Thumbnail));
 
 		album = createAlbum(0x1001, null, false); // no artwork available
 		Assert.assertNull(resourceLocator.getAlbumImagePath(album, ImageType.Artwork));
 		Assert.assertNull(resourceLocator.getAlbumImagePath(album, ImageType.Tile));
 		Assert.assertNull(resourceLocator.getAlbumImagePath(album, ImageType.Thumbnail));
 
 		resourceLocator = new SimpleResourceLocator(null, false, true); // noImages
 		album = createAlbum(0x1001, null, true);
 		Assert.assertNull(resourceLocator.getAlbumImagePath(album, ImageType.Artwork));
 		Assert.assertNull(resourceLocator.getAlbumImagePath(album, ImageType.Tile));
 		Assert.assertNull(resourceLocator.getAlbumImagePath(album, ImageType.Thumbnail));
 	}
 
 	@Test
 	public void testGetAlbumIndexPath() {
 		SimpleResourceLocator resourceLocator = new SimpleResourceLocator(null, false, false);
 		Assert.assertEquals("albums/index.json", resourceLocator.getAlbumIndexPath());
 	}
 
 	@Test
 	public void testGetAlbumPath() {
 		SimpleResourceLocator resourceLocator = new SimpleResourceLocator(null, false, false);
 		Assert.assertEquals("albums/10/01/index.json", resourceLocator.getAlbumPath(new Album(0x1001, "foo")));
 	}
 
 	@Test
 	public void testGetArtistImagePath() {
 		AlbumArtist artist;
 
 		SimpleResourceLocator resourceLocator = new SimpleResourceLocator(null, false, false);
 		artist = createAlbum(0x1001, new AlbumArtist(0, "foo"), true).getArtist();
 		Assert.assertNull(resourceLocator.getArtistImagePath(artist, ImageType.Artwork));
 		Assert.assertNull(resourceLocator.getArtistImagePath(artist, ImageType.Tile));
 		Assert.assertEquals("albums/10/01/thumbnail.png", resourceLocator.getArtistImagePath(artist, ImageType.Thumbnail));
 
 		artist = createAlbum(0x1001, new AlbumArtist(0, null), true).getArtist(); // no album artist name -> no images
 		Assert.assertNull(resourceLocator.getArtistImagePath(artist, ImageType.Artwork));
 		Assert.assertNull(resourceLocator.getArtistImagePath(artist, ImageType.Tile));
 		Assert.assertNull(resourceLocator.getArtistImagePath(artist, ImageType.Thumbnail));
 
 		artist = createAlbum(0x1001, new AlbumArtist(0, null), false).getArtist(); // no artwork available
 		Assert.assertNull(resourceLocator.getArtistImagePath(artist, ImageType.Artwork));
 		Assert.assertNull(resourceLocator.getArtistImagePath(artist, ImageType.Tile));
 		Assert.assertNull(resourceLocator.getArtistImagePath(artist, ImageType.Thumbnail));
 		
 		resourceLocator = new SimpleResourceLocator(null, false, true); // noImages
 		artist = createAlbum(0x1001, new AlbumArtist(0, null), true).getArtist();
 		Assert.assertNull(resourceLocator.getArtistImagePath(artist, ImageType.Artwork));
 		Assert.assertNull(resourceLocator.getArtistImagePath(artist, ImageType.Tile));
 		Assert.assertNull(resourceLocator.getArtistImagePath(artist, ImageType.Thumbnail));
 	}
 
 	@Test
 	public void testGetArtistIndexPath() {
 		SimpleResourceLocator resourceLocator = new SimpleResourceLocator(null, false, false);
 		Assert.assertEquals("albumArtists/index.json", resourceLocator.getArtistIndexPath(ArtistType.AlbumArtist));
 		Assert.assertEquals("artists/index.json", resourceLocator.getArtistIndexPath(ArtistType.TrackArtist));
 	}
 
 	@Test
 	public void testFile() throws URISyntaxException {
 		File outputFolder = new File(getClass().getResource("/sample-assets").toURI());
 		SimpleResourceLocator resourceLocator = new SimpleResourceLocator(outputFolder, false, false);
 		Assert.assertEquals(new File(outputFolder, "foo/bar.json"), resourceLocator.getFile("foo/bar.json"));
 	}
 
 	@Test
 	public void testGetServiceIndexPath() {
 		SimpleResourceLocator resourceLocator = new SimpleResourceLocator(null, false, false);
 		Assert.assertEquals("index.json", resourceLocator.getServiceIndexPath());
 	}
 	
 	@Test
 	public void testHugeId() {
 		SimpleResourceLocator resourceLocator = new SimpleResourceLocator(null, false, false);
 		Assert.assertEquals("albums/10/01/index.json", resourceLocator.getAlbumPath(new Album(0x1001, "foo")));
 	}
 
 
 }
