 package eu.masconsult.cleanbulgaria;
 
 
 import android.content.Intent;
 import android.net.Uri;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Spinner;
 import com.google.inject.Inject;
 import com.xtremelabs.robolectric.shadows.ShadowAlertDialog;
 import com.xtremelabs.robolectric.shadows.ShadowHandler;
 import com.xtremelabs.robolectric.shadows.ShadowToast;
 import junit.framework.Assert;
 import org.apache.http.HttpStatus;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import roboguice.inject.InjectView;
 
 import static org.junit.Assert.fail;
 
 @RunWith(UploadActivityTestRunner.class)
 public class UploadScreenTest {
 
 
 	@Inject
 	UploadActivity uploadActivity;
 
 	@InjectView(R.id.wasteTypeSelectButton)
 	private Button selectWasteType;
 
 	@InjectView(R.id.metricTypeSpinner)
 	private Spinner metricTypeSpinner;
 
 	@InjectView(R.id.quantityText)
 	private EditText wasteQuantaty;
 
 	@InjectView(R.id.uploadDataButton)
 	private Button markButton;
 
 	private Uri imageUri;
 
 	@Before
 	public void setUp() {
 		imageUri = Uri.parse("/sdcard/cleanBulgaria/img.jpg");
 
     Intent intent = new Intent();
     intent.setData(imageUri);
     uploadActivity.setIntent(intent);
     uploadActivity.onCreate(null);
 	}
 
 	@Test
 	public void testWasteTypeDialogAppearsOnClick() {
 		selectWasteType.performClick();
 		ShadowAlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();
 		Assert.assertTrue(dialog.isShowing());
 	}
 
 	@Test 
 	public void testWasteTypeDataSelection() {
 		selectWasteType.performClick();
		ShadowAlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();
 
 		dialog.clickOnItem(0);
 		dialog.clickOnItem(2);
 
 
 		boolean[] actual = uploadActivity.getWasteTypes();
 
 		boolean[] expected = new boolean[5];
 		expected[0] = true;
 		expected[2] = true;
 
 		for(int i = 0; i < 5; i++) {
 			if(expected[i] != actual[i])
 				fail();
 		}
 	}
 
 	@Test
 	public void testMetricTypeRightValue() {
 		metricTypeSpinner.bringToFront();
 		metricTypeSpinner.setSelection(1);
 		String expected = "2";
 		Assert.assertEquals(expected, uploadActivity.getMetric());
 	}
 
 	@Test
 	public void testPictureUriIsPresent() {
 
 
 
 		Uri actual = uploadActivity.getIntent().getData();
 
 		Assert.assertEquals(imageUri, actual);
 
 	}
 
 	@Test
 	public void testValidUpload() {
 
 		RobolectricHelper.createPendingHttpResponse().withStatus(HttpStatus.SC_MOVED_TEMPORARILY).add();
 		RobolectricHelper.createPendingHttpResponse().withStatus(HttpStatus.SC_OK).withHeader("Location", "thankyou.php").add();
 		ShadowHandler.idleMainLooper();
 		selectWasteType.performClick();
 		ShadowAlertDialog dialog = ShadowAlertDialog.getLatestAlertDialog();
 
 		dialog.clickOnItem(0);
 		dialog.clickOnItem(2);
 
 		dialog.dismiss();
 
 		metricTypeSpinner.bringToFront();
 		metricTypeSpinner.setSelection(1);
 
 		wasteQuantaty.setText("3");
 
 		markButton.performClick();
 
 		Assert.assertEquals(uploadActivity.getString(R.string.markRequestSuccessful), ShadowToast.getTextOfLatestToast());
 
 	}
 
 
 }
