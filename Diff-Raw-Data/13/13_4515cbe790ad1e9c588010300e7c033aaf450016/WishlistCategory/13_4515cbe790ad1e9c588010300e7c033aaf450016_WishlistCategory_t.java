 package com.wishlistery.domain;
 
 import java.io.Serializable;
 import java.util.List;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
 import javax.persistence.Version;
 
 import com.google.common.collect.Lists;
 
 @Entity
 public class WishlistCategory extends BaseEntity implements Serializable {
     public static final String DEFAULT = "(none)";
     
     private static final long serialVersionUID = 1L;
     @Id
     @GeneratedValue(strategy = GenerationType.AUTO)
     @Column(name = "id", updatable = false, nullable = false)
     private long id = 0;
 
     @JoinColumn
     @ManyToOne(cascade=CascadeType.PERSIST)
     private Wishlist wishlist;
     private String name;
     
    @OrderBy
     @OneToMany(cascade=CascadeType.ALL, orphanRemoval=true, mappedBy="category")
     private List<WishlistItem> items = Lists.newArrayList();
 
     @Version
     private int version = 0;
 
     public WishlistCategory() {
     }
     
     public WishlistCategory(String name) {
         this.name = name;
     }
 
     public Wishlist getWishlist() {
         return wishlist;
     }
 
     public void setWishlist(Wishlist wishlist) {
         this.wishlist = wishlist;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public List<WishlistItem> getItems() {
         return items;
     }
 
     public void setItems(List<WishlistItem> items) {
         this.items = items;
     }
 
     @Override
     public long getId() {
         return id;
     }
 
     public void setId(final long id) {
         this.id = id;
     }
 
     public int getVersion() {
         return version;
     }
 
     public void setVersion(final int version) {
         this.version = version;
     }
     
     public boolean isDefault() {
         return name != null && name.equals(DEFAULT);
     }
 }
