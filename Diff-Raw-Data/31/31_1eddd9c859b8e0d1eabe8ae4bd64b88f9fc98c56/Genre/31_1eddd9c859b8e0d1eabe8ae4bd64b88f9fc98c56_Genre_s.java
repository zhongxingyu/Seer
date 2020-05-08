 
 package Tsoha.domain;
 
 
 import java.io.Serializable;
 import java.util.List;
 import javax.persistence.*;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Pattern;
 
 @Entity
 public class Genre implements Serializable {
     
     @Id
     @GeneratedValue(strategy = GenerationType.TABLE)
     @Column(name="ID")
     private Integer id;
     
     @NotNull(message="Kenttä ei saa olla tyhjä")
     @Pattern(regexp="[a-zA-Z0-9]+", 
    message="Virheellinen syöte")
    @Column
     private String nimi;
     
     @OneToMany
     private List<Peli> pelit;
 
     public Integer getId() {
         return id;
     }
 
     public String getNimi() {
         return nimi;
     }
 
     public void setNimi(String nimi) {
         this.nimi = nimi;
     }
 
     public List<Peli> getPelit() {
         return pelit;
     }
 
     public void setPelit(List<Peli> pelit) {
         this.pelit = pelit;
     }
 
     public void setId(Integer id) {
         this.id = id;
     }
     
     public void addPeli(Peli peli) {
         if(!pelit.contains(peli)){
             pelit.add(peli);
         }
         peli.setGenre(this);
     }
     
     
 }
