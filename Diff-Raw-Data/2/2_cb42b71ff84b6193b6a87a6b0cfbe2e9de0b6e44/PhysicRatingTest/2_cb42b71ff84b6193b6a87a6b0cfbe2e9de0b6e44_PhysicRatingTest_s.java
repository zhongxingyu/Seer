 package ca.charland.tanita;
 
 import android.content.Intent;
 import android.test.ActivityInstrumentationTestCase2;
 
 import ca.charland.tanita.base.activity.manage.BaseAllPeopleListActivity;
 
 import com.jayway.android.robotium.solo.Solo;
 
 public class PhysicRatingTest extends ActivityInstrumentationTestCase2<PhysicRatingActivity>{
 
 	private Solo solo;
 
 	public PhysicRatingTest() {
 		super("ca.charland.tanita", PhysicRatingActivity.class);
 		Intent i = new Intent();
 		i.putExtra(BaseAllPeopleListActivity.PERSON_ID, 5);
 		setActivityIntent(i);
 	}
 	
 	@Override
 	public void setUp() throws Exception {
 		//setUp() is run before a test case is started. 
 		//This is where the solo object is created.
 		solo = new Solo(getInstrumentation(), getActivity());
 	}
 	
 	@Override
 	public void tearDown() throws Exception {
 		//tearDown() is run after a test case has finished. 
 		//finishOpenedActivities() will finish all the activities that have been opened during the test execution.
 		solo.finishOpenedActivities();
 	}
 
 	public void testGoingToNextScreen() throws Exception {
 		solo.sleep(5000);
 		solo.assertCurrentActivity("Expected the physic rating activity", "PhysicRatingActivity");
 		solo.enterText(0, "5");
 		solo.clickOnButton("Next");
 		solo.sleep(5000);
		solo.assertCurrentActivity("Expected Add a new Person activity", "AddANewPersonActivity");
 	}
 
 }
