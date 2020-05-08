 package com.wishlistery.domain;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 import java.io.IOException;
 
 import org.apache.commons.io.IOUtils;
 import org.junit.Test;
 
 import com.google.common.base.Strings;
 import com.google.common.collect.ImmutableSet;
 
 public class WishlistTest {
 
     Wishlist wishlist = new Wishlist();
     
     @Test
     public void canAddAndRemoveItems() {
         WishlistItem item = new WishlistItem();
         wishlist.addItem(item);
         assertEquals(1, wishlist.getItems().size());
        assertEquals(1, item.getId());
         
         wishlist.removeItem(item);
         assertEquals(0, wishlist.getItems().size());
     }
 
     @Test
     public void canAddAndRemoveCategories() {
         wishlist.addCategory("foo");
         assertEquals(1, wishlist.getCategories().size());
         wishlist.removeCategory("foo");
         assertEquals(0, wishlist.getCategories().size());
     }
     
     @Test
     public void canAddAndRemoveViews() {
         wishlist.addView("foo");
         assertEquals(1, wishlist.getViews().size());
         wishlist.removeView("foo");
         assertEquals(0, wishlist.getViews().size());
     }
     
     
     @Test
     public void testGetItemsInCategory() {
         WishlistItem item = new WishlistItem();
         item.setCategory("foo");
         wishlist.addItem(item);
         
         item = new WishlistItem();
         item.setCategory("blah");
         wishlist.addItem(item);
         
         wishlist.addItem(new WishlistItem());
         wishlist.addItem(new WishlistItem());
         
         
         assertEquals(1, wishlist.getItemsInCategory("foo").size());
         assertEquals(1, wishlist.getItemsInCategory("blah").size());
         
         assertEquals(2, wishlist.getItemsInCategory("").size());
         assertEquals(2, wishlist.getItemsInCategory(null).size());
     }
 
     @Test
     public void testGetItemsInView() {
         WishlistItem item = new WishlistItem();
         item.setViews(ImmutableSet.of("foo", "bar"));
         wishlist.addItem(item);
         
         item = new WishlistItem();
         item.setViews(ImmutableSet.of("foo"));
         wishlist.addItem(item);
         
         wishlist.addItem(new WishlistItem());
         wishlist.addItem(new WishlistItem());
         
         
         assertEquals(2, wishlist.getItemsInView("foo").size());
         assertEquals(1, wishlist.getItemsInView("bar").size());
     }
 
     @Test
     public void canPrintQuickEditText() {
         wishlist.getViews().add("Skjonsby Fam");
         wishlist.getViews().add("Kasper Fam");
         
         wishlist.addItem(new WishlistItem("art thing", "we want it on the wall, so it should be hangable", null));
         
         wishlist.addCategory("Tools");
         wishlist.addItem(new WishlistItem("Wrench", "craftsman preferably", "http://sears.com/craftsman"));
         wishlist.getItems().get(1).setCategory("Tools");
         wishlist.getItems().get(1).getViews().add("Skjonsby Fam");
         
         wishlist.addItem(new WishlistItem("Pliars", "craftsman preferably", "http://sears.com/craftsman"));
         wishlist.getItems().get(1).setCategory("Tools");
         wishlist.getItems().get(1).getViews().add("Kasper Fam");
         
         System.out.println(wishlist.getQuickEditText());
     }    
 
     @Test
     public void canParseInlineItemString() {
         WishlistItem item = wishlist.parseItemString("Some Item (this is a description) http://somelink.com [Skjonsby, Kasper]");
         System.out.println(item.getTitle());
         System.out.println(item.getDescription());
         System.out.println(item.getLink());
         System.out.println(item.getViews());
         
         assertEquals("Some Item", item.getTitle());
         assertEquals("this is a description", item.getDescription());
         assertEquals("http://somelink.com", item.getLink());
         assertTrue(item.getViews().contains("Skjonsby"));
         assertTrue(item.getViews().contains("Kasper"));
     }
 
     @Test
     public void canParseInlineFormat() throws IOException {
         wishlist.quickEdit(getQuickEditTestData(1));
         System.out.println(wishlist.getQuickEditText());
         
 //        Emma: shoes,shirt,lamp (something shiney)
 //        Eric: gloves,hat [Skjonsby],toys
 
         assertEquals(2, wishlist.getCategories().size());
         assertTrue(wishlist.getCategories().contains("Emma"));
         assertTrue(wishlist.getCategories().contains("Eric"));
         
         assertEquals(6, wishlist.getItems().size());
         assertEquals(1, wishlist.getViews().size());
         assertTrue(wishlist.getViews().contains("Skjonsby"));
     }
     
     @Test
     public void canParseCanonicalQuickEditText() throws IOException {
         wishlist.quickEdit(getQuickEditTestData(0));
         System.out.println(wishlist.getQuickEditText());
         
         assertEquals(2, wishlist.getCategories().size());
         assertTrue(wishlist.getCategories().contains("Tools"));
         assertTrue(wishlist.getCategories().contains("clothes"));
         
         assertEquals(5, wishlist.getItems().size());
         
     }
     
     @Test
     public void canParseCanonicalQuickEditText2() throws IOException {
         wishlist.quickEdit(getQuickEditTestData(3));
         System.out.println(wishlist.getQuickEditText());
         
         assertEquals(2, wishlist.getCategories().size());
         assertTrue(wishlist.getCategories().contains("cat1"));
         assertTrue(wishlist.getCategories().contains("cat3"));
         
         assertEquals(3, wishlist.getItems().size());
         assertEquals("This is a second item", wishlist.getItem(1).getTitle());
         assertEquals("This is it's description", wishlist.getItem(1).getDescription());
         
     }
     
     private String getQuickEditTestData(int num) throws IOException {
         String testData = IOUtils.toString(getClass().getClassLoader().getResourceAsStream("wishlist-quick-edit.txt"));
         String[] data = testData.split(Strings.repeat("=", 50));
 //        System.out.println(Arrays.toString(data));
         return data[num];
     }
 }
