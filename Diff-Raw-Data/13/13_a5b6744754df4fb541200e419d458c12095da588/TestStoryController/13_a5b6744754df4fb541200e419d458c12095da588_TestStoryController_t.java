 package ca.ualberta.cmput301f13t13.storyhoard.test;
 
 import java.util.ArrayList;
 import java.util.UUID;
 
 import android.test.ActivityInstrumentationTestCase2;
 import ca.ualberta.cmput301f13t13.storyhoard.controllers.StoryController;
 import ca.ualberta.cmput301f13t13.storyhoard.dataClasses.Chapter;
 import ca.ualberta.cmput301f13t13.storyhoard.dataClasses.Story;
 import ca.ualberta.cmput301f13t13.storyhoard.helpGuides.InfoActivity;
 import ca.ualberta.cmput301f13t13.storyhoard.local.ChapterManager;
 import ca.ualberta.cmput301f13t13.storyhoard.local.StoryManager;
 import ca.ualberta.cmput301f13t13.storyhoard.local.Utilities;
 import ca.ualberta.cmput301f13t13.storyhoard.serverClasses.ServerManager;
 
 public class TestStoryController extends ActivityInstrumentationTestCase2<InfoActivity> {
 
 	public TestStoryController() {
 		super(InfoActivity.class);
 	}
 	
 	/**
 	 * Tests saving /updating changes to a story or a new story in memory to 
 	 * the server
 	 */
 	public void testAddChapterPushChangesToServer() {
 		ServerManager sm = ServerManager.getInstance();
 		StoryController sc = StoryController.getInstance(getActivity());
 		sm.setTestServer();
 		
 		Story mockStory = new Story("title1", "author1", "desc1",
 				Utilities.getPhoneId(this.getActivity()));
 		sc.setCurrStoryComplete(mockStory);
 		sc.pushChangesToServer();
 		assertNotNull(sm.getById(mockStory.getId()));
 		
 		sc.addChapter(new Chapter(mockStory.getId(), "new"));
 		sc.pushChangesToServer();
 		mockStory = sm.getById(mockStory.getId());
 		sm.remove(mockStory.getId().toString());
 		assertEquals(mockStory.getChapters().size(), 1);
 	}		
 	
 	/**
 	 * Tests saving /updating changes to a story or a new story in memory to 
 	 * the database.
 	 */	
 	public void testAddChapterAndPushChangesToDb() {
 		StoryManager sm = StoryManager.getInstance(getActivity());
 		StoryController sc = StoryController.getInstance(getActivity());
 		
 		Story mockStory = new Story("title1", "author1", "desc1",
 				Utilities.getPhoneId(this.getActivity()));
 		sc.setCurrStoryComplete(mockStory);
 		sc.pushChangesToDb();
 		assertNotNull(sm.getById(mockStory.getId()));
 		
 		sc.addChapter(new Chapter(mockStory.getId(), "new"));
 		sc.pushChangesToDb();
 		mockStory = sm.getById(mockStory.getId());
 		assertEquals(mockStory.getChapters().size(), 1);
 	}		
 	
 	/**
 	 * Tests placing a reference to model in story controller so
 	 * it can be manipulated.
 	 */
 	public void testSetCurrStoryCompleteAndIncomplete() {
 		StoryManager sm = StoryManager.getInstance(getActivity());
 		ChapterManager cm = ChapterManager.getInstance(getActivity());
 		StoryController sc = StoryController.getInstance(getActivity());
 		Story mockStory = new Story("title1", "author1", "desc1",
 				Utilities.getPhoneId(this.getActivity()));;
 		sm.insert(mockStory);
 		cm.insert(new Chapter(mockStory.getId(), "hello"));
 		sc.setCurrStoryIncomplete(mockStory);
 		
 		assertEquals(sc.getCurrStory().getChapters().size(), 1);
 		sc.setCurrStoryComplete(null);
 		mockStory.getChapters().add(new Chapter(mockStory.getId(), "new"));
 		sc.setCurrStoryComplete(mockStory);
 		
 		assertEquals(sc.getCurrStory().getChapters().size(), 1);
 	}
 	
 	/**
 	 * Tests getting the current story model from story controller.
 	 */
 	public void testGetCurrStory() {
 		StoryController sc = StoryController.getInstance(getActivity());
 		Story mockStory = new Story("title1", "author1", "desc1",
 				Utilities.getPhoneId(this.getActivity()));;
 		sc.setCurrStoryComplete(mockStory);
 		assertNotNull(sc.getCurrStory());
 		
 		assertEquals(sc.getCurrStory().getChapters().size(), 1);
 	}
 
 
 	/**
 	 * Tests using the story controller to edit the story's firstChapId.
 	 * @param id
 	 */
 	public void testEditFirstChapterId(UUID id) {
 		StoryController sc = StoryController.getInstance(getActivity());
 		Story mockStory = new Story("title1", "author1", "desc1",
 				Utilities.getPhoneId(this.getActivity()));;
 		sc.setCurrStoryComplete(mockStory);
 		sc.editFirstChapterId(UUID.randomUUID());
 		assertNotNull(sc.getCurrStory().getFirstChapterId());
 	}
 	
 	public void testEditTitleAuthorDescription(String title) {
 		StoryController sc = StoryController.getInstance(getActivity());
 		
 		Story mockStory = new Story("title1", "author1", "desc1",
 				Utilities.getPhoneId(this.getActivity()));
 		sc.setCurrStoryComplete(mockStory);
 		sc.editTitle("new");
 		sc.editAuthor("new");
 		sc.editDescription("new");
		mockStory = sc.getCurrStory();
		assertFalse(mockStory.);
 	}
 	
 	
 
 	public void updateChapter(Chapter chapter) {
 		for (Chapter chap : story.getChapters()) {
 			if (chap.getId().equals(chapter.getId())) {
 				story.getChapters().remove(chap);
 				break;
 			}
 		}
 		story.getChapters().add(chapter);
 	}	
 
 }
