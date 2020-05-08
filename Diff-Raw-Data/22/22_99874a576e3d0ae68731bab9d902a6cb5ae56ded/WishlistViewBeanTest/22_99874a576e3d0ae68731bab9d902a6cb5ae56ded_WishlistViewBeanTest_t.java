 package com.wishlistery.view;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 import org.junit.Test;
 
 import com.wishlistery.domain.User;
 import com.wishlistery.domain.Wishlist;
 import com.wishlistery.domain.WishlistItem;
 import com.wishlistery.domain.WishlistView;
 import com.wishlistery.security.UserSession;
 
 @SuppressWarnings("serial")
 public class WishlistViewBeanTest {
 
     @Test
     public void testAddItem() {
         final AtomicBoolean called = new AtomicBoolean(false); 
         WishlistViewBean bean = new WishlistViewBean() {
             @Override
             public String save() {
                 called.set(true);
                 return null;
             }
         };
         bean.entity = new WishlistView();
         bean.entity.setItems(new ArrayList<WishlistItem>());
         bean.addItem(new WishlistItem());
         assertEquals(1, bean.entity.getItems().size());
         assertTrue(called.get());
     }
 
     @Test
     public void testRemoveItem() {
         final AtomicBoolean called = new AtomicBoolean(false); 
         WishlistViewBean bean = new WishlistViewBean() {
             @Override
             public String save() {
                 called.set(true);
                 return null;
             }
         };
         WishlistItem item = new WishlistItem();
         bean.entity = new WishlistView();
         bean.entity.setItems(new ArrayList<WishlistItem>(Arrays.asList(item)));
         bean.removeItem(item);
         assertEquals(0, bean.entity.getItems().size());
         assertTrue(called.get());
     }
 
     @Test
     public void testIsViewAsOtherUser() {
         WishlistViewBean bean = new WishlistViewBean();
         bean.setViewAsOtherUser(true);
         assertTrue(bean.isViewAsOtherUser());
     }
 
     @Test
     public void testIsOriginalUser() {
         WishlistViewBean bean = new WishlistViewBean();
         assertFalse(bean.isOriginalUser());
         
         final AtomicBoolean loggedIn = new AtomicBoolean(false);
         bean.session = new UserSession() {
             @Override
             public boolean isLoggedIn() {
                 return loggedIn.get();
             }
             
             @Override
             public User getUser() {
                User user = new User();
                user.setId(1);
                return user;
             }
         };
         assertFalse(bean.isOriginalUser());
         
         loggedIn.set(true);
         
         bean.entity = new WishlistView();
         bean.entity.setWishlist(new Wishlist());
        bean.entity.getWishlist().setUser(null);
         
         assertFalse(bean.isOriginalUser());
         
        User user = new User();
        user.setId(2);
        bean.entity.getWishlist().setUser(user);
         
         assertFalse(bean.isOriginalUser());
        user.setId(1);
        bean.entity.getWishlist().setUser(user);
         
         assertTrue(bean.isOriginalUser());
     }
 }
