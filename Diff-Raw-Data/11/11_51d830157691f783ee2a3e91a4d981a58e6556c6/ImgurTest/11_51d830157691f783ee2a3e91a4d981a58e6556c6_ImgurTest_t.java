 package com.github.cmput301w13t04.food.test;
 
 
 import com.github.cmput301w13t04.food.controller.imgurController;
 
 import android.test.AndroidTestCase;
 
 public class ImgurTest extends AndroidTestCase {
 
 	public void testPost() throws Throwable {
 		imgurController ic1 = new imgurController();
 		String dummy = "dummy";
 
		String dummer = ic1.post(dummy);
		assertNull(dummer);
 	}
 
 	public void testFetch() throws Throwable {
 		imgurController ic1 = new imgurController();
 		String dummy = "dummy";
		String dummer = ic1.fetch(dummy);
		assertNull(dummer);
 	}
 }
