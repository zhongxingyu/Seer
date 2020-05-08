 package oshop.model;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 import java.math.BigDecimal;
 
 @Entity
 @Table(name = "product")
 public class Product extends BaseEntity<Integer> {
 
     @Column(name = "price", precision = 10, scale = 2)
     private BigDecimal price;
 
     @Column(name = "name")
     private String name;
 
     @ManyToOne(fetch = FetchType.LAZY)
     private ProductCategory category;
 
     private Integer imageId;
 
     public BigDecimal getPrice() {
         return price;
     }
 
     public void setPrice(BigDecimal price) {
         this.price = EntityUtils.round(price);
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public ProductCategory getCategory() {
         return category;
     }
 
     public void setCategory(ProductCategory category) {
         this.category = category;
     }
 
     public Integer getImageId() {
         return imageId;
     }
 
     public void setImageId(Integer imageId) {
         this.imageId = imageId;
     }
 }
