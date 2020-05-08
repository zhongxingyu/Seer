 package com.chalmers.schmaps.test;
 
 import com.chalmers.schmaps.CampusMenuActivity;
 import com.chalmers.schmaps.R;
 
 import android.test.ActivityInstrumentationTestCase2;
 import android.test.TouchUtils;
 import android.view.KeyEvent;
 import android.widget.Button;
 
 public class CampusMenuActivityTest extends ActivityInstrumentationTestCase2<CampusMenuActivity> {
 
 	private CampusMenuActivity campusMenuActivity;
 	private Button johannebergButton;
 	private Button lindholmenButton;
 
 	public CampusMenuActivityTest() {
 		super(CampusMenuActivity.class);
 	}
 	
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		setActivityInitialTouchMode(false);
 		this.campusMenuActivity = super.getActivity();
 		johannebergButton = (Button) campusMenuActivity.findViewById(R.id.johannebergButton);
 		lindholmenButton = (Button) campusMenuActivity.findViewById(R.id.lindholmenButton);
 	}
 
 	public void testPreConditions(){
 		super.assertNotNull(johannebergButton);
 		super.assertNotNull(lindholmenButton);
 	}
 		
 	public void testLindholmenButton(){
 		TouchUtils.clickView(this, this.lindholmenButton);
 		super.getInstrumentation().waitForIdleSync();
 		assertEquals("android.intent.action.GOOGLEMAPSHOWLOCATION", campusMenuActivity.getActionString());
 		this.sendKeys(KeyEvent.KEYCODE_BACK);
 	}
 	
 	public void testJohannebergButton(){
 		TouchUtils.clickView(this, this.johannebergButton);
 		super.getInstrumentation().waitForIdleSync();
 		assertEquals("android.intent.action.GOOGLEMAPSHOWLOCATION", campusMenuActivity.getActionString());
 		this.sendKeys(KeyEvent.KEYCODE_BACK);
 	}
 
 
 }
