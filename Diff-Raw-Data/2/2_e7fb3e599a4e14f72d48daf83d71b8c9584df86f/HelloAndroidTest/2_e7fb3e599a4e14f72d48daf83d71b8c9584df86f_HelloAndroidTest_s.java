 package eu.masconsult.hello.test;
 
 import eu.masconsult.hello.HelloAndroidActivity;
 import android.test.ActivityInstrumentationTestCase2;
 import android.widget.TextView;
 
 public class HelloAndroidTest extends ActivityInstrumentationTestCase2<HelloAndroidActivity> {
     private HelloAndroidActivity mActivity;  // the activity under test
     private TextView mView;          // the activity's TextView (the only view)
     private String resourceString;
 
     public HelloAndroidTest() {
       super("eu.masconsult.hello", HelloAndroidActivity.class);
     }
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         mActivity = this.getActivity();
         mView = (TextView) mActivity.findViewById(eu.masconsult.hello.R.id.textview);
         resourceString = mActivity.getString(eu.masconsult.hello.R.string.msg);
     }
     
     public void testPreconditions() {
      assertNull(mView);
     }
     
     public void testText() {
       assertEquals(resourceString,(String)mView.getText());
     }
 }
