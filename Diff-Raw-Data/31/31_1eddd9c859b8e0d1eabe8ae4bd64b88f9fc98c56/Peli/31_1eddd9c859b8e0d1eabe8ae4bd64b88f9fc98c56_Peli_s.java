 package Tsoha.domain;
 
 import java.io.Serializable;
 import java.util.List;
 import javax.persistence.*;
 import javax.validation.constraints.NotNull;
 import javax.validation.constraints.Pattern;
 import javax.validation.constraints.Size;
 
 @Entity
 public class Peli implements Serializable {
 
     @Id
     @GeneratedValue(strategy = GenerationType.TABLE)
     @Column(name = "ID")
     private Integer id;
     
     @NotNull(message="Kenttä ei saa olla tyhjä")
     @Pattern(regexp="[a-zA-Z0-9]+", 
    message="Virheellinen syöte")
    
     @Column(name = "Name")
     private String nimi;
         
     @Column(name = "Lainassa")
     private String lainassa;
     
     @ManyToOne(cascade = {CascadeType.REMOVE})
     private Genre genre;
     
     @OneToMany(cascade = {CascadeType.ALL})
     private List<Kommentti> kommentit;
     
     @OneToMany(cascade = {CascadeType.ALL})
     private List<Arvostelu> arvostelut;
 
     public String getLainassa() {
         return lainassa;
     }
 
     public void setLainassa(String lainassa) {
         this.lainassa = lainassa;
     }
 
     public List<Arvostelu> getArvostelut() {
         return arvostelut;
     }
 
     public void setArvostelut(List<Arvostelu> arvostelut) {
         this.arvostelut = arvostelut;
     }
     
     public Integer getId() {
         return id;
     }
 
     public List<Kommentti> getKommentit() {
         return kommentit;
     }
 
     public void setKommentit(List<Kommentti> kommentit) {
         this.kommentit = kommentit;
     }
 
     public void setId(Integer peliId) {
         this.id = peliId;
     }
 
     public String getNimi() {
         return nimi;
     }
 
     public void setNimi(String nimi) {
         this.nimi = nimi;
     }
 
     public Genre getGenre() {
         return genre;
     }
 
     public void setGenre(Genre genre) {
         this.genre = genre;
     }
 }
