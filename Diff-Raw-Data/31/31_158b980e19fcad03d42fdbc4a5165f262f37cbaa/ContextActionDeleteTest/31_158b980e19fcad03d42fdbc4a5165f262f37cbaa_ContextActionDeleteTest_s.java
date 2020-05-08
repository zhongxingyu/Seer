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
 
 package com.google.code.geobeagle.actions.context;
 
 import static org.easymock.EasyMock.expect;
 import static org.junit.Assert.assertEquals;
 
 import com.google.code.geobeagle.R;
 import com.google.code.geobeagle.activity.cachelist.actions.context.ContextActionDelete;
 import com.google.code.geobeagle.activity.cachelist.actions.context.ContextActionDelete.OnClickOk;
 import com.google.code.geobeagle.activity.cachelist.model.GeocacheVector;
 import com.google.code.geobeagle.activity.cachelist.model.GeocacheVectors;
 import com.google.code.geobeagle.activity.cachelist.presenter.GeocacheListAdapter;
 import com.google.code.geobeagle.activity.cachelist.presenter.TitleUpdater;
 import com.google.code.geobeagle.database.CacheWriter;
 import com.google.inject.Provider;
 
 import org.easymock.EasyMock;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.powermock.api.easymock.PowerMock;
 import org.powermock.core.classloader.annotations.PrepareForTest;
 import org.powermock.modules.junit4.PowerMockRunner;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.app.AlertDialog.Builder;
 import android.content.DialogInterface;
 import android.content.DialogInterface.OnClickListener;
 import android.widget.TextView;
 
 @RunWith(PowerMockRunner.class)
 @PrepareForTest( {
         Activity.class, TextView.class
 })
 public class ContextActionDeleteTest {
 
     @SuppressWarnings("unchecked")
     @Test
     public void testActionDelete() {
         CacheWriter cacheWriter = PowerMock.createMock(CacheWriter.class);
         GeocacheListAdapter geocacheListAdapter = PowerMock.createMock(GeocacheListAdapter.class);
         GeocacheVectors geocacheVectors = PowerMock.createMock(GeocacheVectors.class);
         GeocacheVector geocacheVector = PowerMock.createMock(GeocacheVector.class);
         TitleUpdater titleUpdater = PowerMock.createMock(TitleUpdater.class);
         Provider<CacheWriter> cacheWriterProvider = PowerMock.createMock(Provider.class);
         Activity activity = PowerMock.createMock(Activity.class);
         DialogInterface dialog = PowerMock.createMock(DialogInterface.class);
 
         activity.showDialog(0);
         expect(cacheWriterProvider.get()).andReturn(cacheWriter);
         expect(geocacheVectors.get(17)).andReturn(geocacheVector);
         expect(geocacheVector.getId()).andReturn("GC123");
         cacheWriter.deleteCache("GC123");
         geocacheVectors.remove(17);
         expect(geocacheVectors.size()).andReturn(16).anyTimes();
         geocacheListAdapter.notifyDataSetChanged();
         titleUpdater.update(16, 16);
         dialog.dismiss();
 
         PowerMock.replayAll();
         final ContextActionDelete contextActionDelete = new ContextActionDelete(
                geocacheListAdapter, geocacheVectors, titleUpdater, cacheWriterProvider, activity);
         final OnClickOk onClickOk = new ContextActionDelete.OnClickOk(contextActionDelete);
         contextActionDelete.act(17);
         onClickOk.onClick(dialog, 0);
         PowerMock.verifyAll();
     }
 
     @Test
     public void testGetConfirmBodyText() {
         GeocacheVectors geocacheVectors = PowerMock.createMock(GeocacheVectors.class);
         GeocacheVector geocacheVector = PowerMock.createMock(GeocacheVector.class);
         Activity activity = PowerMock.createMock(Activity.class);
 
         EasyMock.expect(geocacheVectors.get(0)).andReturn(geocacheVector);
         EasyMock.expect(activity.getString(R.string.confirm_delete_body_text)).andReturn(
                 "Delete %1$s: \"%2$s\"?");
         EasyMock.expect(geocacheVector.getId()).andReturn("GC123");
         EasyMock.expect(geocacheVector.getName()).andReturn("my cache");
 
         PowerMock.replayAll();
         ContextActionDelete contextActionDelete = new ContextActionDelete(null, geocacheVectors,
                null, null, activity);
         assertEquals("Delete GC123: \"my cache\"?", contextActionDelete.getConfirmDeleteBodyText());
         PowerMock.verifyAll();
     }
 
     @Test
     public void testGetConfirmDeleteTitle() {
         GeocacheVectors geocacheVectors = PowerMock.createMock(GeocacheVectors.class);
         GeocacheVector geocacheVector = PowerMock.createMock(GeocacheVector.class);
         Activity activity = PowerMock.createMock(Activity.class);
 
         EasyMock.expect(geocacheVectors.get(0)).andReturn(geocacheVector);
         EasyMock.expect(activity.getString(R.string.confirm_delete_title)).andReturn(
                 "Confirm delete %1$s");
         EasyMock.expect(geocacheVector.getId()).andReturn("GC123");
 
         PowerMock.replayAll();
         ContextActionDelete contextActionDelete = new ContextActionDelete(null, geocacheVectors,
                null, null, activity);
         assertEquals("Confirm delete GC123", contextActionDelete.getConfirmDeleteTitle());
         PowerMock.verifyAll();
     }
 
     @Test
     public void testOnCreateDialog() {
         OnClickListener onClickOk = PowerMock.createMock(OnClickListener.class);
         Builder builder = PowerMock.createMock(Builder.class);
         AlertDialog dialog = PowerMock.createMock(AlertDialog.class);
 
         EasyMock.expect(builder.setPositiveButton(R.string.delete_cache, onClickOk)).andReturn(
                 builder);
         EasyMock.expect(builder.create()).andReturn(dialog);
 
         PowerMock.replayAll();
         ContextActionDelete.ContextActionDeleteDialogHelper contextActionDeleteDialogHelper = new ContextActionDelete.ContextActionDeleteDialogHelper(
                 null, onClickOk);
         contextActionDeleteDialogHelper.onCreateDialog(builder);
         PowerMock.verifyAll();
     }
 
     @Test
     public void testOnPrepareDialog() {
         OnClickListener onClickOk = PowerMock.createMock(OnClickListener.class);
         AlertDialog dialog = PowerMock.createMock(AlertDialog.class);
         ContextActionDelete contextActionDelete = PowerMock.createMock(ContextActionDelete.class);
         TextView textView = PowerMock.createMock(TextView.class);
 
         EasyMock.expect(contextActionDelete.getConfirmDeleteTitle()).andReturn(
                 "Confirm delete GC123");
         dialog.setTitle("Confirm delete GC123");
 
         EasyMock.expect(dialog.findViewById(R.id.delete_cache)).andReturn(textView);
         EasyMock.expect(contextActionDelete.getConfirmDeleteBodyText()).andReturn(
                 "Delete GC123: \"my cache\"?");
         textView.setText("Delete GC123: \"my cache\"?");
 
         PowerMock.replayAll();
         ContextActionDelete.ContextActionDeleteDialogHelper contextActionDeleteDialogHelper = new ContextActionDelete.ContextActionDeleteDialogHelper(
                 contextActionDelete, onClickOk);
         contextActionDeleteDialogHelper.onPrepareDialog(dialog);
         PowerMock.verifyAll();
     }
 }
