 package com.wishlistery.domain;
 
 import java.io.Serializable;
 import java.util.List;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.OneToMany;
 import javax.persistence.Version;
 
 
 @Entity
 public class Wishlist extends BaseEntity implements Serializable {
     private static final long serialVersionUID = 1L;
     
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     @Column(name = "id", updatable = false, nullable = false)
     private long id = 0;
     
     @Version
     private int version = 0;
 
     private String name;
     private String description;
     
     private String userId;
     
     @OneToMany(cascade=CascadeType.ALL, orphanRemoval=true, mappedBy="wishlist", fetch=FetchType.EAGER)
     private List<WishlistCategory> categories;
     
     @OneToMany(cascade=CascadeType.ALL, orphanRemoval=true, mappedBy="wishlist", fetch=FetchType.LAZY)
     private List<WishlistView> views;
     
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getDescription() {
         return description;
     }
 
     public void setDescription(String description) {
         this.description = description;
     }
 
     public List<WishlistCategory> getCategories() {
         return categories;
     }
 
     public void setCategories(List<WishlistCategory> categories) {
         this.categories = categories;
     }
 
     public int getVersion() {
         return version;
     }
 
     public void setVersion(final int version) {
         this.version = version;
     }
 
     @Override
     public long getId() {
         return id;
     }
 
     public void setId(long id) {
         this.id = id;
     }
 
     public List<WishlistView> getViews() {
         return views;
     }
 
     public void setViews(List<WishlistView> views) {
         this.views = views;
     }
     
     public boolean isBlank() {
         return categories == null || categories.size() == 0 
                 || (categories.size() == 1 && categories.get(0).getItems().isEmpty());
     }
 
     public String getUserId() {
         return userId;
     }
 
     public void setUserId(String userId) {
         this.userId = userId;
     }
 }
