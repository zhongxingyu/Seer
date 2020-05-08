 package ch.hsr.bieridee.android.test;
 
 import android.test.ActivityInstrumentationTestCase2;
 import ch.hsr.bieridee.android.activities.BreweryListActivity;
 import ch.hsr.bieridee.android.config.Auth;
 import com.jayway.android.robotium.solo.Solo;
 
 public class BreweryListActivityTest extends ActivityInstrumentationTestCase2<BreweryListActivity> {
 
 	private Solo solo;
 
 	public BreweryListActivityTest() {
 		super(BreweryListActivity.class);
 	}
 
 	@Override
 	public void setUp() throws Exception {
 		super.setUp();
		Auth.setAuth("testuser", "testpass");
 		solo = new Solo(getInstrumentation(), getActivity());
 	}
 
 	@Override
 	public void tearDown() throws Exception {
 		solo.finishOpenedActivities();
 		super.tearDown();
 	}
 
 	/**
 	 * Test activity content.
 	 */
 	public void testActivityContent() {
 		assertTrue(solo.searchText("Brauereien")); // title
 		assertTrue(solo.searchText("Brauerei Locher AG")); // brewery name
 		assertTrue(solo.searchText("Die Brauerei Locher ist ein traditionsreicher Familienbetrieb in Appenzell.")); // description
 	}
 }
