 
 package com.google.code.geobeagle.ui.cachelist;
 
 import static org.junit.Assert.*;
 
 import org.easymock.classextension.EasyMock;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.powermock.api.easymock.PowerMock;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.View;
 import android.widget.ListView;
 
 @RunWith(PowerMockRunner.class)
 public class CacheListDelegateTest {
     @Test
     public void testController() {
         GeocacheListController geocacheListController = PowerMock
                 .createStrictMock(GeocacheListController.class);
         ListView listView = PowerMock.createMock(ListView.class);
         View view = PowerMock.createMock(View.class);
         MenuItem menuItem = PowerMock.createMock(MenuItem.class);
 
         EasyMock.expect(geocacheListController.onContextItemSelected(menuItem)).andReturn(true);
         geocacheListController.onListItemClick(listView, view, 28, 42);
         EasyMock.expect(geocacheListController.onOptionsItemSelected(menuItem)).andReturn(true);
 
         PowerMock.replayAll();
         CacheListDelegate cacheListDelegate = new CacheListDelegate(geocacheListController, null);
         cacheListDelegate.onContextItemSelected(menuItem);
         cacheListDelegate.onListItemClick(listView, view, 28, 42);
         cacheListDelegate.onOptionsItemSelected(menuItem);
         PowerMock.verifyAll();
     }
 
     @Test
     public void testCreateOptionsMenu() {
         Menu menu = PowerMock.createMock(Menu.class);
         GeocacheListPresenter geocacheListPresenter = PowerMock
                 .createStrictMock(GeocacheListPresenter.class);
 
         EasyMock.expect(geocacheListPresenter.onCreateOptionsMenu(menu)).andReturn(true);
 
         PowerMock.replayAll();
         assertTrue(new CacheListDelegate(null, geocacheListPresenter).onCreateOptionsMenu(menu));
         PowerMock.verifyAll();
     }
 
     @Test
     public void testOnContextItemSelected() {
         MenuItem menuItem = PowerMock.createMock(MenuItem.class);
         GeocacheListController geocacheListController = PowerMock
                 .createStrictMock(GeocacheListController.class);
 
         EasyMock.expect(geocacheListController.onContextItemSelected(menuItem)).andReturn(true);
 
         PowerMock.replayAll();
         new CacheListDelegate(geocacheListController, null).onContextItemSelected(menuItem);
         PowerMock.verifyAll();
     }
 
     @Test
     public void testOnCreate() {
         GeocacheListPresenter geocacheListPresenter = PowerMock
                 .createStrictMock(GeocacheListPresenter.class);
 
         geocacheListPresenter.onCreate();
 
         PowerMock.replayAll();
         new CacheListDelegate(null, geocacheListPresenter).onCreate();
         PowerMock.verifyAll();
     }
 
     @Test
     public void testOnListItemClick() {
         GeocacheListController geocacheListController = PowerMock
                 .createStrictMock(GeocacheListController.class);
         ListView listView = PowerMock.createMock(ListView.class);
         View view = PowerMock.createMock(View.class);
 
         geocacheListController.onListItemClick(listView, view, 28, 42);
 
         PowerMock.replayAll();
         new CacheListDelegate(geocacheListController, null).onListItemClick(listView, view, 28, 42);
         PowerMock.verifyAll();
     }
 
     @Test
     public void testOnOptionsItemSelected() {
         GeocacheListController geocacheListController = PowerMock
                 .createStrictMock(GeocacheListController.class);
         MenuItem menuItem = PowerMock.createMock(MenuItem.class);
 
         EasyMock.expect(geocacheListController.onOptionsItemSelected(menuItem)).andReturn(true);
 
         PowerMock.replayAll();
         new CacheListDelegate(geocacheListController, null).onOptionsItemSelected(menuItem);
         PowerMock.verifyAll();
     }
 
     @Test
     public void testOnPause() {
         GeocacheListPresenter geocacheListPresenter = PowerMock
                 .createStrictMock(GeocacheListPresenter.class);
         GeocacheListController geocacheListController = PowerMock
                 .createStrictMock(GeocacheListController.class);
 
         geocacheListPresenter.onPause();
         geocacheListController.onPause();
 
         PowerMock.replayAll();
         new CacheListDelegate(geocacheListController, geocacheListPresenter).onPause();
         PowerMock.verifyAll();
     }
 
     @Test
     public void testOnResume() {
         GeocacheListPresenter geocacheListPresenter = PowerMock
                 .createStrictMock(GeocacheListPresenter.class);
        GeocacheListController geocacheListController = PowerMock
        .createStrictMock(GeocacheListController.class);
 
         geocacheListPresenter.onResume();
        geocacheListController.onResume();
 
         PowerMock.replayAll();
        new CacheListDelegate( geocacheListController, geocacheListPresenter).onResume();
         PowerMock.verifyAll();
     }
 }
