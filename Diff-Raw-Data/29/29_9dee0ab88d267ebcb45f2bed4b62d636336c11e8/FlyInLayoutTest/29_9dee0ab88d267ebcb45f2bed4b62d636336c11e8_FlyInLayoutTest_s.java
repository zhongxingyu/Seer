 package karyon.android.layouts;
 
 import android.view.View;
 import android.view.ViewGroup;
 import karyon.android.CustomRoboTestRunner;
 import karyon.android.R;
 import karyon.android.activities.SplashActivity;
 import karyon.android.applications.AndroidApplicationAdaptor;
 import karyon.testing.KaryonTest;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.robolectric.Robolectric;
 import org.robolectric.util.ActivityController;
 
 import static org.junit.Assert.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: kmchugh
  * Date: 20/9/13
  * Time: 10:46 AM
  * To change this template use File | Settings | File Templates.
  */
 @RunWith(CustomRoboTestRunner.class)
 public class FlyInLayoutTest
     extends KaryonTest
 {
     public static class TestLayoutHelper
         extends FlyInLayout.FlyInHelper
     {
         private int m_nLastView;
         private View m_oLastView;
 
         @Override
         protected int translateView(int tnStringID)
         {
             return R.string.cancel;
         }
 
         @Override
         protected void onViewChanged(int tnViewID, View toView)
         {
             m_nLastView = tnViewID;
             m_oLastView = toView;
         }
     }
 
     @Test
     public void testToggleMenu() throws Exception
     {
         startMarker();
 
         ActivityController<SplashActivity> loActivity = Robolectric.buildActivity(SplashActivity.class);
         FlyInLayout loLayout = new FlyInLayout(AndroidApplicationAdaptor.getInstance().getApplicationContext());
 
         loActivity.create();
         loActivity.start();
 
         loActivity.get().setContentView(loLayout);
 
         assertEquals(FlyInLayout.MenuState.CLOSED, loLayout.getState());
 
         loLayout.toggleMenu();
 
         assertEquals(FlyInLayout.MenuState.OPENING, loLayout.getState());
     }
 
     @Test
     public void testToggleMenu_animated() throws Exception
     {
         startMarker();
 
         ActivityController<SplashActivity> loActivity = Robolectric.buildActivity(SplashActivity.class);
         FlyInLayout loLayout = new FlyInLayout(AndroidApplicationAdaptor.getInstance().getApplicationContext());
 
         loActivity.create();
         loActivity.start();
 
         loActivity.get().setContentView(loLayout);
 
         assertEquals(FlyInLayout.MenuState.CLOSED, loLayout.getState());
 
         loLayout.toggleMenu(false);
 
         assertEquals(FlyInLayout.MenuState.OPEN, loLayout.getState());
 
         loLayout.toggleMenu(false);
 
         assertEquals(FlyInLayout.MenuState.CLOSED, loLayout.getState());
 
         loLayout.toggleMenu(true);
 
         assertEquals(FlyInLayout.MenuState.OPENING, loLayout.getState());
     }
 
     @Test
     public void testGetState() throws Exception
     {
         startMarker();
 
         ActivityController<SplashActivity> loActivity = Robolectric.buildActivity(SplashActivity.class);
         FlyInLayout loLayout = new FlyInLayout(AndroidApplicationAdaptor.getInstance().getApplicationContext());
 
         loActivity.create();
         loActivity.start();
 
         loActivity.get().setContentView(loLayout);
 
         assertEquals(FlyInLayout.MenuState.CLOSED, loLayout.getState());
 
         loLayout.toggleMenu(false);
 
         assertEquals(FlyInLayout.MenuState.OPEN, loLayout.getState());
         assertEquals(View.VISIBLE, loLayout.getChildAt(0).getVisibility());
 
         loLayout.toggleMenu(false);
 
         assertEquals(FlyInLayout.MenuState.CLOSED, loLayout.getState());
         assertEquals(View.GONE, loLayout.getChildAt(0).getVisibility());
 
         loLayout.toggleMenu(true);
 
         assertEquals(FlyInLayout.MenuState.OPENING, loLayout.getState());
     }
 
     @Test
     public void testOnAttachedToWindow() throws Exception
     {
         startMarker();
 
         ActivityController<SplashActivity> loActivity = Robolectric.buildActivity(SplashActivity.class);
         FlyInLayout loLayout = new FlyInLayout(AndroidApplicationAdaptor.getInstance().getApplicationContext());
 
         loActivity.create();
         loActivity.start();
 
         loActivity.get().setContentView(loLayout);
         assertEquals(View.GONE, loLayout.getChildAt(0).getVisibility());
     }
 
     @Test
     public void testOnLayout() throws Exception
     {
         startMarker();
 
         ActivityController<SplashActivity> loActivity = Robolectric.buildActivity(SplashActivity.class);
         FlyInLayout loLayout = new FlyInLayout(AndroidApplicationAdaptor.getInstance().getApplicationContext());
 
         loActivity.create();
         loActivity.start();
 
         loActivity.get().setContentView(loLayout);
         loLayout.toggleMenu(false);
         loLayout.toggleMenu(false);
 
         loLayout.onLayout(true, 0, 0, loLayout.getRight(), loLayout.getBottom());
 
         assertEquals(loLayout.getWidth(), loLayout.getChildAt(1).getWidth());
         assertEquals(loLayout.getHeight(), loLayout.getChildAt(1).getHeight());
     }
 
     @Test
     public void testAddItem() throws Exception
     {
         startMarker();
 
         ActivityController<SplashActivity> loActivity = Robolectric.buildActivity(SplashActivity.class);
         FlyInLayout loLayout = new FlyInLayout(AndroidApplicationAdaptor.getInstance().getApplicationContext());
 
         loActivity.create();
         loActivity.start();
 
         loActivity.get().setContentView(loLayout);
 
         assertTrue(loLayout.addItem(R.drawable.attention, R.string.error, R.layout.splash));
         assertFalse(loLayout.addItem(R.drawable.attention, R.string.error, R.layout.splash));
         assertTrue(loLayout.addItem(R.drawable.attention, R.string.app_name, R.layout.splash));
 
         // Test the buttons exist on the menu
         ViewGroup loMenu = (ViewGroup)loLayout.getChildAt(0);
         assertEquals(2, loMenu.getChildCount());
 
         // Test the views exist, 4 views, menu, empty + 2 added views
         assertEquals(4, loLayout.getChildCount());
     }
 
     @Test
     public void testSetCurrentView() throws Exception
     {
         startMarker();
 
         ActivityController<SplashActivity> loActivity = Robolectric.buildActivity(SplashActivity.class);
         FlyInLayout loLayout = new FlyInLayout(AndroidApplicationAdaptor.getInstance().getApplicationContext());
 
         loActivity.create();
         loActivity.start();
 
         loActivity.get().setContentView(loLayout);
 
         assertTrue(loLayout.addItem(R.drawable.attention, R.string.error, R.layout.splash));
         assertTrue(loLayout.addItem(R.drawable.attention, R.string.app_name, R.layout.splash));
 
        assertEquals(View.VISIBLE, loLayout.getChildAt(2).getVisibility());
         assertEquals(View.GONE, loLayout.getChildAt(3).getVisibility());
 
         assertFalse(loLayout.setCurrentView(R.string.error));
         assertTrue(loLayout.setCurrentView(R.string.app_name));
 
         assertEquals(View.GONE, loLayout.getChildAt(2).getVisibility());
         assertEquals(View.VISIBLE, loLayout.getChildAt(3).getVisibility());
     }
 
     @Test
     public void testGetCurrentViewID() throws Exception
     {
         startMarker();
 
         ActivityController<SplashActivity> loActivity = Robolectric.buildActivity(SplashActivity.class);
         FlyInLayout loLayout = new FlyInLayout(AndroidApplicationAdaptor.getInstance().getApplicationContext());
 
         loActivity.create();
         loActivity.start();
 
         loActivity.get().setContentView(loLayout);
 
         assertTrue(loLayout.addItem(R.drawable.attention, R.string.error, R.layout.splash));
         assertTrue(loLayout.addItem(R.drawable.attention, R.string.app_name, R.layout.splash));
 
         ViewGroup loMenu = (ViewGroup)loLayout.getChildAt(0);
 
         assertTrue(loLayout.addView(R.string.cancel, new View(loMenu.getContext())));
 
         loLayout.setCurrentView(R.string.app_name);
         assertEquals(R.string.app_name, loLayout.getCurrentViewID());
         assertEquals(View.VISIBLE, loLayout.getChildAt(3).getVisibility());
 
         loLayout.setCurrentView(R.string.error);
         assertEquals(R.string.error, loLayout.getCurrentViewID());
         assertEquals(View.VISIBLE, loLayout.getChildAt(2).getVisibility());
 
         loLayout.setFlyInHelper(new FlyInLayout.FlyInHelper()
         {
             @Override
             protected int translateView(int tnStringID)
             {
                 return R.string.cancel;
             }
         });
 
         loLayout.setCurrentView(R.string.app_name);
         assertEquals(View.VISIBLE, loLayout.getChildAt(4).getVisibility());
         assertEquals(R.string.cancel, loLayout.getCurrentViewID());
 
         assertFalse(loLayout.setCurrentView(R.string.app_name));
     }
 
     @Test
     public void testAddView() throws Exception
     {
         startMarker();
 
         ActivityController<SplashActivity> loActivity = Robolectric.buildActivity(SplashActivity.class);
         FlyInLayout loLayout = new FlyInLayout(AndroidApplicationAdaptor.getInstance().getApplicationContext());
 
         loActivity.create();
         loActivity.start();
 
         loActivity.get().setContentView(loLayout);
 
         assertTrue(loLayout.addItem(R.drawable.attention, R.string.error, R.layout.splash));
         assertFalse(loLayout.addItem(R.drawable.attention, R.string.error, R.layout.splash));
         assertTrue(loLayout.addItem(R.drawable.attention, R.string.app_name, R.layout.splash));
 
         // Test the buttons exist on the menu
         ViewGroup loMenu = (ViewGroup)loLayout.getChildAt(0);
         assertEquals(2, loMenu.getChildCount());
 
         // Test the views exist, 4 views, menu, empty + 2 added views
         assertEquals(4, loLayout.getChildCount());
 
         assertFalse(loLayout.addView(R.string.error, new View(loMenu.getContext())));
         assertTrue(loLayout.addView(R.string.cancel, new View(loMenu.getContext())));
 
         assertEquals(2, loMenu.getChildCount());
         assertEquals(5, loLayout.getChildCount());
     }
 
     @Test
     public void testSetFlyInHelper() throws Exception
     {
         startMarker();
 
         ActivityController<SplashActivity> loActivity = Robolectric.buildActivity(SplashActivity.class);
         FlyInLayout loLayout = new FlyInLayout(AndroidApplicationAdaptor.getInstance().getApplicationContext());
 
         loActivity.create();
         loActivity.start();
 
         loActivity.get().setContentView(loLayout);
 
         assertTrue(loLayout.addItem(R.drawable.attention, R.string.error, R.layout.splash));
         assertTrue(loLayout.addItem(R.drawable.attention, R.string.app_name, R.layout.splash));
 
         ViewGroup loMenu = (ViewGroup)loLayout.getChildAt(0);
 
         assertTrue(loLayout.addView(R.string.cancel, new View(loMenu.getContext())));
 
         loLayout.setCurrentView(R.string.app_name);
         assertEquals(View.VISIBLE, loLayout.getChildAt(3).getVisibility());
 
         loLayout.setCurrentView(R.string.error);
         assertEquals(View.VISIBLE, loLayout.getChildAt(2).getVisibility());
 
         TestLayoutHelper loHelper = new TestLayoutHelper();
 
         loLayout.setFlyInHelper(loHelper);
 
         loLayout.setCurrentView(R.string.app_name);
         assertEquals(View.VISIBLE, loLayout.getChildAt(4).getVisibility());
         assertFalse(loLayout.setCurrentView(R.string.app_name));
 
         assertEquals(R.string.cancel, loHelper.m_nLastView);
         assertEquals(loLayout.getChildAt(4), loHelper.m_oLastView);
     }
 }
