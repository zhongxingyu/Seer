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
 
 package com.google.code.geobeagle.actions;
 
 import com.google.code.geobeagle.Geocache;
 import com.google.code.geobeagle.actions.CacheActionEdit;
 import com.google.code.geobeagle.activity.EditCacheActivity;
 import org.easymock.EasyMock;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.powermock.api.easymock.PowerMock;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 import android.app.Activity;
 import android.content.Intent;
 
 @PrepareForTest( {
     Activity.class, CacheActionEdit.class
 })
 @RunWith(PowerMockRunner.class)
 public class CacheActionEditTest {
     @Test
     public void testAct() throws Exception {
         Activity activity = PowerMock.createMock(Activity.class);
         Intent intent = PowerMock.createMock(Intent.class);
         Geocache geocache = PowerMock.createMock(Geocache.class);
 
         PowerMock.expectNew(Intent.class, activity, EditCacheActivity.class).andReturn(intent);
        EasyMock.expect(intent.putExtra("geocacheId", (CharSequence) "id1")).andReturn(intent);
        EasyMock.expect(geocache.getId()).andReturn("id1");
         activity.startActivityForResult(intent, 0);
 
         PowerMock.replayAll();
         new CacheActionEdit(activity).act(geocache);
         PowerMock.verifyAll();
     }
 }
