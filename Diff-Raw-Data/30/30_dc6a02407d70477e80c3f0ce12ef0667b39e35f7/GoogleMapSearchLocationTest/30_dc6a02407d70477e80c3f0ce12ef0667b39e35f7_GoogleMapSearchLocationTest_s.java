 package com.chalmers.schmaps.test;
 
 import java.util.*;
 
 import junit.framework.Assert;
 
 import com.chalmers.schmaps.GoogleMapSearchLocation;
 import com.chalmers.schmaps.MapItemizedOverlay;
 import com.chalmers.schmaps.R;
 
 import com.google.android.maps.*;
 
//import android.sax.RootElement;
 import android.test.ActivityInstrumentationTestCase2;
 import android.test.TouchUtils;
 import android.test.ViewAsserts;
 import android.text.style.SuperscriptSpan;
 import android.view.View;
 import android.widget.Button;
 import android.widget.EditText;
 
 public class GoogleMapSearchLocationTest extends ActivityInstrumentationTestCase2<GoogleMapSearchLocation> {
 	
 	GoogleMapSearchLocation activity;
 	private Button editButton;
 	private EditText lectureEdit;
 	private MapView mapview;
 	
 	public GoogleMapSearchLocationTest()
 	{
 		super(GoogleMapSearchLocation.class);
 	}
 	
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		setActivityInitialTouchMode(false);
 		this.activity = super.getActivity();
 		this.editButton = (Button) this.activity.findViewById(R.id.edittextbutton);
 		this.lectureEdit = (EditText) this.activity.findViewById(R.id.edittextlecture);
 		this.mapview = (MapView) this.activity.findViewById(R.id.mapview);
 	}
 	
 	@Override
 	protected void tearDown() throws Exception {
 		// TODO Auto-generated method stub
 		super.tearDown();
 	}
 
 	public void testPreConditions(){
 		super.assertNotNull(editButton);
 		super.assertNotNull(mapview);
 		super.assertNotNull(lectureEdit);
 	}
 	
 	public void testSearchForARoom(){
 		TouchUtils.tapView(this, this.lectureEdit);
 		super.sendKeys("R U N A N");
 		super.getInstrumentation().waitForIdleSync();
 		TouchUtils.clickView(this, this.editButton);
 		super.getInstrumentation().waitForIdleSync();
 		List<Overlay> overlays = mapview.getOverlays();
 		
 		//Test case for when u dont have a position from user
 		//MapItemizedOverlay tempTestOverlay = (MapItemizedOverlay) overlays.get(0);
 		
 		//Test case for when u get a position for the user
 		MapItemizedOverlay tempTestOverlay = (MapItemizedOverlay) overlays.get(1);
 		
 		GeoPoint roomGP = new GeoPoint(57689111, 11973517);
 		
 		assertEquals(roomGP,tempTestOverlay.getItem(0).getPoint());
 		assertEquals("Sven Hultins gata 2",tempTestOverlay.getItem(0).getTitle());
 		assertEquals("",tempTestOverlay.getItem(0).getSnippet());
 
 	}
 	
 	public void testRegexForRoom(){
 		TouchUtils.tapView(this, this.lectureEdit);
		super.sendKeys("R U N A N");
 		super.getInstrumentation().waitForIdleSync();
 		TouchUtils.clickView(this, this.editButton);
 		super.getInstrumentation().waitForIdleSync();
		assertEquals("runan", lectureEdit.getText().toString());
 	}
 }
