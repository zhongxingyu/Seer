 package models;
 
 import play.data.validation.Email;
 import play.data.validation.MaxSize;
 import play.data.validation.Required;
 import play.data.validation.URL;
 import play.db.jpa.Blob;
 import play.db.jpa.Model;
 import play.modules.search.Field;
 
 import javax.persistence.Entity;
 import javax.persistence.ManyToMany;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 import java.util.Date;
 import java.util.List;
 
 @Entity
 @Table(name = "organisme")
 public class Organisme extends Model {
 
     @ManyToOne
     public OrganismeMaster master;
 
     @Field(sortable=true)
     @Required
     @MaxSize(255)
     public String nom;
 
     public Blob logo;
 
     @MaxSize(255)
     public String siret;
 
     @Field
     @MaxSize(255)
     public String telephone;
 
     @Field
     @Email
     @MaxSize(255)
     public String email;
 
     @Field
     @Required
     @URL
     @MaxSize(255)
     public String siteweb;
 
     @Field
     @MaxSize(255)
     public String adresse;
 
     @Field
     @Required
     @MaxSize(255)
     public String ville;
 
     @Field
     @MaxSize(255)
     public String codePostal;
 
     @Field
     @Required
     @MaxSize(255)
     public String produit;
 
     @Field
     @MaxSize(255)
     public String description;
 
     @Field
     @Required
     @MaxSize(255)
     public String tags;
 
     public Date creation;
 
     @MaxSize(255)
     public String interlocuteur;
 
     @URL
     @MaxSize(255)
     public String twitter;
 
     @URL
     @MaxSize(255)
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
     @ManyToMany
     public List<OrganismeActivite> activites;
 
     @Field(joinField="libelle")
     @ManyToMany
     public List<OrganismeDataDomaine> dataDomaines;
 
 }
