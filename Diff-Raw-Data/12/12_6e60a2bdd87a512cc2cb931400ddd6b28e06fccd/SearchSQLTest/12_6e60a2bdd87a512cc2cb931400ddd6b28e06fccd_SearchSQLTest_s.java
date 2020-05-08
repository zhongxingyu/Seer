 /*
  * Copyright [2012] [Simon Fransson]
 
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
 
 import java.io.SequenceInputStream;
 
 import com.chalmers.schmaps.GoogleMapSearchLocation;
 import com.chalmers.schmaps.R;
 import com.chalmers.schmaps.SearchSQL;
 import com.google.android.maps.MapView;
 
 import android.content.Context;
 import android.test.ActivityInstrumentationTestCase2;
 import android.test.AndroidTestCase;
 import android.test.InstrumentationTestCase;
 import android.widget.Button;
 import android.widget.EditText;
 
 /**
  * Test class for testing the SQL class. Different test cases tests so that the
  * get methods get the proper data from the database given an already known query.
  * @author Froll
  *
  */
 public class SearchSQLTest extends ActivityInstrumentationTestCase2<GoogleMapSearchLocation> {
 	private static final int RETURNVALUEIFNOTFOUND = 0;
 	private SearchSQL tester;
 	GoogleMapSearchLocation activity;
 	String theTestValue = "runan";
 	
 	public SearchSQLTest()
 	{
 		super(GoogleMapSearchLocation.class);
 	}
 	
 	/**
 	 * Setup method for instantiating the variables.
 	 */
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		setActivityInitialTouchMode(false);
 		this.activity = super.getActivity();
 		tester = new SearchSQL(activity);
 		tester.createDatabase();
 		tester.openRead();
 	}
 	
 	@Override
 	protected void tearDown() throws Exception {
 		// TODO Auto-generated method stub
 		tester.close();
 		activity.finish();
 		super.tearDown();
 	}
 
 
 	public void testExists() {
 		assertTrue(tester.exists(theTestValue));
 		assertFalse(tester.exists("ValueThatDoesNotExist"));
 	}
 	
 	public void testGetLat() {
 		assertEquals(57689111, tester.getLat(theTestValue));
 		assertEquals(RETURNVALUEIFNOTFOUND, tester.getLat("ValueThatDoesNotExist"));
 	}
 
 
 	public void testGetLong() {
 		assertEquals(11973517, tester.getLong(theTestValue));
 		assertEquals(RETURNVALUEIFNOTFOUND, tester.getLong("ValueThatDoesNotExist"));
 
 	}
 
 
 	public void testGetAddress() {
 		
 		assertEquals("Sven Hultins gata 2", tester.getAddress(theTestValue));
 		assertNull(tester.getAddress("ValueThatDoesNotExist"));
 	}
 
 
 	public void testGetLevel() {
 		
		assertEquals("", tester.getLevel(theTestValue));
 		assertNull(tester.getLevel("ValueThatDoesNotExist"));
 
 	}
 
 
 	public void testGetLocations() {
 		
		assertEquals(2, tester.getLocations("Microwaves").size());
 	}
 
 }
