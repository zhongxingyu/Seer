 package com.jeffskj.wishlist.view;
 
 import javax.faces.bean.ViewScoped;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import org.jboss.seam.international.status.Messages;
 import org.jboss.seam.transaction.Transactional;
 
 import com.jeffskj.wishlist.domain.WishlistItem;
 import com.jeffskj.wishlist.domain.WishlistView;
 
 
 @Named
 @Transactional
 @ViewScoped
 public class WishlistViewBean extends BaseBean<WishlistView> {
     private static final long serialVersionUID = 1L;
 
     @Inject
     Messages messages;
     
     @Inject
    WishlistBean wishlistBean;
     
     private boolean viewAsOtherUser;
     
     @Override
     protected WishlistView newEntityInstance() {
         return new WishlistView();
     }
     
     public void addItem(WishlistItem item) {
         entity.getItems().add(item);
         save();
     }
     
     public void removeItem(WishlistItem item) {
         entity.getItems().remove(item);
         save();
     }
 
     public boolean isViewAsOtherUser() {
        return viewAsOtherUser;
     }
 
     public void setViewAsOtherUser(boolean viewAsOtherUser) {
         this.viewAsOtherUser = viewAsOtherUser;
     }
 }
