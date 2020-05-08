 package net.kurochenko.ispub.department.form;
 
 import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
 import javax.validation.constraints.Size;
 
 /**
  *
  * @author Andrej Kuročenko <kurochenko@mail.muni.cz>
  */
 @Entity
 @Table(name="department")
 public class Department implements Serializable {
     
     @Id
     @Column(name="iddepartment")
     @GeneratedValue
     private Integer iddepartment;
     
     @Column(name="name", unique=true)
     @Size(min = 1,max = 50)
     private String name;
 
     public Integer getIddepartment() {
         return iddepartment;
     }
 
     public void setIddepartment(Integer iddepartment) {
         this.iddepartment = iddepartment;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     @Override
     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (getClass() != obj.getClass()) {
             return false;
         }
         final Department other = (Department) obj;
         if (this.iddepartment != other.iddepartment && (this.iddepartment == null || !this.iddepartment.equals(other.iddepartment))) {
             return false;
         }
         if ((this.name == null) ? (other.name != null) : !this.name.equals(other.name)) {
             return false;
         }
         return true;
     }
 
     @Override
     public int hashCode() {
         int hash = 3;
         hash = 11 * hash + (this.iddepartment != null ? this.iddepartment.hashCode() : 0);
         hash = 11 * hash + (this.name != null ? this.name.hashCode() : 0);
         return hash;
     }
 
     @Override
     public String toString() {
         return "Department{" + "name=" + name + '}';
     }
 }
