 package com.example.shutapp.test;
 
 import com.example.shutapp.ChatActivity;
 import com.example.shutapp.SettingsActivity;
 import com.example.shutapp.NearbyConversationsActivity;
 
 import android.app.Instrumentation.ActivityMonitor;
 import android.test.ActivityInstrumentationTestCase2;
 import android.widget.Button;
 import android.widget.TextView;
 
 public class NearbyConversationTest extends
 		ActivityInstrumentationTestCase2<NearbyConversationsActivity> {
 	
 	private NearbyConversationsActivity nActivity;
 	private Button cButton;
 	private Button nButton;
 	private Button sButton;
 	private Button mButton;
 	private TextView resultView;
 	private ActivityMonitor activityChatMonitor;
 	private ActivityMonitor activitySettingsMonitor;
 	private ActivityMonitor activityNearbyConversationMonitor;
 	
 	@SuppressWarnings("deprecation")
 	public NearbyConversationTest() {
 		super("com.example.shutapp.MainActivity", NearbyConversationsActivity.class);
 	}
 	
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 
 		// register next activity that need to be monitored.
 		activityChatMonitor = getInstrumentation().addMonitor(ChatActivity.class.getName(), null, false);
 		activitySettingsMonitor = getInstrumentation().addMonitor(SettingsActivity.class.getName(), null, false);
 		activityNearbyConversationMonitor = getInstrumentation().addMonitor(NearbyConversationsActivity.class.getName(), null, false);
 
 	    nActivity = getActivity();
 
 	    nButton =
 	    		(Button) nActivity.findViewById(
 	    				com.example.shutapp.R.id.nearby_conversations_button
 	    				);
 	    
 	    mButton =
 	    		(Button) nActivity.findViewById(
 	    				com.example.shutapp.R.id.map_button
 	    				);
 	    
 	    cButton =
 	    		(Button) nActivity.findViewById(
 	    				com.example.shutapp.R.id.chat_button
 	    				);
 	    
 	    sButton =
 	    		(Button) nActivity.findViewById(
 	    				com.example.shutapp.R.id.settings_button
 	    				);
 	    
 	    resultView = (TextView) nActivity.findViewById(
 	    		com.example.shutapp.R.id.textView1); 
 		
 	}
 	
 	public void testPreConditions() {
 		assertNotNull("Cant find the Map Button", mButton);
 		assertNotNull("Cant find the Nearby Conversation Button", nButton);
 		assertNotNull("Cant find the Chat Button", cButton);
 		assertNotNull("Cant find the Settings Button", sButton);
 	}
 	
 	public void testNearbyConversationButton() {
 		
 		nActivity.runOnUiThread(
 				new Runnable() {
 					public void run() {
 						nButton.requestFocus();
 						nButton.performClick();
 					}
 				});
 
 		wait(4);
 		
 		NearbyConversationsActivity nActivity = (NearbyConversationsActivity) getInstrumentation().
 				waitForMonitorWithTimeout(activityNearbyConversationMonitor, 5);
 		
 		// next activity is opened and captured.
 		assertNotNull("Couldt open Activity", nActivity);
 		nActivity.finish();
 
 		assertTrue(true);
 	
 	}
 	
 	public void testChatButton() {
 		
 		nActivity.runOnUiThread(
 				new Runnable() {
 					public void run() {
 						cButton.requestFocus();
 						cButton.performClick();
 					}
 				});
 
 		wait(4);
 		
 		ChatActivity cActivity = (ChatActivity) getInstrumentation().waitForMonitorWithTimeout(activityChatMonitor, 5);
 		
 		// next activity is opened and captured.
 		assertNotNull("Couldt open Activity", cActivity);
 		cActivity.finish();
 
<<<<<<< HEAD
 //		resultView = (TextView) cActivity.findViewById(
 //	    		com.example.shutapp.R.id.write_msg); 
=======
 		resultView = (TextView) cActivity.findViewById(
 	    		com.example.shutapp.R.id.written_msg); 
>>>>>>> 092ec8276d61c94cb80260e566fc741d9dbd2521
 		
 //		assertNotNull("Cant find the Text View", resultView);
 	//    assertEquals("Wrong text",
 	//    		"ChatActivity", resultView.getText());
 	
 	}
 	
 	public void testSettingsButton() {
 		
 		nActivity.runOnUiThread(
 				new Runnable() {
 					public void run() {
 						sButton.requestFocus();
 						sButton.performClick();
 					}
 				});
 
 		wait(4);
 		
 		SettingsActivity sActivity = (SettingsActivity) getInstrumentation().waitForMonitorWithTimeout(activitySettingsMonitor, 5);
 		
 		// next activity is opened and captured.
 		assertNotNull("Couldt open Activity", sActivity);
 		sActivity.finish();
 
 		resultView = (TextView) sActivity.findViewById(
 	    		com.example.shutapp.R.id.textView1); 
 		
 		assertNotNull("Cant find the Text View", resultView);
 	    assertEquals("Wrong text",
 	    		"SettingsActivity", resultView.getText());
 	
 	}
 	
 	public void wait(int sec){
 		
 		try {
 			Thread.sleep(sec*1000);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 		
 	}
 
 }
