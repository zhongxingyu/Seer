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
 
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.InputStream;
 import java.util.logging.Logger;
 
 import javax.imageio.ImageIO;
 
 import com.mpatric.mp3agic.ID3v1;
 import com.mpatric.mp3agic.ID3v2;
 import com.mpatric.mp3agic.Mp3File;
 
 /**
  * MP3 asset parser.
  */
 public class MP3AssetParser implements AssetParser {
	static final Logger LOGGER = Logger.getLogger(MP3AssetParser.class.getName());
 
 	@Override
 	public boolean isAssetFile(File file) {
 		return file.getName().toLowerCase().endsWith(".mp3");
 	}
 
 	@Override
 	public Asset parse(File file) throws Exception {
 		Asset asset = new Asset(file);
 		Mp3File mp3file = new Mp3File(file.getAbsolutePath());
 		asset.setDuration(mp3file.getLengthInSeconds() > 0 ? (int) mp3file.getLengthInSeconds() : null);
 		if (mp3file.hasId3v2Tag()) {
 			ID3v2 info = mp3file.getId3v2Tag();
 			asset.setAlbum(info.getAlbum());
 			asset.setAlbumArtist(info.getAlbumArtist());
 			asset.setArtist(info.getArtist());
 			asset.setArtworkAvailable(info.getAlbumImage() != null);
 			asset.setCompilation(info.isCompilation());
 			asset.setComposer(info.getComposer());
			if (info.getPartOfSet() != null && info.getPartOfSet().trim().length() > 0) {
 				String string = info.getPartOfSet().trim();
 				int index = string.indexOf('/');
 				if (index > 0) {
 					string = string.substring(0, index);
 				}
 				try {
 					asset.setDiscNumber(Integer.valueOf(string));
 				} catch (NumberFormatException e) {
					LOGGER.warning("Could not parse disc number: " + info.getPartOfSet() + " (" + file + ")");
 				}
 			}
 			asset.setGenre(info.getGenreDescription());
 			asset.setName(info.getTitle());
 			if (info.getTrack() != null && info.getTrack().trim().length() > 0) {
 				String string = info.getTrack().trim();
 				int index = string.indexOf('/');
 				if (index > 0) {
 					string = string.substring(0, index);
 				}
 				try {
 					asset.setTrackNumber(Integer.valueOf(string));
 				} catch (NumberFormatException e) {
					LOGGER.warning("Could not parse track number: " + info.getTrack() + " (" + file + ")");
 				}
 			}
 			if (info.getYear() != null && info.getYear().trim().length() > 0) {
 				try {
 					asset.setYear(Integer.valueOf(info.getYear()));
 				} catch (NumberFormatException e) {
					LOGGER.warning("Could not parse year: " + info.getYear() + " (" + file + ")");
 				}
 			}
 		} else if (mp3file.hasId3v1Tag()) {
 			ID3v1 info = mp3file.getId3v1Tag();
 			asset.setAlbum(info.getAlbum());
 			asset.setAlbumArtist(null);
 			asset.setArtist(info.getArtist());
 			asset.setArtworkAvailable(false);
 			asset.setCompilation(false);
 			asset.setComposer(null);
 			asset.setDiscNumber(null);
 			asset.setGenre(info.getGenreDescription());
 			asset.setName(info.getTitle());
 			asset.setTrackNumber(null);
 			if (info.getYear() != null && info.getYear().trim().length() > 0) {
 				try {
 					asset.setYear(Integer.valueOf(info.getYear()));
 				} catch (NumberFormatException e) {
					LOGGER.warning("Could not parse year: " + info.getYear() + " (" + file + ")");
 				}
 			}
 		}
 		return asset;
 	}
 
 	@Override
 	public BufferedImage extractArtwork(File file) throws Exception {
 		Mp3File mp3file = new Mp3File(file.getAbsolutePath());
 		if (mp3file.hasId3v2Tag()) {
 			byte[] imageData = mp3file.getId3v2Tag().getAlbumImage();
 			if (imageData != null) {
 				try (InputStream input = new ByteArrayInputStream(imageData)) {
 					return ImageIO.read(input);
 				}
 			}
 		}
 		return null;
 	}
 }
