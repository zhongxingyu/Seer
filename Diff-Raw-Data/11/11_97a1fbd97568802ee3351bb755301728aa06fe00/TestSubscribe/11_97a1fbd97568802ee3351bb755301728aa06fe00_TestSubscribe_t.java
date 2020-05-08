 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 
 
 import android.content.Intent;
 import android.test.ActivityInstrumentationTestCase2;
 import android.util.Log;
 import android.widget.EditText;
 
 import com.example.friendzyapp.ChatScreenActivity;
 import com.example.friendzyapp.Global;
 import com.example.friendzyapp.LoginActivity;
 import com.example.friendzyapp.R;
 import com.example.friendzyapp.HttpRequests.AcceptMatchRequest;
 import com.example.friendzyapp.HttpRequests.PostStatusRequest;
 import com.example.friendzyapp.HttpRequests.PostMsgRequest;
 import com.example.friendzyapp.SubscriptionAddActivity;
 import com.example.friendzyapp.SubscriptionListActivity;
 import com.google.gson.Gson;
 import com.jayway.android.robotium.solo.Solo;
 
 
 public class TestSubscribe extends ActivityInstrumentationTestCase2<LoginActivity> {
 
 	private static final String TAG = "TestSubscribe";
 	
 	private Solo solo;
 
 	public TestSubscribe() {
 		super(LoginActivity.class);
 		
 		
 	}
 	
 	@Override
 	public LoginActivity getActivity() {
 	    Intent intent = new Intent();
 	    Log.i(TAG, "getActivity() called");
 	    
 	    intent.putExtra("ROBOTIUM_TEST", true);
 	    setActivityIntent(intent);
 	    return super.getActivity();
 	}
 
 	protected void setUp() throws Exception {
 		super.setUp();
 		 Log.i(TAG, "setUp() called");
 		
 		
 		SharedObjects.setUpServer1(); // this will hang until it finishes
 		
 		solo = new Solo(getInstrumentation(), getActivity()); 
 		
 		
 		
 		
 		
 		// wait until false WaitingOnSecondClientAsyncTask
 	}
 
 	protected void tearDown() throws Exception {
 		solo.finishOpenedActivities();
 		
 		super.tearDown();
 	}
 	
 	
 	
 	/*
 	 * Covers flow from login to match finding (Robotium initiates first accept) 
 	 * to chat/map screen and tests basic chat functionality
 	 */
 	public void testSubscribe_1() throws Exception {
 		Log.i(TAG, "testSubscribe_1 is starting");
 		// this test will probably fail on a tablet...
 		
 		solo.assertCurrentActivity("Expected LoginActivity activity", "LoginActivity"); // login is best way to retrieve the right info
 		assertTrue("Failed! Why have we not redirected to Status Screen yet?", solo.waitForActivity("StatusScreenActivity"));
 		
 
 		//solo.assertCurrentActivity("Expected SubscriptionAddActivity activity", "SubscriptionAddActivity"); 
 		
 		
 		
 		solo.clickOnActionBarItem(R.id.action_subscriptions);
 		solo.assertCurrentActivity("Expected SubscriptionListActivity activity", "SubscriptionListActivity"); 
 		// this will include the list for the other subs on this device!!!! so be careful
 		
 		assertFalse("Failed!  text for 'arbitrarytopic32' was found", solo.searchText("arbitrarytopic32"));
 		
 		
 		// add sub
 		solo.clickOnActionBarItem(R.id.action_new_subscription);
 		solo.assertCurrentActivity("Expected SubscriptionAddActivity activity", "SubscriptionAddActivity"); 
 		// the friend picker shall glitch up, so we can't test that I guess.... we have to create a work around...
 		
 		solo.typeText((EditText) solo.getView(R.id.new_subscription_name), "arbitrarytopic32");
 		
 		solo.clickOnActionBarItem(R.id.action_new_subscription);
 		solo.assertCurrentActivity("Expected SubscriptionListActivity activity", "SubscriptionListActivity"); 
 		
 		assertTrue("Failed!  text for 'arbitrarytopic32' not found", solo.waitForText("arbitrarytopic32"));
 		
 		
 		// click on it in the list
 		solo.clickOnText("arbitrarytopic32");
 		
 		solo.assertCurrentActivity("Expected SubscriptionListActivity activity", "SubscriptionDetailActivity");		
 		assertTrue("Failed!  text for 'arbitrarytopic32' not found", solo.waitForText("arbitrarytopic32"));
 		
 		
 		// delete it
 		solo.clickOnActionBarItem(R.id.action_delete_subscription);
 		solo.clickOnText("Yes");
 		
 		solo.assertCurrentActivity("Expected SubscriptionListActivity activity", "SubscriptionListActivity"); 
 		
 		assertFalse("Failed!  text for 'arbitrarytopic32' was found", solo.searchText("arbitrarytopic32"));
 		
 		//solo.clickOnActionBarHomeButton()
 		
 		Log.i(TAG, "testSubscribe_1 has ended");
 	}
 	
 	public void testSubscribe_2() throws Exception {
 		Log.i(TAG, "testSubscribe_2 is starting");
 		
 	
 		solo.assertCurrentActivity("Expected LoginActivity activity", "LoginActivity"); // login is best way to retrieve the right info
 		assertTrue("Failed! Why have we not redirected to Status Screen yet?", solo.waitForActivity("StatusScreenActivity"));
 		
 
 		//solo.assertCurrentActivity("Expected SubscriptionAddActivity activity", "SubscriptionAddActivity"); 
 		
 		
 		
 		solo.clickOnActionBarItem(R.id.action_subscriptions);
 		solo.assertCurrentActivity("Expected SubscriptionListActivity activity", "SubscriptionListActivity"); 
 		// this will include the list for the other subs on this device!!!! so be careful
 		//String r = "a_rarestring nobodyWoUldeverTypenormally";
		String r = "aRAresTriN55Gnobodywouldtype3";
 		// TODO: decide how fix params to prevent spaces or _ or awkward punctuation, it causes server to freak out
 		assertFalse("Failed!  text for '" + r + "' was found", solo.searchText(r));
 		
 		
 		// add sub
 		solo.clickOnActionBarItem(R.id.action_new_subscription);
 		solo.assertCurrentActivity("Expected SubscriptionAddActivity activity", "SubscriptionAddActivity"); 
 		// the friend picker shall glitch up, so we can't test that I guess.... we have to create a work around...
 		
 		solo.typeText((EditText) solo.getView(R.id.new_subscription_name), r);
 		
 		solo.clickOnActionBarItem(R.id.action_new_subscription);
 		solo.assertCurrentActivity("Expected SubscriptionListActivity activity", "SubscriptionListActivity"); 
 		
 		assertTrue("Failed!  text for '" + r + "' not found", solo.waitForText(r));
 		
 		
 		// click on it in the list
 		solo.clickOnText(r);
 		
 		solo.assertCurrentActivity("Expected SubscriptionListActivity activity", "SubscriptionDetailActivity");		
 		assertTrue("Failed!  text for '" + r + "' not found", solo.waitForText(r));
 		
 		
 		// don't delete it
 		solo.clickOnActionBarItem(R.id.action_delete_subscription);
 		solo.clickOnText("No");
 
 		
 		// Now we will test that subscriptions will send a gcm to us!!
 		
 		String params = new Gson().toJson(new PostStatusRequest(SharedObjects.user2.userId, r));
 		SecondClientPostStatusAsyncTask task = new SecondClientPostStatusAsyncTask();
 		task.execute(params); //TODO: have this wait until that ^^^ returns...
 		
 		
 		solo.sleep(10*1000);//in ms
 		assertTrue("Failed! Should be on MatchActivity.  Instead on: "+solo.getCurrentActivity().getClass().getName(), 
 				solo.waitForActivity("MatchActivity"));
 		
 		
 		Log.i(TAG, "testSubscribe_2 has ended");
 	}
 	
 	
 	
 	
 	
 }
