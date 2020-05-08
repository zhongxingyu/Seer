 package c301.AdventureBook.test;
 
 import java.util.ArrayList;
 import java.util.Collections;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import android.content.Context;
 import android.test.AndroidTestCase;
 import c301.AdventureBook.Controllers.LocalLibraryManager;
 import c301.AdventureBook.Controllers.StoryManager;
 import c301.AdventureBook.Models.Story;
 
 /**
  * JUnit test case for the LibraryManager controller.
  * 
  * @author tyleung
  *
  */
 public class LocalLibraryManagerTest extends AndroidTestCase {
 
 	private LocalLibraryManager lManager;
 	private StoryManager sManager;
 	private Context context;
 	private ArrayList<Story> library;
 	private Story story1;
 	private Story story2;
 	private Story story3;
 	
 	@Before
 	protected void setUp() {
 		context = getContext();
 		lManager = LocalLibraryManager.getInstance();
 		lManager.initContext(context);
 		sManager = StoryManager.getInstance();
 		sManager.initContext(context);
 		library = new ArrayList<Story>();
 		
 		// Initialize the story library
 		story1 = new Story("Titletest", "Descriptiontest", "Authortest", "Datetest", "Imagebytetest");
 		story2 = new Story("Titletest2", "Descriptiontest2", "Authortest2", "Datetest2", "Imagebytetest2");
 		story3 = new Story("Titletest3", "Descriptiontest3", "Authortest3", "Datetest3", "Imagebytetest3");
 		
 		library.add(story1);
 		library.add(story2);
 		library.add(story3);
 		
 		
 		// Save the stories
 		for (Story story : library) {
 			sManager.saveStory(story, true);
 		}
 		
 		//lManager.setCurrentLibrary(library);
 	}
 	
 	/**
 	 * Test that the correct context is retrieved. 
 	 */
 	@Test
 	public void testGetActivityContext() {
		Context activityContext = lManager.getActivityContext();
 		assertEquals(context, activityContext);
 	}
 	
 	/**
 	 * Test that the getCurrentLibrary() method returns an ArrayList containing
 	 * the correct stories.
 	 */
 	@Test
 	public void testGetCurrentLibrary() {
 		// Stories are not loaded in normal ordering
 		ArrayList<Story> someLibrary = lManager.getCurrentLibrary();
 		
 		// Check that the correct number of stories were loaded
 		assertTrue(someLibrary.size() == 3);
 		
 		Story someStory1 = someLibrary.get(0);
 		Story someStory2 = someLibrary.get(1);
 		Story someStory3 = someLibrary.get(2);
 		String someStory1Title = someStory1.getTitle();
 		String someStory2Title = someStory2.getTitle();
 		String someStory3Title = someStory3.getTitle();
 		
 		ArrayList<String> someLibraryTitles = new ArrayList<String>();
 		someLibraryTitles.add(someStory1Title);
 		someLibraryTitles.add(someStory2Title);
 		someLibraryTitles.add(someStory3Title);
 		// Sort the stories by title. Here, the ordering should be
 		// <story1, story2, story3>
 		Collections.sort(someLibraryTitles);
 		
 		assertEquals(story1.getTitle(), someLibraryTitles.get(0));
 		assertEquals(story2.getTitle(), someLibraryTitles.get(1));
 		assertEquals(story3.getTitle(), someLibraryTitles.get(2));
 	}
 	
 	/**
 	 * Test that the deleteStory() method deletes a story from library.
 	 */
 	@Test
 	public void testDeleteStory() {
 		lManager.setCurrentLibrary(library);
 		lManager.deleteStory(story1);
 		ArrayList<Story> someLibrary = lManager.getCurrentLibrary();
 		
 		// Check the array size to make sure a story got deleted
 		assertFalse(someLibrary.size() == 3);
 		assertTrue(someLibrary.size() == 2);
 		
 		// Check that someLibrary doesn't contain story1 anymore
 		assertFalse(someLibrary.contains(story1));
 	}
 	
 
 }
