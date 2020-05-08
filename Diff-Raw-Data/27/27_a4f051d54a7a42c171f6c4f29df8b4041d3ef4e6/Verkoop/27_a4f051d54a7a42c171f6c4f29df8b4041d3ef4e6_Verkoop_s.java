 package Model;
 
 import javax.persistence.*;
 import java.util.Date;
 import java.util.HashSet;
 import java.util.Set;
 
 /**
  * Package: Model
  * User: Mathias
  * Date: 16-10-12
  * Time: 21:21
  */
 
 @Entity
 @Table(name = "t_verkoop")
 public class Verkoop {
 
     @Id
     @GeneratedValue(strategy = GenerationType.IDENTITY)
     private Integer id;
 
     @Column
     private Date datum;
 
     @ManyToOne
     private VerkoopKanaal verkoopKanaal;
 
     @OneToMany(mappedBy = "verkoop")
     private Set<Ticket> tickets = new HashSet<Ticket>();
 
    @OneToMany(mappedBy = "isVanVerkoop")
     private Set<Tienbeurtenkaart> tienbeurtenkaarten = new HashSet<Tienbeurtenkaart>();
 
     @ManyToOne
     private Klant klant;
 
     public Verkoop() {
     }
 
     public Integer getId() {
         return id;
     }
 
     public void setId(Integer id) {
         this.id = id;
     }
 
     public Date getDatum() {
         return datum;
     }
 
     public void setDatum(Date datum) {
         this.datum = datum;
     }
 
     public VerkoopKanaal getVerkoopKanaal() {
         return verkoopKanaal;
     }
 
     public void setVerkoopKanaal(VerkoopKanaal verkoopKanaal) {
         this.verkoopKanaal = verkoopKanaal;
     }
 
     public Set<Ticket> getTickets() {
         return tickets;
     }
 
     public void setTickets(Set<Ticket> tickets) {
         this.tickets = tickets;
     }
 
     public Set<Tienbeurtenkaart> getTienbeurtenkaarten() {
         return tienbeurtenkaarten;
     }
 
     public void setTienbeurtenkaarten(Set<Tienbeurtenkaart> tienbeurtenkaarten) {
         this.tienbeurtenkaarten = tienbeurtenkaarten;
     }
 
     public Klant getKlant() {
         return klant;
     }
 
     public void setKlant(Klant klant) {
         this.klant = klant;
     }
 }
