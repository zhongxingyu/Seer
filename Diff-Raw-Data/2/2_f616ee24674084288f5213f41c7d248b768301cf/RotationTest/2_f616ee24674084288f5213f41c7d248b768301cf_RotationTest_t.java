 package com.dhemery.victor.examples.tests;
 
 import com.dhemery.victor.examples.runner.OnVigorApp;
 import org.junit.Test;
 
 import static com.dhemery.victor.IosApplicationOrientation.LANDSCAPE;
 import static com.dhemery.victor.IosApplicationOrientation.PORTRAIT;
 import static com.dhemery.victor.examples.application.ApplicationQueries.orientation;
 import static org.hamcrest.Matchers.is;
 
 public class RotationTest extends OnVigorApp {
     @Test
     public void orientationTests() {
        assertThat(application(), orientation(), is(PORTRAIT));
 
         device().rotateLeft();
         assertThat(application(), orientation(), eventually(), is(LANDSCAPE));
 
         device().rotateRight();
         assertThat(application(), orientation(), eventually(), is(PORTRAIT));
 
         device().rotateRight();
         assertThat(application(), orientation(), eventually(), is(LANDSCAPE));
 
         device().rotateRight();
         assertThat(application(), orientation(), eventually(), is(PORTRAIT));
 
         device().rotateRight();
         assertThat(application(), orientation(), eventually(), is(LANDSCAPE));
 
         device().rotateRight();
         assertThat(application(), orientation(), eventually(), is(PORTRAIT));
     }
 }
