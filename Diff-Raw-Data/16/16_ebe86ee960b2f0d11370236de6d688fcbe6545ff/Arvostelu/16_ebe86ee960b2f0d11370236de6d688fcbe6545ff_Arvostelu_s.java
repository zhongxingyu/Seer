 package Tsoha.domain;
 
 import java.io.Serializable;
 import java.util.List;
 import javax.persistence.*;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Pattern;
 import javax.validation.constraints.Size;
 
 @Entity
 public class Arvostelu implements Serializable {
 
     @Id
     @GeneratedValue(strategy = GenerationType.TABLE)
     @Column(name = "ID")
     private Integer id;
     
     @NotNull(message="Kenttä ei saa olla tyhjä")
     @Pattern(regexp="([a-zA-Z0-9]+[\\s|\\.|\\?|\\!|\\,]*)+",
     message="Virheellinen syöte")
     @Column(name = "Fields")
     private String fields;
   
     @NotNull(message="Kenttä ei saa olla tyhjä")
    @Pattern(regexp="([a-zA-Z0-9]+[\\s|\\.|\\?|\\!|\\,]*)+",
     message="Virheellinen syöte")
     @Column(name = "Rating")
     private String rating;
     
     @ManyToOne
     private Peli peli;
 
     public Peli getPeli() {
         return peli;
     }
 
     public void setPeli(Peli peli) {
         this.peli = peli;
     }
     
     public Integer getId() {
         return id;
     }
 
     public String getViesti() {
         return fields;
     }
 
     public void setId(Integer id) {
         this.id = id;
     }
 
     public void setRating(String rating) {
         this.rating = rating;
     }
 
     public String getRating() {
         return rating;
     }
 
     public void setViesti(String viesti) {
         this.fields = viesti;
     }
 
     public String getFields() {
         return fields;
     }
 
     public void setFields(String fields) {
         this.fields = fields;
     }
 }
