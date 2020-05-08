 package com.github.cmput301w13t04.food.test;
 
 import android.os.Parcel;
 import android.test.AndroidTestCase;
 
 import com.github.cmput301w13t04.food.model.Photo;
 import com.github.cmput301w13t04.food.model.User;
 
 public class PhotoTest extends AndroidTestCase {
 	
 	public void testGetPhotographer() throws Throwable {
 		User Photographer = new User("Photographer");
 		Photo p1 = new Photo("Path", "Description", Photographer);
 		assertEquals(p1.getPhotographer(), Photographer);
 	}
 
 	public void testValidPhotographer() throws Throwable{
 		Photo p2 = new Photo(null, null, null);
 		assertNull(p2.getPhotographer());
 	}
 
 	
 	public void testGetDescription() throws Throwable{
 		User Photographer = new User("Photographer");
 		Photo p1 = new Photo("Path", "Description", Photographer);
 		assertEquals(p1.getDescription(), "Description");
 
 	}
 
 	
 	public void testVaildDescription() throws Throwable{
 		Photo p2 = new Photo(null, null, null);
 		assertNull(p2.getDescription());
 	}
 
 	
 	public void testSetDescription() throws Throwable{
 		User Photographer = new User("Photographer");
 		Photo p1 = new Photo("Path", "Description", Photographer);
 		p1.setDescription("New Description");
 		assertEquals(p1.getDescription(), "New Description");
 	}
 
 	
 	public void testGetPath() throws Throwable{
 		User Photographer = new User("Photographer");
 		Photo p1 = new Photo("Path", "Description", Photographer);
 		assertEquals(p1.getPath(), "Path");
 	}
 
 	
 	public void testValidPath() throws Throwable{
 		Photo p2 = new Photo(null, null, null);
 		assertNull(p2.getPath());
 	}
 
 	
 	public void testSetPath() throws Throwable{
 		User Photographer = new User("Photographer");
 		Photo p1 = new Photo("Path", "Description", Photographer);
 		p1.setPath("New Path");
 		assertEquals(p1.getPath(), "New Path");
 	}
 
 	
 	public void testGetAbsolutePath() throws Throwable{
 		User Photographer = new User("Photographer");
 		Photo p1 = new Photo("Path", "Description", Photographer);
 		String resultPath = null;
 		resultPath = p1.getAbsolutePath();
 		assertNotNull(resultPath);
 		assertEquals(p1.getPath(), "Path");
 
 		p1.setPath("file://Path%20");
 		resultPath = p1.getAbsolutePath();
 		assertNotNull(resultPath);
 		assertEquals(resultPath, "Path ");
 	}
 
 }
