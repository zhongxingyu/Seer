 package com.google.code.easyshopper.utility;
 
 import android.net.Uri;
 import android.test.AndroidTestCase;
 
 import com.google.code.easyshopper.utility.CameraUtils;
 public class CameraUtilsTest extends AndroidTestCase {
 
 	public void testPathComposition() {
 		Uri imagePath = CameraUtils.getImagePath("another_path");
 		Uri expUri = new Uri.Builder().scheme("file").appendEncodedPath("//sdcard/Android/data/com.gmail.gulino.marco.easyshopper/another_path").build();
		assertEquals(expUri, imagePath);
 	}
 }
