 
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
   public int budget_id;
 
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
 
   /**
    * Transaction object constructor
    *
    * @param budget The budget this transaction belongs to
    * @param user The user that created the budget
    * @param subline_number The subline that this transaction belongs to
    * @param name The name of the transaction
    * @param subtotal The total of the transaction.
    * @param order Arbitrary order integer for sorting purposes.
    */
 
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
