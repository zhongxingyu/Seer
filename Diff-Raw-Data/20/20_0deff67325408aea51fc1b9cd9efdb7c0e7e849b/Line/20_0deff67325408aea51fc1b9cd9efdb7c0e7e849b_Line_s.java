 package models;
  
 import play.data.validation.Email;
 import play.data.validation.Required;
 import play.modules.morphia.Model;
 import play.modules.morphia.Model.AutoTimestamp;
 
 import com.google.code.morphia.annotations.Entity;
 import com.google.code.morphia.annotations.Reference;
 

/**
 * The line class
 */
 @AutoTimestamp
 @Entity
 public class Line extends Model {
 
   // @Required
   // public int id;
 
   @Required
   @Reference
   public Budget budget;
 
   @Reference
   public User user;
 
   @Required
  public int lineNumber;
 
   @Required
   public String name;
 
   @Required
   public double subtotal;
 
   public int parent_line_id;
 
   @Required
   public int order;
 
  /**
   * Creates a line object.
   *
   * @param budget The budget associated with this line
   * @param user The user who created/edited this line
   * @param lineNumber The line number associated with this line.
   * @param subtotal The subtotal of this line.
   * @param parentLineId The id of the parent line, if any.
   * @param order The order that the line is supposed to be rendered.
   */
  public Line(Budget budget, User user, int lineNumber, String name, double subtotal, int parentLineId, int order) {
     // this.id = id;
     this.budget = budget;
     this.user = user;
     this.line_number = line_number;
     this.name = name;
     this.subtotal = subtotal;
     this.parent_line_id = parent_line_id;
     this.order = order;
   }
 
 
   public String toString() {
     return name;
   }
 
 }
