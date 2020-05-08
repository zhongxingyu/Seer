 package ezhun.smsb.content;
 
 import android.content.ContentResolver;
 import android.content.ContentValues;
 import android.database.Cursor;
 import android.net.Uri;
 import android.test.ProviderTestCase2;
 import ezhun.smsb.storage.SmsContentProvider;
 import junit.framework.Assert;
 
 public class SmsContentProviderTest extends ProviderTestCase2<SmsContentProvider> {
 	private static final String SENDER = "(069) 129 09 45";
 	private static final int SETUP_COUNT = 7;
 	
 	public SmsContentProviderTest() {
 		super(SmsContentProvider.class, SmsContentProvider.PROVIDER_NAME);
 	}
 	
 	public void setUp() throws Exception{
 		super.setUp();
 		ContentResolver c = this.getMockContentResolver();
 		
 		ContentValues[] values = new ContentValues[SETUP_COUNT];
 		for (int i = 0; i<SETUP_COUNT; i++) {
 			values[i] = new ContentValues();
 			values[i].put(SmsContentProvider.SENDER, SENDER);
 			values[i].put(SmsContentProvider.MESSAGE, "message from setUp #");
 			values[i].put(SmsContentProvider.RECEIVED, (int) (System.currentTimeMillis() / 1000L));
 			values[i].put(SmsContentProvider.SYSTEM_FLAG_SPAM, 0);
 			values[i].put(SmsContentProvider.USER_FLAG_NOT_SPAM, 0);
 			values[i].put(SmsContentProvider.USER_FLAG_SPAM, 0);
 		}
 		c.bulkInsert(SmsContentProvider.CONTENT_URI, values);
 	}
 	
 	public void tearDown() throws Exception {
 		super.tearDown();
 		ContentResolver c = this.getMockContentResolver();
 		c.delete(SmsContentProvider.CONTENT_URI, null, null);
 	}
 
     public void testOnCreate() {
 		String[] dbList = this.getContext().databaseList();
 		if(dbList.length < 1){
 			fail("list of availbale databases is empty");
 		}
 		Assert.assertEquals(dbList[0], "test.sms.db");
 	}
 
 	public void testQuery() {
 		ContentResolver c = this.getMockContentResolver();
 		String[] selectionArgs = { SENDER };
 		Cursor cur = c.query(SmsContentProvider.CONTENT_URI, null, SmsContentProvider.SENDER + " like :1", selectionArgs, null);
		try {
			Assert.assertEquals(SETUP_COUNT, cur.getCount());
		} finally {
			cur.close();
		}
 	}
 
 	public void testGetType() {
 		Uri smsList = Uri.parse("content://" + SmsContentProvider.PROVIDER_NAME + "/sms");
 		Uri sms = Uri.parse("content://" + SmsContentProvider.PROVIDER_NAME + "/sms/13");
 		
 		Assert.assertEquals(SmsContentProvider.TYPE_SMS_LIST, this.getProvider().getType(smsList));
 		Assert.assertEquals(SmsContentProvider.TYPE_SMS, this.getProvider().getType(sms));
 	}
 
 	public void testInsert() {
 		String sender = "097 112 332 6";
 		String message = "this is a test message";
 		int received = (int) (System.currentTimeMillis() / 1000L); 
 		ContentResolver c = this.getMockContentResolver();
 		
 		ContentValues values = new ContentValues();
 		values.put(SmsContentProvider.SENDER, sender);
 		values.put(SmsContentProvider.MESSAGE, message);
 		values.put(SmsContentProvider.RECEIVED, received);
 		values.put(SmsContentProvider.SYSTEM_FLAG_SPAM, 0);
 		values.put(SmsContentProvider.USER_FLAG_NOT_SPAM, 0);
 		values.put(SmsContentProvider.USER_FLAG_SPAM, 0);
 
 		Uri uri = c.insert(SmsContentProvider.CONTENT_URI, values);
 		String id = uri.getLastPathSegment();
 		
 		//now test they are added correctly
 		String[] projection = new String[] {
 				SmsContentProvider._ID,
                 SmsContentProvider.SENDER,
                 SmsContentProvider.MESSAGE,
                 SmsContentProvider.RECEIVED,
                 SmsContentProvider.SYSTEM_FLAG_SPAM,
                 SmsContentProvider.USER_FLAG_NOT_SPAM,
                 SmsContentProvider.USER_FLAG_SPAM};
 		Cursor cur = c.query(uri, projection, null, null, null);
 		try {
 			cur.moveToFirst();
 			Assert.assertEquals(cur.getCount(), 1);
 			Assert.assertEquals(cur.getString(cur.getColumnIndex(SmsContentProvider._ID)), id);
 			Assert.assertEquals(cur.getString(cur.getColumnIndex(SmsContentProvider.SENDER)), sender);
 			Assert.assertEquals(cur.getString(cur.getColumnIndex(SmsContentProvider.MESSAGE)), message);
 			Assert.assertEquals(cur.getInt(cur.getColumnIndex(SmsContentProvider.RECEIVED)), received);
 			Assert.assertEquals(cur.getInt(cur.getColumnIndex(SmsContentProvider.SYSTEM_FLAG_SPAM)), 0);
 			Assert.assertEquals(cur.getInt(cur.getColumnIndex(SmsContentProvider.USER_FLAG_NOT_SPAM)), 0);
 			Assert.assertEquals(cur.getInt(cur.getColumnIndex(SmsContentProvider.USER_FLAG_SPAM)), 0);
 		} finally {
 			cur.close();
 		}
 	}
 
 	public void testDeleteBulk() {
 		ContentResolver c = this.getMockContentResolver();
 		String[] selectionArgs = { SENDER };
 		int count = c.delete(SmsContentProvider.CONTENT_URI, SmsContentProvider.SENDER + " like :1", selectionArgs);
 		Assert.assertEquals(SETUP_COUNT, count);
 	}
 	
 	public void testDeleteById() {
 		ContentResolver c = this.getMockContentResolver();
 		
 		ContentValues values = new ContentValues();
 		values.put(SmsContentProvider.SENDER, "Unknown");
 		values.put(SmsContentProvider.MESSAGE, "Bomba! Putin!");
 		values.put(SmsContentProvider.RECEIVED, (int) (System.currentTimeMillis() / 1000L));
 		values.put(SmsContentProvider.SYSTEM_FLAG_SPAM, 0);
 		values.put(SmsContentProvider.USER_FLAG_NOT_SPAM, 0);
 		values.put(SmsContentProvider.USER_FLAG_SPAM, 0);
 
 		Uri uri = c.insert(SmsContentProvider.CONTENT_URI, values);
 		int count = c.delete(uri, null, null);
 		Assert.assertEquals(1, count);
 	}
 
 	public void testUpdateBulk() {
 		ContentResolver c = this.getMockContentResolver();
 		ContentValues values = new ContentValues();
 		values.put(SmsContentProvider.USER_FLAG_SPAM, 1);
 		int count = c.update(SmsContentProvider.CONTENT_URI, values, null, null);
 		Assert.assertEquals(SETUP_COUNT, count);
 		
 		String[] selectionArgs = {"1"};
 		Cursor cur = c.query(SmsContentProvider.CONTENT_URI, null, SmsContentProvider.USER_FLAG_SPAM + " = :1", selectionArgs, null);
		try {
			Assert.assertEquals(SETUP_COUNT, cur.getCount());
		} finally {
			cur.close();
		}
 	}
 	
 	public void testUpdateById() {
 		String message = "Putin! Bomba! KGB!";
 		
 		ContentResolver c = this.getMockContentResolver();
 		Cursor cur = c.query(SmsContentProvider.CONTENT_URI, null, null, null, null);
 		String id;
 		
 		try {
 			cur.moveToFirst();
 			id = cur.getString(cur.getColumnIndex(SmsContentProvider._ID));
 		} finally {
 			cur.close();
 		}
 		
 		Uri entity = Uri.parse(SmsContentProvider.CONTENT_URI.toString() + "/" + id);
 		ContentValues values = new ContentValues();
 		values.put(SmsContentProvider.MESSAGE, message);
 		values.put(SmsContentProvider.SYSTEM_FLAG_SPAM, 1);
 		int count = c.update(entity, values, null, null);
 		Assert.assertEquals(1, count);
 		
 		String[] selectionArgs = { message };
 		Cursor updcur = c.query(SmsContentProvider.CONTENT_URI, null, SmsContentProvider.MESSAGE + " = :1", selectionArgs, null); 
 		try {
 			updcur.moveToFirst();
 			int value = updcur.getInt(updcur.getColumnIndex(SmsContentProvider.SYSTEM_FLAG_SPAM));
 			Assert.assertEquals(1, updcur.getCount());
 			Assert.assertEquals(1, value);
 		} finally {
 			updcur.close();
 		}
 	}
 }
