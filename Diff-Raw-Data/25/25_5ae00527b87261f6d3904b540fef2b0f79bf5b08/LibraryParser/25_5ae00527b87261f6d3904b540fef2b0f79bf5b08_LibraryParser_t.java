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
 import java.io.FileFilter;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.musicmount.builder.model.Album;
 import org.musicmount.builder.model.AlbumArtist;
 import org.musicmount.builder.model.Disc;
 import org.musicmount.builder.model.Library;
 import org.musicmount.builder.model.Track;
 import org.musicmount.builder.model.TrackArtist;
 
 public class LibraryParser {
 	static final Logger LOGGER = Logger.getLogger(LibraryParser.class.getName());
 
 	private static String trimToNonEmptyStringOrNull(String s) {
 		if (s != null) {
 			s = s.trim();
 			if (s.isEmpty()) {
 				s = null;
 			}
 		}
 		return s;
 	}
 
 	private final AssetParser assetParser;
 	private final FileFilter assetFilter = new FileFilter() {
 		public boolean accept(File file) {
 			return !file.getName().startsWith(".") && (file.isDirectory() || assetParser.isAssetFile(file));
 		}
 	};
 	
 	public LibraryParser(AssetParser assetParser) {
 		this.assetParser = assetParser;
 	}
 	
 	void sortTracks(Library library) {
 		/*
 		 * sort tracks by disc number, track number, title, artist
 		 */
 		Comparator<Track> comparator = new Comparator<Track>() {
 			<T extends Comparable<T>> int compareNullLast(Comparable<T> o1, T o2) {
 				if (o1 != o2) {
 					if (o1 == null) {
 						return +1;
 					} else if (o2 == null) {
 						return -1;
 					} else {
 						int result = o1.compareTo(o2);
 						if (result != 0) {
 							return result;
 						}
 					}
 				}
 				return 0;
 			}
 
 			@Override
 			public int compare(Track o1, Track o2) {
 				int result = compareNullLast(o1.getDiscNumber(), o2.getDiscNumber());
 				if (result != 0) {
 					return result;
 				}
 				result = compareNullLast(o1.getTrackNumber(), o2.getTrackNumber());
 				if (result != 0) {
 					return result;
 				}
 				result = compareNullLast(o1.getTitle(), o2.getTitle());
 				if (result != 0) {
 					return result;
 				}
 				return compareNullLast(o1.getArtist().getTitle(), o2.getArtist().getTitle());
 			}
 		};
 		
 		for (Album album : library.getAlbums()) {
 			for (Disc disc : album.getDiscs().values()) {
 				Collections.sort(disc.getTracks(), comparator);
 			}
 		}
 
 		for (Album album : library.getAlbums()) {
 			Collections.sort(album.getTracks(), comparator);
 		}
 	}
 
 	private TrackArtist uniqueTrackArtist(Album album) {
 		TrackArtist artist = album.getTracks().get(0).getArtist();
 		if (artist != null) {
 			for (Track track : album.getTracks()) {
 				if (track.getArtist() != artist) {
 					return null;
 				}
 			}
 		}
 		return artist;
 	}
 
 	public final Library parse(File assetBaseDir, AssetStore assetStore) {
 		Library library = new Library();
 		
 		// add "various artists" with id 0
 		library.getAlbumArtists().put(null, new AlbumArtist(0, null));
 		
 		// add "unknown artist" with id 0
 		library.getTrackArtists().put(null, new TrackArtist(0, null));
 
 		// parse tracks into library
 		parseLibrary(library, assetBaseDir.getAbsoluteFile(), assetStore);
 
 		/*
 		 * distribute compilations without album artist into "various artists" and
 		 * "unique artist" albums (which are moved to the corresponding album artist). 
 		 */
 		AlbumArtist variousArtists = library.getAlbumArtists().get(null);
 		Iterator<Album> variousArtistsAlbumIterator = variousArtists.getAlbums().values().iterator();
 		while (variousArtistsAlbumIterator.hasNext()) {
 			Album album = variousArtistsAlbumIterator.next();
 			TrackArtist uniqueTrackArtist = uniqueTrackArtist(album);
			if (uniqueTrackArtist != null && uniqueTrackArtist.getTitle() != null) {
 				// get album artist
 				AlbumArtist albumArtist = library.getAlbumArtists().get(uniqueTrackArtist.getTitle());
 				if (albumArtist == null) {
 					albumArtist = new AlbumArtist(library.getAlbumArtists().size(), uniqueTrackArtist.getTitle());
 					library.getAlbumArtists().put(uniqueTrackArtist.getTitle(), albumArtist);
 				}
 				Album targetAlbum = albumArtist.getAlbums().get(album.getTitle());
 				if (targetAlbum != null) {
 					// merge album tracks into existing album
 					for (Disc sourceDisc : album.getDiscs().values()) {
 						Disc targetDisc = targetAlbum.getDiscs().get(sourceDisc.getDiscNumber());
 						if (targetDisc == null) {
 							targetAlbum.getDiscs().put(sourceDisc.getDiscNumber(), sourceDisc);
 						} else {
 							targetDisc.getTracks().addAll(sourceDisc.getTracks());
 						}
 					}
 					uniqueTrackArtist.getAlbums().remove(album);
 				} else {
 					// add whole album to albumArtist
 					albumArtist.getAlbums().put(album.getTitle(), album);
 					album.setArtist(albumArtist);
 				}
 
 				// remove album from variousArtists
 				variousArtistsAlbumIterator.remove();
 			}
 		}
 
 		// remove empty "various artists"
 		if (library.getAlbumArtists().get(null).getAlbums().isEmpty()) {
 			library.getAlbumArtists().remove(null);
 		}
 		
 		// remove empty "unknown artist"
 		if (library.getTrackArtists().get(null).getAlbums().isEmpty()) {
 			library.getTrackArtists().remove(null);
 		}
 		
 		// sort tracks
 		sortTracks(library);
 
 		return library;
 	}
 
 	void parseLibrary(Library library, File directory, AssetStore assetStore) {
 		for (File file : directory.listFiles(assetFilter)) {
 			if (file.isDirectory()) {
 				parseLibrary(library, file, assetStore);
 			} else {
 				Asset asset = null;
 				try {
 					asset = assetStore.getAsset(file);
 					if (asset == null) {
 						asset = assetParser.parse(file);
 					}
 				} catch (Exception e) {
 					LOGGER.log(Level.WARNING, "Could not parse asset file: " + file.getAbsolutePath(), e);
 					continue;
 				}
 				
 				String trackName = trimToNonEmptyStringOrNull(asset.getName());
 				String albumName = trimToNonEmptyStringOrNull(asset.getAlbum());
 				String trackArtistName = trimToNonEmptyStringOrNull(asset.getArtist());
 				String albumArtistName = trimToNonEmptyStringOrNull(asset.getAlbumArtist());
 
 				if (albumArtistName == null && !asset.isCompilation()) { // derive missing album artist for non-compilations
 					albumArtistName = trackArtistName;
 				}
 
 				if (trackArtistName == null && albumArtistName != null) { // derive missing artist from album artist
 					LOGGER.fine("Will use album-artist for missing artist in file: " + file.getAbsolutePath());
 					trackArtistName = albumArtistName;
 				}
 
 				if (trackName == null || albumName == null && trackArtistName == null) { // unusable
 					LOGGER.info("Will skip poorly tagged asset file: " + file.getAbsolutePath());
 					continue;
 				}
 				
 				/*
 				 * determine album artist
 				 */
 				AlbumArtist albumArtist = library.getAlbumArtists().get(albumArtistName);
 				if (albumArtist == null) {
 					albumArtist = new AlbumArtist(library.getAlbumArtists().size(), albumArtistName);
 					library.getAlbumArtists().put(albumArtistName, albumArtist);
 				}
 				
 				/*
 				 * determine album
 				 */
 				Album album = albumArtist.getAlbums().get(albumName);
 				if (album == null) {
 					long albumId = assetStore.createAlbum(asset);
 					album = new Album(albumId, albumName);
 					album.setArtist(albumArtist);
 					albumArtist.getAlbums().put(albumName, album);
 					library.getAlbums().add(album);
 					if (library.getAlbums().size() % 100 == 0 && LOGGER.isLoggable(Level.FINE)) {
 						LOGGER.fine("Progress: #albums = " + library.getAlbums().size());
 					}
 				} else {
 					assetStore.addAsset(asset, album.getAlbumId());
 				}
 				
 				/*
 				 * determine track artist
 				 */
 				TrackArtist trackArtist = library.getTrackArtists().get(trackArtistName);
 				if (trackArtist == null) {
 					trackArtist = new TrackArtist(library.getTrackArtists().size(), trackArtistName);
 					library.getTrackArtists().put(trackArtistName, trackArtist);
 				}
 				trackArtist.getAlbums().add(album);
 				
 				/*
 				 * create track
 				 */
 				Track track = new Track(
 						trackName,
 						asset.getFile(),
 						asset.isArtworkAvailable(),
 						asset.isCompilation(),
 						trimToNonEmptyStringOrNull(asset.getComposer()),
 						asset.getDiscNumber(),
 						asset.getDuration(),
 						trimToNonEmptyStringOrNull(asset.getGenre()),
 						trimToNonEmptyStringOrNull(asset.getGrouping()),
 						asset.getTrackNumber(),
 						asset.getYear()
 				);
 				album.getTracks().add(track);
 				track.setAlbum(album);
 				track.setArtist(trackArtist);
 
 				/*
 				 * determine disc
 				 */
 				Integer discKey = track.getDiscNumber();
 				if (discKey == null) {
 					discKey = Integer.valueOf(0);
 				}
 				Disc disc = album.getDiscs().get(discKey);
 				if (disc == null) {
 					disc = new Disc(discKey.intValue());
 					album.getDiscs().put(discKey, disc);
 				}
 				disc.getTracks().add(track);
 
 				/*
 				 * make sure the album artist also appears as track artist???
 				 */
 //				if (albumArtistName != null && !albumArtistName.equals(trackArtistName)) {
 //					TrackArtist albumTrackArtist = library.getTrackArtists().get(albumArtistName);
 //					if (albumTrackArtist == null) {
 //						albumTrackArtist = new TrackArtist(library.getTrackArtists().size(), albumArtistName);
 //						library.getTrackArtists().put(albumArtistName, albumTrackArtist);
 //					}
 //					albumTrackArtist.getAlbums().add(album);
 //				}
 
 			}
 		}
 	}
 }
