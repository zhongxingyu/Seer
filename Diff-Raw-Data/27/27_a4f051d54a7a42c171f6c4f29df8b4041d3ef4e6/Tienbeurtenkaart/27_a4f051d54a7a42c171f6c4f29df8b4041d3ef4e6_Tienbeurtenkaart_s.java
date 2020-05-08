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
    private Verkoop isVanVerkoop;
 
     public Tienbeurtenkaart() {
     }
 
     public Integer getId() {
         return id;
     }
 
     public void setId(Integer id) {
         this.id = id;
     }
 
    public Verkoop getVanVerkoop() {
        return isVanVerkoop;
     }
 
    public void setVanVerkoop(Verkoop vanVerkoop) {
        isVanVerkoop = vanVerkoop;
     }
 
 }
