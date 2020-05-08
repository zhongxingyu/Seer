 package models;
 
import com.avaje.ebean.validation.NotEmpty;
import com.avaje.ebean.validation.NotNull;
 import play.db.ebean.Model;
 
 import javax.persistence.Entity;
 import javax.persistence.Id;
import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 
 
 @Entity
 @Table(name="subscription_items")
 public class SubscriptionItem extends Model {
 
     @Id
     public Long id;
 
    @NotNull @NotEmpty
     public String unit;

    @NotNull
     public int quantity;
 
 
     public SubscriptionItem(String unit, int quantity) {
         this.unit = unit;
         this.quantity = quantity;
     }
 
 
     public static Finder<Long, SubscriptionItem> find() {
         return new Finder<Long, SubscriptionItem>(Long.class, SubscriptionItem.class);
     }
 
     public static void create(String unit, int quantity) {
         SubscriptionItem item = new SubscriptionItem(unit, quantity);
         item.save();
     }
 }
