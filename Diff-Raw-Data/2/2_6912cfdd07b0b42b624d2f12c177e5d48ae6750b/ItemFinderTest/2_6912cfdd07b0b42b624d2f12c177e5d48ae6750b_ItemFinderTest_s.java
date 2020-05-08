 package com.blogspot.aptgetmoo.dhjclient.test.finder;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.fail;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import com.blogspot.aptgetmoo.dhjclient.finder.ItemFinder;
 import com.blogspot.aptgetmoo.dhjclient.finder.IItemFinder;
 import com.blogspot.aptgetmoo.dhjclient.finder.Item;
 import com.blogspot.aptgetmoo.dhjclient.finder.ItemType;
 
 public class ItemFinderTest {
 
     private static final int ROWS = 20;
 
     private IItemFinder mFinder;
 
     @Before public void setUp() throws Exception {
         mFinder = new ItemFinder(new MockResultPage(), ROWS);
     }
 
     @After public void tearDown() throws Exception {
     }
 
    @Test public void constructorDefaultItemsPerPage() {
         mFinder = new ItemFinder(new MockResultPage());
 
         assertNotNull(mFinder);
         assertEquals(20, mFinder.getItemsPerPage());
         assertEquals(0, mFinder.getResultCount());
         assertEquals(0, mFinder.getTotalPage());
     }
 
     @Test public void constructor() {
         assertNotNull(mFinder);
         assertEquals(20, mFinder.getItemsPerPage());
         assertEquals(0, mFinder.getResultCount());
         assertEquals(0, mFinder.getTotalPage());
     }
 
     /**
      * TODO: Should the parsed result be unit-tested? since it has no well defined structure :( ,
      * it might be a total waste of time
      */
     @Test public void find() {
         ArrayList<Item> items = new ArrayList<Item>();
         try {
             items = mFinder.find("kfc", ItemType.PRODUCT, 1);
         } catch (IOException e) {
             fail("Unexpected IOException");
         }
 
         assertEquals(20,items.size());
     }
 
     @Test public void getTotalPage() {
         assertEquals(0, mFinder.getTotalPage());
 
         try {
             mFinder.find("kfc", ItemType.PRODUCT, 1);
         } catch (IOException e) {
             fail("Unexpected IOException");
         }
 
         assertEquals(4, mFinder.getTotalPage());
     }
 
     @Test public void getResultCount() {
         assertEquals(0, mFinder.getResultCount());
 
         try {
             mFinder.find("kfc", ItemType.PRODUCT, 1);
         } catch (IOException e) {
             fail("Unexpected IOException");
         }
 
         assertEquals(69, mFinder.getResultCount());
     }
 
 }
