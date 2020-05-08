 package models;
 
 import java.util.Date;
 
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 
 import play.data.format.Formats;
 import play.data.validation.Constraints;
 import play.db.ebean.Model;
 
 import com.avaje.ebean.Page;
 
 /**
  * Computer entity managed by ebean.
  */
 @Entity
 @SuppressWarnings("serial")
 public class Computer extends Model {
 
     @Id
     public Long id;
 
     @Constraints.Required
     public String name;
 
     @Formats.DateTime(pattern = "yyyy-MM-dd")
     public Date introduced;
 
     @Formats.DateTime(pattern = "yyyy-MM-dd")
     public Date discontinued;
 
     @ManyToOne
     public Company company;
 
     public Computer() {
     }
 
     public Computer(String name, Date introduced, Company company) {
         this.name = name;
         this.introduced = introduced;
         this.company = company;
     }
 
     public static Finder<Long, Computer> find = new Finder<Long, Computer>(Long.class, Computer.class);
 
     public static Page<Computer> page(int page, int pageSize, String sortBy, String order, String filter) {
         return find.where()
                 .ilike("name", "%" + filter + "%")
                 .orderBy(sortBy + " " + order)
                 .findPagingList(pageSize)
                 .getPage(page);
     }
 
 }
