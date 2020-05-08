 package it.chalmers.dat255_bearded_octo_lama.test.robotium;
 
 import it.chalmers.dat255_bearded_octo_lama.activities.AddAlarmActivity;
 import android.test.ActivityInstrumentationTestCase2;
 
 import com.jayway.android.robotium.solo.Solo;
 
 /**
  * This will test the AddAlarmActivity by using robotium.
  * @author Emil Johansson
  * @date 22 okt 2012
  */
 public class TestAddAlarmActivity extends
 		ActivityInstrumentationTestCase2<AddAlarmActivity> {
 	private Solo solo;
 	
 	public TestAddAlarmActivity() {
 		super("it.chalmers.dat255_bearded_octo_lama", AddAlarmActivity.class);
 	}
 
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		solo = new Solo(getInstrumentation(), getActivity());
 	}
 	public void testAddAlarm(){
 		solo.assertCurrentActivity("Check on activity", AddAlarmActivity.class);
 		solo.sleep(250);		
		solo.clickOnText("1");
		solo.clickOnText("2");
		solo.clickOnText("3");
		solo.clickOnText("4");
 		solo.sendKey(Solo.MENU);
 		solo.sleep(250);
 		solo.sendKey(Solo.MENU);
		solo.clickOnText("Add test");
 		solo.sleep(7000);
 		solo.clickOnButton("Dismiss alarm");
 		solo.sleep(250);
 	}
 	public void testSettings(){
 		solo.assertCurrentActivity("Check on activity", AddAlarmActivity.class);
 		solo.clickOnText("Settings");
 		solo.clickOnCheckBox(0);
 		solo.sleep(250);
 		solo.clickOnCheckBox(1);
 		solo.sleep(250);
 		solo.clickOnCheckBox(2);
 		solo.sleep(250);
 		solo.clickOnCheckBox(1);
 		solo.pressSpinnerItem(1, 3);
 		solo.sleep(250);
 		solo.pressSpinnerItem(2, 2);
 	}
 }
