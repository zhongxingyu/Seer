 package com.cit.eugene.service.dao;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 
 import java.util.List;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.cit.eugene.model.Genre;
 
@ContextConfiguration("test-context.xml")
 @RunWith(SpringJUnit4ClassRunner.class)
 public class JpaGenreDAOTest {
 	
 	@Autowired
 	private JpaGenreDAO jpaGenre;
 
 	@Test
 	@Transactional
 	public void testGetAllGenres() {
 		List<Genre> l = jpaGenre.getAllGenres();
 		assertEquals(11, l.size());
 		Genre g = l.get(1);
 		assertEquals(2, g.getGenreID().longValue());
 		assertEquals("Adventure", g.getGenreName());
 		assertEquals(11, g.getMovies().size());
 		assertNotNull(g.getMovies());
 	}
 }
