 package se.chalmers.watchmetest.model;
 
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.Map;
 
 import junit.framework.TestCase;
 
 import se.chalmers.watchme.model.Movie;
 import se.chalmers.watchme.model.Tag;
 
 /**
  * This class tests methods in se.chalmers.watchme.model.Movie
  * @author mattiashenriksson
  *
  */
 
 public class MovieTest extends TestCase {
 	
 	private Movie batman;
 	
 	public void setUp() throws Exception {
 		super.setUp();
		this.batman = new Movie("batman");
 	}
 	
 	public void testAddTag() {
		Tag action = new Tag("action");
 		batman.addTag(action);
 		assertTrue(batman.getTags().contains(action));
 	}
 	
 	public void testRemoveTag() {
		Tag action = new Tag("action");
 		batman.addTag(action);
 		batman.removeTag(action);
 		assertFalse(batman.getTags().contains(action));
 	}
 	
 	public void testGetTags() {
		Tag action = new Tag("action");
 		batman.addTag(action);
 		assertTrue(batman.getTags().size() == 1);
 	}
 	
 	public void testGetId() {
 		batman.setId(1);
 		assertTrue(batman.getId() == 1);
 	}
 	
 	public void testGetTitle() {
		assertTrue(batman.getTitle().equals("batman"));
 	}
 	
 	public void testGetNote() {
 		Movie superman = new Movie("superman", Calendar.getInstance(), 1, "note");
 		assertTrue(superman.getNote().equals("note"));
 	}
 	
 	public void testSetNote() {
 		batman.setNote("note");
 		assertTrue(batman.getNote().equals("note"));
 	}
 	
 	public void testGetRating() {
 		Movie superman = new Movie("superman", Calendar.getInstance(), 5, "note");
 		assertTrue(superman.getRating() == 5);
 	}
 	
 	public void testSetRating() {
 		batman.setRating(8);
 		assertTrue(batman.getRating() == 8);
 	}
 	
 	public void testHasApiID() {
 		batman.setApiID(10);
 		assertTrue(batman.hasApiIDSet());
 	}
 	
 	/**
 	 * Movie should not have API id set on init
 	 */
 	public void testHasNotApiID() {
 		assertFalse(batman.hasApiIDSet());
 	}
 	
 	public void testMoviePosterSize() {
 		batman.setPosterURL("someimage.png", Movie.PosterSize.MID);
 		
 		assertEquals("someimage.png", batman.getPosterURL(Movie.PosterSize.MID));
 	}
 	
 	public void testNonExistingPosterSize() {
 		assertNull(batman.getPosterURL(Movie.PosterSize.MID));
 		assertNull(batman.getPosterURL(Movie.PosterSize.THUMB));
 	}
 	
 	public void testGetAllPosterSizes() {
 		batman.setPosterURL("someimage1.png", Movie.PosterSize.MID);
 		batman.setPosterURL("someimage2.png", Movie.PosterSize.THUMB);
 		
 		Map<Movie.PosterSize, String> posters = new HashMap<Movie.PosterSize, String>();
 		posters.put(Movie.PosterSize.MID, "someimage1.png");
 		posters.put(Movie.PosterSize.THUMB, "someimage2.png");
 		
 		assertEquals(posters, batman.getPosterURLs());
 	}
 	
 	public void testEquals() {
 		Movie compareObject = null; 
 		assertFalse(batman.equals(compareObject));
 		
		compareObject = new ActionMovie("batman");
 		assertFalse(batman.equals(compareObject));
 		
 		compareObject = new Movie("spiderman");
 		assertFalse(batman.equals(compareObject));
 
		compareObject = new Movie("batman");
 		assertTrue(batman.equals(compareObject));
 	}
 	
 	public void testHashCode() {
		Movie otherMovie = new Movie("batman");
 		Movie notSameMovie = new Movie("spiderman");
 		
 		assertTrue(batman.equals(otherMovie));
 		assertEquals(batman.hashCode(), otherMovie.hashCode());
 		
 		assertFalse(batman.equals(notSameMovie));
 		assertFalse(batman.hashCode() == notSameMovie.hashCode());
 	}
 	
 	/*
 	 * Test that Movie is not equal to possible subclasses
 	 */
 	private class ActionMovie extends Movie {
 		public ActionMovie(String title) {
 			super(title);
 		}			
 	}
 }
