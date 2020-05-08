 package models;
 
 import play.data.validation.Required;
 import play.db.jpa.Blob;
 import play.db.jpa.Model;
 import play.modules.search.Field;
 
 import javax.persistence.Entity;
import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import java.util.Date;
import java.util.List;
 
 @Entity
 public class Organisme extends Model {
 
     @ManyToOne
     public OrganismeMaster master;
 
     @Field(sortable=true)
     @Required
     public String nom;
 
     public Blob logo;
 
     public String siret;
 
     @Field
     public String telephone;
 
     @Field
     public String email;
 
     @Field
     public String siteweb;
 
     @Field
     public String adresse;
 
     @Field
     @Required
     public String ville;
 
     @Field
     public String codePostal;
 
     @Field
     public String produit;
 
     @Field
     public String description;
 
     @Field
     public String tags;
 
    @Field(sortable=true)
     public Date creation;
 
     public String interlocuteur;
 
     public String twitter;
 
     public String facebook;
 
     @Field(sortable=true)
     public Date created = new Date();
 
     public Double wsg_x;
 
     public Double wsg_y;
 
     @ManyToOne
     public User user;
 
     @Field(joinField="libelle")
     @Required
     @ManyToOne
     public OrganismeType type;
 
     @Field(joinField="libelle")
     @Required
     @ManyToOne
     public OrganismeNbSalarie nbSalarie;
 
     @Field(joinField="libelle")
     @Required
     @ManyToOne
     public OrganismeActivite activite;
 
 }
