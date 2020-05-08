 package story.book.test;
 
 import java.util.Date;
 
 import org.junit.Test;
 
 import story.book.model.StoryInfo;
 import story.book.view.Dashboard;
 import android.test.ActivityInstrumentationTestCase2;
 
 public class StoryInfoTest extends ActivityInstrumentationTestCase2
 <story.book.view.Dashboard> {
 
 	private StoryInfo storyInfo;
 	
 	public StoryInfoTest() {
 		super(Dashboard.class);
 		storyInfo = new StoryInfo();
 	}
 	
 	@Test
 	public void testCreation() {
 		assertNotNull(storyInfo);
 	}
 	
 	@Test
 	public void testGetAuthor() {
 		assertEquals(storyInfo.getAuthor(), "");
 	}
 	
 	@Test
 	public void testSetAuthor() {
 		storyInfo.setAuthor("test author");
 		assertEquals(storyInfo.getAuthor(), "test author");
 	}
 	
 	@Test
 	public void testGetTitle() {
 		assertEquals(storyInfo.getTitle(), "");
 	}
 	
 	@Test
 	public void testSetTitle() {
 		storyInfo.setTitle("test title");
 		assertEquals(storyInfo.getTitle(), "test title");
 	}
 
 	@Test
 	public void testGetGenre() {
 		assertEquals(storyInfo.getGenre(), "");
 	}
 	
 	@Test
 	public void testSetGenre() {
 		storyInfo.setGenre("test genre");
 		assertEquals(storyInfo.getGenre(), "test genre");
 	}
 	
 	@Test
 	public void testGetSynopsis() {
 		assertEquals(storyInfo.getSynopsis(), "");
 	}
 	
 	@Test
 	public void testSetSynopsis() {
 		storyInfo.setSynopsis("test synopsis");
 		assertEquals(storyInfo.getSynopsis(), "test synopsis");
 	}
 	
 	@Test
 	public void testGetPublishDate() {
		assertNull(storyInfo.getPublishDateString());
 	}
 	
 	@Test
 	public void testSetPublishDate() {
 		storyInfo.setPublishDate(new Date());
 		assertNotNull(storyInfo.getPublishDate());
 	}
 	
 	@Test
 	public void testGetStartingFragmentID() {
 		assertEquals(storyInfo.getStartingFragmentID(), -1);
 	}
 	
 	@Test
 	public void testSetStartingFragmentID() {
 		storyInfo.setStartingFragmentID(1337);
 		assertEquals(storyInfo.getStartingFragmentID(), 1337);
 	}
 	
 	@Test
 	public void testGetSID() {
 		assertEquals(storyInfo.getSID(), 0);
 	}
 	
 	@Test
 	public void testSetSID() {
 		storyInfo.setSID(1337);
 		assertEquals(storyInfo.getSID(), 1337);
 	}
 	
 	@Test
 	public void testGetPublishState() {
 		assertEquals(storyInfo.getPublishState(), StoryInfo.PublishState.UNPUBLISHED);
 	}
 	
 	@Test
 	public void testSetPublishState() {
 		storyInfo.setPublishState(StoryInfo.PublishState.PUBLISHED);
 		assertEquals(storyInfo.getPublishState(), StoryInfo.PublishState.PUBLISHED);
 	}
 	
 }
