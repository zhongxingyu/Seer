 package edu.sdsmt.cs492.example10.contentprovider.test;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import android.database.Cursor;
 import android.net.Uri;
 import android.test.ProviderTestCase2;
 import android.test.mock.MockContentResolver;
 import android.test.suitebuilder.annotation.Suppress;
 import android.util.Log;
 import edu.sdsmt.cs492.example10.contentprovider.model.Contact;
 import edu.sdsmt.cs492.example10.contentprovider.model.ContactModel;
 import edu.sdsmt.cs492.example10.contentprovider.model.ContactProvider;
 
 public class Test3ContactProvider extends ProviderTestCase2<ContactProvider>
 {
 
 	private static final String TAG = Test3ContactProvider.class.getSimpleName();
 	
 	private static MockContentResolver _resolver;
 	
 	private ContactModel _contactModel;
 	
 	private int _numberOfContacts = 0;
 
 	public Test3ContactProvider()
 	{
 		super(ContactProvider.class, ContactProvider.AUTHORITY);
 	}
 	
 	public Test3ContactProvider(Class<ContactProvider> providerClass, String providerAuthority)
 	{
 		super(providerClass, providerAuthority);
 	}
 
 	@Before
 	protected void setUp() throws Exception
 	{
 		Log.i(TAG, "setUp() called");
 		
 		super.setUp();
 		
 		// Delete and insert dummy contacts.
 		_contactModel = ContactModel.getInstance(getContext());
 		_contactModel.deleteAllContacts();
 		_numberOfContacts = _contactModel.insertDummyContacts(false);
 		
 		// http://www.flashics.com/2012/09/06/testing-content-providers-android-programming/
 		_resolver = this.getMockContentResolver();
 		
 	}
 
 	@After
 	protected void tearDown() throws Exception
 	{
 		Log.i(TAG, "tearDown() called");
 		
 		Log.d(TAG, "Deleted: " + _contactModel.deleteAllContacts());
 		
 		_contactModel = null;
 		
 		super.tearDown();
 	}
 	
	@Override 
	@Suppress
 	public void testAndroidTestCaseSetupProperly()
 	{
 		
 	}
 
 	@Test
 	public void testQuery()
 	{
 		Log.d(TAG,"testQuery() called");
 		
 		Uri uri = ContactProvider.URI_CONTACTS;
 		String[] projection = Contact.COLUMN_NAMES;
 		String selection = null;
 		String[] selectionArgs = null;
 		String sortOrder = Contact.COLUMN_CONTACT_NAME + " ASC";
         
         Cursor result = _resolver.query(uri, projection, selection, selectionArgs, sortOrder);
         
         assertEquals("Contact query results", result.getCount(), _numberOfContacts);
         
         assertEquals("Number of columns does not match", result.getColumnCount(), Contact.COLUMN_NAMES.length);
         
         while (result.moveToNext() == true)
         {
             String id = result.getString(result.getColumnIndex(Contact.COLUMN_ID));
             String name = result.getString(result.getColumnIndex(Contact.COLUMN_CONTACT_NAME));
             
             Log.i(TAG, id + " : " + name);
         }
 	}
 	
 }
