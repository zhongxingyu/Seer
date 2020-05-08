 package Model;
 
 import org.hibernate.annotations.Cascade;
 
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
 @Table(name = "t_klant")
 public class Klant {
     @Id
     @GeneratedValue(strategy= GenerationType.IDENTITY)
     private Integer id;
 
     @Column
     private String naam;
 
     @Column
     private String straat;
 
     @Column
     private Integer huisnummer;
 
     @Column
     private String gemeente;
 
     @Column
     private Integer telefoonnummer;
 
    @OneToMany(mappedBy = "klant")
     private Set<Verkoop> verkopen = new HashSet<Verkoop>();
 
     public Klant() {
     }
 
     public Integer getId() {
         return id;
     }
 
     public void setId(Integer id) {
         this.id = id;
     }
 
     public String getNaam() {
         return naam;
     }
 
     public void setNaam(String naam) {
         this.naam = naam;
     }
 
     public String getStraat() {
         return straat;
     }
 
     public void setStraat(String straat) {
         this.straat = straat;
     }
 
     public Integer getHuisnummer() {
         return huisnummer;
     }
 
     public void setHuisnummer(Integer huisnummer) {
         this.huisnummer = huisnummer;
     }
 
     public String getGemeente() {
         return gemeente;
     }
 
     public void setGemeente(String gemeente) {
         this.gemeente = gemeente;
     }
 
     public Integer getTelefoonnummer() {
         return telefoonnummer;
     }
 
     public void setTelefoonnummer(Integer telefoonnummer) {
         this.telefoonnummer = telefoonnummer;
     }
 
     public Set<Verkoop> getVerkopen() {
         return verkopen;
     }
 
     public void setVerkopen(Set<Verkoop> verkopen) {
         this.verkopen = verkopen;
     }
 }
