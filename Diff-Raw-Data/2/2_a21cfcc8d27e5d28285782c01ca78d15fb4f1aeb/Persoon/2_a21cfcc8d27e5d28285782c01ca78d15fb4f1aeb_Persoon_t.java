 package supermarktmanager;
 
 import java.util.ArrayList;
 import java.util.List;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.ManyToMany;
 
 @Entity
 public abstract class Persoon {
     @Id
     @GeneratedValue
     private Long id;
     
     
     private String Name;
     
     @ManyToMany
    protected List<Locatie> werkplekken = new ArrayList<Locatie>();
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     public String getMemberName() {
         return Name;
     }
 
     public void setMemberName(String memberName) {
         this.Name = memberName;
     }
 }
