 /*
  *      Copyright (c) 2004-2011 YAMJ Members
  *      http://code.google.com/p/moviejukebox/people/list 
  *  
  *      Web: http://code.google.com/p/moviejukebox/
  *  
  *      This software is licensed under a Creative Commons License
  *      See this page: http://code.google.com/p/moviejukebox/wiki/License
  *  
  *      For any reuse or distribution, you must make clear to others the 
  *      license terms of this work.  
  */
 package com.moviejukebox.rottentomatoes;
 
 import java.util.HashSet;
 
 import junit.framework.TestCase;
 
 import com.moviejukebox.rottentomatoes.RottenTomatoes;
 import com.moviejukebox.rottentomatoes.model.Cast;
 import com.moviejukebox.rottentomatoes.model.Link;
 import com.moviejukebox.rottentomatoes.model.Movie;
 import com.moviejukebox.rottentomatoes.model.Review;
 
 public class Test extends TestCase {
     public RottenTomatoes rt = new RottenTomatoes("rnt8xak564a8sxkts5xkqj5z");
 
     public Test(String name) {
         super(name);
     }
     
     protected void setUp() throws Exception {
         super.setUp();
     }
 
     protected void tearDown() throws Exception {
         super.tearDown();
     }
 
     public void testGetLists() {
         HashSet<Link> response = rt.getLists();
         assertEquals(2, response.size());
     }
     
     public void testMoviesSearch() {
         HashSet<Movie> response = rt.moviesSearch("Blade Runner");
         assertEquals(3, response.size());
     }
 
     public void NOtestListsDirectory() {
         HashSet<Link> response = rt.listsDirectory();
         assertEquals(2, response.size());
     }
     
     public void testMovieListsDirectory() {
         HashSet<Link> response = rt.movieListsDirectory();
         assertEquals(4, response.size());
     }
     
     public void testDvdListsDirectory() {
         HashSet<Link> response = rt.dvdListsDirectory();
        assertTrue(response.size() > 0);
     }
 
     public void testOpeningMovies() {
         int maxMovies = 10;
         HashSet<Movie> response = rt.openingMovies(maxMovies);
         assertTrue(response.size() <= maxMovies);
     }
 
     public void testUpcomingMovies() {
         int maxMovies = 10;
         HashSet<Movie> response = rt.upcomingMovies(maxMovies);
         assertTrue((response.size() > 0) && (response.size() <= maxMovies));
     }
 
     public void testNewReleaseDvds() {
         int maxMovies = 10;
         HashSet<Movie> response = rt.newReleaseDvds(maxMovies);
         assertTrue((response.size() > 0) && (response.size() <= maxMovies));
     }
 
     public void testMovieInfo() {
         Movie movie = rt.movieInfo(12886);
         assertEquals("Blade Runner", movie.getTitle());
     }
     
     public void testMovieCast() {
         HashSet<Cast> castList = rt.movieCast(12886);
         assertTrue(castList.size() > 0);
     }
 
     public void testMovieReviews() {
         HashSet<Review> reviewList = rt.movieReviews(12886, Review.ReviewType.all);
         assertTrue(reviewList.size() > 0);
     }
    
 }
