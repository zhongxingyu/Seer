 package com.example.androidpart3;
 
 import android.widget.TextView;
 
 import org.junit.Assert;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.robolectric.Robolectric;
 import org.robolectric.RobolectricTestRunner;
 
 /**
  * Created by jacco on 9/27/13.
  */
 @RunWith(RobolectricTestRunner.class)
 public class MainActivityTest {
     @Test
     public void shouldWelcome() throws Exception {
 
         MainActivity activity;
         activity = Robolectric.buildActivity(MainActivity.class).create().get();
         Assert.assertNotNull(activity);
 
         TextView tView;
         tView = (TextView) activity.findViewById(R.id.introText);
         Assert.assertNotNull(tView);
 
         String introText;
         introText = tView.getText().toString();
         Assert.assertNotNull(introText);
 
        Assert.assertTrue("Check intro text", introText.equals("Hello world"));
 
     }
 
 }
