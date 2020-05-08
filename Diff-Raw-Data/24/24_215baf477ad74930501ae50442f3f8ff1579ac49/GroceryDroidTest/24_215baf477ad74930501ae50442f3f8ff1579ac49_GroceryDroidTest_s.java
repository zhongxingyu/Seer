 package edu.rosehulman.grocerydroid.test;
 
 import android.test.ActivityInstrumentationTestCase2;
 import android.widget.TextView;
 import edu.rosehulman.grocerydroid.MainActivity;
 
 /**
  * Tests the Main Activity.
  *
  * @author Matthew Boutell.
  *         Created Mar 26, 2012.
  */
 public class GroceryDroidTest extends
 		ActivityInstrumentationTestCase2<MainActivity> {
 
 	private MainActivity mActivity;
 	private TextView mView;
 	private String resourceString;
 
 	/**
 	 * Calls another constructor with the given hardcoded info.
 	 *
 	 * @param activityClass
 	 */
 	public GroceryDroidTest() {
 		super(MainActivity.class);
 //		super("edu.rosehulman.grocerydroid", MainActivity.class);
 	}
 
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		this.mActivity = this.getActivity();
 		this.mView = (TextView)this.mActivity.findViewById(edu.rosehulman.grocerydroid.R.id.textview);
 		this.resourceString = this.mActivity.getString(edu.rosehulman.grocerydroid.R.string.hello);
 	}
 	
 	
 	public void testPreconditions() {
 		assertNotNull(this.mView);
 	}
 	
 	public void testText() {
 		assertEquals(this.resourceString, (String)this.mView.getText());
 	}
 	
	public void testConstructItem() {
		
	}
	
	
	
	
	
 }
