 package com.ngdb.entities.shop;
 
 import com.ngdb.entities.article.Article;
 import com.ngdb.entities.article.element.Picture;
 import com.ngdb.entities.article.element.ShopItemPictures;
 import com.ngdb.entities.reference.State;
 import com.ngdb.entities.user.User;
 import org.apache.commons.lang.StringUtils;
 import org.hibernate.annotations.Cache;
 import org.hibernate.annotations.CacheConcurrencyStrategy;
 import org.ocpsoft.pretty.time.PrettyTime;
 
 import javax.persistence.*;
 import java.io.Serializable;
 import java.util.Date;
 import java.util.Locale;
 import java.util.Set;
 
 import static java.text.MessageFormat.format;
 import static javax.persistence.FetchType.LAZY;
 
 @Entity
 @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
 public class ShopItem implements Comparable<ShopItem>, Serializable {
 
     @Id
     @GeneratedValue(strategy = GenerationType.TABLE)
     private Long id;
 
     @ManyToOne(optional = false, fetch = LAZY)
     private User seller;
 
     private static final int MAX_DETAIL_LENGTH = 1024;
 
     @Column(name = "creation_date", nullable = false)
     private Date creationDate;
 
     @Column(name = "modification_date", nullable = false)
     private Date modificationDate;
 
     @Embedded
     private ShopItemPictures pictures;
 
     @OneToOne
     private State state;
 
     @ManyToOne(optional = false, fetch = LAZY)
     private Article article;
 
     private String details;
 
     private Boolean sold = false;
 
     private Double priceInDollars;
 
     private Double priceInEuros;
 
     private Double priceInCustomCurrency;
 
     private String customCurrency;
 
     @Embedded
     private PotentialBuyers potentialBuyers;
 
     public ShopItem() {
         this.creationDate = this.modificationDate = new Date();
     }
 
     @PreUpdate
     public void update() {
         this.modificationDate = new Date();
     }
 
     public String getTitle() {
         return article.getTitle();
     }
 
     public State getState() {
         return state;
     }
 
     public Article getArticle() {
         return article;
     }
 
     public String getDetails() {
         return details;
     }
 
     public Date getCreationDate() {
         return creationDate;
     }
 
     public User getSeller() {
         return seller;
     }
 
     public boolean isSold() {
         return sold == null ? false : sold;
     }
 
     public Long getId() {
         return id;
     }
 
     public Date getModificationDate() {
         return modificationDate;
     }
 
     public void setState(State state) {
         this.state = state;
     }
 
     public void setDetails(String details) {
         details = StringUtils.defaultString(details);
         int end = details.length() < MAX_DETAIL_LENGTH ? details.length() : MAX_DETAIL_LENGTH;
         this.details = details.substring(0, end);
     }
 
     public void setCreationDate(Date creationDate) {
         this.creationDate = creationDate;
     }
 
     public void setArticle(Article article) {
         this.article = article;
     }
 
     public void setSeller(User seller) {
         this.seller = seller;
     }
 
     public void addPicture(Picture picture) {
         pictures.add(picture);
     }
 
     public ShopItemPictures getPictures() {
         return pictures;
     }
 
     public Picture getMainPicture() {
         if (hasNoPicture()) {
             return article.getCover();
         }
         return pictures.first();
     }
 
     public void addPotentialBuyer(User potentialBuyer) {
         potentialBuyers.add(potentialBuyer);
     }
 
     public boolean isNotAlreadyWantedBy(User potentialBuyer) {
         return !potentialBuyers.contains(potentialBuyer);
     }
 
     public void sold() {
         this.sold = true;
     }
 
     public boolean hasNoPicture() {
         return pictures.getCount() == 0;
     }
 
     public void updateModificationDate() {
         this.modificationDate = new Date();
     }
 
     @Override
     public String toString() {
         return format("{0} by {1} for ${2} ({3}e)", getArticle().getTitle(), seller.getLogin(), getPriceInDollars(), getPriceInEuros());
     }
 
     public String getForSaleDate() {
         return new PrettyTime(Locale.UK).format(getCreationDate());
     }
 
     public void setPriceInDollars(Double priceInDollars) {
         this.priceInDollars = priceInDollars;
     }
 
     public Double getPriceInDollars() {
         return priceInDollars;
     }
 
     public void setPriceInEuros(Double priceInEuros) {
         this.priceInEuros = priceInEuros;
     }
 
     public Double getPriceInEuros() {
         return priceInEuros;
     }
 
     public void removePicture(Picture picture) {
         pictures.remove(picture);
     }
 
     @Override
     public int compareTo(ShopItem shopItem) {
        if(shopItem == null || shopItem.article == null) {
            return 0;
        }
         return article.getTitle().compareToIgnoreCase(shopItem.article.getTitle());
     }
 
     public Set<User> getPotentialBuyers() {
         return potentialBuyers.all();
     }
 
     public int getPicturesCount() {
         return pictures.getCount();
     }
 
     public boolean hasCover() {
         return article.hasCover();
     }
 
     public Double getPriceInCustomCurrency() {
         return priceInCustomCurrency;
     }
 
     public void setPriceInCustomCurrency(Double priceInCustomCurrency) {
         this.priceInCustomCurrency = priceInCustomCurrency;
     }
 
     public String getCustomCurrency() {
         return customCurrency;
     }
 
     public void setCustomCurrency(String customCurrency) {
         this.customCurrency = customCurrency;
     }
 
     public Double getPriceIn(String currency) {
         if (currency.equalsIgnoreCase(customCurrency)) {
             return priceInCustomCurrency;
         }
         return null;
     }
 }
