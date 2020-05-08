 /*
  ** Licensed under the Apache License, Version 2.0 (the "License");
  ** you may not use this file except in compliance with the License.
  ** You may obtain a copy of the License at
  **
  **     http://www.apache.org/licenses/LICENSE-2.0
  **
  ** Unless required by applicable law or agreed to in writing, software
  ** distributed under the License is distributed on an "AS IS" BASIS,
  ** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ** See the License for the specific language governing permissions and
  ** limitations under the License.
  */
 
 package com.google.code.geobeagle.ui.cachelist;
 
 import static org.easymock.EasyMock.expect;
 import static org.junit.Assert.assertTrue;
 
 import com.google.code.geobeagle.R;
 import com.google.code.geobeagle.data.GeocacheVector;
 import com.google.code.geobeagle.data.GeocacheVectors;
 import com.google.code.geobeagle.io.GpxImporter;
 import com.google.code.geobeagle.ui.ErrorDisplayer;
 import com.google.code.geobeagle.ui.cachelist.GeocacheListController.CacheListOnCreateContextMenuListener;
 
 import org.easymock.EasyMock;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.powermock.api.easymock.PowerMock;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 import android.app.ListActivity;
 import android.view.ContextMenu;
 import android.view.MenuItem;
 import android.widget.AdapterView.AdapterContextMenuInfo;
 
 @RunWith(PowerMockRunner.class)
 @PrepareForTest( {
         GeocacheListController.class, ListActivity.class, GeocacheListPresenter.class,
         CacheListOnCreateContextMenuListener.class
 })
 public class GeocacheListControllerTest {
 
     @Test
     public void testCacheListOnCreateContextMenuListener() {
         ContextMenu menu = PowerMock.createMock(ContextMenu.class);
         AdapterContextMenuInfo menuInfo = PowerMock.createMock(AdapterContextMenuInfo.class);
         GeocacheVectors geocacheVectors = PowerMock.createMock(GeocacheVectors.class);
         GeocacheVector geocacheVector = PowerMock.createMock(GeocacheVector.class);
 
        expect(geocacheVectors.get(12)).andReturn(geocacheVector);
         expect(geocacheVector.getId()).andReturn("GC123");
         expect(menu.setHeaderTitle("GC123")).andReturn(menu);
         expect(menu.add(0, GeocacheListController.MENU_VIEW, 0, "View")).andReturn(null);
         expect(menu.add(0, GeocacheListController.MENU_DELETE, 1, "Delete")).andReturn(null);
 
         PowerMock.replayAll();
         menuInfo.position = 12;
         new CacheListOnCreateContextMenuListener(geocacheVectors).onCreateContextMenu(menu, null,
                 menuInfo);
         PowerMock.verifyAll();
     }
 
     @Test
     public void testOnContextItemSelected() {
         MenuItem menuItem = PowerMock.createMock(MenuItem.class);
         AdapterContextMenuInfo adapterContextMenuInfo = PowerMock
                 .createMock(AdapterContextMenuInfo.class);
         ContextAction contextAction = PowerMock.createMock(ContextAction.class);
 
         ContextAction contextActions[] = {
                 null, null, contextAction
         };
         expect(menuItem.getMenuInfo()).andReturn(adapterContextMenuInfo);
         expect(menuItem.getItemId()).andReturn(2);
         contextAction.act(75);
 
         PowerMock.replayAll();
         adapterContextMenuInfo.position = 76;
         GeocacheListController geocacheListController = new GeocacheListController(null,
                 contextActions, null, null);
         assertTrue(geocacheListController.onContextItemSelected(menuItem));
         PowerMock.verifyAll();
     }
 
     @Test
     public void testOnContextItemSelectedError() {
         MenuItem menuItem = PowerMock.createMock(MenuItem.class);
         ErrorDisplayer errorDisplayer = PowerMock.createMock(ErrorDisplayer.class);
 
         RuntimeException runtimeException = new RuntimeException();
         expect(menuItem.getMenuInfo()).andThrow(runtimeException);
         errorDisplayer.displayErrorAndStack(runtimeException);
 
         PowerMock.replayAll();
         GeocacheListController geocacheListController = new GeocacheListController(null, null,
                 null, errorDisplayer);
         assertTrue(geocacheListController.onContextItemSelected(menuItem));
         PowerMock.verifyAll();
     }
 
     @Test
     public void testOnCreateContextMenu() {
         ContextMenu contextMenu = PowerMock.createMock(ContextMenu.class);
         MenuItem menuItem = PowerMock.createMock(MenuItem.class);
         GeocacheVectors geocacheVectors = PowerMock.createMock(GeocacheVectors.class);
         GeocacheVector geocacheVector = PowerMock.createMock(GeocacheVector.class);
 
         PowerMock.suppressConstructor(AdapterContextMenuInfo.class);
         AdapterContextMenuInfo contextMenuInfo = new AdapterContextMenuInfo(null, 0, 0);
         contextMenuInfo.position = 42;
        EasyMock.expect(geocacheVectors.get(42)).andReturn(geocacheVector);
         EasyMock.expect(geocacheVector.getId()).andReturn("GCABC");
         EasyMock.expect(contextMenu.setHeaderTitle("GCABC")).andReturn(contextMenu);
         EasyMock.expect(contextMenu.add(0, GeocacheListController.MENU_VIEW, 0, "View")).andReturn(
                 menuItem);
         EasyMock.expect(contextMenu.add(0, GeocacheListController.MENU_DELETE, 1, "Delete"))
                 .andReturn(menuItem);
 
         PowerMock.replayAll();
         new CacheListOnCreateContextMenuListener(geocacheVectors).onCreateContextMenu(contextMenu,
                 null, contextMenuInfo);
         PowerMock.verifyAll();
     }
 
     @Test
     public void testOnListItemClick() {
         final ContextAction contextAction = PowerMock.createMock(ContextAction.class);
         ContextAction contextActions[] = {
                 null, contextAction
         };
 
         contextAction.act(45);
 
         PowerMock.replayAll();
         new GeocacheListController(null, contextActions, null, null).onListItemClick(null, null,
                 46, 0);
         PowerMock.verifyAll();
     }
 
     @Test
     public void testOnListItemClickZero() {
         MenuActions menuActions = PowerMock.createMock(MenuActions.class);
 
         menuActions.act(R.id.menu_refresh);
 
         PowerMock.replayAll();
         new GeocacheListController(menuActions, null, null, null).onListItemClick(null, null, 0, 0);
         PowerMock.verifyAll();
     }
 
     @Test
     public void testOnListItemClickError() {
         final ContextAction contextAction = PowerMock.createMock(ContextAction.class);
         ErrorDisplayer errorDisplayer = PowerMock.createMock(ErrorDisplayer.class);
         ContextAction contextActions[] = {
                 null, contextAction
         };
         RuntimeException runtimeException = new RuntimeException();
         contextAction.act(46);
         EasyMock.expectLastCall().andThrow(runtimeException);
         errorDisplayer.displayErrorAndStack(runtimeException);
 
         PowerMock.replayAll();
         new GeocacheListController(null, contextActions, null, errorDisplayer).onListItemClick(
                 null, null, 47, 0);
         PowerMock.verifyAll();
     }
 
     @Test
     public void testOnOptionsItemSelected() {
         MenuActions menuActions = PowerMock.createMock(MenuActions.class);
         MenuItem menuItem = PowerMock.createMock(MenuItem.class);
 
         expect(menuItem.getItemId()).andReturn(27);
         menuActions.act(27);
 
         PowerMock.replayAll();
         new GeocacheListController(menuActions, null, null, null).onOptionsItemSelected(menuItem);
         PowerMock.verifyAll();
     }
 
     @Test
     public void testOnOptionsItemSelectedError() {
         MenuActions menuActions = PowerMock.createMock(MenuActions.class);
         MenuItem menuItem = PowerMock.createMock(MenuItem.class);
         ErrorDisplayer errorDisplayer = PowerMock.createMock(ErrorDisplayer.class);
 
         Exception exception = new RuntimeException();
         expect(menuItem.getItemId()).andThrow(exception);
         errorDisplayer.displayErrorAndStack(exception);
 
         PowerMock.replayAll();
         new GeocacheListController(menuActions, null, null, errorDisplayer)
                 .onOptionsItemSelected(menuItem);
         PowerMock.verifyAll();
     }
 
     @Test
     public void testOnPause() throws InterruptedException {
         GpxImporter gpxImporter = PowerMock.createMock(GpxImporter.class);
 
         gpxImporter.abort();
 
         PowerMock.replayAll();
         new GeocacheListController(null, null, gpxImporter, null).onPause();
         PowerMock.verifyAll();
     }
 
     @Test
     public void testOnPauseError() throws InterruptedException {
         GpxImporter gpxImporter = PowerMock.createMock(GpxImporter.class);
 
         Exception e = new InterruptedException();
         gpxImporter.abort();
         EasyMock.expectLastCall().andThrow(e);
 
         PowerMock.replayAll();
         new GeocacheListController(null, null, gpxImporter, null).onPause();
         PowerMock.verifyAll();
     }
 }
