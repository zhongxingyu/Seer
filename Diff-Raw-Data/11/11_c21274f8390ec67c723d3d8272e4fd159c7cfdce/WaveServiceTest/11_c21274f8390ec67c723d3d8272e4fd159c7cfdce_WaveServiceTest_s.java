 // 
 //  WaveServiceTest.java
 //  tests
 //  
 //  Created by Philip Kuryloski on 2011-01-27.
 //  Copyright 2011 Philip Kuryloski. All rights reserved.
 // 
 
 package edu.berkeley.androidwave.waveservice;
 
 import edu.berkeley.androidwave.TestUtils;
 import edu.berkeley.androidwave.waverecipe.WaveRecipe;
 import edu.berkeley.androidwave.waverecipe.WaveRecipeOutputDataImpl;
 
 // import android.content.Context;
 import android.content.Intent;
 // import android.content.pm.PackageManager.NameNotFoundException;
 import android.os.IBinder;
 import android.os.RemoteException;
 // import android.test.mock.MockContext;
 import android.test.ServiceTestCase;
 import android.test.suitebuilder.annotation.LargeTest;
 import android.test.suitebuilder.annotation.MediumTest;
 import android.test.suitebuilder.annotation.SmallTest;
 import java.io.*;
 
 /**
  * This is a simple framework for a test of a Service.  See {@link android.test.ServiceTestCase
  * ServiceTestCase} for more information on how to write and extend service tests.
  * 
  * To run this test, you can type:
  * adb shell am instrument -w -e class edu.berkeley.androidwave.waveservice.WaveServiceTest edu.berkeley.androidwave.tests/android.test.InstrumentationTestRunner
  */
 public class WaveServiceTest extends ServiceTestCase<WaveService> {
     
     File cachedRecipe = null;
     
     public WaveServiceTest() {
         super(WaveService.class);
     }
     
     @Override
     protected void setUp() throws Exception {
         super.setUp();
     }
     
     @Override
     protected void tearDown() throws Exception {
         if (cachedRecipe != null && cachedRecipe.exists()) {
             if (cachedRecipe.delete()) {
                 System.out.println("Removed "+cachedRecipe);
             } else {
                 throw new Exception("Removal of "+cachedRecipe+" failed.");
             }
         }
 
         super.tearDown();
     }
 
     /**
      * The name 'test preconditions' is a convention to signal that if this
      * test doesn't pass, the test case was not set up properly and it might
      * explain any and all failures in other tests.  This is not guaranteed
      * to run before other tests, as junit uses reflection to find the tests.
      */
     @SmallTest
     public void testPreconditions() {
     }
     
     /**
      * Test basic startup/shutdown of Service
      */
     @SmallTest
     public void testStartable() {
         Intent startIntent = new Intent();
         startIntent.setClass(getContext(), WaveService.class);
         startService(startIntent); 
     }
     
     /**
      * Test binding to service
      *
      * This service should allow binding over two interfaces, one for the
      * Wave UI, and one for Wave Client Apps.
      */
     @MediumTest
     public void testPrivateBindable() {
         Intent startIntent = new Intent(Intent.ACTION_EDIT);
         startIntent.setClass(getContext(), WaveService.class);
         IBinder service = bindService(startIntent); 
     }
     
     @MediumTest
     public void testPublicBindable() {
         Intent startIntent = new Intent(Intent.ACTION_MAIN);
         startIntent.setClass(getContext(), WaveService.class);
         IBinder service = bindService(startIntent); 
     }
     
     /**
      * test public interface recipe interaction
      */
     @MediumTest
     public void testPublicBindableMore() throws RemoteException {
         
         /**
          * Simple recipeListener
          */
         IWaveRecipeOutputDataListener mListener = new IWaveRecipeOutputDataListener.Stub() {
             public void receiveWaveRecipeOutputData(WaveRecipeOutputDataImpl wrOutput) {
                 System.out.println("WaveRecipeOutputDataImpl received => "+wrOutput);
             }
         };
         
         /**
          * test the remote calls
          */
         Intent startIntent = new Intent(Intent.ACTION_MAIN);
         startIntent.setClass(getContext(), WaveService.class);
         IBinder service = bindService(startIntent); 
         
         IWaveServicePublic mService = IWaveServicePublic.Stub.asInterface(service);
         assertNotNull(mService);
         
         // for now just make the remote call, without validating the result
         mService.recipeExists("edu.berkeley.waverecipe.AccelerometerMagnitude", false);
         mService.isAuthorized("edu.berkeley.waverecipe.AccelerometerMagnitude");
         mService.getAuthorizationIntent("edu.berkeley.waverecipe.AccelerometerMagnitude");
         mService.registerRecipeOutputListener(mListener, false);
     }
     
     /**
      * test the recipeExists call
      */
     @LargeTest
     public void testRecipeExistsNoDownload() throws Exception {
         Intent startIntent = new Intent(Intent.ACTION_MAIN);
         startIntent.setClass(getContext(), WaveService.class);
         IBinder service = bindService(startIntent); 
         
         IWaveServicePublic mService = IWaveServicePublic.Stub.asInterface(service);
         
         // we bypass the net download of the recipe and manually cache it
         cachedRecipe = TestUtils.copyTestAssetToInternal(getSystemContext(), "fixtures/waverecipes/one.waverecipe", WaveRecipe.WAVERECIPE_CACHE_DIR+"/edu.berkeley.waverecipe.AccelerometerMagnitude.waverecipe");
         System.out.println("cachedRecipe => "+cachedRecipe);
         
         assertTrue(mService.recipeExists("edu.berkeley.waverecipe.AccelerometerMagnitude", false));
         
         // now remove it
         if (!cachedRecipe.delete()) {
             throw new Exception("could not remove "+cachedRecipe);
         }
         
         assertFalse(mService.recipeExists("edu.berkeley.waverecipe.AccelerometerMagnitude", false));
     }
}
