 package com.cit.eugene.service.dao;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.fail;
 
 import java.util.Date;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.cit.eugene.model.Movie;
 import com.cit.eugene.model.MovieReservation;
 import com.cit.eugene.model.VideoStoreMember;
 
@ContextConfiguration("file:src/test/resources/test-context.xml")
 @RunWith(SpringJUnit4ClassRunner.class)
 @Transactional
 public class JpaMovieReservationDAOTest {
 
 	@Autowired
 	private JpaMovieReservationDAO jpaMovieReservationDAO;
 
 	@Test
 	public void testStoreOrUpdateMovieReservation() {
 		MovieReservation movieReservation = new MovieReservation();
 		movieReservation.setRented(false);
 		movieReservation.setReservationDate(new Date());
 		Movie movie = new Movie();
 		movie.setMovieID(1l);
 		movieReservation.setMovie(movie);
 		VideoStoreMember memberID = new VideoStoreMember();
 		memberID.setVideoStoreMemberID(1l);
 		movieReservation.setMemberID(memberID);
 		MovieReservation expected = jpaMovieReservationDAO.storeOrUpdateMovieReservation(movieReservation);
 		assertNotNull(expected);
 		assertEquals(expected.getMovie().getMovieID().longValue(), 1l);
 		assertEquals(expected.getMemberID().getVideoStoreMemberID().longValue(), 1l);
 	}
 
 	@Test
 	public void testDeleteMovieReservation() {
 		MovieReservation movieReservation = new MovieReservation();
 		movieReservation.setMovieReservationID(33l);
 		movieReservation.setRented(false);
 		Date d = new Date();
 		movieReservation.setReservationDate(d);
 		Movie movie = new Movie();
 		movie.setMovieID(1l);
 		movieReservation.setMovie(movie);
 		VideoStoreMember memberID = new VideoStoreMember();
 		memberID.setVideoStoreMemberID(1l);
 		movieReservation.setMemberID(memberID);
 		MovieReservation expected = jpaMovieReservationDAO.storeOrUpdateMovieReservation(movieReservation);
 		assertNotNull(expected);
 		assertEquals(d, movieReservation.getReservationDate());
 		assertEquals(33l, movieReservation.getMovieReservationID().longValue());
 		try {
 			jpaMovieReservationDAO.deleteMovieReservation(expected);
 		} catch (Exception e) {
 			e.printStackTrace();
 			fail();
 		}
 	}
 
 }
