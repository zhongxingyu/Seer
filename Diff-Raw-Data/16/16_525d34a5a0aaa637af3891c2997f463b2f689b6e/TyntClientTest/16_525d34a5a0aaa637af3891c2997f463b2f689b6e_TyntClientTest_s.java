 package com.tynt.api.v1;
 
import java.util.List;
 import com.tynt.api.v1.model.Image;
 import com.tynt.api.v1.model.Images;
 import com.tynt.api.v1.model.Page;
 import com.tynt.api.v1.model.Pages;
 import org.junit.Test;
 import com.tynt.api.v1.model.Category;
import com.tynt.api.v1.model.SearchResults;
 import com.tynt.api.v1.model.TyntApiException;
 import java.util.Collection;
 import org.junit.BeforeClass;
 import static org.junit.Assert.*;
 
 /**
  * A test for the TyntClient (API facade) class.
  * 
  * @author Bryan Sant <bryan@tynt.com>
  */
 public class TyntClientTest {
 
     static String tyntAppId = System.getProperty("tynt.api.appid");
     static TyntClient client;
 
     @BeforeClass
     public static void init() {
         if (tyntAppId == null) {
             System.err.println("*** No Tynt app ID was specified");
             System.err.println("*** You can obtain an app ID from http://dev.tynt.com");
            System.err.println("*** Once, you have a Tynt app ID, re-run this test with '-Dtynt.api.appid=<your-app-id>'");
             System.exit(1);
         }
 
         client = new TyntClient(tyntAppId);
     }
 
     @Test
     public void testTopCategories() throws Exception {
         Collection<Category> categories = client.getTopCategories();
 
         assertNotNull("No categories were returned!", categories);
 
         Category nycersCategory = null;
         for (Category category : categories) {
             if (category.getDisplayName().equals("New Yorkers")) {
                 nycersCategory = category;
             }
         }
         assertNotNull("Couldn't find the 'New Yorkers' category", nycersCategory);
 
         Pages pages = client.getTopPagesForCategory(nycersCategory);
         assertNotNull(pages);
         Images images = client.getTopImagesForCategory(nycersCategory);
         assertNotNull(images);
     }
 
     @Test
     public void testTopPages() throws Exception {
         Pages pages = client.getTopPagesForCategory("nycers");
 
         assertNotNull("No pages were returned!", pages);
         assertTrue("No pages were returned!", pages.getItems().size() > 0);
 
         for (Page page : pages.getItems()) {
             assertTrue("Invalid page (missing URL)", page.getUrlText() != null);
         }
     }
 
     @Test
     public void testTopImages() throws Exception {
         Images images = client.getTopImagesForCategory("sports");
 
         assertNotNull("No images were returned!", images);
         assertTrue("No images were returned!", images.getItems().size() > 0);
 
         for (Image image : images.getItems()) {
             assertTrue("Invalid image (missing URL)", image.getImageUrlText() != null);
         }
     }
 
     @Test
     public void testTopTerms() throws Exception {
         Collection<String> terms = client.getTopSearchTerms("technology");
 
         assertNotNull("No terms were returned!", terms);
         assertTrue("No terms were returned!", terms.size() > 0);
     }
 
     @Test
     public void testBadCategory() {
         TyntApiException ex = null;
         try {
             Pages pages = client.getTopPagesForCategory("foo");
         }
         catch (TyntApiException e) {
             ex = e;
         }
 
         assertNotNull("Bad category should have failed, but didn't!", ex);
         assertEquals("Incorrect error message received", ex.getMessage(), "Unknown category 'foo'");
     }
 }
