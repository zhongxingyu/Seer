 
 package ualberta.g12.adventurecreator.test;
 
 import android.test.ActivityInstrumentationTestCase2;
 
 import ualberta.g12.adventurecreator.controllers.FragmentController;
 import ualberta.g12.adventurecreator.controllers.StoryListController;
 import ualberta.g12.adventurecreator.data.Fragment;
 import ualberta.g12.adventurecreator.data.Story;
 import ualberta.g12.adventurecreator.data.StoryList;
 import ualberta.g12.adventurecreator.views.MainActivity;
 
 import java.util.List;
 
 /**
  * These test cases test that the StoryList controller behave to meet the needed requirements.
  * Function that will be tested are: addStory, setStory, and createBlankStory().
  * The storyList controller is made of getteres and setteres that control all variables
  * From the storyList.
  */
 
 public class StoryListControllerTestCases extends ActivityInstrumentationTestCase2<MainActivity> {
     private FragmentController fc;
     private Fragment sf;
 
     public StoryListControllerTestCases(){
         super(MainActivity.class);
     }
     
     public StoryListControllerTestCases(Class<MainActivity> activityClass) {
         super(MainActivity.class);
     }
 
     private StoryListController slc;
     private StoryList sl;
     private List<Story> stories;
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         sl = new StoryList();
         slc = new StoryListController(sl, null);
     }
 
     public void testSetUp() {
         assertNotNull("StoryList was null.", sl);
         assertNotNull("StoryListController was null", sl);
     }
 
     /**
      * Creates a new story to the story list
      */
     public void testAddStory() {
 
         // add a story to our storyList
         sl = new StoryList();
         slc = new StoryListController(sl, null);
         int oldSize = sl.getAllStories().size();
         Story s = new Story("Book", "Dan Dude");
         slc.addStory(s);
 
         assertTrue("StoryList size didn't increase", oldSize < sl.getAllStories().size());
 
     }
 
 
     /**
      * tests the getStoryAtPos function to test if story details can be retrieved 
      */
     public void testGetStoryWithObject() {
 
         // add a story to our storyList
         sl = new StoryList();
         slc = new StoryListController(sl, null);
         Story s = new Story("Book", "Dan Dude");
         slc.addStory(s);
         Story s2 = sl.getStoryAtPos(0);
         assertTrue("Stories don't have same titles", s.getTitle().equals(s2.getTitle()));
         assertTrue("Stories don't have same authors", s.getAuthor().equals(s2.getAuthor()));
     }
 
     /**
      * tests the getTitle function, to see if a title is received
      */
     public void testGetStoryWithTitle() {
 
         // add a story to our storyList
         sl = new StoryList();
         slc = new StoryListController(sl, null);
         Story s = new Story("Book", "Dan Dude");
         slc.addStory(s);
         String s2 = sl.getStoryAtPos(0).getTitle();
         String s3 = sl.getStoryAtPos(0).getAuthor();
         assertTrue("Stories don't have same titles", s.getTitle().equals(s2));
         assertTrue("Stories don't have same authors", s.getAuthor().equals(s3));
     }
 
     /**
      * tests to see whether a story set in a new position takes its place in the story list
      */
     public void testSetStory() {
 
         // add a story to our storyList
         sl = new StoryList();
         slc = new StoryListController(sl, null);
         Story s = new Story("Book", "Dan Dude");
         slc.addStory(s);
         String author = sl.getStoryAtPos(0).getAuthor();
         assertTrue(sl.getStoryAtPos(0).getAuthor().equals(author));
         Story s2 = new Story("Bok", "Dude Dan");
         slc.setStory(s2, 0);
         assertTrue(sl.getStoryAtPos(0).getAuthor().equals(author));
 
     }
 
     /**
      * tests the getStoryAtPos function which should return a certain story from a 
      * specific position in the storylist
      */
     public void testGetStoryAtPos() {
         // add a story to our storyList
         sl = new StoryList();
         slc = new StoryListController(sl, null);
         Story s = new Story("Book", "Dan Dude");
         slc.addStory(s);;
         String author = sl.getStoryAtPos(0).getAuthor();
         assertTrue(sl.getStoryAtPos(0).getAuthor().equals(author));
         Story s2 = sl.getStoryAtPos(0);
         assertTrue(s2.equals(s));
 
     }
 
     /**
      * Tests whether a blank story (story with no parameters) can be made
      */
     public void testCreateBlankStory() {
 
         // add a story to our storyList
         sl = new StoryList();
         slc = new StoryListController(sl, null);
         assertTrue(sl.getAllStories().size() == 0);
         slc.createBlankStory();
         assertTrue(sl.getAllStories().size() == 1);
 
     }
 
     /**
      * test if the initialized screen with no stories does in fact return no stories
      */
     public void testGetInitialListOfStories() {
         sl = new StoryList();
         slc = new StoryListController(sl, null);
         assertNotNull("Inital story list is null", sl);
         assertNotNull("Initial story list controller is null", slc);
         assertNotNull(sl.getAllStories());
         assertTrue("Initial list of stories wasn't 0", sl.getAllStories().size() == 0);
     }
 
     /**
      * tests if new stories can be added to the story list with the addStory function 
      **/
     public void testAddStoriesToList() {
         Story s = new Story("A Dance With Dragons", "George R.R. Martin");
         int oldSize = sl.getAllStories().size();
         slc.addStory(s);
         stories = sl.getAllStories();
         assertTrue("Size of list of stories didn't increase when we added one",
                 oldSize < stories.size());
         oldSize = stories.size();
         s = new Story("A Game of Thrones", "George R.R. Martin");
         slc.addStory(s);
         stories = sl.getAllStories();
         assertTrue("Size of list of stories didn't increase when we added a story",
                 oldSize < stories.size());
     }
 
     /**
      * tests if by using the setStory function, more stories can be added to the storylist
      */
     public void testSetStory2() {
         Story s = new Story("The Winds of Winter", "George R.R. Martin");
         Story s1 = new Story("A Storm of Swords", "George R.R. Martin");
         Story s2 = new Story("A Clash of Kings", "George R.R. Martin");
 
         slc.addStory(s);
         slc.addStory(s1);
         slc.addStory(s2);
 
         int size = sl.getAllStories().size();
         int index = size - 1;
 
         assertTrue("We have zero stories in our story list", size > 0);
 
         Story s3 = new Story("Book!", "Author??");
         slc.setStory(s3, index);
 
         assertTrue("The name of the book didn't change", sl.getAllStories().get(index)
                 .getTitle().equals(s3.getTitle()));
         assertTrue("The author of the book didn't change", sl.getAllStories().get(index)
                 .getAuthor().equals(s3.getAuthor()));
     }
 
 }
