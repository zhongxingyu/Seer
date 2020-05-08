 package uw.cse403.minion.test;
 
 import junit.framework.Assert;
 
 import com.jayway.android.robotium.solo.Solo;
 
 import uw.cse403.minion.GroupCreateActivity;
 import uw.cse403.minion.GroupsActivity;
 import uw.cse403.minion.SaveSharedPreference;
 import uw.cse403.minion.ViewInvitesActivity;
 import android.test.ActivityInstrumentationTestCase2;
 import android.widget.ListView;
 
 public class GroupsActivityTest extends
 	ActivityInstrumentationTestCase2<GroupsActivity> {
 	private Solo solo;
 	private static final String VALID_USERNAME = "UseForTestingOnly";
 	
 	public GroupsActivityTest(){
 		super(GroupsActivity.class);
 	}
 	
 	/**
 	 * setup() instantiates Solo and stores the LoginActivity.
 	 * loginActivity is later used to clear out the "Remember me" settings
 	 * from the application preferences to ensure clean state for each test.
 	 */
 	@Override
 	protected void setUp() throws Exception {
 		solo = new Solo(getInstrumentation(), getActivity());
 		SaveSharedPreference.setUserName(getActivity(), VALID_USERNAME);		
 	}
 	
 	public void testProperButtonsDisplayed(){
 		Assert.assertTrue(solo.searchButton("Create New Group"));
 		Assert.assertTrue(solo.searchButton("Pending Invites"));
 		solo.finishOpenedActivities();
 	}
 	
 	public void testCreateButtonGoesToRightActivity(){
 		solo.clickOnButton("Create New Group");
 		solo.assertCurrentActivity("Create Group Success", GroupCreateActivity.class);
 		solo.finishOpenedActivities();
 	}
 	
 	public void testInvitesButtonGoesToRightActivity(){
 		solo.clickOnButton("Pending Invites");
 		solo.assertCurrentActivity("Invites Success", ViewInvitesActivity.class);
 		solo.finishOpenedActivities();
 	}
 	
	public void testRightNubmerOfGroupsDisplayed(){
 		ListView groups = solo.getView(ListView.class, 0);
 		Assert.assertEquals(1, groups.getAdapter().getCount());
 		solo.finishOpenedActivities();
 	}
 	
 	public void testRightGroupIsDisplayed(){
 		Assert.assertTrue(solo.searchText("GroupForTestingOnly"));
 		Assert.assertTrue(solo.searchText("UseForTestingOnly"));
 		solo.finishOpenedActivities();
 	}
 }
