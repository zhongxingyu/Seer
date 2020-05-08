 /*Copyright  2012 GivDev
  * 
  * This file is part of Gymapp.
  *
  *   Gymapp is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU General Public License as published by
  *   the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *   Gymapp is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU General Public License for more details.
  *
  *   You should have received a copy of the GNU General Public License
  *  along with Gymapp.  If not, see <http://www.gnu.org/licenses/>.
  *
  *	Copyright  2012 GivDev
  *
  */
 package com.Grupp01.gymapp.test;
 
 
 import android.content.SharedPreferences;
 import android.preference.PreferenceManager;
 import android.test.ActivityInstrumentationTestCase2;
 import com.Grupp01.gymapp.MainActivity;
 import com.Grupp01.gymapp.View.Settings.SettingsActivity;
 import com.jayway.android.robotium.solo.Solo;
 
 /** 
  * @author GivDev
  * @version 1.0
  * @peer reviewed by Robert Blomberg
  * @date 21/10/2012
  * Tests setting activity
  */
 
 public class TestASettings extends ActivityInstrumentationTestCase2<MainActivity> {
 	private static final int SETTINGS_BUTTON = 5;
	private static final int TIME = 1000;
 	private Solo solo;
 	/**Constructor for the class. Calls Main Activity since it will
 	 * start from there
 	 */
 	public TestASettings() 
 	{
 		super(MainActivity.class);
 	}
 
 	/**Method for a "clean" start on every test case. Runs in the beginning
 	 * of every test case.
 	 */
 	protected void setUp() throws Exception {
 		super.setUp();
 		solo = new Solo(getInstrumentation(),getActivity());
 	}
 	/**Testing to open the Settings
 	 * Reference to Test-case in documentation: 1.7
 	 */
 	public void testOpenSettings()
 	{		
 		solo.clickOnImageButton(SETTINGS_BUTTON);
 		solo.assertCurrentActivity("Expected settings activity", SettingsActivity.class);
 		
 	}
 	/**Testing to change language
 	 * Reference to Test-case in documentation: 5.2
 	 */
 	public void testChangeLanguage()
 	{		
 		solo.clickOnImageButton(SETTINGS_BUTTON);
 		
 		//Select swedish as language
 		solo.clickOnText("Vlj sprk");
 		solo.clickOnText("Svenska");
 		solo.sleep(500);
 		
 		//Go back to reload the view
 		solo.goBack();
 		solo.clickOnImageButton(SETTINGS_BUTTON);
 		
 		//Make sure the text is swedish
 		assertTrue(solo.searchText("Vlj sprk"));
 		
 		//Go back to english as a language and make sure 
 		//the language is correct just like above.
 		solo.clickOnText("Vlj sprk");
 		solo.clickOnText("English");
 		solo.goBack();
 		solo.clickOnImageButton(SETTINGS_BUTTON);
 		assertTrue(solo.searchText("Select desired language"));
 	}
 
 	/**Testing to change distance unit.
 	 * Reference to Test-case in documentation: 5.1
 	 */
 	public void testChangeDistanceUnit()
 	{		
 		solo.clickOnImageButton(SETTINGS_BUTTON);
 		
 		//Select kilometers as unit
 		solo.clickOnText("Select desired distance");
 		solo.clickOnText("Kilometers");
		solo.sleep(TIME);
 		
 		//Get sharedpreferences and make sure "km" is set to the correct unit
 		SharedPreferences temp = PreferenceManager.getDefaultSharedPreferences(solo.getCurrentActivity());
 		String unit = temp.getString("pref_key_distance", "km");
		solo.sleep(TIME);
 		assertTrue(unit.equals("km"));
 		assertFalse(unit.equals("mi"));
 		
 		//Select miles as unit
 		solo.clickOnText("Select desired distance");
 		solo.clickOnText("Miles");
 		
 		//Get the preferences again and make sure it is different
 		temp = PreferenceManager.getDefaultSharedPreferences(solo.getCurrentActivity());
 		unit = temp.getString("pref_key_distance", "km");
 		assertTrue(unit.equals("mi"));
 		assertFalse(unit.equals("km"));
 	}
 	
 	/**Testing to change weight unit
 	 * Reference to Test-case in documentation: 5.3
 	 */
 	public void testChangeWeightUnit()
 	{		
 		solo.clickOnImageButton(SETTINGS_BUTTON);
 		
 		//Select kilogram as unit
 		solo.clickOnText("Select desired weight unit");
 		solo.clickOnText("Kilogram");
 		
 		//Get sharedpreferences and make sure "kg" is set to the correct unit
 		SharedPreferences temp = PreferenceManager.getDefaultSharedPreferences(solo.getCurrentActivity());
 		String unit = temp.getString("pref_key_weight", "kg");
 		assertTrue(unit.equals("kg"));
 		assertFalse(unit.equals("lb"));
 		
 		//Select miles as unit
 		solo.clickOnText("Weight");
 		solo.clickOnText("Pounds");
 		
 		//Get the preferences again and make sure it is different
 		temp = PreferenceManager.getDefaultSharedPreferences(solo.getCurrentActivity());
 		unit = temp.getString("pref_key_weight", "kg");
 		assertTrue(unit.equals("lb"));
 		assertFalse(unit.equals("kg"));
 	}
 	/**When a test method are done, close it.
 	 */
 	protected void tearDown() throws Exception {
 		solo.finishOpenedActivities();
 	}
 
 }
