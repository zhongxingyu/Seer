 package dit126.group4.group4shop.core;
 
 import java.io.Serializable;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.Table;
 
 /**
  *
  * @author Group4
  */
 @Entity 
 @Table(name="RATING")
 public class Rating implements Serializable {
     
     @Id
     @Column(name = "ID", nullable = false)
     private Long id;
     @Column(name="USER_ID") private Long user_ID;
    @Column(name="PRODUCT_ID") private Long product_ID;
    @Column(name="RATING") private int rating;
    @Column(name="COMMENT") private String comment;
     
     protected Rating(){}
     
    protected Rating(Long id, Long user_ID, Long product_id, int rating, String comment){
         this.id = id;
         this.user_ID = user_ID;
        this.product_ID = product_id;
         this.rating = rating; 
        this.comment = comment; 
     }
     
 }
