 package models.tm;
 
 import java.util.List;
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 import javax.persistence.UniqueConstraint;
 
 import models.account.Account;
 import models.account.AccountModel;
 import models.account.AccountProduct;
 import org.hibernate.annotations.BatchSize;
 import play.data.validation.MaxSize;
 
 /**
  * @author Manuel Bernhardt <bernhardt.manuel@gmail.com>
  */
 @Entity
 @Table(uniqueConstraints = {@UniqueConstraint(name = "id", columnNames = {"naturalId", "account_id"})})
 @BatchSize(size = 10)
 public class Project extends AccountModel {
 
     public String name;
 
     @MaxSize(5000)
     public String description;
 
     @ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH}, optional = true, fetch = FetchType.LAZY)
     public AccountProduct product;
 
     @ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH}, optional = true)
     public ProjectCategory projectCategory;
 
     public Integer reservedSeats;
 
     public Integer maxAvailableSeats;
 
     public Project(String name, Account account, ProjectCategory category) {
         super(account);
         this.account = account;
         this.name = name;
         this.projectCategory = category;
     }
 
     public static List<Project> listByUser(TMUser user) {
        return Project.find("from Project p join Role r join TMUser u where r.project = p and r in elements (u.projectRoles)").<Project>fetch();
     }
 
     @Override
     public boolean equals(Object o) {
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
         if (!super.equals(o)) return false;
 
         Project project = (Project) o;
 
         if (description != null ? !description.equals(project.description) : project.description != null) return false;
         if (maxAvailableSeats != null ? !maxAvailableSeats.equals(project.maxAvailableSeats) : project.maxAvailableSeats != null)
             return false;
         if (!name.equals(project.name)) return false;
         if (product != null ? !product.equals(project.product) : project.product != null) return false;
         if (projectCategory != null ? !projectCategory.equals(project.projectCategory) : project.projectCategory != null)
             return false;
         if (reservedSeats != null ? !reservedSeats.equals(project.reservedSeats) : project.reservedSeats != null)
             return false;
 
         return true;
     }
 
     @Override
     public int hashCode() {
         int result = super.hashCode();
         result = 31 * result + name.hashCode();
         result = 31 * result + (description != null ? description.hashCode() : 0);
         result = 31 * result + (product != null ? product.hashCode() : 0);
         result = 31 * result + (projectCategory != null ? projectCategory.hashCode() : 0);
         result = 31 * result + (reservedSeats != null ? reservedSeats.hashCode() : 0);
         result = 31 * result + (maxAvailableSeats != null ? maxAvailableSeats.hashCode() : 0);
         return result;
     }
 }
