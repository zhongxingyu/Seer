 package com.ngdb.entities.article;
 
 import com.google.common.base.Objects;
 import com.ngdb.entities.article.element.*;
 import com.ngdb.entities.reference.Origin;
 import com.ngdb.entities.reference.Platform;
 import com.ngdb.entities.reference.Publisher;
 import com.ngdb.entities.reference.State;
 import com.ngdb.entities.shop.ShopItem;
 import com.ngdb.entities.shop.ShopItems;
 import com.ngdb.entities.shop.Wish;
 import com.ngdb.entities.user.CollectionObject;
 import com.ngdb.entities.user.User;
 import org.apache.commons.lang.StringUtils;
 import org.hibernate.annotations.Cache;
 import org.hibernate.annotations.CacheConcurrencyStrategy;
 import org.ocpsoft.pretty.time.PrettyTime;
 
 import javax.persistence.*;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlTransient;
 import java.io.Serializable;
 import java.util.*;
 
 import static javax.xml.bind.annotation.XmlAccessType.FIELD;
 
 @Entity
 @Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
 @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
 public abstract class Article implements Comparable<Article>, Serializable{
 
     private static final int MAX_DETAIL_LENGTH = 1024;
 
     @Column(name = "creation_date", nullable = false)
     private Date creationDate;
 
     @Column(name = "modification_date", nullable = false)
     private Date modificationDate;
 
     @Id
     @GeneratedValue(strategy = GenerationType.TABLE)
     private Long id;
 
     @Column(nullable = false)
     private String title;
 
     @Column(name = "release_date")
     @Temporal(TemporalType.DATE)
     private Date releaseDate;
 
     @Column(name = "origin_title")
     private String originTitle;
 
     @Column(name = "platform_short_name")
     private String platformShortName;
 
     @OneToOne(fetch = FetchType.LAZY)
     private Publisher publisher;
 
     @Embedded
     private Notes notes;
 
     @Embedded
     private Tags tags;
 
     @Embedded
     private Files files;
 
     @Embedded
     private ArticlePictures pictures;
 
     @Column(name = "cover_url", nullable = true)
     private String coverUrl;
 
     @Embedded
     private Reviews reviews;
 
     @Embedded
     private Comments comments;
 
     @OneToMany(mappedBy = "article")
     private Set<Wish> wishList;
 
     @Embedded
     private ShopItems shopItems;
 
     @OneToMany(mappedBy = "article")
     private Set<CollectionObject> owners;
 
     private String details;
 
     private String upc;
 
     private String reference;
 
     Article() {
         this.creationDate = this.modificationDate = new Date();
     }
 
     public void updateModificationDate() {
         modificationDate = new Date();
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj instanceof Article) {
             Article a = (Article) obj;
             if (id.equals(a.id)) {
                 return true;
             }
         }
         return super.equals(obj);
     }
 
     @Override
     public int hashCode() {
         return Objects.hashCode(id, title);
     }
 
     public void setOrigin(Origin origin) {
         this.originTitle = origin.getTitle();
     }
 
     public void addPicture(Picture picture) {
         pictures.add(picture);
     }
 
     public ArticlePictures getPictures() {
         return pictures;
     }
 
     public Notes getNotes() {
         return notes;
     }
 
     public Tags getTags() {
         return tags;
     }
 
     public Files getFiles() {
         return files;
     }
 
     public boolean hasFiles() {
         return files.getCount() > 0;
     }
 
     public Set<User> getOwners() {
         Set<User> users = new HashSet<User>();
         for (CollectionObject owner : owners) {
             users.add(owner.getOwner());
         }
         return users;
     }
 
     public void setCover(Picture picture) {
         this.coverUrl = picture.getUrl();
     }
 
     public void setCover(String url) {
         this.coverUrl = url;
     }
 
     public Picture getCover() {
         if(coverUrl == null) {
             return Picture.EMPTY;
         }
         return new Picture(coverUrl);
     }
 
     public void setDetails(String details) {
         details = StringUtils.defaultString(details);
         int end = details.length() < MAX_DETAIL_LENGTH ? details.length() : MAX_DETAIL_LENGTH;
         this.details = details.substring(0, end);
     }
 
     public String getDetails() {
         return details;
     }
 
     public int getOwnersCount() {
         return owners.size();
     }
 
     public int getWishersCount() {
         return wishList.size();
     }
 
     public Reviews getReviews() {
         return reviews;
     }
 
     public String getTitle() {
         return title;
     }
 
     public void setTitle(String title) {
         this.title = title;
     }
 
     public Comments getComments() {
         return comments;
     }
 
     public void setReleaseDate(Date releaseDate) {
         this.releaseDate = releaseDate;
     }
 
     public Date getReleaseDate() {
         return releaseDate;
     }
 
     public Long getId() {
         return id;
     }
 
     public boolean isBuyable() {
         return getAvailableCopyCount() > 0;
     }
 
     public int getAvailableCopyCount() {
         return shopItems.getAvailableCopyCount();
     }
 
     public ShopItems getShopItems() {
         return shopItems;
     }
 
     @Override
     public int compareTo(Article article) {
         return title.compareToIgnoreCase(StringUtils.defaultString(article.title));
     }
 
     @Override
     public String toString() {
         return title;
     }
 
     public Collection<ShopItem> getShopItemsForSale() {
         return shopItems.getShopItemsForSale();
     }
 
     public boolean hasShopItemInState(State state) {
         return shopItems.hasShopItemInState(state);
     }
 
     public int getAvailableCopyInState(State state) {
         return shopItems.getAvailableCopyInState(state);
     }
 
     public double getAveragePriceInState(State state) {
         return shopItems.getAveragePriceInState(state);
     }
 
     public double getMaxPriceInState(State state) {
         return shopItems.getMaxPriceInState(state);
     }
 
     public double getMinPriceInState(State state) {
         return shopItems.getMinPriceInState(state);
     }
 
     public void setPlatform(Platform platform) {
         this.platformShortName = platform.getShortName();
     }
 
     public boolean containsTag(String tag) {
         return tags.contains(tag);
     }
 
     public void addTag(Tag tag) {
         tags.add(tag);
     }
 
     public void addReview(Review review) {
         if(reviews == null) {
             reviews = new Reviews();
         }
         reviews.add(review);
     }
 
     public void removePicture(Picture picture) {
         pictures.remove(picture);
     }
 
     public boolean containsTag(Tag tag) {
         return tags.contains(tag);
     }
 
     public void addNote(Note note) {
         notes.add(note);
     }
 
     public Publisher getPublisher() {
         return publisher;
     }
 
     public void setPublisher(Publisher publisher) {
         this.publisher = publisher;
     }
 
     public boolean hasCover() {
         return getCover() != Picture.EMPTY;
     }
 
     public void setUpc(String upc) {
         this.upc = upc;
     }
 
     public String getUpc() {
         return upc;
     }
 
     public abstract boolean isGame();
     public abstract boolean isHardware();
     public abstract boolean isAccessory();
 
     public abstract String getViewPage();
 
     public String getAverageMark() {
         if (!getHasReviews()) {
             return "0";
         }
         int sum = 0;
         for (Review review : reviews) {
             sum += review.getMarkInPercent();
         }
         return Integer.toString((sum / reviews.count()) / 2);
     }
 
     public boolean getHasReviews() {
         return reviews.count() > 0;
     }
 
     public void addFile(File file) {
         files.add(file);
     }
 
     public boolean hasTags() {
         return tags.count() > 0;
     }
 
     public String getOriginTitle() {
         return originTitle;
     }
 
     public String getPlatformShortName() {
         return platformShortName;
     }
 
     public String getReference() {
         return reference;
     }
 
     public void setReference(String reference) {
         this.reference = reference;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public void setOriginTitle(String originTitle) {
         this.originTitle = originTitle;
     }
 
     public void setPlatformShortName(String platformShortName) {
         this.platformShortName = platformShortName;
     }
 }
