 package edu.csufresno.mycsufi.test;
 
 import java.util.ArrayList;
 
 import android.test.ActivityInstrumentationTestCase2;
 import android.widget.TextView;
 import edu.csufresno.mycsufi.DBAdapter;
 import edu.csufresno.mycsufi.MyCSUFi;
 import edu.csufresno.mycsufi.StudentClass;
 
 public class MyCSUFiTest extends ActivityInstrumentationTestCase2<MyCSUFi> {
 	private MyCSUFi mActivity;
 	private TextView mView;
 	DBAdapter db;
 	private ArrayList<StudentClass> sclass;
 	
 	public MyCSUFiTest() {
 		super("edu.csufresno.mycsufi", MyCSUFi.class);
 		sclass = new ArrayList<StudentClass>();
 	}
 	
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		mActivity = this.getActivity();
 		mView = (TextView) mActivity.findViewById(edu.csufresno.mycsufi.R.id.TextViewMain);
		db = new DBAdapter(mActivity.getBaseContext());
 	}
 	
 	public void testPreconditions() {
 		assertNotNull(mView);
 		assertNotNull(sclass);
		assertNotNull(db);
 	}
 	
 	public void testDBInsertions () {    	
     	sclass.add(new StudentClass("CSCI150", "Liu", "102", "Ag Sci", "3:00pm", "3:50pm", "MoWe"));
 		sclass.add(new StudentClass("CSCI115", "Seki", "108", "Mckee Fisk", "12:00pm", "12:50pm", "MoWeFr"));
 		sclass.add(new StudentClass("CSCI113", "Jin", "108", "Mckee Fisk", "8:00am", "8:50am", "MoWeFr"));
 		sclass.add(new StudentClass("MUSIC171", "Hooshmandrad", "167", "Music", "11:00am", "11:50am", "MoWeFr"));
 		
 		// make sure four entries were added
 		assertTrue(sclass.size() == 4);
 		
 		// open database to test insertion
 		db.open();
 		
 		// make sure db opened successfully
 		assertTrue(db.isOpen());
 		
 		for( int i=0; i<sclass.size(); i++ ) {
 			// make sure there are no errors in insertion
 			assertFalse(db.insertStudentClass(sclass.get(i)) == -1);
 		}		
 	    db.close();
 	    
 	    // make sure database is not closed
 	    assertFalse(db.isOpen());
     }
 }
