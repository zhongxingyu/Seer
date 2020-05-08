 package oshop.model;
 
import org.hibernate.validator.constraints.NotBlank;

 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
 import java.math.BigDecimal;
 
 @Entity
 @Table(name = "product")
 public class Product extends BaseEntity<Integer> {
 
    @NotNull
    @Min(0)
     @Column(name = "price", precision = 10, scale = 2)
     private BigDecimal price;
 
    @NotNull
    @NotBlank
    @Size(max = 255)
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
