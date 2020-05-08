 package net.creativelift.sqlitefun.test;
 
import net.creativelift.sqlitefun.FunDBAdapter;
 import android.content.Context;
 import android.database.Cursor;
 import android.test.AndroidTestCase;
 
 public class FunDbAdapterTest extends AndroidTestCase {
 
     private FunDBAdapter dbAdapter;
     private Context testContext;
 
 	@Override
     protected void setUp() throws Exception {
         super.setUp();
         testContext = this.getContext();
 		dbAdapter = new FunDBAdapter(testContext);
 		//dbAdapter.open();
     }
 
 	protected void tearDown() throws Exception {
 		//dbAdapter.close();
 	}
 
 	public void testFetchingASpecificCampaign() {
 		dbAdapter.open();
 		long new_row_id = dbAdapter.createFunRecord("specific record");
 		Cursor specific_record= dbAdapter.fetchFunRecord(new_row_id);
 		specific_record.moveToFirst();
 		String result = specific_record.getString(1);
 		assertEquals("specific record", result);
 		dbAdapter.close();
 	}
 }
