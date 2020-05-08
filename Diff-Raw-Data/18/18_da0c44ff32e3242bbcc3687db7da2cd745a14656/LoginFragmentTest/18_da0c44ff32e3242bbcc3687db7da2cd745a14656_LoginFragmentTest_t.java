 package com.zipcar.android.test;
 
 import com.zipcar.testingprototype.LoginFragment;
import com.zipcar.testingprototype.data.DataErrorEvent;
import com.zipcar.testingprototype.models.Session;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
import org.robolectric.shadows.ShadowToast;
 
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.junit.Assert.assertThat;
 
 @RunWith(RobolectricGradleTestRunner.class)
 public class LoginFragmentTest {
     private LoginFragment fragment;
 
     @Before
     public void setUp() {
         fragment = new LoginFragment();
         fragment.onCreate(null);
     }
 
     @Test
    public void testSessionError() throws Exception {
        fragment.onSessionError(new DataErrorEvent<Session>(401, null));
        assertThat(ShadowToast.getTextOfLatestToast(), equalTo("There was an error contacting the server: 401"));
     }
 }
