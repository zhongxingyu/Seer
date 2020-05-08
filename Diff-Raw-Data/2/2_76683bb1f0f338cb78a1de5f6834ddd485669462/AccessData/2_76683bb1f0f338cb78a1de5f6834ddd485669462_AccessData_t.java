 package com.shealevy.android.model.test.feature;
 
 import com.shealevy.android.model.test.TestTableDelegate;
 
 import android.test.AndroidTestCase;
 
 public class AccessData extends AndroidTestCase {
 	/*
 	 * As an Android developer
 	 * I want to be able to access the data given by providers
 	 * So that my apps can do use the data easily
 	 */
 	private AndroidModelStepDefinitions stepDefs;
 
 	public AccessData() {
 		super();
 	}
 
 	protected void setUp() throws Exception {
 		super.setUp();
 		stepDefs = new AndroidModelStepDefinitions(getContext());
 	}
 
 	protected void tearDown() throws Exception {
 		super.tearDown();
 	}
 	
 	public void testLoadingData() {
 		// Given a TestTableDelegate AndroidModel called "model1"
 		stepDefs.givenATestTableDelegateAndroidModelCalled_("model1");
 		// And a TestTableDelegate AndroidModel called "model2"
 		stepDefs.givenATestTableDelegateAndroidModelCalled_("model2");
 		
 		// When I set the ID of TestTableDelegate AndroidModel "model1" to 2
 		stepDefs.whenISetThe_OfTestTableDelegateAndroidModel_To_(TestTableDelegate.Field.ID, "model1", 2);
 		// And I load TestTableDelegate AndroidModel "model1" with a fake ContentResolver
 		stepDefs.whenILoadTestTableDelegateAndroidModel_WithAFakeContentResolver("model1");
 		// And I set the NAME of TestTableDelegate AndroidModel "model2" to "ThirdTest"
		stepDefs.whenISetThe_OfTestTableDelegateAndroidModel_To_(TestTableDelegate.Field.NAME, "model2", "ThirdTest");
 		// And I load TestTableDelegate AndroidModel "model2" with a fake ContentResolver
 		stepDefs.whenILoadTestTableDelegateAndroidModel_WithAFakeContentResolver("model2");
 		
 		// Then the NAME of TestTableDelegate AndroidModel "model1" should be set to "SecondTest"
 		stepDefs.thenThe_OfTestTableDelegateAndroidModel_ShouldBeSetTo_(TestTableDelegate.Field.NAME, "model1", "SecondTest");
 		// And the ID of TestTableDelegate AndroidModel "model2" should be set to 3
 		stepDefs.thenThe_OfTestTableDelegateAndroidModel_ShouldBeSetTo_(TestTableDelegate.Field.ID, "model2", 3);
 	}
 
 }
