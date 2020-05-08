 package ezhun.smsb.activity;
 
 import android.test.ActivityInstrumentationTestCase2;
 import android.widget.Spinner;
 import android.widget.SpinnerAdapter;
 import com.jayway.android.robotium.solo.Solo;
 import ezhun.smsb.R;
 import ezhun.smsb.content.ApplicationSettingsFake;
 import ezhun.smsb.storage.ApplicationSettings;
 import ezhun.smsb.storage.DeleteAfter;
 import junit.framework.Assert;
 
 import java.util.ArrayList;
 
 /**
  * A test for settings activity..
  */
 public class SettingsActivityTest extends ActivityInstrumentationTestCase2<SettingsActivity> {
 	private Solo solo;
 	private ApplicationSettings settings;
 
 	public ApplicationSettings getApplicationSettings() {
 		if (settings == null) {
 			settings = new ApplicationSettingsFake(getInstrumentation().getContext());
 		}
 		return settings;
 	}
 
 	public SettingsActivityTest() {
 		super(SettingsActivity.class);
 	}
 
 	@Override
 	public void setUp() throws Exception {
 		super.setUp();
 		solo = new Solo(getInstrumentation(), getActivity());
 	}
 
 	@Override
 	public void tearDown() throws Exception {
 		try {
 			solo.finalize();
 		} catch (Throwable e) {
 
 			e.printStackTrace();
 		}
 		getActivity().finish();
 		super.tearDown();
 	}
 
	public void testSettings_currentActivity () {
		solo.assertCurrentActivity("Expected activity: Settings", SettingsActivity.class);
	}

	public void testSettings_DisplayCheckbox_initial_state() {
		boolean isChecked = solo.isCheckBoxChecked(0);
 		boolean shouldBeChecked = getApplicationSettings().showDisplayNotification();
 		Assert.assertEquals(shouldBeChecked, isChecked);
 	}
 
	public void testSettings_DisplayCheckbox_change() {
		boolean isCheckedInitial = solo.isCheckBoxChecked(0);
		solo.clickOnCheckBox(0);
		boolean isCheckedChanged = solo.isCheckBoxChecked(0);
 		Assert.assertTrue(isCheckedInitial != isCheckedChanged);
 		Assert.assertEquals(isCheckedChanged, getApplicationSettings().showDisplayNotification());
 	}
 
 	public void testSettings_DeleteAfterSpinner_initial_state() {
 		ArrayList<Spinner> spinners = solo.getCurrentSpinners();
 		Assert.assertEquals(1, spinners.size());
 		Spinner deleteAfterSpinner = spinners.get(0);
 		Assert.assertEquals(R.id.ddlDisplayNotification, deleteAfterSpinner.getId());
 
 		DeleteAfter item = (DeleteAfter) deleteAfterSpinner.getSelectedItem();
 
 		Assert.assertEquals(getApplicationSettings().getDeleteAfter().index(), item.index());
 	}
 
 	public void testSettings_DeleteAfterSpinner_change() {
 		ArrayList<Spinner> spinners = solo.getCurrentSpinners();
 		Assert.assertEquals(1, spinners.size());
 		Spinner deleteAfterSpinner = spinners.get(0);
 		Assert.assertEquals(R.id.ddlDisplayNotification, deleteAfterSpinner.getId());
 
 		DeleteAfter item = (DeleteAfter) deleteAfterSpinner.getSelectedItem();
 
 		SpinnerAdapter adapter = deleteAfterSpinner.getAdapter();
 		solo.clickOnText(item.toString());
 
 		DeleteAfter shouldBeSelected = null;
 		for (int i = 0; i < adapter.getCount(); i++) {
 			DeleteAfter adapterItem = (DeleteAfter) adapter.getItem(i);
 			if (item.index() != adapterItem.index()) {
 				shouldBeSelected = adapterItem;
 				solo.clickOnText(adapterItem.toString());
 				break;
 			}
 		}
 
 		Assert.assertNotNull(shouldBeSelected);
 		Assert.assertTrue(shouldBeSelected.index() != item.index());
 
 		DeleteAfter reallySelected = (DeleteAfter) deleteAfterSpinner.getSelectedItem();
 		Assert.assertTrue(reallySelected.index() == shouldBeSelected.index());
 		Assert.assertEquals(reallySelected.index(), getApplicationSettings().getDeleteAfter().index());
 	}
 
 	public void testSettings_EditWhitelistButton_click() {
 		solo.assertCurrentActivity("Expected activity: Settings", SettingsActivity.class);
		solo.clickOnButton("Edit senders white list");
 		solo.waitForActivity(EditWhitelistActivity.class.getSimpleName(), 1000);
 		solo.assertCurrentActivity("Expected activity: EditWhiteList", EditWhitelistActivity.class);
 	}
 
 }
