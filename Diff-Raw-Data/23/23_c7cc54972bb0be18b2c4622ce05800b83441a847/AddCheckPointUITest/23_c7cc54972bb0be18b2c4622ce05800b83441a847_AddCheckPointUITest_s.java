 package se.team05.dialog;
 
 /**
  This file is part of Personal Trainer.
 
  Personal Trainer is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  any later version.
 
  Personal Trainer is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with Personal Trainer.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 import se.team05.R;
 import se.team05.activity.MainActivity;
 import se.team05.activity.MediaSelectorActivity;
 import se.team05.activity.RouteActivity;
 import android.app.Activity;
 import android.test.ActivityInstrumentationTestCase2;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.ImageView;
 import android.widget.ProgressBar;
 
 import com.jayway.android.robotium.solo.Solo;
 
 /**
  * This class tests the functionality of the EditCheckPointDialog and the
  * CheckPoint class. To do this we start at MainActivity and selects the new
  * route button, this will lead us to RouteActivity and there we can tap the map
  * to create a checkPoint and get the EditCheckPointDialog
  * 
  * @author Patrik Thituson
  * 
  */
 public class AddCheckPointUITest extends ActivityInstrumentationTestCase2<MainActivity>
 {
 	private MainActivity activity;
 	private Activity routeActivity;
 	private ImageView newRouteButton;
 	private Solo solo;
 
 	public AddCheckPointUITest()
 	{
 		super(MainActivity.class);
 	}
 
 	/**
 	 * The setUp method initiates the test class with starting MainActivity and
 	 * press the new route button. We check if the current activity is
 	 * RouteActivity and stores it in the variable routeActivity for further
 	 * use.
 	 */
 	@Override
 	protected void setUp() throws Exception
 	{
 		super.setUp();
 		setActivityInitialTouchMode(false);
 
 		solo = new Solo(this.getInstrumentation(), getActivity());
 		activity = this.getActivity();
 
 		newRouteButton = (ImageView) activity.findViewById(se.team05.R.id.image_new_route);
 
 		solo.clickOnView(newRouteButton);
 		solo.assertCurrentActivity("wrong class", RouteActivity.class);
 
 		routeActivity = solo.getCurrentActivity();
 	}
 
 	/**
 	 * Tests if the dialog pops up when click on map and that cancel removes it.
 	 * It also tests the goBack button which should behave like the cancel
 	 * button when a new checkPoint is created. This is done by clicking on a
 	 * position to create a checkpoint and show the dialog and when the dialog
 	 * is closed we click on the same position again. If the checkpoint isnt
 	 * properly removed the cancel button will not show up and the test will
 	 * fail. Same thing for the goBack button.
 	 */
 	public void testShowAndCancelDialog()
 	{
 		solo.clickOnScreen(400, 600);
 		assertTrue("Could not find the dialog!", solo.searchText("Edit CheckPoint"));
 		String cancel = routeActivity.getString(se.team05.R.string.cancel);
 		solo.clickOnButton(cancel);
 		assertFalse("Could find the dialog!", solo.searchText("Edit CheckPoint"));
 		solo.clickOnScreen(400, 600);
 		solo.goBack();
 		solo.clickOnScreen(400, 600);
 		solo.clickOnButton(cancel);
 	}
 
 	/**
 	 * Tests to change all the fields in the dialog and save them in the
 	 * checkpoint. This is done by clicking at the map and change the fields and
 	 * then press save. After the checkpoint is saved we click on the checkpoint
 	 * on the map and should get the same fields that we changed or the test
 	 * will fail. We also record a sound and wait for the toast to show and
 	 * selects a track from the MediaSelectorActivity to save it in the
 	 * checkpoint. When we have checked that everything is saved we delete the
 	 * checkPoint and press at the same position again and the checkpoint should
 	 * have disappeared or it will fail.
 	 */
 	public void testSaveAndDeleteCheckPoint()
 	{
 		solo.clickOnScreen(400, 600);
 		assertTrue("Could not find the dialog!", solo.searchText("Edit CheckPoint"));
 		EditText editText = solo.getEditText("CheckPoint");
 		solo.clearEditText(editText);
 		solo.enterText(editText, "Slottskogen");
 
 		for (View v : solo.getViews())
 		{
 			if (v instanceof ProgressBar)
 			{
 				((ProgressBar) v).setProgress(1000);
 			}
 		}
 
 		solo.clickOnButton("Record");
 		solo.clickOnButton("Stop recording");
 		assertTrue(solo.waitForText("Added new recording"));
 
 		solo.clickOnButton("Select");
 		solo.assertCurrentActivity("Wrong Activity excpected MediaSelectorActivity ", MediaSelectorActivity.class);
 		solo.clickOnCheckBox(0);
 		solo.clickOnActionBarItem(R.id.done);
 
 		String save = routeActivity.getString(se.team05.R.string.save);
		solo.clickOnText(save);
 		assertFalse("Could find the dialog!", solo.searchText("Edit CheckPoint"));
 
 		solo.clickOnScreen(400, 600);
 		assertTrue("Wrong Name!", solo.searchEditText("Slottskogen"));
 		assertFalse("SeekBar not changed!", solo.searchText("30"));
 
 		String delete = routeActivity.getString(se.team05.R.string.delete);
 		solo.clickOnButton(delete);
 		solo.clickOnScreen(400, 600);
 
 		assertFalse("Wrong Name!", solo.searchEditText("Slottskogen"));
 		assertTrue("SeekBar not changed!", solo.searchText("30"));
 
 		solo.clickOnButton("Cancel");
 
 	}
 
 	/**
 	 * Tears down the test class by calling the finish method of the activity
 	 * and call the super method
 	 */
 	@Override
 	protected void tearDown() throws Exception
 	{
 		routeActivity.finish();
 		super.tearDown();
 	}
 
 }
