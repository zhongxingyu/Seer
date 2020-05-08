 package domain;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToOne;
 
 @Entity
 public class Child {
     
     @GeneratedValue
     @Id
     private long id;
     private String name;
     
     @OneToOne
     private Address address;
     @ManyToOne
     private SchoolClass schoolClass;
     
     public Child() {
         super();
     }
     
     public Child(String name, Address address, SchoolClass schoolClass) {
         super();
         this.name = name;
         this.address = address;
         this.schoolClass = schoolClass;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public Address getAddress() {
         return address;
     }
 
     public void setAddress(Address address) {
         this.address = address;
     }
 
     public long getId() {
         return id;
     }
 
     public void setId(long id) {
         this.id = id;
     }
 
     public SchoolClass getSchoolClass() {
         return schoolClass;
     }
 
     public void setSchoolClass(SchoolClass schoolClass) {
         this.schoolClass = schoolClass;
     }
 
     @Override
     public String toString() {
        return "Child [id=" + id + ", name=" + name + ", address=" + address + ", schoolClass=" + schoolClass + "]";
     }
 
     
 }
