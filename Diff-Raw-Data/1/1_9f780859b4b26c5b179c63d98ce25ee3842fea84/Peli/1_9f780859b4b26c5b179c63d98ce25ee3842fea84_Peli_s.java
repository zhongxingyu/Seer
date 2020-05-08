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
     
     //@Pattern(regexp="\\w+")
     @NotNull(message = "Pitäähän pelillä nyt nimi olla..")
     @Column(name = "Name")
     private int nimi;
     
     @ManyToOne(cascade = {CascadeType.ALL})
    @Column(name = "Genre")
     private Genre genre;
 
     public Integer getId() {
         return id;
     }
 
     public void setId(Integer peliId) {
         this.id = id;
     }
 
     public int getNimi() {
         return nimi;
     }
 
     public void setNimi(Integer nimi) {
         this.nimi = nimi;
     }
 
     public Genre getGenre() {
         return genre;
     }
 
     public void setGenre(Genre genre) {
         this.genre = genre;
     }
 }
