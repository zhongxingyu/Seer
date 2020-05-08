 package com.zipcar.android.test;
 
 import android.content.Intent;
 import android.support.v4.app.Fragment;
 import android.support.v4.app.FragmentActivity;
 import android.support.v4.app.FragmentManager;
 import android.support.v4.app.FragmentTransaction;
 
 import com.zipcar.testingprototype.ListActivity;
 import com.zipcar.testingprototype.LoginFragment;
 import com.zipcar.testingprototype.data.DataErrorEvent;
 import com.zipcar.testingprototype.models.Session;
import com.zipcar.testingprototype.shared.AndroidModule;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.robolectric.Robolectric;
 import org.robolectric.shadows.ShadowActivity;
 import org.robolectric.shadows.ShadowToast;
 
import dagger.ObjectGraph;

 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.junit.Assert.assertThat;
 import static org.robolectric.Robolectric.shadowOf;
 
 @RunWith(RobolectricGradleTestRunner.class)
 public class LoginFragmentTest {
     private LoginFragment fragment;
    private ObjectGraph objectGraph;
 
     public static void startFragment( Fragment fragment )
     {
         FragmentActivity activity = Robolectric.buildActivity(FragmentActivity.class)
                 .create()
                 .start()
                 .resume()
                 .get();
 
         FragmentManager fragmentManager = activity.getSupportFragmentManager();
         FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
         fragmentTransaction.add( fragment, null );
         fragmentTransaction.commit();
     }
 
     @Before
     public void setUp() {
         fragment = new LoginFragment();

        objectGraph = ObjectGraph.create(new AndroidModule(null));
        objectGraph.inject(fragment);

         startFragment(fragment);
     }
 
     @Test
     public void testHttpError() throws Exception {
         fragment.onSessionError(new DataErrorEvent<Session>(401, null));
         assertThat(ShadowToast.getTextOfLatestToast(), equalTo("There was an error contacting the server: 401"));
     }
 
     @Test(expected = RuntimeException.class)
     public void testInvalidHttpError() throws Exception {
         fragment.onSessionError(new DataErrorEvent<Session>(0, null));
     }
 
     @Test
     public void testZapiError() throws Exception {
         fragment.onSessionError(new DataErrorEvent<Session>("Test ZAPI error", null));
         assertThat(ShadowToast.getTextOfLatestToast(), equalTo("Test ZAPI error"));
     }
 
     @Test
     public void testSessionAvailable() throws Exception {
         fragment.onSessionAvailable(new Session("someToken"));
 
         ShadowActivity shadowActivity = shadowOf(fragment.getActivity());
         Intent startedIntent = shadowActivity.getNextStartedActivity();
 
         assertThat(startedIntent.getComponent().getClassName(),
                 equalTo(ListActivity.class.getName()));
     }
 }
