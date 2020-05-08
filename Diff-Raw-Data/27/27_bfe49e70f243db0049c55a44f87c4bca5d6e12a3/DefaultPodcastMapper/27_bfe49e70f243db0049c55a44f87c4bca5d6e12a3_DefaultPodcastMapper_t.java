 /* 
  * Audiolicious - Your Music Library Statistics
  * Copyright (C) 2011, Michal Huniewicz
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see http://www.m1key.me
  */
 
 package me.m1key.audiolicious.objectmapper.trackmappers;
 
 import java.util.Date;
 import java.util.Map;
 
 import javax.ejb.EJB;
 import javax.ejb.Local;
 import javax.ejb.Singleton;
 
 import me.m1key.audiolicious.commons.XmlNodeName;
 import me.m1key.audiolicious.domain.to.PodcastTo;
 import me.m1key.audiolicious.domain.to.RatingTo;
 import me.m1key.audiolicious.objectmapper.CannotMapTrackValuesException;
 import me.m1key.audiolicious.objectmapper.TrackMapper;
 import me.m1key.audiolicious.objectmapper.extractor.DataExtractor;
 
 @Singleton
 @Local({ PodcastMapper.class, TrackMapper.class })
 public class DefaultPodcastMapper extends NonAggregateTrackMapper<PodcastTo>
 		implements PodcastMapper {
 
 	@EJB
 	private DataExtractor extractor;
 
 	@Override
 	public PodcastTo map(Map<XmlNodeName, String> trackValues) {
 		if (!canMap(trackValues)) {
 			throw new CannotMapTrackValuesException(this, trackValues);
 		}
 
 		Date dateAdded = extractor.extractDate(trackValues,
 				XmlNodeName.DATE_ADDED);
 		Date dateModified = extractor.extractDate(trackValues,
 				XmlNodeName.DATE_MODIFIED);
 		String album = extractor.extractString(trackValues, XmlNodeName.ALBUM);
 		String albumArtist = extractor.extractString(trackValues,
 				XmlNodeName.ALBUM_ARTIST);
 		String artist = extractor
 				.extractString(trackValues, XmlNodeName.ARTIST);
 		String genre = extractor.extractString(trackValues, XmlNodeName.GENRE);
 		String name = extractor.extractString(trackValues, XmlNodeName.NAME);
 		RatingTo rating = extractor.extractRating(trackValues,
 				XmlNodeName.RATING);
 		int playCount = extractor.extractInt(trackValues,
 				XmlNodeName.PLAY_COUNT);
 		int year = extractor.extractInt(trackValues, XmlNodeName.YEAR);
 		boolean hasVideo = extractor.extractBoolean(trackValues,
 				XmlNodeName.HAS_VIDEO);
 		int videoHeight = extractor.extractInt(trackValues,
 				XmlNodeName.VIDEO_HEIGHT);
 		int videoWidth = extractor.extractInt(trackValues,
 				XmlNodeName.VIDEO_WIDTH);
 		boolean hd = extractor.extractBoolean(trackValues, XmlNodeName.HD);
 
 		return new PodcastTo(name, album, artist, albumArtist, year, genre,
 				dateAdded, dateModified, rating, playCount, hasVideo,
 				videoHeight, videoWidth, hd);
 	}
 
 	@Override
 	public boolean canMap(Map<XmlNodeName, String> trackValues) {
 		if (trackValues.isEmpty()) {
 			return false;
 		}
 
 		if (locationsEndsWith(trackValues, ".m4b")) {
 			return false;
 		}
 
 		if (genreEquals(trackValues, "Podcast")) {
 			return true;
 		}
 
 		if (isPodcast(trackValues)) {
 			return true;
 		}
 
 		return false;
 	}
 
 	@Override
 	protected DataExtractor getDataExtractor() {
 		return extractor;
 	}
 
 	public void setDataExtractor(DataExtractor dataExtractor) {
 		this.extractor = dataExtractor;
 	}
 
 }
