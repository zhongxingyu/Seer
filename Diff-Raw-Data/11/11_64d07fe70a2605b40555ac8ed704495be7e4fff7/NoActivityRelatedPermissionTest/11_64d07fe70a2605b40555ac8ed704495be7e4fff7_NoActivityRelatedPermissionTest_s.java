 /*
  * Copyright (C) 2009 The Android Open Source Project
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package android.permission.cts;
 
 import dalvik.annotation.TestTargetClass;
 
 import android.app.Activity;
 import android.app.ActivityManager;
 import android.app.AlertDialog;
 import android.content.Context;
 import android.test.ActivityInstrumentationTestCase2;
 import android.test.UiThreadTest;
 import android.test.suitebuilder.annotation.MediumTest;
 import android.view.WindowManager;
 import android.view.WindowManager.BadTokenException;
 
 /**
  * Verify the Activity related operations require specific permissions.
  */
 @TestTargetClass(Activity.class)
 public class NoActivityRelatedPermissionTest
         extends ActivityInstrumentationTestCase2<PermissionStubActivity> {
 
     private PermissionStubActivity mActivity;
 
     public NoActivityRelatedPermissionTest() {
         super("com.android.cts.permission", PermissionStubActivity.class);
     }
 
     @Override
     protected void setUp() throws Exception {
         super.setUp();
         mActivity = getActivity();
     }
 
     /**
      * Verify that adding window of different types in Window Manager requires permissions.
      * <p>Requires Permission:
      *   {@link android.Manifest.permission#SYSTEM_ALERT_WINDOW}.
      */
     @UiThreadTest
     @MediumTest
     public void testSystemAlertWindow() {
         final int[] types = new int[] {
                 WindowManager.LayoutParams.TYPE_PHONE,
                 WindowManager.LayoutParams.TYPE_PRIORITY_PHONE,
                 WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
                 WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                 WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
             };
 
         AlertDialog dialog = (AlertDialog) (mActivity.getDialog());
         // Use normal window type will success
         dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_APPLICATION);
         dialog.show();
 
         // Test special window types which need to be check SYSTEM_ALERT_WINDOW
         // permission.
         for (int i = 0; i < types.length; i++) {
             dialog = (AlertDialog) (mActivity.getDialog());
             dialog.getWindow().setType(types[i]);
             try {
                 dialog.show();
                 fail("Add dialog to Window Manager did not throw BadTokenException as expected");
             } catch (BadTokenException e) {
                 // Expected
             }
         }
     }
 
     /**
      * Verify that setting Activity's persistent attribute requires permissions.
      * <p>Requires Permission:
      *   {@link android.Manifest.permission#PERSISTENT_ACTIVITY}.
      */
     @UiThreadTest
     @MediumTest
     public void testSetPersistent() {
         try {
             mActivity.setPersistent(true);
             fail("Activity.setPersistent() did not throw SecurityException as expected");
         } catch (SecurityException e) {
             // Expected
         }
     }
 
     /**
      * Verify that get task requires permissions.
      * <p>Requires Permission:
      *   {@link android.Manifest.permission#GET_TASKS}
      */
     @MediumTest
     public void testGetTask() {
         ActivityManager manager = (ActivityManager) getActivity()
                 .getSystemService(Context.ACTIVITY_SERVICE);
         try {
             manager.getRunningTasks(1);
             fail("Activity.getRunningTasks did not throw SecurityException as expected");
         } catch (SecurityException e) {
             // Expected
         }
 
         try {
             manager.getRecentTasks(1, 0);
             fail("Activity.getRunningTasks did not throw SecurityException as expected");
         } catch (SecurityException e) {
             // Expected
         }
     }
 }
