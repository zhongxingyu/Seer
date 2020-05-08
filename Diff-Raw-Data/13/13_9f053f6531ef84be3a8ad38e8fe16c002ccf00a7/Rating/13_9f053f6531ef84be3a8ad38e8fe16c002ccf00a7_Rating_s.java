 package dit126.group4.group4shop.core;
 
 import java.io.Serializable;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.Table;
import javax.validation.constraints.*;
 
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
    @Column(name="RATING") private String rating;
     
     protected Rating(){}
     
    protected Rating(Long id, Long user_ID, String rating){
         this.id = id;
         this.user_ID = user_ID;
         this.rating = rating; 
     }
     
 }
