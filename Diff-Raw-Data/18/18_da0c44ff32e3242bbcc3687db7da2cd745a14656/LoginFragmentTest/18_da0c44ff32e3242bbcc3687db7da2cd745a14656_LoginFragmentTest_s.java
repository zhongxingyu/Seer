 package com.zipcar.android.test;
 
 import com.zipcar.testingprototype.LoginFragment;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 
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
    public void shouldBeTrue() throws Exception {
        assertThat(true, equalTo(true));
     }
 }
