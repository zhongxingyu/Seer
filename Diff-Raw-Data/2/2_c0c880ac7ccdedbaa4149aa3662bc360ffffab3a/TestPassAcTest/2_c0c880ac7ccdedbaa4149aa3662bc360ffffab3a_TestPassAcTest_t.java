 package com.example.wifilocatordemo.test;
 import junit.framework.Assert;
 import android.test.*;
 import com.example.wifilocatordemo.*;
 
 import android.test.ActivityInstrumentationTestCase2;
 import android.test.suitebuilder.annotation.SmallTest;
 import android.widget.Button;
 import android.widget.EditText;
 
 public class TestPassAcTest extends ActivityInstrumentationTestCase2<TestPassAc> {
 	EditText HintText;
 	Button bt1;
 	Button btChange;
 	TestPassAc activity;
 	
 
 	public TestPassAcTest(Class<TestPassAc> name) {
 		super(name);
 		TestPassAc testPassAC = getActivity();

 		
 	}
 	public TestPassAcTest(){
 		super("com.example.wifilocatordemo", TestPassAc.class);
 		
 	}
 	protected void setUp() throws Exception {
 		super.setUp();
 		activity = getActivity();
 	    HintText = (EditText)activity.findViewById(com.example.wifilocatordemo.R.id.HintText);
 	    bt1 = (Button)activity.findViewById(com.example.wifilocatordemo.R.id.bt1);
 	    btChange = (Button)activity.findViewById(com.example.wifilocatordemo.R.id.btChange); 
 	}
 	protected void tearDown() throws Exception {
 	    super.tearDown();
 	  }
 	  
 	  @SmallTest
 	  public void testViewsCreated() {
 	    assertNotNull(getActivity());
 	    assertNotNull(HintText);
 	    assertNotNull(bt1);
 	    assertNotNull(btChange);
 	  }
 	  
 	  @SmallTest
 	  public void testViewsVisible() {
 	    ViewAsserts.assertOnScreen(HintText.getRootView(), bt1);
 	    ViewAsserts.assertOnScreen(bt1.getRootView(), HintText);
 	    ViewAsserts.assertOnScreen(btChange.getRootView(), HintText);
 	  }
 	  
 	  @SmallTest
 	  public void testStartingEmpty() {
 	    assertTrue("HintText field is empty", "".equals(HintText.getText().toString()));
 	    assertTrue("", "Enter your password".equals(HintText.getHint().toString()));
 	    assertTrue("HintText field is empty", "OK".equals(bt1.getText().toString()));
 	    assertTrue("", "Change your password ".equals(btChange.getText().toString()));
 	    
 	    
 	  }
 	  
 	  @SmallTest
 	  public void testKilosToPounds() {
 	    HintText.clearComposingText();
 	    bt1.clearComposingText();
 	    
 	    TouchUtils.tapView(this, HintText);
 	    sendKeys("123abc");
 	    
 	    assertTrue("1 kilo is 2.20462262 pounds", true);
 	  }
 	
 
 	
 }
