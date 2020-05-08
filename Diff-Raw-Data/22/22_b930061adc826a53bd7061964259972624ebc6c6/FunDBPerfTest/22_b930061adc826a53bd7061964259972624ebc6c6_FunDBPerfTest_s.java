 package net.creativelift.sqlitefun.test;
 
import net.creativelift.FunDBAdapter;
 import android.content.Context;
 import android.database.Cursor;
 import android.test.AndroidTestCase;
 import android.test.PerformanceTestCase;
 
 public class FunDBPerfTest extends AndroidTestCase implements PerformanceTestCase {
 	
 	private FunDBAdapter dbAdapter;
 	private Context testContext;
 	
 	private static final int ITERATIONS = 1000;
 	Intermediates i;
 
 	@Override
 	public boolean isPerformanceOnly() {
 		return true;
 	}
 
 	@Override
 	public int startPerformance(Intermediates intermediates) {
 		intermediates.setInternalIterations(ITERATIONS * 10);
 		i = intermediates;
 		return 1;
 		//return 0;
 	}
 	
 	@Override
 	protected void setUp() throws Exception {
         super.setUp();
         testContext = this.getContext();
 		dbAdapter = new FunDBAdapter(testContext);
 		//dbAdapter.open();
 	}
 	
 	public void startTiming(boolean realTime) {
 		if (i != null ) {
 			i.finishTiming(realTime);
 		}
 	}
 	
 	public void finishTiming(boolean realTime) {
 		if (i != null) {
 			i.finishTiming(realTime);
 		}
 	}
 	
 	public void addIntermediate(String name) {
 		if (i != null) {
 			i.addIntermediate(name);
 		}
 	}
 
 	public void addIntermediate(String name, long timeInNS) {
 		if (i != null) {
 			i.addIntermediate(name, timeInNS);
 		}
 	}
 	
 	public void testSomething () {
 		startTiming(true);
 		
 		dbAdapter.open();
 		long new_row_id = dbAdapter.createFunRecord("specific record");
 		Cursor specific_record = dbAdapter.fetchFunRecord(new_row_id);
 		specific_record.moveToFirst();
 		String result = specific_record.getString(1);
 		assertEquals("specific record", result);
 		
 		dbAdapter.close();
 		
 		finishTiming(true);
 	}
 	
 	
 }
