 package uw.cse403.nonogramfun.tests.frontend;
 
 import com.jayway.android.robotium.solo.Solo;
 import uw.cse403.nonogramfun.MainActivity;
 import uw.cse403.nonogramfun.PlayGameScreen;
 import android.app.Activity;
 import android.content.Intent;
 import android.graphics.Color;
 import android.test.ActivityInstrumentationTestCase2;
 import android.widget.Button;
 /**
  * Test the case of small gameboard of PlayGameScreen
  * @author Huiqi Wang
  *
  */
 public class Test_PlayGameScreen_Small extends ActivityInstrumentationTestCase2<PlayGameScreen> {
 	private Activity activity;
 	private Solo solo;
 	final int SIZE_SMALL = 5;
 	
 	public Test_PlayGameScreen_Small() {
 		super(PlayGameScreen.class);
 	}
 
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		Intent i = new Intent();
 		i.putExtra("size", SIZE_SMALL);
 		i.putExtra("test", true);
 		setActivityIntent(i);
 		activity = getActivity();
 		solo = new Solo(getInstrumentation(),activity);
 	}
 
 	@Override
 	protected void tearDown() throws Exception {
 		solo.finishOpenedActivities();
 	}
 
 	/**
 	 * White Box Test
 	 * Test if the gameboard buttons are all created in the view, and if clicking the
 	 * buttons will mark the buttons
 	 */
 	public void testViewsCreated(){
 		solo.assertCurrentActivity("Not CreateGameScreen", PlayGameScreen.class);
 		String text;
 		for(int i = 1; i < SIZE_SMALL+1; i++){
 			for (int j = 1; j < SIZE_SMALL+1; j++){
 				
 				text = Integer.toString(j)+""+Integer.toString(i);
 				assertEquals(true, solo.searchButton(text));
 
 				Button button = solo.getButton(text);
 				solo.clickOnButton(text);
 				assertEquals(Color.BLACK, button.getCurrentTextColor());
 				
 			}
 		}
 	}
 	
 	/**
 	 * Black Box Test
 	 * Test if the submit button exists
 	 */
 	public void testSubmitButtonView(){
 		solo.assertCurrentActivity("Not PlayGameScreen", PlayGameScreen.class);
 		assertEquals(true,solo.searchButton("Submit"));	
 	}
 	
 	/**
 	 * Black Box Test
 	 * Test if the submit button functions properly when the answer is wrong
 	 */
 	public void testSubmitWrongGame(){
 		solo.assertCurrentActivity("Not PlayGameScreen", PlayGameScreen.class);
 		solo.clickOnButton("Submit");	
 		assertEquals(true,solo.searchText("Try Again"));
		solo.clickOnButton("Okay");	
 		solo.assertCurrentActivity("Not MainActivity", MainActivity.class);
 	}
 	
 	/**
 	 * Black Box Test
 	 * Test if the hint button exists
 	 */
 	public void testHintButtonView(){
 		solo.assertCurrentActivity("Not PlayGameScreen", PlayGameScreen.class);
 		assertEquals(true,solo.searchButton("Hint"));	
 		solo.clickOnButton("Hint");
 	}
 }
