 package com.ccbilleu.cinema.service;
 
 import java.util.Collection;
 import java.util.Set;
 
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.annotation.Rollback;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.ccbilleu.cinema.db.exception.DaoException;
 import com.ccbilleu.cinema.db.model.Cinema;
 import com.ccbilleu.cinema.db.model.Movie;
 import com.ccbilleu.cinema.db.model.MovieShow;
 import com.ccbilleu.cinema.db.model.Seat;
 import com.ccbilleu.cinema.db.model.Theatre;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations="classpath:application-context.xml")
 public class PersistenceServiceTest {
 
 	@Autowired
 	private CinemaService cinemaService;
 	
 	@Autowired
 	private TestService testService;
 	
 	@Before
 	public void init() {
 		//testService.setupTestData();
 	}
 	
 	@Test
 	@Transactional
 	@Rollback(false)
 	public void testListCinemas() throws DaoException {
 		Cinema cinema = cinemaService.getDefaultCinema();
 		Assert.assertNotNull(cinema);
 		Assert.assertEquals("Arena Cinema Max", cinema.getName());
 		
 		Set<Movie> movies = cinema.getMovies();
 		Assert.assertNotNull(movies);
 		Assert.assertFalse(movies.isEmpty());
 		
 		Movie movie = movies.iterator().next();
 		Collection<MovieShow> movieShows = movie.getMovieShows();
 		Assert.assertNotNull(movieShows);
 		Assert.assertFalse(movieShows.isEmpty());
 		
 		MovieShow movieShow = movieShows.iterator().next();
 		
 		Theatre theatre = movieShow.getTheatre();
 		Assert.assertNotNull(theatre);
 		
 		Set<Seat> seats = movieShow.getSeats();
 		Assert.assertNotNull(seats);
 		Assert.assertFalse(seats.isEmpty());
 	}
 	
 }
