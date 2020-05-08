 package com.cit.eugene.service.dao;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.cit.eugene.model.Movie;
 
@ContextConfiguration("file:src/test/resources/test-context.xml")
 @RunWith(SpringJUnit4ClassRunner.class)
 @Transactional
 public class JpaMovieDAOTest {
 
 	@Autowired
 	private JpaMovieDAO jpaMovieDAO;
 	
 	@Before
 	public void setUp() throws Exception {
 	}
 
 	@Test
 	public void testGetAllMovies() {
 		List<Movie> l = jpaMovieDAO.getAllMovies();
 		assertEquals(25, l.size());
 	}
 
 	@Test
 	public void testGetMovieListingByGenreID() {
 		List<Movie> l1  = jpaMovieDAO.getMovieListingByGenreID(2l);
 		assertEquals(11, l1.size());
 		List<Movie> l2  = jpaMovieDAO.getMovieListingByGenreID(5l);
 		assertEquals(5, l2.size());
 		List<Movie> l3  = jpaMovieDAO.getMovieListingByGenreID(6l);
 		assertEquals(15, l3.size());
 		List<Movie> l4  = jpaMovieDAO.getMovieListingByGenreID(9l);
 		assertEquals(3, l4.size());
 	}
 
 	@Test
 	public void testGetMovieByID() {
 		Movie m = jpaMovieDAO.getMovieByID(1l);
 		assertNotNull(m);
 		assertEquals("13 Assassins", m.getTitle());
 		assertEquals("13_Assassins.jpg", m.getPosterFileName());
 		assertEquals("A group of assassins come together for a suicide mission to kill an evil lord.", m.getSummary());
 		assertEquals(2010, m.getYear().intValue());
 		assertEquals("Kji Yakusho, Takayuki Yamada, Ysuke Iseya", m.getActorsDisplay());
 		assertEquals("Takashi Miike", m.getDirectorsDisplay());
 		assertEquals("", m.getProducersDisplay());
 		assertEquals("", m.getStudioDisplay());
 		assertEquals(false, m.isRented());
 		assertEquals("3.99", m.getPrice().toString());
 		assertEquals(2, m.getRating().intValue());
 		assertEquals(3, m.getGenres().size());
 	}
 
 }
