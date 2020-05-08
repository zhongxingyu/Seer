
 package models;
 
 import play.data.validation.Email;
 import play.data.validation.Required;
 import play.modules.morphia.Model;
 import play.modules.morphia.Model.AutoTimestamp;
 
 import com.google.code.morphia.annotations.Entity;
 import com.google.code.morphia.annotations.Reference;
 
 @AutoTimestamp
 @Entity
 public class Transaction extends Model {
 
   // @Required
   // public int id;
 
   @Required
   @Reference
   public Budget budget;
 
   @Reference
   public User user;
 
   @Required
   public int subline_number;
 
   @Required
   public String name;
 
   @Required
   public double subtotal;
 
   public int subline_id;
 
   @Required
   public int order;
 
  public Transaction(Budget budget, User user, int subline_number, String name, double subtotal, int subline_id, int order) {
     // this.id = id;
     this.budget = budget;
     this.user = user;
    this.subline_number = subline_number;
     this.name = name;
     this.subtotal = subtotal;
     this.subline_id = subline_id;
     this.order = order;
   }
 
 
   public String toString() {
     return name;
   }
 
 }
