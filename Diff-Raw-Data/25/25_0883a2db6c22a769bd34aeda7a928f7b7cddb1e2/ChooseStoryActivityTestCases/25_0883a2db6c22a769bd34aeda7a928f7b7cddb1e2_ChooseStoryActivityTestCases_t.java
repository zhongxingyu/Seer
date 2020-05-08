 /* Covers test cases for use cases:
  * 2, 16
  * 
  * http://stackoverflow.com/questions/10845937/how-to-do-junit-testing-in-android
  * Referenced Oct 23, 2013 for general format of Test class
  * 
  */
 
 import ca.ualberta.cs.livojevi_notes.R;
 import ca.ualberta.cs.livojevi_notes.TitleAndDescriptionAdapter;
 import android.test.ActivityInstrumentationTestCase2;
 import android.view.MenuItem;
 import android.widget.ListView;
 import android.widget.PopupMenu;
 import android.widget.TextView;
 
 public class ChooseStoryActivityTestCases extends ActivityInstrumentationTestCase2<ChooseStoryActivity> {
 	//declare activity and widgets
 	private ChooseStoryActivity myChooseStoryActivity;
 	private TextView myView;
 	private ListView availableStoryList;
 	private PopupMenu storyListPopupMenu;
 
 	//Constructor
 	public ChooseStoryActivityTestCases(){
 		super("ualberta.g12.adventurecreator.ChooseStoryActivity", ChooseStoryActivity.class);
 	}	
 
 	//initializes things you want to be set for all tests (not necessary)
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 	}
 
 	/* Test Cases - all start with "test" */
 
 	//Use Case 2, test 1/3
 	public void testBrowseStoryList() {
 		addMultiStory();
 
 		//starts the activity
		myChooseStoryActivity = this.getActivity();
		myView = (TextView) myChooseStoryActivity.findViewById(ualberta.g12.adventurecreator.ChooseStoryActivity.R.id.textviewID);
		
 
 		assertTrue(scrollable(mView.getText()));
 	}
 
 	//Use Case 2, test 2/3
 	public void testBrowseNoScrollList() {
 		addOneStory();
 
 		//starts the activity
		myChooseStoryActivity = this.getActivity();
		myView = (TextView) myChooseStoryActivity.findViewById(ualberta.g12.adventurecreator.ChooseStoryActivity.R.id.textviewID);
		
 
 		assertFalse(scrollable(mView.getText()));
 	}
 
 	//Use Case 2, test 3/3
 	public void testBrowseEmptyStoryList() {
 		//starts the activity
		myChooseStoryActivity = this.getActivity();
		myView = (TextView) myChooseStoryActivity.findViewById(ualberta.g12.adventurecreator.ChooseStoryActivity.R.id.textviewID);
		
 		String Story = myView.getText();
 		assertEquals(Story, null);
 	}
 
 	//Use Case 16, test 1/1
 	public void testDowloadOtherUserStory() {
 		Story otherUserStory = new Story();
 		otherUserStory.setAuthor("OtherUser");
 
 		//uploads/publishes story
 		addStory(otherUserStory);
 
 		//starts the activity (which will automatically find all 
 		//available stories and load them into the availableStoryList)
		myChooseStoryActivity = this.getActivity();
		availableStoryList = (ListView) myChooseStoryActivity.findViewById(ualberta.g12.adventurecreator.ChooseStoryActivity.R.id.storyListID);
		storyListPopupMenu = (PopupMenu) myChooseStoryActivity.findViewById(ualberta.g12.adventurecreator.ChooseStoryActivity.R.id.storyListPoupMenuID);

 
 		//selects first (and only) story
 		availableStoryList.setSelection(0);
 		
 		//causes popupMenu to open for selection
 		availableStoryList.performLongClick();
 
 		//gets download option if available
 		MenuItem downloadOption = storyListPopupMenu.getMenu().getItem(2);
 
 		//asserts user can Download the story
 		assertTrue(downloadOption.getTitle().equals("Download"));
 	}
 
 }
