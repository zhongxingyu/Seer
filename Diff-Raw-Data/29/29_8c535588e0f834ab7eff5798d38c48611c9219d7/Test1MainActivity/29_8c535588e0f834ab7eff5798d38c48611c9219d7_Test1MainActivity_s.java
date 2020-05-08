 package edu.sdsmt.cs492.example10.contentprovider.test;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import android.test.ActivityInstrumentationTestCase2;
 import android.test.UiThreadTest;
 import android.widget.EditText;
 import edu.sdsmt.cs492.example10.contentprovider.ContactDetailFragment;
 import edu.sdsmt.cs492.example10.contentprovider.ContactListFragment;
 import edu.sdsmt.cs492.example10.contentprovider.MainActivity;
 import edu.sdsmt.cs492.example10.contentprovider.R;
 import edu.sdsmt.cs492.example10.contentprovider.model.Contact;
 import edu.sdsmt.cs492.example10.contentprovider.model.ContactModel;
 
 // Reference:  http://developer.android.com/tools/testing/activity_test.html#CreateTestCaseClass
 public class Test1MainActivity extends ActivityInstrumentationTestCase2<MainActivity>
 {
 	private MainActivity _mainActivity;
 	private ContactListFragment _fragmentContactList;
 	private ContactDetailFragment _fragmentContactDetail;
 	
 	private int _numberOfContacts = 3;
 
 	public Test1MainActivity()
 	{
 		super(MainActivity.class);
 	}
 
 	@Before
 	protected void setUp() throws Exception
 	{
 		super.setUp();
 		
 		setActivityInitialTouchMode(false);
 	    _mainActivity = getActivity();
 	    
 		// Get reference to the list fragment.
 	    _fragmentContactList = (ContactListFragment) _mainActivity.getFragmentManager().findFragmentByTag(MainActivity.FRAGMENT_CONTACT_LIST_TAG);
 
 	}
 
 	@After
 	protected void tearDown() throws Exception
 	{
 		
 		super.tearDown();
 	}
 
 	@Test
 	public final void test0PreCondition()
 	{
 		// NOTE:  This type of unit testing was flacky when trying to actually
 		//        insert and delete contacts per test.  Rather, as long as the
 		//        contacts were inserted in "pre-condition" fashion and deleted
 		//        at the end of the set of tests, all was good.
 		//        
 		//        Part of the flackiness (i.e. technical term) happened when
 		//        the Content Provider and Loaded did not get notified on data
 		//        set changes.  Even trying to perform the notify on the Content
 		//        Provider did not do the trick either.
 		
 		ContactModel _contactModel = ContactModel.getInstance(_mainActivity);
 		_contactModel.deleteAllContacts();
 		_numberOfContacts = _contactModel.insertDummyContacts(false);
 		
 		assertTrue("No dummy contacts inserted", _numberOfContacts > 0);
 	}
 	
	@Test
 	public final void test1ContactList()
 	{
 		// Good testing practice.
 		// Make sure there are rows, etc.
 		assertTrue("At least one Contact is required.", _fragmentContactList.getListAdapter().getCount() > 0);
 	}
 	
	@Test
 	public final void test2ContactDetail()
 	{
 		
		// NOTE:  This method is not decorated with @UiThreadTest, so you have to 
 		//        specifically call runOnUiThread for those methods that require
 		//        calling on the main thread.
 		
 		_mainActivity.runOnUiThread(new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				
 				_fragmentContactList.getListView().requestFocus();
 				_fragmentContactList.getListView().setSelection(0);
 				
 				_fragmentContactList.getListView().performItemClick(_fragmentContactList.getView(), 
 						                                            0, 
 						                                            _fragmentContactList.getListView().getSelectedItemId());
 				
 				assertTrue("Contact not selected", _fragmentContactList.getListView().getSelectedItemId() > 0);
 				
 				// Make sure the detail fragment is available.
 				// Fragments are lazily assigned.
 				_mainActivity.getFragmentManager().executePendingTransactions();
 				
 			}
 		});
 		
 		// Need to make sure the Fragment is visible.
 		getInstrumentation().waitForIdleSync();
 		
 		// Get a reference to the view contact fragment.
 		_fragmentContactDetail = (ContactDetailFragment) _mainActivity.getFragmentManager().findFragmentByTag(MainActivity.FRAGMENT_CONTACT_DETAIL_TAG);
 		
 		assertNotNull("ContactDetailFragment null", _fragmentContactDetail);
 		
 		// Get a Contact that was passed to the fragment.
 		Contact contact = _fragmentContactDetail.getArguments().getParcelable(ContactDetailFragment.BUNDLE_KEY_CONTACT);
 		
 		assertNotNull("Contact null", contact);
 		
 		// Get the contact name displayed in the text box.
 		EditText editText = (EditText) _fragmentContactDetail.getView().findViewById(R.id.editTextName);
 		String contactName = editText.getText().toString();
 		
 		assertEquals("Contact name is different", ContactModel.getInstance(_mainActivity).getContact(contact.ContactID).Name, contactName);
 		
 	}
 	
 	@Test
 	@UiThreadTest
 	public final void test3StatePauseResume()
 	{
 		
		// NOTE:  This test is decorated with @UiThreadTest, so all of the calls are executed
 		//        on the main thread.  Different from the previous test where a Runnable was
 		//        used to perform clicks on the UI thread.
 				
 		_fragmentContactList.getListView().requestFocus();
 		_fragmentContactList.getListView().setSelection(0);
 		
 		_fragmentContactList.getListView().performItemClick(_fragmentContactList.getView(), 
                 											0, 
                 											_fragmentContactList.getListView().getSelectedItemId());
 
 		assertTrue("Contact not selected", _fragmentContactList.getListView().getSelectedItemId() > 0);
 		
 		// Make sure the detail fragment is available.
 		// Fragments are lazily assigned.
 		_mainActivity.getFragmentManager().executePendingTransactions();
 				
 		// Get a reference to the view contact fragment.
 		_fragmentContactDetail = (ContactDetailFragment) _mainActivity.getFragmentManager().findFragmentByTag(MainActivity.FRAGMENT_CONTACT_DETAIL_TAG);
 		
 		assertNotNull("ContactDetailFragment null", _fragmentContactDetail);
 		
 		// Get a Contact that was passed to the fragment.
 		Contact contact = _fragmentContactDetail.getArguments().getParcelable(ContactDetailFragment.BUNDLE_KEY_CONTACT);
 		
 		assertNotNull("Contact null", contact);
 		
 		// Get the contact name displayed in the text box.
 		EditText editText = (EditText) _fragmentContactDetail.getView().findViewById(R.id.editTextName);
 		String contactName = editText.getText().toString();
 		
 		assertTrue("Contact name | Expected: " + contactName + 
 				   " Actual: " + ContactModel.getInstance(_mainActivity).getContact(contact.ContactID).Name, 
 				   contactName.equals(ContactModel.getInstance(_mainActivity).getContact(contact.ContactID).Name));
 		
 		// ***PAUSE Activity.
 		getInstrumentation().callActivityOnPause(_mainActivity);
 
 		// Edit the text in the Contact Name field.
 		String newContactName = contactName + _mainActivity.getLocalClassName();
 		editText.setText(newContactName);
 		editText = null;
 		
 		// *** RESUME Activity.
 		getInstrumentation().callActivityOnResume(_mainActivity);
 		
 		// Just because, find the Fragment again and get the value
 		// stored in the Contact Name field.
 		_fragmentContactDetail = (ContactDetailFragment) _mainActivity.getFragmentManager().findFragmentByTag(MainActivity.FRAGMENT_CONTACT_DETAIL_TAG);
 		editText = (EditText) _fragmentContactDetail.getView().findViewById(R.id.editTextName);
 		contactName = editText.getText().toString();
 		
 		// Is it the modified version?
 		assertTrue("Contact name | Expected: " + contactName + 
 				   " Actual: " + newContactName, 
 				   contactName.equals(newContactName));
 	}
 	
 	@Test
 	public final void test4StateDestroy()
 	{
 		// Destroy the Activity.
 		_mainActivity.finish();
 		
 		// Restart Activity.
 		_mainActivity = getActivity();
 		
 		// Get a reference to the list contact fragment.
 		_fragmentContactList = (ContactListFragment) _mainActivity.getFragmentManager().findFragmentByTag(MainActivity.FRAGMENT_CONTACT_LIST_TAG);
 		
 		assertNotNull("ContactListFragment null", _fragmentContactList);
 		
 		assertEquals("Number of contacts", _numberOfContacts, _fragmentContactList.getListAdapter().getCount());
 	}
 	
 	@Test
 	public final void test5PostCondition()
 	{
 		ContactModel _contactModel = ContactModel.getInstance(_mainActivity);
 		_numberOfContacts = _contactModel.deleteAllContacts();
 		
 		assertTrue("No dummy contacts deleted", _numberOfContacts >= 0);
 	}
 }
