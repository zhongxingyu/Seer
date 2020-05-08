 /*
  * Copyright [2012] [Dina Zuko]
 
    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
 
        http://www.apache.org/licenses/LICENSE-2.0
 
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License. 
  */
 
 package com.chalmers.schmaps.test;
 
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 
 
 import com.chalmers.schmaps.GoogleMapSearchLocation;
 import com.chalmers.schmaps.MapItemizedOverlay;
 import com.chalmers.schmaps.R;
 import com.google.android.maps.GeoPoint;
 import com.google.android.maps.MapView;
 import com.google.android.maps.OverlayItem;
 import com.jayway.android.robotium.solo.Solo;
 
 import android.app.AlertDialog;
 import android.app.Dialog;
 import android.graphics.drawable.Drawable;
 import android.test.ActivityInstrumentationTestCase2;
 import android.util.Log;
 import android.view.View;

 public class MapItemizedOverlayTest extends ActivityInstrumentationTestCase2<GoogleMapSearchLocation> {
 	private static final int THEOVERLAYITEMONMAP = 0;
 
 	private GoogleMapSearchLocation activity;
 	private MapItemizedOverlay mapOverlay;
 	private Drawable drawable;
 	private Solo solo;
 
 	private AlertDialog dialogShowing;
 
 	
 	public MapItemizedOverlayTest(){
 		super(GoogleMapSearchLocation.class);
 	}
 	
 	@Override
 	protected void setUp() 	throws Exception {
 		super.setUp();
 		solo = new Solo(getInstrumentation(), getActivity());
 		this.activity = super.getActivity();
 		drawable = activity.getResources().getDrawable(R.drawable.androidgubbemini);
 		mapOverlay = new MapItemizedOverlay(drawable,activity);		
 	}
 	
 	@Override
 	protected void tearDown() throws Exception {
 		super.tearDown();
 	}
 	
 	/**
	 * Tests that a overlay is added 
 	 */
 	public void testAddOverlay(){
 		GeoPoint p = new GeoPoint(67854516,20215681);
 		OverlayItem item = new OverlayItem(p, "Hej Kiruna", "Här är det kallt");
 		mapOverlay.addOverlay(item);
 		assertEquals(1,mapOverlay.size());
 	}	
 	
 	public void testRemoveOverlay(){
 		GeoPoint p = new GeoPoint(67854516,20215681);
 		GeoPoint p2 = new GeoPoint(29385034,30482932);
 		OverlayItem item = new OverlayItem(p, "RAWR", "TESTRAWR");
 		OverlayItem item2 = new OverlayItem(p2, "RAWR2", "TESTRAWR");
 		mapOverlay.addOverlay(item);
 		mapOverlay.addOverlay(item2);
 		assertEquals(2,mapOverlay.size());
 		mapOverlay.removeOverlay();
 		assertEquals(0, mapOverlay.size());
 		
 	}
 	
 	public void testOnTap() throws InterruptedException{
 		GeoPoint p = new GeoPoint(67854516,20215681);
 		OverlayItem item = new OverlayItem(p, "RAWR", "TESTRAWR");
 		mapOverlay.addOverlay(item);
 		mapOverlay.onTap(THEOVERLAYITEMONMAP);
 		super.getInstrumentation().waitForIdleSync();
 		try {
			Field dialogToShow = activity.getClass().getDeclaredField("alertDialog");
 			dialogToShow.setAccessible(true);
			dialogShowing = (AlertDialog) dialogToShow.get(this.activity);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		assertTrue(dialogShowing.isShowing());
 		}
 }
