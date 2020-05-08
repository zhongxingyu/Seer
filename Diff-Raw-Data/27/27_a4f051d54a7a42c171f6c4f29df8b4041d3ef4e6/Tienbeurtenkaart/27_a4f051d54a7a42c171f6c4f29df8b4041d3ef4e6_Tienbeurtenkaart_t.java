 package Model;
 
 import javax.persistence.*;
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * Package: Model
  * User: Mathias
  * Date: 16-10-12
  * Time: 20:58
  */
 @Entity
 @Table(name = "t_tienbeurtenkaart")
 public class Tienbeurtenkaart {
     @Id
     @GeneratedValue(strategy= GenerationType.IDENTITY)
     private Integer id;
 
     @ManyToOne
    private Verkoop verkoop;
 
     public Tienbeurtenkaart() {
     }
 
     public Integer getId() {
         return id;
     }
 
     public void setId(Integer id) {
         this.id = id;
     }
 
    public Verkoop getVerkoop() {
        return verkoop;
     }
 
    public void setVerkoop(Verkoop vanVerkoop) {
        verkoop = vanVerkoop;
     }
 
 }
