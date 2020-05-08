 package org.atlasapi.remotesite.space;
 
 import org.atlasapi.genres.AtlasGenre;
 import org.atlasapi.genres.GenreMap;
 
 public class TheSpaceGenreMap extends GenreMap {
 
     static {
         genres.put("http://thespace.org/by/genre/dance", AtlasGenre.MUSIC);
         genres.put("http://thespace.org/by/genre/music", AtlasGenre.MUSIC);
         genres.put("http://thespace.org/by/genre/literature-spoken-word", AtlasGenre.FACTUAL);
        genres.put("http://thespace.org/by/genre/performance-festival", AtlasGenre.FACTUAL);
         genres.put("http://thespace.org/by/genre/theatre", AtlasGenre.FACTUAL);
        genres.put("http://thespace.org/by/genre/visual-media-arts", AtlasGenre.FACTUAL);
         genres.put("http://thespace.org/by/genre/film", AtlasGenre.FILM);
     }
 }
