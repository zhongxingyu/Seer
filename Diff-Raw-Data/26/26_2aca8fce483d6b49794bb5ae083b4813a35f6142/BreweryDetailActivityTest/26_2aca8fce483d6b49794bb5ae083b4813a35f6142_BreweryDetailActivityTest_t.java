 package ch.hsr.bieridee.android.test;
 
 import android.content.Intent;
 import android.test.ActivityInstrumentationTestCase2;
 import ch.hsr.bieridee.android.activities.BreweryDetailActivity;
 import ch.hsr.bieridee.android.config.Auth;
 import com.jayway.android.robotium.solo.Solo;
 
 public class BreweryDetailActivityTest extends ActivityInstrumentationTestCase2<BreweryDetailActivity> {
 
 	private Solo solo;
 
 	public BreweryDetailActivityTest() {
 		super(BreweryDetailActivity.class);
 	}
 
 	@Override
 	public void setUp() throws Exception {
 		super.setUp();
 
		Auth.setAuth("testuser", "$2$10$ae5deb822e0d719929004uD0KL0l5rHNCSFKcfBvoTzG5Og6O/Xxu");
 
 		final Intent i = new Intent();
		i.putExtra(BreweryDetailActivity.EXTRA_BREWERY_ID, 71L);
 		setActivityIntent(i);
 
 		final BreweryDetailActivity activity = getActivity();
 		solo = new Solo(getInstrumentation(), activity);
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
 		assertTrue(solo.searchText("St. Francis Abbey Brewery")); // brewery name
 		assertTrue(solo.searchText("national")); // brewery size
 		assertTrue(solo.searchText("Die älteste irische Brauerei, welche unter Anderem das Kilkenny Bier braut.")); // description
 	}
 }
