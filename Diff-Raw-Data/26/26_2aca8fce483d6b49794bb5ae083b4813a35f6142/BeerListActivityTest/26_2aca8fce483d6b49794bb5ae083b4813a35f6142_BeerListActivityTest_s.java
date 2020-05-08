 package ch.hsr.bieridee.android.test;
 
 import android.test.ActivityInstrumentationTestCase2;
 import ch.hsr.bieridee.android.activities.BeerListActivity;
 import ch.hsr.bieridee.android.config.Auth;
 import com.jayway.android.robotium.solo.Solo;
 
 public class BeerListActivityTest extends ActivityInstrumentationTestCase2<BeerListActivity> {
 
 	private Solo solo;
 
 	public BeerListActivityTest() {
 		super(BeerListActivity.class);
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
 		assertTrue(solo.searchText("Bierliste")); // title
 		assertTrue(solo.searchText("Holzfass Bier")); // beer name
 		assertTrue(solo.searchText("Appenzeller Bier")); // beer brand
 	}
 }
