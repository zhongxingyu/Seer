 package karyon.android.activities;
 
 import android.content.res.Configuration;
 import android.os.Bundle;
 import android.view.Window;
 import karyon.android.CustomRoboTestRunner;
 import karyon.android.R;
 import karyon.dynamicCode.Java;
 import karyon.testing.KaryonTest;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.robolectric.Robolectric;
 import org.robolectric.util.ActivityController;
 
 import java.lang.reflect.Method;
 
 import static org.junit.Assert.*;
 import static org.junit.Assert.assertEquals;
 
 /**
  * Created with IntelliJ IDEA.
  * User: kmchugh
  * Date: 16/9/13
  * Time: 3:47 PM
  * To change this template use File | Settings | File Templates.
  */
 @RunWith(CustomRoboTestRunner.class)
 public class BaseActivityTest
     extends KaryonTest
 {
     public static class TestActivity2
         extends SplashActivity
     {
         private boolean m_lInitialised;
 
         public boolean isInitialised()
         {
             return m_lInitialised;
         }
 
         public TestActivity2()
         {
             super(new ActivityImpl<SplashActivity>(){
 
                 @Override
                 public void onStart()
                 {
                     ((TestActivity2)getActivity()).m_lInitialised = true;
                 }
             });
 
             getImpl().setActivity(this);
         }
     }
 
     public static class TestActivity
         extends SplashActivity
     {
         private boolean m_lInitialised;
         private boolean m_lIsRestarted;
         private boolean m_lIsStarted;
         private boolean m_lIsResumed;
         private boolean m_lIsPaused;
         private boolean m_lIsStopped;
         private boolean m_lIsDestroyed;
         private boolean m_lLowMemory;
 
         public boolean isInitialised()
         {
             return m_lInitialised;
         }
 
         public boolean isRestarted()
         {
             return m_lIsRestarted;
         }
 
         public boolean isStarted()
         {
             return m_lIsStarted;
         }
 
         public boolean isResumed()
         {
             return m_lIsResumed;
         }
 
         public boolean isPaused()
         {
             return m_lIsPaused;
         }
 
         public boolean isStopped()
         {
             return m_lIsStopped;
         }
 
         public boolean isDestroyed()
         {
             return m_lIsDestroyed;
         }
 
         private boolean isLowMemory()
         {
             return m_lLowMemory;
         }
 
         @Override
         public boolean onInit(Bundle toSavedState)
         {
             m_lInitialised = true;
             return m_lInitialised;
         }
 
         @Override
         public void onRestarted()
         {
             m_lIsRestarted = true;
         }
 
         @Override
         public void onStarted()
         {
             m_lIsStarted = true;
         }
 
         @Override
         public void onResumed()
         {
             m_lIsResumed = true;
         }
 
         @Override
         public void onPaused()
         {
             m_lIsPaused = true;
         }
 
         @Override
         public void onStopped()
         {
             m_lIsStopped = true;
         }
 
         @Override
         public void onDestroyed()
         {
             m_lIsDestroyed = true;
         }
 
         @Override
         public void onMemoryLow()
         {
             m_lLowMemory = true;
         }
 
         @Override
         public int getLandscapeViewResourceID()
         {
             return R.layout.error;
         }
     }
 
     @Test
     public void testInstantiate() throws Exception
     {
         startMarker();
         SplashActivity loActivity = BaseActivity.instantiate(SplashActivity.class);
         assertNotNull(loActivity);
         assertEquals(SplashActivity.class, loActivity.getClass());
     }
 
     @Test
     public void testConstructor_implementation() throws Exception
     {
         startMarker();
         ActivityController<TestActivity2> loActivity = Robolectric.buildActivity(TestActivity2.class);
 
         assertFalse(loActivity.get().isInitialised());
 
         loActivity.create();
         loActivity.start();
 
         assertTrue(loActivity.get().isInitialised());
     }
 
     @Test
     public void testGetImpl() throws Exception
     {
         startMarker();
 
         startMarker();
         ActivityController<TestActivity> loActivity = Robolectric.buildActivity(TestActivity.class);
         ActivityController<TestActivity> loActivity1 = Robolectric.buildActivity(TestActivity.class);
         ActivityController<TestActivity2> loActivity2 = Robolectric.buildActivity(TestActivity2.class);
         ActivityController<TestActivity2> loActivity3 = Robolectric.buildActivity(TestActivity2.class);
 
         assertNotNull(loActivity.get().getImpl());
         assertNotNull(loActivity1.get().getImpl());
         assertNotNull(loActivity2.get().getImpl());
         assertNotNull(loActivity3.get().getImpl());
 
         assertNotSame(loActivity.get().getImpl().getClass(), loActivity2.get().getImpl().getClass());
         assertSame(loActivity.get().getImpl().getClass(), loActivity1.get().getImpl().getClass());
         assertSame(loActivity2.get().getImpl().getClass(), loActivity3.get().getImpl().getClass());
     }
 
     @Test
     public void testGetContentViewID_landscape() throws Exception
     {
         startMarker();
         ActivityController<TestActivity> loActivity = Robolectric.buildActivity(TestActivity.class);
         loActivity.get().getResources().getConfiguration().orientation = Configuration.ORIENTATION_LANDSCAPE;
         loActivity.create();
         assertEquals(loActivity.get().getLandscapeViewResourceID(), loActivity.get().getContentViewID());
     }
 
     @Test
     public void testGetContentViewID_portrait() throws Exception
     {
         startMarker();
         ActivityController<TestActivity> loActivity = Robolectric.buildActivity(TestActivity.class);
         loActivity.get().getResources().getConfiguration().orientation = Configuration.ORIENTATION_PORTRAIT;
         loActivity.create();
         assertEquals(loActivity.get().getPortraitViewResourceID(), loActivity.get().getContentViewID());
     }
 
     @Test
     public void testGetPortraitViewResourceID()
             throws Exception
     {
         startMarker();
         ActivityController<TestActivity> loActivity = Robolectric.buildActivity(TestActivity.class);
         assertEquals(R.layout.splash, loActivity.get().getPortraitViewResourceID());
     }
 
     @Test
     public void testGetLandscapeViewResourceID() throws Exception
     {
         startMarker();
         ActivityController<TestActivity> loActivity = Robolectric.buildActivity(TestActivity.class);
         assertEquals(R.layout.error, loActivity.get().getLandscapeViewResourceID());
     }
 
     @Test
     public void testCanShowTitle() throws Exception
     {
         startMarker();
         ActivityController<TestActivity> loActivity = Robolectric.buildActivity(TestActivity.class);
        assertFalse(loActivity.get().canShowTitle());
     }
 
     @Test
     public void testGetCustomTitleDrawable() throws Exception
     {
         startMarker();
         ActivityController<TestActivity> loActivity = Robolectric.buildActivity(TestActivity.class);
         assertEquals(0, loActivity.get().getCustomTitleDrawable());
     }
 
     @Test
     public void testSetWindowFeature() throws Exception
     {
         startMarker();
         ActivityController<TestActivity> loActivity = Robolectric.buildActivity(TestActivity.class);
         assertTrue(loActivity.get().setWindowFeature(Window.FEATURE_NO_TITLE));
     }
 
     @Test
     public void testOnCreate() throws Exception
     {
         startMarker();
         ActivityController<TestActivity> loActivity = Robolectric.buildActivity(TestActivity.class);
 
         assertFalse(loActivity.get().isInitialised());
 
         loActivity.create();
 
         assertTrue(loActivity.get().isInitialised());
     }
 
     @Test
     public void testOnInit() throws Exception
     {
         startMarker();
         ActivityController<TestActivity> loActivity = Robolectric.buildActivity(TestActivity.class);
 
         assertFalse(loActivity.get().isInitialised());
 
         loActivity.create();
 
         assertTrue(loActivity.get().isInitialised());
     }
 
     @Test
     public void testOnRestart() throws Exception
     {
         startMarker();
         ActivityController<TestActivity> loActivity = Robolectric.buildActivity(TestActivity.class);
 
         loActivity.create();
 
         assertFalse(loActivity.get().isRestarted());
 
         loActivity.pause();
 
         // robolectric doesn't seem to call restart, so we do it ourselves
         loActivity.restart();
         Method loMethod = Java.getMethod(TestActivity.class, "onRestart", null);
         if (loMethod != null)
         {
             loMethod.invoke(loActivity.get());
         }
 
         assertTrue(loActivity.get().isRestarted());
     }
 
     @Test
     public void testOnStart() throws Exception
     {
         startMarker();
         ActivityController<TestActivity> loActivity = Robolectric.buildActivity(TestActivity.class);
 
         loActivity.create();
 
         assertFalse(loActivity.get().isStarted());
 
         loActivity.start();
 
         assertTrue(loActivity.get().isStarted());
     }
 
     @Test
     public void testOnResume() throws Exception
     {
         startMarker();
         ActivityController<TestActivity> loActivity = Robolectric.buildActivity(TestActivity.class);
 
         loActivity.create();
 
         assertFalse(loActivity.get().isResumed());
 
         loActivity.start();
         loActivity.pause();
         loActivity.resume();
 
         assertTrue(loActivity.get().isResumed());
     }
 
     @Test
     public void testOnPause() throws Exception
     {
         startMarker();
         ActivityController<TestActivity> loActivity = Robolectric.buildActivity(TestActivity.class);
 
         loActivity.create();
 
         assertFalse(loActivity.get().isPaused());
 
         loActivity.start();
         loActivity.pause();
 
         assertTrue(loActivity.get().isPaused());
     }
 
     @Test
     public void testOnStop() throws Exception
     {
         startMarker();
         ActivityController<TestActivity> loActivity = Robolectric.buildActivity(TestActivity.class);
 
         loActivity.create();
 
         assertFalse(loActivity.get().isStopped());
 
         loActivity.start();
         loActivity.stop();
 
         assertTrue(loActivity.get().isStopped());
     }
 
     @Test
     public void testOnDestroy() throws Exception
     {
         startMarker();
         ActivityController<TestActivity> loActivity = Robolectric.buildActivity(TestActivity.class);
 
         loActivity.create();
 
         assertFalse(loActivity.get().isDestroyed());
 
         loActivity.start();
         loActivity.stop();
         loActivity.destroy();
 
         assertTrue(loActivity.get().isDestroyed());
     }
 
     @Test
     public void testOnMemoryLow() throws Exception
     {
         startMarker();
         ActivityController<TestActivity> loActivity = Robolectric.buildActivity(TestActivity.class);
 
         loActivity.create();
 
         assertFalse(loActivity.get().isLowMemory());
 
         Method loMethod = Java.getMethod(TestActivity.class, "onLowMemory", null);
         if (loMethod != null)
         {
             loMethod.invoke(loActivity.get());
         }
 
         assertTrue(loActivity.get().isLowMemory());
     }
 
     @Test
     public void testGetContext() throws Exception
     {
         startMarker();
         ActivityController<TestActivity> loActivity = Robolectric.buildActivity(TestActivity.class);
 
 
         assertSame(loActivity.get(), loActivity.get().getContext());
     }
 }
