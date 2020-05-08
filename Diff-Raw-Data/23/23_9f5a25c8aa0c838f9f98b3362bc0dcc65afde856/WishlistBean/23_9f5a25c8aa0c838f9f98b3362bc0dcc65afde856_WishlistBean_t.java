 package com.jeffskj.wishlist.view;
 
 import java.io.IOException;
 import java.util.List;
 
 import javax.faces.bean.ViewScoped;
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import org.jboss.seam.international.status.Messages;
 import org.jboss.seam.transaction.Transactional;
 
 import com.google.common.collect.Lists;
 import com.jeffskj.wishlist.domain.Wishlist;
 import com.jeffskj.wishlist.domain.WishlistCategory;
 import com.jeffskj.wishlist.domain.WishlistItem;
 import com.jeffskj.wishlist.domain.WishlistView;
 import com.jeffskj.wishlist.security.UserSession;
 
 
 @Named
 @Transactional
 @ViewScoped
 public class WishlistBean extends BaseBean<Wishlist> {
     private static final long serialVersionUID = 1L;
 
     private WishlistItem item = new WishlistItem();
     private WishlistCategory category = new WishlistCategory();
     private WishlistView view = new WishlistView();
     
     private String newWishlistName;
     
     @Inject
     Messages messages;
     
     @Inject
     UserSession session;
     
     @Override
     protected Wishlist newEntityInstance() {
         Wishlist wishlist = new Wishlist();
         if (session != null && session.isLoggedIn()) {
             wishlist.setUserId(session.getUser().getId());
         }
         WishlistCategory cat = new WishlistCategory(WishlistCategory.DEFAULT);
         wishlist.setCategories(Lists.newArrayList(cat));
         cat.setWishlist(wishlist);
         return wishlist;
     }
 
     public boolean isOriginalUser() {
         if (session != null && session.isLoggedIn()) {
             return entity.getUserId() != null && entity.getUserId().equals(session.getUser().getId());
         }
         return false;
     }
     
     @Override
     public String save() {
         if (newWishlistName != null) {
             Wishlist current = entity;
             long currentId = getId();
             setId(0);
             entity = newEntityInstance();
             entity.setName(newWishlistName);
             String dest = super.save();
             setId(currentId);
             entity = current;
             
             return dest;
         }
         
         if (!isOriginalUser()) { return "pretty:home"; }
         return super.save();
     }
     
     public List<Wishlist> getUserWishlists() {
         return em.createQuery("from Wishlist where userId = :id", Wishlist.class)
                 .setParameter("id", session.getUser().getId()).getResultList();
     }
     
     public String createView() throws IOException {
         if (!isOriginalUser()) { return "pretty:home"; }
         
         view.setWishlist(entity);
         em.persist(view);
         entity.getViews().add(view);
        return "/wishlistView.jsf?faces-redirect=true&id=" + view.getId();
     }
     
     public void addItem() {
         if (!isOriginalUser()) { return; }
         
         for (WishlistCategory cat : entity.getCategories()) {
             if (cat.equals(item.getCategory())) {
                 cat.getItems().add(item);
                 break;
             }
         }
         save();
         item = new WishlistItem();
     }
 
     public void removeItem() {
         if (!isOriginalUser()) { return; }
         
         for (WishlistCategory cat : entity.getCategories()) {
             if (cat.equals(item.getCategory())) {
                 cat.getItems().remove(item);
             }
         }
         save();
         item = new WishlistItem();
     }
     
     public void updateItem() {
         if (!isOriginalUser()) { return; }
         
         for (WishlistCategory cat : entity.getCategories()) {
             if (cat.equals(item.getCategory())) {
                 for (WishlistItem it : cat.getItems()) {
                     if (it.equals(item)) {
                         it.setCategory(item.getCategory());
                         it.setDescription(item.getDescription());
                         it.setLink(item.getLink());
                         it.setTitle(item.getTitle());
                     }
                 }
             }
         }
         save();
         item = new WishlistItem();
     }
     
     public void addCategory() {
         if (!isOriginalUser()) { return; }
         
         entity.getCategories().add(category);
         category.setWishlist(entity);
         save();
         category = new WishlistCategory();
     }
     
     public void removeCategory() {
         if (!isOriginalUser()) { return; }
     
         if (!category.getItems().isEmpty()) {
             messages.error("cannot remove non-empty category");
             return;
         }
         entity.getCategories().remove(category);
         save();
         category = new WishlistCategory();
     }
     
     public void updateCategory() {
         if (!isOriginalUser()) { return; }
         
         for (WishlistCategory cat : entity.getCategories()) {
             if (cat.equals(category)) {
                 cat.setName(category.getName());
             }
         }
         save();
         category = new WishlistCategory();
     }
     
     public WishlistItem getItem() {
         return item;
     }
 
 
     public void setItem(WishlistItem item) {
         this.item = item;
     }
 
 
     public WishlistCategory getCategory() {
         return category;
     }
 
 
     public void setCategory(WishlistCategory category) {
         this.category = category;
     }
 
 
     public WishlistView getView() {
         return view;
     }
 
 
     public void setView(WishlistView view) {
         this.view = view;
     }
 
     public String getNewWishlistName() {
         return newWishlistName;
     }
 
     public void setNewWishlistName(String newWishlistName) {
         this.newWishlistName = newWishlistName;
     }
 }
