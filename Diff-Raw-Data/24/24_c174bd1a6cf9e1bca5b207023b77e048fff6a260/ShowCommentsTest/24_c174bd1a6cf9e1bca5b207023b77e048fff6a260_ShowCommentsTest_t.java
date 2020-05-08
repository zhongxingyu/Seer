 package edu.upenn.cis350;
 import android.app.Activity;
 import android.content.Intent;
 import android.test.ActivityInstrumentationTestCase2;
 import android.widget.Button;
 import android.widget.EditText;
 import android.widget.Spinner;
 
 
public class ShowCommentsTest extends ActivityInstrumentationTestCase2<ShowMessage> {
 
 	public ShowCommentsTest() {
		super("edu.upenn.cis350", ShowMessage.class);
 	}
 	
 	private Button submitButton;
 	private Activity activity;
 	private EditText commentText;	
 	
 	public void setUp() throws Exception{
 		super.setUp();
 		Intent intent = new Intent();
 		intent.putExtra("uname", "User Name");
//    	MessagePOJO message = new MessagePOJO();
//    	message.setMessageId(1);
//    	message.setText("Message");
//    	message.setAuthor("Joe Cruz");
//    	message.setTimestamp("March 18, 2012");
//    	intent.putExtra("messagePOJO", message);
 
 		activity = getActivity();
 		submitButton = (Button)activity.findViewById(R.id.newCommentButton);
 		commentText = (EditText)activity.findViewById(R.id.newCommentText);
 	}
 	
 	public void testSubmitEmpty(){
 		
 		activity.runOnUiThread(new Runnable() {
 			public void run() {
 				assertNotNull(submitButton);
 				boolean result = submitButton.performClick();
 				assertTrue(result);
 			}
 		});
 		
 	}
 	
 	public void testSubmitFilled(){
 		
 		activity.runOnUiThread(new Runnable() {
 			public void run() {
 				assertNotNull(commentText);
 				commentText.requestFocus();
 			}
 		});
 		
 		getInstrumentation().waitForIdleSync();
 		sendKeys("N E W C O M M E N T");
 		assertEquals(commentText.getText().toString(), "newcomment");
 		
 		activity.runOnUiThread(new Runnable() {
 			public void run() {
 				assertNotNull(submitButton);
 				boolean result = submitButton.performClick();
 				assertTrue(result);
 			}
 		});
 		
 		getInstrumentation().waitForIdleSync();
 		
 		
 	}
 
 }
