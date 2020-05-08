 package bowling.data.domain;
 
 import javax.persistence.*;
 import java.io.Serializable;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Dan
  * Date: 3/17/12
  * Time: 12:01 AM
  */
 @Entity
 @Table(name = "Team")
 public class Team implements Serializable {
 
     private Long id;
     private String name;
    private String nbr;
     private String externalId;
     private League league;
 
     @Id
     @Column(name = "id")
     @GeneratedValue(strategy = GenerationType.AUTO)
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     @Column(name = "name")
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
    public String getNbr() {
        return nbr;
    }

    public void setNbr(String nbr) {
        this.nbr = nbr;
    }

     @Column(name = "external_id")
     public String getExternalId() {
         return externalId;
     }
 
     public void setExternalId(String externalId) {
         this.externalId = externalId;
     }
 
     @ManyToOne
     public League getLeague() {
         return league;
     }
 
     public void setLeague(League league) {
         this.league = league;
     }
 }
