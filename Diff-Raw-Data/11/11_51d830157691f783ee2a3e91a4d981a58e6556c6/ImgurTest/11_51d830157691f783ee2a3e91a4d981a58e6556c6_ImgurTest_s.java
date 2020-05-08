 package com.github.cmput301w13t04.food.test;
 
import java.io.IOException;
 
 import com.github.cmput301w13t04.food.controller.imgurController;
 
 import android.test.AndroidTestCase;
 
 public class ImgurTest extends AndroidTestCase {
 
 	public void testPost() throws Throwable {
 		imgurController ic1 = new imgurController();
 		String dummy = "dummy";
 
		ic1.post(dummy);
		assertNull(dummy);
 	}
 
 	public void testFetch() throws Throwable {
 		imgurController ic1 = new imgurController();
 		String dummy = "dummy";
		ic1.fetch(dummy);
		assertNull(dummy);
 	}
 }
