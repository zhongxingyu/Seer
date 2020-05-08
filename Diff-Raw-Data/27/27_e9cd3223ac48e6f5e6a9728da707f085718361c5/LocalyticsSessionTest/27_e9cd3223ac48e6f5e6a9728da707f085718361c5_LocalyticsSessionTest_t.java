 package edu.channel4.mobilemetrics.sdk.android.test;
 
 import java.lang.reflect.Field;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.TimeUnit;
 
 import android.test.ActivityInstrumentationTestCase2;
 import android.util.Log;
 import edu.channel4.mobilemetrics.sample.android.SampleApplication;
 import edu.channel4.mobilemetrics.sdk.android.LocalyticsSession;
 
// TODO: To deal with class and variable scope, try making things in LocalyticsSession
// package scope.
 public class LocalyticsSessionTest extends
 		ActivityInstrumentationTestCase2<SampleApplication> {
	private static final String TAG = "TestLocalytics";
	private LocalyticsSession localyticsSession;
	private static final String LOCALYTICS_KEY = "sample_key";
 
 	/**
 	 * The number of seconds you want to wait for the network before timing out.
 	 */
 	private static final int NETWORK_TIMEOUT_TIME = 30;
 
 	public LocalyticsSessionTest() {
 		super(SampleApplication.class);
 	}
 
 	public LocalyticsSessionTest(Class<SampleApplication> activityClass) {
 		super(activityClass);
 	}
 
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		localyticsSession = new LocalyticsSession(getActivity(), LOCALYTICS_KEY);
 		localyticsSession.open();
 	}
 
 	@Override
 	protected void tearDown() throws Exception {
 		super.tearDown();
 	}
 
 	/**
 	 * Test JsonObjects.BlobHeader - Just check that all the string values are
 	 * set to what they should be. This is important for regression testing 
 	 * changing these will break our APEX.
 	 */
 	public void testJSONObjectsBlobHeader() {

 	}
 
 	/**
 	 * LocalyticsSession() - constructor, it should call
 	 * AuthenticateSalesforceTask correctly.
 	 * 
 	 * AuthenticateSalesforceTask - use Reflection. Make sure it reads in values
 	 * from the "res/values/strings.xml" file. Make sure it sends a correctly
 	 * formatted HTTP Request, and maybe make sure that it gets back a response.
 	 * 
 	 * @throws Throwable
 	 */
 	public void testAuthenticateSalesforceTask() throws Throwable {
 		// Create a signal switch to notify us when the AsyncTask is done.
 		final CountDownLatch signal = new CountDownLatch(1);
 
 		// Add the signal switch to the AsyncTask.
 		final LocalyticsSession.AuthenticateSalesforceTask authenticateSalesforceTask = localyticsSession.new AuthenticateSalesforceTask() {
 			@Override
 			protected void onPostExecute(String result) {
 				super.onPostExecute(result);
 				signal.countDown();
 			}
 		};
 
 		// Execute the AsyncTask on the UI thread.
 		runTestOnUiThread(new Runnable() {
 			@Override
 			public void run() {
 				authenticateSalesforceTask.execute();
 			}
 		});
 
 		// Wait for the signal to flip, or timeout if it takes longer than the
 		// NETWORK_TIMEOUT_TIME in seconds.
 		signal.await(NETWORK_TIMEOUT_TIME, TimeUnit.SECONDS);
 
 		// Use reflection (gross) to grab the private static fields from the
 		// LocalyticsSession
 		Field accessTokenField = localyticsSession.getClass().getDeclaredField(
 				"access_token");
 		Field instanceURLField = localyticsSession.getClass().getDeclaredField(
 				"instance_url");
 
 		// Make the fields accessible (reflection sucks)
 		accessTokenField.setAccessible(true);
 		instanceURLField.setAccessible(true);
 
 		// Grab the values for the fields. Use 'null' as the object, since
 		// they're static fields.
 		String accessToken = (String) accessTokenField.get(null);
 		String instanceURL = (String) instanceURLField.get(null);
 
 		// Report them to the user.
 		Log.i(TAG, "Access token: " + accessToken);
 		Log.i(TAG, "Instance URL: " + instanceURL);
 
		// Ensure that the access token and URL are not empty strings or null
 		assertFalse(accessToken.trim().equals(""));
 		assertFalse(instanceURL.trim().equals(""));
 	}
 
 	/**
 	 * LocalyticsSession.UploadHandler.handleMessage() - for MESSAGE_UPLOAD.
 	 */
 	public void testLocalyticsSessionUploadHandlerHandleMessage() {
 
 	}
 
 	/**
 	 * LocalyticsSession.UploadHandler.uploadSessions() - We should add back in
 	 * the GZIP functionality. Let's manually test that before we write unit
 	 * tests for it. To do that, we need to finish the APEX endpoint that
 	 * accepts our new JSON schema.
 	 */
 	public void testLocalyticsSessionUploadHandlerUploadSessions() {
 
 	}
 
 	/**
 	 * LocalyticsSession.UploadHandler.convertDatabaseToJson() - The return type
 	 * of this function might change from List<JSONObject> to just a single
 	 * JSONObject. Let me know when you're going to write the test for this
 	 * function and I can change the return type.
 	 */
 	public void testLocalyticsSessionUploadHandlerConvertDatabaseToJSON() {
 
 	}
 
 }
