 package com.cs301w01.meatload.test.ActivityTests;
 
 import com.cs301w01.meatload.R;
 import com.cs301w01.meatload.activities.ViewTagsActivity;
 
 import android.app.Instrumentation;
 import android.content.Context;
 import android.test.ActivityInstrumentationTestCase2;
 import android.widget.EditText;
 import android.widget.ListView;
 
 public class ViewTagsActivityTest extends ActivityInstrumentationTestCase2<ViewTagsActivity> {
 	private Instrumentation mInstrumentation;
 	private Context mContext;
 	private ViewTagsActivity mActivity;
 	private final int SLEEP_TIME = 500;
 
 	public ViewTagsActivityTest() {
 		super("com.cs301w01.meatload", ViewTagsActivity.class);
 	}
 
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 
 		mActivity = getActivity();
 		mContext = mActivity.getBaseContext();
 	}
 
 	@Override
 	protected void tearDown() throws Exception {
 		super.tearDown();
 
 		if (mActivity != null) {
 			mActivity.finish();
 		}
 
 	}
 
 	public void testBlankTag() {
 		// display all possible tags
 	}
 
 	public void testSpecificTag() {
 		final EditText searchField = (EditText) mActivity.findViewById(R.id.tagSearchEditText);
 		mActivity.runOnUiThread(new Runnable() {
 			public void run() {
 				searchField.setText("Fish");
 			}
 		});
 		sleep();
 
 		ListView allTagsLV = (ListView) mActivity.findViewById(R.id.tagListView);
 		assertEquals(allTagsLV.getChildCount(), 1);
 		assertEquals(allTagsLV.getItemAtPosition(0), "Fish");
 	}
 
 	public void testGenericTag() {
 		// enter AA and ensure all tags with AA in them appear
 	}
 
 	public void testAddTag() {
 		// Click on tag, ensure it is removed from top group and added to bottom
 		ListView allTagsLV = (ListView) mActivity.findViewById(R.id.tagListView);
 		// TODO: How do you click on a list item again?
 		
 		ListView selectedTagsLV = (ListView) mActivity.findViewById(R.id.selectedTagsListView);
		//assertEquals(selectedTagsLV.getItemAtPosition(0), "Fish");
 	}
 
 	public void testTwoTags() {
 		// select two tags, ensure both added below and that number of photos is
 		// correct
 	}
 
 	private void sleep() {
 		try {
 			Thread.sleep(SLEEP_TIME);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 			assertTrue("Sleep failed", false);
 		}
 	}
 
 }
