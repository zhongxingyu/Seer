 /**
  * ShopShopViewer
  * Copyright (C) 2012 Remko Plantenga
  * 
  * This file is part of ShopShopViewer.
  *
  * ShopShopViewer is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * ShopShopViewer is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with ShopShopViewer. If not, see <http://www.gnu.org/licenses/>.
  */
 package de.remk0.shopshopviewer;
 
 import java.io.BufferedOutputStream;
 import java.io.ByteArrayOutputStream;
 import java.io.InputStream;
 
 import org.easymock.EasyMock;
 
 import android.app.ListActivity;
 import android.content.Intent;
 import android.widget.ListAdapter;
 import android.widget.ListView;
 
 import com.jayway.android.robotium.solo.Solo;
 
 import de.remk0.shopshopviewer.io.FileAccess;
 import de.remk0.test.ActivityInstrumentationTestCase2WithResources;
 
 /**
  * @author Remko Plantenga
  * 
  */
 public class DisplayFileActivityTest extends
         ActivityInstrumentationTestCase2WithResources<DisplayFileActivity> {
     private Solo solo;
     private ByteArrayOutputStream out = new ByteArrayOutputStream();
 
     public DisplayFileActivityTest() {
         super("de.remk0.shopshopviewer", DisplayFileActivity.class);
     }
 
     @Override
     protected void setUp() throws Exception {
         ShopShopViewerApplication context = (ShopShopViewerApplication) this
                 .getInstrumentation().getTargetContext()
                 .getApplicationContext();
         FileAccess fileAccess = EasyMock.createMock(FileAccess.class);
         final InputStream is = getResources("de.remk0.shopshopviewer.test")
                 .openRawResource(de.remk0.shopshopviewer.test.R.raw.nederland2);
        EasyMock.expect(fileAccess.getFile("file1")).andReturn(is);
         BufferedOutputStream bufferedOut = new BufferedOutputStream(out);
         // TODO why 2 times?
         EasyMock.expect(fileAccess.openFile("file1")).andReturn(bufferedOut)
                 .times(2);
         EasyMock.replay(fileAccess);
         context.setFileAccess(fileAccess);
 
         Intent intent = new Intent();
         intent.putExtra("de.remk0.shopshopviewer.fileName", "file1");
         setActivityIntent(intent);
 
         solo = new Solo(getInstrumentation(), getActivity());
     }
 
     @Override
     protected void tearDown() {
         solo.finishOpenedActivities();
     }
 
     public void testResume() {
 
         solo.waitForView(ListView.class);
 
         ListAdapter currentListAdapter = ((ListActivity) solo
                 .getCurrentActivity()).getListAdapter();
         assertNotNull(currentListAdapter);
 
         getInstrumentation().callActivityOnPause(getActivity());
 
         getInstrumentation().callActivityOnResume(getActivity());
 
         solo.waitForView(ListView.class);
     }
 
 }
