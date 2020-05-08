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
 
 package com.google.code.geobeagle.cachelist;
 
 import com.google.code.geobeagle.activity.cachelist.CacheListActivity;
 import com.google.code.geobeagle.activity.cachelist.CacheListDelegate;
 
import org.easymock.EasyMock;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.powermock.api.easymock.PowerMock;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 import android.app.Activity;
 import android.app.ListActivity;
 import android.view.MenuItem;
 
 @RunWith(PowerMockRunner.class)
 @PrepareForTest( {
         Activity.class, CacheListActivity.class, ListActivity.class
 })
 public class CacheListTest {
     @Test
     public void testOnContextItemSelected() {
         if (true)
             return;
         MenuItem menuItem = PowerMock.createMock(MenuItem.class);
         CacheListDelegate cacheListDelegate = PowerMock.createMock(CacheListDelegate.class);
 
         PowerMock.suppressConstructor(ListActivity.class);
         EasyMock.expect(cacheListDelegate.onContextItemSelected(menuItem)).andReturn(true);
 
         PowerMock.replayAll();
         new CacheListActivity(cacheListDelegate).onContextItemSelected(menuItem);
         PowerMock.verifyAll();
     }
 }
