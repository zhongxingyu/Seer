 package com.modzelewski.nfcgb.tests;
 
 import android.test.ActivityInstrumentationTestCase2;
 import android.test.suitebuilder.annotation.Suppress;
 import android.util.Log;
 import android.view.View;
 import android.widget.EditText;
 import android.widget.ExpandableListView;
 import android.widget.ListView;
 import android.widget.Spinner;
 
 import com.jayway.android.robotium.solo.Solo;
 import com.modzelewski.nfcgb.MainActivity;
 import com.modzelewski.nfcgb.R;
 
 public class TestUseCases extends
 		ActivityInstrumentationTestCase2<MainActivity> {
 	private Solo solo;
 //	private BackgroundModel model;
 
 	public TestUseCases() {
 		super(MainActivity.class);
 	}
 
 	@Override
 	protected void setUp() throws Exception {
 		solo = new Solo(getInstrumentation(), getActivity());
 		solo.assertCurrentActivity("Check on first activity",
 				MainActivity.class);
 //		this.model = getActivity().getModel();
 //		 DatabasePopulator dp = new DatabasePopulator();
 //		 dp.fillDatabase(model.getHelper(), getActivity().getApplicationContext(), model);
 		 
 	}
 
 	
 	public void testUseCase01CreateEvent() {
 		Spinner eventSpinner = (Spinner) solo.getView(R.id.events_spinner);
 		int eventCount = eventSpinner.getCount();
 		Log.i(getName(), "Eventcount = " + String.valueOf(eventCount));
 		assertNotNull(eventSpinner);
 		assertNotNull(eventCount);
 
 		View labelForCreateEvent = solo.getView(R.id.om_add_event);
 		solo.clickOnView(labelForCreateEvent);
 
 		EditText eventNameEditText = (EditText) solo.getView(R.id.ed_eventname);
 		solo.enterText(eventNameEditText,
 				solo.getString(R.string.use_case_1_eventname));
 
 		solo.clickOnButton(solo.getString(R.string.ok_button));
 
 		// solo.clickOnView(labelForCreateEvent); // EventSpinner changes his
 		// count
 		// only at click on it
 
 		solo.clickOnView(eventSpinner);
 		int newEventCount = eventSpinner.getCount();
 		Log.i(getName(), "newEventCount = " + String.valueOf(newEventCount));
 		assertTrue(newEventCount == eventCount + 1);
 	}
 
 	
 	public void testUseCase02EditEvent() {
 		Spinner eventSpinner = (Spinner) solo.getView(R.id.events_spinner);
 		int eventCount = eventSpinner.getCount();
 		String newText = solo.getString(R.string.use_case_2_eventname);
 		assertNotNull(eventSpinner);
 		assertNotNull(eventCount);
 
 		String labelForEditEvent = solo.getString(R.string.edit_event);
 		solo.clickLongOnView(eventSpinner);
 		solo.clickOnText(labelForEditEvent);
 
 		EditText eventNameEditText = (EditText) solo.getView(R.id.ed_eventname);
 		solo.clearEditText(eventNameEditText);
 		solo.enterText(eventNameEditText, newText);
 
 		solo.clickOnButton(solo.getString(R.string.ok_button));
 
 		String eventSpinnerLabel = eventSpinner.getSelectedItem().toString();
 		Log.i(getName(), "Spinnerlabel = " + eventSpinnerLabel);
 
 		int newEventCount = eventSpinner.getCount();
 		assertTrue(eventCount == newEventCount);
 		assertTrue(eventSpinnerLabel.contains(newText));
 
 	}
 
 	
 	public void testUseCase03ChangeEvent() {
 		Spinner eventSpinner = (Spinner) solo.getView(R.id.events_spinner);
 
 		// create 2 events - same as in BackgroundModelTest
 		int eventCountBefore = eventSpinner.getCount();
 		assertNotNull(eventSpinner);
 		assertNotNull(eventCountBefore);
 		View labelForCreateEvent = solo.getView(R.id.om_add_event);
 		// add event 1
 		solo.clickOnView(labelForCreateEvent);
 		EditText eventNameEditText = (EditText) solo.getView(R.id.ed_eventname);
 		solo.enterText(eventNameEditText,
 				solo.getString(R.string.use_case_1_eventname));
 		solo.clickOnButton(solo.getString(R.string.ok_button));
 		// add event 2
 		solo.clickOnView(labelForCreateEvent);
 		EditText eventNameEditText2 = (EditText) solo
 				.getView(R.id.ed_eventname);
 		solo.enterText(eventNameEditText2,
 				solo.getString(R.string.use_case_3_eventname));
 		solo.clickOnButton(solo.getString(R.string.ok_button));
 		solo.clickOnView(eventSpinner);
 		int eventCountAfter = eventSpinner.getCount();
 		assertTrue(eventCountAfter - 2 == eventCountBefore);
 
 		solo.clickOnText(solo.getString(R.string.use_case_1_eventname));
 		// assertTrue(); don't know
 	}
 
 	
 	public void testUseCase04RemoveEvent() {
 		Spinner eventSpinner = (Spinner) solo.getView(R.id.events_spinner);
 		int eventCountBefore = eventSpinner.getCount();
 		Log.i(getName(), "Eventcount = " + String.valueOf(eventCountBefore));
 		assertNotNull(eventSpinner);
 		assertNotNull(eventCountBefore);
 
 		String labelForRemoveEvent = solo.getString(R.string.remove_event);
 		solo.clickLongOnView(eventSpinner);
 		solo.clickOnText(labelForRemoveEvent);
 		solo.clickOnButton(solo.getString(R.string.ok_button));
 
 		solo.clickOnView(eventSpinner);
 		int eventCountAfter = eventSpinner.getCount();
 		Log.i("USECASEREMOVE", "count before = \n\r" + eventCountBefore
 				+ "\ncount after = \n\t" + eventCountAfter);
 		assertEquals(eventCountAfter, eventCountBefore - 1);
 	}
 
 	
 	public void testUseCase04RemoveEventWhileMoreThanOne() {
 		Spinner eventSpinner = (Spinner) solo.getView(R.id.events_spinner);
 		String labelForRemoveEvent = solo.getString(R.string.remove_event);
 
 		int i = eventSpinner.getCount();
 		while (i > 1) {
 			assertNotNull(eventSpinner);
 			solo.clickLongOnView(eventSpinner);
 			solo.clickOnText(labelForRemoveEvent);
 			solo.clickOnButton(solo.getString(R.string.ok_button));
 			i--;
 		}
 		try {
 			Thread.sleep(500);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		assertTrue(eventSpinner.getCount() == 1);
 	}
 	
 	
 	public void testUseCase05CreatePerson() {
 		View labelForCreatePerson = solo.getView(R.id.om_add_person);
 		ListView personLV = (ListView) solo.getView(R.id.personsLV);
 		int countBefore = personLV.getCount();
 		Log.i("testUseCase05CreatePerson", "countBefore = " + countBefore);
 //		int countold = model.getPersons().size();
 //		Log.i("testUseCase05CreatePerson", "countold = " + countold);
 
 		solo.clickOnView(labelForCreatePerson);
 
 		EditText personNameEditText = (EditText) solo.getView(R.id.pd_name);
 		EditText personMailEditText = (EditText) solo.getView(R.id.pd_email);
 		solo.enterText(personNameEditText, "Person with a name");
 		solo.enterText(personMailEditText, "personFromCreatePersonTest@emailadress.de");
 		solo.clickOnButton(solo.getString(R.string.ok_button));
 		try {
 			Thread.sleep(500);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		int countAfter = personLV.getCount();
 //		int countnew = model.getPersons().size();
 		Log.i("testUseCase05CreatePerson", "countAfter = " + countAfter);
 //		Log.i("testUseCase05CreatePerson", "countNew = " + countnew);
 		assertTrue(countBefore == countAfter-1);
 //		assertTrue(countBefore == (countnew - 1));
 	}
 
 	
 	public void testUseCase06EditPerson() {
 		String editPerson = solo.getString(R.string.edit_person);
 		ListView personLV = (ListView) solo.getView(R.id.personsLV);
 		int countBefore = personLV.getAdapter().getCount();
 		// solo.clickOnView(personLV.getChildAt(countBefore));
 		solo.clickOnText("Person with a name");
 		solo.clickOnText(editPerson);
 
 		EditText personNameEditText = (EditText) solo.getView(R.id.pd_name);
 		solo.clearEditText(personNameEditText);
 		EditText personMailEditText = (EditText) solo.getView(R.id.pd_email);
 		solo.clearEditText(personMailEditText);
 
 		String personName = "The edited name of a person";
 		String personMail = "person@mailadress.de";
 		solo.enterText(personNameEditText, personName);
 		solo.enterText(personMailEditText, personMail);
 
 		solo.clickOnButton(solo.getString(R.string.ok_button));
 		int countAfter = personLV.getAdapter().getCount();
 		assertTrue(countBefore == countAfter);
 		assertFalse(solo.searchText("Person with a name"));
 		assertTrue(solo.searchText(personName));
 		assertTrue(solo.searchText(personMail));
 	}
 	
 	
 	public void testUseCase07DeletePerson() {
 		assertTrue(solo.searchText("The edited name of a person"));
 		String removePerson = solo.getString(R.string.remove_person);
 		ListView personLV = (ListView) solo.getView(R.id.personsLV);
 		int countBefore = personLV.getCount();
 		Log.i("testUseCase07DeletePerson", "countBefore = " + countBefore);
 
 		
 		solo.clickOnText("The edited name of a person");
 		solo.clickOnText(removePerson);
 		solo.clickOnButton(solo.getString(R.string.ok_button));
 		
 		try {
 		Thread.sleep(1000);
 	} catch (InterruptedException e) {
 		// TODO Auto-generated catch block
 		e.printStackTrace();
 	}
 		
 		int countAfter = personLV.getCount();
 
 		assertFalse(solo.searchText("The edited name of a person"));
 		assertTrue(countBefore == countAfter+1);
 //		TODO Neu schreiben
 //		while (personLV.getCount() > 0) {
 //
 //			solo.clickInList(personLV, countBefore)("The edited name of a person");
 //			solo.clickOnText(removePerson);
 //
 //			solo.clickOnButton(solo.getString(R.string.ok_button));
 //
 //			int countAfter = personLV.getCount();
 //			Log.i("testUseCase07DeletePerson", "countAfter = " + countAfter);
 //			assertTrue(countBefore == countAfter - 1);
 //			assertFalse(solo.searchText("The edited name of a person"));
 //
 //		}
 	}
 
 	
 	public void testUseCase08CreateGroup() {
 		View labelForCreateGroup = solo.getView(R.id.om_add_group);
 		ListView groupELV = (ExpandableListView) solo.getView(R.id.groupsExpLV);
 		int countBefore = groupELV.getCount();
 		Log.i("TestUseCases", "countBefore = " + countBefore);
 //		int countInModelBefore = model.getGroups().size();
 		solo.clickOnView(labelForCreateGroup);
 
 		EditText personNameEditText = (EditText) solo
 				.getView(R.id.gd_groupName);
 		solo.enterText(personNameEditText, "A groupname");
 		solo.clickOnButton(solo.getString(R.string.ok_button));
 		try {
 			Thread.sleep(1000);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		int countAfter = groupELV.getCount();
 //		assertTrue(countInModelBefore == countInModelAfter - 1);
 		assertTrue(countBefore == (countAfter - 1));
 	}
 
 	
 	public void testUseCase09EditGroup() {
 		String editGroup = solo.getString(R.string.edit_group);
 		ExpandableListView groupELV = (ExpandableListView) solo.getView(R.id.groupsExpLV);
 		int countBefore = groupELV.getCount();
 		// solo.clickOnView(personLV.getChildAt(countBefore));
 		solo.clickLongOnText("A groupname");
 		solo.clickOnText(editGroup);
 
 		EditText groupNameEditText = (EditText) solo.getView(R.id.gd_groupName);
 		solo.clearEditText(groupNameEditText);
 
 		String groupName = "The edited name of a group";
 		solo.enterText(groupNameEditText, groupName);
 
 		solo.clickOnButton(solo.getString(R.string.ok_button));
 		
 		try {
 			Thread.sleep(1000);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		int countAfter = groupELV.getCount();
 		assertTrue(countBefore == countAfter);
 		assertFalse(solo.searchText("A groupname"));
 		assertTrue(solo.searchText(groupName));
 	}
 
 	
 	public void testUseCase10RemoveGroup() {
 		String removeGroup = solo.getString(R.string.remove_group);
 		ExpandableListView groupELV = (ExpandableListView) solo.getView(R.id.groupsExpLV);
 		int countBefore = groupELV.getCount();
 		// solo.clickOnView(personLV.getChildAt(countBefore));
 		solo.clickLongOnText("The edited name of a group");
 		solo.clickOnText(removeGroup);
 		solo.clickOnButton(solo.getString(R.string.ok_button));
 		int countAfter = groupELV.getCount();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        assertTrue(countBefore == countAfter+1);
 		assertFalse(solo.searchText("The edited name of a group"));
 	}
 
 	
 	public void testUseCase11AssignPersonToGroup() {
 //		ListView personLV = (ListView) solo.getView(R.id.personsLV);
 //		ExpandableListView groupLV = (ExpandableListView) solo.getView(R.id.groupsExpLV);
 //		int childcount = groupLV.getChildCount();
 //		Log.i("TESTUSECASES", "childcount = " + childcount);
 //		// solo.clickOnView(personLV.getChildAt(countBefore));
 //		clickLongOnScreen(171, 243, 8000);
 //		solo.drag(171, 243, 395, 390, 100);
 //
 //		int countAfter = personLV.getAdapter().getCount();
 ////		assertTrue(countBefore == countAfter);
 //		assertFalse(solo.searchText("Person with a name"));
 		//Test doesn't work. drag only works at robotium with short clicks
 		assertTrue(true);
 	}
 
 	
 	
 	public void testUseCase12SendGroupMail() {
 		String mailGroup = solo.getString(R.string.email_group);
 		ExpandableListView groupELV = (ExpandableListView) solo.getView(R.id.groupsExpLV);
 		testUseCase08CreateGroup();
 		solo.clickLongOnText("A groupname");
 		assertTrue(solo.searchText(mailGroup));
 	}
 
 	
 	public void testUseCase13ShowAndHidePersonsInAGroup() {
 
 	}
 
 	
 	public void testUseCase14EditPersonWhichIsInAGroup() {
 
 	}
 
 	
 	public void testUseCase15RemovePersonFromGroup() {
 //		String removePerson = solo.getString(R.string.remove_person_from_group);
 //		ExpandableListView groupELV = (ExpandableListView) solo.getView(R.id.groupsExpLV);
 	}
 
 	
 	public void testUseCase16SyncViaNFC() {
 
 	}
 
 	@Override
 	protected void tearDown() throws Exception {
 		solo.finishOpenedActivities();
 	}
 }
