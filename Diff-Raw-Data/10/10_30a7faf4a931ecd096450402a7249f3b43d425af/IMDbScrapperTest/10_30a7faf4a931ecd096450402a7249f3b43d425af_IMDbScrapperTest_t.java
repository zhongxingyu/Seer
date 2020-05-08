 /*
  * movie-renamer-core
  * Copyright (C) 2012 Nicolas Magré
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package fr.free.movierenamer.scrapper.impl;
 
 import java.util.List;
 import java.util.Locale;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 import fr.free.movierenamer.info.CastingInfo;
 import fr.free.movierenamer.info.ImageInfo;
 import fr.free.movierenamer.info.ImageInfo.ImageCategoryProperty;
 import fr.free.movierenamer.info.MovieInfo;
 import fr.free.movierenamer.scrapper.MovieScrapperTest;
 import fr.free.movierenamer.searchinfo.Movie;
 
 /**
  * Class IMDbScrapperTest
  * 
  * @author Simon QUÉMÉNEUR
  */
 public class IMDbScrapperTest extends MovieScrapperTest {
   private IMDbScrapper imdb = null;
 
   @Override
   public void init() {
     imdb = new IMDbScrapper();
   }
 
   @Override
   public void search() throws Exception {
     imdb.setLocale(Locale.FRENCH);
     List<Movie> results = imdb.search("il était une fois dans l'ouest");
 
     Movie movie = results.get(0);
 
     Assert.assertEquals("Il était une fois dans l'ouest", movie.getName());
    Assert.assertEquals("http://ia.media-imdb.com/images/M/MV5BMTgwMzU1MDEyMl5BMl5BanBnXkFtZTcwNDc5Mzg3OA@@._V1._SY70_SX100.jpg", movie.getURL().toExternalForm());
     Assert.assertEquals(1968, movie.getYear());
     Assert.assertEquals(64116, movie.getImdbId());
     Assert.assertEquals(64116, movie.getMediaId());
   }
   
   @Test
   public void searchRedirect() throws Exception {
     imdb.setLocale(Locale.FRENCH);
     List<Movie> results = imdb.search("le pont de la rivière kwai");
 
     Movie movie = results.get(0);
 
     Assert.assertEquals("Le pont de la rivière Kwai", movie.getName());
     Assert.assertEquals("http://ia.media-imdb.com/images/M/MV5BMTc2NzA0NTEwNF5BMl5BanBnXkFtZTcwMzA0MTk3OA@@._V1._SY70_SX100.jpg", movie.getURL().toExternalForm());
     Assert.assertEquals(1957, movie.getYear());
     Assert.assertEquals(50212, movie.getImdbId());
     Assert.assertEquals(50212, movie.getMediaId());
     
   }
 
   @Override
   public void getMovieInfo() throws Exception {
     imdb.setLocale(Locale.ITALIAN);
     MovieInfo movie = imdb.getInfo(new Movie(64116, null, null, -1, -1));
 
     Assert.assertEquals("C'era una volta il West", movie.getTitle());
     Assert.assertEquals(Integer.valueOf(175), Integer.valueOf(movie.getRuntime()));
   }
 
   @Override
   public void getCasting() throws Exception {
     boolean success = false;
     List<CastingInfo> cast = imdb.getCasting(new Movie(64116, null, null, -1, -1));
     for(CastingInfo info : cast) {
       if(info.isDirector()) {
         success = "Sergio Leone".equals(info.getName());
       }
     }
     
     Assert.assertTrue(success);
   };
 
   @Override
   public void getImages() throws Exception {
     List<ImageInfo> images = imdb.getImages(new Movie(64116, null, null, -1, -1));
     
     Assert.assertEquals(ImageCategoryProperty.unknown, images.get(0).getCategory());
     Assert.assertEquals("http://ia.media-imdb.com/images/M/MV5BMTM2NTQ2MzkwNV5BMl5BanBnXkFtZTcwMjU1ODIwNw@@._V1._SY214_SX314_.jpg", images.get(1).getHref().toExternalForm());
   }
 }
